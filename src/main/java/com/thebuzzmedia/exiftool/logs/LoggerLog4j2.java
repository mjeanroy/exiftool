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

import org.apache.logging.log4j.LogManager;

/**
 * Implementation of logger using log4j2 as
 * internal implementation.
 */
class LoggerLog4j2 implements Logger {

	/**
	 * Internal Logger.
	 */
	private final org.apache.logging.log4j.Logger log;

	/**
	 * Create logger.
	 * This constructor should only be called by {@link LoggerFactory}.
	 *
	 * @param name Logger name.
	 */
	LoggerLog4j2(Class<?> name) {
		this.log = LogManager.getLogger(name);
	}

	@Override
	public void trace(String message) {
		log.trace(message);
	}

	@Override
	public void trace(String message, Object p1) {
		log.trace(message, p1);
	}

	@Override
	public void trace(String message, Object p1, Object p2) {
		log.trace(message, p1, p2);
	}

	@Override
	public void info(String message) {
		log.info(message);
	}

	@Override
	public void info(String message, Object p1) {
		log.info(message, p1);
	}

	@Override
	public void info(String message, Object p1, Object p2) {
		log.info(message, p1, p2);
	}

	@Override
	public void debug(String message) {
		log.debug(message);
	}

	@Override
	public void debug(String message, Object p1) {
		log.debug(message, p1);
	}

	@Override
	public void debug(String message, Object p1, Object p2) {
		log.debug(message, p1, p2);
	}

	@Override
	public void warn(String message) {
		log.warn(message);
	}

	@Override
	public void warn(String message, Throwable ex) {
		log.warn(message, ex);
	}

	@Override
	public void warn(String message, Object p1) {
		log.warn(message, p1);
	}

	@Override
	public void warn(String message, Object p1, Object p2) {
		log.warn(message, p1, p2);
	}

	@Override
	public void error(String message) {
		log.error(message);
	}

	@Override
	public void error(String message, Object p1) {
		log.error(message, p1);
	}

	@Override
	public void error(String message, Object p1, Object p2) {
		log.error(message, p1, p2);
	}

	@Override
	public void error(String message, Throwable ex) {
		log.error(message, ex);
	}

	@Override
	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}
}
