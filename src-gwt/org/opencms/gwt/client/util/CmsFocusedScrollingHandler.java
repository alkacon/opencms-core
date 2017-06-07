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

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Native preview handler that focuses the on scroll wheel mouse event on the given scroll panel.<p>
 */
public final class CmsFocusedScrollingHandler implements NativePreviewHandler {

    /** The scroll panel. */
    private ScrollPanel m_scrollPanel;

    /** The scroll handler registration. */
    private HandlerRegistration m_handlerRegistration;

    /**
     * Constructor.<p>
     *
     * @param scrollPanel the scroll panel to focus on
     */
    private CmsFocusedScrollingHandler(ScrollPanel scrollPanel) {

        m_scrollPanel = scrollPanel;
    }

    /**
     * Installs a focused scrolling handler on the given widget.<p>
     *
     * @param scrollPanel the scroll panel
     *
     * @return the focused scrolling handler
     */
    public static CmsFocusedScrollingHandler installFocusedScrollingHandler(ScrollPanel scrollPanel) {

        if (scrollPanel == null) {
            throw new UnsupportedOperationException("No scroll panel given");
        }
        CmsFocusedScrollingHandler handler = new CmsFocusedScrollingHandler(scrollPanel);
        handler.register();
        return handler;
    }

    /**
     * @see com.google.gwt.user.client.Event.NativePreviewHandler#onPreviewNativeEvent(com.google.gwt.user.client.Event.NativePreviewEvent)
     */
    public void onPreviewNativeEvent(NativePreviewEvent event) {

        if ((Event.ONMOUSEWHEEL == event.getTypeInt()) && (m_handlerRegistration != null)) {
            CmsPositionBean position = CmsPositionBean.generatePositionInfo(m_scrollPanel);
            if (position.isOverElement(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY())) {
                boolean cancelEvent = false;
                int velocity = event.getNativeEvent().getMouseWheelVelocityY();
                if (velocity > 0) {

                    Widget child = m_scrollPanel.getWidget();
                    int scrollSpace = child.getOffsetHeight()
                        - m_scrollPanel.getVerticalScrollPosition()
                        - m_scrollPanel.getOffsetHeight();
                    CmsDebugLog.getInstance().printLine("Scrolling down:  " + scrollSpace);
                    cancelEvent = scrollSpace <= 0;
                } else {
                    CmsDebugLog.getInstance().printLine("Scrolling up:  " + m_scrollPanel.getVerticalScrollPosition());
                    cancelEvent = m_scrollPanel.getVerticalScrollPosition() == 0;
                }
                if (cancelEvent) {
                    event.cancel();
                }
            } else {
                removeHandler();
            }

        }

    }

    /**
     * Returns if the handler is currently registered.<p>
     *
     * @return <code>true</code> if the handler is currently registered and active
     */
    public boolean isRegistered() {

        return m_handlerRegistration != null;
    }

    /**
     * Registers the handler.<p>
     */
    public void register() {

        removeHandler();
        m_handlerRegistration = Event.addNativePreviewHandler(this);
    }

    /**
     * Removes the handler and deactivates it.<p>
     * Call {@link #register()} the register again.<p>
     */
    public void removeHandler() {

        if (m_handlerRegistration != null) {
            m_handlerRegistration.removeHandler();
            m_handlerRegistration = null;
        }
    }
}
