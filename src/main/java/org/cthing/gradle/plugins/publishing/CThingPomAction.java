/*
 * Copyright 2025 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.gradle.plugins.publishing;

import java.util.Set;
import java.util.function.Supplier;

import org.cthing.projectversion.ProjectVersion;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.publish.maven.MavenPom;


/**
 * An {@link Action} which populates a {@link MavenPom} with C Thing Software publishing information.
 */
public class CThingPomAction implements Action<MavenPom> {

    private static final String ORGANIZATION_NAME = "C Thing Software";
    private static final String ORGANIZATION_URL = "https://www.cthing.com";
    private static final String GITHUB_BASE_URL = "https://github.com";

    private final Project project;
    private final Supplier<Set<String>> findCThingDependencies;
    private final Supplier<Set<String>> findCThingGradlePlugins;
    private String user;
    private PomLicense license;
    private PomCISystem ciSystem;

    public CThingPomAction(final Project project, final Supplier<Set<String>> findCThingDependencies,
                           final Supplier<Set<String>> findCThingGradlePlugins) {
        this.project = project;
        this.findCThingDependencies = findCThingDependencies;
        this.findCThingGradlePlugins = findCThingGradlePlugins;
        this.user = "cthing";
        this.license = PomLicense.ASL2;
        this.ciSystem = PomCISystem.GitHubActions;
    }

    /**
     * Obtains the source code repository username for the project (e.g. the GitHub user for the project).
     *
     * @return Source code repository username.
     */
    public String getUser() {
        return this.user;
    }

    /**
     * Sets the source code repository username for the project (e.g. the GitHub user for the project).
     *
     * @param user Source code repository username for the project
     * @return This action
     */
    public CThingPomAction setUser(final String user) {
        this.user = user;
        return this;
    }

    /**
     * Obtains the project license.
     *
     * @return Project license
     */
    public PomLicense getLicense() {
        return this.license;
    }

    /**
     * Sets the project license.
     *
     * @param license Project license
     * @return This action
     */
    public CThingPomAction setLicense(final PomLicense license) {
        this.license = license;
        return this;
    }

    /**
     * Obtains the CI system used to build the project.
     *
     * @return CI system used to build the project
     */
    public PomCISystem getCiSystem() {
        return this.ciSystem;
    }

    /**
     * Sets the CI system used to build the project.
     *
     * @param ciSystem CI system which builds the project
     * @return This action
     */
    public CThingPomAction setCiSystem(final PomCISystem ciSystem) {
        this.ciSystem = ciSystem;
        return this;
    }

    @Override
    public void execute(final MavenPom mavenPom) {
        final String githubUrl = GITHUB_BASE_URL + "/" + this.user + "/" + this.project.getName();

        mavenPom.getName().convention(this.project.getName());
        mavenPom.getDescription().convention(this.project.getDescription());
        mavenPom.getUrl().convention(githubUrl);

        mavenPom.organization(organization -> {
            organization.getName().convention(ORGANIZATION_NAME);
            organization.getUrl().convention(ORGANIZATION_URL);
        });

        mavenPom.licenses(licenses -> licenses.license(license -> {
            license.getName().convention(this.license.getName());
            license.getUrl().convention(this.license.getUrl());
        }));

        mavenPom.developers(developers -> developers.developer(developer -> {
            developer.getId().convention("baron");
            developer.getName().convention("Baron Roberts");
            developer.getEmail().convention("baron@cthing.com");
            developer.getOrganization().convention(ORGANIZATION_NAME);
            developer.getOrganizationUrl().convention(ORGANIZATION_URL);
        }));

        mavenPom.scm(scm -> {
            scm.getConnection().convention("scm:git:" + githubUrl + ".git");
            scm.getDeveloperConnection().convention("scm:git:git@github.com:" + this.user + "/"
                                                            + this.project.getName() + ".git");
            scm.getUrl().convention(githubUrl);
        });

        mavenPom.issueManagement(issues -> {
            issues.getSystem().convention("GitHub Issues");
            issues.getUrl().convention(githubUrl + "/issues");
        });

        if (this.ciSystem == PomCISystem.GitHubActions) {
            mavenPom.ciManagement(ciManagement -> {
                ciManagement.getUrl().convention(githubUrl + "/actions");
                ciManagement.getSystem().convention("GitHub Actions");
            });
        } else if (this.ciSystem == PomCISystem.CThingJenkins) {
            mavenPom.ciManagement(ciManagement -> ciManagement.getSystem().convention(ORGANIZATION_NAME + " Jenkins"));
        }

        if (this.project.getVersion() instanceof ProjectVersion projectVersion) {
            mavenPom.getProperties().put("cthing.build.date", projectVersion.getBuildDate());
            mavenPom.getProperties().put("cthing.build.number", projectVersion.getBuildNumber());
        }

        final Set<String> dependencies = this.findCThingDependencies.get();
        if (!dependencies.isEmpty()) {
            mavenPom.getProperties().put("cthing.dependencies", String.join(" ", dependencies));
        }

        final Set<String> plugins = this.findCThingGradlePlugins.get();
        if (!plugins.isEmpty()) {
            mavenPom.getProperties().put("cthing.gradle.plugins", String.join(" ", plugins));
        }
    }
}
