/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsCategoriesTab.java,v $
 * Date   : $Date: 2010/05/07 13:59:19 $
 * Version: $Revision: 1.9 $
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

import org.opencms.ade.galleries.client.A_CmsTabHandler;
import org.opencms.ade.galleries.client.CmsCategoriesTabHandler;
import org.opencms.ade.galleries.shared.CmsCategoryInfoBean;
import org.opencms.ade.galleries.shared.CmsGalleryDialogBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.client.ui.CmsFloatDecoratedPanel;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPair;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Provides the widget for the categories tab.<p>
 * 
 * It displays the available categories in the given sort order.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.9 $
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

        // TODO: remove the reference to the checkbox when the event source is clicked checkBox and not the toogleButton
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
                m_tabHandler.onSelectCategory(m_categoryPath);
            } else {
                m_tabHandler.onDeselectCategory(m_categoryPath);
            }
        }
    }

    /** Text metrics key. */
    private static final String TM_CATEGORY_TAB = "CategoryTab";

    /** The reference to the handler of this tab. */
    protected CmsCategoriesTabHandler m_tabHandler;

    /** The flag to indicate when the categories are opened for the fist time. */
    private boolean m_isInitOpen;

    /**
     * Constructor.<p>
     */
    public CmsCategoriesTab() {

        super();
        m_scrollList.truncate(TM_CATEGORY_TAB, CmsGalleryDialog.DIALOG_WIDTH);

        m_isInitOpen = false;
    }

    /**
     * Fill the content of the categories tab panel.<p>
     * 
     * @param dialogBean the gallery dialog data bean containing the available categories
     */
    public void fillContent(CmsGalleryDialogBean dialogBean) {

        setInitOpen(true);

        CmsCategoryTreeEntry categoryRoot = dialogBean.getCategories();
        if (categoryRoot.getChildren() != null) {
            for (CmsCategoryTreeEntry category : categoryRoot.getChildren()) {
                // set the category tree entry bean
                CmsCategoryInfoBean categoryBean = new CmsCategoryInfoBean(
                    category.getTitle(),
                    category.getPath(),
                    null,
                    category.getPath(),
                    category.getIconResource());
                // set the list item widget
                CmsListItemWidget listItemWidget = new CmsListItemWidget(categoryBean);
                Image icon = new Image(categoryBean.getIconResource());
                icon.setStyleName(DIALOG_CSS.listIcon());
                listItemWidget.setIcon(icon);
                // the checkbox
                CmsCheckBox checkBox = new CmsCheckBox();
                checkBox.addClickHandler(new CheckboxHandler(categoryBean.getId(), checkBox));
                // set the category tree item and add to list 
                CmsCategoryTreeItem treeItem = new CmsCategoryTreeItem(true, checkBox, listItemWidget);
                treeItem.init(categoryBean.getId(), categoryBean.getTitle(), categoryBean.getSubTitle());
                addChildren(treeItem, category.getChildren());
                addWidgetToList(treeItem);
            }
        }
    }

    /**
     * Returns the panel with the content of the categories search parameter.<p>
     *  
     * @param selectedCategories the list of selected categories by the user
     * @return the panel showing the selected categories
     */
    // TODO: handle the case the selected item is not found 
    public CmsFloatDecoratedPanel getCategoriesParamsPanel(ArrayList<String> selectedCategories) {

        CmsFloatDecoratedPanel categoriesPanel = new CmsFloatDecoratedPanel();
        String panelText = "";
        if (selectedCategories.size() == 1) {
            panelText = panelText.concat("<b>").concat(Messages.get().key(Messages.GUI_PARAMS_LABEL_CATEGORY_0)).concat(
                "</b> ");
            CmsCategoryTreeItem categoryItem = searchCategoryItem(m_scrollList, selectedCategories.get(0));
            String title = categoryItem.getItemTitle();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                title = categoryItem.getSubTitle();
            }
            panelText = panelText.concat(" ").concat(title);
        } else {
            panelText = panelText.concat("<b>").concat(Messages.get().key(Messages.GUI_PARAMS_LABEL_CATEGORIES_0)).concat(
                "</b> ");
            for (String categoryPath : selectedCategories) {
                CmsCategoryTreeItem categoryItem = searchCategoryItem(m_scrollList, categoryPath);
                String title = categoryItem.getItemTitle();
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                    title = categoryItem.getSubTitle();
                }
                panelText = panelText.concat(" ").concat(title);
            }
        }
        categoriesPanel.add(new HTMLPanel(CmsDomUtil.Tag.div.name(), panelText));

        return categoriesPanel;
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
     * Sets the tab handler.<p>
     *
     * @param tabHandler the tab handler to set
     */
    public void setTabHandler(CmsCategoriesTabHandler tabHandler) {

        m_tabHandler = tabHandler;
    }

    /**
     * Deselect the categories  in the category list.<p>
     * 
     * @param categories the categories to deselect
     */
    public void uncheckCategories(ArrayList<String> categories) {

        for (String category : categories) {
            CmsCategoryTreeItem item = searchCategoryItem(m_scrollList, category);
            item.getCheckbox().setChecked(false);
        }
    }

    /**
     * Updates the content of the categories list.<p>
     * 
     * @param categoriesBeans the updates list of categories tree item beans
     * @param selectedCategories the categories to select in the list by update
     */
    public void updateContent(ArrayList<CmsCategoryInfoBean> categoriesBeans, ArrayList<String> selectedCategories) {

        clearList();
        for (CmsCategoryInfoBean categoryBean : categoriesBeans) {
            // set the list item widget
            CmsListItemWidget listItemWidget = new CmsListItemWidget(categoryBean);
            Image icon = new Image(categoryBean.getIconResource());
            icon.setStyleName(DIALOG_CSS.listIcon());
            listItemWidget.setIcon(icon);
            // the checkbox
            CmsCheckBox checkBox = new CmsCheckBox();
            if (selectedCategories.contains(categoryBean.getId())) {
                checkBox.setChecked(true);
            }
            checkBox.addClickHandler(new CheckboxHandler(categoryBean.getId(), checkBox));
            // set the category list item and add to list 
            CmsCategoryTreeItem listItem = new CmsCategoryTreeItem(false, checkBox, listItemWidget);
            listItem.init(categoryBean.getId(), categoryBean.getTitle(), categoryBean.getSubTitle());
            addWidgetToList(listItem);
        }
    }

    /**
     * Updates the content of th categories tree.<p>
     * 
     * @param treeEntry the root category entry
     * @param selectedCategories the categories to select after update
     */
    public void updateContent(CmsCategoryTreeEntry treeEntry, ArrayList<String> selectedCategories) {

        clearList();
        if (treeEntry.getChildren() != null) {
            // add the first level and children
            for (CmsCategoryTreeEntry category : treeEntry.getChildren()) {
                // set the category tree entry bean
                CmsCategoryInfoBean categoryBean = new CmsCategoryInfoBean(
                    category.getTitle(),
                    category.getPath(),
                    null,
                    category.getPath(),
                    category.getIconResource());

                // set the list item widget
                CmsListItemWidget listItemWidget = new CmsListItemWidget(categoryBean);
                Image icon = new Image(categoryBean.getIconResource());
                icon.setStyleName(DIALOG_CSS.listIcon());
                listItemWidget.setIcon(icon);
                // the checkbox
                CmsCheckBox checkBox = new CmsCheckBox();
                if (selectedCategories.contains(categoryBean.getId())) {
                    checkBox.setChecked(true);
                }
                checkBox.addClickHandler(new CheckboxHandler(categoryBean.getId(), checkBox));

                // set the category tree item and add to list 
                CmsCategoryTreeItem treeItem = new CmsCategoryTreeItem(true, checkBox, listItemWidget);
                treeItem.init(categoryBean.getId(), categoryBean.getTitle(), categoryBean.getSubTitle());
                addChildren(treeItem, category.getChildren(), selectedCategories);
                addWidgetToList(treeItem);
                treeItem.setOpen(true);
            }
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected ArrayList<CmsPair<String, String>> getSortList() {

        ArrayList<CmsPair<String, String>> list = new ArrayList<CmsPair<String, String>>();
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
    protected A_CmsTabHandler getTabHandler() {

        // TODO: Auto-generated method stub
        return m_tabHandler;
    }

    /**
     * Adds children item to the category tree.<p>
     * 
     * @param parent the parent item 
     * @param children the list of children
     */
    private void addChildren(CmsCategoryTreeItem parent, List<CmsCategoryTreeEntry> children) {

        if (children != null) {
            for (CmsCategoryTreeEntry child : children) {
                // set the category tree entry bean
                CmsCategoryInfoBean childBean = new CmsCategoryInfoBean(
                    child.getTitle(),
                    child.getPath(),
                    null,
                    child.getPath(),
                    child.getIconResource());
                // set the list item widget
                CmsListItemWidget childListItemWidget = new CmsListItemWidget(childBean);
                Image icon = new Image(childBean.getIconResource());
                icon.setStyleName(DIALOG_CSS.listIcon());
                childListItemWidget.setIcon(icon);
                // the checkbox
                CmsCheckBox checkBox = new CmsCheckBox();
                checkBox.addClickHandler(new CheckboxHandler(childBean.getId(), checkBox));
                // set the category tree item and add to parent tree item
                CmsCategoryTreeItem treeItem = new CmsCategoryTreeItem(true, checkBox, childListItemWidget);
                treeItem.init(childBean.getId(), childBean.getTitle(), childBean.getSubTitle());
                parent.addChild(treeItem);
                addChildren(treeItem, child.getChildren());
            }
        }
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
        ArrayList<String> selectedCategories) {

        if (children != null) {
            for (CmsCategoryTreeEntry child : children) {
                // set the category tree entry bean
                CmsCategoryInfoBean childBean = new CmsCategoryInfoBean(
                    child.getTitle(),
                    child.getPath(),
                    null,
                    child.getPath(),
                    child.getIconResource());
                // set the list item widget
                CmsListItemWidget childListItemWidget = new CmsListItemWidget(childBean);
                Image icon = new Image(childBean.getIconResource());
                icon.setStyleName(DIALOG_CSS.listIcon());
                childListItemWidget.setIcon(icon);
                // the checkbox
                CmsCheckBox checkBox = new CmsCheckBox();
                if (selectedCategories.contains(childBean.getId())) {
                    checkBox.setChecked(true);
                }
                checkBox.addClickHandler(new CheckboxHandler(childBean.getId(), checkBox));
                // set the category tree item and add to parent tree item
                CmsCategoryTreeItem treeItem = new CmsCategoryTreeItem(true, checkBox, childListItemWidget);
                treeItem.init(childBean.getId(), childBean.getTitle(), childBean.getSubTitle());
                if (selectedCategories.contains(childBean.getId())) {
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