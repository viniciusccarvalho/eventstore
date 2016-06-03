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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import io.igx.eventstore.Checkpoint;
import io.igx.eventstore.Commit;
import io.igx.eventstore.CommitAttempt;
import io.igx.eventstore.persistence.BaseCommit;
import io.igx.eventstore.serializers.Serializer;
import io.igx.eventstore.Snapshot;
import io.igx.eventstore.persistence.PersistentStream;
import io.igx.eventstore.persistence.StreamHead;
import io.igx.eventstore.persistence.jdbc.properties.SQLCommands;
import reactor.core.publisher.Flux;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * @author Vinicius Carvalho
 */
public class JDBCPersistentStream implements PersistentStream {


	private JdbcTemplate template;
	private NamedParameterJdbcTemplate namedTemplate;
	private SQLCommands sqlCommands;
	private Serializer serializer;
	private LobHandler lobHandler;

	public JDBCPersistentStream(JdbcTemplate template, SQLCommands sqlCommands, Serializer serializer, LobHandler lobHandler) {
		this.template = template;
		this.serializer = serializer;
		this.sqlCommands = sqlCommands;
		this.lobHandler = lobHandler;
		this.namedTemplate = new NamedParameterJdbcTemplate(template);
	}

	public boolean isDisposed() {
		return false;
	}

	public void initialize() {

	}

	public Flux<Commit> from(String bucketId, LocalDateTime start) {
		return query(sqlCommands.getCommitsFromInstant(), new Object[]{bucketId,start.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond()});
	}

	public Flux<Commit> from(String checkpointToken) {
		return query(sqlCommands.getCommitsFromCheckpoint(),new Object[]{Long.valueOf(checkpointToken)});
	}

	public Flux<Commit> from(String bucketId, String checkpointToken) {
		return query(sqlCommands.getCommitsFromBucketAndCheckpoint(), new Object[]{bucketId,Long.valueOf(checkpointToken)});
	}

	public Flux<Commit> from(String bucketId, LocalDateTime start, LocalDateTime stop) {
		return query(sqlCommands.getCommitsFromToInstant(),new Object[]{bucketId,start.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond(),stop.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond()});
	}

	public Flux<Commit> from(String bucketId, String streamId, int minRevision, int maxRevision) {
		return query(sqlCommands.getCommitsFromStartingRevision(),
				new Object[]{bucketId,streamId,minRevision,maxRevision,0});
	}

	public Checkpoint getCheckPoint(String checkpointToken) {
		return null;
	}

	public Flux<Commit> getUndispatchedCommits() {
		return query(sqlCommands.getUndispatchedCommits(),new Object[]{});
	}

	public void markCommitAsDispatched(Commit commit) {
		template.update(sqlCommands.getMarkCommitAsDispatched(), new Object[]{commit.getBucketId(),commit.getStreamId(), commit.getCommitSequence()});
	}

	public void purge() {
		String sql[] = sqlCommands.getPurgeStorage().split(";");
		template.update(sql[0]);
		template.update(sql[1]);
	}

	public void purge(String bucketId) {
		String sql[] = sqlCommands.getPurgeBucket().split(";");
		template.update(sql[0], new Object[]{bucketId});
		template.update(sql[1], new Object[]{bucketId});
	}

	public void drop() {
		throw new UnsupportedOperationException();
	}

	public void deleteStream(String bucketId, String streamId) {
		String sql[] = sqlCommands.getDeleteStream().split(";");
		template.update(sql[0],new Object[]{bucketId,streamId});
		template.update(sql[1],new Object[]{bucketId,streamId});
	}

	@Override
	public Integer getCurrentStreamRevision(String bucketId, String streamId, Integer minRevision, Integer maxRevision) {
		return template.queryForObject(sqlCommands.getCurrentStreamRevision(),new Object[]{bucketId,streamId,minRevision,maxRevision},Integer.class);
	}

	@Override
	public Integer getCurrentCommitSequence(String bucketId, String streamId, Integer minRevision, Integer maxRevision) {
		return template.queryForObject(sqlCommands.getCurrentCommitSequence(),new Object[]{bucketId,streamId,maxRevision,maxRevision},Integer.class);
	}


