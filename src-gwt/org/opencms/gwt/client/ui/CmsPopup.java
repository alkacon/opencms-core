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

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsFadeAnimation;
import org.opencms.util.CmsStringUtil;

import java.util.Iterator;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;

/**
 * Provides a pop up dialog base.
 * 
 * @since 8.0.0
 */
public class CmsPopup extends PopupPanel implements I_CmsAutoHider {

    /**
     * The dialog button panel.<p>
     */
    private class ButtonPanel extends FlowPanel {

        /**
         * Default constructor.<p>
         */
        protected ButtonPanel() {

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
     * The dialog close button.<p>
     */
    private class CloseButton extends CmsPushButton {

        /**
         * Default constructor.<p>
         */
        protected CloseButton() {

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

    /** The default width of this dialog. */
    private static final int DEFAULT_WIDTH = 300;

    /** The window width. */
    protected int m_windowWidth;

    /** The panel holding the dialog's buttons. */
    private ButtonPanel m_buttonPanel;

    /** The dialog caption. */
    private Caption m_caption;

    private WidgetCollection m_children;

    /** Body offset left. */
    private int m_clientLeft;

    /** Body offset top. */
    private int m_clientTop;

    /** The panel for the close button. */
    private CloseButton m_close;

    /** The popup container element. */
    private com.google.gwt.user.client.Element m_containerElement;

    /** The content height correction, used when explicitly setting the dialog height. */
    private int m_contentHeightCorrection = 6;

    /** Flag if dragging. */
    private boolean m_dragging;

    /** Drag starting x position. */
    private int m_dragStartX;

    /** Drag starting y position. */
    private int m_dragStartY;

    /** The main widget of this dialog containing all others. */
    private Element m_main;

    /** The resize handler registration .*/
    private HandlerRegistration m_resizeHandlerRegistration;

    /** Signals whether a animation should be used to show the popup or not. */
    private boolean m_useAnimation = true;

    /**
     * Constructor.<p>
     */
    public CmsPopup() {

        this(DEFAULT_WIDTH);
    }

    /**
     * Constructor setting the width of the dialog.<p>
     * 
     * @param width the width to set
     */
    public CmsPopup(int width) {

        super(false, true);
        // super(autoHide, modal);

        m_containerElement = super.getContainerElement();
        setStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popup());
        m_containerElement.setClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupContent());
        setGlassStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupOverlay());
        Element dragOverlay = DOM.createDiv();
        dragOverlay.setClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().dragOverlay());
        getElement().insertFirst(dragOverlay);

