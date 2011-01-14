/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/properties/Attic/CmsPropertyInheritanceState.java,v $
 * Date   : $Date: 2011/01/14 12:05:15 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.sitemap.properties;

import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class encapsulates a state of a property inheritance calculation for sitemap entries.<p>
 * 
 * Warning: This class is used by GWT client-side code (See GwtBase.gwt.xml for a list of
 * classes used by GWT client-side code). If you change this class, either make sure that 
 * your changes are compatible with GWT, or write a separate client version of the class 
 * and put it into super_src. 
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public class CmsPropertyInheritanceState {

    /** The current state's map of computed/inherited properties. */
    private Map<String, CmsComputedPropertyValue> m_currentProperties;

    /** The property configuration. */
    private Map<String, CmsXmlContentProperty> m_propertyConfig;

    /**
     * Creates a new property inheritance state based on an initial set of inherited properties.<p>
     * 
     * @param initialProps the initial computed properties 
     * @param propertyConfiguration the property configuration 
     */
    public CmsPropertyInheritanceState(
        Map<String, CmsComputedPropertyValue> initialProps,
        Map<String, CmsXmlContentProperty> propertyConfiguration) {

        m_currentProperties = initialProps;
        m_propertyConfig = propertyConfiguration;
    }

    /**
     * Creates a new property inheritance state and also uses the property configuration to initialize the property values.<p>
     * 
     * @param initialProps the initial properties 
     * @param propertyConfiguration the property configuration 
     * @param updateDefaults if true, the property configuration will be used to initialize the property values 
     */
    public CmsPropertyInheritanceState(
        Map<String, CmsComputedPropertyValue> initialProps,
        Map<String, CmsXmlContentProperty> propertyConfiguration,
        boolean updateDefaults) {

        m_propertyConfig = propertyConfiguration;
        if (updateDefaults) {
            m_currentProperties = combinePropertyMaps(
                initialProps,
                getComputedPropertyValuesFromPropertyDefinitions(propertyConfiguration));
        } else {
            m_currentProperties = initialProps;
        }
    }

    /**
     * Creates an empty property inheritance state.<p>
     * 
     * @param propertyConfiguration the property configuration 
     */
    public CmsPropertyInheritanceState(Map<String, CmsXmlContentProperty> propertyConfiguration) {

        m_currentProperties = getComputedPropertyValuesFromPropertyDefinitions(propertyConfiguration);
        m_propertyConfig = propertyConfiguration;
    }

    /**
     * Combines a parent computed property value and a child computed property value into a result computed property value
     * for the child.<p>
     *  
     * @param parentValue the computed property value for the parent 
     * @param childValue the computed property value for the child 
     * @return the combined computed property value for the child  
     */
    public static CmsComputedPropertyValue combineParentAndChildValues(
        CmsComputedPropertyValue parentValue,
        CmsComputedPropertyValue childValue) {

        if ((parentValue == null) && (childValue == null)) {
            // both are null; we're done 
            return null;
        }
        // only one is null; replace it with a CmsComputedPropertyValue containing nulls 
        parentValue = CmsComputedPropertyValue.normalize(parentValue);
        childValue = CmsComputedPropertyValue.normalize(childValue);

        // compute both components of the result 
        CmsSourcedValue newInherit = CmsSourcedValue.overrideValue(
            parentValue.getInheritSourcedValue(),
            childValue.getInheritSourcedValue());
        CmsSourcedValue newOwn = CmsSourcedValue.overrideValue(newInherit, childValue.getOwnSourcedValue());

        if ((newOwn == null) && (newInherit == null)) {
            return null;
        }
        CmsComputedPropertyValue result = new CmsComputedPropertyValue(newOwn, newInherit);
        return result;
    }

    /**
     * Converts a property definition map to a map containing the default property values configured in the property definitions.<p>
     *   
     * @param propDefs the property definitions 
     * 
     * @return a map of default property values 
     */
    public static Map<String, CmsComputedPropertyValue> getComputedPropertyValuesFromPropertyDefinitions(
        Map<String, CmsXmlContentProperty> propDefs) {

        Map<String, CmsComputedPropertyValue> result = new HashMap<String, CmsComputedPropertyValue>();
        for (Map.Entry<String, CmsXmlContentProperty> entry : propDefs.entrySet()) {
            String key = entry.getKey();
            CmsXmlContentProperty propDef = entry.getValue();
            if (propDef.getDefault() != null) {
                CmsComputedPropertyValue propValue = CmsComputedPropertyValue.createDefaultValue(propDef.getDefault());
                result.put(key, propValue);
            }
        }
        return result;
    }

    /**
     * Returns the currently inherited properties of the state.<p>
     * 
     * @return the map of inherited properties 
     */
    public Map<String, CmsComputedPropertyValue> getInheritedProperties() {

        return Collections.unmodifiableMap(m_currentProperties);
    }

    /**
     * Creates a new property inheritance state by updating the properties of the current state with a new set of 
     * properties.<p>
     * 
     * @param ownProps the properties which should be used to update the property state
     *  
     * @param location the location from which ownProps originates
     * 
     * @return the updated property state  
     */
    public CmsPropertyInheritanceState update(Map<String, CmsSimplePropertyValue> ownProps, String location) {

        Map<String, CmsComputedPropertyValue> ownComputedProps = createComputedPropertyMap(ownProps, location);
        Map<String, CmsComputedPropertyValue> resultProps = combinePropertyMaps(m_currentProperties, ownComputedProps);
        return new CmsPropertyInheritanceState(resultProps, m_propertyConfig);
    }

    /**
     * Creates a new property inheritance state by updating the property definitions of this state with a new set of property 
     * definitions.<p>
     * 
     * @param propDefs the property definitions with which the state should be updated
     *  
     * @return the new property inheritance state 
     */
    public CmsPropertyInheritanceState update(Map<String, CmsXmlContentProperty> propDefs) {

        Map<String, CmsComputedPropertyValue> defaults = getComputedPropertyValuesFromPropertyDefinitions(propDefs);
        Map<String, CmsComputedPropertyValue> result = combinePropertyMaps(m_currentProperties, defaults);
        return new CmsPropertyInheritanceState(result, propDefs);
    }

    /** 
     * Combines a parent map and a child map of property values for inheritance.<p>
     * 
     * @param parent the map of properties of the parent 
     * @param child the map of properties of the child 
     * 
     * @return the combined property map 
     */
    protected Map<String, CmsComputedPropertyValue> combinePropertyMaps(
        Map<String, CmsComputedPropertyValue> parent,
        Map<String, CmsComputedPropertyValue> child) {

        Map<String, CmsComputedPropertyValue> result = new HashMap<String, CmsComputedPropertyValue>();
        Set<String> keys = new HashSet<String>();
        keys.addAll(parent.keySet());
        keys.addAll(child.keySet());
        for (String propName : keys) {
            CmsComputedPropertyValue childProp = child.get(propName);
            if (shouldInherit(propName)) {
                CmsComputedPropertyValue parentValue = parent.get(propName);
                CmsComputedPropertyValue combinedValue = combineParentAndChildValues(parentValue, childProp);
                if (combinedValue != null) {
                    result.put(propName, combinedValue);
                }
            } else {
                if (childProp != null) {
                    result.put(propName, childProp);
                }
            }
        }
        return result;
    }

    /**
     * Creates a map of {@link CmsComputedPropertyValue} objects from a map of {@link CmsSimplePropertyValue} objects.<p>
     * 
     * @param simpleProps a map from strings to simple property values 
     * 
     * @param location the location to put into the {@link CmsComputedPropertyValue} objects
     * 
     * @return a map of computed property values
     */
    protected Map<String, CmsComputedPropertyValue> createComputedPropertyMap(
        Map<String, CmsSimplePropertyValue> simpleProps,
        String location) {

        Map<String, CmsComputedPropertyValue> result = new HashMap<String, CmsComputedPropertyValue>();
        for (Map.Entry<String, CmsSimplePropertyValue> entry : simpleProps.entrySet()) {
            String propName = entry.getKey();
            CmsSimplePropertyValue simpleValue = entry.getValue();
            result.put(propName, CmsComputedPropertyValue.create(simpleValue, location));
        }
        return result;
    }

    /**
     * Helper method which decides whether a given property should be inherited or not.<p>
     * 
     * @param propName the name of the property
     *  
     * @return true if the property should be inherited 
     */
    protected boolean shouldInherit(String propName) {

        CmsXmlContentProperty propConf = m_propertyConfig.get(propName);
        if (propConf == null) {
            return false;
        }
        String selectInherit = propConf.getSelectInherit();
        if (selectInherit == null) {
            return true;
        }
        if (propConf.getSelectInherit().equalsIgnoreCase("false")) {
            return false;
        }
        return true;
    }

}
