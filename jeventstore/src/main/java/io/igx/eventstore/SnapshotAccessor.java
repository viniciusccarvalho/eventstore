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

import io.igx.eventstore.persistence.StorageException;
import io.igx.eventstore.persistence.StorageUnavailableException;
import io.igx.eventstore.persistence.StreamHead;
import reactor.core.publisher.Flux;

/**
 * @author Vinicius Carvalho
 * From https://github.com/NEventStore/NEventStore/blob/master/src/NEventStore/IAccessSnapshots.cs, renamed to be more java(ish)
 *
 * Indicates the ability to get or retrieve a snapshot for a given stream.
 * Instances of this class must be designed to be multi-thread safe such that they can be shared between threads.
 */
public interface SnapshotAccessor {

	/**
	 * Gets the most recent snapshot which was taken on or before the revision indicated.
	 * @param bucketId The value which uniquely identifies bucket the stream belongs to.
	 * @param streamId The stream to be searched for a snapshot.
	 * @param maxRevision The maximum revision possible for the desired snapshot.
	 * @throws StorageException
	 * @throws StorageUnavailableException
	 * @return If found, it returns the snapshot; otherwise null is returned.
	 */
	Snapshot getSnapshot(String bucketId, String streamId, int maxRevision);

	/**
	 * Adds the snapshot provided to the stream indicated.
	 * @param snapshot The snapshot to save.
	 * @return If the snapshot was added, returns true; otherwise false.
	 * @throws StorageException
	 * @throws StorageUnavailableException
	 */
	boolean add(Snapshot snapshot);

	/**
	 * Gets identifiers for all streams whose head and last snapshot revisions differ by at least the threshold specified.
	 * @param bucketId The value which uniquely identifies bucket the stream belongs to.
	 * @param maxThreshold The maximum difference between the head and most recent snapshot revisions.
	 * @throws StorageException
	 * @throws StorageUnavailableException
	 * @return The streams for which the head and snapshot revisions differ by at least the threshold specified.
	 */
	Flux<StreamHead> getStreamsToSnapshot(String bucketId, int maxThreshold);
}
