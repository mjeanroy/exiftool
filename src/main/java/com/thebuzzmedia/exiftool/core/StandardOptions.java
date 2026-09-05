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

package com.thebuzzmedia.exiftool.core;

import com.thebuzzmedia.exiftool.ExifToolOptions;
import com.thebuzzmedia.exiftool.Format;
import com.thebuzzmedia.exiftool.commons.lang.PreConditions;
import com.thebuzzmedia.exiftool.commons.lang.ToStringBuilder;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.thebuzzmedia.exiftool.commons.iterables.Collections.isNotEmpty;
import static com.thebuzzmedia.exiftool.commons.iterables.Collections.size;
import static com.thebuzzmedia.exiftool.commons.lang.PreConditions.notBlank;
import static com.thebuzzmedia.exiftool.commons.lang.PreConditions.notEmpty;
import static com.thebuzzmedia.exiftool.commons.lang.Strings.isNotEmpty;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

/// Support options from exiftool binary. Most options are documented
/// here: [https://linux.die.net/man/1/exiftool](https://linux.die.net/man/1/exiftool).
public final class StandardOptions implements ExifToolOptions {

	/// Create builder.
	///
	/// @return Builder for [StandardOptions].
	public static Builder builder() {
		return new Builder();
	}

	/// Set output format.
	private final Format format;

	/// Ignore minor errors and warnings.
	private final boolean ignoreMinorErrors;

	/// Set format for GPS coordinates.
	///
	/// Examples:
	/// - "%d deg %d' %.2f" --> 54 deg 59' 22.80"  (default for reading)
	/// - "%d %d %.8f" --> 54 59 22.80000000  (default for copying)
	/// - "%d deg %.4f min" --> 54 deg 59.3800 min
	/// - "%.6f degrees" --> 54.989667 degrees$
	private final String coordFormat;

	/// Set format for date/time values. Consult the "strftime" man page on your system for details
	/// on the supported format.
	///
	/// The default format is equivalent to "%Y:%m:%d %H:%M:%S".
	private final String dateFormat;

	/// Specify encoding for special characters.
	private final List<String> charsets;

	/// Password for processing protected files.
	private final String password;

	/// Escape values for HTML.
	private final boolean escapeHtml;

	/// Escape values for XML.
	private final boolean escapeXml;

	/// Add features from plug-in module.
	private final List<String> modules;

	/// Set current language for tag descriptions and converted values. LANG is "de", "fr", "ja", etc.
	/// Note that tag/group names are always English, independent of the lang setting,
	/// and translation of warning/error messages has not yet been implemented.
	private final String lang;

	/// Allow or suppress duplicate tag names to be extracted.
	private final boolean duplicates;

	/// Extract information from embedded documents in EPS and PDF files, embedded MPF images in JPEG and MPO files,
	/// streaming metadata in AVCHD videos, and the resource fork of Mac OS files.
	private final boolean extractEmbedded;

	/// Extract values of unknown tags.
	/// Add another -u to also extract unknown information from binary data blocks.
	/// This option applies to tags with numerical tag ID's, and causes tag names like "Exif\_0xc5d9" to be generated for unknown information.
	/// It has no effect on information types which have human-readable tag ID's (such as XMP), since unknown tags are extracted automatically from these formats.
	private final boolean extractUnknown;

	/// Avoid extracting composite tags.
	private final boolean noCompositeTags;

	/// Whether or not to override original file when writing information to an image. Caution: This option should only
	/// be used if you already have separate backup copies of your image files.
	///
	/// Two mode are available:
	///   - Renaming a temporary file to replace the original. This deletes the original file and replaces it with the
	///     edited version in a single operation.
	///   - Similar to -overwrite\_original except that an extra step is added to allow the original file attributes to be preserved.
	///     For example, on a Mac this causes the original file creation date, ownership, type, creator, label color and icon to be preserved.
	///     This is implemented by opening the original file in update mode and replacing its data with a copy of a temporary file before
	///     deleting the temporary. The extra step results in slower performance, so the first mode should be used instead unless necessary.
	private final OverwriteMode overwriteOriginal;

	/// Output information in the form of exiftool arguments, suitable for use with the -@ option when writing.
	private final boolean useArgsFormat;

