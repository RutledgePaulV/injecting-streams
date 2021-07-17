[![Build Status](https://www.travis-ci.com/RutledgePaulV/injecting-streams.svg?branch=develop)](https://www.travis-ci.com/RutledgePaulV/injecting-streams)
[![Coverage Status](https://coveralls.io/repos/github/RutledgePaulV/injecting-streams/badge.svg?branch=develop)](https://coveralls.io/github/RutledgePaulV/injecting-streams?branch=develop)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.rutledgepaulv/injecting-streams/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.rutledgepaulv/injecting-streams)

## Injecting Streams

Implements a `java.io.OutputStream` that injects bytes as data is written to the original output stream. Bytes are
injected before or after a delimiter of your choice (an array of known bytes). Correct for all uses of the returned 
stream, carefully implemented for optimal efficiency, and has zero dependencies.

### Usage

You might use this to inject additional markup into a stream of html content without having to otherwise parse the
markup or convert back into text.

```java

var source       = new FileInputStream("original.html");
var sink         = new FileOutputStream("modified.html");
var modifiedSink = InjectingStreams.injectAfterOutput(sink, "<head>", "<script>alert('hello, world')</script>");

try {
    IOUtils.copy(source,modifiedSink);
} finally {
    IOUtils.close(source);
    IOUtils.close(modifiedSink);
}

```

```java

var source       = new FileInputStream("original.html");
var sink         = new FileOutputStream("modified.html");
var modifiedSink = InjectingStreams.injectBeforeOutput(sink, "</body>", "<script>alert('hello, world')</script>");

try {
    IOUtils.copy(source,modifiedSink);
} finally {
    IOUtils.close(source);
    IOUtils.close(modifiedSink);
}

```

### Installation

```xml 

<dependencies>
    <dependency>
        <groupId>com.github.rutledgepaulv</groupId>
        <artifactId>injecting-streams</artifactId>
        <version>2.0</version>
    </dependency>
</dependencies>

```