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
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.thebuzzmedia.exiftool.tests.TestConstants.IS_WINDOWS;

public final class ProcessLeakDetectorExtension implements BeforeEachCallback, AfterEachCallback {

	/// The namespace in which extension data will be stored.
	private static final Namespace NAMESPACE = Namespace.create(ProcessLeakDetectorExtension.class.getName());

	private static final String KEY = "processes";

	private final String processName;

	public ProcessLeakDetectorExtension(String processName) {
		this.processName = Objects.requireNonNull(processName, "Process name must not be null");
	}

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		getStore(extensionContext).put(KEY, getProcesses());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void afterEach(ExtensionContext extensionContext) throws Exception {
		Set<String> openedBefore = (Set<String>) getStore(extensionContext).get(KEY, Set.class);
		Set<String> process = getProcesses();
		process.removeAll(openedBefore);
		if (!process.isEmpty()) {
			throw new AssertionError(
					String.format("Expect opened process during test to have been closed (opened process: %s)", process)
			);
		}
	}

	/// Get the internal store from the test context.
	///
	/// @param context The test context.
	/// @return The internal store.
	private static Store getStore(ExtensionContext context) {
		return context.getStore(NAMESPACE);
	}

	private Set<String> getProcesses() throws Exception {
		final String command;

		if (IS_WINDOWS) {
			command = System.getenv("windir") + "\\system32\\" + "tasklist.exe";
		}
		else {
			command = "ps -e";
		}

		Process p = Runtime.getRuntime().exec(command);
		BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;

		Set<String> openedProcesses = new HashSet<>();
		while ((line = input.readLine()) != null) {
			if (line.contains(processName)) {
				openedProcesses.add(line);
			}
		}

		input.close();

		return openedProcesses;
	}
}
