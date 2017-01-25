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

import org.opencms.db.CmsResourceState;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean holding all info to be displayed in {@link org.opencms.gwt.client.ui.CmsListItemWidget}s.<p>
 *
 * @see org.opencms.gwt.client.ui.CmsListItemWidget
 *
 * @since 8.0.0
 */
public class CmsListInfoBean implements IsSerializable {

    /** Lock icons. */
    public enum LockIcon {
        /** Closed lock. */
        CLOSED,
        /** No lock. */
        NONE,
        /** Open lock. */
        OPEN,
        /** Shared closed lock. */
        SHARED_CLOSED,
        /** Shared open lock. */
        SHARED_OPEN
    }

    /**
     * Enum for the type of page icon which should be displayed.<p>
     */
    public enum StateIcon {
        /** copy page icon. */
        copy,
        /** export page icon. */
        export,
        /** secure page icon. */
        secure,
        /** standard page icon. */
        standard
    }

    /** CSS class for multi-line additional info's. */
    public static final String CSS_CLASS_MULTI_LINE = "multiLineLabel";

    /** The additional info. */
    private List<CmsAdditionalInfoBean> m_additionalInfo;

    /** The detail resource type. */
    private String m_detailResourceType;

    /** Flag which indicates whether this was generated for a folder. */
    private Boolean m_isFolder;

    /** The lock icon. */
    private LockIcon m_lockIcon;

    /** The lock icon title. */
    private String m_lockIconTitle;

    /** Flag to control whether a resource state of 'changed' should be visualized with an overlay icon. */
    private boolean m_markChangedState = true;

    /** The resource state. */
    private CmsResourceState m_resourceState;

    /** The resource type name of the resource. */
    private String m_resourceType;

    /** The state icon information: for the type of state icon which should be displayed. The state icon indicates if a resource is exported, secure etc. */
    private StateIcon m_stateIcon;

    /** The sub-title. */
    private String m_subTitle;

    /** The title. */
    private String m_title;

    /**
     * Default constructor.<p>
     */
    public CmsListInfoBean() {

        // empty
    }

    /**
     * Constructor.<p>
     *
     * @param title the title
     * @param subtitle the subtitle
     * @param additionalInfo the additional info
     */
    public CmsListInfoBean(String title, String subtitle, List<CmsAdditionalInfoBean> additionalInfo) {

        m_title = title;
        m_subTitle = subtitle;
        m_additionalInfo = additionalInfo;
    }

    /**
     * Sets a new additional info.<p>
     *
     * @param name the additional info name
     * @param value the additional info value
     */
    public void addAdditionalInfo(String name, String value) {

        getAdditionalInfo().add(new CmsAdditionalInfoBean(name, value, null));
    }

    /**
     * Sets a new additional info.<p>
     *
     * @param name the additional info name
     * @param value the additional info value
     * @param style the CSS style to apply to the info
     */
    public void addAdditionalInfo(String name, String value, String style) {

        getAdditionalInfo().add(new CmsAdditionalInfoBean(name, value, style));
    }

    /**
     * Returns the additional info.<p>
     *
     * @return the additional info
     */
    public List<CmsAdditionalInfoBean> getAdditionalInfo() {

        if (m_additionalInfo == null) {
            m_additionalInfo = new ArrayList<CmsAdditionalInfoBean>();
        }
        return m_additionalInfo;
    }

    /**
     * Returns the detail resource type.<p>
     *
     * @return the detail resource type
     */
    public String getDetailResourceType() {

        return m_detailResourceType;
    }

    /**
     * Returns a flag which indicates whether this info bean was generated for a folder.<p>
     *
     * This may not be set (i.e. null).
     *
     * @return a Boolean indicating whether this bean was generated for a folder
     */
    public Boolean getIsFolder() {

        return m_isFolder;
    }

    /**
     * Returns the lock icon.<p>
     *
     * @return the lockIcon
     */
    public LockIcon getLockIcon() {

        return m_lockIcon;
    }

    /**
     * Returns the lock icon title.<p>
     *
     * @return the lock icon title
     */
    public String getLockIconTitle() {

        return m_lockIconTitle;
    }

    /**
     * Returns the resourceState.<p>
     *
     * @return the resourceState
     */
    public CmsResourceState getResourceState() {

        return m_resourceState;
    }

    /**
     * Returns the resource type name.<p>
     *
     * @return the resource type name
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the state icon.<p>
     *
     * The state icon indicates if a resource is exported, secure etc.<p>
     *
     * @return the state Icon
     */
    public StateIcon getStateIcon() {

        return m_stateIcon;
    }

    /**
     * Returns the sub-title.<p>
     *
     * @return the sub-title
     */
    public String getSubTitle() {

        return m_subTitle;
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
     * Returns if the bean has additional info elements.<p>
     *
     * @return <code>true</code> if the bean has additional info elements
     */
    public boolean hasAdditionalInfo() {

        return (m_additionalInfo != null) && (m_additionalInfo.size() > 0);
    }

    /**
     * Returns true if the 'changed' resource state should be marked by an icon.<p>
     *
     * @return true if the 'changed' resource state should be marked by an icon.<p>
     */
    public boolean isMarkChangedState() {

        return m_markChangedState;
    }

    /**
     * Sets the additional info.<p>
     *
     * @param additionalInfo the additional info to set
     */
    public void setAdditionalInfo(List<CmsAdditionalInfoBean> additionalInfo) {

        m_additionalInfo = additionalInfo;
    }

    /**
     * Sets the detail resource type.<p>
     *
     * @param detailResourceType the detail resource type to set
     */
    public void setDetailResourceType(String detailResourceType) {

        m_detailResourceType = detailResourceType;
    }

    /**
     * Sets thE 'isFolder' flag.<p>
     *
     * @param isFolder the new value
     */
    public void setIsFolder(Boolean isFolder) {

        m_isFolder = isFolder;
    }

    /**
     * Sets the lock icon.<p>
     *
     * @param lockIcon the lock icon to set
     */
    public void setLockIcon(LockIcon lockIcon) {

        m_lockIcon = lockIcon;
    }

    /**
     * Sets the lock icon title.<p>
     *
     * @param lockIconTitle the lock icon title to set
     */
    public void setLockIconTitle(String lockIconTitle) {

        m_lockIconTitle = lockIconTitle;
    }

    /**
     * Enables or disables the display of the 'changed' icon for the 'changed' resource state.<p>
     *
     * @param markChanged true if the 'changed' state should be displayed
     */
    public void setMarkChangedState(boolean markChanged) {

        m_markChangedState = markChanged;
    }

    /**
     * Sets the resourceState.<p>
     *
     * @param resourceState the resourceState to set
     */
    public void setResourceState(CmsResourceState resourceState) {

        m_resourceState = resourceState;
    }

    /**
     * Sets the resource type name.<p>
     *
     * @param resourceType the resource type name to set
     */
    public void setResourceType(String resourceType) {

        m_resourceType = resourceType;
    }

    /**
     * Sets the state icon.<p>
     *
     * The state icon indicates if a resource is exported, secure etc.<p>
     *
     * @param stateIcon the state icon to set
     */
    public void setStateIcon(StateIcon stateIcon) {

        m_stateIcon = stateIcon;
    }

    /**
     * Sets the sub-title.<p>
     *
     * @param subTitle the sub-title to set
     */
    public void setSubTitle(String subTitle) {

        m_subTitle = subTitle;
    }

    /**
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }
}
