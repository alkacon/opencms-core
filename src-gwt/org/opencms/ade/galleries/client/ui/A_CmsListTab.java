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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.I_CmsGalleryDialogCss;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.upload.client.ui.CmsUploadButton;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a widget for the content of a tab.<p>
 * 
 * @since 8.0.
 */
public abstract class A_CmsListTab extends A_CmsTab implements ValueChangeHandler<String> {

    /** Selection handler to handle check box click events and double clicks on the list items. */
    protected abstract class A_SelectionHandler implements ClickHandler, DoubleClickHandler {

        /** The reference to the checkbox. */
        private CmsCheckBox m_checkBox;

        /**
         * Constructor.<p>
         * 
         * @param checkBox the item check box
         */
        protected A_SelectionHandler(CmsCheckBox checkBox) {

            m_checkBox = checkBox;
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            onSelectionChange();
        }

        /**
         * @see com.google.gwt.event.dom.client.DoubleClickHandler#onDoubleClick(com.google.gwt.event.dom.client.DoubleClickEvent)
         */
        public void onDoubleClick(DoubleClickEvent event) {

            m_checkBox.setChecked(true);
            onSelectionChange();
            getTabHandler().selectResultTab();
            event.stopPropagation();
            event.preventDefault();
        }

        /**
         * Returns the check box.<p>
         * 
         * @return the check box
         */
        protected CmsCheckBox getCheckBox() {

            return m_checkBox;
        }

        /**
         * Executed on selection change. Either when the check box was clicked or on double click on a list item.<p>
         */
        protected abstract void onSelectionChange();
    }

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    interface I_CmsListTabUiBinder extends UiBinder<Widget, A_CmsListTab> {
        // GWT interface, nothing to do here
    }

    /** The css bundle used for this widget. */
    protected static final I_CmsGalleryDialogCss DIALOG_CSS = I_CmsLayoutBundle.INSTANCE.galleryDialogCss();

    /** The filtering delay. */
    private static final int FILTER_DELAY = 200;

    /** Text metrics key. */
    private static final String TM_GALLERY_SORT = "gallerySort";

    /** The ui-binder instance for this class. */
    private static I_CmsListTabUiBinder uiBinder = GWT.create(I_CmsListTabUiBinder.class);

    /** A label for displaying additional information about the tab. */
    protected HasText m_infoLabel;

    /** The borded panel to hold the scrollable list. */
    @UiField
    protected ScrollPanel m_list;

    /** The option panel. */
    @UiField
    protected FlowPanel m_options;

    /** The Quick filter text box. */
    protected CmsTextBox m_quickFilter;

    /** The scrollable list panel. */
    protected CmsList<? extends I_CmsListItem> m_scrollList;

    /** The select box to change the sort order. */
    protected CmsSelectBox m_sortSelectBox;

    /** The option panel. */
    @UiField
    protected FlowPanel m_tab;

    /** The quick filter timer. */
    private Timer m_filterTimer;

    /**
     * The default constructor with drag handler.<p>
     * 
     * @param tabId the tab id 
     */
    public A_CmsListTab(GalleryTabId tabId) {

        this(tabId.name());
    }

    /**
     * Sets up a list tab with a given tab id.<p>
     *  
     * @param tabId the tab id 
     */
    public A_CmsListTab(String tabId) {

        super(tabId);
        uiBinder.createAndBindUi(this);
        initWidget(uiBinder.createAndBindUi(this));
        List<CmsPair<String, String>> sortList = getSortList();
        if (sortList != null) {
            m_sortSelectBox = new CmsSelectBox(sortList);
            m_sortSelectBox.addValueChangeHandler(this);
            m_sortSelectBox.addStyleName(DIALOG_CSS.selectboxWidth());
            m_sortSelectBox.truncate(TM_GALLERY_SORT, 200);
            m_options.add(m_sortSelectBox);
            Label infoLabel = new Label();
            infoLabel.setStyleName(DIALOG_CSS.infoLabel());
            m_infoLabel = infoLabel;
            m_options.insert(infoLabel, 0);
            m_quickFilter = new CmsTextBox();
            m_quickFilter.setVisible(hasQuickFilter());
            m_quickFilter.addValueChangeHandler(this);
            m_quickFilter.addStyleName(DIALOG_CSS.quickFilterBox());
            m_quickFilter.setTriggerChangeOnKeyPress(true);
            m_quickFilter.setGhostValue(Messages.get().key(Messages.GUI_QUICK_FINDER_SEARCH_0), true);
            m_quickFilter.setGhostModeClear(true);
            m_options.insert(m_quickFilter, 0);
        }
        m_filterTimer = new Timer() {

            @Override
            public void run() {

                getTabHandler().onSort(m_sortSelectBox.getFormValueAsString(), m_quickFilter.getFormValueAsString());

            }
        };
        m_scrollList = createScrollList();
        m_list.add(m_scrollList);
    }

