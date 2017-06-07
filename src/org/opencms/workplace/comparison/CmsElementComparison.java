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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import java.util.Locale;

/**
 * Comparison of two xml page elements.<p>
 */
public class CmsElementComparison extends CmsAttributeComparison implements Comparable<CmsElementComparison> {

    /** The element locale.<p> */
    private Locale m_locale;

    /**
     * Creates a new element comparison.<p>
     *
     * @param locale the locale of the comparison
     * @param name the name of the element
     */
    public CmsElementComparison(Locale locale, String name) {

        m_locale = locale;
        setName(name);
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(CmsElementComparison diffItem) {

        if (this == diffItem) {
            return 0;
        }

        // first compare by name
        if (getName().compareTo(diffItem.getName()) != 0) {
            return getName().compareTo(diffItem.getName());
        }
        // then by locale
        return m_locale.toString().compareTo(diffItem.getLocale().toString());
    }

    /**
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
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
    public Locale getLocale() {

        return m_locale;
    }

    /**
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_locale.hashCode() + getName().hashCode();
    }

    /**
     * Sets the locale.<p>
     *
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

}
