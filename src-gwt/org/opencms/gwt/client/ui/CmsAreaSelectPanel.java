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

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsPositionBean;
import org.opencms.gwt.client.util.CmsPositionBean.Area;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.HasMouseMoveHandlers;
import com.google.gwt.event.dom.client.HasMouseUpHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Select area widget. Allows the user to select an area inside the widget.<p>
 *
 * @since 8.0.0
 */
public class CmsAreaSelectPanel extends Composite
implements HasWidgets, IndexedPanel, HasValueChangeHandlers<CmsPositionBean>, HasMouseDownHandlers, HasMouseUpHandlers,
HasClickHandlers, HasMouseMoveHandlers, MouseDownHandler, MouseUpHandler, MouseMoveHandler {

    /** The ui-binder interface. */
    protected interface I_CmsAreaSelectPanelUiBinder extends UiBinder<HTMLPanel, CmsAreaSelectPanel> {
        // GWT interface, nothing to do
    }

    /** States of the slect area panel. */
    private enum State {
        /** Dragging the selection. */
        DRAGGING, /** Nothing selected. */
        EMPTY, /** Resizing the height. */
        RESIZE_HEIGHT, /** Resizing the width. */
        RESIZE_WIDTH, /** Selected. */
        SELECTED, /** Selecting new selection. */
        SELECTING
    }

    /** The ui-binder for this widget. */
    private static I_CmsAreaSelectPanelUiBinder m_uiBinder = GWT.create(I_CmsAreaSelectPanelUiBinder.class);

    /** The marker. */
    @UiField
    protected Element m_marker;

    /** Select overlay. */
    @UiField
    protected Element m_overlayBottom;

    /** Select overlay. */
    @UiField
    protected Element m_overlayLeft;

    /** Select overlay. */
    @UiField
    protected Element m_overlayRight;

    /** Select overlay. */
    @UiField
    protected Element m_overlayTop;

    /** The panel holding added widgets. */
    @UiField
    protected FlowPanel m_panel;

    /** The currently selected area. */
    private CmsPositionBean m_currentSelection;

    /** Select area size. */
    private int m_elementHeight;

    /** Select area size. */
    private int m_elementWidth;

    /** Starting point of the selection. */
    private int m_firstX;

    /** Starting point of the selection. */
    private int m_firstY;

    /** Fixed selection ratio. */
    private double m_heightToWidth;

    /** Fire all events flag. */
    private boolean m_isFireAll;

    /** The main widget. */
    private HTMLPanel m_main;

    /** Style of the selection marker. */
    private Style m_markerStyle;

    /** Mouse over area. */
    private Area m_mouseOverArea;

    /** Cursor offset while dragging a selection. */
    private int m_moveOffsetX;

    /** Cursor offset while dragging a selection. */
    private int m_moveOffsetY;

    /** Style of image overlay. */
    private Style m_overlayBottomStyle;

    /** Style of image overlay. */
    private Style m_overlayLeftStyle;

    /** Style of image overlay. */
    private Style m_overlayRightStyle;

    /** Style of image overlay. */
    private Style m_overlayTopStyle;

    /** Select area state. */
    private State m_state;

    /**
     * Constructor.<p>
     */
    public CmsAreaSelectPanel() {

        m_main = m_uiBinder.createAndBindUi(this);
        initWidget(m_main);
        m_state = State.EMPTY;
        m_heightToWidth = 0;
        setHandlers();

        m_markerStyle = m_marker.getStyle();
        m_overlayLeftStyle = m_overlayLeft.getStyle();
        m_overlayBottomStyle = m_overlayBottom.getStyle();
        m_overlayRightStyle = m_overlayRight.getStyle();
        m_overlayTopStyle = m_overlayTop.getStyle();

    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#add(com.google.gwt.user.client.ui.Widget)
     */
    public void add(Widget w) {

        m_panel.add(w);
    }

    /**
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    public HandlerRegistration addClickHandler(ClickHandler handler) {

        return addDomHandler(handler, ClickEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseDownHandlers#addMouseDownHandler(com.google.gwt.event.dom.client.MouseDownHandler)
     */
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {

        return addDomHandler(handler, MouseDownEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseMoveHandlers#addMouseMoveHandler(com.google.gwt.event.dom.client.MouseMoveHandler)
     */
    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {

        return addDomHandler(handler, MouseMoveEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseUpHandlers#addMouseUpHandler(com.google.gwt.event.dom.client.MouseUpHandler)
     */
    public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {

        return addDomHandler(handler, MouseUpEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<CmsPositionBean> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#clear()
     */
    public void clear() {

        m_panel.clear();
    }

    /**
     * Removes the current selection.<p>
     */
    public void clearSelection() {

        m_state = State.EMPTY;
        showSelect(false);
        m_currentSelection = null;
    }

    /**
     * Returns the position of the selected area, or <code>null</code> if nothing is selected.<p>
     *
     * @param relative if <code>true</code> the relative position is returned, otherwise the absolute position
     *
     * @return the position of the selected area
     */
    public CmsPositionBean getAreaPosition(boolean relative) {

        // returning the relative position
        if (relative) {
            return new CmsPositionBean(m_currentSelection);
        }

        // returning the absolute position
        CmsPositionBean abs = new CmsPositionBean(m_currentSelection);
        abs.setTop(m_currentSelection.getTop() + getElement().getAbsoluteTop());
        abs.setLeft(m_currentSelection.getLeft() + getElement().getAbsoluteLeft());
        return abs;
    }

    /**
     * @see com.google.gwt.user.client.ui.IndexedPanel#getWidget(int)
     */
    public Widget getWidget(int index) {

        return m_panel.getWidget(index);
    }

    /**
     * @see com.google.gwt.user.client.ui.IndexedPanel#getWidgetCount()
     */
    public int getWidgetCount() {

        return m_panel.getWidgetCount();
    }

    /**
     * @see com.google.gwt.user.client.ui.IndexedPanel#getWidgetIndex(com.google.gwt.user.client.ui.Widget)
     */
    public int getWidgetIndex(Widget child) {

        return m_panel.getWidgetIndex(child);
    }

    /**
     * Returns if the value change event will always be fired, or only when a select/resize/move operation is finished.<p>
     *
     * @return <code>true</code> if the value change event will always be fired
     */
    public boolean isFireAll() {

        return m_isFireAll;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#iterator()
     */
    public Iterator<Widget> iterator() {

        return m_panel.iterator();
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseDownHandler#onMouseDown(com.google.gwt.event.dom.client.MouseDownEvent)
     */
    public void onMouseDown(MouseDownEvent event) {

        if (event.getNativeButton() != NativeEvent.BUTTON_LEFT) {
            // only act on left button down, ignore right click
            return;
        }
        cacheElementSize();
        switch (m_state) {
            case EMPTY:
                DOM.setCapture(getElement());
                m_state = State.SELECTING;
                m_firstX = event.getRelativeX(getElement());
                m_firstY = event.getRelativeY(getElement());
                m_currentSelection = new CmsPositionBean();
                setSelectPosition(m_firstX, m_firstY, 0, 0);
                showSelect(true);

                break;
            case SELECTED:

                m_firstX = event.getRelativeX(getElement());
                m_firstY = event.getRelativeY(getElement());
                if (m_mouseOverArea == null) {
                    // mouse not over selection, remove selection
                    clearSelection();
                    fireChangeEvent(true);
                    break;
                }
                switch (m_mouseOverArea) {
                    case BORDER_TOP:
                        m_state = State.RESIZE_HEIGHT;

                        // fixing opposite border
                        m_firstX = m_currentSelection.getLeft();
                        m_firstY = m_currentSelection.getTop() + m_currentSelection.getHeight();
                        break;
                    case BORDER_BOTTOM:
                        m_state = State.RESIZE_HEIGHT;

                        // fixing opposite border
                        m_firstX = m_currentSelection.getLeft();
                        m_firstY = m_currentSelection.getTop();
                        break;
                    case BORDER_LEFT:
                        m_state = State.RESIZE_WIDTH;

                        // fixing opposite border
                        m_firstX = m_currentSelection.getLeft() + m_currentSelection.getWidth();
                        m_firstY = m_currentSelection.getTop();
                        break;
                    case BORDER_RIGHT:
                        m_state = State.RESIZE_WIDTH;

                        // fixing opposite border
                        m_firstX = m_currentSelection.getLeft();
                        m_firstY = m_currentSelection.getTop();
                        break;
                    case CENTER:
                        m_state = State.DRAGGING;
                        m_moveOffsetX = m_firstX - m_currentSelection.getLeft();
                        m_moveOffsetY = m_firstY - m_currentSelection.getTop();
                        break;
                    case CORNER_BOTTOM_LEFT:
                        m_state = State.SELECTING;

                        // fixing opposite corner
                        m_firstX = m_currentSelection.getLeft() + m_currentSelection.getWidth();
                        m_firstY = m_currentSelection.getTop();
                        break;
                    case CORNER_BOTTOM_RIGHT:
                        m_state = State.SELECTING;
                        // fixing opposite corner
                        m_firstX = m_currentSelection.getLeft();
                        m_firstY = m_currentSelection.getTop();
                        break;
                    case CORNER_TOP_LEFT:
                        m_state = State.SELECTING;

                        // fixing opposite corner
                        m_firstX = m_currentSelection.getLeft() + m_currentSelection.getWidth();
                        m_firstY = m_currentSelection.getTop() + m_currentSelection.getHeight();
                        break;
                    case CORNER_TOP_RIGHT:
                        m_state = State.SELECTING;

                        // fixing opposite corner
                        m_firstX = m_currentSelection.getLeft();
                        m_firstY = m_currentSelection.getTop() + m_currentSelection.getHeight();
                        break;
                    default:
                }
                DOM.setCapture(getElement());

                break;
            case DRAGGING:
            case RESIZE_HEIGHT:
            case RESIZE_WIDTH:
            case SELECTING:
            default:
                // Messed up selection state.
                // May happen if mouse-cursor was moved outside the window or frame while button pressed and button was released outside.
                if (m_currentSelection != null) {
                    m_state = State.SELECTED;
                    fireChangeEvent(true);
                } else {
                    // this should never happen
                    clearSelection();
                }
        }

        event.preventDefault();
        event.stopPropagation();
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseMoveHandler#onMouseMove(com.google.gwt.event.dom.client.MouseMoveEvent)
     */
    public void onMouseMove(MouseMoveEvent event) {

        int secondX = event.getRelativeX(getElement());
        int secondY = event.getRelativeY(getElement());
        cacheElementSize();
        // restricting cursor input to the area of the select panel
        secondX = (secondX < 0) ? 0 : ((secondX > m_elementWidth) ? m_elementWidth : secondX);
        secondY = (secondY < 0) ? 0 : ((secondY > m_elementHeight) ? m_elementHeight : secondY);

        switch (m_state) {
            case SELECTING:
                if (m_heightToWidth > 0) {
                    // fixed height to width ratio
                    // calculate the appropriate dimensions
                    int tempX = getXForY(secondX, secondY);
                    if (((tempX > secondX) && (secondX > m_firstX)) || ((tempX < secondX) && (secondX < m_firstX))) {
                        secondY = getYForX(secondX, secondY);
                    } else {
                        secondX = tempX;
                    }
                }
                positionX(secondX);
                positionY(secondY);
                fireChangeEvent(false);
                break;
            case DRAGGING:
                moveTo(secondX - m_moveOffsetX, secondY - m_moveOffsetY);
                fireChangeEvent(false);
                break;
            case RESIZE_HEIGHT:
                if (m_heightToWidth > 0) {
                    // fixed ratio, need the recalculate width to
                    int tempX = getXForY(secondX, secondY);
                    if ((tempX < 0) || (tempX > m_elementWidth)) {
                        tempX = secondX;
                        secondY = getYForX(secondX, secondY);
                    }
                    positionX(tempX);
                }
                positionY(secondY);
                fireChangeEvent(false);
                break;
            case RESIZE_WIDTH:
                if (m_heightToWidth > 0) {
                    // fixed ratio, need the recalculate height to
                    int tempY = getYForX(secondX, secondY);
                    if ((tempY < 0) || (tempY > m_elementWidth)) {
                        tempY = secondY;
                        secondX = getXForY(secondX, secondY);
                    }
                    positionY(getYForX(secondX, secondY));
                }
                positionX(secondX);
                fireChangeEvent(false);
                break;
            case SELECTED:
                // read over which area of the selection the cursor is positioned
                m_mouseOverArea = m_currentSelection.getArea(secondX, secondY, 30);

                // show the appropriate cursor
                if (m_mouseOverArea == null) {
                    m_markerStyle.setCursor(Cursor.DEFAULT);
                    break;
                }
                switch (m_mouseOverArea) {
                    case BORDER_LEFT:
                    case BORDER_RIGHT:
                        m_markerStyle.setCursor(Cursor.E_RESIZE);
                        break;
                    case BORDER_TOP:
                    case BORDER_BOTTOM:
                        m_markerStyle.setCursor(Cursor.N_RESIZE);
                        break;
                    case CENTER:
                        m_markerStyle.setCursor(Cursor.MOVE);
                        break;
                    case CORNER_BOTTOM_RIGHT:
                    case CORNER_TOP_LEFT:
                        m_markerStyle.setCursor(Cursor.NW_RESIZE);
                        break;
                    case CORNER_TOP_RIGHT:
                    case CORNER_BOTTOM_LEFT:
                        m_markerStyle.setCursor(Cursor.NE_RESIZE);
                        break;
                    default:
                }
                break;
            case EMPTY:
            default:
        }

    }

    /**
     * @see com.google.gwt.event.dom.client.MouseUpHandler#onMouseUp(com.google.gwt.event.dom.client.MouseUpEvent)
     */
    public void onMouseUp(MouseUpEvent event) {

        switch (m_state) {
            case SELECTING:
            case DRAGGING:
            case RESIZE_HEIGHT:
            case RESIZE_WIDTH:
                m_state = State.SELECTED;
                m_mouseOverArea = null;
                fireChangeEvent(true);
                DOM.releaseCapture(getElement());
                event.preventDefault();
                event.stopPropagation();
                break;
            case SELECTED:
            case EMPTY:
            default:
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.IndexedPanel#remove(int)
     */
    public boolean remove(int index) {

        return m_panel.remove(index);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#remove(com.google.gwt.user.client.ui.Widget)
     */
    public boolean remove(Widget w) {

        return m_panel.remove(w);
    }

    /**
     * Resets the select area ratio.<p>
     */
    public void resetRatio() {

        m_heightToWidth = 0;
    }

    /**
     * Sets the selection area.<p>
     *
     * @param relative <code>true</code> if provided position is relative to the select area, not absolute to the page
     * @param pos the area position to select
     */
    public void setAreaPosition(boolean relative, CmsPositionBean pos) {

        if (pos == null) {
            return;
        }
        m_state = State.SELECTED;
        showSelect(true);
        m_currentSelection = new CmsPositionBean();
        m_firstX = pos.getLeft();
        m_firstY = pos.getTop();
        if (!relative) {
            m_firstX -= getElement().getAbsoluteLeft();
            m_firstY -= getElement().getAbsoluteTop();
        }
        //        setSelectPosition(m_firstX, m_firstY, 0, 0);
        setSelectPosition(m_firstX, m_firstY, pos.getHeight(), pos.getWidth());
    }

    /**
     * Sets if the value change event will always be fired, or only when a select/resize/move operation is finished.<p>
     *
     * @param isFireAll <code>true</code> to always be fire the value change event
     */
    public void setFireAll(boolean isFireAll) {

        m_isFireAll = isFireAll;
    }

    /**
     * Sets a fixed selection ratio. Set <code>0</code> to remove the fix.<p>
     *
     * @param heightToWidth the height to width ratio
     */
    public void setRatio(double heightToWidth) {

        m_heightToWidth = heightToWidth;
    }

    /**
     * Caches the select area element size.<p>
     */
    private void cacheElementSize() {

        // cache element size if necessary
        if ((m_elementHeight == 0) && (m_elementWidth == 0)) {
            m_elementHeight = getElement().getOffsetHeight();
            m_elementWidth = getElement().getOffsetWidth();
        }
    }

    /**
     * Fires the value change event.<p>
     *
     * @param alwaysFire <code>true</code> to always fire the change event, ignoring the fire all flag
     */
    private void fireChangeEvent(boolean alwaysFire) {

        if (alwaysFire || m_isFireAll) {
            ValueChangeEvent.fire(this, m_currentSelection);
        }
    }

    /**
     * Calculates the matching X (left/width) value in case of a fixed height/width ratio.<p>
     *
     * @param newX the cursor X offset to the selection area
     * @param newY the cursor Y offset to the selection area
     *
     * @return the matching X value
     */
    private int getXForY(int newX, int newY) {

        int width = (int)Math.floor((newY - m_firstY) / m_heightToWidth);
        int result = m_firstX + width;
        if (((m_firstX - newX) * (m_firstX - result)) < 0) {
            result = m_firstX - width;
        }
        return result;
    }

    /**
     * Calculates the matching Y (top/height) value in case of a fixed height/width ratio.<p>
     *
     * @param newX the cursor X offset to the selection area
     * @param newY the cursor Y offset to the selection area
     *
     * @return the matching Y value
     */
    private int getYForX(int newX, int newY) {

        int height = (int)Math.floor((newX - m_firstX) * m_heightToWidth);
        int result = m_firstY + height;
        if (((m_firstY - newY) * (m_firstY - result)) < 0) {
            result = m_firstY - height;
        }
        return result;
    }

    /**
     * Moves the select area to the specified position, while keeping the size.<p>
     *
     * @param posX the new X position
     * @param posY the new Y position
     */
    private void moveTo(int posX, int posY) {

        posX = (posX < 0)
        ? 0
        : (((posX + m_currentSelection.getWidth()) >= m_elementWidth)
        ? m_elementWidth - m_currentSelection.getWidth()
        : posX);
        posY = (posY < 0)
        ? 0
        : (((posY + m_currentSelection.getHeight()) >= m_elementHeight)
        ? m_elementHeight - m_currentSelection.getHeight()
        : posY);

        m_markerStyle.setTop(posY, Unit.PX);
        m_markerStyle.setLeft(posX, Unit.PX);

        m_overlayLeftStyle.setWidth(posX, Unit.PX);

        m_overlayTopStyle.setLeft(posX, Unit.PX);
        m_overlayTopStyle.setHeight(posY, Unit.PX);

        m_overlayBottomStyle.setLeft(posX, Unit.PX);
        m_overlayBottomStyle.setHeight(m_elementHeight - posY - m_currentSelection.getHeight(), Unit.PX);

        m_overlayRightStyle.setWidth(m_elementWidth - posX - m_currentSelection.getWidth(), Unit.PX);

        m_currentSelection.setTop(posY);
        m_currentSelection.setLeft(posX);
    }

    /**
     * Setting a new left/top value for the selection.<p>
     *
     * @param secondX the cursor X offset to the selection area
     */
    private void positionX(int secondX) {

        if (secondX < m_firstX) {
            setSelectPositionX(secondX, m_firstX - secondX);
        } else {
            setSelectWidth(secondX - m_firstX);
        }
    }

    /**
     * Setting a new top/height value for the selection.<p>
     *
     * @param secondY the cursor Y offset to the selection area
     */
    private void positionY(int secondY) {

        if (secondY < m_firstY) {
            setSelectPositionY(secondY, m_firstY - secondY);
        } else {
            setSelectHeight(secondY - m_firstY);
        }
    }

    /**
     * Sets self as mouse down, up and move handler.<p>
     */
    private void setHandlers() {

        addMouseDownHandler(this);
        addMouseMoveHandler(this);
        addMouseUpHandler(this);
    }

    /**
     * Adjusts the select area height, while keeping the Y position of the top/left corner.<p>
     *
     * @param height the new height
     */
    private void setSelectHeight(int height) {

        m_markerStyle.setHeight(height, Unit.PX);

        m_overlayBottomStyle.setHeight(m_elementHeight - m_currentSelection.getTop() - height, Unit.PX);

        m_currentSelection.setHeight(height);
    }

    /**
     * Sets position and size of the select area.<p>
     *
     * @param posX the new X position
     * @param posY the new Y position
     * @param height the new height
     * @param width the new width
     */
    private void setSelectPosition(int posX, int posY, int height, int width) {

        setSelectPositionX(posX, width);
        setSelectPositionY(posY, height);
    }

    /**
     * Sets X position and width of the select area.<p>
     *
     * @param posX the new X position
     * @param width the new width
     */
    private void setSelectPositionX(int posX, int width) {

        m_markerStyle.setLeft(posX, Unit.PX);
        m_markerStyle.setWidth(width, Unit.PX);

        m_overlayLeftStyle.setWidth(posX, Unit.PX);
        m_overlayTopStyle.setLeft(posX, Unit.PX);
        m_overlayTopStyle.setWidth(width, Unit.PX);
        m_overlayBottomStyle.setLeft(posX, Unit.PX);
        m_overlayBottomStyle.setWidth(width, Unit.PX);
        m_overlayRightStyle.setWidth(m_elementWidth - posX - width, Unit.PX);

        m_currentSelection.setLeft(posX);
        m_currentSelection.setWidth(width);
    }

    /**
     * Sets Y position and height of the select area.<p>
     *
     * @param posY the new Y position
     * @param height the new height
     */
    private void setSelectPositionY(int posY, int height) {

        m_markerStyle.setTop(posY, Unit.PX);
        m_markerStyle.setHeight(height, Unit.PX);

        m_overlayTopStyle.setHeight(posY, Unit.PX);
        m_overlayBottomStyle.setHeight(m_elementHeight - posY - height, Unit.PX);

        m_currentSelection.setTop(posY);
        m_currentSelection.setHeight(height);
    }

    /**
     * Adjusts the select area width, while keeping the X position of the top/left corner.<p>
     *
     * @param width the new width
     */
    private void setSelectWidth(int width) {

        m_markerStyle.setWidth(width, Unit.PX);

        m_overlayTopStyle.setWidth(width, Unit.PX);
        m_overlayBottomStyle.setWidth(width, Unit.PX);
        m_overlayRightStyle.setWidth(m_elementWidth - m_currentSelection.getLeft() - width, Unit.PX);

        m_currentSelection.setWidth(width);
    }

    /**
     * Shows or hides the select area.<p>
     *
     * @param show if <code>true</code> the select area will be shown
     */
    private void showSelect(boolean show) {

        if (show) {
            m_main.addStyleName(I_CmsLayoutBundle.INSTANCE.selectAreaCss().showSelect());
            return;
        }
        m_main.removeStyleName(I_CmsLayoutBundle.INSTANCE.selectAreaCss().showSelect());
    }
}
