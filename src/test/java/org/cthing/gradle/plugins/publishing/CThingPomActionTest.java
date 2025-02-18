/*
 * Copyright 2025 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.gradle.plugins.publishing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.cthing.projectversion.BuildType;
import org.cthing.projectversion.ProjectVersion;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.XmlProvider;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.publish.maven.MavenPom;
import org.gradle.api.publish.maven.MavenPomCiManagement;
import org.gradle.api.publish.maven.MavenPomContributorSpec;
import org.gradle.api.publish.maven.MavenPomDeveloper;
import org.gradle.api.publish.maven.MavenPomDeveloperSpec;
import org.gradle.api.publish.maven.MavenPomDistributionManagement;
import org.gradle.api.publish.maven.MavenPomIssueManagement;
import org.gradle.api.publish.maven.MavenPomLicense;
import org.gradle.api.publish.maven.MavenPomLicenseSpec;
import org.gradle.api.publish.maven.MavenPomMailingListSpec;
import org.gradle.api.publish.maven.MavenPomOrganization;
import org.gradle.api.publish.maven.MavenPomScm;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CThingPomActionTest {

    private static class TestOrganization implements MavenPomOrganization {

        private final Property<String> name;
        private final Property<String> url;

        TestOrganization(final Project project) {
            this.name = project.getObjects().property(String.class);
            this.url = project.getObjects().property(String.class);
        }

        @Override
        public Property<String> getName() {
            return this.name;
        }

        @Override
        public Property<String> getUrl() {
            return this.url;
        }
    }

    private static class TestLicense implements MavenPomLicense {

        private final Property<String> name;
        private final Property<String> url;
        private final Property<String> distribution;
        private final Property<String> comments;

        TestLicense(final Project project) {
            this.name = project.getObjects().property(String.class);
            this.url = project.getObjects().property(String.class);
            this.distribution = project.getObjects().property(String.class);
            this.comments = project.getObjects().property(String.class);
        }

        @Override
        public Property<String> getName() {
            return this.name;
        }

        @Override
        public Property<String> getUrl() {
            return this.url;
        }

        @Override
        public Property<String> getDistribution() {
            return this.distribution;
        }

        @Override
        public Property<String> getComments() {
            return this.comments;
        }
    }

    private static class TestLicenses implements MavenPomLicenseSpec {

        final List<MavenPomLicense> licenses;

        private final Project project;

        TestLicenses(final Project project) {
            this.project = project;
            this.licenses = new ArrayList<>();
        }

        @Override
        public void license(final Action<? super MavenPomLicense> action) {
            final MavenPomLicense license = new TestLicense(this.project);
            action.execute(license);
            this.licenses.add(license);
        }
    }

    private static class TestDeveloper implements MavenPomDeveloper {

        private final Property<String> id;
        private final Property<String> name;
        private final Property<String> email;
        private final Property<String> url;
        private final Property<String> organization;
        private final Property<String> organizationUrl;
        private final SetProperty<String> roles;
        private final Property<String> timezone;
        private final MapProperty<String, String> properties;

        TestDeveloper(final Project project) {
            this.id = project.getObjects().property(String.class);
            this.name = project.getObjects().property(String.class);
            this.email = project.getObjects().property(String.class);
            this.url = project.getObjects().property(String.class);
            this.organization = project.getObjects().property(String.class);
            this.organizationUrl = project.getObjects().property(String.class);
            this.roles = project.getObjects().setProperty(String.class);
            this.timezone = project.getObjects().property(String.class);
            this.properties = project.getObjects().mapProperty(String.class, String.class);
        }

        @Override
        public Property<String> getId() {
            return this.id;
        }

        @Override
        public Property<String> getName() {
            return this.name;
        }

        @Override
        public Property<String> getEmail() {
            return this.email;
        }

        @Override
        public Property<String> getUrl() {
            return this.url;
        }

        @Override
        public Property<String> getOrganization() {
            return this.organization;
        }

        @Override
        public Property<String> getOrganizationUrl() {
            return this.organizationUrl;
        }

        @Override
        public SetProperty<String> getRoles() {
            return this.roles;
        }

        @Override
        public Property<String> getTimezone() {
            return this.timezone;
        }

        @Override
        public MapProperty<String, String> getProperties() {
            return this.properties;
        }
    }

    private static class TestDevelopers implements MavenPomDeveloperSpec {

        final List<MavenPomDeveloper> developers;

        private final Project project;

        TestDevelopers(final Project project) {
            this.project = project;
            this.developers = new ArrayList<>();
        }

        @Override
        public void developer(final Action<? super MavenPomDeveloper> action) {
            final MavenPomDeveloper developer = new TestDeveloper(this.project);
            action.execute(developer);
            this.developers.add(developer);
        }
    }

    private static class TestScm implements MavenPomScm {

        private final Property<String> connection;
        private final Property<String> developerConnection;
        private final Property<String> url;
        private final Property<String> tag;

        TestScm(final Project project) {
            this.connection = project.getObjects().property(String.class);
            this.developerConnection = project.getObjects().property(String.class);
            this.url = project.getObjects().property(String.class);
            this.tag = project.getObjects().property(String.class);
        }

        @Override
        public Property<String> getConnection() {
            return this.connection;
        }

        @Override
        public Property<String> getDeveloperConnection() {
            return this.developerConnection;
        }

        @Override
        public Property<String> getUrl() {
            return this.url;
        }

        @Override
        public Property<String> getTag() {
            return this.tag;
        }
    }

    private static class TestIssueManagement implements MavenPomIssueManagement {

        private final Property<String> system;
        private final Property<String> url;

        TestIssueManagement(final Project project) {
            this.system = project.getObjects().property(String.class);
            this.url = project.getObjects().property(String.class);
        }

        @Override
        public Property<String> getSystem() {
            return this.system;
        }

        @Override
        public Property<String> getUrl() {
            return this.url;
        }
    }

    private static class TestCIManagement implements MavenPomCiManagement {

        private final Property<String> system;
        private final Property<String> url;

        TestCIManagement(final Project project) {
            this.system = project.getObjects().property(String.class);
            this.url = project.getObjects().property(String.class);
        }

        @Override
        public Property<String> getSystem() {
            return this.system;
        }

        @Override
        public Property<String> getUrl() {
            return this.url;
        }
    }

    private static class TestPom implements MavenPom {

        final TestOrganization organization;
        final TestLicenses licenses;
        final TestDevelopers developers;
        final TestScm scm;
        final TestIssueManagement issueManagement;
        final TestCIManagement ciManagement;

        private final Property<String> name;
        private final Property<String> description;
        private final Property<String> url;
        private final Property<String> inceptionYear;
        private final MapProperty<String, String> properties;

        TestPom(final Project project) {
            this.name = project.getObjects().property(String.class);
            this.description = project.getObjects().property(String.class);
            this.url = project.getObjects().property(String.class);
            this.inceptionYear = project.getObjects().property(String.class);
            this.organization = new TestOrganization(project);
            this.licenses = new TestLicenses(project);
            this.developers = new TestDevelopers(project);
            this.scm = new TestScm(project);
            this.issueManagement = new TestIssueManagement(project);
            this.ciManagement = new TestCIManagement(project);
            this.properties = project.getObjects().mapProperty(String.class, String.class);
        }

        @Override
        public String getPackaging() {
            return "";
        }

        @Override
        public void setPackaging(final String packaging) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Property<String> getName() {
            return this.name;
        }

        @Override
        public Property<String> getDescription() {
            return this.description;
        }

        @Override
        public Property<String> getUrl() {
            return this.url;
        }

        @Override
        public Property<String> getInceptionYear() {
            return this.inceptionYear;
        }

        @Override
        public void organization(final Action<? super MavenPomOrganization> action) {
            action.execute(this.organization);
        }

        @Override
        public void licenses(final Action<? super MavenPomLicenseSpec> action) {
            action.execute(this.licenses);
        }

        @Override
        public void developers(final Action<? super MavenPomDeveloperSpec> action) {
            action.execute(this.developers);
        }

        @Override
        public void contributors(final Action<? super MavenPomContributorSpec> action) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void scm(final Action<? super MavenPomScm> action) {
            action.execute(this.scm);
        }

        @Override
        public void issueManagement(final Action<? super MavenPomIssueManagement> action) {
            action.execute(this.issueManagement);
        }

        @Override
        public void ciManagement(final Action<? super MavenPomCiManagement> action) {
            action.execute(this.ciManagement);
        }

        @Override
        public void distributionManagement(final Action<? super MavenPomDistributionManagement> action) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void mailingLists(final Action<? super MavenPomMailingListSpec> action) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MapProperty<String, String> getProperties() {
            return this.properties;
        }

        @Override
        public void withXml(final Action<? super XmlProvider> action) {
            throw new UnsupportedOperationException();
        }
    }

    private Project project;
    private CThingPublishingExtension extension;

    @BeforeEach
    public void setup() throws IOException {
        this.project = ProjectBuilder.builder().build();
        this.extension = mock(CThingPublishingExtension.class);

        final Path projectDir = this.project.getProjectDir().toPath();
        Files.createDirectories(projectDir.resolve(".git"));
        Files.writeString(projectDir.resolve(".git/config"),
                          """
                          [core]
                              repositoryformatversion = 0
                              filemode = true
                              bare = false
                              logallrefupdates = true
                          [remote "origin"]
                              url = git@github.com:cthing/test.git
                              fetch = +refs/heads/*:refs/remotes/origin/*
                          [branch "master"]
                              remote = origin
                              merge = refs/heads/master
                          [gui]
                              wmstate = normal
                              geometry = 2050x1149+28+58 804 393
                          """);
    }

    @Test
    public void testMinimumMetadata() {
        final CThingPomAction action = new CThingPomAction(this.project, this.extension::findCThingDependencies,
                                                           this.extension::findCThingGradlePlugins);

        final TestPom pom = new TestPom(this.project);
        action.execute(pom);

        assertThat(pom).isNotNull();
        assertThat(pom.getName().getOrNull()).isEqualTo("test");
        assertThat(pom.getDescription().getOrNull()).isNull();
        assertThat(pom.getUrl().getOrNull()).isEqualTo("https://github.com/cthing/test");
        assertThat(pom.organization).satisfies(org -> {
            assertThat(org.getName().getOrNull()).isEqualTo("C Thing Software");
            assertThat(org.getUrl().getOrNull()).isEqualTo("https://www.cthing.com");
        });
        assertThat(pom.licenses.licenses).hasSize(1).satisfies(license -> {
            assertThat(license.getName().getOrNull()).isEqualTo(PomLicense.ASL2.getName());
            assertThat(license.getUrl().getOrNull()).isEqualTo(PomLicense.ASL2.getUrl());
        }, atIndex(0));
        assertThat(pom.developers.developers).hasSize(1).satisfies(developer -> {
            assertThat(developer.getId().getOrNull()).isEqualTo("baron");
            assertThat(developer.getName().getOrNull()).isEqualTo("Baron Roberts");
            assertThat(developer.getEmail().getOrNull()).isEqualTo("baron@cthing.com");
            assertThat(developer.getOrganization().getOrNull()).isEqualTo("C Thing Software");
            assertThat(developer.getOrganizationUrl().getOrNull()).isEqualTo("https://www.cthing.com");
        }, atIndex(0));
        assertThat(pom.scm).satisfies(scm -> {
            assertThat(scm.getConnection().getOrNull()).isEqualTo("scm:git:git://github.com/cthing/test.git");
            assertThat(scm.getDeveloperConnection().getOrNull()).isEqualTo("scm:git:ssh://git@github.com/cthing/test.git");
            assertThat(scm.getUrl().getOrNull()).isEqualTo("https://github.com/cthing/test");
        });
        assertThat(pom.issueManagement).satisfies(issueManagement -> {
            assertThat(issueManagement.getUrl().getOrNull()).isEqualTo("https://github.com/cthing/test/issues");
            assertThat(issueManagement.getSystem().getOrNull()).isEqualTo("GitHub Issues");
        });
        assertThat(pom.ciManagement).satisfies(ciManagement -> {
            assertThat(ciManagement.getUrl().getOrNull()).isEqualTo("https://github.com/cthing/test/actions");
            assertThat(ciManagement.getSystem().getOrNull()).isEqualTo("GitHub Actions");
        });
        assertThat(pom.getProperties().getOrNull()).isEmpty();
    }

    @Test
    public void testWithProjectVersion() {
        final ProjectVersion version = new ProjectVersion("1.2.3", BuildType.snapshot);
        this.project.setVersion(version);
        final CThingPomAction action = new CThingPomAction(this.project, this.extension::findCThingDependencies,
                                                           this.extension::findCThingGradlePlugins);

        final TestPom pom = new TestPom(this.project);
        action.execute(pom);

        assertThat(pom.getProperties().getOrNull())
                .containsEntry("cthing.build.date", version.getBuildDate())
                .containsEntry("cthing.build.number", version.getBuildNumber());
    }

    @Test
    public void testWithDescription() {
        this.project.setDescription("Hello world");
        final CThingPomAction action = new CThingPomAction(this.project, this.extension::findCThingDependencies,
                                                           this.extension::findCThingGradlePlugins);

        final TestPom pom = new TestPom(this.project);
        action.execute(pom);

        assertThat(pom.getDescription().getOrNull()).isEqualTo("Hello world");
    }

    @Test
    public void testWithLicense() {
        final CThingPomAction action = new CThingPomAction(this.project, this.extension::findCThingDependencies,
                                                           this.extension::findCThingGradlePlugins);
        action.setLicense(PomLicense.INTERNAL);
        assertThat(action.getLicense()).isEqualTo(PomLicense.INTERNAL);

        final TestPom pom = new TestPom(this.project);
        action.execute(pom);

        assertThat(pom.licenses.licenses).hasSize(1).satisfies(license -> {
            assertThat(license.getName().getOrNull()).isEqualTo(PomLicense.INTERNAL.getName());
            assertThat(license.getUrl().getOrNull()).isEqualTo(PomLicense.INTERNAL.getUrl());
        }, atIndex(0));
    }

    @Test
    public void testWithCiSystem() {
        final CThingPomAction action = new CThingPomAction(this.project, this.extension::findCThingDependencies,
                                                           this.extension::findCThingGradlePlugins);
        action.setCiSystem(PomCISystem.CThingJenkins);
        assertThat(action.getCiSystem()).isEqualTo(PomCISystem.CThingJenkins);

        final TestPom pom = new TestPom(this.project);
        action.execute(pom);

        assertThat(pom.ciManagement).satisfies(ciManagement -> {
            assertThat(ciManagement.getUrl().getOrNull()).isNull();
            assertThat(ciManagement.getSystem().getOrNull()).isEqualTo("C Thing Software Jenkins");
        });
    }

    @Test
    public void testWithoutCiSystem() {
        final CThingPomAction action = new CThingPomAction(this.project, this.extension::findCThingDependencies,
                                                           this.extension::findCThingGradlePlugins);
        action.setCiSystem(PomCISystem.None);
        assertThat(action.getCiSystem()).isEqualTo(PomCISystem.None);

        final TestPom pom = new TestPom(this.project);
        action.execute(pom);

        assertThat(pom.ciManagement).satisfies(ciManagement -> {
            assertThat(ciManagement.getUrl().getOrNull()).isNull();
            assertThat(ciManagement.getSystem().getOrNull()).isNull();
        });
    }

    @Test
    public void testWithoutDevelopers() {
        final CThingPomAction action = new CThingPomAction(this.project, this.extension::findCThingDependencies,
                                                           this.extension::findCThingGradlePlugins);
        action.setDevelopers(Set.of());
        assertThat(action.getDevelopers()).isEmpty();

        final TestPom pom = new TestPom(this.project);
        action.execute(pom);

        assertThat(pom.developers.developers).isEmpty();
    }

    @Test
    public void testWithDevelopers() {
        final PomDeveloper developer = new PomDeveloper("a", "b", "c");
        final CThingPomAction action = new CThingPomAction(this.project, this.extension::findCThingDependencies,
                                                           this.extension::findCThingGradlePlugins);
        action.setDevelopers(Set.of(developer));
        assertThat(action.getDevelopers()).containsExactly(developer);

        final TestPom pom = new TestPom(this.project);
        action.execute(pom);

        assertThat(pom.developers.developers).satisfiesExactly(first -> {
            assertThat(first.getId().get()).isEqualTo("a");
            assertThat(first.getName().get()).isEqualTo("b");
            assertThat(first.getEmail().get()).isEqualTo("c");
        });
    }

    @Test
    public void testWithAddedDevelopers() {
        final CThingPomAction action = new CThingPomAction(this.project, this.extension::findCThingDependencies,
                                                           this.extension::findCThingGradlePlugins);
        action.addDeveloper(new PomDeveloper("a", "b", "c"));
        assertThat(action.getDevelopers()).hasSize(2);

        final TestPom pom = new TestPom(this.project);
        action.execute(pom);

        assertThat(pom.developers.developers).satisfiesExactly(first -> {
            assertThat(first.getId().get()).isEqualTo("a");
            assertThat(first.getName().get()).isEqualTo("b");
            assertThat(first.getEmail().get()).isEqualTo("c");
        }, second -> {
            assertThat(second.getId().get()).isEqualTo("baron");
            assertThat(second.getName().get()).isEqualTo("Baron Roberts");
            assertThat(second.getEmail().get()).isEqualTo("baron@cthing.com");
        });
    }

    @Test
    public void testWithPlugins() {
        final CThingPomAction action = new CThingPomAction(this.project, this.extension::findCThingDependencies,
                                                           this.extension::findCThingGradlePlugins);

        final Set<String> plugins = new TreeSet<>();
        plugins.add("a");
        plugins.add("b");
        plugins.add("c");
        when(this.extension.findCThingGradlePlugins()).thenReturn(plugins);

        final TestPom pom = new TestPom(this.project);
        action.execute(pom);

        assertThat(pom.getProperties().getOrNull()).containsEntry("cthing.gradle.plugins", "a b c");
    }

    @Test
    public void testWithDependencies() {
        final CThingPomAction action = new CThingPomAction(this.project, this.extension::findCThingDependencies,
                                                           this.extension::findCThingGradlePlugins);

        final Set<String> dependencies = new TreeSet<>();
        dependencies.add("a");
        dependencies.add("b");
        dependencies.add("c");
        when(this.extension.findCThingDependencies()).thenReturn(dependencies);

        final TestPom pom = new TestPom(this.project);
        action.execute(pom);

        assertThat(pom.getProperties().getOrNull()).containsEntry("cthing.dependencies", "a b c");
    }
}
