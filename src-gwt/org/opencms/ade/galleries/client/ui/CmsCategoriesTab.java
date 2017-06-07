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

import org.opencms.ade.galleries.client.CmsCategoriesTabHandler;
import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.category.CmsDataValue;
import org.opencms.gwt.client.ui.tree.CmsTree;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.shared.CmsCategoryBean;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.ui.Label;

/**
 * Provides the widget for the categories tab.<p>
 *
 * It displays the available categories in the given sort order.
 *
 * @since 8.0.
 */
public class CmsCategoriesTab extends A_CmsListTab {

    /**
     * Handles the change of the item selection.<p>
     */
    private class SelectionHandler extends A_SelectionHandler {

        /** The category path as id for the selected category. */
        private String m_categoryPath;

        /**
         * Constructor.<p>
         *
         * @param categoryPath as id for the selected category
         * @param checkBox the reference to the checkbox
         */
        public SelectionHandler(String categoryPath, CmsCheckBox checkBox) {

            super(checkBox);
            m_categoryPath = categoryPath;
        }

        /**
         * @see org.opencms.ade.galleries.client.ui.A_CmsListTab.A_SelectionHandler#onSelectionChange()
         */
        @Override
        protected void onSelectionChange() {

            if (getCheckBox().isChecked()) {
                getTabHandler().onSelectCategory(m_categoryPath);
            } else {
                getTabHandler().onDeselectCategory(m_categoryPath);
            }

        }
    }

    /** The category icon CSS classes. */
    private static final String CATEGORY_ICON_CLASSES = CmsIconUtil.getResourceIconClasses("category", true);

    /** Map of the categories by path. */
    private Map<String, CmsCategoryBean> m_categories;

    /** The flag to indicate when the categories are opened for the fist time. */
    private boolean m_isInitOpen;

    /** The tab handler. */
    private CmsCategoriesTabHandler m_tabHandler;

    /**
     * Constructor.<p>
     *
     * @param tabHandler the tab handler
     */
    public CmsCategoriesTab(CmsCategoriesTabHandler tabHandler) {

        super(GalleryTabId.cms_tab_categories);
        m_tabHandler = tabHandler;
        m_isInitOpen = false;
        init();
    }

    /**
     * Fill the content of the categories tab panel.<p>
     *
     * @param categoryRoot the category tree root entry
     * @param selected the selected categories
     */
    public void fillContent(List<CmsCategoryTreeEntry> categoryRoot, List<String> selected) {

        setInitOpen(true);

        updateContentTree(categoryRoot, selected);
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getParamPanels(org.opencms.ade.galleries.shared.CmsGallerySearchBean)
     */
    @Override
    public List<CmsSearchParamPanel> getParamPanels(CmsGallerySearchBean searchObj) {

        List<CmsSearchParamPanel> result = new ArrayList<CmsSearchParamPanel>();
        for (String categoryPath : searchObj.getCategories()) {
            CmsCategoryBean categoryItem = m_categories.get(categoryPath);
            String title = "";
            if (categoryItem != null) {
                title = categoryItem.getTitle();
            }
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                title = categoryPath;
            }
            CmsSearchParamPanel panel = new CmsSearchParamPanel(
                Messages.get().key(Messages.GUI_PARAMS_LABEL_CATEGORIES_0),
                this);
            panel.setContent(title, categoryPath);
            result.add(panel);
        }
        return result;
    }

    /**
     * Returns the isInitOpen.<p>
     *
     * @return the isInitOpen
     */
    public boolean isInitOpen() {

        return m_isInitOpen;
    }

    /**
     * Opens the first level in the categories tree.<p>
     */
    public void openFirstLevel() {

        if (!m_categories.isEmpty()) {
            for (int i = 0; i < m_scrollList.getWidgetCount(); i++) {
                CmsTreeItem item = (CmsTreeItem)m_scrollList.getItem(i);
                item.setOpen(true);
            }
        }
    }

    /**
     * Sets the isInitOpen.<p>
     *
     * @param isInitOpen the isInitOpen to set
     */
    public void setInitOpen(boolean isInitOpen) {

        m_isInitOpen = isInitOpen;
    }

    /**
     * Deselect the categories  in the category list.<p>
     *
     * @param categories the categories to deselect
     */
    public void uncheckCategories(List<String> categories) {

        for (String category : categories) {
            CmsTreeItem item = searchTreeItem(m_scrollList, category);
            item.getCheckBox().setChecked(false);
        }
    }

