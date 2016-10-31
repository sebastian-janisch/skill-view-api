package org.sjanisch.skillview.core.analysis.api;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

import org.sjanisch.skillview.core.contribution.api.Contributor;

/**
 * A contributor universe lists all {@link Contributor contributors} that are
 * considered active for a given time period.
 * <p>
 * There is no statement made as to what 'active' means and this detail is due
 * to the concrete implementation. An implementor could decide that a
 * contributor is kept active even though he has not contributed in the last
 * month.
 * <p>
 * Implementors of this interface must retain thread-safety.
 * 
 * @author sebastianjanisch
 *
 */
public interface ContributorUniverse {

	/**
	 * 
	 * @return the start point (exclusive) that this contributor universe
	 *         covers. Never {@code null}.
	 */
	Instant getPeriodStart();

	/**
	 * 
	 * @return the end point (inclusive) that this contributor universe covers.
	 *         Never {@code null}.
	 */
	Instant getPeriodEnd();

	/**
	 * 
	 * @return a collection (possibly unmodifiable) of contributors that are
	 *         considered active within the given time period. Never
	 *         {@code null}.
	 */
	Collection<Contributor> getContributors();

	/**
	 * 
	 * @param periodStart
	 *            exclusive. Must not be {@code null} and must not be after end.
	 * @param periodEnd
	 *            inclusive. Must not be {@code null} and must not be before
	 *            start.
	 * @param contributors
	 *            must not be {@code null}. Copy will be taken.
	 * @return never {@code null}
	 */
	public static ContributorUniverse of(Instant periodStart, Instant periodEnd, Collection<Contributor> contributors) {
		Objects.requireNonNull(periodStart, "periodStart");
		Objects.requireNonNull(periodEnd, "periodEnd");
		Objects.requireNonNull(contributors, "contributors");

		if (periodStart.isAfter(periodEnd)) {
			String msg = "start cannot be after end: %s %s";
			throw new IllegalArgumentException(String.format(msg, periodStart, periodEnd));
		}

		Collection<Contributor> copy = Collections.unmodifiableSet(new HashSet<>(contributors));

		return new ContributorUniverse() {

			@Override
			public Instant getPeriodStart() {
				return periodStart;
			}

			@Override
			public Instant getPeriodEnd() {
				return periodEnd;
			}

			@Override
			public Collection<Contributor> getContributors() {
				return copy;
			}
		};
	}
}
