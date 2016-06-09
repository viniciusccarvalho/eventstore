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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.igx.eventstore.EventMessage;

/**
 * @author Vinicius Carvalho
 */
public abstract class AbstractEventTests {
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
