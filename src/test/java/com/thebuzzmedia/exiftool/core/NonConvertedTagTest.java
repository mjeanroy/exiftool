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

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NonConvertedTagTest {

	@Test
	public void it_should_implement_equals_hash_code() {
		EqualsVerifier.forClass(NonConvertedTag.class).verify();
	}

	@Test
	public void it_should_implement_to_string() {
		NonConvertedTag t = NonConvertedTag.of(StandardTag.AUTHOR);
		assertThat(t).hasToString(
				"NonConvertedTag{" +
					"original: AUTHOR" +
				"}"
		);
	}
}