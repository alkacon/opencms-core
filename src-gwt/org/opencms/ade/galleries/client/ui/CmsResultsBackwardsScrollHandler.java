/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;

import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * Scroll handler class which loads more items in the front of the search result list when the user scrolls to the
 * top.<p>
 */
public class CmsResultsBackwardsScrollHandler implements ScrollHandler {

    /** The scroll threshold in pixels. */
    public static final int SCROLL_THRESHOLD = 50;

    /** Flag used to temporarily disable the scroll handler. */
    protected boolean m_enabled = true;

    /** The index of the first shown page. */
    protected int m_firstShownPage;

    /** The number of results per page. */
    protected int m_pageSize;

    /** The list of search result beans. */
    protected List<CmsResultItemBean> m_resultBeans;

    /** The search results tab. */
    protected CmsResultsTab m_resultsTab;

    /** The search bean. */
    protected CmsGallerySearchBean m_searchBean;

    /**
     * Creates a new handler instance for a given results tab.<p>
     *
     * @param resultsTab the results tab for which to create the handler
     */
    public CmsResultsBackwardsScrollHandler(CmsResultsTab resultsTab) {

        m_resultsTab = resultsTab;
    }

    /**
     * Checks whether more items can be loaded at the front of the list.<p>
     *
     * @return true if more items can be loaded at the front of the list
     */
    public boolean hasMore() {

        return m_firstShownPage > 1;
    }

    /**
     * @see com.google.gwt.event.dom.client.ScrollHandler#onScroll(com.google.gwt.event.dom.client.ScrollEvent)
     */
    public void onScroll(ScrollEvent event) {

        if (m_searchBean == null) {
            return;
        }
        if (m_resultBeans == null) {
            return;
        }
        if (!m_enabled) {
            return;
        }
        final ScrollPanel scrollPanel = (ScrollPanel)event.getSource();
        final int scrollPos = scrollPanel.getVerticalScrollPosition();
        if ((scrollPos <= SCROLL_THRESHOLD) && hasMore()) {
            m_enabled = false;
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                /**
                 * @see com.google.gwt.core.client.Scheduler.ScheduledCommand#execute()
                 */
                public void execute() {

                    m_enabled = true;
                    m_resultsTab.getList().setVerticalScrollPosition(50);
                }
            });
            loadPreviousPage();
        }

    }

    /**
     * Updates the handler with a new search bean.<p>
     *
     * @param searchBean the search bean
     */
    public void updateSearchBean(CmsGallerySearchBean searchBean) {

        m_searchBean = searchBean;
        if (searchBean != null) {
            m_pageSize = searchBean.getMatchesPerPage();
            int lastPage = searchBean.getLastPage();
            // we don't just store the search bean because it gets reused for multiple searches
            // and so the result list may change.
            m_resultBeans = searchBean.getResults();
            if (lastPage != -1) {
                loadPage(lastPage);
                m_firstShownPage = lastPage;
                if (lastPage > 1) {
                    loadPage(lastPage - 1);
                    m_firstShownPage = lastPage - 1;
                }
            }
        } else {
            m_resultBeans = null;
        }
    }

    /**
     * Loads a page with a given index.<p>
     *
     * @param pageNum the index of the page to load
     */
    protected void loadPage(int pageNum) {

        int start = (pageNum - 1) * m_pageSize;
        List<CmsResultItemBean> results = m_resultBeans;
        int end = start + m_pageSize;
        if (end > results.size()) {
            end = results.size();
        }
        List<CmsResultItemBean> page = results.subList(start, end);
        boolean showPath = SortParams.path_asc.name().equals(m_searchBean.getSortOrder())
            || SortParams.path_desc.name().equals(m_searchBean.getSortOrder());
        m_resultsTab.addContentItems(page, true, showPath);
    }

    /**
     * Loads the page before the first shown page.<p>
     */
    protected void loadPreviousPage() {

        loadPage(m_firstShownPage - 1);
        m_firstShownPage -= 1;
    }

}
