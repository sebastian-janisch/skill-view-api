package org.sjanisch.skillview.core.analysis.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sjanisch.skillview.core.analysis.api.ContributionScore;
import org.sjanisch.skillview.core.analysis.api.ContributionScoreService;
import org.sjanisch.skillview.core.analysis.api.ContributionScorer;
import org.sjanisch.skillview.core.analysis.api.ContributionScorerDefinition;
import org.sjanisch.skillview.core.analysis.api.DetailedContributionScore;
import org.sjanisch.skillview.core.analysis.api.ScoreOriginator;
import org.sjanisch.skillview.core.contribution.api.Contribution;
import org.sjanisch.skillview.core.contribution.api.ContributionId;
import org.sjanisch.skillview.core.contribution.api.ContributionService;
import org.sjanisch.skillview.core.contribution.api.Contributor;
import org.sjanisch.skillview.core.contribution.api.Project;
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

		logInit();
	}

	@Override
	public Stream<DetailedContributionScore> getContributionScores(Instant startExclusive, Instant endInclusive) {
		Objects.requireNonNull(startExclusive, "startExclusive");
		Objects.requireNonNull(endInclusive, "endInclusive");

		AtomicLong scoredContributions = new AtomicLong();

		Stream<Contribution> contributions = contributionService.retrieveContributions(startExclusive, endInclusive);

		Function<Contribution, List<DetailedContributionScore>> score = contribution -> {

			List<DetailedContributionScore> scores = scorers.stream().map(scorer -> {
				ContributionScorerDefinition definition = scorer.getDefinition();

				OptionalDouble rawScore = scorer.score(contribution);

				if (!rawScore.isPresent()) {
					return null;
				}

				Instant scoreTime = contribution.getContributionTime();
				Project project = contribution.getProject();
				ContributionId contributionId = contribution.getId();
				Contributor contributor = contribution.getContributor();
				ScoreOriginator scoreOriginator = definition.getScoreOriginator();
				ContributionScore contributionScore = ContributionScore.of(definition.getSkillTag(),
						rawScore.getAsDouble());

				DetailedContributionScore result = DetailedContributionScore.of(contributionScore, scoreTime, project,
						contributionId, contributor, scoreOriginator);

				log(contribution, result, scoredContributions.incrementAndGet());

				return result;
			}).filter(Objects::nonNull).collect(Collectors.toList());

			return scores;
		};

		Stream<DetailedContributionScore> scores = contributions.parallel().map(score).flatMap(List::stream);

		return scores.onClose(() -> contributions.close());
	}

	private void logInit() {
		if (log.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder();

			sb.append(String.format("Starting contribution based scoring service with %s scorers", scorers.size()));
			sb.append(System.lineSeparator());
			for (ContributionScorer scorer : scorers) {
				sb.append("    ").append(scorer.getDefinition().getScoreOriginator().getValue());
				sb.append(" for skill tag ");
				sb.append(scorer.getDefinition().getSkillTag().getValue()).append(" with neutral score ");
				sb.append(scorer.getDefinition().getNeutralScore());
				sb.append(System.lineSeparator());
			}
			sb.setLength(sb.length() - System.lineSeparator().length());

			log.info(sb.toString());
		}
	}

	private void log(Contribution contribution, DetailedContributionScore contributionScore, long scoredContributions) {
		if (log.isInfoEnabled() && scoredContributions % 1000 == 0) {
			log.info(String.format("Scored %s contributions", scoredContributions));
		}

		if (log.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();

			sb.append("Scored contribution by ").append(contribution.getContributor().getName());
			sb.append(" at time ").append(contribution.getContributionTime()).append(": ");

			sb.append(contributionScore.getSkillTag().getValue()).append(" ");
			sb.append(contributionScore.getScore()).append(" ");
			sb.append(contributionScore.getScoreOriginator().getValue());

			log.debug(sb.toString());
		}

	}

}
