/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/properties/Attic/CmsSourcedValue.java,v $
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

/**
 * This class represents a string value together with its "source", which is just another arbitary string.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsSourcedValue {

    /** The source of the value. */
    private String m_source;

    /** The actual value. */
    private String m_value;

    /**
     * Creates a new sourced value.<p>
     * 
     * @param value the actual value 
     * @param source the source of the value 
     */
    public CmsSourcedValue(String value, String source) {

        super();
        m_value = value;
        m_source = source;
    }

    /**
     * Returns the second of two values if it's not null, else the first value.<p>
     * 
     * @param a the first value 
     * @param b the second value 
     * 
     * @return b if it isn't null, else a 
     */
    public static CmsSourcedValue overrideValue(CmsSourcedValue a, CmsSourcedValue b) {

        if (b != null) {
            return b;
        }
        return a;
    }

    /**
     * Returns the source of the value.<p>
     * 
     * @return the source of the value 
     */
    public String getSource() {

        return m_source;
    }

    /**
     * Returns the actual value.<p>
     * 
     * @return the actual value 
     */
    public String getValue() {

        return m_value;
    }

}
