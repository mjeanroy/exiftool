/*
 * Copyright 2011 The Buzz Media, LLC
 * Copyright 2015-2026 Mickael Jeanroy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thebuzzmedia.exiftool.tests;

import java.lang.reflect.Field;

/// Reflection Utilities used in unit tests.
public final class ReflectionTestUtils {

	private ReflectionTestUtils() {
	}

	/// Read private field on given class instance.
	///
	/// @param o The class instance (must not be `null`).
	/// @param name Name of the field to read.
	/// @param <T> Type of class.
	/// @return The value of given field (may be `null`).
	public static <T> T readPrivateField(Object o, String name) {
		return doRead(o.getClass(), o, name);
	}

	/// Read static private field on given class.
	///
	/// @param klass The class.
	/// @param name Name of the field.
	/// @param <T> Type of field value.
	/// @return The field value, may be `null`.
	public static <T> T readStaticPrivateField(Class<?> klass, String name) {
		return doRead(klass, null, name);
	}

	@SuppressWarnings("unchecked")
	private static <T> T doRead(Class<?> klass, Object instance, String name) {
		try {
			Field field = klass.getDeclaredField(name);
			field.setAccessible(true);
			return (T) field.get(instance);
		}
		catch (Exception ex) {
			throw new AssertionError(ex);
		}
	}

	/// Write value on private field of a given class instance.
	///
	/// @param o The instance.
	/// @param name Name of the field to write.
	/// @param value The value.
	/// @param <T> Type of class.
	public static <T> void writePrivateField(Object o, String name, T value) {
		doWrite(o.getClass(), o, name, value);
	}

	/// Write value on a private field of a given class.
	///
	/// @param name Name of the field to write.
	/// @param value The value.
	/// @param <T> Type of class.
	private static <T, V> void doWrite(Class<T> klass, Object instance, String name, V value) {
		try {
			Field field = klass.getDeclaredField(name);
			field.setAccessible(true);
			field.set(instance, value);
		}
		catch (Exception ex) {
			throw new AssertionError(ex);
		}
	}

	/// Check if given class is available on classpath.
	///
	/// @param klazz Class name to check (FQN).
	/// @return True if class is available, false otherwise.
	public static boolean isClassAvailable(String klazz) {
		try {
			Class.forName(klazz);
			return true;
		}
		catch (ClassNotFoundException ex) {
			return false;
		}
	}
}
