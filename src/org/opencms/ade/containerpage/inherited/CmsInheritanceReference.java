/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.inherited;

import org.opencms.file.CmsResource;

import java.util.Locale;

/**
 * Reference to an container inheritance configuration.<p>
 *
 */
public class CmsInheritanceReference {

    /** The description. */
    private String m_description;

    /** The locale. */
    private Locale m_locale;

    /** The container configuration name. */
    private String m_name;

    /** The resource from which this reference has been read. */
    private CmsResource m_resource;

    /** The title. */
    private String m_title;

    /**
     * Creates a new instance.<p>
     *
     * @param name the container configuration name
     * @param title the title
     * @param description the description
     * @param res the resource
     * @param locale the locale
     */
    public CmsInheritanceReference(String name, String title, String description, CmsResource res, Locale locale) {

        m_title = title;
        m_name = name;
        m_description = description;
        m_resource = res;
        m_locale = locale;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
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
     * Gets the configuration name.<p>
     *
     * @return the configuration name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Gets the reference resource.<p>
     *
     * @return the reference resource
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Gets the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

}
