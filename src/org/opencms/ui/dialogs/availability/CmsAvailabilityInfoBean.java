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

package org.opencms.ui.dialogs.availability;

import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsPrincipalBean;

import java.io.Serializable;
import java.util.Map;

/**
 * A bean that holds the informations of the availability dialog.<p>
 */
public class CmsAvailabilityInfoBean implements Serializable {

    /** The serial version id. */
    private static final long serialVersionUID = -3132436076946143159L;

    /** The default expiration date of a resource, which is: never expires. */
    private static final long DATE_EXPIRED_DEFAULT = Long.MAX_VALUE;

    /** The default release date of a resource, which is: always released. */
    private static final long DATE_PUBLISH_SCHEDULED_DEFAULT = 0;

    /** The default release date of a resource, which is: always released. */
    private static final long DATE_RELEASED_DEFAULT = 0;

    /** The expiration date. */
    private long m_dateExpired = DATE_EXPIRED_DEFAULT;

    /** The publish scheduled date. */
    private long m_datePubScheduled = DATE_PUBLISH_SCHEDULED_DEFAULT;

    /** The release date. */
    private long m_dateReleased = DATE_RELEASED_DEFAULT;

    /** Signals whether the current uri has siblings or not. */
    private boolean m_hasSiblings;

    /** Signals whether the siblings of the current uri should be modified or not. */
    private boolean m_modifySiblings;

    /** Signals whether the notification is enabled or not. */
    private boolean m_notificationEnabled;

    /** The notification interval. */
    private int m_notificationInterval;

    /** The page info for displaying the CmsListItemWidget. */
    private CmsListInfoBean m_pageInfo;

    /** A Map with all responsible users. */
    private Map<CmsPrincipalBean, String> m_responsibles;

    /** The current resource type. */
    private String m_resType;

    /** The vfs path. */
    private String m_vfsPath;

    /**
     * The default constructor.<p>
     */
    public CmsAvailabilityInfoBean() {

        // noop
    }

    /**
     * The public constructor.<p>
     *
     * @param resType the resource type
     * @param datePubScheduled the publish scheduled date
     * @param dateReleased the release date
     * @param dateExpired the expiration date
     * @param notificationInterval the notification interval
     * @param notificationEnabled the notification flag
     * @param hasSiblings the sibling flag
     * @param modifySiblings the modify sibling flag
     * @param responsibles the responsible users map
     * @param vfsPath the vfsPath for the resource
     * @param pageInfo the page info
     */
    public CmsAvailabilityInfoBean(
        String resType,
        long datePubScheduled,
        long dateReleased,
        long dateExpired,
        int notificationInterval,
        boolean notificationEnabled,
        boolean hasSiblings,
        boolean modifySiblings,
        Map<CmsPrincipalBean, String> responsibles,
        String vfsPath,
        CmsListInfoBean pageInfo) {

        m_resType = resType;
        m_datePubScheduled = datePubScheduled;
        m_dateReleased = dateReleased;
        m_dateExpired = dateExpired;
        m_notificationInterval = notificationInterval;
        m_notificationEnabled = notificationEnabled;
        m_hasSiblings = hasSiblings;
        m_modifySiblings = modifySiblings;
        m_responsibles = responsibles;
        m_vfsPath = vfsPath;
        m_pageInfo = pageInfo;
    }

    /**
     * Returns the dateExpired.<p>
     *
     * @return the dateExpired
     */
    public long getDateExpired() {

        return m_dateExpired;
    }

    /**
     * Returns the datePubScheduled.<p>
     *
     * @return the datePubScheduled
     */
    public long getDatePubScheduled() {

        return m_datePubScheduled;
    }

    /**
     * Returns the dateReleased.<p>
     *
     * @return the dateReleased
     */
    public long getDateReleased() {

        return m_dateReleased;
    }

    /**
     * Returns the notificationInterval.<p>
     *
     * @return the notificationInterval
     */
    public int getNotificationInterval() {

        return m_notificationInterval;
    }

    /**
     * Returns the pageInfo.<p>
     *
     * @return the pageInfo
     */
    public CmsListInfoBean getPageInfo() {

        return m_pageInfo;
    }

    /**
     * Returns the responsibles.<p>
     *
     * @return the responsibles
     */
    public Map<CmsPrincipalBean, String> getResponsibles() {

        return m_responsibles;
    }

    /**
     * Returns the resType.<p>
     *
     * @return the resType
     */
    public String getResType() {

        return m_resType;
    }

    /**
     * Returns the vfsPath.<p>
     *
     * @return the vfsPath
     */
    public String getVfsPath() {

        return m_vfsPath;
    }

    /**
     * Returns the hasSiblings.<p>
     *
     * @return the hasSiblings
     */
    public boolean isHasSiblings() {

        return m_hasSiblings;
    }

    /**
     * Returns the modifySiblings.<p>
     *
     * @return the modifySiblings
     */
    public boolean isModifySiblings() {

        return m_modifySiblings;
    }

    /**
     * Returns the notificationEnabled.<p>
     *
     * @return the notificationEnabled
     */
    public boolean isNotificationEnabled() {

        return m_notificationEnabled;
    }

    /**
     * Sets the dateExpired.<p>
     *
     * @param dateExpired the dateExpired to set
     */
    public void setDateExpired(long dateExpired) {

        m_dateExpired = dateExpired;
    }

    /**
     * Sets the datePubScheduled.<p>
     *
     * @param datePubScheduled the datePubScheduled to set
     */
    public void setDatePubScheduled(long datePubScheduled) {

        m_datePubScheduled = datePubScheduled;
    }

    /**
     * Sets the dateReleased.<p>
     *
     * @param dateReleased the dateReleased to set
     */
    public void setDateReleased(long dateReleased) {

        m_dateReleased = dateReleased;
    }

    /**
     * Sets the hasSiblings.<p>
     *
     * @param hasSiblings the hasSiblings to set
     */
    public void setHasSiblings(boolean hasSiblings) {

        m_hasSiblings = hasSiblings;
    }

    /**
     * Sets the modifySiblings.<p>
     *
     * @param modifySiblings the modifySiblings to set
     */
    public void setModifySiblings(boolean modifySiblings) {

        m_modifySiblings = modifySiblings;
    }

    /**
     * Sets the notificationEnabled.<p>
     *
     * @param notificationEnabled the notificationEnabled to set
     */
    public void setNotificationEnabled(boolean notificationEnabled) {

        m_notificationEnabled = notificationEnabled;
    }

    /**
     * Sets the notificationInterval.<p>
     *
     * @param notificationInterval the notificationInterval to set
     */
    public void setNotificationInterval(int notificationInterval) {

        m_notificationInterval = notificationInterval;
    }

    /**
     * Sets the pageInfo.<p>
     *
     * @param pageInfo the pageInfo to set
     */
    public void setPageInfo(CmsListInfoBean pageInfo) {

        m_pageInfo = pageInfo;
    }

    /**
     * Sets the responsibles.<p>
     *
     * @param responsibles the responsibles to set
     */
    public void setResponsibles(Map<CmsPrincipalBean, String> responsibles) {

        m_responsibles = responsibles;
    }

    /**
     * Sets the resType.<p>
     *
     * @param resType the resType to set
     */
    public void setResType(String resType) {

        m_resType = resType;
    }

    /**
     * Sets the vfsPath.<p>
     *
     * @param vfsPath the vfsPath to set
     */
    public void setVfsPath(String vfsPath) {

        m_vfsPath = vfsPath;
    }
}
