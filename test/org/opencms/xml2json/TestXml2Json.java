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

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.json.JSONObject;
import org.opencms.jsp.util.CmsJspJsonWrapper;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.xml2json.CmsJsonAccessPolicy;
import org.opencms.xml.xml2json.CmsJsonResult;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerContext;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerXmlContent;
import org.opencms.xml.xml2json.renderer.CmsJsonRendererXmlContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
     * Test for access policy.
     *
     * @throws Exception
     */
    public void testAccessExclude() throws Exception {

        CmsJsonAccessPolicy access = CmsJsonAccessPolicy.parse(getClass().getResourceAsStream("access-ex.xml"));
        CmsObject cms = getCmsObject();
        assertFalse(access.checkAccess(cms, "/system/foo"));
        assertFalse(access.checkAccess(cms, "/system/modules/foo"));
        assertFalse(access.checkAccess(cms, "/shared/foo"));
        assertTrue(access.checkAccess(cms, "/sites/default/foo"));
    }

    /**
     * Test for access policy.
     *
     * @throws Exception
     */
    public void testAccessGroup() throws Exception {

        CmsObject cms = getCmsObject();
        cms.createUser("Test1", "password", "", new HashMap<>());
        cms.createGroup("JsonAccessGroup", "", 0, null);
        CmsJsonAccessPolicy access = CmsJsonAccessPolicy.parse(getClass().getResourceAsStream("access1.xml"));
        CmsObject otherCms = OpenCms.initCmsObject(cms);
        otherCms.loginUser("Test1", "password");
        assertFalse(access.checkAccess(otherCms, "/foo"));
        cms.addUserToGroup("Test1", "JsonAccessGroup");
        assertTrue(access.checkAccess(otherCms, "/foo"));
        assertFalse(access.checkAccess(otherCms, "/forbidden/foo"));

    }

    /**
     * Test for access policy.
     *
     * @throws Exception
     */
    public void testAccessIncludeExclude() throws Exception {

        CmsJsonAccessPolicy access = CmsJsonAccessPolicy.parse(getClass().getResourceAsStream("access-in-ex.xml"));
        CmsObject cms = getCmsObject();
        assertTrue(access.checkAccess(cms, "/system/foo"));
        assertTrue(access.checkAccess(cms, "/shared/foo"));
        assertFalse(access.checkAccess(cms, "/system/modules/foo"));
        assertFalse(access.checkAccess(cms, "/sites/default/foo"));
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testChoice() throws Exception {

        runDataTest("choice-test.xml");
    }

    /**
     * Tests use of custom XML content renderer.
     *
     * @throws Exception
     */
    public void testCustomRenderer() throws Exception {

        CmsObject cms = getCmsObject();
        String folder = "/system/jsontest";
        if (cms.existsResource(folder)) {
            cms.deleteResource(folder, CmsResource.DELETE_PRESERVE_SIBLINGS);
        }
        cms.createResource(folder, 0);
        I_CmsResourceType contentType = OpenCms.getResourceManager().getResourceType("xjparent");
        CmsParameterConfiguration data = readXmlTestData(getClass(), "custom-test.xml");
        String testFile = folder + "/test.xml";
        CmsResource testFileRes = cms.createResource(
            testFile,
            contentType,
            data.get("input").trim().getBytes("UTF-8"),
            new ArrayList<>());
        String expected = new String(data.get("output").trim());

        CmsJsonHandlerXmlContent handler = new CmsJsonHandlerXmlContent();
        CmsObject rootCms = OpenCms.initCmsObject(cms);
        Map<String, String> params = new HashMap<>();
        params.put("locale", "en");
        CmsJsonHandlerContext context = new CmsJsonHandlerContext(
            cms,
            rootCms,
            testFile,
            testFileRes,
            params,
            new CmsParameterConfiguration(),
            new CmsJsonAccessPolicy(true));
        CmsJsonResult result = handler.renderJson(context);
        Object jsonObj = result.getJson();

        String actual = JSONObject.valueToString(jsonObj, 0, 4);
        expected = JSONObject.valueToString(new JSONObject(expected), 0, 4);
        assertEquals(expected, actual);
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testEmpty() throws Exception {

        runDataTest("empty-test.xml");
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testRemoveInArray() throws Exception {

        runRemoveTest("remove-array.xml");
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testRemoveInvalidType() throws Exception {

        runRemoveTest("remove-invalid-type.xml");
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testRemoveSimple() throws Exception {

        runRemoveTest("remove-simple.xml");
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testRemoveSuperfluousCharacters() throws Exception {

        runRemoveTest("remove-superfluous-characters.xml");
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testRemoveWildcard() throws Exception {

        runRemoveTest("remove-wildcard.xml");
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testSimple() throws Exception {

        runDataTest("simple-test.xml");
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testTypes() throws Exception {

        runDataTest("types-test.xml");
    }

    /**
     * Read XML input and expected output from data file and check if rendered JSON matches expected output.
     *
     * @param name the test data file name
     * @throws Exception
     */
    protected void runDataTest(String name) throws Exception {

        CmsObject cms = getCmsObject();
        String folder = "/system/jsontest";
        if (cms.existsResource(folder)) {
            cms.deleteResource(folder, CmsResource.DELETE_PRESERVE_SIBLINGS);
        }
        cms.createResource(folder, 0);
        I_CmsResourceType contentType = OpenCms.getResourceManager().getResourceType("xjparent");
        CmsParameterConfiguration data = readXmlTestData(getClass(), name);
        String testFile = folder + "/test.xml";
        cms.createResource(testFile, contentType, data.get("input").trim().getBytes("UTF-8"), new ArrayList<>());
        String expected = new String(data.get("output").trim());
        CmsJsonRendererXmlContent renderer = new CmsJsonRendererXmlContent(cms);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, cms.readFile(testFile));
        Object jsonObj = renderer.render(content, Locale.ENGLISH);

        String actual = JSONObject.valueToString(jsonObj, 0, 4);
        expected = JSONObject.valueToString(new JSONObject(expected), 0, 4);
        assertEquals(expected, actual);
    }

    /**
     * Runs test for JSON path removal with the data file with the given file name.
     *
     * <p>The file contains entries for the JSON input, the path to be removed, and the expected output.
     *
     * @param name the test data file name
     * @throws Exception if something goes wrong
     */
    protected void runRemoveTest(String name) throws Exception {

        CmsParameterConfiguration data = readXmlTestData(getClass(), name);
        String inputJson = data.get("input");
        String path = data.get("path");
        String outputJson = data.get("output");
        JSONObject jsonObj = new JSONObject(inputJson);
        CmsJspJsonWrapper wrapper = new CmsJspJsonWrapper(jsonObj);
        wrapper.removePath(path);
        String fmtExpectedJson = JSONObject.valueToString(new JSONObject(outputJson), 0, 4);
        String fmtActualJson = JSONObject.valueToString(wrapper.getObject(), 0, 4);
        assertEquals("Failed check in path removal test <" + name + ">", fmtExpectedJson, fmtActualJson);
    }

}