	/// Create options.
	///
	/// @param format Output format.
	/// @param ignoreMinorErrors Ignore minor errors and warnings.
	/// @param coordFormat Format for GPS coordinates.
	/// @param dateFormat Format for date/time values.
	/// @param charsets Specify encoding for special characters.
	/// @param password Password for processing protected files.
	/// @param modules Add features from plug-in module.
	/// @param escapeHtml Escape values for HTML.
	/// @param escapeXml Escape values for XML.
	/// @param lang Lang.
	/// @param duplicates Allow or suppress duplicate tag names to be extracted.
	/// @param extractEmbedded Extract information from embedded documents.
	/// @param extractUnknown Extract unknown tags.
	/// @param noCompositeTags Do not extract composite tags.
	/// @param overwriteMode The overwrite mode.
	/// @param useArgsFormat Output information in the form of exiftool arguments.
	private StandardOptions(
			Format format,
			boolean ignoreMinorErrors,
			String coordFormat,
			String dateFormat,
			Collection<String> charsets,
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
			boolean useArgsFormat
	) {
		this.format = format;
		this.ignoreMinorErrors = ignoreMinorErrors;
		this.coordFormat = coordFormat;
		this.dateFormat = dateFormat;
		this.charsets = new ArrayList<>(charsets);
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

		for (String charset : charsets) {
			arguments.add("-charset");
			arguments.add(charset);
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

	/// Get [#format]
	///
	/// @return [#format]
	public Format getFormat() {
		return format;
	}

	/// Get [#ignoreMinorErrors]
	///
	/// @return [#ignoreMinorErrors]
	public boolean isIgnoreMinorErrors() {
		return ignoreMinorErrors;
	}

	/// Get [#coordFormat]
	///
	/// @return [#coordFormat]
	public String getCoordFormat() {
		return coordFormat;
	}

	/// Get [#dateFormat]
	///
	/// @return [#dateFormat]
	public String getDateFormat() {
		return dateFormat;
	}

	/// Get [#charsets]
	///
	/// @return [#charsets]
	public List<String> getCharsets() {
		return unmodifiableList(charsets);
	}

	/// Here for backward compatibility, will be removed
	/// in the next major release.
	///
	/// @return First valid Charset.
	/// @deprecated Use [#getCharsets()] instead.
	@Deprecated
	public Charset getCharset() {
		for (String charset : charsets) {
			if (Charset.isSupported(charset)) {
				return Charset.forName(charset);
			}
		}

		return null;
	}

	/// Get [#password]
	///
	/// @return [#password]
	public String getPassword() {
		return password;
	}

	/// Get [#modules]
	///
	/// @return [#modules]
	public List<String> getModules() {
		return modules;
	}

	/// Get [#escapeHtml]
	///
	/// @return [#escapeHtml]
	public boolean isEscapeHtml() {
		return escapeHtml;
	}

	/// Get [#escapeXml]
	///
	/// @return [#escapeXml]
	public boolean isEscapeXml() {
		return escapeXml;
	}

	/// Get [#lang]
	///
	/// @return [#lang]
	public String getLang() {
		return lang;
	}

	/// Get [#duplicates]
	///
	/// @return [#duplicates]
	public boolean isDuplicates() {
		return duplicates;
	}

	/// Get [#extractEmbedded]
	///
	/// @return [#extractEmbedded]
	public boolean isExtractEmbedded() {
		return extractEmbedded;
	}

	/// Get [#extractUnknown]
	///
	/// @return [#extractUnknown]
	public boolean isExtractUnknown() {
		return extractUnknown;
	}

	/// Get [#noCompositeTags]
	///
	/// @return [#noCompositeTags]
	public boolean isWithoutCompositeTags() {
		return noCompositeTags;
	}

	/// Check if writing metadata will overwrite original file **(not in place)**.
	///
	/// @return `true` if writing to file will overwrite it, `false otherwise`.
	/// @see #isOverwriteOriginalInPlace()
	public boolean isOverwriteOriginal() {
		return overwriteOriginal == OverwriteMode.COPY;
	}

	/// Check if writing metadata will overwrite original file **in place**.
	///
	/// @return `true` if writing to file will overwrite it in place, `false otherwise`.
	/// @see #isOverwriteOriginal()
	public boolean isOverwriteOriginalInPlace() {
		return overwriteOriginal == OverwriteMode.IN_PLACE;
	}

	/// Get [#useArgsFormat]
	///
	/// @return [#useArgsFormat]
	public boolean isUseArgsFormat() {
		return useArgsFormat;
	}

	/// Re-Create builder from given options.
	///
	/// @return Builder.
	public Builder toBuilder() {
		return new Builder()
				.withFormat(format)
				.withIgnoreMinorErrors(ignoreMinorErrors)
				.withCoordFormat(coordFormat)
				.withDateFormat(dateFormat)
				.withCharsets(charsets)
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
					&& Objects.equals(charsets, opts.charsets)
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
				charsets,
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
				.append("charsets", charsets)
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

	/// Builder for [StandardOptions].
	public static class Builder {

		/// Set output format.
		///
		/// @see StandardOptions#format
		private Format format;

		/// Ignore minor errors and warnings.
		///
		/// @see StandardOptions#ignoreMinorErrors
		private boolean ignoreMinorErrors;

		/// Set format for GPS coordinates.
		///
		/// @see StandardOptions#coordFormat
		private String coordFormat;

		/// Set format for date/time values.
		///
		/// @see StandardOptions#dateFormat
		private String dateFormat;

		/// Specify encoding for special characters.
		///
		/// @see StandardOptions#charsets
		private final Set<String> charsets;

		/// Password for processing protected files.
		///
		/// @see StandardOptions#password
		private String password;

		/// Escape values for HTML.
		///
		/// @see StandardOptions#escapeHtml
		private boolean escapeHtml;

		/// Escape values for XML.
		///
		/// @see StandardOptions#escapeXml
		private boolean escapeXml;

		/// Add features from plug-in module.
		///
		/// @see StandardOptions#modules
		private final Set<String> modules;

		/// ExifTool lang.
		///
		/// @see StandardOptions#lang
		private String lang;

		/// Allow or suppress duplicate tag names to be extracted.
		///
		/// @see StandardOptions#duplicates
		private boolean duplicates;

		/// Extract information from embedded documents.
		///
		/// @see StandardOptions#extractEmbedded
		private boolean extractEmbedded;

		/// Extract values of unknown tags.
		///
		/// @see StandardOptions#extractUnknown
		private boolean extractUnknown;

		/// Do not extract composite tags.
		///
		/// @see StandardOptions#noCompositeTags
		private boolean noCompositeTags;

		/// Overwrite original file mode.
		///
		/// @see StandardOptions#overwriteOriginal
		private OverwriteMode overwriteOriginal;

		/// Output information in the form of exiftool arguments.
		private boolean useArgsFormat;

		private Builder() {
			this.ignoreMinorErrors = false;
			this.format = StandardFormat.HUMAN_READABLE;
			this.coordFormat = null;
			this.dateFormat = null;
			this.charsets = new LinkedHashSet<>();
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

		/// Update [#format]
		///
		/// @param format New [#format]
		/// @return The builder.
		public Builder withFormat(Format format) {
			this.format = format;
			return this;
		}

		/// Update [#ignoreMinorErrors]
		///
		/// @param ignoreMinorErrors New [#ignoreMinorErrors]
		/// @return The builder.
		public Builder withIgnoreMinorErrors(boolean ignoreMinorErrors) {
			this.ignoreMinorErrors = ignoreMinorErrors;
			return this;
		}

		/// Update [#coordFormat]
		///
		/// @param coordFormat New [#coordFormat]
		/// @return The builder.
		public Builder withCoordFormat(String coordFormat) {
			this.coordFormat = coordFormat;
			return this;
		}

		/// Update [#dateFormat]
		///
		/// @param dateFormat New [#dateFormat]
		/// @return The builder.
		public Builder withDateFormat(String dateFormat) {
			this.dateFormat = dateFormat;
			return this;
		}

		/// Update [#charsets]
		///
		/// @param charset New [#charsets]
		/// @return The builder.
		public Builder withCharset(Charset charset) {
			return withCharset(charset.displayName());
		}

		/// Update [#charsets]
		///
		/// @param charset New [#charsets]
		/// @return The builder.
		public Builder withCharset(String charset) {
			this.charsets.add(notBlank(charset, "charset must be set"));
			return this;
		}

		/// Update [#charsets]
		///
		/// @param charsets New [#charsets]
		/// @return The builder.
		public Builder withCharsets(Collection<String> charsets) {
			this.charsets.addAll(charsets);
			return this;
		}

		/// Update [#password]
		///
		/// @param password New [#password]
		/// @return The builder.
		public Builder withPassword(String password) {
			this.password = password;
			return this;
		}

		/// Add new exiftool module.
		///
		/// @param module Module name.
		/// @param others Other (optional) module names.
		/// @return The builder.
		public Builder useModules(String module, String... others) {
			List<String> modules = new ArrayList<>(1 + others.length);
			modules.add(module);
			Collections.addAll(modules, others);
			return useModules(modules);
		}

		/// Add new exiftool modules.
		///
		/// @param modules Module names.
		/// @return The builder.
		public Builder useModules(Collection<String> modules) {
			this.modules.addAll(modules);
			return this;
		}

		/// Update [#escapeHtml]
		///
		/// @param escapeHtml New [#escapeHtml]
		/// @return The builder.
		public Builder withEscapeHtml(boolean escapeHtml) {
			this.escapeHtml = escapeHtml;
			return this;
		}

		/// Update [#escapeXml]
		///
		/// @param escapeXml New [#escapeXml]
		/// @return The builder.
		public Builder withEscapeXml(boolean escapeXml) {
			this.escapeXml = escapeXml;
			return this;
		}

		/// Update [#format] with [StandardFormat#NUMERIC].
		///
		/// @return The builder.
		public Builder withNumericFormat() {
			this.format = StandardFormat.NUMERIC;
			return this;
		}

		/// Update [#format] with [StandardFormat#HUMAN_READABLE].
		///
		/// @return The builder.
		public Builder withHumanReadableFormat() {
			this.format = StandardFormat.HUMAN_READABLE;
			return this;
		}

		/// Update [#lang]
		///
		/// @param lang New [#lang]
		/// @return The builder.
		public Builder withLang(String lang) {
			this.lang = lang;
			return this;
		}

		/// Update [#duplicates]
		///
		/// @param duplicates [#duplicates]
		/// @return The builder.
		public Builder withDuplicates(boolean duplicates) {
			this.duplicates = duplicates;
			return this;
		}

		/// Update [#extractEmbedded]
		///
		/// @param extractEmbedded New [#extractEmbedded]
		/// @return The builder.
		public Builder withExtractEmbedded(boolean extractEmbedded) {
			this.extractEmbedded = extractEmbedded;
			return this;
		}

		/// Update [#noCompositeTags]
		///
		/// @param noCompositeTags New [#noCompositeTags]
		/// @return The builder.
		public Builder withoutCompositeTags(boolean noCompositeTags) {
			this.noCompositeTags = noCompositeTags;
			return this;
		}

		/// Do not overwrite original file.
		///
		/// @return The builder.
		/// @deprecated Use [#doNotOverwriteOriginal()] instead.
		@Deprecated
		public Builder doNotOverwiteOriginal() {
			return doNotOverwriteOriginal();
		}

		/// Do not overwrite original file.
		///
		/// @return The builder.
		public Builder doNotOverwriteOriginal() {
			return withOverwriteMode(OverwriteMode.NONE);
		}

		/// Overwrite original file.
		///
		/// @return The builder.
		/// @deprecated Use [#withOverwriteOriginal()] instead.
		@Deprecated
		public Builder withOverwiteOriginal() {
			return withOverwriteOriginal();
		}

		/// Overwrite original file.
		///
		/// @return The builder.
		public Builder withOverwriteOriginal() {
			return withOverwriteMode(OverwriteMode.COPY);
		}

		/// Overwrite original file in place.
		///
		/// Caution: this may cause some performance issues, prefer [#withOverwiteOriginal()] if possible.
		///
		/// @return The builder.
		/// @deprecated Use [#withOverwriteOriginalInPlace()] instead.
		@Deprecated
		public Builder withOverwiteOriginalInPlace() {
			return withOverwriteOriginalInPlace();
		}

		/// Overwrite original file in place.
		///
		/// Caution: this may cause some performance issues, prefer [#withOverwiteOriginal()] if possible.
		///
		/// @return The builder.
		public Builder withOverwriteOriginalInPlace() {
			return withOverwriteMode(OverwriteMode.IN_PLACE);
		}

		/// Update overwrite mode.
		///
		/// @param mode The mode.
		/// @return The builder.
		private Builder withOverwriteMode(OverwriteMode mode) {
			this.overwriteOriginal = mode;
			return this;
		}

		/// Update [#extractUnknown].
		///
		/// @param extractUnknown The flag.
		/// @return The builder.
		public Builder withExtractUnknown(boolean extractUnknown) {
			this.extractUnknown = extractUnknown;
			return this;
		}

		/// Update [#useArgsFormat].
		///
		/// @param useArgsFormat The flag.
		/// @return The builder.
		public Builder withUseArgsFormat(boolean useArgsFormat) {
			this.useArgsFormat = useArgsFormat;
			return this;
		}

		/// Build ExifTool options.
		///
		/// @return Options.
		public StandardOptions build() {
			return new StandardOptions(
					format,
					ignoreMinorErrors,
					coordFormat,
					dateFormat,
					charsets,
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

		/// Get [#format]
		///
		/// @return [#format]
		public Format getFormat() {
			return format;
		}

		/// Get [#ignoreMinorErrors]
		///
		/// @return [#ignoreMinorErrors]
		public boolean isIgnoreMinorErrors() {
			return ignoreMinorErrors;
		}

		/// Get [#coordFormat]
		///
		/// @return [#coordFormat]
		public String getCoordFormat() {
			return coordFormat;
		}

		/// Get [#dateFormat]
		///
		/// @return [#dateFormat]
		public String getDateFormat() {
			return dateFormat;
		}

		/// Get [#charsets]
		///
		/// @return [#charsets]
		public Set<String> getCharsets() {
			return unmodifiableSet(charsets);
		}

		/// Here for backward compatibility, will be removed
		/// in the next major release.
		///
		/// @return First valid Charset.
		/// @deprecated Use [#getCharsets()] instead.
		@Deprecated
		public Charset getCharset() {
			for (String charset : charsets) {
				if (Charset.isSupported(charset)) {
					return Charset.forName(charset);
				}
			}

			return null;
		}

		/// Get [#password]
		///
		/// @return [#password]
		public String getPassword() {
			return password;
		}

		/// Get [#escapeHtml]
		///
		/// @return [#escapeHtml]
		public boolean isEscapeHtml() {
			return escapeHtml;
		}

		/// Get [#escapeXml]
		///
		/// @return [#escapeXml]
		public boolean isEscapeXml() {
			return escapeXml;
		}

		/// Get [#modules]
		///
		/// @return [#modules]
		public Set<String> getModules() {
			return unmodifiableSet(modules);
		}

		/// Get [#lang]
		///
		/// @return [#lang]
		public String getLang() {
			return lang;
		}

		/// Get [#duplicates]
		///
		/// @return [#duplicates]
		public boolean isDuplicates() {
			return duplicates;
		}

		/// Get [#extractEmbedded]
		///
		/// @return [#extractEmbedded]
		public boolean isExtractEmbedded() {
			return extractEmbedded;
		}

		/// Get [#extractUnknown]
		///
		/// @return [#extractUnknown]
		public boolean isExtractUnknown() {
			return extractUnknown;
		}

		/// Get [#noCompositeTags]
		///
		/// @return [#noCompositeTags]
		public boolean isWithoutCompositeTags() {
			return noCompositeTags;
		}

		/// Check if writing metadata will overwrite original file **(not in place)**.
		///
		/// @return `true` if writing to file will overwrite it, `false otherwise`.
		/// @see #isOverwriteOriginalInPlace()
		public boolean isOverwriteOriginal() {
			return overwriteOriginal == OverwriteMode.COPY;
		}

		/// Check if writing metadata will overwrite original file **in place**.
		///
		/// @return `true` if writing to file will overwrite it in place, `false otherwise`.
		/// @see #isOverwriteOriginal()
		public boolean isOverwriteOriginalInPlace() {
			return overwriteOriginal == OverwriteMode.IN_PLACE;
		}

		/// Get [#useArgsFormat]
		///
		/// @return [#useArgsFormat]
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
					.append("charsets", charsets)
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
