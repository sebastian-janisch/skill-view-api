package org.sjanisch.skillview.core.analysis.api;

import java.util.stream.Stream;

/**
 * Augments the {@link ContributionScoreService} by adding functionality to
 * persist {@link DetailedContributionScore contribution scores}.
 * <p>
 * Implementors must retain thread safety.
 * 
 * @author sebastianjanisch
 *
 */
public interface WritableContributionScoreService extends ContributionScoreService {

	/**
	 * 
	 * @param contributionScores
	 *            must not be {@code null}.
	 */
	void saveContributionScores(Stream<DetailedContributionScore> contributionScores);

}
