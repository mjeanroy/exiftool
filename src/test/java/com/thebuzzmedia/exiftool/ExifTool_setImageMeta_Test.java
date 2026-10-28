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

import com.thebuzzmedia.exiftool.core.StandardFormat;
import com.thebuzzmedia.exiftool.core.StandardOptions;
import com.thebuzzmedia.exiftool.core.StandardTag;
import com.thebuzzmedia.exiftool.exceptions.UnwritableFileException;
import com.thebuzzmedia.exiftool.process.Command;
import com.thebuzzmedia.exiftool.process.CommandExecutor;
import com.thebuzzmedia.exiftool.process.CommandResult;
import com.thebuzzmedia.exiftool.process.OutputHandler;
import com.thebuzzmedia.exiftool.tests.builders.CommandResultBuilder;
import com.thebuzzmedia.exiftool.tests.builders.FileBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.thebuzzmedia.exiftool.tests.MockitoTestUtils.anyListOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExifTool_setImageMeta_Test {

	private String path;
	private CommandExecutor executor;
	private ExecutionStrategy strategy;
	private Map<StandardTag, String> tags;

	private ExifTool exifTool;

	@BeforeEach
	void setUp() throws Exception {
		executor = mock(CommandExecutor.class);
		strategy = mock(ExecutionStrategy.class);
		path = "exiftool";
		tags = new LinkedHashMap<StandardTag, String>() {{
			put(StandardTag.APERTURE, "foo");
			put(StandardTag.ARTIST, "bar");
		}};

		CommandResult result = new CommandResultBuilder().output("9.36").build();
		when(executor.execute(any(Command.class))).thenReturn(result);
		when(strategy.isSupported(any(Version.class))).thenReturn(true);

		exifTool = new ExifTool(path, executor, strategy);

		reset(executor);
	}

	@Test
	void it_should_fail_if_image_is_null() {
		assertThatThrownBy(() -> exifTool.setImageMeta(null, StandardFormat.HUMAN_READABLE, tags))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Image cannot be null and must be a valid stream of image data.");
	}

	@Test
	void it_should_fail_if_format_is_null() {
		Format format = null;
		Map<StandardTag, String> tags = ExifTool_setImageMeta_Test.this.tags;
		assertThatThrownBy(() -> exifTool.setImageMeta(mock(File.class), format, tags))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Format cannot be null.");
	}

	@Test
	void it_should_fail_if_options_is_null() {
		ExifToolOptions options = null;
		Map<StandardTag, String> tags = ExifTool_setImageMeta_Test.this.tags;
		assertThatThrownBy(() -> exifTool.setImageMeta(mock(File.class), options, tags))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Options cannot be null.");
	}

	@Test
	void it_should_fail_if_tags_is_null() {
		StandardFormat format = StandardFormat.HUMAN_READABLE;
		Map<? extends Tag, String> tags = null;
		assertThatThrownBy(() -> exifTool.setImageMeta(mock(File.class), format, tags))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Tags cannot be null and must contain 1 or more Tag to query the image for.");
	}

	@Test
	void it_should_fail_if_tags_is_empty() {
		assertThatThrownBy(() -> exifTool.setImageMeta(mock(File.class), StandardFormat.HUMAN_READABLE, Collections.emptyMap()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Tags cannot be null and must contain 1 or more Tag to query the image for.");
	}

	@Test
	void it_should_fail_with_unknown_file() {
		File image = new FileBuilder("foo.png").exists(false).build();
		assertThatThrownBy(() -> exifTool.setImageMeta(image, StandardFormat.HUMAN_READABLE, tags))
				.isInstanceOf(UnwritableFileException.class)
				.hasMessage(
						"Unable to read the given image [/tmp/foo.png], " +
								"ensure that the image exists at the given withPath and that " +
								"the executing Java process has permissions to read it."
				);
	}

	@Test
	void it_should_fail_with_non_writable_file() {
		File image = new FileBuilder("foo.png").canWrite(false).build();
		assertThatThrownBy(() -> exifTool.setImageMeta(image, StandardFormat.HUMAN_READABLE, tags))
				.isInstanceOf(UnwritableFileException.class)
				.hasMessage(
						"Unable to read the given image [/tmp/foo.png], " +
								"ensure that the image exists at the given withPath and that " +
								"the executing Java process has permissions to read it."
				);
	}

	@Test
	@SuppressWarnings("unchecked")
	void it_should_set_image_meta_data() throws Exception {
		File image = new FileBuilder("foo.png").build();
		Format format = StandardFormat.HUMAN_READABLE;

		doAnswer(new WriteTagsAnswer()).when(strategy).execute(
				same(executor), same(path), anyListOf(String.class), any(OutputHandler.class)
		);

		exifTool.setImageMeta(image, format, tags);

		ArgumentCaptor<List<String>> argsCaptor = ArgumentCaptor.forClass(List.class);
		verify(strategy).execute(same(executor), same(path), argsCaptor.capture(), any(OutputHandler.class));

		List<String> args = argsCaptor.getValue();
		assertThat(args).hasSize(7).containsExactly(
				"-sep",
				"|>☃",
				"-S",
				"-ApertureValue=foo",
				"-Artist=bar",
				"/tmp/foo.png",
				"-execute"
		);
	}

	@Test
	@SuppressWarnings("unchecked")
	void it_should_set_image_meta_data_in_numeric_format() throws Exception {
		File image = new FileBuilder("foo.png").build();
		Format format = StandardFormat.NUMERIC;

		doAnswer(new WriteTagsAnswer()).when(strategy).execute(
				same(executor), same(path), anyListOf(String.class), any(OutputHandler.class)
		);

		exifTool.setImageMeta(image, format, tags);

		ArgumentCaptor<List<String>> argsCaptor = ArgumentCaptor.forClass(List.class);
		verify(strategy).execute(same(executor), same(path), argsCaptor.capture(), any(OutputHandler.class));

		List<String> args = argsCaptor.getValue();
		assertThat(args).hasSize(8).containsExactly(
				"-sep",
				"|>☃",
				"-n",
				"-S",
				"-ApertureValue=foo",
				"-Artist=bar",
				"/tmp/foo.png",
				"-execute"
		);
	}

	@Test
	@SuppressWarnings("unchecked")
	void it_should_set_image_meta_data_in_numeric_format_by_default() throws Exception {
		File image = new FileBuilder("foo.png").build();

		doAnswer(new WriteTagsAnswer()).when(strategy).execute(
				same(executor), same(path), anyListOf(String.class), any(OutputHandler.class)
		);

		exifTool.setImageMeta(image, tags);

		ArgumentCaptor<List<String>> argsCaptor = ArgumentCaptor.forClass(List.class);
		verify(strategy).execute(same(executor), same(path), argsCaptor.capture(), any(OutputHandler.class));

		List<String> args = argsCaptor.getValue();
		assertThat(args).hasSize(8).containsExactly(
				"-sep",
				"|>☃",
				"-n",
				"-S",
				"-ApertureValue=foo",
				"-Artist=bar",
				"/tmp/foo.png",
				"-execute"
		);
	}

	@Test
	@SuppressWarnings("unchecked")
	void it_should_set_image_meta_data_with_given_options() throws Exception {
		File image = new FileBuilder("foo.png").build();
		ExifToolOptions options = StandardOptions.builder().withFormat(StandardFormat.NUMERIC).withIgnoreMinorErrors(true).build();

		doAnswer(new WriteTagsAnswer()).when(strategy).execute(
				same(executor), same(path), anyListOf(String.class), any(OutputHandler.class)
		);

		exifTool.setImageMeta(image, options, tags);

		ArgumentCaptor<List<String>> argsCaptor = ArgumentCaptor.forClass(List.class);
		verify(strategy).execute(same(executor), same(path), argsCaptor.capture(), any(OutputHandler.class));

		List<String> args = argsCaptor.getValue();
		assertThat(args).hasSize(9).containsExactly(
				"-sep",
				"|>☃",
				"-n",
				"-m",
				"-S",
				"-ApertureValue=foo",
				"-Artist=bar",
				"/tmp/foo.png",
				"-execute"
		);
	}

	private static final class WriteTagsAnswer implements Answer<String> {
		@Override
		public String answer(InvocationOnMock invocation) {
			return "{ready}";
		}
	}
}
