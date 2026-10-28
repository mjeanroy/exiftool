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

import com.thebuzzmedia.exiftool.process.Command;
import com.thebuzzmedia.exiftool.process.CommandExecutor;
import com.thebuzzmedia.exiftool.process.OutputHandler;
import com.thebuzzmedia.exiftool.process.executor.DefaultCommandResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("resource")
class DefaultStrategyTest {

	@Test
	void it_should_execute_command() throws Exception {
		String exifTool = "exiftool";
		List<String> args = asList("-S", "-n", "-XArtist", "-XComment", "-execute");
		CommandExecutor executor = mock(CommandExecutor.class);
		OutputHandler handler = mock(OutputHandler.class);

		DefaultStrategy strategy = new DefaultStrategy();
		strategy.execute(executor, exifTool, args, handler);

		ArgumentCaptor<Command> cmdCaptor = ArgumentCaptor.forClass(Command.class);
		verify(executor).execute(cmdCaptor.capture(), same(handler));

		List<String> expectedArguments = new ArrayList<>();
		expectedArguments.add(exifTool);
		expectedArguments.addAll(args);

		Command cmd = cmdCaptor.getValue();
		assertThat(cmd.getArguments()).hasSameSizeAs(expectedArguments).isEqualTo(expectedArguments);
	}

	@Test
	void it_should_never_be_running() {
		DefaultStrategy strategy = new DefaultStrategy();
		assertThat(strategy.isRunning()).isFalse();
	}

	@Test
	void it_should_do_nothing_on_close() {
		new DefaultStrategy().close();
	}

	@Test
	void it_should_do_nothing_on_shutdown() {
		new DefaultStrategy().shutdown();
	}

	@Test
	void it_should_execute_command_and_read_errors_separately() throws Exception {
		String exifTool = "exiftool";
		List<String> args = asList("-json", "/tmp/foo.png", "-execute");
		CommandExecutor executor = mock(CommandExecutor.class);
		OutputHandler handler = mock(OutputHandler.class);
		OutputHandler errorHandler = mock(OutputHandler.class);
		when(executor.execute(any(Command.class), same(handler), same(errorHandler))).thenReturn(new DefaultCommandResult(1, ""));

		DefaultStrategy strategy = new DefaultStrategy();
		Integer exitCode = strategy.execute(executor, exifTool, args, handler, errorHandler);

		assertThat(exitCode).isEqualTo(1);

		ArgumentCaptor<Command> cmdCaptor = ArgumentCaptor.forClass(Command.class);
		verify(executor).execute(cmdCaptor.capture(), same(handler), same(errorHandler));
		assertThat(cmdCaptor.getValue().getArguments()).containsExactly(exifTool, "-json", "/tmp/foo.png", "-execute");
	}
}
