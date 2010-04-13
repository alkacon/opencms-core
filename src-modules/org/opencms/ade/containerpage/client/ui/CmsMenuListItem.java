/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsMenuListItem.java,v $
 * Date   : $Date: 2010/04/13 14:29:43 $
 * Version: $Revision: 1.1 $
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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.draganddrop.I_CmsDragContainerElement;
import org.opencms.ade.containerpage.client.draganddrop.I_CmsDragTargetContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.draganddrop.I_CmsDragTarget;
import org.opencms.gwt.client.draganddrop.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
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

/**
 * A tool-bar menu list item widget.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsMenuListItem extends CmsListItemWidget implements I_CmsDragContainerElement {

    private I_CmsDragTargetContainer m_dragParent;

    private String m_clientId;

    /**
     * Constructor.<p>
     * 
     * @param elementData the element data
     * @param dragParent the drag parent
     */
    public CmsMenuListItem(CmsContainerElement elementData, I_CmsDragTargetContainer dragParent) {

        super(new CmsListInfoBean(elementData.getTitle(), elementData.getFile(), null));
        m_dragParent = dragParent;
        m_clientId = elementData.getClientId();

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#getDragParent()
     */
    public I_CmsDragTarget getDragParent() {

        // TODO: Auto-generated method stub
        return m_dragParent;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#isHandleEvent(com.google.gwt.dom.client.NativeEvent)
     */
    public boolean isHandleEvent(NativeEvent event) {

        // TODO: generate handle and implement this method properly
        return false;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDragCancel(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragCancel(I_CmsDragHandler<?, ?> handler) {

        // TODO: Auto-generated method stub

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDragEnter(org.opencms.gwt.client.draganddrop.I_CmsDragHandler, org.opencms.gwt.client.draganddrop.I_CmsDragTarget)
     */
    public void onDragEnter(I_CmsDragHandler<?, ?> handler, I_CmsDragTarget target) {

        // TODO: Auto-generated method stub

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDragLeave(org.opencms.gwt.client.draganddrop.I_CmsDragHandler, org.opencms.gwt.client.draganddrop.I_CmsDragTarget)
     */
    public void onDragLeave(I_CmsDragHandler<?, ?> handler, I_CmsDragTarget target) {

        // TODO: Auto-generated method stub

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDragStart(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragStart(I_CmsDragHandler<?, ?> handler) {

        // TODO: Auto-generated method stub

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDragStop(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragStop(I_CmsDragHandler<?, ?> handler) {

        // TODO: Auto-generated method stub

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDropTarget(org.opencms.gwt.client.draganddrop.I_CmsDragHandler, org.opencms.gwt.client.draganddrop.I_CmsDragTarget)
     */
    public void onDropTarget(I_CmsDragHandler<?, ?> handler, I_CmsDragTarget target) {

        // TODO: Auto-generated method stub

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#setDragParent(org.opencms.gwt.client.draganddrop.I_CmsDragTarget)
     */
    public void setDragParent(I_CmsDragTarget target) {

        // TODO: Auto-generated method stub

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
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragContainerElement#clearDrag()
     */
    public void clearDrag() {

        Style style = getElement().getStyle();
        style.clearPosition();
        style.clearWidth();
        style.clearTop();
        style.clearLeft();
        style.clearZIndex();
        style.clearMargin();
        getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());

    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragContainerElement#getClientId()
     */
    public String getClientId() {

        return m_clientId;
    }

    /**
     * @see org.opencms.ade.containerpage.client.draganddrop.I_CmsDragContainerElement#prepareDrag()
     */
    public void prepareDrag() {

        String width = CmsDomUtil.getCurrentStyle(getElement(), CmsDomUtil.Style.width);
        Style style = getElement().getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setMargin(0, Unit.PX);
        style.setProperty(CmsDomUtil.Style.width.name(), width);
        style.setZIndex(100);
        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());

    }

}