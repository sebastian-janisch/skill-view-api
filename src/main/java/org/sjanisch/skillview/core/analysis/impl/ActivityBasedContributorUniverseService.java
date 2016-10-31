package org.sjanisch.skillview.core.analysis.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

import org.sjanisch.skillview.core.analysis.api.ContributorActivity;
import org.sjanisch.skillview.core.analysis.api.ContributorActivityService;
import org.sjanisch.skillview.core.analysis.api.ContributorUniverse;
import org.sjanisch.skillview.core.analysis.api.ContributorUniverseService;
import org.sjanisch.skillview.core.contribution.api.Contributor;

/**
 * 
 * @author sebastianjanisch
 *
 */
public class ActivityBasedContributorUniverseService implements ContributorUniverseService {

	private final ContributorActivityService contributorActivityService;
	private final Duration grandFathering;

	/**
	 * 
	 * @param contributorActivityService
	 *            must not be {@code null}
	 * @param grandFathering
	 *            indicates how long a {@link Contributor contributor} can stay
	 *            inactive without getting dropped out of the universe. Must not
	 *            be {@code null}.
	 */
	public ActivityBasedContributorUniverseService(ContributorActivityService contributorActivityService,
			Duration grandFathering) {
		this.contributorActivityService = Objects.requireNonNull(contributorActivityService,
				"contributorActivityService");
		this.grandFathering = Objects.requireNonNull(grandFathering, "grandFathering");
	}

	@Override
	public ContributorUniverse getContributorUniverse(Instant startExclusive, Instant endInclusive) {
		Objects.requireNonNull(startExclusive, "startExclusive");
		Objects.requireNonNull(endInclusive, "endInclusive");

		Instant adjustedStartExclusive = startExclusive.minus(grandFathering);

		ContributorActivity contributorActivity = contributorActivityService
				.getContributorActivity(adjustedStartExclusive, endInclusive);

		Collection<Contributor> result = contributorActivity.getActiveContributors(startExclusive, startExclusive);

		return ContributorUniverse.of(startExclusive, endInclusive, result);
	}

}
