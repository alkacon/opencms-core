/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/properties/Attic/CmsSourcedValue.java,v $
 * Date   : $Date: 2010/10/11 06:40:55 $
 * Version: $Revision: 1.3 $
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

import org.opencms.util.CmsObjectUtil;

import java.io.Serializable;

/**
 * This class represents a string value together with its "source", which is just another arbitary string.<p>
 * 
 * Warning: This class is used by GWT client-side code (See GwtBase.gwt.xml for a list of
 * classes used by GWT client-side code). If you change this class, either make sure that 
 * your changes are compatible with GWT, or write a separate client version of the class 
 * and put it into super_src. 
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsSourcedValue implements Serializable {

    /** id for serialization. */
    private static final long serialVersionUID = 6967322591643126376L;

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
     * Hidden constructor for serialization.<p>
     *  
     */
    protected CmsSourcedValue() {

        // do nothing  
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
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {

        if ((o == null) || !(o instanceof CmsSourcedValue)) {
            return false;
        }
        CmsSourcedValue val = (CmsSourcedValue)o;
        return CmsObjectUtil.equals(m_source, val.m_source) && CmsObjectUtil.equals(m_value, val.m_value);
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

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return CmsObjectUtil.computeHashCode(m_value, m_source);
    }

}
