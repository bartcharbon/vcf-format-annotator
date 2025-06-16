package org.molgenis.vcf.annotator;

import org.apache.commons.cli.*;
import org.molgenis.vcf.annotator.model.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Locale.Category;

import static java.util.Objects.requireNonNull;

@Component
class AppCommandLineRunner implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppCommandLineRunner.class);

  private static final int STATUS_MISC_ERROR = 1;
  private static final int STATUS_COMMAND_LINE_USAGE_ERROR = 64;

  private final AppCommandLineToSettingsMapper appCommandLineToSettingsMapper;
  private final CommandLineParser commandLineParser;

  AppCommandLineRunner(
      AppCommandLineToSettingsMapper appCommandLineToSettingsMapper) {
    this.appCommandLineToSettingsMapper = requireNonNull(appCommandLineToSettingsMapper);

    this.commandLineParser = new DefaultParser();
  }

  @Override
  public void run(String... args) {
    // workaround for https://github.com/samtools/htsjdk/issues/1510
    Locale.setDefault(Category.FORMAT, Locale.US);

    try {
      Settings settings = createSettings(args);
      SampleAnnotator.annotate(settings);
    } catch (Exception e) {
      LOGGER.error(e.getLocalizedMessage(), e);
      System.exit(STATUS_MISC_ERROR);
    }
  }

  private Settings createSettings(String... args) {
    CommandLine commandLine = null;
    try {
      commandLine = commandLineParser.parse(AppCommandLineOptions.getAppOptions(), args);
    } catch (ParseException e) {
      logException(e);
      System.exit(STATUS_COMMAND_LINE_USAGE_ERROR);
    }

    AppCommandLineOptions.validateCommandLine(commandLine);
    return appCommandLineToSettingsMapper.map(commandLine, args);
  }

  @SuppressWarnings("java:S106")
  private void logException(ParseException e) {
    LOGGER.error(e.getLocalizedMessage(), e);

    // following information is only logged to system out
    System.out.println();
    HelpFormatter formatter = new HelpFormatter();
    formatter.setOptionComparator(null);
    String cmdLineSyntax = "java -jar sampleAnnotator.jar";
    formatter.printHelp(cmdLineSyntax, AppCommandLineOptions.getAppOptions(), true);
    System.out.println();
  }
}
