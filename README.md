# Gradle Java Cross-Compile Plugin

![Support Status](https://img.shields.io/badge/nebula-active-green.svg)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com.netflix.nebula/gradle-java-cross-compile-plugin/maven-metadata.xml.svg?label=gradlePluginPortal)](https://plugins.gradle.org/plugin/nebula.java-cross-compile)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.netflix.nebula/gradle-java-cross-compile-plugin/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.netflix.nebula/gradle-java-cross-compile-plugin)
![CI](https://github.com/nebula-plugins/gradle-java-cross-compile-plugin/actions/workflows/ci.yml/badge.svg)
![Publish](https://github.com/nebula-plugins/gradle-java-cross-compile-plugin/actions/workflows/publish.yml/badge.svg)
[![Apache 2.0](https://img.shields.io/github/license/nebula-plugins/gradle-java-cross-compile-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Automatically configures the bootstrap classpath when the requested `targetCompatibility` is less than the current Java version, avoiding:

	warning: [options] bootstrap class path not set in conjunction with -source 1.7

The plugin supports Java, Groovy joint compilation, and Kotlin. The plugin locates JDKs via either:

- Environment variables
    - In the form `JDK1x` where `x` is the major version, for instance `JDK18` for Java 8
- Default installation locations for MacOS, Ubuntu and Windows
    - Where more than one version of the JDK is available for a given version is available, the highest is used
    - The lookup prefers Oracle JDKs, but falls back to OpenJDK (zulu) where possible
- [SDKMAN!](http://sdkman.io/) JDK candidates
	- The lookup prefers JDKs with no suffix, then Oracle JDKs then OpenJDK (zulu)

# Quick Start

Refer to the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/nebula.java-cross-compile) for instructions on how to apply the plugin.
