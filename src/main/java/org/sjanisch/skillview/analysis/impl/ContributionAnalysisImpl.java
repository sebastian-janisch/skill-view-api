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
package org.sjanisch.skillview.analysis.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.sjanisch.skillview.analysis.api.ContributionAnalysis;
import org.sjanisch.skillview.analysis.api.ContributionScore;
import org.sjanisch.skillview.analysis.api.ScoreOriginator;
import org.sjanisch.skillview.analysis.api.SkillTag;
import org.sjanisch.skillview.contribution.api.Contributor;

/**
 * 
 * @author sebastianjanisch
 *
 */
public class ContributionAnalysisImpl implements ContributionAnalysis {

	private final Map<Contributor, Set<ContributionScore>> data;

	/**
	 * 
	 * @param data
	 *            must not be {@code null}. Copy will be taken.
	 */
	public ContributionAnalysisImpl(Map<Contributor, Set<ContributionScore>> data) {
		Objects.requireNonNull(data, "data");
		this.data = Collections.unmodifiableMap(new HashMap<>(data));
	}

	@Override
	public Optional<Instant> getStartTime() {
		// @formatter:off
		Optional<Instant> result = data
				.values()
				.stream()
				.flatMap(Set::stream)
				.map(ContributionScore::getScoreTime)
				.collect(Collectors.minBy(Instant::compareTo));
		// @formatter:on

		return result;
	}

	@Override
	public Optional<Instant> getEndTime() {
		// @formatter:off
		Optional<Instant> result = data
				.values()
				.stream()
				.flatMap(Set::stream)
				.map(ContributionScore::getScoreTime)
				.collect(Collectors.maxBy(Instant::compareTo));
		// @formatter:on

		return result;
	}

	@Override
	public Collection<Contributor> getContributors() {
		return data.keySet();
	}

	@Override
	public Collection<ContributionScore> getScores(Contributor contributor) {
		Objects.requireNonNull(contributor, "contributor");

		Set<ContributionScore> scores = data.get(contributor);
		if (scores == null) {
			throw new IllegalArgumentException("unknown contributor " + contributor.getName());
		}

		// @formatter:off
		Map<Instant, Map<ScoreOriginator, Map<SkillTag, List<ContributionScore>>>> grouped = scores
				.stream()
				.collect(Collectors.groupingBy(ContributionScore::getScoreTime,
						 Collectors.groupingBy(ContributionScore::getScoreOriginator, 
						 Collectors.groupingBy(ContributionScore::getSkillTag))));
		// @formatter:on

		Collection<ContributionScore> result = new LinkedList<>();
		for (Instant scoreTime : grouped.keySet()) {
			for (ScoreOriginator scoreOriginator : grouped.get(scoreTime).keySet()) {
				for (SkillTag skillTag : grouped.get(scoreTime).get(scoreOriginator).keySet()) {
					double summedScore = grouped.get(scoreTime).get(scoreOriginator).get(skillTag).stream()
							.mapToDouble(ContributionScore::getScore).sum();
					result.add(ContributionScore.of(skillTag, summedScore, scoreOriginator, scoreTime));
				}
			}
		}

		return result;
	}

}
