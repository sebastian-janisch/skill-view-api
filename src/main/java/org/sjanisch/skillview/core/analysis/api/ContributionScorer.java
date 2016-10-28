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

import java.util.Collection;

import org.sjanisch.skillview.core.contribution.api.Contribution;

/**
 * Assigns {@link ContributionScore scores} to a {@link Contribution
 * contribution}.
 * <p>
 * See {@link ContributionScore} for restrictions on the values of a score.
 * <p>
 * Implementors must retain thread-safety and immutability.
 * 
 * @author sebastianjanisch
 *
 */
public interface ContributionScorer {

	/**
	 * 
	 * @param contribution
	 *            must not be {@code null}
	 * @return a (possible unmodifiable) collection of scores for which the
	 *         {@link ContributionScore#getSkillTag() skill tag} is distinct.
	 *         Never {@code null}.
	 */
	Collection<ContributionScore> score(Contribution contribution);

	/**
	 * A neutral score is the score that is assigned in case of absence of a
	 * contribution for a contributor.
	 * <p>
	 * For example, a scorer that counts lines of contribution content (i.e. one
	 * line gives a score of {@code 1}, two lines a score of {@code 2}, etc.)
	 * would return {@code 0} as its neutral score.
	 * 
	 * @return neutral score
	 */
	double getNeutralScore();

}
