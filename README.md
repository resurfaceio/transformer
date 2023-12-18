# resurfaceio-transformer

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

## Moving through time

By default, the `response.time_millis` field is reset to the current system time millis.
This can be changed by specifying both an `OPERATION` and `AMOUNT` of time:

```
java -DOPERATION=sub -DAMOUNT=3d -DFILE_IN=./one.ndjson.gz -DFILE_OUT=./two.ndjson.gz -Xmx192M -jar target/main-jar-with-dependencies.jar
```

The previous command sets the `response.time_millis` field of each call in the `two.ndjson.gz` dataset
by subtracting 1728000000 milliseconds (equivalent to 20 days) from each corresponding call in the `one.ndjson.gz`
dataset, effectively translating the dataset through time.

The available operations are:

- `add`: Adds `AMOUNT` in millis to `response.time_millis` for the input dataset.
- `sub`: Subtracts `AMOUNT` in millis to `response.time_millis` for the input dataset.

The amount can be specified directly in millis, or indirectly through the use of a time unit suffix:

- `s`: second. Corresponds to `1000` milliseconds.
- `n`: minute. Corresponds to `60 * 1s` milliseconds.
- `h`: hour.   Corresponds to `60 * 1n` milliseconds.
- `d`: day.    Corresponds to `24 * 1h` milliseconds.
- `w`: week.   Corresponds to `7 * 1d` milliseconds.
- `m`: month.  Corresponds to `4 * 1w` milliseconds.
- `y`: year.   Corresponds to `12 * 1m` milliseconds.

---
<small>&copy; 2016-2023 <a href="https://resurface.io">Graylog, Inc.</a></small>
