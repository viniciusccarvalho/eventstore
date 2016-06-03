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

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.igx.eventstore.Commit;
import io.igx.eventstore.CommitAttempt;
import io.igx.eventstore.EventMessage;
import io.igx.eventstore.Snapshot;
import io.igx.eventstore.persistence.BaseSnapshot;
import io.igx.eventstore.serializers.Serializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import reactor.core.publisher.Flux;
import reactor.core.subscriber.Subscribers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Vinicius Carvalho
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(JDBCPersistentApplication.class)

public class JDBCPersistentStreamTests {

	@Autowired
	private JDBCPersistentStream persistentStream;

	@Before
	public void clean(){
		persistentStream.purge();
	}

	@Autowired
	private Serializer serializer;



	@Test
	public void commit(){
		CommitAttempt attempt = new CommitAttempt("Account","1",1, UUID.randomUUID(),1,System.currentTimeMillis(), Collections.emptyMap(),Collections.singletonList(new EventMessage(new AccountCreatedEvent(1,1000.0))));
		Commit commit = persistentStream.commit(attempt);
		Flux<Commit> commits = persistentStream.from(commit.getBucketId(),commit.getStreamId(),commit.getStreamRevision(),commit.getStreamRevision()+1);
		int total = commits.toList().get().size();
		Assert.assertTrue(total == 1);
	}

	@Test
	public void multipleCommits(){
		CommitAttempt attempt = new CommitAttempt("Account","1",1, UUID.randomUUID(),1,System.currentTimeMillis(), Collections.emptyMap(),Collections.singletonList(new EventMessage(new AccountCreatedEvent(1,1000.0))));
		Commit commit = persistentStream.commit(attempt);
		CommitAttempt attempt2 = new CommitAttempt("Account","1",1, UUID.randomUUID(),1,System.currentTimeMillis(), Collections.emptyMap(),Collections.singletonList(new EventMessage(new FundsTransferedEvent(-100.0))));
		persistentStream.commit(attempt2);
		Flux<Commit> commits = persistentStream.from(commit.getBucketId(),commit.getStreamId(),commit.getStreamRevision(),commit.getStreamRevision()+1);
		int total = commits.toList().get().size();
		Assert.assertTrue(total == 2);
	}

	@Test
	public void undispatched() throws Exception{
		CommitAttempt attempt = new CommitAttempt("Account","1",1, UUID.randomUUID(),1,System.currentTimeMillis(), Collections.emptyMap(),Collections.singletonList(new EventMessage(new AccountCreatedEvent(1,1000.0))));
		Commit commit = persistentStream.commit(attempt);
		Flux<Commit> commits = persistentStream.getUndispatchedCommits();
		int total = commits.toList().get().size();
		Assert.assertTrue(total == 1);
	}

	@Test
	public void markDispatched() throws Exception{
		CommitAttempt attempt = new CommitAttempt("Account","1",1, UUID.randomUUID(),1,System.currentTimeMillis(), Collections.emptyMap(),Collections.singletonList(new EventMessage(new AccountCreatedEvent(1,1000.0))));
		Commit commit = persistentStream.commit(attempt);
		persistentStream.markCommitAsDispatched(commit);
		Flux<Commit> commits = persistentStream.getUndispatchedCommits();
		int total = commits.toList().get().size();
		Assert.assertTrue(total == 0);
	}

	@Test
	public void snapshot() throws Exception {
		CommitAttempt attempt = new CommitAttempt("Account","1",1, UUID.randomUUID(),1,System.currentTimeMillis(), Collections.emptyMap(),Collections.singletonList(new EventMessage(new AccountCreatedEvent(1,1000.0))));
		Commit commit = persistentStream.commit(attempt);
		CommitAttempt attempt2 = new CommitAttempt("Account","1",1, UUID.randomUUID(),2,System.currentTimeMillis(), Collections.emptyMap(),Collections.singletonList(new EventMessage(new FundsTransferedEvent(-100.0))));
		persistentStream.commit(attempt2);
		Flux<Commit> commits = persistentStream.from(attempt.getBucketId(),attempt.getStreamId(),Integer.MIN_VALUE,Integer.MAX_VALUE);
		final AccountAggregate accountAggregate = new AccountAggregate();
		final CountDownLatch latch = new CountDownLatch(2);
		commits.flatMap(c ->{return Flux.fromIterable(c.getEvents());}).subscribe(Subscribers.consumer(eventMessage -> {
			accountAggregate.apply(eventMessage);
			latch.countDown();
		}));
		latch.await();
		Assert.assertTrue(accountAggregate.getAmount() == 900);
		Snapshot<AccountAggregate> snapshot = new BaseSnapshot<>(attempt.getBucketId(),attempt.getStreamId(),2,accountAggregate);
		boolean persisted = persistentStream.add(snapshot);
		Assert.assertTrue(persisted);
}

	public static class AccountCreatedEvent{
		Integer id;
		Double amount;
		final Long createdTime;

		@JsonCreator
		public AccountCreatedEvent(@JsonProperty("id") Integer id, @JsonProperty("amount") Double amount) {
			this.id = id;
			this.amount = amount;
			this.createdTime = System.currentTimeMillis();
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public Double getAmount() {
			return amount;
		}

		public void setAmount(Double amount) {
			this.amount = amount;
		}

		public Long getCreatedTime() {
			return createdTime;
		}
	}

	public static class FundsTransferedEvent {
		final Double amount;

		@JsonCreator
		public FundsTransferedEvent(@JsonProperty("amount") Double amount) {
			this.amount = amount;
		}

		public Double getAmount() {
			return amount;
		}


	}

	public static class AccountAggregate {
		private Integer id;
		private Double amount;
		private Long createdTime;

		public void apply(EventMessage event){
			if(AccountCreatedEvent.class.isAssignableFrom(event.getBody().getClass())){
				AccountCreatedEvent createEvent = (AccountCreatedEvent)event.getBody();
				this.id = createEvent.getId();
				this.amount = createEvent.getAmount();
				this.createdTime = createEvent.getCreatedTime();
			}
			if(FundsTransferedEvent.class.isAssignableFrom(event.getBody().getClass())){
				FundsTransferedEvent fundsTransferedEvent = (FundsTransferedEvent)event.getBody();
				this.amount += fundsTransferedEvent.getAmount();
			}
		}

		public Integer getId() {
			return id;
		}

		public Double getAmount() {
			return amount;
		}

		public Long getCreatedTime() {
			return createdTime;
		}
	}

}
