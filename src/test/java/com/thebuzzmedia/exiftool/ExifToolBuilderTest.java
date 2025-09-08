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

import com.thebuzzmedia.exiftool.core.schedulers.DefaultScheduler;
import com.thebuzzmedia.exiftool.core.schedulers.NoOpScheduler;
import com.thebuzzmedia.exiftool.core.strategies.DefaultStrategy;
import com.thebuzzmedia.exiftool.core.strategies.PoolStrategy;
import com.thebuzzmedia.exiftool.core.strategies.StayOpenStrategy;
import com.thebuzzmedia.exiftool.process.Command;
import com.thebuzzmedia.exiftool.process.CommandExecutor;
import com.thebuzzmedia.exiftool.process.CommandResult;
import com.thebuzzmedia.exiftool.process.executor.DefaultCommandExecutor;
import com.thebuzzmedia.exiftool.tests.builders.CommandResultBuilder;
import com.thebuzzmedia.exiftool.tests.builders.FileBuilder;
import com.thebuzzmedia.exiftool.tests.junit.SystemPropertyExtension;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.thebuzzmedia.exiftool.core.schedulers.SchedulerDuration.duration;
import static com.thebuzzmedia.exiftool.tests.ReflectionTestUtils.readPrivateField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.collection;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExifToolBuilderTest {

	@RegisterExtension
	public SystemPropertyExtension exifToolProps = new SystemPropertyExtension(
			"exiftool.path",
			"exiftool.processCleanupDelay"
	);

	private CommandExecutor executor;
	private ExecutionStrategy strategy;
	private Scheduler scheduler;
	private String path;
	private String configPath;

	private ExifToolBuilder builder;

	@BeforeEach
	void setUp() throws Exception {
		executor = mock(CommandExecutor.class);
		strategy = mock(ExecutionStrategy.class);
		scheduler = mock(Scheduler.class);

		builder = new ExifToolBuilder();
		path = "/foo";
		configPath = "/bar";

		// Mock ExifTool Version
		CommandResult v9_36 = new CommandResultBuilder()
				.output("9.36")
				.build();

		when(executor.execute(any(Command.class))).thenReturn(v9_36);
		when(strategy.isSupported(any(Version.class))).thenReturn(true);
	}

	@Test
	void it_should_update_path() {
		ExifToolBuilder res = builder.withPath(path);
		assertThat(res).isSameAs(builder);
		assertThat(builder).extracting("path").isEqualTo(path);
	}

	@Test
	void it_should_update_path_with_file() {
		File file = new FileBuilder("/usr/local/exiftool").build();
		ExifToolBuilder res = builder.withPath(file);
		assertThat(res).isSameAs(builder);
		assertThat(builder).extracting("path").isEqualTo(file.getAbsolutePath());
	}

	@Test
	void it_should_update_config_path() {
		ExifToolBuilder res = builder.withConfig(configPath);
		assertThat(res).isSameAs(builder);
		assertThat(builder).extracting("configPath").isEqualTo(configPath);
	}

	@Test
	void it_should_update_config_path_with_file() {
		File file = new FileBuilder("config.config").build();
		ExifToolBuilder res = builder.withConfig(file);
		assertThat(res).isSameAs(builder);
		assertThat(builder).extracting("configPath").isEqualTo(file.getAbsolutePath());
	}

	@Test
	void it_should_update_executor() {
		ExifToolBuilder res = builder.withExecutor(executor);
		assertThat(res).isSameAs(builder);
		assertThat(builder).extracting("executor").isEqualTo(executor);
	}

	@Test
	void it_should_enable_stay_open_feature() {
		assertThat(builder).extracting("stayOpen").isNull();

		ExifToolBuilder r1 = builder.enableStayOpen();
		assertThat(r1).isSameAs(builder);
		assertThat(builder).extracting("stayOpen").isEqualTo(true);
		assertThat(builder).extracting("cleanupDelay").isNull();
	}

	@Test
	void it_should_enable_stay_open_feature_with_delay() {
		assertThat(builder).extracting("stayOpen").isNull();
		assertThat(builder).extracting("cleanupDelay").isNull();

		long delay = 10000;
		ExifToolBuilder r1 = builder.enableStayOpen(delay);

		assertThat(r1).isSameAs(builder);
		assertThat(builder).extracting("stayOpen").isEqualTo(true);
		assertThat(builder).extracting("cleanupDelay").isEqualTo(delay);
	}

	@Test
	void it_should_enable_stay_open_feature_with_scheduler() {
		assertThat(builder).extracting("stayOpen").isNull();
		assertThat(builder).extracting("scheduler").isNull();

		ExifToolBuilder r1 = builder.enableStayOpen(scheduler);

		assertThat(r1).isSameAs(builder);
		assertThat(builder).extracting("stayOpen").isEqualTo(true);
		assertThat(builder).extracting("scheduler").isSameAs(scheduler);
	}

	@Test
	void it_should_override_strategy() {
		assertThat(builder).extracting("strategy").isNull();

		ExifToolBuilder r1 = builder.withStrategy(strategy);

		assertThat(r1).isSameAs(builder);
		assertThat(builder).extracting("strategy").isSameAs(strategy);
	}

	@Test
	void it_should_create_exiftool_with_custom_props() {
		ExifTool exifTool = builder
				.withPath(path)
				.withConfig(configPath)
				.withExecutor(executor)
				.enableStayOpen()
				.build();

		assertThat(exifTool).extracting("path").isEqualTo(path);
		assertThat(exifTool).extracting("executor").isEqualTo(executor);
		assertThat(exifTool).extracting("strategy").isExactlyInstanceOf(StayOpenStrategy.class);
		assertThat(exifTool).extracting("strategy").extracting("configPath").isEqualTo(configPath);
	}

	@Test
	void it_should_create_exiftool_and_get_path_from_system_property() {
		System.setProperty("exiftool.path", path);
		ExifTool exifTool = builder.withExecutor(executor).build();
		assertThat(exifTool).extracting("path").isEqualTo(path);
	}

	@Test
	void it_should_create_exiftool_and_get_path_from_default() {
		ExifTool exifTool = builder.withExecutor(executor).build();
		assertThat(exifTool).extracting("path").isEqualTo("exiftool");
	}

	@Test
	void it_should_create_exiftool_and_get_delay_from_default_property() {
		long delay = 100;
		System.setProperty("exiftool.processCleanupDelay", String.valueOf(delay));

		ExifTool exifTool = builder
				.withExecutor(executor)
				.enableStayOpen()
				.build();

		assertThat(exifTool).extracting("strategy").isExactlyInstanceOf(StayOpenStrategy.class);
		assertThat(exifTool).extracting("strategy.scheduler.executionDelay").isEqualTo(duration(delay, TimeUnit.MILLISECONDS));
	}

	@Test
	void it_should_create_exiftool_and_get_delay_from_default() {
		ExifTool exifTool = builder
				.withExecutor(executor)
				.enableStayOpen()
				.build();

		assertThat(exifTool).extracting("strategy").isExactlyInstanceOf(StayOpenStrategy.class);
		assertThat(exifTool).extracting("strategy.scheduler.executionDelay").isEqualTo(duration(600000L, TimeUnit.MILLISECONDS));
	}

	@Test
	void it_should_create_exiftool_and_use_scheduler() {
		ExifTool exifTool = builder
				.withExecutor(executor)
				.enableStayOpen(scheduler)
				.build();

		assertThat(exifTool).extracting("strategy").isExactlyInstanceOf(StayOpenStrategy.class);
		assertThat(exifTool).extracting("strategy.scheduler").isSameAs(scheduler);
	}

	@Test
	void it_should_not_enable_stay_open_by_default() {
		ExifTool exifTool = builder.withExecutor(executor).build();
		assertThat(exifTool).extracting("strategy").isExactlyInstanceOf(DefaultStrategy.class);
	}

	@Test
	void it_should_create_with_default_executor() {
		ExifTool exifTool = builder.build();
		assertThat(exifTool).extracting("executor").isExactlyInstanceOf(DefaultCommandExecutor.class);
	}

	@Test
	void it_should_create_with_custom_strategy() {
		ExifTool exifTool = builder
				.withExecutor(executor)
				.withStrategy(strategy)
				.build();

		assertThat(exifTool).extracting("strategy").isEqualTo(strategy);
	}

	@Test
	void it_should_create_with_pool_strategy() {
		ExifTool exifTool = builder.withExecutor(executor).withPoolSize(10, 500).build();

		assertThat(exifTool).extracting("strategy").isExactlyInstanceOf(PoolStrategy.class);
		assertThat(exifTool).extracting("strategy.pool").asInstanceOf(collection(ExecutionStrategy.class))
				.hasSize(10)
				.are(new Condition<ExecutionStrategy>() {
					@Override
					public boolean matches(ExecutionStrategy value) {
						return value instanceof StayOpenStrategy;
					}
				})
				.are(new Condition<ExecutionStrategy>() {
					@Override
					public boolean matches(ExecutionStrategy value) {
						StayOpenStrategy stayOpenStrategy = (StayOpenStrategy) value;
						Scheduler scheduler = readPrivateField(stayOpenStrategy, "scheduler");
						return scheduler instanceof DefaultScheduler;
					}
				});
	}

	@Test
	void it_should_not_create_pool_strategy_with_negative_pool() {
		ExifTool exifTool = builder.withExecutor(executor).withPoolSize(0, 0).build();
		assertThat(exifTool).extracting("strategy").isExactlyInstanceOf(DefaultStrategy.class);
	}

	@Test
	void it_should_not_create_pool_strategy_with_negative_pool_without_delay() {
		ExifTool exifTool = builder.withExecutor(executor).withPoolSize(0).build();
		assertThat(exifTool).extracting("strategy").isExactlyInstanceOf(DefaultStrategy.class);
	}

	@Test
	void it_should_create_with_pool_strategy_and_no_op_scheduler() {
		ExifTool exifTool = builder.withExecutor(executor).withPoolSize(10).build();

		assertThat(exifTool).extracting("strategy").isExactlyInstanceOf(PoolStrategy.class);
		assertThat(exifTool).extracting("strategy.pool").asInstanceOf(collection(ExecutionStrategy.class))
				.hasSize(10)
				.are(new Condition<ExecutionStrategy>("Pool should only contains instance of StayOpenStrategy") {
					@Override
					public boolean matches(ExecutionStrategy value) {
						return value instanceof StayOpenStrategy;
					}
				})
				.are(new Condition<ExecutionStrategy>("Pool should only contains instance of NoOpScheduler.") {
					@Override
					public boolean matches(ExecutionStrategy value) {
						StayOpenStrategy stayOpenStrategy = (StayOpenStrategy) value;
						Scheduler scheduler = readPrivateField(stayOpenStrategy, "scheduler");
						return scheduler instanceof NoOpScheduler;
					}
				});
	}
}
