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
package com.thebuzzmedia.exiftool.commons.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RawLineReaderTest {

	@Test
	void it_should_read_lines_with_line_terminators() throws Exception {
		assertThat(readAll("foo\nbar\r\n\nbaz")).containsExactly("foo\n", "bar\r\n", "\n", "baz");
	}

	@Test
	void it_should_read_last_line_terminated_by_new_line() throws Exception {
		assertThat(readAll("foo\nbar\n")).containsExactly("foo\n", "bar\n");
	}

	@Test
	void it_should_not_split_on_carriage_return() throws Exception {
		assertThat(readAll("a\rb\r\n\r")).containsExactly("a\rb\r\n", "\r");
	}

	@Test
	void it_should_read_empty_stream() throws Exception {
		assertThat(readAll("")).isEmpty();
	}

	@Test
	void it_should_return_null_after_end_of_stream() throws Exception {
		RawLineReader reader = new RawLineReader(stream("foo"));
		assertThat(reader.readLine()).isEqualTo("foo");
		assertThat(reader.readLine()).isNull();
		assertThat(reader.readLine()).isNull();
	}

	@Test
	void it_should_read_lines_longer_than_buffer() throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 20000; i++) {
			sb.append((char) ('a' + i % 26));
		}

		String line = sb.toString();
		assertThat(readAll(line + "\n" + line)).containsExactly(line + "\n", line);
	}

	@Test
	void it_should_decode_multibyte_characters_split_across_reads() throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 5000; i++) {
			sb.append("é☃😀");
		}

		String text = sb + "\n" + sb + "\n";

		// Return one byte at a time, to split every multibyte character.
		InputStream is = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)) {
			@Override
			public synchronized int read(byte[] b, int off, int len) {
				return super.read(b, off, Math.min(len, 1));
			}
		};

		RawLineReader reader = new RawLineReader(is);
		assertThat(reader.readLine()).isEqualTo(sb + "\n");
		assertThat(reader.readLine()).isEqualTo(sb + "\n");
		assertThat(reader.readLine()).isNull();
	}

	@Test
	void it_should_preserve_the_exact_content() throws Exception {
		String text = "[{\n  \"Comment\": \"café ☃\",\r\n  \"A\": \"\tb\r\"\n}]\n\n";
		assertThat(String.join("", readAll(text))).isEqualTo(text);
	}

	@Test
	void it_should_keep_buffered_bytes_between_calls() throws Exception {
		// Each call returns only the bytes available: subsequent lines must not be lost.
		InputStream is = new InputStream() {
			private final byte[][] chunks = {
					"foo\nba".getBytes(StandardCharsets.UTF_8),
					"r\n".getBytes(StandardCharsets.UTF_8),
			};
			private int chunk = 0;

			@Override
			public int read() {
				throw new UnsupportedOperationException();
			}

			@Override
			public int read(byte[] b, int off, int len) {
				if (chunk >= chunks.length) {
					return -1;
				}

				byte[] bytes = chunks[chunk++];
				System.arraycopy(bytes, 0, b, off, bytes.length);
				return bytes.length;
			}
		};

		RawLineReader reader = new RawLineReader(is);
		assertThat(reader.readLine()).isEqualTo("foo\n");
		assertThat(reader.readLine()).isEqualTo("bar\n");
		assertThat(reader.readLine()).isNull();
	}

	@Test
	void it_should_consider_zero_byte_read_as_end_of_stream() throws Exception {
		InputStream is = new InputStream() {
			@Override
			public int read() {
				return 0;
			}

			@Override
			public int read(byte[] b, int off, int len) {
				return 0;
			}
		};

		assertThat(new RawLineReader(is).readLine()).isNull();
	}

	@Test
	void it_should_decode_with_given_charset() throws Exception {
		InputStream is = new ByteArrayInputStream("café\n".getBytes(StandardCharsets.ISO_8859_1));
		assertThat(new RawLineReader(is, StandardCharsets.ISO_8859_1).readLine()).isEqualTo("café\n");
	}

	@Test
	void it_should_propagate_errors() {
		InputStream is = new InputStream() {
			@Override
			public int read() throws IOException {
				throw new IOException("Stream closed");
			}
		};

		RawLineReader reader = new RawLineReader(is);
		assertThatThrownBy(reader::readLine).isInstanceOf(IOException.class).hasMessage("Stream closed");
	}

	@Test
	void it_should_fail_without_stream() {
		assertThatThrownBy(() -> new RawLineReader(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Input stream should not be null");
	}

	private static List<String> readAll(String text) throws IOException {
		RawLineReader reader = new RawLineReader(stream(text));
		List<String> lines = new ArrayList<>();
		String line;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}

		return lines;
	}

	private static InputStream stream(String text) {
		return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
	}
}
