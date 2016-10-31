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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A weighting scheme defines how different {@link ScoreOriginator originators}
 * for the same {@link SkillTag skill} should be combined into a single score.
 * <p>
 * Implementors must retain thread-safety and immutability.
 * 
 * @author sebastianjanisch
 *
 */
public interface WeightingScheme {

	/**
	 * 
	 * @return a collection (possibly unmodifiable) of skill tags covered by
	 *         this instance. Never {@code null};
	 */
	Collection<SkillTag> getSkillTags();

	/**
	 * Note than an exception will be thrown if given skill tag does not exist.
	 * 
	 * @param skillTag
	 *            must not be {@code null}
	 * @return never {@code null}
	 */
	Weighting getWeighting(SkillTag skillTag);

	/**
	 * 
	 * @param weightings
	 *            must not be {@code null} and must not contain duplicate skill
	 *            tags.
	 * @return never {@code null}
	 */
	public static WeightingScheme of(Collection<Weighting> weightings) {
		Objects.requireNonNull(weightings, "weightings");

		Map<SkillTag, Weighting> copy = weightings.stream().collect(Collectors.toMap(Weighting::getSkillTag, w -> w));

		return new WeightingScheme() {

			@Override
			public Collection<SkillTag> getSkillTags() {
				return Collections.unmodifiableSet(copy.keySet());
			}

			@Override
			public Weighting getWeighting(SkillTag skillTag) {
				Objects.requireNonNull(skillTag, "skillTag");

				if (!copy.containsKey(skillTag)) {
					throw new IllegalArgumentException("unknown skill tag " + skillTag);
				}

				Weighting weighting = copy.get(skillTag);

				return weighting;
			}

		};
	}

}
