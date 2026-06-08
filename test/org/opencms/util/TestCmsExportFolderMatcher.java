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

package org.opencms.util;

import org.opencms.staticexport.CmsExportFolderMatcher;
import org.opencms.test.OpenCmsTestRunner;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

/**
 * @since 6.0.0
 */
public class TestCmsExportFolderMatcher extends OpenCmsTestRunner {

    private static String checkRes = "/system/opencms.ini";

    /**
     * Tests for the resource name translation.<p>
     */
    @Test
    public void testTranslateResource() {

        /** default folders. */
        ArrayList<String> folders = new ArrayList<String>();

        folders.add("\\/sites\\/.*");
        folders.add("\\/system\\/galleries\\/.*");
        folders.add("\\/system\\/modules\\/.*\\/resources\\/.*");

        CmsExportFolderMatcher matcher = new CmsExportFolderMatcher(folders, checkRes);

        boolean test;
        test = matcher.match("/system/opencms.ini");
        assertEquals(true, test);

        test = matcher.match("/sites/default/index.html");
        assertEquals(true, test);

        test = matcher.match("/sites/default/folder/index.html");
        assertEquals(true, test);

        test = matcher.match("/gibtsnicht/index.html");
        assertEquals(false, test);

        test = matcher.match("/system/galleries/pics/demo.gif");
        assertEquals(true, test);

        test = matcher.match("/system/modules/org.opencms.welcome/resources/test.gif");
        assertEquals(true, test);

        test = matcher.match("/system/modules/org.opencms.welcome/templates/test.jsp");
        assertEquals(false, test);

    }

}
