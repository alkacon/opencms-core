/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml2json;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import junit.framework.Test;

/**
 * Tests for the XML2JSON feature.
 */
public class TestXml2Json extends OpenCmsTestCase {

    /** The module path for the test data. */
    public static final String MOD_PATH = "/system/modules/org.opencms.test.xml2json";

    /**
     * Creates a new instance.
     *
     * @param name the name of the test
     */
    public TestXml2Json(String name) {

        super(name);
    }

    /**
     * Returns the test suite.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestXml2Json.class, "xml2json", "/", "WEB-INF/xml2json");

    }

    /**
     * Helper module for generating a full path from the sub-path below the module folder.
     *
     * @param subPath the path below the module folder
     * @return the full path
     */
    static String modPath(String subPath) {

        return CmsStringUtil.joinPaths(MOD_PATH, subPath);
    }

    /**
     * Checks that the test data is set up correctly.
     *
     * @throws Exception
     */
    public void testDataAvailable() throws Exception {

        CmsObject cms = getCmsObject();
        CmsFile file = cms.readFile(modPath("test1.xml"));
        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(file);
        assertEquals("xjparent", type.getTypeName());

        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);

    }

}
