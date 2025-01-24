/*
 * Copyright 2025 C Thing Software
 * All rights reserved.
 */
package org.cthing.gradle.plugins.publishing;

/**
 * Identifies a software license.
 */
public enum PomLicense {

    /** An <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache Software License version 2</a>. */
    ASL2("Apache-2.0", "https://www.apache.org/licenses/LICENSE-2.0"),

    /** GNU General Public License, version 2. */
    GPL2("GPL-2.0-only", "https://www.gnu.org/licenses/old-licenses/gpl-2.0-standalone.html"),

    /** Represents software that will not be released and therefore does not require a license. */
    INTERNAL("LicenseRef-CTHING-internal", "https://www.cthing.com/licenses/internal.txt"),

    /** License for JetBrains IDEs. */
    JETBRAINS("LicenseRef-JETBRAINS-toolbox", "https://www.jetbrains.com/store/license_personal.html"),

    /** MIT License. */
    MIT("MIT", "https://opensource.org/license/mit");

    private final String name;
    private final String url;

    PomLicense(final String name, final String url) {
        this.name = name;
        this.url = url;
    }

    /**
     * Obtains the name for the license.
     *
     * @return Name for the license. The <a href="https://spdx.org/licenses/">SPDX identifier</a> is used if one
     *      exists for the license. Otherwise, a
     *      <a href="https://spdx.github.io/spdx-spec/v2-draft/SPDX-license-expressions/">user defined SPDX
     *      identifier</a> is used.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Obtains the URL to the text of the license.
     *
     * @return URL pointing to the text of the license. May be {@code null}.
     */
    public String getUrl() {
        return this.url;
    }
}
