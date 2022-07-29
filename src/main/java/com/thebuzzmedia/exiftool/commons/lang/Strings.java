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

/**
 * Static String Utilities.
 */
public final class Strings {

	// Ensure non instantiation.
	private Strings() {
	}

	/**
	 * Check that given string is not {@code null} or empty.
	 *
	 * @param value Given string to check.
	 * @return {@code true} if {@code value} is {@code null} or empty.
	 */
	public static boolean isNotEmpty(String value) {
		return value != null && !value.isEmpty();
	}


	/**
	 * Try to parse a string as an Integer and return the value if valid, or returns fallback value
	 *.
	 * @param value The string to convert to an Integer
	 * @return Optional Integer which will be parsed if valid, and {@code fallbackValue} if not.
	 */
	public static int tryParseInt(String value, int fallbackValue) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return fallbackValue;
		}
	}
}
