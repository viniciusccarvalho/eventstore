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

package io.igx.eventstore.persistence;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import io.igx.eventstore.Checkpoint;
import io.igx.eventstore.Commit;
import io.igx.eventstore.CommitEvent;
import io.igx.eventstore.SnapshotAccessor;
import reactor.core.publisher.Flux;

/**
 * @author Vinicius Carvalho
 * Renamed from https://github.com/NEventStore/NEventStore/blob/master/src/NEventStore/Persistence/IPersistStreams.cs
 *
 * Indicates the ability to adapt the underlying persistence infrastructure to behave like a stream of events.
 * Instances of this class must be designed to be multi-thread safe such that they can be shared between threads.
 */
public interface PersistentStream extends CommitEvent, SnapshotAccessor{
	/**
	 * TODO: It may not make any sense given that we don't have an IDisposable interface in Java
	 * @return value indicating whether this instance has been disposed of.
	 */
	boolean isDisposed();

	/**
	 * Initializes and prepares the storage for use, if not already performed.
	 * @throws StorageException
	 * @throws StorageUnavailableException
	 */
	void initialize();

	/**
	 * Gets all commits on or after from the specified starting time.
	 * @param bucketId The value which uniquely identifies bucket the stream belongs to.
	 * @param start The point in time at which to start.
	 * @return All commits that have occurred on or after the specified starting time.
	 * @throws StorageException
	 * @throws StorageUnavailableException
	 */
	Flux<Commit> from(String bucketId, LocalDateTime start);

	/**
	 * Gets all commits after the specified checkpoint. Use null to get from the beginning.
	 * @param checkpointToken The checkpoint token.
	 * @return a Flux with all commits
	 */
	Flux<Commit> from(String checkpointToken);

	/**
	 * Gets all commits after from the specified checkpoint. Use null to get from the beginning.
	 * @param bucketId The value which uniquely identifies bucket the stream belongs to.
	 * @param checkpointToken The checkpoint token.
	 * @return a Flux with all commits
	 */
	Flux<Commit> from(String bucketId, String checkpointToken);

	/**
	 * Gets all commits on or after from the specified starting time and before the specified end time.
	 * @param bucketId The value which uniquely identifies bucket the stream belongs to.
	 * @param start The point in time at which to start
	 * @param stop The point in time at which to end.
	 * @throws StorageException
	 * @throws StorageUnavailableException
	 * @return a Flux with all commits that have occurred on or after the specified starting time and before the end time.
	 *
	 */
	Flux<Commit> from(String bucketId, LocalDateTime start, LocalDateTime stop);

	/**
	 * Gets a checkpoint object that is comparable with other checkpoints from this storage engine.
	 * @param checkpointToken The checkpoint token
	 * @return A checkpoint instance
	 */
	Checkpoint getCheckPoint(String checkpointToken);

	/**
	 * @throws StorageException
	 * @throws StorageUnavailableException
	 * @return a flux of commits that has not yet been dispatched.
	 */
	Flux<Commit> getUndispatchedCommits();

	/**
	 *  Marks the commit specified as dispatched.
	 * @param commit The commit to be marked as dispatched.
	 * @throws StorageException
	 * @throws StorageUnavailableException
	 */
	void markCommitAsDispatched(Commit commit);

	/**
	 * Completely DESTROYS the contents of ANY and ALL streams that have been successfully persisted.  Use with caution.
	 */
	void purge();

	/**
	 * Completely DESTROYS the contents of ANY and ALL streams that have been successfully persisted
	 * in the specified bucket.  Use with caution.
	 * @param bucketId
	 */
	void purge(String bucketId);

	/**
	 * Completely DESTROYS the contents and schema (if applicable) containting ANY and ALL streams that have been
	 * successfully persisted
	 * in the specified bucket.  Use with caution.
	 */
	void drop();

	/**
	 * Deletes a stream.
	 * @param bucketId The bucket Id from which the stream is to be deleted.
	 * @param streamId The stream Id of the stream that is to be deleted.
	 */
	void deleteStream(String bucketId, String streamId);

	/**
	 * Finds the current maximum revision a stream is located at.
	 * This method is not found in the .net version
	 * @param bucketId
	 * @param streamId
	 * @param minRevision
	 * @param maxRevision
	 * @return
	 */
	Integer getCurrentStreamRevision(String bucketId, String streamId, Integer minRevision, Integer maxRevision);

	/**
	 *
	 * @param bucketId
	 * @param streamId
	 * @param minRevision
	 * @param maxRevision
	 * @return
	 */
	Integer getCurrentCommitSequence(String bucketId, String streamId, Integer minRevision, Integer maxRevision);

}
