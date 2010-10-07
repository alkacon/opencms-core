/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/properties/Attic/CmsSimplePropertyValue.java,v $
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
 * This class represents a value of a sitemap property.<p>
 * 
 * It actually contains two values; one for the sitemap entry on which the property is set, 
 * and another which will be inherited by sub-entries of the sitemap entry.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsSimplePropertyValue implements Serializable {

    /** The  serial version UUID. */
    private static final long serialVersionUID = -3877602794262512650L;

    /** The value which will be inherited by sub-entries. */
    private String m_inheritValue;

    /** The property value for the sitemap entry. */
    private String m_ownValue;

    /** 
     * Creates a new property value. 
     * 
     * @param ownValue the property value for the sitemap entry 
     * @param inheritValue the property value which will be inherited by sub-entries 
     */
    public CmsSimplePropertyValue(String ownValue, String inheritValue) {

        super();
        m_ownValue = ownValue;
        m_inheritValue = inheritValue;
    }

    /**
     * No-args constructor for serialization.<p>
     */
    protected CmsSimplePropertyValue() {

        // do nothing 
    }

    /**
     * Returns the property value which will be inherited by sub-entries of the entry on which this property value is set.<p>
     *  
     * @return the property value which will be inherited by sub-entries  
     */
    public String getInheritValue() {

        return m_inheritValue;
    }

    /**
     * Returns the own property value for an entry.<p>
     * 
     * @return the entry's own property value 
     */
    public String getOwnValue() {

        return m_ownValue;
    }

}
