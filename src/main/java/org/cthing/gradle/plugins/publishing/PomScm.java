/*
 * Copyright 2025 C Thing Software
 * All rights reserved.
 */
package org.cthing.gradle.plugins.publishing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.jspecify.annotations.Nullable;


/**
 * Provides the source code management URLs. Assumes that Git is being used and parses the
 * {@code .git/config} file to determine the origin URL. The URL is parsed so that an
 * appropriate URL for the POM {@code scm} section can be generated.
 */
public class PomScm {

    private static final Pattern REMOTE_SECTION_REGEX = Pattern.compile("\\s*\\[remote\\s+\".+\"]");
    private static final Pattern SECTION_START_REGEX = Pattern.compile("\\s*\\[");
    private static final Pattern REMOTE_URL_REGEX = Pattern.compile("\\s*url\\s*=\\s*(\\S+)");
    private static final Pattern GIT_EXTENSION_REGEX = Pattern.compile("\\.git$");

    private final Project project;

    @Nullable
    private String originalUrl;

    @Nullable
    private String readOnlyUrl;

    @Nullable
    private String readWriteUrl;

    @Nullable
    private String browseUrl;

    /**
     * Constructs an SCM object for the specified Gradle project.
     *
     * @param project Gradle project
     */
    public PomScm(final Project project) {
        this.project = project;

        parseConfig();
    }

    /**
     * Indicates whether a Git remote URL was found in the project.
     *
     * @return {@code true} if a Git remote URL was found.
     */
    public boolean isPresent() {
        return this.originalUrl != null;
    }

    /**
     * Provides the URL for the POM SCM {@code connection} tag.
     *
     * @return URL for read-only access to the Git repository.
     */
    public Provider<String> getReadOnly() {
        return this.project.provider(() -> this.readOnlyUrl);
    }

    /**
     * Provides the URL for the POM SCM {@code developerConnection} tag.
     *
     * @return URL for read-write access to the Git repository.
     */
    public Provider<String> getReadWrite() {
        return this.project.provider(() -> this.readWriteUrl);
    }

    /**
     * Provides the URL for the POM SCM {@code url} tag.
     *
     * @return URL for browsing the Git repository.
     */
    public Provider<String> getBrowse() {
        return this.project.provider(() -> this.browseUrl);
    }

    /**
     * Provides the URL as found in the Git config file.
     *
     * @return Remote URL from the Git config file
     */
    public Provider<String> getOriginalUrl() {
        return this.project.provider(() -> this.originalUrl);
    }

    /**
     * Parses the Git config file to find the remote URL.
     */
    private void parseConfig() {
        final File configFile = new File(this.project.getRootDir(), ".git/config");
        if (configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile, StandardCharsets.UTF_8))) {
                this.originalUrl = parseRemote(reader);
                if (this.originalUrl == null) {
                    return;
                }

                final URI remoteUri = normalizeRemoteUrl(this.originalUrl);
                final String scheme = remoteUri.getScheme();
                final String host = remoteUri.getHost();
                final int port = remoteUri.getPort();
                final String path = remoteUri.getPath();

                this.readWriteUrl = "scm:git:" + remoteUri;

                this.readOnlyUrl = "ssh".equals(scheme)
                                   ? "scm:git:git://" + host + (port == -1 ? "" : (":" + port)) + path
                                   : "scm:git:" + remoteUri;

                this.browseUrl = "file".equals(scheme)
                                 ? remoteUri.toString()
                                 : "https://" + host + GIT_EXTENSION_REGEX.matcher(path).replaceFirst("");
            } catch (final IOException ignore) {
                // Ignore
            }
        }
    }

    /**
     * Parses the remote section of the Git config file to extract the remote URL.
     *
     * @param reader Reader opened on the Git config file
     * @return Remote URL or {@code null} if not found
     * @throws IOException if there was a problem reading the config file
     */
    @Nullable
    private static String parseRemote(final BufferedReader reader) throws IOException {
        boolean inRemoteSection = false;

        String line;
        while ((line = reader.readLine()) != null) {
            if (REMOTE_SECTION_REGEX.matcher(line).matches()) {
                inRemoteSection = true;
                continue;
            }
            if (inRemoteSection) {
                if (SECTION_START_REGEX.matcher(line).lookingAt()) {
                    return null;
                }

                final Matcher matcher = REMOTE_URL_REGEX.matcher(line);
                if (matcher.matches()) {
                    return matcher.group(1);
                }
            }
        }
        return null;
    }

    /**
     * Normalizes the remote URL into a valid URI.
     *
     * @param url Git remote URL to normalize into a valid URI
     * @return Git remote URI
     */
    private static URI normalizeRemoteUrl(final String url) {
        final String normalizedUrl;
        if (url.startsWith("/")) {
            normalizedUrl = "file://" + url;
        } else if (url.startsWith("git@")) {
            normalizedUrl = "ssh://" + url.replace(':', '/');
        } else if (url.startsWith("git+ssh:")) {
            normalizedUrl = "ssh:" + url.substring("git+ssh:".length());
        } else {
            normalizedUrl = url;
        }
        return URI.create(normalizedUrl);
    }

    @Override
    public String toString() {
        return this.originalUrl == null ? "<empty>" : this.originalUrl;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        return Objects.equals(this.originalUrl, ((PomScm)obj).originalUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.originalUrl);
    }
}
