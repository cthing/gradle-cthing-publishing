/*
 * Copyright 2025 C Thing Software
 * All rights reserved.
 */
package org.cthing.gradle.plugins.publishing;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import static org.assertj.core.api.Assertions.assertThat;


public class PomDeveloperTest {

    @Test
    public void testProperties() {
        final PomDeveloper developer = new PomDeveloper("joe", "Joe Blow", "joe@blow.com");
        assertThat(developer.getId()).isEqualTo("joe");
        assertThat(developer.getName()).isEqualTo("Joe Blow");
        assertThat(developer.getEmail()).isEqualTo("joe@blow.com");
        assertThat(developer).hasToString("joe");
    }

    @Test
    public void testEquality() {
        EqualsVerifier.forClass(PomDeveloper.class).usingGetClass().verify();
    }
}
