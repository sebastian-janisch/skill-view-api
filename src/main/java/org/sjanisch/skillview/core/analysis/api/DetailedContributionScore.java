package org.sjanisch.skillview.core.analysis.api;

import java.time.Instant;
import java.util.Objects;

import org.sjanisch.skillview.core.contribution.api.Contributor;
import org.sjanisch.skillview.core.contribution.api.Project;

/**
 * A detailed contribution score adds contextual parameters to a contribution
 * score, such as when the score was achieved, the project it refers to, etc.
 * <p>
 * <p>
 * {@link #hashCode()} and {@link #equals(Object)} are not to be implemented.
 * <p>
 * Implementors must retain thread-safety and immutability.
 * 
 * @author sebastianjanisch
 *
 */
public interface DetailedContributionScore extends ContributionScore {

	/**
	 * 
	 * @return indicates when this score was achieved. Never {@code null}.
	 */
	Instant getScoreTime();

	/**
	 * 
	 * @return the project this score relates to. Never {@code null}.
	 */
	Project getProject();

	/**
	 * 
	 * @return the contributor for this score. Never {@code null}.
	 */
	Contributor getContributor();

	/**
	 * Must all be non {@code null}
	 * 
	 * @param score
	 * @param scoreTime
	 * @param project
	 * @param contributor
	 * @return never {@code null}
	 */
	public static DetailedContributionScore of(ContributionScore score, Instant scoreTime, Project project,
			Contributor contributor) {
		Objects.requireNonNull(score, "score");
		Objects.requireNonNull(scoreTime, "scoreTime");
		Objects.requireNonNull(project, "project");
		Objects.requireNonNull(contributor, "contributor");

		return new DetailedContributionScore() {

			@Override
			public SkillTag getSkillTag() {
				return score.getSkillTag();
			}

			@Override
			public ScoreOriginator getScoreOriginator() {
				return score.getScoreOriginator();
			}

			@Override
			public double getScore() {
				return score.getScore();
			}

			@Override
			public Instant getScoreTime() {
				return scoreTime;
			}

			@Override
			public Project getProject() {
				return project;
			}

			@Override
			public Contributor getContributor() {
				return contributor;
			}

			@Override
			public String toString() {
				return String.format("%s[%s:%s:%s:%s]", getClass().getSimpleName(), score.toString(),
						project.toString(), scoreTime.toString(), contributor.toString());
			}
		};
	}
}
