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

import org.opencms.util.CmsStringUtil;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean representing a file version for the history dialog.<p>
 *
 * Since the history dialog can also display offline resources, or resources read from the Online project without a historical version id,
 * this class is needed rather than a single integer to represent a version.<p>
 *
 */
public class CmsHistoryVersion implements IsSerializable {

    /**
     * Enum for distinguishing between offline and online project.<p>
     */
    public enum OfflineOnline {
        /** Offline project. */
        offline,
        /** Online project. */
        online;
    }

    /** We could use a nullable Boolean here, but use a custom enum for clarity. */
    private OfflineOnline m_offlineOnline;

    /** The version number. */
    private Integer m_versionNumber;

    /**
     * Creates a new instance.<p>
     *
     * @param versionNumber the version number
     * @param offlineOnline the offline/online state
     */
    public CmsHistoryVersion(Integer versionNumber, OfflineOnline offlineOnline) {

        m_offlineOnline = offlineOnline;
        m_versionNumber = versionNumber;
    }

    /**
     * Default constructor for serialization.<p>
     */
    protected CmsHistoryVersion() {

        // Do nothing

    }

    /**
     * Converts a string to a CmsHistoryVersion.<p>
     *
     * This is the inverse of toString().
     *
     * @param s the string from which to read the history version
     *
     * @return the history version
     */
    public static CmsHistoryVersion fromString(String s) {

        List<String> l = CmsStringUtil.splitAsList(s, ":");
        if (l.size() == 2) {

            Integer ver = null;
            try {
                ver = Integer.valueOf(l.get(0));
            } catch (Exception e) {
                //
            }
            OfflineOnline onlineStatus = "null".equals("" + l.get(1)) ? null : OfflineOnline.valueOf(l.get(1));
            return new CmsHistoryVersion(ver, onlineStatus);
        }
        return null;

    }

    /**
     * Gets the version number, or null if no version number was set.<p>
     *
     * @return the version number
     */
    public Integer getVersionNumber() {

        return m_versionNumber;
    }

    /**
     * Returns true if this is the offline version.<p>
     *
     * @return true if this is the offline version
     */
    public boolean isOffline() {

        return OfflineOnline.offline.equals(m_offlineOnline);
    }

    /**
     * Returns true if this is the online version.<p>
     *
     * @return true if this is the online version
     */
    public boolean isOnline() {

        return OfflineOnline.online.equals(m_offlineOnline);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return m_versionNumber + ":" + m_offlineOnline;
    }

}
