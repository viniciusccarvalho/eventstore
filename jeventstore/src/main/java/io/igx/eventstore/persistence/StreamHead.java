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

/**
 * @author Vinicius Carvalho
 */
public class StreamHead {
	private final String bucketId;
	private final String streamId;
	private final Integer headRevision;
	private final Integer snapShotRevision;

	public StreamHead(String bucketId, String streamId, Integer headRevision, Integer snapShotRevision) {
		this.bucketId = bucketId;
		this.streamId = streamId;
		this.headRevision = headRevision;
		this.snapShotRevision = snapShotRevision;
	}

	public String getBucketId() {
		return bucketId;
	}

	public String getStreamId() {
		return streamId;
	}

	public Integer getHeadRevision() {
		return headRevision;
	}

	public Integer getSnapShotRevision() {
		return snapShotRevision;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StreamHead that = (StreamHead) o;

		return streamId.equals(that.streamId);

	}

	@Override
	public int hashCode() {
		return streamId.hashCode();
	}
}
