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

package com.thebuzzmedia.exiftool;

import com.thebuzzmedia.exiftool.core.schedulers.DefaultScheduler;
import com.thebuzzmedia.exiftool.core.schedulers.NoOpScheduler;
import com.thebuzzmedia.exiftool.core.strategies.DefaultStrategy;
import com.thebuzzmedia.exiftool.core.strategies.PoolStrategy;
import com.thebuzzmedia.exiftool.core.strategies.StayOpenStrategy;
import com.thebuzzmedia.exiftool.logs.Logger;
import com.thebuzzmedia.exiftool.logs.LoggerFactory;
import com.thebuzzmedia.exiftool.process.CommandExecutor;
import com.thebuzzmedia.exiftool.process.executor.CommandExecutors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.thebuzzmedia.exiftool.core.schedulers.SchedulerDuration.millis;
import static com.thebuzzmedia.exiftool.process.executor.CommandExecutors.newExecutor;

/// Builder for [ExifTool] instance.
///
/// This builder should be used to create instance of [com.thebuzzmedia.exiftool.ExifTool].
///
/// ## Settings
///
/// ### Path
///
/// Set the absolute withPath to the ExifTool executable on the host system running
/// this class as defined by the `exiftool.withPath` system property.
/// If set, value will be used, otherwise, withPath will be read:
/// - From system property. This system property can be set on startup
///   with `-Dexiftool.withPath=/withPath/to/exiftool` or by
///   calling [System#setProperty(String, String)] before
///   this class is loaded.
/// - Default value is `exiftool`. In this case, `exiftool`
///   command must be globally available.
///
/// If ExifTool is on your system withPath and running the command `exiftool`
/// successfully executes it, leaving this value unchanged will work fine on
/// any platform. If the ExifTool executable is named something else or not
/// in the system withPath, then this property will need to be set to point at it
/// before using this class.
///
/// On Windows be sure to double-escape the withPath to the tool,
/// for example: `-Dexiftool.withPath=C:\\\\Tools\\\\exiftool.exe`.
///
/// Default value is `exiftool`.
///
/// Relative withPath values (e.g. `bin/tools/exiftool`) are executed with
/// relation to the base directory the VM process was started in. Essentially
/// the directory that `new File(".").getAbsolutePath()` points at
/// during runtime.
///
/// #### Executor
///
/// Executor is the component responsible for executing command line on the
/// system. Most of the time, the default should be fine, but if you want to tune
/// the used withExecutor, then this property is for you.
///
/// Custom withExecutor must implement [com.thebuzzmedia.exiftool.process.CommandExecutor] interface.
///
/// #### Stay Open Strategy
///
/// ExifTool [8.36](http://u88.n24.queensu.ca/exiftool/forum/index.php/topic,1402.msg12933.html#msg12933)
/// added a new persistent-process feature that allows ExifTool to stay
/// running in a daemon mode and continue accepting commands via a file or stdin.
///
/// This feature is disabled by default.
///
/// **NOTE:** If `stay_open` flag is enabled, then an
/// instance of [com.thebuzzmedia.exiftool.exceptions.UnsupportedFeatureException]
/// may be thrown during ExifTool creation.
///
/// If this exception occurs, then you should probably:
///   - Update your ExifTool version.
///   - Create new ExifTool without this feature.
///
/// **Usage:**
///
/// ```
///     final ExifTool exifTool;
///     try {
///         exifTool = new ExifToolBuilder()
///             .enableStayOpen()
///             .build();
///     }
///     catch (UnsupportedFeatureException ex) {
///         exifTool = new ExifToolBuilder().build();
///     }
/// ```
///
/// #### Custom Strategies
///
/// If default strategies are not enough, you can easily provide your own using
/// the [#withStrategy] method.
///
/// **Usage:**
/// ```
///   ExifTool exifTool = new ExifToolBuilder()
///     .withStrategy(new MyCustomStrategy())
///     .build();
/// ```
public class ExifToolBuilder {

	/// Class Logger.
	private static final Logger log = LoggerFactory.getLogger(ExifToolBuilder.class);

