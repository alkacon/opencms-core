/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
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
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsResourceTableProperty;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Container.Filter;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.shared.ui.combobox.FilteringMode;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.TreeTable;

/**
 * Dialog with a site selector and file tree which can be used to select resources.<p>
 */
public class CmsResourceSelectDialog extends CustomComponent {

    /**
     * Class for site select options.<p>
     */
    public static class Options {

        /**Indexed container.*/
        private IndexedContainer m_siteSelectionContainer;

        /**
         * Returns the siteSelectionContainer.<p>
         *
         * @return the siteSelectionContainer
         */
        public IndexedContainer getSiteSelectionContainer() {

            return m_siteSelectionContainer;
        }

        /**
         * Sets the siteSelectionContainer.<p>
         *
         * @param siteSelectionContainer the siteSelectionContainer to set
         */
        public void setSiteSelectionContainer(IndexedContainer siteSelectionContainer) {

            m_siteSelectionContainer = siteSelectionContainer;
        }

    }

    /**
     * Information needed for filtering the resource tree.
     */
    static class FilterData {

        /** The set of folders to open. */
        private Set<CmsResource> m_openFolders = new HashSet<>();

        /** The set of root paths directly matching the filter. */
        private Set<String> m_matchedPaths = new HashSet<>();

        /**
         * Gets the set of root paths of resources directly matching the filter.
         *
         * @return the set of paths of matching resources
         */
        public Set<String> getMatchedPaths() {

            return m_matchedPaths;
        }

        /**
         * Gets the set of folders to open +
         *
         * @return the set of folders to open
         */
        public Set<CmsResource> getOpenFolders() {

            return m_openFolders;
        }
    }

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
    protected CmsResourceFilter m_resourceFilter;

    /** The resource initially displayed at the root of the tree. */
    protected CmsResource m_root;

    /** The file tree (wrapped in an array, because Vaadin Declarative tries to bind it otherwise) .*/
    private CmsResourceTreeTable m_fileTree;

    /** Boolean flag indicating whether the tree is currently filtered. */
    private boolean m_isSitemapView = true;

    /** The site root. */
    private String m_siteRoot;

    /** Contains the data for the tree. */
    private CmsResourceTreeContainer m_treeData;

    /** The currently applied filter. */
    private Filter m_currentFilter;

    /**
     * Creates a new instance.<p>
     *
     * @param filter the resource filter to use
     *
     * @throws CmsException if something goes wrong
     */
    public CmsResourceSelectDialog(CmsResourceFilter filter)
    throws CmsException {

        this(filter, A_CmsUI.getCmsObject());
    }

    /**
     * public constructor with given CmsObject.<p>
     *
     * @param filter filter the resource filter to use
     * @param cms CmsObejct to use
     * @throws CmsException if something goes wrong
     */
    public CmsResourceSelectDialog(CmsResourceFilter filter, CmsObject cms)
    throws CmsException {

        this(filter, cms, new Options());
    }

