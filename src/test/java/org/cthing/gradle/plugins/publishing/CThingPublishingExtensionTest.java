/*
 * Copyright 2025 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.gradle.plugins.publishing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.plugin.devel.PluginDeclaration;
import org.gradle.testfixtures.ProjectBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CThingPublishingExtensionTest {

    @Test
    public void testNormalizeArtifactName() {
        assertThat(CThingPublishingExtension.normalizeArtifactName("artifact")).isEqualTo("artifact");
        assertThat(CThingPublishingExtension.normalizeArtifactName("/home/joe/artifact")).isEqualTo("artifact");
        assertThat(CThingPublishingExtension.normalizeArtifactName("")).isEqualTo("");
    }

    @Test
    public void testIsGradlePlugin() {
        assertThat(CThingPublishingExtension.isGradlePluginMarker("org.cthing.artifact")).isFalse();
        assertThat(CThingPublishingExtension.isGradlePluginMarker("org.cthing.artifact.gradle.plugin")).isTrue();
        assertThat(CThingPublishingExtension.isGradlePluginMarker("")).isFalse();
    }

    private record TestArtifact(String name, @Nullable String classifier, @Nullable String extension,
                                String notation) {
    }

    public static Stream<Arguments> dependencyProvider() {
        return Stream.of(
                Arguments.of(true, "org.joe", "1.2.3",
                             List.of(new TestArtifact("art1", null, null, "org.joe:art1:1.2.3"))),
                Arguments.of(false, "org.cthing", "1.2.3",
                             List.of(new TestArtifact("art1", null, null, "org.cthing:art1:1.2.3"))),
                Arguments.of(false, "com.cthing", "1.2.3",
                             List.of(new TestArtifact("art1", null, null, "com.cthing:art1:1.2.3"),
                                     new TestArtifact("art2", null, null, "com.cthing:art2:1.2.3"))),
                Arguments.of(false, "org.cthing", "1.2.3",
                             List.of(new TestArtifact("art1", "linux", null, "org.cthing:art1:1.2.3:linux"))),
                Arguments.of(false, "org.cthing", "1.2.3",
                             List.of(new TestArtifact("art1", null, "jar", "org.cthing:art1:1.2.3"))),
                Arguments.of(false, "org.cthing", "1.2.3",
                             List.of(new TestArtifact("art1", null, "zip", "org.cthing:art1:1.2.3@zip"))),
                Arguments.of(false, "org.cthing", "1.2.3",
                             List.of(new TestArtifact("art1", null, "", "org.cthing:art1:1.2.3@"))),
                Arguments.of(false, "org.cthing", "1.2.3",
                             List.of(new TestArtifact("art1", "linux", "zip", "org.cthing:art1:1.2.3:linux@zip"))),
                Arguments.of(false, "org.cthing", "1.2.3",
                             List.of(new TestArtifact("/home/joe/art1", null, null, "org.cthing:art1:1.2.3")))
        );
    }

    @ParameterizedTest
    @MethodSource("dependencyProvider")
    public void testRecordDependency(final boolean empty, final String group, final String version,
                                     final List<TestArtifact> artifacts) {
        final Project project = ProjectBuilder.builder().withName("testProject").build();
        project.getPluginManager().apply("org.cthing.cthing-publishing");
        final CThingPublishingExtension publishingExtension = new CThingPublishingExtension(project);

        final ResolvedDependency resolvedDependency = mock(ResolvedDependency.class);
        when(resolvedDependency.getModuleGroup()).thenReturn(group);
        when(resolvedDependency.getModuleVersion()).thenReturn(version);
        when(resolvedDependency.getModuleArtifacts())
                .thenAnswer(invocation -> artifacts.stream()
                                                   .map(artifact -> makeArtifact(artifact.name(),
                                                                                 artifact.classifier(),
                                                                                 artifact.extension()))
                                                   .collect(Collectors.toSet()));

        final Set<String> resolvedDependencies = new HashSet<>();
        publishingExtension.recordDependency(resolvedDependencies, resolvedDependency);
        if (empty) {
            assertThat(resolvedDependencies).isEmpty();
        } else {
            final Set<String> expected = artifacts.stream()
                                                  .map(TestArtifact::notation)
                                                  .collect(Collectors.toSet());
            assertThat(resolvedDependencies).isEqualTo(expected);
        }
    }

    public static Stream<Arguments> signingProvider() {
        return Stream.of(
            Arguments.of(true,  true,  true,  true),
            Arguments.of(true,  true,  false, false),
            Arguments.of(true,  false, true,  false),
            Arguments.of(true,  false, false, false),
            Arguments.of(false, true,  true,  false),
            Arguments.of(false, true,  false, false),
            Arguments.of(false, false, true,  false),
            Arguments.of(false, false, false, false)
        );
    }

    @ParameterizedTest
    @MethodSource("signingProvider")
    public void testCanSign(final boolean hasKeyId, final boolean hasPassword,
                            final boolean hasRingFile, final boolean expected) {
        final Project project = ProjectBuilder.builder().withName("testProject").build();
        project.getPluginManager().apply("org.cthing.cthing-publishing");

        final ExtraPropertiesExtension ext = project.getExtensions().getByType(ExtraPropertiesExtension.class);

        if (hasKeyId) {
            ext.set("signing.keyId", "abcd");
        }
        if (hasPassword) {
            ext.set("signing.password", "efgh");
        }
        if (hasRingFile) {
            ext.set("signing.secretKeyRingFile", "wxyz");
        }

        final CThingPublishingExtension publishingExtension = new CThingPublishingExtension(project);
        assertThat(publishingExtension.canSign()).isEqualTo(expected);
    }

    public static Stream<Arguments> gradlePortalProvider() {
        return Stream.of(
                Arguments.of(true,  true,  true),
                Arguments.of(true,  false, false),
                Arguments.of(false, true,  false),
                Arguments.of(false, false, false)
        );
    }

    @ParameterizedTest
    @MethodSource("gradlePortalProvider")
    public void testHasGradlePluginPortalCredentials(final boolean hasPublishKey, final boolean hasPublishSecret,
                                                     final boolean expected) {
        final Project project = ProjectBuilder.builder().withName("testProject").build();
        project.getPluginManager().apply("org.cthing.cthing-publishing");

        final ExtraPropertiesExtension ext = project.getExtensions().getByType(ExtraPropertiesExtension.class);

        if (hasPublishKey) {
            ext.set("gradle.publish.key", "abcd");
        }
        if (hasPublishSecret) {
            ext.set("gradle.publish.secret", "efgh");
        }

        final CThingPublishingExtension publishingExtension = new CThingPublishingExtension(project);
        assertThat(publishingExtension.hasGradlePluginPortalCredentials()).isEqualTo(expected);
    }

    @Test
    @DisplayName("Not a plugin project")
    public void testFindCThingGradlePlugins1() {
        final Project project = ProjectBuilder.builder().withName("testProject").build();
        project.getPluginManager().apply("org.cthing.cthing-publishing");

        final CThingPublishingExtension publishingExtension = new CThingPublishingExtension(project);
        assertThat(publishingExtension.findCThingGradlePlugins()).isEmpty();
    }

    @Test
    @DisplayName("Plugin project creates no plugins")
    public void testFindCThingGradlePlugins2() {
        final Project project = ProjectBuilder.builder().withName("testProject").build();
        project.getPluginManager().apply("org.cthing.cthing-publishing");
        project.getPluginManager().apply("java-gradle-plugin");

        final CThingPublishingExtension publishingExtension = new CThingPublishingExtension(project);
        assertThat(publishingExtension.findCThingGradlePlugins()).isEmpty();
    }

    @Test
    @DisplayName("Project creates plugins")
    @SuppressWarnings("unchecked")
    public void testFindCThingGradlePlugins3() {
        final PluginDeclaration plugin1 = mock(PluginDeclaration.class);
        when(plugin1.getId()).thenReturn("org.cthing.plugin1");
        final PluginDeclaration plugin2 = mock(PluginDeclaration.class);
        when(plugin2.getId()).thenReturn("com.cthing.plugin2");
        final PluginDeclaration plugin3 = mock(PluginDeclaration.class);
        when(plugin3.getId()).thenReturn("org.foobar.plugin3");

        final NamedDomainObjectContainer<@NonNull PluginDeclaration> plugins = mock(NamedDomainObjectContainer.class);
        when(plugins.stream()).thenReturn(Stream.of(plugin1, plugin2, plugin3));

        final GradlePluginDevelopmentExtension pluginExt = mock(GradlePluginDevelopmentExtension.class);
        when(pluginExt.getPlugins()).thenReturn(plugins);

        final ExtensionContainer extensionContainer = mock(ExtensionContainer.class);
        when(extensionContainer.findByType(GradlePluginDevelopmentExtension.class)).thenReturn(pluginExt);

        final Project project = mock(Project.class);
        when(project.getExtensions()).thenReturn(extensionContainer);

        final CThingPublishingExtension publishingExtension = new CThingPublishingExtension(project);
        assertThat(publishingExtension.findCThingGradlePlugins()).containsExactlyInAnyOrder("org.cthing.plugin1",
                                                                                            "com.cthing.plugin2");
    }

    @Test
    @DisplayName("Project has no dependencies")
    public void testFindCThingDependencies1() {
        final Project project = ProjectBuilder.builder().withName("testProject").build();
        project.getPluginManager().apply("org.cthing.cthing-publishing");

        final CThingPublishingExtension publishingExtension = new CThingPublishingExtension(project);
        assertThat(publishingExtension.findCThingDependencies()).isEmpty();
    }

    @Test
    @DisplayName("Project with only library dependencies")
    public void testFindCThingDependencies2() {
        final ResolvedArtifact resolvedArtifact1 = makeArtifact("art1", null, "jar");
        final ResolvedArtifact resolvedArtifact2 = makeArtifact("art2", "linux", "jar");
        final ResolvedArtifact resolvedArtifact3 = makeArtifact("art3", null, "zip");
        final ResolvedArtifact resolvedArtifact4 = makeArtifact("art4", null, "jar");

        final ResolvedDependency resolvedDependency1 = makeDependency("dep1", "org.cthing", "1.2.3",
                                                                      resolvedArtifact1, resolvedArtifact2);
        final ResolvedDependency resolvedDependency2 = makeDependency("dep2", "com.cthing", "2.0.0", resolvedArtifact3);
        final ResolvedDependency resolvedDependency3 = makeDependency("dep3", "com.foobar", "3.0.0", resolvedArtifact4);

        final ResolvedConfiguration resolvedConfiguration = makeResolvedConfiguration(resolvedDependency1,
                                                                                      resolvedDependency2,
                                                                                      resolvedDependency3);

        final ScriptHandler scriptHandler = mock(ScriptHandler.class);

        final ConfigurationContainer configurationContainer1 = mock(ConfigurationContainer.class);
        when(scriptHandler.getConfigurations()).thenReturn(configurationContainer1);

        final Project project = mock(Project.class);
        when(project.getName()).thenReturn("test");
        when(project.getGroup()).thenReturn("testGroup");
        when(project.getBuildscript()).thenReturn(scriptHandler);

        final ConfigurationContainer configurationContainer2 = mock(ConfigurationContainer.class);
        doAnswer(invocation -> {
            final Consumer<Configuration> consumer = invocation.getArgument(0);
            consumer.accept(makeConfiguration(true, resolvedConfiguration));
            return null;
        }).when(configurationContainer2).forEach(any());
        when(project.getConfigurations()).thenReturn(configurationContainer2);

        final CThingPublishingExtension publishingExtension = new CThingPublishingExtension(project);
        assertThat(publishingExtension.findCThingDependencies())
                .containsExactlyInAnyOrder("com.cthing:art3:2.0.0@zip",
                                           "org.cthing:art1:1.2.3",
                                           "org.cthing:art2:1.2.3:linux");
    }

    @Test
    @DisplayName("Project with only plugin dependencies")
    public void testFindCThingDependencies3() {
        final ResolvedArtifact resolvedArtifact1 = makeArtifact("foo.gradle.plugin", null, "pom");
        final ResolvedArtifact resolvedArtifact2 = makeArtifact("plugins", null, "jar");

        final ResolvedDependency resolvedDependency1 = makeDependency("foo.gradle.plugin", "org.cthing", "1.2.3",
                                                                      resolvedArtifact1);
        final ResolvedDependency resolvedDependency2 = makeDependency("plugins", "org.cthing", "1.2.3",
                                                                      resolvedArtifact2);

        when(resolvedDependency1.getChildren()).thenReturn(Set.of(resolvedDependency2));

        final ResolvedConfiguration resolvedConfiguration = makeResolvedConfiguration(resolvedDependency1);

        final ScriptHandler scriptHandler = mock(ScriptHandler.class);

        final ConfigurationContainer configurationContainer1 = mock(ConfigurationContainer.class);
        doAnswer(invocation -> {
            final Consumer<Configuration> consumer = invocation.getArgument(0);
            consumer.accept(makeConfiguration(true, resolvedConfiguration));
            return null;
        }).when(configurationContainer1).forEach(any());
        when(scriptHandler.getConfigurations()).thenReturn(configurationContainer1);

        final Project project = mock(Project.class);
        when(project.getName()).thenReturn("test");
        when(project.getGroup()).thenReturn("testGroup");
        when(project.getBuildscript()).thenReturn(scriptHandler);

        final ConfigurationContainer configurationContainer2 = mock(ConfigurationContainer.class);
        when(project.getConfigurations()).thenReturn(configurationContainer2);

        final CThingPublishingExtension publishingExtension = new CThingPublishingExtension(project);
        assertThat(publishingExtension.findCThingDependencies()).containsExactlyInAnyOrder("org.cthing:plugins:1.2.3");
    }

    @Test
    @DisplayName("Project with no resolvable configurations")
    public void testFindCThingDependencies4() {
        final ResolvedArtifact resolvedArtifact = makeArtifact("art1", null, "jar");
        final ResolvedDependency resolvedDependency = makeDependency("dep1", "org.cthing", "1.2.3", resolvedArtifact);
        final ResolvedConfiguration resolvedConfiguration = makeResolvedConfiguration(resolvedDependency);

        final ScriptHandler scriptHandler = mock(ScriptHandler.class);

        final ConfigurationContainer configurationContainer1 = mock(ConfigurationContainer.class);
        when(scriptHandler.getConfigurations()).thenReturn(configurationContainer1);

        final Project project = mock(Project.class);
        when(project.getBuildscript()).thenReturn(scriptHandler);

        final ConfigurationContainer configurationContainer2 = mock(ConfigurationContainer.class);
        doAnswer(invocation -> {
            final Consumer<Configuration> consumer = invocation.getArgument(0);
            consumer.accept(makeConfiguration(false, resolvedConfiguration));
            return null;
        }).when(configurationContainer2).forEach(any());
        when(project.getConfigurations()).thenReturn(configurationContainer2);

        final CThingPublishingExtension publishingExtension = new CThingPublishingExtension(project);
        assertThat(publishingExtension.findCThingDependencies()).isEmpty();
    }

    private ResolvedArtifact makeArtifact(final String name, @Nullable final String classifier,
                                          @Nullable final String extension) {
        final ResolvedArtifact artifact = mock(ResolvedArtifact.class);
        when(artifact.getName()).thenReturn(name);
        when(artifact.getClassifier()).thenReturn(classifier);
        when(artifact.getExtension()).thenReturn(extension);
        return artifact;
    }

    private ResolvedDependency makeDependency(final String name, final String group, final String version,
                                              final ResolvedArtifact... artifacts) {
        final ResolvedDependency dependency = mock(ResolvedDependency.class);
        when(dependency.getModuleGroup()).thenReturn(group);
        when(dependency.getModuleName()).thenReturn(name);
        when(dependency.getModuleVersion()).thenReturn(version);
        when(dependency.getModuleArtifacts()).thenReturn(Set.of(artifacts));
        return dependency;
    }

    private ResolvedConfiguration makeResolvedConfiguration(final ResolvedDependency... dependencies) {
        final ResolvedConfiguration configuration = mock(ResolvedConfiguration.class);
        when(configuration.getFirstLevelModuleDependencies()).thenReturn(Set.of(dependencies));
        return configuration;
    }

    private Configuration makeConfiguration(final boolean resolvable,
                                            final ResolvedConfiguration resolvedConfiguration) {
        final Configuration configuration = mock(Configuration.class);
        when(configuration.isCanBeResolved()).thenReturn(resolvable);
        when(configuration.getResolvedConfiguration()).thenReturn(resolvedConfiguration);
        return configuration;
    }
}
