/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsProperty.java,v $
 * Date   : $Date: 2005/06/27 23:22:15 $
 * Version: $Revision: 1.28 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.file;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;

/**
 * Represents a property mapped to a structure and/or resource record of a resource.<p>
 * 
 * A property is an object that contains three string values: a name, a property value which is mapped
 * to the structure record of a resource, and a property value which is mapped to the resource
 * record of a resource. A property object is valid if it has both values or just one value set.
 * Each property needs at least a name and one value set.<p>
 * 
 * A property value mapped to the structure record of a resource is significant for a single
 * resource (sibling). A property value mapped to the resource record of a resource is significant
 * for all siblings of a resource record. This is possible by getting the "compound value" 
 * (see {@link #getValue()}) of a property in case a property object has both values set. The compound 
 * value of a property object is the value mapped to the structure record, because it's structure 
 * value is more significant than it's resource value. This allows to set a property only one time 
 * on the resource record, and the property takes effect on all siblings of this resource record.<p>
 * 
 * The ID of the structure or resource record where a property value is mapped to is represented by 
 * the "PROPERTY_MAPPING_ID" table attribute in the database. The "PROPERTY_MAPPING_TYPE" table 
 * attribute (see {@link #STRUCTURE_RECORD_MAPPING} and {@link #RESOURCE_RECORD_MAPPING})
 * determines whether the value of the "PROPERTY_MAPPING_ID" attribute of the current row is
 * a structure or resource record ID.<p>
 * 
 * Property objects are written to the database using {@link org.opencms.file.CmsObject#writePropertyObject(String, CmsProperty)}
 * or {@link org.opencms.file.CmsObject#writePropertyObjects(String, List)}, no matter
 * whether you want to save a new (non-existing) property, update an existing property, or delete an
 * existing property. To delete a property you would write a property object with either the
 * structure and/or resource record values set to {@link #DELETE_VALUE} to indicate that a
 * property value should be deleted in the database. Set property values to null if they should
 * remain unchanged in the database when a property object is written. As for example you want to
 * update just the structure value of a property, you would set the structure value to the new string,
 * and the resource value to null (which is already the case by default).<p>
 * 
 * Use {@link #setAutoCreatePropertyDefinition(boolean)} to set a boolean flag whether a missing property
 * definition should be created implicitly for a resource type when a property is written to the database.
 * The default value for this flag is <code>false</code>. Thus, you receive a CmsException if you try
 * to write a property of a resource with a resource type which lacks a property definition for
 * this resource type. It is not a good style to set {@link #setAutoCreatePropertyDefinition(boolean)}
 * on true to make writing properties to the database work in any case, because then you will loose
 * control about which resource types support which property definitions.<p>
 * 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.28 $
 * 
 * @since 6.0.0 
 */
public class CmsProperty implements Serializable, Cloneable, Comparable {

    /**
     * Signals that the resource property values of a resource
     * should be deleted using deleteAllProperties.<p>
     */
    public static final int DELETE_OPTION_DELETE_RESOURCE_VALUES = 3;

    /**
     * Signals that both the structure and resource property values of a resource
     * should be deleted using deleteAllProperties.<p>
     */
    public static final int DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES = 1;

    /**
     * Signals that the structure property values of a resource
     * should be deleted using deleteAllProperties.<p>
     */
    public static final int DELETE_OPTION_DELETE_STRUCTURE_VALUES = 2;

    /**
     * An empty string to decide that a property value should be deleted when this
     * property object is written to the database.<p>
     */
    public static final String DELETE_VALUE = new String("");

    /**
     * Value of the "mapping-type" database attribute to indicate that a property value is mapped
     * to a resource record.<p>
     */
    public static final int RESOURCE_RECORD_MAPPING = 2;

    /**
     * Value of the "mapping-type" database attribute to indicate that a property value is mapped
     * to a structure record.<p>
     */
    public static final int STRUCTURE_RECORD_MAPPING = 1;

    /** Key used for a individual (structure) property value. */
    public static final String TYPE_INDIVIDUAL = "individual";

    /** Key used for a shared (resource) property value. */
    public static final String TYPE_SHARED = "shared";

    /**
     * A null property object to be used in caches if a property is not found.<p>
     */
    private static final CmsProperty NULL_PROPERTY = new CmsProperty();

    /**
     * Boolean flag to decide if the property definition for this property should be created 
     * implicitly on any write operation if doesn't exist already.<p>
     */
    private boolean m_autoCreatePropertyDefinition;

    /**
     * The name of this property.<p>
     */
    private String m_name;

