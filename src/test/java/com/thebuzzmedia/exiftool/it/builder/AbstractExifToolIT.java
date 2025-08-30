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

package com.thebuzzmedia.exiftool.it.builder;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.Version;
import com.thebuzzmedia.exiftool.exceptions.ExifToolNotFoundException;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.UUID;

import static com.thebuzzmedia.exiftool.tests.TestConstants.EXIF_TOOL;
import static com.thebuzzmedia.exiftool.tests.TestConstants.EXIF_TOOL_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

abstract class AbstractExifToolIT {

	@Test
	void it_should_get_version() throws Exception {
		try (ExifTool exifTool = create().withPath(EXIF_TOOL.getAbsolutePath()).build()) {
			Version version = exifTool.getVersion();
			assertThat(version.getMajor()).isEqualTo(10);
			assertThat(version.getMinor()).isEqualTo(16);
			assertThat(version.getPatch()).isEqualTo(0);
		}
	}

	@Test
	void it_should_fail_with_missing_exiftool() {
		String separator = FileSystems.getDefault().getSeparator();
		String path = separator + UUID.randomUUID() + separator + "exiftool";
		ExifToolBuilder builder = create().withPath(path);

		assertThatThrownBy(builder::build)
				.isInstanceOf(ExifToolNotFoundException.class)
				.hasMessageContaining("Cannot run program \"" + path + "\"")
				.hasMessageContaining("error=2");
	}

	@Test
	void it_should_read_config() throws Exception {
		ExifToolBuilder builder = create()
			.withPath(EXIF_TOOL.getAbsolutePath())
			.withConfig(EXIF_TOOL_CONFIG.getAbsolutePath());

		try (ExifTool exifTool = builder.build()) {
			String result = exifTool.getRawExifToolOutput(Arrays.asList("-ver", "-execute"));
			assertThat(result).startsWith(EXIF_TOOL_CONFIG.getAbsolutePath() + " did not return a true value at ");
		}
	}

	abstract ExifToolBuilder create();
}
