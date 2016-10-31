package org.sjanisch.skillview.core.analysis.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.IsCloseTo.*;
import static org.junit.Assert.assertThat;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;
import org.sjanisch.skillview.core.analysis.api.ContributionScore;
import org.sjanisch.skillview.core.analysis.api.ContributionScorerDefinition;
import org.sjanisch.skillview.core.analysis.api.ContributionScorerDefinitions;
import org.sjanisch.skillview.core.analysis.api.DetailedContributionScore;
import org.sjanisch.skillview.core.analysis.api.ScoreOriginator;
import org.sjanisch.skillview.core.analysis.api.SkillTag;
import org.sjanisch.skillview.core.analysis.api.Weighting;
import org.sjanisch.skillview.core.analysis.api.WeightingScheme;
import org.sjanisch.skillview.core.contribution.api.Contributor;
import org.sjanisch.skillview.core.contribution.api.Project;

/**
 * 
 * @author sebastianjanisch
 *
 */
public class ContributionAnalysisImplTest {

	@Test
	public void testAllMethods_EmptyAnalysis_ExpectEmptyResults() {
		ContributionAnalysisImpl analysis = new ContributionAnalysisImpl(Collections.emptySet(),
				WeightingScheme.of(Collections.emptySet()), ContributionScorerDefinitions.of(Collections.emptySet()));

		assertThat(analysis.getStartTime().isPresent(), is(false));
		assertThat(analysis.getEndTime().isPresent(), is(false));
		assertThat(analysis.getScores().isEmpty(), is(true));
		assertThat(analysis.getScores(DetailedContributionScore::getContributor).isEmpty(), is(true));
		assertThat(analysis.getNormalisedScores(DetailedContributionScore::getContributor).isEmpty(), is(true));
	}

	@Test
	public void testGetStartEndTime_GivenScores_ExpectMinAndMax() {
		Instant min = Instant.now().minusSeconds(60);
		Instant max = Instant.now();

		DetailedContributionScore score1 = createScore("JAVA", 5.0, min, "SkillView", "sjanisch", "O1");
		DetailedContributionScore score2 = createScore("JAVA", 1.0, max, "SkillView", "sjanisch", "O1");

		HashSet<DetailedContributionScore> scores = new HashSet<>(Arrays.asList(score1, score2));
		WeightingScheme weightingScheme = singleWeightingScheme("JAVA", "O1");
		ContributionScorerDefinitions contributionScorerDefinitions = singleContributionScorer("JAVA", "O1", 0.0);

		ContributionAnalysisImpl analysis = new ContributionAnalysisImpl(scores, weightingScheme,
				contributionScorerDefinitions);

		assertThat(analysis.getStartTime().isPresent(), is(true));
		assertThat(analysis.getEndTime().isPresent(), is(true));
		assertThat(analysis.getStartTime().get(), equalTo(min));
		assertThat(analysis.getEndTime().get(), equalTo(max));
	}

	@Test
	public void testGetScores_GivenScores_ExpectInputScoresBack() {
		DetailedContributionScore score1 = createScore("JAVA", 5.0, Instant.now(), "SkillView", "sjanisch", "O1");
		DetailedContributionScore score2 = createScore("JAVA", 1.0, Instant.now(), "SkillView", "sjanisch", "O1");

		HashSet<DetailedContributionScore> inputScores = new HashSet<>(Arrays.asList(score1, score2));
		WeightingScheme weightingScheme = singleWeightingScheme("JAVA", "O1");
		ContributionScorerDefinitions contributionScorerDefinitions = singleContributionScorer("JAVA", "O1", 0.0);

		ContributionAnalysisImpl analysis = new ContributionAnalysisImpl(inputScores, weightingScheme,
				contributionScorerDefinitions);

		Collection<DetailedContributionScore> outputScores = analysis.getScores();

		assertThat(outputScores.size(), is(2));
		assertThat(outputScores, hasItem(score1));
		assertThat(outputScores, hasItem(score2));
	}

