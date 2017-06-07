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

package org.opencms.acacia.client.entity;

import org.opencms.acacia.shared.CmsEntity;
import org.opencms.acacia.shared.CmsEntityAttribute;
import org.opencms.acacia.shared.CmsType;

import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * Tests for the acacia editor data back-end.<p>
 */
public class EntityBackendTest extends GWTTestCase {

    /** The complex type attribute name. */
    public static final String ATTRIBUTE_NAME = "http://complex/simpleAttribute";

    /** The complex type name. */
    public static final String COMPLEX_TYPE_ID = "cms:complex";

    /** The simple type name. */
    public static final String SIMPLE_TYPE_ID = "cms:simple";

    /** Change counter. */
    private int m_changeCount;

    /**
     * @see com.google.gwt.junit.client.GWTTestCase#getModuleName()
     */
    @Override
    public String getModuleName() {

        return "org.opencms.acacia.AcaciaBare";
    }

    /**
     * Tests the deep copy and remove methods.<p>
     */
    public void testCopyAndRemoveEntity() {

        I_CmsEntityBackend vie = getBackendInstance();
        CmsType simpleType = vie.createType(SIMPLE_TYPE_ID);
        CmsType complex = vie.createType(COMPLEX_TYPE_ID);
        complex.addAttribute(ATTRIBUTE_NAME, simpleType, 1, 3);
        String typeId = "type:very-complex";
        CmsType veryComplex = vie.createType(typeId);
        // using types defined in previous test
        String complexAttributeName = "complexAttribute";
        veryComplex.addAttribute(complexAttributeName, complex, 0, 1);
        String attributeEntityId = "my-attribute-entity";
        CmsEntity attributeEntity = vie.createEntity(attributeEntityId, COMPLEX_TYPE_ID);
        String value = "my attribute value";
        attributeEntity.addAttributeValue(ATTRIBUTE_NAME, value);
        String testEntityId = "my-test-entity";
        CmsEntity testEntity = vie.createEntity(testEntityId, typeId);
        testEntity.addAttributeValue(complexAttributeName, attributeEntity);

        CmsEntity copy = testEntity.createDeepCopy("my-entity-copy");
        CmsEntityAttribute attribute = copy.getAttribute(complexAttributeName);
        assertNotNull("The entity should have an attribute: " + complexAttributeName, attribute);
        assertTrue("The attribute should be of the a complex type", attribute.isComplexValue());
        System.out.println("trying to remove");
        // test remove entity function
        vie.removeEntity(testEntityId);
        assertNull("The entity should no longer be registered", vie.getEntity(testEntityId));
        assertNull("The child entity should also be removed", vie.getEntity(attributeEntityId));
    }

    /**
     * Tests if a new entity can be created within VIE.<p>
     */
    public void testCreateEntity() {

        String entityId = "http://myEntityId";
        String entityType = "cms:myType";
        I_CmsEntityBackend vie = getBackendInstance();
        vie.createType(entityType);
        CmsEntity entity = vie.createEntity(entityId, entityType);
        assertNotNull("The newly created entity should not be null", entity);
        assertEquals("The entity id should match the initial id", entityId, entity.getId());
        assertEquals("The type name should match the initial type name", entityType, entity.getTypeName());
    }

    /**
     * Tests the type creation.<p>
     */
    public void testCreateType() {

        String typeId = "cms:mySimpleType";
        CmsType type = getBackendInstance().createType(typeId);
        assertNotNull("The newly created type should not be null", type);
        assertEquals("The type id should match the initial id", typeId, type.getId());
        assertTrue("This should be a simple type, as it has no attributes.", type.isSimpleType());
    }

    /**
     * Tests the creation of new type attributes.<p>
     */
    public void testEntityAttribute() {

        I_CmsEntityBackend vie = getBackendInstance();

        CmsType simple = vie.createType(SIMPLE_TYPE_ID);
        CmsType complex = vie.createType(COMPLEX_TYPE_ID);
        complex.addAttribute(ATTRIBUTE_NAME, simple, 1, 3);
        assertNotNull("The newly created type should not be null", simple);
        assertNotNull("The newly created type should not be null", complex);
        assertEquals("The type id should match the initial id", SIMPLE_TYPE_ID, simple.getId());
        assertEquals("The type id should match the initial id", COMPLEX_TYPE_ID, complex.getId());
        assertTrue("This should be a simple type, as it has no attributes.", simple.isSimpleType());
        //        assertFalse("This should be no simple type, as it has attributes.", complex.isSimpleType());
        assertNotNull("The attribute list should not be null.", complex.getAttributeNames());
        assertEquals("The attribute names list should have a length of 1.", 1, complex.getAttributeNames().size());
        String attributeType = complex.getAttributeTypeName(ATTRIBUTE_NAME);
        assertEquals("The attribute should be of the type: " + SIMPLE_TYPE_ID, SIMPLE_TYPE_ID, attributeType);
        assertEquals(
            "The max occurrence should be the same as set above",
            3,
            complex.getAttributeMaxOccurrence(ATTRIBUTE_NAME));
        assertEquals(
            "The min occurrence should be the same as set above",
            1,
            complex.getAttributeMinOccurrence(ATTRIBUTE_NAME));
        List<String> attributes = complex.getAttributeNames();
        assertEquals("The attribute name should be as given above.", ATTRIBUTE_NAME, attributes.get(0));

        // testing attribute values
        CmsEntity entity = vie.createEntity("my-entity", complex.getId());
        String value = "my attribute value";
        entity.addAttributeValue(ATTRIBUTE_NAME, value);
        CmsEntityAttribute attribute = entity.getAttribute(ATTRIBUTE_NAME);
        assertNotNull("The entity should have an attribute: " + ATTRIBUTE_NAME, attribute);
        assertEquals("The attribute value should match the previously set value", value, attribute.getSimpleValue());
        assertTrue("The attribute should only have a single value", attribute.isSingleValue());
        entity.addAttributeValue(ATTRIBUTE_NAME, "");
        attribute = entity.getAttribute(ATTRIBUTE_NAME);
        assertFalse("The attribute should no longer be a single value attribute", attribute.isSingleValue());
        assertEquals("The value count should be 2", 2, attribute.getValueCount());

        // testing empty string attribute value
        CmsEntity entity2 = vie.createEntity("my-second-entity", complex.getId());
        String value2 = "";
        entity2.addAttributeValue(ATTRIBUTE_NAME, value2);
        CmsEntityAttribute attribute2 = entity2.getAttribute(ATTRIBUTE_NAME);
        assertNotNull(
            "The entity should have an attribute even though the value was an empty string: " + ATTRIBUTE_NAME,
            attribute2);
        assertEquals(
            "The attribute value should match the previously set empty string value",
            value2,
            attribute2.getSimpleValue());

    }

