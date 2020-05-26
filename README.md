[![Build Status](https://travis-ci.org/molgenis/vip-report.svg?branch=master)](https://travis-ci.org/molgenis/vip-report)
[![Quality Status](https://sonarcloud.io/api/project_badges/measure?project=molgenis_vip-report&metric=alert_status)](https://sonarcloud.io/dashboard?id=molgenis_vip-report)
# Variant Interpretation Pipeline - VCF Report Generator
Command-line application to generate a report for any VCF (Variant Call Format) file based on a report template.
## Requirements
- Java 11

## Usage
```
java -jar vcf-report.jar -i <arg> [-o <arg>] [-f] [-t <arg>] [-d]
 -i,--input <arg>      Input VCF file (.vcf or .vcf.gz).
 -o,--output <arg>     Output report file (.html).
 -f,--force            Override the output file if it already exists.
 -t,--template <arg>   Report template file (.html).
 -d,--debug            Enable debug mode (additional logging and pretty printed report.
```
```
java -jar vcf-report.jar -v
 -v,--version   Print version.
```
## Examples
```
java -jar vcf-report.jar -i my.vcf.gz
java -jar vcf-report.jar -i my.vcf.gz -o my-report.html
java -jar vcf-report.jar -i my.vcf.gz -o my-report.html -t my-template.html
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
The format of the report data is described in the ```org.molgenis.vcf.report.model``` Java classes.
