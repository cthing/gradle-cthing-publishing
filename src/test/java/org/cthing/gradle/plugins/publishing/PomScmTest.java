/*
 * Copyright 2025 C Thing Software
 * All rights reserved.
 */
package org.cthing.gradle.plugins.publishing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import static org.assertj.core.api.Assertions.assertThat;


public class PomScmTest {

    @Test
    public void testNoConfig() {
        final Project project = ProjectBuilder.builder().build();
        final PomScm scm = new PomScm(project);
        assertThat(scm.isPresent()).isFalse();
        assertThat(scm.getReadOnly().isPresent()).isFalse();
        assertThat(scm.getReadWrite().isPresent()).isFalse();
        assertThat(scm.getBrowse().isPresent()).isFalse();
        assertThat(scm.getOriginalUrl().isPresent()).isFalse();
        assertThat(scm).hasToString("<empty>");
    }

    @Test
    public void testNoRemote() throws IOException {
        final Project project = ProjectBuilder.builder().build();
        final Path projectDir = project.getProjectDir().toPath();
        Files.createDirectories(projectDir.resolve(".git"));
        Files.writeString(projectDir.resolve(".git/config"),
                          """
                          [core]
                              repositoryformatversion = 0
                              filemode = true
                              bare = false
                              logallrefupdates = true
                          [gui]
                              wmstate = normal
                              geometry = 2050x1149+28+58 804 393
                          """);
        final PomScm scm = new PomScm(project);
        assertThat(scm.isPresent()).isFalse();
        assertThat(scm.getReadOnly().isPresent()).isFalse();
        assertThat(scm.getReadWrite().isPresent()).isFalse();
        assertThat(scm.getBrowse().isPresent()).isFalse();
        assertThat(scm.getOriginalUrl().isPresent()).isFalse();
        assertThat(scm).hasToString("<empty>");
    }

    @Test
    public void testNoRemoteUrl() throws IOException {
        final Project project = ProjectBuilder.builder().build();
        final Path projectDir = project.getProjectDir().toPath();
        Files.createDirectories(projectDir.resolve(".git"));
        Files.writeString(projectDir.resolve(".git/config"),
                          """
                          [core]
                              repositoryformatversion = 0
                              filemode = true
                              bare = false
                              logallrefupdates = true
                          [remote "origin"]
                              fetch = +refs/heads/*:refs/remotes/origin/*
                          [gui]
                              wmstate = normal
                              geometry = 2050x1149+28+58 804 393
                          """);
        final PomScm scm = new PomScm(project);
        assertThat(scm.isPresent()).isFalse();
        assertThat(scm.getReadOnly().isPresent()).isFalse();
        assertThat(scm.getReadWrite().isPresent()).isFalse();
        assertThat(scm.getBrowse().isPresent()).isFalse();
        assertThat(scm.getOriginalUrl().isPresent()).isFalse();
        assertThat(scm).hasToString("<empty>");
    }

    public static Stream<Arguments> urlProvider() {
        return Stream.of(
                Arguments.of("git@github.com:cthing/myproject.git",
                             "scm:git:git://github.com/cthing/myproject.git",
                             "scm:git:ssh://git@github.com/cthing/myproject.git",
                             "https://github.com/cthing/myproject"),
                Arguments.of("ssh://www.host.com/dir1/dir2/repo.git",
                             "scm:git:git://www.host.com/dir1/dir2/repo.git",
                             "scm:git:ssh://www.host.com/dir1/dir2/repo.git",
                             "https://www.host.com/dir1/dir2/repo"),
                Arguments.of("ssh://joe@www.host.com:8080/dir1/dir2/repo.git",
                             "scm:git:git://www.host.com:8080/dir1/dir2/repo.git",
                             "scm:git:ssh://joe@www.host.com:8080/dir1/dir2/repo.git",
                             "https://www.host.com/dir1/dir2/repo"),
                Arguments.of("git+ssh://joe@www.host.com/joe/dir1/dir2/repo.git",
                             "scm:git:git://www.host.com/joe/dir1/dir2/repo.git",
                             "scm:git:ssh://joe@www.host.com/joe/dir1/dir2/repo.git",
                             "https://www.host.com/joe/dir1/dir2/repo"),
                Arguments.of("git://www.host.com/dir1/dir2/repo.git",
                             "scm:git:git://www.host.com/dir1/dir2/repo.git",
                             "scm:git:git://www.host.com/dir1/dir2/repo.git",
                             "https://www.host.com/dir1/dir2/repo"),
                Arguments.of("https://www.host.com/dir1/dir2/repo.git",
                             "scm:git:https://www.host.com/dir1/dir2/repo.git",
                             "scm:git:https://www.host.com/dir1/dir2/repo.git",
                             "https://www.host.com/dir1/dir2/repo"),
                Arguments.of("/dir1/dir2/repo",
                             "scm:git:file:///dir1/dir2/repo",
                             "scm:git:file:///dir1/dir2/repo",
                             "file:///dir1/dir2/repo")
        );
    }

    @ParameterizedTest
    @MethodSource("urlProvider")
    public void testRemoteUrls(final String url, final String readOnly, final String readWrite, final String browse)
            throws IOException {
        final Project project = ProjectBuilder.builder().build();
        final Path projectDir = project.getProjectDir().toPath();
        Files.createDirectories(projectDir.resolve(".git"));
        Files.writeString(projectDir.resolve(".git/config"),
                          """
                          [core]
                              repositoryformatversion = 0
                              filemode = true
                              bare = false
                              logallrefupdates = true
                          [remote "origin"]
                              url = %s
                              fetch = +refs/heads/*:refs/remotes/origin/*
                          [branch "master"]
                              remote = origin
                              merge = refs/heads/master
                          [gui]
                              wmstate = normal
                              geometry = 2050x1149+28+58 804 393
                          """.formatted(url));
        final PomScm scm = new PomScm(project);
        assertThat(scm.isPresent()).isTrue();
        assertThat(scm.getReadOnly().get()).isEqualTo(readOnly);
        assertThat(scm.getReadWrite().get()).isEqualTo(readWrite);
        assertThat(scm.getBrowse().get()).isEqualTo(browse);
        assertThat(scm.getOriginalUrl().get()).isEqualTo(url);
        assertThat(scm).hasToString(url);
    }

    @Test
    public void testEquality() {
        EqualsVerifier.forClass(PomScm.class)
                      .usingGetClass()
                      .suppress(Warning.NONFINAL_FIELDS, Warning.ALL_FIELDS_SHOULD_BE_USED)
                      .verify();
    }
}
