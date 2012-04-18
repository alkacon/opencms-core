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

import org.opencms.gwt.client.util.CmsFocusedScrollingHandler;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Scroll panel implementation allowing focused scrolling.<p>
 */
public class CmsScrollPanel extends ScrollPanel {

    /** The prevent outer scrolling handler. */
    private CmsFocusedScrollingHandler m_focusedScrollingHandler;

    /** The scroll handler registration. */
    private HandlerRegistration m_handlerRegistration;

    /** The browser specific scroll panel implementation. */
    private I_CmsScrollPanelImpl m_impl;

    /**
     * Constructor.<p>
     * 
     * @see com.google.gwt.user.client.ui.ScrollPanel#ScrollPanel()
     */
    public CmsScrollPanel() {

        super(DOM.createDiv(), DOM.createDiv(), DOM.createDiv());
        m_impl = GWT.create(I_CmsScrollPanelImpl.class);
        m_impl.initialize(this);
    }

    /**
     * Enables or disables the focused scrolling feature.<p>
     * Focused scrolling is enabled by default.<p>
     * 
     * @param enable <code>true</code> to enable the focused scrolling feature
     */
    public void enableFocusedScrolling(boolean enable) {

        if (enable) {
            if (m_handlerRegistration == null) {
                m_handlerRegistration = addScrollHandler(new ScrollHandler() {

                    public void onScroll(ScrollEvent event) {

                        ensureFocusedScrolling();
                    }
                });
            }
        } else if (m_handlerRegistration != null) {
            m_handlerRegistration.removeHandler();
            m_handlerRegistration = null;
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.SimplePanel#iterator()
     */
    @Override
    public Iterator<Widget> iterator() {

        Iterator<Widget> result = m_impl.getSpezialIterator();
        return result != null ? result : super.iterator();
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onBrowserEvent(com.google.gwt.user.client.Event)
     */
    @Override
    public void onBrowserEvent(Event event) {

        m_impl.onBrowserEvent(event);
        super.onBrowserEvent(event);
    }

    /**
     * @see com.google.gwt.user.client.ui.CustomScrollPanel#onResize()
     */
    @Override
    public void onResize() {

        m_impl.onResize();
        super.onResize();
    }

    /**
     * Adopt a widget. This may be needed by the scroll panel implementations.<p>
     * 
     * @param child the child widget
     */
    protected void adoptChild(Widget child) {

        super.adopt(child);
    }

    /**
     * Ensures the focused scrolling event preview handler is registered.<p>
     */
    protected void ensureFocusedScrolling() {

        if (m_focusedScrollingHandler == null) {
            m_focusedScrollingHandler = CmsFocusedScrollingHandler.installFocusedScrollingHandler(this);
        } else if (!m_focusedScrollingHandler.isRegistered()) {
            m_focusedScrollingHandler.register();
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.ScrollPanel#getContainerElement()
     */
    @Override
    protected Element getContainerElement() {

        return super.getContainerElement();
    }

    /**
     * @see com.google.gwt.user.client.ui.ScrollPanel#getScrollableElement()
     */
    @Override
    protected Element getScrollableElement() {

        return super.getScrollableElement();
    }

    /**
     * @see com.google.gwt.user.client.ui.ScrollPanel#onAttach()
     */
    @Override
    protected void onAttach() {

        super.onAttach();
        m_impl.onAttach();
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    protected void onLoad() {

        super.onLoad();
        m_impl.onLoad();
    }
}