    /**
     * Returns the list.<p>
     *
     * @return the list
     */
    public ScrollPanel getList() {

        return m_list;
    }

    /**
     * Will be triggered if the value in the select box changes.<p>
     * 
     * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
     */
    public void onValueChange(ValueChangeEvent<String> event) {

        cancelQuickFilterTimer();
        if (event.getSource() == m_sortSelectBox) {
            getTabHandler().onSort(event.getValue(), hasQuickFilter() ? m_quickFilter.getFormValueAsString() : null);
        }
        if ((event.getSource() == m_quickFilter)
            && (CmsStringUtil.isEmptyOrWhitespaceOnly(event.getValue()) || (event.getValue().length() >= 3))) {
            // only act if filter length is at least 3 characters or empty
            scheduleQuickFilterTimer();
        }
        m_quickFilter.setVisible(hasQuickFilter());
    }

    /**
     * Adds a widget to the front of the list.<p>
     * 
     * @param listItem the list item to add 
     */
    protected void addWidgetToFrontOfList(Widget listItem) {

        m_scrollList.insert(listItem, 0);

    }

    /**
     * Add a list item widget to the list panel.<p>
     * 
     * @param listItem the list item to add
     */
    protected void addWidgetToList(Widget listItem) {

        m_scrollList.add(listItem);
    }

    /**
     * Add a widget to the option panel.<p>
     * 
     * The option panel should contain drop down boxes or other list options.
     * 
     * @param widget the widget to add
     */
    protected void addWidgetToOptions(Widget widget) {

        m_options.add(widget);
    }

    /**
     * Cancels the quick filter timer.<p>
     */
    protected void cancelQuickFilterTimer() {

        m_filterTimer.cancel();
    }

    /**
     * Clears the list panel.<p>
     */
    protected void clearList() {

        m_scrollList.clearList();
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
     * Creates an upload button for the given target.<p>
     * 
     * @param target the upload target folder
     * 
     * @return the upload button
     */
    protected CmsUploadButton createUploadButtonForTarget(String target) {

        CmsUploadButton uploadButton = new CmsUploadButton();
        uploadButton.setTargetFolder(target);
        uploadButton.setText(null);
        uploadButton.setTitle(Messages.get().key(Messages.GUI_GALLERY_UPLOAD_TITLE_1, target));
        uploadButton.setButtonStyle(ButtonStyle.TRANSPARENT, null);
        uploadButton.setImageClass(I_CmsImageBundle.INSTANCE.style().uploadIcon());
        uploadButton.setDialogCloseHandler(getTabHandler());
        return uploadButton;
    }

    /**
     * Returns a list with sort values for this tab.<p>
     * 
     * @return list of sort order value/text pairs
     */
    protected abstract List<CmsPair<String, String>> getSortList();

    /**
     * Returns if this tab has quick filter enabled.<p>
     * 
     * @return <code>true</code> if this tab has quick filter enabled
     */
    protected abstract boolean hasQuickFilter();

    /**
     * Schedules the quick filter action.<p>
     */
    protected void scheduleQuickFilterTimer() {

        m_filterTimer.schedule(FILTER_DELAY);
    }

    /**
     * Searches in the categories tree or list the item and returns it.<p>
     * 
     * @param list the list of items to start from
     * @param categoryPath the category id to search
     * @return the category item widget
     */
    protected CmsTreeItem searchTreeItem(CmsList<? extends I_CmsListItem> list, String categoryPath) {

        CmsTreeItem resultItem = (CmsTreeItem)list.getItem(categoryPath);
        // item is not in this tree level
        if (resultItem == null) {
            // if list is not empty
            for (int i = 0; i < list.getWidgetCount(); i++) {
                CmsTreeItem listItem = (CmsTreeItem)list.getWidget(i);
                if (listItem.getChildCount() == 0) {
                    continue;
                }
                // continue search in children
                resultItem = searchTreeItem(listItem.getChildren(), categoryPath);
                // break the search if result item is found
                if (resultItem != null) {
                    break;
                }
            }
        }
        return resultItem;
    }
}
