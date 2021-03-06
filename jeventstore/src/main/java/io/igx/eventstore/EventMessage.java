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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vinicius Carvalho
 *  Represents a single element in a stream of events.
 */
public class EventMessage<T> {

	protected Map<String,Object> headers;
	protected T body;


	public EventMessage(){
		this.headers = new HashMap<String, Object>();
	}

	public EventMessage(Map<String, Object> headers, T body) {
		this.headers = headers;
		this.body = body;
	}

	public EventMessage(T body) {
		this(new HashMap<String, Object>(),body);
	}

	/**
	 *
	 * @return the metadata which provides additional, unstructured information about this message.
	 */
	public Map<String, Object> getHeaders() {
		return Collections.unmodifiableMap(headers);
	}

	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}

	public T getBody() {
		return body;
	}

	public void setBody(T body) {
		this.body = body;
	}
}
