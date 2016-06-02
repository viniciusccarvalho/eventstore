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

package io.igx.eventstore.persistence.jdbc.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Vinicius Carvalho
 */
@ConfigurationProperties(prefix = "sql.commands")
public class SQLCommands {

	private String appendSnapshotToCommit;
	private String duplicateCommit;
	private String commitsFromInstant;
	private String commitsFromToInstant;
	private String commitsFromStartingRevision;
	private String snapshot;
	private String streamsRequiringSnapshots;
	private String undispatchedCommits;
	private String markCommitAsDispatched;
	private String purgeStorage;
	private String purgeBucket;
	private String dropTables;
	private String commitsFromCheckpoint;
	private String commitsFromBucketAndCheckpoint;
	private String deleteStream;
	private String persistCommit;
	private String currentStreamRevision;
	private String currentCommitSequence;

	public String getCurrentCommitSequence() {
		return currentCommitSequence;
	}

	public void setCurrentCommitSequence(String currentCommitSequence) {
		this.currentCommitSequence = currentCommitSequence;
	}

	public String getCurrentStreamRevision() {
		return currentStreamRevision;
	}

	public void setCurrentStreamRevision(String currentStreamRevision) {
		this.currentStreamRevision = currentStreamRevision;
	}

	public String getPersistCommit() {
		return persistCommit;
	}

	public void setPersistCommit(String persistCommit) {
		this.persistCommit = persistCommit;
	}

	public String getAppendSnapshotToCommit() {
		return appendSnapshotToCommit;
	}

	public void setAppendSnapshotToCommit(String appendSnapshotToCommit) {
		this.appendSnapshotToCommit = appendSnapshotToCommit;
	}

	public String getDuplicateCommit() {
		return duplicateCommit;
	}

	public void setDuplicateCommit(String duplicateCommit) {
		this.duplicateCommit = duplicateCommit;
	}

	public String getCommitsFromInstant() {
		return commitsFromInstant;
	}

	public void setCommitsFromInstant(String commitsFromInstant) {
		this.commitsFromInstant = commitsFromInstant;
	}

	public String getCommitsFromToInstant() {
		return commitsFromToInstant;
	}

	public void setCommitsFromToInstant(String commitsFromToInstant) {
		this.commitsFromToInstant = commitsFromToInstant;
	}

	public String getCommitsFromStartingRevision() {
		return commitsFromStartingRevision;
	}

	public void setCommitsFromStartingRevision(String commitsFromStartingRevision) {
		this.commitsFromStartingRevision = commitsFromStartingRevision;
	}

	public String getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(String snapshot) {
		this.snapshot = snapshot;
	}

	public String getStreamsRequiringSnapshots() {
		return streamsRequiringSnapshots;
	}

	public void setStreamsRequiringSnapshots(String streamsRequiringSnapshots) {
		this.streamsRequiringSnapshots = streamsRequiringSnapshots;
	}

	public String getUndispatchedCommits() {
		return undispatchedCommits;
	}

	public void setUndispatchedCommits(String undispatchedCommits) {
		this.undispatchedCommits = undispatchedCommits;
	}

	public String getMarkCommitAsDispatched() {
		return markCommitAsDispatched;
	}

	public void setMarkCommitAsDispatched(String markCommitAsDispatched) {
		this.markCommitAsDispatched = markCommitAsDispatched;
	}

	public String getPurgeStorage() {
		return purgeStorage;
	}

	public void setPurgeStorage(String purgeStorage) {
		this.purgeStorage = purgeStorage;
	}

	public String getPurgeBucket() {
		return purgeBucket;
	}

	public void setPurgeBucket(String purgeBucket) {
		this.purgeBucket = purgeBucket;
	}

	public String getDropTables() {
		return dropTables;
	}

	public void setDropTables(String dropTables) {
		this.dropTables = dropTables;
	}

	public String getCommitsFromCheckpoint() {
		return commitsFromCheckpoint;
	}

	public void setCommitsFromCheckpoint(String commitsFromCheckpoint) {
		this.commitsFromCheckpoint = commitsFromCheckpoint;
	}

	public String getCommitsFromBucketAndCheckpoint() {
		return commitsFromBucketAndCheckpoint;
	}

	public void setCommitsFromBucketAndCheckpoint(String commitsFromBucketAndCheckpoint) {
		this.commitsFromBucketAndCheckpoint = commitsFromBucketAndCheckpoint;
	}

	public String getDeleteStream() {
		return deleteStream;
	}

	public void setDeleteStream(String deleteStream) {
		this.deleteStream = deleteStream;
	}
}
