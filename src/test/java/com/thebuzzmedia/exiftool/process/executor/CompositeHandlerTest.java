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

import com.thebuzzmedia.exiftool.process.OutputHandler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompositeHandlerTest {

	@Test
	void it_should_read_line() {
		String line = "foo";

		OutputHandler h1 = mock(OutputHandler.class);
		OutputHandler h2 = mock(OutputHandler.class);

		when(h1.readLine(anyString())).thenReturn(true);
		when(h2.readLine(anyString())).thenReturn(true);

		CompositeHandler handler = new CompositeHandler(h1, h2);

		boolean result = handler.readLine(line);

		assertThat(result).isTrue();
		verify(h1).readLine(line);
		verify(h2).readLine(line);
	}

	@Test
	void it_should_read_line_and_stop_with_first_false_value() {
		String line = "foo";
		String end = "{ready}";

		OutputHandler h1 = mock(OutputHandler.class);
		OutputHandler h2 = mock(OutputHandler.class);

		when(h1.readLine(line)).thenReturn(true);
		when(h2.readLine(line)).thenReturn(true);

		when(h1.readLine(end)).thenReturn(false);
		when(h2.readLine(end)).thenReturn(true);

		CompositeHandler handler = new CompositeHandler(h1, h2);

		boolean r1 = handler.readLine(line);
		assertThat(r1).isTrue();
		verify(h1).readLine(line);
		verify(h2).readLine(line);

		boolean r2 = handler.readLine(end);
		assertThat(r2).isFalse();
		verify(h1).readLine(end);
		verify(h2).readLine(end);
	}

	@Test
	void it_should_read_raw_line() {
		String rawLine = "foo\r\n";

		OutputHandler h1 = mock(OutputHandler.class);
		OutputHandler h2 = mock(OutputHandler.class);

		when(h1.readRawLine(rawLine)).thenReturn(true);
		when(h2.readRawLine(rawLine)).thenReturn(false);

		CompositeHandler handler = new CompositeHandler(h1, h2);

		boolean result = handler.readRawLine(rawLine);

		assertThat(result).isFalse();
		verify(h1).readRawLine(rawLine);
		verify(h2).readRawLine(rawLine);
		verify(h1, never()).readLine(anyString());
		verify(h2, never()).readLine(anyString());
	}
}
