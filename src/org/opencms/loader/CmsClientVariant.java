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

package org.opencms.loader;

import org.opencms.i18n.I_CmsMessageContainer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Bean that represents a client variant of a template context.<p>
 */
public class CmsClientVariant {

    /** The nice name of the variant. */
    private I_CmsMessageContainer m_message;

    /** The internal name of the client variant. */
    private String m_name;

    /** An additional map of parameters for the variant. */
    private Map<String, String> m_parameters;

    /** The screen height. */
    private int m_screenHeight;

    /** The screen width. */
    private int m_screenWidth;

    /**
     * Creates a new instance.<p>
     *
     * @param name the internal name of the client variant
     * @param message the nice name of the variant
     * @param width the screen width
     * @param height the screen height
     * @param parameters parameters for the variant
     */
    public CmsClientVariant(
        String name,
        I_CmsMessageContainer message,
        int width,
        int height,
        Map<String, String> parameters) {

        m_name = name;
        m_screenWidth = width;
        m_screenHeight = height;
        m_message = message;
        m_parameters = new HashMap<String, String>(parameters);
    }

    /**
     * Gets the internal name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Gets the nice name for a locale.<p>
     *
     * @param locale the locale
     *
     * @return the nice name
     */
    public String getNiceName(Locale locale) {

        return m_message.key(locale);
    }

    /**
     * Gets the parameters.<p>
     *
     * @return the parameters
     */
    public Map<String, String> getParameters() {

        return m_parameters;
    }

    /**
     * Gets the screen height.<p>
     *
     * @return the screen height
     */
    public int getScreenHeight() {

        return m_screenHeight;
    }

    /**
     * Gets the screen width.<p>
     *
     * @return the screen width
     */
    public int getScreenWidth() {

        return m_screenWidth;
    }
}
