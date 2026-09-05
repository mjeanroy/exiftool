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

package com.thebuzzmedia.exiftool.commons.lang;

import java.util.ArrayList;
import java.util.List;

/// Builder to write `toString` function in a fluent way.
public class ToStringBuilder {
	/// The characters used to start object representation.
	private static final String OBJECT_START = "{";

	/// The characters used to end object representation.
	private static final String OBJECT_END = "}";

	/// The double quote character.
	private static final String DOUBLE_QUOTE = "\"";

	/// The null value that will be output.
	private static final String NULL_VALUE = "null";

	/// Separator used between the name of a field and its value.
	private static final String KEY_VALUE_SEPARATOR = ": ";

	/// Separator between each field.
	private static final String FIELD_SEPARATOR = ", ";

	/// Create the builder with given class (the simple name will be used to start
	/// the `toString` value).
	///
	/// @param klass The class.
	/// @return The builder.
	public static ToStringBuilder create(Class<?> klass) {
		return new ToStringBuilder(klass);
	}

	/// The class being serialized.
	private final Class<?> klass;

	/// The formatted fields to display.
	private final List<String> fields;

	/// The current size of all formatted fields.
	private int size;

	/// Create the builder.
	///
	/// @param klass The class that will be used to start output.
	private ToStringBuilder(Class<?> klass) {
		this.klass = klass;
		this.size = 0;
		this.fields = new ArrayList<>();
	}

	/// Append new string field.
	///
	/// @param name Field name.
	/// @param value Field value.
	/// @return The builder.
	public ToStringBuilder append(String name, String value) {
		String val = value == null ? NULL_VALUE : DOUBLE_QUOTE + value + DOUBLE_QUOTE;
		return appendValue(name, val);
	}

	/// Append new object field.
	///
	/// @param name Field name.
	/// @param value Field value.
	/// @return The builder.
	public ToStringBuilder append(String name, long value) {
		return appendValue(name, String.valueOf(value));
	}

	/// Append new object field.
	///
	/// @param name Field name.
	/// @param object Field value.
	/// @return The builder.
	public ToStringBuilder append(String name, Object object) {
		String value = object == null ? NULL_VALUE : object.toString();
		return appendValue(name, value);
	}

	/// Internal method to append new value.
	///
	/// @param name The field name.
	/// @param value The field value.
	/// @return The builder.
	private ToStringBuilder appendValue(String name, CharSequence value) {
		String formattedValue = name + KEY_VALUE_SEPARATOR + value;
		this.fields.add(formattedValue);
		this.size += formattedValue.length();
		return this;
	}

	/// Create the final string.
	///
	/// @return The final string.
	public String build() {
		String klassName = klass.getSimpleName();
		String formattedFields = joinFields();
		int fullSize = klassName.length() + formattedFields.length() + 2;
		return new StringBuilder(fullSize)
				.append(klassName)
				.append(OBJECT_START)
				.append(formattedFields)
				.append(OBJECT_END)
				.toString();
	}

	private String joinFields() {
		int fullSize = size + (fields.size() * FIELD_SEPARATOR.length());
		StringBuilder sb = new StringBuilder(fullSize);
		boolean first = true;

		for (String field : fields) {
			if (!first) {
				sb.append(FIELD_SEPARATOR);
			}

			sb.append(field);
			first = false;
		}

		return sb.toString();
	}
}
