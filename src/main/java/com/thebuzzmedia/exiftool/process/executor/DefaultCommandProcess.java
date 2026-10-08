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
import com.thebuzzmedia.exiftool.process.CommandProcess;
import com.thebuzzmedia.exiftool.process.OutputHandler;

import com.thebuzzmedia.exiftool.commons.io.RawLineReader;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.thebuzzmedia.exiftool.commons.io.IOs.closeQuietly;
import static com.thebuzzmedia.exiftool.commons.lang.Objects.firstNonNull;
import static com.thebuzzmedia.exiftool.commons.lang.PreConditions.notEmpty;
import static java.util.Objects.requireNonNull;

/// Default implementation for [CommandProcess] interface.
///
/// This implementation used instance of [InputStream] to handle
/// read operation and instance of [OutputStream] to handle write
/// operation. These streams may come from instance of [Process] for instance.
///
/// Output is read with a [RawLineReader]: raw lines (i.e. lines including their
/// line terminator) are given to [OutputHandler#readRawLine(String)], so that handlers
/// may rebuild the exact output, decoded as UTF-8.
///
/// If the error stream is read separately (see [#DefaultCommandProcess(InputStream, OutputStream, InputStream, boolean)]),
/// a background (daemon) thread continuously reads the error stream: this guarantees that the process will never
/// be blocked because its error stream is full, and lines written to the error stream can be read using
/// [#readErrorLine(long, TimeUnit)].
///
/// **Note:** This implementation is not thread safe.
public class DefaultCommandProcess implements CommandProcess {

	/// Class Logger.
	private static final Logger log = LoggerFactory.getLogger(DefaultCommandProcess.class);

	/// Instance of [InputStream].
	/// This stream will be used to handle read operation.
	private final InputStream is;

	/// Output stream.
	/// This stream will be used to handle write operation.
	private final OutputStream os;

	/// Error Stream.
	private final InputStream err;

	/// The reader used to read output, kept between read operations so that
	/// no buffered output is lost.
	private final RawLineReader reader;

	/// Lines read from the error stream, not yet consumed, `null` if the error
	/// stream is not read separately.
	private final BlockingQueue<String> errors;

	/// Flag to know if a given process has been closed.
	private boolean close;

	/// Create process.
	///
	/// The error stream is not read by this process (it is expected to be merged
	/// with the input stream, see [ProcessBuilder#redirectErrorStream(boolean)]).
	///
	/// @param is Input stream.
	/// @param os Output stream.
	/// @param err Error stream.
	public DefaultCommandProcess(InputStream is, OutputStream os, InputStream err) {
		this(is, os, err, false);
	}

	/// Create process.
	///
	/// @param is Input stream.
	/// @param os Output stream.
	/// @param err Error stream.
	/// @param readErrorStream If `true`, the error stream is continuously read by a background thread and lines
	///                        written to the error stream can be read using [#readErrorLine(long, TimeUnit)].
	public DefaultCommandProcess(InputStream is, OutputStream os, InputStream err, boolean readErrorStream) {
		this.is = requireNonNull(is, "Input stream should not be null");
		this.os = requireNonNull(os, "Output stream should not be null");
		this.err = requireNonNull(err, "Error stream should not be null");
		this.reader = new RawLineReader(is, StandardCharsets.UTF_8);
		this.errors = readErrorStream ? new LinkedBlockingQueue<String>() : null;
		this.close = false;

		if (readErrorStream) {
			Thread thread = new Thread(new ErrorStreamReader(new RawLineReader(err, StandardCharsets.UTF_8), errors), "exiftool-stderr");
			thread.setDaemon(true);
			thread.start();
		}
	}

	@Override
	public String read() throws IOException {
		return doRead(null);
	}

	@Override
	public String read(OutputHandler handler) throws IOException {
		return doRead(requireNonNull(handler, "Handler should not be null"));
	}

	@Override
	public boolean hasErrorStream() {
		return errors != null;
	}

	@Override
	public String readErrorLine(long timeout, TimeUnit unit) throws IOException {
		if (errors == null) {
			throw new UnsupportedOperationException("Error stream is not read separately from the output of this process");
		}

		final String line;
		try {
			line = errors.poll(timeout, unit);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new InterruptedIOException("Interrupted while reading error stream");
		}

		if (line == ErrorStreamReader.EOF) {
			// Keep it, so that next calls will not block.
			errors.offer(ErrorStreamReader.EOF);
			return null;
		}

		return line;
	}

