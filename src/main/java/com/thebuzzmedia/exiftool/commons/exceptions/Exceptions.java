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

package com.thebuzzmedia.exiftool.commons.exceptions;

/// Static Exceptions Utilities.
public final class Exceptions {

	private Exceptions() {
	}

	/// Coerce an unchecked Throwable to a RuntimeException:
	///
	/// - If the Throwable is an Error, throw it.
	/// - If it is a [RuntimeException] return it.
	/// - Otherwise, throw [IllegalStateException].
	///
	/// @param t Original throwable.
	/// @return Wrapped exception.
	public static RuntimeException launderThrowable(Throwable t) {
		if (t instanceof RuntimeException) {
			return (RuntimeException) t;
		}
		else if (t instanceof Error) {
			throw (Error) t;
		}
		else {
			throw new IllegalStateException("Not unchecked", t);
		}
	}
}
