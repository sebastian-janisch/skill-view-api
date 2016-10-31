package org.sjanisch.skillview.core.analysis.api;

import java.time.Instant;

/**
 * Gives access to {@link ContributorUniverse} instances.
 * <p>
 * Implementors of this interface must retain thread-safety.
 * 
 * @author sebastianjanisch
 *
 */
public interface ContributorUniverseService {

	/**
	 * 
	 * @param startExclusive
	 *            must not be {@code null}
	 * @param endInclusive
	 *            must not be {@code null}
	 * @return never {@code null}
	 */
	ContributorUniverse getContributorUniverse(Instant startExclusive, Instant endInclusive);

}
