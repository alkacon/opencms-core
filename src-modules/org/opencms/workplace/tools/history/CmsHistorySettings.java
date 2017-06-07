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

package org.opencms.workplace.tools.history;

import org.opencms.main.OpenCms;

/**
 * Bean to store the entries made by the user in the History Settings form in the
 * administration view.<p>
 *
 * @since 6.9.1
 */
public class CmsHistorySettings {

    /** Constant for the deleted resources history mode: disabled. */
    public static final int MODE_DELETED_HISTORY_DISABLED = 0;

    /** Constant for the deleted resources history mode: keep without versions. */
    public static final int MODE_DELETED_HISTORY_KEEP_NO_VERSIONS = 1;

    /** Constant for the deleted resources history mode: keep with versions. */
    public static final int MODE_DELETED_HISTORY_KEEP_WITH_VERSIONS = 2;

    /** The mode how the deleted resource history is kept. */
    private int m_mode;

    /** Number of versions to keep. */
    private int m_versions;

    /**
     * Default constructor initializing values.<p>
     */
    public CmsHistorySettings() {

        m_versions = OpenCms.getSystemInfo().getHistoryVersions();

        int versionsDeleted = OpenCms.getSystemInfo().getHistoryVersionsAfterDeletion();
        if (versionsDeleted == 0) {
            m_mode = MODE_DELETED_HISTORY_DISABLED;
        } else if (versionsDeleted == 1) {
            m_mode = MODE_DELETED_HISTORY_KEEP_NO_VERSIONS;
        } else if ((versionsDeleted > 1) || (versionsDeleted == -1)) {
            m_mode = MODE_DELETED_HISTORY_KEEP_WITH_VERSIONS;
        } else {
            m_mode = MODE_DELETED_HISTORY_DISABLED;
        }
    }

    /**
     * Returns the mode how the deleted resource history is kept.<p>
     *
     * @return the mode how the deleted resource history is kept
     */
    public int getMode() {

        return m_mode;
    }

    /**
     * Returns the number of versions to keep.<p>
     *
     * @return the number of versions to keep
     */
    public int getVersions() {

        return m_versions;
    }

    /**
     * Sets the how the deleted resource history is kept.<p>
     *
     * @param mode the mode how the deleted resource history is kept
     */
    public void setMode(int mode) {

        m_mode = mode;
    }

    /**
     * Sets the number of versions to keep.<p>
     *
     * @param versions the number of versions to keep
     */
    public void setVersions(int versions) {

        m_versions = versions;
    }
}
