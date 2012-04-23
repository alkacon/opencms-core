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

package org.opencms.file;

import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsStringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;

/**
 * Represents a property (meta-information) mapped to a VFS resource.<p>
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
 * @since 6.0.0 
 */
public class CmsProperty implements Serializable, Cloneable, Comparable<CmsProperty> {

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
    public static final String DELETE_VALUE = "";

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

    /** The delimiter value for separating values in a list, per default this is the <code>|</code> char. */
    public static final char VALUE_LIST_DELIMITER = '|';

    /** The list delimiter replacement String used if the delimiter itself is contained in a String value. */
    public static final String VALUE_LIST_DELIMITER_REPLACEMENT = "%(ld)";

    /** The delimiter value for separating values in a map, per default this is the <code>=</code> char. */
    public static final char VALUE_MAP_DELIMITER = '=';

    /** The map delimiter replacement String used if the delimiter itself is contained in a String value. */
    public static final String VALUE_MAP_DELIMITER_REPLACEMENT = "%(md)";

    /** The null property object to be used in caches if a property is not found. */
    private static final CmsProperty NULL_PROPERTY = new CmsProperty();

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 93613508924212782L;

    /**
     * Boolean flag to decide if the property definition for this property should be created 
     * implicitly on any write operation if doesn't exist already.<p>
     */
    private boolean m_autoCreatePropertyDefinition;

    /** Indicates if the property is frozen (required for <code>NULL_PROPERTY</code>). */
    private boolean m_frozen;

    /** The name of this property. */
    private String m_name;

    /** The origin root path of the property. */
    private String m_origin;

    /** The value of this property attached to the resource record. */
    private String m_resourceValue;

    /** The (optional) value list of this property attached to the resource record. */
    private List<String> m_resourceValueList;

    /** The (optional) value map of this property attached to the resource record. */
    private Map<String, String> m_resourceValueMap;

    /** The value of this property attached to the structure record. */
    private String m_structureValue;

    /** The (optional) value list of this property attached to the structure record. */
    private List<String> m_structureValueList;

    /** The (optional) value map of this property attached to the structure record. */
    private Map<String, String> m_structureValueMap;

    /**
     * Creates a new CmsProperty object.<p>
     * 
     * The structure and resource property values are initialized to null. The structure and
     * resource IDs are initialized to {@link org.opencms.util.CmsUUID#getNullUUID()}.<p>
     */
    public CmsProperty() {

        // nothing to do, all values will be initialized with <code>null</code> or <code>false</code> by default
    }

    /**
     * Creates a new CmsProperty object using the provided values.<p>
     *
     * If the property definition does not exist for the resource type it
     * is automatically created when this property is written.
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
     * If <code>null</code> is supplied for the resource or structure value, this 
     * value will not be available for this property.<p>
     * 
     * @param name the name of the property definition
     * @param structureValue the value to write as structure property, or <code>null</code>
     * @param resourceValue the value to write as resource property , or <code>null</code>
     * @param autoCreatePropertyDefinition if <code>true</code>, the property definition for this property will be 
     *      created implicitly on any write operation if it doesn't exist already
     */
    public CmsProperty(String name, String structureValue, String resourceValue, boolean autoCreatePropertyDefinition) {

        m_name = name.trim();
        m_structureValue = structureValue;
        m_resourceValue = resourceValue;
        m_autoCreatePropertyDefinition = autoCreatePropertyDefinition;
    }

    /**
     * Static initializer required for freezing the <code>{@link #NULL_PROPERTY}</code>.<p>
     */
    static {

        NULL_PROPERTY.m_frozen = true;
        NULL_PROPERTY.m_name = "";
    }

