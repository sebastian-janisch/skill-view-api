package org.sjanisch.skillview.core.analysis.api;

/**
 * Adds write capabilities to {@link ContributorActivityService}.
 * <p>
 * Implementors of this interface must retain thread-safety.
 * 
 * @author sebastianjanisch
 *
 */
public interface WritableContributorActivityService extends ContributorActivityService {

	/**
	 * 
	 * @param contributorActivity
	 *            must not be {@code null}
	 */
	void writeContributorActivity(ContributorActivity contributorActivity);

}
