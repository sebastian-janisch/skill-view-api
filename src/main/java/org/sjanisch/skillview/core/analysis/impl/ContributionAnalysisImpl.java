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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sjanisch.skillview.core.analysis.api.ContributionAnalysis;
import org.sjanisch.skillview.core.analysis.api.ContributionScore;
import org.sjanisch.skillview.core.analysis.api.DetailedContributionScore;
import org.sjanisch.skillview.core.contribution.api.Contributor;

/**
 * Thread-safe and immutable implementation of {@link ContributionAnalysis}.
 * 
 * @author sebastianjanisch
 *
 */
public class ContributionAnalysisImpl implements ContributionAnalysis {

	private final List<DetailedContributionScore> data;
	private final Instant start;
	private final Instant end;
	private final Predicate<DetailedContributionScore> filter;
	private final boolean normalised;

	private final Instant earliest;
	private final Instant latest;

	/**
	 * 
	 * @param data
	 *            unnormalised scores. Must not be {@code null}. Copy will be
	 *            taken.
	 * @param start
	 *            the start time of this analysis which must not be {@code null}
	 *            and must not be less or equal to the earliest entry in the
	 *            data.
	 * @param end
	 *            the end time of this analysis which must not be {@code null}
	 *            and must not be greater than the latest entry in the data.
	 */
	public ContributionAnalysisImpl(Collection<DetailedContributionScore> data, Instant start, Instant end) {
		Objects.requireNonNull(data, "data");
		this.data = Collections.unmodifiableList(new ArrayList<>(data));
		this.start = Objects.requireNonNull(start, "start");
		this.end = Objects.requireNonNull(end, "end");

		LongSummaryStatistics stats = data.stream().map(DetailedContributionScore::getScoreTime)
				.mapToLong(Instant::toEpochMilli).summaryStatistics();

		if (Instant.ofEpochMilli(stats.getMin()).isBefore(start)) {
			String msg = String.format("%s vs %s", Instant.ofEpochMilli(stats.getMin()), start);
			throw new IllegalArgumentException("earliest element is before start time: " + msg);
		}

		if (Instant.ofEpochMilli(stats.getMax()).isAfter(end)) {
			String msg = String.format("%s vs %s", Instant.ofEpochMilli(stats.getMax()), end);
			throw new IllegalArgumentException("latest element is after end time: " + msg);
		}

		this.earliest = Instant.ofEpochMilli(stats.getMin());
		this.latest = Instant.ofEpochMilli(stats.getMax());

		filter = __ -> true;
		this.normalised = false;
	}

	private ContributionAnalysisImpl(List<DetailedContributionScore> data, Instant start, Instant end,
			Predicate<DetailedContributionScore> filter, boolean normalised) {
		this.data = data; // no copy, internal contructor
		this.start = start;
		this.end = end;
		this.filter = filter;
		this.normalised = normalised;

		LongSummaryStatistics stats = data.stream().filter(filter).map(DetailedContributionScore::getScoreTime)
				.mapToLong(Instant::toEpochMilli).summaryStatistics();

		this.earliest = Instant.ofEpochMilli(stats.getMin());
		this.latest = Instant.ofEpochMilli(stats.getMax());
	}

	@Override
	public Instant getStartTime() {
		return start;
	}

	@Override
	public Instant getEndTime() {
		return end;
	}

	@Override
	public Collection<Contributor> getContributors() {
		return data().map(DetailedContributionScore::getContributor).collect(Collectors.toSet());
	}

	@Override
	public Map<Contributor, Collection<ContributionScore>> getScores() {
		if (normalised) {
			Map<Contributor, Collection<ContributionScore>> result = normalise(data(), null);
			return result;
		} else {
			// @formatter:off
			Map<Contributor, Collection<ContributionScore>> result = data()
					.collect(Collectors.groupingBy(DetailedContributionScore::getContributor, 
												   Collectors.mapping(ContributionScore.class::cast, 
														   		      Collectors.toCollection(ArrayList::new))));
			// @formatter:on
			return result;
		}
	}

	@Override
	public Collection<ContributionScore> getScores(Contributor contributor) {
		Objects.requireNonNull(contributor, "contributor");

		if (normalised) {
			Map<Contributor, Collection<ContributionScore>> normalised = normalise(data(), contributor);
			return normalised.getOrDefault(contributor, Collections.emptySet());
		} else {
			Collection<ContributionScore> result = data().filter(score -> score.getContributor().equals(contributor))
					.collect(Collectors.toSet());
			return result;
		}
	}

