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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file;

import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceFilter;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the "writeProperty" method of the CmsObject.<p>
 *
 */
public class TestProperty extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestProperty(String arg0) {

        super(arg0);
    }

    /**
     * Test the writeProperty method to create a list of properties.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to create the properies
     * @param propertyList1 the properties to create
     * @throws Throwable if something goes wrong
     */
    public static void createProperties(OpenCmsTestCase tc, CmsObject cms, String resource1, List propertyList1)
    throws Throwable {

        tc.storeResources(cms, resource1);

        long timestamp = System.currentTimeMillis();

        cms.lockResource(resource1);
        cms.writePropertyObjects(resource1, propertyList1);
        cms.unlockResource(resource1);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
        // project must be current project
        tc.assertProject(cms, resource1, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        tc.assertState(cms, resource1, tc.getPreCalculatedState(resource1));
        // date last modified must be after the test timestamp
        tc.assertDateLastModifiedAfter(cms, resource1, timestamp);
        // the user last modified must be the current user
        tc.assertUserLastModified(cms, resource1, cms.getRequestContext().getCurrentUser());
        // the properties must be new
        tc.assertPropertyNew(cms, resource1, propertyList1);
    }

    /**
     * Test the writeProperty method to create one property.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to add a propery
     * @param property1 the property to create
     * @throws Throwable if something goes wrong
     */
    public static void createProperty(OpenCmsTestCase tc, CmsObject cms, String resource1, CmsProperty property1)
    throws Throwable {

        tc.storeResources(cms, resource1);

        long timestamp = System.currentTimeMillis();

        cms.lockResource(resource1);
        cms.writePropertyObject(resource1, property1);
        cms.unlockResource(resource1);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
        // project must be current project
        tc.assertProject(cms, resource1, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        tc.assertState(cms, resource1, tc.getPreCalculatedState(resource1));
        // date last modified must be after the test timestamp
        tc.assertDateLastModifiedAfter(cms, resource1, timestamp);
        // the user last modified must be the current user
        tc.assertUserLastModified(cms, resource1, cms.getRequestContext().getCurrentUser());
        // the property must be new
        tc.assertPropertyNew(cms, resource1, property1);
    }

    /**
     * Test the writeProperty method to remove a list of properties.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to remove the properies
     * @param propertyList1 the properties to remove
     * @throws Throwable if something goes wrong
     */
    public static void removeProperties(OpenCmsTestCase tc, CmsObject cms, String resource1, List propertyList1)
    throws Throwable {

        tc.storeResources(cms, resource1);

        long timestamp = System.currentTimeMillis();

        cms.lockResource(resource1);
        cms.writePropertyObjects(resource1, propertyList1);
        cms.unlockResource(resource1);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
        // project must be current project
        tc.assertProject(cms, resource1, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        tc.assertState(cms, resource1, tc.getPreCalculatedState(resource1));
        // date last modified must be after the test timestamp
        tc.assertDateLastModifiedAfter(cms, resource1, timestamp);
        // the user last modified must be the current user
        tc.assertUserLastModified(cms, resource1, cms.getRequestContext().getCurrentUser());
        // the properties must have been removed
        tc.assertPropertyRemoved(cms, resource1, propertyList1);
    }

    /**
     * Test the writeProperty method to remove one property.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to remove a propery
     * @param property1 the property to remove
     * @throws Throwable if something goes wrong
     */
    public static void removeProperty(OpenCmsTestCase tc, CmsObject cms, String resource1, CmsProperty property1)
    throws Throwable {

        tc.storeResources(cms, resource1);

        long timestamp = System.currentTimeMillis();

        cms.lockResource(resource1);
        cms.writePropertyObject(resource1, property1);
        cms.unlockResource(resource1);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
        // project must be current project
        tc.assertProject(cms, resource1, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        tc.assertState(cms, resource1, tc.getPreCalculatedState(resource1));
        // date last modified must be after the test timestamp
        tc.assertDateLastModifiedAfter(cms, resource1, timestamp);
        // the user last modified must be the current user
        tc.assertUserLastModified(cms, resource1, cms.getRequestContext().getCurrentUser());
        // the property must be removed
        tc.assertPropertyRemoved(cms, resource1, property1);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestProperty.class.getName());

        suite.addTest(new TestProperty("testFrozenProperty"));
        suite.addTest(new TestProperty("testNullProperty"));
        suite.addTest(new TestProperty("testSharedPropertyIssue1"));
        suite.addTest(new TestProperty("testPropertyLists"));
        suite.addTest(new TestProperty("testPropertyMaps"));
        suite.addTest(new TestProperty("testWriteProperty"));
        suite.addTest(new TestProperty("testWriteProperties"));
        suite.addTest(new TestProperty("testRemoveProperty"));
        suite.addTest(new TestProperty("testRemoveProperties"));
        suite.addTest(new TestProperty("testCreateProperty"));
        suite.addTest(new TestProperty("testCreateProperties"));
        suite.addTest(new TestProperty("testWritePropertyOnFolder"));
        suite.addTest(new TestProperty("testDefaultPropertyCreation"));
        suite.addTest(new TestProperty("testCaseSensitiveProperties"));
        suite.addTest(new TestProperty("testReadResourcesWithProperty"));
        suite.addTest(new TestProperty("testReadLocalizedProperty"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Test the writeProperty method with a list of properties.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to write the properies
     * @param propertyList1 the properties to write
     * @throws Throwable if something goes wrong
     */
    public static void writeProperties(OpenCmsTestCase tc, CmsObject cms, String resource1, List propertyList1)
    throws Throwable {

        tc.storeResources(cms, resource1);

        long timestamp = System.currentTimeMillis();

        cms.lockResource(resource1);
        cms.writePropertyObjects(resource1, propertyList1);
        cms.unlockResource(resource1);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
        // project must be current project
        tc.assertProject(cms, resource1, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        tc.assertState(cms, resource1, CmsResource.STATE_CHANGED);
        // date last modified must be after the test timestamp
        tc.assertDateLastModifiedAfter(cms, resource1, timestamp);
        // the user last modified must be the current user
        tc.assertUserLastModified(cms, resource1, cms.getRequestContext().getCurrentUser());
        // the property must have the new value
        tc.assertPropertyChanged(cms, resource1, propertyList1);
    }

    /**
     * Test the writeProperty method with one property.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to write a propery
     * @param property1 the property to write
     * @throws Throwable if something goes wrong
     */
    public static void writeProperty(OpenCmsTestCase tc, CmsObject cms, String resource1, CmsProperty property1)
    throws Throwable {

        tc.storeResources(cms, resource1);

        long timestamp = System.currentTimeMillis();

        cms.lockResource(resource1);
        cms.writePropertyObject(resource1, property1);
        cms.unlockResource(resource1);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
        // project must be current project
        tc.assertProject(cms, resource1, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        tc.assertState(cms, resource1, tc.getPreCalculatedState(resource1));
        // date last modified must be after the test timestamp
        tc.assertDateLastModifiedAfter(cms, resource1, timestamp);
        // the user last modified must be the current user
        tc.assertUserLastModified(cms, resource1, cms.getRequestContext().getCurrentUser());
        // the property must have the new value
        tc.assertPropertyChanged(cms, resource1, property1);
    }

    /**
     * Tests the proper behaviour for case sensitiveness in property definition names.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCaseSensitiveProperties() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing proper behaviour for case sensitiveness in property definition names");
        CmsProperty myProperty = new CmsProperty("myProperty", "myValue", "myValue");
        CmsProperty myproperty = new CmsProperty("myproperty", "myvalue", "myvalue");
        cms.lockResource("/index.html");
        cms.writePropertyObject("/index.html", myProperty);
        cms.writePropertyObject("/index.html", myproperty);
        cms.unlockResource("/index.html");
        assertEquals("myValue", cms.readPropertyObject("/index.html", "myProperty", false).getResourceValue());
        assertEquals("myvalue", cms.readPropertyObject("/index.html", "myproperty", false).getResourceValue());
    }

    /**
     * Tests the writePropertyObjects method for removing of properties.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCreateProperties() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing creating multiple properties on a resource");
        CmsProperty property8 = new CmsProperty("Newproperty", "testvalue1", "testvalue2");
        CmsProperty property9 = new CmsProperty("AnotherNewproperty", "anothervalue", null);
        List propertyList3 = new ArrayList();
        propertyList3.add(property8);
        propertyList3.add(property9);
        createProperties(this, cms, "/index.html", propertyList3);
    }

    /**
     * Tests the writePropertyObject method for removing of properties.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCreateProperty() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing creating one property on a resource");
        CmsProperty property7 = new CmsProperty("Newproperty", "testvalue1", "testvalue2");
        createProperty(this, cms, "/folder1/index.html", property7);
    }

    /**
     * Test default property creation (from resource type configuration).<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testDefaultPropertyCreation() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing default property creation");

        String resourcename = "/folder1/article_test.html";
        byte[] content = new byte[0];

        // resource 27 is article (xml content) with default properties
        cms.createResource(resourcename, OpenCmsTestCase.ARTICLE_TYPEID, content, null);

        // ensure created resource type
        assertResourceType(cms, resourcename, OpenCmsTestCase.ARTICLE_TYPEID);
        // project must be current project
        assertProject(cms, resourcename, cms.getRequestContext().getCurrentProject());
        // state must be "new"
        assertState(cms, resourcename, CmsResource.STATE_NEW);
        // the user last modified must be the current user
        assertUserLastModified(cms, resourcename, cms.getRequestContext().getCurrentUser());

        CmsProperty property1, property2;
        property1 = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "Test title", null);
        property2 = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_TITLE, false);
        assertTrue(property1.isIdentical(property2));

        property1 = new CmsProperty(
            "template-elements",
            "/system/modules/org.opencms.frontend.templateone.form/pages/form.html",
            null);
        property2 = cms.readPropertyObject(resourcename, "template-elements", false);
        assertTrue(property1.isIdentical(property2));

        property1 = new CmsProperty(
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            null,
            "Admin_/folder1/article_test.html_/sites/default/folder1/article_test.html");
        property2 = cms.readPropertyObject(resourcename, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false);
        assertTrue(property1.isIdentical(property2));

        // publish the project
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);
    }

    /**
     * Tests changing the values of a frozen property.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testFrozenProperty() throws Throwable {

        CmsProperty property = CmsProperty.getNullProperty();
        if (!property.isFrozen()) {
            fail("NULL_PROPERTY is not frozen!");
        }
        boolean gotException = false;
        try {
            property.setAutoCreatePropertyDefinition(true);
        } catch (CmsRuntimeException e) {
            assertEquals(org.opencms.file.Messages.ERR_PROPERTY_FROZEN_1, e.getMessageContainer().getKey());
            gotException = true;
        }
        assertTrue("Operation did not throw the required Exception", gotException);
        gotException = false;
        try {
            property.setFrozen(false);
        } catch (CmsRuntimeException e) {
            assertEquals(org.opencms.file.Messages.ERR_PROPERTY_FROZEN_1, e.getMessageContainer().getKey());
            gotException = true;
        }
        assertTrue("Operation did not throw the required Exception", gotException);
        gotException = false;
        try {
            property.setName("SomeString");
        } catch (CmsRuntimeException e) {
            assertEquals(org.opencms.file.Messages.ERR_PROPERTY_FROZEN_1, e.getMessageContainer().getKey());
            gotException = true;
        }
        assertTrue("Operation did not throw the required Exception", gotException);
        gotException = false;
        try {
            property.setValue("SomeString", CmsProperty.TYPE_INDIVIDUAL);
        } catch (CmsRuntimeException e) {
            assertEquals(org.opencms.file.Messages.ERR_PROPERTY_FROZEN_1, e.getMessageContainer().getKey());
            gotException = true;
        }
        assertTrue("Operation did not throw the required Exception", gotException);
        gotException = false;
        try {
            property.setResourceValue("SomeString");
        } catch (CmsRuntimeException e) {
            assertEquals(org.opencms.file.Messages.ERR_PROPERTY_FROZEN_1, e.getMessageContainer().getKey());
            gotException = true;
        }
        assertTrue("Operation did not throw the required Exception", gotException);
        gotException = false;
        try {
            property.setStructureValue("SomeString");
        } catch (CmsRuntimeException e) {
            assertEquals(org.opencms.file.Messages.ERR_PROPERTY_FROZEN_1, e.getMessageContainer().getKey());
            gotException = true;
        }
        assertTrue("Operation did not throw the required Exception", gotException);
        gotException = false;
        try {
            property.setResourceValueList(Collections.singletonList("SomeString"));
        } catch (CmsRuntimeException e) {
            assertEquals(org.opencms.file.Messages.ERR_PROPERTY_FROZEN_1, e.getMessageContainer().getKey());
            gotException = true;
        }
        assertTrue("Operation did not throw the required Exception", gotException);
        gotException = false;
        try {
            property.setStructureValueList(Collections.singletonList("SomeString"));
        } catch (CmsRuntimeException e) {
            assertEquals(org.opencms.file.Messages.ERR_PROPERTY_FROZEN_1, e.getMessageContainer().getKey());
            gotException = true;
        }
        assertTrue("Operation did not throw the required Exception", gotException);
    }

    /**
     * Tests the NULL_PROPERTY.<p>
     *
     * @throws Exception if the test fails
     */
    public void testNullProperty() throws Exception {

        // get the null property
        CmsProperty nullProperty = CmsProperty.getNullProperty();
        // create another property
        CmsProperty p = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "Some title", null);
        // do a comparison
        assertFalse("Created property must not be equal to NULL_PROPERTY", p.equals(nullProperty));
        assertFalse("NULL_PROPERTY must not be equal to created Property", nullProperty.equals(p));
        assertTrue("NULL_PROPERTY must be equal to itself", nullProperty.equals(nullProperty));
        assertTrue("NULL_PROPERTY must be identical to itself", nullProperty == CmsProperty.getNullProperty());
    }

    /**
     * Tests reading and writing property lists.<p>
     *
     * @throws Exception if the test fails
     */
    public void testPropertyLists() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing reading and writing property lists");

        String source = "/xmlcontent/article_0001.html";
        cms.lockResource(source);

        CmsProperty prop;
        prop = cms.readPropertyObject(source, CmsPropertyDefinition.PROPERTY_TITLE, false);

        // basic asserts so we know for sure where we start
        assertEquals("Sample article 1  (>>SearchEgg1<<)", prop.getValue());
        assertEquals("Sample article 1  (>>SearchEgg1<<)", prop.getStructureValue());
        assertNull(prop.getResourceValue());

        // simple list asserts
        assertEquals(1, prop.getValueList().size());
        assertEquals(1, prop.getStructureValueList().size());
        assertNull(prop.getResourceValueList());

        // now set the title as a list
        List list = new ArrayList();
        String value = "";
        for (int i = 1; i <= 10; i++) {
            String s = "Title " + i;
            list.add(s);
            value += s;
            if (i < 10) {
                value += CmsProperty.VALUE_LIST_DELIMITER;
            }
        }
        prop.setStructureValueList(list);

        // asserts on non-written property
        assertEquals(value, prop.getValue());
        assertEquals(value, prop.getStructureValue());
        assertNull(prop.getResourceValue());
        assertEquals(10, prop.getValueList().size());
        assertEquals(10, prop.getStructureValueList().size());
        assertNull(prop.getResourceValueList());
        list = prop.getValueList();
        for (int i = 1; i <= 10; i++) {
            String s = "Title " + i;
            assertEquals(s, list.get(i - 1).toString());
        }

        // write the property object
        cms.writePropertyObject(source, prop);

        // read and check the property
        CmsProperty prop2 = cms.readPropertyObject(source, CmsPropertyDefinition.PROPERTY_TITLE, false);

        // asserts on written property
        assertEquals(value, prop2.getValue());
        assertEquals(value, prop2.getStructureValue());
        assertNull(prop2.getResourceValue());
        assertEquals(10, prop2.getValueList().size());
        assertEquals(10, prop2.getStructureValueList().size());
        assertNull(prop2.getResourceValueList());
        list = prop2.getValueList();
        for (int i = 1; i <= 10; i++) {
            String s = "Title " + i;
            assertEquals(s, list.get(i - 1).toString());
        }

        // setting a list via the single string value
        prop.setStructureValue(null);
        value = "Test|Toast|Hi|Ho";
        prop.setValue(value, CmsProperty.TYPE_SHARED);

        assertEquals(value, prop.getValue());
        assertEquals(value, prop.getResourceValue());
        assertNull(prop.getStructureValue());
        assertEquals(4, prop.getValueList().size());
        assertEquals(4, prop.getResourceValueList().size());
        assertNull(prop.getStructureValueList());
        assertEquals("Test", prop.getResourceValueList().get(0));
        assertEquals("Toast", prop.getResourceValueList().get(1));
        assertEquals("Hi", prop.getResourceValueList().get(2));
        assertEquals("Ho", prop.getResourceValueList().get(3));

        // check the behaviour if a list entry contains a delimiter character
        list = new ArrayList(3);
        String s = "delimtest|1";
        list.add(s);
        list.add("othervalue");
        list.add("thirdvalue");
        prop.setStructureValueList(list);
        s = CmsStringUtil.substitute(
            s,
            String.valueOf(CmsProperty.VALUE_LIST_DELIMITER),
            CmsProperty.VALUE_LIST_DELIMITER_REPLACEMENT);
        s += CmsProperty.VALUE_LIST_DELIMITER + "othervalue" + CmsProperty.VALUE_LIST_DELIMITER + "thirdvalue";
        assertEquals(s, prop.getStructureValue());

    }

    /**
     * Tests reading and writing property maps.<p>
     *
     * @throws Exception if the test fails
     */
    public void testPropertyMaps() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing reading and writing property maps");

        String source = "/xmlcontent/article_0001.html";
        cms.lockResource(source);

        CmsProperty prop;
        prop = cms.readPropertyObject(source, CmsPropertyDefinition.PROPERTY_TITLE, false);

        // set the value to a simple key/value list
        prop.setValue("a=1|b=2", CmsProperty.TYPE_INDIVIDUAL);
        assertEquals(2, prop.getStructureValueMap().size());
        assertEquals("1", prop.getStructureValueMap().get("a"));

        // now set the title as a map
        Map map = new HashMap(3);
        map.put("k1", "v1");
        map.put("k2", "v2|=");
        map.put("k3", "v3");
        prop.setStructureValueMap(map);

        // write the property object
        cms.writePropertyObject(source, prop);

        // read and check the property
        CmsProperty prop2 = cms.readPropertyObject(source, CmsPropertyDefinition.PROPERTY_TITLE, false);

        // check the created map
        assertEquals(3, prop2.getStructureValueMap().size());
        assertEquals("v1", prop2.getStructureValueMap().get("k1"));
        assertEquals("v2|=", prop2.getStructureValueMap().get("k2"));
        assertEquals("v3", prop2.getStructureValueMap().get("k3"));
        assertNull(prop.getResourceValueMap());

    }

    /**
     * Test for reading locale specific properties
     * @throws CmsException thrown if writing or reading the properties fails.
     */
    public void testReadLocalizedProperty() throws CmsException {

        CmsObject cms = getCmsObject();
        echo("Test reading local specific properties");
        CmsProperty property1 = new CmsProperty("all", "all", "all");
        CmsProperty property1de = new CmsProperty("all_de", "all de", "all de");
        CmsProperty property1deDE = new CmsProperty("all_de_DE", "all de DE", "all de DE");
        CmsProperty property2 = new CmsProperty("onlyde_de", "onlyde de", "onlyde de");

        List<CmsProperty> propertyList = new ArrayList<CmsProperty>();
        propertyList.add(property1);
        propertyList.add(property1de);
        propertyList.add(property1deDE);
        propertyList.add(property2);

        String resource = "/index.html";

        cms.lockResource(resource);
        cms.writePropertyObjects(resource, propertyList);
        cms.unlockResource(resource);

        CmsProperty prop = cms.readPropertyObject(resource, "all", false, new Locale("de", "DE"));
        assertEquals("all de DE", prop.getStructureValue());
        prop = cms.readPropertyObject(resource, "all", false, new Locale("de"));
        assertEquals("all de", prop.getStructureValue());
        prop = cms.readPropertyObject(resource, "all", false, null);
        assertEquals("all", prop.getStructureValue());
        prop = cms.readPropertyObject(resource, "all", false, new Locale("en", "GB"));
        assertEquals("all", prop.getStructureValue());

        prop = cms.readPropertyObject(resource, "onlyde", false, new Locale("de", "DE"));
        assertEquals("onlyde de", prop.getStructureValue());
        prop = cms.readPropertyObject(resource, "onlyde", false, new Locale("de"));
        assertEquals("onlyde de", prop.getStructureValue());
        prop = cms.readPropertyObject(resource, "onlyde", false, null);
        assertEquals(CmsProperty.getNullProperty(), prop);
        prop = cms.readPropertyObject(resource, "onlyde", false, new Locale("en", "GB"));
        assertEquals(CmsProperty.getNullProperty(), prop);

    }

    /**
     * Tests the writePropertyObject method for writing of a property on a folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadResourcesWithProperty() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing reading resources with property");

        String typesUri = "/types";
        CmsResource res = cms.readResource(typesUri);
        // now set "exportname" property and try again
        cms.lockResource(typesUri);
        cms.writePropertyObject(typesUri, new CmsProperty(CmsPropertyDefinition.PROPERTY_EXPORTNAME, "myfolder", null));
        // publish the changes
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        List result = cms.readResourcesWithProperty(CmsPropertyDefinition.PROPERTY_EXPORTNAME);
        assertTrue(result.contains(res));
    }

    /**
     * Tests the writePropertyObjects method for removing of multiple properties.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testRemoveProperties() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing removing multiple properties on a resource");
        CmsProperty property5 = new CmsProperty("Title", CmsProperty.DELETE_VALUE, CmsProperty.DELETE_VALUE);
        CmsProperty property6 = new CmsProperty("NavPos", CmsProperty.DELETE_VALUE, CmsProperty.DELETE_VALUE);
        List propertyList2 = new ArrayList();
        propertyList2.add(property5);
        propertyList2.add(property6);
        removeProperties(this, cms, "/folder1/page1.html", propertyList2);
    }

    /**
     * Tests the writePropertyObject method for removing of a single property.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testRemoveProperty() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing removing one property on a resource");
        CmsProperty property4 = new CmsProperty("Title", CmsProperty.DELETE_VALUE, CmsProperty.DELETE_VALUE);
        removeProperty(this, cms, "/folder1/page2.html", property4);
    }

    /**
     * Tests an issue with shared properties after deletion of the original sibling.<p>
     *
     * Scenario:
     * A file A has property P set with value V as shared property. Now A is renamed to B.
     * Then B is published directly with all siblings (A is now deleted and removed).
     * Issue: Property P is now empty in B, but should still have the V value.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSharedPropertyIssue1() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing issue with shared properties after deletion of the original sibling");

        // switch to the "Offline" project
        CmsProject offline = cms.readProject("Offline");
        cms.getRequestContext().setCurrentProject(offline);

        // create and publish the resource
        String source = "/folder1/testprop.txt";
        String dest = "/folder1/testprop2.txt";

        cms.createResource(source, CmsResourceTypePlain.getStaticTypeId());
        cms.unlockProject(offline.getUuid());
        OpenCms.getPublishManager().publishResource(cms, source);
        OpenCms.getPublishManager().waitWhileRunning();

        // now create the shared property on the source
        cms.lockResource(source);
        CmsProperty descProperty = new CmsProperty(CmsPropertyDefinition.PROPERTY_DESCRIPTION, null, "A shared value");
        cms.writePropertyObject(source, descProperty);
        cms.unlockProject(offline.getUuid());
        OpenCms.getPublishManager().publishResource(cms, source);
        OpenCms.getPublishManager().waitWhileRunning();

        // now move the resource to a new name and publish again, ensure the property is still there
        cms.lockResource(source);
        cms.moveResource(source, dest);
        cms.unlockProject(offline.getUuid());
        OpenCms.getPublishManager().publishResource(cms, dest, true, new CmsShellReport(Locale.ENGLISH));
        OpenCms.getPublishManager().waitWhileRunning();

        CmsProperty resultProperty = cms.readPropertyObject(dest, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false);
        assertEquals("A shared value", descProperty.getResourceValue());
        assertTrue(
            "Property '" + CmsPropertyDefinition.PROPERTY_DESCRIPTION + "' must be identical",
            descProperty.isIdentical(resultProperty));
    }

    /**
     * Tests the writeProperties method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testWriteProperties() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing writing multiple properties on a resource");
        CmsProperty property2 = new CmsProperty("Title", "OpenCms", null);
        CmsProperty property3 = new CmsProperty("NavPos", "1", null);
        List propertyList1 = new ArrayList();
        propertyList1.add(property2);
        propertyList1.add(property3);
        writeProperties(this, cms, "/folder1/page3.html", propertyList1);
    }

    /**
     * Tests the writePropertyObject method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testWriteProperty() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing writing one  property on a resource");
        CmsProperty property1 = new CmsProperty("Title", "OpenCms", null);
        writeProperty(this, cms, "/folder1/image1.gif", property1);
    }

    /**
     * Tests the writePropertyObject method for writing of a property on a folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testWritePropertyOnFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing writing one property on a folder");
        CmsProperty property10 = new CmsProperty("Title", "OpenCms", null);
        writeProperty(this, cms, "/folder2/", property10);
    }
}