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

package com.thebuzzmedia.exiftool.commons.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Static Class Utilities.
 */
public final class ClassUtils {

	// Ensure non instantiation.
	private ClassUtils() {
	}

	/**
	 * Get the given class if available on classpath, otherwise throw {@link ReflectionException}.
	 *
	 * @param klass Class name.
	 * @return The class instance if available.
	 * @throws ReflectionException If the given class is not found in the classpath.
	 */
	public static Class<?> lookupClass(String klass) {
		try {
			return Class.forName(klass);
		}
		catch (ClassNotFoundException ex) {
			throw new ReflectionException(ex);
		}
	}

	/**
	 * Find static method with given return type and parameter types on given class.
	 *
	 * @param klass          The class.
	 * @param methodName     The method name.
	 * @param returnType     The method return type.
	 * @param parameterTypes Parameter types.
	 * @return The method.
	 * @throws ReflectionException If given method does not exist.
	 * @throws ReflectionException If given method is not accessible.
	 */
	public static MethodHandle findStaticMethod(Class<?> klass, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
		MethodHandles.Lookup lookup = MethodHandles.publicLookup();
		MethodType mt = MethodType.methodType(returnType, parameterTypes);

		try {
			return lookup.findStatic(klass, methodName, mt);
		}
		catch (NoSuchMethodException | IllegalAccessException ex) {
			throw new ReflectionException(ex);
		}
	}

	/**
	 * Find non-static method with given return type parameter types on given class.
	 *
	 * @param klass          The class.
	 * @param methodName     The method name.
	 * @param returnType     The method return type.
	 * @param parameterTypes Parameter types.
	 * @return The method.
	 * @throws ReflectionException If given method does not exist.
	 * @throws ReflectionException If given method is not accessible.
	 */
	public static MethodHandle findMethod(Class<?> klass, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
		MethodHandles.Lookup lookup = MethodHandles.publicLookup();
		MethodType mt = MethodType.methodType(returnType, parameterTypes);

		try {
			return lookup.findVirtual(klass, methodName, mt);
		}
		catch (NoSuchMethodException | IllegalAccessException ex) {
			throw new ReflectionException(ex);
		}
	}

	/**
	 * Invoke given static method.
	 *
	 * @param method The method to invoke.
	 * @param args   Method parameters.
	 * @return The method result.
	 * @throws ReflectionException If an error occurs while calling method.
	 */
	public static Object invokeStatic(MethodHandle method, Object... args) {
		return doInvoke(method, args);
	}

	/**
	 * Invoke given method.
	 *
	 * @param method The method to invoke.
	 * @param target The target instance.
	 * @param args   Method parameters.
	 * @return The method result.
	 * @throws ReflectionException If an error occurs while calling method.
	 */
	public static Object invoke(MethodHandle method, Object target, Object... args) {
		return doInvoke(method.bindTo(target), args);
	}

	/**
	 * Invoke given method with given arguments.
	 *
	 * @param method The method to invoke.
	 * @param args   Method parameters.
	 * @return The method result.
	 * @throws ReflectionException If an error occurs while calling method.
	 */
	private static Object doInvoke(MethodHandle method, Object... args) {
		try {
			return method.invokeWithArguments(args);
		}
		catch (Throwable ex) {
			throw new ReflectionException(ex);
		}
	}

	/**
	 * Check if given class is available on classpath.
	 *
	 * @param klass Class name to check (FQN).
	 * @return True if class is available, false otherwise.
	 */
	public static boolean isPresent(String klass) {
		try {
			Class.forName(klass);
			return true;
		}
		catch (ClassNotFoundException ex) {
			return false;
		}
	}

	public static class ReflectionException extends RuntimeException {
		private ReflectionException(Throwable cause) {
			super(cause);
		}
	}
}
