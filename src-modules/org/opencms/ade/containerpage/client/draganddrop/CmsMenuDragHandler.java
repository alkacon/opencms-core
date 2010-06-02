/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/draganddrop/Attic/CmsMenuDragHandler.java,v $
 * Date   : $Date: 2010/06/02 06:56:00 $
 * Version: $Revision: 1.12 $
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

import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.ArrayList;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Drag and drop handler for the favorite list menu.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.12 $
 * 
 * @since 8.0.0
 */
public class CmsMenuDragHandler
extends A_CmsSortingDragHandler<I_CmsDragContainerElement<I_CmsDragTargetContainer>, I_CmsDragTargetContainer> {

    /** The provisional drag parent. */
    private CmsDragTargetMenu m_provisionalParent;

    /**
     * Constructor.<p>
     */
    public CmsMenuDragHandler() {

        super();
        m_animationEnabled = true;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragHandler#createDraggableListItemWidget(org.opencms.gwt.shared.CmsListInfoBean, java.lang.String)
     */
    public CmsListItemWidget createDraggableListItemWidget(CmsListInfoBean infoBean, String id) {

        throw new UnsupportedOperationException();
    }

    /**
     * Creates a clone of element to be dragged around.<p>
     * 
     * @param element the element to clone
     * @param dragParent the drag parent
     * @param clientId the client id
     * 
     * @return the generated clone
     */
    protected CmsDragContainerElement createDragClone(
        com.google.gwt.user.client.Element element,
        I_CmsDragTargetContainer dragParent,
        String clientId) {

        com.google.gwt.user.client.Element elementClone = DOM.createDiv();
        elementClone.setInnerHTML(element.getInnerHTML());
        elementClone.setClassName(element.getClassName());
        CmsDragContainerElement dragElement = new CmsDragContainerElement(
            elementClone,
            dragParent,
            clientId,
            null,
            null,
            false);
        registerMouseHandler(dragElement);
        return dragElement;
    }

    /**
     * Creates a place-holder for the draggable element.<p>
     * 
     * @param element the element
     * 
     * @return the place-holder widget
     */
    protected Widget createPlaceholder(CmsDragMenuElement element) {

        Widget result = new HTML(element.getElement().getInnerHTML());
        result.addStyleName(element.getElement().getClassName()
            + " "
            + I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());

        Element overlay = DOM.createDiv();
        overlay.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().placeholderOverlay());
        result.getElement().appendChild(overlay);

        return result;
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

        // nothing to do here
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

        // nothing to do here
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#prepareElementForDrag()
     */
    @Override
    protected void prepareElementForDrag() {

        // at the very beginning the current target is the list where the drag element is dragged from 
        m_currentTarget = m_dragElement.getDragParent();

        // create clone
        CmsDragContainerElement clone = createDragClone(
            m_dragElement.getElement(),
            m_currentTarget,
            m_dragElement.getClientId());

        // we append the drag element to the body to prevent any kind of issues 
        // (ie when the parent is styled with overflow:hidden)
        // and with put it additionally inside a provisional parent for parent dependent styling 
        // and we position it absolutely on the original parent for the eventual animation when releasing 
        m_provisionalParent = new CmsDragTargetMenu();
        m_provisionalParent.setWidth(m_dragElement.getElement().getOffsetWidth() + "px");
        m_provisionalParent.getElement().getStyle().setPosition(Position.ABSOLUTE);
        Element listEl = m_currentTarget.getElement();
        m_provisionalParent.getElement().getStyle().setTop(listEl.getAbsoluteTop(), Unit.PX);
        m_provisionalParent.getElement().getStyle().setLeft(listEl.getAbsoluteLeft(), Unit.PX);
        m_provisionalParent.getElement().getStyle().setZIndex(99999);
        RootPanel.get().add(m_provisionalParent);
        m_targets = new ArrayList<I_CmsDragTargetContainer>();
        m_targets.add(m_currentTarget);
        // this is because our drag element is the widget and not the list item
        m_placeholder = ((CmsDragMenuElement)m_dragElement).getParentListItem();
        m_placeholder.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());
        // switch to the clone
        m_dragElement = clone;
        // important: capture all mouse events and dispatch them to this element until released
        DOM.setCapture(m_dragElement.getElement());
        m_provisionalParent.add((Widget)m_dragElement);
        m_dragElement.prepareDrag();
        // TODO: resolve ie7/8 issue with loosing the element while dragging
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#restoreElementAfterDrag()
     */
    @Override
    protected void restoreElementAfterDrag() {

        // m_dragElement.getDragParent().insert(m_dragElement, m_dragElement.getDragParent().getWidgetIndex(m_placeholder));
        m_placeholder.removeStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());
        m_provisionalParent.removeFromParent();
        m_provisionalParent = null;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#targetSortChangeAction()
     */
    @Override
    protected void targetSortChangeAction() {

        // nothing to do here
    }
}
