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
import java.util.Collection;
import java.util.Objects;

import org.sjanisch.skillview.core.analysis.api.ContributionAnalysis;
import org.sjanisch.skillview.core.analysis.api.ContributionAnalysisService;
import org.sjanisch.skillview.core.analysis.api.ContributionScoreService;
import org.sjanisch.skillview.core.analysis.api.DetailedContributionScore;

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

	/**
	 * 
	 * @param contributionScoreService
	 *            must not be {@code null}
	 */
	public ContributionAnalysisServiceImpl(ContributionScoreService contributionScoreService) {
		this.contributionScoreService = Objects.requireNonNull(contributionScoreService, "contributionScoreService");
	}

	@Override
	public ContributionAnalysis computeContributionAnalysis(Instant startExclusive, Instant endInclusive) {
		Objects.requireNonNull(startExclusive, "startExclusive");
		Objects.requireNonNull(endInclusive, "endInclusive");

		Collection<DetailedContributionScore> scores = contributionScoreService.getContributionScores(startExclusive,
				endInclusive);

		return new ContributionAnalysisImpl(scores, startExclusive, endInclusive);
	}

}
