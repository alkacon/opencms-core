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

package org.opencms.gwt.client.property;

import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsClientTemplateBean;
import org.opencms.gwt.shared.property.CmsPropertiesBean;
import org.opencms.gwt.shared.property.CmsPropertyChangeSet;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A simpler implementation of the property editor handler interface which only provides
 * the data to edit a single file's properties (i.e. does not support combined folder/default file
 * property editing.<p>
 *
 * @since 8.0.0
 */
public class CmsSimplePropertyEditorHandler implements I_CmsPropertyEditorHandler {

    /** The data necessary for editing the properties. */
    protected CmsPropertiesBean m_propertiesBean;

    /** The context menu handler. */
    private I_CmsContextMenuHandler m_handler;

    /** Flag controlling whether the file name is editable. */
    private boolean m_hasEditableName;

    /** Object used to save the properties, i.e. send the changed properties to the server. */
    private I_CmsPropertySaver m_propertySaver;

    /**
     * Creates a new instance.<p>
     *
     * @param handler  the context menu handler
     */
    public CmsSimplePropertyEditorHandler(I_CmsContextMenuHandler handler) {

        m_handler = handler;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getAllPropertyNames()
     */
    public List<String> getAllPropertyNames() {

        return m_propertiesBean.getAllProperties();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getDefaultFileId()
     */
    public CmsUUID getDefaultFileId() {

        return null;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getDefaultFileProperties()
     */
    public Map<String, CmsClientProperty> getDefaultFileProperties() {

        return null;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getDialogTitle()
     */
    public String getDialogTitle() {

        // remove this method, it isn't needed anymore
        return "This is thedialog title.";
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getForbiddenUrlNames()
     */
    public List<String> getForbiddenUrlNames() {

        return Collections.emptyList();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getId()
     */
    public CmsUUID getId() {

        return m_propertiesBean.getStructureId();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getInheritedProperty(java.lang.String)
     */
    public CmsClientProperty getInheritedProperty(String name) {

        return m_propertiesBean.getInheritedProperties().get(name);
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getModeClass()
     */
    public String getModeClass() {

        return null;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getName()
     */
    public String getName() {

        String result = CmsResource.getName(m_propertiesBean.getSitePath());
        if (result.endsWith("/") && (result.length() > 0)) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getOwnProperties()
     */
    public Map<String, CmsClientProperty> getOwnProperties() {

        return m_propertiesBean.getOwnProperties();

    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getPageInfo()
     */
    public CmsListInfoBean getPageInfo() {

        return m_propertiesBean.getPageInfo();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getPath()
     */
    public String getPath() {

        return m_propertiesBean.getSitePath();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getPossibleTemplates()
     */
    public Map<String, CmsClientTemplateBean> getPossibleTemplates() {

        return m_propertiesBean.getTemplates();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#handleSubmit(java.lang.String, java.lang.String, java.util.List, boolean, org.opencms.gwt.client.property.CmsReloadMode)
     */
    public void handleSubmit(
        final String newUrlName,
        String vfsPath,
        final List<CmsPropertyModification> propertyChanges,
        final boolean editedName,
        CmsReloadMode reloadMode) {

        // ignore reloadMode; it's only relevant for the sitemap editor

        CmsRpcAction<Void> saveAction = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(300, false);

                if (!CmsStringUtil.isEmptyOrWhitespaceOnly(newUrlName) && editedName) {
                    propertyChanges.add(
                        new CmsPropertyModification(
                            null,
                            CmsPropertyModification.FILE_NAME_PROPERTY,
                            newUrlName,
                            true));
                }
                CmsPropertyChangeSet changeBean = new CmsPropertyChangeSet(
                    m_propertiesBean.getStructureId(),
                    propertyChanges);
                saveProperties(changeBean, this);
            }

            @Override
            protected void onResponse(Void result) {

                stop(false);
                onSubmitSuccess();
            }

        };
        saveAction.execute();

    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#hasEditableName()
     */
    public boolean hasEditableName() {

        return m_hasEditableName;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#isFolder()
     */
    public boolean isFolder() {

        return (m_propertiesBean != null) && m_propertiesBean.isFolder();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#isHiddenProperty(java.lang.String)
     */
    public boolean isHiddenProperty(String key) {

        return false;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#isSimpleMode()
     */
    public boolean isSimpleMode() {

        return false;
    }

    /**
     * Enables / disables editable file name.<p>
     *
     * @param editable true if the file name should be editable
     */
    public void setEditableName(boolean editable) {

        m_hasEditableName = editable;
    }

    /**
     * Sets the data necessary to edit the properties.<p>
     *
     * @param propertiesBean the data which is used to edit the properties
     */
    public void setPropertiesBean(CmsPropertiesBean propertiesBean) {

        m_propertiesBean = propertiesBean;
    }

    /**
     * Sets the property saver.<p>
     *
     * @param saver the property saver
     */
    public void setPropertySaver(I_CmsPropertySaver saver) {

        m_propertySaver = saver;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#useAdeTemplates()
     */
    public boolean useAdeTemplates() {

        return (m_propertiesBean != null) && (m_propertiesBean.isContainerPage() || m_propertiesBean.isFolder());
    }

    /**
     * Returns the context menu handler.<p>
     *
     * @return the context menu handler
     */
    protected I_CmsContextMenuHandler getContextMenuHandler() {

        return m_handler;
    }

    /**
     * Called when the form is submitted successfully.<p>
     */
    protected void onSubmitSuccess() {

        if (getContextMenuHandler() != null) {
            getContextMenuHandler().refreshResource(m_propertiesBean.getStructureId());
        }
    }

    /**
     * Save properties.<p>
     *
     * @param changes the property changes
     * @param callback the result callback
     */
    protected void saveProperties(CmsPropertyChangeSet changes, AsyncCallback<Void> callback) {

        if (m_propertySaver != null) {
            m_propertySaver.saveProperties(changes, callback);
        } else {
            CmsCoreProvider.getVfsService().saveProperties(changes, callback);
        }
    }

}