    /**
     * Updates the content of the categories list.<p>
     *
     * @param categoriesBeans the updates list of categories tree item beans
     * @param selectedCategories the categories to select in the list by update
     */
    public void updateContentList(List<CmsCategoryBean> categoriesBeans, List<String> selectedCategories) {

        clearList();
        if (m_categories == null) {
            m_categories = new HashMap<String, CmsCategoryBean>();
        }
        if ((categoriesBeans != null) && !categoriesBeans.isEmpty()) {
            for (CmsCategoryBean categoryBean : categoriesBeans) {
                m_categories.put(categoryBean.getPath(), categoryBean);
                // set the list item widget
                CmsDataValue dataValue = new CmsDataValue(
                    600,
                    3,
                    CATEGORY_ICON_CLASSES,
                    categoryBean.getTitle(),
                    CmsStringUtil.isNotEmptyOrWhitespaceOnly(categoryBean.getDescription())
                    ? categoryBean.getDescription()
                    : categoryBean.getPath());
                // the checkbox
                CmsCheckBox checkBox = new CmsCheckBox();
                if ((selectedCategories != null) && selectedCategories.contains(categoryBean.getPath())) {
                    checkBox.setChecked(true);
                }
                SelectionHandler selectionHandler = new SelectionHandler(categoryBean.getPath(), checkBox);
                checkBox.addClickHandler(selectionHandler);
                dataValue.addClickHandler(selectionHandler);
                dataValue.setUnselectable();
                // set the category list item and add to list
                CmsTreeItem listItem = new CmsTreeItem(false, checkBox, dataValue);
                listItem.setSmallView(true);
                listItem.setId(categoryBean.getPath());
                addWidgetToList(listItem);
            }
        } else {
            showIsEmptyLabel();
        }
    }

    /**
     * Updates the content of th categories tree.<p>
     *
     * @param treeEntries the root category entry
     * @param selectedCategories the categories to select after update
     */
    @SuppressWarnings("unchecked")
    public void updateContentTree(List<CmsCategoryTreeEntry> treeEntries, List<String> selectedCategories) {

        clearList();
        if (m_categories == null) {
            m_categories = new HashMap<String, CmsCategoryBean>();
        }
        if ((treeEntries != null) && !treeEntries.isEmpty()) {
            // add the first level and children
            for (CmsCategoryTreeEntry category : treeEntries) {
                // set the category tree item and add to list
                CmsTreeItem treeItem = buildTreeItem(category, selectedCategories);
                treeItem.setTree((CmsTree<CmsTreeItem>)m_scrollList);
                addChildren(treeItem, category.getChildren(), selectedCategories);
                addWidgetToList(treeItem);
                treeItem.setOpen(true, false);
            }
        } else {
            showIsEmptyLabel();
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#createScrollList()
     */
    @Override
    protected CmsList<? extends I_CmsListItem> createScrollList() {

        CmsTree<CmsTreeItem> tree = new CmsTree<CmsTreeItem>();
        tree.addOpenHandler(new OpenHandler<CmsTreeItem>() {

            public void onOpen(OpenEvent<CmsTreeItem> event) {

                onContentChange();
            }
        });
        return tree;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected LinkedHashMap<String, String> getSortList() {

        LinkedHashMap<String, String> list = new LinkedHashMap<String, String>();
        list.put(SortParams.tree.name(), Messages.get().key(Messages.GUI_SORT_LABEL_HIERARCHIC_0));
        list.put(SortParams.title_asc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_TITLE_ASC_0));
        list.put(SortParams.title_desc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_TITLE_DECS_0));

        return list;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getTabHandler()
     */
    @Override
    protected CmsCategoriesTabHandler getTabHandler() {

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
     * Adds children item to the category tree and select the categories.<p>
     *
     * @param parent the parent item
     * @param children the list of children
     * @param selectedCategories the list of categories to select
     */
    private void addChildren(CmsTreeItem parent, List<CmsCategoryTreeEntry> children, List<String> selectedCategories) {

        if (children != null) {
            for (CmsCategoryTreeEntry child : children) {
                // set the category tree item and add to parent tree item
                CmsTreeItem treeItem = buildTreeItem(child, selectedCategories);
                if ((selectedCategories != null) && selectedCategories.contains(child.getPath())) {
                    parent.setOpen(true);
                    openParents(parent);
                }
                parent.addChild(treeItem);
                addChildren(treeItem, child.getChildren(), selectedCategories);
            }
        }
    }

    /**
     * Builds a tree item for the given category.<p>
     *
     * @param category the category
     * @param selectedCategories the selected categories
     *
     * @return the tree item widget
     */
    private CmsTreeItem buildTreeItem(CmsCategoryTreeEntry category, List<String> selectedCategories) {

        m_categories.put(category.getPath(), category);
        // set the list item widget
        CmsDataValue dataValue = new CmsDataValue(
            600,
            3,
            CATEGORY_ICON_CLASSES,
            category.getTitle(),
            CmsStringUtil.isNotEmptyOrWhitespaceOnly(category.getDescription())
            ? category.getDescription()
            : category.getPath());

        // the checkbox
        CmsCheckBox checkBox = new CmsCheckBox();
        if ((selectedCategories != null) && selectedCategories.contains(category.getPath())) {
            checkBox.setChecked(true);
        }
        SelectionHandler selectionHandler = new SelectionHandler(category.getPath(), checkBox);
        checkBox.addClickHandler(selectionHandler);
        dataValue.addClickHandler(selectionHandler);
        dataValue.addButton(createSelectButton(selectionHandler));
        dataValue.setUnselectable();
        // set the category tree item and add to list
        CmsTreeItem treeItem = new CmsTreeItem(true, checkBox, dataValue);
        treeItem.setSmallView(true);
        treeItem.setId(category.getPath());
        return treeItem;
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

        Label isEmptyLabel = new Label(Messages.get().key(Messages.GUI_TAB_CATEGORIES_IS_EMPTY_0));
        CmsTreeItem treeItem = new CmsTreeItem(false, isEmptyLabel);
        treeItem.setSmallView(true);
        m_scrollList.add(treeItem);
    }
}