    /**
     * Searches in a list for the first occurrence of a {@link CmsProperty} object with the given name.<p> 
     *
     * To check if the "null property" has been returned if a property was 
     * not found, use {@link #isNullProperty()} on the result.<p> 
     *
     * @param name a property name
     * @param list a list of {@link CmsProperty} objects
     * @return the index of the first occurrence of the name in they specified list, 
     *      or {@link CmsProperty#getNullProperty()} if the name is not found
     */
    public static final CmsProperty get(String name, List<CmsProperty> list) {

        CmsProperty property = null;
        name = name.trim();
        // choose the fastest method to traverse the list
        if (list instanceof RandomAccess) {
            for (int i = 0, n = list.size(); i < n; i++) {
                property = list.get(i);
                if (property.m_name.equals(name)) {
                    return property;
                }
            }
        } else {
            Iterator<CmsProperty> i = list.iterator();
            while (i.hasNext()) {
                property = i.next();
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
     * Calls <code>{@link #setAutoCreatePropertyDefinition(boolean)}</code> for each
     * property object in the given List with the given <code>value</code> parameter.<p>
     * 
     * This method will modify the objects in the input list directly.<p>
     * 
     * @param list a list of {@link CmsProperty} objects to modify
     * @param value boolean value
     * 
     * @return the modified list of {@link CmsProperty} objects
     * 
     * @see #setAutoCreatePropertyDefinition(boolean)
     */
    public static final List<CmsProperty> setAutoCreatePropertyDefinitions(List<CmsProperty> list, boolean value) {

        CmsProperty property;

        // choose the fastest method to traverse the list
        if (list instanceof RandomAccess) {
            for (int i = 0, n = list.size(); i < n; i++) {
                property = list.get(i);
                property.m_autoCreatePropertyDefinition = value;
            }
        } else {
            Iterator<CmsProperty> i = list.iterator();
            while (i.hasNext()) {
                property = i.next();
                property.m_autoCreatePropertyDefinition = value;
            }
        }

        return list;
    }

    /**
     * Calls <code>{@link #setFrozen(boolean)}</code> for each
     * {@link CmsProperty} object in the given List if it is not already frozen.<p>
     * 
     * This method will modify the objects in the input list directly.<p>
     * 
     * @param list a list of {@link CmsProperty} objects
     * 
     * @return the modified list of properties
     * 
     * @see #setFrozen(boolean)
     */
    public static final List<CmsProperty> setFrozen(List<CmsProperty> list) {

        CmsProperty property;

        // choose the fastest method to traverse the list
        if (list instanceof RandomAccess) {
            for (int i = 0, n = list.size(); i < n; i++) {
                property = list.get(i);
                if (!property.isFrozen()) {
                    property.setFrozen(true);
                }
            }
        } else {
            Iterator<CmsProperty> i = list.iterator();
            while (i.hasNext()) {
                property = i.next();
                if (!property.isFrozen()) {
                    property.setFrozen(true);
                }
            }
        }

        return list;
    }

    /**
     * Transforms a Map of String values into a list of 
     * {@link CmsProperty} objects with the property name set from the
     * Map key, and the structure value set from the Map value.<p>
     * 
     * @param map a Map with String keys and String values
     * 
     * @return a list of {@link CmsProperty} objects
     */
    public static List<CmsProperty> toList(Map<String, String> map) {

        if ((map == null) || (map.size() == 0)) {
            return Collections.emptyList();
        }

        List<CmsProperty> result = new ArrayList<CmsProperty>(map.size());
        Iterator<Map.Entry<String, String>> i = map.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, String> e = i.next();
            CmsProperty property = new CmsProperty(e.getKey(), e.getValue(), null);
            result.add(property);
        }

        return result;
    }

    /**
     * Transforms a list of {@link CmsProperty} objects into a Map which uses the property name as
     * Map key (String), and the property value as Map value (String).<p>
     * 
     * @param list a list of {@link CmsProperty} objects
     * 
     * @return a Map which uses the property names as
     *      Map keys (String), and the property values as Map values (String)
     */
    public static Map<String, String> toMap(List<CmsProperty> list) {

        if ((list == null) || (list.size() == 0)) {
            return Collections.emptyMap();
        }

        String name = null;
        String value = null;
        CmsProperty property = null;
        Map<String, String> result = new HashMap<String, String>(list.size());

        // choose the fastest method to traverse the list
        if (list instanceof RandomAccess) {
            for (int i = 0, n = list.size(); i < n; i++) {
                property = list.get(i);
                name = property.m_name;
                value = property.getValue();
                result.put(name, value);
            }
        } else {
            Iterator<CmsProperty> i = list.iterator();
            while (i.hasNext()) {
                property = i.next();
                name = property.m_name;
                value = property.getValue();
                result.put(name, value);
            }
        }

        return result;
    }

    /**
     * Checks if the property definition for this property will be 
     * created implicitly on any write operation if doesn't already exist.<p>
     * 
     * @return <code>true</code>, if the property definition for this property will be created implicitly on any write operation
     */
    public boolean autoCreatePropertyDefinition() {

        return m_autoCreatePropertyDefinition;
    }

    /**
     * Creates a clone of this property.<p>
     *  
     * @return a clone of this property
     * 
     * @see #cloneAsProperty()
     */
    @Override
    public CmsProperty clone() {

        return cloneAsProperty();
    }

    /**
     * Creates a clone of this property that already is of type <code>{@link CmsProperty}</code>.<p>
     * 
     * The cloned property will not be frozen.<p>
     * 
     * @return a clone of this property that already is of type <code>{@link CmsProperty}</code>
     */
    public CmsProperty cloneAsProperty() {

        if (this == NULL_PROPERTY) {
            // null property must never be cloned
            return NULL_PROPERTY;
        }
        CmsProperty clone = new CmsProperty();
        clone.m_name = m_name;
        clone.m_structureValue = m_structureValue;
        clone.m_structureValueList = m_structureValueList;
        clone.m_resourceValue = m_resourceValue;
        clone.m_resourceValueList = m_resourceValueList;
        clone.m_autoCreatePropertyDefinition = m_autoCreatePropertyDefinition;
        clone.m_origin = m_origin;
        // the value for m_frozen does not need to be set as it is false by default

        return clone;
    }

    /**
     * Compares this property to another Object.<p>
     * 
     * @param obj the other object to be compared
     * @return if the argument is a property object, returns zero if the name of the argument is equal to the name of this property object, 
     *      a value less than zero if the name of this property is lexicographically less than the name of the argument, 
     *      or a value greater than zero if the name of this property is lexicographically greater than the name of the argument 
     */
    public int compareTo(CmsProperty obj) {

        if (obj == this) {
            return 0;
        }
        return m_name.compareTo(obj.m_name);
    }

    /**
     * Tests if a specified object is equal to this CmsProperty object.<p>
     * 
     * Two property objects are equal if their names are equal.<p>
     * 
     * In case you want to compare the values as well as the name, 
     * use {@link #isIdentical(CmsProperty)} instead.<p>
     * 
     * @param obj another object
     * @return true, if the specified object is equal to this CmsProperty object
     * 
     * @see #isIdentical(CmsProperty)
     */
    @Override
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
     * @return the name of this property
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the root path of the resource from which the property was read.<p>
     * 
     * @return the root path of the resource from which the property was read
     */
    public String getOrigin() {

        return m_origin;
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
     * Returns the value of this property attached to the resource record, split as a list.<p>
     * 
     * This list is build form the resource value, which is split into separate values
     * using the <code>|</code> char as delimiter. If the delimiter is not found,
     * then the list will contain one entry which is equal to <code>{@link #getResourceValue()}</code>.<p>
     * 
     * @return the value of this property attached to the resource record, split as a (unmodifiable) list of Strings
     */
    public List<String> getResourceValueList() {

        if ((m_resourceValueList == null) && (m_resourceValue != null)) {
            // use lazy initializing of the list
            m_resourceValueList = createListFromValue(m_resourceValue);
            m_resourceValueList = Collections.unmodifiableList(m_resourceValueList);
        }
        return m_resourceValueList;
    }

    /**
     * Returns the value of this property attached to the resource record as a map.<p>
     * 
     * This map is build from the used value, which is split into separate key/value pairs
     * using the <code>|</code> char as delimiter. If the delimiter is not found,
     * then the map will contain one entry.<p>
     * 
     * The key/value pairs are separated with the <code>=</code>.<p>
     * 
     * @return the value of this property attached to the resource record, as an (unmodifiable) map of Strings
     */
    public Map<String, String> getResourceValueMap() {

        if ((m_resourceValueMap == null) && (m_resourceValue != null)) {
            // use lazy initializing of the map
            m_resourceValueMap = createMapFromValue(m_resourceValue);
            m_resourceValueMap = Collections.unmodifiableMap(m_resourceValueMap);
        }
        return m_resourceValueMap;
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
     * Returns the value of this property attached to the structure record, split as a list.<p>
     * 
     * This list is build form the structure value, which is split into separate values
     * using the <code>|</code> char as delimiter. If the delimiter is not found,
     * then the list will contain one entry which is equal to <code>{@link #getStructureValue()}</code>.<p>
     * 
     * @return the value of this property attached to the structure record, split as a (unmodifiable) list of Strings
     */
    public List<String> getStructureValueList() {

        if ((m_structureValueList == null) && (m_structureValue != null)) {
            // use lazy initializing of the list
            m_structureValueList = createListFromValue(m_structureValue);
            m_structureValueList = Collections.unmodifiableList(m_structureValueList);
        }
        return m_structureValueList;
    }

    /**
     * Returns the value of this property attached to the structure record as a map.<p>
     * 
     * This map is build from the used value, which is split into separate key/value pairs
     * using the <code>|</code> char as delimiter. If the delimiter is not found,
     * then the map will contain one entry.<p>
     * 
     * The key/value pairs are separated with the <code>=</code>.<p>
     * 
     * @return the value of this property attached to the structure record, as an (unmodifiable) map of Strings
     */
    public Map<String, String> getStructureValueMap() {

        if ((m_structureValueMap == null) && (m_structureValue != null)) {
            // use lazy initializing of the map
            m_structureValueMap = createMapFromValue(m_structureValue);
            m_structureValueMap = Collections.unmodifiableMap(m_structureValueMap);
        }
        return m_structureValueMap;
    }

    /**
     * Returns the compound value of this property.<p>
     * 
     * The value returned is the value of {@link #getStructureValue()}, if it is not <code>null</code>.
     * Otherwise the value if {@link #getResourceValue()} is returned (which may also be <code>null</code>).<p>
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
     * @param defaultValue a default value which is returned if both the structure and resource values are <code>null</code>
     * 
     * @return the compound value of this property, or the default value
     */
    public String getValue(String defaultValue) {

        if (this == CmsProperty.NULL_PROPERTY) {
            // return the default value if this property is the null property
            return defaultValue;
        }

        // somebody might have set both values to null manually
        // on a property object different from the null property...
        return (m_structureValue != null) ? m_structureValue : ((m_resourceValue != null)
        ? m_resourceValue
        : defaultValue);
    }

    /**
     * Returns the compound value of this property, split as a list.<p>
     * 
     * This list is build form the used value, which is split into separate values
     * using the <code>|</code> char as delimiter. If the delimiter is not found,
     * then the list will contain one entry.<p>
     * 
     * The value returned is the value of {@link #getStructureValueList()}, if it is not <code>null</code>.
     * Otherwise the value of {@link #getResourceValueList()} is returned (which may also be <code>null</code>).<p>
     * 
     * @return the compound value of this property, split as a (unmodifiable) list of Strings
     */
    public List<String> getValueList() {

        return (m_structureValue != null) ? getStructureValueList() : getResourceValueList();
    }

    /**
     * Returns the compound value of this property, split as a list, or a specified default value list,
     * if both the structure and resource values are null.<p>
     * 
     * In other words, this method returns the defaultValue if this property object 
     * is the null property (see {@link CmsProperty#getNullProperty()}).<p>
     * 
     * @param defaultValue a default value list which is returned if both the structure and resource values are <code>null</code>
     * 
     * @return the compound value of this property, split as a (unmodifiable) list of Strings
     */
    public List<String> getValueList(List<String> defaultValue) {

        if (this == CmsProperty.NULL_PROPERTY) {
            // return the default value if this property is the null property
            return defaultValue;
        }

        // somebody might have set both values to null manually
        // on a property object different from the null property...
        return (m_structureValue != null) ? getStructureValueList() : ((m_resourceValue != null)
        ? getResourceValueList()
        : defaultValue);
    }

    /**
     * Returns the compound value of this property as a map.<p>
     * 
     * This map is build from the used value, which is split into separate key/value pairs
     * using the <code>|</code> char as delimiter. If the delimiter is not found,
     * then the map will contain one entry.<p>
     * 
     * The key/value pairs are separated with the <code>=</code>.<p>
     * 
     * The value returned is the value of {@link #getStructureValueMap()}, if it is not <code>null</code>.
     * Otherwise the value of {@link #getResourceValueMap()} is returned (which may also be <code>null</code>).<p>
     * 
     * @return the compound value of this property as a (unmodifiable) map of Strings
     */
    public Map<String, String> getValueMap() {

        return (m_structureValue != null) ? getStructureValueMap() : getResourceValueMap();
    }

    /**
     * Returns the compound value of this property as a map, or a specified default value map,
     * if both the structure and resource values are null.<p>
     * 
     * In other words, this method returns the defaultValue if this property object 
     * is the null property (see {@link CmsProperty#getNullProperty()}).<p>
     * 
     * @param defaultValue a default value map which is returned if both the structure and resource values are <code>null</code>
     * 
     * @return the compound value of this property as a (unmodifiable) map of Strings
     */
    public Map<String, String> getValueMap(Map<String, String> defaultValue) {

        if (this == CmsProperty.NULL_PROPERTY) {
            // return the default value if this property is the null property
            return defaultValue;
        }

        // somebody might have set both values to null manually
        // on a property object different from the null property...
        return (m_structureValue != null) ? getStructureValueMap() : ((m_resourceValue != null)
        ? getResourceValueMap()
        : defaultValue);
    }

    /**
     * Returns the hash code of the property, which is based only on the property name, not on the values.<p>
     * 
     * The resource and structure values are not taken into consideration for the hashcode generation 
     * because the {@link #equals(Object)} implementation also does not take these into consideration.<p>
     *
     * @return the hash code of the property
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_name.hashCode();
    }

    /**
     * Checks if the resource value of this property should be deleted when this
     * property object is written to the database.<p>
     * 
     * @return true, if the resource value of this property should be deleted
     * @see CmsProperty#DELETE_VALUE
     */
    public boolean isDeleteResourceValue() {

        return (m_resourceValue == DELETE_VALUE) || ((m_resourceValue != null) && (m_resourceValue.length() == 0));
    }

    /**
     * Checks if the structure value of this property should be deleted when this
     * property object is written to the database.<p>
     * 
     * @return true, if the structure value of this property should be deleted
     * @see CmsProperty#DELETE_VALUE
     */
    public boolean isDeleteStructureValue() {

        return (m_structureValue == DELETE_VALUE) || ((m_structureValue != null) && (m_structureValue.length() == 0));
    }

    /**
     * Returns <code>true</code> if this property is frozen, that is read only.<p>
     *
     * @return <code>true</code> if this property is frozen, that is read only
     */
    public boolean isFrozen() {

        return m_frozen;
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

        return NULL_PROPERTY.equals(this);
    }

    /**
     * Sets the boolean flag to decide if the property definition for this property should be 
     * created implicitly on any write operation if doesn't exist already.<p>
     * 
     * @param value true, if the property definition for this property should be created implicitly on any write operation
     */
    public void setAutoCreatePropertyDefinition(boolean value) {

        checkFrozen();
        m_autoCreatePropertyDefinition = value;
    }

    /**
     * Sets the frozen state of the property, if set to <code>true</code> then this property is read only.<p>
     *
     * If the property is already frozen, then setting the frozen state to <code>true</code> again is allowed, 
     * but setting the value to <code>false</code> causes a <code>{@link CmsRuntimeException}</code>.<p>
     *
     * @param frozen the frozen state to set
     */
    public void setFrozen(boolean frozen) {

        if (!frozen) {
            checkFrozen();
        }
        m_frozen = frozen;
    }

    /**
     * Sets the name of this property.<p>
     * 
     * @param name the name to set
     */
    public void setName(String name) {

        checkFrozen();
        m_name = name.trim();
    }

    /**
     * Sets the path of the resource from which the property was read.<p>
     * 
     * @param originRootPath the root path of the root path from which the property was read
     */
    public void setOrigin(String originRootPath) {

        checkFrozen();
        m_origin = originRootPath;
    }

    /**
     * Sets the value of this property attached to the resource record.<p>
     * 
     * @param resourceValue the value of this property attached to the resource record
     */
    public void setResourceValue(String resourceValue) {

        checkFrozen();
        m_resourceValue = resourceValue;
        m_resourceValueList = null;
    }

    /**
     * Sets the value of this property attached to the resource record from the given list of Strings.<p>
     * 
     * The value will be created from the individual values of the given list, which are appended
     * using the <code>|</code> char as delimiter.<p>
     * 
     * @param valueList the list of value (Strings) to attach to the resource record
     */
    public void setResourceValueList(List<String> valueList) {

        checkFrozen();
        if (valueList != null) {
            m_resourceValueList = new ArrayList<String>(valueList);
            m_resourceValueList = Collections.unmodifiableList(m_resourceValueList);
            m_resourceValue = createValueFromList(m_resourceValueList);
        } else {
            m_resourceValueList = null;
            m_resourceValue = null;
        }
    }

    /**
     * Sets the value of this property attached to the resource record from the given map of Strings.<p>
     * 
     * The value will be created from the individual values of the given map, which are appended
     * using the <code>|</code> char as delimiter, the map keys and values are separated by a <code>=</code>.<p>
     * 
     * @param valueMap the map of key/value (Strings) to attach to the resource record
     */
    public void setResourceValueMap(Map<String, String> valueMap) {

        checkFrozen();
        if (valueMap != null) {
            m_resourceValueMap = new HashMap<String, String>(valueMap);
            m_resourceValueMap = Collections.unmodifiableMap(m_resourceValueMap);
            m_resourceValue = createValueFromMap(m_resourceValueMap);
        } else {
            m_resourceValueMap = null;
            m_resourceValue = null;
        }
    }

    /**
     * Sets the value of this property attached to the structure record.<p>
     * 
     * @param structureValue the value of this property attached to the structure record
     */
    public void setStructureValue(String structureValue) {

        checkFrozen();
        m_structureValue = structureValue;
        m_structureValueList = null;
    }

    /**
     * Sets the value of this property attached to the structure record from the given list of Strings.<p>
     * 
     * The value will be created from the individual values of the given list, which are appended
     * using the <code>|</code> char as delimiter.<p>
     * 
     * @param valueList the list of value (Strings) to attach to the structure record
     */
    public void setStructureValueList(List<String> valueList) {

        checkFrozen();
        if (valueList != null) {
            m_structureValueList = new ArrayList<String>(valueList);
            m_structureValueList = Collections.unmodifiableList(m_structureValueList);
            m_structureValue = createValueFromList(m_structureValueList);
        } else {
            m_structureValueList = null;
            m_structureValue = null;
        }
    }

    /**
     * Sets the value of this property attached to the structure record from the given map of Strings.<p>
     * 
     * The value will be created from the individual values of the given map, which are appended
     * using the <code>|</code> char as delimiter, the map keys and values are separated by a <code>=</code>.<p>
     * 
     * @param valueMap the map of key/value (Strings) to attach to the structure record
     */
    public void setStructureValueMap(Map<String, String> valueMap) {

        checkFrozen();
        if (valueMap != null) {
            m_structureValueMap = new HashMap<String, String>(valueMap);
            m_structureValueMap = Collections.unmodifiableMap(m_structureValueMap);
            m_structureValue = createValueFromMap(m_structureValueMap);
        } else {
            m_structureValueMap = null;
            m_structureValue = null;
        }
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

        checkFrozen();
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
    @Override
    public String toString() {

        StringBuffer strBuf = new StringBuffer();

        strBuf.append("[").append(getClass().getName()).append(": ");
        strBuf.append("name: '").append(m_name).append("'");
        strBuf.append(", value: '").append(getValue()).append("'");
        strBuf.append(", structure value: '").append(m_structureValue).append("'");
        strBuf.append(", resource value: '").append(m_resourceValue).append("'");
        strBuf.append(", frozen: ").append(m_frozen);
        strBuf.append(", origin: ").append(m_origin);
        strBuf.append("]");

        return strBuf.toString();
    }

    /**
     * Checks if this property is frozen, that is read only.<p> 
     */
    private void checkFrozen() {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_PROPERTY_FROZEN_1, toString()));
        }
    }

    /**
     * Returns the list value representation for the given String.<p>
     * 
     * The given value is split along the <code>|</code> char.<p>
     * 
     * @param value the value to create the list representation for
     * 
     * @return the list value representation for the given String
     */
    private List<String> createListFromValue(String value) {

        if (value == null) {
            return null;
        }
        List<String> result = CmsStringUtil.splitAsList(value, VALUE_LIST_DELIMITER);
        if (value.indexOf(VALUE_LIST_DELIMITER_REPLACEMENT) != -1) {
            List<String> tempList = new ArrayList<String>(result.size());
            Iterator<String> i = result.iterator();
            while (i.hasNext()) {
                String item = i.next();
                tempList.add(rebuildDelimiter(item, VALUE_LIST_DELIMITER, VALUE_LIST_DELIMITER_REPLACEMENT));
            }
            result = tempList;
        }

        return result;
    }

    /**
     * Returns the map value representation for the given String.<p>
     * 
     * The given value is split along the <code>|</code> char, the map keys and values are separated by a <code>=</code>.<p>
     * 
     * @param value the value to create the map representation for
     * 
     * @return the map value representation for the given String
     */
    private Map<String, String> createMapFromValue(String value) {

        if (value == null) {
            return null;
        }
        List<String> entries = createListFromValue(value);
        Iterator<String> i = entries.iterator();
        Map<String, String> result = new HashMap<String, String>(entries.size());
        boolean rebuildDelimiters = false;
        if (value.indexOf(VALUE_MAP_DELIMITER_REPLACEMENT) != -1) {
            rebuildDelimiters = true;
        }
        while (i.hasNext()) {
            String entry = i.next();
            int index = entry.indexOf(VALUE_MAP_DELIMITER);
            if (index != -1) {
                String key = entry.substring(0, index);
                String val = "";
                if ((index + 1) < entry.length()) {
                    val = entry.substring(index + 1);
                }
                if (CmsStringUtil.isNotEmpty(key)) {
                    if (rebuildDelimiters) {
                        key = rebuildDelimiter(key, VALUE_MAP_DELIMITER, VALUE_MAP_DELIMITER_REPLACEMENT);
                        val = rebuildDelimiter(val, VALUE_MAP_DELIMITER, VALUE_MAP_DELIMITER_REPLACEMENT);
                    }
                    result.put(key, val);
                }
            }
        }
        return result;
    }

    /**
     * Returns the single String value representation for the given value list.<p>
     * 
     * @param valueList the value list to create the single String value for
     * 
     * @return the single String value representation for the given value list
     */
    private String createValueFromList(List<String> valueList) {

        if (valueList == null) {
            return null;
        }
        StringBuffer result = new StringBuffer(valueList.size() * 32);
        Iterator<String> i = valueList.iterator();
        while (i.hasNext()) {
            result.append(replaceDelimiter(i.next().toString(), VALUE_LIST_DELIMITER, VALUE_LIST_DELIMITER_REPLACEMENT));
            if (i.hasNext()) {
                result.append(VALUE_LIST_DELIMITER);
            }
        }
        return result.toString();
    }

    /**
     * Returns the single String value representation for the given value map.<p>
     * 
     * @param valueMap the value map to create the single String value for
     * 
     * @return the single String value representation for the given value map
     */
    private String createValueFromMap(Map<String, String> valueMap) {

        if (valueMap == null) {
            return null;
        }
        StringBuffer result = new StringBuffer(valueMap.size() * 32);
        Iterator<Map.Entry<String, String>> i = valueMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, String> entry = i.next();
            String key = entry.getKey();
            String value = entry.getValue();
            key = replaceDelimiter(key, VALUE_LIST_DELIMITER, VALUE_LIST_DELIMITER_REPLACEMENT);
            key = replaceDelimiter(key, VALUE_MAP_DELIMITER, VALUE_MAP_DELIMITER_REPLACEMENT);
            value = replaceDelimiter(value, VALUE_LIST_DELIMITER, VALUE_LIST_DELIMITER_REPLACEMENT);
            value = replaceDelimiter(value, VALUE_MAP_DELIMITER, VALUE_MAP_DELIMITER_REPLACEMENT);
            result.append(key);
            result.append(VALUE_MAP_DELIMITER);
            result.append(value);
            if (i.hasNext()) {
                result.append(VALUE_LIST_DELIMITER);
            }
        }
        return result.toString();
    }

    /**
     * Rebuilds the given delimiter character from the replacement string.<p>
     * 
     * @param value the string that is scanned
     * @param delimiter the delimiter character to rebuild
     * @param delimiterReplacement the replacement string for the delimiter character
     * @return the substituted string
     */
    private String rebuildDelimiter(String value, char delimiter, String delimiterReplacement) {

        return CmsStringUtil.substitute(value, delimiterReplacement, String.valueOf(delimiter));
    }

    /**
     * Replaces the given delimiter character with the replacement string.<p>
     *  
     * @param value the string that is scanned
     * @param delimiter the delimiter character to replace
     * @param delimiterReplacement the replacement string for the delimiter character
     * @return the substituted string
     */
    private String replaceDelimiter(String value, char delimiter, String delimiterReplacement) {

        return CmsStringUtil.substitute(value, String.valueOf(delimiter), delimiterReplacement);
    }
}