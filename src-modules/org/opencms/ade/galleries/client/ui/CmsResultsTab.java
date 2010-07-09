/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsResultsTab.java,v $
 * Date   : $Date: 2010/07/09 07:04:03 $
 * Version: $Revision: 1.25 $
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

import org.opencms.ade.galleries.client.CmsResultsTabHandler;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPair;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides the widget for the results tab.<p>
 * 
 * It displays the selected search parameter, the sort order and
 * the search results for the current search.
 * 
 * @author Polina Smagina
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.25 $
 * 
 * @since 8.0.
 */
public class CmsResultsTab extends A_CmsListTab {

    /**
     * Scroll handler which executes an action when the user has scrolled to the bottom.<p>
     * 
     * @author Georg Westenberger
     * @author Ruediger Kurz
     * 
     * @version $Revision: 1.25 $
     * 
     * @since 8.0.0
     */
    protected class CmsScrollToBottomAsynchronHandler implements ScrollHandler {

        /**
         * If the lower edge of the content being scrolled is at most this many pixels below the lower
         * edge of the scrolling viewport, the action is triggered.
         */
        public static final int DEFAULT_SCROLL_THRESHOLD = 20;

        /**
         * Constructs a new scroll handler with a custom scroll threshold.
         * 
         * The scroll threshold is the distance from the bottom edge of the scrolled content
         * such that when the distance from the bottom edge of the scroll viewport to the bottom
         * edge of the scrolled content becomes lower than the distance, the scroll action is triggered.
         * 
         */
        public CmsScrollToBottomAsynchronHandler() {

            // noop
        }

        /**
         * @see com.google.gwt.event.dom.client.ScrollHandler#onScroll(com.google.gwt.event.dom.client.ScrollEvent)
         */
        public void onScroll(ScrollEvent event) {

            if (!m_hasMoreResults) {
                return;
            }
            final ScrollPanel scrollPanel = (ScrollPanel)event.getSource();
            final int scrollPos = scrollPanel.getScrollPosition();
            Widget child = scrollPanel.getWidget();
            int childHeight = child.getOffsetHeight();
            int ownHeight = scrollPanel.getOffsetHeight();
            boolean isBottom = scrollPos + ownHeight >= childHeight - DEFAULT_SCROLL_THRESHOLD;
            if (isBottom) {
                getTabHandler().onScrollToBottom();
                setScrollPosition(scrollPos);
            }
        }
    }

    /**
     * Special click handler to use with preview button.<p>
     */
    private class PreviewHandler implements ClickHandler {

        /** The id of the selected item. */
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
    private class SelectHandler implements ClickHandler {

        /** The id of the selected item. */
        private String m_resourcePath;

        /** The resource type of the selected item. */
        private String m_resourceType;

        /** The resource title. */
        private String m_title;

        /**
         * Constructor.<p>
         * 
         * @param resourcePath the item resource path 
         * @param title the resource title
         * @param resourceType the item resource type
         */
        public SelectHandler(String resourcePath, String title, String resourceType) {

            m_resourcePath = resourcePath;
            m_resourceType = resourceType;
            m_title = title;
        }

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            getTabHandler().selectResource(m_resourcePath, m_title, m_resourceType);
        }
    }

    /** Text metrics key. */
    private static final String TM_RESULT_TAB = "ResultTab";

    /** Stores the information if more results in the search object are available. */
    protected boolean m_hasMoreResults;

    /** The reference to the drag handler for the list elements. */
    private I_CmsDragHandler<?, ?> m_dragHandler;

    /** The panel showing the search parameters. */
    private FlowPanel m_params;

    /** The reference to the handler of this tab. */
    private CmsResultsTabHandler m_tabHandler;

