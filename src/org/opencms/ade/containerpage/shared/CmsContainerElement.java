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

package org.opencms.ade.containerpage.shared;

import org.opencms.gwt.shared.CmsPermissionInfo;
import org.opencms.gwt.shared.I_CmsHasIconClasses;
import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean holding basic container element information.<p>
 *
 * @since 8.0.0
 */
public class CmsContainerElement implements IsSerializable, I_CmsHasIconClasses {

    /** The model group states. */
    public static enum ModelGroupState {

        /** Is model group state. */
        isModelGroup,

        /** No model group what so ever. */
        noGroup,

        /** Former copy model group. */
        wasModelGroup;

        /**
         * Evaluates the given state string.<p>
         *
         * @param state the state
         *
         * @return the model group state
         */
        public static ModelGroupState evaluate(String state) {

            ModelGroupState result = null;
            if (state != null) {
                try {
                    result = ModelGroupState.valueOf(state);
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
            if (result == null) {
                result = noGroup;
            }
            return result;
        }
    }

    /** HTML class used to identify containers. */
    public static final String CLASS_CONTAINER = "oc-container";

    /** HTML class used to identify container elements. */
    public static final String CLASS_CONTAINER_ELEMENT_END_MARKER = "oc-element-end";

    /** HTML class used to identify container elements. */
    public static final String CLASS_CONTAINER_ELEMENT_START_MARKER = "oc-element-start";

    /** HTML class used to identify error message for elements where rendering failed to render. */
    public static final String CLASS_ELEMENT_ERROR = "oc-element-error";

    /** HTML class used to identify group container elements. */
    public static final String CLASS_GROUP_CONTAINER_ELEMENT_MARKER = "oc-groupcontainer";

    /** The create as new setting key. */
    public static final String CREATE_AS_NEW = "create_as_new";

    /** The element instance id settings key. */
    public static final String ELEMENT_INSTANCE_ID = "element_instance_id";

    /** The group container resource type name. */
    public static final String GROUP_CONTAINER_TYPE_NAME = "groupcontainer";

    /** The resource type name for inherited container references.  */
    public static final String INHERIT_CONTAINER_TYPE_NAME = "inheritance_group";

    /** The is model group always replace element setting key. */
    public static final String IS_MODEL_GROUP_ALWAYS_REPLACE = "is_model_group_always_replace";

    /** The container id marking the edit menus. */
    public static final String MENU_CONTAINER_ID = "cms_edit_menu_container";

    /** The model group id setting key. */
    public static final String MODEL_GROUP_ID = "model_group_id";

    /** Prefix for new system element settings. */
    public static final String SYSTEM_SETTING_PREFIX = "SYSTEM::";

    /** The is model group element setting key. */
    public static final String MODEL_GROUP_STATE = "model_group_state";

    /** Key for the setting that replaces the CreateNew element. */
    public static final String SETTING_CREATE_NEW = "SYSTEM::create_new";

    /**
     * Key for the setting used to identify which page this element was read from originally.
     *
     * <p>This setting is not stored when saving a container page.
     **/
    public static final String SETTING_PAGE_ID = "SYSTEM::pageId";

    /** The use as copy model setting key. */
    public static final String USE_AS_COPY_MODEL = "use_as_copy_model";

    private CmsElementLockInfo m_lockInfo = new CmsElementLockInfo(null, false);

    /** The element client id. */
    private String m_clientId;

    /** The copy in models flag. */
    private boolean m_copyInModels;

    /** The 'create new' status of the element. */
    private boolean m_createNew;

    /** The element view this element belongs to by it's type. */
    private CmsUUID m_elementView;

    /** Indicates an edit handler is configured for the given resource type. */
    private boolean m_hasEditHandler;

    /** Flag to indicate that this element may have settings. */
    private boolean m_hasSettings;

    /** The resource type icon CSS classes. */
    private String m_iconClasses;

    /** The inheritance info for this element. */
    private CmsInheritanceInfo m_inheritanceInfo;

    /** The model group always replace flag. */
    private boolean m_isModelGroupAlwaysReplace;

    /** The model group id or null. */
    private CmsUUID m_modelGroupId;

    /** Flag indicating a new element. */
    private boolean m_new;

    /** Flag which controls whether the new editor is disabled for this element. */
    private boolean m_newEditorDisabled;

    /** The permission info for the element resource. */
    private CmsPermissionInfo m_permissionInfo;

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

    /** The former copy model status. */
    private boolean m_wasModelGroup;

    /** True if the element is marked as 'reused'. */
    private boolean m_reused;

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
        result.m_iconClasses = m_iconClasses;
        result.m_sitePath = m_sitePath;
        result.m_subTitle = m_subTitle;
        result.m_title = m_title;
        result.m_elementView = m_elementView;
        result.m_modelGroupId = m_modelGroupId;
        result.m_wasModelGroup = m_wasModelGroup;
        result.m_isModelGroupAlwaysReplace = m_isModelGroupAlwaysReplace;
        result.m_reused = m_reused;
        return result;
    }

