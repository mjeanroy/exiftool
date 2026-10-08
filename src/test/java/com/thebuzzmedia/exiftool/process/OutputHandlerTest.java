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
package com.thebuzzmedia.exiftool.process;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OutputHandlerTest {

	@Test
	void it_should_give_line_without_line_terminator() {
		Lines handler = new Lines(true);

		assertThat(handler.readRawLine("foo\n")).isTrue();
		assertThat(handler.readRawLine("bar\r\n")).isTrue();
		assertThat(handler.readRawLine("\n")).isTrue();
		assertThat(handler.readRawLine("baz")).isTrue();

		assertThat(handler.lines).containsExactly("foo", "bar", "", "baz");
	}

	@Test
	void it_should_split_lines_on_carriage_return_as_buffered_reader() {
		Lines handler = new Lines(true);

		assertThat(handler.readRawLine("a\rb\r\r\n")).isTrue();

		assertThat(handler.lines).containsExactly("a", "b", "");
	}

	@Test
	void it_should_stop_splitting_lines_once_handler_returns_false() {
		Lines handler = new Lines(false);

		assertThat(handler.readRawLine("a\rb\n")).isFalse();

		assertThat(handler.lines).containsExactly("a");
	}

	@Test
	void it_should_give_null_at_end_of_output() {
		Lines handler = new Lines(true);

		assertThat(handler.readRawLine(null)).isFalse();

		assertThat(handler.lines).containsExactly((String) null);
	}

	private static final class Lines implements OutputHandler {
		private final boolean hasNext;
		private final List<String> lines = new ArrayList<>();

		private Lines(boolean hasNext) {
			this.hasNext = hasNext;
		}

		@Override
		public boolean readLine(String line) {
			lines.add(line);
			return line != null && hasNext;
		}
	}
}
