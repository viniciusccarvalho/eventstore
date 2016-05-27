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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import io.igx.eventstore.exceptions.ConcurrencyException;
import io.igx.eventstore.exceptions.DuplicateCommitException;
import io.igx.eventstore.exceptions.StreamNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vinicius Carvalho
 */
public final class OptimisticEventStream implements EventStream{

	private final Collection<EventMessage<?>> committed;
	private final Collection<UUID> identifiers;
	private final Collection<EventMessage> events;
	private final CommitEvent persistence;
	private final Map<String,Object> commitedHeaders;
	private final Map<String,Object> headers;
	private Logger logger = LoggerFactory.getLogger(OptimisticEventStream.class);
	private String bucketId;
	private String streamId;
	private Integer streamRevision;
	private Integer commitSequence;

	public OptimisticEventStream(String bucketId, String streamId, CommitEvent persistence){

		this.bucketId = bucketId;
		this.streamId = streamId;
		this.persistence = persistence;
		this.committed = new LinkedList<EventMessage<?>>();
		this.identifiers = new LinkedList<UUID>();
		this.events = new LinkedList<EventMessage>();
		this.commitedHeaders = new HashMap<String, Object>();
		this.headers = new HashMap<String, Object>();
	}

	public OptimisticEventStream(String bucketId, String streamId, CommitEvent persistence, int minRevision, int maxRevision){
		this(bucketId,streamId,persistence);
		Collection<Commit> commits =persistence.from(bucketId,streamId,minRevision,maxRevision);
		populateStream(minRevision,maxRevision,commits);
		if( minRevision > 0 && committed.size() == 0)
			throw new StreamNotFoundException();
	}

	public OptimisticEventStream(Snapshot snapshot, CommitEvent persistence, int maxRevision){
		this(snapshot.getBucketId(),snapshot.getStreamId(),persistence);
		Collection<Commit> commits = persistence.from(snapshot.getBucketId(),snapshot.getStreamId(),snapshot.getStreamRevision(),maxRevision);
		populateStream(snapshot.getStreamRevision()+1,maxRevision,commits);
		streamRevision = snapshot.getStreamRevision() + committed.size();
	}


	public String getBucketId() {
		return bucketId;
	}

	public String getStreamId() {
		return streamId;
	}

	public Integer getStreamRevision() {
		return streamRevision;
	}

	public Integer getCommitSequence() {
		return commitSequence;
	}

	public Collection<EventMessage<?>> getCommitedEvents() {
		return Collections.unmodifiableCollection(committed);
	}

	public Map<String, Object> getCommitedHeaders() {
		return commitedHeaders;
	}

	public Collection<EventMessage> getUncommitedEvents() {
		return Collections.unmodifiableCollection(events);
	}

	public Map<String, Object> getUncommitedHeaders() {
		return headers;
	}

	public void add(EventMessage uncommittedEvent) {
		if(uncommittedEvent == null || uncommittedEvent.getBody() == null)
			return;
		logger.debug("Appending uncommitted event to stream {}",streamId);
		events.add(uncommittedEvent);
	}

	public void commitChanges(UUID guid) {
		logger.debug("Attempting to commit all changes on stream {} to the underlying store.",streamId);
		if(identifiers.contains(guid)){
			throw new DuplicateCommitException();
		}
		if(!hasChanges())
			return;
		try{
			persistChanges(guid);
		} catch (ConcurrencyException ex){
			logger.info("The underlying stream {} has changed since the last known commit, refreshing the stream.",streamId);
			Collection<Commit> commits = persistence.from(bucketId,streamId, streamRevision +1,Integer.MAX_VALUE);
			populateStream(streamRevision + 1,Integer.MAX_VALUE,commits);
			throw ex;
		}

	}

	public void clearChanges() {
		logger.debug("Clearing all uncommitted changes on stream {}",streamId);
		events.clear();
		headers.clear();
	}

	private void persistChanges(UUID guid){
		CommitAttempt attempt = buildCommitAttempt(guid);
		logger.debug("Pushing attempt {} on stream {} to the underlying store.",guid,streamId);
		Commit commit = persistence.commit(attempt);
		populateStream(streamRevision+1,attempt.streamRevision, Collections.singletonList(commit));
		clearChanges();
	}

	private void populateStream(int minRevision, int maxRevision, Collection<Commit> commits){
		for(Commit commit : commits){
			logger.debug("Adding commit {} with {} events to stream {}",commit.getGuid(),commit.getEvents().size(),streamId);
			identifiers.add(commit.getGuid());
			this.commitSequence = commit.getCommitSequence();
			int currentRevision = commit.getStreamRevision() - commit.getEvents().size() + 1;
			if (currentRevision > maxRevision)
			{
				return;
			}
			copyToCommitedHeaders(commit);
			copyToEvents(minRevision,maxRevision,currentRevision,commit);
		}
	}

	private void copyToCommitedHeaders(Commit commit){
		commitedHeaders.putAll(commit.getHeaders());
	}

	private void copyToEvents(int minRevision, int maxRevision, int currentRevision, Commit commit){
		for(EventMessage event : commit.getEvents()){
			if (currentRevision > maxRevision)
			{
				logger.debug("Ignoring some events on commit {} of stream {} because they go beyond revision {}.", commit.getGuid(), streamId, maxRevision);
				break;
			}
			if (currentRevision++ < minRevision)
			{
				logger.debug("Ignoring some events on commit {} of stream {} because they starting before revision {}.", commit.getGuid(), streamId, maxRevision);
				continue;
			}
			events.add(event);
			streamRevision = currentRevision +1;
		}
	}

	private CommitAttempt buildCommitAttempt(UUID guid){
		logger.debug("Building a commit attempt {} on stream {}.",guid,streamId);
		CommitAttempt attempt = new CommitAttempt(bucketId,
				streamId,
				streamRevision + events.size(),
				guid,
				commitSequence+1,
				LocalDateTime.now(),
				headers,
				events);
		return attempt;
	}

	private boolean hasChanges(){
		if(events.size() > 0)
			return true;
		logger.warn("There are no outstanding changes to be committed stream {}.",streamId);
		return false;
	}
}
