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

import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsListTreeCss;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A very basic list implementation to hold {@link CmsListItemWidget}.<p>
 *
 * @param <I> the specific list item implementation
 *
 * @since 8.0.0
 */
public class CmsList<I extends I_CmsListItem> extends ComplexPanel implements I_CmsTruncable, I_CmsDropTarget {

    /** The css bundle used for this widget. */
    private static final I_CmsListTreeCss CSS = I_CmsLayoutBundle.INSTANCE.listTreeCss();

    /** The drag'n drop handler. */
    protected CmsDNDHandler m_dndHandler;

    /** The current place holder. */
    protected Element m_placeholder;

    /** The placeholder position index. */
    protected int m_placeholderIndex = -1;

    /** The child width in px for truncation. */
    private int m_childWidth;

    /** Flag to indicate if drag'n drop on the root node is allowed. */
    private boolean m_dropEnabled;

    /** The map of items. */
    private Map<String, I> m_items;

    /** Flag to indicate if the list will always return <code>true</code> on check target requests within drag and drop. */
    private boolean m_takeAll;

    /** The text metrics prefix. */
    private String m_tmPrefix;

    /**
     * Constructor.<p>
     */
    public CmsList() {

        setElement((Element)DOM.createElement(CmsDomUtil.Tag.ul.name()));
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
        add(widget, (Element)getElement());
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
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#checkPosition(int, int, Orientation)
     */
    public boolean checkPosition(int x, int y, Orientation orientation) {

        if (!isDropEnabled()) {
            return false;
        }
        if (isDNDTakeAll()) {
            return true;
        }
        switch (orientation) {
            case HORIZONTAL:
                return CmsDomUtil.checkPositionInside(getElement(), x, -1);
            case VERTICAL:
                return CmsDomUtil.checkPositionInside(getElement(), -1, y);
            case ALL:
            default:
                return CmsDomUtil.checkPositionInside(getElement(), x, y);
        }
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
    public CmsDNDHandler getDnDHandler() {

        return m_dndHandler;
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
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#getPlaceholderIndex()
     */
    public int getPlaceholderIndex() {

        return m_placeholderIndex;

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
        insert(widget, (Element)getElement(), position, true);
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
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#insertPlaceholder(com.google.gwt.dom.client.Element, int, int, Orientation)
     */
    public void insertPlaceholder(Element placeholder, int x, int y, Orientation orientation) {

        m_placeholder = placeholder;
        repositionPlaceholder(x, y, orientation);
    }

    /**
     * Returns if the list will always return <code>true</code> on check target requests within drag and drop.<p>
     *
     * @return <code>true</code> if take all is enabled for drag and drop
     */
    public boolean isDNDTakeAll() {

        return m_takeAll;
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
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#onDrop(org.opencms.gwt.client.dnd.I_CmsDraggable)
     */
    public void onDrop(I_CmsDraggable draggable) {

        return;
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
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#removePlaceholder()
     */
    public void removePlaceholder() {

        if (m_placeholder == null) {
            return;
        }
        m_placeholder.removeFromParent();
        m_placeholder = null;
        m_placeholderIndex = -1;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDropTarget#repositionPlaceholder(int, int, Orientation)
     */
    public void repositionPlaceholder(int x, int y, Orientation orientation) {

        switch (orientation) {
            case HORIZONTAL:
                m_placeholderIndex = CmsDomUtil.positionElementInside(
                    m_placeholder,
                    getElement(),
                    m_placeholderIndex,
                    x,
                    -1);
                break;
            case VERTICAL:
                m_placeholderIndex = CmsDomUtil.positionElementInside(
                    m_placeholder,
                    getElement(),
                    m_placeholderIndex,
                    -1,
                    y);
                break;
            case ALL:
            default:
                m_placeholderIndex = CmsDomUtil.positionElementInside(
                    m_placeholder,
                    getElement(),
                    m_placeholderIndex,
                    x,
                    y);
                break;
        }
    }

    /**
     * Sets the drag'n drop handler.<p>
     *
     * @param handler the handler to set
     */
    public void setDNDHandler(CmsDNDHandler handler) {

        m_dndHandler = handler;
    }

    /**
     * Sets if the list will always return <code>true</code> on check target requests within drag and drop.<p>
     *
     * @param takeAll <code>true</code> to enable take all for drag and drop
     */
    public void setDNDTakeAll(boolean takeAll) {

        m_takeAll = takeAll;
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
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsPrefix, int widgetWidth) {

        m_childWidth = widgetWidth;
        for (Widget item : this) {
            if (item instanceof I_CmsTruncable) {
                ((I_CmsTruncable)item).truncate(textMetricsPrefix, widgetWidth);
            }
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
