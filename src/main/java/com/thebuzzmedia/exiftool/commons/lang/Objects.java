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
 * Static Objects Utilities.
 */
public final class Objects {

	// Ensure non instantiation.
	private Objects() {
	}

	/**
	 * Returns the first parameter that is not {@code null}.
	 *
	 * @param val1 First value.
	 * @param val2 Second value.
	 * @param others Other values.
	 * @param <T> Type of parameters.
	 * @return The first non {@code null} parameter.
	 */
	@SafeVarargs
	public static <T> T firstNonNull(T val1, T val2, T... others) {
		if (val1 != null) {
			return val1;
		}

		if (val2 != null) {
			return val2;
		}

		for (T current : others) {
			if (current != null) {
				return current;
			}
		}

		return null;
	}
}