	@Test
	public void testGetNormalisedScores_GivenOneScorePerOriginator_Expect0NormalisedScore() {
		DetailedContributionScore score1 = createScore("JAVA", 5.0, Instant.now(), "SkillView", "sjanisch", "O1");

		HashSet<DetailedContributionScore> inputScores = new HashSet<>(Arrays.asList(score1));
		WeightingScheme weightingScheme = singleWeightingScheme("JAVA", "O1");
		ContributionScorerDefinitions contributionScorerDefinitions = singleContributionScorer("JAVA", "O1", 0.0);

		ContributionAnalysisImpl analysis = new ContributionAnalysisImpl(inputScores, weightingScheme,
				contributionScorerDefinitions);

		Map<Contributor, Collection<ContributionScore>> normalisedScores = analysis
				.getNormalisedScores(DetailedContributionScore::getContributor);

		assertThat(normalisedScores.size(), is(1));
		assertThat(normalisedScores.keySet(), hasItem(Contributor.of("sjanisch")));

		Collection<ContributionScore> scores = normalisedScores.get(Contributor.of("sjanisch"));
		assertThat(scores.size(), is(1));

		ContributionScore score = scores.iterator().next();
		assertThat(score.getScore().getAsDouble(), is(0.0));
		assertThat(score.getSkillTag(), is(SkillTag.of("JAVA")));
	}

	@Test
	public void testGetNormalisedScores_GivenTwoScoresForOneContributorPerOriginator_Expect0NormalisedScore() {
		DetailedContributionScore score1 = createScore("JAVA", 5.0, Instant.now(), "SkillView", "sjanisch", "O1");
		DetailedContributionScore score2 = createScore("JAVA", 10.0, Instant.now(), "SkillView", "sjanisch", "O1");

		HashSet<DetailedContributionScore> inputScores = new HashSet<>(Arrays.asList(score1, score2));
		WeightingScheme weightingScheme = singleWeightingScheme("JAVA", "O1");
		ContributionScorerDefinitions contributionScorerDefinitions = singleContributionScorer("JAVA", "O1", 0.0);

		ContributionAnalysisImpl analysis = new ContributionAnalysisImpl(inputScores, weightingScheme,
				contributionScorerDefinitions);

		Map<Contributor, Collection<ContributionScore>> normalisedScores = analysis
				.getNormalisedScores(DetailedContributionScore::getContributor);

		assertThat(normalisedScores.size(), is(1));
		assertThat(normalisedScores.keySet(), hasItem(Contributor.of("sjanisch")));

		Collection<ContributionScore> scores = normalisedScores.get(Contributor.of("sjanisch"));
		assertThat(scores.size(), is(1));

		ContributionScore score = scores.iterator().next();
		assertThat(score.getScore().getAsDouble(), is(0.0));
		assertThat(score.getSkillTag(), is(SkillTag.of("JAVA")));
	}

	@Test
	public void testGetNormalisedScores_GivenScoresForTwoContributorsPerOriginator_ExpectNormalisedScores() {
		DetailedContributionScore score1 = createScore("JAVA", 5.0, Instant.now(), "SkillView", "sjanisch", "O1");
		DetailedContributionScore score2 = createScore("JAVA", 10.0, Instant.now(), "SkillView", "jondoe", "O1");

		HashSet<DetailedContributionScore> inputScores = new HashSet<>(Arrays.asList(score1, score2));
		WeightingScheme weightingScheme = singleWeightingScheme("JAVA", "O1");
		ContributionScorerDefinitions contributionScorerDefinitions = singleContributionScorer("JAVA", "O1", 0.0);

		ContributionAnalysisImpl analysis = new ContributionAnalysisImpl(inputScores, weightingScheme,
				contributionScorerDefinitions);

		Map<Contributor, Collection<ContributionScore>> normalisedScores = analysis
				.getNormalisedScores(DetailedContributionScore::getContributor);

		assertThat(normalisedScores.size(), is(2));
		assertThat(normalisedScores.keySet(), hasItem(Contributor.of("sjanisch")));
		assertThat(normalisedScores.keySet(), hasItem(Contributor.of("jondoe")));

		Collection<ContributionScore> sjanischScores = normalisedScores.get(Contributor.of("sjanisch"));
		Collection<ContributionScore> jondoeScores = normalisedScores.get(Contributor.of("jondoe"));
		assertThat(sjanischScores.size(), is(1));
		assertThat(jondoeScores.size(), is(1));

		ContributionScore sjanischScore = sjanischScores.iterator().next();
		ContributionScore jondoeScore = jondoeScores.iterator().next();

		double mean = (5.0 + 10.0) / 2;
		double stdDev = Math.sqrt(1 / 2.0 * (Math.pow(5.0 - mean, 2) + Math.pow(10.0 - mean, 2)));

		assertThat(sjanischScore.getScore().getAsDouble(), is(closeTo((5.0 - mean) / stdDev, 1e-10)));
		assertThat(jondoeScore.getScore().getAsDouble(), is(closeTo((10.0 - mean) / stdDev, 1e-10)));
	}

