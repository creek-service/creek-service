[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Coverage Status](https://coveralls.io/repos/github/creek-service/creek-service/badge.svg?branch=main)](https://coveralls.io/github/creek-service/creek-service?branch=main)
[![build](https://github.com/creek-service/creek-service/actions/workflows/build.yml/badge.svg)](https://github.com/creek-service/creek-service/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.creekservice/creek-service-context.svg)](https://central.sonatype.dev/search?q=creek-service-*)
[![CodeQL](https://github.com/creek-service/creek-service/actions/workflows/codeql.yml/badge.svg)](https://github.com/creek-service/creek-service/actions/workflows/codeql.yml)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/creek-service/creek-service/badge)](https://api.securityscorecards.dev/projects/github.com/creek-service/creek-service)
[![OpenSSF Best Practices](https://bestpractices.coreinfrastructure.org/projects/6899/badge)](https://bestpractices.coreinfrastructure.org/projects/6899)

# Creek Service

The core of the Creek system.

See [CreekService.org](https://www.creekservice.org) for info on Creek Service.

## Modules

* **[context](context)**: Provides features to make it easier to write Kafka and Kafka Streams based microservices.
* **[extension](extension)**: Contains base types used to implement a Creek service extension and a means of loading extensions                           
* **[test-java-eight](test-java-eight)**: Contains functional testing extension loading *without* Java 9 modularity
* **[test-java-nine](test-java-nine)**: Contains functional testing extension loading *with* Java 9 modularity
* **[test-java-eight-extension](test-java-eight-extension)**: Contains a test-only service extension *without* Java 9 module info
* **[test-java-nine-extension](test-java-nine-extension)**: Contains a test-only service extension *with* Java 9 module info
