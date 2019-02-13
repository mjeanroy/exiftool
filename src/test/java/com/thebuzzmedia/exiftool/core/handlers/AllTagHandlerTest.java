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

import com.thebuzzmedia.exiftool.Constants;
import com.thebuzzmedia.exiftool.Tag;
import com.thebuzzmedia.exiftool.core.UnspecifiedTag;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AllTagHandlerTest {

	@Test
	public void it_should_read_null_line() {
		AllTagHandler handler = new AllTagHandler();
		boolean hasNext = handler.readLine(null);
		assertThat(hasNext).isFalse();
		assertThat(handler.getTags())
			.isNotNull()
			.isEmpty();
	}

	@Test
	public void it_should_read_last_line() {
		AllTagHandler handler = new AllTagHandler();
		boolean hasNext = handler.readLine("{ready}");
		assertThat(hasNext).isFalse();
		assertThat(handler.getTags())
			.isNotNull()
			.isEmpty();
	}

	@Test
	public void it_should_read_tag_line() {
		Tag tag = new UnspecifiedTag("baz");
		String value = "foobar";

		AllTagHandler handler = new AllTagHandler();
		boolean hasNext = handler.readLine(tag.getName() + ": " + value);

		Map<Tag, String> results = handler.getTags();
		assertThat(hasNext).isTrue();
		assertThat(results)
			.isNotNull()
			.isNotEmpty()
			.hasSize(1);

		assertThat(results)
				.containsKey(tag);
		assertThat(results.get(tag))
				.isEqualTo(value);
	}

	@Test
	public void it_should_read_tag_line_with_additional_pattern() {
		Tag tag = new UnspecifiedTag("baz");
		String value = "foobar: foo";

		AllTagHandler handler = new AllTagHandler();
		handler.readLine(tag.getName() + ": " + value);

		Map<Tag, String> results = handler.getTags();
		assertThat(results)
				.isNotNull()
				.isNotEmpty()
				.hasSize(1);

		assertThat(results)
				.containsKey(tag);
		assertThat(results.get(tag))
				.isEqualTo(value);
	}

	@Test
	public void it_should_read_tag_line_with_multiple_values() {
		Tag tag = new UnspecifiedTag("baz");
		String value = "foo" + Constants.SEPARATOR + "bar";

		AllTagHandler handler = new AllTagHandler();
		boolean hasNext = handler.readLine(tag.getName() + ": " + value);

		Map<Tag, String> results = handler.getTags();
		assertThat(hasNext).isTrue();
		assertThat(results)
				.isNotNull()
				.isNotEmpty()
				.hasSize(1);

		assertThat(results)
				.containsKey(tag);
		assertThat(tag.parse(results.get(tag)))
				.isEqualTo(new String[]{"foo", "bar"});
	}
}
