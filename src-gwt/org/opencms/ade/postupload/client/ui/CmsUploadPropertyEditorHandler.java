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

package org.opencms.ade.postupload.client.ui;

import org.opencms.ade.postupload.shared.CmsPostUploadDialogPanelBean;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.property.CmsReloadMode;
import org.opencms.gwt.client.property.I_CmsPropertyEditorHandler;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsClientTemplateBean;
import org.opencms.gwt.shared.property.CmsPropertyChangeSet;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.gwt.shared.rpc.I_CmsVfsServiceAsync;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The property editor handler class for editing properties of uploaded files.<p>
 */
public class CmsUploadPropertyEditorHandler implements I_CmsPropertyEditorHandler {

    /** The data used for editing the properties of a single resource. */
    CmsPostUploadDialogPanelBean m_data;

    /** The dialog containing the property editor. */
    CmsUploadPropertyDialog m_dialog;

    /**
     * Creates a new instance.<p>
     *
     * @param dialog the dialog containing the property editor
     * @param data the data for editing the properties
     */
    public CmsUploadPropertyEditorHandler(CmsUploadPropertyDialog dialog, CmsPostUploadDialogPanelBean data) {

        m_data = data;
        m_dialog = dialog;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getAllPropertyNames()
     */
    public List<String> getAllPropertyNames() {

        return new ArrayList<String>(m_data.getPropertyDefinitions().keySet());
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

        return "DUMMY TITLE";
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

        return m_data.getStructureId();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getInheritedProperty(java.lang.String)
     */
    public CmsClientProperty getInheritedProperty(String name) {

        return null;
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

        return m_data.getStructureId().toString();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getOwnProperties()
     */
    public Map<String, CmsClientProperty> getOwnProperties() {

        return m_data.getProperties();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getPageInfo()
     */
    public CmsListInfoBean getPageInfo() {

        return m_data.getInfoBean();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getPath()
     */
    public String getPath() {

        return "DUMMY PATH";
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getPossibleTemplates()
     */
    public Map<String, CmsClientTemplateBean> getPossibleTemplates() {

        return null;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#handleSubmit(java.lang.String, java.lang.String, java.util.List, boolean, org.opencms.gwt.client.property.CmsReloadMode)
     */
    public void handleSubmit(
        String newUrlName,
        String vfsPath,
        final List<CmsPropertyModification> propertyChanges,
        boolean editedName,
        CmsReloadMode reloadMode) {

        // ignore reloadMode; it's only relevant for the sitemap editor

        final I_CmsVfsServiceAsync vfsService = CmsCoreProvider.getVfsService();
        CmsRpcAction<Void> saveAction = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(300, false);
                CmsPropertyChangeSet changeBean = new CmsPropertyChangeSet(m_data.getStructureId(), propertyChanges);
                vfsService.saveProperties(changeBean, this);
            }

            @Override
            protected void onResponse(Void result) {

                stop(false);
                m_dialog.runAction();

            }
        };
        saveAction.execute();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#hasEditableName()
     */
    public boolean hasEditableName() {

        return false;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#isFolder()
     */
    public boolean isFolder() {

        // TODO: Auto-generated method stub
        return false;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#isHiddenProperty(java.lang.String)
     */
    public boolean isHiddenProperty(String key) {

        // TODO: Auto-generated method stub
        return false;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#isSimpleMode()
     */
    public boolean isSimpleMode() {

        return false;
    }

    /**
     * Sets the dialog instance.<p>
     *
     * @param dialog the dialog used for editing the properties
     */
    public void setDialog(CmsUploadPropertyDialog dialog) {

        m_dialog = dialog;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#useAdeTemplates()
     */
    public boolean useAdeTemplates() {

        return false;
    }

}
