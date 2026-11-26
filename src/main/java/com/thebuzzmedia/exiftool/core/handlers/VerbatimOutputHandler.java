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

import com.thebuzzmedia.exiftool.process.OutputHandler;

/// An [OutputHandler] implementation that collects the output produced by ExifTool, exactly as
/// it has been printed.
///
/// Contrary to [RawOutputHandler]:
/// - Line terminators are preserved (`\n`, or `\r\n` on Windows), including the last one: the collected
///   output is exactly the output that has been printed by ExifTool.
/// - The output is not interpreted: reading stops only once the end of the output is reached (i.e. when the
///   line is `null`), a `"{ready}"` line is collected as any other line. Detecting the end of the output of a
///   command when ExifTool's `stay_open` feature is enabled is the responsibility of the execution strategy.
///
/// The exact output can only be collected if raw lines are given to [#readRawLine(String)]. If lines are given
/// to [#readLine(String)], line terminators are lost and each line is collected followed by `\n`.
///
/// **Note:** This implementation is not thread safe.
public class VerbatimOutputHandler implements OutputHandler {

	/// The line terminator appended to lines given without their terminator.
	private static final char LF = '\n';

	/// The collected output.
	private final StringBuilder output;

	/// Creates a new empty handler.
	public VerbatimOutputHandler() {
		this.output = new StringBuilder();
	}

	@Override
	public boolean readLine(String line) {
		if (line == null) {
			return false;
		}

		output.append(line).append(LF);
		return true;
	}

	@Override
	public boolean readRawLine(String rawLine) {
		if (rawLine == null) {
			return false;
		}

		output.append(rawLine);
		return true;
	}

	/// Returns the output collected from ExifTool.
	///
	/// @return the collected output, an empty string if nothing has been printed.
	public String getOutput() {
		return output.toString();
	}
}