    /**
     * The value of this property attached to the structure record.<p>
     */
    private String m_resourceValue;

    /**
     * The value of this property attached to the resource record.<p>
     */
    private String m_structureValue;

    /**
     * Creates a new CmsProperty object.<p>
     * 
     * The structure and resource property values are initialized to null. The structure and
     * resource IDs are initialized to {@link org.opencms.util.CmsUUID#getNullUUID()}.<p>
     */
    public CmsProperty() {

        // noting to do, all values will be initialized with "null" or "false" by default
        m_name = null;
        m_structureValue = null;
        m_resourceValue = null;
        m_autoCreatePropertyDefinition = false;
    }

    /**
     * Creates a new CmsProperty object using the provided values.<p>
     *
     * If the property definition does not exist for the resource type it
     * is automatically created when this propery is written.
     * 
     * @param name the name of the property definition
     * @param structureValue the value to write as structure property
     * @param resourceValue the value to write as resource property 
     */
    public CmsProperty(String name, String structureValue, String resourceValue) {

        this(name, structureValue, resourceValue, true);
    }

    /**
     * Creates a new CmsProperty object using the provided values.<p>
     * 
     * @param name the name of the property definition
     * @param structureValue the value to write as structure property
     * @param resourceValue the value to write as resource property 
     * @param autoCreatePropertyDefinition true, if the property definition for this property should be created mplicitly on any write operation if doesn't exist already
     */
    public CmsProperty(String name, String structureValue, String resourceValue, boolean autoCreatePropertyDefinition) {

        m_name = name;
        m_structureValue = structureValue;
        m_resourceValue = resourceValue;
        m_autoCreatePropertyDefinition = autoCreatePropertyDefinition;
    }

    /**
     * Searches in a list for the first occurence of a Cms property object with the given name.<p> 
     *
     * To check if the "null property" has been returned if a property was 
     * not found, use {@link #isNullProperty()} on the result.<p> 
     *
     * @param name a property name
     * @param list a list of Cms property objects
     * @return the index of the first occurrence of the name in they specified list, or the "null-property" if the name is not found
     */
    public static final CmsProperty get(String name, List list) {

        CmsProperty property = null;

        // choose the fastest method to traverse the list
        if (list instanceof RandomAccess) {
            for (int i = 0, n = list.size(); i < n; i++) {
                property = (CmsProperty)list.get(i);
                if (property.m_name.equals(name)) {
                    return property;
                }
            }
        } else {
            Iterator i = list.iterator();
            while (i.hasNext()) {
                property = (CmsProperty)i.next();
                if (property.m_name.equals(name)) {
                    return property;
                }
            }
        }

        return NULL_PROPERTY;
    }

    /**
     * Returns the null property object.<p>
     * 
     * @return the null property object
     */
    public static final CmsProperty getNullProperty() {

        return NULL_PROPERTY;
    }

    /**
     * Sets in each property object of a specified list a boolean value to decide if missing 
     * property definitions should be created implicitly or not if the property objects of the
     * list are written to the database.<p>
     * 
     * @param list a list of property objects
     * @param value boolean value
     * @return the list which each property object set that missing property definitions should be created implicitly
     * @see #setAutoCreatePropertyDefinition(boolean)
     */
    public static final List setAutoCreatePropertyDefinitions(List list, boolean value) {

        CmsProperty property = null;

        // choose the fastest method to traverse the list
        if (list instanceof RandomAccess) {
            for (int i = 0, n = list.size(); i < n; i++) {
                property = (CmsProperty)list.get(i);
                property.m_autoCreatePropertyDefinition = value;
            }
        } else {
            Iterator i = list.iterator();
            while (i.hasNext()) {
                property = (CmsProperty)i.next();
                property.m_autoCreatePropertyDefinition = value;
            }
        }

        return list;
    }

    /**
     * Transforms a map with compound (String) values keyed by property names into a list of 
     * CmsProperty objects with structure values.<p>
     * 
     * This method is to prevent issues with backward incompatibilities in older code.
     * Use this method with caution, it might be removed without with being deprecated
     * before.<p>
     * 
     * @param map a map with compound (String) values keyed by property names
     * @return a list of CmsProperty objects
     */
    public static List toList(Map map) {

        String name = null;
        String value = null;
        CmsProperty property = null;
        List properties = null;
        Object[] names = null;

        if (map == null || map.size() == 0) {
            return Collections.EMPTY_LIST;
        }

        properties = new ArrayList(map.size());
        names = map.keySet().toArray();
        for (int i = 0; i < names.length; i++) {
            name = (String)names[i];
            value = (String)map.get(name);

            property = new CmsProperty();
            property.m_name = name;
            property.m_structureValue = value;
            properties.add(property);
        }

        return properties;
    }

