# resurfaceio-transformer
Modify or merge NDJSON files

This command-line utility transforms and merges locally stored files in [NDJSON format](https://resurface.io/json.html).
The resulting files can then be imported into a remote Resurface database.

[![CodeFactor](https://www.codefactor.io/repository/github/resurfaceio/transformer/badge)](https://www.codefactor.io/repository/github/resurfaceio/transformer)
[![License](https://img.shields.io/github/license/resurfaceio/transformer)](https://github.com/resurfaceio/transformer/blob/v3.6.x/LICENSE)
[![Contributing](https://img.shields.io/badge/contributions-welcome-green.svg)](https://github.com/resurfaceio/transformer/blob/v3.6.x/CONTRIBUTING.md)

## Usage

Download executable jar:
```
wget https://dl.cloudsmith.io/public/resurfaceio/public/maven/io/resurface/resurfaceio-transformer/3.6.3/resurfaceio-transformer-3.6.3.jar
```

Merging two files while removing duplicates:
```
java -DTRANSFORM_DUPLICATES=drop -DFILES_IN=source1.ndjson.gz,source2.ndjson.gz -DFILE_OUT=results.ndjson.gz -Xmx192M -jar resurfaceio-transformer-3.6.3.jar
```

Randomly shuffling unique calls across the last year:
```
java -DTRANSFORM_DUPLICATES=drop -DTRANSFORM_RESPONSE_TIME_MILLIS=shuffle:1y -DFILES_IN=source.ndjson.gz -DFILE_OUT=results.ndjson.gz -Xmx192M -jar resurfaceio-transformer-3.6.3.jar
```

⚠️ This utility reads and writes files in .ndjson.gz format exclusively. This compressed file format can be exported from a
Resurface database, or generated using the [ndjson](https://github.com/resurfaceio/ndjson) library.

## Parameters

```
FILES_IN: comma-separated list of files to use as input
FILE_OUT: resulting file to be created (must not already exist!)

TRANSFORM_DUPLICATES: keep|drop
TRANSFORM_INTERVAL_MILLIS: keep|drop|randomize:<time>
TRANSFORM_RESPONSE_TIME_MILLIS: keep|drop|add:<time>|subtract:<time>|shuffle:<time>

^ where <time> is <integer><unit>
  and <unit> is 'y' (year), 'm' (month), 'w' (week), 'd' (day), 'h' (hour), 'n' (minute), 's' (second)
```

## Dependencies

* Java 17
* [resurfaceio/ndjson](https://github.com/resurfaceio/ndjson)

## Installing with Maven

⚠️ We publish our official binaries on [CloudSmith](https://cloudsmith.com) rather than Maven Central, because CloudSmith
is awesome.

If you want to call this utility from your own Java application, add these sections to `pom.xml` to install:

```xml
<dependency>
    <groupId>io.resurface</groupId>
    <artifactId>resurfaceio-transformer</artifactId>
    <version>3.6.3</version>
</dependency>
```

```xml
<repositories>
    <repository>
        <id>resurfaceio-public</id>
        <url>https://dl.cloudsmith.io/public/resurfaceio/public/maven/</url>
        <releases>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </releases>
    </repository>
</repositories>
```

---
<small>&copy; 2016-2024 <a href="https://resurface.io">Graylog, Inc.</a></small>
