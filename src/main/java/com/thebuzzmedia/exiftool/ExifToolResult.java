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

import com.thebuzzmedia.exiftool.commons.lang.ToStringBuilder;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/// The result of an execution of `exiftool` (see [ExifTool#execute(java.io.File, java.util.List)]).
///
/// A result is defined by:
/// - The output of the command, exactly as printed by `exiftool` on its standard output (decoded as UTF-8).
/// - The errors (and warnings) of the command, as printed by `exiftool` on its error stream.
/// - The exit code of the command, when it is known.
///
/// **Note:** this implementation is immutable and thread safe.
public final class ExifToolResult {

	/// The output.
	private final String output;

	/// The errors.
	private final String errors;

	/// The exit code, `null` if it is unknown.
	private final Integer exitCode;

	/// Create result.
	///
	/// @param output The output.
	/// @param errors The errors, may be empty.
	/// @param exitCode The exit code, `null` if it is unknown.
	/// @throws NullPointerException If `output` or `errors` is `null`.
	public ExifToolResult(String output, String errors, Integer exitCode) {
		this.output = requireNonNull(output, "Output cannot be null.");
		this.errors = requireNonNull(errors, "Errors cannot be null.");
		this.exitCode = exitCode;
	}

	/// Get the output of the command, exactly as printed by `exiftool` on its standard output
	/// (including line terminators), decoded as UTF-8.
	///
	/// When `exiftool` is used in daemon mode (`-stay_open True`), the marker printed at the end of the output
	/// of each command (`{ready}`) is not part of the output: the output is the same as the output of a one-shot `exiftool` process.
	///
	/// @return The output, an empty string if nothing has been printed.
	public String getOutput() {
		return output;
	}

	/// Get the errors (and warnings) of the command, as printed by `exiftool` on its error stream
	/// (including line terminators), decoded as UTF-8.
	///
	/// @return The errors, an empty string if nothing has been printed.
	public String getErrors() {
		return errors;
	}

	/// Get the exit code of the command.
	///
	/// The exit code is only known when a one-shot `exiftool` process is used for the command (i.e, when
	/// `exiftool` is not used in daemon mode).
	///
	/// @return The exit code, `null` if it is unknown.
	public Integer getExitCode() {
		return exitCode;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (o instanceof ExifToolResult) {
			ExifToolResult r = (ExifToolResult) o;
			return Objects.equals(output, r.output)
					&& Objects.equals(errors, r.errors)
					&& Objects.equals(exitCode, r.exitCode);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(output, errors, exitCode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.create(getClass())
				.append("output", output)
				.append("errors", errors)
				.append("exitCode", exitCode)
				.build();
	}
}
