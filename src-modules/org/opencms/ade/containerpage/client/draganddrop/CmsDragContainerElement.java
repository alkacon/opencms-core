/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/draganddrop/Attic/CmsDragContainerElement.java,v $
 * Date   : $Date: 2010/04/28 13:03:40 $
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

package org.opencms.ade.containerpage.client.draganddrop;

import org.opencms.ade.containerpage.client.ui.CmsElementOptionBar;
import org.opencms.gwt.client.draganddrop.I_CmsDragElementExt;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.draganddrop.I_CmsDragTarget;
import org.opencms.gwt.client.draganddrop.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.css.I_CmsToolbarButtonLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Implementation of a draggable element. To be used for content elements within a container-page.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 8.0.0
 */
public class CmsDragContainerElement extends AbsolutePanel implements I_CmsDragElementExt, HasClickHandlers {

    private static final String MOVE_HANDLE_CLASS = I_CmsButton.ButtonData.MOVE.getIconClass();

    /** The current place holder element. */
    protected Widget m_currentPlaceholder;

    /** The start offset left of the element to its parent. */
    protected int m_cursorOffsetLeft;

    /** The start offset top of the element to its parent. */
    protected int m_cursorOffsetTop;

    /** The drag handle widget. */
    protected Widget m_dragHandle;

    /** The current place holder element. */
    protected HTML m_placeholder;

    /** The elements style. */
    protected Style m_style;

    /** The elements client id. */
    private String m_clientId;

    /** The current drag parent. */
    private I_CmsDragTarget m_dragParent;

    /** The option bar, holding optional function buttons. */
    private CmsElementOptionBar m_elementOptionBar;

    /** The no edit reason, if empty editing is allowed. */
    private String m_noEditReason;

    /** The element resource site-path. */
    private String m_sitePath;

