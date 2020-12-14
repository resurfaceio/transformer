# resurfaceio-transformer

## Supported file formats

* .ndjson - Newline Delimited JSON (http://ndjson.org)
* .ndjson.gz - Newline Delimited JSON with GZIP compression

Each line in the input file is parsed as a Resurface message:
https://resurface.io/json.html

## System requirements

* Java 8 or 11
* Maven

## Building from sources

```
git clone https://github.com/resurfaceio/transformer.git resurfaceio-transformer
cd resurfaceio-transformer
mvn package
```

## Transforming local file

```
FILE_IN=~/Dropbox/datasets/website.ndjson.gz FILE_OUT=~/Downloads/test.ndjson.gz java -Xmx192M -jar target/main-jar-with-dependencies.jar
```
