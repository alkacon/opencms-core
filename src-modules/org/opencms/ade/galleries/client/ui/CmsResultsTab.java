/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsResultsTab.java,v $
 * Date   : $Date: 2010/09/14 14:20:24 $
 * Version: $Revision: 1.32 $
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
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.util.CmsPair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * @version $Revision: 1.32 $
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
     * @version $Revision: 1.32 $
     * 
     * @since 8.0.0
     */
    protected class CmsAsynchronousScrollToBottomHandler implements ScrollHandler {

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
        public CmsAsynchronousScrollToBottomHandler() {

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
    protected class PreviewHandler implements ClickHandler {

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
    protected class SelectHandler implements ClickHandler {

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

    /** The optional dnd manager. */
    private CmsDNDHandler m_dndHandler;

    /** The reference to the drag handler for the list elements. */
    private I_CmsDragHandler<?, ?> m_dragHandler;

    /** The panel showing the search parameters. */
    private FlowPanel m_params;

    /** The reference to the handler of this tab. */
    private CmsResultsTabHandler m_tabHandler;

    private Set<String> m_types;

    /**
     * The constructor.<p>
     * 
     * @param tabHandler the tab handler 
     * @param dragHandler the drag handler
     * @param dndHandler the dnd manager
     */
    public CmsResultsTab(CmsResultsTabHandler tabHandler, I_CmsDragHandler<?, ?> dragHandler, CmsDNDHandler dndHandler) {

        super(GalleryTabId.cms_tab_results);
        m_types = new HashSet<String>();
        m_hasMoreResults = false;
        m_dragHandler = dragHandler;
        m_dndHandler = dndHandler;
        m_tabHandler = tabHandler;
        m_scrollList.truncate(TM_RESULT_TAB, CmsGalleryDialog.DIALOG_WIDTH);
        m_params = new FlowPanel();
        m_params.setStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().tabOptions());
        m_tab.insert(m_params, 0);
        getList().addScrollHandler(new CmsAsynchronousScrollToBottomHandler());
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
     * Generates the result list items and adds them to the widget.<p>
     * 
     * @param searchObj the current search object containing search results
     */
    private void addContent(CmsGallerySearchBean searchObj) {

        List<CmsResultItemBean> list = searchObj.getResults();
        if (list == null) {
            return;
        }
        for (CmsResultItemBean resultItem : list) {
            m_types.add(resultItem.getType());
            CmsResultListItem listItem = new CmsResultListItem(resultItem, m_dragHandler, m_dndHandler);
            listItem.addPreviewClickHandler(new PreviewHandler(resultItem.getPath(), resultItem.getType()));
            if (m_tabHandler.hasSelectResource()) {
                listItem.addSelectClickHandler(new SelectHandler(
                    resultItem.getPath(),
                    resultItem.getTitle(),
                    resultItem.getType()));
            }
            addWidgetToList(listItem);
        }
        if (m_types.size() == 1) {
            getList().addStyleName(I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().tilingList());
        } else {
            getList().removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().tilingList());
        }
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
     * Updates the height (with border) of the result list panel according to the search parameter panels shown.<p>    
     */
    private void updateListSize() {

        int tabHeight = m_tab.getElement().getClientHeight();
        // sanity check on tab height
        tabHeight = tabHeight > 0 ? tabHeight : 434;

        int paramsHeight = m_params.isVisible() ? m_params.getOffsetHeight()
            + CmsDomUtil.getCurrentStyleInt(m_params.getElement(), CmsDomUtil.Style.marginBottom) : 0;

        int optionsHeight = m_options.getOffsetHeight()
            + CmsDomUtil.getCurrentStyleInt(m_options.getElement(), CmsDomUtil.Style.marginBottom);

        // 3 is some offset, because of the list border
        int newListSize = tabHeight - paramsHeight - optionsHeight - 2;
        // another sanity check, don't set any negative height 
        if (newListSize > 0) {
            m_list.getElement().getStyle().setHeight(newListSize, Unit.PX);
        }
    }
}