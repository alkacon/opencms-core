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

package org.opencms.file;

import org.opencms.main.CmsException;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.util.CmsUUID;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Unit test for the "createPropertyDefinition", "readPropertyDefiniton" and
 * "readAllPropertyDefintions" methods of the CmsObject.<p>
 *
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPropertyDefinition extends OpenCmsTestRunner {

    /**
     * Test the createPropertyDefintion method.<p>
     *
     * @param tc the OpenCms test runner
     * @param cms the CmsObject
     * @param propertyDefiniton1 the property definition to create
     * @throws Throwable if something goes wrong
     */
    public static void createPropertyDefinition(OpenCmsTestRunner tc, CmsObject cms, String propertyDefiniton1)
    throws Throwable {

        // get all propertydefintions
        List allPropertydefintions = cms.readAllPropertyDefinitions();
        // create a property defintion with a dummy id value (the real id is created by db)
        CmsPropertyDefinition prop = new CmsPropertyDefinition(CmsUUID.getNullUUID(), propertyDefiniton1);

        cms.createPropertyDefinition(propertyDefiniton1);

        // check if the propertsdefintion was written
        tc.assertPropertydefinitionExist(cms, prop);
        // check if all other properties are still identical
        tc.assertPropertydefinitions(cms, allPropertydefintions, prop);
    }

    /**
     * Test to create, read and delete a property definition through the cache driver.<p>
     *
     * @param cms the CmsObject
     * @throws Throwable if something goes wrong
     */
    public static void createReadDeletePropertyDefinition(CmsObject cms) throws Throwable {

        CmsPropertyDefinition propertyDefinition = null;
        String propertyDefinitionName = "new-unknown-property";

        // 1) create a property definition

        try {
            // create a new property definition
            propertyDefinition = cms.createPropertyDefinition(propertyDefinitionName);
        } catch (CmsException e) {
            fail("Error creating property definition " + propertyDefinitionName + ", " + e.toString());
            return;
        }

        assertEquals(propertyDefinitionName, propertyDefinition.getName());

        // 2) read the created property definition

        try {
            // read the created property definition
            propertyDefinition = null;
            propertyDefinition = cms.readPropertyDefinition(propertyDefinitionName);
        } catch (CmsException e) {
            fail("Error reading property definition " + propertyDefinitionName + ", " + e.toString());
            return;
        }

        assertEquals(propertyDefinitionName, propertyDefinition.getName());

        // 3) check if the new created property is contained in the list of all property definitions

        List allPropertyDefinitions = null;
        try {
            // read all property definitions
            allPropertyDefinitions = cms.readAllPropertyDefinitions();
        } catch (CmsException e) {
            fail("Error reading all property definitions, " + e.toString());
            return;
        }

        boolean found = false;
        for (int i = 0, n = allPropertyDefinitions.size(); i < n; i++) {
            if (((CmsPropertyDefinition)allPropertyDefinitions.get(i)).equals(propertyDefinition)) {
                found = true;
                break;
            }
        }

        if (!found) {
            fail("Property definition " + propertyDefinitionName + " is not in the list of all property definitions");
        }

        // 4) delete the new property definition again

        try {
            // delete the property definition again
            cms.deletePropertyDefinition(propertyDefinitionName);
        } catch (CmsException e) {
            fail("Error deleting property definition " + propertyDefinitionName + ", " + e.toString());
        }

        // 5) re-read the delete property definition to check that it is not found

        try {
            // re-read the created property definition
            propertyDefinition = null;
            propertyDefinition = cms.readPropertyDefinition(propertyDefinitionName);
        } catch (CmsException e) {
            // intentionally left blank
        }

        assertNull(propertyDefinition);

        // 6) re-read the list of all property definitions to check that it is not contained

        allPropertyDefinitions = null;
        try {
            // read all property definitions
            allPropertyDefinitions = cms.readAllPropertyDefinitions();
        } catch (CmsException e) {
            fail("Error reading all property definitions, " + e.toString());
            return;
        }

        found = false;
        for (int i = 0, n = allPropertyDefinitions.size(); i < n; i++) {
            if (((CmsPropertyDefinition)allPropertyDefinitions.get(i)).equals(propertyDefinition)) {
                found = true;
                break;
            }
        }

        if (found) {
            fail("Property definition " + propertyDefinitionName + " is still in the list of all property definitions");
        }

    }

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Test the createPropertyDefintion method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(1)
    public void testCreatePropertyDefinition() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing createPropetyDefinition and readPropertyDefiniton");
        createPropertyDefinition(this, cms, "NewPropertyDefinition");
    }

    /**
     * Test to create, read and delete a property definition through the cache driver.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(2)
    public void testCreateReadDeletePropertyDefinition() throws Throwable {

        CmsObject cms = getCmsObject();

        echo("Testing creation of a new property definition");
        createReadDeletePropertyDefinition(cms);
    }

    /**
     * Tests reading all resources that have a specific property definition set.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(3)
    public void testGetResourcesWithProperty() throws Exception {

        echo("Testing reading all resources with a specific property");
        CmsObject cms = getCmsObject();
        List result;

        result = cms.readResourcesWithProperty(CmsPropertyDefinition.PROPERTY_TITLE);
        assertEquals(63, result.size());
        result = cms.readResourcesWithProperty(CmsPropertyDefinition.PROPERTY_TEMPLATE);
        assertEquals(25, result.size());
    }

}
