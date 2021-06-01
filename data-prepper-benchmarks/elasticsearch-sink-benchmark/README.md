# Elasticsearch sink raw span Benchmarks

This package contains benchmarks for the elasticsearch sink output on raw trace spans using JMH: https://openjdk.java.net/projects/code-tools/jmh/ . 

Integration with gradle is done with the following gradle plugin for JMH: https://github.com/melix/jmh-gradle-plugin.

The plugin creates a source set for the JMH benchmarks, and provides a few gradle tasks for running and building the benchmarks.

## Configuration

- endpoint: A string represents Amazon Elasticsearch Service endpoint. Required for benchmark.

## Running the tests via gradle task

Tests can be run via the "jmh" gradle task provided by the plugin. The README for the plugin provides the various parameters that
can be provided to the plugin. 

## Running the tests via JAR

To run the tests via JAR, you can build the benchmark jar using the gradle task "shadowJar". This jar is an executable jar 
that runs the benchmark tests. Example command:

```
java -jar elasticsearch-sink-benchmark-0.1-beta-all.jar -p endpoint=https://<your-aes-address>.<region>.es.amazonaws.com 
```

## Results