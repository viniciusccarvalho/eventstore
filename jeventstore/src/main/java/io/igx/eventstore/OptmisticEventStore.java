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

import java.util.Collection;
import java.util.List;

import io.igx.eventstore.persistence.PersistentStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

/**
 * @author Vinicius Carvalho
 */
public  class OptmisticEventStore implements EventStore, CommitEvent{

	protected final PersistentStream persistentStream;
	protected final Collection<PipelineHook> hooks;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public OptmisticEventStore(PersistentStream persistentStream, Collection<PipelineHook> hooks) {
		if(persistentStream == null)
			throw new IllegalArgumentException("Persistence can not be null");
		this.persistentStream = persistentStream;
		this.hooks = hooks;
		//TODO how are we going to deal with pipeline hooks and decorator?
	}

	public Flux<Commit> from(String bucketId, String streamId, int minRevision, int maxRevision) {
		return persistentStream.from(bucketId,streamId,minRevision,maxRevision);
	}

	public Commit commit(CommitAttempt attempt) {
		for (PipelineHook hook: hooks) {
			logger.debug("Pushing commit '{}' to pre-commit hook of type '{}'.",attempt.getGuid(),hook.getClass().getName());
			if(hook.preCommit(attempt)) {
				continue;
			}
			logger.info("Pipeline hook of type '{}' rejected attempt '{}'.", hook.getClass().getName(), attempt.getGuid());
		}
		logger.info("Committing attempt '{}' which contains {} events to the underlying persistence engine.", attempt.getGuid(), attempt.getEvents().size());
		Commit commit = persistentStream.commit(attempt);
		for (PipelineHook hook: hooks) {
			logger.debug("Pushing commit '{}' to post-commit hook of type '{}'.", attempt.getGuid(), hook.getClass().getName());
			hook.postCommit(commit);
		}
		return commit;
	}

	@Override
	public PersistentStream getDelegate() {
		return persistentStream;
	}

	public EventStream create(String bucketId, String streamId) {
		logger.info("Creating stream '{}' in bucket '{}'.",streamId,bucketId);
		return new OptimisticEventStream(bucketId,streamId,this.persistentStream);
	}

	public EventStream open(String bucketId, String streamId, int minRevision, int maxRevision) {
		maxRevision = maxRevision <= 0 ? Integer.MAX_VALUE : maxRevision;
		logger.debug("Opening stream '{}' from bucket '{}' between revisions {} and {}.", streamId, bucketId, minRevision, maxRevision);
		return new OptimisticEventStream(bucketId, streamId, this.persistentStream, minRevision, maxRevision);
	}

	public EventStream open(Snapshot snapshot, int maxRevision) {
		if(snapshot == null){
			throw new IllegalArgumentException("Snapshot can't be null");
		}
		logger.debug("Opening stream '{}' with snapshot at {} up to revision {}.", snapshot.getStreamId(), snapshot.getStreamRevision(), maxRevision);
		maxRevision = maxRevision <= 0 ? Integer.MAX_VALUE : maxRevision;
		return new OptimisticEventStream(snapshot, this.persistentStream, maxRevision);
	}

	@Override
	public void startDispatchScheduler() {

	}
}
