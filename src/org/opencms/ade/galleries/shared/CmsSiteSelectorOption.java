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

package org.opencms.ade.galleries.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean class which represents an option for the site selector in the gallery dialog.<p>
 */
public class CmsSiteSelectorOption implements IsSerializable {

    /** Enum for indicating the site type. */
    public enum Type {
        /** Current subsite. */
        currentSubsite, /** Root site. */
        root, /** Shared folder. */
        shared, /** Normally configured site. */
        site;
    }

    /** Flag which indicates whether this site is the current site. */
    private boolean m_isCurrentSite;

    /** The message to display for this site. */
    private String m_message;

    /** The site root of this site. */
    private String m_siteRoot;

    /** The type of site. */
    private Type m_type;

    /**
     * Default constructor.<p>
     */
    public CmsSiteSelectorOption() {

        // do nothing
    }

    /**
     * Creates a new site selector option.<p>
     *
     * @param type the site type
     * @param siteRoot the site root
     * @param isCurrentSite true if this is the current site
     * @param message the message to display for this site
     */
    public CmsSiteSelectorOption(Type type, String siteRoot, boolean isCurrentSite, String message) {

        m_type = type;
        m_siteRoot = siteRoot;
        m_isCurrentSite = isCurrentSite;
        m_message = message;
    }

    /**
     * Gets the message to display for this site.<p>
     *
     * @return the message to display for this site
     */
    public String getMessage() {

        return m_message;
    }

    /**
     * Gets the site root for the site.<p>
     *
     * @return the site root
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Gets the type of the site.<p>
     *
     * @return the type of the site
     */
    public Type getType() {

        return m_type;
    }

    /**
     * Returns true if this site is the current site.<p>
     *
     * @return true if this is the current site
     */
    public boolean isCurrentSite() {

        return m_isCurrentSite;
    }
}
