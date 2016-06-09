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

package io.igx.eventstore.persistence.jdbc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.igx.eventstore.EventMessage;
import io.igx.eventstore.EventStream;
import io.igx.eventstore.OptimisticEventStream;
import static org.junit.Assert.*;

import io.igx.eventstore.exceptions.ConcurrencyException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Vinicius Carvalho
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(JDBCPersistentApplication.class)
public class OptimisticEventStreamTests extends AbstractEventTests{
	@Autowired
	private JDBCPersistentStream persistentStream;

	@Before
	public void clean(){
		persistentStream.purge();
	}


	@Test
	public void simpleCommit() throws Exception{

		EventStream stream = new OptimisticEventStream("Account","1",persistentStream);
		stream.getUncommitedHeaders().put("Serializer","JsonSerializer");
		stream.add(new EventMessage(new AccountCreatedEvent(1,1000.0)));
		stream.add(new EventMessage(new FundsTransferedEvent(100.0)));
		stream.commitChanges(UUID.randomUUID());
		List<EventMessage> events = stream.getCommitedEvents().toList().get();
		Map<String,Object> committedHeaders = stream.getCommitedHeaders();
		assertNotNull(committedHeaders);
		assertEquals("JsonSerializer",committedHeaders.get("Serializer"));
		assertEquals(2,events.size());

	}

	@Test(expected = ConcurrencyException.class)
	public void optmisticConcurrencyTests() throws Exception{
		EventStream firstStream = new OptimisticEventStream("Account","1",persistentStream);
		EventStream secondStream = new OptimisticEventStream("Account","1",persistentStream);
		firstStream.add(new EventMessage(new AccountCreatedEvent(1,1000.0)));
		firstStream.commitChanges(UUID.randomUUID());
		secondStream.add(new EventMessage(new FundsTransferedEvent(100.0)));
		secondStream.commitChanges(UUID.randomUUID());
	}

	@Test
	public void optmisticConcurrencyCheck() throws Exception{
		EventStream firstStream = new OptimisticEventStream("Account","1",persistentStream);
		EventStream secondStream = new OptimisticEventStream("Account","1",persistentStream);
		firstStream.add(new EventMessage(new AccountCreatedEvent(1,1000.0)));
		firstStream.commitChanges(UUID.randomUUID());
		Long currentRevision = secondStream.getStreamRevision();
		secondStream.add(new EventMessage(new FundsTransferedEvent(100.0)));
		try{
			secondStream.commitChanges(UUID.randomUUID());
		}catch (Exception e){}
		List<EventMessage> commitedEvents = secondStream.getCommitedEvents().toList().get();
		assertEquals(currentRevision+1,secondStream.getStreamRevision().longValue());
		assertEquals(1,commitedEvents.size());
		secondStream.commitChanges(UUID.randomUUID());
		commitedEvents = secondStream.getCommitedEvents().toList().get();
		assertEquals(2,commitedEvents.size());
	}
}
