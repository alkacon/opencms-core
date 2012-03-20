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

package org.opencms.ade.sitemap.client.edit;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapLayoutBundle;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.property.CmsReloadMode;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsClientTemplateBean;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The mode handler for the 'edit entry' mode of the sitemap entry editor.<p>
 * 
 * @since 8.0.0
 */
public class CmsEditEntryHandler extends A_CmsSitemapEntryEditorHandler {

    /** True if the sitemap editor is in simple mode. */
    private boolean m_isSimpleMode;

    /** The page info bean. */
    private CmsListInfoBean m_pageInfo;

    /**
     * Creates a new instance of this class.<p>
     * 
     * @param controller the sitemap controller for this mode 
     * @param entry the sitemap entry for this mode
     * @param isSimpleMode true if the sitemap entry editor is in simple mode  
     */
    public CmsEditEntryHandler(CmsSitemapController controller, CmsClientSitemapEntry entry, boolean isSimpleMode) {

        super(controller, entry);
        m_isSimpleMode = isSimpleMode;

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

        return m_entry.getDefaultFileId();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getDefaultFileProperties()
     */
    public Map<String, CmsClientProperty> getDefaultFileProperties() {

        return m_entry.getDefaultFileProperties();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getDialogTitle()
     */
    public String getDialogTitle() {

        return Messages.get().key(Messages.GUI_EDIT_ENTRY_TITLE_0);

    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getForbiddenUrlNames()
     */
    public List<String> getForbiddenUrlNames() {

        if (m_entry.getSitePath().equals("/")) {
            return Collections.<String> emptyList();
        }

        List<String> result = new ArrayList<String>();
        String parentPath = CmsResource.getParentFolder(m_entry.getSitePath());
        CmsClientSitemapEntry parent = m_controller.getEntry(parentPath);
        if (parent == null) {
            return result;
        }
        for (CmsClientSitemapEntry child : parent.getSubEntries()) {
            if (child != m_entry) {
                result.add(child.getName());
            }
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getId()
     */
    public CmsUUID getId() {

        return m_entry.getId();
    }

    /**
     * Gets the property object which would be inherited by a sitemap entry.<p>
     * 
     * @param name the name of the property 
     * @return the property object which would be inherited 
     */
    public CmsClientProperty getInheritedProperty(String name) {

        return CmsSitemapView.getInstance().getController().getInheritedPropertyObject(m_entry, name);
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getModeClass()
     */
    public String getModeClass() {

        if (CmsSitemapView.getInstance().isNavigationMode()) {
            return I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().navMode();
        } else {
            return I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().vfsMode();
        }
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getOwnProperties()
     */
    public Map<String, CmsClientProperty> getOwnProperties() {

        return m_entry.getOwnProperties();
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

        return m_entry.getSitePath();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#getPossibleTemplates()
     */
    public Map<String, CmsClientTemplateBean> getPossibleTemplates() {

        return CmsSitemapView.getInstance().getController().getData().getTemplates();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#handleSubmit(java.lang.String, java.lang.String, java.util.List, boolean, org.opencms.gwt.client.property.CmsReloadMode)
     */
    public void handleSubmit(
        String newUrlName,
        String vfsPath,
        List<CmsPropertyModification> propertyChanges,
        boolean editedName,
        CmsReloadMode reloadStatus) {

        if (editedName) {
            m_controller.editAndChangeName(m_entry, newUrlName, propertyChanges, false, reloadStatus);
        } else {
            m_controller.edit(m_entry, propertyChanges, reloadStatus);
        }

    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#hasEditableName()
     */
    public boolean hasEditableName() {

        return !getEntry().isRoot();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#isFolder()
     */
    public boolean isFolder() {

        return m_entry.isFolderType();
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#isHiddenProperty(java.lang.String)
     */
    public boolean isHiddenProperty(String key) {

        return CmsSitemapView.getInstance().getController().isHiddenProperty(key);
    }

    /**
     * @see org.opencms.gwt.client.property.I_CmsPropertyEditorHandler#isSimpleMode()
     */
    public boolean isSimpleMode() {

        return m_isSimpleMode;
    }

    /**
     * Sets the page info bean.<p>
     * 
     * @param pageInfo the page info bean
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

    /**
     * Gets the edited sitemap entry.<p>
     * 
     * @return the edited sitemap entry 
     */
    protected CmsClientSitemapEntry getEntry() {

        return m_entry;
    }

    /**
     * Returns the path for the given URL name.<p>
     * 
     * @param urlName the URL name to create the path for
     * 
     * @return the new path for the given URL name
     */
    protected String getPath(String urlName) {

        if (urlName.equals("")) {
            return m_entry.getSitePath();
        }
        return CmsResource.getParentFolder(m_entry.getSitePath()) + urlName + "/";
    }

}
