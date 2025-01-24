/*
 * Copyright 2025 C Thing Software
 * All rights reserved.
 */
package org.cthing.gradle.plugins.publishing;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;


class PomLicenseTest {

    @ParameterizedTest
    @MethodSource("licenseDataProvider")
    @DisplayName("License URL")
    void testLicense(final PomLicense license, final String licenseName, final String licenseUrl) {
        assertThat(license.getUrl()).isEqualTo(licenseUrl);
        assertThat(license.getName()).isEqualTo(licenseName);
    }

    private static Stream<Arguments> licenseDataProvider() {
        return Stream.of(
                Arguments.of(PomLicense.ASL2,
                             "Apache-2.0",
                             "https://www.apache.org/licenses/LICENSE-2.0"),
                Arguments.of(PomLicense.GPL2,
                             "GPL-2.0-only",
                             "https://www.gnu.org/licenses/old-licenses/gpl-2.0-standalone.html"),
                Arguments.of(PomLicense.JETBRAINS,
                             "LicenseRef-JETBRAINS-toolbox",
                             "https://www.jetbrains.com/store/license_personal.html"),
                Arguments.of(PomLicense.INTERNAL,
                             "LicenseRef-CTHING-internal",
                             "https://www.cthing.com/licenses/internal.txt"),
                Arguments.of(PomLicense.MIT,
                             "MIT",
                             "https://opensource.org/license/mit")
        );
    }
}
