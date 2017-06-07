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

package org.opencms.ui.apps;

/**
 * The app visibility status.<p>
 */
public class CmsAppVisibilityStatus {

    /** The active and visible status. */
    public static final CmsAppVisibilityStatus ACTIVE = new CmsAppVisibilityStatus(true, true, null);

    /** The invisible status. */
    public static final CmsAppVisibilityStatus INVISIBLE = new CmsAppVisibilityStatus(false, false, null);

    /** The active flac. */
    private boolean m_active;

    /** The help text for visible but inactive apps. */
    private String m_helpText;

    /** The visibility flac. */
    private boolean m_visible;

    /**
     * Constructor.<p>
     *
     * @param visible if visible
     * @param active if active
     * @param helpText the help text for visible but inactive apps
     */
    public CmsAppVisibilityStatus(boolean visible, boolean active, String helpText) {

        m_visible = visible;
        m_active = active;
        m_helpText = helpText;
    }

    /**
     * Returns the help text for visible but inactive apps.<p>
     *
     * @return the help text for visible but inactive apps
     */
    public String getHelpText() {

        return m_helpText;
    }

    /**
     * Returns if the app is active.<p>
     *
     * @return if active
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * Returns if the app is visible.<p>
     *
     * @return if visible
     */
    public boolean isVisible() {

        return m_visible;
    }

}
