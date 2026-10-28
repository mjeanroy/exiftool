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
package com.thebuzzmedia.exiftool.it.execute;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.ExifToolResult;
import com.thebuzzmedia.exiftool.Tag;
import com.thebuzzmedia.exiftool.Version;
import com.thebuzzmedia.exiftool.core.StandardFormat;
import com.thebuzzmedia.exiftool.core.StandardTag;
import com.thebuzzmedia.exiftool.core.UnspecifiedTag;
import com.thebuzzmedia.exiftool.exceptions.UnreadableFileException;
import com.thebuzzmedia.exiftool.tests.junit.ProcessLeakDetectorExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.thebuzzmedia.exiftool.commons.lang.Objects.firstNonNull;
import static com.thebuzzmedia.exiftool.tests.FileTestUtils.copy;
import static com.thebuzzmedia.exiftool.tests.TestConstants.EXIF_TOOL;
import static com.thebuzzmedia.exiftool.tests.TestConstants.IS_WINDOWS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/// Check that [ExifTool#execute(File, List)] returns exactly the output of `exiftool` run directly.
abstract class AbstractExifToolExecuteIT {

	/// Path of exiftool: the bundled version by default, may be overridden (using `EXIFTOOL_IT_PATH` environment variable) to run these tests with another version.
	static final String PATH = firstNonNull(System.getenv("EXIFTOOL_IT_PATH"), EXIF_TOOL.getAbsolutePath());

	private static final File IMAGES = new File("src/test/resources/images");

	/// Arguments giving a deterministic output: with `-sort`, the order of tags does not depend on files previously processed by the process.
	private static final List<String> JSON = asList("-json", "-n", "-sort");

	@RegisterExtension
	ProcessLeakDetectorExtension processes = new ProcessLeakDetectorExtension(PATH);

	private ExifTool exifTool;

	@BeforeEach
	void setUp() {
		exifTool = create(new ExifToolBuilder().withPath(PATH));
	}

	@AfterEach
	void tearDown() throws Exception {
		exifTool.close();
	}

	/// Create the instance to test.
	///
	/// @param builder Builder, with exiftool path.
	/// @return The instance.
	abstract ExifTool create(ExifToolBuilder builder);

	/// The exit code returned by the strategy, given the exit code of exiftool.
	///
	/// @param exitCode Exit code of exiftool.
	/// @return The expected exit code.
	abstract Integer expectedExitCode(int exitCode);

	@Test
	void it_should_return_exact_json_output() throws Exception {
		for (File image : images()) {
			Output expected = run("-json", "-n", image.getAbsolutePath());

			// Use a new process: exiftool may print composite tags in a different order once other files have been processed.
			try (ExifTool exifTool = create(new ExifToolBuilder().withPath(PATH))) {
				ExifToolResult result = exifTool.execute(image, asList("-json", "-n"));

				assertThat(result.getOutput()).as("Output of %s", image).isEqualTo(expected.output);
				assertThat(result.getErrors()).as("Errors of %s", image).isEqualTo(expected.errors);
				assertThat(result.getExitCode()).isEqualTo(expectedExitCode(0));
				assertThat(result.getOutput()).startsWith("[{").endsWith("}]\n");
			}
		}
	}

	@Test
	void it_should_return_exact_sorted_json_output_with_same_process() throws Exception {
		for (File image : images()) {
			ExifToolResult result = exifTool.execute(image, JSON);
			Output expected = run("-json", "-n", "-sort", image.getAbsolutePath());

			assertThat(result.getOutput()).as("Output of %s", image).isEqualTo(expected.output);
			assertThat(result.getErrors()).as("Errors of %s", image).isEqualTo(expected.errors);
		}
	}

	@Test
	void it_should_return_exact_output_with_other_formats() throws Exception {
		File image = image("htc-glacier-cat-ladder.jpg");
		List<List<String>> commands = asList(
				asList("-a", "-G1", "-s"),
				asList("-X"),
				asList("-args", "-n"),
				asList("-json", "-b", "-ThumbnailImage"),
				asList("-p", "$FileName $ImageSize"),
				asList("-csv", "-Make", "-Model")
		);

		for (List<String> args : commands) {
			ExifToolResult result = exifTool.execute(image, args);

			List<String> direct = new ArrayList<>(args);
			direct.add(image.getAbsolutePath());
			Output expected = run(direct.toArray(new String[0]));

			assertThat(result.getOutput()).as("Output of %s", args).isEqualTo(expected.output).isNotEmpty();
			assertThat(result.getErrors()).as("Errors of %s", args).isEqualTo(expected.errors);
		}
	}