    /**
     * public constructor.<p>
     *
     * @param filter resource filter
     * @param cms CmsObject
     * @param options options
     * @throws CmsException exception
     */
    public CmsResourceSelectDialog(CmsResourceFilter filter, final CmsObject _cms, Options options)
    throws CmsException {

        m_resourceFilter = filter;
        setCompositionRoot(new CmsResourceSelectDialogContents());
        IndexedContainer container = options.getSiteSelectionContainer() != null
        ? options.getSiteSelectionContainer()
        : CmsVaadinUtils.getAvailableSitesContainer(_cms, PROPERTY_SITE_CAPTION);
        getSiteSelector().setContainerDataSource(container);

        final CmsObject cms;
        if (!_cms.existsResource("/", CmsResourceFilter.IGNORE_EXPIRATION)) {
            cms = OpenCms.initCmsObject(_cms);
            cms.getRequestContext().setSiteRoot("/system/");
        } else {
            cms = _cms;
        }
        m_siteRoot = cms.getRequestContext().getSiteRoot();

        getSiteSelector().setValue(
            CmsVaadinUtils.getPathItemId(getSiteSelector().getContainerDataSource(), m_siteRoot));
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
        m_fileTree = createTree(cms, root);
        m_fileTree.setColumnExpandRatio(CmsResourceTreeTable.CAPTION_FOLDERS, 5);
        m_fileTree.setColumnExpandRatio(CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT, 1);
        m_treeData = m_fileTree.getTreeContainer();
        updateRoot(cms, root);

        getContents().getTreeContainer().addComponent(m_fileTree);
        m_fileTree.setSizeFull();
        getContents().addAttachListener(event -> {
            Window window = CmsVaadinUtils.getWindow(this);
            if (window != null) {
                window.addActionHandler(new Handler() {

                    private final Action enterKeyShortcutAction = new ShortcutAction(
                        null,
                        ShortcutAction.KeyCode.ENTER,
                        null);

                    @Override
                    public Action[] getActions(Object target, Object sender) {

                        return new Action[] {enterKeyShortcutAction};
                    }

                    @Override
                    public void handleAction(Action action, Object sender, Object target) {

                        if (enterKeyShortcutAction.equals(action)) {
                            if (target == getContents().getFilterBox()) {
                                updateFilter();
                            }
                        }

                    }
                });
            }
        });

        updateView();

        getContents().getFilterButton().addClickListener(event -> {
            updateFilter();
        });

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
     * Disables the option to select resources from other sites.<p>
     */
    public void disableSiteSwitch() {

        getSiteSelector().setEnabled(false);
    }

    /**
     * Opens the given path.<p>
     *
     * @param path the path to open
     */
    public void openPath(String path) {

        if (!CmsStringUtil.isPrefixPath(m_root.getRootPath(), path)) {
            CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(path);
            if (site != null) {
                // the given path is a root path switch to the determined site
                getSiteSelector().setValue(site.getSiteRoot());
                path = m_currentCms.getRequestContext().removeSiteRoot(path);
            } else if (OpenCms.getSiteManager().startsWithShared(path)) {
                getSiteSelector().setValue(OpenCms.getSiteManager().getSharedFolder());
            } else if (path.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)) {
                Container container = getSiteSelector().getContainerDataSource();
                String newSiteRoot = null;
                for (String possibleSiteRoot : Arrays.asList("", "/", "/system", "/system/")) {
                    if (container.containsId(possibleSiteRoot)) {
                        newSiteRoot = possibleSiteRoot;
                        break;
                    }
                }
                if (newSiteRoot == null) {
                    LOG.warn(
                        "Couldn't open path in site selector because neither root site nor system folder are in the site selector. path="
                            + path);
                    return;
                }
                getSiteSelector().setValue(newSiteRoot);
            }
        }
        if (!"/".equals(path)) {
            List<CmsUUID> idsToOpen = Lists.newArrayList();
            try {
                CmsResource currentFolder = m_currentCms.readResource(CmsResource.getParentFolder(path));
                if (!m_root.getStructureId().equals(currentFolder.getStructureId())) {
                    idsToOpen.add(currentFolder.getStructureId());
                    CmsResource parentFolder = null;

                    do {
                        try {
                            parentFolder = m_currentCms.readParentFolder(currentFolder.getStructureId());
                            idsToOpen.add(parentFolder.getStructureId());
                            currentFolder = parentFolder;
                        } catch (CmsException | NullPointerException e) {
                            LOG.info(e.getLocalizedMessage(), e);
                            break;
                        }
                    } while (!parentFolder.getStructureId().equals(m_root.getStructureId()));
                    // we need to iterate from "top" to "bottom", so we reverse the list of folders
                    Collections.reverse(idsToOpen);

                    for (CmsUUID id : idsToOpen) {
                        m_fileTree.expandItem(id);
                    }
                }
            } catch (CmsException e) {
                LOG.debug("Can not read parent folder of current path.", e);
            }
        }
    }

    /**
     * Switches between the folders and sitemap view of the tree.<p>
     *
     * @param showSitemapView <code>true</code> to show the sitemap view
     */
    public void showSitemapView(boolean showSitemapView) {

        if (m_isSitemapView != showSitemapView) {
            m_isSitemapView = showSitemapView;
            updateView();
        }
    }

    /**
     * Displays the start resource by opening all nodes in the tree leading to it.<p>
     *
     * @param startResource the resource which should be shown in the tree
     */
    public void showStartResource(CmsResource startResource) {

        openPath(startResource.getRootPath());
    }

    /**
     * Creates the resource tree for the given root.<p>
     *
     * @param cms the CMS context
     * @param root the root resource
     * @return the resource tree
     */
    protected CmsResourceTreeTable createTree(CmsObject cms, CmsResource root) {

        return new CmsResourceTreeTable(cms, root, m_resourceFilter);
    }

    /**
     * Gets the content panel of this dialog.<p>
     *
     * @return content panel of this dialog
     */
    protected CmsResourceSelectDialogContents getContents() {

        return ((CmsResourceSelectDialogContents)getCompositionRoot());
    }

