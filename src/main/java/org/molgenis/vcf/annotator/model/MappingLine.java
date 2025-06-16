package org.molgenis.vcf.annotator.model;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MappingLine {
  @CsvBindByName(
      column = "NCBI gene ID",
      required = true)
  String entrez;

  @CsvBindByName(
      column = "Ensembl gene ID",
      required = true)
  String ensembl;

}
