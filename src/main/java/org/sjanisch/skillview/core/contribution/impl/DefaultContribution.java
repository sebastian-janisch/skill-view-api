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
package org.sjanisch.skillview.core.contribution.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

import org.sjanisch.skillview.core.contribution.api.Contribution;
import org.sjanisch.skillview.core.contribution.api.ContributionId;
import org.sjanisch.skillview.core.contribution.api.ContributionItem;
import org.sjanisch.skillview.core.contribution.api.Contributor;
import org.sjanisch.skillview.core.contribution.api.Project;

/**
 * Immutable implemenation of {@link Contribution}.
 * 
 * @author sebastianjanisch
 *
 */
public class DefaultContribution implements Contribution {

	public static final class Builder {
		private final ContributionId id;
		private final Project project;
		private final Contributor contributor;
		private final Instant contributionTime;
		private String message;
		private final Collection<ContributionItem> contributionItems = new LinkedList<>();

		/**
		 * 
		 * @param id
		 * @param project
		 * @param contributor
		 * @param contributionTime
		 */
		private Builder(ContributionId id, Project project, Contributor contributor, Instant contributionTime) {
			this.id = id;
			this.project = project;
			this.contributor = contributor;
			this.contributionTime = contributionTime;
		}

		/**
		 * 
		 * @param message
		 *            can be {@code null}
		 * @return this instance. Never {@code null}.
		 */
		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}

		/**
		 * 
		 * @param contributionItem
		 *            must not be {@code null}.
		 * @return this instance. Never {@code null}.
		 */
		public Builder addContributionItem(ContributionItem contributionItem) {
			this.contributionItems.add(contributionItem);
			return this;
		}

		/**
		 * 
		 * @return new {@link Contribution} with contents of this builder. Never
		 *         {@code null}.
		 */
		public Contribution build() {
			return new DefaultContribution(id, project, contributor, contributionTime, message, contributionItems);
		}
	}

	private final ContributionId id;
	private final Project project;
	private final Contributor contributor;
	private final Instant contributionTime;
	private final String message;
	private final Collection<ContributionItem> contributionItems;

	// @formatter:off
	private DefaultContribution(
			ContributionId id,
			Project project,
			Contributor contributor, 
			Instant contributionTime, 
			String message,
			Collection<ContributionItem> contributionItems) {
		// @formatter:on
		this.id = Objects.requireNonNull(id, "id");
		this.project = Objects.requireNonNull(project, "project");
		this.contributor = Objects.requireNonNull(contributor, "contributor");
		this.contributionTime = Objects.requireNonNull(contributionTime, "contributionTime");
		this.message = message == null ? null : message.trim().isEmpty() ? null : message.trim();
		Objects.requireNonNull(contributionItems, "contributionItems");
		this.contributionItems = Collections.unmodifiableCollection(new LinkedList<>(contributionItems));
	}

	/**
	 * Must all be non {@code null}.
	 * 
	 * @param id
	 * @param project
	 * @param contributor
	 * @param contributionTime
	 */
	public static Builder newBuilder(ContributionId id, Project project, Contributor contributor,
			Instant contributionTime) {
		return new Builder(id, project, contributor, contributionTime);
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
	public Collection<ContributionItem> getContributionItems() {
		return contributionItems;
	}

}
