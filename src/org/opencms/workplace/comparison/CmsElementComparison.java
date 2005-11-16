/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/comparison/CmsElementComparison.java,v $
 * Date   : $Date: 2005/11/16 12:12:55 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.comparison;

/**
 * Comparison of two xml page elements.<p>
 * 
 * @author Jan Baudisch
 */
public class CmsElementComparison extends CmsAttributeComparison {

    /** The element locale.<p> */
    private String m_locale;

    /** 
     * Creates a new element comparison.<p> 
     * 
     * @param locale the locale of the comparison
     * @param name the name of the element
     */
    public CmsElementComparison(String locale, String name) {

        m_locale = locale;
        setName(name);
    }

    /**
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {

        if (!(o instanceof CmsElementComparison)) {
            return false;
        }
        CmsElementComparison diffItem = (CmsElementComparison)o;
        return getName().equals(diffItem.getName()) && m_locale.equals(diffItem.getLocale());
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public String getLocale() {

        return m_locale;
    }

    /**
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return m_locale.hashCode() + getName().hashCode();
    }

    /**
     * Sets the locale.<p>
     *
     * @param locale the locale to set
     */
    public void setLocale(String locale) {

        m_locale = locale;
    }

}
