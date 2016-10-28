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
package org.sjanisch.skillview.core.contribution.api;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

/**
 * A contribution describes an addition to a version control system at a given
 * point in time carried out by a {@link Contributor contributor} which consists
 * of potentially multiple {@link ContributionItem contribution items}.
 * <p>
 * Implementors must retain thread-safety and immutability.
 * 
 * @author sebastianjanisch
 *
 */
public interface Contribution {

	/**
	 * 
	 * @return never {@code null}.
	 */
	ContributionId getId();

	/**
	 * 
	 * @return the project that this contribution originated from. Never
	 *         {@code null}.
	 */
	Project getProject();

	/**
	 * 
	 * @return never {@code null}.
	 */
	Contributor getContributor();

	/**
	 * 
	 * @return the time of this contribution. Never {@code null}.
	 */
	Instant getContributionTime();

	/**
	 * 
	 * @return the comment message of this contribution. Never {@code null}.
	 */
	Optional<String> getMessage();

	/**
	 * 
	 * @return a collection (possibly unmodifiable) of contribution items that
	 *         belong to this contribution. Never {@code null}.
	 */
	Collection<ContributionItem> getContributionItems();

}
