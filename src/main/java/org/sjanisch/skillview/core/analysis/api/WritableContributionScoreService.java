package org.sjanisch.skillview.core.analysis.api;

import java.util.Collection;

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
	void saveContributionScores(Collection<DetailedContributionScore> contributionScores);

}
