/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.util;

import org.opencms.staticexport.CmsExportFolderMatcher;
import org.opencms.test.OpenCmsTestCase;

import java.util.ArrayList;

/**
 * @since 6.0.0
 */
public class TestCmsExportFolderMatcher extends OpenCmsTestCase {

    private static String checkRes = "/system/opencms.ini";

    /**
     * Tests for the resource name translation.<p>
     */
    public void testTranslateResource() {

        /** default folders. */
        ArrayList folders = new ArrayList();

        folders.add("\\/sites\\/.*");
        folders.add("\\/system\\/galleries\\/.*");
        folders.add("\\/system\\/modules\\/.*\\/resources\\/.*");

        CmsExportFolderMatcher matcher = new CmsExportFolderMatcher(folders, checkRes);

        boolean test;
        test = matcher.match("/system/opencms.ini");
        assertEquals(test, true);

        test = matcher.match("/sites/default/index.html");
        assertEquals(test, true);

        test = matcher.match("/sites/default/folder/index.html");
        assertEquals(test, true);

        test = matcher.match("/gibtsnicht/index.html");
        assertEquals(test, false);

        test = matcher.match("/system/galleries/pics/demo.gif");
        assertEquals(test, true);

        test = matcher.match("/system/modules/org.opencms.welcome/resources/test.gif");
        assertEquals(test, true);

        test = matcher.match("/system/modules/org.opencms.welcome/templates/test.jsp");
        assertEquals(test, false);

    }

}
