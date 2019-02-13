/**
 * Copyright 2011 The Buzz Media, LLC
 * Copyright 2015 Mickael Jeanroy <mickael.jeanroy@gmail.com>
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

package com.thebuzzmedia.exiftool.tests;

import com.thebuzzmedia.exiftool.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * Static utilities for tags.
 */
public final class TagUtils {

	private TagUtils() {
	}

	/**
	 * Parse tag results into a map of tag names to parsed values
	 * @param tags map of tags to values
	 * @return map of tag names to parsed values
	 */
	public static Map<String, Object> parseTags(Map<Tag, String> tags) {
		final Map<String, Object> map = new HashMap<>(tags.size());
		for(Map.Entry<Tag, String> entry : tags.entrySet()) {
			String displayName = entry.getKey().getDisplayName();
			Object value = entry.getKey().parse(entry.getValue());
			map.put(displayName, value);
		}
		return map;
	}
}
