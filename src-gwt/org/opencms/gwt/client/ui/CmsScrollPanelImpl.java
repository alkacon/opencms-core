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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.I_CmsDescendantResizeHandler;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Style;
import org.opencms.gwt.client.util.CmsFadeAnimation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractNativeScrollbar;
import com.google.gwt.user.client.ui.VerticalScrollbar;
import com.google.gwt.user.client.ui.Widget;

/**
 * Scroll panel implementation with custom scroll bars. Works in all browsers but IE7.<p>
 */
public class CmsScrollPanelImpl extends CmsScrollPanel {

    /**
     * Handler to show and hide the scroll bar on hover.<p>
     */
    private class HoverHandler implements MouseOutHandler, MouseOverHandler {

        /** The owner element. */
        Element m_owner;

        /** The element to fade in and out. */
        private Element m_fadeElement;

        /** The currently running hide animation. */
        private CmsFadeAnimation m_hideAnimation;

        /** The timer to hide the scroll bar with a delay. */
        private Timer m_removeTimer;

        /**
         * Constructor.<p>
         *
         * @param owner the owner element
         * @param fadeElement the element to fade in and out on hover
         */
        HoverHandler(Element owner, Element fadeElement) {

            m_owner = owner;
            m_fadeElement = fadeElement;
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
            m_removeTimer.schedule(1000);
        }

        /**
         * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
         */
        public void onMouseOver(MouseOverEvent event) {

            if ((m_hideAnimation != null)
                || !CmsDomUtil.hasClass(I_CmsLayoutBundle.INSTANCE.scrollBarCss().showBars(), m_owner)) {
                if (m_hideAnimation != null) {
                    m_hideAnimation.cancel();
                    m_hideAnimation = null;
                } else {
                    CmsFadeAnimation.fadeIn(m_fadeElement, null, 100);
                }
                m_owner.addClassName(I_CmsLayoutBundle.INSTANCE.scrollBarCss().showBars());
            }
            if (m_removeTimer != null) {
                m_removeTimer.cancel();
                m_removeTimer = null;
            }
        }

