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

package com.thebuzzmedia.exiftool.tests;

import org.mockito.ArgumentMatchers;

import java.util.List;

/// Static Mockito Utilities.
/// Used in test only.
public final class MockitoTestUtils {

	// Ensure non instantiation.
	private MockitoTestUtils() {
	}

	/// Replacement for deprecated Mockito#anyListOf.
	///
	/// @param klass The class.
	/// @param <T> Type of elements.
	/// @return List of elements of type T.
	public static <T> List<T> anyListOf(Class<T> klass) {
		return ArgumentMatchers.anyList();
	}
}
