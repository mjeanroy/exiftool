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
import com.thebuzzmedia.exiftool.process.Command;
import com.thebuzzmedia.exiftool.process.CommandExecutor;
import com.thebuzzmedia.exiftool.process.CommandProcess;
import com.thebuzzmedia.exiftool.process.OutputHandler;
import com.thebuzzmedia.exiftool.process.command.CommandBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/// Execution strategy that use `exiftool` with the `stay_open` feature.
///
/// A single `exiftool` process is started (on the first execution) and re-used for next executions: arguments
/// are written to its standard input (as an argfile, using `-@ -`), each command being terminated by `-execute`.
///
/// ### Framing
///
/// When the process is started, its version is checked (by running `-ver`). With ExifTool 9.15 or later (supporting
/// `-echo4`), and if the error stream of the process can be read separately (see [CommandExecutor#startWithErrorStream(Command)]),
/// each command (i.e arguments ending with `-execute`) is framed using a marker unique to the command, `{readyNUM}`:
///
/// ```text
/// -echo4
/// {readyNUM}
/// <arguments>
/// -executeNUM
/// ```
///
/// ExifTool then prints `{readyNUM}` at the end of the output of the command (instead of `{ready}`), and at the end of
/// the error stream of the command (`-echo4` writes its argument to the error stream once the command has been processed,
/// even if an option of the command is invalid). This allows to:
/// - Read exactly the output of the command, including an unterminated last line.
/// - Read exactly the errors of the command: errors cannot be mixed between commands. Errors are given to the handler
///   after the output (as if the error stream was merged with the output).
///
/// `NUM` is incremented for each command, starting with a random number when the process is started.
///
/// Otherwise (older version, custom executor, or arguments not ending with `-execute`), arguments are written as-is and
/// output is read until `{ready}`.
///
/// If an error occurs while a command is executed, the process is closed: it will be started again for the next execution.
public class StayOpenStrategy implements ExecutionStrategy {

	/// Class Logger.
	private static final Logger log = LoggerFactory.getLogger(StayOpenStrategy.class);

	/// Minimum version of `exiftool` supporting `stay_open` feature.
	private static final Version V8_36 = new Version("8.36");

	/// Minimum version of `exiftool` supporting `-echo4` option, used to frame the error stream of commands.
	private static final Version V9_15 = new Version("9.15");

	/// Argument terminating a command.
	private static final String EXECUTE = "-execute";

	/// Marker printed by `exiftool` once a command terminated by `-execute` has been processed.
	private static final String READY = "{ready}";

	/// Pattern of markers printed by `exiftool` once a command terminated by `-executeNUM` has been processed.
	private static final Pattern READY_NUM = Pattern.compile("\\{ready\\d+}");

	/// Maximum time to wait for the end of the errors of a command, once its output has been read.
	///
	/// ExifTool flushes its error stream before printing the end of the output of the command, so this
	/// timeout should never be reached unless the process is broken.
	private static final long ERROR_TIMEOUT_MS = 10000;

	/// Scheduler: will be used to perform automatic cleanup.
	///
	/// If automatic cleanup is disabled (if delay is equal or less than zero),
	/// then it will be set to `null`.
	private final Scheduler scheduler;

	/// Process opened when the first execution is called.
	/// This process will remain open until a call to [#close] is made.
	private CommandProcess process;

	/// Version of the current process, `null` if it is unknown.
	private Version processVersion;

	/// Flag indicating if commands sent to current process are framed.
	private boolean framed;

	/// The identifier of the last framed command sent to current process.
	private long lastCommandId;

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

