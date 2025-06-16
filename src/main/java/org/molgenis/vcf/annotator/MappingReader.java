package org.molgenis.vcf.annotator;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import org.molgenis.vcf.annotator.model.MappingLine;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MappingReader {
    public static Map<String, String> readMappingFile(File input) {
        Map<String, String> mapping = new HashMap<>();
        try (Reader reader = Files.newBufferedReader(input.toPath(), UTF_8)) {

            CsvToBean<MappingLine> csvToBean =
                    new CsvToBeanBuilder<MappingLine>(reader)
                            .withSeparator('\t')
                            .withType(MappingLine.class)
                            .withThrowExceptions(false)
                            .build();
            handleCsvParseExceptions(csvToBean.getCapturedExceptions());
            List<MappingLine> lines = csvToBean.parse();
            lines.forEach(line -> mapping.put(line.getEntrez(), line.getEnsembl()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return mapping;
    }

    static void handleCsvParseExceptions(List<CsvException> exceptions) {
        exceptions.forEach(
                csvException -> {
                    // ignore errors parsing trailing comment lines
                    if (!(csvException.getLine()[0].startsWith("#"))) {
                        //FIXME
                        System.out.println(
                                    String.format("%s,%s", csvException.getLineNumber(), csvException.getMessage()));
                        }
                });
    }
}
