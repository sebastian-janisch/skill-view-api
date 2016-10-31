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
package org.sjanisch.skillview.core.analysis.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.sjanisch.skillview.core.contribution.api.Contributor;

/**
 * Holds {@link ContributionScore scores} made accessible by their
 * {@link Contributor}.
 * <p>
 * Implementors must retain thread-safety and immutability.
 * 
 * @author sebastianjanisch
 *
 */
public interface ContributionAnalysis {

	/**
	 * 
	 * @return the earliest time that this analysis covers or empty if there are
	 *         is no data contained in this instance. Never {@code null}.
	 */
	default Optional<Instant> getStartTime() {
		Optional<Instant> result = getScores().stream().map(DetailedContributionScore::getScoreTime)
				.min(Instant::compareTo);
		return result;
	}

	/**
	 * 
	 * @return the latest time that this analysis covers or empty if there are
	 *         is no data contained in this instance. Never {@code null}.
	 */
	default Optional<Instant> getEndTime() {
		Optional<Instant> result = getScores().stream().map(DetailedContributionScore::getScoreTime)
				.max(Instant::compareTo);
		return result;
	}

	/**
	 * 
	 * @return a collection (possibly unmodifiable) of all contribution scores
	 *         covered by this instance. Never {@code null}.
	 */
	Collection<DetailedContributionScore> getScores();

	/**
	 * 
	 * @param partitionFunc
	 *            must not be {@code null}
	 * @return a map (possibly unmodifiable) of scores grouped by the given
	 *         function. Never {@code null}.
	 */
	default <E> Map<E, Collection<DetailedContributionScore>> getScores(
			Function<DetailedContributionScore, E> partitionFunc) {
		Objects.requireNonNull(partitionFunc, "partitionFunc");

		Map<E, Collection<DetailedContributionScore>> result = getScores().stream()
				.collect(Collectors.groupingBy(partitionFunc, Collectors.toCollection(ArrayList::new)));

		return result;
	}

	/**
	 * Forms normalised scores for each {@link SkillTag skill tag} for the
	 * partitions identified by given function based on all scores contained in
	 * this instance.
	 * <p>
	 * The normalisation is formed as a basic z-score, i.e.
	 * {@code (score - avg(score) / std(score))}.
	 * 
	 * @return a map (possibly unmodifiable) of normalised contribution scores.
	 */
	<E> Map<E, Collection<ContributionScore>> getNormalisedScores(Function<DetailedContributionScore, E> partitionFunc);

}
