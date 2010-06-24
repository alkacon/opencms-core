/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/tree/Attic/CmsTree.java,v $
 * Date   : $Date: 2010/06/24 09:05:26 $
 * Version: $Revision: 1.6 $
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

import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.dnd.CmsDropEvent;
import org.opencms.gwt.client.ui.dnd.CmsDropPosition;
import org.opencms.gwt.client.ui.dnd.I_CmsDraggable;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.logical.shared.HasOpenHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasAnimation;

/**
 * A tree of list items.<p>
 * 
 * @param <I> the specific tree item implementation 
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public class CmsTree<I extends CmsTreeItem> extends CmsList<I> implements HasOpenHandlers<I>, HasAnimation {

    /** The event handlers for the tree. */
    protected HandlerManager m_handlers;

    /** Flag to indicate is animations are enabled or not. */
    private boolean m_animate;

    /** The drop target place holder, when hovering a tree item. */
    private CmsTreeItem m_treePlaceholder;

    /**
     * Constructor.<p>
     */
    public CmsTree() {

        m_animate = false;
        m_handlers = new HandlerManager(this);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasOpenHandlers#addOpenHandler(com.google.gwt.event.logical.shared.OpenHandler)
     */
    public HandlerRegistration addOpenHandler(final OpenHandler<I> handler) {

        m_handlers.addHandler(OpenEvent.getType(), handler);
        return new HandlerRegistration() {

            /**
             * @see com.google.gwt.event.shared.HandlerRegistration#removeHandler()
             */
            public void removeHandler() {

                m_handlers.removeHandler(OpenEvent.getType(), handler);
            }
        };
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsList#check(int, int)
     */
    @Override
    public boolean check(int x, int y) {

        Element element = getElement();
        // check if the mouse pointer is within the width of the target 
        int left = CmsDomUtil.getRelativeX(x, element);
        int offsetWidth = element.getOffsetWidth();
        if ((left <= 0) || (left >= offsetWidth)) {
            return false;
        }

        // check if the mouse pointer is within the height of the target 
        int top = CmsDomUtil.getRelativeY(y, element);
        int offsetHeight = element.getOffsetHeight();
        if ((top <= 0) || (top >= offsetHeight)) {
            return false;
        }
        return true;
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#fireEvent(com.google.gwt.event.shared.GwtEvent)
     */
    @Override
    public void fireEvent(GwtEvent<?> event) {

        m_handlers.fireEvent(event);
    }

    /**
     * Fires an open event for a tree item.<p>
     *
     * @param item the tree item for which the open event should be fired
     */
    public void fireOpen(I item) {

        OpenEvent.fire(this, item);
    }

    /**
     * Returns the tree entry with the given path.<p>
     * 
     * @param path the path to look for
     * 
     * @return the tree entry with the given path, or <code>null</code> if not found
     */
    @SuppressWarnings("unchecked")
    public I getItemByPath(String path) {

        String[] names = CmsStringUtil.splitAsArray(path, "/");
        I result = null;
        for (String name : names) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
                // in case of leading slash
                continue;
            }
            if (result != null) {
                result = (I)result.getChild(name);
            } else {
                // match the root node
                result = getItem(name);
            }
            if (result == null) {
                // not found
                break;
            }
        }
        return result;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasAnimation#isAnimationEnabled()
     */
    public boolean isAnimationEnabled() {

        return m_animate;
    }

    /**
     * Here the meaning is enabling dropping on the root level.<p>
     * 
     * Use {@link CmsTreeItem#isDropEnabled()} for dropping on tree items.<p>
     * 
     * @see org.opencms.gwt.client.ui.CmsList#isDropEnabled()
     */
    @Override
    public boolean isDropEnabled() {

        return super.isDropEnabled();
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsList#onDrop()
     */
    @Override
    public void onDrop() {

        if (m_treePlaceholder == null) {
            return;
        }
        m_treePlaceholder.onDragOverOut();
        if (!m_treePlaceholder.isOpen()) {
            m_treePlaceholder.setOpen(true);
        }
        m_treePlaceholder = null;
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsList#removePlaceholder()
     */
    @Override
    public void removePlaceholder() {

        super.removePlaceholder();
        if (m_treePlaceholder != null) {
            m_treePlaceholder.onDragOverOut();
            m_treePlaceholder = null;
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.HasAnimation#setAnimationEnabled(boolean)
     */
    public void setAnimationEnabled(boolean enable) {

        m_animate = enable;
    }

    /**
     * Here the meaning is enabling dropping on the root level.<p>
     * 
     * Use {@link CmsTreeItem#setDropEnabled(boolean)} for dropping on tree items.<p>
     * 
     * @see org.opencms.gwt.client.ui.CmsList#setDropEnabled(boolean)
     */
    @Override
    public void setDropEnabled(boolean enabled) {

        super.setDropEnabled(enabled);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsList#setPlaceholder(int, int, CmsDropEvent)
     */
    @Override
    public CmsDropPosition setPlaceholder(int x, int y, CmsDropEvent event) {

        for (int index = 0; index < getWidgetCount(); index++) {
            CmsTreeItem item = getItem(index);
            Element itemElement = item.getElement();

            String positioning = itemElement.getStyle().getPosition();
            if ((positioning.equals(Position.ABSOLUTE.getCssName()) || positioning.equals(Position.FIXED.getCssName()))
                || !item.isVisible()) {
                // only take visible and not 'position:absolute' elements into account, also ignore the place-holder
                continue;
            }

            // check if the mouse pointer is within the width of the element 
            int left = CmsDomUtil.getRelativeX(x, itemElement);
            if ((left <= 0) || (left >= itemElement.getClientWidth())) {
                continue;
            }

            // check if the mouse pointer is within the height of the element 
            int top = CmsDomUtil.getRelativeY(y, itemElement);
            int height = itemElement.getClientHeight();
            if ((top <= 0) || (top >= height)) {
                continue;
            }

            CmsDropPosition position = item.setPlaceholder(x, y, event);
            if (position == null) {
                // no match
                return position;
            }
            I_CmsDraggable draggable = event.getDraggable();
            if (draggable instanceof CmsListItem) {
                String id = ((CmsListItem)draggable).getId();
                position.setName(id);
            }
            CmsDebugLog.getInstance().printLine(position.toString());
            return position;
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsList#registerItem(org.opencms.gwt.client.ui.I_CmsListItem)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void registerItem(I item) {

        super.registerItem(item);
        item.setTree((CmsTree<CmsTreeItem>)this);
    }

    /**
     * Sets the place holder.<p>
     * 
     * @param e the place holder to set
     */
    protected void setPlaceholder(CmsTreeItem e) {

        removePlaceholder();
        m_treePlaceholder = e;
        m_treePlaceholder.onDragOverIn();
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsList#setPlaceholder(com.google.gwt.dom.client.Element)
     */
    @Override
    protected void setPlaceholder(Element placeholder) {

        super.setPlaceholder(placeholder);
    }
}
