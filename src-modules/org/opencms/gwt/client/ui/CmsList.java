/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsList.java,v $
 * Date   : $Date: 2010/09/08 08:34:01 $
 * Version: $Revision: 1.19 $
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
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsListTreeCss;
import org.opencms.gwt.client.ui.dnd.CmsDnDManager;
import org.opencms.gwt.client.ui.dnd.CmsDropEvent;
import org.opencms.gwt.client.ui.dnd.CmsDropPosition;
import org.opencms.gwt.client.ui.dnd.I_CmsDraggable;
import org.opencms.gwt.client.ui.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A very basic list implementation to hold {@link CmsListItemWidget}.<p>
 * 
 * @param <I> the specific list item implementation 
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.19 $
 * 
 * @since 8.0.0
 */
public class CmsList<I extends I_CmsListItem> extends ComplexPanel implements I_CmsTruncable, I_CmsDropTarget {

    /** The css bundle used for this widget. */
    private static final I_CmsListTreeCss CSS = I_CmsLayoutBundle.INSTANCE.listTreeCss();

    /** Flag to indicate if drag'n drop is enabled. */
    protected boolean m_dndEnabled;

    /** The drag'n drop handler. */
    protected CmsDnDManager m_dndManager;

    /** The current place holder. */
    protected Element m_placeholder;

    /** The child width in px for truncation. */
    private int m_childWidth;

    /** Flag to indicate if drag'n drop on the root node is allowed. */
    private boolean m_dropEnabled;

    /** The map of items. */
    private Map<String, I> m_items;

    /** The text metrics prefix. */
    private String m_tmPrefix;

    /**
     * Constructor.<p>
     */
    public CmsList() {

        setElement(DOM.createElement(CmsDomUtil.Tag.ul.name()));
        setStyleName(CSS.list());
        m_items = new HashMap<String, I>();
    }

    /**
     * @see com.google.gwt.user.client.ui.Panel#add(com.google.gwt.user.client.ui.Widget)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void add(Widget widget) {

        assert widget instanceof I_CmsListItem;
        add(widget, getElement());
        registerItem((I)widget);
    }

    /**
     * Adds an item to the list.<p>
     * 
     * @param item the item to add
     * 
     * @see #add(Widget)
     */
    public void addItem(I item) {

        add((Widget)item);
    }

