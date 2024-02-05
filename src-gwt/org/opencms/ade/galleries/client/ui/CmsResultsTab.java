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

import org.opencms.ade.galleries.client.CmsResultContextMenuHandler;
import org.opencms.ade.galleries.client.CmsResultsTabHandler;
import org.opencms.ade.galleries.client.I_CmsGalleryHandler;
import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsGalleryFolderBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchScope;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.ade.upload.client.ui.CmsDialogUploadButtonHandler;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuButton;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuHandler;
import org.opencms.gwt.client.ui.externallink.CmsEditExternalLinkDialog;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.upload.CmsUploadButton;
import org.opencms.gwt.client.ui.input.upload.I_CmsUploadButtonHandler;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsEmbeddedDialogHandler;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides the widget for the results tab.<p>
 *
 * It displays the selected search parameter, the sort order and
 * the search results for the current search.
 *
 * @since 8.0.
 */
public class CmsResultsTab extends A_CmsListTab {

    /**
     * Click-handler for the delete button.<p>
     */
    public class DeleteHandler implements ClickHandler {

        /** The resource path of the selected item. */
        protected String m_resourcePath;

        /**
         * Constructor.<p>
         *
         * @param resourcePath the item resource path
         */
        protected DeleteHandler(String resourcePath) {

            m_resourcePath = resourcePath;
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            getTabHandler().deleteResource(m_resourcePath);
        }

    }

    /**
     * Enum representing different options for the results tab.
     */
    public enum ParamType {
        /** Search scope. */
        scope,
        /** Query text. */
        text;
    }

    /**
     * Bar containing the search scope selection and a text search field.
     */
    public class SearchBar extends Composite {

        /** The field for the text search. */
        @UiField
        protected CmsTextBox m_searchInput;

        /** The search button. */
        @UiField
        protected CmsPushButton m_textSearchButton;

        /** The select box for the search scope selection. */
        @UiField
        protected CmsSelectBox m_scopeSelection;

        /**
         * Creates a new instance.
         */
        public SearchBar() {

            I_CmsSearchBarUiBinder uiBinder = GWT.create(I_CmsSearchBarUiBinder.class);
            FlowPanel content = uiBinder.createAndBindUi(this);
            initWidget(content);
            m_searchInput.setGhostValue(Messages.get().key(Messages.GUI_QUICK_FINDER_SEARCH_0), true);
            m_searchInput.setGhostModeClear(true);
            m_textSearchButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
            m_textSearchButton.setImageClass(I_CmsButton.SEARCH_SMALL);
            m_textSearchButton.setTitle(Messages.get().key(Messages.GUI_TAB_SEARCH_SEARCH_EXISTING_0));
        }

        /**
         * Gets the scope selection widget.
         *
         * @return the scope selection widget
         */
        public CmsSelectBox getScopeSelection() {

            return m_scopeSelection;
        }

        /**
         * Gets the search button.
         *
         * @return the search button
         */
        public CmsPushButton getSearchButton() {

            return m_textSearchButton;
        }

        /**
         * Gets the search input field.
         *
         * @return the search input field
         */
        public CmsTextBox getSearchInput() {

            return m_searchInput;
        }

        /**
         * Handles the change event on the search scope select box.<p>
         *
         * @param event the change event
         */
        @UiHandler("m_scopeSelection")
        protected void onScopeChange(ValueChangeEvent<String> event) {

            String value = event.getValue();
            m_tabHandler.setScope(CmsGallerySearchScope.valueOf(value));

        }
    }

    /**
     * Scroll handler which executes an action when the user has scrolled to the bottom.<p>
     *
     * @since 8.0.0
     */
    protected class CmsAsynchronousScrollToBottomHandler implements ScrollHandler {

        /**
         * If the lower edge of the content being scrolled is at most this many pixels below the lower
         * edge of the scrolling viewport, the action is triggered.
         */
        public static final int DEFAULT_SCROLL_THRESHOLD = 200;

