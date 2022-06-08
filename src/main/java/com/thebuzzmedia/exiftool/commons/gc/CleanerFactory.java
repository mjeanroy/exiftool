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

package com.thebuzzmedia.exiftool.commons.gc;

import com.thebuzzmedia.exiftool.commons.reflection.ClassUtils;

/**
 * Factory for {@link Cleaner}.
 */
public final class CleanerFactory {

	// Ensure non instantiation.
	private CleanerFactory() {
	}

	/**
	 * Create GC cleaner using the best available implementation.
	 *
	 * @return Cleaner instance.
	 */
	public static Cleaner createCleaner() {
		if (ClassUtils.isPresent("java.lang.ref.Cleaner")) {
			return JdkCleaner.create();
		}

		if (ClassUtils.isPresent("sun.misc.Cleaner")) {
			return UnsafeCleaner.create();
		}

		return NoOpCleaner.create();
	}
}