	@Override
	public Collection<ContributionAnalysis> getScores(Duration timeWindow) {
		Objects.requireNonNull(timeWindow, "timeWindow");

		long excessStart = (long) Math
				.floor(Duration.between(start, earliest).getSeconds() / (timeWindow.getSeconds() + 0.0));
		long excessEnd = (long) Math.ceil(Duration.between(latest, end).getSeconds() / (timeWindow.getSeconds() + 0.0));

		Instant adjustedStart = start.plus(Duration.ofSeconds(timeWindow.getSeconds() * excessStart));
		Instant adjustedEnd = end.minus(Duration.ofSeconds(timeWindow.getSeconds() * excessEnd));

		Instant currentStart = adjustedStart;

		Collection<ContributionAnalysis> result = new LinkedList<>();
		do {
			Instant closedStart = currentStart;
			Instant closedEnd = closedStart.plus(timeWindow);

			Predicate<DetailedContributionScore> filter = score -> !closedStart.isAfter(score.getScoreTime());
			filter = filter.and(score -> score.getScoreTime().isBefore(closedEnd));

			result.add(new ContributionAnalysisImpl(data, closedStart, closedEnd.minusSeconds(1),
					filter.and(this.filter), normalised));

		} while (!(currentStart = currentStart.plus(timeWindow)).isAfter(adjustedEnd));

		return Collections.unmodifiableCollection(result);
	}

	@Override
	public boolean isNormalised() {
		return normalised;
	}

	@Override
	public ContributionAnalysis normalised() {
		return new ContributionAnalysisImpl(data, start, end, filter, true);
	}

	@Override
	public ContributionAnalysis denormalised() {
		return new ContributionAnalysisImpl(data, start, end, filter, false);
	}

	private Stream<DetailedContributionScore> data() {
		return data.stream().filter(filter);
	}

	private Map<Contributor, Collection<ContributionScore>> normalise(Stream<DetailedContributionScore> unnormalised,
			Contributor contributor) {
		Map<NormaliseKey, List<DetailedContributionScore>> groups = unnormalised.collect(groupingBy(NormaliseKey::new));

		Map<Contributor, Collection<ContributionScore>> result = new HashMap<>();
		for (NormaliseKey key : groups.keySet()) {
			List<DetailedContributionScore> scores = groups.get(key);
			Map<Contributor, Collection<ContributionScore>> normalised = normalise(key, scores, contributor);

			for (Contributor c : normalised.keySet()) {
				result.putIfAbsent(c, new LinkedList<>());
				result.get(c).addAll(normalised.get(c));
			}
		}

		return result;
	}

	private Map<Contributor, Collection<ContributionScore>> normalise(NormaliseKey key,
			List<DetailedContributionScore> scores, Contributor contributor) {
		if (scores.isEmpty()) {
			return Collections.emptyMap();
		}

		// @formatter:off
		Map<Contributor, Double> scoreSums = scores
				.stream()
				.collect(Collectors.groupingBy(DetailedContributionScore::getContributor, 
											   Collectors.mapping(score -> score.getScore(), 
													   		      Collectors.summingDouble(d -> d))));
		// @formatter:on

		double average = scoreSums.values().stream().mapToDouble(Double::doubleValue).average().getAsDouble();
		double sumOfSquares = scoreSums.values().stream().mapToDouble(Double::doubleValue)
				.map(score -> Math.pow(score - average, 2)).sum();

		double stdDev = Math.sqrt(1.0 / scoreSums.size() * sumOfSquares);

		DoubleUnaryOperator toStandardised = score -> {
			if(stdDev == 0.0) {
				return 0.0;
			}
			double standardised = (score - average) / stdDev;
			return standardised;
		};

		Predicate<Contributor> contributorFilter = c -> contributor == null ? true : c.equals(contributor);

		// @formatter:off
		Map<Contributor, Double> standardisedScores = scoreSums
				.entrySet()
				.stream()
				.filter(e -> contributorFilter.test(e.getKey()))
				.collect(Collectors.toMap(Entry::getKey, e -> toStandardised.applyAsDouble(e.getValue())));
		
		Map<Contributor, Collection<ContributionScore>> result = standardisedScores
				.entrySet()
				.stream()
				.collect(Collectors.groupingBy(Entry::getKey, 
											   Collectors.mapping(e -> 
											   		   ContributionScore.of(key.score.getSkillTag(), 
													   e.getValue(), 
													   key.score.getScoreOriginator()), Collectors.toCollection(ArrayList::new))));
		// @formatter:on

		return result;
	}

	private static class NormaliseKey {
		private final DetailedContributionScore score;

		public NormaliseKey(DetailedContributionScore score) {
			this.score = score;
		}

		@Override
		public int hashCode() {
			return Objects.hash(score.getScoreOriginator(), score.getSkillTag());
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof NormaliseKey)) {
				return false;
			}
			NormaliseKey key = (NormaliseKey) obj;
			return key.score.getScoreOriginator().equals(score.getScoreOriginator())
					&& key.score.getSkillTag().equals(score.getSkillTag());
		}

	}
}
