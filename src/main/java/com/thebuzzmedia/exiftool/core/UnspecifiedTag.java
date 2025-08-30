/**
 * Copyright 2011 The Buzz Media, LLC
 * Copyright 2015-2019 Mickael Jeanroy
 * Copyright 2019 David Edwards
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
import java.util.regex.Pattern;

import static com.thebuzzmedia.exiftool.Constants.SEPARATOR;

/**
 * Class that can represent any tag, not just the standard tags.
 * <p>
 * Since the type of value is unknown, typed parsing is not possible,
 * and the tag does not know whether multiple values are expected or not.
 * Consequently, when parsing, it returns a {@code String[]} regardless of whether
 * the tag will ever have multiple values. Further parsing is then up to the caller.
 *
 * @author David Edwards (david@more.fool.me.uk)
 */
public final class UnspecifiedTag implements Tag {

	/**
	 * The tag name.
	 */
	private final String name;

	/**
	 * Create tag.
	 *
	 * @param name Tag name.
	 */
	public UnspecifiedTag(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDisplayName() {
		return name;
	}

	@Override
	public <T> T parse(String value) {
		// Clearly this won't work if caller is expecting anything other than
		// a string array. This is due to the way the {@link Tag} interface is designed.
		return (T) value.split(Pattern.quote(SEPARATOR));
	}

	@Override
	public String toString() {
		return ToStringBuilder.create(getClass())
				.append("name", name)
				.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o instanceof UnspecifiedTag) {
			UnspecifiedTag t = (UnspecifiedTag) o;
			return Objects.equals(name, t.name);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
