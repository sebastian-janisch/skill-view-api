package org.sjanisch.skillview.core.analysis.api;

import java.time.Instant;
import java.util.stream.Stream;

public interface ContributionScoreService {

	Stream<DetailedContributionScore> getContributionScores(Instant startExclusive, Instant endInclusive);

}