		synchronized (this) {
			start(executor, exifTool);

			if (framed && endsWithExecute(arguments)) {
				// Errors are given to the handler after the output, as if the error stream was merged with the output.
				Gate output = new Gate(handler);
				executeFramed(arguments, output, output);

				// Emulate the end of the output, as printed by exiftool with "-execute".
				output.readLine(READY);
			}
			else {
				ReadyHandler output = new ReadyHandler(handler);
				executeLegacy(arguments, output, output::isComplete);
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

	/// Start daemon process if it is not already started, and reset the cleanup task.
	///
	/// @param executor ExifTool withExecutor.
	/// @param exifTool ExifTool withPath.
	/// @throws IOException If the process cannot be started.
	private synchronized void start(CommandExecutor executor, String exifTool) throws IOException {
		// Start daemon process if it is not already started.
		// If this is our first time calling getImageMeta with a "stayOpen"
		// connection, set up the persistent process and run it so it is
		// ready to receive commands from us.
		if (process == null || process.isClosed()) {
			log.debug("Start exiftool process");

			Command command = CommandBuilder.builder(exifTool, 4)
					.addArgument("-stay_open", "True")
					.addArgument("-@")
					.addArgument("-")
					.build();

			process = executor.startWithErrorStream(command);
			processVersion = null;
			framed = false;
			lastCommandId = ThreadLocalRandom.current().nextInt(1, 1000000000);

			if (process.hasErrorStream()) {
				try {
					processVersion = readVersion();
				}
				catch (IOException | RuntimeException ex) {
					log.error(ex.getMessage(), ex);
					safeCloseProcess();
					throw ex;
				}

				if (processVersion.compareTo(V9_15) >= 0) {
					framed = true;
				}
				else {
					// Errors cannot be framed: restore previous behavior (errors merged with output).
					log.debug("ExifTool {} does not support -echo4, restart exiftool process with error stream merged with output", processVersion);
					safeCloseProcess();
					process = executor.start(command);
				}
			}

			log.debug("ExifTool process started (version: {}, framed commands: {})", processVersion, framed);
		}

		// Always reset the cleanup task.
		scheduler.stop();
		scheduler.start(this::safeClose);
	}

	/// Read the version of current process.
	///
	/// @return The version.
	/// @throws IOException If the version cannot be read.
	private Version readVersion() throws IOException {
		VersionHandler handler = new VersionHandler();
		FramedOutputHandler output = new FramedOutputHandler(READY, handler);

		process.write("-ver" + Constants.BR, EXECUTE + Constants.BR);
		process.flush();
		process.read(output);

		if (!output.isComplete() || handler.version == null) {
			throw new IOException("Unable to read the version of the ExifTool daemon process");
		}

		return new Version(handler.version);
	}

	/// Execute command using the legacy protocol: arguments are written as-is.
	///
	/// @param arguments Command line arguments.
	/// @param handler Handler to read command output, until `{ready}`.
	/// @param complete Check if `{ready}` has been read by the handler.
	/// @throws IOException If an error occurred during execution.
	private void executeLegacy(List<String> arguments, OutputHandler handler, BooleanSupplier complete) throws IOException {
		List<String> newArgs = arguments.stream().map(input -> input + Constants.BR).collect(Collectors.toList());

		try {
			process.write(newArgs);
			process.flush();
			process.read(handler);
		}
		catch (IOException ex) {
			// The state of the process is unknown: it must not be re-used.
			log.error(ex.getMessage(), ex);
			safeCloseProcess();
			throw ex;
		}

		if (!complete.getAsBoolean()) {
			// The state of the process is unknown: it must not be re-used.
			safeCloseProcess();
			throw new IOException("ExifTool daemon process stopped before the end of the command output");
		}
	}

	/// Execute framed command (see class documentation).
	///
	/// @param arguments Command line arguments, ending with `-execute`.
	/// @param outputHandler Handler to read command output.
	/// @param errorHandler Handler to read lines written to the error stream.
	/// @throws IOException If an error occurred during execution.
	private void executeFramed(List<String> arguments, Gate outputHandler, Gate errorHandler) throws IOException {
		final long id = ++lastCommandId;
		final String marker = "{ready" + id + "}";

		List<String> newArgs = new ArrayList<>(arguments.size() + 4);
		newArgs.add("-echo4" + Constants.BR);
		newArgs.add(marker + Constants.BR);
		for (String argument : arguments.subList(0, arguments.size() - 1)) {
			newArgs.add(argument + Constants.BR);
		}
		newArgs.add(EXECUTE + id + Constants.BR);

		try {
			discardErrors();

			process.write(newArgs);
			process.flush();

			FramedOutputHandler output = new FramedOutputHandler(marker, outputHandler);
			process.read(output);
			if (!output.isComplete()) {
				throw new IOException("ExifTool daemon process stopped before the end of the command output");
			}

			readErrors(marker, errorHandler);
		}
		catch (IOException | RuntimeException ex) {
			// The state of the process is unknown: it must not be re-used.
			log.error(ex.getMessage(), ex);
			safeCloseProcess();
			throw ex;
		}
	}

	/// Read errors of the command identified by given marker (see class documentation).
	///
	/// @param marker The marker.
	/// @param handler Handler to read lines written to the error stream.
	/// @throws IOException If the end of the errors of the command cannot be read.
	private void readErrors(String marker, OutputHandler handler) throws IOException {
		final long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(ERROR_TIMEOUT_MS);

		while (true) {
			final long timeout = Math.max(0, deadline - System.nanoTime());
			final String rawLine = process.readErrorLine(timeout, TimeUnit.NANOSECONDS);
			if (rawLine == null) {
				throw new IOException("ExifTool daemon process did not print the end of the command errors");
			}

			final String line = stripLineTerminator(rawLine);
			if (line.equals(marker)) {
				return;
			}

			if (line.endsWith(marker)) {
				// Last error is not terminated by a new line.
				handler.readRawLine(line.substring(0, line.length() - marker.length()));
				return;
			}

			if (READY_NUM.matcher(line).matches()) {
				log.warn("Discard end of errors of a previous command: {}", line);
				continue;
			}

			handler.readRawLine(rawLine);
		}
	}

	/// Discard errors printed while no command was executed (should not happen).
	///
	/// @throws IOException If the error stream cannot be read.
	private void discardErrors() throws IOException {
		String rawLine;
		while ((rawLine = process.readErrorLine(0, TimeUnit.MILLISECONDS)) != null) {
			log.warn("Discard unexpected error: {}", rawLine);
		}
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

	private static boolean endsWithExecute(List<String> arguments) {
		return !arguments.isEmpty() && EXECUTE.equals(arguments.get(arguments.size() - 1));
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

	/// Handler forwarding lines to a delegate handler until it returns `false` once.
	private static final class Gate implements OutputHandler {

		/// The delegate handler.
		private final OutputHandler delegate;

		/// Flag indicating if the delegate handler still accepts lines.
		private boolean open;

		private Gate(OutputHandler delegate) {
			this.delegate = delegate;
			this.open = true;
		}

		@Override
		public boolean readLine(String line) {
			if (open) {
				open = delegate.readLine(line);
			}

			return open;
		}

		@Override
		public boolean readRawLine(String rawLine) {
			if (open) {
				open = delegate.readRawLine(rawLine);
			}

			return open;
		}
	}

	/// Handler reading output of a command until given marker, which is not forwarded to the delegate handler.
	///
	/// Output is read until the marker, even if the delegate handler stops reading output before.
	private static final class FramedOutputHandler implements OutputHandler {

		/// The marker printed at the end of the output.
		private final String marker;

		/// The delegate handler.
		private final OutputHandler delegate;

		/// Flag indicating if the marker has been read.
		private boolean complete;

		private FramedOutputHandler(String marker, OutputHandler delegate) {
			this.marker = marker;
			this.delegate = delegate;
			this.complete = false;
		}

		@Override
		public boolean readLine(String line) {
			if (line == null) {
				return false;
			}

			if (line.equals(marker)) {
				complete = true;
				return false;
			}

			if (line.endsWith(marker)) {
				complete = true;
				delegate.readLine(line.substring(0, line.length() - marker.length()));
				return false;
			}

			delegate.readLine(line);
			return true;
		}

		@Override
		public boolean readRawLine(String rawLine) {
			if (rawLine == null) {
				return false;
			}

			final String line = stripLineTerminator(rawLine);
			if (line.equals(marker)) {
				complete = true;
				return false;
			}

			if (line.endsWith(marker)) {
				// The last line of the output is not terminated by a new line: the marker is printed on the same line.
				complete = true;
				delegate.readRawLine(line.substring(0, line.length() - marker.length()));
				return false;
			}

			delegate.readRawLine(rawLine);
			return true;
		}

		private boolean isComplete() {
			return complete;
		}
	}

	/// Handler reading the first line of output.
	private static final class VersionHandler implements OutputHandler {

		/// The version, `null` until a non-empty line is read.
		private String version;

		@Override
		public boolean readLine(String line) {
			if (version == null && line != null && !line.trim().isEmpty()) {
				version = line.trim();
			}

			return line != null;
		}
	}
}
