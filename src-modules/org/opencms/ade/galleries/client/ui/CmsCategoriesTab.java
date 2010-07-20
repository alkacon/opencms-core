/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsCategoriesTab.java,v $
 * Date   : $Date: 2010/07/20 10:28:08 $
 * Version: $Revision: 1.17 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.ade.galleries.shared.CmsCategoryBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Provides the widget for the categories tab.<p>
 * 
 * It displays the available categories in the given sort order.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.17 $
 * 
 * @since 8.0.
 */
public class CmsCategoriesTab extends A_CmsListTab {

    /** 
     * Extended ClickHandler class to use with checkboxes in the category list.<p>
     *  
     * The checkbox handler saves the id of category item, which was selected.  
     */
    private class CheckboxHandler implements ClickHandler {

        /** The category path as id for the selected category. */
        private String m_categoryPath;

        /** The reference to the checkbox. */
        private CmsCheckBox m_checkBox;

        /**
         * Constructor.<p>
         * 
         * @param categoryPath as id for the selected category
         * @param checkBox the reference to the checkbox
         */
        public CheckboxHandler(String categoryPath, CmsCheckBox checkBox) {

            m_categoryPath = categoryPath;
            m_checkBox = checkBox;
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            if (m_checkBox.isChecked()) {
                getTabHandler().onSelectCategory(m_categoryPath);
            } else {
                getTabHandler().onDeselectCategory(m_categoryPath);
            }
        }
    }

    /** The category icon CSS classes. */
    private static final String CATEGORY_ICON_CLASSES = CmsIconUtil.getResourceIconClasses("folder", false);

    /** Text metrics key. */
    private static final String TM_CATEGORY_TAB = "CategoryTab";

    /** The flag to indicate when the categories are opened for the fist time. */
    private boolean m_isInitOpen;

    /** The search parameter panel for this tab. */
    private CmsSearchParamPanel m_paramPanel;

    /** The tab handler. */
    private CmsCategoriesTabHandler m_tabHandler;

    /**
     * Constructor.<p>
     * 
     * @param tabHandler the tab handler 
     */
    public CmsCategoriesTab(CmsCategoriesTabHandler tabHandler) {

        super(GalleryTabId.cms_tab_categories);
        m_scrollList.truncate(TM_CATEGORY_TAB, CmsGalleryDialog.DIALOG_WIDTH);
        m_tabHandler = tabHandler;
        m_isInitOpen = false;
    }

    /**
     * Fill the content of the categories tab panel.<p>
     * 
     * @param categoryRoot the category tree root entry 
     */
    public void fillContent(CmsCategoryTreeEntry categoryRoot) {

        setInitOpen(true);

        updateContent(categoryRoot, null);
    }

    /**
     * Returns the content of the categories search parameter.<p>
     *  
     * @param selectedCategories the list of selected categories by the user
     * 
     * @return the selected categories
     */
    public String getCategoriesParams(List<String> selectedCategories) {

        if ((selectedCategories == null) || (selectedCategories.size() == 0)) {
            return null;
        }
        StringBuffer result = new StringBuffer(128);
        for (String categoryPath : selectedCategories) {
            CmsCategoryTreeItem categoryItem = searchCategoryItem(m_scrollList, categoryPath);
            String title = categoryItem.getItemTitle();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                title = categoryItem.getSubTitle();
            }
            result.append(title).append(", ");
        }
        result.delete(result.length() - 2, result.length());

        return result.toString();
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getParamPanel(org.opencms.ade.galleries.shared.CmsGallerySearchBean)
     */
    @Override
    public CmsSearchParamPanel getParamPanel(CmsGallerySearchBean searchObj) {

        if (m_paramPanel == null) {
            m_paramPanel = new CmsSearchParamPanel(Messages.get().key(Messages.GUI_PARAMS_LABEL_CATEGORIES_0), this);
        }
        String content = getCategoriesParams(searchObj.getCategories());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(content)) {
            m_paramPanel.setContent(content);
            return m_paramPanel;
        }
        return null;
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

