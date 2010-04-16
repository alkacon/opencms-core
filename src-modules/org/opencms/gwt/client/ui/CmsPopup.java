/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsPopup.java,v $
 * Date   : $Date: 2010/04/16 13:54:15 $
 * Version: $Revision: 1.8 $
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

import java.util.Iterator;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

/**
 * Provides a pop up dialog base.
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 8.0.0
 */
public class CmsPopup {

    /**
     * The pop up dialog wrapped by the public class.<p>
     */
    protected class PopupDialog extends DialogBox {

        /** The popup container element. */
        private com.google.gwt.user.client.Element m_containerElement;

        /** The main widget of this dialog containing all others. */
        private FlowPanel m_main;

        /**
         * Constructor.<p>
         */
        protected PopupDialog() {

            super(false);
            m_containerElement = super.getContainerElement();
            setStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popup());
            m_main = new FlowPanel();
            setWidget(m_main);
            m_containerElement.setClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupContent()
                + " "
                + I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll()
                + " "
                + I_CmsLayoutBundle.INSTANCE.generalCss().shadow());
            setGlassStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupOverlay());
            ((UIObject)getCaption()).getElement().addClassName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerTop());
        }

        /**
         * @see com.google.gwt.user.client.ui.SimplePanel#add(com.google.gwt.user.client.ui.Widget)
         */
        @Override
        public void add(Widget w) {

            this.m_main.add(w);
        }

        /**
         * Removes all content widgets.<p>
         */
        @Override
        public void clear() {

            m_main.clear();
        }

        /**
         * Returns the content widget with the given index.<p>
         * 
         * @param index the index of the widget
         * 
         * @return the widget
         * 
         * @see com.google.gwt.user.client.ui.ComplexPanel#getWidget(int)
         */
        public Widget getWidget(int index) {

            return m_main.getWidget(index);
        }

        /**
         * Returns the number of content widgets.<p>
         * 
         * @return the number of widgets
         * 
         * @see com.google.gwt.user.client.ui.ComplexPanel#getWidgetCount()
         */
        public int getWidgetCount() {

            return m_main.getWidgetCount();
        }

        /**
         * Returns the index of the given widget.<p>
         * 
         * @param child the widget to get the index for
         * 
         * @return the widget index
         * 
         * @see com.google.gwt.user.client.ui.ComplexPanel#getWidgetIndex(com.google.gwt.user.client.ui.Widget)
         */
        public int getWidgetIndex(Widget child) {

            return m_main.getWidgetIndex(child);
        }

        /**
         * Inserts the widget before the specified index.<p>
         * 
         * @param w the widget to insert
         * @param beforeIndex the index before which it will be inserted
         * 
         * @throws IndexOutOfBoundsException if beforeIndex is out of range
         */
        public void insert(Widget w, int beforeIndex) throws IndexOutOfBoundsException {

            m_main.insert(w, beforeIndex);
        }

        /**
         * @see com.google.gwt.user.client.ui.DecoratedPopupPanel#iterator()
         */
        @Override
        public Iterator<Widget> iterator() {

            return m_main.iterator();
        }

        /**
         * Removes the widget at the specified index.<p>
         * 
         * @param index the index to remove
         * 
         * @return false if the widget is not present
         */
        public boolean remove(int index) {

            return m_main.remove(index);
        }

        /**
         * Removes the specified widget.<p>
         * 
         * @param w the widget to remove
         * 
         * @return false if the widget is not present
         */
        @Override
        public boolean remove(Widget w) {

            return m_main.remove(w);
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
    }

    /** The default width of this dialog. */
    private static final String DEFAULT_WIDTH = "300px";

    /** The wrapped pop up dialog. */
    private PopupDialog m_dialog;

    /**
     * Constructor.<p>
     */
    public CmsPopup() {

        m_dialog = new PopupDialog();
        m_dialog.setWidth(DEFAULT_WIDTH);
    }

    /**
     * Constructor setting the width of the dialog.<p>
     * 
     * @param width the width to set
     * @param unit the width unit 
     */
    public CmsPopup(int width, Unit unit) {

        m_dialog = new PopupDialog();
        m_dialog.setWidth(width + unit.toString());
    }

    /**
     * Constructor setting the dialog caption.<p>
     * 
     * @param caption the caption to set
     */
    public CmsPopup(String caption) {

        this();
        m_dialog.setText(caption);
    }

    /**
     * Constructor setting caption and width.<p>
     * 
     * @param caption the caption to set
     * @param width the width to set
     * @param unit the width unit
     */
    public CmsPopup(String caption, int width, Unit unit) {

        this(width, unit);
        m_dialog.setText(caption);
    }

    /**
     * Adds the given child widget.<p>
     * 
     * @param w the widget
     * 
     * @see org.opencms.gwt.client.ui.CmsPopup.PopupDialog#add(com.google.gwt.user.client.ui.Widget)
     */
    public void add(Widget w) {

        m_dialog.add(w);
    }

    /**
     * Mouse events that occur within an autoHide partner will not hide a panel set to autoHide.<p>
     * 
     * @param partner the auto hide partner to add
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#addAutoHidePartner(com.google.gwt.dom.client.Element)
     */
    public void addAutoHidePartner(Element partner) {

        m_dialog.addAutoHidePartner(partner);
    }

    /**
     * Adds a {@link com.google.gwt.event.logical.shared.CloseEvent} handler to the dialog.<p>
     * 
     * @param handler the handler to add
     * 
     * @return the registration for the event
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#addCloseHandler(com.google.gwt.event.logical.shared.CloseHandler)
     */
    public HandlerRegistration addCloseHandler(CloseHandler<PopupPanel> handler) {

        return m_dialog.addCloseHandler(handler);
    }

    /**
     * Adds a dependent style name.<p>
     * 
     * @param styleSuffix the style suffix
     * 
     * @see com.google.gwt.user.client.ui.UIObject#addStyleDependentName(java.lang.String)
     */
    public void addStyleDependentName(String styleSuffix) {

        m_dialog.addStyleDependentName(styleSuffix);
    }

    /**
     * Adds another style name.<p>
     * 
     * @param style the style name
     * 
     * @see com.google.gwt.user.client.ui.UIObject#addStyleName(java.lang.String)
     */
    public void addStyleName(String style) {

        m_dialog.addStyleName(style);
    }

    /**
     * Opens the dialog at the center of the browser window, or positions it there, if already visible.<p> 
     */
    public void center() {

        m_dialog.center();
    }

    /**
     * Clears all child widgets.<p>
     * 
     * @see org.opencms.gwt.client.ui.CmsPopup.PopupDialog#clear()
     */
    public void clear() {

        m_dialog.clear();
    }

    /**
     * Fires the given event to all the appropriate handlers.
     * 
     * @param event the event to be fired
     * 
     * @see com.google.gwt.user.client.ui.Widget#fireEvent(com.google.gwt.event.shared.GwtEvent)
     */
    public void fireEvent(GwtEvent<?> event) {

        m_dialog.fireEvent(event);
    }

    /**
     * Gets the dialog's absolute left position in pixels, as measured from the browser window's client area.<p>
     * 
     * @return the dialog's absolute left position
     * 
     * @see com.google.gwt.user.client.ui.UIObject#getAbsoluteLeft()
     */
    public int getAbsoluteLeft() {

        return m_dialog.getAbsoluteLeft();
    }

    /**
     * Gets the dialog's absolute top position in pixels, as measured from the browser window's client area.<p>
     * 
     * @return the dialog's absolute top position
     * 
     * @see com.google.gwt.user.client.ui.UIObject#getAbsoluteTop()
     */
    public int getAbsoluteTop() {

        return m_dialog.getAbsoluteTop();
    }

    /**
     * Gets the panel's offset height in pixels. Calls to setHeight(String) before the panel's child widget is set will not influence the offset height.<p>
     * 
     * @return the object's offset height
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#getOffsetHeight()
     */
    public int getOffsetHeight() {

        return m_dialog.getOffsetHeight();
    }

    /**
     * Gets the panel's offset width in pixels. Calls to setWidth(String) before the panel's child widget is set will not influence the offset width.<p>
     * 
     * @return the object's offset width
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#getOffsetWidth()
     */
    public int getOffsetWidth() {

        return m_dialog.getOffsetWidth();
    }

    /**
     * Gets the popup's left position relative to the browser's client area.<p>
     * 
     * @return the popup's left position
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#getPopupLeft()
     */
    public int getPopupLeft() {

        return m_dialog.getPopupLeft();
    }

    /**
     * Gets the popup's top position relative to the browser's client area.<p>
     * 
     * @return the popup's top position
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#getPopupTop()
     */
    public int getPopupTop() {

        return m_dialog.getPopupTop();
    }

    /**
     * Returns the caption text content.<p>
     * 
     * @return the caption text
     * 
     * @see com.google.gwt.user.client.ui.DialogBox#getText()
     */
    public String getText() {

        return m_dialog.getText();
    }

    /**
     * Returns the pop up dialog's title.<p>
     * 
     * @return the title
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#getTitle()
     */
    public String getTitle() {

        return m_dialog.getTitle();
    }

    /**
     * Returns the child widget with the given index.<p>
     * 
     * @param index the index
     * 
     * @return the child widget
     * 
     * @see org.opencms.gwt.client.ui.CmsPopup.PopupDialog#getWidget(int)
     */
    public Widget getWidget(int index) {

        return m_dialog.getWidget(index);
    }

    /**
     * Returns the number of child widgets.<p>
     * 
     * @return the number of child widgets
     * 
     * @see org.opencms.gwt.client.ui.CmsPopup.PopupDialog#getWidgetCount()
     */
    public int getWidgetCount() {

        return m_dialog.getWidgetCount();
    }

    /**
     * Returns the index of the given child widget.<p>
     * 
     * @param child the child widget
     * 
     * @return the index
     *  
     * @see org.opencms.gwt.client.ui.CmsPopup.PopupDialog#getWidgetIndex(com.google.gwt.user.client.ui.Widget)
     */
    public int getWidgetIndex(Widget child) {

        return m_dialog.getWidgetIndex(child);
    }

    /**
     * Hides the dialog window.<p>
     */
    public void hide() {

        m_dialog.hide();
    }

    /**
     * Inserts a child widget before the given index.<p>
     * 
     * @param w the child widget
     * @param beforeIndex the index
     * 
     * @throws IndexOutOfBoundsException if the index is out of bounds
     * 
     * @see org.opencms.gwt.client.ui.CmsPopup.PopupDialog#insert(com.google.gwt.user.client.ui.Widget, int)
     */
    public void insert(Widget w, int beforeIndex) throws IndexOutOfBoundsException {

        m_dialog.insert(w, beforeIndex);
    }

    /**
     * Returns if animation is enabled.<p>
     * 
     * @return true if animation is enabled
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#isAnimationEnabled()
     */
    public boolean isAnimationEnabled() {

        return m_dialog.isAnimationEnabled();
    }

    /**
     * Returns if the dialog element is attached to the DOM.<p>
     * 
     * @return true if attached
     * 
     * @see com.google.gwt.user.client.ui.Widget#isAttached()
     */
    public boolean isAttached() {

        return m_dialog.isAttached();
    }

    /**
     * Returns if the auto hide feature is enabled.<p>
     * 
     * @return true if auto hide is enabled
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#isAutoHideEnabled()
     */
    public boolean isAutoHideEnabled() {

        return m_dialog.isAutoHideEnabled();
    }

    /**
     * Returns if the auto hide on history event feature is enabled.<p>
     * 
     * @return true if auto hide is enabled
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#isAutoHideOnHistoryEventsEnabled()
     */
    public boolean isAutoHideOnHistoryEventsEnabled() {

        return m_dialog.isAutoHideOnHistoryEventsEnabled();
    }

    /**
     * Returns if the dialog overlay is enabled.<p>
     * 
     * @return true if the overlay will be shown
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#isGlassEnabled()
     */
    public boolean isGlassEnabled() {

        return m_dialog.isGlassEnabled();
    }

    /**
     * Returns if the dialog is modal.<p>
     * 
     * @return true if the dialog is modal
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#isModal()
     */
    public boolean isModal() {

        return m_dialog.isModal();
    }

    /**
     * Returns true if the popup should preview all native events, even if the event has already been consumed by another popup.<p>
     * 
     * @return true if the popup should preview all native events
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#isPreviewingAllNativeEvents()
     */
    public boolean isPreviewingAllNativeEvents() {

        return m_dialog.isPreviewingAllNativeEvents();
    }

    /**
     * Determines whether or not this popup is showing.<p>
     * 
     * @return Determines whether or not this popup is showing
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#isShowing()
     */
    public boolean isShowing() {

        return m_dialog.isShowing();
    }

    /**
     * Determines whether or not this popup is visible.<p>
     * 
     * @return true if the object is visible
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#isVisible()
     */
    public boolean isVisible() {

        return m_dialog.isVisible();
    }

    /**
     * Gets an iterator for the contained widgets.<p>
     * 
     * @return the widget iterator
     * 
     * @see org.opencms.gwt.client.ui.CmsPopup.PopupDialog#iterator()
     */
    public Iterator<Widget> iterator() {

        return m_dialog.iterator();
    }

    /**
     * Fired whenever a browser event is received.<p>
     * 
     * @param event the event
     * 
     * @see com.google.gwt.user.client.ui.DialogBox#onBrowserEvent(com.google.gwt.user.client.Event)
     */
    public void onBrowserEvent(Event event) {

        m_dialog.onBrowserEvent(event);
    }

    /**
     * Removes a dependent style name.<p>
     * 
     * @param styleSuffix the dependent style name to remove
     * 
     * @see com.google.gwt.user.client.ui.UIObject#removeStyleDependentName(java.lang.String)
     */
    public void removeStyleDependentName(String styleSuffix) {

        m_dialog.removeStyleDependentName(styleSuffix);
    }

    /**
     * Removes a style name.<p>
     * 
     * @param style the style name to remove
     * 
     * @see com.google.gwt.user.client.ui.UIObject#removeStyleName(java.lang.String)
     */
    public void removeStyleName(String style) {

        m_dialog.removeStyleName(style);
    }

    /**
     * Sets the animation enabled.<p>
     * 
     * @param enable true to enable, false to disable
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#setAnimationEnabled(boolean)
     */
    public void setAnimationEnabled(boolean enable) {

        m_dialog.setAnimationEnabled(enable);
    }

    /**
     * Enable or disable the autoHide feature. When enabled, the popup will be automatically hidden when the user clicks outside of it.<p>
     * 
     * @param autoHide enable true to enable, false to disable
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#setAutoHideEnabled(boolean)
     */
    public void setAutoHideEnabled(boolean autoHide) {

        m_dialog.setAutoHideEnabled(autoHide);
    }

    /**
     * Enable or disable autoHide on history change events. When enabled, the popup will be automatically hidden when the history token changes, such as when the user presses the browser's back button. Disabled by default.<p>
     * 
     * @param enabled enable true to enable, false to disable
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#setAutoHideOnHistoryEventsEnabled(boolean)
     */
    public void setAutoHideOnHistoryEventsEnabled(boolean enabled) {

        m_dialog.setAutoHideOnHistoryEventsEnabled(enabled);
    }

    /**
     * When enabled, the background will be blocked with a semi-transparent pane the next time it is shown. If the PopupPanel is already visible, the glass will not be displayed until it is hidden and shown again.<p>
     * 
     * @param enabled enable true to enable, false to disable
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#setGlassEnabled(boolean)
     */
    public void setGlassEnabled(boolean enabled) {

        m_dialog.setGlassEnabled(enabled);
    }

    /**
     * Set the dialog to modal.<p>
     * 
     * @param modal enable true to enable, false to disable
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#setModal(boolean)
     */
    public void setModal(boolean modal) {

        m_dialog.setModal(modal);
    }

    /**
     * Sets the object's size, in pixels, not including decorations such as border, margin, and padding.<p>
     * 
     * @param width the object's new width, in pixels
     * @param height the object's new height, in pixels
     * 
     * @see com.google.gwt.user.client.ui.UIObject#setPixelSize(int, int)
     */
    public void setPixelSize(int width, int height) {

        m_dialog.setPixelSize(width, height);
    }

    /**
     * Sets the popup's position using a {@link PositionCallback}, and shows the popup. The callback allows positioning to be performed based on the offsetWidth and offsetHeight of the popup, which are normally not available until the popup is showing. By positioning the popup before it is shown, the the popup will not jump from its original position to the new position.<p>
     * 
     * @param callback the callback
     */
    public void setPopupPositionAndShow(PositionCallback callback) {

        m_dialog.setPopupPositionAndShow(callback);
    }

    /**
     * Sets the popup's position relative to the browser's client area.<p>
     * 
     * @param left position left in pixels
     * @param top position top in pixels
     */
    public void setPosition(int left, int top) {

        m_dialog.setPopupPosition(left, top);

    }

    /**
     * Sets the object's size.<p>
     * 
     * @param width the width
     * @param height the height
     * @param unit the unit to use
     */
    public void setSize(int width, int height, Unit unit) {

        m_dialog.setSize(width + unit.toString(), height + unit.toString());
    }

    /**
     * Sets the object's size. This size does not include decorations such as border, margin, and padding.<p>
     * 
     * @param width the object's new width, in CSS units (e.g. "10px", "1em")
     * @param height the object's new height, in CSS units (e.g. "10px", "1em")
     * @see com.google.gwt.user.client.ui.UIObject#setSize(java.lang.String, java.lang.String)
     */
    public void setSize(String width, String height) {

        m_dialog.setSize(width, height);
    }

    /**
     * Sets the captions text.<p>
     * 
     * @param text the text to set
     * 
     * @see com.google.gwt.user.client.ui.DialogBox#setText(java.lang.String)
     */
    public void setText(String text) {

        m_dialog.setText(text);
    }

    /**
     * Sets the dialog widget title.<p>
     * 
     * @param title the title to set
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#setTitle(java.lang.String)
     */
    public void setTitle(String title) {

        m_dialog.setTitle(title);
    }

    /**
     * Sets the dialog width.<p>
     * 
     * @param width the width, in CSS units (e.g. "10px", "1em")
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#setWidth(java.lang.String)
     */
    public void setWidth(String width) {

        m_dialog.setWidth(width);
    }

    /**
     * Shows the popup and attach it to the page. It must have a child widget before this method is called.<p>
     */
    public void show() {

        m_dialog.show();
    }

    /**
     * Normally, the popup is positioned directly below the relative target, with its left edge aligned with the left edge of the target. Depending on the width and height of the popup and the distance from the target to the bottom and right edges of the window, the popup may be displayed directly above the target, and/or its right edge may be aligned with the right edge of the target.<p>
     * 
     * @param target the target to show the popup below
     * 
     * @see com.google.gwt.user.client.ui.PopupPanel#showRelativeTo(com.google.gwt.user.client.ui.UIObject)
     */
    public final void showRelativeTo(UIObject target) {

        m_dialog.showRelativeTo(target);
    }

    /**
     * Returns the dialog widget. 
     * 
     * @return the dialog widget
     */
    protected PopupDialog getDialog() {

        return m_dialog;
    }

}
