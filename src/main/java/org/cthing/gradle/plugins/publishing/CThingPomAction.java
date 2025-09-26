/*
 * Copyright 2025 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.gradle.plugins.publishing;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
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

    private final Project project;
    private final Supplier<Set<String>> findCThingDependencies;
    private final Supplier<Set<String>> findCThingGradlePlugins;
    private PomLicense license;
    private PomCISystem ciSystem;
    private final Set<PomDeveloper> developers;

    public CThingPomAction(final Project project, final Supplier<Set<String>> findCThingDependencies,
                           final Supplier<Set<String>> findCThingGradlePlugins) {
        this.project = project;
        this.findCThingDependencies = findCThingDependencies;
        this.findCThingGradlePlugins = findCThingGradlePlugins;
        this.license = PomLicense.ASL2;
        this.ciSystem = PomCISystem.GitHubActions;
        this.developers = new TreeSet<>(Comparator.comparing(PomDeveloper::getId));

        final PomDeveloper developer = new PomDeveloper("baron", "Baron Roberts", "baron@cthing.com");
        this.developers.add(developer);
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

    /**
     * Obtains the project developers.
     *
     * @return Project developers. If there are no developers listed, an empty set is returned.
     */
    public Set<PomDeveloper> getDevelopers() {
        return Collections.unmodifiableSet(this.developers);
    }

    /**
     * Sets the project developers.
     *
     * @param developers Project developers. Specify the empty set to clear the list of developers.
     * @return This action
     */
    public CThingPomAction setDevelopers(final Set<PomDeveloper> developers) {
        this.developers.clear();
        this.developers.addAll(developers);
        return this;
    }

    /**
     * Adds a project developer to the existing set of developers.
     *
     * @param developer Project developer to add
     * @return This action
     */
    public CThingPomAction addDeveloper(final PomDeveloper developer) {
        this.developers.add(developer);
        return this;
    }

    @Override
    public void execute(final MavenPom mavenPom) {
        final PomScm scmUrls = new PomScm(this.project);

        mavenPom.getName().convention(this.project.getName());
        mavenPom.getDescription().convention(this.project.getDescription());
        mavenPom.getUrl().convention(scmUrls.getBrowse());

        mavenPom.organization(organization -> {
            organization.getName().convention(ORGANIZATION_NAME);
            organization.getUrl().convention(ORGANIZATION_URL);
        });

        mavenPom.licenses(licenses -> licenses.license(license -> {
            license.getName().convention(this.license.getName());
            license.getUrl().convention(this.license.getUrl());
        }));

        mavenPom.developers(mavenDevelopers -> {
            for (PomDeveloper developer : this.developers) {
                mavenDevelopers.developer(mavenDeveloper -> {
                    mavenDeveloper.getId().set(developer.getId());
                    mavenDeveloper.getName().set(developer.getName());
                    mavenDeveloper.getEmail().set(developer.getEmail());
                    mavenDeveloper.getOrganization().convention(ORGANIZATION_NAME);
                    mavenDeveloper.getOrganizationUrl().convention(ORGANIZATION_URL);
                });
            }
        });

        if (scmUrls.isPresent()) {
            mavenPom.scm(scm -> {
                scm.getConnection().convention(scmUrls.getReadOnly());
                scm.getDeveloperConnection().convention(scmUrls.getReadWrite());
                scm.getUrl().convention(scmUrls.getBrowse());
            });

            mavenPom.issueManagement(issues -> {
                issues.getSystem().convention("GitHub Issues");
                issues.getUrl().convention(scmUrls.getBrowse().map(url -> url + "/issues"));
            });
        }

        if (this.ciSystem == PomCISystem.GitHubActions) {
            if (scmUrls.isPresent()) {
                mavenPom.ciManagement(ciManagement -> {
                    ciManagement.getUrl().convention(scmUrls.getBrowse().map(url -> url + "/actions"));
                    ciManagement.getSystem().convention("GitHub Actions");
                });
            }
        } else if (this.ciSystem == PomCISystem.CThingJenkins) {
            mavenPom.ciManagement(ciManagement -> ciManagement.getSystem().convention(ORGANIZATION_NAME + " Jenkins"));
        }

        if (this.project.getVersion() instanceof ProjectVersion projectVersion) {
            mavenPom.getProperties().put("cthing.build.date", projectVersion.getBuildDate());
            mavenPom.getProperties().put("cthing.build.number", projectVersion.getBuildNumber());
        }

        // To avoid configuration mutation errors, defer finding dependencies until after the
        // project has been evaluated.
        this.project.afterEvaluate(p -> {
            final Set<String> dependencies = this.findCThingDependencies.get();
            if (!dependencies.isEmpty()) {
                mavenPom.getProperties().put("cthing.dependencies", String.join(" ", dependencies));
            }

            final Set<String> plugins = this.findCThingGradlePlugins.get();
            if (!plugins.isEmpty()) {
                mavenPom.getProperties().put("cthing.gradle.plugins", String.join(" ", plugins));
            }
        });
    }
}
