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
 * Bean to store the entries made by the user in the History Clear form in the
 * administration view.<p>
 *
 * @since 6.9.1
 */
public class CmsHistoryClear {

    /** Mode for clearing the versions of the deleted resources too. */
    private String m_clearDeletedMode;

    /** The date versions older than will be cleared. */
    private long m_clearOlderThan;

    /** Number of versions to keep. */
    private int m_keepVersions;

    /**
     * Default constructor initializing values.<p>
     */
    public CmsHistoryClear() {

        m_keepVersions = OpenCms.getSystemInfo().getHistoryVersions();
        m_clearDeletedMode = CmsHistoryClearDialog.MODE_CLEANDELETED_KEEP_RESTORE_VERSION;
    }

    /**
     * Returns the date versions older than will be cleared.<p>
     *
     * @return the date versions older than will be cleared
     */
    public long getClearOlderThan() {

        return m_clearOlderThan;
    }

    /**
     * Returns the number of versions to keep.<p>
     *
     * @return the number of versions to keep
     */
    public int getKeepVersions() {

        return m_keepVersions;
    }

    /**
     * Returns the clear deleted mode.<p>
     *
     * @return the clear deleted mode
     */
    public String getClearDeletedMode() {

        return m_clearDeletedMode;
    }

    /**
     * Sets the clear deleted mode.<p>
     *
     * @param clearDeletedMode the clear deleted mode
     */
    public void setClearDeletedMode(String clearDeletedMode) {

        m_clearDeletedMode = clearDeletedMode;
    }

    /**
     * Sets the date versions older than will be cleared.<p>
     *
     * @param clearOlderThan the date versions older than will be cleared
     */
    public void setClearOlderThan(long clearOlderThan) {

        m_clearOlderThan = clearOlderThan;
    }

    /**
     * Sets the number of versions to keep.<p>
     *
     * @param keepVersions the number of versions to keep
     */
    public void setKeepVersions(int keepVersions) {

        m_keepVersions = keepVersions;
    }
}
