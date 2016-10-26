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
package org.sjanisch.skillview.analysis.api;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import org.sjanisch.skillview.contribution.api.Contributor;

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
	 * @return the earliest time that this analysis covers. Never {@code null}.
	 */
	Instant getStartTime();

	/**
	 * 
	 * @return the latest time that this analysis covers. Never {@code null}.
	 */
	Instant getEndTime();

	/**
	 * 
	 * @return a collection (possibly unmodifiable) of all covered contributors
	 *         by this analysis. Might be empty but never {@code null}.
	 */
	Collection<Contributor> getContributors();

	/**
	 * Note that an empty collection will be returned if given contributor is
	 * not contained in {@link #getContributors()}.
	 * 
	 * @param contributor
	 *            must not be {@code null}.
	 * @return a collection (possibly unmodifiable) of contribution scores for
	 *         given contributor. Never {@code null}.
	 */
	Collection<ContributionScore> getScores(Contributor contributor);

	/**
	 * 
	 * @return a map (possibly unmodifiable) of contribution scores for all
	 *         contributors covered by this instance. Never {@code null}.
	 */
	Map<Contributor, Collection<ContributionScore>> getScores();

	/**
	 * 
	 * @param timeWindow
	 *            must not be {@code null}
	 * @return a sorted collection (possibly unmodifiable) of analysis instances
	 *         based on this instance for start and end time of each instance is
	 *         no larger than the given time window. Never {@code null}.
	 */
	Collection<ContributionAnalysis> getScores(Duration timeWindow);

	/**
	 * Partitions the underlying scores by their
	 * {@link ContributionScore#getScoreOriginator() originator} and
	 * {@link ContributionScore#getSkillTag() skill tag} and normalises for each
	 * partition.
	 * <p>
	 * The normalisation is formed as a basic z-score, i.e.
	 * {@code (score - avg(score) / std(score))}.
	 * <p>
	 * To normalise each partition the scores for each contributor are summed up
	 * before mean, standard deviation and normalised score are calculated.
	 * <p>
	 * Note that if the underlying data is already normalised then this instance
	 * will be returned.
	 * 
	 * @return a view of this analysis for which all scores are normalised
	 *         (z-scores). Never {@code null}.
	 */
	ContributionAnalysis normalised();

	/**
	 * 
	 * @return an instance containing the raw scores. Never {@code null}.
	 */
	ContributionAnalysis denormalised();

	/**
	 * 
	 * @return {@code true} if this instance contains normalised scores,
	 *         {@code false} else.
	 */
	boolean isNormalised();

}
