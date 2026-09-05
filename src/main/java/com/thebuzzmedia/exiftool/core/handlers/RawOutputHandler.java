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

import static com.thebuzzmedia.exiftool.core.handlers.StopHandler.stopHandler;

import com.thebuzzmedia.exiftool.Constants;
import com.thebuzzmedia.exiftool.process.OutputHandler;

/// An [OutputHandler] implementation that collects the raw output produced by ExifTool.
///
/// Each line is appended to the output as-is, with [Constants#BR] inserted between
/// consecutive lines.
///
/// Reading stops when the end of the output is detected.
/// This occurs when either:
/// - the input line is `null`, indicating the end of the stream, or
/// - the input line is exactly `"{ready}"`, indicating the end of the output when ExifTool's `stay_open` feature is enabled.
///
/// The terminating line is not included in the collected output.
public class RawOutputHandler implements OutputHandler {

	private final StringBuilder output;

	/// Creates a new empty raw output handler.
	public RawOutputHandler() {
		this.output = new StringBuilder();
	}

	@Override
	public boolean readLine(String line) {
		// If line is null, then this is the end.
		// If line is strictly equals to "{ready}", then it means that stay_open feature
		// is enabled and this is the end of the output.
		if (!stopHandler().readLine(line)) {
			return false;
		}

		if (output.length() > 0) {
			output.append(Constants.BR);
		}
		output.append(line);

		return true;
	}

	/// Returns the raw output collected from ExifTool.
	///
	/// @return the collected raw output
	public String getOutput() {
		// output the raw string that exiftool outputs
		return output.toString();
	}
}
