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
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsGalleryFolderBean;
import org.opencms.ade.galleries.shared.CmsGalleryGroup;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsGalleryTreeEntry;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsSimpleListItem;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.externallink.CmsEditExternalLinkDialog;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.client.util.CmsEmbeddedDialogHandler;
import org.opencms.gwt.client.util.CmsScrollToBottomHandler;
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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

        /** True if output should be grouped. */
        protected boolean m_useGroups;

        /**
         * Creates a new instance.<p>
         * @param folders the list of folders for which to generate list items
         * @param grouped true if the list items should be displayed in groups (this assumes the items have already been sorted correctly)
         */
        public ListItemGenerator(List<CmsGalleryFolderBean> folders, boolean grouped) {

            if (folders == null) {
                folders = new ArrayList<CmsGalleryFolderBean>();
            }

            m_beanIterator = folders.iterator();
            m_useGroups = grouped;
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
            CmsTreeItem treeItem = createTreeItem(gallery, m_selectedGalleries, false, m_useGroups);
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
                CmsGalleryGroup group = (CmsGalleryGroup)treeItem.getData();
                if ((group != null) && (group != m_lastGroup)) {
                    m_lastGroup = group;
                    CmsSimpleListItem header = new CmsSimpleListItem();
                    header.getElement().setInnerText(getGroupName(group));
                    String groupHeaderClass = I_CmsLayoutBundle.INSTANCE.galleryDialogCss().groupHeader();
                    header.addStyleName(groupHeaderClass);
                    addWidgetToList(header);
                }

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
            CmsTreeItem treeItem = createTreeItem(gallery, m_selectedGalleries, true, false);
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

    /** The labels to display for groups. */
    protected Map<CmsGalleryGroup, String> m_groupLabels = new HashMap<>();

    /** An iterator which produces new list items which should be added to the tab.<p> */
    protected Iterator<CmsTreeItem> m_itemIterator;

    /** The group of the gallery folder list item that was last rendered. */
    protected CmsGalleryGroup m_lastGroup;

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
     * @param grouped true if the items should be broken into groups
     */
    public void fillContent(List<CmsGalleryFolderBean> galleryInfos, List<String> selectedGalleries, boolean grouped) {

        clearList();
        m_lastGroup = null;
        m_selectedGalleries = selectedGalleries;
        if (!galleryInfos.isEmpty()) {
            for (CmsGalleryFolderBean galleryInfo : galleryInfos) {
                m_galleries.put(galleryInfo.getPath(), galleryInfo);
            }
            m_itemIterator = new ListItemGenerator(galleryInfos, grouped);
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
     * @param useGroups true if the galleries should be broken into groups (this assumes the galleries have already been sorted correctly)
     */
    public void updateListContent(
        List<CmsGalleryFolderBean> galleries,
        List<String> selectedGalleries,
        boolean useGroups) {

        clearList();
        fillContent(galleries, selectedGalleries, useGroups);
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
                CmsTreeItem treeItem = createTreeItem(child, selectedGalleries, true, false);
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
     * @param useGroups true if the gallery tree items should be broken into groups
     *
     * @return the tree item
     */
    protected CmsTreeItem createTreeItem(
        CmsGalleryFolderBean galleryInfo,
        List<String> selectedGalleries,
        boolean forTree,
        boolean useGroups) {

        CmsListItemWidget listItemWidget = new CmsListItemWidget(galleryInfo);
        listItemWidget.setUnselectable();
        CmsCheckBox checkBox = new CmsCheckBox();
        SelectionHandler selectionHandler = new SelectionHandler(galleryInfo.getPath(), checkBox);
        checkBox.addClickHandler(selectionHandler);
        listItemWidget.addClickHandler(selectionHandler);
        if ((selectedGalleries != null) && selectedGalleries.contains(galleryInfo.getPath())) {
            checkBox.setChecked(true);
        }

        if (galleryInfo.isEditable()) {
            String uploadAction = galleryInfo.getUploadAction();

            if (null != uploadAction) {
                CmsPushButton uploadButton = new CmsPushButton(I_CmsButton.UPLOAD_SMALL);
                uploadButton.setText(null);
                uploadButton.setTitle(Messages.get().key(Messages.GUI_GALLERY_UPLOAD_TITLE_1, galleryInfo.getPath()));
                uploadButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
                uploadButton.addClickHandler(new ClickHandler() {

                    public void onClick(ClickEvent event) {

                        // prevent event from bubbling up to surrounding widget
                        event.stopPropagation();

                        CmsRpcAction<CmsUUID> action = new CmsRpcAction<CmsUUID>() {

                            @Override
                            public void execute() {

                                start(0, true);
                                CmsCoreProvider.getVfsService().getStructureId(galleryInfo.getPath(), this);
                            }

                            @Override
                            protected void onResponse(CmsUUID result) {

                                stop(false);
                                List<CmsUUID> resultIds = new ArrayList<>();
                                resultIds.add(result);
                                CmsEmbeddedDialogHandler.openDialog(
                                    uploadAction,
                                    resultIds,
                                    id -> getTabHandler().updateIndex());
                            }

                        };
                        action.execute();

                    }
                });
                listItemWidget.addButton(uploadButton);

            } else {
                if (CmsEditExternalLinkDialog.LINK_GALLERY_RESOURCE_TYPE_NAME.equals(galleryInfo.getType())) {
                    CmsPushButton createExternalLink = createNewExternalLinkButton(galleryInfo.getPath());
                    if (createExternalLink != null) {
                        listItemWidget.addButton(createExternalLink);
                    }
                } else {
                    if (!CmsCoreProvider.get().isUploadDisabled()) {
                        String uploadPath = CmsStringUtil.joinPaths(
                            CmsCoreProvider.get().getSiteRoot(),
                            galleryInfo.getPath());
                        if (CmsCoreProvider.get().getUploadRestriction().isUploadEnabled(uploadPath)) {
                            listItemWidget.addButton(createUploadButtonForTarget(galleryInfo.getPath(), false));
                        }
                    }
                }
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
        if (useGroups) {
            treeItem.setData(galleryInfo.getGroup());
        }
        if (galleryInfo.getGroup() != null) {
            m_groupLabels.putIfAbsent(galleryInfo.getGroup(), galleryInfo.getGroupLabel());
        }
        treeItem.setId(galleryInfo.getPath());
        return treeItem;
    }

    /**
     * Gets the label to display for a group.
     *
     * @param group the gallery group
     * @return the label to display for the gallery group
     */
    protected String getGroupName(CmsGalleryGroup group) {

        if (m_groupLabels.containsKey(group)) {
            return m_groupLabels.get(group);
        } else {
            return "" + group;
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected LinkedHashMap<String, String> getSortList() {

        LinkedHashMap<String, String> list = new LinkedHashMap<String, String>();
        list.put(SortParams.grouped.name(), Messages.get().key(Messages.GUI_SORT_LABEL_GROUPED_0));
        list.put(SortParams.grouped_title.name(), Messages.get().key(Messages.GUI_SORT_LABEL_GROUPED_TITLE_0));
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
