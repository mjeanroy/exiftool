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

import com.thebuzzmedia.exiftool.process.CommandExecutor;
import com.thebuzzmedia.exiftool.process.OutputHandler;

import java.io.IOException;
import java.util.List;

/// ExifTool execution strategy.
///
/// For instance:
/// - Execution using a one-shot process.
/// - Execution using `stay_open` flag: this strategy means that a
///   process is started and re-used for next executions.
///
/// Each implementation will define the main logic for reading and
/// writing metadata (this is the main purpose for the [#execute] method.
///
/// Implementation should also define a close method: this method
/// will be used to stop remaining process and clean previous execution.
///
/// Calling [#close] method should not prevent instances to be used
/// for a next execution.
public interface ExecutionStrategy extends AutoCloseable {

	/// Execute exiftool command.
	///
	/// @param executor ExifTool withExecutor.
	/// @param exifTool ExifTool withPath.
	/// @param arguments Command line arguments.
	/// @param handler Handler to read command output.
	/// @throws IOException If an error occurred during execution.
	void execute(CommandExecutor executor, String exifTool, List<String> arguments, OutputHandler handler) throws IOException;

	/// Execute exiftool command, reading the error stream separately from the output.
	///
	/// Arguments are given to exiftool as-is: implementations should not add any argument changing the output
	/// of the command (they may add arguments required by the execution protocol). Strategies able to preserve
	/// the exact output should give raw lines (i.e. lines including their line terminator) to
	/// [OutputHandler#readRawLine(String)].
	///
	/// Default implementation delegates to [#execute(CommandExecutor, String, List, OutputHandler)]: errors are
	/// then handled as this method handles them (for instance, merged with the output), `errorHandler` is not used,
	/// and the exit code is not known.
	///
	/// @param executor ExifTool withExecutor.
	/// @param exifTool ExifTool withPath.
	/// @param arguments Command line arguments.
	/// @param outputHandler Handler to read command output.
	/// @param errorHandler Handler to read lines written to the error stream.
	/// @return The exit code of the command, `null` if it is not known (for instance, when a process is re-used for several commands).
	/// @throws IOException If an error occurred during execution.
	default Integer execute(CommandExecutor executor, String exifTool, List<String> arguments, OutputHandler outputHandler, OutputHandler errorHandler) throws IOException {
		execute(executor, exifTool, arguments, outputHandler);
		return null;
	}

	/// Check if exiftool process is currently running.
	/// This method is important especially if `stay_open` flag has been enabled.
	///
	/// @return `true` if `exiftool` process is currently open, `false` otherwise.
	boolean isRunning();

	/// Check if this strategy should is supported with this specific version.
	///
	/// @param version ExifTool Version.
	/// @return `true` if this strategy may be used safely with this specific version, `false` otherwise.
	boolean isSupported(Version version);

	/// This method should be used to:
	/// - Close remaining process (if any).
	/// - Clean previous executions.
	///
	/// For instance, with the `stay_open` flag, this method should:
	/// - Close opened process.
	/// - Stop task used to automatically close process.
	///
	/// Once closed, ExifTool should still be able to use this strategy
	/// if a call to [#execute] is made.
	///
	/// @throws Exception If an error occurred while stopping exiftool client.
	void close() throws Exception;

	/// Shutdown the strategy.
	///
	/// @throws Exception If an error occurred while stopping exiftool client.
	void shutdown() throws Exception;
}
