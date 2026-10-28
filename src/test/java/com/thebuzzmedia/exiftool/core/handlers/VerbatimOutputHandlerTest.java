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
package com.thebuzzmedia.exiftool.core.handlers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerbatimOutputHandlerTest {

	@Test
	void it_should_collect_raw_lines() {
		VerbatimOutputHandler handler = new VerbatimOutputHandler();

		assertThat(handler.readRawLine("[{\r\n")).isTrue();
		assertThat(handler.readRawLine("  \"A\": \"a\rb\"\n")).isTrue();
		assertThat(handler.readRawLine("}]")).isTrue();

		assertThat(handler.getOutput()).isEqualTo("[{\r\n  \"A\": \"a\rb\"\n}]");
	}

	@Test
	void it_should_not_interpret_ready_marker() {
		VerbatimOutputHandler handler = new VerbatimOutputHandler();

		assertThat(handler.readRawLine("{ready}\n")).isTrue();
		assertThat(handler.readLine("{ready}")).isTrue();

		assertThat(handler.getOutput()).isEqualTo("{ready}\n{ready}\n");
	}

	@Test
	void it_should_collect_lines() {
		VerbatimOutputHandler handler = new VerbatimOutputHandler();

		assertThat(handler.readLine("foo")).isTrue();
		assertThat(handler.readLine("")).isTrue();

		assertThat(handler.getOutput()).isEqualTo("foo\n\n");
	}

	@Test
	void it_should_stop_at_end_of_output() {
		VerbatimOutputHandler handler = new VerbatimOutputHandler();

		assertThat(handler.readRawLine(null)).isFalse();
		assertThat(handler.readLine(null)).isFalse();

		assertThat(handler.getOutput()).isEmpty();
	}
}