    /**
     * Returns the resource type icon CSS rules.<p>
     *
     * @return the resource type icon CSS rules
     */
    public String getBigIconClasses() {

        return m_iconClasses;
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

    public CmsElementLockInfo getLockInfo() {

        return m_lockInfo;
    }

    /**
     * Returns the model group id.<p>
     *
     * @return the model group id
     */
    public CmsUUID getModelGroupId() {

        return m_modelGroupId;
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
     * @see org.opencms.gwt.shared.I_CmsHasIconClasses#getSmallIconClasses()
     */
    public String getSmallIconClasses() {

        // not needed
        return null;
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
     * Returns if an edit handler is configured for the given resource type.<p>
     *
     * @return <code>true</code> if an edit handler is configured for the given resource type
     */
    public boolean hasEditHandler() {

        return m_hasEditHandler;
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
     * Returns the copy in models flag.<p>
     *
     * @return the copy in models flag
     */
    public boolean isCopyInModels() {

        return m_copyInModels;
    }

    /**
     * Reads the 'create new' status of the element.<p>
     *
     * When the page containing the element is used a model page, this flag determines whether a copy of the element
     * is created when creating a new page from that model page.<p>
     *
     * @return the 'create new' status of the element
     */
    public boolean isCreateNew() {

        return m_createNew;
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
     * Returns if the element is a model group.<p>
     *
     * @return <code>true</code> if the element is a model group
     */
    public boolean isModelGroup() {

        return m_modelGroupId != null;
    }

    /**
     * Returns if all instances of this element should be replaced within a model group.<p>
     *
     * @return <code>true</code> if all instances of this element should be replaced within a model group
     */
    public boolean isModelGroupAlwaysReplace() {

        return m_isModelGroupAlwaysReplace;
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
     * True if the element is marked as reused.
     *
     * @return true if the element is marked as reused
     */
    public boolean isReused() {

        return m_reused;
    }

    /**
     * Returns the former copy model status.<p>
     *
     * @return the former copy model status
     */
    public boolean isWasModelGroup() {

        return m_wasModelGroup;
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
     * Sets the copy in models flag.<p>
     *
     * @param copyInModels the copy in models flag to set
     */
    public void setCopyInModels(boolean copyInModels) {

        m_copyInModels = copyInModels;
    }

    /**
     * Sets the 'create new' status of the element.<p>
     *
     * @param createNew the new 'create new' status
     */
    public void setCreateNew(boolean createNew) {

        m_createNew = createNew;
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
     * Sets the if an edit handler is configured for the given resource type.<p>
     *
     * @param hasEditHandler if an edit handler is configured for the given resource type
     */
    public void setHasEditHandler(boolean hasEditHandler) {

        m_hasEditHandler = hasEditHandler;
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
     * Sets the resource type icon CSS rules.<p>
     *
     * @param iconRules resource type icon CSS rules to set
     */
    public void setIconClasses(String iconRules) {

        m_iconClasses = iconRules;
    }

    /**
     * Sets the inheritance info for this element.<p>
     *
     * @param inheritanceInfo the inheritance info for this element to set
     */
    public void setInheritanceInfo(CmsInheritanceInfo inheritanceInfo) {

        m_inheritanceInfo = inheritanceInfo;
    }

    public void setLockInfo(CmsElementLockInfo lockInfo) {

        m_lockInfo = lockInfo;
    }

    /**
     * Sets if all instances of this element should be replaced within a model group.<p>
     *
     * @param alwaysReplace if all instances of this element should be replaced within a model group
     */
    public void setModelGroupAlwaysReplace(boolean alwaysReplace) {

        m_isModelGroupAlwaysReplace = alwaysReplace;
    }

    /**
     * Sets the model group id.<p>
     *
     * @param modelGroupId <code>true</code> if the element is a model group
     */
    public void setModelGroupId(CmsUUID modelGroupId) {

        m_modelGroupId = modelGroupId;
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
     * Sets the 'reused' status.
     *
     * @param reused the 'reused' status
     */
    public void setReused(boolean reused) {

        m_reused = reused;
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

    /**
     * Sets the was model group flag.<p>
     *
     * @param wasModelGroup the was model group flag to set
     */
    public void setWasModelGroup(boolean wasModelGroup) {

        m_wasModelGroup = wasModelGroup;
    }
}
