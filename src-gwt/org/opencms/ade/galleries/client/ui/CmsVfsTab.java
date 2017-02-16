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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.CmsVfsTabHandler;
import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsVfsEntryBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.category.CmsDataValue;
import org.opencms.gwt.client.ui.tree.A_CmsLazyOpenHandler;
import org.opencms.gwt.client.ui.tree.CmsLazyTree;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The tab widget for selecting folders from the VFS tree.<p>
 *
 * @since 8.0.0
 */
public class CmsVfsTab extends A_CmsListTab {

    /**
     * Handles the change of the item selection.<p>
     */
    private class SelectionHandler extends A_SelectionHandler {

        /** The category path as id for the selected category. */
        private CmsVfsEntryBean m_vfsEntry;

        /**
         * Constructor.<p>
         *
         * @param vfsEntry the vfs entry represented by the list item
         * @param checkBox the reference to the checkbox
         */
        public SelectionHandler(CmsVfsEntryBean vfsEntry, CmsCheckBox checkBox) {

            super(checkBox);
            m_vfsEntry = vfsEntry;
            m_selectionHandlers.add(this);
        }

        /**
         * @see org.opencms.ade.galleries.client.ui.A_CmsListTab.A_SelectionHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        @Override
        public void onClick(ClickEvent event) {

            if (isIncludeFiles()) {
                super.onClick(event);
            } else if (getTabHandler().hasSelectResource()) {
                String selectPath = m_tabHandler.getSelectPath(m_vfsEntry);
                getTabHandler().selectResource(
                    selectPath,
                    m_vfsEntry.getStructureId(),
                    m_vfsEntry.getDisplayName(),
                    I_CmsGalleryProviderConstants.RESOURCE_TYPE_FOLDER);
            }
        }

        /**
         * @see org.opencms.ade.galleries.client.ui.A_CmsListTab.A_SelectionHandler#onSelectionChange()
         */
        @Override
        protected void onSelectionChange() {

            if (isIncludeFiles()) {
                getTabHandler().onSelectFolder(m_vfsEntry.getRootPath(), getCheckBox().isChecked());
            }
        }

        /**
         * @see org.opencms.ade.galleries.client.ui.A_CmsListTab.A_SelectionHandler#selectBeforeGoingToResultTab()
         */
        @Override
        protected void selectBeforeGoingToResultTab() {

            for (SelectionHandler otherHandler : m_selectionHandlers) {
                if ((otherHandler != this)
                    && (otherHandler.getCheckBox() != null)
                    && otherHandler.getCheckBox().isChecked()) {
                    otherHandler.getCheckBox().setChecked(false);
                    otherHandler.onSelectionChange();
                }
            }
            getCheckBox().setChecked(true);
            onSelectionChange();
        }
    }

    /** The tab handler. */
    protected CmsVfsTabHandler m_tabHandler;

    /** The selection handlers for the current tab. */
    List<SelectionHandler> m_selectionHandlers = new ArrayList<SelectionHandler>();

    /** Flag indicating files are included. */
    private boolean m_includeFiles;

    /** Flag which indicates whether the tab has been initialized. */
    private boolean m_initialized;

    /** A map of tree items indexed by VFS path. */
    private Map<String, CmsLazyTreeItem> m_itemsByPath = new HashMap<String, CmsLazyTreeItem>();

    /** The list of tree items. */
    private List<CmsLazyTreeItem> m_treeItems = new ArrayList<CmsLazyTreeItem>();

    /**
     * Constructor.<p>
     *
     * @param tabHandler the tab handler
     * @param includeFiles the include files flag
     */
    public CmsVfsTab(CmsVfsTabHandler tabHandler, boolean includeFiles) {

        super(GalleryTabId.cms_tab_vfstree);
        m_tabHandler = tabHandler;
        m_includeFiles = includeFiles;
        init();
    }

    /**
     * Checks the check boxes for the selected folders.<p>
     *
     * @param folders the folders for which to check the check boxes
     */
    public void checkFolders(Set<String> folders) {

        if (folders != null) {
            for (String folder : folders) {
                CmsLazyTreeItem item = m_itemsByPath.get(folder);
                if (item != null) {
                    item.getCheckBox().setChecked(true);
                }
            }
        }

    }

    /**
     * Sets the initial folders in the VFS tab.<p>
     *
     * @param entries the root folders to display
     */
    public void fillInitially(List<CmsVfsEntryBean> entries) {

        fillInitially(entries, null);
    }