    /**
     * Transforms a list of CmsProperty objects with structure and resource values into a map with
     * compound (String) values keyed by property names.<p>
     *
     * This method is to prevent issues with backward incompatibilities in older code.
     * Use this method with caution, it might be removed without with being deprecated
     * before.<p>
     * 
     * @param list a list of CmsProperty objects
     * @return a map with compound (String) values keyed by property names
     */
    public static Map toMap(List list) {

        Map result = null;
        String name = null;
        String value = null;
        CmsProperty property = null;

        if (list == null || list.size() == 0) {
            return Collections.EMPTY_MAP;
        }

        result = new HashMap();

        // choose the fastest method to traverse the list
        if (list instanceof RandomAccess) {
            for (int i = 0, n = list.size(); i < n; i++) {
                property = (CmsProperty)list.get(i);
                name = property.m_name;
                value = property.getValue();
                result.put(name, value);
            }
        } else {
            Iterator i = list.iterator();
            while (i.hasNext()) {
                property = (CmsProperty)i.next();
                name = property.m_name;
                value = property.getValue();
                result.put(name, value);
            }
        }

        return result;
    }

    /**
     * Checks if the property definition for this property should be 
     * created implicitly on any write operation if doesn't exist already.<p>
     * 
     * @return true, if the property definition for this property should be created implicitly on any write operation
     */
    public boolean autoCreatePropertyDefinition() {

        return m_autoCreatePropertyDefinition;
    }

    /**
     * Creates a clone of this property.<p>
     *  
     * @return a clone of this property
     */
    public Object clone() {

        CmsProperty property = new CmsProperty();

        property.m_name = m_name;
        property.m_structureValue = m_structureValue;
        property.m_resourceValue = m_resourceValue;
        property.m_autoCreatePropertyDefinition = m_autoCreatePropertyDefinition;

        return property;
    }

    /**
     * Compares this property to another Object.<p>
     * 
     * If the Object is a property, this method behaves like {@link String#compareTo(java.lang.String)}.
     * Otherwise, it throws a ClassCastException (as properties are comparable only to other properties).<p>
     *  
     * @param obj the other object to be compared
     * @return if the argument is a property object, returns zero if the name of the argument is equal to the name of this property object, 
     *      a value less than zero if the name of this property is lexicographically less than the name of the argument, 
     *      or a value greater than zero if the name of this property is lexicographically greater than the name of the argument 
     */
    public int compareTo(Object obj) {

        if (obj == this) {
            return 0;
        }
        if (obj instanceof CmsProperty) {
            return m_name.compareTo(((CmsProperty)obj).m_name);
        }
        return 0;
    }

    /**
     * Checks if the resource value of this property should be deleted when this
     * property object is written to the database.<p>
     * 
     * @return true, if the resource value of this property should be deleted
     * @see CmsProperty#DELETE_VALUE
     */
    public boolean deleteResourceValue() {

        return (m_resourceValue != null && m_resourceValue.length() == 0);
    }

    /**
     * Checks if the structure value of this property should be deleted when this
     * property object is written to the database.<p>
     * 
     * @return true, if the structure value of this property should be deleted
     * @see CmsProperty#DELETE_VALUE
     */
    public boolean deleteStructureValue() {

        return (m_structureValue != null && m_structureValue.length() == 0);
    }

    /**
     * Tests if a specified object is equal to this CmsProperty object.<p>
     * 
     * Two property objecs are equal if their names are equal.<p>
     * 
     * @param obj another object
     * @return true, if the specified object is equal to this CmsProperty object
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsProperty) {
            return ((CmsProperty)obj).m_name.equals(m_name);
        }
        return false;
    }

    /**
     * Returns the name of this property.<p>
     * 
     * @return name of this property
     * 
     * @deprecated use {@link #getName()} instead
     */
    public String getKey() {

        return getName();
    }

    /**
     * Returns the name of this property.<p>
     * 
     * @return the name of this property
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the value of this property attached to the resource record.<p>
     * 
     * @return the value of this property attached to the resource record
     */
    public String getResourceValue() {

        return m_resourceValue;
    }

    /**
     * Returns the value of this property attached to the structure record.<p>
     * 
     * @return the value of this property attached to the structure record
     */
    public String getStructureValue() {

        return m_structureValue;
    }

