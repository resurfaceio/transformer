# resurfaceio-transformer

## Supported file formats

* .ndjson.gz - Newline Delimited JSON with GZIP compression

Each line in the input file is parsed as a Resurface message:
https://resurface.io/json.html

## System requirements

* Java 11
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
<small>&copy; 2016-2023 <a href="https://resurface.io">Resurface Labs Inc.</a></small>
