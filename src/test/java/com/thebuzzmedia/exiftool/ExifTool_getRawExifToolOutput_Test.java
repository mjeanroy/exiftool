package com.thebuzzmedia.exiftool;

import static com.thebuzzmedia.exiftool.tests.MockitoTestUtils.anyListOf;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thebuzzmedia.exiftool.exceptions.UnreadableFileException;
import com.thebuzzmedia.exiftool.process.Command;
import com.thebuzzmedia.exiftool.process.CommandExecutor;
import com.thebuzzmedia.exiftool.process.CommandResult;
import com.thebuzzmedia.exiftool.process.OutputHandler;
import com.thebuzzmedia.exiftool.tests.builders.CommandResultBuilder;
import com.thebuzzmedia.exiftool.tests.builders.FileBuilder;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ExifTool_getRawExifToolOutput_Test {
  private String path;
  private CommandExecutor executor;
  private ExecutionStrategy strategy;
  private List<String> args;

  private ExifTool exifTool;

  @BeforeEach
  void setUp() throws Exception {
    executor = mock(CommandExecutor.class);
    strategy = mock(ExecutionStrategy.class);
    path = "exiftool";
    args = asList("-a", "-u", "-g1", "-j");
    CommandResult cmd = new CommandResultBuilder().output("9.36").build();
    when(executor.execute(any(Command.class))).thenReturn(cmd);
    when(strategy.isSupported(any(Version.class))).thenReturn(true);

    exifTool = new ExifTool(path, executor, strategy);

    reset(executor);
  }

  @Test
  void it_should_fail_if_image_is_null() {
    assertThatThrownBy(() -> exifTool.getRawExifToolOutput(null, args))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Image cannot be null and must be a valid stream of image data.");
  }

  @Test
  void it_should_fail_if_arguments_is_null() {
    assertThatThrownBy(() -> exifTool.getRawExifToolOutput(mock(File.class), null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("Arguments cannot be null.");
  }

  @Test
  void it_should_fail_with_unknown_file() {
    File image = new FileBuilder("foo.png").exists(false).build();
    assertThatThrownBy(() -> exifTool.getRawExifToolOutput(image, args))
        .isInstanceOf(UnreadableFileException.class)
        .hasMessage(
            "Unable to read the given image [/tmp/foo.png], " +
                "ensure that the image exists at the given withPath and that the " +
                "executing Java process has permissions to read it."
        );
  }

  @Test
  @SuppressWarnings("unchecked")
  void it_should_get_raw_image_metadata() throws Exception {
    // Given
    File image = new FileBuilder("foo.png").build();

    String rawOutput = "[{\n"
        + "\"SourceFile\": \"tmp/foo.png\",\n"
        + "\"IFD0\": {\n"
        + "\"Make\": \"HTC\",\n"
        + "\"Model\": \"myTouch 4G\",\n"
        + "\"XResolution\": 72,\n"
        + "\"YResolution\": 72,\n"
        + "\"ResolutionUnit\": \"inches\",\n"
        + "\"YCbCrPositioning\": \"Centered\"\n"
        + "},\n"
        + "}]";

    doAnswer(new ReadRawOutputAnswer(rawOutput, "{ready}")).when(strategy).execute(
        same(executor), same(path), anyListOf(String.class), any(OutputHandler.class)
    );

    // When
    String result = exifTool.getRawExifToolOutput(image, args);

    // Then
    ArgumentCaptor<List<String>> argsCaptor = ArgumentCaptor.forClass(List.class);
    verify(strategy).execute(same(executor), same(path), argsCaptor.capture(), any(OutputHandler.class));

    List<String> arguments = argsCaptor.getValue();
    assertThat(arguments).isNotEmpty().containsExactly(
        "-a",
        "-u",
        "-g1",
        "-j",
        "/tmp/foo.png",
        "-execute"
    );
    assertThat(result).isEqualTo("hello");
  }

  private static final class ReadRawOutputAnswer implements Answer<Void> {
    private final String rawOutput;

    private final String end;

    private ReadRawOutputAnswer(String rawOutput, String end) {
      this.rawOutput = rawOutput;
      this.end = end;
    }

    @Override
    public Void answer(InvocationOnMock invocation) {
      OutputHandler handler = (OutputHandler) invocation.getArguments()[3];
      String[]lines = rawOutput.split(System.getProperty("line.separator"));
      // read raw output
      for(String tmpLine : lines){
        handler.readLine(tmpLine);
      }

      // Read last line
      handler.readLine(end);

      return null;
    }
  }
}
