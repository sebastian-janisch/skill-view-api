package org.sjanisch.skillview.core.analysis.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Holds available {@link ContributionScorerDefinition contributrion scorer
 * definitions}.
 * <p>
 * Implementors must retain immutability and thread-safety.
 * 
 * @author sebastianjanisch
 *
 */
public interface ContributionScorerDefinitions {

	/**
	 * 
	 * @return a collection (possibly unmodifiable) of all covered
	 *         {@link ScoreOriginator score originators}. Never {@code null}.
	 */
	Collection<ScoreOriginator> getScoreOriginators();

	/**
	 * Note that an exception will be thrown if given originator is not
	 * contained in {@link #getScoreOriginators()}.
	 * 
	 * @param scoreOriginator
	 *            must not be {@code null}
	 * @return never {@code null}.
	 */
	ContributionScorerDefinition getDefinition(ScoreOriginator scoreOriginator);

	/**
	 * 
	 * @param contributionScorerDefinitions
	 *            must not be {@code null}. Copy will be taken.
	 * @return never {@code null}
	 */
	public static ContributionScorerDefinitions of(
			Collection<ContributionScorerDefinition> contributionScorerDefinitions) {
		Objects.requireNonNull(contributionScorerDefinitions, "contributionScorerDefinitions");

		Map<ScoreOriginator, ContributionScorerDefinition> copy = contributionScorerDefinitions.stream()
				.collect(Collectors.toMap(ContributionScorerDefinition::getScoreOriginator, Function.identity()));

		return new ContributionScorerDefinitions() {

			@Override
			public Collection<ScoreOriginator> getScoreOriginators() {
				return Collections.unmodifiableSet(copy.keySet());
			}

			@Override
			public ContributionScorerDefinition getDefinition(ScoreOriginator scoreOriginator) {
				if (!copy.containsKey(scoreOriginator)) {
					throw new IllegalArgumentException("unknown score originator " + scoreOriginator);
				}
				return copy.get(scoreOriginator);
			}

		};
	}

}
