package com.thebuzzmedia.exiftool.core.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class RawOutputHandlerTest {

	@Test
	void it_should_read_null_line() {
		RawOutputHandler handler = new RawOutputHandler();
		boolean hasNext = handler.readLine(null);
		assertThat(hasNext).isFalse();
		assertThat(handler.getOutput()).isNotNull().isEmpty();
	}

	@Test
	void it_should_read_last_line() {
		RawOutputHandler handler = new RawOutputHandler();
		boolean hasNext = handler.readLine("{ready}");
		assertThat(hasNext).isFalse();
		assertThat(handler.getOutput()).isNotNull().isEmpty();
	}

	@Test
	void it_should_read_line() {
		String rawOutput = "[{\"SourceFile\": \"tmp/foo.jpg\",\"IFD0\": {\"Make\": \"HTC\","
			+ "\"Model\": \"myTouch 4G\", \"XResolution\": 72, \"YResolution\": 72,"
			+ "\"ResolutionUnit\": \"inches\", \"YCbCrPositioning\": \"Centered\"},}]";

	RawOutputHandler handler = new RawOutputHandler();
	boolean hasNext = handler.readLine(rawOutput);

		String result = handler.getOutput();
		assertThat(hasNext).isTrue();
		assertThat(result).hasSize(rawOutput.length());

		assertThat(result).isEqualTo(rawOutput);
	}
}
