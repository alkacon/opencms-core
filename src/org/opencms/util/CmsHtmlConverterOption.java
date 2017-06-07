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

package org.opencms.util;

/**
 * Represents a single HTML converter configuration as defined in the OpenCms configuration file <code>opencms-vfs.xml</code>.<p>
 *
 * This is only used to write back the definitions to the configuration file.<p>
 */
public class CmsHtmlConverterOption {

    /** The class used for HTML conversion of the configured option. */
    private String m_className;

    /** Flag indicating if this is an automatically generated default option. */
    private boolean m_default;

    /** The name of the configured option. */
    private String m_name;

    /**
     * Constructor, with parameters.<p>
     *
     * @param name the name of the configured option
     * @param className the class used for HTML conversion of the configured option
     */
    public CmsHtmlConverterOption(String name, String className) {

        this(name, className, false);
    }

    /**
     * Constructor, with parameters.<p>
     *
     * @param name the name of the configured option
     * @param className the class used for HTML conversion of the configured option
     * @param isDefault the flag indicating if this is an automatically generated default option
     */
    public CmsHtmlConverterOption(String name, String className, boolean isDefault) {

        m_name = name;
        m_className = className;
        m_default = isDefault;
    }

    /**
     * Returns the class used for HTML conversion of the configured option.<p>
     *
     * @return the class used for HTML conversion of the configured option
     */
    public String getClassName() {

        return m_className;
    }

    /**
     * Returns the name of the configured option.<p>
     *
     * @return the name of the configured option
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns if the option is an automatically generated default option.<p>
     *
     * @return <code>true</code> if the option is an automatically generated default option, otherwise <code>false</code>
     */
    public boolean isDefault() {

        return m_default;
    }

}
