/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsDialogBox.java,v $
 * Date   : $Date: 2011/03/02 08:04:24 $
 * Version: $Revision: 1.2 $
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * A table-less implementation of {@link com.google.gwt.user.client.ui.DialogBox}.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsDialogBox extends PopupPanel {

    /**
     * The dialog caption.<p>
     */
    private class Caption extends HTML {

        /**
         * Default constructor.<p>
         */
        protected Caption() {

            // nothing to do
        }

        /**
         * Making function visible.<p>
         * 
         * @see com.google.gwt.user.client.ui.Widget#onAttach()
         */
        @Override
        protected void onAttach() {

            super.onAttach();
        }

        /**
         * Making function visible.<p>
         * 
         * @see com.google.gwt.user.client.ui.Widget#onDetach()
         */
        @Override
        protected void onDetach() {

            super.onDetach();
        }
    }

    /**
     * The dialog mouse handler.<p>
     */
    private class MouseHandler implements MouseDownHandler, MouseUpHandler, MouseMoveHandler {

        /**
         * Default constructor.<p>
         */
        protected MouseHandler() {

            // nothing to do
        }

        public void onMouseDown(MouseDownEvent event) {

            beginDragging(event);
        }

        public void onMouseMove(MouseMoveEvent event) {

            continueDragging(event);
        }

        public void onMouseUp(MouseUpEvent event) {

            endDragging(event);
        }
    }

    /** The window width. */
    protected int m_windowWidth;

    /** The dialog caption. */
    private Caption m_caption;

    /** Body offset left. */
    private int m_clientLeft;

    /** Body offset top. */
    private int m_clientTop;

    /** The popup container element. */
    private com.google.gwt.user.client.Element m_containerElement;

    /** Flag if dragging. */
    private boolean m_dragging;

    /** Drag starting x position. */
    private int m_dragStartX;

    /** Drag starting y position. */
    private int m_dragStartY;

    /** The resize handler registration .*/
    private HandlerRegistration m_resizeHandlerRegistration;

    /**
     * Creates an empty dialog box. It should not be shown until its child widget
     * has been added using {@link #add(com.google.gwt.user.client.ui.Widget)}.
     */
    public CmsDialogBox() {

        this(false);
    }

    /**
     * Creates an empty dialog box specifying its "auto-hide" property. It should
     * not be shown until its child widget has been added using
     * {@link #add(com.google.gwt.user.client.ui.Widget)}.
     * 
     * @param autoHide <code>true</code> if the dialog should be automatically
     *          hidden when the user clicks outside of it
     */
    public CmsDialogBox(boolean autoHide) {

        this(autoHide, true);
    }

    /**
     * Creates an empty dialog box specifying its "auto-hide" property. It should
     * not be shown until its child widget has been added using
     * {@link #add(com.google.gwt.user.client.ui.Widget)}.
     * 
     * @param autoHide <code>true</code> if the dialog should be automatically
     *          hidden when the user clicks outside of it
     * @param modal <code>true</code> if keyboard and mouse events for widgets not
     *          contained by the dialog should be ignored
     */
    public CmsDialogBox(boolean autoHide, boolean modal) {

        super(autoHide, modal);
        m_containerElement = super.getContainerElement();
        setStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popup());
        m_containerElement.setClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupContent()
            + " "
            + I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        setGlassStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupOverlay());
        Element dragOverlay = DOM.createDiv();
        dragOverlay.setClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().dragOverlay());
        getElement().insertFirst(dragOverlay);
        Element shadowDiv = DOM.createDiv();
        shadowDiv.setClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupShadow()
            + " "
            + I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        getElement().insertFirst(shadowDiv);
        m_caption = new Caption();
        m_caption.setStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().caption());
        m_caption.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerTop());

        // Add the caption to the top of the popup-panel. We need to
        // logically adopt the caption so we can catch mouse events.
        DOM.appendChild(m_containerElement, m_caption.getElement());
        adopt(m_caption);

        m_windowWidth = Window.getClientWidth();
        m_clientLeft = Document.get().getBodyOffsetLeft();
        m_clientTop = Document.get().getBodyOffsetTop();

        MouseHandler mouseHandler = new MouseHandler();
        addDomHandler(mouseHandler, MouseDownEvent.getType());
        addDomHandler(mouseHandler, MouseUpEvent.getType());
        addDomHandler(mouseHandler, MouseMoveEvent.getType());
    }

    /**
     * Provides access to the dialog's caption.
     * 
     * This method is final because the Caption interface will expand. Therefore
     * it is highly likely that subclasses which implemented this method would end
     * up breaking.
     * 
     * @return the logical caption for this dialog box
     */
    public final Caption getCaption() {

        return m_caption;
    }

    /**
     * Returns the caption's HTML.<p>
     * 
     * @return the caption's HTML
     */
    public String getHTML() {

        return m_caption.getHTML();
    }

    /**
     * Returns the caption's text.<p>
     * 
     * @return the caption's text
     */
    public String getText() {

        return m_caption.getText();
    }

    /**
     * @see com.google.gwt.user.client.ui.PopupPanel#hide()
     */
    @Override
    public void hide() {

        if (m_resizeHandlerRegistration != null) {
            m_resizeHandlerRegistration.removeHandler();
            m_resizeHandlerRegistration = null;
        }
        super.hide();
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onBrowserEvent(com.google.gwt.user.client.Event)
     */
    @Override
    public void onBrowserEvent(Event event) {

        // If we're not yet dragging, only trigger mouse events if the event occurs
        // in the caption wrapper
        switch (event.getTypeInt()) {
            case Event.ONMOUSEDOWN:
            case Event.ONMOUSEUP:
            case Event.ONMOUSEMOVE:
            case Event.ONMOUSEOVER:
            case Event.ONMOUSEOUT:
                if (!m_dragging && !isCaptionEvent(event)) {
                    return;
                }
                break;
            default:
        }

        super.onBrowserEvent(event);
    }

    /**
     * @see com.google.gwt.user.client.ui.PopupPanel#setHeight(java.lang.String)
     */
    @Override
    public void setHeight(String height) {

        Style style;
        if (getWidget() == null) {
            style = m_containerElement.getStyle();
        } else {
            style = getWidget().getElement().getStyle();
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(height)) {
            style.clearHeight();
        } else {
            style.setProperty("height", height);
        }
    }

    /**
     * Sets the html string inside the caption.
     * 
     * Use {@link #setWidget(com.google.gwt.user.client.ui.Widget)} to set the contents inside the
     * {@link CmsDialogBox}.
     * 
     * @param html the object's new HTML
     */
    public void setHTML(String html) {

        m_caption.setHTML(html);
    }

    /**
     * Sets the text inside the caption.
     *
     * Use {@link #setWidget(com.google.gwt.user.client.ui.Widget)} to set the contents inside the
     * {@link CmsDialogBox}.
     *
     * @param text the object's new text
     */
    public void setText(String text) {

        m_caption.setText(text);
    }

    /**
     * @see com.google.gwt.user.client.ui.PopupPanel#setWidth(java.lang.String)
     */
    @Override
    public void setWidth(String width) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(width)) {
            m_containerElement.getStyle().clearWidth();
        } else {
            m_containerElement.getStyle().setProperty("width", width);
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.PopupPanel#show()
     */
    @Override
    public void show() {

        if (m_resizeHandlerRegistration == null) {
            m_resizeHandlerRegistration = Window.addResizeHandler(new ResizeHandler() {

                public void onResize(ResizeEvent event) {

                    m_windowWidth = event.getWidth();
                }
            });
        }
        super.show();
    }

    /**
     * Called on mouse down in the caption area, begins the dragging loop by
     * turning on event capture.
     * 
     * @see DOM#setCapture
     * @see #continueDragging
     * @param event the mouse down event that triggered dragging
     */
    protected void beginDragging(MouseDownEvent event) {

        m_dragging = true;
        DOM.setCapture(getElement());
        m_dragStartX = event.getX();
        m_dragStartY = event.getY();
        addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().dragging());
    }

    /**
     * Called on mouse move in the caption area, continues dragging if it was
     * started by {@link #beginDragging}.
     * 
     * @see #beginDragging
     * @see #endDragging
     * @param event the mouse move event that continues dragging
     */
    protected void continueDragging(MouseMoveEvent event) {

        if (m_dragging) {
            int absX = event.getX() + getAbsoluteLeft();
            int absY = event.getY() + getAbsoluteTop();

            // if the mouse is off the screen to the left, right, or top, don't
            // move the dialog box. This would let users lose dialog boxes, which
            // would be bad for modal popups.
            if ((absX < m_clientLeft) || (absX >= m_windowWidth) || (absY < m_clientTop)) {
                return;
            }

            setPopupPosition(absX - m_dragStartX, absY - m_dragStartY);
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.Panel#doAttachChildren()
     */
    @Override
    protected void doAttachChildren() {

        try {
            super.doAttachChildren();
        } finally {
            // See comment in doDetachChildren for an explanation of this call
            m_caption.onAttach();
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.Panel#doDetachChildren()
     */
    @Override
    protected void doDetachChildren() {

        try {
            super.doDetachChildren();
        } finally {
            // We need to detach the caption specifically because it is not part of the
            // iterator of Widgets that the {@link SimplePanel} super class returns.
            // This is similar to a {@link ComplexPanel}, but we do not want to expose
            // the caption widget, as its just an internal implementation.
            m_caption.onDetach();
        }
    }

    /**
     * Called on mouse up in the caption area, ends dragging by ending event
     * capture.
     * 
     * @param event the mouse up event that ended dragging
     * 
     * @see DOM#releaseCapture
     * @see #beginDragging
     * @see #endDragging
     */
    protected void endDragging(MouseUpEvent event) {

        m_dragging = false;
        DOM.releaseCapture(getElement());
        removeStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().dragging());
    }

    /**
     * @see com.google.gwt.user.client.ui.PopupPanel#getContainerElement()
     */
    @Override
    protected com.google.gwt.user.client.Element getContainerElement() {

        if (m_containerElement == null) {
            m_containerElement = super.getContainerElement();
        }
        return m_containerElement;
    }

    /**
     * Override to work around the glass overlay still showing after dialog hide.<p>
     * 
     * @see com.google.gwt.user.client.ui.Widget#onDetach()
     */
    @Override
    protected void onDetach() {

        super.onDetach();
        if (this.getGlassElement() != null) {
            this.getGlassElement().removeFromParent();
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.PopupPanel#onPreviewNativeEvent(com.google.gwt.user.client.Event.NativePreviewEvent)
     */
    @Override
    protected void onPreviewNativeEvent(NativePreviewEvent event) {

        // We need to preventDefault() on mouseDown events (outside of the
        // DialogBox content) to keep text from being selected when it
        // is dragged.
        NativeEvent nativeEvent = event.getNativeEvent();

        if (!event.isCanceled() && (event.getTypeInt() == Event.ONMOUSEDOWN) && isCaptionEvent(nativeEvent)) {
            nativeEvent.preventDefault();
        }

        super.onPreviewNativeEvent(event);
    }

    @SuppressWarnings("static-access")
    private boolean isCaptionEvent(NativeEvent event) {

        EventTarget target = event.getEventTarget();
        if (Element.is(target)) {
            return m_caption.getElement().isOrHasChild(Element.as(target));
        }
        return false;
    }
}
