/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/Attic/CmsProperty.java,v $
 * Date   : $Date: 2004/03/31 14:01:10 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.db;

import org.opencms.util.CmsUUID;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;

/**
 * Represents a property mapped to a structure and/or resource record.<p>
 * 
 * A property is an object that contains three string values: a key, a property value which is mapped
 * to the structure record of a resource, and a property value which is mapped to the resource
 * record of a resource. A property object is valid if it has both values or just one value set.
 * Each property needs at least a key and one value set.<p>
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
 * attribute (see {@link #C_STRUCTURE_RECORD_MAPPING} and {@link #C_RESOURCE_RECORD_MAPPING})
 * determines whether the value of the "PROPERTY_MAPPING_ID" attribute of the current row is
 * a structure or resource record ID. A property object contains also the UUID's of the structure 
 * and/or resource record(s) where it's values are mapped to.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2004/03/31 14:01:10 $
 * @since build_5_1_14
 */
public class CmsProperty extends Object implements Serializable, Cloneable, Comparable {

    /**
     * An empty string to decide that a property value should be deleted when this
     * property object is written to the database.<p>
     */
    public static final String C_DELETE_VALUE = new String("");

    /**
     * A null property object to be used in caches if a property is not found.<p>
     */
    private static final CmsProperty C_NULL_PROPERTY = new CmsProperty();

    /**
     * Value of the "mapping-type" database attribute to indicate that a property value is mapped
     * to a resource record.<p>
     */
    public static final int C_RESOURCE_RECORD_MAPPING = 2;

    /**
     * Value of the "mapping-type" database attribute to indicate that a property value is mapped
     * to a structure record.<p>
     */
    public static final int C_STRUCTURE_RECORD_MAPPING = 1;

    /**
     * Boolean flag to decide if the property definition for this property should be created 
     * implicitly on any write operation if doesn't exist already.<p>
     */
    private boolean m_createPropertyDefinition;

    /**
     * The key name of this property.<p>
     */
    private String m_key;

    /**
     * The UUID of the resource record where this property is mapped to.<p>
     */
    private CmsUUID m_resourceId;

    /**
     * The value of this property attached to the structure record.<p>
     */
    private String m_resourceValue;

    /**
     * The UUID of the structure record where this property is mapped to.<p>
     */
    private CmsUUID m_structureId;

    /**
     * The value of this property attached to the resource record.<p>
     */
    private String m_structureValue;

    /**
     * Creates a new CmsProperty object.<p>
     * 
     * The structure and resource property values are initialized to null. The structure and
     * resource IDs are initialized to {@link CmsUUID#getNullUUID()}.<p>
     */
    public CmsProperty() {
        m_key = null;
        m_structureValue = null;
        m_resourceValue = null;
        m_createPropertyDefinition = false;
        m_structureId = CmsUUID.getNullUUID();
        m_resourceId = CmsUUID.getNullUUID();
    }

    /**
     * Returns the null property object.<p>
     * 
     * @return the null property object
     */
    public static final CmsProperty getNullProperty() {
        return CmsProperty.C_NULL_PROPERTY;
    }

    /**
     * Transforms a map with compound (String) values keyed by property keys into a list of 
     * CmsProperty objects with structure values.<p>
     * 
     * @param map a map with compound (String) values keyed by property keys
     * @return a list of CmsProperty objects
     */
    public static List toList(Map map) {
        String key = null;
        String value = null;
        CmsProperty property = null;
        List properties = (List) new ArrayList(map.size());
        Object[] keys = null;

        keys = map.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            key = (String) keys[i];
            value = (String) map.get(key);

            property = new CmsProperty();
            property.m_key = key;
            property.m_structureValue = value;
            properties.add(property);
        }
        
