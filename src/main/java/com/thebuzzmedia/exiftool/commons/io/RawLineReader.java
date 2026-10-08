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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

/// Read lines from an [InputStream], preserving line terminators.
///
/// Contrary to [java.io.BufferedReader#readLine()], this reader:
/// - Only splits on `\n`: a returned line ends with `\n` (or `\r\n`), except the
///   last one of the stream if the stream does not end with a line terminator.
/// - Keeps the line terminator in the returned line, so that concatenating
///   all lines gives exactly the content of the stream.
/// - Can be used several times on the same stream (for instance, to read the output
///   of successive commands sent to a long-running process): it never reads bytes
///   that will not be returned by a subsequent call.
///
/// Each line is decoded with the given charset (UTF-8 by default). Since a line is only
/// split after a `\n` byte, a multibyte UTF-8 character is never split across two lines.
///
/// **Note:** This implementation is not thread safe.
public final class RawLineReader {

	/// Size of internal buffer.
	private static final int BUFFER_SIZE = 8192;

	/// The line feed byte.
	private static final byte LF = '\n';

	/// The stream to read.
	private final InputStream is;

	/// Charset used to decode lines.
	private final Charset charset;

	/// Internal buffer.
	private final byte[] buffer;

	/// Current position in internal buffer.
	private int position;

	/// Number of valid bytes in internal buffer.
	private int limit;

	/// Flag set once the end of the stream has been reached.
	private boolean eof;

	/// Create reader, decoding lines as UTF-8.
	///
	/// @param is The stream to read.
	public RawLineReader(InputStream is) {
		this(is, StandardCharsets.UTF_8);
	}

	/// Create reader.
	///
	/// @param is The stream to read.
	/// @param charset Charset used to decode lines.
	public RawLineReader(InputStream is, Charset charset) {
		this.is = requireNonNull(is, "Input stream should not be null");
		this.charset = requireNonNull(charset, "Charset should not be null");
		this.buffer = new byte[BUFFER_SIZE];
		this.position = 0;
		this.limit = 0;
		this.eof = false;
	}

	/// Read next line, including its line terminator.
	///
	/// The returned line ends with `\n`, unless it is the last line of the stream
	/// and the stream does not end with `\n`.
	///
	/// @return The next line, `null` once the end of the stream has been reached.
	/// @throws IOException If an error occurred while reading the stream.
	public String readLine() throws IOException {
		ByteArrayOutputStream line = null;

		while (fill()) {
			int start = position;
			while (position < limit && buffer[position] != LF) {
				position++;
			}

			boolean found = position < limit;
			if (found) {
				// Include line terminator.
				position++;
			}

			if (line == null) {
				if (found) {
					// Fast path: the entire line is available in internal buffer.
					return new String(buffer, start, position - start, charset);
				}

				line = new ByteArrayOutputStream(2 * (position - start));
			}

			line.write(buffer, start, position - start);

			if (found) {
				break;
			}
		}

		return line == null ? null : new String(line.toByteArray(), charset);
	}

	/// Ensure that internal buffer contains bytes to read.
	///
	/// @return `true` if at least one byte is available, `false` if the end of the stream has been reached.
	/// @throws IOException If an error occurred while reading the stream.
	private boolean fill() throws IOException {
		if (position < limit) {
			return true;
		}

		if (eof) {
			return false;
		}

		int n = is.read(buffer, 0, buffer.length);

		// A blocking stream never returns zero byte: consider it as the end of the stream (and do not loop forever).
		if (n <= 0) {
			eof = true;
			position = 0;
			limit = 0;
			return false;
		}

		position = 0;
		limit = n;
		return true;
	}
}
