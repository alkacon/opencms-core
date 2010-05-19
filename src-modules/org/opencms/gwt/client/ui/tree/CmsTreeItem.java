/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/tree/Attic/CmsTreeItem.java,v $
 * Date   : $Date: 2010/05/19 10:18:00 $
 * Version: $Revision: 1.10 $
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
import org.opencms.gwt.client.ui.CmsSimpleListItem;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsListTreeCss;
import org.opencms.gwt.client.util.CmsStyleVariable;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * List tree item implementation.<p>
 * 
 * Implemented as:
 * <pre>
 * &lt;li class='listTreeItem listTreeItem*state*'>
 *   &lt;span class='listTreeItemImage'>&lt;/span>
 *   &lt;div class='listTreeItemContent'>...*content*&lt;/div>
 *   &lt;ul class='listTreeItemChildren'>
 *      *children*
 *   &lt;/ul>
 * &lt;/li>
 * </pre>
 * 
 * Where state can be <code>opened</code>, <code>closed</code> or <code>leaf</code>.<p>
 * 
 * @author Georg Westenberger
 * @author Michael Moossen
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 8.0.0
 */
public class CmsTreeItem extends CmsSimpleListItem {

    /** The duration of the animations. */
    public static final int ANIMATION_DURATION = 200;

    /** The CSS bundle used for this widget. */
    private static final I_CmsListTreeCss CSS = I_CmsLayoutBundle.INSTANCE.listTreeCss();

    /** The children list. */
    protected CmsList<CmsTreeItem> m_children;

    /** The element showing the open/close icon. */
    protected ToggleButton m_opener;

    /** The style variable controlling this tree item's leaf/non-leaf state. */
    private CmsStyleVariable m_leafStyleVar;

    /** Flag to indicate if open or closed. */
    private boolean m_open;

    /** The item parent. */
    private CmsTreeItem m_parentItem;

    /** The style variable controlling this tree item's open/closed state. */
    private CmsStyleVariable m_styleVar;

    /** The tree reference. */
    private CmsTree<CmsTreeItem> m_tree;

    /** 
     * Default constructor.<p>
     */
    public CmsTreeItem() {

        super();
        m_styleVar = new CmsStyleVariable(this);
        m_leafStyleVar = new CmsStyleVariable(this);
        m_opener = createOpener();
        m_content.addToFloat(m_opener);
        m_children = new CmsList<CmsTreeItem>();
        m_children.setStyleName(CSS.listTreeItemChildren());
        m_panel.add(m_children);
        onChangeChildren();
        m_open = true;
        setOpen(false);
    }

