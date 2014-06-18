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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean holding basic container element information.<p>
 * 
 * @since 8.0.0
 */
public class CmsContainerElement implements IsSerializable {

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

    /** Flag to indicate that this element may have settings. */
    private boolean m_hasSettings;

    /** The inheritance info for this element. */
    private CmsInheritanceInfo m_inheritanceInfo;

    /** Flag indicating a new element. */
    private boolean m_new;

    /** Flag which controls whether the new editor is disabled for this element. */
    private boolean m_newEditorDisabled;

    /** The no edit reason. If empty editing is allowed. */
    private String m_noEditReason;

    /** Flag indicating if the given resource is released and not expired. */
    private boolean m_releasedAndNotExpired = true;

    /** The resource type for new elements. If this field is not empty, the element is regarded as new and not created yet. */
    private String m_resourceType;

    /** The full site path. */
    private String m_sitePath;

    /** 
     * Indicates if the current user has view permissions on the element resource. 
     * Without view permissions, the element can neither be edited, nor moved. 
     **/
    private boolean m_viewPermission;

    /** 
     * Indicates if the current user has write permissions on the element resource. 
     * Without write permissions, the element can not be edited. 
     **/
    private boolean m_writePermission;

    /**
     * Default constructor.<p>
     */
    public CmsContainerElement() {

        // empty
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

        return m_noEditReason;
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

        return m_viewPermission;
    }

    /**
     * Returns if the user has write permission.<p>
     *
     * @return <code>true</code> if the user has write permission
     */
    public boolean hasWritePermission() {

        return m_writePermission;
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
     * Sets the no edit reason.<p>
     *
     * @param noEditReason the no edit reason to set
     */
    public void setNoEditReason(String noEditReason) {

        m_noEditReason = noEditReason;
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
     * Sets if the current user has view permissions for the element resource.<p>
     *
     * @param viewPermission the view permission to set
     */
    public void setViewPermission(boolean viewPermission) {

        m_viewPermission = viewPermission;
    }

    /**
     * Sets the user write permission.<p>
     *
     * @param writePermission the user write permission to set
     */
    public void setWritePermission(boolean writePermission) {

        m_writePermission = writePermission;
    }

}
