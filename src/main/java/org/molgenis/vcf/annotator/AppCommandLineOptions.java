package org.molgenis.vcf.annotator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.String.format;

class AppCommandLineOptions {

  static final String OPT_INPUT = "i";
  static final String OPT_INPUT_LONG = "input";
  static final String OPT_MAPPING = "m";
  static final String OPT_MAPPING_LONG = "mapping";
  static final String OPT_ANNOTATION = "a";
  static final String OPT_ANNOTATION_LONG = "annotation";
  static final String OPT_COLUMNS = "c";
  static final String OPT_COLUMNS_LONG = "columns";
  static final String OPT_KEY = "k";
  static final String OPT_KEY_LONG = "key";
  static final String OPT_OUTPUT = "o";
  static final String OPT_OUTPUT_LONG = "output";
  static final String OPT_FORCE = "f";
  static final String OPT_FORCE_LONG = "force";
  static final String OPT_PROBANDS = "pb";
  static final String OPT_PROBANDS_LONG = "probands";
  private static final Options APP_OPTIONS;

  static {
    Options appOptions = new Options();
    appOptions.addOption(
        Option.builder(OPT_INPUT)
            .hasArg(true)
            .required()
            .longOpt(OPT_INPUT_LONG)
            .desc("VEP* annotated input VCF file.")
            .build());
    appOptions.addOption(
        Option.builder(OPT_OUTPUT)
            .hasArg(true)
            .longOpt(OPT_OUTPUT_LONG)
            .desc("Output VCF file (.vcf or .vcf.gz).")
            .build());
    appOptions.addOption(
        Option.builder(OPT_FORCE)
            .longOpt(OPT_FORCE_LONG)
            .desc("Override the output file if it already exists.")
            .build());
    appOptions.addOption(
            Option.builder(OPT_COLUMNS)
                    .hasArg(true)
                    .longOpt(OPT_COLUMNS_LONG)
                    .desc("TODO.")
                    .build());
    appOptions.addOption(
            Option.builder(OPT_MAPPING)
                    .hasArg(true)
                    .longOpt(OPT_MAPPING_LONG)
                    .desc("TODO.")
                    .build());
    appOptions.addOption(
            Option.builder(OPT_ANNOTATION)
                    .hasArg(true)
                    .longOpt(OPT_ANNOTATION_LONG)
                    .desc("TODO.")
                    .build());
    appOptions.addOption(
            Option.builder(OPT_KEY)
                    .hasArg(true)
                    .longOpt(OPT_KEY_LONG)
                    .desc("TODO.")
                    .build());
    appOptions.addOption(
        Option.builder(OPT_PROBANDS)
            .hasArg(true)
            .longOpt(OPT_PROBANDS_LONG)
            .desc("Comma-separated list of proband names.")
            .build());
    APP_OPTIONS = appOptions;
  }

  private AppCommandLineOptions() {}

  static Options getAppOptions() {
    return APP_OPTIONS;
  }

  static void validateCommandLine(CommandLine commandLine) {
    validateInput(commandLine);
    validateOutput(commandLine);
  }

  private static void validateInput(CommandLine commandLine) {
    Path inputPath = Path.of(commandLine.getOptionValue(OPT_INPUT));
    if (!Files.exists(inputPath)) {
      throw new IllegalArgumentException(
          format("Input file '%s' does not exist.", inputPath));
    }
    if (Files.isDirectory(inputPath)) {
      throw new IllegalArgumentException(
          format("Input file '%s' is a directory.", inputPath));
    }
    if (!Files.isReadable(inputPath)) {
      throw new IllegalArgumentException(
          format("Input file '%s' is not readable.", inputPath));
    }
    String inputPathStr = inputPath.toString();
    if (!inputPathStr.endsWith(".vcf") && !inputPathStr.endsWith(".vcf.gz")) {
      throw new IllegalArgumentException(
          format("Input file '%s' is not a .vcf or .vcf.gz file.", inputPathStr));
    }
  }

  private static void validateOutput(CommandLine commandLine) {
    if (!commandLine.hasOption(OPT_OUTPUT)) {
      return;
    }

    Path outputPath = Path.of(commandLine.getOptionValue(OPT_OUTPUT));

    if (!commandLine.hasOption(OPT_FORCE) && Files.exists(outputPath)) {
      throw new IllegalArgumentException(
          format("Output file '%s' already exists", outputPath));
    }
  }
}
