[![Build Status](https://app.travis-ci.com/molgenis/vip-report.svg?branch=main)](https://app.travis-ci.com/molgenis/vip-report)
[![Quality Status](https://sonarcloud.io/api/project_badges/measure?project=molgenis_vip-report&metric=alert_status)](https://sonarcloud.io/dashboard?id=molgenis_vip-report)

# Variant Interpretation Pipeline - VCF Report Generator
Command-line application to generate a report for any VCF (Variant Call Format) file based on a
report template.

## Requirements
- Java 21

## Usage
```
usage: java -jar vcf-report.jar -i <arg> [-m <arg>] [-o <arg>] [-f] [-t <arg>] [-pb
       <arg>] [-pd <arg>] [-ph <arg>] [-ms <arg>] [-r <arg>]
       [-g <arg>] [-c <arg>] [-dt <arg>] [-d]
 -i,--input <arg>            Input VCF file (.vcf or .vcf.gz).
 -m,--metadata <arg>         VCF metadata file (.json).
 -o,--output <arg>           Output report file (.html).
 -f,--force                  Override the output file if it already
                             exists.
 -t,--template <arg>         Report template file (.html).
 -tc,--template_config <arg> Report template configuration file (.json).
 -pb,--probands <arg>        Comma-separated list of proband names.
 -pd,--pedigree <arg>        Comma-separated list of pedigree files
                             (.ped).
 -ph,--phenotypes <arg>      Comma-separated list of sample-phenotypes
                             (e.g. HP:123 or HP:123;HP:234 or
                             sample0/HP:123,sample1/HP:234). Phenotypes
                             are CURIE formatted (prefix:reference) and
                             separated by a semicolon.
 -ms,--max_samples <arg>     Integer stating the maximum number of samples
                             to be available in the report. Default: 100
 -r,--reference <arg>        Reference sequence file (.fasta.gz, .fna.gz,
                             .fa.gz, .ffn.gz, .faa.gz or .frn.gz).
 -g,--genes <arg>            Genes file to be used as reference track in the 
                             genome browser, UCSC NCBI RefSeq GFF file 
                             (gff.gz or gff3.gz).
 -c,--cram <arg>             Comma-separated list of sample-bam files
                             (e.g.
                             sample0=/path/to/0.cram,sample1=/path/to/1.cram
                             ).
 -dt,--decision_tree <arg>   Decision tree file as used in
                             vip-decision-tree (.json).
 -d,--debug                  Enable debug mode (additional logging and
                             pretty printed report).

usage: java -jar vcf-report.jar -v
 -v,--version   Print version.
```

*: [CURIE](https://phenopackets-schema.readthedocs.io/en/latest/resource.html#rstcurie)

## Examples
```
java -jar vcf-report.jar -i my.vcf.gz -m metadata.json -t my-template.html
java -jar vcf-report.jar -i my.vcf.gz -m metadata.json -o my-report.html -t my-template.html
java -jar vcf-report.jar -i my.vcf.gz -m metadata.json -o my-report.html -t my-template.html
java -jar vcf-report.jar -i my.vcf.gz -m metadata.json -o my-report.html -t my-template.html -pb sample0,sample1 -pd my.ped,my_other.ped
java -jar vcf-report.jar -i my.vcf.gz -m metadata.json -o my-report.html -t my-template.html -pb sample0,sample1 -pd my.ped,my_other.ped
java -jar vcf-report.jar -i my.vcf.gz -m metadata.json -o my-report.html -t my-template.html -pb sample0,sample1 -pd my.ped,my_other.ped -ph HP:0000001;HP:0000002
java -jar vcf-report.jar -i my.vcf.gz -m metadata.json -o my-report.html -t my-template.html -pb sample0,sample1 -pd my.ped,my_other.ped -ph sampleId1/HP:0000001;HP:0000002,sampleId2/HP:0000001
java -jar vcf-report.jar -i my.vcf.gz -m metadata.json -o my-report.html -t my-template.html -pb sample0,sample1 -pd my.ped,my_other.ped -ph sampleId1/HP:0000001;HP:0000002,sampleId2/HP:0000001 -ms 10
java -jar vcf-report.jar -i my.vcf.gz -m metadata.json -o my-report.html -t my-template.html -pb sample0,sample1 -pd my.ped,my_other.ped -ph sampleId1/HP:0000001;HP:0000002,sampleId2/HP:0000001 -ms 10 -tc template_config.json
java -jar vcf-report.jar -v
```

## Frequently asked questions
### Why doesn't my report load in the web browser?
You report might contain more data than your web browser can handle. Try reducing the number of VCF records, setting a lower value for `--max_samples` and reducing alignment data in .cram files.
In case of long-read alignment sequences try removing `--cram` from the argument list.

## Development

### Installation
Generate a personal access token in GitHub with at least the scope "read:packages".

Then add a settings.xml to your Maven .m2 folder, or edit it if you already have one. It should
contain the following:

```
<?xml version="1.0"?>

<settings xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/SETTINGS/1.0.0">
  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>
  <profiles>
    <profile>
      <id>github</id>
      <repositories>
        <repository>
          <id>central</id>
          <url>https://repo1.maven.org/maven2</url>
          </repository>
          <repository>
            <id>github</id>
            <url>https://maven.pkg.github.com/molgenis/vip-utils</url>
            <snapshots>
              <enabled>true</enabled>
            </snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>

  <servers>
    <server>
      <id>github</id>
      <username>[YOUR VIP USERNAME]</username>
      <password>[YOUR PERSONAL ACCESS TOKEN]</password>
    </server>
   </servers>
</settings>
```

### Template
The report generator transforms the input data to a JavaScript object (window.api) that is injected
into the report template at the end of the head tag.

#### Example
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
    window.api = {...}
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

### API
The format of the variant report data is described in the ```org.molgenis.vcf.report.model``` Java
classes.

The format of the phenotype data is described
here: [Phenopackets_Person](https://phenopackets-schema.readthedocs.io/en/latest/pedigree.html#person)

The format of the sample data is described
here: [Phenopacket](https://phenopackets-schema.readthedocs.io/en/latest/phenopacket.html)
Please note that only a subset of PhenotypicFeature fields is returned.

#### Example

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
