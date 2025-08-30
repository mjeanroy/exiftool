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

package com.thebuzzmedia.exiftool.logs;

/**
 * A provider for {@link Logger} instance, that can be used with the Service Provider
 * Interface (see {@link java.util.ServiceLoader}).
 */
public interface LoggerProvider {

	/**
	 * Create the logger.
	 *
	 * @param klass The logger name.
	 * @return The logger instance.
	 */
	Logger getLogger(Class<?> klass);
}
