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

package org.opencms.ade.sitemap.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Sitemap info object. Contains descriptive info about the current site/sub-site.<p>
 *
 * @since 8.0.2
 */
public class CmsSitemapInfo implements IsSerializable {

    /** The current project. */
    private String m_currentProject;

    /** Site description. */
    private String m_description;

    /** The site default locale. */
    private String m_siteLocale;

    /** The name of the current site. */
    private String m_siteHost;

    /** The site title. */
    private String m_title;

    /**
     * Constructor for serialization only.<p>
     */
    protected CmsSitemapInfo() {

    }

    /**
     * Constructor.<p>
     *
     * @param currentProject the current project
     * @param description the site description
     * @param siteLocale the site default locale
     * @param siteHost the site host
     * @param title the title
     */
    public CmsSitemapInfo(String currentProject, String description, String siteLocale, String siteHost, String title) {

        m_currentProject = currentProject;
        m_description = description;
        m_siteLocale = siteLocale;
        m_siteHost = siteHost;
        m_title = title;
    }

    /**
     * Returns the current project.<p>
     *
     * @return the current project
     */
    public String getCurrentProject() {

        return m_currentProject;
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
     * Returns the site default locale.<p>
     *
     * @return the site default locale
     */
    public String getSiteLocale() {

        return m_siteLocale;
    }

    /**
     * Returns the host of the current site.<p>
     *
     * @return the host of the current site
     */
    public String getSiteHost() {

        return m_siteHost;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }
}
