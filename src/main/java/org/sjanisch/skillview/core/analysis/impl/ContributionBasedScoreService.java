package org.sjanisch.skillview.core.analysis.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sjanisch.skillview.core.analysis.api.ContributionScore;
import org.sjanisch.skillview.core.analysis.api.ContributionScoreService;
import org.sjanisch.skillview.core.analysis.api.ContributionScorer;
import org.sjanisch.skillview.core.analysis.api.DetailedContributionScore;
import org.sjanisch.skillview.core.analysis.api.ScoreOriginator;
import org.sjanisch.skillview.core.contribution.api.Contribution;
import org.sjanisch.skillview.core.contribution.api.ContributionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the {@link ContributionScoreService} against a
 * {@link ContributionService contribution service} and a collection of
 * {@link ContributionScorer scorers} to score each contribution.
 * <p>
 * This implementation is immutable and thread-safe.
 * 
 * @author sebastianjanisch
 *
 */
public class ContributionBasedScoreService implements ContributionScoreService {

	private static final Logger log = LoggerFactory.getLogger(ContributionBasedScoreService.class);

	private final ContributionService contributionService;
	private final Collection<ContributionScorer> scorers;

	/**
	 * 
	 * @param contributionService
	 *            must not be {@code null}
	 * @param scorers
	 *            must not be {@code null}. Copy will be taken.
	 */
	public ContributionBasedScoreService(ContributionService contributionService,
			Collection<ContributionScorer> scorers) {
		this.contributionService = Objects.requireNonNull(contributionService, "contributionService");
		Objects.requireNonNull(scorers, "scorers");

		this.scorers = Collections.unmodifiableCollection(new LinkedList<>(scorers));
	}

	@Override
	public Collection<DetailedContributionScore> getContributionScores(Instant startExclusive, Instant endInclusive) {
		Objects.requireNonNull(startExclusive, "startExclusive");
		Objects.requireNonNull(endInclusive, "endInclusive");

		AtomicLong scoredContributions = new AtomicLong();

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
									.map(toDetailedContributionScore(contribution, scorer))
									.collect(Collectors.toList());
							log(contribution, contributionScore, scoredContributions.incrementAndGet());
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

			return scores;
		}
	}

	private static Function<ContributionScore, DetailedContributionScore> toDetailedContributionScore(
			Contribution contribution, ContributionScorer scorer) {
		return score -> {
			ScoreOriginator scoreOriginator = ScoreOriginator.of(scorer.getClass().getName());
			return DetailedContributionScore.of(score, contribution.getContributionTime(), contribution.getProject(),
					contribution.getContributor(), scoreOriginator);
		};
	}

	private void log(Contribution contribution, Collection<DetailedContributionScore> contributionScores,
			long scoredContributions) {
		if (log.isInfoEnabled() && scoredContributions % 10000 == 0) {
			log.info(String.format("Scored %s contributions", scoredContributions));
		}

		if (log.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();

			sb.append("Scored contribution by ").append(contribution.getContributor().getName());
			sb.append(" at time ").append(contribution.getContributionTime()).append(": ");

			if (contributionScores.isEmpty()) {
				sb.append("no score");
			} else {
				sb.append(System.lineSeparator());

				for (DetailedContributionScore score : contributionScores) {
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
