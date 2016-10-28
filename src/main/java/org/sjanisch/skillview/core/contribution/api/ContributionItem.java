package org.sjanisch.skillview.core.contribution.api;

import java.util.Objects;

/**
 * A contribution item describes a <b>single</b> addition to a version control
 * system which could be part of a larger addition (e.g. one file out of a multi
 * file commit).
 * <p>
 * {@link #hashCode()} and {@link #equals(Object)} are not be implemented.
 * <p>
 * Implementors must retain thread-safety and immutability.
 * 
 * @author sebastianjanisch
 *
 */
public interface ContributionItem {

	/**
	 * 
	 * @return the path name of this contribution. Never {@code null} or
	 *         whitespace.
	 */
	String getPath();

	/**
	 * 
	 * @return the content of this contribution. Never {@code null}.
	 */
	String getContent();

	/**
	 * 
	 * @return the previous content of this contribution. Never {@code null}.
	 */
	String getPreviousContent();

	/**
	 * Must all be non {@code null}.
	 * 
	 * @param path
	 * @param previousContent
	 * @param content
	 * @return never {@code null}.
	 */
	public static ContributionItem of(String path, String previousContent, String content) {
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(previousContent, "previousContent");
		Objects.requireNonNull(content, "content");

		return new ContributionItem() {

			@Override
			public String getPreviousContent() {
				return previousContent;
			}

			@Override
			public String getPath() {
				return path;
			}

			@Override
			public String getContent() {
				return content;
			}

			@Override
			public String toString() {
				return String.format("%s[%s]", getClass().getSimpleName(), path);
			}
		};

	}

}
