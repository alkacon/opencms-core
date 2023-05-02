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

package org.opencms.gwt.client.ui.input.category;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.CmsSimpleListItem;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.I_CmsTruncable;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsCategoryField;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.shared.CmsCategoryBean;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Builds the category tree.<p>
 * */
public class CmsCategoryTree extends Composite implements I_CmsTruncable, HasValueChangeHandlers<List<String>> {

    /** Sorting parameters. */
    public enum SortParams implements IsSerializable {

        /** Date last modified ascending. */
        dateLastModified_asc,

        /** Date last modified descending. */
        dateLastModified_desc,

        /** Resource path ascending sorting. */
        path_asc,

        /** Resource path descending sorting.*/
        path_desc,

        /** Title ascending sorting. */
        title_asc,

        /** Title descending sorting. */
        title_desc,

        /** Tree.*/
        tree,

        /** Resource type ascending sorting. */
        type_asc,

        /** Resource type descending sorting. */
        type_desc;
    }

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    interface I_CmsCategoryTreeUiBinder extends UiBinder<Widget, CmsCategoryTree> {
        // GWT interface, nothing to do here
    }

    /**
     * Inner class for select box handler.<p>
     */
    private class CategoryValueChangeHandler implements ValueChangeHandler<String> {

        /**
         * Default Constructor.<p>
         */
        public CategoryValueChangeHandler() {

            // nothing to do
        }

        /**
         * Will be triggered if the value in the select box changes.<p>
         *
         * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
         */
        public void onValueChange(ValueChangeEvent<String> event) {

            cancelQuickFilterTimer();
            if (event.getSource() == m_sortSelectBox) {

                List<CmsTreeItem> categories = new ArrayList<CmsTreeItem>(m_categories.values());
                SortParams sort = SortParams.valueOf(event.getValue());
                sort(categories, sort);
            }
            if ((event.getSource() == m_quickSearch)) {
                if (!m_listView) {
                    m_listView = true;
                    m_sortSelectBox.setFormValueAsString(SortParams.title_asc.name());
                }
                if (hasQuickFilter()) {

                    if ((CmsStringUtil.isEmptyOrWhitespaceOnly(event.getValue()) || (event.getValue().length() >= 2))) {
                        // only act if filter length is at least 3 characters or empty
                        scheduleQuickFilterTimer();
                    }
                } else {
                    checkQuickSearchStatus();
                }
            }
        }

    }

    /**
     * Inner class for check box handler.<p>
     */
    private class CheckBoxValueChangeHandler implements ValueChangeHandler<Boolean> {

        /** Path of the TreeItem. */
        private CmsTreeItem m_item;

        /**
         * Default constructor.<p>
         * @param item The CmsTreeItem of this check box
         */
        public CheckBoxValueChangeHandler(CmsTreeItem item) {

            m_item = item;

        }

        /**
         * Is triggered if an check box is selected or deselected.<p>
         *
         * @param event The event that is triggered
         */
        public void onValueChange(ValueChangeEvent<Boolean> event) {

            toggleSelection(m_item, false);
        }

    }

    /**
     * Inner class for check box handler.<p>
     */
    private class DataValueClickHander implements ClickHandler {

        /** The TreeItem. */
        private CmsTreeItem m_item;

        /** Constructor to set the right CmsTreeItem for this handler.<p>
         *
         * @param item the CmsTreeItem of this Handler
         */
        public DataValueClickHander(CmsTreeItem item) {

            m_item = item;
        }

        /**
         * Is triggered if the DataValue widget is clicked.<p>
         * If its check box was selected the click will deselect this box otherwise it will select it.
         *
         * @param event The event that is triggered
         * */
        public void onClick(ClickEvent event) {

            if (isEnabled()) {
                toggleSelection(m_item, true);
            }
        }

    }

    /** The filtering delay. */
    private static final int FILTER_DELAY = 100;

    /** Text metrics key. */
    private static final String TM_GALLERY_SORT = "gallerySort";

    /** The ui-binder instance for this class. */
    private static I_CmsCategoryTreeUiBinder uiBinder = GWT.create(I_CmsCategoryTreeUiBinder.class);

    /** Map of categories. */
    protected Map<String, CmsTreeItem> m_categories;

    /** Map from category paths to the paths of their children. */
    protected Map<String, List<String>> m_childrens;

    /** A label for displaying additional information about the tab. */
    protected HasText m_infoLabel;

