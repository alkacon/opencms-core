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

package org.opencms.ade.sitemap.client.edit;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapLayoutBundle;
import org.opencms.ade.sitemap.shared.CmsLocaleComparePropertyData;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync;
import org.opencms.gwt.client.property.CmsReloadMode;
import org.opencms.gwt.client.property.I_CmsPropertyEditorHandler;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsJsUtil;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsClientTemplateBean;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Map;

/**
 * Property submission handler for the sitemap editor's locale comparison mode.<p>
 */
public class CmsLocaleComparePropertyHandler implements I_CmsPropertyEditorHandler {

    /** The property data loaded from the server. */
    private CmsLocaleComparePropertyData m_data;

    /** The page information. */
    private CmsListInfoBean m_pageInfo;

    /**
     * Creates a new instance.<p>
     *
     * @param data the property data loaded from the server
     */
    public CmsLocaleComparePropertyHandler(CmsLocaleComparePropertyData data) {
        m_data = data;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getAllPropertyNames()
     */
    public List<String> getAllPropertyNames() {

        return CmsSitemapView.getInstance().getController().getData().getAllPropertyNames();

    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getDefaultFileId()
     */
    public CmsUUID getDefaultFileId() {

        return m_data.getDefaultFileId();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getDefaultFileProperties()
     */
    public Map<String, CmsClientProperty> getDefaultFileProperties() {

        return m_data.getDefaultFileProperties();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getDialogTitle()
     */
    public String getDialogTitle() {

        return Messages.get().key(Messages.GUI_PROPERTY_EDITOR_TITLE_0);
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getForbiddenUrlNames()
     */
    public List<String> getForbiddenUrlNames() {

        return m_data.getForbiddenUrlNames();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getId()
     */
    public CmsUUID getId() {

        return m_data.getId();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getInheritedProperty(java.lang.String)
     */
    public CmsClientProperty getInheritedProperty(String name) {

        return m_data.getInheritedProperty(name);
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getModeClass()
     */
    public String getModeClass() {

        return I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().navMode();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getName()
     */
    public String getName() {

        return m_data.getName();

    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getOwnProperties()
     */
    public Map<String, CmsClientProperty> getOwnProperties() {

        return m_data.getOwnProperties();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getPageInfo()
     */
    public CmsListInfoBean getPageInfo() {

        return m_pageInfo;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getPath()
     */
    public String getPath() {

        return m_data.getPath();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getPossibleTemplates()
     */
    public Map<String, CmsClientTemplateBean> getPossibleTemplates() {

        // TODO Auto-generated method stub
        return null;
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

        final CmsUUID id = m_data.getId();
        final I_CmsSitemapServiceAsync service = CmsSitemapView.getInstance().getController().getService();
        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(0, false);
                service.savePropertiesForLocaleCompareMode(id, newUrlName, propertyChanges, editedName, this);
            }

            @Override
            protected void onResponse(Void result) {

                stop(false);
                CmsDebugLog.consoleLog("Saved");
                CmsJsUtil.callNamedFunctionWithString(CmsGwtConstants.CALLBACK_HANDLE_CHANGED_PROPERTIES, "" + id);

            }
        };
        action.execute();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#hasEditableName()
     */
    public boolean hasEditableName() {

        return m_data.hasEditableName();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#isFolder()
     */
    public boolean isFolder() {

        return m_data.isFolder();
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

        return true;
    }

    /**
     * Sets the page information.<p>
     *
     * @param pageInfo the page information
     */
    public void setPageInfo(CmsListInfoBean pageInfo) {

        m_pageInfo = pageInfo;

    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#useAdeTemplates()
     */
    public boolean useAdeTemplates() {

        return true;
    }

}
