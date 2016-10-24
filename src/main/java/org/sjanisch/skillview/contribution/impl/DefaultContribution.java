/*
MIT License

Copyright (c) 2016 Sebastian Janisch

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package org.sjanisch.skillview.contribution.impl;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.sjanisch.skillview.contribution.api.Contribution;
import org.sjanisch.skillview.contribution.api.ContributionId;
import org.sjanisch.skillview.contribution.api.Contributor;
import org.sjanisch.skillview.contribution.api.Project;

/**
 * Immutable implemenation of {@link Contribution}.
 * 
 * @author sebastianjanisch
 *
 */
public class DefaultContribution implements Contribution {

	private final ContributionId id;
	private final Project project;
	private final Contributor contributor;
	private final Instant contributionTime;
	private final String message;
	private final String fileName;
	private final ByteBuffer content;
	private final ByteBuffer previousContent;

	/**
	 * Must all be non {@code null} except for {@code message} and
	 * {@code previousContent}.
	 * 
	 * @param id
	 * @param project
	 * @param contributor
	 * @param contributionTime
	 * @param message
	 * @param fileName
	 * @param content
	 *            copy will be taken
	 * @param previousContent
	 *            copy will be taken
	 */
	// @formatter:off
	public DefaultContribution(
			ContributionId id,
			Project project,
			Contributor contributor, 
			Instant contributionTime, 
			String message,
			String fileName, 
			ByteBuffer content, 
			ByteBuffer previousContent) {
		// @formatter:on
		this.id = Objects.requireNonNull(id, "id");
		this.project = Objects.requireNonNull(project, "project");
		this.contributor = Objects.requireNonNull(contributor, "contributor");
		this.contributionTime = Objects.requireNonNull(contributionTime, "contributionTime");
		this.message = message == null ? null : message.trim().isEmpty() ? null : message.trim();
		this.fileName = Objects.requireNonNull(fileName, "fileName");

		Objects.requireNonNull(content, "content");
		this.content = ByteBuffer.wrap(content.array()).asReadOnlyBuffer();

		if (previousContent == null) {
			this.previousContent = ByteBuffer.wrap("".getBytes()).asReadOnlyBuffer();
		} else {
			this.previousContent = ByteBuffer.wrap(previousContent.array()).asReadOnlyBuffer();
		}
	}

	@Override
	public ContributionId getId() {
		return id;
	}
	
	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public Contributor getContributor() {
		return contributor;
	}

	@Override
	public Instant getContributionTime() {
		return contributionTime;
	}

	@Override
	public Optional<String> getMessage() {
		return Optional.ofNullable(message);
	}

	@Override
	public String getPath() {
		return fileName;
	}

	@Override
	public ByteBuffer getContent() {
		return content;
	}

	@Override
	public ByteBuffer getPreviousContent() {
		return previousContent;
	}

}
