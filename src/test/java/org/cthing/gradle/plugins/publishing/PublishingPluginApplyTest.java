/*
 * Copyright 2025 C Thing Software
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cthing.gradle.plugins.publishing;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class PublishingPluginApplyTest {

    @Test
    public void testApply() {
        final Project project = ProjectBuilder.builder().withName("testProject").build();
        project.getPluginManager().apply("org.cthing.cthing-publishing");

        assertThat(project.getExtensions().findByType(CThingPublishingExtension.class)).isNotNull();
        assertThat(project.getExtensions().findByType(CThingRepoExtension.class)).isNotNull();
    }
}
