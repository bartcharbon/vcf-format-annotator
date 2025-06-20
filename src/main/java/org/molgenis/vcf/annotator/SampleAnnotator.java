package org.molgenis.vcf.annotator;

import htsjdk.variant.variantcontext.*;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.*;
import org.molgenis.vcf.annotator.model.Settings;

import java.util.*;

public class SampleAnnotator {
    public static void annotate(Settings settings) {
        Map<String, String> mapping = MappingReader.readMappingFile(settings.getMapping());
        VCFReader reader = new VCFFileReader(settings.getInputPath(), false);
        Map<String, Integer> vepMapping = getVepMapping(reader.getHeader());
        Map<String, Map<String, String>> annotations = TSVReader.readTSV(settings.getAnnotationFile(), settings.getKeyColumn());
        VariantContextWriter vcfWriter = new VariantContextWriterBuilder()
                .clearOptions()
                .setOutputFile(settings.getOutputPath())
                .build();
        VCFHeader header = reader.getHeader();
        Set<String> columns = new HashSet<>();
        columns.addAll(Set.of(settings.getColumns()));
        columns.add("vipGene");
        for (String column : columns) {
            header.addMetaDataLine(new VCFFormatHeaderLine(column, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, String.format("Annotation from %s column of %s", column, settings.getAnnotationFile().getName())));
        }
        vcfWriter.writeHeader(header);
        annotateVariant(settings, reader, vepMapping, mapping, annotations, vcfWriter);
        vcfWriter.close();
    }

    private static void annotateVariant(Settings settings, VCFReader reader, Map<String, Integer> vepMapping, Map<String, String> mapping, Map<String, Map<String, String>> annotations, VariantContextWriter vcfWriter) {
        for (VariantContext variantContext : reader) {
            VariantContextBuilder vcBuilder = new VariantContextBuilder(variantContext);
            List<String> csqs = variantContext.getAttributeAsStringList("CSQ", "");
            Map<Genotype, Set<String>> genotypeGenes = new HashMap<>();
            for (String csq : csqs) {
                Integer allele_num = Integer.valueOf(csq.split("\\|")[vepMapping.get("ALLELE_NUM")]);
                String entrez = csq.split("\\|")[vepMapping.get("Gene")];
                for (Genotype genotype : variantContext.getGenotypes()) {
                    Set<String> genes = new HashSet<>();
                    for (Allele allele : genotype.getAlleles()) {
                        if (variantContext.getAlleleIndex(allele) == allele_num) {
                            if (genotypeGenes.containsKey(genotype)) {
                                genes.addAll(genotypeGenes.get(genotype));
                            }
                            if(!entrez.isEmpty()) {
                                genes.add(entrez);
                            }
                        }
                    }
                    genotypeGenes.put(genotype, genes);
                }
            }
            List<Genotype> genotypes = new ArrayList<>();
            for (Genotype genotype : variantContext.getGenotypes()) {
                if (genotypeGenes.containsKey(genotype)) {
                    GenotypeBuilder builder = new GenotypeBuilder(genotype);
                    builder.attribute("vipGene", String.join(",", genotypeGenes.get(genotype)));
                    for (String entrez : genotypeGenes.get(genotype)) {
                        String ensembl = mapping.get(entrez);
                        Map<String, String> geneAnnotations = annotations.get(ensembl);
                        if(geneAnnotations != null) {
                            for (String column : settings.getColumns()) {
                                builder.attribute(column, geneAnnotations.get(column));
                            }
                        }
                    }
                    genotypes.add(builder.make());
                } else {
                    genotypes.add(genotype);
                }
            }
            vcBuilder.genotypes(genotypes);
            vcfWriter.add(vcBuilder.make());
        }
    }

    private static Map<String, Integer> getVepMapping(VCFHeader header) {
        Map<String, Integer> indices = new HashMap<>();
        for (VCFInfoHeaderLine line : header.getInfoHeaderLines()) {
            if (line.getID().equals("CSQ")) {
                String desc = line.getDescription();
                int startIdx = desc.indexOf("Format:") + 7;
                String formatPart = desc.substring(startIdx);
                String[] fields = formatPart.split("\\|");
                for (int i = 0; i < fields.length; i++) {
                    indices.put(fields[i], i);
                }
            }
        }
        return indices;
    }
}
