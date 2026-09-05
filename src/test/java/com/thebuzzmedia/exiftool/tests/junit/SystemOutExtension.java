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
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/// Catch System.out logging and store in a buffer.
public class SystemOutExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

	/// The namespace in which extension data will be stored.
	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ProcessLeakDetectorExtension.class.getName());

	private static final String ORIGINAL_OUT_KEY = "original_out";
	private static final String COMPOSITE_OUT_KEY = "composite_out";

	@Override
	public void beforeEach(ExtensionContext context) {
		PrintStream originalOut = System.out;
		CompositeOutputStream compositeOut = new CompositeOutputStream(
				new ByteArrayOutputStream()
		);

		System.setOut(new PrintStream(compositeOut));

		getStore(context).put(ORIGINAL_OUT_KEY, originalOut);
		getStore(context).put(COMPOSITE_OUT_KEY, compositeOut);
	}

	@Override
	public void afterEach(ExtensionContext context) {
		PrintStream originalOut = getStore(context).remove(ORIGINAL_OUT_KEY, PrintStream.class);
		CompositeOutputStream compositeOut = getStore(context).remove(COMPOSITE_OUT_KEY, CompositeOutputStream.class);

		try {
			compositeOut.flush();
		}
		catch (IOException ex) {
			// No worries
		}

		try {
			compositeOut.close();
		}
		catch (IOException ex) {
			// No worries
		}

		// Restore original out stream
		System.setOut(originalOut);
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		return parameterContext.getParameter().getType() == SystemOut.class;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		CompositeOutputStream out = getStore(extensionContext).get(COMPOSITE_OUT_KEY, CompositeOutputStream.class);
		return new SystemOut(out);
	}

	public static final class SystemOut {
		private final CompositeOutputStream out;

		private SystemOut(CompositeOutputStream out) {
			this.out = out;
		}

		public String getOut() {
			return out == null ? null : out.sb.toString();
		}

		public String getTrimmedOut() {
			return out == null ? null : out.sb.toString().trim();
		}
	}

	private static final class CompositeOutputStream extends OutputStream implements ExtensionContext.Store.CloseableResource {
		private final OutputStream o1;
		private final StringBuilder sb;

		private CompositeOutputStream(OutputStream o1) {
			this.o1 = o1;
			this.sb = new StringBuilder();
		}

		@Override
		public void write(int b) throws IOException {
			o1.write(b);
			sb.append((char) b);
		}

		@Override
		public void flush() throws IOException {
			super.flush();
			o1.flush();
		}

		@Override
		public void close() throws IOException {
			super.close();
			o1.close();
			sb.delete(0, sb.length());
		}
	}

	/// Get the internal store from the test context.
	///
	/// @param context The test context.
	/// @return The internal store.
	private static ExtensionContext.Store getStore(ExtensionContext context) {
		return context.getStore(NAMESPACE);
	}
}
