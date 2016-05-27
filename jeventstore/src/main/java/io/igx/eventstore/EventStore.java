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

import io.igx.eventstore.exceptions.StreamNotFoundException;
import io.igx.eventstore.persistence.PersistentStream;
import io.igx.eventstore.persistence.StorageUnavailableException;
import io.igx.eventstore.persistence.StorageException;

/**
 * @author Vinicius Carvalho
 * Renamed from https://github.com/NEventStore/NEventStore/blob/master/src/NEventStore/IStoreEvents.cs
 * Indicates the ability to store and retreive a stream of events.
 */
public interface EventStore {

	/**
	 * Returns the underling PersistentStreams implementation for this EventStore. Renamed to resemble EntityManager naming
	 * @return the PersistentStream implementation
	 */
	PersistentStream getDelegate();

	/**
	 * Creates a new Stream
	 * @param bucketId The value which uniquely identifies bucket the stream belongs to.
	 * @param streamId The value which uniquely identifies the stream within the bucket to be created.
	 * @return an empty Stream
	 */
	EventStream create(String bucketId, String streamId);

	/**
	 * Reads the stream indicated from the minimum revision specified up to the maximum revision specified or creates
	 * an empty stream if no commits are found and a minimum revision of zero is provided.
	 *
	 * @param bucketId The value which uniquely identifies bucket the stream belongs to.
	 * @param streamId The value which uniquely identifies the stream within the bucket to be created.
	 * @param minRevision The minimum revision of the stream to be read.
	 * @param maxRevision The maximum revision of the stream to be read.
	 * @throws StorageException
	 * @throws StorageUnavailableException
	 * @throws StreamNotFoundException
	 * @return A series of committed events represented as a stream.
	 */
	EventStream open(String bucketId, String streamId, int minRevision, int maxRevision);

	/**
	 * Reads the stream indicated from the point of the snapshot forward until the maximum revision specified.
	 *
	 * @param snapshot The snapshot of the stream to be read
	 * @param maxRevision The maximum revision of the stream to be read.
	 * @throws StorageException
	 * @throws StorageUnavailableException
	 * @return A series of committed events represented as a stream.
	 */
	EventStream open(Snapshot snapshot, int maxRevision);

	/**
	 * Starts the dispatch scheduler. If the dispatch scheduler is set to startup automatically, this will not have any affect.
	 */
	void startDispatchScheduler();
}
