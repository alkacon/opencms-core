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
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;

import org.apache.commons.logging.Log;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.VerticalLayout;

/**
 * Dialog with a site selector and file tree which can be used to select resources.<p>
 */
public class CmsResourceSelectDialog extends CmsBasicDialog {

    /**
     * Converts resource selection to path (string) selection - either as root paths or site paths.<p>
     */
    class PathSelectionAdapter implements I_CmsSelectionHandler<CmsResource> {

        /** The wrapped string selection handler. */
        private I_CmsSelectionHandler<String> m_pathHandler;

        /** If true, pass site paths to the wrapped path handler, else root paths. */
        private boolean m_useSitePaths;

        /**
         * Creates a new instance.<p>
         *
         * @param pathHandler the selection handler to call
         * @param useSitePaths true if we want changes as site paths
         */
        public PathSelectionAdapter(I_CmsSelectionHandler<String> pathHandler, boolean useSitePaths) {
            m_pathHandler = pathHandler;
            m_useSitePaths = useSitePaths;
        }

        /**
         * @see org.opencms.ui.components.fileselect.I_CmsSelectionHandler#onSelection(java.lang.Object)
         */
        @SuppressWarnings("synthetic-access")
        public void onSelection(CmsResource selected) {

            String path = selected.getRootPath();
            if (m_useSitePaths) {
                try {
                    CmsObject cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                    cms.getRequestContext().setSiteRoot(m_siteRoot);
                    path = cms.getRequestContext().removeSiteRoot(path);
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);

                }
            }
            m_pathHandler.onSelection(path);
        }
    }

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceSelectDialog.class);

    /** The property used for the site caption. */
    public static final String PROPERTY_SITE_CAPTION = "caption";

    /** The site selector. */
    private ComboBox m_siteSelector;

    /** Contains the data for the tree. */
    private CmsResourceTreeContainer m_treeData;

    /** The site root. */
    private String m_siteRoot;

    /** Container for the tree component. */
    private VerticalLayout m_treeContainer;

    /** The file tree (wrapped in an array, because Vaadin Declarative tries to bind it otherwise) .*/
    private CmsResourceTree[] m_fileTree = {null};

    /** The resource filter. */
    private CmsResourceFilter m_filter;

    /**
     * Creates a new instance.<p>
     *
     * @param filter the resource filter to use
     *
     * @throws CmsException if something goes wrong
     */
    public CmsResourceSelectDialog(CmsResourceFilter filter)
    throws CmsException {
        m_filter = filter;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        CmsObject cms = A_CmsUI.getCmsObject();
        m_siteSelector.setContainerDataSource(CmsVaadinUtils.getAvailableSitesContainer(cms, PROPERTY_SITE_CAPTION));
        m_siteRoot = cms.getRequestContext().getSiteRoot();
        m_siteSelector.setValue(m_siteRoot);
        m_siteSelector.setNullSelectionAllowed(false);
        m_siteSelector.setItemCaptionPropertyId(PROPERTY_SITE_CAPTION);
        m_siteSelector.setFilteringMode(FilteringMode.CONTAINS);
        m_siteSelector.addValueChangeListener(new ValueChangeListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                String site = (String)(event.getProperty().getValue());
                onSiteChange(site);
            }
        });

        CmsResource root = cms.readResource("/");
        CmsResourceTree fileTree = new CmsResourceTree(cms, root, m_filter);
        m_fileTree[0] = fileTree;
        m_treeData = fileTree.getTreeContainer();
        m_treeContainer.addComponent(fileTree);
    }

    /**
     * Adds a new path selection handler.<p>
     *
     * @param useSitePaths true if we want changes as site paths, false for root paths
     *
     * @param rootPathHandler the selection handler to call when the user selects a file
     */
    public void addPathSelectionHandler(boolean useSitePaths, I_CmsSelectionHandler<String> rootPathHandler) {

        m_fileTree[0].addResourceSelectionHandler(new PathSelectionAdapter(rootPathHandler, useSitePaths));
    }

    /**
     * Called when the user changes the site.<p>
     *
     * @param site the new site root
     */
    protected void onSiteChange(String site) {

        try {
            m_treeData.removeAllItems();
            CmsObject rootCms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            rootCms.getRequestContext().setSiteRoot("");
            CmsResource siteRootResource = rootCms.readResource(site);
            m_treeData.initRoot(rootCms, siteRootResource, m_filter);
            m_fileTree[0].expandItem(siteRootResource.getStructureId());
            m_siteRoot = site;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

}
