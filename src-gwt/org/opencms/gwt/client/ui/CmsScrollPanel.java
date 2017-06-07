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
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsFocusedScrollingHandler;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Scroll panel implementation allowing focused scrolling.<p>
 */
public class CmsScrollPanel extends ScrollPanel implements HasResizeHandlers, I_CmsDescendantResizeHandler {

    /**Inner class for the resize button. */
    protected class ResizeButton extends CmsPushButton {

        /**
         * Default constructor.<p>
         */
        public ResizeButton() {

            super();
            setStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().resizeButton());

        }

        /**
         * @see com.google.gwt.user.client.ui.CustomButton#onAttach()
         */
        @Override
        protected void onAttach() {

            super.onAttach();
        }

        /**
         * @see com.google.gwt.user.client.ui.CustomButton#onDetach()
         */
        @Override
        protected void onDetach() {

            super.onDetach();
        }
    }

    /**
     * Drag and drop event preview handler.<p>
     *
     * To be used while dragging.<p>
     */
    protected class ResizeEventPreviewHandler implements NativePreviewHandler {

        /**
         * @see com.google.gwt.user.client.Event.NativePreviewHandler#onPreviewNativeEvent(com.google.gwt.user.client.Event.NativePreviewEvent)
         */
        public void onPreviewNativeEvent(NativePreviewEvent event) {

            Event nativeEvent = Event.as(event.getNativeEvent());
            switch (DOM.eventGetType(nativeEvent)) {
                case Event.ONMOUSEMOVE:
                    // dragging
                    setNewHeight(nativeEvent);
                    onResizeDescendant();
                    event.cancel();
                    break;
                case Event.ONMOUSEUP:
                    m_previewHandlerRegistration.removeHandler();
                    m_previewHandlerRegistration = null;
                    break;
                case Event.ONKEYDOWN:
                    break;
                case Event.ONMOUSEWHEEL:
                    //onMouseWheelScroll(nativeEvent);
                    break;
                default:
                    // do nothing
            }

            nativeEvent.preventDefault();
            nativeEvent.stopPropagation();

        }

    }

    /** The preview handler registration. */
    protected HandlerRegistration m_previewHandlerRegistration;

    /** The start Y coordination. */
    private int m_clientY;

    /** The default height. */
    private double m_defaultHeight = -1;

    /** The prevent outer scrolling handler. */
    private CmsFocusedScrollingHandler m_focusedScrollingHandler;

    /** The scroll handler registration. */
    private HandlerRegistration m_handlerRegistration;

    /** Saves if the scrolling panel has the resize button. */
    private boolean m_isResize;

    /** The start height. */
    private double m_oldheight;

    /** The button to resize the scrolling panel. */
    private ResizeButton m_resize;

    /**
     * Constructor.<p>
     *
     * @see com.google.gwt.user.client.ui.ScrollPanel#ScrollPanel()
     */
    public CmsScrollPanel() {

        m_resize = new ResizeButton();

    }

    /**
     * Constructor to be used by {@link org.opencms.gwt.client.ui.CmsScrollPanelImpl}.<p>
     *
     * @param root the root element of the widget
     * @param scrollabel the scrollable element of the widget
     * @param container the container element of the widget
     */
    protected CmsScrollPanel(Element root, Element scrollabel, Element container) {

        super(root, scrollabel, container);
        m_resize = new ResizeButton();

    }

    /**
     * @see com.google.gwt.event.logical.shared.HasResizeHandlers#addResizeHandler(com.google.gwt.event.logical.shared.ResizeHandler)
     */
    public HandlerRegistration addResizeHandler(ResizeHandler handler) {

        return addHandler(handler, ResizeEvent.getType());
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
     * @see org.opencms.gwt.client.I_CmsDescendantResizeHandler#onResizeDescendant()
     */
    public void onResizeDescendant() {

        // not needed
    }

    /**
     * @see com.google.gwt.user.client.ui.SimplePanel#remove(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public boolean remove(Widget w) {

        if (w == m_resize) {
            try {
                orphan(m_resize);
            } finally {
                getElement().removeChild(m_resize.getElement());
            }
            return true;
        } else {
            return super.remove(w);
        }
    }

    /**
     * Sets the default height of the scrolling panel.
     *
     * @param height the default height
     */
    public void setDefaultHeight(double height) {

        m_defaultHeight = height;
    }

    /**
     * Sets the scrollpanel resizeable.<p>
     *
     * @param resize true if the scrollpanel should be resizeable.
     */
    public void setResizable(boolean resize) {

        if (resize != m_isResize) {
            if (resize) {
                getElement().appendChild(m_resize.getElement());
                adopt(m_resize);
                m_resize.addMouseDownHandler(new MouseDownHandler() {

                    public void onMouseDown(MouseDownEvent event) {

                        setStartParameters(event);
                        CmsDebugLog.getInstance().printLine("Registering preview handler");
                        if (m_previewHandlerRegistration != null) {
                            m_previewHandlerRegistration.removeHandler();
                        }
                        m_previewHandlerRegistration = Event.addNativePreviewHandler(new ResizeEventPreviewHandler());
                    }
                });
            } else {
                m_resize.removeFromParent();
            }
            m_isResize = resize;
        }
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
     * @see com.google.gwt.user.client.ui.ScrollPanel#onAttach()
     */
    @Override
    protected void onAttach() {

        super.onAttach();
        if (m_isResize) {
            m_resize.onAttach();
        }

    }

    /**
     * @see com.google.gwt.user.client.ui.ScrollPanel#onDetach()
     */
    @Override
    protected void onDetach() {

        super.onDetach();
        if (m_isResize) {
            m_resize.onDetach();
        }
    }

    /**
     * Executed on mouse move while dragging.<p>
     *
     * @param event the event
     */
    protected void setNewHeight(Event event) {

        double newheight = m_oldheight + (event.getClientY() - m_clientY);
        if (m_defaultHeight != -1) {
            if (newheight < m_defaultHeight) {
                newheight = m_defaultHeight;
            }
        }
        ResizeEvent.fire(this, getOffsetWidth(), (int)newheight);
        getElement().getStyle().setHeight(newheight, Unit.PX);
    }

    /**
     * Sets the start parameters of the resize event.<p>
     *
     * @param event the mouse event
     */
    protected void setStartParameters(MouseDownEvent event) {

        m_oldheight = Double.parseDouble(getElement().getStyle().getHeight().replace("px", ""));
        m_clientY = event.getClientY();
    }

}