	@Test
	public void testGetNormalisedScores_GivenMultipleScoresForTwoContributorsPerOriginator_ExpectSumScoresPerContributor() {
		DetailedContributionScore score1 = createScore("JAVA", 1.0, Instant.now(), "SkillView", "sjanisch", "O1");
		DetailedContributionScore score2 = createScore("JAVA", 4.0, Instant.now(), "SkillView", "sjanisch", "O1");
		DetailedContributionScore score3 = createScore("JAVA", 12.0, Instant.now(), "SkillView", "jondoe", "O1");
		DetailedContributionScore score4 = createScore("JAVA", -2.0, Instant.now(), "SkillView", "jondoe", "O1");

		HashSet<DetailedContributionScore> inputScores = new HashSet<>(Arrays.asList(score1, score2, score3, score4));
		WeightingScheme weightingScheme = singleWeightingScheme("JAVA", "O1");
		ContributionScorerDefinitions contributionScorerDefinitions = singleContributionScorer("JAVA", "O1", 0.0);

		ContributionAnalysisImpl analysis = new ContributionAnalysisImpl(inputScores, weightingScheme,
				contributionScorerDefinitions);

		Map<Contributor, Collection<ContributionScore>> normalisedScores = analysis
				.getNormalisedScores(DetailedContributionScore::getContributor);

		assertThat(normalisedScores.size(), is(2));
		assertThat(normalisedScores.keySet(), hasItem(Contributor.of("sjanisch")));
		assertThat(normalisedScores.keySet(), hasItem(Contributor.of("jondoe")));

		Collection<ContributionScore> sjanischScores = normalisedScores.get(Contributor.of("sjanisch"));
		Collection<ContributionScore> jondoeScores = normalisedScores.get(Contributor.of("jondoe"));
		assertThat(sjanischScores.size(), is(1));
		assertThat(jondoeScores.size(), is(1));

		ContributionScore sjanischScore = sjanischScores.iterator().next();
		ContributionScore jondoeScore = jondoeScores.iterator().next();

		double sjanischSum = (1.0 + 4.0);
		double jondoeSum = (12.0 - 2.0);

		double mean = (sjanischSum + jondoeSum) / 2;
		double stdDev = Math.sqrt(1 / 2.0 * (Math.pow(sjanischSum - mean, 2) + Math.pow(jondoeSum - mean, 2)));

		assertThat(sjanischScore.getScore().getAsDouble(), is(closeTo((sjanischSum - mean) / stdDev, 1e-10)));
		assertThat(jondoeScore.getScore().getAsDouble(), is(closeTo((jondoeSum - mean) / stdDev, 1e-10)));
	}

