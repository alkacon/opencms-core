/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsDnDListHandler.java,v $
 * Date   : $Date: 2010/06/08 09:01:21 $
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

import org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Drag and drop handler for generic lists.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsDnDListHandler extends A_CmsSortingDragHandler<CmsDnDListItem, CmsDnDList<CmsDnDListItem>> {

    /** The handler manager. */
    protected HandlerManager m_handlerManager;

    /** The provisional drag parent. */
    private CmsDnDList<? extends CmsDnDListItem> m_provisionalParent;

    /** The original ID of the draggable element. */
    private String m_srcId;

    /** The source list of the draggable element. */
    private CmsDnDList<CmsDnDListItem> m_srcList;

    /**
     * Constructor.<p>
     */
    public CmsDnDListHandler() {

        super();
        m_handlerManager = new HandlerManager(this);
        m_animationEnabled = true;
        // TODO: the abstract generic dnd handler should not clear the targets every time!!! see #clearDrag
        m_targets = new ArrayList<CmsDnDList<CmsDnDListItem>>();
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsDragHandler#addDragTarget(org.opencms.gwt.client.draganddrop.I_CmsDragTarget)
     */
    @Override
    public void addDragTarget(CmsDnDList<CmsDnDListItem> target) {

        m_targets.add(0, target);
    }

    /**
     * Adds a new list drop event handler.<p>
     * 
     * @param handler the handler to add
     * 
     * @return the handler registration
     */
    public HandlerRegistration addListDropHandler(I_CmsDnDListDropHandler handler) {

        return m_handlerManager.addHandler(CmsDnDListDropEvent.getType(), handler);
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragHandler#createDraggableListItemWidget(org.opencms.gwt.shared.CmsListInfoBean, java.lang.String)
     */
    public CmsListItemWidget createDraggableListItemWidget(CmsListInfoBean infoBean, String id) {

        throw new UnsupportedOperationException();
    }

    /**
     * Removes the given target.<p>
     * 
     * @param target the target to remove
     */
    public void removeDragTarget(CmsDnDList<CmsDnDListItem> target) {

        m_targets.remove(target);
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsDragHandler#clearDrag()
     */
    @Override
    protected void clearDrag() {

        if (m_currentTarget != null) {
            elementDropAction();
            m_currentTarget = null;
        } else {
            elementCancelAction();
        }
        restoreElementAfterDrag();
        Document.get().getBody().removeClassName(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.dragdropCss().dragStarted());
        m_dragElement = null;
        m_placeholder = null;
        // TODO: the abstract generic dnd handler should not clear the targets every time!!!, see ctor
        // m_targets = null;
        m_currentTarget = null;
    }

    /**
     * Creates a clone of element to be dragged around.<p>
     * 
     * @param element the element to clone
     * @param dragParent the drag parent
     * 
     * @return the generated clone
     */
    protected CmsDnDListItem createDragClone(Element element, CmsDnDList<? extends CmsDnDListItem> dragParent) {

        Element elementClone = DOM.createDiv();
        elementClone.setInnerHTML(element.getInnerHTML());
        elementClone.setClassName(element.getClassName());
        CmsDnDListItem dragElement = new CmsDnDListItem(elementClone);
        dragElement.setDragParent(dragParent);

        // remove all decorations
        List<com.google.gwt.dom.client.Element> elems = CmsDomUtil.getElementsByClass(
            I_CmsLayoutBundle.INSTANCE.floatDecoratedPanelCss().decorationBox(),
            CmsDomUtil.Tag.div,
            dragElement.getElement());
        for (com.google.gwt.dom.client.Element elem : elems) {
            elem.removeFromParent();
        }

        registerMouseHandler(dragElement);
        return dragElement;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#elementCancelAction()
     */
    @Override
    protected void elementCancelAction() {

        // nothing to do here
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#elementDropAction()
     */
    @Override
    protected void elementDropAction() {

        CmsDnDListDropEvent event = new CmsDnDListDropEvent(
            m_srcList,
            m_srcId,
            m_currentTarget,
            ((CmsDnDListItem)m_placeholder).getId());
        m_handlerManager.fireEvent(event);
        m_currentTarget.fireDropEvent(event);
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#elementEnterTargetAction()
     */
    @Override
    protected void elementEnterTargetAction() {

        // nothing to do here
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#elementLeaveTargetAction()
     */
    @Override
    protected void elementLeaveTargetAction() {

        // nothing to do
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#prepareElementForDrag()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void prepareElementForDrag() {

        m_currentTarget = (CmsDnDList<CmsDnDListItem>)m_dragElement.getDragParent();
        m_srcId = m_dragElement.getId();
        m_srcList = m_currentTarget;
        CmsDnDListItem clone = createDragClone(m_dragElement.getElement(), m_currentTarget);

        // we append the drag element to the body to prevent any kind of issues 
        // (ie when the parent is styled with overflow:hidden)
        // and with put it additionally inside a provisional parent for parent dependent styling 
        // and we position it absolutely on the original parent for the eventual animation when releasing 
        m_provisionalParent = new CmsDnDList<CmsDnDListItem>();
        m_provisionalParent.setWidth(m_dragElement.getElement().getOffsetWidth() + "px");
        m_provisionalParent.getElement().getStyle().setPosition(Position.ABSOLUTE);
        Element listEl = m_currentTarget.getElement();
        m_provisionalParent.getElement().getStyle().setTop(listEl.getAbsoluteTop(), Unit.PX);
        m_provisionalParent.getElement().getStyle().setLeft(listEl.getAbsoluteLeft(), Unit.PX);
        m_provisionalParent.getElement().getStyle().setZIndex(99999);
        RootPanel.get().add(m_provisionalParent);

        m_placeholder = m_dragElement;
        m_placeholder.addStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());
        ((CmsDnDListItem)m_placeholder).onDrag();

        m_dragElement = clone;

        // important: capture all mouse events and dispatch them to this element until released
        DOM.setCapture(m_dragElement.getElement());
        m_provisionalParent.add(m_dragElement);
        m_dragElement.prepareDrag();
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#restoreElementAfterDrag()
     */
    @Override
    protected void restoreElementAfterDrag() {

        m_placeholder.removeStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());
        m_provisionalParent.removeFromParent();
        m_provisionalParent = null;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#targetSortChangeAction()
     */
    @Override
    protected void targetSortChangeAction() {

        // nothing to do
    }
}
