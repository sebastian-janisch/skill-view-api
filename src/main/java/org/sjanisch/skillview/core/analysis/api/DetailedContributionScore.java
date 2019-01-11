package org.sjanisch.skillview.core.analysis.api;

import java.time.Instant;
import java.util.Objects;
import java.util.OptionalDouble;

import org.sjanisch.skillview.core.contribution.api.Contributor;
import org.sjanisch.skillview.core.contribution.api.Project;

/**
 * A detailed contribution score adds contextual parameters to a contribution
 * score, such as when the score was achieved, the project it refers to, etc.
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
	 * @return a hash code that together with project, time and contributor identifies this commit. Never {@code null}.
	 */ 
	String getContributionHash();

	/**
	 * 
	 * @return the contributor for this score. Never {@code null}.
	 */
	Contributor getContributor();

	/**
	 * 
	 * @return Never {@code null}.
	 */
	ScoreOriginator getScoreOriginator();

	/**
	 * Must all be non {@code null}
	 * 
	 * @param score
	 * @param scoreTime
	 * @param project
	 * @param contributionHash
	 * @param contributor
	 * @param scoreOriginator
	 * @return never {@code null}
	 */
	public static DetailedContributionScore of(ContributionScore score, Instant scoreTime, Project project,
			String contributionHash, Contributor contributor, ScoreOriginator scoreOriginator) {
		Objects.requireNonNull(score, "score");
		Objects.requireNonNull(scoreTime, "scoreTime");
		Objects.requireNonNull(project, "project");
		Objects.requireNonNull(contributionHash, "contributionHash");
		Objects.requireNonNull(contributor, "contributor");
		Objects.requireNonNull(scoreOriginator, "scoreOriginator");

		return new DetailedContributionScore() {

			@Override
			public SkillTag getSkillTag() {
				return score.getSkillTag();
			}

			@Override
			public ScoreOriginator getScoreOriginator() {
				return scoreOriginator;
			}

			@Override
			public OptionalDouble getScore() {
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
			public Project getContributionHash() {
				return contributionHash;
			}

			@Override
			public Contributor getContributor() {
				return contributor;
			}

			@Override
			public String toString() {
				return String.format("%s[%s:%s:%s:%s:%s:%s]", getClass().getSimpleName(), score.toString(),
						project.toString(), project.getContributionHash(), scoreTime.toString(), 
						     contributor.toString(), scoreOriginator.toString());
			}
		};
	}
}
