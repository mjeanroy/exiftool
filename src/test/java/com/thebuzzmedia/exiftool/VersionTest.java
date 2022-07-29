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

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionTest {

	@Test
	public void it_should_parse_version() {
		Version v1 = new Version("1");
		assertThat(v1.getMajor()).isEqualTo(1);
		assertThat(v1.getMinor()).isZero();
		assertThat(v1.getPatch()).isZero();

		Version v2 = new Version("1.1");
		assertThat(v2.getMajor()).isEqualTo(1);
		assertThat(v2.getMinor()).isEqualTo(1);
		assertThat(v2.getPatch()).isZero();

		Version v3 = new Version("1.1.1");
		assertThat(v3.getMajor()).isEqualTo(1);
		assertThat(v3.getMinor()).isEqualTo(1);
		assertThat(v3.getPatch()).isEqualTo(1);
	}

	@Test
	public void it_should_implement_to_string() {
		assertThat(new Version("1")).hasToString("1.0.0");
		assertThat(new Version("1.1")).hasToString("1.1.0");
		assertThat(new Version("1.1.1")).hasToString("1.1.1");
	}

	@Test
	public void it_should_implement_equals_hash_code() {
		EqualsVerifier.forClass(Version.class).verify();
	}

	@Test
	public void it_should_be_comparable() {
		Version v1 = new Version("9.1.1");
		Version v2 = new Version("9.1.2");
		Version v3 = new Version("9.1.1");
		Version v4 = new Version("10.0.0");

		assertThat(v1.compareTo(v1)).isZero();
		assertThat(v1.compareTo(v2)).isLessThan(0);
		assertThat(v2.compareTo(v1)).isGreaterThan(0);
		assertThat(v1.compareTo(v4)).isLessThan(0);
		assertThat(v4.compareTo(v1)).isGreaterThan(0);
		assertThat(v1.compareTo(v3)).isZero();
	}

	@Test
	public void handle_warnings() {
		Version v1 = new Version("10.48 [Warning: Library version is 10.10]");

		assertThat(v1.getMajor()).isEqualTo(10);
		assertThat(v1.getMinor()).isEqualTo(48);
		assertThat(v1.getPatch()).isEqualTo(0);

	}

	@Test
	public void handle_invalid_strings() {
		Version v1 = new Version("xxx.xx.xx");

		assertThat(v1.getMajor()).isEqualTo(0);
		assertThat(v1.getMinor()).isEqualTo(0);
		assertThat(v1.getPatch()).isEqualTo(0);

	}
}
