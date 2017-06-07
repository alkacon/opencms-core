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

import org.opencms.ade.galleries.client.CmsGalleriesTabHandler;
import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.shared.CmsGalleryFolderBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsGalleryTreeEntry;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsSimpleListItem;
import org.opencms.gwt.client.ui.externallink.CmsEditExternalLinkDialog;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.client.util.CmsScrollToBottomHandler;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;

/**
 * Provides the widget for the galleries(folder) tab.<p>
 *
 * It displays the available gallery folders in the given order.
 *
 * @since 8.0.
 */
public class CmsGalleriesTab extends A_CmsListTab {

    /**
     * A class which generates list items incrementally to fill the galleries tab.<p>
     */
    protected class ListItemGenerator implements Iterator<CmsTreeItem> {

        /** The internal iterator over the gallery beans. */
        protected Iterator<CmsGalleryFolderBean> m_beanIterator;

        /**
         * Creates a new instance.<p>
         * @param folders the list of folders for which to generate list items
         */
        public ListItemGenerator(List<CmsGalleryFolderBean> folders) {

            if (folders == null) {
                folders = new ArrayList<CmsGalleryFolderBean>();
            }

            m_beanIterator = folders.iterator();
        }

        /**
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {

            return m_beanIterator.hasNext();
        }

        /**
         * @see java.util.Iterator#next()
         */
        public CmsTreeItem next() {

            CmsGalleryFolderBean gallery = m_beanIterator.next();
            CmsTreeItem treeItem = createTreeItem(gallery, m_selectedGalleries, false);
            return treeItem;
        }

        /**
         * @see java.util.Iterator#remove()
         */
        public void remove() {

            throw new UnsupportedOperationException();
        }
    }

    /**
     * Command for adding more list items to the list of publish items.<p>
     */
    protected class MoreItemsCommand implements RepeatingCommand {

        /** The number of items left to add. */
        private int m_numItems;

        /**
         * Creates a new instance.<p>
         *
         * @param numItems the maximal number of items to add
         */
        public MoreItemsCommand(int numItems) {

            m_numItems = numItems;
        }

        /**
         * @see com.google.gwt.core.client.Scheduler.RepeatingCommand#execute()
         */
        public boolean execute() {

            if (m_numItems == 0) {
                setLoading(false);
                onContentChange();
                return false;
            }
            boolean hasMore = m_itemIterator.hasNext();
            if (!hasMore) {
                setLoading(false);
                onContentChange();
                return false;
            } else {
                CmsTreeItem treeItem = m_itemIterator.next();
                addWidgetToList(treeItem);
            }
            m_numItems -= 1;
            return true;
        }

    }

    /**
     * A class which generates tree items incrementally to fill the galleries tab.<p>
     */
    protected class TreeItemGenerator implements Iterator<CmsTreeItem> {

        /** The internal iterator over the gallery beans. <p> */
        protected Iterator<CmsGalleryTreeEntry> m_beanIterator;

        /**
         * Creates a new instance.<p>
         *
         * @param folders the folders from which to generate list items
         */
        public TreeItemGenerator(List<CmsGalleryTreeEntry> folders) {

            if (folders == null) {
                folders = new ArrayList<CmsGalleryTreeEntry>();
            }

            m_beanIterator = folders.iterator();
        }

        /**
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {

            return m_beanIterator.hasNext();
        }

        /**
         * @see java.util.Iterator#next()
         */
        public CmsTreeItem next() {

            CmsGalleryTreeEntry gallery = m_beanIterator.next();
            CmsTreeItem treeItem = createTreeItem(gallery, m_selectedGalleries, true);
            addChildren(treeItem, gallery.getChildren(), m_selectedGalleries);
            treeItem.setOpen(true);
            return treeItem;
        }

        /**
         * @see java.util.Iterator#remove()
         */
        public void remove() {

            throw new UnsupportedOperationException();
        }
    }

    /**
     * Handles the change of the item selection.<p>
     */
    private class SelectionHandler extends A_SelectionHandler {

        /** The gallery path as id for the selected gallery. */
        private String m_galleryPath;

        /**
         * Constructor.<p>
         *
         * @param gallerPath as id for the selected category
         * @param checkBox the reference to the checkbox
         */
        public SelectionHandler(String gallerPath, CmsCheckBox checkBox) {

            super(checkBox);
            m_galleryPath = gallerPath;
            m_selectionHandlers.add(this);
        }

