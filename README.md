# Gradle Java Cross-Compile Plugin

![Support Status](https://img.shields.io/badge/nebula-supported-brightgreen.svg)
[![Build Status](https://travis-ci.org/nebula-plugins/gradle-cross-compile-plugin.svg?branch=master)](https://travis-ci.org/nebula-plugins/gradle-cross-compile-plugin)
[![Coverage Status](https://coveralls.io/repos/nebula-plugins/gradle-cross-compile-plugin/badge.svg?branch=master&service=github)](https://coveralls.io/github/nebula-plugins/gradle-cross-compile-plugin?branch=master)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/nebula-plugins/gradle-cross-compile-plugin?utm_source=badgeutm_medium=badgeutm_campaign=pr-badge)
[![Apache 2.0](https://img.shields.io/github/license/nebula-plugins/gradle-cross-compile-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Automatically configures the bootstrap classpath when the requested `targetCompatibility` is less than the current Java version, avoiding:

	warning: [options] bootstrap class path not set in conjunction with -source 1.7

The plugin supports Java, Groovy joint compilation, and Kotlin. The plugin locates JDKs via either:

- Environment variables
    - In the form `JDK1x` where `x` is the major version, for instance `JDK18` for Java 8
- Default installation locations for MacOS, Ubuntu and Windows
    - Where more than one version of the JDK is available for a given version is available, the highest is used
    - The lookup prefers Oracle JDKs, but falls back to OpenJDK where possible 

# Quick Start

Refer to the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/nebula.cross-compile) for instructions on how to apply the plugin.
