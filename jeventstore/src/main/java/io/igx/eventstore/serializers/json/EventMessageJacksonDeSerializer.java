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
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import io.igx.eventstore.EventMessage;

/**
 * @author Vinicius Carvalho
 */
public class EventMessageJacksonDeSerializer extends JsonDeserializer<EventMessage>{

	private ObjectMapper mapper;

	public EventMessageJacksonDeSerializer(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public EventMessage deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
		JsonNode node = jsonParser.readValueAsTree();
		Map headers = mapper.treeToValue(node.get("headers"), Map.class);
		EventMessage eventMessage = null;
		try {
			Class clazz = Class.forName(node.get("@type").asText());
			Object payload = mapper.treeToValue(node.get("body"),clazz);
			eventMessage = new EventMessage(headers,payload);

		}
		catch (ClassNotFoundException e) {
			throw new RuntimeJsonMappingException(e.getMessage());
		}
		return eventMessage;
	}
}
