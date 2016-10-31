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

import java.util.Objects;
import java.util.OptionalDouble;

/**
 * A contribution score describes a numerical value assigned to a certain skill.
 * <p>
 * The score does not follow a specific scale but must be linear (i.e. a score
 * of {@code 10} must be twice as valuable compared to a score of {@code 5}.
 * <p>
 * {@link #hashCode()} and {@link #equals(Object)} are not to be implemented.
 * <p>
 * Implementors must retain thread-safety and immutability.
 * 
 * @author sebastianjanisch
 *
 */
public interface ContributionScore {

	/**
	 * 
	 * @return the skill tag for which this score applies. Never {@code null}.
	 */
	SkillTag getSkillTag();

	/**
	 * 
	 * @return the score if it could be determined. Never {@code null}.
	 */
	OptionalDouble getScore();

	/**
	 * 
	 * @param skillTag
	 *            must not be {@code null}
	 * @param score
	 * @return never {@code null}
	 */
	public static ContributionScore of(SkillTag skillTag, double score) {
		Objects.requireNonNull(skillTag, "skillTag");

		OptionalDouble wrapped = Double.isNaN(score) ? OptionalDouble.empty() : OptionalDouble.of(score);

		return new ContributionScore() {

			@Override
			public SkillTag getSkillTag() {
				return skillTag;
			}

			@Override
			public OptionalDouble getScore() {
				return wrapped;
			}

			@Override
			public String toString() {
				return String.format("%s[%s:%f:%s]", getClass().getSimpleName(), skillTag.getValue(), score);
			}
		};

	}

}