    /**
     * Constructor.<p>
     * 
     * @param element the DOM element
     * @param parent the drag parent
     * @param clientId the client id
     * @param sitePath the element site-path
     * @param noEditReason the no edit reason, if empty, editing is allowed
     */
    public CmsDragContainerElement(
        Element element,
        I_CmsDragTarget parent,
        String clientId,
        String sitePath,
        String noEditReason) {

        super(element);
        m_clientId = clientId;
        m_sitePath = sitePath;
        m_noEditReason = noEditReason;
        setDragParent(parent);

        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElement());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    public HandlerRegistration addClickHandler(ClickHandler handler) {

        return addDomHandler(handler, ClickEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasContextMenuHandlers#addContextMenuHandler(com.google.gwt.event.dom.client.ContextMenuHandler)
     */
    public HandlerRegistration addContextMenuHandler(ContextMenuHandler handler) {

        return addDomHandler(handler, ContextMenuEvent.getType());
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
     * @see com.google.gwt.event.dom.client.HasMouseOutHandlers#addMouseOutHandler(com.google.gwt.event.dom.client.MouseOutHandler)
     */
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {

        return addDomHandler(handler, MouseOutEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOverHandlers#addMouseOverHandler(com.google.gwt.event.dom.client.MouseOverHandler)
     */
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {

        return addDomHandler(handler, MouseOverEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseUpHandlers#addMouseUpHandler(com.google.gwt.event.dom.client.MouseUpHandler)
     */
    public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {

        return addDomHandler(handler, MouseUpEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseWheelHandlers#addMouseWheelHandler(com.google.gwt.event.dom.client.MouseWheelHandler)
     */
    public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {

        return addDomHandler(handler, MouseWheelEvent.getType());
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElementExt#clearDrag()
     */
    public void clearDrag() {

        Style style = getElement().getStyle();
        style.clearPosition();
        style.clearWidth();
        style.clearTop();
        style.clearLeft();
        style.clearZIndex();
        style.clearMargin();
        style.clearDisplay();
        getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElementBackground());
        getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElementBorder());
        getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());
        getElement().removeClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().shadow());
        getElementOptionBar().removeStyleName(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().cmsHovering());

    }

    /**
     * Returns the client id.<p>
     *
     * @return the client id
     */
    public String getClientId() {

        return m_clientId;
    }

    /**
     * Returns the cursor offset left.<p>
     *
     * @return the cursor offset left
     */
    public int getCursorOffsetLeft() {

        return m_cursorOffsetLeft;
    }

    /**
     * Returns the cursor offset top.<p>
     *
     * @return the cursor offset top
     */
    public int getCursorOffsetTop() {

        return m_cursorOffsetTop;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#getDragParent()
     */
    public I_CmsDragTarget getDragParent() {

        return m_dragParent;
    }

    /**
     * Returns the option bar of this element.<p>
     * 
     * @return the option bar widget
     */
    public CmsElementOptionBar getElementOptionBar() {

        return m_elementOptionBar;
    }

    /**
     * Returns the no edit reason.<p>
     *
     * @return the no edit reason
     */
    public String getNoEditReason() {

        return m_noEditReason;
    }

    /**
     * Returns the site-path.<p>
     *
     * @return the site-path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#isHandleEvent(com.google.gwt.dom.client.NativeEvent)
     */
    public boolean isHandleEvent(NativeEvent event) {

        EventTarget target = event.getEventTarget();
        if (com.google.gwt.dom.client.Element.is(target)) {
            return CmsDomUtil.hasClass(MOVE_HANDLE_CLASS, com.google.gwt.dom.client.Element.as(target));
        }
        return false;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDragCancel(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragCancel(I_CmsDragHandler<?, ?> handler) {

        // nothing to do here, everything is done by the drag handler

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDragEnter(org.opencms.gwt.client.draganddrop.I_CmsDragHandler, org.opencms.gwt.client.draganddrop.I_CmsDragTarget)
     */
    public void onDragEnter(I_CmsDragHandler<?, ?> handler, I_CmsDragTarget target) {

        // nothing to do here, everything is done by the drag handler
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDragLeave(org.opencms.gwt.client.draganddrop.I_CmsDragHandler, org.opencms.gwt.client.draganddrop.I_CmsDragTarget)
     */
    public void onDragLeave(I_CmsDragHandler<?, ?> handler, I_CmsDragTarget target) {

        // nothing to do here, everything is done by the drag handler

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDragStart(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragStart(I_CmsDragHandler<?, ?> handler) {

        // nothing to do here, everything is done by the drag handler
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDragStop(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragStop(I_CmsDragHandler<?, ?> handler) {

        // nothing to do here, everything is done by the drag handler
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDropTarget(org.opencms.gwt.client.draganddrop.I_CmsDragHandler, org.opencms.gwt.client.draganddrop.I_CmsDragTarget)
     */
    public void onDropTarget(I_CmsDragHandler<?, ?> handler, I_CmsDragTarget target) {

        // nothing to do here, everything is done by the drag handler
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElementExt#prepareDrag()
     */
    public void prepareDrag() {

        String width = CmsDomUtil.getCurrentStyle(getElement(), CmsDomUtil.Style.width);
        Style style = getElement().getStyle();
        style.setPosition(Position.ABSOLUTE);
        //style.setPosition(Position.FIXED);
        style.setMargin(0, Unit.PX);
        style.setProperty(CmsDomUtil.Style.width.name(), width);
        style.setZIndex(100);
        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());
        getElement().addClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().shadow());
        if (!CmsDomUtil.hasBackground(getElement())) {
            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElementBackground());
        }

        if (!CmsDomUtil.hasBorder(getElement())) {
            getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElementBorder());
        }
        getElementOptionBar().addStyleName(I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().cmsHovering());

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#setDragParent(org.opencms.gwt.client.draganddrop.I_CmsDragTarget)
     */
    public void setDragParent(I_CmsDragTarget target) {

        m_dragParent = target;
    }

    /**
     * Sets the elementOptionBar.<p>
     *
     * @param elementOptionBar the elementOptionBar to set
     */
    public void setElementOptionBar(CmsElementOptionBar elementOptionBar) {

        if ((m_elementOptionBar != null) && (getWidgetIndex(m_elementOptionBar) >= 0)) {
            m_elementOptionBar.removeFromParent();
        }
        add(elementOptionBar);
        m_elementOptionBar = elementOptionBar;
    }

    /**
     * Sets the drag handle of the element. If set, the element will only be draggable by the handle.<p>
     * 
     * @param handle the handle
     * @throws UnsupportedOperationException thrown if the handle to set is not a child element of the draggable
     */
    protected void setDragHandle(Widget handle) throws UnsupportedOperationException {

        if ((handle == null) || getElement().isOrHasChild(handle.getElement())) {
            m_dragHandle = handle;
        } else {
            throw new UnsupportedOperationException("The drag handle has to be a child of the draggable element.");
        }
    }
}
