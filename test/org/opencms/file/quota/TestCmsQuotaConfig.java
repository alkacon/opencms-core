/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.quota;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.opencms.util.CmsSizeFormatter;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * Tests the shared budget value format and decimal byte conversion.<p>
 */
public class TestCmsQuotaConfig {

    @Test
    public void testFractionalGigabytesKeepBytePrecision() {

        assertEquals(Long.valueOf(500000000), CmsQuotaConfig.parse("0.5"));
        assertEquals(Long.valueOf(500000000), CmsQuotaConfig.parse("0,5"));
        assertEquals(Long.valueOf(100000000), CmsQuotaConfig.parse("0.1"));
        assertEquals(Long.valueOf(100000000), CmsQuotaConfig.parse("0,1"));
        assertEquals("100 MB", CmsSizeFormatter.format(CmsQuotaConfig.parse("0,1"), Locale.GERMAN));
        assertEquals(Long.valueOf(2500000000L), CmsQuotaConfig.parse("2.5"));
    }

    @Test
    public void testInvalidSettingsAreUnavailable() {

        for (String invalid : new String[] {"-1", "0", "2.55", "broken", "2147483648"}) {
            assertEquals(CmsQuotaDefinition.UNAVAILABLE, CmsQuotaConfig.parse(invalid));
        }
    }

    @Test
    public void testInvalidSystemDefaultProperties() {

        for (String value : new String[] {"0", "-1", "2.55", "unset", "invalid"}) {
            assertEquals(
                CmsQuotaDefinition.UNAVAILABLE,
                CmsQuotaConfig.parseSystemDefault(
                    ("upload.quota.systemdefault=" + value).getBytes(StandardCharsets.UTF_8)));
        }
        String invalidEscape = "upload.quota.systemdefault=" + (char)92 + "uXYZW";
        assertEquals(
            CmsQuotaDefinition.UNAVAILABLE,
            CmsQuotaConfig.parseSystemDefault(invalidEscape.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testSettingsInBytes() {

        assertNull(CmsQuotaConfig.parse(null));
        assertEquals(Long.valueOf(0), CmsQuotaConfig.parse("unlimited"));
        assertEquals(Long.valueOf(2 * CmsQuotaDefinition.BYTES_PER_GB), CmsQuotaConfig.parse("2"));
        assertEquals(
            Long.valueOf(Integer.MAX_VALUE * CmsQuotaDefinition.BYTES_PER_GB),
            CmsQuotaConfig.parse("2147483647"));
    }

    @Test
    public void testSystemDefaultProperties() {

        assertEquals(
            Long.valueOf(2000000000L),
            CmsQuotaConfig.parseSystemDefault(
                "# Systemweite Vorgabe\nupload.quota.systemdefault = 2\n".getBytes(StandardCharsets.UTF_8)));
        assertEquals(
            Long.valueOf(500000000L),
            CmsQuotaConfig.parseSystemDefault("upload.quota.systemdefault: 0,5\n".getBytes(StandardCharsets.UTF_8)));
        assertEquals(
            Long.valueOf(0),
            CmsQuotaConfig.parseSystemDefault(
                "upload.quota.systemdefault=unlimited\n".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testUnsetSystemDefault() {

        for (String contents : new String[] {
            "",
            "# upload.quota.systemdefault=2\n",
            "upload.quota.systemdefault=\n",
            "upload.quota.systemdefault=   \n",
            "upload.quota.default=2\nupload.quota=5\n"}) {
            assertNull(CmsQuotaConfig.parseSystemDefault(contents.getBytes(StandardCharsets.UTF_8)));
        }
    }
}
