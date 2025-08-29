/**
 * Copyright 2011 The Buzz Media, LLC
 * Copyright 2015-2019 Mickael Jeanroy
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

package com.thebuzzmedia.exiftool.commons.lang;

import com.thebuzzmedia.exiftool.exceptions.UnreadableFileException;
import com.thebuzzmedia.exiftool.exceptions.UnwritableFileException;

import java.io.File;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Static PreConditions Utilities.
 */
public final class PreConditions {

	// Ensure non instantiation.
	private PreConditions() {
	}

	/**
	 * Ensures that a string is:
	 * <ul>
	 *   <li>Not {@code null}.</li>
	 *   <li>Not empty.</li>
	 *   <li>Not blank (i.e contains at least one character other than space).</li>
	 * </ul>
	 *
	 * @param val     Value to check.
	 * @param message Message passed to thrown exception.
	 * @return Original value if it is not {@code null}.
	 * @throws java.lang.NullPointerException     If {@code val} is {@code null}.
	 * @throws java.lang.IllegalArgumentException If {@code val} is empty or blank.
	 */
	public static String notBlank(String val, String message) {
		requireNonNull(val, message);
		if (val.isEmpty() || val.trim().isEmpty()) {
			throw new IllegalArgumentException(message);
		}

		return val;
	}

	/**
	 * Ensures that array is:
	 * <ul>
	 *   <li>Not {@code null}.</li>
	 *   <li>Not empty.</li>
	 * </ul>
	 *
	 * @param val     Value to check.
	 * @param message Message passed to thrown exception.
	 * @param <T>     Type of elements in array.
	 * @return Original value if it is not empty.
	 * @throws java.lang.NullPointerException     If {@code val} is {@code null}.
	 * @throws java.lang.IllegalArgumentException If {@code val} is empty.
	 */
	public static <T> T[] notEmpty(T[] val, String message) {
		requireNonNull(val, message);
		if (val.length == 0) {
			throw new IllegalArgumentException(message);
		}

		return val;
	}

	/**
	 * Ensures that map is:
	 * <ul>
	 *   <li>Not {@code null}.</li>
	 *   <li>Not empty.</li>
	 * </ul>
	 *
	 * @param val     Value to check.
	 * @param message Message passed to thrown exception.
	 * @param <T>     Type of keys in map.
	 * @param <U>     Type of values in map.
	 * @return Original value if it is not empty.
	 * @throws java.lang.NullPointerException     If {@code val} is {@code null}.
	 * @throws java.lang.IllegalArgumentException If {@code val} is empty.
	 */
	public static <T, U> Map<T, U> notEmpty(Map<T, U> val, String message) {
		requireNonNull(val, message);
		if (val.isEmpty()) {
			throw new IllegalArgumentException(message);
		}

		return val;
	}

	/**
	 * Ensures that iterable element is:
	 * <ul>
	 *   <li>Not {@code null}.</li>
	 *   <li>Not empty.</li>
	 * </ul>
	 *
	 * @param val     Value to check.
	 * @param message Message passed to thrown exception.
	 * @param <T>     Type of elements in iterable structure.
	 * @return Original value if it is not empty.
	 * @throws java.lang.NullPointerException     If {@code val} is {@code null}.
	 * @throws java.lang.IllegalArgumentException If {@code val} is empty.
	 */
	public static <T> Iterable<T> notEmpty(Iterable<T> val, String message) {
		requireNonNull(val, message);

		if (!val.iterator().hasNext()) {
			throw new IllegalArgumentException(message);
		}

		return val;
	}

	/**
	 * Check if given number is strictly positive (strictly greater than zero).
	 *
	 * @param nb      Number.
	 * @param message Error message.
	 * @param <T>     Type of number.
	 * @return Original number.
	 * @throws NullPointerException     If {@code nb} is {@code null}.
	 * @throws IllegalArgumentException If {@code nb} is less than or equal to zero.
	 */
	public static <T extends Number> T isPositive(T nb, String message) {
		requireNonNull(nb, message);
		if (nb.doubleValue() <= 0) {
			throw new IllegalArgumentException(message);
		}

		return nb;
	}

	/**
	 * Check that a given file exist and is readable.
	 *
	 * @param file    File to check.
	 * @param message Error message.
	 * @return Original file.
	 * @throws NullPointerException    If {@code file} is {@code null}.
	 * @throws UnreadableFileException If {@code file} does not exist.
	 * @throws UnreadableFileException If {@code file} cannot be read.
	 */
	public static File isReadable(File file, String message) {
		requireNonNull(file, message);
		if (!file.exists() || !file.canRead()) {
			throw new UnreadableFileException(file, message);
		}

		return file;
	}

	/**
	 * Check that a given file exist and is writable.
	 *
	 * @param file    File to check.
	 * @param message Error message.
	 * @return Original file.
	 * @throws NullPointerException    If {@code file} is {@code null}.
	 * @throws UnreadableFileException If {@code file} does not exist.
	 * @throws UnreadableFileException If {@code file} cannot be updated.
	 */
	public static File isWritable(File file, String message) {
		requireNonNull(file, message);
		if (!file.exists() || !file.canWrite()) {
			throw new UnwritableFileException(file, message);
		}

		return file;
	}
}
