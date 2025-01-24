/*
 * Copyright 2025 C Thing Software
 * All rights reserved.
 */
package org.cthing.gradle.plugins.publishing;

/**
 * Identifies a continuous integration system used by C Thing Software projects.
 */
public enum PomCISystem {
    /** Indicates that no CI system is being used. */
    None,

    /** Specifies the use of the GitHub Actions CI system. */
    GitHubActions,

    /** Specifies the use of the internal C Thing Software Jenkins CI system. */
    CThingJenkins
}