        /**
         * Constructs a new scroll handler with a custom scroll threshold.
         *
         * The scroll threshold is the distance from the bottom edge of the scrolled content
         * such that when the distance from the bottom edge of the scroll viewport to the bottom
         * edge of the scrolled content becomes lower than the distance, the scroll action is triggered.
         *
         */
        public CmsAsynchronousScrollToBottomHandler() {

            // noop
        }

        /**
         * @see com.google.gwt.event.dom.client.ScrollHandler#onScroll(com.google.gwt.event.dom.client.ScrollEvent)
         */
        public void onScroll(ScrollEvent event) {

            if (!m_hasMoreResults || getTabHandler().isLoading()) {
                return;
            }
            final ScrollPanel scrollPanel = (ScrollPanel)event.getSource();
            final int scrollPos = scrollPanel.getVerticalScrollPosition();
            Widget child = scrollPanel.getWidget();
            int childHeight = child.getOffsetHeight();
            int ownHeight = scrollPanel.getOffsetHeight();
            boolean isBottom = (scrollPos + ownHeight) >= (childHeight - DEFAULT_SCROLL_THRESHOLD);
            if (isBottom) {
                getTabHandler().onScrollToBottom();
                setScrollPosition(scrollPos);
            }
        }
    }

    /**
     * Special click handler to use with preview button.<p>
     */
    protected class PreviewHandler implements ClickHandler {

        /** The resource path of the selected item. */
        private String m_resourcePath;

        /** The resource type of the selected item. */
        private String m_resourceType;

        /**
         * Constructor.<p>
         *
         * @param resourcePath the item resource path
         * @param resourceType the item resource type
         */
        public PreviewHandler(String resourcePath, String resourceType) {

            m_resourcePath = resourcePath;
            m_resourceType = resourceType;
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            getTabHandler().openPreview(m_resourcePath, m_resourceType);

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
     * UiBinder interface for the search bar.
     */
    @UiTemplate("CmsResultsTabSearchBar.ui.xml")
    interface I_CmsSearchBarUiBinder extends UiBinder<FlowPanel, SearchBar> {
        // UiBinder
    }

    /** The big thumbnails view name. */
    static final String BIG = "big";

    /** The details view name. */
    static final String DETAILS = "details";

    /** The small thumbnails view name. */
    static final String SMALL = "small";

    /** The handler for scrolling to the top of the scroll panel. */
    protected CmsResultsBackwardsScrollHandler m_backwardScrollHandler = new CmsResultsBackwardsScrollHandler(this);

    /** Stores the information if more results in the search object are available. */
    protected boolean m_hasMoreResults;

    /** The result list item which corresponds to a preset value in the editor. */
    protected CmsResultListItem m_preset;

    /** The gallery handler. */
    I_CmsGalleryHandler m_galleryHandler;

    /** The context menu handler. */
    private CmsContextMenuHandler m_contextMenuHandler;

    /** The optional dnd manager. */
    private CmsDNDHandler m_dndHandler;

    /** A HTML widget for the message if nor search params were selected. */
    private HTML m_noParamsMessage;

    /** The panel showing the search parameters. */
    private FlowPanel m_params;

    /** The view select box. */
    private CmsSelectBox m_selectView;

    /** The button to create new external link resources. */
    private CmsPushButton m_specialUploadButton;

    /** The reference to the handler of this tab. */
    CmsResultsTabHandler m_tabHandler;

    /** Set of resource types currently displayed in the result list. */
    private Set<String> m_types;

    /** The upload button. */
    private CmsUploadButton m_uploadButton;

    /** The search bar. */
    private SearchBar m_searchBar = new SearchBar();

    /** The default scope. */
    private CmsGallerySearchScope m_defaultScope;

    /**
     * The constructor.<p>
     *
     * @param tabHandler the tab handler
     * @param dndHandler the dnd manager
     * @param galleryHandler the gallery handler
     * @param scope the initial scope
     * @param defaultScope the default scope
     **/
    public CmsResultsTab(
        CmsResultsTabHandler tabHandler,
        CmsDNDHandler dndHandler,
        I_CmsGalleryHandler galleryHandler,
        CmsGallerySearchScope scope,
        CmsGallerySearchScope defaultScope) {

        super(GalleryTabId.cms_tab_results);
        m_defaultScope = defaultScope;
        m_galleryHandler = galleryHandler;
        m_additionalWidgets.add(m_searchBar);
        for (CmsGallerySearchScope choice : CmsGallerySearchScope.values()) {
            String name = Messages.get().key(choice.getKey());
            m_searchBar.getScopeSelection().addOption(choice.name(), name);
        }
        m_searchBar.getScopeSelection().selectValue(scope.name());
        m_searchBar.getSearchButton().addClickHandler(event -> {
            m_tabHandler.updateResult();
        });
        m_searchBar.getSearchInput().addValueChangeHandler(event -> {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(event.getValue()) && (event.getValue().length() >= 3)) {
                m_tabHandler.setSearchQuery(event.getValue());
            } else {
                m_tabHandler.setSearchQuery(null);
            }
        });
        m_searchBar.getSearchInput().addKeyPressHandler(event -> {
            if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                m_tabHandler.updateResult();
            }
        });
        tabHandler.addSearchChangeHandler(new ValueChangeHandler<CmsGallerySearchBean>() {

            @SuppressWarnings("synthetic-access")
            public void onValueChange(ValueChangeEvent<CmsGallerySearchBean> event) {

                // only set the query if the tab is not currently selected
                if (!isSelected()) {
                    m_searchBar.getSearchInput().setFormValueAsString(event.getValue().getQuery());
                }
            }
        });