        /**
         * Hides the scroll bar.<p>
         */
        void clearShowing() {

            m_hideAnimation = CmsFadeAnimation.fadeOut(m_fadeElement, new Command() {

                public void execute() {

                    m_owner.removeClassName(I_CmsLayoutBundle.INSTANCE.scrollBarCss().showBars());

                }
            }, 200);

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

    /**
     * Constructor.<p>
     */
    public CmsScrollPanelImpl() {

        super(DOM.createDiv(), DOM.createDiv(), DOM.createDiv());
        setStyleName(I_CmsLayoutBundle.INSTANCE.scrollBarCss().scrollPanel());
        Element scrollable = getScrollableElement();
        scrollable.getStyle().clearPosition();
        scrollable.getStyle().setOverflowX(Overflow.HIDDEN);
        scrollable.setClassName(I_CmsLayoutBundle.INSTANCE.scrollBarCss().scrollable());
        getElement().appendChild(scrollable);
        Element container = getContainerElement();
        container.setClassName(I_CmsLayoutBundle.INSTANCE.scrollBarCss().scrollContainer());
        scrollable.appendChild(container);
        m_scrollLayer = DOM.createDiv();
        getElement().appendChild(m_scrollLayer);
        m_scrollLayer.setClassName(I_CmsLayoutBundle.INSTANCE.scrollBarCss().scrollbarLayer());
        CmsScrollBar scrollbar = new CmsScrollBar(scrollable, container);
        setVerticalScrollbar(scrollbar, 8);
        m_hiddenSize = DOM.createDiv();
        m_hiddenSize.setClassName(I_CmsLayoutBundle.INSTANCE.scrollBarCss().hiddenSize());

        /*
         * Listen for scroll events from the root element and the scrollable element
         * so we can align the scrollbars with the content. Scroll events usually
         * come from the scrollable element, but they can also come from the root
         * element if the user clicks and drags the content, which reveals the
         * hidden scrollbars.
         */
        Event.sinkEvents(getElement(), Event.ONSCROLL);
        Event.sinkEvents(scrollable, Event.ONSCROLL);
        initHoverHandler();
    }

    /**
     * @see com.google.gwt.user.client.ui.SimplePanel#iterator()
     */
    @Override
    public Iterator<Widget> iterator() {

        // Return a simple iterator that enumerates the 0 or 1 elements in this
        // panel.
        List<Widget> widgets = new ArrayList<Widget>();
        if (getWidget() != null) {
            widgets.add(getWidget());
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

            @Override
            public void remove() {

                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onBrowserEvent(com.google.gwt.user.client.Event)
     */
    @Override
    public void onBrowserEvent(Event event) {

        // Align the scrollbars with the content.
        if (Event.ONSCROLL == event.getTypeInt()) {
            maybeUpdateScrollbarPositions();
        }
        super.onBrowserEvent(event);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsScrollPanel#onResizeDescendant()
     */
    @Override
    public void onResizeDescendant() {

        int maxHeight = CmsDomUtil.getCurrentStyleInt(getElement(), Style.maxHeight);
        if (maxHeight > 0) {
            getScrollableElement().getStyle().setPropertyPx("maxHeight", maxHeight);
        }
        // appending div to measure panel width, doing it every time anew to avoid rendering bugs in Chrome
        getElement().appendChild(m_hiddenSize);
        int width = m_hiddenSize.getClientWidth();
        m_hiddenSize.removeFromParent();
        if (width > 0) {
            getContainerElement().getStyle().setWidth(width, Unit.PX);
            maybeUpdateScrollbars();
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsScrollPanel#setResizable(boolean)
     */
    @Override
    public void setResizable(boolean resize) {

        super.setResizable(resize);
        if (resize) {
            m_scrollbar.asWidget().getElement().getStyle().setMarginBottom(7, Unit.PX);
        } else {
            m_scrollbar.asWidget().getElement().getStyle().setMarginBottom(0, Unit.PX);
        }
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
     * @see com.google.gwt.user.client.ui.ScrollPanel#onAttach()
     */
    @Override
    protected void onAttach() {

        super.onAttach();
        hideNativeScrollbars();
        onResizeDescendant();
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    protected void onLoad() {

        super.onLoad();
        hideNativeScrollbars();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                onResizeDescendant();
            }
        });
    }

    /**
     * Hide the native scrollbars. We call this after attaching to ensure that we
     * inherit the direction (rtl or ltr).
     */
    private void hideNativeScrollbars() {

        m_nativeScrollbarWidth = AbstractNativeScrollbar.getNativeScrollbarWidth();
        getScrollableElement().getStyle().setMarginRight(-(m_nativeScrollbarWidth + 10), Unit.PX);
    }

    /**
     * Initializes the hover handler to hide and show the scroll bar on hover.<p>
     */
    private void initHoverHandler() {

        HoverHandler handler = new HoverHandler(getElement(), m_scrollbar.asWidget().getElement());
        addDomHandler(handler, MouseOverEvent.getType());
        addDomHandler(handler, MouseOutEvent.getType());
    }

    /**
     * Synchronize the scroll positions of the scrollbars with the actual scroll
     * position of the content.
     */
    private void maybeUpdateScrollbarPositions() {

        if (!isAttached()) {
            return;
        }

        if (m_scrollbar != null) {
            int vPos = getVerticalScrollPosition();
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

        if (!isAttached()) {
            return;
        }

        /*
         * Measure the height and width of the content directly. Note that measuring
         * the height and width of the container element (which should be the same)
         * doesn't work correctly in IE.
         */
        Widget w = getWidget();
        int contentHeight = (w == null) ? 0 : w.getOffsetHeight();

        // Determine which scrollbars to show.
        int realScrollbarHeight = 0;
        int realScrollbarWidth = 0;
        if ((m_scrollbar != null) && (getElement().getClientHeight() < contentHeight)) {
            // Vertical scrollbar is defined and required.
            realScrollbarWidth = m_verticalScrollbarWidth;
        }

        if (realScrollbarWidth > 0) {
            m_scrollLayer.getStyle().clearDisplay();

            m_scrollbar.setScrollHeight(Math.max(0, contentHeight - realScrollbarHeight));
        } else if (m_scrollLayer != null) {
            m_scrollLayer.getStyle().setDisplay(Display.NONE);
        }
        if (m_scrollbar instanceof I_CmsDescendantResizeHandler) {
            ((I_CmsDescendantResizeHandler)m_scrollbar).onResizeDescendant();
        }
        maybeUpdateScrollbarPositions();
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
            remove(m_scrollbar);
        }
        m_scrollLayer.appendChild(scrollbar.asWidget().getElement());
        adopt(scrollbar.asWidget());

        // Logical attach.
        m_scrollbar = scrollbar;
        m_verticalScrollbarWidth = width;

        // Initialize the new scrollbar.
        m_verticalScrollbarHandlerRegistration = scrollbar.addValueChangeHandler(new ValueChangeHandler<Integer>() {

            public void onValueChange(ValueChangeEvent<Integer> event) {

                int vPos = scrollbar.getVerticalScrollPosition();
                int v = getVerticalScrollPosition();
                if (v != vPos) {
                    setVerticalScrollPosition(vPos);
                }

            }
        });
        maybeUpdateScrollbars();
    }
}