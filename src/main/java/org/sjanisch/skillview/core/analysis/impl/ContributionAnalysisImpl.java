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
package org.sjanisch.skillview.core.analysis.impl;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.sjanisch.skillview.core.analysis.api.ContributionAnalysis;
import org.sjanisch.skillview.core.analysis.api.ContributionScore;
import org.sjanisch.skillview.core.analysis.api.ContributionScorerDefinition;
import org.sjanisch.skillview.core.analysis.api.ContributionScorerDefinitions;
import org.sjanisch.skillview.core.analysis.api.DetailedContributionScore;
import org.sjanisch.skillview.core.analysis.api.ScoreOriginator;
import org.sjanisch.skillview.core.analysis.api.SkillTag;
import org.sjanisch.skillview.core.analysis.api.Weighting;
import org.sjanisch.skillview.core.analysis.api.WeightingScheme;
import org.sjanisch.skillview.core.contribution.api.Contributor;
import org.sjanisch.skillview.core.utility.Lazy;

/**
 * Thread-safe and immutable implementation of {@link ContributionAnalysis}.
 * 
 * @author sebastianjanisch
 *
 */
public class ContributionAnalysisImpl implements ContributionAnalysis {

	private final List<DetailedContributionScore> data;
	private final WeightingScheme weightingScheme;
	private final ContributionScorerDefinitions contributionScorerDefinitions;

	private final Lazy<Collection<Contributor>> allContributors;
	private final Lazy<Map<ScoreOriginator, DescriptiveStats>> descriptiveStats;

	/**
	 * 
	 * @param data
	 *            unnormalised scores. Must not be {@code null}. Copy will be
	 *            taken.
	 * @param weightingScheme
	 *            used to combine scores for normalisation. Must not be
	 *            {@code null}.
	 * @param contributionScorerDefinitions
	 *            must not be {@code null}.
	 */
	public ContributionAnalysisImpl(Collection<DetailedContributionScore> data, WeightingScheme weightingScheme,
			ContributionScorerDefinitions contributionScorerDefinitions) {
		Objects.requireNonNull(data, "data");
		this.weightingScheme = Objects.requireNonNull(weightingScheme, "weightingScheme");
		this.contributionScorerDefinitions = Objects.requireNonNull(contributionScorerDefinitions,
				"contributionScorerDefinitions");

		this.data = Collections.unmodifiableList(new ArrayList<>(data));

		this.allContributors = Lazy.of(this::getContributors);
		this.descriptiveStats = Lazy.of(this::computeDescriptiveStatistics);
	}

	@Override
	public Collection<DetailedContributionScore> getScores() {
		return data;
	}

	@Override
	public <E> Map<E, Collection<ContributionScore>> getNormalisedScores(
			Function<DetailedContributionScore, E> partitionFunc) {
		Objects.requireNonNull(partitionFunc, "partitionFunc");

		Map<Contributor, Collection<ContributionScore>> normalisedScores = computeNormalisedScores();
		Map<E, Collection<ContributionScore>> unnormalisedResult = createPartitionScores(partitionFunc,
				normalisedScores);

		// normalise again
		// @formatter:off
		Map<SkillTag, List<ContributionScore>> scoresBySkillTag = unnormalisedResult
				.values()
				.stream()
				.flatMap(Collection::stream)
				.collect(Collectors.groupingBy(ContributionScore::getSkillTag));
		// @formatter:on

		Map<SkillTag, UnaryOperator<ContributionScore>> normalisers = new HashMap<>();
		for (SkillTag skillTag : scoresBySkillTag.keySet()) {
			List<ContributionScore> scores = scoresBySkillTag.get(skillTag);
			double mean = scores.stream().map(ContributionScore::getScore).filter(OptionalDouble::isPresent)
					.mapToDouble(OptionalDouble::getAsDouble).average().orElse(Double.NaN);
			double sumOfSquares = scores.stream().map(ContributionScore::getScore).filter(OptionalDouble::isPresent)
					.mapToDouble(OptionalDouble::getAsDouble).map(score -> Math.pow(score - mean, 2)).sum();
			double stdDev = Math.sqrt(1.0 / scores.size() * sumOfSquares);

			if (Double.isNaN(mean) || Double.isNaN(stdDev) || stdDev == 0.0) {
				normalisers.put(skillTag, __ -> ContributionScore.of(skillTag, 0.0));
			} else {
				normalisers.put(skillTag, score -> {
					return ContributionScore.of(skillTag, (score.getScore().getAsDouble() - mean) / stdDev);
				});
			}
		}

		Map<E, Collection<ContributionScore>> result = new HashMap<>();
		for (E partition : unnormalisedResult.keySet()) {
			result.put(partition, new LinkedList<>());
			for (ContributionScore score : unnormalisedResult.get(partition)) {
				UnaryOperator<ContributionScore> normaliser = normalisers.get(score.getSkillTag());
				ContributionScore normalised = normaliser.apply(score);
				result.get(partition).add(normalised);
			}
		}

		return result;
	}