        m_contextMenuHandler = new CmsResultContextMenuHandler(tabHandler);
        m_types = new HashSet<String>();
        m_hasMoreResults = false;
        m_dndHandler = dndHandler;
        m_tabHandler = tabHandler;
        m_params = new FlowPanel();
        m_params.setStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().tabParamsPanel());
        m_tab.insert(m_params, 0);
        getList().addScrollHandler(new CmsAsynchronousScrollToBottomHandler());
        getList().addScrollHandler(m_backwardScrollHandler);
        init();
        Map<String, String> views = new LinkedHashMap<String, String>();
        views.put(DETAILS, Messages.get().key(Messages.GUI_VIEW_LABEL_DETAILS_0));
        views.put(SMALL, Messages.get().key(Messages.GUI_VIEW_LABEL_SMALL_ICONS_0));
        views.put(BIG, Messages.get().key(Messages.GUI_VIEW_LABEL_BIG_ICONS_0));
        String resultViewType = m_tabHandler.getResultViewType();
        if (!views.containsKey(resultViewType)) {
            resultViewType = SMALL;
        }
        m_selectView = new CmsSelectBox(views);
        m_selectView.addStyleName(DIALOG_CSS.selectboxWidth());
        m_selectView.selectValue(resultViewType);
        selectView(resultViewType);
        addWidgetToOptions(m_selectView);
        m_selectView.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                selectView(event.getValue());
                setScrollPosition(0);
                onContentChange();
                getTabHandler().setResultViewType(event.getValue());
            }
        });
    }

    /**
     * Checks if the type is viewable as an image in the gallery result tab.<p>
     *
     * @param typeName the type to check
     * @return true if the type can be viewed as an image in the result tab
     */
    public static boolean isImagelikeType(String typeName) {

        return CmsGwtConstants.TYPE_IMAGE.equals(typeName);
    }

    /**
     * Clears all search parameters.<p>
     */
    @Override
    public void clearParams() {

        CmsDebugLog.getInstance().printLine("Unallowed call to clear params in result tab.");
    }

    /**
     * Fill the content of the results tab.<p>
     *
     * @param searchObj the current search object containing search results
     * @param paramPanels list of search parameter panels to show
     */
    public void fillContent(final CmsGallerySearchBean searchObj, List<CmsSearchParamPanel> paramPanels) {

        removeNoParamMessage();

        // in case there is a single type selected and the current sort order is not by type,
        // hide the type ascending and type descending sort order options
        SortParams currentSorting = SortParams.valueOf(searchObj.getSortOrder());

        // this should always be true in the results tab, but put it in an 'if' in case of future changes
        if (m_sortSelectBox instanceof CmsSelectBox) {
            if ((searchObj.getTypes().size() == 1)
                && !((currentSorting == SortParams.type_asc) || (currentSorting == SortParams.type_desc))) {
                ((CmsSelectBox)m_sortSelectBox).setItems(getSortList(false));
            } else {
                ((CmsSelectBox)m_sortSelectBox).setItems(getSortList(true));
            }
        } else {
            CmsDebugLog.consoleLog("gallery result tab sort box is not a CmsSelectBox!");
        }
        m_sortSelectBox.selectValue(searchObj.getSortOrder());
        displayResultCount(getResultsDisplayed(searchObj), searchObj.getResultCount());
        m_searchBar.getSearchInput().setFormValueAsString(searchObj.getQuery());
        if (searchObj.getScope() != null) {
            m_searchBar.getScopeSelection().setFormValue(searchObj.getScope().name());
        }
        paramPanels.addAll(getParamPanels(searchObj));

        m_hasMoreResults = searchObj.hasMore();
        if (searchObj.hasReplacedResults()) {
            m_preset = null;
            getList().scrollToTop();
            clearList();
            showParams(paramPanels);
            addContent(searchObj);
            getList().getElement().getStyle().clearDisplay();
        } else if (searchObj.getPage() == 1) {
            m_preset = null;
            getList().scrollToTop();
            clearList();
            showParams(paramPanels);
            m_backwardScrollHandler.updateSearchBean(searchObj);
            getList().getElement().getStyle().clearDisplay();
            scrollToPreset();
        } else {
            showParams(paramPanels);
            addContent(searchObj);
        }
        showUpload(searchObj);
    }

    /**
     * Returns the drag and drop handler.<p>
     *
     * @return the drag and drop handler
     */
    public CmsDNDHandler getDNDHandler() {

        return m_dndHandler;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getParamPanels(org.opencms.ade.galleries.shared.CmsGallerySearchBean)
     */
    @Override
    public List<CmsSearchParamPanel> getParamPanels(CmsGallerySearchBean searchObj) {

        List<CmsSearchParamPanel> result = new ArrayList<CmsSearchParamPanel>();
        CmsGallerySearchScope scope = CmsGallerySearchScope.valueOf(
            m_searchBar.getScopeSelection().getFormValueAsString());
        if ((scope != m_defaultScope)) {
            CmsSearchParamPanel panel = new CmsSearchParamPanel(
                Messages.get().key(Messages.GUI_PARAMS_LABEL_SCOPE_0),
                this);
            panel.setContent(Messages.get().key(scope.getKey()), ParamType.scope.name());
            result.add(panel);
        }

        String query = m_searchBar.getSearchInput().getFormValueAsString();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(query)) {
            CmsSearchParamPanel panel = new CmsSearchParamPanel(
                Messages.get().key(Messages.GUI_TAB_SEARCH_LABEL_TEXT_0),
                this);
            panel.setContent(query, ParamType.text.name());
            result.add(panel);
        }

        return result;

    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getRequiredHeight()
     */
    @Override
    public int getRequiredHeight() {

        return super.getRequiredHeight()
            + (m_searchBar.getOffsetHeight())
            + (m_params.isVisible() ? m_params.getOffsetHeight() + 5 : 21);
    }

    /**
     * Returns the delete handler.<p>
     *
     * @param resourcePath the resource path of the resource
     *
     * @return the delete handler
     */
    public DeleteHandler makeDeleteHandler(String resourcePath) {

        return new DeleteHandler(resourcePath);
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#onResize()
     */
    @Override
    public void onResize() {

        super.onResize();
        // check if more result items should be loaded to fill the available height
        if (m_hasMoreResults
            && !getTabHandler().isLoading()
            && (m_list.getOffsetHeight() > (m_scrollList.getOffsetHeight() - 100))) {
            getTabHandler().onScrollToBottom();
        }
    }

    /**
     * Removes the no params message.<p>
     */
    public void removeNoParamMessage() {

        if (m_noParamsMessage != null) {
            m_tab.remove(m_noParamsMessage);
        }
    }

    /**
     * Removes the query.
     */
    public void removeQuery() {

        m_searchBar.getSearchInput().setFormValueAsString("");
    }

    /**
     * Removes the scope.
     */
    public void removeScope() {

        m_searchBar.getScopeSelection().setFormValueAsString(m_defaultScope.name());
    }

    /**
     * Updates the height (with border) of the result list panel according to the search parameter panels shown.<p>
     */
    public void updateListSize() {

        int paramsHeight = m_params.isVisible()
        ? m_params.getOffsetHeight()
            + CmsDomUtil.getCurrentStyleInt(m_params.getElement(), CmsDomUtil.Style.marginBottom)
        : 21;
        int optionsHeight = m_options.getOffsetHeight()
            + CmsDomUtil.getCurrentStyleInt(m_options.getElement(), CmsDomUtil.Style.marginBottom);
        int addHeight = m_additionalWidgets.getOffsetHeight()
            + CmsDomUtil.getCurrentStyleInt(m_additionalWidgets.getElement(), CmsDomUtil.Style.marginBottom);

        int listTop = paramsHeight + optionsHeight + addHeight + 5;
        // another sanity check, don't set any top value below 35
        if (listTop > 35) {
            m_list.getElement().getStyle().setTop(listTop, Unit.PX);
        }
    }

    /**
     * Appends the list items for the search results from a search bean.<p>
     *
     * @param searchBean a search bean containing results
     */
    protected void addContent(CmsGallerySearchBean searchBean) {

        if (searchBean.getResults() != null) {
            boolean showPath = SortParams.path_asc.name().equals(searchBean.getSortOrder())
                || SortParams.path_desc.name().equals(searchBean.getSortOrder());
            addContentItems(searchBean.getResults(), false, showPath);
        }
    }

    /**
     * Adds list items for a list of search results.<p>
     *
     * @param list the list of search results
     * @param front if true, list items will be added to the front of the list, else at the back
     * @param showPath <code>true</code> to show the resource path in sub title
     */
    protected void addContentItems(List<CmsResultItemBean> list, boolean front, boolean showPath) {

        if (front) {
            list = Lists.reverse(list);
        }
        for (CmsResultItemBean resultItem : list) {
            addSingleResult(resultItem, front, showPath);
        }
        if (isTilingViewAllowed()) {
            m_selectView.getElement().getStyle().clearDisplay();
            selectView(m_selectView.getFormValueAsString());
        } else {
            m_selectView.getElement().getStyle().setDisplay(Display.NONE);
            selectView(DETAILS);
        }
        onContentChange();
    }

    /**
     * Adds a list item for a single search result.<p>
     *
     * @param resultItem the search result
     * @param front if true, adds the list item to the front of the list, else at the back
     * @param showPath <code>true</code> to show the resource path in sub title
     */
    protected void addSingleResult(CmsResultItemBean resultItem, boolean front, boolean showPath) {

        m_types.add(resultItem.getType());
        boolean hasPreview = m_tabHandler.hasPreview(resultItem.getType());
        CmsDNDHandler dndHandler = m_dndHandler;
        if (!m_galleryHandler.filterDnd(resultItem)) {
            dndHandler = null;
        }
        CmsResultListItem listItem = new CmsResultListItem(resultItem, hasPreview, showPath, dndHandler);
        if (resultItem.isPreset()) {
            m_preset = listItem;
        }
        if (hasPreview) {
            listItem.addPreviewClickHandler(new PreviewHandler(resultItem.getPath(), resultItem.getType()));
        }
        CmsUUID structureId = new CmsUUID(resultItem.getClientId());
        listItem.getListItemWidget().addButton(
            new CmsContextMenuButton(structureId, m_contextMenuHandler, AdeContext.gallery));
        listItem.getListItemWidget().addOpenHandler(new OpenHandler<CmsListItemWidget>() {

            public void onOpen(OpenEvent<CmsListItemWidget> event) {

                onContentChange();
            }
        });
        if (m_tabHandler.hasSelectResource()) {
            SelectHandler selectHandler = new SelectHandler(
                resultItem.getPath(),
                structureId,
                resultItem.getRawTitle(),
                resultItem.getType());
            listItem.addSelectClickHandler(selectHandler);

            // this affects both tiled and non-tiled result lists.
            listItem.addDoubleClickHandler(selectHandler);
        }
        m_galleryHandler.processResultItem(listItem);
        if (front) {
            addWidgetToFrontOfList(listItem);
        } else {
            addWidgetToList(listItem);
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#clearList()
     */
    @Override
    protected void clearList() {

        super.clearList();
        m_types.clear();
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected LinkedHashMap<String, String> getSortList() {

        return getSortList(true);
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getTabHandler()
     */
    @Override
    protected CmsResultsTabHandler getTabHandler() {

        return m_tabHandler;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#hasQuickFilter()
     */
    @Override
    protected boolean hasQuickFilter() {

        // quick filter not available for this tab
        return false;
    }

    /**
     * Scrolls to the result which corresponds to a preset value in the editor.<p>
     */
    protected void scrollToPreset() {

        final ScrollPanel scrollPanel = getList();
        if (m_preset != null) {
            Widget child = scrollPanel.getWidget();
            if (child instanceof CmsList<?>) {
                @SuppressWarnings("unchecked")
                CmsList<I_CmsListItem> list = (CmsList<I_CmsListItem>)child;
                if (list.getWidgetCount() > 0) {
                    final Widget first = (Widget)list.getItem(0);
                    Timer timer = new Timer() {

                        @Override
                        public void run() {

                            int firstTop = first.getElement().getAbsoluteTop();
                            int presetTop = m_preset.getElement().getAbsoluteTop();
                            final int offset = presetTop - firstTop;
                            if (offset >= 0) {
                                scrollPanel.setVerticalScrollPosition(offset);
                            } else {
                                // something is seriously wrong with the positioning if this case occurs
                                scrollPanel.scrollToBottom();
                            }
                        }
                    };
                    timer.schedule(10);
                }
            }
        }
    }

    /**
     * Helper for setting the scroll position of the scroll panel.<p>
     *
     * @param pos the scroll position
     */
    protected void setScrollPosition(final int pos) {

        getList().setVerticalScrollPosition(pos);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            /**
             * @see com.google.gwt.core.client.Scheduler.ScheduledCommand#execute()
             */
            public void execute() {

                if (getList().getVerticalScrollPosition() != pos) {
                    getList().setVerticalScrollPosition(pos);
                }

            }
        });

    }

    /**
     * Selects the view with the given name.<p>
     *
     * @param viewName the view name
     */
    void selectView(String viewName) {

        if (DETAILS.equals(viewName)) {
            getList().removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().tilingList());
        } else if (SMALL.equals(viewName)) {
            getList().addStyleName(I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().tilingList());
            getList().addStyleName(I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().smallThumbnails());
        } else if (BIG.equals(viewName)) {
            getList().addStyleName(I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().tilingList());
            getList().removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().smallThumbnails());
        }
    }

    /**
     * Creates special upload button (for external link galleries or galleries with custom upload actions).
     *
     * @param gallery the gallery bean
     *
     * @return the new button
     */
    private CmsPushButton createSpecialUploadButton(CmsGalleryFolderBean gallery) {

        if (CmsEditExternalLinkDialog.LINK_GALLERY_RESOURCE_TYPE_NAME.equals(gallery.getType())) {
            return createNewExternalLinkButton(gallery.getPath());
        } else if (gallery.getUploadAction() != null) {
            CmsPushButton uploadButton = new CmsPushButton(I_CmsButton.UPLOAD_SMALL);
            uploadButton.setText(null);
            uploadButton.setTitle(Messages.get().key(Messages.GUI_GALLERY_UPLOAD_TITLE_1, gallery.getPath()));
            uploadButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
            uploadButton.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    CmsRpcAction<CmsUUID> action = new CmsRpcAction<CmsUUID>() {

                        @Override
                        public void execute() {

                            start(0, true);
                            CmsCoreProvider.getVfsService().getStructureId(gallery.getPath(), this);
                        }

                        @Override
                        protected void onResponse(CmsUUID result) {

                            stop(false);
                            List<CmsUUID> resultIds = new ArrayList<>();
                            resultIds.add(result);
                            CmsEmbeddedDialogHandler.openDialog(
                                gallery.getUploadAction(),
                                resultIds,
                                id -> getTabHandler().updateIndex());
                        }

                    };
                    action.execute();
                }
            });
            return uploadButton;
        } else {
            return null;
        }
    }

    /**
     * Displays the result count.<p>
     *
     * @param displayed the displayed result items
     * @param total the total of result items
     */
    private void displayResultCount(int displayed, int total) {

        String message = Messages.get().key(
            Messages.GUI_LABEL_NUM_RESULTS_2,
            Integer.valueOf(displayed),
            Integer.valueOf(total));
        m_infoLabel.setText(message);
    }

    /**
     * Returns the count of the currently displayed results.<p>
     *
     * @param searchObj the search bean
     *
     * @return the count of the currently displayed results
     */
    private int getResultsDisplayed(CmsGallerySearchBean searchObj) {

        if (searchObj.hasReplacedResults()) {
            return searchObj.getResults().size();
        }
        int resultsDisplayed = searchObj.getMatchesPerPage() * searchObj.getLastPage();
        return (resultsDisplayed > searchObj.getResultCount()) ? searchObj.getResultCount() : resultsDisplayed;
    }

    /**
     * Returns the list of properties to sort the results according to.<p>
     *
     * @param includeType <code>true</code> to include sort according to type
     *
     * @return the sort list
     */
    private LinkedHashMap<String, String> getSortList(boolean includeType) {

        LinkedHashMap<String, String> list = new LinkedHashMap<String, String>();
        list.put(SortParams.title_asc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_TITLE_ASC_0));
        list.put(SortParams.title_desc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_TITLE_DECS_0));
        list.put(
            SortParams.dateLastModified_asc.name(),
            Messages.get().key(Messages.GUI_SORT_LABEL_DATELASTMODIFIED_ASC_0));
        list.put(
            SortParams.dateLastModified_desc.name(),
            Messages.get().key(Messages.GUI_SORT_LABEL_DATELASTMODIFIED_DESC_0));
        list.put(SortParams.path_asc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_PATH_ASC_0));
        list.put(SortParams.path_desc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_PATH_DESC_0));
        list.put(SortParams.score.name(), Messages.get().key(Messages.GUI_SORT_LABEL_SCORE_0));
        if (includeType) {
            list.put(SortParams.type_asc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_TYPE_ASC_0));
            list.put(SortParams.type_desc.name(), Messages.get().key(Messages.GUI_SORT_LABEL_TYPE_DESC_0));
        }
        return list;
    }

    /**
     * Checks if the thumbnail tiling view is allowed for the given result items.<p>
     *
     * @return <code>true</code> if the thumbnail tiling view is allowed for the given result items
     */
    private boolean isTilingViewAllowed() {

        if (m_types.size() == 0) {
            return false;
        }
        for (String typeName : m_types) {
            if (!isImagelikeType(typeName)) {
                return false;
            }
        }
        return true;

    }

    /**
     * Check if we need a special upload button for the gallery.
     *
     * @param gallery a gallery bean
     * @return true if we need a special upload button
     */
    private boolean needsSpecialButton(CmsGalleryFolderBean gallery) {

        return (gallery.getUploadAction() != null)
            || CmsEditExternalLinkDialog.LINK_GALLERY_RESOURCE_TYPE_NAME.equals(gallery.getType());

    }

    /**
     * Displays the selected search parameters in the result tab.<p>
     *
     * @param paramPanels the list of search parameter panels to show
     *
     */
    private void showParams(List<CmsSearchParamPanel> paramPanels) {

        m_params.clear();
        if ((paramPanels == null) || (paramPanels.size() == 0)) {
            m_params.setVisible(false);
            updateListSize();
            return;
        }
        m_params.setVisible(true);
        for (CmsSearchParamPanel panel : paramPanels) {
            m_params.add(panel);
        }
        updateListSize();
    }

    /**
     * Shows the upload button if appropriate.<p>
     *
     * @param searchObj the current search object
     */
    private void showUpload(CmsGallerySearchBean searchObj) {

        boolean uploadDisabled = CmsCoreProvider.get().isUploadDisabled();
        Set<String> targets = new HashSet<String>();

        if (searchObj.getGalleries() != null) {
            targets.addAll(searchObj.getGalleries());
        }
        if (searchObj.getFolders() != null) {
            targets.addAll(searchObj.getFolders());
        }
        if (m_specialUploadButton != null) {
            m_specialUploadButton.removeFromParent();
            m_specialUploadButton = null;
        }
        if (m_uploadButton == null) {
            m_uploadButton = createUploadButtonForTarget("", false);
            m_uploadButton.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().resultTabUpload());
            m_tab.insert(m_uploadButton, 0);
        } else {
            m_uploadButton.getElement().getStyle().clearDisplay();
        }
        if (targets.size() == 1) {
            CmsGalleryFolderBean galleryFolder = getTabHandler().getGalleryInfo(targets.iterator().next());

            if ((galleryFolder != null) && needsSpecialButton(galleryFolder)) {

                m_specialUploadButton = createSpecialUploadButton(galleryFolder);
                if (m_specialUploadButton != null) {
                    m_specialUploadButton.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().resultTabUpload());
                    m_tab.insert(m_specialUploadButton, 0);
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(searchObj.getNoUploadReason())) {
                        m_specialUploadButton.enable();
                    } else {
                        m_specialUploadButton.disable(searchObj.getNoUploadReason());
                    }
                }
                m_uploadButton.getElement().getStyle().setDisplay(Display.NONE);
            } else {
                String uploadTarget = targets.iterator().next();
                I_CmsUploadButtonHandler handler = m_uploadButton.getButtonHandler();
                if (handler instanceof CmsDialogUploadButtonHandler) {
                    ((CmsDialogUploadButtonHandler)handler).setTargetFolder(uploadTarget);
                    // in case the upload target is a folder the root path is used
                    ((CmsDialogUploadButtonHandler)handler).setIsTargetRootPath(searchObj.getFolders().size() == 1);
                    m_uploadButton.updateFileInput();
                }
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(searchObj.getNoUploadReason())) {
                    m_uploadButton.enable();
                    m_uploadButton.setTitle(Messages.get().key(Messages.GUI_GALLERY_UPLOAD_TITLE_1, uploadTarget));
                } else {
                    m_uploadButton.disable(searchObj.getNoUploadReason());
                }
            }
        } else {
            m_uploadButton.disable(Messages.get().key(Messages.GUI_GALLERY_UPLOAD_TARGET_UNSPECIFIC_0));
        }
        if (uploadDisabled) {

            for (Widget button : new Widget[] {m_uploadButton, m_specialUploadButton}) {
                if (button != null) {
                    button.removeFromParent();
                }
            }
            m_uploadButton = null;
            m_specialUploadButton = null;
        }

    }
}
