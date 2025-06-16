package org.molgenis.vcf.annotator;

import org.molgenis.vcf.annotator.model.Settings;

import java.io.*;
import java.util.*;

public class TSVReader {
    public static Map<String, Map<String, String>> readTSV(File file, String rowKey){
        Map<String, Map<String, String>> result = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line == null) {
                return Map.of();
            }

            String[] headers = line.split("\t");

            while ((line = br.readLine()) != null) {
                String[] values = line.split("\t", -1);
                Map<String, String> row = new LinkedHashMap<>();

                for (int i = 0; i < headers.length; i++) {
                    String key = headers[i].replace("\"", "");
                    String value = i < values.length ? values[i].replace("\"", "") : "";
                    row.put(key, value);
                }
                result.put(row.get(rowKey).split("\\.")[0],row);//FIXME: configured key + risky split
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return result;
    }

    private static String createKey(Settings settings, Map<String, String> row) {
        //FIXME
        return null;
    }
}