	/// Function to get default path value.
	private static final PathFunction PATH = new PathFunction();

	/// Function to get default cleanup interval.
	private static final DelayFunction DELAY = new DelayFunction();

	/// Function to get default executor environment.
	private static final ExecutorFunction EXECUTOR = new ExecutorFunction();

	/// ExifTool path.
	private String path;

	/// ExifTool executor.
	private CommandExecutor executor;

	/// Check if `stay_open` flag should be enabled.
	private Boolean stayOpen;

	/// Cleanup Delay.
	private Long cleanupDelay;

	/// Custom execution strategy.
	private ExecutionStrategy strategy;

	/// Custom Scheduler.
	private Scheduler scheduler;

	/// Pool size.
	private int poolSize;

	/// Create builder with default settings.
	public ExifToolBuilder() {
	}

	/// Override default path.
	/// Default path is defined by the environment property `exiftool.path` or is
	/// set with `exiftool` otherwise. Setting the path explicitly will disable automatic
	/// lookup.
	///
	/// @param path New path.
	/// @return Current builder.
	public ExifToolBuilder withPath(String path) {
		log.debug("Set path: {}", path);
		this.path = path;
		return this;
	}

	/// Override default path.
	///
	/// Default path is defined by the environment property `exiftool.path` or is
	/// set with `exiftool` otherwise. Setting the path explicitly will disable automatic
	/// lookup.
	///
	/// **Note:** If path is not an executable file, a warning
	/// will be logged but it will not fail.
	///
	/// @param path New path.
	/// @return Current builder.
	public ExifToolBuilder withPath(File path) {
		log.debug("Set path: {}", path);

		if (!path.canExecute()) {
			log.warn("Executable {} is not executable, exiftool may fail later", path);
		}

		this.path = path.getAbsolutePath();
		return this;
	}

	/// Override default exifTool executor.
	///
	/// @param executor New withExecutor.
	/// @return Current builder.
	public ExifToolBuilder withExecutor(CommandExecutor executor) {
		log.debug("Set withExecutor: {}", executor);
		this.executor = executor;
		return this;
	}

	/// Enable `stay_open` feature.
	///
	/// @return Current builder.
	public ExifToolBuilder enableStayOpen() {
		log.debug("Enable 'stay_open' feature");

		// If strategy has already been defined, log a warning.
		if (strategy != null) {
			log.warn("A custom strategy is defined, enabling 'stay_open' feature will be ignored");
		}

		this.stayOpen = true;
		return this;
	}

	/// Enable `stay_open` feature.
	/// **Note:**
	/// - If {link #withStrategy} is called, then calling this method
	///   is useless.
	/// - If {link #enableStayOpen(scheduler} is called, then calling this method is
	///   useless.
	///
	/// @param cleanupDelay Interval (in milliseconds) between automatic clean operation.
	/// @return Current builder.
	public ExifToolBuilder enableStayOpen(long cleanupDelay) {
		log.debug("Enable 'stay_open' feature");

		if (strategy != null) {
			log.warn("A custom strategy is defined, enabling 'stay_open' feature will be ignored");
		}

		if (scheduler != null) {
			log.warn("A custom scheduler is already defined, it will be ignored");
		}

		this.stayOpen = true;
		this.cleanupDelay = cleanupDelay;
		return this;
	}

	/// Enable `stay_open` feature and perform cleanup task using given `scheduler`.
	/// **Note:**
	///
	///   - If [#withStrategy] has already been called, then calling is useless.
	///   - If [#enableStayOpen(long)] has already been called, then given `delay` will be
	///     ignored and the specified scheduler will be used.
	///
	/// @param scheduler Scheduler used to process automatic cleanup task..
	/// @return Current builder.
	public ExifToolBuilder enableStayOpen(Scheduler scheduler) {
		log.debug("Enable 'stay_open' feature");

		if (strategy != null) {
			log.warn("A custom strategy is defined, enabling 'stay_open' feature will be ignored");
		}
		if (cleanupDelay != null) {
			log.warn("Custom scheduler is defined, previous delay will be ignored");
		}

		this.stayOpen = true;
		this.scheduler = scheduler;
		return this;
	}

