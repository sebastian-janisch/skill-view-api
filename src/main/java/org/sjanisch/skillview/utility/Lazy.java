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
package org.sjanisch.skillview.utility;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A thread safe implementation of a lazy initialiser.
 * <p>
 * Typical use cases are the creation of singletons or preventing to call into
 * overridable methods from within the constructor.
 * <p>
 * {@link #hashCode()} and {@link #equals(Object)} are implemented against
 * {@link #get()} which implies that the underlying value will be resolved if
 * {@link #hashCode()} or {@link #equals(Object)} are invoked.
 * 
 * @author sebastianjanisch
 *
 */
public class Lazy<E> {

	private ConcurrentHashMap<String, E> value = new ConcurrentHashMap<>();
	private Supplier<E> valueSupplier;

	private Lazy(Supplier<E> valueSupplier) {
		this.valueSupplier = Objects.requireNonNull(valueSupplier, "valueSupplier");
	}

	/**
	 * 
	 * @param valueSupplier
	 *            must not be {@code null}
	 */
	public static <E> Lazy<E> of(Supplier<E> valueSupplier) {
		return new Lazy<>(valueSupplier);
	}

	/**
	 * 
	 * @return triggers the resolution of the underlying lazy value of simply
	 *         returns it if it is already resolved. Blocks if resolution is
	 *         already in progress.
	 */
	public E get() {
		return value.computeIfAbsent("VALUE", __ -> valueSupplier.get());
	}

	@Override
	public int hashCode() {
		return get().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		E value = get();
		if (value == null && obj == null) {
			return true;
		}

		if (value == null ^ obj == null) {
			return false;
		}

		return value.equals(obj);
	}

}
