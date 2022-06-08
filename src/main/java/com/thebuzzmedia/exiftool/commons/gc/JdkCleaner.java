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

import static com.thebuzzmedia.exiftool.commons.lang.PreConditions.notNull;
import static com.thebuzzmedia.exiftool.commons.reflection.ClassUtils.findMethod;
import static com.thebuzzmedia.exiftool.commons.reflection.ClassUtils.findStaticMethod;
import static com.thebuzzmedia.exiftool.commons.reflection.ClassUtils.invoke;
import static com.thebuzzmedia.exiftool.commons.reflection.ClassUtils.invokeStatic;
import static com.thebuzzmedia.exiftool.commons.reflection.ClassUtils.lookupClass;

final class JdkCleaner implements Cleaner {

	static JdkCleaner create() {
		Class<?> cleanerClass = lookupClass("java.lang.ref.Cleaner");
		Class<?> cleanableClass = lookupClass("java.lang.ref.Cleaner$Cleanable");
		MethodHandle create = findStaticMethod(cleanerClass, "create", cleanerClass);

		Object cleaner = invokeStatic(create);
		MethodHandle register = findMethod(cleanerClass, "register", cleanableClass, Object.class, Runnable.class);
		return new JdkCleaner(cleaner, register);
	}

	private final Object cleaner;
	private final MethodHandle register;

	private JdkCleaner(Object cleaner, MethodHandle register) {
		this.cleaner = notNull(cleaner, "Cleaner must not be null");
		this.register = notNull(register, "Register method must not be null");
	}

	@Override
	public void register(Object ref, Runnable cleanupTask) {
		invoke(register, cleaner, ref, cleanupTask);
	}
}
