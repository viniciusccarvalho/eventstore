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

import io.igx.eventstore.exceptions.ConcurrencyException;
import io.igx.eventstore.persistence.StorageException;
import io.igx.eventstore.persistence.StorageUnavailableException;
import reactor.core.publisher.Flux;

/**
 * @author Vinicius Carvalho
 * Indicates the ability to commit events and access events to and from a given stream.
 * Instances of this class must be designed to be multi-thread safe such that they can be shared between threads.
 */
public interface CommitEvent {

	//TODO Consider using Flux<Commit> instead

	/**
	 * Gets the corresponding commits from the stream indicated starting at the revision specified until the
	 * end of the stream sorted in ascending order--from oldest to newest.
	 * @param bucketId The value which uniquely identifies bucket the stream belongs to.
	 * @param streamId The stream from which the events will be read.
	 * @param minRevision The minimum revision of the stream to be read.
	 * @param maxRevision The maximum revision of the stream to be read.
	 * @throws StorageException
	 * @throws StorageUnavailableException
	 * @return A series of committed events from the stream specified sorted in ascending order.
	 */
	Flux<Commit> from(String bucketId, String streamId, int minRevision, int maxRevision);

	/**
	 * Writes the to-be-commited events provided to the underlying persistence mechanism.
	 * @param attempt The series of events and associated metadata to be commited.
	 * @throws StorageException
	 * @throws StorageUnavailableException
	 * @throws ConcurrencyException
	 * @return The commited value to the underling storage engine
	 *
	 */
	Commit commit(CommitAttempt attempt);


}