	@Test
	void it_should_return_exact_output_for_file_with_non_ascii_name(@TempDir File tmp) throws Exception {
		File image = new File(tmp, "café ☃ 日本.jpg");
		Files.copy(image("nexus-s-electric-cars.jpg").toPath(), image.toPath());

		ExifToolResult result = exifTool.execute(image, asList("-json", "-n"));
		Output expected = run("-json", "-n", image.getAbsolutePath());

		assertThat(result.getOutput()).isEqualTo(expected.output).contains("café ☃ 日本.jpg");
		assertThat(result.getErrors()).isEqualTo(expected.errors);
	}

	@Test
	void it_should_return_list_tags_as_json_array(@TempDir File tmp) throws Exception {
		File image = copy(image("nexus-s-electric-cars.jpg"), tmp);
		Output write = run("-overwrite_original", "-XMP:Subject=a", "-XMP:Subject=b", "-XMP:Subject=café", image.getAbsolutePath());
		assertThat(write.exitCode).as(write.errors).isZero();

		ExifToolResult result = exifTool.execute(image, asList("-json", "-XMP:Subject"));

		assertThat(result.getOutput()).containsPattern("\"Subject\": \\[\\s*\"a\",\\s*\"b\",\\s*\"café\"\\s*\\]");
		assertThat(result.getOutput()).isEqualTo(run("-json", "-XMP:Subject", image.getAbsolutePath()).output);
	}

	@Test
	void it_should_use_separator_for_list_tags_with_each_request(@TempDir File tmp) throws Exception {
		File image = copy(image("nexus-s-electric-cars.jpg"), tmp);
		Output write = run("-overwrite_original", "-XMP:Subject=a", "-XMP:Subject=b", image.getAbsolutePath());
		assertThat(write.exitCode).as(write.errors).isZero();

		Tag subject = new UnspecifiedTag("Subject");

		// The separator must be used for every request, not only the first one sent to a daemon process.
		for (int i = 0; i < 3; i++) {
			Map<Tag, String> meta = exifTool.getImageMeta(image, StandardFormat.NUMERIC, singletonList(subject));
			assertThat(meta).containsEntry(subject, "a|>☃b");
		}
	}

	@Test
	void it_should_return_errors_of_missing_file_and_remain_usable() throws Exception {
		File image = image("nexus-s-electric-cars.jpg");
		String missing = new File(IMAGES, "missing.jpg").getAbsolutePath();

		ExifToolResult result = exifTool.execute(image, asList("-json", missing));
		Output expected = run("-json", missing, image.getAbsolutePath());

		assertThat(result.getErrors()).isEqualTo(expected.errors).contains("File not found").contains(missing);
		assertThat(result.getOutput()).isEqualTo(expected.output);
		assertThat(result.getExitCode()).isEqualTo(expectedExitCode(expected.exitCode));
		assertThat(expected.exitCode).isEqualTo(1);

		// Errors must not leak into the next result.
		ExifToolResult next = exifTool.execute(image, JSON);
		assertThat(next.getOutput()).isEqualTo(run("-json", "-n", "-sort", image.getAbsolutePath()).output);
		assertThat(next.getErrors()).isEmpty();
	}

	@Test
	void it_should_return_errors_of_invalid_option_and_remain_usable() throws Exception {
		File image = image("nexus-s-electric-cars.jpg");

		for (List<String> args : asList(asList("-json", "-lang", "zz"), asList("-json", "-if", "$Foo =~ /(/"), asList("-json", "-nope"))) {
			ExifToolResult result = exifTool.execute(image, args);

			List<String> direct = new ArrayList<>(args);
			direct.add(image.getAbsolutePath());
			Output expected = run(direct.toArray(new String[0]));

			assertThat(result.getOutput()).as("Output of %s", args).isEqualTo(expected.output);
			assertThat(result.getErrors()).as("Errors of %s", args).isEqualTo(expected.errors);
		}

		ExifToolResult next = exifTool.execute(image, JSON);
		assertThat(next.getOutput()).isEqualTo(run("-json", "-n", "-sort", image.getAbsolutePath()).output);
		assertThat(next.getErrors()).isEmpty();
	}

	@Test
	void it_should_support_quiet_option_if_end_of_output_is_printed() throws Exception {
		File image = image("nexus-s-electric-cars.jpg");
		boolean daemon = expectedExitCode(0) == null;

		if (daemon && exifTool.getVersion().compareTo(new Version("12.10")) < 0) {
			assertThatThrownBy(() -> exifTool.execute(image, asList("-q", "-json")))
					.isInstanceOf(IllegalArgumentException.class);
		}
		else {
			ExifToolResult result = exifTool.execute(image, asList("-q", "-q", "-json", "-n"));
			assertThat(result.getOutput()).isEqualTo(run("-q", "-q", "-json", "-n", image.getAbsolutePath()).output);
		}

		// Still usable.
		assertThat(exifTool.execute(image, JSON).getOutput()).isEqualTo(run("-json", "-n", "-sort", image.getAbsolutePath()).output);
	}

