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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import io.igx.eventstore.Commit;
import io.igx.eventstore.EventMessage;
import io.igx.eventstore.persistence.BaseCommit;
import io.igx.eventstore.serializers.Serializer;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author Vinicius Carvalho
 */
public class CommitRowMapper implements RowMapper<Commit> {


	private Serializer serializer;

	public CommitRowMapper(Serializer serializer) {
		this.serializer = serializer;
	}

	public Commit mapRow(ResultSet rs, int rowNum) throws SQLException {
		return new BaseCommit(rs.getString("STREAM_ID"),
				rs.getString("BUCKET_ID"),
				rs.getInt("STREAM_REVISION"),
				UUID.fromString(rs.getString("COMMIT_ID")),
				rs.getInt("COMMIT_SEQUENCE"),
				rs.getLong("COMMIT_STAMP"),
				String.valueOf(rs.getLong("CHECKPOINT_NUMBER")),
				serializer.deserializeMap(rs.getBytes("HEADERS")),
				serializer.deserializeCollection(rs.getBytes("PAYLOAD"), EventMessage.class));
	}
}
