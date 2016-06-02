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
import java.util.Map;
import java.util.UUID;


/**
 * @author Vinicius Carvalho
 *
 * Represents a series of events which have been fully committed as a single unit and which apply to the stream indicated.
 */
public interface Commit {
	/**
	 *
	 * @return  the value which identifies bucket to which the the stream and the the commit belongs.
	 */
	String getBucketId();

	/**
	 *
	 * @return  the value which uniquely identifies the stream to which the commit belongs.
	 */
	String getStreamId();

	/**
	 *
	 * @return  the value which indicates the revision of the most recent event in the stream to which this commit applies.
	 */
	Integer getStreamRevision();

	/**
	 *
	 * @return   the value which uniquely identifies the commit within the stream.
	 */
	UUID getGuid();

	/**
	 *
	 * @return the value which indicates the sequence (or position) in the stream to which this commit applies.
	 */
	Integer getCommitSequence();

	/**
	 *
	 * @return the point in time at which the commit was persisted.
	 */
	Long getCommitStamp();

	/**
	 *
	 * @return the metadata which provides additional, unstructured information about this commit.
	 */
	Map<String,Object> getHeaders();

	/**
	 *
	 * @return the collection of event messages to be committed as a single unit.
	 */
	Collection<EventMessage> getEvents();

	/**
	 *
	 * @return The checkpoint that represents the storage level order.
	 */
	String getCheckpointToken();

}