    /**
     * Tests the event handling on entities.<p>
     * Relies on the types created in {@link #testEntityAttribute()}.<p>
     */
    public void testEntityEvents() {

        I_CmsEntityBackend vie = getBackendInstance();
        CmsType simpleType = vie.createType(SIMPLE_TYPE_ID);
        CmsType complex = vie.createType(COMPLEX_TYPE_ID);
        complex.addAttribute(ATTRIBUTE_NAME, simpleType, 1, 3);
        CmsEntity entity = vie.createEntity("myEntity", COMPLEX_TYPE_ID);
        final String changeValue = "my new value";
        resetChangeCount();
        entity.addValueChangeHandler(new ValueChangeHandler<CmsEntity>() {

            public void onValueChange(ValueChangeEvent<CmsEntity> event) {

                incrementChangeCount();
                String attributeValue = event.getValue().getAttribute(ATTRIBUTE_NAME).getSimpleValue();
                assertEquals(changeValue, attributeValue);
            }
        });
        entity.setAttributeValue(ATTRIBUTE_NAME, changeValue);
        assertEquals(1, getChangeCount());
    }

    /**
     * Tests if an attribute with three identical values remains intact as a nested entity.<p>
     */
    public void testSameValueAttributes() {

        I_CmsEntityBackend vie = getBackendInstance();
        String mySimple = "simple:mySimple";
        String myComplex = "complex:myComplex";
        String myComplexNested = "complex:myComplexNested";
        String nestedAttributeName = "http://complex/complexAttribute";
        CmsType simpleType = vie.createType(mySimple);
        CmsType complexNested = vie.createType(myComplexNested);
        complexNested.addAttribute(ATTRIBUTE_NAME, simpleType, 1, 3);
        CmsType complex = vie.createType(myComplex);
        complex.addAttribute(nestedAttributeName, complex, 1, 3);
        // testing attribute values
        CmsEntity nestedEntity = vie.createEntity("my-same-value-entity", complexNested.getId());
        String value = "test";
        nestedEntity.addAttributeValue(ATTRIBUTE_NAME, value);
        nestedEntity.addAttributeValue(ATTRIBUTE_NAME, value);
        nestedEntity.addAttributeValue(ATTRIBUTE_NAME, value);
        CmsEntity mainEntity = vie.createEntity("kljsfh", complex.getId());
        mainEntity.addAttributeValue(nestedAttributeName, nestedEntity);
        assertEquals(
            "There should be three attribute values",
            3,
            mainEntity.getAttribute(nestedAttributeName).getComplexValue().getAttribute(
                ATTRIBUTE_NAME).getValueCount());
    }

    /**
     * Tests the DOM element selector methods of VIE.<p>
     */
    public void testSelectors() {

        assertTrue("Document should be available", Document.get() != null);
        assertTrue("Document body should be available", Document.get().getBody() != null);
        Element div = Document.get().createDivElement();
        div.setAttribute("about", "http://testEntity");
        Element innerDiv = Document.get().createDivElement();
        innerDiv.setInnerText("my value");
        innerDiv.setAttribute("property", ATTRIBUTE_NAME);
        div.appendChild(innerDiv);
        Document.get().getBody().appendChild(div);
        List<com.google.gwt.dom.client.Element> elements = getBackendInstance().getAttributeElements(
            "http://testEntity",
            ATTRIBUTE_NAME,
            null);
        assertNotNull(elements);
        assertEquals(1, elements.size());
        assertEquals("my value", elements.get(0).getInnerText());
    }

    /**
     * Returns the change count.<p>
     *
     * @return the change count
     */
    protected int getChangeCount() {

        return m_changeCount;
    }

    /**
     * Increments the change counter.<p>
     */
    protected void incrementChangeCount() {

        m_changeCount++;
    }

    /**
     * Returns the {@link CmsEntityBackend} instance.<p>
     *
     * @return the {@link CmsEntityBackend} instance
     */
    private I_CmsEntityBackend getBackendInstance() {

        return new CmsEntityBackend();
    }

    /**
     * Resets the change counter.<p>
     */
    private void resetChangeCount() {

        m_changeCount = 0;
    }
}