	@Test
	void it_should_fail_with_unreadable_file(@TempDir File tmp) throws Exception {
		assumeFalse(IS_WINDOWS);

		File image = copy(image("nexus-s-electric-cars.jpg"), tmp);
		assumeTrue(image.setReadable(false, false));
		assumeFalse(image.canRead(), "File is still readable (running as root?)");

		assertThatThrownBy(() -> exifTool.execute(image, asList("-json", "-n")))
				.isInstanceOf(UnreadableFileException.class);
	}

	@Test
	void it_should_return_output_of_each_sequential_request() throws Exception {
		Map<File, String> expected = expectedOutputs();
		List<File> images = new ArrayList<>(expected.keySet());

		for (int i = 0; i < 40; i++) {
			File image = images.get(i % images.size());
			ExifToolResult result = exifTool.execute(image, JSON);
			assertThat(result.getOutput()).as("Output of %s (request %s)", image, i).isEqualTo(expected.get(image));
			assertThat(result.getErrors()).isEmpty();
		}
	}

	@Test
	void it_should_return_output_of_each_concurrent_request() throws Exception {
		Map<File, String> expected = expectedOutputs();
		List<File> images = new ArrayList<>(expected.keySet());

		final int threads = 4;
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		try {
			List<Future<Void>> futures = new ArrayList<>();
			for (int t = 0; t < threads; t++) {
				final int offset = t;
				futures.add(pool.submit((Callable<Void>) () -> {
					for (int i = 0; i < images.size(); i++) {
						File image = images.get((i + offset) % images.size());
						ExifToolResult result = exifTool.execute(image, JSON);
						assertThat(result.getOutput()).as("Output of %s", image).isEqualTo(expected.get(image));
						assertThat(result.getErrors()).isEmpty();
					}

					return null;
				}));
			}

			for (Future<Void> future : futures) {
				future.get(5, TimeUnit.MINUTES);
			}
		}
		finally {
			pool.shutdownNow();
		}
	}

	@Test
	void it_should_alternate_with_get_image_meta() throws Exception {
		File image = image("htc-glacier-cat-ladder.jpg");
		String json = run("-json", "-n", "-sort", image.getAbsolutePath()).output;
		List<Tag> tags = asList(StandardTag.MAKE, StandardTag.MODEL, StandardTag.IMAGE_WIDTH);

		Map<Tag, String> meta = null;
		for (int i = 0; i < 3; i++) {
			Map<Tag, String> current = exifTool.getImageMeta(image, StandardFormat.NUMERIC, tags);
			assertThat(current).containsEntry(StandardTag.MAKE, "HTC").hasSize(3);
			if (meta != null) {
				assertThat(current).isEqualTo(meta);
			}
			meta = current;

			ExifToolResult result = exifTool.execute(image, JSON);
			assertThat(result.getOutput()).isEqualTo(json);
		}
	}

	private Map<File, String> expectedOutputs() throws Exception {
		Map<File, String> expected = new HashMap<>();
		for (File image : images()) {
			expected.put(image, run("-json", "-n", "-sort", image.getAbsolutePath()).output);
		}

		return expected;
	}

	private static List<File> images() {
		File[] files = IMAGES.listFiles();
		assertThat(files).isNotEmpty();
		Arrays.sort(files);
		return asList(files);
	}

	private static File image(String name) {
		File file = new File(IMAGES, name);
		assertThat(file).exists();
		return file;
	}

	/// Run exiftool directly, in a one-shot process.
	///
	/// @param args Arguments.
	/// @return Output, errors and exit code.
	static Output run(String... args) throws Exception {
		List<String> command = new ArrayList<>();
		command.add(PATH);
		command.addAll(asList(args));

		File errors = File.createTempFile("exiftool", ".err");
		try {
			Process process = new ProcessBuilder(command).redirectError(errors).start();
			process.getOutputStream().close();

			String output = readFully(process.getInputStream());
			int exitCode = process.waitFor();

			return new Output(output, new String(Files.readAllBytes(errors.toPath()), StandardCharsets.UTF_8), exitCode);
		}
		finally {
			Files.delete(errors.toPath());
		}
	}

	private static String readFully(InputStream is) throws IOException {
		try (InputStream stream = is) {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			int n;
			while ((n = stream.read(buffer)) > 0) {
				bytes.write(buffer, 0, n);
			}

			return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
		}
	}

	static final class Output {
		final String output;
		final String errors;
		final int exitCode;

		private Output(String output, String errors, int exitCode) {
			this.output = output;
			this.errors = errors;
			this.exitCode = exitCode;
		}
	}
}
