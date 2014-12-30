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

package org.opencms.ade.containerpage.shared;

import org.opencms.gwt.shared.CmsPermissionInfo;
import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean holding basic container element information.<p>
 * 
 * @since 8.0.0
 */
public class CmsContainerElement implements IsSerializable {

    /** HTML class used to identify containers. */
    public static final String CLASS_CONTAINER = "cms_ade_container";

    /** HTML class used to identify container elements. */
    public static final String CLASS_CONTAINER_ELEMENT_END_MARKER = "cms_ade_element_end";

    /** HTML class used to identify container elements. */
    public static final String CLASS_CONTAINER_ELEMENT_START_MARKER = "cms_ade_element_start";

    /** HTML class used to identify error message for elements where rendering failed to render. */
    public static final String CLASS_ELEMENT_ERROR = "cms_ade_element_error";

    /** HTML class used to identify group container elements. */
    public static final String CLASS_GROUP_CONTAINER_ELEMENT_MARKER = "cms_ade_groupcontainer";

    /** The group container resource type name. */
    public static final String GROUP_CONTAINER_TYPE_NAME = "groupcontainer";

    /** The resource type name for inherited container references.  */
    public static final String INHERIT_CONTAINER_TYPE_NAME = "inheritance_group";

    /** The element client id. */
    private String m_clientId;

    /** The element view this element belongs to by it's type. */
    private CmsUUID m_elementView;

    /** Flag to indicate that this element may have settings. */
    private boolean m_hasSettings;

    /** The inheritance info for this element. */
    private CmsInheritanceInfo m_inheritanceInfo;

    /** Flag indicating a new element. */
    private boolean m_new;

    /** Flag which controls whether the new editor is disabled for this element. */
    private boolean m_newEditorDisabled;

    /** Flag indicating if the given resource is released and not expired. */
    private boolean m_releasedAndNotExpired = true;

    /** The resource type for new elements. If this field is not empty, the element is regarded as new and not created yet. */
    private String m_resourceType;

    /** The full site path. */
    private String m_sitePath;

    /** The sub title. */
    private String m_subTitle;

    /** The title. */
    private String m_title;

    /** The permission info for the element resource. */
    private CmsPermissionInfo m_permissionInfo;

    /**
     * Default constructor.<p>
     */
    public CmsContainerElement() {

        // empty
    }

    /**
     * Copies the container element.<p>
     * 
     * @return the new copy of the container element
     */
    public CmsContainerElement copy() {

        CmsContainerElement result = new CmsContainerElement();
        result.m_clientId = m_clientId;
        result.m_hasSettings = m_hasSettings;
        result.m_inheritanceInfo = m_inheritanceInfo;
        result.m_new = m_new;
        result.m_newEditorDisabled = m_newEditorDisabled;
        result.m_permissionInfo = new CmsPermissionInfo(
            m_permissionInfo.hasViewPermission(),
            m_permissionInfo.hasWritePermission(),
            m_permissionInfo.getNoEditReason());
        result.m_releasedAndNotExpired = m_releasedAndNotExpired;
        result.m_resourceType = m_resourceType;
        result.m_sitePath = m_sitePath;
        result.m_subTitle = m_subTitle;
        result.m_title = m_title;
        result.m_elementView = m_elementView;
        return result;

    }

    /**
     * Returns the client id.<p>
     *
     * @return the client id
     */
    public String getClientId() {

        return m_clientId;
    }

    /**
     * Returns the element view this element belongs to by it's type.<p>
     *
     * @return the element view
     */
    public CmsUUID getElementView() {

        return m_elementView;
    }

    /**
     * Returns the inheritance info for this element.<p>
     *
     * @return the inheritance info for this element
     */
    public CmsInheritanceInfo getInheritanceInfo() {

        return m_inheritanceInfo;
    }

    /**
     * Returns the no edit reason. If empty editing is allowed.<p>
     *
     * @return the no edit reason
     */
    public String getNoEditReason() {

        return m_permissionInfo.getNoEditReason();
    }

    /**
     * Returns the resource type name for elements.<p>
     * 
     * @return the resource type name
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the site path.<p>
     *
     * @return the site path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the sub title.<p>
     * 
     * @return the sub title
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
     * Returns if the element may have settings.<p>
     *
     * @param containerId the container id
     * 
     * @return <code>true</code> if the element may have settings
     */
    public boolean hasSettings(String containerId) {

        return m_hasSettings;
    }