    /**
     * Sets the initial folders in the VFS tab.<p>
     *
     * @param entries the root folders to display
     * @param selectedSiteRoot site root that should be selected in the select box
     */
    public void fillInitially(List<CmsVfsEntryBean> entries, String selectedSiteRoot) {

        clear();
        for (CmsVfsEntryBean entry : entries) {
            if (entry != null) {
                CmsLazyTreeItem item = createItem(entry);
                addWidgetToList(item);
            }
        }
        if (null != selectedSiteRoot) {
            selectSite(selectedSiteRoot);
        }
        m_initialized = true;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getParamPanels(org.opencms.ade.galleries.shared.CmsGallerySearchBean)
     */
    @Override
    public List<CmsSearchParamPanel> getParamPanels(CmsGallerySearchBean searchObj) {

        List<CmsSearchParamPanel> result = new ArrayList<CmsSearchParamPanel>();
        for (String folder : searchObj.getFolders()) {
            CmsSearchParamPanel panel = new CmsSearchParamPanel(
                Messages.get().key(Messages.GUI_PARAMS_LABEL_FOLDERS_0),
                this);
            panel.setContent(folder, folder);
            result.add(panel);
        }
        return result;
    }

    /**
     * Checks if the tab is initialized.<p>
     *
     * @return true if the tab is initialized
     */
    public boolean isInitialized() {

        return m_initialized;
    }

    /**
     * This method is called when the VFS tree preload data is received.<p>
     *
     * @param vfsPreloadData the VFS tree preload data
     */
    public void onReceiveVfsPreloadData(CmsVfsEntryBean vfsPreloadData) {

        String siteRoot = vfsPreloadData.getSiteRoot();
        fillInitially(Collections.singletonList(vfsPreloadData), siteRoot);
    }

    /**
     * Un-checks the check boxes for each folder passed in the <code>folders</code> parameter.<p>
     *
     * @param folders the folders for which the check boxes should be unchecked
     */
    public void uncheckFolders(Collection<String> folders) {

        for (String folder : folders) {
            CmsLazyTreeItem item = m_itemsByPath.get(folder);
            if (item != null) {
                item.getCheckBox().setChecked(false);
            }
        }
    }

    /**
     * Clears the contents of the tab and resets the mapping from tree items to VFS beans.<p>
     */
    protected void clear() {

        clearList();
        m_selectionHandlers.clear();
        m_treeItems.clear();
    }

    /**
     * Helper method for creating a VFS tree item widget from a VFS entry bean.<p>
     *
     * @param vfsEntry the VFS entry bean
     *
     * @return the tree item widget
     */
    protected CmsLazyTreeItem createItem(final CmsVfsEntryBean vfsEntry) {

        String name = null;
        String rootPath = vfsEntry.getRootPath();
        if (rootPath.equals("/") || rootPath.equals("")) {
            name = "/";
        } else {
            name = CmsResource.getName(vfsEntry.getRootPath());

            if (name.endsWith("/")) {
                name = name.substring(0, name.length() - 1);
            }
        }
        CmsDataValue dataValue = new CmsDataValue(
            600,
            3,
            CmsIconUtil.getResourceIconClasses(I_CmsGalleryProviderConstants.RESOURCE_TYPE_FOLDER, true),
            name,
            vfsEntry.getDisplayName());
        if (vfsEntry.isSearchMatch()) {
            dataValue.setSearchMatch(true);
        }
        dataValue.setUnselectable();
        if (vfsEntry.isEditable()) {
            dataValue.addButton(createUploadButtonForTarget(vfsEntry.getRootPath(), true));
        }
        CmsLazyTreeItem result;
        SelectionHandler selectionHandler;
        CmsCheckBox checkbox = null;
        if (isIncludeFiles()) {
            checkbox = new CmsCheckBox();
            result = new CmsLazyTreeItem(checkbox, dataValue, true);
            selectionHandler = new SelectionHandler(vfsEntry, checkbox);
            checkbox.addClickHandler(selectionHandler);
            dataValue.addClickHandler(selectionHandler);
            dataValue.addButton(createSelectButton(selectionHandler));
        } else {
            result = new CmsLazyTreeItem(dataValue, true);
            selectionHandler = new SelectionHandler(vfsEntry, null);
        }
        // we need this in a final variable to access it in the click handler
        if (getTabHandler().hasSelectResource()) {
            String selectPath = m_tabHandler.getSelectPath(vfsEntry);
            dataValue.addButton(
                createSelectResourceButton(
                    selectPath,
                    vfsEntry.getStructureId(),
                    vfsEntry.getDisplayName(),
                    I_CmsGalleryProviderConstants.RESOURCE_TYPE_FOLDER));
        }
        result.setData(vfsEntry);
        m_itemsByPath.put(vfsEntry.getRootPath(), result);
        result.setLeafStyle(false);
        result.setSmallView(true);
        m_treeItems.add(result);
        if (vfsEntry.getChildren() != null) {
            for (CmsVfsEntryBean child : vfsEntry.getChildren()) {
                result.addChild(createItem(child));
            }
            result.onFinishLoading();
            result.setOpen(true, false);
            if (vfsEntry.getChildren().isEmpty()) {
                result.setLeafStyle(true);
            }
        }

        return result;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#createScrollList()
     */
    @Override
    protected CmsList<? extends I_CmsListItem> createScrollList() {

        CmsLazyTree<CmsLazyTreeItem> tree = new CmsLazyTree<CmsLazyTreeItem>(
            new A_CmsLazyOpenHandler<CmsLazyTreeItem>() {

                /**
                 * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#load(org.opencms.gwt.client.ui.tree.CmsLazyTreeItem)
                 */
                public void load(final CmsLazyTreeItem target) {

                    CmsVfsEntryBean entry = target.getData();
                    String path = entry.getRootPath();
                    AsyncCallback<List<CmsVfsEntryBean>> callback = new AsyncCallback<List<CmsVfsEntryBean>>() {

                        /**
                         * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
                         */
                        public void onFailure(Throwable caught) {

                            // should never be called

                        }

                        /**
                         * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(java.lang.Object)
                         */
                        public void onSuccess(List<CmsVfsEntryBean> result) {

                            for (CmsVfsEntryBean childEntry : result) {
                                CmsLazyTreeItem item = createItem(childEntry);
                                target.addChild(item);
                            }
                            target.onFinishLoading();
                            target.setOpen(true, false);
                            onContentChange();
                        }
                    };

                    m_tabHandler.getSubFolders(path, callback);

                }
            });
        tree.addOpenHandler(new OpenHandler<CmsLazyTreeItem>() {

            public void onOpen(OpenEvent<CmsLazyTreeItem> event) {

                Set<CmsUUID> ids = getOpenElementIds();
                CmsVfsEntryBean entry = event.getTarget().getData();
                ids.add(entry.getStructureId());
                getTabHandler().onChangeTreeState(ids);
                onContentChange();
            }

        });
        tree.addCloseHandler(new CloseHandler<CmsLazyTreeItem>() {

            public void onClose(CloseEvent<CmsLazyTreeItem> event) {

                Set<CmsUUID> ids = getOpenElementIds();
                CmsVfsEntryBean entry = event.getTarget().getData();
                ids.remove(entry.getStructureId());
                getTabHandler().onChangeTreeState(ids);
            }

        });
        return tree;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected LinkedHashMap<String, String> getSortList() {

        return m_tabHandler.getSortList();
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getTabHandler()
     */
    @Override
    protected CmsVfsTabHandler getTabHandler() {

        return m_tabHandler;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#hasQuickFilter()
     */
    @Override
    protected boolean hasQuickFilter() {

        return true;
    }

    /**
     * Returns if files are included.<p>
     *
     * @return <code>true</code> if files are included
     */
    protected boolean isIncludeFiles() {

        return m_includeFiles;
    }

    /**
     * Collects the structure ids belonging to open tree entries.<p>
     *
     * @return the structure ids for  the open tree entries
     */
    Set<CmsUUID> getOpenElementIds() {

        Set<CmsUUID> ids = new HashSet<CmsUUID>();
        for (CmsLazyTreeItem item : m_treeItems) {
            CmsVfsEntryBean entry = item.getData();
            if (item.isOpen()) {
                ids.add(entry.getStructureId());
            }
        }
        return ids;
    }

    /**
     * Selects a specific site.<p>
     *
     * @param siteRoot the site root
     */
    private void selectSite(String siteRoot) {

        if (m_sortSelectBox == null) {
            return;
        }
        Map<String, String> options = m_sortSelectBox.getItems();
        String option = null;
        for (Map.Entry<String, String> entry : options.entrySet()) {
            if (CmsStringUtil.comparePaths(entry.getKey(), siteRoot)) {
                option = entry.getKey();
                break;
            }
        }
        if (option != null) {
            m_sortSelectBox.setFormValue(option, false);
        }
    }

}
