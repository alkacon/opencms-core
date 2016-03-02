/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean representing an entry in the quick launch menu.<p>
 */
public class CmsQuickLaunchData implements IsSerializable {

    /** Default target (may be null). */
    private String m_defaultTarget;

    /** Default URL (may be null). */
    private String m_defaultUrl;

    /** The icon URL. */
    private String m_iconUrl;

    /** True if this is a legacy tool (necessary because the icons for legacy tools are smaller). */
    private boolean m_legacy;

    /** Internal name of the quick launch item. */
    private String m_name;

    /** User readable title of the quick launch item. */
    private String m_title;

    /**
     * Creates a new instance.<p>
     *
     * @param name the internal name
     * @param defaultUrl the default URL
     * @param defaultTarget the default target
     * @param title the title
     * @param iconUrl the icon URL
     * @param legacy true if this is a legacy dialog
     */
    public CmsQuickLaunchData(
        String name,
        String defaultUrl,
        String defaultTarget,
        String title,
        String iconUrl,
        boolean legacy) {
        super();
        m_name = name;
        m_title = title;
        m_defaultUrl = defaultUrl;
        m_defaultTarget = defaultTarget;
        m_iconUrl = iconUrl;
        m_legacy = legacy;

    }

    /**
     * Default constructor for serialization.<p>
     */
    protected CmsQuickLaunchData() {
        // Default constructor for serialization
    }

    /**
     * Returns the defaultTarget.<p>
     *
     * @return the defaultTarget
     */
    public String getDefaultTarget() {

        return m_defaultTarget;
    }

    /**
     * Returns the defaultUrl.<p>
     *
     * @return the defaultUrl
     */
    public String getDefaultUrl() {

        return m_defaultUrl;
    }

    /**
     * Returns the iconUrl.<p>
     *
     * @return the iconUrl
     */
    public String getIconUrl() {

        return m_iconUrl;
    }

    /**
     * Returns the name.<p>
     *
     * @return the name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns true if this item opens a legacy dialog.<p>
     *
     * @return true if this item opens a legacy dialog
     */
    public boolean isLegacy() {

        return m_legacy;
    }

}
