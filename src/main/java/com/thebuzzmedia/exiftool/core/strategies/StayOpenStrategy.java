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

package com.thebuzzmedia.exiftool.core.strategies;

import com.thebuzzmedia.exiftool.Constants;
import com.thebuzzmedia.exiftool.ExecutionStrategy;
import com.thebuzzmedia.exiftool.Scheduler;
import com.thebuzzmedia.exiftool.Version;
import com.thebuzzmedia.exiftool.logs.Logger;
import com.thebuzzmedia.exiftool.logs.LoggerFactory;
import com.thebuzzmedia.exiftool.process.CommandExecutor;
import com.thebuzzmedia.exiftool.process.CommandProcess;
import com.thebuzzmedia.exiftool.process.OutputHandler;
import com.thebuzzmedia.exiftool.process.command.CommandBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/// Execution strategy that use `exiftool` with the `stay_open` feature.
///
/// If an error occurs while a command is executed, the process is closed: it will be started again for the next execution.
public class StayOpenStrategy implements ExecutionStrategy {

	/// Class Logger.
	private static final Logger log = LoggerFactory.getLogger(StayOpenStrategy.class);

	/// Minimum version of `exiftool` supporting `stay_open` feature.
	private static final Version V8_36 = new Version("8.36");

	/// Marker printed by `exiftool` once a command terminated by `-execute` has been processed.
	private static final String READY = "{ready}";

	/// Scheduler: will be used to perform automatic cleanup.
	///
	/// If automatic cleanup is disabled (if delay is equal or less than zero),
	/// then it will be set to `null`.
	private final Scheduler scheduler;

	/// Process opened when the first execution is called.
	/// This process will remain open until a call to [#close] is made.
	private CommandProcess process;

