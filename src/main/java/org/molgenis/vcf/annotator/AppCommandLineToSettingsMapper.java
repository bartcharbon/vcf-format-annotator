package org.molgenis.vcf.annotator;

import org.apache.commons.cli.CommandLine;
import org.molgenis.vcf.annotator.model.Mode;
import org.molgenis.vcf.annotator.model.Settings;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

import static org.molgenis.vcf.annotator.AppCommandLineOptions.*;

@Component
class AppCommandLineToSettingsMapper {

  Settings map(CommandLine commandLine, String... args) {
    Path inputPath = Path.of(commandLine.getOptionValue(OPT_INPUT));
    Path outputPath = Path.of(commandLine.getOptionValue(OPT_OUTPUT));
    Path mappingPath = Path.of(commandLine.getOptionValue(OPT_MAPPING));
    String[] columns = commandLine.getOptionValue(OPT_COLUMNS).split(",");
    String key = commandLine.getOptionValue(OPT_KEY);
    Path annotationPath = Path.of(commandLine.getOptionValue(OPT_ANNOTATION));
    return Settings.builder()
        .inputPath(inputPath.toFile())
            .mapping(mappingPath.toFile())
            .columns(columns)
            .mode(Mode.ENSEMBL) // FIXME hardcoded
            .annotationFile(annotationPath.toFile())
            .keyColumn(key)
            .outputPath(outputPath.toFile())
        .build();
  }
}
