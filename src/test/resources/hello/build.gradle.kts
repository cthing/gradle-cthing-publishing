import org.cthing.projectversion.BuildType
import org.cthing.projectversion.ProjectVersion
import org.cthing.gradle.plugins.publishing.PomLicense

plugins {
    java
    `maven-publish`
    id("org.cthing.cthing-publishing")
}

version = ProjectVersion("0.1.0", BuildType.snapshot)
group = "org.cthing"
description = "hello world"

publishing {
    publications {
        register("jar", MavenPublication::class) {
            from(components["java"])

            val pomAction = cthingPublishing.createPomAction().setLicense(PomLicense.MIT);
            pom(pomAction)
        }
    }

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
