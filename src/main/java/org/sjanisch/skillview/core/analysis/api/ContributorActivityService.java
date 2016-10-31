package org.sjanisch.skillview.core.analysis.api;

import java.time.Instant;

/**
 * Gives access to {@link ContributorActivity} instances.
 * <p>
 * Implementors of this interface must retain thread-safety.
 * 
 * @author sebastianjanisch
 *
 */
public interface ContributorActivityService {

	/**
	 * @param startExclusive
	 *            must not be {@code null}
	 * @param endInclusive
	 *            must not be {@code null}
	 * @return never {@code null}
	 */
	ContributorActivity getContributorActivity(Instant startExclusive, Instant endInclusive);

}
