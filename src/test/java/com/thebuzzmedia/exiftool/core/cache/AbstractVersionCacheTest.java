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

package com.thebuzzmedia.exiftool.core.cache;

import com.thebuzzmedia.exiftool.Version;
import com.thebuzzmedia.exiftool.VersionCache;
import com.thebuzzmedia.exiftool.exceptions.ExifToolNotFoundException;
import com.thebuzzmedia.exiftool.process.Command;
import com.thebuzzmedia.exiftool.process.CommandExecutor;
import com.thebuzzmedia.exiftool.process.CommandResult;
import com.thebuzzmedia.exiftool.tests.builders.CommandResultBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

abstract class AbstractVersionCacheTest<T extends VersionCache> {

	private String exifTool;

	private CommandExecutor executor;

	private ArgumentCaptor<Command> cmdCaptor;

	@BeforeEach
	void setUp() throws Exception {
		exifTool = "exiftool";
		executor = mock(CommandExecutor.class);
		cmdCaptor = ArgumentCaptor.forClass(Command.class);

		CommandResult result = new CommandResultBuilder()
				.output("9.36")
				.build();

		when(executor.execute(any(Command.class))).thenReturn(result);
	}

	@Test
	void it_should_get_version() throws Exception {
		VersionCache cache = create();
		Version version = cache.load(exifTool, executor);

		assertThat(version).isEqualTo(new Version("9.36.0"));
		verify(executor).execute(cmdCaptor.capture());
		assertThat(cmdCaptor.getValue().getArguments()).hasSize(2).containsExactly(
				exifTool, "-ver"
		);
	}

	@Test
	void it_should_throw_exception_without_success_response() throws Exception {
		VersionCache cache = create();
		CommandResult result = new CommandResultBuilder().success(false).output(null).build();

		when(executor.execute(any(Command.class))).thenReturn(result);

		assertThatThrownBy(() -> cache.load(exifTool, executor))
				.isInstanceOf(ExifToolNotFoundException.class)
				.hasMessage("Cannot find exiftool from path: exiftool");
	}

	@Test
	void it_should_return_null_with_io_exception() throws Exception {
		VersionCache cache = create();
		IOException ex = new IOException();

		when(executor.execute(any(Command.class))).thenThrow(ex);

		assertThatThrownBy(() -> cache.load(exifTool, executor))
				.isInstanceOf(ExifToolNotFoundException.class)
				.hasCause(ex);
	}

	@Test
	void it_should_get_version_once() throws Exception {
		VersionCache cache = create();

		Version v1 = cache.load(exifTool, executor);
		assertThat(v1).isEqualTo(new Version("9.36.0"));
		verify(executor).execute(any(Command.class));

		reset(executor);

		Version v2 = cache.load(exifTool, executor);
		assertThat(v2).isSameAs(v1);
		verify(executor, never()).execute(any(Command.class));
	}

	@Test
	void it_should_clear_cache() throws Exception {
		VersionCache cache = create();
		cache.load(exifTool, executor);
		assertThat(size(cache)).isNotZero().isPositive();

		cache.clear();
		assertThat(size(cache)).isZero();
	}

	/// Create the cache implementation.
	///
	/// @return Cache implementation.
	abstract T create();

	/// Get cache size.
	///
	/// @param cache Cache implementation.
	/// @return The cache size.
	abstract long size(VersionCache cache) throws Exception;
}
