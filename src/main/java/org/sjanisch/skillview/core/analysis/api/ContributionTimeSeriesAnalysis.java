package org.sjanisch.skillview.core.analysis.api;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;

import org.sjanisch.skillview.core.contribution.api.Contributor;

/**
 * 
 * @author sebastianjanisch
 *
 */
public interface ContributionTimeSeriesAnalysis {

	/**
	 * 
	 * @param contributor
	 * @param timeWindow
	 * @param decay
	 * @return
	 */
	Collection<ContributionScore> getScores(Contributor contributor, Duration timeWindow, DecayFunction decay);

	Map<Contributor, Collection<ContributionScore>> getScores(Duration timeWindow, DecayFunction decay);

}
