jmulticlose
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.jmulticlose/com.io7m.jmulticlose.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.jmulticlose%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/com.io7m.jmulticlose/com.io7m.jmulticlose?server=https%3A%2F%2Fs01.oss.sonatype.org&style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/jmulticlose/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/jmulticlose.svg?style=flat-square)](https://codecov.io/gh/io7m-com/jmulticlose)
![Java Version](https://img.shields.io/badge/21-java?label=java&color=e6c35c)

![com.io7m.jmulticlose](./src/site/resources/jmulticlose.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/jmulticlose/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/jmulticlose/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/jmulticlose/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/jmulticlose/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/jmulticlose/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/jmulticlose/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/jmulticlose/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/jmulticlose/actions?query=workflow%3Amain.windows.temurin.lts)|

## Motivation

The `jmulticlose` package implements a simple extension to the Java
`try-with-resources` statement that allows for closing many resources
at once - without nested or compound statements - in a type-safe manner.

## Usage

Create a `CloseableCollection` to track resources. Add resources to it. The
resources will be closed when the collection is closed. All resources will be closed
even if any of the individual resources raises an exception.


```
final Resource r0;
final Resource r1;
final Resource r2;

try (CloseableCollectionType&lt;ClosingResourceFailedException&gt; c = CloseableCollection.create()) {
  r0 = c.add(new Resource(0));
  r1 = c.add(new Resource(1));
  r2 = c.add(new Resource(2));
}
```

