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

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

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
	Optional<Instant> getStartTime();

	/**
	 * 
	 * @return the latest time that this analysis covers. Never {@code null}.
	 */
	Optional<Instant> getEndTime();

	/**
	 * 
	 * @return a collection (possibly unmodifiable) of all covered contributors
	 *         by this analysis.
	 */
	Collection<Contributor> getContributors();

	/**
	 * 
	 * @param contributor
	 *            must not be {@code null}.
	 * @return a collection (possibly unmodifiable) of total contribution scores
	 *         added up by their {@link SkillTag} and grouped by their
	 *         {@link ContributionScore#getOriginator() originator} and
	 *         {@link ContributionScore#getScoreTime()}. Never {@code null}.
	 */
	Collection<ContributionScore> getScores(Contributor contributor);

}
