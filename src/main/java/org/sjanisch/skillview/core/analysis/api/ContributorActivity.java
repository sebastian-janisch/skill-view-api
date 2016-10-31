package org.sjanisch.skillview.core.analysis.api;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.sjanisch.skillview.core.contribution.api.Contributor;

/**
 * Contributor activity logs at what points in time a contributor has made
 * contributions.
 * <p>
 * Implementors of this interface must retain immutability and thread-safety.
 * 
 * @author sebastianjanisch
 *
 */
public interface ContributorActivity {

	/**
	 * 
	 * @return the start point (exclusive) that this instance covers. Never
	 *         {@code null}.
	 */
	Instant getPeriodStart();

	/**
	 * 
	 * @return the end point (inclusive) that this instance covers. Never
	 *         {@code null}.
	 */
	Instant getPeriodEnd();

	/**
	 * 
	 * @return a map (possibly unmodifiable) of all activities for contributors
	 *         between time frame of this instance. Never {@code null}.
	 */
	Map<Contributor, Collection<Instant>> getContributorActivities();

	/**
	 * @param startExclusive
	 *            must not be {@code null}
	 * @param endInclusive
	 *            must not be {@code null}
	 * @return a collection (possibly unmodifiable) of {@link Contributor
	 *         contributors} who have been active in the given time frame. Never
	 *         {@code null}.
	 */
	default Collection<Contributor> getActiveContributors(Instant startExclusive, Instant endInclusive) {
		Objects.requireNonNull(startExclusive, "startExclusive");
		Objects.requireNonNull(endInclusive, "endInclusive");

		Predicate<Contributor> wasActive = contributor -> wasActive(contributor, startExclusive, endInclusive);
		Set<Contributor> result = getContributorActivities().keySet().stream().filter(wasActive)
				.collect(Collectors.toSet());

		return result;
	}

	/**
	 * 
	 * @param startExclusive
	 *            must not be {@code null} and must not be before
	 *            {@link #getPeriodStart()}.
	 * @param endInclusive
	 *            must not be {@code null} and must not be before
	 *            {@link #getPeriodEnd()}.
	 * @return {@code true} if the given contributor was active between given
	 *         time frame, {@code false} else.
	 */
	boolean wasActive(Contributor contributor, Instant startExclusive, Instant endInclusive);

	/**
	 * @param startExclusive
	 *            must not be {@code null} and must not be after end.
	 * @param endInclusive
	 *            must not be {@code null} and must not be before start.
	 * @param contributorActivities
	 *            must not be {@code null}. Copy will be taken.
	 * @return never {@code null}
	 */
	public static ContributorActivity of(Instant startExclusive, Instant endInclusive,
			Map<Contributor, Collection<Instant>> contributorActivities) {
		Objects.requireNonNull(contributorActivities, "contributorActivities");
		Objects.requireNonNull(startExclusive, "startExclusive");
		Objects.requireNonNull(endInclusive, "endInclusive");

		if (startExclusive.isAfter(endInclusive)) {
			String msg = "start cannot be after end: %s %s";
			throw new IllegalArgumentException(String.format(msg, startExclusive, endInclusive));
		}

		Map<Contributor, TreeSet<Instant>> copy = Collections.unmodifiableMap(contributorActivities.keySet().stream()
				.collect(Collectors.toMap(c -> c, c -> new TreeSet<>(contributorActivities.get(c)))));

		return new ContributorActivity() {
			@Override
			public Instant getPeriodStart() {
				return startExclusive;
			}

			@Override
			public Instant getPeriodEnd() {
				return endInclusive;
			}

			@Override
			public Map<Contributor, Collection<Instant>> getContributorActivities() {
				return copy.keySet().stream().collect(Collectors.toMap(c -> c, c -> copy.get(c)));
			}

			@Override
			public boolean wasActive(Contributor contributor, Instant startExclusive, Instant endInclusive) {
				Objects.requireNonNull(contributor, "contributor");
				Objects.requireNonNull(startExclusive, "startExclusive");
				Objects.requireNonNull(endInclusive, "endInclusive");

				if (startExclusive.isBefore(getPeriodStart())) {
					String msg = "start must not be before start of this instance: %s %s";
					throw new IllegalArgumentException(String.format(msg, startExclusive, getPeriodStart()));
				}

				if (endInclusive.isAfter(getPeriodEnd())) {
					String msg = "end must not be after end of this instance: %s %s";
					throw new IllegalArgumentException(String.format(msg, endInclusive, getPeriodEnd()));
				}

				if (!copy.containsKey(contributor)) {
					return false;
				}

				boolean wasActive = copy.get(contributor).subSet(startExclusive, false, endInclusive, true).isEmpty();

				return wasActive;
			}
		};
	}

}