        /**
         * @see org.opencms.ade.galleries.client.ui.A_CmsListTab.A_SelectionHandler#onSelectionChange()
         */
        @Override
        protected void onSelectionChange() {

            if (getCheckBox().isChecked()) {
                getTabHandler().onSelectGallery(m_galleryPath);
            } else {
                getTabHandler().onDeselectGallery(m_galleryPath);
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

    /** The batch size for adding new elements to the tab.<p> */
    protected static final int LOAD_BATCH_SIZE = 50;

    /** An iterator which produces new list items which should be added to the tab.<p> */
    protected Iterator<CmsTreeItem> m_itemIterator;

    /** List of selected galleries. */
    protected List<String> m_selectedGalleries;

    /** The selection handlers. */
    List<SelectionHandler> m_selectionHandlers = Lists.newArrayList();

    /** Map of gallery folders by path. */
    private Map<String, CmsGalleryFolderBean> m_galleries;

    /** Flag which indicates whether new elements are currently being inserted into the galleries tab.<p> */
    private boolean m_loading;

    /** The tab handler. */
    private CmsGalleriesTabHandler m_tabHandler;

    /**
     * Constructor.<p>
     *
     * @param tabHandler the tab handler
     */
    public CmsGalleriesTab(CmsGalleriesTabHandler tabHandler) {

        super(GalleryTabId.cms_tab_galleries);
        getList().addScrollHandler(new CmsScrollToBottomHandler(new Runnable() {

            public void run() {

                if (!isLoading()) {
                    loadMoreItems();
                }
            }
        }));
        m_tabHandler = tabHandler;
        m_galleries = new HashMap<String, CmsGalleryFolderBean>();
        init();
    }

    /**
     * Fill the content of the galleries tab panel.<p>
     *
     * @param galleryInfos the gallery info beans
     * @param selectedGalleries the list of galleries to select
     */
    public void fillContent(List<CmsGalleryFolderBean> galleryInfos, List<String> selectedGalleries) {

        clearList();
        m_selectedGalleries = selectedGalleries;
        if (!galleryInfos.isEmpty()) {
            for (CmsGalleryFolderBean galleryInfo : galleryInfos) {
                m_galleries.put(galleryInfo.getPath(), galleryInfo);
            }

            m_itemIterator = new ListItemGenerator(galleryInfos);
            loadMoreItems();
        } else {
            showIsEmptyLabel();
        }
        onContentChange();
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getParamPanels(org.opencms.ade.galleries.shared.CmsGallerySearchBean)
     */
    @Override
    public List<CmsSearchParamPanel> getParamPanels(CmsGallerySearchBean searchObj) {

        List<CmsSearchParamPanel> result = new ArrayList<CmsSearchParamPanel>();
        for (String galleryPath : searchObj.getGalleries()) {
            CmsGalleryFolderBean galleryBean = m_galleries.get(galleryPath);
            if (galleryBean != null) {
                String title = galleryBean.getTitle();
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                    title = galleryBean.getPath();
                }
                CmsSearchParamPanel panel = new CmsSearchParamPanel(
                    Messages.get().key(Messages.GUI_PARAMS_LABEL_GALLERIES_0),
                    this);
                panel.setContent(title, galleryPath);
                result.add(panel);
            }
        }
        return result;
    }

    /**
     * Returns the value of the "loading" flag, which indicates whether new elements are currently being added into the galleries tab.<p>
     *
     * @return the "loading" flag
     */
    public boolean isLoading() {

        return m_loading;

    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#onSelection()
     */
    @Override
    public void onSelection() {

        super.onSelection();
        Timer timer = new Timer() {

            @Override
            public void run() {

                m_quickSearch.setFocus(true);
            }
        };
        timer.schedule(20);
    }

    /**
     * Sets the "loading" flag.<p>
     *
     * @param loading the new value of the loading flag
     */
    public void setLoading(boolean loading) {

        m_loading = loading;
    }

    /**
    * De-selects the galleries in the galleries list.<p>
    *
    * @param galleries the galleries to deselect
    */
    public void uncheckGalleries(List<String> galleries) {

        for (String gallery : galleries) {
            CmsListItem item = searchTreeItem(m_scrollList, gallery);
            if (item != null) {
                item.getCheckBox().setChecked(false);
            }
        }
    }

    /**
     * Update the galleries list.<p>
     *
     * @param galleries the new gallery list
     * @param selectedGalleries the list of galleries to select
     */
    public void updateListContent(List<CmsGalleryFolderBean> galleries, List<String> selectedGalleries) {

        clearList();
        fillContent(galleries, selectedGalleries);
    }

    /**
     * Update the galleries tree.<p>
     *
     * @param galleryTreeEntries the new gallery tree list
     * @param selectedGalleries the list of galleries to select
     */
    public void updateTreeContent(List<CmsGalleryTreeEntry> galleryTreeEntries, List<String> selectedGalleries) {

        clearList();
        m_selectedGalleries = selectedGalleries;
        if (!galleryTreeEntries.isEmpty()) {
            m_itemIterator = new TreeItemGenerator(galleryTreeEntries);
            loadMoreItems();
        } else {
            showIsEmptyLabel();
        }
        onContentChange();
    }

    /**
     * Adds children to the gallery tree and select the galleries.<p>
     *
     * @param parent the parent item
     * @param children the list of children
     * @param selectedGalleries the list of galleries to select
     */
    protected void addChildren(CmsTreeItem parent, List<CmsGalleryTreeEntry> children, List<String> selectedGalleries) {

        if (children != null) {
            for (CmsGalleryTreeEntry child : children) {
                // set the category tree item and add to parent tree item
                CmsTreeItem treeItem = createTreeItem(child, selectedGalleries, true);
                if ((selectedGalleries != null) && selectedGalleries.contains(child.getPath())) {
                    parent.setOpen(true);
                    openParents(parent);
                }
                parent.addChild(treeItem);
                addChildren(treeItem, child.getChildren(), selectedGalleries);
            }
        }
    }

    /**
     * Creates a tree item widget used in list and tree view of this tab.<p>
     *
     * @param galleryInfo the gallery folder bean
     * @param selectedGalleries the selected galleries
     * @param forTree <code>true</code> if the item is used within tree view
     *
     * @return the tree item
     */
    protected CmsTreeItem createTreeItem(
        CmsGalleryFolderBean galleryInfo,
        List<String> selectedGalleries,
        boolean forTree) {

        CmsListItemWidget listItemWidget = new CmsListItemWidget(
            new CmsListInfoBean(galleryInfo.getTitle(), galleryInfo.getPath(), null));
        listItemWidget.setIcon(CmsIconUtil.getResourceIconClasses(galleryInfo.getType(), false));
        listItemWidget.setUnselectable();
        CmsCheckBox checkBox = new CmsCheckBox();
        SelectionHandler selectionHandler = new SelectionHandler(galleryInfo.getPath(), checkBox);
        checkBox.addClickHandler(selectionHandler);
        listItemWidget.addClickHandler(selectionHandler);
        if ((selectedGalleries != null) && selectedGalleries.contains(galleryInfo.getPath())) {
            checkBox.setChecked(true);
        }

        if (galleryInfo.isEditable()) {
            if (CmsEditExternalLinkDialog.LINK_GALLERY_RESOURCE_TYPE_NAME.equals(galleryInfo.getType())) {
                CmsPushButton createExternalLink = createNewExternalLinkButton(galleryInfo.getPath());
                if (createExternalLink != null) {
                    listItemWidget.addButton(createExternalLink);
                }
            } else {
                listItemWidget.addButton(createUploadButtonForTarget(galleryInfo.getPath(), false));
            }
        }
        listItemWidget.addButton(createSelectButton(selectionHandler));
        if (m_tabHandler.hasGalleriesSelectable()) {
            CmsPushButton selectButton = createSelectResourceButton(
                galleryInfo.getPath(),
                CmsUUID.getNullUUID(),
                "",
                "");
            listItemWidget.addButton(selectButton);
        }

        CmsTreeItem treeItem = new CmsTreeItem(forTree, checkBox, listItemWidget);
        treeItem.setId(galleryInfo.getPath());
        return treeItem;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected LinkedHashMap<String, String> getSortList() {

        LinkedHashMap<String, String> list = new LinkedHashMap<String, String>();
        list.put(SortParams.title_asc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_TITLE_ASC_0));
        list.put(SortParams.title_desc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_TITLE_DECS_0));
        list.put(SortParams.type_asc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_TYPE_ASC_0));
        list.put(SortParams.type_desc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_TYPE_DESC_0));
        list.put(SortParams.tree.name(), Messages.get().key(Messages.GUI_SORT_LABEL_HIERARCHIC_0));
        return list;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getTabHandler()
     */
    @Override
    protected CmsGalleriesTabHandler getTabHandler() {

        return m_tabHandler;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#hasQuickFilter()
     */
    @Override
    protected boolean hasQuickFilter() {

        // allow filter if not in tree mode
        return SortParams.tree != SortParams.valueOf(m_sortSelectBox.getFormValueAsString());
    }

    /**
     * Adds more gallery list items to display in the tab, if available.<p>
     */
    protected void loadMoreItems() {

        setLoading(true);
        MoreItemsCommand cmd = new MoreItemsCommand(30);
        Scheduler.get().scheduleFixedDelay(cmd, 1);
    }

    /**
     * Goes up the tree and opens the parents of the item.<p>
     *
     * @param item the child item to start from
     */
    private void openParents(CmsTreeItem item) {

        if (item != null) {
            item.setOpen(true);
            openParents(item.getParentItem());
        }
    }

    /**
     * Shows the tab list is empty label.<p>
     */
    private void showIsEmptyLabel() {

        CmsSimpleListItem item = new CmsSimpleListItem();
        Label isEmptyLabel = new Label(Messages.get().key(Messages.GUI_TAB_GALLERIES_IS_EMPTY_0));
        item.add(isEmptyLabel);
        m_scrollList.add(item);
    }
}