	private Map<Contributor, Collection<ContributionScore>> computeNormalisedScores() {
		Map<Contributor, Collection<ContributionScore>> normalisedScores = new HashMap<>();

		Collection<SkillTag> skillTags = weightingScheme.getSkillTags();
		for (SkillTag skillTag : skillTags) {
			Weighting weighting = weightingScheme.getWeighting(skillTag);
			Collection<ScoreOriginator> scoreOriginators = weighting.getScoreOriginators();

			// normalise and weight
			Map<Contributor, Double> weightedScores = new HashMap<>();
			for (ScoreOriginator scoreOriginator : scoreOriginators) {
				DescriptiveStats stats = descriptiveStats.get().get(scoreOriginator);
				if (stats != null) {
					Map<Contributor, ContributionScore> normalised = stats.normalise(data);
					for (Contributor contributor : normalised.keySet()) {
						ContributionScore normalisedScore = normalised.get(contributor);
						double weightedScore = normalisedScore.getScore().getAsDouble()
								* weighting.getWeight(scoreOriginator);
						if (!weightedScores.containsKey(contributor)) {
							weightedScores.put(contributor, 0.0);
						}
						weightedScores.put(contributor, weightedScores.get(contributor) + weightedScore);
					}
				}
			}

			// normalise weighted
			double mean = weightedScores.values().stream().mapToDouble(Double::doubleValue).average()
					.orElse(Double.NaN);

			double sumOfSquares = weightedScores.values().stream().mapToDouble(Double::doubleValue)
					.map(score -> Math.pow((score - mean), 2)).sum();

			double stdDev = Math.sqrt(1.0 / weightedScores.size() * sumOfSquares);

			ContributionScore absentScore = ContributionScore.of(skillTag, 0.0);
			if (Double.isNaN(mean) || Double.isNaN(stdDev) || stdDev == 0.0) {
				for (Contributor contributor : allContributors.get()) {
					normalisedScores.putIfAbsent(contributor, new LinkedList<>());
					normalisedScores.get(contributor).add(absentScore);
				}
			} else {
				for (Contributor contributor : allContributors.get()) {
					normalisedScores.putIfAbsent(contributor, new LinkedList<>());
					Double weightedScore = weightedScores.get(contributor);
					if (weightedScore == null) {
						normalisedScores.get(contributor).add(absentScore);
					} else {
						double normalesedScore = (weightedScore - mean) / stdDev;
						normalisedScores.get(contributor).add(ContributionScore.of(skillTag, normalesedScore));
					}
				}
			}
		}
		return normalisedScores;
	}

	private <E> Map<E, Collection<ContributionScore>> createPartitionScores(
			Function<DetailedContributionScore, E> partitionFunc,
			Map<Contributor, Collection<ContributionScore>> normalisedScores) {
		Map<E, Collection<ContributionScore>> result = new HashMap<>();

		Map<E, List<DetailedContributionScore>> partitions = data.stream().collect(groupingBy(partitionFunc));
		for (E partition : partitions.keySet()) {
			List<DetailedContributionScore> scores = partitions.get(partition);
			Set<Contributor> contributors = scores.stream().map(DetailedContributionScore::getContributor)
					.collect(toSet());
			double weight = 1.0 / contributors.size();

			Map<SkillTag, ContributionScore> partitionScores = new HashMap<>();
			for (Contributor contributor : contributors) {
				Collection<ContributionScore> normalisedScoresForContributor = normalisedScores.get(contributor);
				for (ContributionScore score : normalisedScoresForContributor) {
					double weightedScore = score.getScore().getAsDouble() * weight;
					if (!partitionScores.containsKey(score.getSkillTag())) {
						partitionScores.put(score.getSkillTag(),
								ContributionScore.of(score.getSkillTag(), weightedScore));
					} else {
						double existingScore = partitionScores.get(score.getSkillTag()).getScore().getAsDouble();
						partitionScores.put(score.getSkillTag(),
								ContributionScore.of(score.getSkillTag(), existingScore + weightedScore));
					}
				}
			}

			result.put(partition, partitionScores.values());
		}
		return result;
	}