    /**
     * Creates a new list tree item containing several widgets.<p>
     * 
     * @param showOpeners if true, show open/close icons
     * @param content the widgets to put into the tree item 
     */
    public CmsTreeItem(boolean showOpeners, Widget... content) {

        this();
        if (content.length > 0) {
            // put all but the last widget into the float section
            for (int i = 0; i < content.length - 1; i++) {
                m_content.addToFloat(content[i]);
            }
            add(content[content.length - 1]);
        }
        if (!showOpeners) {
            hideOpeners();
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsSimpleListItem#add(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public void add(Widget w) {

        m_content.add(w);
    }

    /**
     * Adds a child list item.<p>
     * 
     * @param item the child to add
     * 
     * @see org.opencms.gwt.client.ui.CmsList#addItem(org.opencms.gwt.client.ui.I_CmsListItem)
     */
    public void addChild(CmsTreeItem item) {

        m_children.addItem(item);
        adopt(item);
    }

    /**
     * Removes all children.<p>
     * 
     * @see org.opencms.gwt.client.ui.CmsList#clearList()
     */
    public void clearChildren() {

        for (int i = getChildCount(); i > 0; i--) {
            removeChild(i - 1);
        }
    }

    /**
     * Returns the child tree item at the given position.<p>
     * 
     * @param index the position
     * 
     * @return the tree item
     * 
     * @see org.opencms.gwt.client.ui.CmsList#getItem(int)
     */
    public CmsTreeItem getChild(int index) {

        return m_children.getItem(index);
    }

    /**
     * Returns the tree item with the given id.<p>
     * 
     * @param itemId the id of the item to retrieve
     * 
     * @return the tree item
     * 
     * @see org.opencms.gwt.client.ui.CmsList#getItem(String)
     */
    public CmsTreeItem getChild(String itemId) {

        return m_children.getItem(itemId);
    }

    /**
     * Helper method which gets the number of children.<p>
     * 
     * @return the number of children
     * 
     * @see org.opencms.gwt.client.ui.CmsList#getWidgetCount()
     */
    public int getChildCount() {

        return m_children.getWidgetCount();
    }

    /**
     * Returns the parent item.<p>
     *
     * @return the parent item
     */
    public CmsTreeItem getParentItem() {

        return m_parentItem;
    }

    /**
     * Gets the tree to which this tree item belongs, or null if it does not belong to a tree.<p>
     * 
     * @return a tree or null
     */
    public CmsTree<CmsTreeItem> getTree() {

        return m_tree;
    }

    /**
     * Returns the widget at the given position.<p>
     * 
     * @param index the position
     * 
     * @return  the widget at the given position
     */
    public Widget getWidget(int index) {

        return m_content.getWidget(index);
    }

    /** 
     * Hides the open/close icons for this tree item and its descendants.<p>
     */
    public void hideOpeners() {

        addStyleName(CSS.listTreeItemNoOpeners());
    }

    /**
     * Inserts the given item at the given position.<p>
     * 
     * @param item the item to insert
     * @param position the position
     * 
     * @see org.opencms.gwt.client.ui.CmsList#insertItem(org.opencms.gwt.client.ui.I_CmsListItem, int)
     */
    public void insertChild(CmsTreeItem item, int position) {

        m_children.insert(item, position);
        adopt(item);
    }

    /**
     * Checks if the item is open or closed.<p>
     *
     * @return <code>true</code> if open
     */
    public boolean isOpen() {

        return m_open;
    }

    /**
     * Removes an item from the list.<p>
     * 
     * @param item the item to remove
     * 
     * @return the removed item 
     * 
     * @see org.opencms.gwt.client.ui.CmsList#removeItem(org.opencms.gwt.client.ui.I_CmsListItem)
     */
    public CmsTreeItem removeChild(final CmsTreeItem item) {

        item.setParentItem(null);
        item.setTree(null);
        onChangeChildren();
        if ((m_tree != null) && m_tree.isAnimationEnabled()) {
            // could be null if already detached
            // animate
            (new Animation() {

                /**
                 * @see com.google.gwt.animation.client.Animation#onComplete()
                 */
                @Override
                protected void onComplete() {

                    super.onComplete();
                    m_children.removeItem(item);
                }

                /**
                 * @see com.google.gwt.animation.client.Animation#onUpdate(double)
                 */
                @Override
                protected void onUpdate(double progress) {

                    item.getElement().getStyle().setOpacity(1 - progress);
                }
            }).run(ANIMATION_DURATION);
        } else {
            m_children.removeItem(item);
        }
        return item;
    }

    /**
     * Removes the item identified by the given index from the list.<p>
     * 
     * @param index the index of the item to remove
     * 
     * @return the removed item 
     * 
     * @see org.opencms.gwt.client.ui.CmsList#remove(int)
     */
    public CmsTreeItem removeChild(int index) {

        return removeChild(m_children.getItem(index));
    }

    /**
     * Removes an item from the list.<p>
     * 
     * @param itemId the id of the item to remove
     * 
     * @return the removed item
     * 
     * @see org.opencms.gwt.client.ui.CmsList#removeItem(String)
     */
    public CmsTreeItem removeChild(String itemId) {

        return removeChild(m_children.getItem(itemId));
    }

    /**
     * Opens or closes this tree item (i.e. shows or hides its descendants).<p>
     * 
     * @param open if true, open the tree item, else close it
     */
    public void setOpen(boolean open) {

        if (m_open == open) {
            return;
        }
        m_open = open;

        m_styleVar.setValue(open ? CSS.listTreeItemOpen() : CSS.listTreeItemClosed());
        m_opener.setDown(open);
        if (open) {
            for (Widget widget : m_children) {
                if (widget instanceof CmsSimpleListItem) {
                    ((CmsSimpleListItem)widget).updateLayout();
                }
            }
            fireOpen();
        }
    }

    /**
     * Sets the parent item.<p>
     *
     * @param parentItem the parent item to set
     */
    public void setParentItem(CmsTreeItem parentItem) {

        m_parentItem = parentItem;
    }

    /**
     * Sets the tree to which this tree item belongs.<p>
     * 
     * This is automatically called when this tree item or one of its ancestors is inserted into a tree.<p>
     * 
     * @param tree the tree into which the item has been inserted
     */
    public void setTree(CmsTree<CmsTreeItem> tree) {

        m_tree = tree;
        for (Widget widget : m_children) {
            if (widget instanceof CmsTreeItem) {
                ((CmsTreeItem)widget).setTree(tree);
            }
        }
    }

    /**
     * Adopts the given item.<p>
     * 
     * @param item the item to adopt
     */
    protected void adopt(final CmsTreeItem item) {

        item.setParentItem(this);
        item.setTree(m_tree);
        onChangeChildren();
        if ((m_tree != null) && m_tree.isAnimationEnabled()) {
            // could be null if not yet attached
            item.getElement().getStyle().setOpacity(0);
            // animate
            (new Animation() {

                /**
                 * @see com.google.gwt.animation.client.Animation#onUpdate(double)
                 */
                @Override
                protected void onUpdate(double progress) {

                    item.getElement().getStyle().setOpacity(progress);
                }
            }).run(ANIMATION_DURATION);
        }
    }

    /**
     * Creates the button for opening/closing this item.<p>
     * 
     * @return a button
     */
    protected ToggleButton createOpener() {

        final ToggleButton opener = new ToggleButton();
        opener.setStyleName(CSS.listTreeItemHandler());
        opener.getUpFace().setImage(getPlusImage());
        opener.getDownFace().setImage(getMinusImage());
        opener.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent e) {

                setOpen(opener.isDown());
            }
        });
        return opener;
    }

    /** 
     * Fires the open event on the tree.<p> 
     */
    protected void fireOpen() {

        if (m_tree != null) {
            m_tree.fireOpen(this);
        }
    }

    /**
     * The '-' image.<p>
     * 
     * @return the minus image 
     */
    protected Image getMinusImage() {

        return new Image(I_CmsImageBundle.INSTANCE.minus());
    }

    /**
     * The '+' image.<p>
     * 
     * @return the plus image 
     */
    protected Image getPlusImage() {

        return new Image(I_CmsImageBundle.INSTANCE.plus());
    }

    /**
     * Helper method which is called when the list of children changes.<p> 
     */
    protected void onChangeChildren() {

        int count = getChildCount();
        m_leafStyleVar.setValue(count == 0 ? CSS.listTreeItemLeaf() : CSS.listTreeItemInternal());
    }
}
