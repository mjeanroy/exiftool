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
package com.thebuzzmedia.exiftool;

import com.thebuzzmedia.exiftool.exceptions.UnreadableFileException;
import com.thebuzzmedia.exiftool.process.Command;
import com.thebuzzmedia.exiftool.process.CommandExecutor;
import com.thebuzzmedia.exiftool.process.CommandResult;
import com.thebuzzmedia.exiftool.process.OutputHandler;
import com.thebuzzmedia.exiftool.tests.builders.CommandResultBuilder;
import com.thebuzzmedia.exiftool.tests.builders.FileBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static com.thebuzzmedia.exiftool.tests.MockitoTestUtils.anyListOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExifTool_execute_Test {

	private String path;
	private CommandExecutor executor;
	private ExecutionStrategy strategy;
	private File image;

	private ExifTool exifTool;

	@BeforeEach
	void setUp() throws Exception {
		executor = mock(CommandExecutor.class);
		strategy = mock(ExecutionStrategy.class);
		path = "exiftool";
		image = new FileBuilder("foo.png").build();

		CommandResult result = new CommandResultBuilder().output("9.36").build();
		when(executor.execute(any(Command.class))).thenReturn(result);
		when(strategy.isSupported(any(Version.class))).thenReturn(true);

		exifTool = new ExifTool(path, executor, strategy);

		reset(executor);
	}

	@Test
	void it_should_fail_if_file_is_null() {
		assertThatThrownBy(() -> exifTool.execute(null, singletonList("-json")))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("File cannot be null.");
	}

	@Test
	void it_should_fail_if_arguments_is_null() {
		assertThatThrownBy(() -> exifTool.execute(image, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Arguments cannot be null.");
	}

	@Test
	void it_should_fail_if_arguments_contains_null() {
		assertThatThrownBy(() -> exifTool.execute(image, asList("-json", null)))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Arguments cannot contain null.");
	}

	@Test
	void it_should_fail_with_unknown_file() {
		File file = new FileBuilder("foo.png").exists(false).build();
		assertThatThrownBy(() -> exifTool.execute(file, singletonList("-json")))
				.isInstanceOf(UnreadableFileException.class)
				.hasMessage(
						"Unable to read the given image [/tmp/foo.png], " +
								"ensure that the image exists at the given withPath and that " +
								"the executing Java process has permissions to read it."
				);
	}

	@Test
	void it_should_fail_with_non_readable_file() {
		File file = new FileBuilder("foo.png").canRead(false).build();
		assertThatThrownBy(() -> exifTool.execute(file, singletonList("-json")))
				.isInstanceOf(UnreadableFileException.class);
	}

	@Test
	void it_should_fail_with_file_path_containing_a_line_break() {
		File file = new FileBuilder("foo\nbar.png").build();
		assertThatThrownBy(() -> exifTool.execute(file, singletonList("-json")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("File path cannot contain a line break: /tmp/foo\nbar.png");
	}

	@Test
	void it_should_fail_with_arguments_containing_a_line_break() throws Exception {
		for (String arg : asList("-Comment=a\nb", "-Comment=a\rb", "\n", "-json\r")) {
			assertThatThrownBy(() -> exifTool.execute(image, asList("-n", arg)))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Argument cannot contain a line break: " + arg);
		}

		verify(strategy, never()).execute(any(CommandExecutor.class), any(String.class), anyListOf(String.class), any(OutputHandler.class), any(OutputHandler.class));
	}

	@Test
	void it_should_fail_with_reserved_arguments() throws Exception {
		List<String> reserved = asList(
				"-stay_open", "-STAY_OPEN", "-@", "-execute", "-execute3", "-EXECUTE", "-Execute12",
				"-echo3", "-echo4", "-ECHO4", "--", " -execute", "\t-echo4", "−execute", "#[CSTR]-execute"
		);

		for (String arg : reserved) {
			assertThatThrownBy(() -> exifTool.execute(image, asList("-json", arg)))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Argument is reserved and cannot be used: " + arg);
		}

		verify(strategy, never()).execute(any(CommandExecutor.class), any(String.class), anyListOf(String.class), any(OutputHandler.class), any(OutputHandler.class));
	}

	@Test
	void it_should_accept_arguments_similar_to_reserved_ones() throws Exception {
		List<String> allowed = asList(
				"--FileName", "-echo", "-echo2", "-echo1", "-executed", "-stay_opened", "-@@", "execute", "stay_open",
				"-Comment=-execute", "-", "", "-json", "-n", "-q"
		);

		exifTool.execute(image, allowed);

		verify(strategy).execute(same(executor), same(path), anyListOf(String.class), any(OutputHandler.class), any(OutputHandler.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	void it_should_execute_given_arguments_on_file() throws Exception {
		when(strategy.execute(same(executor), same(path), anyListOf(String.class), any(OutputHandler.class), any(OutputHandler.class)))
				.thenAnswer(invocation -> {
					OutputHandler output = invocation.getArgument(3);
					OutputHandler errors = invocation.getArgument(4);
					output.readRawLine("[{\r\n");
					output.readRawLine("  \"Artist\": \"café\"\r\n");
					output.readRawLine("}]");
					errors.readRawLine("Warning: foo\n");
					return 0;
				});

		ExifToolResult result = exifTool.execute(image, asList("-json", "-n"));

		assertThat(result.getOutput()).isEqualTo("[{\r\n  \"Artist\": \"café\"\r\n}]");
		assertThat(result.getErrors()).isEqualTo("Warning: foo\n");
		assertThat(result.getExitCode()).isZero();

		ArgumentCaptor<List<String>> argsCaptor = ArgumentCaptor.forClass(List.class);
		verify(strategy).execute(same(executor), same(path), argsCaptor.capture(), any(OutputHandler.class), any(OutputHandler.class));
		assertThat(argsCaptor.getValue()).containsExactly("-json", "-n", "/tmp/foo.png", "-execute");

		verify(strategy, never()).execute(any(CommandExecutor.class), any(String.class), anyListOf(String.class), any(OutputHandler.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	void it_should_execute_without_arguments() throws Exception {
		when(strategy.execute(same(executor), same(path), anyListOf(String.class), any(OutputHandler.class), any(OutputHandler.class))).thenReturn(null);

		ExifToolResult result = exifTool.execute(image, Collections.emptyList());

		assertThat(result.getOutput()).isEmpty();
		assertThat(result.getErrors()).isEmpty();
		assertThat(result.getExitCode()).isNull();

		ArgumentCaptor<List<String>> argsCaptor = ArgumentCaptor.forClass(List.class);
		verify(strategy).execute(same(executor), same(path), argsCaptor.capture(), any(OutputHandler.class), any(OutputHandler.class));
		assertThat(argsCaptor.getValue()).containsExactly("/tmp/foo.png", "-execute");
	}
}
