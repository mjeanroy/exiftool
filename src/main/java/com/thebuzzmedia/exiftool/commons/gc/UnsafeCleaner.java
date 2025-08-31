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

import java.lang.invoke.MethodHandle;

import static com.thebuzzmedia.exiftool.commons.reflection.ClassUtils.findStaticMethod;
import static com.thebuzzmedia.exiftool.commons.reflection.ClassUtils.invokeStatic;
import static com.thebuzzmedia.exiftool.commons.reflection.ClassUtils.lookupClass;
import static java.util.Objects.requireNonNull;

/**
 * Implementation of {@link Cleaner} using {@code sun.misc.Cleaner} implementation.
 * <p>
 * This cleaner is created using reflection, as {@code sun.misc.Cleaner} does not exist in Java >= 9.
 * Once this library will support java >= 9 only, this implementation will be removed.
 */
final class UnsafeCleaner implements Cleaner {

	/**
	 * Create "unsafe" cleaner.
	 *
	 * @return Cleaner, using {@code sun.misc.Cleaner} available with Java < 9.
	 */
	static UnsafeCleaner create() {
		Class<?> cleanerClass = lookupClass("sun.misc.Cleaner");
		MethodHandle register = findStaticMethod(cleanerClass, "create", cleanerClass, Object.class, Runnable.class);
		return new UnsafeCleaner(register);
	}

	/**
	 * Static register method on {@code sun.misc.Cleaner} class.
	 */
	private final MethodHandle register;

	private UnsafeCleaner(MethodHandle register) {
		this.register = requireNonNull(register, "Register method must not be null");
	}

	@Override
	public void register(Object ref, Runnable cleanupTask) {
		invokeStatic(register, ref, cleanupTask);
	}
}
