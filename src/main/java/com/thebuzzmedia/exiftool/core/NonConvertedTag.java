/**
 * Copyright 2011 The Buzz Media, LLC
 * Copyright 2015-2019 Mickael Jeanroy
 * Copyright 2019 jack@pixbits.com
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

import com.thebuzzmedia.exiftool.Tag;
import com.thebuzzmedia.exiftool.commons.lang.ToStringBuilder;

import java.util.Objects;

/**
 * Utility class used to generate tags which are not print converted. This is
 * done in ExifTool by suffixing {@code #} to the tag and it has the same
 * effect of {@code -n} but applied on a per-tag basis.
 * <p>
 * The class wraps another tag and manages its different query name. By design
 * {@code NonConvertedTag.of(Tag.ANY)} is not equal to {@code Tag.ANY}
 * since it's possible to query two different formats of the same tag.
 *
 * @author Jack (jack@pixbits.com)
 */
public final class NonConvertedTag implements Tag {

	/**
	 * Create the tag from given original one.
	 *
	 * @param original The original one.
	 * @return The new tag.
	 */
	public static NonConvertedTag of(Tag original) {
		return new NonConvertedTag(original);
	}

	/**
	 * The original tag.
	 */
	private final Tag original;

	private NonConvertedTag(Tag original) {
		this.original = original;
	}

	@Override
	public String getName() {
		return original.getName() + "#";
	}

	@Override
	public String getDisplayName() {
		return original.getName();
	}

	@Override
	public <T> T parse(String value) {
		return original.parse(value);
	}

	@Override
	public String toString() {
		return ToStringBuilder.create(getClass())
				.append("original", original)
				.build();
	}

	@Override
	public int hashCode() {
		return Objects.hash(original);
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (other instanceof NonConvertedTag) {
			NonConvertedTag t = (NonConvertedTag) other;
			return Objects.equals(t.original, original);
		}

		return false;
	}
}
