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

import io.igx.eventstore.Snapshot;

/**
 * @author Vinicius Carvalho
 */
public class BaseSnapshot<T> implements Snapshot<T>{

	private final String bucketId;
	private final String streamId;
	private final Long streamRevision;
	private final T payload;

	public BaseSnapshot(String bucketId, String streamId, Long streamRevision, T payload) {
		this.bucketId = bucketId;
		this.streamId = streamId;
		this.streamRevision = streamRevision;
		this.payload = payload;
	}

	@Override
	public String getBucketId() {
		return bucketId;
	}

	@Override
	public String getStreamId() {
		return streamId;
	}

	@Override
	public Long getStreamRevision() {
		return streamRevision;
	}

	@Override
	public T getPayload() {
		return payload;
	}
}