        return properties;
    }

    /**
     * Transforms a list of CmsProperty objects with structure and resource values into a map with
     * compound (String) values keyed by property keys.<p>
     * 
     * @param list a list of CmsProperty objects
     * @return a map with compound (String) values keyed by property keys
     */
    public static Map toMap(List list) {
        Map result = null;
        String key = null;
        String value = null;
        CmsProperty property = null;

        if (list == null || list.size() == 0) {
            return Collections.EMPTY_MAP;
        }

        result = (Map) new HashMap();
        
        // choose the fastest method to iterate the list
        if (list instanceof RandomAccess) {
            for (int i = 0, n = list.size(); i < n; i++) {
                property = (CmsProperty) list.get(i);
                key = property.m_key;
                value = property.getValue();
                result.put(key, value);
            }
        } else {
            Iterator i = list.iterator();
            while (i.hasNext()) {
                property = (CmsProperty) i.next();
                key = property.m_key;
                value = property.getValue();
                result.put(key, value);                
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
    public boolean createPropertyDefinition() {
        return m_createPropertyDefinition;
    }

    /**
     * Sets the boolean flag to decide if the property definition for this property should be 
     * created implicitly on any write operation if doesn't exist already.<p>
     * 
     * @param value true, if the property definition for this property should be created implicitly on any write operation
     */
    public void setCreatePropertyDefinition(boolean value) {
        m_createPropertyDefinition = value;
    }

    /**
     * Sets in each property object of a specified list a boolean value to decide if missing 
     * property definitions should be created implicitly or not if the property objects of the
     * list are written to the database.<p>
     * 
     * @param list a list of property objects
     * @param value boolean value
     * @return the list which each property object set that missing property definitions should be created implicitly
     * @see #setCreatePropertyDefinition(boolean)
     */
    public static final List setCreatePropertyDefinitions(List list, boolean value) {
        CmsProperty property = null;

        // choose the fastest method to traverse the list
        if (list instanceof RandomAccess) {
            for (int i = 0, n = list.size(); i < n; i++) {
                property = (CmsProperty) list.get(i);
                property.m_createPropertyDefinition = value;
            }
        } else {
            Iterator i = list.iterator();
            while (i.hasNext()) {
                property = (CmsProperty) i.next();
                property.m_createPropertyDefinition = value;
            }
        }
        
        return list;
    }
    
    /**
     * Checks if the resource value of this property should be deleted when this
     * property object is written to the database.<p>
     * 
     * @return true, if the resource value of this property should be deleted
     * @see CmsProperty#C_DELETE_VALUE
     */
    public boolean deleteResourceValue() {
        return (m_resourceValue != null && m_resourceValue.length() == 0);
    }

    /**
     * Checks if the structure value of this property should be deleted when this
     * property object is written to the database.<p>
     * 
     * @return true, if the structure value of this property should be deleted
     * @see CmsProperty#C_DELETE_VALUE
     */
    public boolean deleteStructureValue() {
        return (m_structureValue != null && m_structureValue.length() == 0);
    }

    /**
     * Tests if a specified object is equal to this CmsProperty object.<p>
     * 
     * @param object another object
     * @return true, if the specified object is equal to this CmsProperty object
     */
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (object == null || !(object instanceof CmsProperty)) {
            return false;
        }

        // compare the key
        if (m_key == null) {
            isEqual = (((CmsProperty) object).getKey() == null);
        } else {
            isEqual = m_key.equals(((CmsProperty) object).getKey());
        }

        // compare the structure value
        if (m_structureValue == null) {
            isEqual &= (((CmsProperty) object).getStructureValue() == null);
        } else {
            isEqual &= m_structureValue.equals(((CmsProperty) object).getStructureValue());
        }

        // compare the resource value
        if (m_resourceValue == null) {
            isEqual &= (((CmsProperty) object).getResourceValue() == null);
        } else {
            isEqual &= m_resourceValue.equals(((CmsProperty) object).getResourceValue());
        }

        return isEqual;
    }

    /**
     * Returns the key name of this property.<p>
     * 
     * @return key name of this property
     */
    public String getKey() {
        return m_key;
    }

    /**
     * Returns the UUID of the resource record where this property is mapped to.<p>
     * 
     * @return the UUID of the resource record where this property is mapped to
     */
    public CmsUUID getResourceId() {
        return m_resourceId;
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
     * Returns the UUID of the structure record where this property is mapped to.<p>
     * 
     * @return the UUID of the structure record where this property is mapped to
     */
    public CmsUUID getStructureId() {
        return m_structureId;
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
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return new StringBuffer().append(m_key).append("_").append(m_structureValue).append("_").append(m_resourceValue).toString().hashCode();
    }

    /**
     * Sets the key name of this property.<p>
     * 
     * @param key the key name of this property
     */
    public void setKey(String key) {
        m_key = key;
    }

    /**
     * Sets the UUID of the resource record where this property is mapped to.<p>
     * 
     * @param resourceId the UUID of the resource record where this property is mapped to
     */
    public void setResourceId(CmsUUID resourceId) {
        m_resourceId = resourceId;
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
     * Sets the UUID of the structure record where this property is mapped to.<p>
     * 
     * @param structureId the UUID of the structure record where this property is mapped to
     */
    public void setStructureId(CmsUUID structureId) {
        m_structureId = structureId;
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
     * Returns a string representation of this property object.<p>
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer strBuf = new StringBuffer();

        strBuf.append("[").append(getClass().getName()).append(": ");
        strBuf.append("key: ").append(m_key);
        strBuf.append(", value: '").append(getValue()).append("'");
        strBuf.append(", structure value: '").append(m_structureValue).append("'");
        strBuf.append(", resource value: '").append(m_resourceValue).append("'");
        strBuf.append("]");

        return strBuf.toString();
    }
    
    /**
     * Compares two CmsProperty objects by their key names.<p>
     *  
     * @param object another object to compare with this property object
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object object) {
        if (object instanceof CmsProperty) {
            return m_key.compareTo(((CmsProperty) object).getKey());
        }

        return 0;
    }    

}
