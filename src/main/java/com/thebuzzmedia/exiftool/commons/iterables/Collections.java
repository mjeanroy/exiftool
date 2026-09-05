/*
 * Copyright 2011 The Buzz Media, LLC
 * Copyright 2015-2026 Mickael Jeanroy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thebuzzmedia.exiftool.commons.iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/// Static Collection Utilities.
public final class Collections {

	// Ensure non instantiation.
	private Collections() {
	}

	/// Check if collection is empty (`null` or empty).
	///
	/// @param values Collection of values.
	/// @param <T> Type of element in collection.
	/// @return `true` if collection is empty (`null` or empty), `false` otherwise.
	public static <T> boolean isEmpty(Collection<T> values) {
		return values == null || values.isEmpty();
	}

	/// Check if collection is not empty (not `null` and not empty).
	///
	/// @param values Collection of values.
	/// @param <T> Type of element in collection.
	/// @return `true` if collection is not empty (not `null` and not empty), `false` otherwise.
	public static <T> boolean isNotEmpty(Collection<T> values) {
		return !isEmpty(values);
	}

	/// Get size of collection.
	/// If collection is empty (null or empty), zero will be returned.
	///
	/// @param values Collection of values.
	/// @param <T> Type of element in collection.
	/// @return Size of collection.
	public static <T> int size(Collection<T> values) {
		return values == null ? 0 : values.size();
	}

	/// Create in memory collection from given iterable elements.
	/// - If `iterables` is null, an empty array list is returned.
	/// - If `iterables` is already an instance of [Collection], it is returned.
	/// - Otherwise, iterable structure is iterated and loaded into an [ArrayList].
	///
	/// @param iterables Given iterable.
	/// @param <T> Type of element in iterable structure.
	/// @return The collection result.
	public static <T> Collection<T> toCollection(Iterable<T> iterables) {
		if (iterables == null) {
			return new ArrayList<>(0);
		}

		if (iterables instanceof Collection) {
			return (Collection<T>) iterables;
		}

		return StreamSupport.stream(iterables.spliterator(), false).collect(Collectors.toList());
	}

	/// Add all elements of given iterable structure to given collection.
	///
	/// @param collection The collection.
	/// @param iterable The iterable structure.
	/// @param <T> Type of elements.
	public static <T> void addAll(Collection<T> collection, Iterable<T> iterable) {
		if (iterable == null || collection == null) {
			return;
		}

		for (T value : iterable) {
			collection.add(value);
		}
	}
}
