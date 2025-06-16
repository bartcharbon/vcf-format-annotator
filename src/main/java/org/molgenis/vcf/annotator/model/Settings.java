package org.molgenis.vcf.annotator.model;

import lombok.Builder;
import lombok.Value;

import java.io.File;

@Value
@Builder
public class Settings {
    File inputPath;
    File outputPath;
    File annotationFile;
    Mode mode;
    String[] columns;
    String keyColumn;
    File mapping;
}
