/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsList.java,v $
 * Date   : $Date: 2010/05/06 13:37:38 $
 * Version: $Revision: 1.13 $
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
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.HashMap;
import java.util.Map;

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
 * @version $Revision: 1.13 $
 * 
 * @since 8.0.0
 */
public class CmsList<I extends I_CmsListItem> extends ComplexPanel implements I_CmsTruncable {

    /** The css bundle used for this widget. */
    private static final I_CmsListTreeCss CSS = I_CmsLayoutBundle.INSTANCE.listTreeCss();

    /** The child width in px for truncation. */
    private int m_childWidth;

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
    @Override
    public void add(Widget widget) {

        assert widget instanceof I_CmsListItem;
        add(widget, getElement());
        registerItem((I_CmsListItem)widget);
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
     * Clears the list.<p>
     */
    public void clearList() {

        clear();
        m_items.clear();
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
     * Inserts the given widget at the given position.<p>
     * 
     * @param widget the widget to insert
     * @param position the position
     */
    public void insert(Widget widget, int position) {

        assert widget instanceof I_CmsListItem;
        insert(widget, getElement(), position, true);
        registerItem((I_CmsListItem)widget);
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
     * Removes an item from the list.<p>
     * 
     * @param item the item to remove
     * 
     * @see #remove(Widget)
     */
    public void removeItem(I item) {

        remove((Widget)item);
        if (item.getId() != null) {
            m_items.remove(item.getId());
        }
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
     * Updates the layout for all list items in this list.<p>
     */
    public void updateLayout() {

        for (Widget widget : this) {
            if (widget instanceof CmsSimpleListItem) {
                ((CmsSimpleListItem)widget).updateLayout();
            }
        }
    }

    /**
     * Registers the given item on this list.<p>
     * 
     * @param item the item to register
     */
    @SuppressWarnings("unchecked")
    protected void registerItem(I_CmsListItem item) {

        if (item.getId() != null) {
            m_items.put(item.getId(), (I)item);
        }
        if (m_tmPrefix != null) {
            item.truncate(m_tmPrefix, m_childWidth);
        }
    }
}
