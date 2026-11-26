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

import com.thebuzzmedia.exiftool.Scheduler;
import com.thebuzzmedia.exiftool.core.handlers.VerbatimOutputHandler;
import com.thebuzzmedia.exiftool.process.Command;
import com.thebuzzmedia.exiftool.process.CommandExecutor;
import com.thebuzzmedia.exiftool.process.CommandProcess;
import com.thebuzzmedia.exiftool.process.OutputHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.thebuzzmedia.exiftool.tests.TestConstants.BR;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/// Test the framing of commands with a fake exiftool daemon process.
class StayOpenStrategyFramingTest {

	private Scheduler scheduler;
	private CommandExecutor executor;
	private List<FakeDaemon> daemons;
	private String version;
	private Function<Request, Reply> responder;

	private StayOpenStrategy strategy;

	@BeforeEach
	void setUp() throws Exception {
		scheduler = mock(Scheduler.class);
		executor = mock(CommandExecutor.class);
		daemons = new ArrayList<>();
		version = "10.16";
		responder = request -> new Reply("out of " + request.args + "\n", "");

		when(executor.startWithErrorStream(any(Command.class))).thenAnswer(invocation -> newDaemon(true));
		when(executor.start(any(Command.class))).thenAnswer(invocation -> newDaemon(false));

		strategy = new StayOpenStrategy(scheduler, false);
	}

	@AfterEach
	void tearDown() throws Exception {
		strategy.close();
	}

	@Test
	void it_should_frame_command_and_read_output_and_errors() throws Exception {
		responder = request -> new Reply("line 1\nline 2\n", "Warning: foo\n");

		VerbatimOutputHandler out = new VerbatimOutputHandler();
		VerbatimOutputHandler err = new VerbatimOutputHandler();
		Integer exitCode = strategy.execute(executor, "exiftool", asList("-json", "/tmp/foo.png", "-execute"), out, err);

		assertThat(exitCode).isNull();
		assertThat(out.getOutput()).isEqualTo("line 1\nline 2\n");
		assertThat(err.getOutput()).isEqualTo("Warning: foo\n");

		FakeDaemon daemon = singleDaemon();
		assertThat(daemon.requests).hasSize(2);
		assertThat(daemon.requests.get(0).args).containsExactly("-ver", "-execute");

		Request request = daemon.requests.get(1);
		assertThat(request.args).containsExactly("-echo4", request.marker(), "-json", "/tmp/foo.png", "-execute" + request.id);
		assertThat(request.marker()).isEqualTo("{ready" + request.id + "}");

		verify(executor).startWithErrorStream(any(Command.class));
		verify(executor, never()).start(any(Command.class));
	}

	@Test
	void it_should_increment_command_identifier() throws Exception {
		strategy.execute(executor, "exiftool", asList("-json", "-execute"), new VerbatimOutputHandler(), new VerbatimOutputHandler());
		strategy.execute(executor, "exiftool", asList("-json", "-execute"), new VerbatimOutputHandler(), new VerbatimOutputHandler());
		strategy.execute(executor, "exiftool", asList("-S", "-execute"), mock(OutputHandler.class));

		FakeDaemon daemon = singleDaemon();
		assertThat(daemon.requests).hasSize(4);

		long id = daemon.requests.get(1).id;
		assertThat(id).isPositive();
		assertThat(daemon.requests.get(2).id).isEqualTo(id + 1);
		assertThat(daemon.requests.get(3).id).isEqualTo(id + 2);
	}

	@Test
	void it_should_read_output_not_terminated_by_a_new_line() throws Exception {
		responder = request -> new Reply("{\"a\": 1}", "Error: bar");

		VerbatimOutputHandler out = new VerbatimOutputHandler();
		VerbatimOutputHandler err = new VerbatimOutputHandler();
		strategy.execute(executor, "exiftool", asList("-json", "-execute"), out, err);

		assertThat(out.getOutput()).isEqualTo("{\"a\": 1}");
		assertThat(err.getOutput()).isEqualTo("Error: bar");
	}

	@Test
	void it_should_read_output_exactly() throws Exception {
		String output = "[{\n  \"Comment\": \"caf\u00e9 \u2603 {ready}\",\r\n  \"Lone\": \"a\rb\"\n}]\n\n";
		responder = request -> new Reply(output, "");

		VerbatimOutputHandler out = new VerbatimOutputHandler();
		VerbatimOutputHandler err = new VerbatimOutputHandler();
		strategy.execute(executor, "exiftool", asList("-json", "-execute"), out, err);

		assertThat(out.getOutput()).isEqualTo(output);
		assertThat(err.getOutput()).isEmpty();
	}

