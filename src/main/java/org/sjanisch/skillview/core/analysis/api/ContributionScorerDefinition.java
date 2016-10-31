package org.sjanisch.skillview.core.analysis.api;

import java.util.Objects;

/**
 * Descibes the attributes of a contriution scorer.
 * <p>
 * {@link #hashCode()} and {@link #equals(Object)} are implemented against
 * {@link #getScoreOriginator()}.
 * <p>
 * Implementors must retain immutability and thread-safety.
 * 
 * @author sebastianjanisch
 *
 */
public interface ContributionScorerDefinition {

	/**
	 * 
	 * @return an originator that uniquely identifies this scorer. Never
	 *         {@code null}.
	 */
	ScoreOriginator getScoreOriginator();

	/**
	 * 
	 * @return the skill for which this definition applies. Never {@code null}.
	 */
	SkillTag getSkillTag();

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

	/**
	 * 
	 * @param scoreOriginator
	 *            must not be {@code null}
	 * @param skillTag
	 *            must not be {@code null}
	 * @param neutralScore
	 *            must not be {@code null}
	 * @return never {@code null}
	 */
	public static ContributionScorerDefinition of(ScoreOriginator scoreOriginator, SkillTag skillTag,
			double neutralScore) {
		Objects.requireNonNull(scoreOriginator, "scoreOriginator");
		Objects.requireNonNull(skillTag, "skillTag");

		return new ContributionScorerDefinition() {

			@Override
			public ScoreOriginator getScoreOriginator() {
				return scoreOriginator;
			}

			@Override
			public SkillTag getSkillTag() {
				return skillTag;
			}

			@Override
			public double getNeutralScore() {
				return neutralScore;
			}

			@Override
			public String toString() {
				return String.format("%s[%s:%s:%s]", getClass().getSimpleName(), getScoreOriginator(), getSkillTag(),
						getNeutralScore());
			}

			@Override
			public int hashCode() {
				return scoreOriginator.hashCode();
			}

			@Override
			public boolean equals(Object obj) {
				return obj != null && obj instanceof ContributionScorerDefinition
						&& ((ContributionScorerDefinition) obj).getScoreOriginator().equals(scoreOriginator);
			}

		};
	}

}
