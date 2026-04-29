[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![codecov](https://codecov.io/gh/creek-service/creek-service/branch/main/graph/badge.svg)](https://codecov.io/gh/creek-service/creek-service)
[![build](https://github.com/creek-service/creek-service/actions/workflows/build.yml/badge.svg)](https://github.com/creek-service/creek-service/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.creekservice/creek-service-context.svg)](https://central.sonatype.dev/search?q=creek-service-*)
[![CodeQL](https://github.com/creek-service/creek-service/actions/workflows/codeql.yml/badge.svg)](https://github.com/creek-service/creek-service/actions/workflows/codeql.yml)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/creek-service/creek-service/badge)](https://api.securityscorecards.dev/projects/github.com/creek-service/creek-service)
[![OpenSSF Best Practices](https://bestpractices.coreinfrastructure.org/projects/6899/badge)](https://bestpractices.coreinfrastructure.org/projects/6899)

# Creek Service

The libraries published from this repository form the core of the Creek system used to implement Creek based microservices
and extensions to Creek itself.

See [CreekService.org](https://www.creekservice.org) for info on Creek Service.

## Modules

* **[context](context)** [[JavaDocs](https://javadoc.io/doc/org.creekservice/creek-service-context)]: Provides features to make it easier to write Kafka and Kafka Streams based microservices.
* **[extension](extension)** [[JavaDocs](https://javadoc.io/doc/org.creekservice/creek-service-extension)]: Contains base types used to implement a Creek service extension and a means of loading extensions                           
* **[test-java-eight](test-java-eight)**: Contains functional testing extension loading *without* Java 9 modularity
* **[test-java-nine](test-java-nine)**: Contains functional testing extension loading *with* Java 9 modularity
* **[test-java-eight-extension](test-java-eight-extension)**: Contains a test-only service extension *without* Java 9 module info
* **[test-java-nine-extension](test-java-nine-extension)**: Contains a test-only service extension *with* Java 9 module info

