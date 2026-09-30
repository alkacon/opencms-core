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

package org.opencms.main;

import java.lang.reflect.Field;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Tests the runtime Java EE version override. */
public class TestCmsSystemInfoJavaEe {

    /**
     * Tests known servlet versions and preservation of configured values for unknown versions.
     *
     * @throws Exception if the test setup fails
     */
    @Test
    public void testJavaEeVersion() throws Exception {

        final int[][] versions = {{3, 0}, {3, 1}, {4, 0}, {5, 0}, {6, 0}, {6, 1}, {0, 0}, {6, 2}, {7, 0}};
        final String[] expected = {
            "6 (javax)",
            "7 (javax)",
            "8 (javax)",
            "9 (jakarta)",
            "10 (jakarta)",
            "11 (jakarta)",
            "configured",
            "12 (jakarta)",
            "configured"};
        for (int i = 0; i < versions.length; i++) {
            final CmsSystemInfo info = new CmsSystemInfo();
            final CmsSystemInfo.BuildInfoItem item = info.new BuildInfoItem("configured", "Java EE", "javaee");
            final Field field = CmsSystemInfo.class.getDeclaredField("m_buildInfo");
            field.setAccessible(true);
            field.set(info, Collections.singletonMap("javaee", item));
            info.initJavaEeVersion(versions[i][0], versions[i][1]);
            Assertions.assertEquals(expected[i], info.getBuildInfo().get("javaee").getValue());
            Assertions.assertEquals("Java EE", item.getNiceName());
            Assertions.assertEquals("javaee", item.getKeyName());
        }
    }

    /**
     * Tests that a missing configured entry is left absent.
     *
     * @throws Exception if the test setup fails
     */
    @Test
    public void testMissingJavaEeVersion() throws Exception {

        final CmsSystemInfo info = new CmsSystemInfo();
        final Field field = CmsSystemInfo.class.getDeclaredField("m_buildInfo");
        field.setAccessible(true);
        field.set(info, Collections.emptyMap());
        info.initJavaEeVersion(6, 0);
        Assertions.assertFalse(info.getBuildInfo().containsKey("javaee"));
    }
}
