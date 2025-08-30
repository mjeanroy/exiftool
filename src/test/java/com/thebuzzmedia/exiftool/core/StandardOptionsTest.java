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

package com.thebuzzmedia.exiftool.core;

import com.thebuzzmedia.exiftool.core.StandardOptions.Builder;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class StandardOptionsTest {

	@Test
	void it_should_create_default_options() {
		StandardOptions opts = StandardOptions.builder().build();

		assertThat(opts).isNotNull();
		assertThat(opts.getFormat()).isEqualTo(StandardFormat.HUMAN_READABLE);
		assertThat(opts.getCharset()).isNull();
		assertThat(opts.getPassword()).isNull();
		assertThat(opts.getDateFormat()).isNull();
		assertThat(opts.getCoordFormat()).isNull();
		assertThat(opts.isEscapeXml()).isFalse();
		assertThat(opts.isEscapeHtml()).isFalse();
		assertThat(opts.isIgnoreMinorErrors()).isFalse();
		assertThat(opts.getModules()).isNotNull().isEmpty();
		assertThat(opts.getLang()).isNull();
		assertThat(opts.isDuplicates()).isFalse();
		assertThat(opts.isExtractEmbedded()).isFalse();
		assertThat(opts.isExtractUnknown()).isFalse();
		assertThat(opts.isWithoutCompositeTags()).isFalse();
		assertThat(opts.isOverwriteOriginal()).isFalse();
		assertThat(opts.isOverwriteOriginalInPlace()).isFalse();
		assertThat(opts.isUseArgsFormat()).isFalse();
		assertThat(opts.serialize()).isNotNull().isEmpty();
	}

	@Test
	void it_should_set_numeric_format() {
		StandardFormat format = StandardFormat.NUMERIC;
		StandardOptions opts = StandardOptions.builder()
				.withFormat(format)
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.getFormat()).isEqualTo(format);
		assertThat(opts.serialize()).hasSize(1).containsExactly("-n");
		assertThat(opts.toBuilder().getFormat()).isEqualTo(StandardFormat.NUMERIC);
	}

	@Test
	void it_should_set_human_readable_format() {
		StandardFormat format = StandardFormat.HUMAN_READABLE;
		StandardOptions opts = StandardOptions.builder()
				.withFormat(format)
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.getFormat()).isEqualTo(format);
		assertThat(opts.serialize()).isNotNull().isEmpty();
		assertThat(opts.toBuilder().getFormat()).isEqualTo(StandardFormat.HUMAN_READABLE);
	}

	@Test
	void it_should_use_human_readable_format() {
		StandardOptions opts = StandardOptions.builder()
				.withHumanReadableFormat()
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.getFormat()).isEqualTo(StandardFormat.HUMAN_READABLE);
		assertThat(opts.serialize()).isNotNull().isEmpty();
		assertThat(opts.toBuilder().getFormat()).isEqualTo(StandardFormat.HUMAN_READABLE);
	}

	@Test
	void it_should_use_numeric_format() {
		StandardOptions opts = StandardOptions.builder()
				.withNumericFormat()
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.getFormat()).isEqualTo(StandardFormat.NUMERIC);
		assertThat(opts.serialize()).hasSize(1).containsExactly("-n");
		assertThat(opts.toBuilder().getFormat()).isEqualTo(StandardFormat.NUMERIC);
	}

	@Test
	void it_should_set_coord_format() {
		String coordFormat = "%d deg %d' %.2f";
		StandardOptions opts = StandardOptions.builder()
				.withCoordFormat(coordFormat)
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.getCoordFormat()).isEqualTo(coordFormat);
		assertThat(opts.serialize()).hasSize(2).containsExactly("-coordFormat", coordFormat);
		assertThat(opts.toBuilder().getCoordFormat()).isEqualTo(coordFormat);
	}

	@Test
	void it_should_set_date_format() {
		String dateFormat = "%Y:%m:%d %H:%M:%S";
		StandardOptions opts = StandardOptions.builder()
				.withDateFormat(dateFormat)
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.getDateFormat()).isEqualTo(dateFormat);
		assertThat(opts.serialize()).hasSize(2).containsExactly("-dateFormat", dateFormat);
		assertThat(opts.toBuilder().getDateFormat()).isEqualTo(dateFormat);
	}

	@Test
	void it_should_escape_html() {
		boolean escapeHtml = true;
		StandardOptions opts = StandardOptions.builder()
				.withEscapeHtml(escapeHtml)
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.isEscapeHtml()).isTrue();
		assertThat(opts.serialize()).hasSize(1).containsExactly("-E");
		assertThat(opts.toBuilder().isEscapeHtml()).isTrue();
	}

	@Test
	void it_should_escape_xml() {
		boolean escapeXml = true;
		StandardOptions opts = StandardOptions.builder()
				.withEscapeXml(escapeXml)
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.isEscapeXml()).isTrue();
		assertThat(opts.serialize()).hasSize(1).containsExactly("-ex");
		assertThat(opts.toBuilder().isEscapeXml()).isTrue();
	}

	@Test
	void it_should_ignore_minor_errors() {
		boolean ignoreMinorErrors = true;
		StandardOptions opts = StandardOptions.builder()
				.withIgnoreMinorErrors(ignoreMinorErrors)
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.isIgnoreMinorErrors()).isTrue();
		assertThat(opts.serialize()).hasSize(1).containsExactly("-m");
		assertThat(opts.toBuilder().isIgnoreMinorErrors()).isTrue();
	}

	@Test
	void it_should_set_password() {
		String password = "azerty123!";
		StandardOptions opts = StandardOptions.builder()
				.withPassword(password)
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.getPassword()).isEqualTo(password);
		assertThat(opts.serialize()).hasSize(2).containsExactly("-password", password);
		assertThat(opts.toBuilder().getPassword()).isEqualTo(password);
	}

	@Test
	void it_should_set_charset() {
		Charset charset = StandardCharsets.UTF_8;
		StandardOptions opts = StandardOptions.builder()
				.withCharset(charset)
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.getCharset()).isEqualTo(charset);
		assertThat(opts.serialize()).hasSize(2).containsExactly("-charset", "UTF-8");
		assertThat(opts.toBuilder().getCharset()).isEqualTo(charset);
	}

	@Test
	void it_should_use_modules() {
		String module = "MWG";
		StandardOptions opts = StandardOptions.builder()
				.useModules(module)
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.getModules()).hasSize(1).containsExactly(module);
		assertThat(opts.serialize()).hasSize(2).containsExactly("-use", module);
		assertThat(opts.toBuilder().getModules()).hasSize(1).containsExactly(module);
	}

	@Test
	void it_should_use_module_list() {
		String module = "MWG";
		StandardOptions opts = StandardOptions.builder()
				.useModules(singletonList(module))
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.getModules()).hasSize(1).containsExactly(module);
		assertThat(opts.serialize()).hasSize(2).containsExactly("-use", "MWG");
		assertThat(opts.toBuilder().getModules()).hasSize(1).containsExactly(module);
	}

	@Test
	void it_should_set_lang() {
		String lang = "fr";
		StandardOptions opts = StandardOptions.builder()
				.withLang(lang)
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.getLang()).isEqualTo(lang);
		assertThat(opts.serialize()).hasSize(2).containsExactly("-lang", lang);
		assertThat(opts.toBuilder().getLang()).isEqualTo(lang);
	}

	@Test
	void it_should_set_duplicates() {
		boolean duplicates = true;
		StandardOptions opts = StandardOptions.builder()
				.withDuplicates(true)
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.isDuplicates()).isEqualTo(duplicates);
		assertThat(opts.serialize()).hasSize(1).containsExactly("-duplicates");
		assertThat(opts.toBuilder().isDuplicates()).isTrue();
	}

	@Test
	void it_should_set_extract_embedded() {
		StandardOptions opts = StandardOptions.builder()
				.withExtractEmbedded(true)
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.isExtractEmbedded()).isTrue();
		assertThat(opts.serialize()).hasSize(1).containsExactly("-extractEmbedded");
		assertThat(opts.toBuilder().isExtractEmbedded()).isTrue();
	}

	@Test
	void it_should_set_extract_unknown() {
		StandardOptions opts = StandardOptions.builder()
				.withExtractUnknown(true)
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.isExtractUnknown()).isTrue();
		assertThat(opts.serialize()).hasSize(1).containsExactly("-u");
		assertThat(opts.toBuilder().isExtractUnknown()).isTrue();
	}

	@Test
	void it_should_set_no_composite_tags() {
		StandardOptions opts = StandardOptions.builder()
			.withoutCompositeTags(true)
			.build();

		assertThat(opts).isNotNull();
		assertThat(opts.isWithoutCompositeTags()).isTrue();
		assertThat(opts.serialize()).hasSize(1).containsExactly("-e");
		assertThat(opts.toBuilder().isWithoutCompositeTags()).isTrue();
	}

	@Test
	void it_should_overwrite_original() {
		StandardOptions opts = StandardOptions.builder()
				.withOverwriteOriginal()
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.isOverwriteOriginal()).isTrue();
		assertThat(opts.isOverwriteOriginalInPlace()).isFalse();
		assertThat(opts.serialize()).hasSize(1).containsExactly("-overwrite_original");

		Builder builder = opts.toBuilder();
		assertThat(builder.isOverwriteOriginal()).isTrue();
		assertThat(builder.isOverwriteOriginalInPlace()).isFalse();

		builder.doNotOverwriteOriginal();
		assertThat(builder.isOverwriteOriginal()).isFalse();
		assertThat(builder.isOverwriteOriginalInPlace()).isFalse();
	}

	@Test
	void it_should_overwrite_original_in_place() {
		StandardOptions opts = StandardOptions.builder()
				.withOverwriteOriginalInPlace()
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.isOverwriteOriginal()).isFalse();
		assertThat(opts.isOverwriteOriginalInPlace()).isTrue();
		assertThat(opts.serialize()).hasSize(1).containsExactly("-overwrite_original_in_place");

		Builder builder = opts.toBuilder();
		assertThat(builder.isOverwriteOriginal()).isFalse();
		assertThat(builder.isOverwriteOriginalInPlace()).isTrue();

		builder.doNotOverwriteOriginal();
		assertThat(builder.isOverwriteOriginal()).isFalse();
		assertThat(builder.isOverwriteOriginalInPlace()).isFalse();
	}

	@Test
	void it_should_use_args_format() {
		StandardOptions opts = StandardOptions.builder()
				.withUseArgsFormat(true)
				.build();

		assertThat(opts).isNotNull();
		assertThat(opts.isUseArgsFormat()).isTrue();
		assertThat(opts.serialize()).hasSize(1).containsExactly("-args");
		assertThat(opts.toBuilder().isUseArgsFormat()).isTrue();
	}

	@Test
	void it_should_implement_equals_hash_code() {
		EqualsVerifier.forClass(StandardOptions.class)
				.withPrefabValues(Charset.class, StandardCharsets.UTF_8, StandardCharsets.UTF_16)
				.verify();
	}

	@Test
	void it_should_implement_to_string() {
		StandardOptions opts = StandardOptions.builder().build();

		// @formatter:off
		assertThat(opts).hasToString(
				"StandardOptions{" +
						"format: HUMAN_READABLE, " +
						"ignoreMinorErrors: false, " +
						"coordFormat: null, " +
						"dateFormat: null, " +
						"charset: null, " +
						"password: null, " +
						"modules: [], " +
						"escapeHtml: false, " +
						"escapeXml: false, " +
						"lang: null, " +
						"duplicates: false, " +
						"extractEmbedded: false, " +
						"extractUnknown: false, " +
						"noCompositeTags: false, " +
						"overwriteOriginal: NONE, " +
						"useArgsFormat: false" +
				"}"
		);
		// @formatter:on
	}

	@Test
	void it_should_implement_builder_to_string() {
		Builder builder = StandardOptions.builder();

		// @formatter:off
		assertThat(builder).hasToString(
				"Builder{" +
						"format: HUMAN_READABLE, " +
						"ignoreMinorErrors: false, " +
						"coordFormat: null, " +
						"dateFormat: null, " +
						"charset: null, " +
						"password: null, " +
						"modules: [], " +
						"escapeHtml: false, " +
						"escapeXml: false, " +
						"lang: null, " +
						"duplicates: false, " +
						"extractEmbedded: false, " +
						"noCompositeTags: false, " +
						"overwriteOriginal: NONE, " +
						"useArgsFormat: false" +
				"}"
		);
		// @formatter:on
	}
}
