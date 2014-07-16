coding-conventions
==================

A set of conventions, recommendations, guidelines and configuration files for Checkstyle and Eclipse to be able to keep the structural quality of the code high.

# Eclipse

Check the [Cookbook](http://www.everit.org/cookbook/ide/index.html#installing_eclipse) for Eclipse installation and configuration.

# Git and SVN

## Repository naming convention

### Common rules

The name of the repository must describe the project itself and the following rules must apply:
 - olny lower case
 - '-' used as word separator
 - numbers allowed only in special cases

### Type #1

Projects without dedicated API like plugins, support or helper projects, configurations, etc. stored in one single repository. E.g.:
 - [eosgi-maven-plugin](https://github.com/everit-org/eosgi-maven-plugin)
 - [transaction-helper](https://github.com/everit-org/transaction-helper)
 - [thymeleaf-mvel2](https://github.com/everit-org/thymeleaf-mvel2)

### Type #2

Projects with different versioning lifecycle should be stored in separate repositories. E.g. API and implementation projects:
 - one with ending "-api" for the API part,
 - one with ending "-ri" for the implementation, in case of "ri" it means "Reference Implementation"

E.g.:
 - [resource-api](https://github.com/everit-org/resource-api)
 - [resource-ri](https://github.com/everit-org/resource-ri)
 - [resource-ext](https://github.com/everit-org/resource-ext) (API extension that does not belong to the core functionalities of the project)

## Project structure by repositories

If no integration test project is necessary, then one single artifact exists in the repository.
Otherwise the following names should be picked:
 - "core" or "component" for the project itself
 - "schema" for data model schemas, e.g. database schema, XML schema. etc.
 - "tests" for the integration tests of the project

Other segmentations are allowed if better structural quality can be achived with it.

# Maven

## GAV

### GroupId

In case of an Open Source project, the groupId must start with "org.everit...". If it is an OSGi project then it is "org.everit.osgi".

### ArtifactId

The artifactId must start with groupId and must be followed with the name of the repository (replacing all '-' with '.').

E.g.:
 - org.everit.osgi.resource.api
 - org.everit.osgi.resource.ri
 - org.everit.osgi.resource.ext

### Version

The version number must follow the rules of [Semantic Versioning](http://semver.org/).

## Deployment rules

### Do not deploy

 - Artifacts with POM packaging (e.g. parent POM's that support the Maven build, to build all of the sub projects together).
 - Test and integration test artifacts.
 - Artifacts that are not referenced from other projects (except if it is a dedicated artifact).

### Deploy

All of the artifacts that do not match the "do not deploy" rule.

## The parent POM of an artifact

In case of an Open Source project the newest version of the "org.everit.config.oss", otherwise the "org.everit.config.main" configuration projects must be used. The following rules must be applied:
 - the parent of the project's parent is an artifact that holds the confiugration (.oss or .main)
 - the parent of the tests project is the project's parent POM
 - the parent of the other artifacts is a parent POM that holds the configuration (.oss or .main)

# Java version

The lowest supported Java version is 1.6. Using higher versions of Java (1.7 and 1.8) is possible if:
 - the code is more readable,
 - the code is clearer,
 - the performance is higher,
 - etc.

Using higher version of Java without specific reason is not recommended.

# Java package

 - The java package in a Maven artifact must match the artifactId. In case of API projects the "api" ending in the package is unnecessary and not recommended.
 - Internal packages that are not exported as OSGi package must named "...inetrnal".
