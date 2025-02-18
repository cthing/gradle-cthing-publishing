/*
 * Copyright 2025 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.gradle.plugins.publishing;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.file.PathUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;


public class PublishingPluginIntegTest {
    private static final Path BASE_DIR = Path.of(System.getProperty("buildDir"), "integTest");
    private static final Path WORKING_DIR = Path.of(System.getProperty("projectDir"), "testkit");

    static {
        try {
            Files.createDirectories(BASE_DIR);
            Files.createDirectories(WORKING_DIR);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Path projectDir;

    public static Stream<Arguments> gradleVersionProvider() {
        return Stream.of(
                arguments("8.0"),
                arguments(GradleVersion.current().getVersion())
        );
    }

    @BeforeEach
    public void setup() throws IOException {
        this.projectDir = Files.createTempDirectory(BASE_DIR, "project");

        Files.createDirectories(this.projectDir.resolve(".git"));
        Files.writeString(this.projectDir.resolve(".git/config"),
                          """
                          [core]
                              repositoryformatversion = 0
                              filemode = true
                              bare = false
                              logallrefupdates = true
                          [remote "origin"]
                              url = git@github.com:cthing/hello.git
                              fetch = +refs/heads/*:refs/remotes/origin/*
                          [branch "master"]
                              remote = origin
                              merge = refs/heads/master
                          [gui]
                              wmstate = normal
                              geometry = 2050x1149+28+58 804 393
                          """);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionProvider")
    public void testGeneratePom(final String gradleVersion) throws Exception {
        copyProject("hello");

        final BuildResult result = createGradleRunner(gradleVersion, "generatePomFileForJarPublication").build();
        final BuildTask pomTask = result.task(":generatePomFileForJarPublication");
        assertThat(pomTask).isNotNull();
        assertThat(pomTask.getOutcome()).as(result.getOutput()).isEqualTo(TaskOutcome.SUCCESS);

        final Path pomFile = this.projectDir.resolve("build/publications/jar/pom-default.xml");
        assertThat(pomFile).exists();

        final Document doc = parse(pomFile);
        final XPath xpath = createXPath();

        assertThat(xpath.evaluate("/project/name", doc)).isEqualTo("hello");
        assertThat(xpath.evaluate("/project/description", doc)).isEqualTo("hello world");
        assertThat(xpath.evaluate("/project/url", doc)).isEqualTo("https://github.com/cthing/hello");
        assertThat(xpath.evaluate("/project/organization/name", doc)).isEqualTo("C Thing Software");
        assertThat(xpath.evaluate("/project/organization/url", doc)).isEqualTo("https://www.cthing.com");
        assertThat(xpath.evaluate("/project/licenses/license/name", doc)).isEqualTo("MIT");
        assertThat(xpath.evaluate("/project/licenses/license/url", doc)).isEqualTo("https://opensource.org/license/mit");
        assertThat(xpath.evaluate("/project/developers/developer/id", doc)).isEqualTo("baron");
        assertThat(xpath.evaluate("/project/developers/developer/name", doc)).isEqualTo("Baron Roberts");
        assertThat(xpath.evaluate("/project/developers/developer/email", doc)).isEqualTo("baron@cthing.com");
        assertThat(xpath.evaluate("/project/developers/developer/organization", doc)).isEqualTo("C Thing Software");
        assertThat(xpath.evaluate("/project/developers/developer/organizationUrl", doc)).isEqualTo("https://www.cthing.com");
        assertThat(xpath.evaluate("/project/scm/connection", doc)).isEqualTo("scm:git:git://github.com/cthing/hello.git");
        assertThat(xpath.evaluate("/project/scm/developerConnection", doc)).isEqualTo("scm:git:ssh://git@github.com/cthing/hello.git");
        assertThat(xpath.evaluate("/project/scm/url", doc)).isEqualTo("https://github.com/cthing/hello");
        assertThat(xpath.evaluate("/project/issueManagement/system", doc)).isEqualTo("GitHub Issues");
        assertThat(xpath.evaluate("/project/issueManagement/url", doc)).isEqualTo("https://github.com/cthing/hello/issues");
        assertThat(xpath.evaluate("/project/ciManagement/system", doc)).isEqualTo("GitHub Actions");
        assertThat(xpath.evaluate("/project/ciManagement/url", doc)).isEqualTo("https://github.com/cthing/hello/actions");
        assertThat(xpath.evaluate("/project/properties/cthing.build.number", doc)).matches("\\d+");
        assertThat(xpath.evaluate("/project/properties/cthing.build.date", doc)).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z");
    }

    @SuppressWarnings("SameParameterValue")
    private void copyProject(final String projectName) throws IOException {
        final URL projectUrl = getClass().getResource("/" + projectName);
        assertThat(projectUrl).isNotNull();
        PathUtils.copyDirectory(Path.of(projectUrl.getPath()), this.projectDir);
    }

    private GradleRunner createGradleRunner(final String gradleVersion, final String... arguments) {
        return GradleRunner.create()
                           .withProjectDir(this.projectDir.toFile())
                           .withTestKitDir(WORKING_DIR.toFile())
                           .withArguments(arguments)
                           .withPluginClasspath()
                           .withGradleVersion(gradleVersion);
    }

    private Document parse(final Path xmlFile) throws Exception {
        final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        try (InputStream inputStream = Files.newInputStream(xmlFile)) {
            return docBuilder.parse(new InputSource(inputStream));
        }
    }

    private XPath createXPath() {
        final XPathFactory xPathFactory = XPathFactory.newInstance();
        return xPathFactory.newXPath();
    }
}
