/*
 * Copyright 2025 C Thing Software
 * All rights reserved.
 */
package org.cthing.gradle.plugins.publishing;

import java.util.Objects;


/**
 * Represents a project developer.
 */
public class PomDeveloper {

    private final String id;
    private final String name;
    private final String email;

    /**
     * Constructs a developer.
     *
     * @param id Organizational identifier for the developer
     * @param name Developer name
     * @param email Developer email address
     */
    public PomDeveloper(final String id, final String name, final String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    /**
     * Obtains the developer organizational identifier.
     *
     * @return Identifier for the developer.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Obtains the name of the developer.
     *
     * @return Developer name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Obtains the email address of the developer.
     *
     * @return Developer email address.
     */
    public String getEmail() {
        return this.email;
    }

    @Override
    public String toString() {
        return this.id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final PomDeveloper that = (PomDeveloper)obj;
        return Objects.equals(this.id, that.id)
                && Objects.equals(this.name, that.name)
                && Objects.equals(this.email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.name, this.email);
    }
}
