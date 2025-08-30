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

import com.thebuzzmedia.exiftool.ExifToolOptions;
import com.thebuzzmedia.exiftool.Format;
import com.thebuzzmedia.exiftool.commons.lang.ToStringBuilder;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.thebuzzmedia.exiftool.commons.iterables.Collections.isNotEmpty;
import static com.thebuzzmedia.exiftool.commons.lang.Strings.isNotEmpty;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

/**
 * Support options from exiftool binary. Most options are documented
 * here: <a href="https://linux.die.net/man/1/exiftool">https://linux.die.net/man/1/exiftool</a>.
 */
public final class StandardOptions implements ExifToolOptions {

	/**
	 * Create builder.
	 *
	 * @return Builder for {@link StandardOptions}.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Set output format.
	 */
	private final Format format;

	/**
	 * Ignore minor errors and warnings.
	 */
	private final boolean ignoreMinorErrors;

	/**
	 * Set format for GPS coordinates.
	 *
	 * <br>
	 *
	 * Examples:
	 *
	 * <ul>
	 *   <li>"%d deg %d' %.2f" --> 54 deg 59' 22.80"  (default for reading)</li>
	 *   <li>"%d %d %.8f" --> 54 59 22.80000000  (default for copying)</li>
	 *   <li>"%d deg %.4f min" --> 54 deg 59.3800 min</li>
	 *   <li>"%.6f degrees" --> 54.989667 degrees$</li>
	 * </ul>
	 */
	private final String coordFormat;

	/**
	 * Set format for date/time values. Consult the "strftime" man page on your system for details
	 * on the supported format.
	 *
	 * <br>
	 *
	 * The default format is equivalent to "%Y:%m:%d %H:%M:%S".
	 */
	private final String dateFormat;

	/**
	 * Specify encoding for special characters.
	 */
	private final Charset charset;

	/**
	 * Password for processing protected files.
	 */
	private final String password;

	/**
	 * Escape values for HTML.
	 */
	private final boolean escapeHtml;

	/**
	 * Escape values for XML.
	 */
	private final boolean escapeXml;

	/**
	 * Add features from plug-in module.
	 */
	private final List<String> modules;

	/**
	 * Set current language for tag descriptions and converted values. LANG is "de", "fr", "ja", etc.
	 *
	 * Note that tag/group names are always English, independent of the lang setting,
	 * and translation of warning/error messages has not yet been implemented.
	 */
	private final String lang;

	/**
	 * Allow or suppress duplicate tag names to be extracted.
	 */
	private final boolean duplicates;

	/**
	 * Extract information from embedded documents in EPS and PDF files, embedded MPF images in JPEG and MPO files,
	 * streaming metadata in AVCHD videos, and the resource fork of Mac OS files.
	 */
	private final boolean extractEmbedded;

	/**
	 * Extract values of unknown tags.
	 * Add another -u to also extract unknown information from binary data blocks.
	 * This option applies to tags with numerical tag ID's, and causes tag names like "Exif_0xc5d9" to be generated for unknown information.
	 * It has no effect on information types which have human-readable tag ID's (such as XMP), since unknown tags are extracted automatically from these formats.
	 */
	private final boolean extractUnknown;

	/**
	 * Avoid extracting composite tags.
	 */
	private final boolean noCompositeTags;

	/**
	 * Wether or not to override original file when writing information to an image. Caution: This option should only
	 * be used if you already have separate backup copies of your image files.
	 *
	 * Two mode are available:
	 * <ul>
	 *   <li>
	 *     Renaming a temporary file to replace the original. This deletes the original file and replaces it with the
	 *     edited version in a single operation.
	 *   </li>
	 *   <li>
	 *     Similar to -overwrite_original except that an extra step is added to allow the original file attributes to be preserved.
	 *     For example, on a Mac this causes the original file creation date, ownership, type, creator, label color and icon to be preserved.
	 *     This is implemented by opening the original file in update mode and replacing its data with a copy of a temporary file before
	 *     deleting the temporary. The extra step results in slower performance, so the first mode should be used instead unless necessary.
	 *   </li>
	 * </ul>
	 */
	private final OverwriteMode overwriteOriginal;