        m_caption = new Caption();
        m_caption.setStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().caption());
        // Add the caption to the top of the popup-panel. We need to
        // logically adopt the caption so we can catch mouse events.
        DOM.appendChild(m_containerElement, m_caption.getElement());
        adopt(m_caption);
        m_children = new WidgetCollection(this);
        m_main = DOM.createDiv();
        m_main.addClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupMainContent());
        m_main.addClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().contentPadding());
        DOM.appendChild(m_containerElement, m_main);
        m_buttonPanel = new ButtonPanel();
        m_buttonPanel.setStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().hideButtonPanel());
        // Add the caption to the top of the popup-panel. We need to
        // logically adopt the caption so we can catch mouse events.
        DOM.appendChild(m_containerElement, m_buttonPanel.getElement());
        adopt(m_buttonPanel);

        MouseHandler mouseHandler = new MouseHandler();
        addDomHandler(mouseHandler, MouseDownEvent.getType());
        addDomHandler(mouseHandler, MouseUpEvent.getType());
        addDomHandler(mouseHandler, MouseMoveEvent.getType());

        setWidth(width);
        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().hideCaption());
    }

    /**
     * Constructor setting the dialog caption.<p>
     * 
     * @param caption the caption to set
     */
    public CmsPopup(String caption) {

        this();
        setCaption(caption);
    }

    /**
     * Constructor setting caption and width.<p>
     * 
     * @param caption the caption to set
     * @param width the width to set
     */
    public CmsPopup(String caption, int width) {

        this(width);
        setCaption(caption);
    }

    /**
     * The constructor.<p>
     * 
     * @param title the title and heading of the dialog
     * @param content the content widget
     */
    public CmsPopup(String title, Widget content) {

        this(title);
        setMainContent(content);
    }

    /**
     * Wraps the given Widget with a cornered border, padding and margin.<p>
     *  
     * @param w the widget to wrap
     * 
     * @return a new widget that wraps the given one
     */
    public static Widget wrapWithBorderPadding(Widget w) {

        w.addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().borderPadding());
        w.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        SimplePanel panel = new SimplePanel();
        panel.add(w);
        return panel;
    }

    /**
     * Adds the given child widget.<p>
     * 
     * @param w the widget
     */
    @Override
    public void add(Widget w) {

        add(w, m_main);
    }

    /**
     * Adds a button widget to the button panel.<p>
     * 
     * @param button the button widget
     */
    public void addButton(Widget button) {

        addButton(button, 0);
    }

    /**
     * Adds a button widget to the button panel before the given position.<p>
     * 
     * @param button the button widget
     * @param position the position to insert the button
     */
    public void addButton(Widget button, int position) {

        m_buttonPanel.insert(button, position);
        m_buttonPanel.setStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupButtonPanel());
    }

    /**
     * Adds a close "button" to the top of the popup.<p>
     * 
     * @param cmd the command that should be executed when the close button is clicked
     */
    public void addDialogClose(final Command cmd) {

        if (m_close == null) {
            m_close = new CloseButton();
            m_close.setTitle(Messages.get().key(Messages.GUI_CLOSE_0));
            m_close.addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().closePopup());
            m_close.setImageClass(I_CmsLayoutBundle.INSTANCE.dialogCss().closePopupImage());
            m_close.setButtonStyle(ButtonStyle.TRANSPARENT, null);
            m_close.addClickHandler(new ClickHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
                 */
                public void onClick(ClickEvent event) {

                    boolean cancelled = false;
                    try {
                        if (cmd != null) {
                            cmd.execute();
                        }
                    } catch (CmsCancelCloseException e) {
                        cancelled = true;
                    } finally {
                        if (!cancelled) {
                            hide();
                        }
                    }
                }
            });
            DOM.appendChild(m_containerElement, m_close.getElement());
            adopt(m_close);
        }
    }

    /**
     * Replaces current notification widget by an overlay.<p>
     */
    public void catchNotifications() {

        // remember current notification widget
        final I_CmsNotificationWidget widget = CmsNotification.get().getWidget();
        // create our own notification overlay
        final CmsDialogNotificationWidget notificationWidget = new CmsDialogNotificationWidget();
        add(notificationWidget);
        CmsNotification.get().setWidget(notificationWidget);

        // when closing the dialog
        addCloseHandler(new CloseHandler<PopupPanel>() {

            /**
             * @see CloseHandler#onClose(CloseEvent)
             */
            public void onClose(CloseEvent<PopupPanel> event) {

                // restore the previous notification widget
                CmsNotification.get().setWidget(widget);
                // remove the overlay notification widget
                remove(notificationWidget);
            }
        });
    }

    /**
     * @see com.google.gwt.user.client.ui.PopupPanel#center()
     */
    @Override
    public void center() {

        super.center();
        if (m_resizeHandlerRegistration == null) {
            m_resizeHandlerRegistration = Window.addResizeHandler(new ResizeHandler() {

                public void onResize(ResizeEvent event) {

                    m_windowWidth = event.getWidth();
                }
            });
        }
    }

    /**
     * Shows the dialog and centers it horizontally, but positions it at a fixed vertical position.<p>
     * 
     * @param top the top position
     */
    public void centerHorizontally(int top) {

        show();
        int left = (Window.getClientWidth() - getOffsetWidth()) >> 1;
        setPopupPosition(Math.max(Window.getScrollLeft() + left, 0), Math.max(Window.getScrollTop() + top, 0));
    }

    /**
     * @see com.google.gwt.user.client.ui.Panel#clear()
     */
    @Override
    public void clear() {

        for (Widget w : this) {
            // Orphan.
            try {
                orphan(w);
            } finally {
                // Physical detach.
                Element elem = w.getElement();
                DOM.removeChild(DOM.getParent(elem), elem);
            }
        }
        m_children = new WidgetCollection(this);
    }

    /**
     * Returns the dialog caption text.<p>
     * 
     * @return the dialog caption
     */
    public String getCaption() {

        return m_caption.getText();
    }

    /**
     * Returns the child widget with the given index.<p>
     * 
     * @param index the index
     * 
     * @return the child widget
     */
    public Widget getWidget(int index) {

        return getChildren().get(index);
    }

    /**
     * Returns the number of child widgets.<p>
     * 
     * @return the number of child widgets
     */
    public int getWidgetCount() {

        return getChildren().size();
    }

    /**
     * Returns the index of the given widget.<p>
     * 
     * @param child the child widget
     * 
     * @return the index of the child widget
     */
    public int getWidgetIndex(IsWidget child) {

        return getWidgetIndex(asWidgetOrNull(child));
    }

    /**
     * Returns the index of the given child widget.<p>
     * 
     * @param child the child widget
     * 
     * @return the index
     */
    public int getWidgetIndex(Widget child) {

        return getChildren().indexOf(child);
    }

    /**
     * Returns <code>true</code> if a caption is set for this popup <code>false</code> otherwise.<p>
     * 
     * @return <code>true</code> if a caption is set for this popup <code>false</code> otherwise
     */
    public boolean hasCaption() {

        return CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_caption.getText());
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
     * Inserts a child widget before the given index.<p>
     * 
     * @param w the child widget
     * @param beforeIndex the index
     * 
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    public void insert(Widget w, int beforeIndex) throws IndexOutOfBoundsException {

        insert(w, m_main, beforeIndex, true);
    }

    /**
     * Inserts a widget as the first widget in the popup.<p>
     * 
     * @param widget the widget to insert 
     */
    public void insertFront(Widget widget) {

        insert(widget, 0);
    }

    /**
     * @see com.google.gwt.user.client.ui.SimplePanel#iterator()
     */
    @Override
    public Iterator<Widget> iterator() {

        return getChildren().iterator();
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
     * Removes a child widget.<p>
     * 
     * @param index the index of the widget to remove
     * 
     * @return <code>true</code> if the there was a widget at the given index to remove
     */
    public boolean remove(int index) {

        Widget w = getWidget(index);
        if (w != null) {
            return remove(getWidget(index));
        }
        return false;
    }

    /**
     * @see com.google.gwt.user.client.ui.SimplePanel#remove(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public boolean remove(Widget w) {

        // Validate.
        if (w.getParent() != this) {
            return false;
        }
        // Orphan.
        try {
            orphan(w);
        } finally {
            // Physical detach.
            Element elem = w.getElement();
            DOM.removeChild(DOM.getParent(elem), elem);

            // Logical detach.
            getChildren().remove(w);
        }
        return true;
    }

    /**
     * Removes all buttons.<p>
     */
    public void removeAllButtons() {

        m_buttonPanel.clear();
    }

    /**
     * Removes the given button widget from the button panel.<p>
     * 
     * @param button the button widget to remove
     */
    public void removeButton(Widget button) {

        m_buttonPanel.remove(button);
        if (m_buttonPanel.getWidgetCount() == 0) {
            m_buttonPanel.setStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().hideButtonPanel());
        }
    }

    /**
     * Removes the padding from the popup's content.<p>
     */
    public void removePadding() {

        m_main.removeClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().contentPadding());
        m_contentHeightCorrection = -6;
    }

    /**
     * Sets the popup's content background.<p>
     * 
     * @param color the color to set
     */
    public void setBackgroundColor(String color) {

        m_main.getStyle().setBackgroundColor(color);
    }

    /**
     * Sets the captions text.<p>
     * 
     * @param caption the text to set
     */
    public void setCaption(String caption) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(caption)) {
            getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().hideCaption());
            m_caption.setText(caption);
        } else {
            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().hideCaption());
        }
    }

    /**
     * Sets the height for the popup content.<p>
     * 
     * @param height the height in pixels
     */
    public void setHeight(int height) {

        if (height <= 0) {
            m_containerElement.getStyle().clearWidth();
        } else {
            int contentHeight = height;
            if (hasCaption()) {
                contentHeight = contentHeight - 36;
            }
            if (hasButtons()) {
                contentHeight = contentHeight - 34;
            }
            contentHeight = contentHeight - m_contentHeightCorrection;
            m_containerElement.getStyle().setProperty("height", height + Unit.PX.toString());
            m_main.getStyle().setProperty("height", contentHeight + Unit.PX.toString());
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.PopupPanel#setHeight(java.lang.String)
     */
    @Override
    @Deprecated
    public void setHeight(String height) {

        throw new UnsupportedOperationException();
    }

    /**
     * Replaces the content from the main widget.<p>
     * 
     * @param w the widget that should replace the main content
     */
    public void setMainContent(Widget w) {

        clear();
        add(w);
    }

    /**
     * @see com.google.gwt.user.client.ui.UIObject#setSize(java.lang.String, java.lang.String)
     */
    @Override
    @Deprecated
    public void setPixelSize(int width, int height) {

        throw new UnsupportedOperationException();
    }

    /**
     * Sets the popup's dialog position to 'fixed'.<p>
     */
    public void setPositionFixed() {

        getElement().getStyle().setPosition(Position.FIXED);
    }

    /**
     * @see com.google.gwt.user.client.ui.UIObject#setSize(java.lang.String, java.lang.String)
     */
    @Override
    @Deprecated
    public void setSize(String width, String height) {

        throw new UnsupportedOperationException();
    }

    /**
     * Sets the use animation flag.<p>
     * 
     * @param use <code>true</code> if the animation should be used, default is <code>true</code>
     */
    public void setUseAnimation(boolean use) {

        m_useAnimation = use;
    }

    /**
     * Unsupported operation.<p>
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#setWidget(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    @Deprecated
    public void setWidget(Widget w) {

        throw new UnsupportedOperationException();
    }

    /**
     * Sets the width for the popup content.<p>
     * 
     * @param width the width in pixels
     */
    public void setWidth(int width) {

        if (width <= 0) {
            m_containerElement.getStyle().clearWidth();
        } else {
            m_containerElement.getStyle().setProperty("width", width + Unit.PX.toString());
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.PopupPanel#setWidth(java.lang.String)
     */
    @Override
    @Deprecated
    public void setWidth(String width) {

        throw new UnsupportedOperationException();
    }

    /**
     * @see com.google.gwt.user.client.ui.PopupPanel#show()
     */
    @Override
    public void show() {

        super.show();
        if (m_useAnimation) {
            CmsFadeAnimation.fadeIn(getElement(), null, 500);
        }
        if (m_resizeHandlerRegistration == null) {
            m_resizeHandlerRegistration = Window.addResizeHandler(new ResizeHandler() {

                public void onResize(ResizeEvent event) {

                    m_windowWidth = event.getWidth();
                }
            });
        }
    }

    /**
     * Adds a new child widget to the panel, attaching its Element to the
     * specified container Element.
     * 
     * @param child the child widget to be added
     * @param container the element within which the child will be contained
     */
    protected void add(Widget child, Element container) {

        // Detach new child.
        child.removeFromParent();

        // Logical attach.
        getChildren().add(child);

        // Physical attach.
        DOM.appendChild(container, child.getElement());

        // Adopt.
        adopt(child);
    }

    /**
     * Adjusts beforeIndex to account for the possibility that the given widget is
     * already a child of this panel.
     * 
     * @param child the widget that might be an existing child
     * @param beforeIndex the index at which it will be added to this panel
     * @return the modified index
     */
    protected int adjustIndex(Widget child, int beforeIndex) {

        checkIndexBoundsForInsertion(beforeIndex);

        // Check to see if this widget is already a direct child.
        if (child.getParent() == this) {
            // If the Widget's previous position was left of the desired new position
            // shift the desired position left to reflect the removal
            int idx = getWidgetIndex(child);
            if (idx < beforeIndex) {
                beforeIndex--;
            }
        }

        return beforeIndex;
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
        m_windowWidth = Window.getClientWidth();
        m_clientLeft = Document.get().getBodyOffsetLeft();
        m_clientTop = Document.get().getBodyOffsetTop();
        DOM.setCapture(getElement());
        m_dragStartX = event.getX();
        m_dragStartY = event.getY();
        addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().dragging());
    }

    /**
     * Checks that <code>index</code> is in the range [0, getWidgetCount()), which
     * is the valid range on accessible indexes.
     * 
     * @param index the index being accessed
     */
    protected void checkIndexBoundsForAccess(int index) {

        if ((index < 0) || (index >= getWidgetCount())) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Checks that <code>index</code> is in the range [0, getWidgetCount()], which
     * is the valid range for indexes on an insertion.
     * 
     * @param index the index where insertion will occur
     */
    protected void checkIndexBoundsForInsertion(int index) {

        if ((index < 0) || (index > getWidgetCount())) {
            throw new IndexOutOfBoundsException();
        }
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
            m_buttonPanel.onAttach();
            if (m_close != null) {
                m_close.onAttach();
            }
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
            m_buttonPanel.onDetach();
            if (m_close != null) {
                m_close.onDetach();
            }
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
     * Gets the list of children contained in this panel.
     * 
     * @return a collection of child widgets
     */
    protected WidgetCollection getChildren() {

        return m_children;
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
     * Insert a new child Widget into this Panel at a specified index, attaching
     * its Element to the specified container Element. The child Element will
     * either be attached to the container at the same index, or simply appended
     * to the container, depending on the value of <code>domInsert</code>.
     * 
     * @param child the child Widget to be added
     * @param container the Element within which <code>child</code> will be
     *          contained
     * @param beforeIndex the index before which <code>child</code> will be
     *          inserted
     * @param domInsert if <code>true</code>, insert <code>child</code> into
     *          <code>container</code> at <code>beforeIndex</code>; otherwise
     *          append <code>child</code> to the end of <code>container</code>.
     */
    protected void insert(Widget child, Element container, int beforeIndex, boolean domInsert) {

        // Validate index; adjust if the widget is already a child of this panel.
        beforeIndex = adjustIndex(child, beforeIndex);

        // Detach new child.
        child.removeFromParent();

        // Logical attach.
        getChildren().insert(child, beforeIndex);

        // Physical attach.
        if (domInsert) {
            DOM.insertChild(container, child.getElement(), beforeIndex);
        } else {
            DOM.appendChild(container, child.getElement());
        }

        // Adopt.
        adopt(child);
    }

    /**
     * Override to work around the glass overlay still showing after dialog hide.<p>
     * 
     * @see com.google.gwt.user.client.ui.Widget#onDetach()
     */
    @Override
    protected void onDetach() {

        super.onDetach();
        if (getGlassElement() != null) {
            getGlassElement().removeFromParent();
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

    /**
     * Appends the arrow element to the popup's dialog.<p>
     * 
     * @param arrow the arrow element to add
     */
    protected void showArrow(Element arrow) {

        getElement().appendChild(arrow);
    }

    /**
     * Returns <code>true</code> if this popup has buttons <code>false</code> otherwise.<p>
     * 
     * @return <code>true</code> if this popup has buttons <code>false</code> otherwise
     */
    private boolean hasButtons() {

        return m_buttonPanel.getWidgetCount() != 0;
    }

    /**
     * Checks if the target of the given event is the caption or a child of the caption.<p>
     * 
     * @param event the event to check
     * 
     * @return <code>true</code> if the target of the given event is the caption <code>false</code> otherwise
     */
    private boolean isCaptionEvent(NativeEvent event) {

        EventTarget target = event.getEventTarget();
        if (com.google.gwt.dom.client.Element.is(target)) {
            return m_caption.getElement().isOrHasChild(com.google.gwt.dom.client.Element.as(target));
        }
        return false;
    }

    /**
     * Returns the maximum available height inside the popup.<p>
     * 
     * @param fixedContentHeight fixed content height to deduct from the available height
     * 
     * @return the maximum available height
     */
    public int getAvailableHeight(int fixedContentHeight) {

        if (m_buttonPanel.isVisible()) {
            fixedContentHeight += m_buttonPanel.getOffsetHeight();
        }
        return Window.getClientHeight() - 150 - fixedContentHeight;
    }
}