    /**
     * Returns if the current user has view permissions for the element resource.<p>
     *
     * @return <code>true</code> if the current user has view permissions for the element resource
     */
    public boolean hasViewPermission() {

        return m_permissionInfo.hasViewPermission();
    }

    /**
     * Returns if the user has write permission.<p>
     *
     * @return <code>true</code> if the user has write permission
     */
    public boolean hasWritePermission() {

        return m_permissionInfo.hasWritePermission();
    }

    /**
     * Returns if the given element is of the type group container.<p>
     * 
     * @return <code>true</code> if the given element is of the type group container
     */
    public boolean isGroupContainer() {

        return GROUP_CONTAINER_TYPE_NAME.equals(m_resourceType);
    }

    /**
     * Returns if the given element is of the type inherit container.<p>
     * 
     * @return <code>true</code> if the given element is of the type inherit container
     */
    public boolean isInheritContainer() {

        return INHERIT_CONTAINER_TYPE_NAME.equals(m_resourceType);
    }

    /**
     * Returns if the element is new and has not been created in the VFS yet.<p>
     * 
     * @return <code>true</code> if the element is not created in the VFS yet
     */
    public boolean isNew() {

        return m_new;
    }

    /**
     * Returns true if the new editor is disabled for this element.<p>
     * 
     * @return true if the new editor is disabled for this element 
     */
    public boolean isNewEditorDisabled() {

        return m_newEditorDisabled;
    }

    /**
     * Returns if the given resource is released and not expired.<p>
     *
     * @return <code>true</code> if the given resource is released and not expired
     */
    public boolean isReleasedAndNotExpired() {

        return m_releasedAndNotExpired;
    }

    /**
     * Sets the client id.<p>
     *
     * @param clientId the client id to set
     */
    public void setClientId(String clientId) {

        m_clientId = clientId;
    }

    /**
     * Sets the element view.<p>
     *
     * @param elementView the element view to set
     */
    public void setElementView(CmsUUID elementView) {

        m_elementView = elementView;
    }

    /**
     * Sets if the element may have settings.<p>
     *
     * @param hasSettings <code>true</code> if the element may have settings
     */
    public void setHasSettings(boolean hasSettings) {

        m_hasSettings = hasSettings;
    }

    /**
     * Sets the inheritance info for this element.<p>
     *
     * @param inheritanceInfo the inheritance info for this element to set
     */
    public void setInheritanceInfo(CmsInheritanceInfo inheritanceInfo) {

        m_inheritanceInfo = inheritanceInfo;
    }

    /**
     * Sets the 'new' flag.<p>
     * 
     * @param isNew <code>true</code> on a new element
     */
    public void setNew(boolean isNew) {

        m_new = isNew;
    }

    /**
     * Disables the new editor for this element.<p>
     * 
     * @param disabled if true, the new editor will be disabled for this element
     */
    public void setNewEditorDisabled(boolean disabled) {

        m_newEditorDisabled = disabled;
    }

    /**
     * Sets the permission info.<p>
     *
     * @param permissionInfo the permission info to set
     */
    public void setPermissionInfo(CmsPermissionInfo permissionInfo) {

        m_permissionInfo = permissionInfo;
    }

    /**
     * Sets if the given resource is released and not expired.<p>
     *
     * @param releasedAndNotExpired <code>true</code> if the given resource is released and not expired
     */
    public void setReleasedAndNotExpired(boolean releasedAndNotExpired) {

        m_releasedAndNotExpired = releasedAndNotExpired;
    }

    /**
     * Sets the element resource type.<p>
     * 
     * @param resourceType the element resource type
     */
    public void setResourceType(String resourceType) {

        m_resourceType = resourceType;
    }

    /**
     * Sets the site path.<p>
     *
     * @param sitePath the site path to set
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }

    /**
     * Sets the sub title.<p>
     * 
     * @param subTitle the sub title
     */
    public void setSubTitle(String subTitle) {

        m_subTitle = subTitle;
    }

    /**
     * Sets the title.<p>
     * 
     * @param title the title
     */
    public void setTitle(String title) {

        m_title = title;
    }
}