    /** Vale to store the widget mode. True means the single selection. */
    protected boolean m_isSingleSelection;

    /** Vale to store the view mode. True means the list view. */
    protected boolean m_listView;

    /** The option panel. */
    @UiField
    protected FlowPanel m_options;

    /** The quick search box. */
    protected CmsTextBox m_quickSearch;

    /** List of categories selected from the server. */
    protected List<CmsCategoryTreeEntry> m_resultList;

    /** List of categories. */
    protected CmsList<CmsTreeItem> m_scrollList;

    /** The quick search button. */
    protected CmsPushButton m_searchButton;

    /**
     * List of all selected categories.
     *
     * <p>IMPORTANT: This may unfortunately contain either category paths or category site paths.
     *  */
    protected Collection<String> m_selectedCategories;

    /** Result string for single selection. */
    protected String m_singleResult = "";

    /** The select box to change the sort order. */
    protected CmsSelectBox m_sortSelectBox;

    /** The scroll panel. */
    @UiField
    CmsScrollPanel m_list;

    /** The main panel. */
    @UiField
    FlowPanel m_tab;

    /** The disable reason, will be displayed as check box title. */
    private String m_disabledReason;

    /** The quick filter timer. */
    private Timer m_filterTimer;

    /** The category selection enabled flag. */
    private boolean m_isEnabled;

    /** Flag, indicating if the category tree should be collapsed when shown first. */
    private boolean m_showCollapsed;

    /**
     * Default Constructor.<p>
     */
    public CmsCategoryTree() {

        uiBinder.createAndBindUi(this);
        initWidget(uiBinder.createAndBindUi(this));
        m_isEnabled = true;
    }

    /**
     * Constructor to collect all categories and build a view tree.<p>
     *
     * @param selectedCategories A list of all selected categories
     * @param height The height of this widget
     * @param isSingleValue Sets the modes of this widget
     * @param categories the categories
     **/
    public CmsCategoryTree(
        Collection<String> selectedCategories,
        int height,
        boolean isSingleValue,
        List<CmsCategoryTreeEntry> categories) {

        this(selectedCategories, height, isSingleValue, categories, false);
    }

