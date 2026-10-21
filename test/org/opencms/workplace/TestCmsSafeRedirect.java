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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.site.CmsSiteMatcher;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Tests redirect validation without an initialized OpenCms runtime. */
public class TestCmsSafeRedirect {

    private final List<CmsSiteMatcher> m_servers = Arrays.asList(
        new CmsSiteMatcher("https://trusted.example"),
        new CmsSiteMatcher("http://alias.example:8080"),
        new CmsSiteMatcher("https://workplace.example:8443"));

    @Test
    public void acceptsBothWorkplaceSchemes() {

        for (String configured : Arrays.asList("http://workplace.example", "https://workplace.example")) {
            CmsSiteMatcher matcher = new CmsSiteMatcher(configured);
            List<CmsSiteMatcher> servers = Arrays.asList(
                matcher.forDifferentScheme("http"),
                matcher.forDifferentScheme("https"));
            assertTrue(CmsDialog.isSafeRedirect("http://workplace.example/", servers));
            assertTrue(CmsDialog.isSafeRedirect("https://workplace.example/", servers));
            assertFalse(CmsDialog.isSafeRedirect("https://workplace.example:8443/", servers));
            assertFalse(CmsDialog.isSafeRedirect("https://workplace.example.invalid/", servers));
        }
        CmsSiteMatcher matcher = new CmsSiteMatcher("https://workplace.example:8443");
        List<CmsSiteMatcher> servers = Arrays.asList(
            matcher.forDifferentScheme("http"),
            matcher.forDifferentScheme("https"));
        assertTrue(CmsDialog.isSafeRedirect("http://workplace.example:8443/", servers));
        assertTrue(CmsDialog.isSafeRedirect("https://workplace.example:8443/", servers));
        assertFalse(CmsDialog.isSafeRedirect("https://workplace.example/", servers));
    }

    @Test
    public void acceptsLocalPathsAndExactOrigins() {

        for (String link : Arrays.asList(
            "/",
            "/system/workplace/views/admin/admin-main.jsp?path=%2Fdatabase",
            "/opencms/workplace#!explorer/",
            "/frontend/page.html?value=%22quoted%22#section",
            "https://trusted.example/page?x=1#fragment",
            "HTTPS://TRUSTED.EXAMPLE:443/page",
            "http://alias.example:8080/",
            "https://workplace.example:8443/workplace")) {
            assertTrue(CmsDialog.isSafeRedirect(link, m_servers), link);
        }
    }

    @Test
    public void doesNotTreatDefaultMatcherAsTrustedOrigin() {

        assertFalse(
            CmsDialog.isSafeRedirect("https://unknown.example/", Arrays.asList(CmsSiteMatcher.DEFAULT_MATCHER)));
    }

    @Test
    public void rejectsUntrustedAndAmbiguousTargets() {

        for (String link : Arrays.asList(
            null,
            "",
            "relative/path",
            "?query=1",
            "#fragment",
            "//evil.example/path",
            "///evil.example/path",
            "/\\evil.example/path",
            "https:\\trusted.example/path",
            " https://trusted.example/",
            "/\t/evil.example",
            "/path\r\nLocation: https://evil.example",
            "https://trusted.example.evil.example/",
            "https://trusted.example@evil.example/",
            "https://evil.example@trusted.example/",
            "https://trusted.example:444/",
            "http://trusted.example/",
            "https://alias.example:8080/",
            "https://unknown.example/",
            "https:///trusted.example/",
            "https://trusted.example/%zz",
            "javascript:alert(1)",
            "data:text/html,test",
            "/workplace/x\";alert(1);//",
            "/workplace/</script><script>alert(1)</script>")) {
            assertFalse(CmsDialog.isSafeRedirect(link, m_servers), String.valueOf(link));
        }
    }
}
