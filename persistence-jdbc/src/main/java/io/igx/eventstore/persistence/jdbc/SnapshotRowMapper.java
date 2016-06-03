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

import io.igx.eventstore.Snapshot;
import io.igx.eventstore.persistence.BaseSnapshot;
import io.igx.eventstore.serializers.Serializer;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author Vinicius Carvalho
 */
public class SnapshotRowMapper<T> implements RowMapper<Snapshot<T>> {

	private Serializer serializer;
	private Class<T> type;
	public SnapshotRowMapper(Serializer serializer, Class<T> type) {
		this.serializer = serializer;
		this.type = type;
	}

	@Override
	public Snapshot<T> mapRow(ResultSet rs, int rowNum) throws SQLException {
		return new BaseSnapshot<>(rs.getString("BUCKET_ID"),rs.getString("STREAM_ID"),rs.getInt("STREAM_REVISION"), (T) serializer.deserialize(rs.getBytes("PAYLOAD"),type));
	}
}
