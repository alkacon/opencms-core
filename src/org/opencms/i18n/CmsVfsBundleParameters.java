/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.i18n;

import java.util.Locale;

/**
 * Data class containing the parameters for a VFS-based resource bundle.<p>
 */
public class CmsVfsBundleParameters {

    /** The base path in the VFS. */
    private String m_basePath;

    /** True if this is the set of parameters for the default message bundle. */
    private boolean m_isDefault;

    /** The locale of the message bundle. */
    private Locale m_locale;

    /** The name of the message bundle. */
    private String m_name;

    /** A string constant indicating the type of the VFS bundle. */
    private String m_type;

    /**
     * 
     * @param name the name of the message bundle 
     * @param basePath the root base path of the message bundle 
     * @param locale the locale of the message bundle 
     * @param isDefault true if this is the set of parameters for the default locale 
     * @param type a string constant indicating the type of the resource bundle 
     */
    public CmsVfsBundleParameters(String name, String basePath, Locale locale, boolean isDefault, String type) {

        m_name = name;
        m_basePath = basePath;
        m_locale = locale;
        m_isDefault = isDefault;
        m_type = type;
    }

    /**
     * Gets the base path of the resource bundle.<p>
     * 
     * @return the base path of the resource bundle 
     */
    public String getBasePath() {

        return m_basePath;
    }

    /**
     * Gets the locale.<p>
     * 
     * @return the locale 
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Gets the name of the message bundle.<p>
     * 
     * @return the name of the message bundle 
     */
    public String getName() {

        return m_name;
    }

    /**
     * Gets the resource bundle type.<p>
     * 
     * @return the resource bundle type 
     */
    public String getType() {

        return m_type;
    }

    /**
     * Returns true if this is the set of parameters for the default message bundle.<p>
     * 
     * @return true if this is the set of parameters for the default message bundle 
     */
    public boolean isDefault() {

        return m_isDefault;
    }

}
