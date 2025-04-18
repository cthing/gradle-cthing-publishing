# ![C Thing Software](https://www.cthing.com/branding/CThingSoftware-57x60.png "C Thing Software") gradle-cthing-publishing

[![CI](https://github.com/cthing/gradle-cthing-publishing/actions/workflows/ci.yml/badge.svg)](https://github.com/cthing/gradle-cthing-publishing/actions/workflows/ci.yml)
[![Portal](https://img.shields.io/gradle-plugin-portal/v/org.cthing.cthing-publishing?label=Plugin%20Portal&logo=gradle)](https://plugins.gradle.org/plugin/org.cthing.cthing-publishing)

A Gradle plugin that provides support for the publishing of C Thing Software artifacts. This plugin
is applicable only to C Thing Software projects.

## Usage

The plugin is available from the
[Gradle Plugin Portal](https://plugins.gradle.org/plugin/org.cthing.cthing-publishing) and can be
applied to a Gradle project using the `plugins` block:

```kotlin
buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
  id("org.cthing.cthing-publishing") version "2.0.0"
}
```

The plugin creates two extensions, `cthingPublishing` and `cthingRepo`. The `cthingPublishing`
extension provides methods for obtaining the direct dependencies on C Thing Software artifacts
and create an action to populate the POM fields with C Thing Software publishing information.
The `cthingRepo` extension provides information about the C Thing Software artifact repository.

The following is an example of a typical usage of the `cthingPublishing` extension:
```kotlin
publishing {
    publications {
        register("jar", MavenPublication::class) {
            from(components["java"])

            pom(cthingPublishing.createPomAction())
        }
    }
}
```
The following is an example of a typical usage of the `cthingRepo` extension:
```kotlin
publishing {
    val repoUrl = cthingRepo.repoUrl
    if (repoUrl != null) {
        repositories {
            maven {
                name = "CThingMaven"
                setUrl(repoUrl)
                credentials {
                    username = cthingRepo.user
                    password = cthingRepo.password
                }
            }
        }
    }
}
```

## Compatibility

The following Gradle and Java versions are supported:

| Plugin Version | Gradle Version | Minimum Java Version |
|----------------|----------------|----------------------|
| 1.+            | 8.0+           | 17                   |
| 2.+            | 8.0+           | 17                   |

## Building

The plugin is compiled for Java 17. If a Java 17 toolchain is not available, one will be downloaded.

Gradle is used to build the plugin:
```bash
./gradlew build
```
The Javadoc for the plugin can be generated by running:
```bash
./gradlew javadoc
```

## Releasing

This project is released on the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/org.cthing.cthing-publishing).
Perform the following steps to create a release.

- Commit all changes for the release
- In the `build.gradle.kts` file, edit the `ProjectVersion` object
    - Set the version for the release. The project follows [semantic versioning](https://semver.org/).
    - Set the build type to `BuildType.release`
- Commit the changes
- Wait until CI successfully builds the release candidate
- Verify GitHub Actions build is successful
- In a browser go to the C Thing Software Jenkins CI page
- Run the `gradle-cthing-publishing-validate` job
- Wait until that job successfully completes
- Run the `gradle-cthing-publishing-release` job to release the plugin to the Gradle Plugin Portal
- Wait for the plugin to be reviewed and made available by the Gradle team
- In a browser, go to the project on GitHub
- Generate a release with the tag `<version>`
- In the build.gradle.kts file, edit the `ProjectVersion` object
    - Increment the version patch number
    - Set the build type to `BuildType.snapshot`
- Update the `CHANGELOG.md` with the changes in the release and prepare for next release changes
- Update the `Usage` and `Compatibility` sections in the `README.md` with the latest artifact release version
- Commit these changes
