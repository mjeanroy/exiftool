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

import com.thebuzzmedia.exiftool.commons.io.StreamVisitor;

/// Handler that should be used to handle command line output.
///
/// Each line is give to the [#readLine(String)] method.
/// This method should return:
/// - `true` if next line should be read. For instance, if current line is `null`, it probably
///   means that no more output is available. This may let handlers to implement a custom
///   logic.
/// - `false` if next line should not be read (end of output).
public interface OutputHandler extends StreamVisitor {

	/// Read a line from command output.
	///
	/// Returned value is a boolean: it should indicate if next line should be
	/// read or if output is finished.
	///
	/// @param line Line output.
	/// @return Boolean indicating if next line should be read.
	boolean readLine(String line);

	/// Read a raw line from command output, i.e a line including its line terminator.
	///
	/// A raw line ends with `\n` (or `\r\n`), except the last line of the output if the output
	/// does not end with a line terminator. A `null` raw line means that no more output is available.
	///
	/// This method is called instead of [#readLine(String)] by processes and executors able to
	/// preserve the exact command output: it allows handlers to rebuild the output byte per byte.
	///
	/// The default implementation removes the line terminator and gives the line(s) to [#readLine(String)],
	/// splitting it the same way [java.io.BufferedReader#readLine()] does: a raw line containing
	/// a carriage return (`\r`) not followed by `\n` is given as several lines.
	///
	/// @param rawLine Raw line output, including its line terminator.
	/// @return Boolean indicating if next line should be read.
	default boolean readRawLine(String rawLine) {
		if (rawLine == null) {
			return readLine(null);
		}

		int end = rawLine.length();
		if (end > 0 && rawLine.charAt(end - 1) == '\n') {
			end--;
		}
		if (end > 0 && rawLine.charAt(end - 1) == '\r') {
			end--;
		}

		String line = rawLine.substring(0, end);
		if (line.indexOf('\r') < 0) {
			return readLine(line);
		}

		for (String part : line.split("\r", -1)) {
			if (!readLine(part)) {
				return false;
			}
		}

		return true;
	}
}