    /**
     * Returns the compound value of this property.<p>
     * 
     * The value returned is the structure value, if only the structure value is set.
     * Dito for the resource value, if only the resource value is set. If both values are
     * set, the structure value is returned.
     * 
     * @return the compound value of this property
     */
    public String getValue() {

        return (m_structureValue != null) ? m_structureValue : m_resourceValue;
    }

    /**
     * Returns the compound value of this property, or a specified default value,
     * if both the structure and resource values are null.<p>
     * 
     * In other words, this method returns the defaultValue if this property object 
     * is the null property (see {@link CmsProperty#getNullProperty()}).<p>
     * 
     * @param defaultValue a default value which is returned if both the structure and resource values are null
     * @return the compound value of this property, or the default value
     */
    public String getValue(String defaultValue) {

        if (this == CmsProperty.NULL_PROPERTY) {
            // return the default value if this property is the null property
            return defaultValue;
        }

        // somebody might have set both values to null manually
        // on a property object different from the null property...
        return (m_structureValue != null) ? m_structureValue : ((m_resourceValue != null) ? m_resourceValue
        : defaultValue);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        StringBuffer strBuf = new StringBuffer();

        strBuf.append(m_name);
        strBuf.append("_");
        strBuf.append(m_structureValue);
        strBuf.append("_");
        strBuf.append(m_resourceValue);

        return strBuf.toString().hashCode();
    }

    /**
     * Tests if a given CmsProperty is identical to this CmsProperty object.<p>
     * 
     * The property object are identical if their name, structure and 
     * resource values are all equals.<p>
     * 
     * @param property another property object
     * @return true, if the specified object is equal to this CmsProperty object
     */
    public boolean isIdentical(CmsProperty property) {

        boolean isEqual;

        // compare the name
        if (m_name == null) {
            isEqual = (property.getName() == null);
        } else {
            isEqual = m_name.equals(property.getName());
        }

        // compare the structure value
        if (m_structureValue == null) {
            isEqual &= (property.getStructureValue() == null);
        } else {
            isEqual &= m_structureValue.equals(property.getStructureValue());
        }

        // compare the resource value
        if (m_resourceValue == null) {
            isEqual &= (property.getResourceValue() == null);
        } else {
            isEqual &= m_resourceValue.equals(property.getResourceValue());
        }

        return isEqual;
    }

    /**
     * Checks if this property object is the null property object.<p>
     * 
     * @return true if this property object is the null property object
     */
    public boolean isNullProperty() {

        return this == NULL_PROPERTY;
    }

    /**
     * Sets the boolean flag to decide if the property definition for this property should be 
     * created implicitly on any write operation if doesn't exist already.<p>
     * 
     * @param value true, if the property definition for this property should be created implicitly on any write operation
     */
    public void setAutoCreatePropertyDefinition(boolean value) {

        m_autoCreatePropertyDefinition = value;
    }

    /**
     * Sets the name of this property.<p>
     * 
     * @param name the name of this property
     * 
     * @deprecated use {@link #setName(String)} instead
     */
    public void setKey(String name) {

        setName(name);
    }

    /**
     * Sets the name of this property.<p>
     * 
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the value of this property attached to the resource record.<p>
     * 
     * @param resourceValue the value of this property attached to the resource record
     */
    public void setResourceValue(String resourceValue) {

        m_resourceValue = resourceValue;
    }

    /**
     * Sets the value of this property attached to the structure record.<p>
     * 
     * @param structureValue the value of this property attached to the structure record
     */
    public void setStructureValue(String structureValue) {

        m_structureValue = structureValue;
    }

    /**
     * Sets the value of this property as either shared or 
     * individual value.<p>
     * 
     * If the given type equals {@link CmsProperty#TYPE_SHARED} then
     * the value is set as a shared (resource) value, otherwise it
     * is set as individual (structure) value.<p>
     * 
     * @param value the value to set
     * @param type the value type to set
     */
    public void setValue(String value, String type) {

        setAutoCreatePropertyDefinition(true);
        if (TYPE_SHARED.equalsIgnoreCase(type)) {
            // set the provided value as shared (resource) value
            setResourceValue(value);
        } else {
            // set the provided value as individual (structure) value
            setStructureValue(value);
        }
    }

    /**
     * Returns a string representation of this property object.<p>
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer strBuf = new StringBuffer();

        strBuf.append("[").append(getClass().getName()).append(": ");
        strBuf.append("name: '").append(m_name).append("'");
        strBuf.append(", value: '").append(getValue()).append("'");
        strBuf.append(", structure value: '").append(m_structureValue).append("'");
        strBuf.append(", resource value: '").append(m_resourceValue).append("'");
        strBuf.append("]");

        return strBuf.toString();
    }

}