	/**
	 * Output information in the form of exiftool arguments, suitable for use with the -@ option when writing.
	 */
	private final boolean useArgsFormat;

	/**
	 * Create options.
	 *
	 * @param format Output format.
	 * @param ignoreMinorErrors Ignore minor errors and warnings.
	 * @param coordFormat Format for GPS coordinates.
	 * @param dateFormat Format for date/time values.
	 * @param charset Specify encoding for special characters.
	 * @param password Password for processing protected files.
	 * @param modules Add features from plug-in module.
	 * @param escapeHtml Escape values for HTML.
	 * @param escapeXml Escape values for XML.
	 * @param lang Lang.
	 * @param duplicates Allow or suppress duplicate tag names to be extracted.
	 * @param extractEmbedded Extract information from embedded documents.
	 * @param extractUnknown Extract unknown tags.
	 * @param noCompositeTags Do not extract composite tags.
	 * @param overwriteMode The overwrite mode.
	 * @param useArgsFormat Output information in the form of exiftool arguments.
	 */
	private StandardOptions(
			Format format,
			boolean ignoreMinorErrors,
			String coordFormat,
			String dateFormat,
			Charset charset,
			String password,
			Collection<String> modules,
			boolean escapeHtml,
			boolean escapeXml,
			String lang,
			boolean duplicates,
			boolean extractEmbedded,
			boolean extractUnknown,
			boolean noCompositeTags,
			OverwriteMode overwriteMode,
			boolean useArgsFormat) {

		this.format = format;
		this.ignoreMinorErrors = ignoreMinorErrors;
		this.coordFormat = coordFormat;
		this.dateFormat = dateFormat;
		this.charset = charset;
		this.password = password;
		this.modules = unmodifiableList(new ArrayList<>(modules));
		this.escapeHtml = escapeHtml;
		this.escapeXml = escapeXml;
		this.lang = lang;
		this.extractEmbedded = extractEmbedded;
		this.extractUnknown = extractUnknown;
		this.noCompositeTags = noCompositeTags;
		this.duplicates = duplicates;
		this.overwriteOriginal = overwriteMode;
		this.useArgsFormat = useArgsFormat;
	}

	@Override
	public Iterable<String> serialize() {
		List<String> arguments = new ArrayList<>(30);

		if (format != null) {
			arguments.addAll(format.getArgs());
		}

		if (ignoreMinorErrors) {
			arguments.add("-m");
		}

		if (extractUnknown) {
			arguments.add("-u");
		}

		if (useArgsFormat) {
			arguments.add("-args");
		}

		if (isNotEmpty(dateFormat)) {
			arguments.add("-dateFormat");
			arguments.add(dateFormat);
		}

		if (isNotEmpty(coordFormat)) {
			arguments.add("-coordFormat");
			arguments.add(coordFormat);
		}

		if (charset != null) {
			arguments.add("-charset");
			arguments.add(charset.displayName());
		}

		if (isNotEmpty(password)) {
			arguments.add("-password");
			arguments.add(password);
		}

		if (isNotEmpty(modules)) {
			for (String module : modules) {
				arguments.add("-use");
				arguments.add(module);
			}
		}

		if (escapeHtml) {
			arguments.add("-E");
		}

		if (escapeXml) {
			arguments.add("-ex");
		}

		if (isNotEmpty(lang)) {
			arguments.add("-lang");
			arguments.add(lang);
		}

		if (duplicates) {
			arguments.add("-duplicates");
		}

		if (extractEmbedded) {
			arguments.add("-extractEmbedded");
		}

		if (noCompositeTags) {
			arguments.add("-e");
		}

		String overwrite = overwriteOriginal == null ? null : overwriteOriginal.arg;
		if (isNotEmpty(overwrite)) {
			arguments.add(overwrite);
		}

		return arguments;
	}

	/**
	 * Get {@link #format}
	 *
	 * @return {@link #format}
	 */
	public Format getFormat() {
		return format;
	}

