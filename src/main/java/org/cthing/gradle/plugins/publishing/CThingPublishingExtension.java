/*
 * Copyright 2025 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.gradle.plugins.publishing;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.publish.maven.MavenPom;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.plugin.devel.PluginDeclaration;


/**
 * Provides methods for generating information used in publishing C Thing Software artifacts.
 */
public class CThingPublishingExtension {

    private static final String GRADLE_PLUGIN_SUFFIX = ".gradle.plugin";
    private static final Set<String> CTHING_GROUPS = Set.of("org.cthing", "com.cthing");

    private final Project project;

    public CThingPublishingExtension(final Project project) {
        this.project = project;
    }

    /**
     * Creates a new instance of an {@link Action} which populates a {@link MavenPom} with C Thing Software publishing
     * information.
     *
     * @return New {@link MavenPom} {@link Action} instance.
     */
    public CThingPomAction createPomAction() {
        return new CThingPomAction(this.project, this::findCThingDependencies, this::findCThingGradlePlugins);
    }

    /**
     * Obtains all directs dependencies on C Thing Software artifacts. This information is used in CI to
     * determine dependent projects.
     *
     * @return Direct dependencies on C Thing Software artifacts in Gradle dependency notation:
     *     {@code group:name:version:classifier@extension}. If the project has no dependencies on
     *     C Thing Software artifacts, an empty set is returned.
     */
    public Set<String> findCThingDependencies() {
        final Set<String> resolvedDependencies = new TreeSet<>();

        // Obtain direct dependencies from both the compile configurations and the build script configurations.
        // The latter provides dependencies on C Thing Software Gradle plugins.
        final List<ConfigurationContainer> configContainers = List.of(this.project.getBuildscript().getConfigurations(),
                                                                      this.project.getConfigurations());
        configContainers.forEach(configContainer -> configContainer.forEach(config -> {
            if (config.isCanBeResolved()) {
                config.getResolvedConfiguration()
                      .getFirstLevelModuleDependencies()
                      .forEach(rdep -> {
                          // If the dependency is a Gradle plugin marker, go one level down to get the plugin
                          // artifact dependency.
                          if (isGradlePluginMarker(rdep.getModuleName())) {
                              rdep.getChildren().forEach(child -> recordDependency(resolvedDependencies, child));
                          } else {
                              recordDependency(resolvedDependencies, rdep);
                          }
                      });
            }
        }));

        return resolvedDependencies;
    }

    /**
     * Obtains the identifiers of any Gradle plugins created by the project.
     *
     * @return Identifiers of Gradle plugins created by the project. If no plugins are created,
     *      an empty set is returned.
     */
    public Set<String> findCThingGradlePlugins() {
        final Set<String> plugins = new TreeSet<>();
        final GradlePluginDevelopmentExtension gradlePluginDev =
                this.project.getExtensions().findByType(GradlePluginDevelopmentExtension.class);
        if (gradlePluginDev != null) {
            gradlePluginDev.getPlugins()
                           .stream()
                           .map(PluginDeclaration::getId)
                           .filter(id -> CTHING_GROUPS.stream().anyMatch(id::startsWith))
                           .forEach(plugins::add);
        }
        return plugins;
    }

    /**
     * Indicates whether artifacts can be signed. An artifact can be signed if the {@code signing.keyId},
     * {@code signing.password} and {@code signing.secretKeyRingFile} properties are defined.
     *
     * @return {@code true} if artifacts can be signed.
     */
    public boolean canSign() {
        return this.project.hasProperty("signing.keyId")
                && this.project.hasProperty("signing.password")
                && this.project.hasProperty("signing.secretKeyRingFile");
    }

    /**
     * Indicates whether the credentials are present to publish a Gradle plugin to Gradle's plugin portal site.
     *
     * @return {@code true} if it is possible to publish to the Gradle plugin portal.
     */
    public boolean hasGradlePluginPortalCredentials() {
        return this.project.hasProperty("gradle.publish.key") && this.project.hasProperty("gradle.publish.secret");
    }

    /**
     * If the specified resolved dependency is a C Thing Software artifact, this method formats it into Gradle
     * dependency notation and adds it to the specified set of dependencies.
     *
     * @param resolvedDependencies Resolved dependencies to which the specified dependency should be added, if
     *      it is a C Thing Software artifact.
     * @param resolvedDependency Resolved dependency to consider adding to the set of dependencies
     */
    void recordDependency(final Set<String> resolvedDependencies, final ResolvedDependency resolvedDependency) {
        final String group = resolvedDependency.getModuleGroup();

        // Only record the dependency if it is on a C Thing Software artifact and is not on the project itself
        // (e.g. the dependency analysis plugin creates dependencies on the project itself).
        if (CTHING_GROUPS.contains(group)
                && !(this.project.getGroup().equals(group)
                && this.project.getName().equals(resolvedDependency.getModuleName()))) {
            final String version = resolvedDependency.getModuleVersion();
            resolvedDependency.getModuleArtifacts().forEach(artifact -> {
                final StringBuilder dependency = new StringBuilder()
                        .append(group)
                        .append(':')
                        .append(normalizeArtifactName(artifact.getName()))
                        .append(':')
                        .append(version);

                final String classifier = artifact.getClassifier();
                if (classifier != null) {
                    dependency.append(':').append(classifier);
                }

                final String extension = artifact.getExtension();
                if (extension != null && !"jar".equals(extension)) {
                    dependency.append('@').append(extension);
                }

                resolvedDependencies.add(dependency.toString());
            });
        }
    }

    /**
     * In order to resolve Gradle plugin identifiers to their implementation artifacts, Gradle uses a
     * marker dependency. The marker dependencies always have a name ending in ".gradle.plugin" and have
     * only one dependency, the plugin implementation artifact. This method indicates whether a given
     * dependency name represents a Gradle plugin marker.
     *
     * @param depName Name of the dependency to test
     * @return {@code true} if the specified dependency name represents a Gradle plugin marker
     */
    static boolean isGradlePluginMarker(final String depName) {
        return depName.endsWith(GRADLE_PLUGIN_SUFFIX);
    }

    /**
     * Certain plugins such as the IntelliJ Platform Gradle Plugin use the absolute path to an
     * artifact in the Gradle cache for the name of the artifact. This results in a string that
     * is meaningless on other systems. If an absolute path is provided, shorten it to the filename
     * (i.e. last component of the path).
     *
     * @param artifactName Name of the artifact to normalize
     * @return If the name is an absolute path, shorten it to the filename. Otherwise, return the
     *      artifact name unchanged.
     */
    static String normalizeArtifactName(final String artifactName) {
        return artifactName.startsWith(File.separator) ? new File(artifactName).getName() : artifactName;
    }
}
