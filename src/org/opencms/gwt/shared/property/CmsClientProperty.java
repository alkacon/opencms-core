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

package org.opencms.gwt.shared.property;

import org.opencms.util.CmsStringUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A client-side bean for representing an OpenCms property.<p>
 *
 * @since 8.0.0
 */
public class CmsClientProperty implements IsSerializable {

    /**
     * An enum used for addressing a specific value in a property.<p>
     */
    public enum Mode {
        /** The effective value. */
        effective, /** The resource value. */
        resource, /** The structure value. */
        structure;
    }

    /**
     * Construction function which creates a new property with a given name.<p>
     */
    public static final Function<String, CmsClientProperty> CREATE_PROPERTY = new Function<String, CmsClientProperty>() {

        /**
         * Creates a new property.<p>
         *
         * @param name the property name
         *
         * @return the new property
         */
        public CmsClientProperty apply(String name) {

            return new CmsClientProperty(name, "", "");
        }
    };

    /** The path component identifying a resource value. */
    public static final String PATH_RESOURCE_VALUE = "R";

    /** The path component identifying a structure value. */
    public static final String PATH_STRUCTURE_VALUE = "S";

    /** The copyright property name. */
    public static final String PROPERTY_COPYRIGHT = "Copyright";

    /** The default-file property name. */
    public static final String PROPERTY_DEFAULTFILE = "default-file";

    /** The Description property name. */
    public static final String PROPERTY_DESCRIPTION = "Description";

    /** The NavPos property name. */
    public static final String PROPERTY_NAVINFO = "NavInfo";

    /** The NavPos property name. */
    public static final String PROPERTY_NAVPOS = "NavPos";

    /** The NavText property name. */
    public static final String PROPERTY_NAVTEXT = "NavText";

    /** The NavText property name. */
    public static final String PROPERTY_TEMPLATE = "template";

    /** The Title property name. */
    public static final String PROPERTY_TITLE = "Title";

    /** The name of the property. */
    private String m_name;

    /** The origin of the property (will usually be null). */
    private String m_origin;

    /** The resource value of the property. */
    private String m_resourceValue;

    /** The structure value of the property. */
    private String m_structureValue;

    /**
     * Copy constructor.<p>
     *
     * @param property the object from which to copy the data
     */
    public CmsClientProperty(CmsClientProperty property) {

        m_name = property.m_name;
        m_structureValue = property.m_structureValue;
        m_resourceValue = property.m_resourceValue;
    }

    /**
     * Creates a new client property bean.<p>
     *
     * @param name the property name
     * @param structureValue the structure value
     * @param resourceValue the resource value
     */
    public CmsClientProperty(String name, String structureValue, String resourceValue) {

        m_name = name;
        m_structureValue = structureValue;
        m_resourceValue = resourceValue;
    }

    /**
     * Empty default constructor, used for serialization.<p>
     */
    protected CmsClientProperty() {

        //empty constructor for serialization
    }

    /**
     * Helper method for copying a map of properties.<p>
     *
     * @param props the property map to copy
     *
     * @return a copy of the property map
     */
    public static Map<String, CmsClientProperty> copyProperties(Map<String, CmsClientProperty> props) {

        Map<String, CmsClientProperty> result = new HashMap<String, CmsClientProperty>();
        for (Map.Entry<String, CmsClientProperty> entry : props.entrySet()) {
            String key = entry.getKey();
            CmsClientProperty copiedValue = new CmsClientProperty(entry.getValue());
            result.put(key, copiedValue);
        }
        return result;
    }

