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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import io.igx.eventstore.exceptions.ConcurrencyException;
import io.igx.eventstore.exceptions.DuplicateCommitException;
import io.igx.eventstore.exceptions.StreamNotFoundException;
import io.igx.eventstore.persistence.PersistentStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * @author Vinicius Carvalho
 */
public final class OptimisticEventStream implements EventStream{

	private Logger logger = LoggerFactory.getLogger(OptimisticEventStream.class);

	private final List<EventMessage> events;
	private final PersistentStream persistence;
	private final Map<String,Object> headers;

	private String bucketId;
	private String streamId;
	private AtomicLong streamRevision = new AtomicLong(0L);
	private Long commitSequence = 0L;
	private Long minRevision = Long.MIN_VALUE;
	private Long maxRevision = Long.MAX_VALUE;

	public OptimisticEventStream(String bucketId, String streamId, PersistentStream persistence){
		this(bucketId,streamId,persistence,Long.MIN_VALUE,Long.MAX_VALUE);
	}

	public OptimisticEventStream(String bucketId, String streamId, PersistentStream persistence, Long minRevision, Long maxRevision){
		this.bucketId = bucketId;
		this.streamId = streamId;
		this.persistence = persistence;
		this.events = new LinkedList<EventMessage>();
		this.headers = new HashMap<String, Object>();
		this.commitSequence = persistence.getCurrentCommitSequence(bucketId,streamId,minRevision,maxRevision);
		this.streamRevision.set(persistence.getCurrentStreamRevision(bucketId,streamId,minRevision,maxRevision));

		//TODO: Find another way to throw error (count on db?)
		//if( minRevision > 0 && committed.size() == 0)
		//	throw new StreamNotFoundException();
	}

	public OptimisticEventStream(Snapshot snapshot, PersistentStream persistence, Long maxRevision){
		this(snapshot.getBucketId(),snapshot.getStreamId(),persistence,Long.MIN_VALUE,maxRevision);
	}


	public String getBucketId() {
		return bucketId;
	}

	public String getStreamId() {
		return streamId;
	}

	public Long getStreamRevision() {
		return streamRevision.get();
	}

	public Long getCommitSequence() {
		return commitSequence;
	}

	public Flux<EventMessage> getCommitedEvents() {
		return persistence
				.from(this.bucketId,this.streamId,this.minRevision,this.maxRevision)
				.flatMap(commit -> {return Flux.fromIterable(commit.getEvents());});
	}

	public Map<String, Object> getCommitedHeaders() {
		return persistence.from(this.bucketId, this.streamId, this.minRevision,this.maxRevision)
				.flatMap(commit -> {return Flux
						.fromIterable(commit
								.getHeaders()
								.entrySet());})
				.collect(Collectors
						.toMap(Map.Entry::getKey,Map.Entry::getValue))
				.get();
	}

	public List<EventMessage> getUncommitedEvents() {
		return Collections.unmodifiableList(events);
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

		if(!hasChanges())
			return;
		try{
			persistChanges(guid);
		} catch (ConcurrencyException ex){
			logger.info("The underlying stream {} has changed since the last known commit, refreshing the stream.",streamId);
			this.commitSequence = persistence.getCurrentCommitSequence(bucketId,streamId,streamRevision.get(),Long.MAX_VALUE);
			this.streamRevision.set(persistence.getCurrentStreamRevision(bucketId,streamId,streamRevision.get(),Long.MAX_VALUE));
			throw ex;
		}

	}



	private void persistChanges(UUID guid){
		CommitAttempt attempt = buildCommitAttempt(guid);
		logger.debug("Pushing attempt {} on stream {} to the underlying store.",guid,streamId);
		Commit commit = persistence.commit(attempt);

		this.commitSequence = commit.getCommitSequence();
		populateStream(streamRevision.incrementAndGet(),attempt.streamRevision, Collections.singletonList(commit));
		clearChanges();
	}

	public void clearChanges() {
		logger.debug("Clearing all uncommitted changes on stream {}",streamId);
		events.clear();
		headers.clear();
	}

	/**
	 * @deprecated: Instead of going for an eager approach, an Stream is now lazy, we won't load commits into memory.
	 * Duplicates can now only happen during a commit to the backing store
	 *
	 * @param minRevision
	 * @param maxRevision
	 * @param commits
	 */
	private void populateStream(Long minRevision, Long maxRevision, Collection<Commit> commits){
		for(Commit commit : commits){
			logger.debug("Adding commit {} with {} events to stream {}",commit.getGuid(),commit.getEvents().size(),streamId);
			this.commitSequence = commit.getCommitSequence();
			Long currentRevision = commit.getStreamRevision() - commit.getEvents().size() + 1;
			if (currentRevision > maxRevision)
			{
				return;
			}
		}
	}

	/**
	 * @deprecated
	 * @param commit
	 */
	private void copyToCommitedHeaders(Commit commit){
		//commitedHeaders.putAll(commit.getHeaders());
	}

	/**
	 * @deprecated
	 * @param minRevision
	 * @param maxRevision
	 * @param currentRevision
	 * @param commit
	 */
	private void copyToEvents(Long minRevision, Long maxRevision, Long currentRevision, Commit commit){
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
			//committed.add(event);
			streamRevision.getAndAdd(currentRevision+1) ;
		}
	}

	private CommitAttempt buildCommitAttempt(UUID guid){
		logger.debug("Building a commit attempt {} on stream {}.",guid,streamId);
		CommitAttempt attempt = new CommitAttempt(bucketId,
				streamId,
				streamRevision.get() + events.size(),
				guid,
				commitSequence+1,
				System.currentTimeMillis(),
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
