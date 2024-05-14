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

import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.I_CmsGalleryDialogCss;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.upload.client.I_CmsUploadContext;
import org.opencms.ade.upload.client.ui.CmsDialogUploadButtonHandler;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.I_CmsTruncable;
import org.opencms.gwt.client.ui.externallink.CmsEditExternalLinkDialog;
import org.opencms.gwt.client.ui.input.A_CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.upload.CmsUploadButton;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a widget for the content of a tab.<p>
 *
 * @since 8.0.
 */
public abstract class A_CmsListTab extends A_CmsTab implements ValueChangeHandler<String>, I_CmsTruncable {

    /** Selection handler to handle check box click events and double clicks on the list items. */
    protected abstract class A_SelectionHandler implements ClickHandler {

        /** The reference to the checkbox. */
        private CmsCheckBox m_checkBox;

        /** The the select button, can be used instead of a double click to select and search. */
        private CmsPushButton m_selectButton;

        /**
         * Constructor.<p>
         *
         * @param checkBox the item check box
         */
        protected A_SelectionHandler(CmsCheckBox checkBox) {

            m_checkBox = checkBox;
        }

        /**
         * Returns the select button.<p>
         *
         * @return the select button
         */
        public CmsPushButton getSelectButton() {

            return m_selectButton;
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            if (event.getSource().equals(m_checkBox)) {
                onSelectionChange();
            } else {
                selectBeforeGoingToResultTab();
                getTabHandler().selectResultTab();
            }
        }

        /**
         * Sets the select button, can be used instead of a double click to select and search.<p>
         *
         * @param button the select button
         */
        public void setSelectButton(CmsPushButton button) {

            m_selectButton = button;
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

        /**
         * This method is called if a list item is selected in a way such that the result tab should be displayed
         * immediately.<p>
         */
        protected void selectBeforeGoingToResultTab() {

            m_checkBox.setChecked(true);
            onSelectionChange();
        }
    }

    /**
     * Special click handler to use with select button.<p>
     */
    protected class SelectHandler implements ClickHandler, DoubleClickHandler {

        /** The id of the selected item. */
        private String m_resourcePath;

        /** The resource type of the selected item. */
        private String m_resourceType;

        /** The structure id. */
        private CmsUUID m_structureId;

        /** The resource title. */
        private String m_title;

        /**
         * Constructor.<p>
         *
         * @param resourcePath the item resource path
         * @param structureId the structure id
         * @param title the resource title
         * @param resourceType the item resource type
         */
        public SelectHandler(String resourcePath, CmsUUID structureId, String title, String resourceType) {

            m_resourcePath = resourcePath;
            m_structureId = structureId;
            m_resourceType = resourceType;
            m_title = title;
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            getTabHandler().selectResource(m_resourcePath, m_structureId, m_title, m_resourceType);
        }

        /**
         * @see com.google.gwt.event.dom.client.DoubleClickHandler#onDoubleClick(com.google.gwt.event.dom.client.DoubleClickEvent)
         */
        public void onDoubleClick(DoubleClickEvent event) {

            getTabHandler().selectResource(m_resourcePath, m_structureId, m_title, m_resourceType);
        }
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

    /** Text metrics key. */
    private static final String TM_LIST_TAB = "ListTab";

    /** The ui-binder instance for this class. */
    private static I_CmsListTabUiBinder uiBinder = GWT.create(I_CmsListTabUiBinder.class);

    /** A label for displaying additional information about the tab. */
    protected HasText m_infoLabel;

    /** The borded panel to hold the scrollable list. */
    @UiField
    protected CmsScrollPanel m_list;

    /** The option panel. */
    @UiField
    protected FlowPanel m_options;

    /** The quick search box. */
    protected CmsTextBox m_quickSearch;

    /** The scrollable list panel. */
    protected CmsList<? extends I_CmsListItem> m_scrollList;

    /** The quick search button. */
    protected CmsPushButton m_searchButton;

    /** The select box to change the sort order. */
    protected A_CmsSelectBox<?> m_sortSelectBox;

    /** The option panel. */
    @UiField
    protected FlowPanel m_tab;

    /** The quick filter timer. */
    private Timer m_filterTimer;

    /** The last quick search value. */
    private String m_lastQuickSearchValue = "";

    /** The quick search handler registration. */
    private HandlerRegistration m_quickSearchRegistration;

