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

package org.opencms.ui.components.fileselect;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.filter.UnsupportedFilterException;
import com.vaadin.ui.CheckBox;

/**
 * Folder select dialog for the siteamp 'copy page' feature.<p>
 *
 * Normally only shows folders which are either part of the sitemap, or are needed to navigate there starting from the site root,
 * however other folders can be optionally be made visible by the user.
 */
public class CmsSitemapFolderSelectDialog extends CmsResourceSelectDialog {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapFolderSelectDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Boolean flag indicating whether the tree is currently filtered. */
    protected boolean m_filtered = true;

    /** The sitemap tree container. */
    private CmsSitemapTreeContainer m_sitemapTreeContainer;

    /** The tree. */
    private CmsResourceTree m_tree;

    /**
     * Creates a new instance.<p>
     *
     * @throws CmsException if something goes wrong
     */
    public CmsSitemapFolderSelectDialog()
    throws CmsException {
        super(CmsResourceFilter.IGNORE_EXPIRATION.addRequireFolder());
        CheckBox checkBox = new CheckBox();
        checkBox.setValue(Boolean.valueOf(!m_filtered));
        checkBox.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                Boolean checked = (Boolean)(event.getProperty().getValue());
                m_filtered = !checked.booleanValue();
                updateFilter();
            }
        });
        getContents().getAdditionalWidgets().setVisible(true);
        getContents().getAdditionalWidgets().addComponent(checkBox);
        checkBox.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_COPYPAGE_SHOW_ALL_FOLDERS_0));
        updateFilter();
    }

    /**
     * Displays the start resource by opening all nodes in the tree leading to it.<p>
     *
     * @param startResource the resource which should be shown in the tree
     */
    public void showStartResource(CmsResource startResource) {

        CmsObject cms = m_currentCms;
        if (CmsStringUtil.isPrefixPath(m_root.getRootPath(), startResource.getRootPath())) {
            String oldSiteRoot = cms.getRequestContext().getSiteRoot();
            List<CmsUUID> idsToOpen = Lists.newArrayList();
            try {
                cms.getRequestContext().setSiteRoot("");
                CmsResource currentFolder = startResource;
                CmsResource parentFolder = null;

                do {
                    try {
                        parentFolder = cms.readParentFolder(currentFolder.getStructureId());
                        idsToOpen.add(parentFolder.getStructureId());
                        currentFolder = parentFolder;
                    } catch (CmsException e) {
                        LOG.info(e.getLocalizedMessage(), e);
                        break;
                    }
                } while (!parentFolder.getStructureId().equals(m_root.getStructureId()));
                // we need to iterate from "top" to "bottom", so we reverse the list of folders
                Collections.reverse(idsToOpen);

                for (CmsUUID id : idsToOpen) {
                    m_tree.expandItem(id);
                }

            } finally {
                cms.getRequestContext().setSiteRoot(oldSiteRoot);
            }

        }

    }

    /**
     * @see org.opencms.ui.components.fileselect.CmsResourceSelectDialog#createTree(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    protected CmsResourceTree createTree(CmsObject cms, CmsResource root) {

        CmsSitemapTreeContainer container = new CmsSitemapTreeContainer();
        m_sitemapTreeContainer = container;
        m_tree = new CmsResourceTree(
            cms,
            root,
            m_filter,
            container,
            CmsResourceTableProperty.PROPERTY_TYPE_ICON,
            CmsResourceTableProperty.PROPERTY_RESOURCE_NAME);
        return m_tree;
    }

    /**
     * Updates the filtering state.<p>
     */
    protected void updateFilter() {

        if (m_filtered) {
            m_sitemapTreeContainer.removeAllContainerFilters();
            try {
                m_sitemapTreeContainer.addContainerFilter(new CmsNavigationFilter(m_currentCms, m_root));
            } catch (UnsupportedFilterException | CmsException e) {
                CmsErrorDialog.showErrorDialog(e);
            }
        } else {
            m_sitemapTreeContainer.removeAllContainerFilters();
        }
    }

    /**
     * @see org.opencms.ui.components.fileselect.CmsResourceSelectDialog#updateRoot(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    protected void updateRoot(CmsObject rootCms, CmsResource siteRootResource) {

        super.updateRoot(rootCms, siteRootResource);
        updateFilter();

    }

}
