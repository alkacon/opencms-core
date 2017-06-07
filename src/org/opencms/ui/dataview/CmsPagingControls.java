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

package org.opencms.ui.dataview;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;

import java.util.List;

import com.google.common.collect.Lists;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * Set of buttons allowing the user to navigate between pages in a paged list.<p>
 */
public class CmsPagingControls extends HorizontalLayout {

    /**
     * Callback which is called when the user changes the page.<p>
     */
    public static interface I_PagingCallback {

        /**
         * Method which is called when the user changes the page.<p>
         *
         * @param page the page number
         */
        void pageChanged(int page);
    }

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The registered callback. */
    private List<I_PagingCallback> m_callbacks = Lists.newArrayList();

    /** The page number. */
    private int m_page;

    /** Button to go to the next page. */
    private Button m_forward = new Button(FontAwesome.FORWARD);

    /** Button to go to the last page. */
    private Button m_fastForward = new Button(FontAwesome.FAST_FORWARD);

    /** Button to go to the previous page. */
    private Button m_back = new Button(FontAwesome.BACKWARD);

    /** Button to go the first page. */
    private Button m_fastBack = new Button(FontAwesome.FAST_BACKWARD);

    /** Label to show the current page number. */
    private Label m_label = new Label();

    /** The results label. */
    private Label m_resultsLabel = new Label();

    /** The index of the last page. */
    private int m_lastPage;

    /** The page size. */
    private int m_pageSize;

    /** The result count. */
    private int m_resultCount;

    /**
     * Creates a new instance.<p>
     */
    public CmsPagingControls() {
        setMargin(true);
        addComponent(m_label);
        addComponent(m_fastBack);
        addComponent(m_back);
        addComponent(m_forward);
        addComponent(m_fastForward);
        addComponent(m_resultsLabel);
        m_resultsLabel.setWidthUndefined();
        m_label.setWidthUndefined();
        setExpandRatio(m_resultsLabel, 1.0f);
        setComponentAlignment(m_resultsLabel, Alignment.TOP_RIGHT);
        setSpacing(true);

        m_forward.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void buttonClick(ClickEvent event) {

                setPage(Math.min(m_lastPage, m_page + 1), true);
                updateButtons();
            }
        });

        m_fastForward.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void buttonClick(ClickEvent event) {

                setPage(m_lastPage, true);
                updateButtons();

            }
        });
        m_back.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void buttonClick(ClickEvent event) {

                setPage(Math.max(0, m_page - 1), true);
                updateButtons();

            }

        });

        m_fastBack.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                setPage(0, true);
                updateButtons();

            }
        });
    }

    /**
     * Adds a new page change callback.<p>
     *
     * @param callback the callback
     */
    public void addCallback(I_PagingCallback callback) {

        m_callbacks.add(callback);
    }

    /**
     * Notifies the registered listeners of a page change.<p>
     *
     * @param pageNum the page index
     */
    public void firePageChanged(int pageNum) {

        for (I_PagingCallback callback : m_callbacks) {
            callback.pageChanged(pageNum);
        }
    }

    /**
     * Gets the current page number.<p>
     *
     * @return the current page number
     */
    public int getPage() {

        return m_page;
    }

    /**
     * Removes a registered callback.<p>
     *
     * @param callback the callback to remove
     */
    public void removeCallback(I_PagingCallback callback) {

        m_callbacks.remove(callback);
    }

    /**
     * Resets the paging controls (used when the size of the underlying list changes).<p>
     *
     * @param resultCount total number of results
     * @param pageSize size of a page
     * @param fireChanged true if the listeners should be notified
     */
    public void reset(int resultCount, int pageSize, boolean fireChanged) {

        m_lastPage = resultCount == 0 ? 0 : (resultCount - 1) / pageSize;
        m_resultCount = resultCount;
        m_pageSize = pageSize;
        setPage(0, false);
        updateButtons();
        if (fireChanged) {
            firePageChanged(0);
        }
    }

    /**
     * Sets the page index.<p>
     *
     * @param page the page index
     * @param fireChanged true if the registered listeners should be notified
     */
    public void setPage(int page, boolean fireChanged) {

        m_page = page;
        m_label.setValue("( " + (1 + m_page) + " / " + (m_lastPage + 1) + " )");
        int start = (m_page * m_pageSize) + 1;
        int end = Math.min((start + m_pageSize) - 1, m_resultCount);
        String resultsMsg = CmsVaadinUtils.getMessageText(
            Messages.GUI_DATAVIEW_RESULTS_3,
            "" + start,
            "" + end,
            "" + m_resultCount);
        m_resultsLabel.setValue(start <= end ? resultsMsg : "");
        if (fireChanged) {
            firePageChanged(page);
        }
    }

    /**
     * Updates the button state, i.e. enables/disables the buttons depending on whether we are on the first or last page or not.<p>
     */
    public void updateButtons() {

        for (Button button : new Button[] {m_forward, m_fastForward}) {
            button.setEnabled(m_page < m_lastPage);
        }
        for (Button button : new Button[] {m_back, m_fastBack}) {
            button.setEnabled(m_page > 0);
        }
    }

}
