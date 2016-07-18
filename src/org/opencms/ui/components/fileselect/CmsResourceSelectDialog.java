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

import org.apache.commons.logging.Log;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;

/**
 * Dialog with a site selector and file tree which can be used to select resources.<p>
 */
public class CmsResourceSelectDialog extends CustomComponent {

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

    /** The property used for the site caption. */
    public static final String PROPERTY_SITE_CAPTION = "caption";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceSelectDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The CMS context. */
    protected CmsObject m_currentCms;

    /** The resource filter. */
    protected CmsResourceFilter m_filter;

    /** The resource initially displayed at the root of the tree. */
    protected CmsResource m_root;

    /** The file tree (wrapped in an array, because Vaadin Declarative tries to bind it otherwise) .*/
    private CmsResourceTree m_fileTree = null;

    /** The site root. */
    private String m_siteRoot;

    /** Contains the data for the tree. */
    private CmsResourceTreeContainer m_treeData;

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

        CmsObject cms = A_CmsUI.getCmsObject();
        setCompositionRoot(new CmsResourceSelectDialogContents());
        getSiteSelector().setContainerDataSource(CmsVaadinUtils.getAvailableSitesContainer(cms, PROPERTY_SITE_CAPTION));
        m_siteRoot = cms.getRequestContext().getSiteRoot();
        getSiteSelector().setValue(m_siteRoot);
        getSiteSelector().setNullSelectionAllowed(false);
        getSiteSelector().setItemCaptionPropertyId(PROPERTY_SITE_CAPTION);
        getSiteSelector().setFilteringMode(FilteringMode.CONTAINS);
        getSiteSelector().addValueChangeListener(new ValueChangeListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                String site = (String)(event.getProperty().getValue());
                onSiteChange(site);
            }
        });

        CmsResource root = cms.readResource("/");
        CmsResourceTree fileTree = createTree(cms, root);
        updateRoot(cms, root);
        m_fileTree = fileTree;
        m_treeData = fileTree.getTreeContainer();
        getContents().getTreeContainer().addComponent(fileTree);
        fileTree.setSizeFull();
    }

    /**
     * Adds a resource selection handler.<p>
     *
     * @param handler the handler
     */
    public void addSelectionHandler(I_CmsSelectionHandler<CmsResource> handler) {

        m_fileTree.addResourceSelectionHandler(handler);
    }

    /**
     * Creates the resource tree for the given root.<p>
     *
     * @param cms the CMS context
     * @param root the root resource
     * @return the resource tree
     */
    protected CmsResourceTree createTree(CmsObject cms, CmsResource root) {

        return new CmsResourceTree(cms, root, m_filter);
    }

    /**
     * Gets the file tree.<p>
     *
     * @return the file tree
     */
    protected CmsResourceTree getFileTree() {

        return m_fileTree;
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
            m_fileTree.expandItem(siteRootResource.getStructureId());
            m_siteRoot = site;
            updateRoot(rootCms, siteRootResource);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Updates the current site root resource.<p>
     *
     * @param rootCms the CMS context
     * @param siteRootResource the resource corresponding to a site root
     */
    protected void updateRoot(CmsObject rootCms, CmsResource siteRootResource) {

        m_root = siteRootResource;
        m_currentCms = rootCms;
    }

    /**
     * Gets the content panel of this dialog.<p>
     *
     * @return content panel of this dialog
     */
    CmsResourceSelectDialogContents getContents() {

        return ((CmsResourceSelectDialogContents)getCompositionRoot());
    }

    /**
     * Gets the site selector.<p>
     *
     * @return the site selector
     */
    private ComboBox getSiteSelector() {

        return getContents().getSiteSelector();
    }

}