	@Test
	void it_should_give_errors_after_output_with_legacy_api() throws Exception {
		responder = request -> new Reply("Artist: foo\n", "Warning: bar\n");

		List<String> lines = new ArrayList<>();
		OutputHandler handler = line -> {
			lines.add(line);
			return line != null && !line.equals("{ready}");
		};

		strategy.execute(executor, "exiftool", asList("-S", "-Artist", "/tmp/foo.png", "-execute"), handler);

		assertThat(lines).containsExactly("Artist: foo", "Warning: bar", "{ready}");
	}

	@Test
	void it_should_read_whole_output_even_if_handler_stops_before() throws Exception {
		responder = request -> new Reply("line 1\nline 2\n", "error 1\nerror 2\n");

		List<String> lines = new ArrayList<>();
		OutputHandler handler = line -> {
			lines.add(line);
			return false;
		};

		strategy.execute(executor, "exiftool", asList("-S", "-execute"), handler);
		strategy.execute(executor, "exiftool", asList("-S", "-execute"), handler);

		assertThat(lines).containsExactly("line 1", "line 1");
		assertThat(singleDaemon().stdout).isEmpty();
		assertThat(singleDaemon().stderr).isEmpty();
	}

	@Test
	void it_should_discard_stale_errors() throws Exception {
		VerbatimOutputHandler out = new VerbatimOutputHandler();
		VerbatimOutputHandler err = new VerbatimOutputHandler();
		strategy.execute(executor, "exiftool", asList("-json", "-execute"), out, err);

		FakeDaemon daemon = singleDaemon();
		daemon.stderr.add("unexpected\n");
		daemon.stderr.add("{ready42}\n");

		responder = request -> {
			// Stale marker printed in the middle of the errors of the command.
			return new Reply("ok\n", "{ready1}\nWarning: foo\n");
		};

		out = new VerbatimOutputHandler();
		err = new VerbatimOutputHandler();
		strategy.execute(executor, "exiftool", asList("-json", "-execute"), out, err);

		assertThat(out.getOutput()).isEqualTo("ok\n");
		assertThat(err.getOutput()).isEqualTo("Warning: foo\n");
	}

	@Test
	void it_should_close_process_if_output_ends_before_marker_and_restart_it() throws Exception {
		strategy.execute(executor, "exiftool", asList("-json", "-execute"), new VerbatimOutputHandler(), new VerbatimOutputHandler());

		responder = request -> Reply.crash("partial output\n");

		VerbatimOutputHandler out = new VerbatimOutputHandler();
		assertThatThrownBy(() -> strategy.execute(executor, "exiftool", asList("-json", "-execute"), out, new VerbatimOutputHandler()))
				.isInstanceOf(IOException.class)
				.hasMessage("ExifTool daemon process stopped before the end of the command output");

		FakeDaemon first = singleDaemon();
		assertThat(first.closed).isTrue();
		assertThat(first.stopped).isTrue();
		assertThat(strategy.isRunning()).isFalse();

		responder = request -> new Reply("ok\n", "");
		VerbatimOutputHandler out2 = new VerbatimOutputHandler();
		strategy.execute(executor, "exiftool", asList("-json", "-execute"), out2, new VerbatimOutputHandler());

		assertThat(out2.getOutput()).isEqualTo("ok\n");
		assertThat(daemons).hasSize(2);
		verify(executor, times(2)).startWithErrorStream(any(Command.class));
	}

	@Test
	void it_should_close_process_if_end_of_errors_is_not_printed() throws Exception {
		strategy.execute(executor, "exiftool", asList("-json", "-execute"), new VerbatimOutputHandler(), new VerbatimOutputHandler());

		FakeDaemon daemon = singleDaemon();
		daemon.printErrorMarker = false;

		assertThatThrownBy(() -> strategy.execute(executor, "exiftool", asList("-json", "-execute"), new VerbatimOutputHandler(), new VerbatimOutputHandler()))
				.isInstanceOf(IOException.class)
				.hasMessage("ExifTool daemon process did not print the end of the command errors");

		assertThat(daemon.closed).isTrue();
		assertThat(strategy.isRunning()).isFalse();
	}

	@Test
	void it_should_close_process_if_version_cannot_be_read() {
		responder = request -> Reply.crash("");

		assertThatThrownBy(() -> strategy.execute(executor, "exiftool", asList("-json", "-execute"), new VerbatimOutputHandler(), new VerbatimOutputHandler()))
				.isInstanceOf(IOException.class)
				.hasMessage("Unable to read the version of the ExifTool daemon process");

		assertThat(singleDaemon().closed).isTrue();
		assertThat(strategy.isRunning()).isFalse();
	}

	@Test
	void it_should_reject_quiet_option_before_12_10() {
		for (String arg : asList("-q", "-Q", "-quiet", " -QUIET", "-T", "-table", "\u2212q")) {
			assertThatThrownBy(() -> strategy.execute(executor, "exiftool", asList("-json", arg, "-execute"), new VerbatimOutputHandler(), new VerbatimOutputHandler()))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining(arg);
		}

		// The process is still usable.
		assertThat(singleDaemon().closed).isFalse();
	}

