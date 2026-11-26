package com.thebuzzmedia.exiftool.readme.raw;

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.ExifToolResult;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ExifParser {

	private static final ExifTool exifTool = new ExifToolBuilder()
			.withPoolSize(4)  // Allow 4 process
			.enableStayOpen()
			.build();

	public static String parse(File image) throws IOException {
		// Output of exiftool is returned as is: no option is added by the library.
		ExifToolResult result = exifTool.execute(image, Arrays.asList("-json", "-n"));

		if (!result.getErrors().isEmpty()) {
			System.err.println("ExifTool errors: " + result.getErrors());
		}

		return result.getOutput();
	}

	public static void main(String[] args) throws Exception {
		try {
			for (String image : args) {
				System.out.println(ExifParser.parse(new File(image)));
			}
		} finally {
			exifTool.close();
		}
	}
}
