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

package com.thebuzzmedia.exiftool.tests.junit;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.addAll;

/// Clear system property before each test, and restore value
/// after each test.
public class SystemPropertyExtension implements BeforeEachCallback, AfterEachCallback {

	/// The namespace in which extension data will be stored.
	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ProcessLeakDetectorExtension.class.getName());

	private static final String KEY = "SYSTEM_ENV";

	/// Property names.
	private final Set<String> props;

	/// Create rule with property name.
	///
	/// @param prop Property name.
	public SystemPropertyExtension(String prop, String... other) {
		props = new HashSet<>();
		props.add(prop);
		addAll(props, other);
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		SystemEnv env = new SystemEnv();
		for (String prop : props) {
			env.put(prop, System.getProperty(prop));
			System.clearProperty(prop);
		}

		getStore(context).put(KEY, env);
	}

	@Override
	public void afterEach(ExtensionContext context) {
		SystemEnv env = getStore(context).remove(KEY, SystemEnv.class);

		for (Map.Entry<String, String> entry : env.entries()) {
			String prop = entry.getKey();
			String value = entry.getValue();
			if (value == null) {
				System.clearProperty(prop);
			}
			else {
				System.setProperty(prop, value);
			}
		}

		env.clear();
	}

	/// Get the internal store from the test context.
	///
	/// @param context The test context.
	/// @return The internal store.
	private static Store getStore(ExtensionContext context) {
		return context.getStore(NAMESPACE);
	}

	private static final class SystemEnv {
		private final Map<String, String> env;

		private SystemEnv() {
			this.env = new HashMap<>();
		}

		private void put(String key, String value) {
			this.env.put(key, value);
		}

		private Set<Map.Entry<String, String>> entries() {
			return this.env.entrySet();
		}

		private void clear() {
			this.env.clear();
		}
	}
}
