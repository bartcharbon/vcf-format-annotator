package org.molgenis.vcf.annotator;

import htsjdk.variant.variantcontext.*;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.*;
import org.molgenis.vcf.annotator.model.Settings;

import java.util.*;

public class SampleAnnotator {
    public static void annotate(Settings settings){
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
        if(!columns.contains("EnsemblID")){
            columns.add("EnsemblID");
        }
        for(String column: columns){
            header.addMetaDataLine(new VCFFormatHeaderLine(column,VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, String.format("Annotation from %s column of %s", column, settings.getAnnotationFile().getName())));
        }
        vcfWriter.writeHeader(header);
        annotateVariant(settings, reader, vepMapping, mapping, annotations, vcfWriter);
        vcfWriter.close();
    }

    //FIXME: should be per gene not per CSQ
    private static void annotateVariant(Settings settings, VCFReader reader, Map<String, Integer> vepMapping, Map<String, String> mapping, Map<String, Map<String, String>> annotations, VariantContextWriter vcfWriter) {
        for(VariantContext variantContext : reader) {
            VariantContextBuilder vcBuilder = new VariantContextBuilder(variantContext);
            List<String> csqs = variantContext.getAttributeAsStringList("CSQ", "");
            Map<Key, String> variantAnnotations = new HashMap<>();
            for (String csq : csqs) {
                String entrez = csq.split("\\|")[vepMapping.get("Gene")];
                String ensembl = mapping.get(entrez);
                Integer allele_num = Integer.valueOf(csq.split("\\|")[vepMapping.get("ALLELE_NUM")]);
                Map<String, String> gene_annotations = annotations.get(ensembl);
                createAnnotationMap(settings, gene_annotations, variantAnnotations, allele_num);
            }
            List<Genotype> genotypes = new ArrayList<>();
            for (Genotype genotype : variantContext.getGenotypes()) {
                annotateGenotype(variantContext, genotype, variantAnnotations, genotypes);
            }
            vcBuilder.genotypes(genotypes);
            vcfWriter.add(vcBuilder.make());
        }
    }

    private static void createAnnotationMap(Settings settings, Map<String, String> gene_annotations, Map<Key, String> variantAnnotations, Integer allele_num) {
        if (gene_annotations != null) {
            Set<String> columns = new HashSet<>();
            columns.addAll(Set.of(settings.getColumns()));
            if(!columns.contains("EnsemblID")){
                columns.add("EnsemblID");
            }
            for (String column : columns) {
                String value = variantAnnotations.containsKey(new Key(column, allele_num)) ?
                        variantAnnotations.get(new Key(column, allele_num)) + "," + gene_annotations.get(column) :
                        gene_annotations.get(column);
                variantAnnotations.put(new Key(column, allele_num), value);
            }
        }
    }

    private static void annotateGenotype(VariantContext variantContext, Genotype genotype, Map<Key, String> variantAnnotations, List<Genotype> genotypes) {
        List<Allele> alleles = genotype.getAlleles();
        GenotypeBuilder builder = new GenotypeBuilder(genotype);
        for (Key key : variantAnnotations.keySet()) {
            boolean isAnnotate = false;
            for (Allele allele : alleles) {
                if (variantContext.getAlleleIndex(allele) == key.getAlleleNum() - 1) {
                    isAnnotate = true;
                }
            }
            if (isAnnotate) {
                String value = variantAnnotations.get(key);
                if(value != null) {
                    if (genotype.getExtendedAttribute(key.getKey()) != null) {
                        value = genotype.getExtendedAttribute(key.getKey()) + "," + value;
                    } else {
                        value = variantAnnotations.get(key);
                    }
                    builder.attribute(key.getKey(), value);
                }
            }
            genotypes.add(builder.make());
        }
    }

    private static Map<String, Integer> getVepMapping(VCFHeader header) {
        Map<String, Integer> indices = new HashMap<>();
        for(VCFInfoHeaderLine line : header.getInfoHeaderLines()){
            if(line.getID().equals("CSQ")){
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
