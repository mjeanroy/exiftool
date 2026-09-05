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

package com.thebuzzmedia.exiftool.core;

import com.thebuzzmedia.exiftool.Format;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/// Enum used to define the 2 different output formats that [StandardTag]
/// values can be returned in: numeric or human-readable text.
///
/// ExifTool, via the `-n` command line arg, is capable of returning
/// most values in their raw numeric form (e.g. Aperture="2.8010323841") as well
/// as a more human-readable/friendly format (e.g. Aperture="2.8").
///
/// If the caller finds the human-readable format easier to process,
/// [StandardFormat#HUMAN_READABLE] can be specified when calling
/// [com.thebuzzmedia.exiftool.ExifTool#getImageMeta(File, Format, Collection)]
/// and the returned [String] values processed manually by the caller.
///
/// In order to see the types of values that are returned when
/// [StandardFormat#HUMAN_READABLE] is used, you can check the
/// comprehensive [ExifTool Tag Guide](http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/index.html).
///
/// This makes sense with some values like Aperture that in
/// [StandardFormat#NUMERIC] format end up returning as 14-decimal-place,
/// high precision values that are near the intended value (e.g. "2.79999992203711" instead
/// of just returning "2.8").
///
/// On the other hand, other values (like Orientation) are easier to parse when their numeric value (1-8)
/// is returned instead of a much longer friendly name (e.g. "Mirror horizontal
/// and rotate 270 CW").
///
/// @author Riyad Kalla (software@thebuzzmedia.com)
/// @author Mickael Jeanroy
/// @since 1.1
public enum StandardFormat implements Format {
	/// Numeric Output.
	NUMERIC {
		@Override
		public List<String> getArgs() {
			return singletonList("-n");
		}
	},

	/// Human readable Output.
	HUMAN_READABLE {
		@Override
		public List<String> getArgs() {
			return emptyList();
		}
	}
}
