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
import java.util.Map;
import java.util.UUID;

import io.igx.eventstore.exceptions.ConcurrencyException;
import io.igx.eventstore.exceptions.DuplicateCommitException;
import io.igx.eventstore.persistence.StorageException;
import io.igx.eventstore.persistence.StorageUnavailableException;
import reactor.core.publisher.Flux;

/**
 * @author Vinicius Carvalho
 * Indicates the ability to track a series of events and commit them to durable storage.
 * Instances of this class are single threaded and should not be shared between threads.
 */
public interface EventStream {
	/**
	 *
	 * @return the value which identifies bucket to which the the stream belongs.
	 */
	String getBucketId();

	/**
	 *
	 * @return the value which uniquely identifies the stream to which the stream belongs.
	 */
	String getStreamId();

	/**
	 *
	 * @return the value which indiciates the most recent committed revision of event stream.
	 */
	Long getStreamRevision();

	/**
	 *
	 * @return the value which indicates the most recent committed sequence identifier of the event stream.
	 */
	Long getCommitSequence();

	/**
	 *
	 * @return a flux of events which have been successfully persisted to durable storage.
	 */
	Flux<EventMessage> getCommitedEvents();

	/**
	 *
	 * @return the collection of committed headers associated with the stream.
	 */
	Map<String,Object> getCommitedHeaders();

	/**
	 *
	 * @return a flux of yet-to-be-committed events that have not yet been persisted to durable storage.
	 */
	List<EventMessage> getUncommitedEvents();

	/**
	 *
	 * @return the collection of yet-to-be-committed headers associated with the uncommitted events.
	 */
	Map<String,Object> getUncommitedHeaders();

	/**
	 *  Adds the event messages provided to the session to be tracked.
	 * @param uncommittedEvent The event to be tracked.
	 */
	void add(EventMessage uncommittedEvent);

	/**
	 * Commits the changes to durable storage.
	 * @param guid The value which uniquely identifies the commit
	 * @throws ConcurrencyException
	 * @throws StorageUnavailableException
	 * @throws StorageException
	 * @throws DuplicateCommitException
	 */
	void commitChanges(UUID guid);

	/**
	 *  Clears the uncommitted changes.
	 */
	void clearChanges();
}