	/// Override default execution strategy.
	/// **If [#enableStayOpen] has been called, then strategy associated with `stay_open` flag
	/// will be ignored.**
	///
	/// @param strategy Strategy.
	/// @return Current builder.
	public ExifToolBuilder withStrategy(ExecutionStrategy strategy) {
		log.debug("Overriding default strategy");

		if (stayOpen != null) {
			log.warn("Flag 'stay_open' has been enabled and you are overriding the default execution strategy.");
			log.warn("Enabling 'stay_open' feature will be ignored");
		}

		this.strategy = strategy;
		return this;
	}

	/// Override default execution strategy:
	///
	///   - a pool of [StayOpenStrategy] with a size of `poolSize` will be used.
	///   - Default scheduler instances will be used with a delay of `cleanupDelay`.
	///
	/// @param poolSize Pool size.
	/// @param cleanupDelay Cleanup delay for each scheduler of pool elements.
	/// @return Current builder.
	public ExifToolBuilder withPoolSize(int poolSize, long cleanupDelay) {
		log.debug("Overriding default strategy");

		if (poolSize > 0) {
			this.poolSize = poolSize;
			this.cleanupDelay = cleanupDelay;
		}
		else {
			log.warn("Pool size has been enabled with a value less or equal than zero, ignore it.");
		}

		return this;
	}

	/// Override default execution strategy:
	///
	///   - a pool of [StayOpenStrategy] with a size of `poolSize` will be used.
	///   - No cleanup scheduler will be used (use [#withPoolSize(int, long)] instead.
	///
	/// @param poolSize Pool size.
	/// @return Current builder.
	public ExifToolBuilder withPoolSize(int poolSize) {
		log.debug("Overriding default strategy");

		if (poolSize > 0) {
			this.poolSize = poolSize;
			this.cleanupDelay = 0L;
		}
		else {
			log.warn("Pool size has been enabled with a value less or equal than zero, ignore it.");
		}

		return this;
	}

	/// Create exiftool instance with previous settings.
	///
	/// @return Exiftool instance.
	public ExifTool build() {
		String path = firstNonNull(this.path, PATH);
		CommandExecutor executor = firstNonNull(this.executor, EXECUTOR);
		ExecutionStrategy strategy = firstNonNull(this.strategy, new StrategyFunction(stayOpen, cleanupDelay, scheduler, poolSize));

		// Add some debugging information
		if (log.isDebugEnabled()) {
			log.debug("Create ExifTool instance:");
			log.debug(" - Path: {}", path);
			log.debug(" - Executor: {}", executor);
			log.debug(" - Strategy: {}", strategy);
			log.debug(" - StayOpen: {}", stayOpen);
		}

		return new ExifTool(path, executor, strategy);
	}

	/// Return first non null value:
	///
	///   - If first parameter is not null, then it is returned.
	///   - Otherwise, result of function is returned.
	///
	/// @param value First value.
	/// @param factory Function used to get non null value.
	/// @param <T> Type of values.
	/// @return Non null value.
	private static <T> T firstNonNull(T value, FactoryFunction<T> factory) {
		return value == null ? factory.apply() : value;
	}

	/// Interface to return values.
	/// This interface should be used by builder to lazily create
	/// default settings parameters.
	///
	/// @param <T> Type of settings.
	private interface FactoryFunction<T> {
		T apply();
	}

	/// Return the absolute path to the ExifTool executable on the host system running
	/// this class as defined by the `exiftool.path` system property.
	///
	/// This system property can be set on startup with `-Dexiftool.path=/path/to/exiftool`
	/// or by calling [System#setProperty(String, String)] before
	/// this class is loaded.
	///
	/// On Windows be sure to double-escape the path to the tool,
	/// for example: `-Dexiftool.path=C:\\\\Tools\\\\exiftool.exe`.
	/// Default value is `exiftool`.
	private static class PathFunction implements FactoryFunction<String> {
		@Override
		public String apply() {
			return System.getProperty("exiftool.path", "exiftool");
		}
	}

