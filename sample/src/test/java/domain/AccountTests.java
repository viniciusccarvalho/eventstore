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

package domain;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import io.igx.eventstore.EventMessage;
import io.igx.eventstore.EventStore;
import io.igx.eventstore.EventStream;
import io.igx.eventstore.persistence.jdbc.CommitRowMapper;
import io.igx.eventstore.sample.SampleApplication;
import io.igx.eventstore.serializers.json.JacksonSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Vinicius Carvalho
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(SampleApplication.class)
public class AccountTests {

	@Autowired
	private EventStore eventStore;

	private JdbcTemplate template;

	@Test
	public void createAccount() throws Exception{
		AccountCreatedEvent accountCreatedEvent = new AccountCreatedEvent("1","joe",1000.0);
		EventStream stream = eventStore.open("Account","1",Long.MIN_VALUE,Long.MAX_VALUE);
		stream.add(new EventMessage(accountCreatedEvent));
		stream.commitChanges(UUID.randomUUID());
	}


	public void fluxTest() throws Exception {
		Connection conn;
		Flux.create(subscriber -> {
			template.query("", new RowCallbackHandler() {
				@Override
				public void processRow(ResultSet rs) throws SQLException {
					while(rs.next() && !subscriber.isCancelled()){
						int col = 0;
						CommitRowMapper rowMapper = new CommitRowMapper(new JacksonSerializer());
						subscriber.onNext(rowMapper.mapRow(rs,col++));
					}
					if(!subscriber.isCancelled()){
						subscriber.onComplete();
					}
				}
			});
		});
	}
}
