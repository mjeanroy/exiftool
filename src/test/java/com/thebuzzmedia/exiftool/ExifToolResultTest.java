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
package com.thebuzzmedia.exiftool;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExifToolResultTest {

	@Test
	void it_should_create_result() {
		ExifToolResult result = new ExifToolResult("[{}]\n", "Warning: foo\n", 0);

		assertThat(result.getOutput()).isEqualTo("[{}]\n");
		assertThat(result.getErrors()).isEqualTo("Warning: foo\n");
		assertThat(result.getExitCode()).isZero();
	}

	@Test
	void it_should_create_result_without_exit_code() {
		ExifToolResult result = new ExifToolResult("", "", null);

		assertThat(result.getOutput()).isEmpty();
		assertThat(result.getErrors()).isEmpty();
		assertThat(result.getExitCode()).isNull();
	}

	@Test
	void it_should_fail_without_output() {
		assertThatThrownBy(() -> new ExifToolResult(null, "", null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Output cannot be null.");
	}

	@Test
	void it_should_fail_without_errors() {
		assertThatThrownBy(() -> new ExifToolResult("", null, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Errors cannot be null.");
	}

	@Test
	void it_should_implement_equals_and_hashCode() {
		EqualsVerifier.forClass(ExifToolResult.class).withNonnullFields("output", "errors").verify();
	}

	@Test
	void it_should_implement_toString() {
		assertThat(new ExifToolResult("foo", "bar", 1)).hasToString("ExifToolResult{output: \"foo\", errors: \"bar\", exitCode: 1}");
	}
}
