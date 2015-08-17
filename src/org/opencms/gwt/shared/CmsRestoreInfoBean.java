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

package org.opencms.gwt.shared;

import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The bean with the necessary information for the "Restore" dialog.<p>
 */
public class CmsRestoreInfoBean implements IsSerializable {

    /** A flag which indicates whether a move operation can be undone. */
    private boolean m_canUndoMove;

    /** The bean containing the data for the list item widget. */
    private CmsListInfoBean m_listInfoBean;

    /** The offline modification date. */
    private String m_offlineDate;

    /** The offline root path of the resource. */
    private String m_offlinePath;

    /** The online modification date. */
    private String m_onlineDate;

    /** The online root path of the resource. */
    private String m_onlinePath;

    /** The structure id of the resource. */
    private CmsUUID m_structureId;

    /**
     * Creates a new instance.<p>
     */
    public CmsRestoreInfoBean() {

        // empty
    }

    /**
     * Returns true if the move operation can be undone.<p>
     *
     * @return true if the move operation can be undone
     */
    public boolean canUndoMove() {

        return m_canUndoMove;
    }

    /**
     * Gets the bean containing the information for the file info box.<p>
     *
     * @return the bean with the information for the file info box
     */
    public CmsListInfoBean getListInfoBean() {

        return m_listInfoBean;
    }

    /**
     * Gets the offline modification date.<p>
     *
     * @return the offline modification date
     */
    public String getOfflineDate() {

        return m_offlineDate;
    }

    /**
     * Gets the offline root path of the resource.<p>
     *
     * @return the offline path of the resource
     */
    public String getOfflinePath() {

        return m_offlinePath;
    }

    /**
     * Gets the online modification date.<p>
     *
     * @return the online modification date
     */
    public String getOnlineDate() {

        return m_onlineDate;
    }

    /**
     * Gets the online root path of the resource.<p>
     *
     * @return the online path of the resource
     */
    public String getOnlinePath() {

        return m_onlinePath;
    }

    /**
     * Gets the structure id of the resource for which changes should be undone.<p>
     *
     * @return the structure id of the resource which changes should be undone
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Returns true if the resource was moved.<p>
     *
     * @return true if the resource was moved
     */
    public boolean isMoved() {

        return !m_offlinePath.equals(m_onlinePath);
    }

    /**
     * Sets the 'canUndoMove' property.<p>
     *
     * @param canUndoMove the new value for the 'canUndoMove' property
     */
    public void setCanUndoMove(boolean canUndoMove) {

        m_canUndoMove = canUndoMove;
    }

    /**
     * Sets the list info bean for the resource.<p>
     *
     * @param listInfoBean the list info bean for the resource
     */
    public void setListInfoBean(CmsListInfoBean listInfoBean) {

        m_listInfoBean = listInfoBean;
    }

    /**
     * Sets the offline modification date.<p>
     *
     * @param offlineDate the offline modification date
     */
    public void setOfflineDate(String offlineDate) {

        m_offlineDate = offlineDate;
    }

    /**
     * Sets the offline root path.<p>
     *
     * @param offlinePath the offline path
     */
    public void setOfflinePath(String offlinePath) {

        m_offlinePath = offlinePath;
    }

    /**
     * Sets the online modification date.<p>
     *
     * @param onlineDate the online modification date
     */
    public void setOnlineDate(String onlineDate) {

        m_onlineDate = onlineDate;
    }

    /**
     * Sets the online root path.<p>
     *
     * @param onlinePath the online root path
     */
    public void setOnlinePath(String onlinePath) {

        m_onlinePath = onlinePath;
    }

    /**
     * Sets the structure id of the resource.<p>
     *
     * @param structureId the structure id to set
     */
    public void setStructureId(CmsUUID structureId) {

        m_structureId = structureId;
    }

}
