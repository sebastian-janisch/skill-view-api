package org.sjanisch.skillview.core.analysis.api;

import java.time.Instant;
import java.util.Collection;

public interface ContributionScoreService {

	Collection<DetailedContributionScore> getContributionScores(Instant startExclusive, Instant endInclusive);
	
}
