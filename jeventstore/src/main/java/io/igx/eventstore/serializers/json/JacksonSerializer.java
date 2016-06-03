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

package io.igx.eventstore.serializers.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.igx.eventstore.EventMessage;
import io.igx.eventstore.serializers.Serializer;

/**
 * @author Vinicius Carvalho
 */
public class JacksonSerializer<T> implements Serializer<T> {

	private ObjectMapper mapper;

	public JacksonSerializer(){
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(EventMessage.class,new EventMessageJacksonSerializer());

		module.addDeserializer(EventMessage.class,new EventMessageJacksonDeSerializer(mapper));
		mapper.registerModule(module);
		this.mapper = mapper;
	}

	public JacksonSerializer(ObjectMapper mapper){
		this.mapper = mapper;
	}

	@Override
	public byte[] serialize(T payload) {
		byte[] bytes = null;
		try {
			bytes = mapper.writeValueAsBytes(payload);
		}
		catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return bytes;
	}

	@Override
	public <T> T deserialize(byte[] bytes, Class<T> clazz) {
		T payload = null;
		try {
			payload = mapper.readValue(bytes,clazz);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return payload;
	}

	@Override
	public Map<String, Object> deserializeMap(byte[] bytes) {
		Map<String,Object> payload = new HashMap<>();
		try {
			payload = mapper.readValue(bytes,TypeFactory.defaultInstance().constructMapType(HashMap.class,String.class,Object.class));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return payload;
	}

	@Override
	public List<T> deserializeCollection(byte[] bytes, Class<T> clazz) {
		List<T> payload = new ArrayList<>();
		try {
			payload = mapper.readValue(bytes, TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, clazz));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return payload;
	}
}
