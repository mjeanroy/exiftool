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

package com.thebuzzmedia.exiftool.process.executor;

import com.thebuzzmedia.exiftool.logs.Logger;
import com.thebuzzmedia.exiftool.logs.LoggerFactory;
import com.thebuzzmedia.exiftool.process.Command;
import com.thebuzzmedia.exiftool.process.CommandExecutor;
import com.thebuzzmedia.exiftool.process.CommandProcess;
import com.thebuzzmedia.exiftool.process.CommandResult;
import com.thebuzzmedia.exiftool.process.OutputHandler;

import com.thebuzzmedia.exiftool.commons.io.RawLineReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.thebuzzmedia.exiftool.commons.io.IOs.closeQuietly;
import static com.thebuzzmedia.exiftool.commons.io.IOs.readInputStream;
import static java.util.Objects.requireNonNull;

/// Default Executor.
public class DefaultCommandExecutor implements CommandExecutor {

	/// Class logger.
	private static final Logger log = LoggerFactory.getLogger(DefaultCommandExecutor.class);

	/// Create default executor.
	public DefaultCommandExecutor() {
	}

	@Override
	public CommandResult execute(Command command) throws IOException {
		return readProcessOutput(command, null);
	}

	@Override
	public CommandResult execute(Command command, OutputHandler handler) throws IOException {
		return readProcessOutput(command, requireNonNull(handler, "Handler should not be null"));
	}

	/// Execute command and build the result, reading the error stream separately from the output.
	///
	/// The output is read in the current thread, while the error stream is read in a background
	/// thread (so that the process can never be blocked because one of these streams is full). The input
	/// stream of the process is closed immediately.
	///
	/// Raw lines are given to [OutputHandler#readRawLine(String)]. Once a handler returns `false`, remaining
	/// lines are read but ignored, until the end of the process.
	///
	/// @param command Command.
	/// @param handler Custom output handler.
	/// @param errorHandler Custom handler for lines written to the error stream.
	/// @return Result of execution.
	/// @throws IOException If an error occurred during operation.
	@Override
	public CommandResult execute(Command command, OutputHandler handler, OutputHandler errorHandler) throws IOException {
		requireNonNull(handler, "Handler should not be null");
		requireNonNull(errorHandler, "Error handler should not be null");

		final Process proc = createProcess(command, false);
		final ResultHandler out = new ResultHandler();
		final AtomicReference<IOException> errorFailure = new AtomicReference<>();
		final Thread errorReader = new Thread(() -> {
			try {
				readRawLines(proc.getErrorStream(), errorHandler);
			}
			catch (IOException ex) {
				errorFailure.set(ex);
			}
		}, "exiftool-stderr");

		boolean completed = false;

		try {
			errorReader.setDaemon(true);
			errorReader.start();

			// Nothing will be written: let the process know.
			closeQuietly(proc.getOutputStream());

			readRawLines(proc.getInputStream(), new CompositeHandler(handler, out));

			errorReader.join();
			int exitStatus = proc.waitFor();
			completed = true;

			if (errorFailure.get() != null) {
				throw errorFailure.get();
			}

			return new DefaultCommandResult(exitStatus, out.getOutput());
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new InterruptedIOException("Interrupted while waiting for the end of the process");
		}
		finally {
			if (!completed) {
				proc.destroy();
			}

			// Close streams.
			closeQuietly(proc.getInputStream());
			closeQuietly(proc.getOutputStream());
			closeQuietly(proc.getErrorStream());
		}
	}

	@Override
	public CommandProcess start(Command command) throws IOException {
		final Process proc = createProcess(command);
		return new DefaultCommandProcess(proc.getInputStream(), proc.getOutputStream(), proc.getErrorStream());
	}

	/// Start command line and return associated process, the error stream being
	/// read separately from the output (see [DefaultCommandProcess#readErrorLine(long, java.util.concurrent.TimeUnit)]).
	///
	/// @param command Command.
	/// @return Process.
	/// @throws IOException If an error occurred during operation.
	@Override
	public CommandProcess startWithErrorStream(Command command) throws IOException {
		final Process proc = createProcess(command, false);
		return new DefaultCommandProcess(proc.getInputStream(), proc.getOutputStream(), proc.getErrorStream(), true);
	}

	/// Read all raw lines of given stream until its end, giving them to handler until it returns `false`.
	///
	/// @param is The stream.
	/// @param handler The handler.
	/// @throws IOException If an error occurred while reading the stream.
	private static void readRawLines(InputStream is, OutputHandler handler) throws IOException {
		RawLineReader reader = new RawLineReader(is, StandardCharsets.UTF_8);
		boolean hasNext = true;
		String rawLine;
		do {
			rawLine = reader.readLine();
			if (hasNext) {
				hasNext = handler.readRawLine(rawLine);
			}
		}
		while (rawLine != null);
	}

	private CommandResult readProcessOutput(Command cmd, OutputHandler h) throws IOException {
		final Process proc = createProcess(cmd);
		final ResultHandler h1 = new ResultHandler();
		final OutputHandler handler = h == null ? h1 : new CompositeHandler(h, h1);

		readInputStream(proc.getInputStream(), handler);

		// Wait for end of process
		try {
			proc.waitFor();
			return new DefaultCommandResult(proc.exitValue(), h1.getOutput());
		}
		catch (InterruptedException ex) {
			log.error(ex.getMessage(), ex);
			return new DefaultCommandResult(-1, null);
		}
		finally {
			// Close streams.
			closeQuietly(proc.getInputStream());
			closeQuietly(proc.getOutputStream());
			closeQuietly(proc.getErrorStream());
		}
	}

	private Process createProcess(Command command) throws IOException {
		return createProcess(command, true);
	}

	private Process createProcess(Command command, boolean redirectErrorStream) throws IOException {
		try {
			List<String> args = command.getArguments();
			ProcessBuilder builder = new ProcessBuilder(args).redirectErrorStream(redirectErrorStream);
			return builder.start();
		}
		catch (IOException ex) {
			log.error(ex.getMessage(), ex);
			throw ex;
		}
	}
}
