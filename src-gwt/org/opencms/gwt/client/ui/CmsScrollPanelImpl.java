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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Style;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractNativeScrollbar;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.VerticalScrollbar;
import com.google.gwt.user.client.ui.Widget;

public class CmsScrollPanelImpl implements I_CmsScrollPanelImpl {

    /**
     * Handler to show and hide the scroll bar on hover.<p>
     */
    private class HoverHandler implements MouseOutHandler, MouseOverHandler {

        /** The owner element. */
        private Element m_owner;

        /** The timer to hide the scroll bar with a delay. */
        private Timer m_removeTimer;

        /**
         * Constructor.<p>
         * 
         * @param owner the owner element
         */
        HoverHandler(Element owner) {

            m_owner = owner;
        }

        /**
         * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
         */
        public void onMouseOut(MouseOutEvent event) {

            m_removeTimer = new Timer() {

                /**
                 * @see com.google.gwt.user.client.Timer#run()
                 */
                @Override
                public void run() {

                    clearShowing();
                }
            };
            m_removeTimer.schedule(2000);
        }

        /**
         * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
         */
        public void onMouseOver(MouseOverEvent event) {

            m_owner.addClassName(I_CmsLayoutBundle.INSTANCE.scrollBarCss().showBars());
            if (m_removeTimer != null) {
                m_removeTimer.cancel();
                m_removeTimer = null;
            }
        }

        /**
         * Hides the scroll bar.<p>
         */
        void clearShowing() {

            m_owner.removeClassName(I_CmsLayoutBundle.INSTANCE.scrollBarCss().showBars());
            m_removeTimer = null;
        }
    }

    /** Hidden element to measure the appropriate size of the container element. */
    private Element m_hiddenSize;

    /** The measured width of the native scroll bars. */
    private int m_nativeScrollbarWidth;

    /** The vertical scroll bar. */
    private VerticalScrollbar m_scrollbar;

    /** The scroll layer. */
    private Element m_scrollLayer;

    /** The scroll bar change handler registration. */
    private HandlerRegistration m_verticalScrollbarHandlerRegistration;

    /** The scroll bar width. */
    private int m_verticalScrollbarWidth;
    protected CmsScrollPanel m_panel;

    public void initialize(CmsScrollPanel panel) {

        m_panel = panel;
        panel.setStyleName(I_CmsLayoutBundle.INSTANCE.scrollBarCss().scrollPanel());
        m_hiddenSize = DOM.createDiv();
        m_hiddenSize.setClassName(I_CmsLayoutBundle.INSTANCE.scrollBarCss().hiddenSize());
        panel.getElement().appendChild(m_hiddenSize);
        Element scrollable = panel.getScrollableElement();
        scrollable.getStyle().clearPosition();
        scrollable.setClassName(I_CmsLayoutBundle.INSTANCE.scrollBarCss().scrollable());
        panel.getElement().appendChild(scrollable);
        Element container = panel.getContainerElement();

        container.setClassName(I_CmsLayoutBundle.INSTANCE.scrollBarCss().scrollContainer());
        scrollable.appendChild(container);
        m_scrollLayer = DOM.createDiv();
        panel.getElement().appendChild(m_scrollLayer);
        m_scrollLayer.setClassName(I_CmsLayoutBundle.INSTANCE.scrollBarCss().scrollbarLayer());
        CmsScrollBar scrollbar = new CmsScrollBar(scrollable, container);
        setVerticalScrollbar(scrollbar, 12);

        /*
         * Listen for scroll events from the root element and the scrollable element
         * so we can align the scrollbars with the content. Scroll events usually
         * come from the scrollable element, but they can also come from the root
         * element if the user clicks and drags the content, which reveals the
         * hidden scrollbars.
         */
        Event.sinkEvents(m_panel.getElement(), Event.ONSCROLL);
        Event.sinkEvents(scrollable, Event.ONSCROLL);
        initHoverHandler();
    }

    public Iterator<Widget> getSpezialIterator() {

        // Return a simple iterator that enumerates the 0 or 1 elements in this
        // panel.
        List<Widget> widgets = new ArrayList<Widget>();
        if (m_panel.getWidget() != null) {
            widgets.add(m_panel.getWidget());
        }
        if (getVerticalScrollBar() != null) {
            widgets.add(getVerticalScrollBar().asWidget());
        }
        final Iterator<Widget> internalIterator = widgets.iterator();
        return new Iterator<Widget>() {

            public boolean hasNext() {

                return internalIterator.hasNext();
            }

            public Widget next() {

                return internalIterator.next();
            }

            public void remove() {

                throw new UnsupportedOperationException();
            }
        };
    }

    public void onBrowserEvent(Event event) {

        // Align the scrollbars with the content.
        if (Event.ONSCROLL == event.getTypeInt()) {
            maybeUpdateScrollbarPositions();
        }
    }

    public void onResize() {

        int width = m_hiddenSize.getClientWidth();
        if (width > 0) {
            m_panel.getContainerElement().getStyle().setWidth(width, Unit.PX);
            maybeUpdateScrollbars();
        }
    }

