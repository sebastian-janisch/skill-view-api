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
package org.sjanisch.skillview.analysis.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sjanisch.skillview.analysis.api.ContributionAnalysis;
import org.sjanisch.skillview.analysis.api.ContributionAnalysisService;
import org.sjanisch.skillview.analysis.api.ContributionScore;
import org.sjanisch.skillview.analysis.api.ContributionScorer;
import org.sjanisch.skillview.analysis.api.DetailedContributionScore;
import org.sjanisch.skillview.contribution.api.Contribution;
import org.sjanisch.skillview.contribution.api.ContributionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the {@link ContributionAnalysisService} against a
 * {@link ContributionService contribution service} and a collection of
 * {@link ContributionScorer scorers} to score each contribution.
 * <p>
 * This implementation is immutable and thread-safe.
 * 
 * @author sebastianjanisch
 *
 */
public class ContributionAnalysisServiceImpl implements ContributionAnalysisService {

	private static final Logger log = LoggerFactory.getLogger(ContributionAnalysisServiceImpl.class);

	private final ContributionService contributionService;
	private final Collection<ContributionScorer> scorers;

	/**
	 * 
	 * @param contributionService
	 *            must not be {@code null}
	 * @param scorers
	 *            must not be {@code null}. Copy will be taken.
	 */
	public ContributionAnalysisServiceImpl(ContributionService contributionService,
			Collection<ContributionScorer> scorers) {
		this.contributionService = Objects.requireNonNull(contributionService, "contributionService");
		Objects.requireNonNull(scorers, "scorers");

		this.scorers = Collections.unmodifiableCollection(new LinkedList<>(scorers));
	}

	@Override
	public ContributionAnalysis computeContributionAnalysis(Instant startExclusive, Instant endInclusive) {
		Objects.requireNonNull(startExclusive, "startExclusive");
		Objects.requireNonNull(endInclusive, "endInclusive");

		try (Stream<Contribution> contributions = contributionService.retrieveContributions(startExclusive,
				endInclusive)) {

			// @formatter:off
			Function<Contribution, List<DetailedContributionScore>> score = contribution -> {
				List<DetailedContributionScore> scores = scorers
						.stream()
						.map(scorer -> {
							Collection<DetailedContributionScore> contributionScore = scorer
									.score(contribution)
									.stream()
									.map(toDetailedContributionScore(contribution))
									.collect(Collectors.toList());
							log(contribution, contributionScore);
							return contributionScore;
						})
						.flatMap(Collection::stream)
						.collect(Collectors.toList());
				return scores;
			};
			
			List<DetailedContributionScore> scores = contributions
						.parallel()
						.map(score)
						.flatMap(List::stream)
						.collect(Collectors.toList());
			// @formatter:on

			return new ContributionAnalysisImpl(scores, startExclusive, endInclusive);
		}
	}

	private static Function<ContributionScore, DetailedContributionScore> toDetailedContributionScore(
			Contribution contribution) {
		return score -> {
			return DetailedContributionScore.of(score, contribution.getContributionTime(), contribution.getProject(),
					contribution.getContributor());
		};
	}

	private void log(Contribution contribution, Collection<DetailedContributionScore> contributionScore) {
		if (log.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();

			sb.append("Scored contribution by ").append(contribution.getContributor().getName());
			sb.append(" at time ").append(contribution.getContributionTime()).append(": ");

			if (contributionScore.isEmpty()) {
				sb.append("no score");
			} else {
				sb.append(System.lineSeparator());

				for (ContributionScore score : contributionScore) {
					sb.append("    ");
					sb.append(score.getSkillTag().getValue()).append(" ");
					sb.append(score.getScore()).append(" ");
					sb.append(score.getScoreOriginator().getValue());
					sb.append(System.lineSeparator());
				}
				sb.setLength(sb.length() - System.lineSeparator().length());
			}

			log.debug(sb.toString());
		}

	}
}