    /** Panel to put additional widgets. */
    @UiField
    FlowPanel m_additionalWidgets;

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
        m_scrollList = createScrollList();
        m_list.add(m_scrollList);
    }

    /**
     * Returns the list.<p>
     *
     * @return the list
     */
    public CmsScrollPanel getList() {

        return m_list;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getRequiredHeight()
     */
    @Override
    public int getRequiredHeight() {

        int list = m_scrollList.getOffsetHeight();
        list = list > 82 ? list : 82;
        return list + 47;
    }

    /**
     * Call on content change to update the layout.<p>
     */
    public void onContentChange() {

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                Widget parent = getParent();
                while (parent != null) {
                    if (parent instanceof CmsGalleryDialog) {
                        ((CmsGalleryDialog)parent).updateSizes();
                        parent = null;
                    } else {
                        parent = parent.getParent();
                    }
                }
            }
        });
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#onResize()
     */
    @Override
    public void onResize() {

        m_list.onResizeDescendant();
    }

    /**
     * Will be triggered if the value in the select box changes.<p>
     *
     * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
     */
    public void onValueChange(ValueChangeEvent<String> event) {

        cancelQuickFilterTimer();
        if (event.getSource() == m_sortSelectBox) {
            // depending on the sort value the tab may or may not have the quick filter ability
            if (hasQuickFilter()) {
                if (m_quickSearch == null) {
                    createQuickBox();
                }
            } else {
                removeQuickBox();
            }
            getTabHandler().onSort(event.getValue(), hasQuickFilter() ? m_quickSearch.getFormValueAsString() : null);
        }
        if ((event.getSource() == m_quickSearch)) {
            if (hasQuickFilter()) {

                if ((CmsStringUtil.isEmptyOrWhitespaceOnly(event.getValue()) || (event.getValue().length() >= 3))) {
                    // only act if filter length is at least 3 characters or empty
                    if ((m_lastQuickSearchValue == null) || !m_lastQuickSearchValue.equals(event.getValue())) {
                        scheduleQuickFilterTimer();
                    }
                    m_lastQuickSearchValue = event.getValue();
                }
            } else {
                checkQuickSearchStatus();
            }
        }
    }