	public Commit commit(CommitAttempt attempt) {
		return persistCommit(attempt);
	}



	public <T> Snapshot<T> getSnapshot(String bucketId, String streamId, int maxRevision, Class<T> type) {
		return template.queryForObject(sqlCommands.getSnapshot(),new Object[]{bucketId,streamId,maxRevision}, new SnapshotRowMapper<T>(serializer,type));
	}

	public boolean add(Snapshot snapshot) {
		return template.execute(sqlCommands.getAppendSnapshotToCommit(), new AbstractLobCreatingPreparedStatementCallback(lobHandler){
			@Override
			protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException, DataAccessException {
				ps.setString(1,snapshot.getBucketId());
				ps.setString(2,snapshot.getStreamId());
				ps.setInt(3,snapshot.getStreamRevision());
				lobCreator.setBlobAsBytes(ps,4,serializer.serialize(snapshot.getPayload()));
				ps.setString(5,snapshot.getBucketId());
				ps.setString(6,snapshot.getStreamId());
				ps.setInt(7,snapshot.getStreamRevision());
				ps.setString(8,snapshot.getBucketId());
				ps.setString(9,snapshot.getStreamId());
				ps.setInt(10,snapshot.getStreamRevision());

			}
		}) > 0;
	}

	public Flux<StreamHead> getStreamsToSnapshot(String bucketId, int maxThreshold) {
		return Flux.create(subscriber -> {
			try {
				template.query(sqlCommands.getStreamsRequiringSnapshots(), new Object[] {bucketId, maxThreshold}, new ResultSetExtractor<Object>() {
					@Override
					public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
						int row = 0;
						StreamHeadRowMapper rowMapper = new StreamHeadRowMapper();
						while(rs.next() && !subscriber.isCancelled()){
							subscriber.onNext(rowMapper.mapRow(rs,row++));
						}
						if(!subscriber.isCancelled()){
							subscriber.onComplete();
						}
						return null;
					}
				});
			}catch (Exception ex){
				if(!subscriber.isCancelled()){
					subscriber.onError(ex);
				}
			}
		});
	}

	private Flux<Commit> query(String sql, Object[] arguments){
		return Flux.create(subscriber -> {
			try{

				template.query(sql, arguments, new ResultSetExtractor<Object>() {
					@Override
					public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
						int row = 0;
						CommitRowMapper rowMapper = new CommitRowMapper(serializer);
						while(rs.next() && !subscriber.isCancelled()){
							subscriber.onNext(rowMapper.mapRow(rs,row++));
						}
						if(!subscriber.isCancelled()){
							subscriber.onComplete();
						}
						return null;
					}
				});
			}catch (Exception ex){
				if(!subscriber.isCancelled()){
					subscriber.onError(ex);
				}
			}
		});
	}

	private Commit persistCommit(final CommitAttempt attempt){
		KeyHolder keyHolder = new GeneratedKeyHolder();
		template.update(new AbstractLobPreparedStatementCreator(lobHandler,sqlCommands.getPersistCommit(),"CHECKPOINT_NUMBER") {
			@Override
			protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException, DataAccessException {
				ps.setString(1,attempt.getBucketId());
				ps.setString(2,attempt.getStreamId());
				ps.setString(3,attempt.getStreamId());
				ps.setString(4,attempt.getGuid().toString());
				ps.setInt(5,attempt.getCommitSequence());
				ps.setInt(6,attempt.getStreamRevision());
				ps.setInt(7,attempt.getEvents().size());
				ps.setLong(8, attempt.getCommitStamp());
				lobCreator.setBlobAsBytes(ps,9,serializer.serialize(attempt.getHeaders()));
				lobCreator.setBlobAsBytes(ps,10,serializer.serialize(attempt.getEvents()));
			}
		},keyHolder);

		return new BaseCommit(attempt.getStreamId(),
				attempt.getBucketId(),
				attempt.getStreamRevision(),
				attempt.getGuid(),
				attempt.getCommitSequence(),
				attempt.getCommitStamp(),
				keyHolder.getKey().toString(),
				attempt.getHeaders(),
				attempt.getEvents());
	}
}
