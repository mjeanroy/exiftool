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

/// Process interface.
///
/// A process will define some methods to:
///   - Read output until a condition returns `false`.
///   - Send input stream to the opened process.
///   - Check if process is closed or still opened.
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
	///   - A null line is read.
	///   - Handler returns false when line is read.
	///
	/// Since command process will not be closed, a simple string
	/// is returned (an exit status cannot be computed).
	///
	/// @param handler Output handler.
	/// @return Full output.
	/// @throws java.io.IOException If an error occurred during operation.
	String read(OutputHandler handler) throws IOException;

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