    /**
     * @see org.opencms.gwt.client.ui.dnd.I_CmsDropTarget#check(int, int)
     */
    public boolean check(int x, int y) {

        if (!isDropEnabled()) {
            return false;
        }
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
     * Clears the list.<p>
     */
    public void clearList() {

        clear();
        m_items.clear();
    }

    /**
     * Returns the drag'n drop handler.<p>
     *
     * @return the handler
     */
    public CmsDnDManager getDnDManager() {

        return m_dndManager;
    }

    /**
     * Returns the list item at the given position.<p>
     * 
     * @param index the position
     * 
     * @return the list item
     * 
     * @see #getWidget(int)
     */
    @SuppressWarnings("unchecked")
    public I getItem(int index) {

        return (I)getWidget(index);
    }

    /**
     * Returns the list item with the given id.<p>
     * 
     * @param itemId the id of the item to retrieve
     * 
     * @return the list item
     * 
     * @see #getWidget(int)
     */
    public I getItem(String itemId) {

        return m_items.get(itemId);
    }

    /**
     * Returns the given item position.<p>
     * 
     * @param item the item to get the position for
     * 
     * @return the item position
     */
    public int getItemPosition(I item) {

        return getWidgetIndex((Widget)item);
    }

    /**
     * Inserts the given widget at the given position.<p>
     * 
     * @param widget the widget to insert
     * @param position the position
     */
    @SuppressWarnings("unchecked")
    public void insert(Widget widget, int position) {

        assert widget instanceof I_CmsListItem;
        insert(widget, getElement(), position, true);
        registerItem((I)widget);
    }

    /**
     * Inserts the given item at the given position.<p>
     * 
     * @param item the item to insert
     * @param position the position
     */
    public void insertItem(I item, int position) {

        insert((Widget)item, position);
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
     * Checks if dropping is enabled.<p>
     *
     * @return <code>true</code> if dropping is enabled
     */
    public boolean isDropEnabled() {

        return m_dropEnabled;
    }

    /**
     * @see org.opencms.gwt.client.ui.dnd.I_CmsDropTarget#onDrop()
     */
    public void onDrop() {

        // nothing to do here
    }

    /**
     * @see com.google.gwt.user.client.ui.ComplexPanel#remove(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public boolean remove(Widget w) {

        boolean result = super.remove(w);
        if (result && (w instanceof I_CmsListItem)) {
            String id = ((I_CmsListItem)w).getId();
            if (id != null) {
                m_items.remove(id);
            }
        }
        return result;
    }

    /**
     * Removes an item from the list.<p>
     * 
     * @param item the item to remove
     * 
     * @return the removed item
     * 
     * @see #remove(Widget)
     */
    public I removeItem(I item) {

        remove((Widget)item);
        return item;
    }

    /**
     * Removes an item from the list.<p>
     * 
     * @param itemId the id of the item to remove
     * 
     * @return the removed item
     * 
     * @see #remove(Widget)
     */
    public I removeItem(String itemId) {

        I item = m_items.get(itemId);
        remove((Widget)item);
        return item;
    }

    /**
     * @see org.opencms.gwt.client.ui.dnd.I_CmsDropTarget#removePlaceholder()
     */
    public void removePlaceholder() {

        if (m_placeholder == null) {
            return;
        }
        m_placeholder.removeFromParent();
        m_placeholder = null;
    }

    /**
     * Enables/Disables drag'n drop.<p>
     * 
     * @param enabled <code>true</code> to enable drag'n drop 
     */
    public void setDnDEnabled(boolean enabled) {

        if (m_dndEnabled == enabled) {
            return;
        }
        m_dndEnabled = enabled;
        if (m_dndManager == null) {
            // set default DnD manager
            m_dndManager = new CmsDnDManager();
            // add this as a drop target
            m_dndManager.addDragTarget(this);
        }
    }

    /**
     * Sets the drag'n drop handler.<p>
     *
     * @param handler the handler to set
     */
    public void setDnDManager(CmsDnDManager handler) {

        m_dndManager = handler;
    }

    /**
     * Enables/disables dropping.<p>
     *
     * @param enabled <code>true</code> to enable, or <code>false</code> to disable
     */
    public void setDropEnabled(boolean enabled) {

        m_dropEnabled = enabled;
    }

    /**
     * @see org.opencms.gwt.client.ui.dnd.I_CmsDropTarget#setPlaceholder(int, int, CmsDropEvent)
     */
    public CmsDropPosition setPlaceholder(int x, int y, CmsDropEvent event) {

        Element targetElement = getElement();
        // TODO: use binary search instead of this linear search, will improve performance from O(n) to O(log(n))!
        for (int index = 0; index < targetElement.getChildCount(); index++) {
            Node node = targetElement.getChild(index);
            if (!(node instanceof Element)) {
                continue;
            }
            Element child = (Element)node;

            String positioning = child.getStyle().getPosition();
            if (positioning.equals(Position.ABSOLUTE.getCssName()) || positioning.equals(Position.FIXED.getCssName())) {
                // only not 'position:absolute' elements into account, 
                // not visible children will be excluded in the next condition
                continue;
            }

            // check if the mouse pointer is within the width of the element 
            int left = CmsDomUtil.getRelativeX(x, child);
            if ((left <= 0) || (left >= child.getOffsetWidth())) {
                continue;
            }

            // check if the mouse pointer is within the height of the element 
            int top = CmsDomUtil.getRelativeY(y, child);
            int height = child.getOffsetHeight();
            if ((top <= 0) || (top >= height)) {
                continue;
            }

            CmsDropPosition position = null;
            boolean checkPos = ((event.getTarget() == this) && (event.getPosition() != null));
            I_CmsDraggable draggable = event.getDraggable();
            if (draggable.getElement() == child) {
                // this case occurs when start dragging
                if (checkPos && (event.getPosition().getPosition() == index)) {
                    // nothing has changed
                    return null;
                }
                // insert place holder before the current child
                removePlaceholder();
                m_placeholder = draggable.getPlaceHolder(this);
                targetElement.insertBefore(m_placeholder, child);
                position = new CmsDropPosition(null, index, null, m_placeholder);
            } else if (top < height / 2) {
                // the mouse pointer is within the upper half of the element
                if (checkPos && (event.getPosition().getPosition() == index)) {
                    // nothing has changed
                    return null;
                }
                removePlaceholder();
                m_placeholder = draggable.getPlaceHolder(this);
                targetElement.insertBefore(m_placeholder, child);
                position = new CmsDropPosition(null, index, null, m_placeholder);
            } else {
                // the mouse pointer is within the bottom half of the element
                if (checkPos && (event.getPosition().getPosition() == index + 1)) {
                    // nothing has changed
                    return null;
                }
                removePlaceholder();
                m_placeholder = draggable.getPlaceHolder(this);
                targetElement.insertAfter(m_placeholder, child);
                position = new CmsDropPosition(null, index + 1, null, m_placeholder);
            }
            if (draggable instanceof CmsListItem) {
                // add name if available
                String name = ((CmsListItem)draggable).getId();
                position.setName(name);
            }
            return position;
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsPrefix, int widgetWidth) {

        m_childWidth = widgetWidth;
        for (I_CmsListItem item : m_items.values()) {
            item.truncate(textMetricsPrefix, widgetWidth);
        }
        m_tmPrefix = textMetricsPrefix;
    }

    /**
     * Changes the id for the given item.<p>
     * 
     * @param item the item to change the id for
     * @param id the new id
     */
    protected void changeId(I item, String id) {

        if (m_items.remove(item.getId()) != null) {
            m_items.put(id, item);
        }
    }

    /**
     * Registers the given item on this list.<p>
     * 
     * @param item the item to register
     */
    protected void registerItem(I item) {

        if (item.getId() != null) {
            m_items.put(item.getId(), item);
        }
        if (m_tmPrefix != null) {
            item.truncate(m_tmPrefix, m_childWidth);
        }
    }

    /**
     * Sets the current drag'n drop place holder.<p>
     * 
     * @param placeholder the element to set as place holder
     */
    protected void setPlaceholder(Element placeholder) {

        removePlaceholder();
        m_placeholder = placeholder;
    }
}
