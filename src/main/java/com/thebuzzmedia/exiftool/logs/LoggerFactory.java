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

package com.thebuzzmedia.exiftool.logs;

import java.util.Iterator;
import java.util.ServiceLoader;

import static com.thebuzzmedia.exiftool.commons.reflection.DependencyUtils.isLog4j2Available;
import static com.thebuzzmedia.exiftool.commons.reflection.DependencyUtils.isLog4jAvailable;
import static com.thebuzzmedia.exiftool.commons.reflection.DependencyUtils.isSlf4jAvailable;

/// Factory to use to create [com.thebuzzmedia.exiftool.logs.Logger] instances.
///
/// Appropriate implementation will be used depending on classpath.
/// Verification is done in the following order:
///   - Check from [LoggerProvider] registered using Java Service Provider Interface (see [ServiceLoader]).
///   - If slf4j is defined, then it will be used.
///   - If slf4j is defined, then it will be used.
///   - If log4j is defined, it will be used.
///   - Finally, instance of [com.thebuzzmedia.exiftool.logs.DefaultLogger] is used.
public final class LoggerFactory {

	// Ensure non instantiation.
	private LoggerFactory() {
	}

	/// The custom logger provider provided using the Service Provider Interface.
	private static final LoggerProvider loggerProvider;

	static {
		// First, discover using the ServiceProvider API.
		ServiceLoader<LoggerProvider> loggerProviders = ServiceLoader.load(LoggerProvider.class);
		Iterator<LoggerProvider> it = loggerProviders.iterator();
		loggerProvider = it.hasNext() ? it.next() : null;
	}

	/// Return a logger named corresponding to the class passed as parameter,
	///
	/// @param klass the returned logger will be named after clazz.
	/// @return Logger implementation.
	public static Logger getLogger(Class<?> klass) {
		// First, discover using the ServiceProvider API.
		if (loggerProvider != null) {
			return loggerProvider.getLogger(klass);
		}

		// Then, try using classpath detection.

		// 1- First try slf4j
		if (isSlf4jAvailable()) {
			return new LoggerSlf4j(klass);
		}

		// 2- Then, try log4j2
		if (isLog4j2Available()) {
			return new LoggerLog4j2(klass);
		}

		// 3- Then, try log4j
		if (isLog4jAvailable()) {
			return new LoggerLog4j(klass);
		}

		// 4- Return default logger...
		return new DefaultLogger(Boolean.getBoolean("exiftool.debug"));
	}
}
