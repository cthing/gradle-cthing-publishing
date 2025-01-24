/*
 * Copyright 2025 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.gradle.plugins.publishing;

import org.cthing.projectversion.ProjectVersion;
import org.gradle.api.Project;
import org.jspecify.annotations.Nullable;


/**
 * Provides information about the internal C Thing Software repository.
 */
public class CThingRepoExtension {

    /** Property providing the username to access the repository. */
    public static final String USER_PROPERTY = "cthing.nexus.user";

    /** Property providing the password to access the repository. */
    public static final String PASSWORD_PROPERTY = "cthing.nexus.password";

    /** Property providing the URL to download artifacts. */
    public static final String DOWNLOAD_URL_PROPERTY = "cthing.nexus.downloadUrl";

    /** Property providing the URL to publish release artifacts. */
    public static final String RELEASES_URL_PROPERTY = "cthing.nexus.releasesUrl";

    /** Property providing the URL to publish release candidate artifacts. */
    public static final String CANDIDATES_URL_PROPERTY = "cthing.nexus.candidatesUrl";

    /** Property providing the URL to publish snapshot artifacts. */
    public static final String SNAPSHOTS_URL_PROPERTY = "cthing.nexus.snapshotsUrl";

    /** Property providing the URL to publish release Debian packages. */
    public static final String APT_RELEASES_URL_PROPERTY = "cthing.nexus.aptReleasesUrl";

    /** Property providing the URL to publish release candidate Debian packages. */
    public static final String APT_CANDIDATES_URL_PROPERTY = "cthing.nexus.aptCandidatesUrl";

    /** Property providing the URL to publish snapshot Debian packages. */
    public static final String APT_SNAPSHOTS_URL_PROPERTY = "cthing.nexus.aptSnapshotsUrl";

    /** Property providing the URL to publish a Maven site. */
    public static final String SITE_URL_PROPERTY = "cthing.nexus.sitesUrl";

    private final Project project;

    public CThingRepoExtension(final Project project) {
        this.project = project;
    }

    /**
     * Obtains the username to access the repository.
     *
     * @return Username to access the repository.
     */
    @Nullable
    public String getUser() {
        return (String)this.project.findProperty(USER_PROPERTY);
    }

    /**
     * Obtains the password to access the repository.
     *
     * @return Password to access the repository.
     */
    @Nullable
    public String getPassword() {
        return (String)this.project.findProperty(PASSWORD_PROPERTY);
    }

    /**
     * Indicates whether the properties are defined to allow access to the repository.
     *
     * @return {@code true} if the properties are defined to allow access to the repository.
     */
    public boolean hasCredentials() {
        return this.project.hasProperty(USER_PROPERTY) && this.project.hasProperty(PASSWORD_PROPERTY);
    }

    /**
     * Obtains the URL to download artifacts.
     *
     * @return URL to download artifacts.
     */
    @Nullable
    public String getDownloadUrl() {
        return (String)this.project.findProperty(DOWNLOAD_URL_PROPERTY);
    }

    /**
     * Obtains the URL to publish release artifacts.
     *
     * @return URL to publish release artifacts.
     */
    @Nullable
    public String getReleasesUrl() {
        return (String)this.project.findProperty(RELEASES_URL_PROPERTY);
    }

    /**
     * Obtains the URL to publish release candidate artifacts.
     *
     * @return URL to publish release candidate artifacts.
     */
    @Nullable
    public String getCandidatesUrl() {
        return (String)this.project.findProperty(CANDIDATES_URL_PROPERTY);
    }

    /**
     * Obtains the URL to publish snapshot artifacts.
     *
     * @return URL to publish snapshot artifacts.
     */
    @Nullable
    public String getSnapshotsUrl() {
        return (String)this.project.findProperty(SNAPSHOTS_URL_PROPERTY);
    }

    /**
     * Obtains the URL to publish artifacts to either the snapshot or release candidate repository based
     * on the project version.
     *
     * @return URL to publish artifacts based on the project version.
     */
    @Nullable
    public String getRepoUrl() {
        if (this.project.getVersion() instanceof ProjectVersion projectVersion) {
            return projectVersion.isSnapshotBuild() ? getSnapshotsUrl() : getCandidatesUrl();
        }
        return null;
    }

    /**
     * Obtains the URL to publish release Debian packages.
     *
     * @return URL to publish release Debian packages.
     */
    @Nullable
    public String getAptReleasesUrl() {
        return (String)this.project.findProperty(APT_RELEASES_URL_PROPERTY);
    }

    /**
     * Obtains the URL to publish release candidate Debian packages.
     *
     * @return URL to publish release candidate Debian packages.
     */
    @Nullable
    public String getAptCandidatesUrl() {
        return (String)this.project.findProperty(APT_CANDIDATES_URL_PROPERTY);
    }

    /**
     * Obtains the URL to publish snapshot Debian packages.
     *
     * @return URL to publish snapshot Debian packages.
     */
    @Nullable
    public String getAptSnapshotsUrl() {
        return (String)this.project.findProperty(APT_SNAPSHOTS_URL_PROPERTY);
    }

    /**
     * Obtains the URL to publish a Maven site.
     *
     * @return URL to publish a Maven site.
     */
    @Nullable
    public String getSiteUrl() {
        return (String)this.project.findProperty(SITE_URL_PROPERTY);
    }
}