    /**
     * Gets the path value for a property object (which may be null) and a property access mode.<p>
     *
     * @param property the property which values to access
     * @param mode the property access mode
     *
     * @return the path value for the property and access mode
     */
    public static CmsPathValue getPathValue(CmsClientProperty property, Mode mode) {

        if (property != null) {
            return property.getPathValue(mode);
        }
        switch (mode) {
            case resource:
                return new CmsPathValue("", PATH_RESOURCE_VALUE);
            case structure:
                return new CmsPathValue("", PATH_STRUCTURE_VALUE);
            case effective:
                return new CmsPathValue("", PATH_STRUCTURE_VALUE);
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Checks if a property is null or empty.<p>
     *
     * @param prop the property to check
     *
     * @return true if the property is null or empty
     */
    public static boolean isPropertyEmpty(CmsClientProperty prop) {

        return (prop == null) || prop.isEmpty();
    }

    /**
     * Makes a "lazy copy" of a map of properties, which will create properties on lookup if they don't already exist.<p>
     *
     * @param properties the properties to copy
     *
     * @return the lazy copy of the properties
     */
    public static Map<String, CmsClientProperty> makeLazyCopy(Map<String, CmsClientProperty> properties) {

        if (properties == null) {
            return null;
        }
        return toLazyMap(copyProperties(properties));
    }

    /**
     * Helper method for removing empty properties from a map.<p>
     *
     * @param props the map from which to remove empty properties
     */
    public static void removeEmptyProperties(Map<String, CmsClientProperty> props) {

        Iterator<Map.Entry<String, CmsClientProperty>> iter = props.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, CmsClientProperty> entry = iter.next();
            CmsClientProperty value = entry.getValue();
            if (value.isEmpty()) {
                iter.remove();
            }
        }
    }

    /**
     * Creates a lazy property map which creates properties on lookup if they don'T exist.<p>
     *
     * @param properties the properties which should be initially put into the map
     *
     * @return the lazy property map
     */
    public static Map<String, CmsClientProperty> toLazyMap(Map<String, CmsClientProperty> properties) {

        Map<String, CmsClientProperty> result = new CmsLazyPropertyMap(properties);
        return result;
    }

    /**
     * Returns the effective value of the property.<p>
     *
     * @return the effective value of the property
     */
    public String getEffectiveValue() {

        return getPathValue().getValue();
    }

    /**
     * Returns the name of the property.<p>
     *
     * @return the name of the property
     */
    public String getName() {

        return m_name;
    }

    /**
     * Gets the origin of the property (might return null).<p>
     *
     * @return the origin root path of the property, or null
     */
    public String getOrigin() {

        return m_origin;
    }

    /**
     * Returns the effective path value of the property.<p>
     *
     * @return the effective path value of the property
     */
    public CmsPathValue getPathValue() {

        if (!CmsStringUtil.isEmpty(m_structureValue)) {
            return new CmsPathValue(m_structureValue, PATH_STRUCTURE_VALUE);
        } else if (!CmsStringUtil.isEmpty(m_resourceValue)) {
            return new CmsPathValue(m_resourceValue, PATH_RESOURCE_VALUE);
        } else {
            return new CmsPathValue(null, PATH_STRUCTURE_VALUE);
        }
    }

    /**
     * Gets the path value for a specific access mode.<p>
     * @param mode the access mode
     *
     * @return the path value for the access mode
     */
    public CmsPathValue getPathValue(Mode mode) {

        switch (mode) {
            case resource:
                return new CmsPathValue(m_resourceValue, PATH_RESOURCE_VALUE);
            case structure:
                return new CmsPathValue(m_structureValue, PATH_STRUCTURE_VALUE);
            case effective:
            default:
                return getPathValue();
        }
    }

    /**
     * Returns the resource value of the property.<p>
     *
     * @return the resource value
     */
    public String getResourceValue() {

        return m_resourceValue;
    }

    /***
     * Returns the structure value of the property.<p>
     *
     * @return  the structure value
     */
    public String getStructureValue() {

        return m_structureValue;
    }

    /**
     * Checks if both values of the property are empty.<p>
     *
     * @return true if both values of the property are empty
     */
    public boolean isEmpty() {

        return CmsStringUtil.isEmpty(m_resourceValue) && CmsStringUtil.isEmpty(m_structureValue);

    }

    /**
     * Sets the origin of the property.<p>
     *
     * @param origin the origin root path of the property
     */
    public void setOrigin(String origin) {

        m_origin = origin;
    }

    /**
     * Sets the resource value .<P>
     *
     * @param resourceValue the new resource value
     */
    public void setResourceValue(String resourceValue) {

        m_resourceValue = resourceValue;
    }

    /**
     * Sets the structure value.<p>
     *
     * @param structureValue the new structure value
     */
    public void setStructureValue(String structureValue) {

        m_structureValue = structureValue;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return Objects.toStringHelper(this).add("name", m_name).add("structureValue", m_structureValue).add(
            "resourceValue",
            m_resourceValue).toString();

    }

    /**
     * Creates a copy of the property, but changes the origin in the copy.<p>
     *
     * @param origin the new origin
     *
     * @return the copy of the property
     */
    public CmsClientProperty withOrigin(String origin) {

        CmsClientProperty property = new CmsClientProperty(this);
        property.setOrigin(origin);
        return property;
    }

}