    public void onAttach() {

        hideNativeScrollbars();
        m_panel.onResize();
    }

    public void onLoad() {

        hideNativeScrollbars();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                m_panel.onResize();
            }
        });
    }

    /**
     * Set the scrollbar used for vertical scrolling.
     * 
     * @param scrollbar the scrollbar, or null to clear it
     * @param width the width of the scrollbar in pixels
     */
    private void setVerticalScrollbar(final CmsScrollBar scrollbar, int width) {

        // Validate.
        if ((scrollbar == m_scrollbar) || (scrollbar == null)) {
            return;
        }
        // Detach new child.

        scrollbar.asWidget().removeFromParent();
        // Remove old child.
        if (m_scrollbar != null) {
            if (m_verticalScrollbarHandlerRegistration != null) {
                m_verticalScrollbarHandlerRegistration.removeHandler();
                m_verticalScrollbarHandlerRegistration = null;
            }
            m_panel.remove(m_scrollbar);
        }
        m_scrollLayer.appendChild(scrollbar.asWidget().getElement());
        m_panel.adoptChild(scrollbar.asWidget());

        // Logical attach.
        m_scrollbar = scrollbar;
        m_verticalScrollbarWidth = width;

        // Initialize the new scrollbar.
        m_verticalScrollbarHandlerRegistration = scrollbar.addValueChangeHandler(new ValueChangeHandler<Integer>() {

            public void onValueChange(ValueChangeEvent<Integer> event) {

                int vPos = scrollbar.getVerticalScrollPosition();
                int v = m_panel.getVerticalScrollPosition();
                if (v != vPos) {
                    m_panel.setVerticalScrollPosition(vPos);
                }

            }
        });
        maybeUpdateScrollbars();
    }

    /**
     * Returns the vertical scroll bar.<p>
     * 
     * @return the vertical scroll bar
     */
    protected VerticalScrollbar getVerticalScrollBar() {

        return m_scrollbar;
    }

    /**
     * Hide the native scrollbars. We call this after attaching to ensure that we
     * inherit the direction (rtl or ltr).
     */
    private void hideNativeScrollbars() {

        m_nativeScrollbarWidth = AbstractNativeScrollbar.getNativeScrollbarWidth();
        m_panel.getScrollableElement().getStyle().setMarginRight(-(m_nativeScrollbarWidth + 10), Unit.PX);
        int maxHeight = CmsDomUtil.getCurrentStyleInt(m_panel.getElement(), Style.maxHeight);
        if (maxHeight > 0) {
            m_panel.getScrollableElement().getStyle().setPropertyPx("maxHeight", maxHeight);
        }
    }

    /**
     * Initializes the hover handler to hide and show the scroll bar on hover.<p>
     */
    private void initHoverHandler() {

        HoverHandler handler = new HoverHandler(m_panel.getElement());
        m_panel.addDomHandler(handler, MouseOverEvent.getType());
        m_panel.addDomHandler(handler, MouseOutEvent.getType());
    }

    /**
     * Synchronize the scroll positions of the scrollbars with the actual scroll
     * position of the content.
     */
    private void maybeUpdateScrollbarPositions() {

        if (!m_panel.isAttached()) {
            return;
        }

        if (m_scrollbar != null) {
            int vPos = m_panel.getVerticalScrollPosition();
            if (m_scrollbar.getVerticalScrollPosition() != vPos) {
                m_scrollbar.setVerticalScrollPosition(vPos);
            }
        }
    }

    /**
     * Update the position of the scrollbars.<p>
     * If only the vertical scrollbar is present, it takes up the entire height of
     * the right side. If only the horizontal scrollbar is present, it takes up
     * the entire width of the bottom. If both scrollbars are present, the
     * vertical scrollbar extends from the top to just above the horizontal
     * scrollbar, and the horizontal scrollbar extends from the left to just right
     * of the vertical scrollbar, leaving a small square in the bottom right
     * corner.<p>
     */
    private void maybeUpdateScrollbars() {

        if (!m_panel.isAttached()) {
            return;
        }

        /*
         * Measure the height and width of the content directly. Note that measuring
         * the height and width of the container element (which should be the same)
         * doesn't work correctly in IE.
         */
        Widget w = m_panel.getWidget();
        int contentHeight = (w == null) ? 0 : w.getOffsetHeight();

        // Determine which scrollbars to show.
        int realScrollbarHeight = 0;
        int realScrollbarWidth = 0;
        if ((m_scrollbar != null) && (m_panel.getElement().getClientHeight() < contentHeight)) {
            // Vertical scrollbar is defined and required.
            realScrollbarWidth = m_verticalScrollbarWidth;
        }

        if (realScrollbarWidth > 0) {
            m_scrollLayer.getStyle().clearDisplay();

            m_scrollbar.setScrollHeight(Math.max(0, contentHeight - realScrollbarHeight));
        } else if (m_scrollLayer != null) {
            m_scrollLayer.getStyle().setDisplay(Display.NONE);
        }
        if (m_scrollbar instanceof RequiresResize) {
            ((RequiresResize)m_scrollbar).onResize();
        }
        maybeUpdateScrollbarPositions();
    }
}
