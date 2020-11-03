[![Build Status](https://travis-ci.org/molgenis/vip-report.svg?branch=master)](https://travis-ci.org/molgenis/vip-report)
[![Quality Status](https://sonarcloud.io/api/project_badges/measure?project=molgenis_vip-report&metric=alert_status)](https://sonarcloud.io/dashboard?id=molgenis_vip-report)
# Variant Interpretation Pipeline - VCF Report Generator
Command-line application to generate a report for any VCF (Variant Call Format) file based on a report template.
## Requirements
- Java 11

## Usage
```
java -jar vcf-report.jar -i <arg> [-o <arg>] [-f]
       [-t <arg>] [-pb <arg>] [-pd <arg>] [-ph <arg>] [-d]
 -i,--input <arg>         Input VCF file (.vcf or .vcf.gz).
 -o,--output <arg>        Output report file (.html).
 -f,--force               Override the output file if it already exists.
 -t,--template <arg>      Report template file (.html).
 -pb,--probands <arg>     Comma-separated list of proband names.
 -pd,--pedigree <arg>     Comma-separated list of pedigree files (.ped).
 -ph,--phenotypes <arg>   Comma-separated list of sample-phenotypes (e.g. HPO:123 or HPO:123;HPO:234 or sample0/HPO:123,sample1/HPO:234). Phenotypes are CURIE formatted (prefix:reference) and separated by a semicolon.
 -mr,--max_records <arg>   Integer stating the maximum number of records to be available in the report. Default: 100
 -ms,--max_samples <arg>   Integer stating the maximum number of samples to be available in the report. Default: 100
 -d,--debug               Enable debug mode (additional logging and pretty
                          printed report.

usage: java -jar vcf-report.jar -v
 -v,--version   Print version.
```
*: [CURIE](https://phenopackets-schema.readthedocs.io/en/latest/resource.html#rstcurie)

## Examples
```
java -jar vcf-report.jar -i my.vcf.gz
java -jar vcf-report.jar -i my.vcf.gz -o my-report.html
java -jar vcf-report.jar -i my.vcf.gz -o my-report.html -t my-template.html
java -jar vcf-report.jar -i my.vcf.gz -o my-report.html -t my-template.html -pb sample0,sample1 -pd my.ped,my_other.ped
java -jar vcf-report.jar -i my.vcf.gz -o my-report.html -t my-template.html -pb sample0,sample1 -pd my.ped,my_other.ped
java -jar vcf-report.jar -i my.vcf.gz -o my-report.html -t my-template.html -pb sample0,sample1 -pd my.ped,my_other.ped -ph HP:0000001;HP:0000002
java -jar vcf-report.jar -i my.vcf.gz -o my-report.html -t my-template.html -pb sample0,sample1 -pd my.ped,my_other.ped -ph sampleId1/HP:0000001;HP:0000002,sampleId2/HP:0000001
java -jar vcf-report.jar -i my.vcf.gz -o my-report.html -t my-template.html -pb sample0,sample1 -pd my.ped,my_other.ped -ph sampleId1/HP:0000001;HP:0000002,sampleId2/HP:0000001 -mr 1000 -ms 10
java -jar vcf-report.jar -v
```
## Template
The report generator transforms the input data to a JavaScript object (window.api) that is injected into the report template at the end of the head tag.
### Example
Consider the following template:
```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <title>Report</title>
  </head>
  <body>
    <div id="report"></div>
    <script>
        document.getElementById("report").innerHTML = JSON.stringify(api.data)
    </script>
  </body>
</html>
```
The resulting report after rendering the template using input data will look like:
```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <title>Report</title>
    <script>
      window.api = { ... }
    </script>
  </head>
  <body>
    <div id="report"></div>
    <script>
        document.getElementById("report").innerHTML = JSON.stringify(api)
    </script>
  </body>
</html>
```
## API
The format of the variant report data is described in the ```org.molgenis.vcf.report.model``` Java classes.

The format of the phenotype data is described here: [Phenopackets_Person](https://phenopackets-schema.readthedocs.io/en/latest/pedigree.html#person)

The format of the sample data is described here: [Phenopacket](https://phenopackets-schema.readthedocs.io/en/latest/phenopacket.html)
Please note that only a subset of PhenotypicFeature fields is returned.
### Example
```
"phenotypes": {
    "items": [
      {
        "subject": {
          "id": "SampleId123"
        },
        "phenotypicFeaturesCount": 2,
        "phenotypicFeaturesList": [
          {
            "type": {
              "id": "HP:123456",
              "label": "HP:123456"
            }
          },
          {
            "type": {
              "id": "HP:234567",
              "label": "HP:234567"
            }
          }
        ]
      }
```
