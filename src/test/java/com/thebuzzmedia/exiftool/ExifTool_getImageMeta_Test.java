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
import com.thebuzzmedia.exiftool.core.UnspecifiedTag;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.thebuzzmedia.exiftool.tests.MockitoTestUtils.anyListOf;
import static com.thebuzzmedia.exiftool.tests.TagTestUtils.parseTags;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExifTool_getImageMeta_Test {

	private String path;
	private CommandExecutor executor;
	private ExecutionStrategy strategy;

	private ExifTool exifTool;

	@BeforeEach
	void setUp() throws Exception {
		executor = mock(CommandExecutor.class);
		strategy = mock(ExecutionStrategy.class);
		path = "exiftool";

		CommandResult cmd = new CommandResultBuilder().output("9.36").build();
		when(executor.execute(any(Command.class))).thenReturn(cmd);
		when(strategy.isSupported(any(Version.class))).thenReturn(true);

		exifTool = new ExifTool(path, executor, strategy);

		reset(executor);
	}

	@Test
	void it_should_fail_if_image_is_null() {
		assertThatThrownBy(() -> exifTool.getImageMeta(null, StandardFormat.HUMAN_READABLE, asList((Tag[]) StandardTag.values())))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Image cannot be null and must be a valid stream of image data.");
	}

	@Test
	void it_should_fail_if_format_is_null() {
		Format format = null;
		List<Tag> tags = asList(StandardTag.values());
		assertThatThrownBy(() -> exifTool.getImageMeta(mock(File.class), format, tags))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Format cannot be null.");
	}

	@Test
	void it_should_fail_if_options_is_null() {
		ExifToolOptions options = null;
		List<Tag> tags = asList(StandardTag.values());
		assertThatThrownBy(() -> exifTool.getImageMeta(mock(File.class), options, tags))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Options cannot be null.");
	}

	@Test
	void it_should_fail_if_tags_is_null() {
		Format format = StandardFormat.HUMAN_READABLE;
		Collection<? extends Tag> tags = null;
		assertThatThrownBy(() -> exifTool.getImageMeta(mock(File.class), format, tags))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Tags cannot be null and must contain 1 or more Tag to query the image for.");
	}

	@Test
	void it_should_fail_if_tags_is_empty() {
		assertThatThrownBy(() -> exifTool.getImageMeta(mock(File.class), StandardFormat.HUMAN_READABLE, Collections.emptyList()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Tags cannot be null and must contain 1 or more Tag to query the image for.");
	}

	@Test
	void it_should_fail_with_unknown_file() {
		File image = new FileBuilder("foo.png").exists(false).build();
		assertThatThrownBy(() -> exifTool.getImageMeta(image, StandardFormat.HUMAN_READABLE, asList((Tag[]) StandardTag.values())))
				.isInstanceOf(UnreadableFileException.class)
				.hasMessage(
						"Unable to read the given image [/tmp/foo.png], " +
								"ensure that the image exists at the given withPath and that the " +
								"executing Java process has permissions to read it."
				);
	}

	@Test
	void it_should_fail_with_non_readable_file() {
		File image = new FileBuilder("foo.png").canRead(false).build();
		assertThatThrownBy(() -> exifTool.getImageMeta(image, StandardFormat.HUMAN_READABLE, asList((Tag[]) StandardTag.values())))
				.isInstanceOf(UnreadableFileException.class)
				.hasMessage(
						"Unable to read the given image [/tmp/foo.png], " +
								"ensure that the image exists at the given withPath and that the " +
								"executing Java process has permissions to read it."
				);
	}

	@Test
	@SuppressWarnings("unchecked")
	void it_should_get_image_metadata() throws Exception {
		// Given
		Format format = StandardFormat.HUMAN_READABLE;
		File image = new FileBuilder("foo.png").build();

		Map<Tag, String> tags = new LinkedHashMap<>();
		tags.put(StandardTag.ARTIST, "bar");
		tags.put(StandardTag.COMMENT, "foo");

		doAnswer(new ReadTagsAnswer(tags, "{ready}")).when(strategy).execute(
				same(executor), same(path), anyListOf(String.class), any(OutputHandler.class)
		);

		// When
		Map<Tag, String> results = exifTool.getImageMeta(image, format, tags.keySet());

		// Then
		ArgumentCaptor<List<String>> argsCaptor = ArgumentCaptor.forClass(List.class);
		verify(strategy).execute(same(executor), same(path), argsCaptor.capture(), any(OutputHandler.class));
		assertThat(results).hasSize(tags.size()).isEqualTo(tags);

		List<String> args = argsCaptor.getValue();
		assertThat(args).isNotEmpty().containsExactly(
				"-sep",
				"|>☃",
				"-S",
				"-Artist",
				"-XPComment",
				"/tmp/foo.png",
				"-execute"
		);
	}

	@Test
	@SuppressWarnings("unchecked")
	void it_should_get_image_metadata_in_numeric_format() throws Exception {
		// Given
		Format format = StandardFormat.NUMERIC;
		File image = new FileBuilder("foo.png").build();

		Map<Tag, String> tags = new LinkedHashMap<>();
		tags.put(StandardTag.ARTIST, "foo");
		tags.put(StandardTag.COMMENT, "bar");

		doAnswer(new ReadTagsAnswer(tags, "{ready}")).when(strategy).execute(
				same(executor), same(path), anyListOf(String.class), any(OutputHandler.class)
		);

		// When
		Map<Tag, String> results = exifTool.getImageMeta(image, format, tags.keySet());

		// Then
		ArgumentCaptor<List<String>> argsCaptor = ArgumentCaptor.forClass(List.class);
		verify(strategy).execute(same(executor), same(path), argsCaptor.capture(), any(OutputHandler.class));
		assertThat(results).hasSize(tags.size()).isEqualTo(tags);

		List<String> args = argsCaptor.getValue();
		assertThat(args).isNotEmpty().containsExactly(
				"-sep",
				"|>☃",
				"-n",
				"-S",
				"-Artist",
				"-XPComment",
				"/tmp/foo.png",
				"-execute"
		);
	}

	@Test
	@SuppressWarnings("unchecked")
	void it_should_get_image_metadata_in_numeric_format_by_default() throws Exception {
		// Given
		File image = new FileBuilder("foo.png").build();
		Map<Tag, String> tags = new LinkedHashMap<>();
		tags.put(StandardTag.ARTIST, "foo");
		tags.put(StandardTag.COMMENT, "bar");

		doAnswer(new ReadTagsAnswer(tags, "{ready}")).when(strategy).execute(
				same(executor), same(path), anyListOf(String.class), any(OutputHandler.class)
		);

		// When
		Map<Tag, String> results = exifTool.getImageMeta(image, tags.keySet());

		// Then
		ArgumentCaptor<List<String>> argsCaptor = ArgumentCaptor.forClass(List.class);
		verify(strategy).execute(same(executor), same(path), argsCaptor.capture(), any(OutputHandler.class));
		assertThat(results).hasSize(tags.size()).isEqualTo(tags);

		List<String> args = argsCaptor.getValue();
		assertThat(args).isNotEmpty().containsExactly(
				"-sep",
				"|>☃",
				"-n",
				"-S",
				"-Artist",
				"-XPComment",
				"/tmp/foo.png",
				"-execute"
		);
	}

	@Test
	@SuppressWarnings("unchecked")
	void it_should_get_all_image_metadata_if_no_tags_specified() throws Exception {
		// Given
		Format format = StandardFormat.HUMAN_READABLE;
		File image = new FileBuilder("foo.png").build();

		Map<Tag, String> tags = new LinkedHashMap<>();
		tags.put(new UnspecifiedTag("Artist"), "bar");
		tags.put(new UnspecifiedTag("XPComment"), "foo");
		tags.put(new UnspecifiedTag("CustomTag"), "baz");

		doAnswer(new ReadTagsAnswer(tags, "{ready}")).when(strategy).execute(
				same(executor), same(path), anyListOf(String.class), any(OutputHandler.class)
		);

		// When
		Map<Tag, String> results = exifTool.getImageMeta(image, format);

		// Then
		ArgumentCaptor<List<String>> argsCaptor = ArgumentCaptor.forClass(List.class);
		verify(strategy).execute(same(executor), same(path), argsCaptor.capture(), any(OutputHandler.class));

		assertThat(results).hasSize(tags.size());
		assertThat(parseTags(results)).containsAllEntriesOf(parseTags(tags));

		List<String> args = argsCaptor.getValue();
		assertThat(args).isNotEmpty().containsExactly(
				"-sep",
				"|>☃",
				"-S",
				"-All",
				"/tmp/foo.png",
				"-execute"
		);
	}

	@Test
	@SuppressWarnings("unchecked")
	void it_should_get_all_image_metadata_in_numeric_format_by_default() throws Exception {
		// Given
		File image = new FileBuilder("foo.png").build();

		Map<Tag, String> tags = new LinkedHashMap<>();
		tags.put(new UnspecifiedTag("Artist"), "foo");
		tags.put(new UnspecifiedTag("XPComment"), "bar");
		tags.put(new UnspecifiedTag("CustomTag"), "baz");

		doAnswer(new ReadTagsAnswer(tags, "{ready}")).when(strategy).execute(
				same(executor), same(path), anyListOf(String.class), any(OutputHandler.class)
		);

		// When
		Map<Tag, String> results = exifTool.getImageMeta(image);

		// Then
		ArgumentCaptor<List<String>> argsCaptor = ArgumentCaptor.forClass(List.class);
		verify(strategy).execute(same(executor), same(path), argsCaptor.capture(), any(OutputHandler.class));

		assertThat(results).hasSize(tags.size());
		assertThat(parseTags(results)).containsAllEntriesOf(parseTags(tags));

		List<String> args = argsCaptor.getValue();
		assertThat(args).isNotEmpty().containsExactly(
				"-sep",
				"|>☃",
				"-n",
				"-S",
				"-All",
				"/tmp/foo.png",
				"-execute"
		);
	}

	@Test
	@SuppressWarnings("unchecked")
	void it_should_get_image_metadata_using_custom_options() throws Exception {
		// Given
		ExifToolOptions options = StandardOptions.builder().withFormat(StandardFormat.NUMERIC).withIgnoreMinorErrors(true).build();
		File image = new FileBuilder("foo.png").build();

		Map<Tag, String> tags = new LinkedHashMap<>();
		tags.put(StandardTag.ARTIST, "foo");
		tags.put(StandardTag.COMMENT, "bar");

		doAnswer(new ReadTagsAnswer(tags, "{ready}")).when(strategy).execute(
				same(executor), same(path), anyListOf(String.class), any(OutputHandler.class)
		);

		// When
		Map<Tag, String> results = exifTool.getImageMeta(image, options, tags.keySet());

		// Then
		ArgumentCaptor<List<String>> argsCaptor = ArgumentCaptor.forClass(List.class);
		verify(strategy).execute(same(executor), same(path), argsCaptor.capture(), any(OutputHandler.class));
		assertThat(results).hasSize(tags.size()).isEqualTo(tags);

		List<String> args = argsCaptor.getValue();
		assertThat(args).isNotEmpty().containsExactly(
				"-sep",
				"|>☃",
				"-n",
				"-m",
				"-S",
				"-Artist",
				"-XPComment",
				"/tmp/foo.png",
				"-execute"
		);
	}

	@Test
	@SuppressWarnings("unchecked")
	void it_should_get_all_image_metadata_using_custom_options() throws Exception {
		// Given
		ExifToolOptions options = StandardOptions.builder().withFormat(StandardFormat.NUMERIC).withIgnoreMinorErrors(true).build();
		File image = new FileBuilder("foo.png").build();

		Map<Tag, String> tags = new LinkedHashMap<>();
		tags.put(new UnspecifiedTag("Artist"), "foo");
		tags.put(new UnspecifiedTag("XPComment"), "bar");
		tags.put(new UnspecifiedTag("CustomTag"), "baz");

		doAnswer(new ReadTagsAnswer(tags, "{ready}")).when(strategy).execute(
				same(executor), same(path), anyListOf(String.class), any(OutputHandler.class)
		);

		// When
		Map<Tag, String> results = exifTool.getImageMeta(image, options);

		// Then
		ArgumentCaptor<List<String>> argsCaptor = ArgumentCaptor.forClass(List.class);
		verify(strategy).execute(same(executor), same(path), argsCaptor.capture(), any(OutputHandler.class));

		assertThat(results).hasSize(tags.size());
		assertThat(parseTags(results)).containsAllEntriesOf(parseTags(tags));

		List<String> args = argsCaptor.getValue();
		assertThat(args).isNotEmpty().containsExactly(
				"-sep",
				"|>☃",
				"-n",
				"-m",
				"-S",
				"-All",
				"/tmp/foo.png",
				"-execute"
		);
	}

	private static final class ReadTagsAnswer implements Answer<Void> {
		private final Map<Tag, String> tags;

		private final String end;

		private ReadTagsAnswer(Map<Tag, String> tags, String end) {
			this.tags = tags;
			this.end = end;
		}

		@Override
		public Void answer(InvocationOnMock invocation) {
			OutputHandler handler = (OutputHandler) invocation.getArguments()[3];

			// Read tags
			for (Map.Entry<Tag, String> entry : tags.entrySet()) {
				handler.readLine(entry.getKey().getName() + ": " + entry.getValue());
			}

			// Read last line
			handler.readLine(end);

			return null;
		}
	}
}
