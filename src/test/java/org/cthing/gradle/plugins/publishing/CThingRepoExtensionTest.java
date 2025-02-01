/*
 * Copyright 2025 C Thing Software
 * All rights reserved.
 */
package org.cthing.gradle.plugins.publishing;

import java.util.stream.Stream;

import org.cthing.projectversion.BuildType;
import org.cthing.projectversion.ProjectVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;


public class CThingRepoExtensionTest {

    @Test
    public void testNoProperties() {
        final Project project = ProjectBuilder.builder().build();
        final CThingRepoExtension extension = new CThingRepoExtension(project);

        assertThat(extension.getUser()).isNull();
        assertThat(extension.getPassword()).isNull();
        assertThat(extension.hasCredentials()).isFalse();
        assertThat(extension.getDownloadUrl()).isNull();
        assertThat(extension.getReleasesUrl()).isNull();
        assertThat(extension.getCandidatesUrl()).isNull();
        assertThat(extension.getSnapshotsUrl()).isNull();
        assertThat(extension.getRepoUrl()).isNull();
        assertThat(extension.getAptReleasesUrl()).isNull();
        assertThat(extension.getAptCandidatesUrl()).isNull();
        assertThat(extension.getAptSnapshotsUrl()).isNull();
        assertThat(extension.getSiteUrl()).isNull();
    }

    @Test
    public void testWithProperties() {
        final Project project = ProjectBuilder.builder().build();
        final ExtraPropertiesExtension properties = project.getExtensions().getExtraProperties();
        final CThingRepoExtension extension = new CThingRepoExtension(project);

        final String user = "cthing";
        final String password = "123456";
        final String downloadUrl = "https://github.com/cthing/downloads";
        final String releasesUrl = "https://github.com/cthing/releases";
        final String candidatesUrl = "https://github.com/cthing/candidates";
        final String snapshotsUrl = "https://github.com/cthing/snapshots";
        final String aptReleasesUrl = "https://github.com/cthing/apt/releases";
        final String aptCandidatesUrl = "https://github.com/cthing/apt/candidates";
        final String aptSnapshotsUrl = "https://github.com/cthing/apt/snapshots";
        final String siteUrl = "https://github.com/cthing/site";

        properties.set(CThingRepoExtension.USER_PROPERTY, user);
        properties.set(CThingRepoExtension.PASSWORD_PROPERTY, password);
        properties.set(CThingRepoExtension.DOWNLOAD_URL_PROPERTY, downloadUrl);
        properties.set(CThingRepoExtension.RELEASES_URL_PROPERTY, releasesUrl);
        properties.set(CThingRepoExtension.CANDIDATES_URL_PROPERTY, candidatesUrl);
        properties.set(CThingRepoExtension.SNAPSHOTS_URL_PROPERTY, snapshotsUrl);
        properties.set(CThingRepoExtension.APT_RELEASES_URL_PROPERTY, aptReleasesUrl);
        properties.set(CThingRepoExtension.APT_CANDIDATES_URL_PROPERTY, aptCandidatesUrl);
        properties.set(CThingRepoExtension.APT_SNAPSHOTS_URL_PROPERTY, aptSnapshotsUrl);
        properties.set(CThingRepoExtension.SITE_URL_PROPERTY, siteUrl);

        assertThat(extension.getUser()).isEqualTo(user);
        assertThat(extension.getPassword()).isEqualTo(password);
        assertThat(extension.hasCredentials()).isTrue();
        assertThat(extension.getDownloadUrl()).isEqualTo(downloadUrl);
        assertThat(extension.getReleasesUrl()).isEqualTo(releasesUrl);
        assertThat(extension.getCandidatesUrl()).isEqualTo(candidatesUrl);
        assertThat(extension.getSnapshotsUrl()).isEqualTo(snapshotsUrl);
        assertThat(extension.getAptReleasesUrl()).isEqualTo(aptReleasesUrl);
        assertThat(extension.getAptCandidatesUrl()).isEqualTo(aptCandidatesUrl);
        assertThat(extension.getAptSnapshotsUrl()).isEqualTo(aptSnapshotsUrl);
        assertThat(extension.getSiteUrl()).isEqualTo(siteUrl);

        final ProjectVersion version = new ProjectVersion("1.2.3", BuildType.snapshot);
        project.setVersion(version);
        if (version.isSnapshotBuild()) {
            assertThat(extension.getRepoUrl()).isEqualTo(snapshotsUrl);
        } else {
            assertThat(extension.getRepoUrl()).isEqualTo(candidatesUrl);
        }
    }

    public static Stream<Arguments> hashProvider() {
        return Stream.of(
                Arguments.of(true,  true,  true),
                Arguments.of(true,  false, false),
                Arguments.of(false, true,  false),
                Arguments.of(false, false, false)
        );
    }

    @ParameterizedTest
    @MethodSource("hashProvider")
    public void testHashCredentials(final boolean hasUser, final boolean hasPassword, final boolean result) {
        final Project project = ProjectBuilder.builder().build();
        final ExtraPropertiesExtension properties = project.getExtensions().getExtraProperties();
        final CThingRepoExtension extension = new CThingRepoExtension(project);

        if (hasUser) {
            properties.set(CThingRepoExtension.USER_PROPERTY, "joe");
        }
        if (hasPassword) {
            properties.set(CThingRepoExtension.PASSWORD_PROPERTY, "1234567");
        }

        assertThat(extension.hasCredentials()).isEqualTo(result);
    }
}