    /**
     * Sets the value selected in the sort select box, if possible.<p>
     *
     * @param value the new value for the sort select box
     * @param fireEvents if true, the change event of the select box is fired
     */
    public void setSortSelectBoxValue(String value, boolean fireEvents) {

        if ((m_sortSelectBox != null) && (value != null)) {
            m_sortSelectBox.setFormValue(value, fireEvents);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int clientWidth) {

        m_scrollList.truncate(TM_LIST_TAB, clientWidth);
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
     * Clears the list panel.<p>
     */
    protected void clearList() {

        m_scrollList.clearList();
        onContentChange();
    }

    /**
     * Generates a new custom upload button.<p>
     *
     * @param nativeMethod the name of the custom JS upload action
     * @param target the upload target
     *
     * @return the button widget
     */
    protected CmsPushButton createCustomUploadButton(final String nativeMethod, final String target) {

        CmsPushButton uploadButton = new CmsPushButton(I_CmsButton.UPLOAD_SMALL);
        uploadButton.setText(null);
        uploadButton.setTitle(Messages.get().key(Messages.GUI_GALLERY_UPLOAD_TITLE_1, target));
        uploadButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
        uploadButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                customUploadAction(nativeMethod, target); /** TODO: Callback */
            }
        });
        return uploadButton;

    }

    /**
     * Generates a button to create new external link resources.<p>
     *
     * @param parentPath the parent folder site path
     *
     * @return the button widget
     */
    protected CmsPushButton createNewExternalLinkButton(final String parentPath) {

        final CmsResourceTypeBean typeInfo = getTabHandler().getTypeInfo(
            CmsEditExternalLinkDialog.POINTER_RESOURCE_TYPE_NAME);
        CmsPushButton createNewButton = null;
        if (typeInfo != null) {
            createNewButton = new CmsPushButton(I_CmsButton.ADD_SMALL);
            createNewButton.setTitle(
                org.opencms.gwt.client.Messages.get().key(
                    org.opencms.gwt.client.Messages.GUI_CREATE_NEW_LINK_DIALOG_TITLE_0));
            createNewButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
            createNewButton.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    CmsEditExternalLinkDialog dialog = CmsEditExternalLinkDialog.showNewLinkDialog(
                        typeInfo,
                        parentPath);
                    dialog.addCloseHandler(new CloseHandler<PopupPanel>() {

                        public void onClose(CloseEvent<PopupPanel> closeEvent) {

                            getTabHandler().updateIndex();
                        }
                    });
                }
            });
        }
        return createNewButton;
    }

    /**
     * Creates the 'optmize gallery' button.
     *
     * @param selectionHandler the selection handler
     * @return the created button
     */
    protected CmsPushButton createOptimizeButton(A_SelectionHandler selectionHandler) {

        CmsPushButton selectButton = new CmsPushButton();
        selectButton.setImageClass(I_CmsButton.EDIT_SMALL);
        selectButton.setTitle("Edit");
        selectButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
        selectionHandler.setSelectButton(selectButton);
        selectButton.addClickHandler(selectionHandler);
        return selectButton;
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
     * Creates the sort select box.
     * @param options the options for the select box
     *
     * @return the sort select box
     */
    protected A_CmsSelectBox<?> createSelectBox(LinkedHashMap<String, String> options) {

        return new CmsSelectBox(options);
    }

    /**
     * Creates a select button.<p>
     *
     * @param selectionHandler the selction handler
     *
     * @return the select button
     */
    protected CmsPushButton createSelectButton(A_SelectionHandler selectionHandler) {

        CmsPushButton selectButton = new CmsPushButton();
        selectButton.setImageClass(I_CmsButton.SEARCH_SMALL);
        selectButton.setTitle(Messages.get().key(Messages.GUI_TAB_SEARCH_SEARCH_EXISTING_0));
        selectButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
        selectionHandler.setSelectButton(selectButton);
        selectButton.addClickHandler(selectionHandler);
        return selectButton;
    }

    /**
     * Creates a button widget to select the specified resource.<p>
     *
     * @param resourcePath the item resource path
     * @param structureId the structure id
     * @param title the resource title
     * @param resourceType the item resource type
     *
     * @return the initialized select resource button
     */
    protected CmsPushButton createSelectResourceButton(
        String resourcePath,
        CmsUUID structureId,
        String title,
        String resourceType) {

        CmsPushButton result = new CmsPushButton();
        result.setImageClass(I_CmsButton.CHECK_SMALL);
        result.setButtonStyle(ButtonStyle.FONT_ICON, null);
        result.setTitle(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_SELECT_0));
        result.addClickHandler(new SelectHandler(resourcePath, structureId, title, resourceType));
        return result;
    }

    /**
     * Creates an upload button for the given target.<p>
     *
     * @param target the upload target folder
     * @param isRootPath true if target is a root path
     *
     * @return the upload button
     */
    protected CmsUploadButton createUploadButtonForTarget(String target, boolean isRootPath) {

        CmsDialogUploadButtonHandler buttonHandler = new CmsDialogUploadButtonHandler(

            new Supplier<I_CmsUploadContext>() {

                public I_CmsUploadContext get() {

                    return new I_CmsUploadContext() {

                        public void onUploadFinished(List<String> uploadedFiles) {

                            getTabHandler().updateIndex();
                        }

                    };
                }
            });

        buttonHandler.setTargetFolder(target);
        buttonHandler.setIsTargetRootPath(isRootPath);
        CmsUploadButton uploadButton = new CmsUploadButton(buttonHandler);
        uploadButton.setText(null);
        uploadButton.setTitle(Messages.get().key(Messages.GUI_GALLERY_UPLOAD_TITLE_1, target));
        uploadButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
        uploadButton.setImageClass(I_CmsButton.UPLOAD_SMALL);
        return uploadButton;
    }

    /**
     * Returns a list with sort values for this tab.<p>
     *
     * @return list of sort order value/text pairs
     */
    protected abstract LinkedHashMap<String, String> getSortList();

    /**
     * Returns if this tab has quick filter enabled.<p>
     *
     * @return <code>true</code> if this tab has quick filter enabled
     */
    protected boolean hasQuickFilter() {

        return false;
    }

    /**
     * Returns if the tab has the quick search box.<p>
     *
     * @return <code>true</code> if the tab has the quick search box
     */
    protected boolean hasQuickSearch() {

        return false;
    }

    /**
     * Call after all handlers have been set.<p>
     */
    protected void init() {

        LinkedHashMap<String, String> sortList = getSortList();
        if (sortList != null) {
            m_sortSelectBox = createSelectBox(sortList);
            m_sortSelectBox.addValueChangeHandler(this);
            m_sortSelectBox.addStyleName(DIALOG_CSS.selectboxWidth());
            m_options.add(m_sortSelectBox);
            Label infoLabel = new Label();
            infoLabel.setStyleName(DIALOG_CSS.infoLabel());
            m_infoLabel = infoLabel;
            m_options.insert(infoLabel, 0);
        }
        createQuickBox();

    }

    /**
     * Sets the search query an selects the result tab.<p>
     */
    protected void quickSearch() {

        if ((m_quickSearch != null) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_quickSearch.getFormValueAsString())) {
            getTabHandler().setSearchQuery(m_quickSearch.getFormValueAsString());
            getTabHandler().selectResultTab();
        }
    }

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
                if (!(list.getWidget(i) instanceof CmsTreeItem)) {
                    continue;
                }
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

    /**
     * Runs the native custom uplod handler.
     *
     * @param nativeMethod the native method name
     * @param target the upload target
     */
    native void customUploadAction(String nativeMethod, String target) /*-{

        var uploadAction = $wnd[nativeMethod];
        nativeMethod(target);

    }-*/;

    /**
     * Creates the quick search/finder box.<p>
     */
    private void createQuickBox() {

        if (hasQuickSearch() || hasQuickFilter()) {
            m_quickSearch = new CmsTextBox();
            //   m_quickFilter.setVisible(hasQuickFilter());
            m_quickSearch.addStyleName(DIALOG_CSS.quickFilterBox());
            m_quickSearch.setTriggerChangeOnKeyPress(true);
            String message = hasQuickFilter()
            ? Messages.get().key(Messages.GUI_QUICK_FINDER_FILTER_0)
            : Messages.get().key(Messages.GUI_QUICK_FINDER_SEARCH_0);
            m_quickSearch.setGhostValue(message, true);
            m_quickSearch.setGhostModeClear(true);
            m_options.insert(m_quickSearch, 0);
            m_searchButton = new CmsPushButton();
            m_searchButton.setImageClass(hasQuickFilter() ? I_CmsButton.FILTER : I_CmsButton.SEARCH_SMALL);
            m_searchButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
            m_searchButton.getElement().getStyle().setFloat(Style.Float.RIGHT);
            m_searchButton.getElement().getStyle().setMarginTop(4, Unit.PX);
            m_options.insert(m_searchButton, 0);
            m_quickSearch.addValueChangeHandler(this);
            if (hasQuickFilter()) {
                m_filterTimer = new Timer() {

                    @Override
                    public void run() {

                        getTabHandler().onSort(
                            m_sortSelectBox.getFormValueAsString(),
                            m_quickSearch.getFormValueAsString());
                        onContentChange();
                    }
                };
                m_searchButton.setTitle(message);
            } else {
                m_quickSearch.addKeyPressHandler(new KeyPressHandler() {

                    public void onKeyPress(KeyPressEvent event) {

                        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                            quickSearch();
                        }
                    }
                });
                m_searchButton.addClickHandler(new ClickHandler() {

                    public void onClick(ClickEvent arg0) {

                        quickSearch();
                    }
                });
                m_quickSearchRegistration = getTabHandler().addSearchChangeHandler(
                    new ValueChangeHandler<CmsGallerySearchBean>() {

                        public void onValueChange(ValueChangeEvent<CmsGallerySearchBean> event) {

                            m_quickSearch.setFormValueAsString(event.getValue().getQuery());
                        }
                    });
                m_searchButton.setTitle(Messages.get().key(Messages.GUI_TAB_SEARCH_SEARCH_EXISTING_0));
            }

        }
    }

    /**
     * Removes the quick search/finder box.<p>
     */
    private void removeQuickBox() {

        if (m_quickSearch != null) {
            m_quickSearch.removeFromParent();
            m_quickSearch = null;
        }
        if (m_searchButton != null) {
            m_searchButton.removeFromParent();
            m_searchButton = null;
        }
        if (m_quickSearchRegistration != null) {
            m_quickSearchRegistration.removeHandler();
            m_quickSearchRegistration = null;
        }
    }
}
