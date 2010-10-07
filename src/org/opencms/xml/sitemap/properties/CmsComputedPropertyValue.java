/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/properties/Attic/CmsComputedPropertyValue.java,v $
 * Date   : $Date: 2010/10/07 07:56:34 $
 * Version: $Revision: 1.1 $
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

import java.io.Serializable;

/**
 * This class represents a property value for a sitemap entry which has been computed by inheritance.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsComputedPropertyValue implements Serializable {

    /** version id for serialization. */
    private static final long serialVersionUID = -5515455604250259569L;

    /** The value which will be inherited by sub-entries. */
    private CmsSourcedValue m_inheritValue;

    /** The value which applies to the current sitemap entry. */
    private CmsSourcedValue m_ownValue;

    /** 
     * Creates a computed property value.<p>
     * 
     * @param own the current entry's own value  
     * @param inherit the value which should be inherited by sub-values  
     */
    public CmsComputedPropertyValue(CmsSourcedValue own, CmsSourcedValue inherit) {

        m_ownValue = own;
        m_inheritValue = inherit;
    }

    /**
     * Hidden default constructor for serialization.<p>
     */
    protected CmsComputedPropertyValue() {

        // do nothing
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
        parentValue = normalize(parentValue);
        childValue = normalize(childValue);

        // computed both components of the result 
        CmsSourcedValue newOwn = CmsSourcedValue.overrideValue(
            parentValue.getInheritSourcedValue(),
            childValue.getOwnSourcedValue());
        CmsSourcedValue newInherit = CmsSourcedValue.overrideValue(
            parentValue.getInheritSourcedValue(),
            childValue.getInheritSourcedValue());

        if ((newOwn == null) && (newInherit == null)) {
            return null;
        }
        CmsComputedPropertyValue result = new CmsComputedPropertyValue(newOwn, newInherit);
        return result;
    }

    /**
     * Helper method for creating a computed property value from a simple property value and a source.<p>
     *  
     * @param propValue the property value 
     * @param source the source of the simple property value 
     * 
     * @return a computed property value 
     */
    public static CmsComputedPropertyValue create(CmsSimplePropertyValue propValue, String source) {

        if (propValue == null) {
            return new CmsComputedPropertyValue(null, null);
        }
        return create(propValue.getOwnValue(), propValue.getInheritValue(), source);
    }

    /**
     * Creates a new computed property value with the same source for both the "own" and "inherit" values.<p>
     * 
     * @param own the value for the entry itself 
     * @param inherit the value which should be inherited by children of the entry 
     * 
     * @param source the source for both "own" and "inherit"
     *  
     * @return a new computed property value 
     */
    public static CmsComputedPropertyValue create(String own, String inherit, String source) {

        CmsSourcedValue ownObj = new CmsSourcedValue(own, source);
        CmsSourcedValue inheritObj = new CmsSourcedValue(inherit, source);
        return new CmsComputedPropertyValue(ownObj, inheritObj);
    }

    /**
     * Helper method which converts a null to a computed property value containing nulls.<p>
     * 
     * The method will return the parameter itself if it isn't null.<p>
     * 
     * @param value a computed property value or null 
     * 
     * @return the original value, or a computed property value containing nulls if the original value was null 
     */
    public static CmsComputedPropertyValue normalize(CmsComputedPropertyValue value) {

        if (value == null) {
            return new CmsComputedPropertyValue(null, null);
        }
        return value;
    }

    /**
     * Returns the value which should be inherited by children of the entry as a sourced value.<p>
     * 
     * @return a sourced value which should apply to children of this entry 
     */
    public CmsSourcedValue getInheritSourcedValue() {

        return m_inheritValue;

    }

    /**
     * Returns the value which will be inherited by child sitemap entries.<p>
     * 
     * @return the value which will be inherited by child sitemap entries  
     */
    public String getInheritValue() {

        return m_inheritValue == null ? null : m_inheritValue.getValue();
    }

    /**
     * Returns the sourced property value which applies to the current sitemap entry.<p>
     *  
     * @return the sourced property value for the current sitemap entry 
     */
    public CmsSourcedValue getOwnSourcedValue() {

        return m_ownValue;
    }

    /**
     * Returns the value which should apply to the entry itself.<p>
     * 
     * @return the value which should apply to the entry itself 
     */
    public String getOwnValue() {

        return m_ownValue == null ? null : m_ownValue.getValue();
    }

    /**
     * Returns the source of this computed property's own value.<p>
     *  
     * @return the source of this property's own value 
     */
    public String getSource() {

        return m_ownValue == null ? null : m_ownValue.getSource();
    }

}
