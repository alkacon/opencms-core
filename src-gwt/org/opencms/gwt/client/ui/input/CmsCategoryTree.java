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

package org.opencms.gwt.client.ui.input;

import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.CmsSimpleListItem;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.shared.CmsCategoryBean;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Builds the category tree.<p>
 * */
public class CmsCategoryTree extends Composite {

    /**
     * Inner class for check box handler.<p>
     */
    class CmsCheckboxValueChangeHandler implements ValueChangeHandler<String> {

        /***/
        CmsCheckBox m_checkbox;

        /***/
        String m_path;

        /**
         * @param checkbox
         */
        public CmsCheckboxValueChangeHandler(CmsCheckBox checkbox, String path) {

            m_checkbox = checkbox;
            m_path = path;
        }

        /**
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<String> event) {

            if (m_checkbox.isChecked()) {
                m_categories.get(m_path);
            }
        }

    }

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    interface I_CmsCategoryTreeUiBinder extends UiBinder<Widget, CmsCategoryTree> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsCategoryTreeUiBinder uiBinder = GWT.create(I_CmsCategoryTreeUiBinder.class);

    /***/
    Map<String, CmsCategoryBean> m_categories;

    /***/
    CmsList<? extends I_CmsListItem> m_scrollList;

    /***/
    private String m_categoryFolder;

    /***/
    protected List<String> m_selectedCategories;

    /***/
    @UiField
    CmsScrollPanel m_list;

    /***/
    private boolean m_isSingleValue;

    /**
     * 
     */
    public CmsCategoryTree() {

        uiBinder.createAndBindUi(this);
        initWidget(uiBinder.createAndBindUi(this));
    }

    /**
     * Default Constructor.<p> 
     * @param selectedCategories 
     * @param height 
     * @param isSingleValue 
     * */
    public CmsCategoryTree(List<String> selectedCategories, int height, boolean isSingleValue) {

        this();
        m_isSingleValue = isSingleValue;
        m_selectedCategories = selectedCategories;
        m_scrollList = createScrollList();
        m_list.setHeight(height + "px");
        genearteList();
        m_list.add(m_scrollList);

    }

    /**
     * Constructor.<p>
     * 
     * @param categories
     * @param scrollList 
     */
    public CmsCategoryTree(Map<String, CmsCategoryBean> categories, CmsList<? extends I_CmsListItem> scrollList) {

        this();
        m_categories = categories;
        m_scrollList = scrollList;
        genearteList();

    }

    /**
     * Adds children item to the category tree and select the categories.<p>
     * 
     * @param parent the parent item 
     * @param children the list of children
     * @param selectedCategories the list of categories to select
     */
    public void addChildren(CmsTreeItem parent, List<CmsCategoryTreeEntry> children, List<String> selectedCategories) {

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
    public CmsTreeItem buildTreeItem(CmsCategoryTreeEntry category, List<String> selectedCategories) {

        m_categories.put(category.getPath(), category);
        // set the list item widget
        CmsDataValue dataValue = new CmsDataValue(
            600,
            3,
            category.getTitle(),
            CmsStringUtil.isNotEmptyOrWhitespaceOnly(category.getDescription())
            ? category.getDescription()
            : category.getPath());
        dataValue.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().categoryItem());
        // the checkbox
        CmsCheckBox checkBox = new CmsCheckBox();
        if ((selectedCategories != null) && selectedCategories.contains(category.getPath())) {
            checkBox.setChecked(true);
        }
        // SelectionHandler selectionHandler = new SelectionHandler(category.getPath(), checkBox);
        // checkBox.addClickHandler(selectionHandler);
        // dataValue.addDomHandler(selectionHandler, ClickEvent.getType());
        // dataValue.addButton(createSelectButton(selectionHandler));
        // set the category tree item and add to list 
        CmsTreeItem treeItem = new CmsTreeItem(true, checkBox, dataValue);
        treeItem.setId(category.getPath());
        return treeItem;
    }

    /**
     * Returns a list of all selected values.<p>
     * 
     * @return a list of selected values
     */
    public List<String> getAllSelected() {

        List<String> result = new ArrayList<String>();
        Iterator<Widget> it = m_scrollList.iterator();
        while (it.hasNext()) {
            collectAllSelected((CmsTreeItem)it.next(), result);

        }
        return result;
    }

    /**
     * @return
     */
    public Map<String, CmsCategoryBean> getCategories() {

        return m_categories;
    }

    /**
     * @return
     */
    public CmsScrollPanel getList() {

        return m_list;
    }

    /**
     * Goes up the tree and opens the parents of the item.<p>
     * 
     * @param item the child item to start from
     */
    public void openParents(CmsTreeItem item) {

        if (item != null) {
            item.setOpen(true);
            openParents(item.getParentItem());
        }
    }