	@Test
	void it_should_not_reject_options_similar_to_quiet() throws Exception {
		for (String arg : asList("-t", "--q", "-qq", "-TAG", "q", "T")) {
			strategy.execute(executor, "exiftool", asList("-json", arg, "-execute"), new VerbatimOutputHandler(), new VerbatimOutputHandler());
		}

		assertThat(singleDaemon().requests).hasSize(7);
	}

	@Test
	void it_should_allow_quiet_option_with_12_10() throws Exception {
		version = "13.55";
		responder = request -> new Reply("[{}]\n", "");

		VerbatimOutputHandler out = new VerbatimOutputHandler();
		strategy.execute(executor, "exiftool", asList("-json", "-q", "-execute"), out, new VerbatimOutputHandler());

		assertThat(out.getOutput()).isEqualTo("[{}]\n");
	}

	@Test
	void it_should_restart_process_with_merged_errors_before_9_15() throws Exception {
		version = "9.14";
		responder = request -> new Reply("Artist: foo\nWarning: bar\n", "");

		VerbatimOutputHandler out = new VerbatimOutputHandler();
		VerbatimOutputHandler err = new VerbatimOutputHandler();
		strategy.execute(executor, "exiftool", asList("-S", "-Artist", "-execute"), out, err);

		assertThat(daemons).hasSize(2);
		assertThat(daemons.get(0).closed).isTrue();

		FakeDaemon legacy = daemons.get(1);
		assertThat(legacy.requests).hasSize(1);
		assertThat(legacy.requests.get(0).args).containsExactly("-S", "-Artist", "-execute");

		assertThat(out.getOutput()).isEqualTo("Artist: foo\nWarning: bar\n");
		assertThat(err.getOutput()).isEmpty();

		verify(executor).startWithErrorStream(any(Command.class));
		verify(executor).start(any(Command.class));
	}

	@Test
	void it_should_close_process_if_legacy_output_ends_before_ready() throws Exception {
		version = "9.14";
		strategy.execute(executor, "exiftool", asList("-S", "-execute"), new VerbatimOutputHandler(), new VerbatimOutputHandler());

		responder = request -> Reply.crash("partial\n");
		assertThatThrownBy(() -> strategy.execute(executor, "exiftool", asList("-S", "-execute"), new VerbatimOutputHandler(), new VerbatimOutputHandler()))
				.isInstanceOf(IOException.class)
				.hasMessage("ExifTool daemon process stopped before the end of the command output");

		assertThat(daemons.get(1).closed).isTrue();
	}

