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

package org.opencms.search;

import org.opencms.i18n.CmsLocaleManager;

import java.util.Locale;

/**
 * An analyzer class is used by Lucene to reduce the content to be indexed
 * with trimmed endings etc.<p>
 *
 * @since 6.0.0
 */
public class CmsSearchAnalyzer {

    /** The class name of the analyzer. */
    private String m_className;

    /** A locale as a key to select the analyzer. */
    private Locale m_locale;

    /**
     * Returns the className.<p>
     *
     * @return the className
     */
    public String getClassName() {

        return m_className;
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
     * Returns the locale as a String.<p>
     *
     * @return the locale as a String
     *
     * @see #getLocale()
     */
    public String getLocaleString() {

        return getLocale().toString();
    }

    /**
     * Sets the class name.<p>
     *
     * @param className the class name
     */
    public void setClassName(String className) {

        m_className = className;
    }

    /**
     * Sets the locale.<p>
     *
     * @param locale the locale
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * Sets the locale as a String.<p>
     *
     * @param locale the locale
     *
     * @see #setLocale(Locale)
     */
    public void setLocaleString(String locale) {

        setLocale(CmsLocaleManager.getLocale(locale));
    }

}