	@Override
	public void write(String input, String... others) throws IOException {
		doWrite(input);

		// Write other inputs.
		for (String o : others) {
			doWrite(o);
		}
	}

	@Override
	public void write(Iterable<String> inputs) throws IOException {
		notEmpty(inputs, "Write inputs should not be empty");
		for (String input : inputs) {
			doWrite(input);
		}
	}

	@Override
	public void flush() throws IOException {
		os.flush();
	}

	@Override
	public boolean isRunning() {
		return !isClosed();
	}

	@Override
	public boolean isClosed() {
		return close;
	}

	@Override
	public void close() throws Exception {
		IOException ex1 = close(os);
		IOException ex2 = close(is);
		IOException ex3 = close(err);

		close = true;

		// Throw exception if something bad happened
		if (ex1 != null || ex2 != null || ex3 != null) {
			throw firstNonNull(ex1, ex2, ex3);
		}
	}

	private IOException close(Closeable closeable) {
		try {
			closeable.close();
			return null;
		}
		catch (IOException ex) {
			log.error(ex.getMessage(), ex);
			return ex;
		}
	}

	private String doRead(OutputHandler h) throws IOException {
		if (isClosed()) {
			throw new IllegalStateException("Cannot read from closed process");
		}

		log.debug("Read command output");

		// Create result handler, and wrap it in a composite
		// handler if one is specified in parameter.
		final ResultHandler out = new ResultHandler();
		final OutputHandler handler = h == null ? out : new CompositeHandler(out, h);

		// Read output stream until the end
		try {
			boolean hasNext = true;
			while (hasNext) {
				String rawLine = reader.readLine();
				hasNext = handler.readRawLine(rawLine);
				log.trace("  - Line: {}", rawLine);
				log.trace("  - Continue: {}", hasNext);

				// End of stream: the stream can be closed.
				if (rawLine == null) {
					closeQuietly(is);
				}
			}
		}
		catch (IOException ex) {
			log.error(ex.getMessage(), ex);
			throw ex;
		}

		// We can return the output
		return out.getOutput();
	}

	private void doWrite(String input) throws IOException {
		if (isClosed()) {
			throw new IllegalStateException("Cannot write from closed process");
		}

		// Check valid input.
		requireNonNull(input, "Write input should not be null");

		// Extract the most appropriate charset, depends on the OS & the JVM.
		Charset charset = guessCharset();

		// Just log some debug information
		log.debug("Send command input with charset {}: {}", charset, input);

		try {
			os.write(input.getBytes(charset));
		}
		catch (IOException ex) {
			log.error(ex.getMessage(), ex);
			throw ex;
		}
	}

	private Charset guessCharset() {
		String nativeEncoding = System.getProperty("native.encoding");
		if (nativeEncoding != null) {
			return Charset.forName(nativeEncoding);
		}

		String fileEncoding = System.getProperty("file.encoding");
		if (fileEncoding != null) {
			return Charset.forName(fileEncoding);
		}

		return StandardCharsets.UTF_8;
	}

	/// Read error stream until its end, and push each raw line to a queue.
	/// Once the end of the stream is reached (or if an error occurred), [#EOF] is pushed.
	private static final class ErrorStreamReader implements Runnable {

		/// Marker pushed once the end of the stream has been reached.
		/// Compared by reference: it cannot be confused with a line read from the stream.
		@SuppressWarnings("StringOperationCanBeSimplified")
		private static final String EOF = new String("<EOF>");

		/// The reader.
		private final RawLineReader reader;

		/// The queue.
		private final BlockingQueue<String> queue;

		private ErrorStreamReader(RawLineReader reader, BlockingQueue<String> queue) {
			this.reader = reader;
			this.queue = queue;
		}

		@Override
		public void run() {
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					log.trace("  - Error: {}", line);
					queue.offer(line);
				}
			}
			catch (IOException ex) {
				// Expected when the process is closed while reading.
				log.debug("Error stream cannot be read anymore: {}", ex.getMessage());
			}
			finally {
				queue.offer(EOF);
			}
		}
	}
}
