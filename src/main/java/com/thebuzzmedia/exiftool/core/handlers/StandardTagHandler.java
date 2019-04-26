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

package com.thebuzzmedia.exiftool.core.handlers;

import com.thebuzzmedia.exiftool.Tag;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * Read specified input tags line by line.
 *
 * <br>
 *
 * This class is not thread-safe and should be used to
 * read exiftool output from one thread (should not be shared across
 * several threads).
 */
public class StandardTagHandler extends BaseTagHandler {

	/**
	 * List of expected inputs.
	 */
	private final Map<String, Tag> inputs;

	/**
	 * Create handler with expected list of tags to parse.
	 *
	 * @param tags Expected list of tags.
	 */
	public StandardTagHandler(Collection<? extends Tag> tags) {
		Map<String, Tag> inputs = new HashMap<>();
		for (Tag tag : tags) {
			inputs.put(tag.getDisplayName(), tag);
		}

		this.inputs = unmodifiableMap(inputs);
	}

	@Override
	Tag toTag(String name) {
		// Return the tag a only if we were able to map the name back
		// to a Tag instance. If not, then this is an unknown/unexpected
		// tag return value and we skip it since we cannot translate it
		// back to one of our supported tags.
		return inputs.get(name);
	}
}
