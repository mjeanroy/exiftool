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

package com.thebuzzmedia.exiftool;

import com.thebuzzmedia.exiftool.commons.lang.Strings;

import java.util.Objects;

import static com.thebuzzmedia.exiftool.commons.lang.PreConditions.notBlank;

/**
 * Define a version number with:
 * <ul>
 *   <li>A major identifier.</li>
 *   <li>A minor identifier.</li>
 *   <li>A patch identifier.</li>
 * </ul>
 */
public final class Version implements Comparable<Version> {

	/**
	 * Major Version Identifier.
	 */
	private final int major;

	/**
	 * Minor Version Identifier.
	 */
	private final int minor;

	/**
	 * Patch Version Identifier.
	 */
	private final int patch;

	/**
	 * Create new version number from a given string formatted
	 * such as: {@code [major].[minor].[patch]}.
	 *
	 * Major identifier is mandatory, other elements are optional and will be initialized
	 * to zero by default.
	 *
	 * Valid format: 1.1.0 / 1.1 / 1
	 *
	 * @param version Version number.
	 */
	public Version(String version) {
		notBlank(version, "Version number should be set");

		String[] parts = version.split("[ .,]+");
		major = Strings.tryParseInt(parts[0], 0);
		minor = parts.length >= 2 ? Strings.tryParseInt(parts[1], 0) : 0;
		patch = parts.length >= 3 ? Strings.tryParseInt(parts[2], 0) : 0;

	}

	/**
	 * Gets {@link #major}.
	 *
	 * @return {@link #major}.
	 */
	public int getMajor() {
		return major;
	}

	/**
	 * Gets {@link #minor}.
	 *
	 * @return {@link #minor}.
	 */
	public int getMinor() {
		return minor;
	}

	/**
	 * Gets {@link #patch}.
	 *
	 * @return {@link #patch}.
	 */
	public int getPatch() {
		return patch;
	}

	@Override
	public int hashCode() {
		return Objects.hash(major, minor, patch);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (o instanceof Version) {
			Version v = (Version) o;
			return Objects.equals(v.major, major) && Objects.equals(v.minor, minor) && Objects.equals(v.patch, patch);
		}

		return false;
	}

	@Override
	public String toString() {
		return major + "." + minor + "." + patch;
	}

	@Override
	public int compareTo(Version version) {
		int result = Integer.compare(major, version.major);

		if (result == 0) {
			result = Integer.compare(minor, version.minor);
			if (result == 0) {
				result = Integer.compare(patch, version.patch);
			}
		}

		return result;
	}
}
