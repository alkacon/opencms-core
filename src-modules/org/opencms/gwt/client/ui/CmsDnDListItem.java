/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsDnDListItem.java,v $
 * Date   : $Date: 2010/06/10 12:56:38 $
 * Version: $Revision: 1.4 $
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

import org.opencms.gwt.client.draganddrop.I_CmsDragElement;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.dom.client.Element;
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

/**
 * List item for DnD.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.4 $
 *  
 * @since 8.0.0 
 */
public class CmsDnDListItem extends CmsListItem implements I_CmsDragElement<CmsDnDList<CmsDnDListItem>> {

    /** The placeholder id, to be sure that there won't be any conflict. */
    public static final String DRAGGED_PLACEHOLDER_ID = "_cms_dragged_placeholder_";

    /** Flag to indicate if drag'n drop is enabled. */
    protected boolean m_dndEnabled;

    /** The event handler registrations. */
    private List<HandlerRegistration> m_handlerRegistrations;

    /** The move handle element. */
    private CmsPushButton m_moveHandle;

    /** The original id while dragging. */
    private String m_originalId;

    /**
     * Constructor.<p>
     */
    public CmsDnDListItem() {

        super();
        m_handlerRegistrations = new ArrayList<HandlerRegistration>();
    }

    /**
     * Wrapping constructor.<p>
     * 
     * @param element the element to wrap
     */
    public CmsDnDListItem(Element element) {

        super(element);
        m_handlerRegistrations = new ArrayList<HandlerRegistration>();
    }

    /**
     * @see com.google.gwt.event.dom.client.HasContextMenuHandlers#addContextMenuHandler(com.google.gwt.event.dom.client.ContextMenuHandler)
     */
    public HandlerRegistration addContextMenuHandler(ContextMenuHandler handler) {

        HandlerRegistration reg = addDomHandler(handler, ContextMenuEvent.getType());
        m_handlerRegistrations.add(reg);
        return reg;
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseDownHandlers#addMouseDownHandler(com.google.gwt.event.dom.client.MouseDownHandler)
     */
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {

        HandlerRegistration reg = addDomHandler(handler, MouseDownEvent.getType());
        m_handlerRegistrations.add(reg);
        return reg;
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseMoveHandlers#addMouseMoveHandler(com.google.gwt.event.dom.client.MouseMoveHandler)
     */
    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {

        HandlerRegistration reg = addDomHandler(handler, MouseMoveEvent.getType());
        m_handlerRegistrations.add(reg);
        return reg;
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOutHandlers#addMouseOutHandler(com.google.gwt.event.dom.client.MouseOutHandler)
     */
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {

        HandlerRegistration req = addDomHandler(handler, MouseOutEvent.getType());
        m_handlerRegistrations.add(req);
        return req;

    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOverHandlers#addMouseOverHandler(com.google.gwt.event.dom.client.MouseOverHandler)
     */
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {

        HandlerRegistration req = addDomHandler(handler, MouseOverEvent.getType());
        m_handlerRegistrations.add(req);
        return req;
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseUpHandlers#addMouseUpHandler(com.google.gwt.event.dom.client.MouseUpHandler)
     */
    public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {

        HandlerRegistration reg = addDomHandler(handler, MouseUpEvent.getType());
        m_handlerRegistrations.add(reg);
        return reg;
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseWheelHandlers#addMouseWheelHandler(com.google.gwt.event.dom.client.MouseWheelHandler)
     */
    public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {

        HandlerRegistration reg = addDomHandler(handler, MouseWheelEvent.getType());
        m_handlerRegistrations.add(reg);
        return reg;
    }

    /**
     * Disables drag'n drop.<p>
     * 
     * Removes all for drag and drop registered mouse event handlers.<p>
     */
    public void disableDnD() {

        m_dndEnabled = false;
        removeDndMouseHandlers();
        if (m_moveHandle != null) {
            getListItemWidget().removeButton(m_moveHandle);
        }
    }

    /**
     * Enables drag'n drop.<p>
     * 
     * @param handler the drag'n drop handler
     */
    public void enableDnD(CmsDnDListHandler handler) {

        m_dndEnabled = true;
        handler.registerMouseHandler(this);
        // add move handle
        CmsPushButton moveHandle = new CmsPushButton();
        moveHandle.setImageClass(I_CmsImageBundle.INSTANCE.style().moveIcon());
        moveHandle.setShowBorder(false);
        getListItemWidget().addButton(moveHandle);
        m_moveHandle = moveHandle;
    }

    /**
     * Returns the original id while dragging.<p>
     * 
     * @return the original id
     */
    public String getOriginalId() {

        return m_originalId == null ? getId() : m_originalId;
    }

    /**
     * Checks if drag'n drop is enabled.<p>
     *
     * @return <code>true</code> if drag'n drop is enabled
     */
    public boolean isDndEnabled() {

        return m_dndEnabled;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragElement#isHandleEvent(com.google.gwt.dom.client.NativeEvent)
     */
    public boolean isHandleEvent(NativeEvent event) {

        if (m_dndEnabled && (m_moveHandle != null)) {
            EventTarget target = event.getEventTarget();
            if (Element.is(target)) {
                return m_moveHandle.getElement().isOrHasChild(Element.as(target));
            }
        }
        return false;
    }

    /**
     * Will be executed when starting dragging, so that this item will be used as place holder.<p>
     */
    public void onDragStart() {

        addStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());
        String originalId = getId();
        setId(DRAGGED_PLACEHOLDER_ID);
        m_originalId = originalId;
    }

    /**
     * Will be executed when stopping dragging, so that this item will not be anymore used as place holder.<p>
     */
    public void onDragStop() {

        removeStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());
        if (m_originalId != null) {
            setId(m_originalId);
        }
    }

    /**
     * Prepares the element for dragging, will be executed on the drag helper.<p>
     */
    public void prepareDrag() {

        Element element = getElement();
        String width = CmsDomUtil.getCurrentStyle(element, CmsDomUtil.Style.width);
        Style style = element.getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setMargin(0, Unit.PX);
        style.setProperty(CmsDomUtil.Style.width.name(), width);
        style.setZIndex(100);

        element.addClassName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().dragging());
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsListItem#setId(java.lang.String)
     */
    @Override
    public void setId(String id) {

        super.setId(id);
        m_originalId = null;
    }

    /**
     * Removes all for drag and drop registered mouse event handlers.<p>
     */
    protected void removeDndMouseHandlers() {

        Iterator<HandlerRegistration> it = m_handlerRegistrations.iterator();
        while (it.hasNext()) {
            it.next().removeHandler();
        }
        m_handlerRegistrations.clear();
    }
}
