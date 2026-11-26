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
import java.util.concurrent.TimeUnit;

/// Process interface.
///
/// A process will define some methods to:
/// - Read output until a condition returns `false`.
/// - Send input stream to the opened process.
/// - Check if process is closed or still opened.
public interface CommandProcess extends AutoCloseable {

	/// Read output until a null line is read.
	///
	/// Since command process will not be closed, a simple string
	/// is returned (an exit status cannot be computed).
	///
	/// @return Command result.
	/// @throws java.io.IOException If an error occurred during operation.
	String read() throws IOException;

	/// Read output until:
	/// - A null line is read.
	/// - Handler returns false when line is read.
	///
	/// Since command process will not be closed, a simple string
	/// is returned (an exit status cannot be computed).
	///
	/// Implementations able to preserve the exact output of the process should give
	/// raw lines (i.e. lines including their line terminator) to [OutputHandler#readRawLine(String)].
	///
	/// @param handler Output handler.
	/// @return Full output.
	/// @throws java.io.IOException If an error occurred during operation.
	String read(OutputHandler handler) throws IOException;

	/// Check if the error stream of this process can be read separately from its
	/// output, using [#readErrorLine(long, TimeUnit)].
	///
	/// Default implementation returns `false`: the error stream is merged with the output
	/// of the process, or is not available.
	///
	/// @return `true` if the error stream can be read separately, `false` otherwise.
	default boolean hasErrorStream() {
		return false;
	}

	/// Read the next line written to the error stream of the process, including its line terminator,
	/// waiting up to the given timeout if no line is available yet.
	///
	/// This operation is only supported if [#hasErrorStream()] returns `true`. Default implementation
	/// throws [UnsupportedOperationException].
	///
	/// @param timeout The maximum time to wait.
	/// @param unit The unit of the `timeout` argument.
	/// @return The next raw line written to the error stream, `null` if no line has been written before the timeout or if the error stream has been closed.
	/// @throws java.io.IOException If an error occurred during operation.
	/// @throws UnsupportedOperationException If the error stream cannot be read separately.
	default String readErrorLine(long timeout, TimeUnit unit) throws IOException {
		throw new UnsupportedOperationException("Error stream cannot be read separately from the output of this process");
	}

	/// Write input string to the current process.
	///
	/// @param input Input.
	/// @param others Other inputs.
	/// @throws java.io.IOException If an error occurred during operation.
	void write(String input, String... others) throws IOException;

	/// Write set of inputs to the current process.
	///
	/// @param inputs Collection of inputs.
	/// @throws java.io.IOException If an error occurred during operation.
	void write(Iterable<String> inputs) throws IOException;

	/// Flush pending write operations.
	///
	/// @throws java.io.IOException If an error occurred during operation.
	void flush() throws IOException;

	/// Check if current process is still opened.
	/// If this method returns `true`, then [#isClosed()] should return `false`.
	///
	/// @return `true` if process is open, `false` otherwise.
	boolean isRunning();

	/// Check if current process has been closed.
	/// If this method returns `true`, then [#isRunning()] should return `false`.
	///
	/// @return `true` if process is closed, `false` otherwise.
	boolean isClosed();
}