	/// Return the interval (in milliseconds) of inactivity before the cleanup thread wakes
	/// up and cleans up the daemon ExifTool process and the read/write streams
	/// used to communicate with it when the `stay_open` feature is
	/// used.
	///
	/// Ever time a call to [ExifTool#getImageMeta(File)] is processed, the timer
	/// keeping track of cleanup is reset; more specifically, this class has to
	/// experience no activity for this duration of time before the cleanup
	/// process is fired up and cleans up the host OS process and the stream
	/// resources.
	///
	/// Any subsequent calls to [ExifTool#getImageMeta(File)] after a cleanup simply
	/// re-initializes the resources.
	///
	/// This system property can be set on startup with `-Dexiftool.processCleanupDelay=600000`
	/// or by calling [System#setProperty(String, String)] before
	/// this class is loaded.
	///
	/// Setting this value to 0 disables the automatic cleanup thread completely
	/// and the caller will need to manually cleanup the external ExifTool
	/// process and read/write streams by calling [ExifTool#close] method.
	/// Default value is `600, 000` (10 minutes).
	private static class DelayFunction implements FactoryFunction<Long> {
		@Override
		public Long apply() {
			return Long.getLong("exiftool.processCleanupDelay", 600000);
		}
	}

	/// Returns the default executor for the created exifTool instance.
	/// Default executor is the result of [CommandExecutors#newExecutor()] method.
	private static class ExecutorFunction implements FactoryFunction<CommandExecutor> {
		@Override
		public CommandExecutor apply() {
			return newExecutor();
		}
	}

	/// Create scheduler used to perform automatic cleanup task.
	/// Default scheduler will depend on the given `delay`:
	///
	///   - If `delay` is less than or equal to zero, then an instance of [NoOpScheduler] will be returned.
	///   - If `delay` is greater than zero, then an instance of [DefaultScheduler] will be returned.
	private static class SchedulerFunction implements FactoryFunction<Scheduler> {
		private final Long delay;

		public SchedulerFunction(Long delay) {
			this.delay = delay;
		}

		@Override
		public Scheduler apply() {
			// Otherwise, this is the StayOpen strategy.
			// We have to look up the delay between automatic clean and create
			// the scheduler.
			final long delay = firstNonNull(this.delay, DELAY);
			return delay > 0 ? new DefaultScheduler(millis(delay)) : new NoOpScheduler();
		}
	}

	/// Returns the [ExecutionStrategy] to use with [ExifTool] instances.
	///
	/// ### Default
	///
	/// By default, a really simple strategy is used (instance of [DefaultStrategy]).
	///
	/// ### StayOpen
	///
	/// If the `stay_open` has been enabled, then an instance of [StayOpenStrategy]
	/// will be created. For this strategy, a scheduler will be created. This scheduler will be used to run
	/// a task to clean resources used by this strategy. This task will run automatically after a specified
	/// delay.
	private static class StrategyFunction implements FactoryFunction<ExecutionStrategy> {
		private final Boolean stayOpen;

		private final Long delay;

		private final Scheduler scheduler;

		private final int poolSize;

		public StrategyFunction(Boolean stayOpen, Long delay, Scheduler scheduler, int poolSize) {
			this.stayOpen = stayOpen;
			this.delay = delay;
			this.scheduler = scheduler;
			this.poolSize = poolSize;
		}

		@Override
		public ExecutionStrategy apply() {
			// First, try the pool strategy.
			if (poolSize > 0) {
				List<ExecutionStrategy> strategies = new ArrayList<>(poolSize);
				for (int i = 0; i < poolSize; i++) {
					Scheduler scheduler = new SchedulerFunction(delay).apply();
					StayOpenStrategy strategy = new StayOpenStrategy(scheduler);
					strategies.add(strategy);
				}

				return new PoolStrategy(strategies);
			}

			// Try the stayOpen strategy.
			if (stayOpen != null && stayOpen) {
				return new StayOpenStrategy(firstNonNull(scheduler, new SchedulerFunction(delay)));
			}

			// Simple use case: nothing has been parametrized, so
			// just return the default strategy.
			return new DefaultStrategy();
		}
	}
}
