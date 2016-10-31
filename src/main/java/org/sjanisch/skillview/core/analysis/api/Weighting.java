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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A weighting is an assignment of weights for different {@link ScoreOriginator
 * originators} for the same {@link SkillTag skill}.
 * <p>
 * Weights for different originators sum to {@code 1}.
 * <p>
 * Implementors must retain thread-safety and immutability.
 * 
 * @author sebastianjanisch
 *
 */
public interface Weighting {

	/**
	 * 
	 * @return the skill tag for which this weighting applies. Never
	 *         {@code null}.
	 */
	SkillTag getSkillTag();

	/**
	 * 
	 * @return a collection (possibly unmodifiable) of covered originators.
	 *         Never {@code null}.
	 */
	Collection<ScoreOriginator> getScoreOriginators();

	/**
	 * Note that weights for all covered originators sum to {@code 1}.
	 * <p>
	 * Note that an exception will be thrown if the given score originator does
	 * not exist.
	 * 
	 * @param scoreOriginator
	 *            must not be {@code null}
	 * @return weight for this originator
	 */
	double getWeight(ScoreOriginator scoreOriginator);

	/**
	 * 
	 * @param skillTag
	 *            must not be {@code null}
	 * @param weights
	 *            must not be {@code null} and weights must sum to {@code 1}.
	 *            Copy will be taken.
	 * @return never {@code null}.
	 */
	public static Weighting of(SkillTag skillTag, Map<ScoreOriginator, Double> weights) {
		Objects.requireNonNull(skillTag, "skillTag");
		Objects.requireNonNull(weights, "weights");

		if (weights.containsKey(null)) {
			throw new IllegalArgumentException("weights must not contain null element");
		}

		double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();
		if (Math.abs(sum - 1.0) > 1e-10) {
			throw new IllegalArgumentException("weights don't sum to 1");
		}

		Map<ScoreOriginator, Double> copy = Collections.unmodifiableMap(new HashMap<>(weights));

		return new Weighting() {

			@Override
			public double getWeight(ScoreOriginator scoreOriginator) {
				if (!copy.containsKey(scoreOriginator)) {
					throw new IllegalArgumentException("unknown score originator " + scoreOriginator);
				}
				Double weight = copy.get(scoreOriginator);
				return weight;
			}

			@Override
			public SkillTag getSkillTag() {
				return skillTag;
			}

			@Override
			public Collection<ScoreOriginator> getScoreOriginators() {
				return Collections.unmodifiableSet(copy.keySet());
			}
		};
	}

}
