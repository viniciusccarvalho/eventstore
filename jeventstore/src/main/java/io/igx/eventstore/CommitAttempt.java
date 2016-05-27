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
import java.util.Map;
import java.util.UUID;

/**
 * @author Vinicius Carvalho
 */
public class CommitAttempt{

	final String streamId;
	final String bucketId;
	final Integer streamRevision;
	final UUID guid;
	final Integer commitSequence;
	final LocalDateTime commitStamp;
	final Map<String,Object> headers;
	final Collection<EventMessage> events;


	public CommitAttempt(String streamId, Integer streamRevision, UUID guid, Integer commitSequence, LocalDateTime commitStamp, Map<String, Object> headers, Collection<EventMessage> events){
		this(Bucket.DEFAULT,streamId,streamRevision,guid,commitSequence,commitStamp,headers,events);
	}

	/**
	 *  Initializes a new instance of the Commit class.
	 * @param bucketId The value which identifies bucket to which the the stream and the the commit belongs
	 * @param streamId The value which uniquely identifies the stream in a bucket to which the commit belongs.
	 * @param streamRevision The value which indicates the revision of the most recent event in the stream to which this commit applies.
	 * @param guid  The value which uniquely identifies the commit within the stream.
	 * @param commitSequence The value which indicates the sequence (or position) in the stream to which this commit applies.
	 * @param commitStamp The point in time at which the commit was persisted.
	 * @param headers The metadata which provides additional, unstructured information about this commit.
	 * @param events The collection of event messages to be committed as a single unit.
	 */
	public CommitAttempt(String bucketId, String streamId, Integer streamRevision, UUID guid, Integer commitSequence, LocalDateTime commitStamp, Map<String, Object> headers, Collection<EventMessage> events) {
		this.streamId = streamId;
		this.bucketId = bucketId;
		this.streamRevision = streamRevision;
		this.guid = guid;
		this.commitSequence = commitSequence;
		this.commitStamp = commitStamp;
		this.headers = headers;
		this.events = events;
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

	public UUID getGuid() {
		return guid;
	}

	public Integer getCommitSequence() {
		return commitSequence;
	}

	public LocalDateTime getCommitStamp() {
		return commitStamp;
	}

	public Map<String, Object> getHeaders() {
		return Collections.unmodifiableMap(headers);
	}

	public Collection<EventMessage> getEvents() {
		return Collections.unmodifiableCollection(events);
	}

}
