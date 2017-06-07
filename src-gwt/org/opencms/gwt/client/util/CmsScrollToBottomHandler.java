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

package org.opencms.gwt.client.util;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Scroll handler which executes an action when the user has scrolled to the bottom.<p>
 *
 * @since 8.0.0
 */
public class CmsScrollToBottomHandler implements ScrollHandler {

    /**
     * If the lower edge of the content being scrolled is at most this many pixels below the lower
     * edge of the scrolling viewport, the action is triggered.
     */
    public static final int DEFAULT_SCROLL_THRESHOLD = 30;

    /**
     * An internal flag that, if false, prevents the scroll action from being executed.<p>
     */
    protected boolean m_enabled = true;

    /**
     * The action which is triggered when the user scrolls to the bottom.<p>
     */
    private Runnable m_callback;

    /**
     * If the lower edge of the content being scrolled is at most this many pixels below the lower
     * edge of the scrolling viewport, the action is triggered.
     */
    private int m_scrollThreshold = DEFAULT_SCROLL_THRESHOLD;

    /**
     * Constructs a new scroll handler.<p>
     *
     * @param callback the action which should be executed when the user scrolls to the bottom.
     */
    public CmsScrollToBottomHandler(Runnable callback) {

        m_callback = callback;
    }

    /**
     * Constructs a new scroll handler with a custom scroll threshold.
     *
     * The scroll threshold is the distance from the bottom edge of the scrolled content
     * such that when the distance from the bottom edge of the scroll viewport to the bottom
     * edge of the scrolled content becomes lower than the distance, the scroll action is triggered.
     *
     * @param callback the action which should be executed when the user scrolls to the bottom.
     * @param scrollThreshold the scroll threshold
     */
    public CmsScrollToBottomHandler(Runnable callback, int scrollThreshold) {

        m_callback = callback;
        m_scrollThreshold = scrollThreshold;
    }

    /**
     * @see com.google.gwt.event.dom.client.ScrollHandler#onScroll(com.google.gwt.event.dom.client.ScrollEvent)
     */
    public void onScroll(ScrollEvent event) {

        if (!m_enabled) {
            return;
        }
        final ScrollPanel scrollPanel = (ScrollPanel)event.getSource();
        final int scrollPos = scrollPanel.getVerticalScrollPosition();
        Widget child = scrollPanel.getWidget();
        int childHeight = child.getOffsetHeight();
        int ownHeight = scrollPanel.getOffsetHeight();
        boolean isBottom = (scrollPos + ownHeight) >= (childHeight - m_scrollThreshold);
        if (isBottom) {
            m_callback.run();
            m_enabled = false;
            scrollPanel.setVerticalScrollPosition(scrollPos);
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                /**
                 * @see com.google.gwt.core.client.Scheduler.ScheduledCommand#execute()
                 */
                public void execute() {

                    scrollPanel.setVerticalScrollPosition(scrollPos);
                    m_enabled = true;
                }
            });
        }
    }
}
