package org.sjanisch.skillview.contribution.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.sjanisch.skillview.contribution.api.Contribution;
import org.sjanisch.skillview.contribution.api.ContributionService;

/**
 * Holds multiple {@link ContributionService} instances and combines their
 * output.
 * <p>
 * This implementation is immutable and thread-safe.
 * 
 * @author sebastianjanisch
 *
 */
public class MultiContributionService implements ContributionService {

	private final Collection<ContributionService> services;

	/**
	 * 
	 * @param services
	 *            must not be {@code null}. Copy will be taken.
	 */
	public MultiContributionService(Collection<ContributionService> services) {
		Objects.requireNonNull(services, "services");
		this.services = Collections.unmodifiableCollection(new LinkedList<>(services));
	}

	@Override
	public Stream<Contribution> retrieveContributions(Instant startExclusive, Instant endInclusive) {
		Objects.requireNonNull(startExclusive, "startExclusive");
		Objects.requireNonNull(endInclusive, "endInclusive");

		Collection<Stream<Contribution>> streams = ConcurrentHashMap.newKeySet(services.size());

		// @formatter:off
		Stream<Contribution> result = services
				.stream()
				.map(service -> service.retrieveContributions(startExclusive, endInclusive))
				.peek(contributions -> streams.add(contributions))
				.reduce(Stream.empty(), Stream::concat);
		// @formatter:on

		result.onClose(() -> streams.forEach(Stream::close));

		return result;
	}

}
