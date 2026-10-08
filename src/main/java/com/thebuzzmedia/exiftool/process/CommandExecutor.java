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

import java.io.IOException;

/// Command Executor.
public interface CommandExecutor {

	/// Execute command and build the result.
	/// **NOTE:** Execution is synchronous.
	///
	/// @param command Command input.
	/// @return Result of execution.
	/// @throws IOException If an error occurred during execution.
	CommandResult execute(Command command) throws IOException;

	/// Execute command and build the result.
	/// **NOTE:** Execution is synchronous.
	///
	/// @param command Command.
	/// @param handler Custom output handler.
	/// @return Result of execution.
	/// @throws java.io.IOException If an error occurred during operation.
	CommandResult execute(Command command, OutputHandler handler) throws IOException;

	/// Execute command and build the result, reading the error stream separately from the output.
	/// **NOTE:** Execution is synchronous.
	///
	/// Implementations able to preserve the exact output should give raw lines (i.e. lines including their
	/// line terminator) to [OutputHandler#readRawLine(String)].
	///
	/// Default implementation delegates to [#execute(Command, OutputHandler)]: the error stream is then
	/// handled as the executor handles it in this method (for instance, merged with the output), and
	/// `errorHandler` is not used.
	///
	/// @param command Command.
	/// @param handler Custom output handler.
	/// @param errorHandler Custom handler for lines written to the error stream.
	/// @return Result of execution.
	/// @throws java.io.IOException If an error occurred during operation.
	default CommandResult execute(Command command, OutputHandler handler, OutputHandler errorHandler) throws IOException {
		return execute(command, handler);
	}

	/// Start command line and return associated process.
	///
	/// This process will be used to:
	/// - Read output.
	/// - Write arguments.
	///
	/// @param command Command.
	/// @return Process.
	/// @throws java.io.IOException If an error occurred during operation.
	CommandProcess start(Command command) throws IOException;

	/// Start command line and return associated process, keeping the error stream of the process
	/// separate from its output: if supported, [CommandProcess#hasErrorStream()] returns `true` and
	/// lines written to the error stream are read using [CommandProcess#readErrorLine(long, java.util.concurrent.TimeUnit)].
	///
	/// Default implementation delegates to [#start(Command)].
	///
	/// @param command Command.
	/// @return Process.
	/// @throws java.io.IOException If an error occurred during operation.
	default CommandProcess startWithErrorStream(Command command) throws IOException {
		return start(command);
	}
}