    /**
     * Shows the tab list is empty label.<p>
     */
    public void showIsEmptyLabel() {

        CmsSimpleListItem item = new CmsSimpleListItem();
        Label isEmptyLabel = new Label(Messages.get().key(Messages.GUI_TAB_CATEGORIES_IS_EMPTY_0));
        item.add(isEmptyLabel);
        m_scrollList.add(item);
    }

    /**
     * Updates the content of the categories list.<p>
     * 
     * @param categoriesBeans the updates list of categories tree item beans
     * @param selectedCategories the categories to select in the list by update
     */
    public void updateContentList(List<CmsCategoryBean> categoriesBeans, List<String> selectedCategories) {

        m_scrollList.clearList();
        // clearList();
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
                    categoryBean.getTitle(),
                    CmsStringUtil.isNotEmptyOrWhitespaceOnly(categoryBean.getDescription())
                    ? categoryBean.getDescription()
                    : categoryBean.getPath());
                dataValue.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().categoryItem());
                //dataValue.setUnselectable();
                //CmsDataValue.setIcon(CATEGORY_ICON_CLASSES);
                // the checkbox
                CmsCheckBox checkBox = new CmsCheckBox();
                if ((selectedCategories != null) && selectedCategories.contains(categoryBean.getPath())) {
                    checkBox.setChecked(true);
                }
                //       SelectionHandler selectionHandler = new SelectionHandler(categoryBean.getPath(), checkBox);
                //       checkBox.addClickHandler(selectionHandler);
                //       dataValue.addDomHandler(selectionHandler, DoubleClickEvent.getType());
                // set the category list item and add to list 
                CmsTreeItem listItem = new CmsTreeItem(false, checkBox, dataValue);
                listItem.setId(categoryBean.getPath());
                addWidgetToList(listItem);
                CmsScrollPanel scrollparent = (CmsScrollPanel)m_scrollList.getParent();
                scrollparent.onResize();
            }
        } else {
            showIsEmptyLabel();
        }
    }

    /**
     * Updates the content of the categories tree.<p>
     * 
     * @param treeEntries the root category entry
     * @param selectedCategories the categories to select after update
     */
    public void updateContentTree(List<CmsCategoryTreeEntry> treeEntries, List<String> selectedCategories) {

        m_scrollList.clearList();
        if (m_categories == null) {
            m_categories = new HashMap<String, CmsCategoryBean>();
        }
        if ((treeEntries != null) && !treeEntries.isEmpty()) {
            // add the first level and children
            for (CmsCategoryTreeEntry category : treeEntries) {
                // set the category tree item and add to list 
                CmsTreeItem treeItem = buildTreeItem(category, selectedCategories);
                addWidgetToList(treeItem);
                addChildren(treeItem, category.getChildren(), selectedCategories);
                treeItem.setOpen(true);
            }
        } else {
            showIsEmptyLabel();
        }

    }

    /**
     * Add a list item widget to the list panel.<p>
     * 
     * @param listItem the list item to add
     */
    protected void addWidgetToList(Widget listItem) {

        m_scrollList.add(listItem);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                m_list.onResize();
            }
        });
    }

    /**
     * Creates the list which should contain the list items of the tab.<p>
     * 
     * @return the newly created list widget 
     */
    protected CmsList<? extends I_CmsListItem> createScrollList() {

        return new CmsList<I_CmsListItem>();
    }

    /**
     * @return 
     */
    protected LinkedHashMap<String, String> getSortList() {

        LinkedHashMap<String, String> list = new LinkedHashMap<String, String>();
        list.put(SortParams.tree.name(), Messages.get().key(Messages.GUI_SORT_LABEL_HIERARCHIC_0));
        list.put(SortParams.title_asc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_TITLE_ASC_0));
        list.put(SortParams.title_desc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_TITLE_DECS_0));

        return list;
    }

    /**
     * @param item 
     * @param result 
     * 
     * */
    private void collectAllSelected(CmsTreeItem item, List<String> result) {

        if (item.getCheckBox().isChecked()) {
            result.add(item.getId());

        }
        Iterator<Widget> it = item.getChildren().iterator();
        while (it.hasNext()) {
            collectAllSelected((CmsTreeItem)it.next(), result);
        }
    }

    /**
     * 
     */
    private void genearteList() {

        // generate a list of all configured categories.
        final List<String> categories = new ArrayList<String>();
        categories.add(m_categoryFolder + "/");

        // start request 
        CmsRpcAction<List<CmsCategoryTreeEntry>> action = new CmsRpcAction<List<CmsCategoryTreeEntry>>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            public void execute() {

                CmsCoreProvider.getService().getCategories("/", true, categories, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(List<CmsCategoryTreeEntry> result) {

                // copy the result to the global variable. 
                updateContentTree(result, m_selectedCategories);
            }

        };
        action.execute();
    }

}