	@Test
	public void testGetNormalisedScores_GivenMultipleScoresForTwoContributorsAndMultipleOriginators_ExpectSumScoresPerContributorAndWeightedScores() {
		DetailedContributionScore score1 = createScore("JAVA", 1.0, Instant.now(), "SkillView", "sjanisch", "O1");
		DetailedContributionScore score2 = createScore("JAVA", 4.0, Instant.now(), "SkillView", "sjanisch", "O1");
		DetailedContributionScore score3 = createScore("JAVA", 12.0, Instant.now(), "SkillView", "jondoe", "O1");
		DetailedContributionScore score4 = createScore("JAVA", -2.0, Instant.now(), "SkillView", "jondoe", "O1");
		DetailedContributionScore score5 = createScore("JAVA", 95.0, Instant.now(), "SkillView", "tomsmith", "O1");

		DetailedContributionScore score6 = createScore("JAVA", 9.0, Instant.now(), "SkillView", "sjanisch", "O2");
		DetailedContributionScore score7 = createScore("JAVA", 4.0, Instant.now(), "SkillView", "sjanisch", "O2");
		DetailedContributionScore score8 = createScore("JAVA", 12.0, Instant.now(), "SkillView", "jondoe", "O2");
		DetailedContributionScore score9 = createScore("JAVA", 33.0, Instant.now(), "SkillView", "jondoe", "O2");
		DetailedContributionScore score10 = createScore("JAVA", 0.0, Instant.now(), "SkillView", "tomsmith", "O2");

		HashSet<DetailedContributionScore> inputScores = new HashSet<>(
				Arrays.asList(score1, score2, score3, score4, score5, score6, score7, score8, score9, score10));
		WeightingScheme weightingScheme = twoWeightingScheme("JAVA", "O1", "O2", 0.75, 0.25);
		ContributionScorerDefinitions contributionScorerDefinitions = twoContributionScorer("JAVA", "O1", "O2", 0.0,
				0.0);

		ContributionAnalysisImpl analysis = new ContributionAnalysisImpl(inputScores, weightingScheme,
				contributionScorerDefinitions);

		Map<Contributor, Collection<ContributionScore>> normalisedScores = analysis
				.getNormalisedScores(DetailedContributionScore::getContributor);

		assertThat(normalisedScores.size(), is(3));
		assertThat(normalisedScores.keySet(), hasItem(Contributor.of("sjanisch")));
		assertThat(normalisedScores.keySet(), hasItem(Contributor.of("jondoe")));
		assertThat(normalisedScores.keySet(), hasItem(Contributor.of("tomsmith")));

		Collection<ContributionScore> sjanischScores = normalisedScores.get(Contributor.of("sjanisch"));
		Collection<ContributionScore> jondoeScores = normalisedScores.get(Contributor.of("jondoe"));
		Collection<ContributionScore> tomsmithScores = normalisedScores.get(Contributor.of("tomsmith"));
		assertThat(sjanischScores.size(), is(1));
		assertThat(jondoeScores.size(), is(1));
		assertThat(tomsmithScores.size(), is(1));

		ContributionScore sjanischScore = sjanischScores.iterator().next();
		ContributionScore jondoeScore = jondoeScores.iterator().next();
		ContributionScore tomsmithScore = tomsmithScores.iterator().next();

		double sjanischSumO1 = (1.0 + 4.0);
		double sjanischSumO2 = (9.0 + 4.0);
		double jondoeSumO1 = (12.0 - 2.0);
		double jondoeSumO2 = (12.0 + 33.0);
		double tomsmithSumO1 = 95.0;
		double tomsmithSumO2 = 0.0;

		double meanO1 = (sjanischSumO1 + jondoeSumO1 + tomsmithSumO1) / 3;
		double stdDevO1 = Math.sqrt(1 / 3.0 * (Math.pow(sjanischSumO1 - meanO1, 2) + Math.pow(jondoeSumO1 - meanO1, 2)
				+ Math.pow(tomsmithSumO1 - meanO1, 2)));

		double meanO2 = (sjanischSumO2 + jondoeSumO2 + tomsmithSumO2) / 3;
		double stdDevO2 = Math.sqrt(1 / 3.0 * (Math.pow(sjanischSumO2 - meanO2, 2) + Math.pow(jondoeSumO2 - meanO2, 2)
				+ Math.pow(tomsmithSumO2 - meanO2, 2)));

		double sjanischNormalisedO1 = (sjanischSumO1 - meanO1) / stdDevO1;
		double jondoeNormalisedO1 = (jondoeSumO1 - meanO1) / stdDevO1;
		double tomsmithNormalisedO1 = (tomsmithSumO1 - meanO1) / stdDevO1;

		double sjanischNormalisedO2 = (sjanischSumO2 - meanO2) / stdDevO2;
		double jondoeNormalisedO2 = (jondoeSumO2 - meanO2) / stdDevO2;
		double tomsmithNormalisedO2 = (tomsmithSumO2 - meanO2) / stdDevO2;

		double sjanischWeighted = sjanischNormalisedO1 * 0.75 + sjanischNormalisedO2 * 0.25;
		double jondoeWeighted = jondoeNormalisedO1 * 0.75 + jondoeNormalisedO2 * 0.25;
		double tomsmithWeighted = tomsmithNormalisedO1 * 0.75 + tomsmithNormalisedO2 * 0.25;

		double weightedMean = (sjanischWeighted + jondoeWeighted + tomsmithWeighted) / 3;
		double weightedStdDev = Math.sqrt(1.0 / 3 * (Math.pow(sjanischWeighted - weightedMean, 2)
				+ Math.pow(jondoeWeighted - weightedMean, 2) + Math.pow(tomsmithWeighted - weightedMean, 2)));

		double sjanischFinal = (sjanischWeighted - weightedMean) / weightedStdDev;
		double jondoeFinal = (jondoeWeighted - weightedMean) / weightedStdDev;
		double tomsmithFinal = (tomsmithWeighted - weightedMean) / weightedStdDev;

		assertThat(sjanischScore.getScore().getAsDouble(), is(closeTo(sjanischFinal, 1e-10)));
		assertThat(jondoeScore.getScore().getAsDouble(), is(closeTo(jondoeFinal, 1e-10)));
		assertThat(tomsmithScore.getScore().getAsDouble(), is(closeTo(tomsmithFinal, 1e-10)));
	}