	private Map<ScoreOriginator, DescriptiveStats> computeDescriptiveStatistics() {
		Map<ScoreOriginator, DescriptiveStats> result = new HashMap<>();

		Set<ScoreOriginator> scoreOriginators = data.stream().map(DetailedContributionScore::getScoreOriginator)
				.collect(Collectors.toSet());

		for (ScoreOriginator scoreOriginator : scoreOriginators) {
			ContributionScorerDefinition definition = contributionScorerDefinitions.getDefinition(scoreOriginator);
			DescriptiveStats descriptiveStats = DescriptiveStats.create(data, definition, allContributors.get());
			result.put(scoreOriginator, descriptiveStats);
		}

		return Collections.unmodifiableMap(result);
	}

	private Collection<Contributor> getContributors() {
		Set<Contributor> result = data.stream().map(DetailedContributionScore::getContributor)
				.collect(Collectors.toSet());
		return Collections.unmodifiableSet(result);
	}

	private static class DescriptiveStats {
		private final ContributionScorerDefinition scorerDefinition;
		private final double mean;
		private final double stdDev;

		DescriptiveStats(ContributionScorerDefinition scorerDefinition, double mean, double stdDev) {
			this.scorerDefinition = scorerDefinition;
			this.mean = mean;
			this.stdDev = stdDev;
		}

		static DescriptiveStats create(Collection<DetailedContributionScore> scores,
				ContributionScorerDefinition scorerDefinition, Collection<Contributor> allContributors) {
			ScoreOriginator scoreOriginator = scorerDefinition.getScoreOriginator();

			if (scores.isEmpty()) {
				return new DescriptiveStats(scorerDefinition, Double.NaN, Double.NaN);
			}

			// @formatter:off
			Map<Contributor, Double> rawScoresByContributors = scores
					.stream()
					.filter(score -> score.getScore().isPresent())
					.filter(score -> score.getScoreOriginator().equals(scoreOriginator))
					.collect(Collectors.groupingBy(DetailedContributionScore::getContributor, 
							                       Collectors.summingDouble(score -> score.getScore().getAsDouble())));
			// @formatter:on

			for (Contributor contributor : allContributors) {
				if (!rawScoresByContributors.containsKey(contributor)) {
					rawScoresByContributors.put(contributor, scorerDefinition.getNeutralScore());
				}
			}

			// @formatter:off
			double[] rawScores = rawScoresByContributors
					.values()
					.stream()
					.mapToDouble(Double::doubleValue)
					.toArray();
			// @formatter:on

			double mean = DoubleStream.of(rawScores).average().getAsDouble();
			double sumOfSquares = DoubleStream.of(rawScores).map(rawScore -> Math.pow((rawScore - mean), 2)).sum();
			double stdDev = Math.sqrt(1.0 / rawScoresByContributors.size() * sumOfSquares);
			return new DescriptiveStats(scorerDefinition, mean, stdDev);
		}

		Map<Contributor, ContributionScore> normalise(Collection<DetailedContributionScore> scores) {
			ScoreOriginator scoreOriginator = scorerDefinition.getScoreOriginator();
			SkillTag skillTag = scorerDefinition.getSkillTag();

			// @formatter:off
			Map<Contributor, Double> rawScores = scores
					.stream()
					.filter(score -> score.getScore().isPresent())
					.filter(score -> score.getScoreOriginator().equals(scoreOriginator))
					.collect(Collectors.groupingBy(DetailedContributionScore::getContributor, 
												   Collectors.summingDouble(score -> score.getScore().getAsDouble())));
			
			Map<Contributor, ContributionScore> normalised = rawScores
					.entrySet()
					.stream()
					.collect(Collectors.toMap(Entry::getKey, e -> ContributionScore.of(skillTag, normalise(e.getValue()))));
			
			// @formatter:on
			return normalised;
		}

		double normalise(double score) {
			if (Double.isNaN(stdDev) || stdDev == 0.0) {
				return 0.0;
			}

			double normalised = (score - mean) / stdDev;
			return normalised;
		}

	}
}