    /**
     * The constructor.<p>
     * 
     * @param tabHandler the tab handler 
     * @param dragHandler the drag handler
     */
    public CmsResultsTab(CmsResultsTabHandler tabHandler, I_CmsDragHandler<?, ?> dragHandler) {

        super(GalleryTabId.cms_tab_results);
        m_hasMoreResults = false;
        m_dragHandler = dragHandler;
        m_tabHandler = tabHandler;
        m_scrollList.truncate(TM_RESULT_TAB, CmsGalleryDialog.DIALOG_WIDTH);
        m_params = new FlowPanel();
        m_params.setStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().tabOptions());
        m_tab.insert(m_params, 0);
        getList().addScrollHandler(new CmsScrollToBottomAsynchronHandler());
    }

    /**
     * Generates the result list items and adds them to the widget.<p>
     * 
     * @param searchObj the current search object containing search results
     */
    public void addContent(CmsGallerySearchBean searchObj) {

        List<CmsResultItemBean> list = searchObj.getResults();
        for (CmsResultItemBean resultItem : list) {

            CmsListItemWidget resultItemWidget;
            CmsListInfoBean infoBean = new CmsListInfoBean(resultItem.getTitle(), resultItem.getDescription(), null);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(resultItem.getExcerpt())) {
                infoBean.addAdditionalInfo(
                    Messages.get().key(Messages.GUI_RESULT_LABEL_EXCERPT_0),
                    resultItem.getExcerpt());
            }
            if (m_dragHandler != null) {
                resultItemWidget = m_dragHandler.createDraggableListItemWidget(infoBean, resultItem.getClientId());
            } else {
                resultItemWidget = new CmsListItemWidget(infoBean);
            }
            // add  preview button
            CmsPushButton previewButton = new CmsPushButton();
            previewButton.setImageClass(I_CmsImageBundle.INSTANCE.style().magnifierIcon());
            previewButton.setShowBorder(false);
            previewButton.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
            previewButton.addClickHandler(new PreviewHandler(resultItem.getPath(), resultItem.getType()));
            resultItemWidget.addButton(previewButton);
            if (m_tabHandler.hasSelectResource()) {
                CmsPushButton selectButton = new CmsPushButton();
                // TODO: use different icon
                selectButton.setImageClass(I_CmsImageBundle.INSTANCE.style().newIcon());
                selectButton.setShowBorder(false);
                selectButton.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
                selectButton.addClickHandler(new SelectHandler(
                    resultItem.getPath(),
                    resultItem.getTitle(),
                    resultItem.getType()));
                resultItemWidget.addButton(selectButton);
            }
            // add file icon
            resultItemWidget.setIcon(CmsIconUtil.getResourceIconClasses(
                resultItem.getType(),
                resultItem.getPath(),
                false));
            CmsResultListItem listItem = new CmsResultListItem(resultItemWidget);
            listItem.setId(resultItem.getPath());
            addWidgetToList(listItem);
        }
    }

    /**
     * Clears all search parameters.<p>
     */
    @Override
    public void clearParams() {

        CmsDebugLog.getInstance().printLine("Unalowed call to clear params in result tab.");
    }

    /**
     * Fill the content of the results tab.<p>
     * 
     * @param searchObj the current search object containing search results
     * @param paramPanels list of search parameter panels to show
     */
    public void fillContent(CmsGallerySearchBean searchObj, List<CmsSearchParamPanel> paramPanels) {

        m_hasMoreResults = searchObj.hasMore();
        if (searchObj.getPage() == 1) {
            getList().scrollToTop();
            getList().getElement().getStyle().setDisplay(Display.NONE);
            clearList();
            showParams(paramPanels);
            addContent(searchObj);
            getList().getElement().getStyle().clearDisplay();
        } else {
            showParams(paramPanels);
            addContent(searchObj);
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#getParamPanel(org.opencms.ade.galleries.shared.CmsGallerySearchBean)
     */
    @Override
    public CmsSearchParamPanel getParamPanel(CmsGallerySearchBean searchObj) {

        // not available for this tab
        return null;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsTab#onSelection()
     */
    @Override
    public void onSelection() {

        super.onSelection();
        updateListSize();
    }

    //    /**
    //     * Updates the content of the results tab.<p>
    //     * 
    //     * @param searchObj the current search object containing search results
    //     * @param typesParams the selected types as a user-readable string
    //     * @param galleriesParams the selected galleries as a user-readable string 
    //     * @param foldersParams the  selected VFS folders as a user-readable string
    //     * @param categoriesParams the selected categories as a user-readable string 
    //     */
    //    public void updateContent(
    //        CmsGallerySearchBean searchObj,
    //        String typesParams,
    //        String galleriesParams,
    //        String foldersParams,
    //        String categoriesParams) {
    //
    //        clearList();
    //        fillContent(searchObj, typesParams, galleriesParams, foldersParams, categoriesParams);
    //    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getSortList()
     */
    @Override
    protected ArrayList<CmsPair<String, String>> getSortList() {

        ArrayList<CmsPair<String, String>> list = new ArrayList<CmsPair<String, String>>();
        list.add(new CmsPair<String, String>(SortParams.title_asc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TITLE_ASC_0)));
        list.add(new CmsPair<String, String>(SortParams.title_desc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TITLE_DECS_0)));
        list.add(new CmsPair<String, String>(SortParams.type_asc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TYPE_ASC_0)));
        list.add(new CmsPair<String, String>(SortParams.type_desc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_TYPE_DESC_0)));
        list.add(new CmsPair<String, String>(SortParams.dateLastModified_asc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_DATELASTMODIFIED_ASC_0)));
        list.add(new CmsPair<String, String>(SortParams.dateLastModified_desc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_DATELASTMODIFIED_DESC_0)));
        list.add(new CmsPair<String, String>(SortParams.path_asc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_PATH_ASC_0)));
        list.add(new CmsPair<String, String>(SortParams.path_desc.name(), Messages.get().key(
            Messages.GUI_SORT_LABEL_PATH_DESC_0)));

        return list;
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.A_CmsListTab#getTabHandler()
     */
    @Override
    protected CmsResultsTabHandler getTabHandler() {

        return m_tabHandler;
    }

    /**
     * Helper for setting the scroll position of the scroll panel.<p>
     * 
     * @param pos the scroll position
     */
    protected void setScrollPosition(final int pos) {

        getList().setScrollPosition(pos);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            /**
             * @see com.google.gwt.core.client.Scheduler.ScheduledCommand#execute()
             */
            public void execute() {

                if (getList().getScrollPosition() != pos) {
                    getList().setScrollPosition(pos);
                }

            }
        });

    }

    /**
     * Displays the selected search parameters in the result tab.<p>
     * 
     * @param searchObj the bean containing the search parameters 
     * @param typesParams a user-readable string containing the selected types
     * @param galleriesParams a user-readable string containing the selected galleries
     * @param foldersParams a user-readable string containing
     * @param categoriesParams
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
     * Updates the height (with border) of the result list panel according to the search parameter panels shown.<p>    
     */
    private void updateListSize() {

        int tabHeight = m_tab.getElement().getClientHeight();
        CmsDebugLog.getInstance().printLine("updating size, tabHeight: " + tabHeight);
        // sanity check on tab height
        tabHeight = tabHeight > 0 ? tabHeight : 434;

        int paramsHeight = m_params.isVisible() ? m_params.getOffsetHeight()
            + CmsDomUtil.getCurrentStyleInt(m_params.getElement(), CmsDomUtil.Style.marginBottom) : 0;

        int optionsHeight = m_options.getOffsetHeight()
            + CmsDomUtil.getCurrentStyleInt(m_options.getElement(), CmsDomUtil.Style.marginBottom);

        // 3 is some offset, because of the list border
        int newListSize = tabHeight - paramsHeight - optionsHeight - 2;
        CmsDebugLog.getInstance().printLine(" paramsHeight: " + paramsHeight + " optionsHeight: " + optionsHeight);
        // another sanity check, don't set any negative height 
        if (newListSize > 0) {
            m_list.getElement().getStyle().setHeight(newListSize, Unit.PX);
        }
    }
}