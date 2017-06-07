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

package org.opencms.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean representing an entry in the quick launch menu.<p>
 */
public class CmsQuickLaunchData implements IsSerializable {

    /** Default URL (may be null). */
    private String m_defaultUrl;

    /** Content for error alert box. */
    private String m_errorMessage;

    /** Title for error alert box. */
    private String m_errorTitle;

    /** The icon URL. */
    private String m_iconUrl;

    /** True if this is a legacy tool (necessary because the icons for legacy tools are smaller). */
    private boolean m_legacy;

    /** Flag to force page reload. */
    private boolean m_reload;

    /** User readable title of the quick launch item. */
    private String m_title;

    /**
     * Creates a new instance.<p>
     *
     * @param defaultUrl the default URL
     * @param title the title
     * @param iconUrl the icon URL
     * @param errorTitle the title for the error alert box
     * @param errorMessage the content for the error alert box
     * @param legacy true if this is a legacy dialog
     * @param reload true if the page should just be reloaded when this is selected
     */
    public CmsQuickLaunchData(

        String defaultUrl,
        String title,
        String iconUrl,
        String errorTitle,
        String errorMessage,
        boolean legacy,
        boolean reload) {

        m_title = title;
        m_defaultUrl = defaultUrl;
        m_iconUrl = iconUrl;
        m_legacy = legacy;
        m_reload = reload;
        m_errorTitle = errorTitle;
        m_errorMessage = errorMessage;

    }

    /**
     * Default constructor for serialization.<p>
     */
    protected CmsQuickLaunchData() {
        // Default constructor for serialization
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
     * Gets the error message.<p>
     *
     * @return the error message
     */
    public String getErrorMessage() {

        return m_errorMessage;
    }

    /**
     * Gets the title for the error alert box.<p>
     *
     * @return the title for the error alert box
     */
    public String getErrorTitle() {

        return m_errorTitle;
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

    /**
     * Return true if the page should be reloaded when this is selected.<p>
     *
     * @return true if the page should be reloaded
     */
    public boolean isReload() {

        return m_reload;
    }

    /**
     * Sets the 'reload' flag.<p>
     *
     * @param reload the new value for the 'reload' flag
     */
    public void setReload(boolean reload) {

        m_reload = reload;
    }

}