	@Test
	void it_should_reject_quiet_option_without_framing() {
		version = "9.14";

		assertThatThrownBy(() -> strategy.execute(executor, "exiftool", asList("-json", "-q", "-execute"), new VerbatimOutputHandler(), new VerbatimOutputHandler()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void it_should_not_frame_arguments_not_ending_with_execute() throws Exception {
		responder = request -> new Reply("foo\n", "");

		List<String> lines = new ArrayList<>();
		OutputHandler handler = line -> {
			lines.add(line);
			return line != null && !line.equals("{ready}");
		};

		strategy.execute(executor, "exiftool", asList("-S", "-execute", "-ver"), handler);

		// Output of the last command will be read by next execution: this is the legacy behavior.
		assertThat(singleDaemon().requests.get(1).args).containsExactly("-S", "-execute");
		assertThat(lines).containsExactly("foo", "{ready}");
	}

	@Test
	void it_should_add_charset_on_windows_with_9_79() throws Exception {
		strategy = new StayOpenStrategy(scheduler, true);
		version = "9.79";

		strategy.execute(executor, "exiftool", asList("-json", "C:\\caf\u00e9.jpg", "-execute"), new VerbatimOutputHandler(), new VerbatimOutputHandler());

		Request request = singleDaemon().requests.get(1);
		assertThat(request.args).containsExactly(
				"-echo4", request.marker(), "-charset", "filename=utf8", "-json", "C:\\caf\u00e9.jpg", "-execute" + request.id
		);
	}

	@Test
	void it_should_not_add_charset_on_windows_before_9_79() throws Exception {
		strategy = new StayOpenStrategy(scheduler, true);
		version = "9.78";

		strategy.execute(executor, "exiftool", asList("-json", "-execute"), new VerbatimOutputHandler(), new VerbatimOutputHandler());

		Request request = singleDaemon().requests.get(1);
		assertThat(request.args).containsExactly("-echo4", request.marker(), "-json", "-execute" + request.id);
	}

	@Test
	void it_should_not_add_charset_if_not_on_windows() throws Exception {
		version = "13.55";

		strategy.execute(executor, "exiftool", asList("-json", "-execute"), new VerbatimOutputHandler(), new VerbatimOutputHandler());

		Request request = singleDaemon().requests.get(1);
		assertThat(request.args).containsExactly("-echo4", request.marker(), "-json", "-execute" + request.id);
	}

	private FakeDaemon singleDaemon() {
		assertThat(daemons).hasSize(1);
		return daemons.get(0);
	}

	private FakeDaemon newDaemon(boolean errorStream) {
		FakeDaemon daemon = new FakeDaemon(errorStream);
		daemons.add(daemon);
		return daemon;
	}

	/// A command received by the fake daemon.
	private static final class Request {
		private final List<String> args;
		private final long id;

		private Request(List<String> args, long id) {
			this.args = args;
			this.id = id;
		}

		private String marker() {
			return "{ready" + id + "}";
		}
	}

	/// Response of the fake daemon to a command.
	private static final class Reply {
		private final String output;
		private final String errors;
		private final boolean crash;

		private Reply(String output, String errors) {
			this(output, errors, false);
		}

		private Reply(String output, String errors, boolean crash) {
			this.output = output;
			this.errors = errors;
			this.crash = crash;
		}

		/// The process stops after printing given output.
		private static Reply crash(String output) {
			return new Reply(output, "", true);
		}
	}

	/// Fake exiftool daemon process, emulating `-stay_open True -@ -`.
	private final class FakeDaemon implements CommandProcess {
		private final boolean errorStream;
		private final List<Request> requests = new ArrayList<>();
		private final List<String> pending = new ArrayList<>();
		private final Deque<String> stdout = new ArrayDeque<>();
		private final Deque<String> stderr = new ArrayDeque<>();
		private boolean printErrorMarker = true;
		private boolean closed = false;
		private boolean stopped = false;

		private FakeDaemon(boolean errorStream) {
			this.errorStream = errorStream;
		}

		@Override
		public String read() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String read(OutputHandler handler) {
			// Once output is consumed, the end of the stream is reached (a real process would block if it is still running).
			boolean hasNext = true;
			while (hasNext) {
				hasNext = handler.readRawLine(stdout.poll());
			}

			return "";
		}

		@Override
		public boolean hasErrorStream() {
			return errorStream;
		}

		@Override
		public String readErrorLine(long timeout, TimeUnit unit) {
			assertThat(errorStream).isTrue();
			return stderr.poll();
		}

		@Override
		public void write(String input, String... others) {
			List<String> inputs = new ArrayList<>();
			inputs.add(input);
			inputs.addAll(asList(others));
			write(inputs);
		}

		@Override
		public void write(Iterable<String> inputs) {
			assertThat(closed).isFalse();

			for (String input : inputs) {
				if (input.equals("-stay_open\nFalse\n")) {
					stopped = true;
					continue;
				}

				assertThat(input).endsWith(BR);
				String arg = input.substring(0, input.length() - BR.length());
				assertThat(arg).doesNotContain("\n");
				pending.add(arg);

				if (arg.matches("-execute\\d*")) {
					respond(new ArrayList<>(pending));
					pending.clear();
				}
			}
		}

		private void respond(List<String> args) {
			String execute = args.get(args.size() - 1);
			long id = execute.length() > "-execute".length() ? Long.parseLong(execute.substring("-execute".length())) : 0;
			Request request = new Request(args, id);
			requests.add(request);

			Reply reply = responder.apply(request);
			if (!reply.crash && args.equals(asList("-ver", "-execute"))) {
				reply = new Reply(version + "\n", "");
			}

			if (reply.crash) {
				stdout.addAll(rawLines(reply.output));
				return;
			}

			int echo4 = args.indexOf("-echo4");
			String errors = reply.errors;
			if (echo4 >= 0 && printErrorMarker) {
				errors += args.get(echo4 + 1) + "\n";
			}

			if (errorStream) {
				stderr.addAll(rawLines(errors));
				stdout.addAll(rawLines(reply.output + "{ready" + (id > 0 ? id : "") + "}\n"));
			}
			else {
				stdout.addAll(rawLines(errors + reply.output + "{ready" + (id > 0 ? id : "") + "}\n"));
			}
		}

		@Override
		public void flush() {
		}

		@Override
		public boolean isRunning() {
			return !closed;
		}

		@Override
		public boolean isClosed() {
			return closed;
		}

		@Override
		public void close() {
			closed = true;
		}
	}

	private static List<String> rawLines(String text) {
		List<String> lines = new ArrayList<>();
		int start = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '\n') {
				lines.add(text.substring(start, i + 1));
				start = i + 1;
			}
		}

		if (start < text.length()) {
			lines.add(text.substring(start));
		}

		return lines;
	}
}
