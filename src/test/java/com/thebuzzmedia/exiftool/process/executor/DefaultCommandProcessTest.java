/**
 * Copyright 2011 The Buzz Media, LLC
 * Copyright 2015-2019 Mickael Jeanroy
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

import com.thebuzzmedia.exiftool.process.OutputHandler;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.thebuzzmedia.exiftool.tests.TestConstants.BR;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultCommandProcessTest {

	@Test
	void it_should_fail_with_null_input() {
		assertThatThrownBy(() -> new DefaultCommandProcess(null, mock(OutputStream.class), mock(InputStream.class)))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Input stream should not be null");
	}

	@Test
	void it_should_fail_with_null_output() {
		assertThatThrownBy(() -> new DefaultCommandProcess(mock(InputStream.class), null, mock(InputStream.class)))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Output stream should not be null");
	}

	@Test
	void it_should_fail_with_null_error_input() {
		assertThatThrownBy(() -> new DefaultCommandProcess(mock(InputStream.class), mock(OutputStream.class), null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Error stream should not be null");
	}

	@Test
	void it_should_close_process() throws Exception {
		OutputStream os = mock(OutputStream.class);
		InputStream is = mock(InputStream.class);
		InputStream err = mock(InputStream.class);

		DefaultCommandProcess process = new DefaultCommandProcess(is, os, err);
		assertThat(process.isClosed()).isFalse();
		assertThat(process.isRunning()).isTrue();

		process.close();

		verify(is).close();
		verify(os).close();
		verify(err).close();

		assertThat(process.isClosed()).isTrue();
		assertThat(process.isRunning()).isFalse();
	}

	@Test
	void it_should_close_process_with_first_failure() throws Exception {
		OutputStream os = mock(OutputStream.class);
		InputStream is = mock(InputStream.class);
		InputStream err = mock(InputStream.class);

		IOException ex = new IOException("fail");
		doThrow(ex).when(os).close();

		DefaultCommandProcess process = new DefaultCommandProcess(is, os, err);
		assertThat(process.isClosed()).isFalse();
		assertThat(process.isRunning()).isTrue();

		try {
			process.close();
			failBecauseExceptionWasNotThrown(IOException.class);
		}
		catch (IOException e) {
			assertThat(e).isSameAs(ex);

			verify(is).close();
			verify(os).close();
			verify(err).close();

			assertThat(process.isClosed()).isTrue();
			assertThat(process.isRunning()).isFalse();
		}
	}

	@Test
	void it_should_close_process_with_second_failure() throws Exception {
		OutputStream os = mock(OutputStream.class);
		InputStream is = mock(InputStream.class);
		InputStream err = mock(InputStream.class);

		IOException ex = new IOException("fail");
		doThrow(ex).when(is).close();

		DefaultCommandProcess process = new DefaultCommandProcess(is, os, err);
		assertThat(process.isClosed()).isFalse();
		assertThat(process.isRunning()).isTrue();

		try {
			process.close();
			failBecauseExceptionWasNotThrown(IOException.class);
		}
		catch (IOException e) {
			assertThat(e).isSameAs(ex);

			verify(is).close();
			verify(os).close();
			verify(err).close();

			assertThat(process.isClosed()).isTrue();
			assertThat(process.isRunning()).isFalse();
		}
	}

	@Test
	void it_should_close_process_with_third_failure() throws Exception {
		OutputStream os = mock(OutputStream.class);
		InputStream is = mock(InputStream.class);
		InputStream err = mock(InputStream.class);

		IOException ex = new IOException("fail");
		doThrow(ex).when(err).close();

		DefaultCommandProcess process = new DefaultCommandProcess(is, os, err);
		assertThat(process.isClosed()).isFalse();
		assertThat(process.isRunning()).isTrue();

		try {
			process.close();
			failBecauseExceptionWasNotThrown(IOException.class);
		}
		catch (IOException e) {
			assertThat(e).isSameAs(ex);

			verify(is).close();
			verify(os).close();
			verify(err).close();

			assertThat(process.isClosed()).isTrue();
			assertThat(process.isRunning()).isFalse();
		}
	}

	@Test
	void it_should_close_process_with_all_failure() throws Exception {
		OutputStream os = mock(OutputStream.class);
		InputStream is = mock(InputStream.class);
		InputStream err = mock(InputStream.class);

		IOException ex1 = new IOException("fail1");
		doThrow(ex1).when(os).close();

		IOException ex2 = new IOException("fail2");
		doThrow(ex2).when(is).close();

		IOException ex3 = new IOException("fail3");
		doThrow(ex3).when(err).close();

		DefaultCommandProcess process = new DefaultCommandProcess(is, os, err);
		assertThat(process.isClosed()).isFalse();
		assertThat(process.isRunning()).isTrue();

		try {
			process.close();
			failBecauseExceptionWasNotThrown(IOException.class);
		}
		catch (IOException e) {
			assertThat(e).isSameAs(ex1);

			verify(is).close();
			verify(os).close();
			verify(err).close();

			assertThat(process.isClosed()).isTrue();
			assertThat(process.isRunning()).isFalse();
		}
	}

	@Test
	void it_should_read_from_input() throws Exception {
		String firstLine = "first-line";
		String secondLine = "second-line";
		String output = firstLine + BR + secondLine;
		InputStream stream = new ByteArrayInputStream(output.getBytes(StandardCharsets.UTF_8));

		DefaultCommandProcess process = new DefaultCommandProcess(stream, mock(OutputStream.class), mock(InputStream.class));
		String out = process.read();
		assertThat(out).isEqualTo(output);
	}

	@Test
	void it_should_not_read_from_closed_process() throws Exception {
		DefaultCommandProcess process = new DefaultCommandProcess(mock(InputStream.class), mock(OutputStream.class), mock(InputStream.class));

		process.close();

		assertThatThrownBy(() -> process.read())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Cannot read from closed process");
	}

	@Test
	void it_should_read_from_input_and_use_handler() throws Exception {
		String firstLine = "first-line";
		String secondLine = "{ready}";
		String thirdLine = "third-line";
		String output = firstLine + BR + secondLine + BR + thirdLine;
		InputStream stream = new ByteArrayInputStream(output.getBytes(StandardCharsets.UTF_8));

		OutputHandler handler = mock(OutputHandler.class);
		when(handler.readLine(anyString())).thenAnswer((Answer<Boolean>) invocation -> {
			String line = (String) invocation.getArguments()[0];
			return !line.equals("{ready}");
		});

		DefaultCommandProcess process = new DefaultCommandProcess(stream, mock(OutputStream.class), mock(InputStream.class));
		String out = process.read(handler);

		assertThat(out).isEqualTo(firstLine + BR + secondLine);

		verify(handler, times(2)).readLine(anyString());
		verify(handler).readLine(firstLine);
		verify(handler).readLine(secondLine);
		verify(handler, never()).readLine(thirdLine);
	}

	@Test
	void it_should_write_from_output() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		String message = "foo";

		DefaultCommandProcess process = new DefaultCommandProcess(mock(InputStream.class), os, mock(InputStream.class));
		process.write(message);

		assertThat(os.toString()).isEqualTo(message);
	}

	@Test
	void it_should_catch_write_failure() throws Exception {
		OutputStream os = mock(OutputStream.class);
		doThrow(new IOException("fail")).when(os).write(any(byte[].class));

		DefaultCommandProcess process = new DefaultCommandProcess(mock(InputStream.class), os, mock(InputStream.class));

		assertThatThrownBy(() -> process.write("foo")).isInstanceOf(IOException.class);
	}

	@Test
	void it_should_not_write_null_output() {
		DefaultCommandProcess process = new DefaultCommandProcess(mock(InputStream.class), mock(OutputStream.class), mock(InputStream.class));
		assertThatThrownBy(() -> process.write((String) null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Write input should not be null");
	}

	@Test
	void it_should_not_write_null_outputs() {
		List<String> inputs = null;
		DefaultCommandProcess process = new DefaultCommandProcess(mock(InputStream.class), mock(OutputStream.class), mock(InputStream.class));
		assertThatThrownBy(() -> process.write(inputs))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Write inputs should not be empty");
	}

	@Test
	void it_should_not_write_empty_outputs() {
		List<String> inputs = emptyList();
		DefaultCommandProcess process = new DefaultCommandProcess(mock(InputStream.class), mock(OutputStream.class), mock(InputStream.class));
		assertThatThrownBy(() -> process.write(inputs))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Write inputs should not be empty");
	}

	@Test
	void it_should_not_write_from_closed_process() throws Exception {
		String input = "foo";
		DefaultCommandProcess process = new DefaultCommandProcess(mock(InputStream.class), mock(OutputStream.class), mock(InputStream.class));

		process.close();

		assertThatThrownBy(() -> process.write(input))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Cannot write from closed process");
	}

	@Test
	void it_should_write_inputs() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		String msg1 = "foo";
		String msg2 = "bar";

		DefaultCommandProcess process = new DefaultCommandProcess(mock(InputStream.class), os, mock(InputStream.class));
		process.write(asList(msg1, msg2));

		assertThat(os.toString()).isEqualTo(msg1 + msg2);
	}
}
