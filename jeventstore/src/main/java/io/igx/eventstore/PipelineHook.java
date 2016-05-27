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

/**
 * Provides the ability to hook into the pipeline of persisting a commit.
 * Instances of this class must be designed to be multi-thread safe such that they can be shared between threads.
 */
public interface PipelineHook {

	/**
	 * Hooks into the selection pipeline just prior to the commit being returned to the caller.
	 * @param committed The commit to be filtered.
	 * @return If successful, returns a populated commit; otherwise returns null.
	 */
	Commit select(Commit committed);

	/**
	 * Hooks into the commit pipeline prior to persisting the commit to durable storage.
	 * @param attempt The attempt to be committed.
	 * @return If processing should continue, returns true; otherwise returns false.
	 * @throws java.util.ConcurrentModificationException
	 * @throws io.igx.eventstore.persistence.StorageException
	 */
	boolean preCommit(CommitAttempt attempt);

	/**
	 * Hooks into the commit pipeline just after the commit has been *successfully* committed to durable storage.
	 * @param committed The commit which has been persisted.
	 */
	void postCommit(Commit committed);

	/**
	 * Invoked when a bucket has been purged. If buckedId is null, then all buckets have been purged.
	 *
	 * @param bucketId The bucket Id that has been purged. Null when all buckets have been purged.
	 */
	void onPurge(String bucketId);

	/**
	 *  Invoked when a stream has been deleted.
	 * @param bucketId The bucket Id from which the stream whch has been deleted.
	 * @param streamId The stream Id of the stream which has been deleted.
	 */
	void onDeleteStream(String bucketId, String streamId);

}
