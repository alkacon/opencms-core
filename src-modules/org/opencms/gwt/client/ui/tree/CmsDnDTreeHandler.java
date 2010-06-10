/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/tree/Attic/CmsDnDTreeHandler.java,v $
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

package org.opencms.gwt.client.ui.tree;

import org.opencms.gwt.client.ui.CmsDnDList;
import org.opencms.gwt.client.ui.CmsDnDListHandler;
import org.opencms.gwt.client.ui.CmsDnDListItem;

import java.util.Iterator;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Drag and drop handler for generic lists.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public class CmsDnDTreeHandler extends CmsDnDListHandler {

    /** The item the user is currently hovering. */
    private CmsDnDTreeItem m_overItem;

    /**
     * Constructor.<p>
     */
    public CmsDnDTreeHandler() {

        super();
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsDragHandler#animateClear()
     */
    @Override
    protected void animateClear() {

        Widget placeHolder = m_placeholder;
        if (m_overItem != null) {
            // replace the place holder to fix the animation
            m_placeholder = m_overItem;
        }
        super.animateClear();
        // restore the place holder for #elementDropAction()
        m_placeholder = placeHolder;
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsDnDListHandler#elementDropAction()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void elementDropAction() {

        if (m_overItem == null) {
            super.elementDropAction();
            return;
        }
        CmsDnDList<? extends CmsDnDListItem> list = m_overItem.m_children;
        list.insert(m_placeholder, list.getWidgetCount());
        m_placeholder.setVisible(true);
        fireEvent((CmsDnDList<CmsDnDListItem>)list);
        m_overItem.setOpen(true);
        resetItemOver();
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsDnDListHandler#elementLeaveTargetAction()
     */
    @Override
    protected void elementLeaveTargetAction() {

        super.elementLeaveTargetAction();
        resetItemOver();
    }

    /**
     * Reset the hovering item.<p>
     */
    protected void resetItemOver() {

        if (m_overItem == null) {
            return;
        }
        m_overItem.onDragOverOut();
        m_overItem = null;
        m_placeholder.setVisible(true);
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#sortTarget()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void sortTarget() {

        Iterator<Widget> it = m_currentTarget.iterator();
        while (it.hasNext()) {
            Widget child = it.next();
            Element element = child.getElement();

            String positioning = element.getStyle().getPosition();
            if ((positioning.equals(Position.ABSOLUTE.getCssName()) || positioning.equals(Position.FIXED.getCssName()))
                || !child.isVisible()
                || (m_placeholder == child)) {
                // only take visible and not 'position:absolute' elements into account, also ignore the place-holder
                continue;
            }

            // check if the mouse pointer is within the width of the element 
            int left = getRelativeX(element);
            if ((left <= 0) || (left >= element.getOffsetWidth())) {
                continue;
            }

            // check if the mouse pointer is within the height of the element 
            int top = getRelativeY(element);
            int height = element.getOffsetHeight();
            if ((top <= 0) || (top >= height)) {
                continue;
            }

            int index = m_currentTarget.getWidgetIndex(child);

            boolean isTree = child instanceof CmsDnDTreeItem;
            if (!isTree) {
                // simple list item case
                if (top < height / 2) {
                    // the mouse pointer is within the top section of the element
                    if (m_currentTarget.getWidgetIndex(m_placeholder) != index) {
                        // only act if the place-holder index has to be changed
                        m_currentTarget.insert(m_placeholder, index);
                        targetSortChangeAction();
                    }
                } else {
                    // the mouse pointer is within the bottom section of the element
                    if (m_currentTarget.getWidgetIndex(m_placeholder) != index + 1) {
                        // only act if the place-holder index has to be changed
                        m_currentTarget.insert(m_placeholder, index + 1);
                        targetSortChangeAction();
                    }
                }
                return;
            }
            // complex tree item case
            CmsDnDTreeItem item = (CmsDnDTreeItem)child;
            height = item.getListItemWidget().getElement().getOffsetHeight();
            if ((top <= 0) || (top >= height)) {
                continue;
            }
            if (top < height / 3) {
                // the mouse pointer is within the top section of the element
                if (m_currentTarget.getWidgetIndex(m_placeholder) != index) {
                    // only act if the place-holder index has to be changed
                    m_currentTarget.insert(m_placeholder, index);
                    targetSortChangeAction();
                }
            } else if (top > 2 * height / 3) {
                // the mouse pointer is within the bottom section of the element
                if ((item.getChildCount() == 0) || !item.isOpen()) {
                    // if the item does not have children or is closed use the same target
                    if (m_currentTarget.getWidgetIndex(m_placeholder) != index + 1) {
                        // only act if the place-holder index has to be changed
                        m_currentTarget.insert(m_placeholder, index + 1);
                        targetSortChangeAction();
                    }
                } else {
                    // but if it has children and they are visible use the children list
                    if (m_currentTarget != null) {
                        elementLeaveTargetAction();
                    }
                    CmsDnDList<? extends CmsDnDListItem> list = item.m_children;
                    m_currentTarget = (CmsDnDList<CmsDnDListItem>)list;
                    elementEnterTargetAction();
                    if (m_currentTarget.getWidgetIndex(m_placeholder) != 0) {
                        // only act if the place-holder index has to be changed
                        m_currentTarget.insert(m_placeholder, 0);
                        targetSortChangeAction();
                    }
                }
            } else {
                // the mouse pointer is within the middle section of the element
                if (m_overItem == child) {
                    // is the same
                    return;
                }
                if (!item.onDragOverIn()) {
                    // we are not allowed to drag over the item
                    return;
                }
                // we will be dropping on an item
                targetSortChangeAction();
                m_placeholder.setVisible(false);
                m_overItem = item;
            }
            return;
        }
        resetItemOver();
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsDnDListHandler#stopDragging(com.google.gwt.event.dom.client.MouseUpEvent)
     */
    @Override
    protected void stopDragging(MouseUpEvent event) {

        if ((event == null) && (m_overItem != null)) {
            // if canceled, reset the hovering item 
            resetItemOver();
        }
        super.stopDragging(event);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsDnDListHandler#targetSortChangeAction()
     */
    @Override
    protected void targetSortChangeAction() {

        super.targetSortChangeAction();
        resetItemOver();
    }
}
