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
package org.sjanisch.skillview.contribution.api;

import java.time.Instant;
import java.util.stream.Stream;

/**
 * Offers functionality to retrieve {@link Contribution contributions}.
 * 
 * @author sebastianjanisch
 *
 */
public interface ContributionService {

	/**
	 * Note that callers need to surround the returned stream with a try-with
	 * statement.
	 * 
	 * @param startExclusive
	 *            must not be {@code null}
	 * @param endInclusive
	 *            must not be {@code null}
	 * @return an stream of contributions between given time frame. Never
	 *         {@code null}.
	 */
	Stream<Contribution> retrieveContributions(Instant startExclusive, Instant endInclusive);

}
