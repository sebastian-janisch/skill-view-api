package org.sjanisch.skillview.core.analysis.api;

import java.util.Collection;

public interface WritableContributionScoreService extends ContributionScoreService {

	void saveContributionScores(Collection<DetailedContributionScore> contributionScores);

}
