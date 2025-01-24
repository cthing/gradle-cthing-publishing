/*
 * Copyright 2025 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.gradle.plugins.publishing;

import org.gradle.api.Plugin;
import org.gradle.api.Project;


/**
 * A plugin that provides publishing information for C Thing Software artifacts.
 */
public class PublishingPlugin implements Plugin<Project> {

    public static final String PUBLISHING_EXTENSION_NAME = "cthingPublishing";
    public static final String REPO_EXTENSION_NAME = "cthingRepo";

    @Override
    public void apply(final Project project) {
        project.getExtensions().create(PUBLISHING_EXTENSION_NAME, CThingPublishingExtension.class, project);
        project.getExtensions().create(REPO_EXTENSION_NAME, CThingRepoExtension.class, project);
    }
}
