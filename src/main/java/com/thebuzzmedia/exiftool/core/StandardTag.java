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

import com.thebuzzmedia.exiftool.Constants;
import com.thebuzzmedia.exiftool.Tag;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Enum used to pre-define a convenient list of tags that can be easily
 * extracted from images using this class with an external install of
 * ExifTool.
 * <p>
 * Each tag defined also includes a type hint for the parsed value
 * associated with it when the default {@link com.thebuzzmedia.exiftool.core.StandardFormat#NUMERIC}
 * value format is used.
 *
 * <p>
 *
 * The types provided by each tag are merely a hint based on the
 * <a href="https://exiftool.org/TagNames/index.html">ExifTool Tag Guide</a>
 * by Phil Harvey; the caller is free to parse or process the returned {@link String} values any way they wish.
 * <p>
 * This list was determined by looking at the common metadata tag values
 * written to images by popular mobile devices (iPhone, Android) as well as
 * cameras like simple point and shoots as well as DSLRs. As an additional
 * source of input the list of supported/common EXIF formats that Flickr
 * supports was also reviewed to ensure the most common/useful tags were
 * being covered here.
 *
 * <br>
 *
 * Please email me or file an issue if you think this list is missing a commonly
 * used tag that should be added to it.
 *
 * @author Riyad Kalla (software@thebuzzmedia.com)
 * @author Mickael Jeanroy
 * @since 1.1
 */
public enum StandardTag implements Tag {

	/**
	 * ISO tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	ISO("ISO", Type.INTEGER),

	/**
	 * ApertureValue tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 */
	APERTURE("ApertureValue", Type.DOUBLE),

	/**
	 * WhiteBalance tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	WHITE_BALANCE("WhiteBalance", Type.INTEGER),

	/**
	 * BrightnessValue tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 */
	BRIGHTNESS("BrightnessValue", Type.DOUBLE),

	/**
	 * Contrast tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	CONTRAST("Contrast", Type.INTEGER),

	/**
	 * Saturation tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	SATURATION("Saturation", Type.INTEGER),

	/**
	 * Sharpness tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	SHARPNESS("Sharpness", Type.INTEGER),

	/**
	 * ShutterSpeedValue tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 */
	SHUTTER_SPEED("ShutterSpeedValue", Type.DOUBLE),

	/**
	 * DigitalZoomRatio tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 */
	DIGITAL_ZOOM_RATIO("DigitalZoomRatio", Type.DOUBLE),

	/**
	 * ImageWidth tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/JPEG.html">JPEG EXIF Tags</a>
	 */
	IMAGE_WIDTH("ImageWidth", Type.INTEGER),

	/**
	 * ImageHeight tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/JPEG.html">JPEG EXIF Tags</a>
	 */
	IMAGE_HEIGHT("ImageHeight", Type.INTEGER),

	/**
	 * XResolution tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/JPEG.html">JPEG EXIF Tags</a>
	 */
	X_RESOLUTION("XResolution", Type.DOUBLE),

	/**
	 * YResolution tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/JPEG.html">JPEG EXIF Tags</a>
	 */
	Y_RESOLUTION("YResolution", Type.DOUBLE),

	/**
	 * Flash tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 */
	FLASH("Flash", Type.INTEGER),

	/**
	 * MeteringMode tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	METERING_MODE("MeteringMode", Type.INTEGER),

	/**
	 * FNumber tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	FNUMBER("FNumber", Type.DOUBLE),

	/**
	 * FocalLength tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	FOCAL_LENGTH("FocalLength", Type.DOUBLE),

	/**
	 * FocalLengthIn35mmFormat tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 */
	FOCAL_LENGTH_35MM("FocalLengthIn35mmFormat", Type.INTEGER),

	/**
	 * ExposureTime tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	EXPOSURE_TIME("ExposureTime", Type.DOUBLE),

	/**
	 * ExposureCompensation tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	EXPOSURE_COMPENSATION("ExposureCompensation", Type.DOUBLE),

	/**
	 * ExposureProgram tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	EXPOSURE_PROGRAM("ExposureProgram", Type.INTEGER),

	/**
	 * Orientation tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	ORIENTATION("Orientation", Type.INTEGER),

	/**
	 * ColorSpace tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/JPEG.html">JPEG EXIF Tags</a>
	 */
	COLOR_SPACE("ColorSpace", Type.INTEGER),

	/**
	 * SensingMethod tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 */
	SENSING_METHOD("SensingMethod", Type.INTEGER),

	/**
	 * Software tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	SOFTWARE("Software", Type.STRING),

	/**
	 * Make tag.
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	MAKE("Make", Type.STRING),

	/**
	 * Model tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	MODEL("Model", Type.STRING),

	/**
	 * LensMake tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	LENS_MAKE("LensMake", Type.STRING),

	/**
	 * LensModel tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	LENS_MODEL("LensModel", Type.STRING),

	/**
	 * OwnerName tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 */
	OWNER_NAME("OwnerName", Type.STRING),

	/**
	 * XPTitle tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 */
	TITLE("XPTitle", Type.STRING),

	/**
	 * XPAuthor tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 */
	AUTHOR("XPAuthor", Type.STRING),

	/**
	 * XPSubject tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 */
	SUBJECT("XPSubject", Type.STRING),

	/**
	 * XPKeywords tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 */
	KEYWORDS("XPKeywords", Type.STRING),

	/**
	 * XPComment tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 */
	COMMENT("XPComment", Type.STRING),

	/**
	 * Rating tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	RATING("Rating", Type.INTEGER),

	/**
	 * RatingPercent tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 */
	RATING_PERCENT("RatingPercent", Type.INTEGER),

	/**
	 * DateTimeOriginal tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	DATE_TIME_ORIGINAL("DateTimeOriginal", Type.STRING),

	/**
	 * GPSLatitude tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/GPS.html">GPS EXIF Tags</a>
	 */
	GPS_LATITUDE("GPSLatitude", Type.DOUBLE),

	/**
	 * GPSLatitudeRef tag.
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/GPS.html">GPS EXIF Tags</a>
	 */
	GPS_LATITUDE_REF("GPSLatitudeRef", Type.STRING),

	/**
	 * GPSLongitude tag.
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/GPS.html">GPS EXIF Tags</a>
	 */
	GPS_LONGITUDE("GPSLongitude", Type.DOUBLE),

	/**
	 * GPSLongitudeRef tag.
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	GPS_LONGITUDE_REF("GPSLongitudeRef", Type.STRING),

	/**
	 * GPSAltitude tag.
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/GPS.html">GPS EXIF Tags</a>
	 */
	GPS_ALTITUDE("GPSAltitude", Type.DOUBLE),

	/**
	 * GPSAltitudeRef tag.
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/GPS.html">GPS EXIF Tags</a>
	 */
	GPS_ALTITUDE_REF("GPSAltitudeRef", Type.INTEGER),

	/**
	 * GPSSpeed tag.
	 * @see <a href="https://exiftool.org/TagNames/GPS.html">GPS EXIF Tags</a>
	 */
	GPS_SPEED("GPSSpeed", Type.DOUBLE),

	/**
	 * GPSSpeedRef tag.
	 * @see <a href="https://exiftool.org/TagNames/GPS.html">GPS EXIF Tags</a>
	 */
	GPS_SPEED_REF("GPSSpeedRef", Type.STRING),

	/**
	 * GPSProcessingMethod tag.
	 * @see <a href="https://exiftool.org/TagNames/GPS.html">GPS EXIF Tags</a>
	 */
	GPS_PROCESS_METHOD("GPSProcessingMethod", Type.STRING),

	/**
	 * GPSDestBearing tag.
	 * @see <a href="https://exiftool.org/TagNames/GPS.html">GPS EXIF Tags</a>
	 */
	GPS_BEARING("GPSDestBearing", Type.DOUBLE),

	/**
	 * GPSDestBearingRef tag.
	 * @see <a href="https://exiftool.org/TagNames/GPS.html">GPS EXIF Tags</a>
	 */
	GPS_BEARING_REF("GPSDestBearingRef", Type.STRING),

	/**
	 * GPSTimeStamp tag.
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/GPS.html">GPS EXIF Tags</a>
	 */
	GPS_TIMESTAMP("GPSTimeStamp", Type.STRING),

	/**
	 * Rotation tag.
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	ROTATION("Rotation", Type.INTEGER),

	/**
	 * ExifVersion tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 */
	EXIF_VERSION("ExifVersion", Type.STRING),

	/**
	 * LensID tag.
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Composite.html">Composite EXIF Tags</a>
	 */
	LENS_ID("LensID", Type.STRING),

	/**
	 * Copyright tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 */
	COPYRIGHT("Copyright", Type.STRING),

	/**
	 * Artist tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 */
	ARTIST("Artist", Type.STRING),

	/**
	 * SubSecTimeOriginal tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 */
	SUB_SEC_TIME_ORIGINAL("SubSecTimeOriginal", Type.INTEGER),

	/**
	 * ObjectName tag.
	 * @see <a href="https://exiftool.org/TagNames/IPTC.html">IPTC EXIF Tags</a>
	 */
	OBJECT_NAME("ObjectName", Type.STRING),

	/**
	 * Caption-Abstract tag.
	 * @see <a href="https://exiftool.org/TagNames/IPTC.html">IPTC EXIF Tags</a>
	 */
	CAPTION_ABSTRACT("Caption-Abstract", Type.STRING),

	/**
	 * Creator tag.
	 */
	CREATOR("Creator", Type.STRING),

	/**
	 * Keywords tag.
	 * @see <a href="https://exiftool.org/TagNames/JPEG.html">JPEG EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/IPTC.html">IPTC EXIF Tags</a>
	 */
	IPTC_KEYWORDS("Keywords", Type.ARRAY),

	/**
	 * CopyrightNotice tag.
	 * @see <a href="https://exiftool.org/TagNames/IPTC.html">IPTC EXIF Tags</a>
	 */
	COPYRIGHT_NOTICE("CopyrightNotice", Type.STRING),

	/**
	 * FileType tag.
	 */
	FILE_TYPE("FileType", Type.STRING),

	/**
	 * FileSize tag.
	 */
	FILE_SIZE("FileSize", Type.LONG),

	/**
	 * AvgBitrate tag.
	 * @see <a href="https://exiftool.org/TagNames/Composite.html">Composite EXIF Tags</a>
	 */
	AVG_BITRATE("AvgBitrate", Type.STRING),

	/**
	 * MIMEType tag.
	 */
	MIME_TYPE("MIMEType", Type.STRING),

	/**
	 * CreateDate tag.
	 * @see <a href="https://exiftool.org/TagNames/EXIF.html">EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	CREATE_DATE("CreateDate", Type.STRING),

	/**
	 * AFAperture tag.
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	AF_APERTURE("AFAperture", Type.DOUBLE),

	/**
	 * ExposureDifference tag.
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	EXPOSURE_DIFFERENCE("ExposureDifference", Type.STRING),

	/**
	 * FocusDistance tag.
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	FOCUS_DISTANCE("FocusDistance", Type.STRING),

	/**
	 * FocusMode tag.
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	FOCUS_MODE("FocusMode", Type.STRING),

	/**
	 * FocusPosition tag.
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	FOCUS_POSITION("FocusPosition", Type.STRING),

	/**
	 * ImageDataSize tag.
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	IMAGE_DATA_SIZE("ImageDataSize", Type.LONG),

	/**
	 * LensFStops tag.
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	LENS_F_STOPS("LensFStops", Type.STRING),

	/**
	 * LensSpec tag.
	 * @see <a href="https://exiftool.org/TagNames/Composite.html">Composite EXIF Tags</a>
	 */
	LENS_SPEC("LensSpec", Type.STRING),

	/**
	 * Megapixels tag.
	 * @see <a href="https://exiftool.org/TagNames/Composite.html">Composite EXIF Tags</a>
	 */
	MEGA_PIXELS("Megapixels", Type.DOUBLE),

	/**
	 * Quality tag.
	 * @see <a href="https://exiftool.org/TagNames/Canon.html">Canon EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/JPEG.html">JPEG EXIF Tags</a>
	 * @see <a href="https://exiftool.org/TagNames/Nikon.html">Nikon EXIF Tags</a>
	 */
	QUALITY("Quality", Type.STRING),

	/**
	 * CreationDate tag.
	 */
	CREATION_DATE("CreationDate", Type.STRING);

	/**
	 * Used to get the name of the tag (e.g. "Orientation", "ISO", etc.).
	 */
	private final String name;

	/**
	 * Used to get a hint for the native type of this tag's value as
	 * specified by Phil Harvey's <a href="http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/index.html">ExifTool Tag Guide</a>.
	 */
	private final Type type;

	StandardTag(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDisplayName() {
		return name;
	}

	@Override
	public <T> T parse(String value) {
		return type.parse(value);
	}

	@SuppressWarnings("unchecked")
	private enum Type {
		INTEGER {
			@Override
			public <T> T parse(String value) {
				return (T) Integer.valueOf(Integer.parseInt(value));
			}
		},

		LONG {
			@Override
			public <T> T parse(String value) {
				return (T) Long.valueOf(Long.parseLong(value));
			}
		},

		DOUBLE {
			@Override
			public <T> T parse(String value) {
				if (Objects.equals("inf", value)) {
					return (T) Double.valueOf(Double.POSITIVE_INFINITY);
				}

				if (Objects.equals(value, "undef")) {
					return (T) Double.valueOf(Double.NaN);
				}

				return (T) Double.valueOf(Double.parseDouble(value));
			}
		},

		STRING {
			@Override
			public <T> T parse(String value) {
				return (T) value;
			}
		},

		ARRAY {
			@Override
			public <T> T parse(String value) {
				return (T) value.split(Pattern.quote(Constants.SEPARATOR));
			}
		};

		public abstract <T> T parse(String value);
	}
}
