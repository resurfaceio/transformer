# resurfaceio-transformer
Modify or merge NDJSON files

This command-line utility transforms and merges locally stored files in [NDJSON format](https://resurface.io/json.html).
The resulting files can then be imported into a remote Resurface database.

[![CodeFactor](https://www.codefactor.io/repository/github/resurfaceio/transformer/badge)](https://www.codefactor.io/repository/github/resurfaceio/transformer)
[![License](https://img.shields.io/github/license/resurfaceio/transformer)](https://github.com/resurfaceio/transformer/blob/v3.6.x/LICENSE)
[![Contributing](https://img.shields.io/badge/contributions-welcome-green.svg)](https://github.com/resurfaceio/transformer/blob/v3.6.x/CONTRIBUTING.md)

## Supported file formats

* .ndjson.gz - Newline Delimited JSON with GZIP compression

Each line in the input file is parsed as a Resurface message:
https://resurface.io/json.html

## System requirements

* Java 17
* Maven

## Building from sources

```
git clone https://github.com/resurfaceio/transformer.git resurfaceio-transformer
cd resurfaceio-transformer
mvn package
```

## Transforming local file

```
java -DFILE_IN=./one.ndjson.gz -DFILE_OUT=./two.ndjson.gz -Xmx192M -jar target/main-jar-with-dependencies.jar
```

---
<small>&copy; 2016-2024 <a href="https://resurface.io">Graylog, Inc.</a></small>