        for (int i = 0; i < m_scrollList.getWidgetCount(); i++) {
            CmsCategoryTreeItem item = (CmsCategoryTreeItem)m_scrollList.getItem(i);
            item.setOpen(true);
        }
    }

    /**
     * Searches in the categories tree or list the item and returns it.<p>
     * 
     * @param list the list of items to start from
     * @param categoryPath the category id to search
     * @return the category item widget
     */
    public CmsCategoryTreeItem searchCategoryItem(CmsList<? extends I_CmsListItem> list, String categoryPath) {

        CmsCategoryTreeItem resultItem = (CmsCategoryTreeItem)list.getItem(categoryPath);
        // item is not in this tree level
        if (resultItem == null) {
            // if list is not empty
            for (int i = 0; i < list.getWidgetCount(); i++) {
                CmsCategoryTreeItem listItem = (CmsCategoryTreeItem)list.getWidget(i);
                if (listItem.getChildCount() == 0) {
                    continue;
                }
                // continue search in children
                resultItem = searchCategoryItem(listItem.getChildren(), categoryPath);
                // break the search if result item is found
                if (resultItem != null) {
                    break;
                }
            }
        }
        return resultItem;
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
            CmsCategoryTreeItem item = searchCategoryItem(m_scrollList, category);
            item.getCheckBox().setChecked(false);
        }
    }

    /**
     * Updates the content of th categories tree.<p>
     * 
     * @param treeEntry the root category entry
     * @param selectedCategories the categories to select after update
     */
    public void updateContent(CmsCategoryTreeEntry treeEntry, List<String> selectedCategories) {

        clearList();
        if (treeEntry.getChildren() != null) {
            // add the first level and children
            for (CmsCategoryTreeEntry category : treeEntry.getChildren()) {
                // set the category tree entry bean
                CmsListInfoBean categoryBean = new CmsListInfoBean(category.getTitle(), category.getDescription(), null);

                // set the list item widget
                CmsListItemWidget listItemWidget = new CmsListItemWidget(categoryBean);
                listItemWidget.setIcon(CATEGORY_ICON_CLASSES);
                // the checkbox
                CmsCheckBox checkBox = new CmsCheckBox();
                if ((selectedCategories != null) && selectedCategories.contains(category.getPath())) {
                    checkBox.setChecked(true);
                }
                checkBox.addClickHandler(new CheckboxHandler(category.getPath(), checkBox));

                // set the category tree item and add to list 
                CmsCategoryTreeItem treeItem = new CmsCategoryTreeItem(true, checkBox, listItemWidget);
                treeItem.init(category.getPath(), categoryBean.getTitle(), categoryBean.getSubTitle());
                addChildren(treeItem, category.getChildren(), selectedCategories);
                addWidgetToList(treeItem);
                treeItem.setOpen(true);
            }
        }
    }

    /**
     * Updates the content of the categories list.<p>
     * 
     * @param categoriesBeans the updates list of categories tree item beans
     * @param selectedCategories the categories to select in the list by update
     */
    public void updateContent(List<CmsCategoryBean> categoriesBeans, List<String> selectedCategories) {

        clearList();
        for (CmsCategoryBean categoryBean : categoriesBeans) {
            // set the list item widget
            CmsListItemWidget listItemWidget = new CmsListItemWidget(new CmsListInfoBean(
                categoryBean.getTitle(),
                categoryBean.getDescription(),
                null));
            listItemWidget.setIcon(CATEGORY_ICON_CLASSES);
            // the checkbox
            CmsCheckBox checkBox = new CmsCheckBox();
            if ((selectedCategories != null) && selectedCategories.contains(categoryBean.getPath())) {
                checkBox.setChecked(true);
            }
            checkBox.addClickHandler(new CheckboxHandler(categoryBean.getPath(), checkBox));
            // set the category list item and add to list 
            CmsCategoryTreeItem listItem = new CmsCategoryTreeItem(false, checkBox, listItemWidget);
            listItem.init(categoryBean.getPath(), categoryBean.getTitle(), categoryBean.getDescription());
            addWidgetToList(listItem);
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected List<CmsPair<String, String>> getSortList() {

        List<CmsPair<String, String>> list = new ArrayList<CmsPair<String, String>>();
        list.add(new CmsPair<String, String>(SortParams.tree.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_HIERARCHIC_0)));
        list.add(new CmsPair<String, String>(SortParams.title_asc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TITLE_ASC_0)));
        list.add(new CmsPair<String, String>(SortParams.title_desc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TITLE_DECS_0)));

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
     * Adds children item to the category tree and select the categories.<p>
     * 
     * @param parent the parent item 
     * @param children the list of children
     * @param selectedCategories the list of categories to select
     */
    private void addChildren(
        CmsCategoryTreeItem parent,
        List<CmsCategoryTreeEntry> children,
        List<String> selectedCategories) {

        if (children != null) {
            for (CmsCategoryTreeEntry child : children) {
                // set the category tree entry bean
                CmsListInfoBean childBean = new CmsListInfoBean(child.getTitle(), child.getDescription(), null);
                // set the list item widget
                CmsListItemWidget childListItemWidget = new CmsListItemWidget(childBean);
                childListItemWidget.setIcon(CATEGORY_ICON_CLASSES);
                // the checkbox
                CmsCheckBox checkBox = new CmsCheckBox();
                if ((selectedCategories != null) && selectedCategories.contains(child.getPath())) {
                    checkBox.setChecked(true);
                }
                checkBox.addClickHandler(new CheckboxHandler(child.getPath(), checkBox));
                // set the category tree item and add to parent tree item
                CmsCategoryTreeItem treeItem = new CmsCategoryTreeItem(true, checkBox, childListItemWidget);
                treeItem.init(child.getPath(), childBean.getTitle(), childBean.getSubTitle());
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
     * Goes up the tree and opens the parents of the item.<p>
     * 
     * @param item the child item to start from
     */
    private void openParents(CmsCategoryTreeItem item) {

        if (item != null) {
            item.setOpen(true);
            openParents((CmsCategoryTreeItem)item.getParentItem());
        }
    }
}