    /**
     * Gets the file tree.<p>
     *
     * @return the file tree
     */
    protected CmsResourceTreeTable getFileTree() {

        return m_fileTree;
    }

    /**
     * Called when the user changes the site.<p>
     *
     * @param site the new site root
     */
    protected void onSiteChange(String site) {

        try {
            removeStringFilter();
            m_treeData.removeAllItems();

            // the tree table caches the open/closed state of items,  even when the content of the data source changes,
            // and then can get confused during a site change because the container items for a site share IDs
            // with the corresponding container items in the root site.
            // The following is a hack to clear the open/closed state cache, which depends on the internals of TreeTable.
            try {
                Field cStrategy = TreeTable.class.getDeclaredField("cStrategy");
                cStrategy.trySetAccessible();
                cStrategy.set(m_fileTree, null);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            CmsResourceSelectDialogContents contents = (CmsResourceSelectDialogContents)getCompositionRoot();
            contents.getFilterBox().setValue("");

            CmsObject rootCms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            rootCms.getRequestContext().setSiteRoot("");
            CmsResource siteRootResource = rootCms.readResource(site);
            m_treeData.initRoot(rootCms, siteRootResource);
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
        updateView();
    }

    /**
     * Updates the filtering state.<p>
     */
    protected void updateView() {

        m_fileTree.showSitemapView(m_isSitemapView);
    }

    private FilterData getFilterData(CmsObject cms, CmsResource root, CmsResourceFilter filter, String filterText)
    throws CmsException {

        List<CmsResource> allResources = cms.readResources(root, filter, true);
        List<CmsResource> matching = allResources.stream().filter(
            res -> res.getName().toLowerCase().contains(filterText.toLowerCase())).collect(Collectors.toList());
        Map<String, CmsResource> resourcesByPath = allResources.stream().collect(
            Collectors.toMap(res -> res.getRootPath(), res -> res, (a, b) -> b));
        FilterData result = new FilterData();
        for (CmsResource res : matching) {
            String path = res.getRootPath();
            result.getMatchedPaths().add(path);
            do {
                path = CmsResource.getParentFolder(path);
                CmsResource parent = resourcesByPath.get(path);
                if (parent != null) {
                    result.getOpenFolders().add(parent);
                }
            } while ((path != null) && !path.equals(root.getRootPath()));
        }
        return result;

    }

    /**
     * Gets the site selector.<p>
     *
     * @return the site selector
     */
    private ComboBox getSiteSelector() {

        return getContents().getSiteSelector();
    }

    /**
     * Removes the current filter.
     */
    private void removeStringFilter() {

        if (m_currentFilter != null) {
            m_treeData.removeContainerFilter(m_currentFilter);
            m_currentFilter = null;
        }
    }

    /**
     * Updates the filtering based on the current content of the filter box.
     */
    private void updateFilter() {

        String filterText = getContents().getFilterBox().getValue();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(filterText)) {
            removeStringFilter();
        } else {
            removeStringFilter();
            try {
                FilterData filterData = getFilterData(m_currentCms, m_root, m_resourceFilter, filterText);
                List<CmsResource> openFolders = new ArrayList<>(filterData.getOpenFolders());
                // sort open folders by path so parents are opened before children
                openFolders.sort(Comparator.comparing(res -> res.getRootPath()));
                for (CmsResource resource : openFolders) {
                    m_fileTree.expandItem(resource.getStructureId());
                }
                m_currentFilter = new Container.Filter() {

                    private Map<CmsUUID, Boolean> m_cache = new HashMap<>();

                    @Override
                    public boolean appliesToProperty(Object propertyId) {

                        return false;
                    }

                    @Override
                    public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {

                        return m_cache.computeIfAbsent((CmsUUID)itemId, id -> {

                            CmsResource resource = (CmsResource)(item.getItemProperty(
                                CmsResourceTreeContainer.PROPERTY_RESOURCE).getValue());
                            for (String matchPath : filterData.getMatchedPaths()) {
                                if (CmsStringUtil.isPrefixPath(matchPath, resource.getRootPath())
                                    || CmsStringUtil.isPrefixPath(resource.getRootPath(), matchPath)) {
                                    return true;
                                }
                            }
                            return false;
                        });
                    }
                };
                m_treeData.addContainerFilter(m_currentFilter);
            } catch (Exception e) {
                CmsErrorDialog.showErrorDialog(e);
            }
        }
    }

}
