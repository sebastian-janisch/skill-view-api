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
package org.sjanisch.skillview.core.analysis.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sjanisch.skillview.core.analysis.api.ContributionAnalysis;
import org.sjanisch.skillview.core.analysis.api.ContributionAnalysisService;
import org.sjanisch.skillview.core.analysis.api.ContributionScoreService;
import org.sjanisch.skillview.core.analysis.api.ContributionScorerDefinitions;
import org.sjanisch.skillview.core.analysis.api.DetailedContributionScore;
import org.sjanisch.skillview.core.analysis.api.WeightingScheme;

/**
 * Implements the {@link ContributionAnalysisService} against a
 * {@link ContributionScoreService contribution score service}.
 * <p>
 * This implementation is immutable and thread-safe.
 * 
 * @author sebastianjanisch
 *
 */
public class ContributionAnalysisServiceImpl implements ContributionAnalysisService {

	private final ContributionScoreService contributionScoreService;
	private final WeightingScheme weightingScheme;
	private final ContributionScorerDefinitions contributionScorerDefinitions;

	/**
	 * 
	 * @param contributionScoreService
	 *            must not be {@code null}
	 * @param weightingScheme
	 *            must not be {@code null}
	 * @param contributionScorerDefinitions
	 *            must not be {@code null}
	 */
	public ContributionAnalysisServiceImpl(ContributionScoreService contributionScoreService,
			WeightingScheme weightingScheme, ContributionScorerDefinitions contributionScorerDefinitions) {
		this.contributionScoreService = Objects.requireNonNull(contributionScoreService, "contributionScoreService");
		this.weightingScheme = Objects.requireNonNull(weightingScheme, "weightingScheme");
		this.contributionScorerDefinitions = Objects.requireNonNull(contributionScorerDefinitions,
				"contributionScorerDefinitions");
	}

	@Override
	public ContributionAnalysis computeContributionAnalysis(Instant startExclusive, Instant endInclusive) {
		Objects.requireNonNull(startExclusive, "startExclusive");
		Objects.requireNonNull(endInclusive, "endInclusive");

		try (Stream<DetailedContributionScore> scores = contributionScoreService.getContributionScores(startExclusive,
				endInclusive)) {
			return new ContributionAnalysisImpl(scores.collect(Collectors.toList()), weightingScheme,
					contributionScorerDefinitions);
		}

	}

}