	/// Create strategy.
	/// Scheduler provided in parameter will be used to clean resources (exiftool process).
	///
	/// @param scheduler Delay between automatic cleanup.
	public StayOpenStrategy(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public void execute(CommandExecutor executor, String exifTool, List<String> arguments, OutputHandler handler) throws IOException {
		log.debug("Using ExifTool in daemon mode (-stay_open True)...");
		List<String> newArgs = arguments.stream().map(input -> input + Constants.BR).collect(Collectors.toList());

		synchronized (this) {
			// Start daemon process if it is not already started.
			// If this is our first time calling getImageMeta with a "stayOpen"
			// connection, set up the persistent process and run it so it is
			// ready to receive commands from us.
			if (process == null || process.isClosed()) {
				log.debug("Start exiftool process");
				process = executor.start(CommandBuilder.builder(exifTool, 4)
						.addArgument("-stay_open", "True")
						.addArgument("-@")
						.addArgument("-")
						.build());
			}

			// Always reset the cleanup task.
			scheduler.stop();
			scheduler.start(this::safeClose);

			ReadyHandler output = new ReadyHandler(handler);

			try {
				process.write(newArgs);
				process.flush();
				process.read(output);
			}
			catch (IOException ex) {
				// The state of the process is unknown: it must not be re-used.
				log.error(ex.getMessage(), ex);
				safeCloseProcess();
				throw ex;
			}

			if (!output.isComplete()) {
				// The state of the process is unknown: it must not be re-used.
				safeCloseProcess();
				throw new IOException("ExifTool daemon process stopped before the end of the command output");
			}
		}
	}

	@Override
	public synchronized boolean isRunning() {
		return process != null && process.isRunning();
	}

	@Override
	public boolean isSupported(Version version) {
		return V8_36.compareTo(version) <= 0;
	}

	@Override
	public synchronized void close() throws Exception {
		if (process != null) {
			closeProcess();
		}

		closeScheduler();
	}

	@Override
	public synchronized void shutdown() throws Exception {
		close();
		shutdownScheduler();
	}

	/// Close pending cleanup task and stop scheduler.
	/// This scheduler may be re-used if necessary.
	private synchronized void closeScheduler() {
		// Try to stop cleanup task
		// Note: If task is not stopped, it may be executed later

		try {
			log.debug("Attempting to stop cleanup task");
			scheduler.stop();
			log.debug("Cleanup task successfully stopped");
		}
		catch (Exception ex) {
			// Should not fail everything.
			// Important to log warning at least.
			log.warn("Cleanup task failed to stop");
			log.warn(ex.getMessage(), ex);
		}
	}

	/// Close pending cleanup task and stop scheduler.
	/// This scheduler may be re-used if necessary.
	private synchronized void shutdownScheduler() {
		// Try to stop cleanup task
		// Note: If task is not stopped, it may be executed later

		try {
			log.debug("Attempting to shutdown cleanup task");
			scheduler.shutdown();
			log.debug("Cleanup task successfully shutdown");
		}
		catch (Exception ex) {
			// Should not fail everything.
			// Important to log warning at least.
			log.warn("Cleanup task failed to shutdown");
			log.warn(ex.getMessage(), ex);
		}
	}

	/// Close ExifTool process.
	/// Process may be re-used if necessary.
	///
	/// @throws Exception If an error occurs during the close operation.
	private synchronized void closeProcess() throws Exception {
		// Dot not forget to set it to null.
		final CommandProcess current = process;
		process = null;

		try {
			// If ExifTool was used in stayOpen mode but getImageMeta was never
			// called then the streams were never initialized and there is nothing
			// to shut down or destroy, otherwise we need to close down all the
			// resources in use.
			log.debug("Attempting to close ExifTool daemon process, issuing '-stay_open\\nFalse\\n' command...");
			try {
				current.write("-stay_open\nFalse\n");
				current.flush();
			}
			finally {
				// Always close streams, even if process cannot be stopped gracefully (for instance, if it is already stopped).
				current.close();
			}

			log.debug("ExifTool daemon process successfully closed");
		}
		catch (Exception ex) {
			// Log some warnings.
			log.warn("ExifTool daemon failed to stop");
			log.warn(ex.getMessage(), ex);

			// Re-throw the error, this will let the caller do what he wants with the exception.
			throw ex;
		}
	}

	/// Close ExifTool process, if it is started, without propagating exceptions.
	private synchronized void safeCloseProcess() {
		if (process == null) {
			return;
		}

		try {
			closeProcess();
		}
		catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	/// This is exactly the same operation as [#close] but catch
	/// all exceptions and log stacktrace.
	///
	/// This method should be used internally to perform a close operation
	/// without catching or propagate exceptions.
	private synchronized void safeClose() {
		try {
			close();
		}
		catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	private static String stripLineTerminator(String rawLine) {
		int end = rawLine.length();
		if (end > 0 && rawLine.charAt(end - 1) == '\n') {
			end--;
		}
		if (end > 0 && rawLine.charAt(end - 1) == '\r') {
			end--;
		}

		return rawLine.substring(0, end);
	}

	/// Handler reading output of a command until `{ready}`, forwarding lines (including `{ready}`) to a delegate
	/// handler until it returns `false` once.
	///
	/// Output is read until `{ready}`, even if the delegate handler stops reading output before: remaining output
	/// of the command is never read as the output of the next command.
	private static final class ReadyHandler implements OutputHandler {

		/// The delegate handler.
		private final OutputHandler delegate;

		/// Flag indicating if the delegate handler still accepts lines.
		private boolean open;

		/// Flag indicating if `{ready}` has been read.
		private boolean complete;

		private ReadyHandler(OutputHandler delegate) {
			this.delegate = delegate;
			this.open = true;
			this.complete = false;
		}

		@Override
		public boolean readLine(String line) {
			if (open) {
				open = delegate.readLine(line);
			}

			return next(line);
		}

		@Override
		public boolean readRawLine(String rawLine) {
			if (open) {
				open = delegate.readRawLine(rawLine);
			}

			return next(rawLine == null ? null : stripLineTerminator(rawLine));
		}

		/// Check if next line should be read, given the current line (without its line terminator).
		///
		/// @param line The current line, `null` if the end of the stream has been reached.
		/// @return `true` if next line should be read.
		private boolean next(String line) {
			complete = READY.equals(line);
			return line != null && !complete;
		}

		private boolean isComplete() {
			return complete;
		}
	}
}
