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

package com.thebuzzmedia.exiftool.commons.gc;

import org.awaitility.core.ThrowingRunnable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.thebuzzmedia.exiftool.tests.ReflectionTestUtils.isClassAvailable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.Assume.assumeTrue;

public class UnsafeCleanerTest {

	@SuppressWarnings("UnusedAssignment")
	@Test
	public void it_should_create_cleaner_and_register_object() {
		assumeTrue(isClassAvailable("sun.misc.Cleaner"));

		UnsafeCleaner cleaner = UnsafeCleaner.create();
		assertThat(cleaner).isExactlyInstanceOf(UnsafeCleaner.class);

		final CleanupTask cleanupTask = new CleanupTask();
		CleanableClass o = new CleanableClass();
		cleaner.register(o, cleanupTask);

		o = null;
		System.gc();

		await().atMost(5, TimeUnit.SECONDS).pollDelay(1, TimeUnit.SECONDS).untilAsserted(new ThrowingRunnable() {
			@Override
			public void run() {
				assertThat(cleanupTask.runned).isTrue();
			}
		});
	}

	private static class CleanupTask implements Runnable {
		private boolean runned;

		@Override
		public void run() {
			runned = true;
		}
	}

	private static class CleanableClass {
	}
}
