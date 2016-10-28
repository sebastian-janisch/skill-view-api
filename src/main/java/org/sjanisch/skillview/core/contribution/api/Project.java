package org.sjanisch.skillview.core.contribution.api;

import java.util.Objects;

/**
 * Designates a project name.
 * <p>
 * {@link #hashCode()} and {@link #equals(Object)} are implemented against
 * {@link #getValue()}.
 * <p>
 * Implementors must retain thread-safety and immutability.
 * 
 * @author sebastianjanisch
 *
 */
public interface Project {

	/**
	 * 
	 * @return never {@code null}
	 */
	String getValue();

	/**
	 * 
	 * @param value
	 *            must not be {@code null} or whitespace.
	 * @return never {@code null}
	 */
	public static Project of(String value) {
		Objects.requireNonNull(value, "value");
		return new Project() {

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
				return obj != null && obj instanceof Project && ((Project) obj).getValue().equals(value);
			}
		};
	}

}
