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

import com.thebuzzmedia.exiftool.tests.junit.SystemOutExtension;
import com.thebuzzmedia.exiftool.tests.junit.SystemOutExtension.SystemOut;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SystemOutExtension.class)
abstract class AbstractLoggerTest {

	private static final String INFO = "INFO";
	private static final String WARN = "WARN";
	private static final String ERROR = "ERROR";
	private static final String TRACE = "TRACE";
	private static final String DEBUG = "DEBUG";

	@Test
	void it_should_display_info(SystemOut out) {
		getLogger().info("message");
		verifyOutput(out, INFO, "message");
	}

	@Test
	void it_should_display_info_with_one_parameter(SystemOut out) {
		getLogger().info("A message: {}", "test");
		verifyOutput(out, INFO, "A message: test");
	}

	@Test
	void it_should_display_info_with_two_parameters(SystemOut out) {
		getLogger().info("A message: {} -- {}", "p1", "p2");
		verifyOutput(out, INFO, "A message: p1 -- p2");
	}

	@Test
	void it_should_display_warn(SystemOut out) {
		getLogger().warn("message");
		verifyOutput(out, WARN, "message");
	}

	@Test
	void it_should_display_warn_with_one_parameter(SystemOut out) {
		getLogger().warn("A message: {}", "p1");
		verifyOutput(out, WARN, "A message: p1");
	}

	@Test
	void it_should_display_warn_with_two_parameters(SystemOut out) {
		getLogger().warn("A message: {} -- {}", "p1", "p2");
		verifyOutput(out, WARN, "A message: p1 -- p2");
	}

	@Test
	void it_should_display_error(SystemOut out) {
		getLogger().error("message");
		verifyOutput(out, ERROR, "message");
	}

	@Test
	void it_should_display_error_with_one_parameter(SystemOut out) {
		getLogger().error("A message: {}", "p1");
		verifyOutput(out, ERROR, "A message: p1");
	}

	@Test
	void it_should_display_error_with_two_parameters(SystemOut out) {
		getLogger().error("A message: {} -- {}", "p1", "p2");
		verifyOutput(out, ERROR, "A message: p1 -- p2");
	}

	@Test
	void it_should_display_trace(SystemOut out) {
		getLogger().trace("message");
		verifyOutput(out, TRACE, "message");
	}

	@Test
	void it_should_display_trace_with_one_parameter(SystemOut out) {
		getLogger().trace("A message: {}", "p1");
		verifyOutput(out, TRACE, "A message: p1");
	}

	@Test
	void it_should_display_trace_with_two_parameters(SystemOut out) {
		getLogger().trace("A message: {} -- {}", "p1", "p2");
		verifyOutput(out, TRACE, "A message: p1 -- p2");
	}

	@Test
	void it_should_display_debug(SystemOut out) {
		getLogger().debug("message");
		verifyOutput(out, DEBUG, "message");
	}

	@Test
	void it_should_display_debug_with_one_parameter(SystemOut out) {
		getLogger().debug("A message: {}", "p1");
		verifyOutput(out, DEBUG, "A message: p1");
	}

	@Test
	void it_should_display_debug_with_two_parameters(SystemOut out) {
		getLogger().debug("A message: {} -- {}", "p1", "p2");
		verifyOutput(out, DEBUG, "A message: p1 -- p2");
	}

	@Test
	void it_should_not_display_debug_if_disabled(SystemOut out) {
		getLoggerWithoutDebug().debug("message");
		assertThat(out.getOut()).isEmpty();
	}

	@Test
	void it_should_check_if_debug_is_enabled() {
		Logger logger1 = getLogger();
		assertThat(logger1.isDebugEnabled()).isTrue();

		Logger logger2 = getLoggerWithoutDebug();
		assertThat(logger2.isDebugEnabled()).isFalse();
	}

	@Test
	void it_should_display_error_exception(SystemOut out) {
		RuntimeException ex = new RuntimeException("Error Message");
		getLogger().error(ex.getMessage(), ex);
		verifyException(out, "ERROR", ex);
	}

	@Test
	void it_should_display_warn_exception(SystemOut out) {
		RuntimeException ex = new RuntimeException("Error Message");
		getLogger().warn(ex.getMessage(), ex);
		verifyException(out, "WARN", ex);
	}

	/// Create default logger to test.
	///
	/// @return Logger.
	abstract Logger getLogger();

	/// Create logger, with debug disabled, to test.
	///
	/// @return Logger.
	abstract Logger getLoggerWithoutDebug();

	private void verifyOutput(SystemOut sysOut, String level, String message) {
		String out = sysOut.getTrimmedOut();
		assertThat(out).contains(level);
		assertThat(out).endsWith(message);
	}

	private void verifyException(SystemOut sysOut, String level, Exception ex) {
		String out = sysOut.getTrimmedOut();
		assertThat(out).contains(level);
		assertThat(out).contains(ex.getMessage());
		assertThat(out).contains("at com.thebuzzmedia.exiftool.logs.AbstractLoggerTest");
	}
}
