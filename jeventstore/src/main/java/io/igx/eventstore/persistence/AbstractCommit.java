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
import java.util.Map;
import java.util.UUID;

import io.igx.eventstore.EventMessage;
import io.igx.eventstore.Commit;

/**
 * @author Vinicius Carvalho
 */
public class AbstractCommit implements Commit {

	final String streamId;
	final String bucketId;
	final Integer streamRevision;
	final UUID guid;
	final Integer commitSequence;
	final LocalDateTime commitStamp;
	final Map<String,Object> headers;
	final Collection<EventMessage> events;
	final String checkpointToken;

	public AbstractCommit(String streamId, String bucketId, Integer streamRevision, UUID guid, Integer commitSequence, LocalDateTime commitStamp, Map<String, Object> headers, Collection<EventMessage> events, String checkpointToken) {
		this.streamId = streamId;
		this.bucketId = bucketId;
		this.streamRevision = streamRevision;
		this.guid = guid;
		this.commitSequence = commitSequence;
		this.commitStamp = commitStamp;
		this.headers = headers;
		this.events = events;
		this.checkpointToken = checkpointToken;
	}

	public String getStreamId() {
		return streamId;
	}

	public String getBucketId() {
		return bucketId;
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
		return headers;
	}

	public Collection<EventMessage> getEvents() {
		return events;
	}

	public String getCheckpointToken() {
		return checkpointToken;
	}
}
