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
package org.sjanisch.skillview.analysis.api;

import java.util.Objects;

/**
 * A score originator defines the process that computed a score.
 * <p>
 * {@link #hashCode()} and {@link #equals(Object)} are implemented against
 * {@link #getValue()}.
 * <p>
 * Implementors must retain thread-safety and immutability.
 * 
 * @author sebastianjanisch
 *
 */
public interface ScoreOriginator {

	/**
	 * 
	 * @return never {@code null} or whitespace.
	 */
	String getValue();

	/**
	 * 
	 * @param value
	 *            must not be {@code null} or whitespace
	 * @return never {@code null}
	 */
	public static ScoreOriginator of(String value) {
		Objects.requireNonNull(value);

		return new ScoreOriginator() {
			@Override
			public String getValue() {
				return value;
			}

			@Override
			public String toString() {
				return String.format("%s[%s]", getClass().getSimpleName(), value);
			}

			@Override
			public int hashCode() {
				return value.hashCode();
			}

			@Override
			public boolean equals(Object obj) {
				return obj != null && obj instanceof ScoreOriginator
						&& ((ScoreOriginator) obj).getValue().equals(value);
			}
		};
	}

}
