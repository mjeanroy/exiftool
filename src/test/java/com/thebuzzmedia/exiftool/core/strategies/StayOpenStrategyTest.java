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
import com.thebuzzmedia.exiftool.process.Command;
import com.thebuzzmedia.exiftool.process.CommandExecutor;
import com.thebuzzmedia.exiftool.process.CommandProcess;
import com.thebuzzmedia.exiftool.process.OutputHandler;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.thebuzzmedia.exiftool.tests.ReflectionTestUtils.writePrivateField;
import static com.thebuzzmedia.exiftool.tests.TestConstants.BR;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class StayOpenStrategyTest {

	private Scheduler scheduler;
	private CommandProcess process;
	private CommandExecutor executor;
	private OutputHandler outputHandler;

	private String exifTool;
	private List<String> args;
	private StayOpenStrategy strategy;

	@BeforeEach
	void setUp() throws Exception {
		scheduler = mock(Scheduler.class);
		process = mock(CommandProcess.class);
		executor = mock(CommandExecutor.class);
		outputHandler = mock(OutputHandler.class);
		exifTool = "exiftool";

		// Mock withExecutor
		when(executor.startWithErrorStream(any(Command.class))).thenReturn(process);

		// Mock process output
		when(process.read(any(OutputHandler.class))).thenAnswer(output("{ready}\n"));

		// Execution arguments
		args = asList("-S", "-n", "-XArtist", "XComment", "-execute");
	}

	@AfterEach
	void tearDown() {
		if (strategy != null) {
			try {
				strategy.close();
			}
			catch (Exception ex) {
				// No worry, that's ok in these unit tests.
			}
		}
	}

	@Test
	void it_should_create_stay_open_strategy() {
		strategy = new StayOpenStrategy(scheduler);
		assertThat(strategy).extracting("scheduler").isSameAs(scheduler);
		assertThat(strategy).extracting("process").isNull();
	}

	@SuppressWarnings("unchecked")
	@Test
	void it_should_execute_command() throws Exception {
		strategy = new StayOpenStrategy(scheduler);
		strategy.execute(executor, exifTool, args, outputHandler);

		ArgumentCaptor<Command> cmdCaptor = ArgumentCaptor.forClass(Command.class);
		ArgumentCaptor<List<String>> argsCaptor = ArgumentCaptor.forClass(List.class);
		InOrder inOrder = inOrder(scheduler, executor, process);
		inOrder.verify(executor).startWithErrorStream(cmdCaptor.capture());
		inOrder.verify(scheduler).stop();
		inOrder.verify(scheduler).start(any(Runnable.class));
		inOrder.verify(process).write(argsCaptor.capture());
		inOrder.verify(process).flush();
		inOrder.verify(process).read(any(OutputHandler.class));

		assertThat(strategy).extracting("process").isSameAs(process);

		verifyStartProcess(cmdCaptor);
		verifyExecutionArguments(argsCaptor);
	}

	@SuppressWarnings("unchecked")
	@Test
	void it_should_not_start_process_twice_if_it_is_running() throws Exception {
		strategy = new StayOpenStrategy(scheduler);

		// Mock Process
		writePrivateField(strategy, "process", process);
		when(process.isClosed()).thenReturn(false);

		strategy.execute(executor, exifTool, args, outputHandler);

		// Executor should not have been started
		verify(executor, never()).startWithErrorStream(any(Command.class));
		verify(executor, never()).start(any(Command.class));

		ArgumentCaptor<List<String>> argsCaptor = ArgumentCaptor.forClass(List.class);
		InOrder inOrder = inOrder(scheduler, process);
		inOrder.verify(process).isClosed();
		inOrder.verify(scheduler).stop();
		inOrder.verify(scheduler).start(any(Runnable.class));
		inOrder.verify(process).write(argsCaptor.capture());
		inOrder.verify(process).flush();
		inOrder.verify(process).read(any(OutputHandler.class));

		verifyExecutionArguments(argsCaptor);
	}

	@SuppressWarnings("unchecked")
	@Test
	void it_should_restart_process_twice_if_it_is_not_running() throws Exception {
		strategy = new StayOpenStrategy(scheduler);

		// Mock Process
		writePrivateField(strategy, "process", process);
		when(process.isClosed()).thenReturn(true);

		strategy.execute(executor, exifTool, args, outputHandler);

		ArgumentCaptor<Command> cmdCaptor = ArgumentCaptor.forClass(Command.class);
		ArgumentCaptor<List<String>> argsCaptor = ArgumentCaptor.forClass(List.class);
		InOrder inOrder = inOrder(scheduler, process, executor);
		inOrder.verify(process).isClosed();
		inOrder.verify(executor).startWithErrorStream(cmdCaptor.capture());
		inOrder.verify(scheduler).stop();
		inOrder.verify(scheduler).start(any(Runnable.class));
		inOrder.verify(process).write(argsCaptor.capture());
		inOrder.verify(process).flush();
		inOrder.verify(process).read(any(OutputHandler.class));

		verifyStartProcess(cmdCaptor);
		verifyExecutionArguments(argsCaptor);
	}

	@Test
	void it_should_try_to_close_process_if_it_is_not_started() throws Exception {
		strategy = new StayOpenStrategy(scheduler);
		strategy.close();
		verify(scheduler).stop();
	}

	@Test
	void it_should_try_to_shutdown_process_if_it_is_not_started() throws Exception {
		strategy = new StayOpenStrategy(scheduler);
		strategy.shutdown();
		verify(scheduler).shutdown();
	}

	@Test
	void it_should_close_process_if_it_is_started() throws Exception {
		strategy = new StayOpenStrategy(scheduler);
		writePrivateField(strategy, "process", process);
		strategy.close();

		InOrder inOrder = inOrder(scheduler, process);
		inOrder.verify(process).write("-stay_open\nFalse\n");
		inOrder.verify(process).flush();
		inOrder.verify(process).close();
		inOrder.verify(scheduler).stop();

		verifyNoMoreInteractions(process);
		verifyNoMoreInteractions(scheduler);
	}

	@Test
	void it_should_shutdown_process_if_it_is_started() throws Exception {
		strategy = new StayOpenStrategy(scheduler);
		writePrivateField(strategy, "process", process);
		strategy.shutdown();

		InOrder inOrder = inOrder(scheduler, process);
		inOrder.verify(process).write("-stay_open\nFalse\n");
		inOrder.verify(process).flush();
		inOrder.verify(process).close();
		inOrder.verify(scheduler).stop();
		inOrder.verify(scheduler).shutdown();

		verifyNoMoreInteractions(process);
		verifyNoMoreInteractions(scheduler);
	}

	@Test
	void it_should_read_whole_output_even_if_handler_stops_before() throws Exception {
		when(process.read(any(OutputHandler.class))).thenAnswer(output("line 1\n", "line 2\n", "{ready}\n", "next\n"));

		List<String> lines = new ArrayList<>();
		OutputHandler handler = line -> {
			lines.add(line);
			return false;
		};

		strategy = new StayOpenStrategy(scheduler);
		strategy.execute(executor, exifTool, args, handler);

		assertThat(lines).containsExactly("line 1");
		verify(process, never()).close();
		assertThat(strategy).extracting("process").isSameAs(process);
	}

	@Test
	void it_should_close_process_if_output_ends_before_ready_and_restart_it() throws Exception {
		when(process.read(any(OutputHandler.class))).thenAnswer(output("partial output\n", null));

		strategy = new StayOpenStrategy(scheduler);
		assertThatThrownBy(() -> strategy.execute(executor, exifTool, args, outputHandler))
				.isInstanceOf(IOException.class)
				.hasMessage("ExifTool daemon process stopped before the end of the command output");

		verify(process).write("-stay_open\nFalse\n");
		verify(process).close();
		assertThat(strategy).extracting("process").isNull();
		assertThat(strategy.isRunning()).isFalse();

		CommandProcess next = mock(CommandProcess.class);
		when(next.read(any(OutputHandler.class))).thenAnswer(output("{ready}\n"));
		when(executor.startWithErrorStream(any(Command.class))).thenReturn(next);

		strategy.execute(executor, exifTool, args, outputHandler);

		verify(executor, times(2)).startWithErrorStream(any(Command.class));
		assertThat(strategy).extracting("process").isSameAs(next);
	}

	@Test
	void it_should_close_process_if_output_cannot_be_read() throws Exception {
		when(process.read(any(OutputHandler.class))).thenThrow(new IOException("Stream closed"));

		strategy = new StayOpenStrategy(scheduler);
		assertThatThrownBy(() -> strategy.execute(executor, exifTool, args, outputHandler))
				.isInstanceOf(IOException.class)
				.hasMessage("Stream closed");

		verify(process).close();
		assertThat(strategy).extracting("process").isNull();
	}

	@Test
	void it_should_close_process_even_if_stop_command_cannot_be_written() throws Exception {
		doThrow(new IOException("Broken pipe")).when(process).write("-stay_open\nFalse\n");

		strategy = new StayOpenStrategy(scheduler);
		strategy.execute(executor, exifTool, args, outputHandler);

		assertThatThrownBy(() -> strategy.close())
				.isInstanceOf(IOException.class)
				.hasMessage("Broken pipe");

		verify(process).close();
		assertThat(strategy.isRunning()).isFalse();
	}

	@Test
	void it_should_check_if_process_is_running() {
		strategy = new StayOpenStrategy(scheduler);
		assertThat(strategy.isRunning()).isFalse();

		writePrivateField(strategy, "process", process);

		when(process.isRunning()).thenReturn(true);
		assertThat(strategy.isRunning()).isTrue();

		when(process.isRunning()).thenReturn(false);
		assertThat(strategy.isRunning()).isFalse();
	}

	private void verifyStartProcess(ArgumentCaptor<Command> cmdCaptor) {
		Command startCmd = cmdCaptor.getValue();
		assertThat(startCmd.getArguments()).hasSize(5).containsExactly(
				exifTool, "-stay_open", "True", "-@", "-"
		);
	}

	private void verifyExecutionArguments(ArgumentCaptor<List<String>> argsCaptor) {
		List<String> processArgs = argsCaptor.getValue();
		assertThat(processArgs)
				.isNotEmpty()
				.are(new Condition<String>() {
					@Override
					public boolean matches(String value) {
						return value.endsWith(BR);
					}
				})
				.isEqualTo(appendBr(args));
	}

	/// Emulate the output of a process: given raw lines are given to the handler, until it returns `false`.
	///
	/// @param rawLines Raw lines, `null` for the end of the stream.
	/// @return The answer.
	private static Answer<String> output(String... rawLines) {
		return invocation -> {
			OutputHandler handler = invocation.getArgument(0);
			for (String rawLine : rawLines) {
				if (!handler.readRawLine(rawLine)) {
					break;
				}
			}

			return "";
		};
	}

	private List<String> appendBr(List<String> list) {
		List<String> results = new ArrayList<>(list.size());
		for (String input : list) {
			results.add(input + BR);
		}

		return results;
	}
}