	/**
	 * Get {@link #ignoreMinorErrors}
	 *
	 * @return {@link #ignoreMinorErrors}
	 */
	public boolean isIgnoreMinorErrors() {
		return ignoreMinorErrors;
	}

	/**
	 * Get {@link #coordFormat}
	 *
	 * @return {@link #coordFormat}
	 */
	public String getCoordFormat() {
		return coordFormat;
	}

	/**
	 * Get {@link #dateFormat}
	 *
	 * @return {@link #dateFormat}
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * Get {@link #charset}
	 *
	 * @return {@link #charset}
	 */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * Get {@link #password}
	 *
	 * @return {@link #password}
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Get {@link #modules}
	 *
	 * @return {@link #modules}
	 */
	public List<String> getModules() {
		return modules;
	}

	/**
	 * Get {@link #escapeHtml}
	 *
	 * @return {@link #escapeHtml}
	 */
	public boolean isEscapeHtml() {
		return escapeHtml;
	}

	/**
	 * Get {@link #escapeXml}
	 *
	 * @return {@link #escapeXml}
	 */
	public boolean isEscapeXml() {
		return escapeXml;
	}

	/**
	 * Get {@link #lang}
	 *
	 * @return {@link #lang}
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * Get {@link #duplicates}
	 *
	 * @return {@link #duplicates}
	 */
	public boolean isDuplicates() {
		return duplicates;
	}

	/**
	 * Get {@link #extractEmbedded}
	 *
	 * @return {@link #extractEmbedded}
	 */
	public boolean isExtractEmbedded() {
		return extractEmbedded;
	}

	/**
	 * Get {@link #extractUnknown}
	 *
	 * @return {@link #extractUnknown}
	 */
	public boolean isExtractUnknown() {
		return extractUnknown;
	}

	/**
	 * Get {@link #noCompositeTags}
	 *
	 * @return {@link #noCompositeTags}
	 */
	public boolean isWithoutCompositeTags() {
		return noCompositeTags;
	}

	/**
	 * Check if writing metadata will overwrite original file <strong>(not in place)</strong>.
	 *
	 * @return {@code true} if writing to file will overwrite it, {@code false otherwise}.
	 * @see #isOverwriteOriginalInPlace()
	 */
	public boolean isOverwriteOriginal() {
		return overwriteOriginal == OverwriteMode.COPY;
	}

	/**
	 * Check if writing metadata will overwrite original file <strong>in place</strong>.
	 *
	 * @return {@code true} if writing to file will overwrite it in place, {@code false otherwise}.
	 * @see #isOverwriteOriginal()
	 */
	public boolean isOverwriteOriginalInPlace() {
		return overwriteOriginal == OverwriteMode.IN_PLACE;
	}

	/**
	 * Get {@link #useArgsFormat}
	 *
	 * @return {@link #useArgsFormat}
	 */
	public boolean isUseArgsFormat() {
		return useArgsFormat;
	}

	/**
	 * Re-Create builder from given options.
	 *
	 * @return Builder.
	 */
	public Builder toBuilder() {
		return new Builder()
				.withFormat(format)
				.withIgnoreMinorErrors(ignoreMinorErrors)
				.withCoordFormat(coordFormat)
				.withDateFormat(dateFormat)
				.withCharset(charset)
				.withPassword(password)
				.useModules(modules)
				.withEscapeHtml(escapeHtml)
				.withEscapeXml(escapeXml)
				.withLang(lang)
				.withDuplicates(duplicates)
				.withExtractEmbedded(extractEmbedded)
				.withExtractUnknown(extractUnknown)
				.withoutCompositeTags(noCompositeTags)
				.withOverwriteMode(overwriteOriginal)
				.withUseArgsFormat(useArgsFormat);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (o instanceof StandardOptions) {
			StandardOptions opts = (StandardOptions) o;
			return Objects.equals(format, opts.format)
					&& Objects.equals(ignoreMinorErrors, opts.ignoreMinorErrors)
					&& Objects.equals(coordFormat, opts.coordFormat)
					&& Objects.equals(dateFormat, opts.dateFormat)
					&& Objects.equals(charset, opts.charset)
					&& Objects.equals(password, opts.password)
					&& Objects.equals(modules, opts.modules)
					&& Objects.equals(escapeHtml, opts.escapeHtml)
					&& Objects.equals(escapeXml, opts.escapeXml)
					&& Objects.equals(lang, opts.lang)
					&& Objects.equals(duplicates, opts.duplicates)
					&& Objects.equals(extractEmbedded, opts.extractEmbedded)
					&& Objects.equals(extractUnknown, opts.extractUnknown)
					&& Objects.equals(noCompositeTags, opts.noCompositeTags)
					&& Objects.equals(overwriteOriginal, opts.overwriteOriginal)
					&& Objects.equals(useArgsFormat, opts.useArgsFormat);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				format,
				ignoreMinorErrors,
				coordFormat,
				dateFormat,
				charset,
				password,
				modules,
				escapeHtml,
				escapeXml,
				lang,
				duplicates,
				extractEmbedded,
				extractUnknown,
				noCompositeTags,
				overwriteOriginal,
				useArgsFormat
		);
	}

	@Override
	public String toString() {
		return ToStringBuilder.create(getClass())
				.append("format", format)
				.append("ignoreMinorErrors", ignoreMinorErrors)
				.append("coordFormat", coordFormat)
				.append("dateFormat", dateFormat)
				.append("charset", charset)
				.append("password", password)
				.append("modules", modules)
				.append("escapeHtml", escapeHtml)
				.append("escapeXml", escapeXml)
				.append("lang", lang)
				.append("duplicates", duplicates)
				.append("extractEmbedded", extractEmbedded)
				.append("extractUnknown", extractUnknown)
				.append("noCompositeTags", noCompositeTags)
				.append("overwriteOriginal", overwriteOriginal)
				.append("useArgsFormat", useArgsFormat)
				.build();
	}

	/**
	 * Builder for {@link StandardOptions}.
	 */
	public static class Builder {

		/**
		 * Set output format.
		 *
		 * @see StandardOptions#format
		 */
		private Format format;

		/**
		 * Ignore minor errors and warnings.
		 *
		 * @see StandardOptions#ignoreMinorErrors
		 */
		private boolean ignoreMinorErrors;

		/**
		 * Set format for GPS coordinates.
		 *
		 * @see StandardOptions#coordFormat
		 */
		private String coordFormat;

		/**
		 * Set format for date/time values.
		 *
		 * @see StandardOptions#dateFormat
		 */
		private String dateFormat;

		/**
		 * Specify encoding for special characters.
		 *
		 * @see StandardOptions#charset
		 */
		private Charset charset;

		/**
		 * Password for processing protected files.
		 *
		 * @see StandardOptions#password
		 */
		private String password;

		/**
		 * Escape values for HTML.
		 *
		 * @see StandardOptions#escapeHtml
		 */
		private boolean escapeHtml;

		/**
		 * Escape values for XML.
		 *
		 * @see StandardOptions#escapeXml
		 */
		private boolean escapeXml;

		/**
		 * Add features from plug-in module.
		 *
		 * @see StandardOptions#modules
		 */
		private final Set<String> modules;

		/**
		 * ExifTool lang.
		 *
		 * @see StandardOptions#lang
		 */
		private String lang;

		/**
		 * Allow or suppress duplicate tag names to be extracted.
		 *
		 * @see StandardOptions#duplicates
		 */
		private boolean duplicates;

		/**
		 * Extract information from embedded documents.
		 *
		 * @see StandardOptions#extractEmbedded
		 */
		private boolean extractEmbedded;

		/**
		 * Extract values of unknown tags.
		 *
		 * @see StandardOptions#extractUnknown
		 */
		private boolean extractUnknown;

		/**
		 * Do not extract composite tags.
		 *
		 * @see StandardOptions#noCompositeTags
		 */
		private boolean noCompositeTags;

		/**
		 * Overwrite original file mode.
		 *
		 * @see StandardOptions#overwriteOriginal
		 */
		private OverwriteMode overwriteOriginal;

		/**
		 * Output information in the form of exiftool arguments.
		 */
		private boolean useArgsFormat;

		private Builder() {
			this.ignoreMinorErrors = false;
			this.format = StandardFormat.HUMAN_READABLE;
			this.coordFormat = null;
			this.dateFormat = null;
			this.charset = null;
			this.password = null;
			this.modules = new LinkedHashSet<>();
			this.escapeHtml = false;
			this.escapeXml = false;
			this.lang = null;
			this.duplicates = false;
			this.extractEmbedded = false;
			this.extractUnknown = false;
			this.noCompositeTags = false;
			this.overwriteOriginal = OverwriteMode.NONE;
			this.useArgsFormat = false;
		}

		/**
		 * Update {@link #format}
		 *
		 * @param format New {@link #format}
		 * @return The builder.
		 */
		public Builder withFormat(Format format) {
			this.format = format;
			return this;
		}

		/**
		 * Update {@link #ignoreMinorErrors}
		 *
		 * @param ignoreMinorErrors New {@link #ignoreMinorErrors}
		 * @return The builder.
		 */
		public Builder withIgnoreMinorErrors(boolean ignoreMinorErrors) {
			this.ignoreMinorErrors = ignoreMinorErrors;
			return this;
		}

		/**
		 * Update {@link #coordFormat}
		 *
		 * @param coordFormat New {@link #coordFormat}
		 * @return The builder.
		 */
		public Builder withCoordFormat(String coordFormat) {
			this.coordFormat = coordFormat;
			return this;
		}

		/**
		 * Update {@link #dateFormat}
		 *
		 * @param dateFormat New {@link #dateFormat}
		 * @return The builder.
		 */
		public Builder withDateFormat(String dateFormat) {
			this.dateFormat = dateFormat;
			return this;
		}

		/**
		 * Update {@link #charset}
		 *
		 * @param charset New {@link #charset}
		 * @return The builder.
		 */
		public Builder withCharset(Charset charset) {
			this.charset = charset;
			return this;
		}

		/**
		 * Update {@link #password}
		 *
		 * @param password New {@link #password}
		 * @return The builder.
		 */
		public Builder withPassword(String password) {
			this.password = password;
			return this;
		}

		/**
		 * Add new exiftool module.
		 *
		 * @param module Module name.
		 * @param others Other (optional) module names.
		 * @return The builder.
		 */
		public Builder useModules(String module, String... others) {
			List<String> modules = new ArrayList<>(1 + others.length);
			modules.add(module);
			Collections.addAll(modules, others);
			return useModules(modules);
		}

		/**
		 * Add new exiftool modules.
		 *
		 * @param modules Module names.
		 * @return The builder.
		 */
		public Builder useModules(Collection<String> modules) {
			this.modules.addAll(modules);
			return this;
		}

		/**
		 * Update {@link #escapeHtml}
		 *
		 * @param escapeHtml New {@link #escapeHtml}
		 * @return The builder.
		 */
		public Builder withEscapeHtml(boolean escapeHtml) {
			this.escapeHtml = escapeHtml;
			return this;
		}

		/**
		 * Update {@link #escapeXml}
		 *
		 * @param escapeXml New {@link #escapeXml}
		 * @return The builder.
		 */
		public Builder withEscapeXml(boolean escapeXml) {
			this.escapeXml = escapeXml;
			return this;
		}

		/**
		 * Update {@link #format} with {@link StandardFormat#NUMERIC}.
		 *
		 * @return The builder.
		 */
		public Builder withNumericFormat() {
			this.format = StandardFormat.NUMERIC;
			return this;
		}

		/**
		 * Update {@link #format} with {@link StandardFormat#HUMAN_READABLE}.
		 *
		 * @return The builder.
		 */
		public Builder withHumanReadableFormat() {
			this.format = StandardFormat.HUMAN_READABLE;
			return this;
		}

		/**
		 * Update {@link #lang}
		 *
		 * @param lang New {@link #lang}
		 * @return The builder.
		 */
		public Builder withLang(String lang) {
			this.lang = lang;
			return this;
		}

		/**
		 * Update {@link #duplicates}
		 *
		 * @param duplicates {@link #duplicates}
		 * @return The builder.
		 */
		public Builder withDuplicates(boolean duplicates) {
			this.duplicates = duplicates;
			return this;
		}

		/**
		 * Update {@link #extractEmbedded}
		 *
		 * @param extractEmbedded New {@link #extractEmbedded}
		 * @return The builder.
		 */
		public Builder withExtractEmbedded(boolean extractEmbedded) {
			this.extractEmbedded = extractEmbedded;
			return this;
		}

		/**
		 * Update {@link #noCompositeTags}
		 *
		 * @param noCompositeTags New {@link #noCompositeTags}
		 * @return The builder.
		 */
		public Builder withoutCompositeTags(boolean noCompositeTags) {
			this.noCompositeTags = noCompositeTags;
			return this;
		}

		/**
		 * Do not overwrite original file.
		 *
		 * @return The builder.
		 * @deprecated Use {@link #doNotOverwriteOriginal()} instead.
		 */
		@Deprecated
		public Builder doNotOverwiteOriginal() {
			return doNotOverwriteOriginal();
		}

		/**
		 * Do not overwrite original file.
		 *
		 * @return The builder.
		 */
		public Builder doNotOverwriteOriginal() {
			return withOverwriteMode(OverwriteMode.NONE);
		}

		/**
		 * Overwrite original file.
		 *
		 * @return The builder.
		 * @deprecated Use {@link #withOverwriteOriginal()} instead.
		 */
		@Deprecated
		public Builder withOverwiteOriginal() {
			return withOverwriteOriginal();
		}

		/**
		 * Overwrite original file.
		 *
		 * @return The builder.
		 */
		public Builder withOverwriteOriginal() {
			return withOverwriteMode(OverwriteMode.COPY);
		}

		/**
		 * Overwrite original file in place.
		 *
		 * <br>
		 *
		 * Caution: this may cause some performance issues, prefer {@link #withOverwiteOriginal()} if possible.
		 *
		 * @return The builder.
		 * @deprecated Use {@link #withOverwriteOriginalInPlace()} instead.
		 */
		@Deprecated
		public Builder withOverwiteOriginalInPlace() {
			return withOverwriteOriginalInPlace();
		}

		/**
		 * Overwrite original file in place.
		 *
		 * <br>
		 *
		 * Caution: this may cause some performance issues, prefer {@link #withOverwiteOriginal()} if possible.
		 *
		 * @return The builder.
		 */
		public Builder withOverwriteOriginalInPlace() {
			return withOverwriteMode(OverwriteMode.IN_PLACE);
		}

		/**
		 * Update overwrite mode.
		 *
		 * @param mode The mode.
		 * @return The builder.
		 */
		private Builder withOverwriteMode(OverwriteMode mode) {
			this.overwriteOriginal = mode;
			return this;
		}

		/**
		 * Update {@link #extractUnknown}.
		 *
		 * @param extractUnknown The flag.
		 * @return The builder.
		 */
		public Builder withExtractUnknown(boolean extractUnknown) {
			this.extractUnknown = extractUnknown;
			return this;
		}

		/**
		 * Update {@link #useArgsFormat}.
		 *
		 * @param useArgsFormat The flag.
		 * @return The builder.
		 */
		public Builder withUseArgsFormat(boolean useArgsFormat) {
			this.useArgsFormat = useArgsFormat;
			return this;
		}

		/**
		 * Build ExifTool options.
		 *
		 * @return Options.
		 */
		public StandardOptions build() {
			return new StandardOptions(
					format,
					ignoreMinorErrors,
					coordFormat,
					dateFormat,
					charset,
					password,
					modules,
					escapeHtml,
					escapeXml,
					lang,
					duplicates,
					extractEmbedded,
					extractUnknown,
					noCompositeTags,
					overwriteOriginal,
					useArgsFormat
			);
		}

		/**
		 * Get {@link #format}
		 *
		 * @return {@link #format}
		 */
		public Format getFormat() {
			return format;
		}

		/**
		 * Get {@link #ignoreMinorErrors}
		 *
		 * @return {@link #ignoreMinorErrors}
		 */
		public boolean isIgnoreMinorErrors() {
			return ignoreMinorErrors;
		}

		/**
		 * Get {@link #coordFormat}
		 *
		 * @return {@link #coordFormat}
		 */
		public String getCoordFormat() {
			return coordFormat;
		}

		/**
		 * Get {@link #dateFormat}
		 *
		 * @return {@link #dateFormat}
		 */
		public String getDateFormat() {
			return dateFormat;
		}

		/**
		 * Get {@link #charset}
		 *
		 * @return {@link #charset}
		 */
		public Charset getCharset() {
			return charset;
		}

		/**
		 * Get {@link #password}
		 *
		 * @return {@link #password}
		 */
		public String getPassword() {
			return password;
		}

		/**
		 * Get {@link #escapeHtml}
		 *
		 * @return {@link #escapeHtml}
		 */
		public boolean isEscapeHtml() {
			return escapeHtml;
		}

		/**
		 * Get {@link #escapeXml}
		 *
		 * @return {@link #escapeXml}
		 */
		public boolean isEscapeXml() {
			return escapeXml;
		}

		/**
		 * Get {@link #modules}
		 *
		 * @return {@link #modules}
		 */
		public Set<String> getModules() {
			return unmodifiableSet(modules);
		}

		/**
		 * Get {@link #lang}
		 *
		 * @return {@link #lang}
		 */
		public String getLang() {
			return lang;
		}

		/**
		 * Get {@link #duplicates}
		 *
		 * @return {@link #duplicates}
		 */
		public boolean isDuplicates() {
			return duplicates;
		}

		/**
		 * Get {@link #extractEmbedded}
		 *
		 * @return {@link #extractEmbedded}
		 */
		public boolean isExtractEmbedded() {
			return extractEmbedded;
		}

		/**
		 * Get {@link #extractUnknown}
		 *
		 * @return {@link #extractUnknown}
		 */
		public boolean isExtractUnknown() {
			return extractUnknown;
		}

		/**
		 * Get {@link #noCompositeTags}
		 *
		 * @return {@link #noCompositeTags}
		 */
		public boolean isWithoutCompositeTags() {
			return noCompositeTags;
		}

		/**
		 * Check if writing metadata will overwrite original file <strong>(not in place)</strong>.
		 *
		 * @return {@code true} if writing to file will overwrite it, {@code false otherwise}.
		 * @see #isOverwriteOriginalInPlace()
		 */
		public boolean isOverwriteOriginal() {
			return overwriteOriginal == OverwriteMode.COPY;
		}

		/**
		 * Check if writing metadata will overwrite original file <strong>in place</strong>.
		 *
		 * @return {@code true} if writing to file will overwrite it in place, {@code false otherwise}.
		 * @see #isOverwriteOriginal()
		 */
		public boolean isOverwriteOriginalInPlace() {
			return overwriteOriginal == OverwriteMode.IN_PLACE;
		}

		/**
		 * Get {@link #useArgsFormat}
		 *
		 * @return {@link #useArgsFormat}
		 */
		public boolean isUseArgsFormat() {
			return useArgsFormat;
		}

		@Override
		public String toString() {
			return ToStringBuilder.create(getClass())
					.append("format", format)
					.append("ignoreMinorErrors", ignoreMinorErrors)
					.append("coordFormat", coordFormat)
					.append("dateFormat", dateFormat)
					.append("charset", charset)
					.append("password", password)
					.append("modules", modules)
					.append("escapeHtml", escapeHtml)
					.append("escapeXml", escapeXml)
					.append("lang", lang)
					.append("duplicates", duplicates)
					.append("extractEmbedded", extractEmbedded)
					.append("noCompositeTags", noCompositeTags)
					.append("overwriteOriginal", overwriteOriginal)
					.append("useArgsFormat", useArgsFormat)
					.build();
		}
	}

	private enum OverwriteMode {
		NONE(null),
		COPY("-overwrite_original"),
		IN_PLACE("-overwrite_original_in_place");

		private final String arg;

		OverwriteMode(String arg) {
			this.arg = arg;
		}
	}
}
