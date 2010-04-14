/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsDraggableListItemWidget.java,v $
 * Date   : $Date: 2010/04/14 14:32:29 $
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.draganddrop.I_CmsDragElementExt;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.draganddrop.I_CmsDragTarget;
import org.opencms.gwt.client.draganddrop.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.dom.client.EventTarget;
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
import com.google.gwt.user.client.ui.Widget;

/**
 * A draggable list item widget.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsDraggableListItemWidget extends CmsListItemWidget implements I_CmsDragElementExt {

    private String m_clientId;

    private boolean m_dragEnabled;

    private I_CmsDragTarget m_dragParent;

    private Widget m_moveHandle;

    /**
     * Constructor.<p>
     * 
     * @param infoBean bean holding the item information
     * @param draggable <code>true</code> to enable dragging and show drag handle
     */
    public CmsDraggableListItemWidget(CmsListInfoBean infoBean, boolean draggable) {

        super(infoBean);
        m_dragEnabled = draggable;
        if (draggable) {
            m_moveHandle = new CmsImageButton(I_CmsImageBundle.INSTANCE.style().moveIcon(), false);
            addButton(m_moveHandle);
        }

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
        getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElementExt#getClientId()
     */
    public String getClientId() {

        return m_clientId;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#getDragParent()
     */
    public I_CmsDragTarget getDragParent() {

        return m_dragParent;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#isHandleEvent(com.google.gwt.dom.client.NativeEvent)
     */
    public boolean isHandleEvent(NativeEvent event) {

        if (m_dragEnabled && (m_moveHandle != null)) {
            EventTarget target = event.getEventTarget();
            if (com.google.gwt.dom.client.Element.is(target)) {
                return m_moveHandle.getElement().isOrHasChild(com.google.gwt.dom.client.Element.as(target));
            }
        }
        return false;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDragCancel(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragCancel(I_CmsDragHandler<?, ?> handler) {

        // override this if necessary

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDragEnter(org.opencms.gwt.client.draganddrop.I_CmsDragHandler, org.opencms.gwt.client.draganddrop.I_CmsDragTarget)
     */
    public void onDragEnter(I_CmsDragHandler<?, ?> handler, I_CmsDragTarget target) {

        // override this if necessary

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDragLeave(org.opencms.gwt.client.draganddrop.I_CmsDragHandler, org.opencms.gwt.client.draganddrop.I_CmsDragTarget)
     */
    public void onDragLeave(I_CmsDragHandler<?, ?> handler, I_CmsDragTarget target) {

        // override this if necessary

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDragStart(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragStart(I_CmsDragHandler<?, ?> handler) {

        // override this if necessary

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDragStop(org.opencms.gwt.client.draganddrop.I_CmsDragHandler)
     */
    public void onDragStop(I_CmsDragHandler<?, ?> handler) {

        // override this if necessary

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#onDropTarget(org.opencms.gwt.client.draganddrop.I_CmsDragHandler, org.opencms.gwt.client.draganddrop.I_CmsDragTarget)
     */
    public void onDropTarget(I_CmsDragHandler<?, ?> handler, I_CmsDragTarget target) {

        // override this if necessary

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElementExt#prepareDrag()
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

    /**
     * Sets the client id.<p>
     *
     * @param clientId the client id to set
     */
    public void setClientId(String clientId) {

        m_clientId = clientId;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#setDragParent(org.opencms.gwt.client.draganddrop.I_CmsDragTarget)
     */
    public void setDragParent(I_CmsDragTarget target) {

        m_dragParent = target;

    }

}