	// @formatter:off
	private static DetailedContributionScore createScore(
			String skillTag, 
			double score, 
			Instant scoreTime,
			String project, 
			String contributor, 
			String scoreOriginator) {
		ContributionScore rawScore = ContributionScore.of(SkillTag.of(skillTag), score);
		return DetailedContributionScore.of(
				rawScore, 
				scoreTime, 
				Project.of(project), 
				Contributor.of(contributor),
				ScoreOriginator.of(scoreOriginator));
	}

	private static WeightingScheme singleWeightingScheme(String skillTag, String scoreOriginator) {
		Weighting weighting = Weighting.of(SkillTag.of(skillTag), 
										   Collections.singletonMap(ScoreOriginator.of(scoreOriginator), 
										   1.0));
		return WeightingScheme.of(Collections.singleton(weighting));
	}
	

	private static WeightingScheme twoWeightingScheme(
			String skillTag, 
			String scoreOriginator1, 
			String scoreOriginator2, 
			double weight1, 
			double weight2) {
		
		Map<ScoreOriginator, Double> weights = new HashMap<>();
		weights.put(ScoreOriginator.of(scoreOriginator1), weight1);
		weights.put(ScoreOriginator.of(scoreOriginator2), weight2);
		
		Weighting weighting = Weighting.of(SkillTag.of(skillTag), weights);
		return WeightingScheme.of(Collections.singleton(weighting));
	}

	private static ContributionScorerDefinitions singleContributionScorer(
			String skillTag,
			String scoreOriginator, 
			double neutralScore) {
		
		ContributionScorerDefinition scorerDefinition = ContributionScorerDefinition.of(
				ScoreOriginator.of(scoreOriginator), 
				SkillTag.of(skillTag), 
				neutralScore);
		
		return ContributionScorerDefinitions.of(Collections.singleton(scorerDefinition));
	}
	
	private static ContributionScorerDefinitions twoContributionScorer(
			String skillTag,
			String scoreOriginator1,
			String scoreOriginator2,
			double neutralScore1,
			double neutralScore2) {
		
		ContributionScorerDefinition scorerDefinition1 = ContributionScorerDefinition.of(
				ScoreOriginator.of(scoreOriginator1), 
				SkillTag.of(skillTag), 
				neutralScore1);
		
		ContributionScorerDefinition scorerDefinition2 = ContributionScorerDefinition.of(
				ScoreOriginator.of(scoreOriginator2), 
				SkillTag.of(skillTag), 
				neutralScore2);
		
		return ContributionScorerDefinitions.of(Arrays.asList(scorerDefinition1, scorerDefinition2));
	}
	// @formatter:on

}
