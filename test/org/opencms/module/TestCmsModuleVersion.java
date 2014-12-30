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

package org.opencms.module;

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.test.OpenCmsTestCase;

/**
 * Tests the module version.<p>
 */
public class TestCmsModuleVersion extends OpenCmsTestCase {

    /**
     * Tests version generation.<p>
     */
    public void testVersionGeneration() {

        CmsModuleVersion v1 = new CmsModuleVersion("1.2.5");
        CmsModuleVersion v2 = new CmsModuleVersion("1.12");

        if (v1.compareTo(v2) > 0) {
            fail("Module version comparison error");
        }

        v1 = new CmsModuleVersion("5");
        v2 = new CmsModuleVersion("1.0.0.1");

        if (v1.compareTo(v2) <= 0) {
            fail("Module version comparison error");
        }

        v1 = new CmsModuleVersion("1.2.5.7");
        v2 = new CmsModuleVersion("1.2.45");

        if (v1.compareTo(v2) > 0) {
            fail("Module version comparison error");
        }

        v1 = new CmsModuleVersion("2.45.6");
        v2 = new CmsModuleVersion("2.45.06");

        if (v1.compareTo(v2) != 0) {
            fail("Module version comparison error");
        }

        v1 = new CmsModuleVersion("1.0.0.0");
        v2 = new CmsModuleVersion("1");

        if (v1.compareTo(v2) != 0) {
            fail("Module version comparison error");
        }

        v1 = new CmsModuleVersion("0.1");
        v2 = new CmsModuleVersion("0.0.0.1");

        if (v1.compareTo(v2) <= 0) {
            fail("Module version comparison error");
        }

        v1 = new CmsModuleVersion("0.08");
        assertEquals("0.8", v1.getVersion());

        v1 = new CmsModuleVersion("00.00");
        assertEquals("0.0", v1.getVersion());

        v1 = new CmsModuleVersion("999.999.999.999");
        assertEquals("999.999.999.999", v1.getVersion());

        boolean gotError = false;
        try {
            new CmsModuleVersion("2..45.6");
        } catch (CmsIllegalArgumentException e) {
            gotError = true;
        }
        if (!gotError) {
            fail("Invalid version generation allowed");
        }

        gotError = false;
        try {
            new CmsModuleVersion(".2.45.6");
        } catch (CmsIllegalArgumentException e) {
            gotError = true;
        }
        if (!gotError) {
            fail("Invalid version generation allowed");
        }

        gotError = false;
        try {
            new CmsModuleVersion("2.45.6.");
        } catch (CmsIllegalArgumentException e) {
            gotError = true;
        }
        if (!gotError) {
            fail("Invalid version generation allowed");
        }

        gotError = false;
        try {
            new CmsModuleVersion("wurst");
        } catch (CmsIllegalArgumentException e) {
            gotError = true;
        }
        if (!gotError) {
            fail("Invalid version generation allowed");
        }

        gotError = false;
        try {
            new CmsModuleVersion("2222.45.6");
        } catch (CmsIllegalArgumentException e) {
            gotError = true;
        }
        if (!gotError) {
            fail("Invalid version generation allowed");
        }

        gotError = false;
        try {
            new CmsModuleVersion("1.2.3.4.5");
        } catch (CmsIllegalArgumentException e) {
            gotError = true;
        }
        if (!gotError) {
            fail("Invalid version generation allowed");
        }
    }

    /**
     * Tests version increment.<p>
     */
    public void testVersionIncrement() {

        CmsModuleVersion v1 = new CmsModuleVersion("1.2.5");
        v1.increment();
        assertEquals("1.2.6", v1.getVersion());

        v1 = new CmsModuleVersion("1.02.05");
        v1.increment();
        assertEquals("1.2.6", v1.getVersion());

        v1 = new CmsModuleVersion("1.02.999");
        v1.increment();
        assertEquals("1.3.0", v1.getVersion());

        v1 = new CmsModuleVersion("0.999");
        v1.increment();
        assertEquals("1.0", v1.getVersion());

        boolean gotError = false;
        try {
            v1 = new CmsModuleVersion("999.999.999.999");
            v1.increment();
        } catch (RuntimeException e) {
            gotError = true;
        }
        if (!gotError) {
            fail("Invalid version increment allowed");
        }
    }

}