    /**
     * Constructor to collect all categories and build a view tree.<p>
     *
     * @param selectedCategories A list of all selected categories
     * @param height The height of this widget
     * @param isSingleValue Sets the modes of this widget
     * @param categories the categories
     * @param showCollapsed if true, the category tree will be collapsed when opened.
     **/
    public CmsCategoryTree(
        Collection<String> selectedCategories,
        int height,
        boolean isSingleValue,
        List<CmsCategoryTreeEntry> categories,
        boolean showCollapsed) {

        this();
        m_isSingleSelection = isSingleValue;
        addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().categoryItem());
        m_list.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().categoryScrollPanel());
        m_selectedCategories = selectedCategories;
        Iterator<String> it = selectedCategories.iterator();
        while (it.hasNext()) {
            m_singleResult = it.next();
        }
        m_scrollList = createScrollList();
        m_list.setHeight(height + "px");
        m_resultList = categories;
        m_list.add(m_scrollList);
        m_showCollapsed = showCollapsed;
        m_childrens = new HashMap<>();
        m_categories = new HashMap<>();
        updateContentTree();
        normalizeSelectedCategories();
        init();
    }

    /**
     * Adds children item to the category tree and select the categories.<p>
     *
     * @param parent the parent item
     * @param children the list of children
     * @param selectedCategories the list of categories to select
     */
    public void addChildren(
        CmsTreeItem parent,
        List<CmsCategoryTreeEntry> children,
        Collection<String> selectedCategories) {

        if (children != null) {
            for (CmsCategoryTreeEntry child : children) {
                // set the category tree item and add to parent tree item
                CmsTreeItem treeItem = buildTreeItem(child, selectedCategories);
                m_childrens.get(parent.getId()).add(treeItem.getId());
                m_childrens.put(treeItem.getId(), new ArrayList<>(child.getChildren().size()));
                if ((selectedCategories != null) && selectedCategories.contains(child.getPath())) {
                    openWithParents(parent);

                }
                if (m_isSingleSelection) {
                    if (treeItem.getCheckBox().isChecked()) {
                        parent.getCheckBox().setChecked(false);
                    }
                }
                parent.addChild(treeItem);
                addChildren(treeItem, child.getChildren(), selectedCategories);
            }
        }
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<String>> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Disabled the category selection.<p>
     *
     * @param disabledReason the disable reason, will be displayed as check box title
     */
    public void disable(String disabledReason) {

        if (m_isEnabled
            || (CmsStringUtil.isNotEmptyOrWhitespaceOnly(disabledReason) && !disabledReason.equals(m_disabledReason))) {
            m_isEnabled = false;
            m_disabledReason = disabledReason;
            m_scrollList.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().disabled());
            setListEnabled(m_scrollList, false, disabledReason);
        }
    }

    /**
     * Enables the category selection.<p>
     */
    public void enable() {

        if (!m_isEnabled) {
            m_isEnabled = true;
            m_disabledReason = null;
            m_scrollList.removeStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().disabled());
            setListEnabled(m_scrollList, true, null);
        }
    }

    /**
     * Represents a value change event.<p>
     */
    public void fireValueChange() {

        ValueChangeEvent.fire(this, getAllSelected());
    }

    /**
     * Returns a list of all selected values.<p>
     *
     * @return a list of selected values
     */
    public List<String> getAllSelected() {

        List<String> result = new ArrayList<String>();
        for (String cat : m_selectedCategories) {
            result.add(m_categories.get(cat).getId());
        }
        return result;
    }

    /**
     * Returns a list of all selected values as Sidepath.<p>
     *
     * @return a list of selected values
     */
    public List<String> getAllSelectedSitePath() {

        List<String> result = new ArrayList<String>();
        for (String cat : m_selectedCategories) {
            result.add(((CmsDataValue)m_categories.get(cat).getMainWidget()).getParameter(2));
        }
        return result;
    }

    /**
     * Returns the scrollpanel of this widget.<p>
     *
     * @return CmsScrollPanel the scrollpanel of this widget
     * */
    public CmsScrollPanel getScrollPanel() {

        return m_list;
    }

    /**
     * Returns the last selected value.<p>
     *
     * @return the last selected value
     */
    public List<String> getSelected() {

        List<String> result = new ArrayList<String>();
        result.add(
            m_singleResult.isEmpty()
            ? ""
            : ((CmsDataValue)m_categories.get(m_singleResult).getMainWidget()).getParameter(2));
        return result;
    }

    /**
     * Returns if the category selection is enabled.<p>
     *
     * @return <code>true</code> if the category selection is enabled
     */
    public boolean isEnabled() {

        return m_isEnabled;
    }

    /**
     * Goes up the tree and opens the parents of the item.<p>
     *
     * @param item the child item to start from
     */
    public void openWithParents(CmsTreeItem item) {

        if (item != null) {
            item.setOpen(true);
            openWithParents(item.getParentItem());
        }
    }

    /**
     * Shows the tab list is empty label.<p>
     */
    public void showIsEmptyLabel() {

        CmsSimpleListItem item = new CmsSimpleListItem();
        Label isEmptyLabel = new Label(Messages.get().key(Messages.GUI_CATEGORIES_IS_EMPTY_0));
        item.add(isEmptyLabel);
        m_scrollList.add(item);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int clientWidth) {

        m_scrollList.truncate(textMetricsKey, clientWidth);
    }

    /**
     * Updates the content of the categories list.<p>
     *
     * @param treeItemsToShow the updates list of categories tree item beans
     */
    public void updateContentList(List<CmsTreeItem> treeItemsToShow) {

        m_scrollList.clearList();
        if ((treeItemsToShow != null) && !treeItemsToShow.isEmpty()) {
            for (CmsTreeItem dataValue : treeItemsToShow) {
                dataValue.removeOpener();
                m_scrollList.add(dataValue);
                CmsScrollPanel scrollparent = (CmsScrollPanel)m_scrollList.getParent();
                scrollparent.onResizeDescendant();
            }
        } else {
            showIsEmptyLabel();
        }
        scheduleResize();
    }

    /**
     * Updates the content of the categories tree.<p>
     */
    public void updateContentTree() {

        m_scrollList.clearList();
        m_childrens.clear();
        m_categories.clear();
        if ((m_resultList != null) && !m_resultList.isEmpty()) {
            // add the first level and children
            for (CmsCategoryTreeEntry category : m_resultList) {
                // set the category tree item and add to list
                CmsTreeItem treeItem = buildTreeItem(category, m_selectedCategories);
                m_childrens.put(treeItem.getId(), new ArrayList<>(category.getChildren().size()));
                addChildren(treeItem, category.getChildren(), m_selectedCategories);
                if (!category.getPath().isEmpty() || (treeItem.getChildCount() > 0)) {
                    m_scrollList.add(treeItem);
                }
                treeItem.setOpen(!m_showCollapsed);
            }
        } else {
            showIsEmptyLabel();
        }
        scheduleResize();
    }

    /**
     * Cancels the quick filter timer.<p>
     */
    protected void cancelQuickFilterTimer() {

        if (m_filterTimer != null) {
            m_filterTimer.cancel();
        }
    }

    /**
     * Checks the quick search input and enables/disables the search button accordingly.<p>
     */
    protected void checkQuickSearchStatus() {

        if ((m_quickSearch != null) && (m_searchButton != null)) {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_quickSearch.getFormValueAsString())) {
                m_searchButton.enable();
            } else {
                m_searchButton.disable("Enter a search query");
            }
        }
    }

    /**
     * Creates the quick search/finder box.<p>
     */
    protected void createQuickBox() {

        m_quickSearch = new CmsTextBox();
        // m_quickFilter.setVisible(hasQuickFilter());
        m_quickSearch.getElement().getStyle().setFloat(Float.RIGHT);
        m_quickSearch.setTriggerChangeOnKeyPress(true);
        m_quickSearch.setGhostValue(Messages.get().key(Messages.GUI_QUICK_FINDER_SEARCH_0), true);
        m_quickSearch.setGhostModeClear(true);
        m_options.insert(m_quickSearch, 0);
        m_searchButton = new CmsPushButton();
        m_searchButton.setImageClass(I_CmsButton.SEARCH);
        m_searchButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
        m_searchButton.setSize(Size.small);
        m_searchButton.getElement().getStyle().setFloat(Style.Float.RIGHT);
        m_searchButton.getElement().getStyle().setMarginTop(4, Unit.PX);
        m_searchButton.getElement().getStyle().setMarginLeft(4, Unit.PX);
        m_options.insert(m_searchButton, 0);
        m_quickSearch.addValueChangeHandler(new CategoryValueChangeHandler());

        m_filterTimer = new Timer() {

            @Override
            public void run() {

                quickSearch();

            }
        };
        m_searchButton.setTitle(Messages.get().key(Messages.GUI_QUICK_FINDER_SEARCH_0));

        m_searchButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent arg0) {

                quickSearch();
            }
        });
    }

    /**
     * Creates the list which should contain the list items of the tab.<p>
     *
     * @return the newly created list widget
     */
    protected CmsList<CmsTreeItem> createScrollList() {

        return new CmsList<CmsTreeItem>();
    }

    /**
     * Gets the filtered list of categories.<p>
     *
     * @param filter the search string to use for filtering
     *
     * @return the filtered category beans
     */
    protected List<CmsTreeItem> getFilteredCategories(String filter) {

        List<CmsTreeItem> result = new ArrayList<CmsTreeItem>();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(filter)) {
            result = new ArrayList<CmsTreeItem>();
            for (CmsTreeItem category : m_categories.values()) {
                if (((CmsDataValue)category.getMainWidget()).matchesFilter(filter, 0, 1)) {
                    result.add(category);
                }
            }
        } else {
            Iterator<CmsTreeItem> it = m_categories.values().iterator();
            while (it.hasNext()) {
                result.add(it.next());
            }

        }
        return result;
    }

    /**
     * List of all sort parameters.<p>
     *
     * @return List of all sort parameters
     */
    protected LinkedHashMap<String, String> getSortList() {

        LinkedHashMap<String, String> list = new LinkedHashMap<String, String>();
        list.put(SortParams.tree.name(), Messages.get().key(Messages.GUI_SORT_LABEL_HIERARCHIC_0));
        list.put(SortParams.title_asc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_TITLE_ASC_0));
        list.put(SortParams.title_desc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_TITLE_DECS_0));
        list.put(SortParams.path_asc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_PATH_ASC_0));
        list.put(SortParams.path_desc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_PATH_DESC_0));

        return list;
    }

    /**
     * Returns true if this widget hat an QuickFilter.<p>
     *
     * @return true if this widget hat an QuickFilter
     */
    protected boolean hasQuickFilter() {

        // allow filter if not in tree mode
        return SortParams.tree != SortParams.valueOf(m_sortSelectBox.getFormValueAsString());
    }

    /**
     * Call after all handlers have been set.<p>
     */
    protected void init() {

        LinkedHashMap<String, String> sortList = getSortList();
        if (sortList != null) {
            // generate the sort select box
            m_sortSelectBox = new CmsSelectBox(sortList);
            // add the right handler
            m_sortSelectBox.addValueChangeHandler(new CategoryValueChangeHandler());
            // style the select box
            m_sortSelectBox.getElement().getStyle().setWidth(200, Unit.PX);
            m_sortSelectBox.truncate(TM_GALLERY_SORT, 200);
            // add it to the right panel
            m_options.add(m_sortSelectBox);
            // create the box label
            Label infoLabel = new Label();
            infoLabel.setStyleName(I_CmsLayoutBundle.INSTANCE.categoryDialogCss().infoLabel());
            m_infoLabel = infoLabel;
            // add it to the right panel
            m_options.insert(infoLabel, 0);
            // create quick search box
            createQuickBox();
        }

    }

    /**
     * Sets the search query an selects the result tab.<p>
     */
    protected void quickSearch() {

        List<CmsTreeItem> categories = new ArrayList<CmsTreeItem>();
        if ((m_quickSearch != null)) {
            categories = getFilteredCategories(hasQuickFilter() ? m_quickSearch.getFormValueAsString() : null);
            sort(categories, SortParams.valueOf(m_sortSelectBox.getFormValueAsString()));
        }
    }

    /**
     * Removes the quick search/finder box.<p>
     */
    protected void removeQuickBox() {

        if (m_quickSearch != null) {
            m_quickSearch.removeFromParent();
            m_quickSearch = null;
        }
        if (m_searchButton != null) {
            m_searchButton.removeFromParent();
            m_searchButton = null;
        }
    }

    /**
     * Schedules the quick filter action.<p>
     */
    protected void scheduleQuickFilterTimer() {

        m_filterTimer.schedule(FILTER_DELAY);
    }

    /**
     * Sorts a list of tree items according to the sort parameter.<p>
     *
     * @param items the items to sort
     * @param sort the sort parameter
     */
    protected void sort(List<CmsTreeItem> items, SortParams sort) {

        int sortParam = -1;
        boolean ascending = true;
        switch (sort) {
            case tree:
                m_quickSearch.setFormValueAsString("");
                m_listView = false;
                updateContentTree();
                break;
            case title_asc:
                sortParam = 0;
                break;
            case title_desc:
                sortParam = 0;
                ascending = false;
                break;
            case path_asc:
                sortParam = 1;
                break;
            case path_desc:
                sortParam = 1;
                ascending = false;
                break;
            default:
                break;
        }
        if (sortParam != -1) {
            m_listView = true;
            items = getFilteredCategories(hasQuickFilter() ? m_quickSearch.getFormValueAsString() : null);
            Collections.sort(items, new CmsListItemDataComparator(sortParam, ascending));
            updateContentList(items);
        }
    }

    /**
     * Called if a category is selected/deselected.
     *
     * The checkbox state of the selected/deselected item has to be the state BEFORE toggling.
     *
     * @param item the tree item that should be selected/deselected.
     * @param changeState flag, indicating if the checkbox state should be changed.
     */
    protected void toggleSelection(CmsTreeItem item, boolean changeState) {

        boolean select = item.getCheckBox().isChecked();
        select = changeState ? !select : select;
        if (m_isSingleSelection) {
            m_selectedCategories.clear();
            uncheckAll(m_scrollList);
        }
        if (select) {
            if (m_isSingleSelection) {
                m_singleResult = item.getId();
            }
            CmsTreeItem currentItem = item;
            do {
                currentItem.getCheckBox().setChecked(true);
                String id = currentItem.getId();
                if (!m_selectedCategories.contains(id)) {
                    m_selectedCategories.add(id);
                }
                currentItem = currentItem.getParentItem();
            } while (currentItem != null);
        } else {
            if (m_isSingleSelection) {
                m_singleResult = "";
            }
            deselectChildren(item);
            CmsTreeItem currentItem = item;
            do {
                currentItem.getCheckBox().setChecked(false);
                String id = currentItem.getId();
                if (m_selectedCategories.contains(id)) {
                    m_selectedCategories.remove(id);
                }
                currentItem = currentItem.getParentItem();
            } while ((currentItem != null) && !hasSelectedChildren(currentItem));
        }
        fireValueChange();
    }

    /**
     * Builds a tree item for the given category.<p>
     *
     * @param category the category
     * @param selectedCategories the selected categories
     *
     * @return the tree item widget
     */
    private CmsTreeItem buildTreeItem(CmsCategoryTreeEntry category, Collection<String> selectedCategories) {

        // generate the widget that should be shown in the list
        CmsDataValue dataValue = new CmsDataValue(
            600,
            3,
            CmsCategoryBean.SMALL_ICON_CLASSES,
            true,
            category.getTitle(),
            "rtl:" + category.getPath(),
            "hide:" + category.getSitePath());

        // create the check box for this item
        CmsCheckBox checkBox = new CmsCheckBox();
        // if it has to be selected, select it
        boolean isPartofPath = false;
        isPartofPath = CmsCategoryField.isParentCategoryOfSelected(category.getPath(), selectedCategories);
        if (isPartofPath) {
            checkBox.setChecked(true);
        }
        if (!isEnabled()) {
            checkBox.disable(m_disabledReason);
        }
        if (category.getPath().isEmpty()) {
            checkBox.setVisible(false);
        }
        // bild the CmsTreeItem out of the widget and the check box
        CmsTreeItem treeItem = new CmsTreeItem(true, checkBox, dataValue);
        // abb the handler to the check box
        dataValue.addClickHandler(new DataValueClickHander(treeItem));

        checkBox.addValueChangeHandler(new CheckBoxValueChangeHandler(treeItem));

        // set the right style for the small view
        treeItem.setSmallView(true);
        treeItem.setId(category.getPath());
        // add it to the list of all categories
        m_categories.put(treeItem.getId(), treeItem);
        return treeItem;
    }

    /**
     * Deselects all child items of the provided item.
     * @param item the item for which all childs should be deselected.d
     */
    private void deselectChildren(CmsTreeItem item) {

        for (String childId : m_childrens.get(item.getId())) {
            CmsTreeItem child = m_categories.get(childId);
            deselectChildren(child);
            child.getCheckBox().setChecked(false);
            if (m_selectedCategories.contains(childId)) {
                m_selectedCategories.remove(childId);
            }
        }
    }

    /**
     * Return true if at least one child of the given tree item is selected.<p>
     * @param item The CmsTreeItem to start the check
     * @return true if the given CmsTreeItem or its children is selected
     */
    private boolean hasSelectedChildren(CmsTreeItem item) {

        for (String childId : m_childrens.get(item.getId())) {
            CmsTreeItem child = m_categories.get(childId);
            if (child.getCheckBox().isChecked() || hasSelectedChildren(child)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Normalize the list of selected categories to fit for the ids of the tree items.
     */
    private void normalizeSelectedCategories() {

        Collection<String> normalizedCategories = new ArrayList<String>(m_selectedCategories.size());
        for (CmsTreeItem item : m_categories.values()) {
            if (item.getCheckBox().isChecked()) {
                normalizedCategories.add(item.getId());
            }
        }
        m_selectedCategories = normalizedCategories;

    }

    /**
     * Schedules the execution of onResize deferred.<p>
     */
    private void scheduleResize() {

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                m_list.onResizeDescendant();
            }
        });
    }

    /**
     * Sets the given tree list enabled/disabled.<p>
     *
     * @param list the list of tree items
     * @param enabled <code>true</code> to enable
     * @param disabledReason the disable reason, will be displayed as check box title
     */
    private void setListEnabled(CmsList<? extends I_CmsListItem> list, boolean enabled, String disabledReason) {

        for (Widget child : list) {
            CmsTreeItem treeItem = (CmsTreeItem)child;
            if (enabled) {
                treeItem.getCheckBox().enable();
            } else {
                treeItem.getCheckBox().disable(disabledReason);
            }
            setListEnabled(treeItem.getChildren(), enabled, disabledReason);
        }
    }

    /**
     * Uncheck all items in the list including all sub-items.
     * @param list list of CmsTreeItem entries.
     */
    private void uncheckAll(CmsList<? extends I_CmsListItem> list) {

        for (Widget it : list) {
            CmsTreeItem treeItem = (CmsTreeItem)it;
            treeItem.getCheckBox().setChecked(false);
            uncheckAll(treeItem.getChildren());
        }
    }

}
