/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Jonathan Oliver, Jonathan Matheus, Damian Hickey and contributors, 2016 Vinicius Carvalho
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.igx.eventstore;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.igx.eventstore.persistence.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vinicius Carvalho
 * Tracks the heads of streams to reduce latency by avoiding roundtrips to storage.
 */
public class OptmisticPipelineHook implements PipelineHook {

	private final static int MAX_STREAMS_TO_TRACK = 128;
	private Integer maxStreamsToTrack;
	private Logger logger = LoggerFactory.getLogger(OptmisticPipelineHook.class);
	private final Map<HeadKey,Commit> heads;
	private final LinkedList<HeadKey> maxItemsToTrack;

	public OptmisticPipelineHook(){
		this(MAX_STREAMS_TO_TRACK);
	}

	public OptmisticPipelineHook(Integer maxStreamsToTrack){
		this.maxStreamsToTrack = maxStreamsToTrack;
		this.heads = new ConcurrentHashMap<HeadKey, Commit>();
		this.maxItemsToTrack = new LinkedList<HeadKey>();
	}

	public Commit select(Commit committed) {
		track(committed);
		return committed;
	}

	public boolean preCommit(CommitAttempt attempt) {
		logger.debug("Verifying that no other commits have succeed on the stream '{}'.",attempt.getStreamId());
		Commit head = getStreamHead(getHeadKey(attempt));

		if (head == null)
		{
			return true;
		}

		if(head.getCommitSequence() >= attempt.getCommitSequence()){
			throw new ConcurrentModificationException();
		}

		if(head.getStreamRevision() >= attempt.getStreamRevision()){
			throw new ConcurrentModificationException();
		}

		if(head.getCommitSequence() <= attempt.getCommitSequence() -1){
			throw new StorageException();
		}

		if(head.getStreamRevision() <= attempt.getStreamRevision() - attempt.getEvents().size()){
			throw new StorageException();
		}

		logger.debug("No other commits have been discovered that conflict for stream '{}'.",attempt.getStreamId());
		return true;
	}

	public void postCommit(Commit committed) {
		track(committed);
	}

	public void onPurge(final String bucketId) {
		synchronized (maxItemsToTrack){
			if(bucketId == null){
				heads.clear();
				maxItemsToTrack.clear();
				return;
			}
			List<HeadKey> headKeys = heads.keySet().stream().filter(k -> k.getBucketId() == bucketId).collect(Collectors.<HeadKey>toList());
			headKeys.forEach(headKey -> removeHead(headKey));
		}
	}

	public void onDeleteStream(String bucketId, String streamId) {
		synchronized (maxItemsToTrack){
			removeHead(new HeadKey(bucketId,streamId));
		}
	}

	public void track(Commit committed){
		if(committed == null)
			return;

		synchronized (maxItemsToTrack){
			updateStreamHead(committed);
			trackUpToCapacity(committed);
		}

	}


	public boolean contains(Commit attempt){
		return getStreamHead(getHeadKey(attempt)) != null;
	}



	private void updateStreamHead(Commit committed){
		HeadKey headKey = getHeadKey(committed);
		Commit head = getStreamHead(headKey);

		if(alreadyTracked(head)){
			maxItemsToTrack.remove(headKey);
		}

		head = (head == null) ? head : committed;
		head = head.getStreamRevision() > committed.getStreamRevision() ? head : committed;

		heads.put(headKey,head);
	}


	private static boolean alreadyTracked(Commit head)
	{
		return head != null;
	}

	private void removeHead(HeadKey headKey){
		heads.remove(headKey);
		HeadKey node = maxItemsToTrack.stream().filter(headKey1 -> headKey1.equals(headKey)).findFirst().get();
		if (node != null){
			maxItemsToTrack.remove(node);
		}
	}


	private void trackUpToCapacity(Commit committed){
		logger.debug("Tracking commit {} on stream '{}'",committed.getCommitSequence(),committed.getStreamId());
		maxItemsToTrack.addFirst(getHeadKey(committed));
		if(maxItemsToTrack.size() <= maxStreamsToTrack){
			return;
		}
		HeadKey expired = maxItemsToTrack.getLast();
		logger.debug("Purging all commits on stream '{}' from tracking.",expired);
		heads.remove(expired);
		maxItemsToTrack.removeLast();
	}


	private HeadKey getHeadKey(Commit commit){
		return new HeadKey(commit.getBucketId(),commit.getStreamId());
	}
	private HeadKey getHeadKey(CommitAttempt attempt){
		return new HeadKey(attempt.getBucketId(),attempt.getStreamId());
	}

	private Commit getStreamHead(HeadKey attempt){
		synchronized (maxItemsToTrack){
			return heads.get(attempt);
		}
	}




	private final class HeadKey {
		private final String bucketId;
		private final String streamId;

		public HeadKey(String bucketId, String streamId) {
			this.bucketId = bucketId;
			this.streamId = streamId;
		}


		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			HeadKey headKey = (HeadKey) o;

			if (bucketId != null ? !bucketId.equals(headKey.bucketId) : headKey.bucketId != null) return false;
			return streamId != null ? streamId.equals(headKey.streamId) : headKey.streamId == null;

		}

		@Override
		public int hashCode() {
			int result = bucketId != null ? bucketId.hashCode() : 0;
			result = 31 * result + (streamId != null ? streamId.hashCode() : 0);
			return result;
		}

		public String getBucketId() {
			return bucketId;
		}

		public String getStreamId() {
			return streamId;
		}

		@Override
		public String toString() {
			final StringBuffer sb = new StringBuffer("HeadKey{");
			sb.append("bucketId='").append(bucketId).append('\'');
			sb.append(", streamId='").append(streamId).append('\'');
			sb.append('}');
			return sb.toString();
		}
	}

}
