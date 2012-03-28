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

package org.opencms.file.collectors;

import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.Serializable;
import java.text.Collator;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Comparator for sorting resource objects based on a selected property value.<p>
 * 
 * Serves as {@link java.util.Comparator} for resources and as comparator key for the resource
 * at the same time. Uses lazy initializing of comparator keys in a resource.<p>
 *  
 * @since 8.0.4
 */
public class CmsPropertyResourceComparator implements Serializable, Comparator<CmsResource> {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -7943213182160054552L;

    /** The sort order. */
    private boolean m_asc;

    /** The current OpenCms user context. */
    private transient CmsObject m_cms;

    /** The internal map of comparator keys. */
    private Map<CmsUUID, CmsPropertyResourceComparator> m_keys;

    /** The property name of this comparator key. */
    private String m_property;

    /** The property value of this comparator key. */
    private String m_propertyValue;

    /**
     * Creates a new instance of this comparator key.<p>
     * 
     * @param cms the current OpenCms user context
     * @param property the name of the sort property (case sensitive)  
     * @param asc the sort order (true=asc, false=desc)
     */
    public CmsPropertyResourceComparator(CmsObject cms, String property, boolean asc) {

        m_property = property;
        m_asc = asc;
        m_cms = cms;
        m_keys = new HashMap<CmsUUID, CmsPropertyResourceComparator>();
    }

    /**
     * Creates a new instance of this comparator key.<p>
     * 
     * @param resource the resource to create the key for
     * @param cms the current OpenCms user context
     * @param property the name of the sort property (case sensitive)  
     * 
     * @return a new instance of this comparator key
     */
    private static CmsPropertyResourceComparator create(CmsResource resource, CmsObject cms, String property) {

        CmsPropertyResourceComparator result = new CmsPropertyResourceComparator(null, null, false);
        result.init(resource, cms, property);
        return result;
    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(CmsResource res0, CmsResource res1) {

        if (res0 == res1) {
            return 0;
        }

        CmsPropertyResourceComparator key0 = m_keys.get(res0.getStructureId());
        CmsPropertyResourceComparator key1 = m_keys.get(res1.getStructureId());

        if (key0 == null) {
            // initialize key if null
            key0 = CmsPropertyResourceComparator.create(res0, m_cms, m_property);
            m_keys.put(res0.getStructureId(), key0);
        }
        if (key1 == null) {
            // initialize key if null
            key1 = CmsPropertyResourceComparator.create(res1, m_cms, m_property);
            m_keys.put(res1.getStructureId(), key1);
        }

        // select a different sort methode for strings or number values                
        if (isNumeric(key0.getPropertyValue()) && isNumeric(key1.getPropertyValue())) {
            // double can do it all.... ;-)
            double dKey0 = Double.parseDouble(key0.getPropertyValue());
            double dKey1 = Double.parseDouble(key1.getPropertyValue());

            if (m_asc) {
                // sort in ascending order
                if (dKey0 > dKey1) {
                    return 1;
                }
                if (dKey0 < dKey1) {
                    return -1;
                }
            } else {
                // sort in descending order
                if (dKey0 > dKey1) {
                    return -1;
                }
                if (dKey0 < dKey1) {
                    return 1;
                }
            }

        } else {
            // sort by property value depending on the locale
            Collator collator = Collator.getInstance(m_cms.getRequestContext().getLocale());
            if (m_asc) {
                // sort in ascending order
                return collator.compare(key0.getPropertyValue(), key1.getPropertyValue());
            } else {
                // sort in descending order
                return collator.compare(key1.getPropertyValue(), key0.getPropertyValue());
            }
        }
        return 0;
    }

    /**
     * Returns the property value of this resource comparator key.<p>
     * 
     * @return property value of this resource comparator key
     */
    public String getPropertyValue() {

        if (!CmsStringUtil.isEmpty(m_propertyValue)) {
            return m_propertyValue.trim();
        } else {
            return "";
        }
    }

    /**
     * Initializes the comparator key based on the member variables.<p> 
     * 
     * @param resource the resource to use 
     * @param cms the current OpenCms user contxt
     * @param property the name of the sort property (case sensitive)  
     */
    private void init(CmsResource resource, CmsObject cms, String property) {

        try {
            cms.readPropertyDefinition(property);
            CmsProperty prop = cms.readPropertyObject(resource, property, false);

            if (prop == CmsProperty.getNullProperty()) {
                m_propertyValue = "";
            } else {
                m_propertyValue = prop.getValue();
            }
        } catch (CmsDbEntryNotFoundException dbe) {
            // property are not configured
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_COLLECTOR_PARAM_PROPERTY_NOT_FOUND_1,
                property));
        } catch (CmsException cmse) {
            // something's gone wrong...
            cmse.printStackTrace();
        }
    }

    /**
     * Check if a string contains a numeric value.<p> 
     * 
     * @param string to check if is a numeric value
     *
     * @return true for a numeric value in the string
     */
    private boolean isNumeric(String value) {

        if (!CmsStringUtil.isEmpty(value)) {
            NumberFormat formatter = NumberFormat.getInstance();
            ParsePosition pos = new ParsePosition(0);
            formatter.parse(value, pos);
            return value.length() == pos.getIndex();
        } else {
            return false;
        }
    }

}