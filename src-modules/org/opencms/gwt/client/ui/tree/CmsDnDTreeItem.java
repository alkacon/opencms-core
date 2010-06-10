/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/tree/Attic/CmsDnDTreeItem.java,v $
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
import org.opencms.gwt.client.ui.CmsDnDListDropEvent;
import org.opencms.gwt.client.ui.CmsDnDListHandler;
import org.opencms.gwt.client.ui.CmsDnDListItem;
import org.opencms.gwt.client.ui.CmsToggleButton;
import org.opencms.gwt.client.ui.I_CmsDnDListDropHandler;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsListTreeCss;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.util.CmsStyleVariable;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Image;
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
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0
 */
public class CmsDnDTreeItem extends CmsDnDListItem {

    /** The duration of the animations. */
    public static final int ANIMATION_DURATION = 200;

    /** The CSS bundle used for this widget. */
    private static final I_CmsListTreeCss CSS = I_CmsLayoutBundle.INSTANCE.listTreeCss();

    /** The width of the opener. */
    private static final int OPENER_WIDTH = 16;

    /** The children list. */
    protected CmsDnDList<CmsDnDTreeItem> m_children;

    /** The element showing the open/close icon. */
    protected CmsToggleButton m_opener;

    /** Timer to open item while dragging and hovering. */
    Timer m_timer;

    /** The style variable controlling this tree item's leaf/non-leaf state. */
    private CmsStyleVariable m_leafStyleVar;

    /** Flag to indicate if open or closed. */
    private boolean m_open;

    /** The item parent. */
    private CmsDnDTreeItem m_parentItem;

    /** The style variable controlling this tree item's open/closed state. */
    private CmsStyleVariable m_styleVar;

    /** The tree reference. */
    private CmsDnDTree<CmsDnDTreeItem> m_tree;

    /**
     * Creates a new list tree item containing a main widget and a check box.<p>
     * 
     * @param showOpeners if true, show open/close icons
     * @param checkbox the check box 
     * @param mainWidget the main widget 
     */
    public CmsDnDTreeItem(boolean showOpeners, CmsCheckBox checkbox, Widget mainWidget) {

        this(showOpeners);
        addMainWidget(mainWidget);
        addCheckBox(checkbox);
        initContent();
        if (!showOpeners) {
            hideOpeners();
        }
    }

    /**
     * Creates a new list tree item containing a main widget.<p>
     * 
     * @param showOpeners if true, show open/close icons 
     * @param mainWidget the main widget 
     */
    public CmsDnDTreeItem(boolean showOpeners, Widget mainWidget) {

        this(showOpeners);
        addMainWidget(mainWidget);
        initContent();
        if (!showOpeners) {
            hideOpeners();
        }

    }

    /** 
     * Default constructor.<p>
     * 
     * @param showOpeners if true, the opener icons should be shown 
     */
    protected CmsDnDTreeItem(boolean showOpeners) {

        super();
        m_styleVar = new CmsStyleVariable(this);
        m_leafStyleVar = new CmsStyleVariable(this);
        m_opener = createOpener();
        addDecoration(m_opener, showOpeners ? OPENER_WIDTH : 0, true);
        m_children = new CmsDnDList<CmsDnDTreeItem>();
        m_children.addListDropHandler(new I_CmsDnDListDropHandler() {

            /**
             * @see org.opencms.gwt.client.ui.I_CmsDnDListDropHandler#onDrop(org.opencms.gwt.client.ui.CmsDnDListDropEvent)
             */
            @SuppressWarnings("unchecked")
            public void onDrop(CmsDnDListDropEvent dropEvent) {

                CmsDnDListItem destItem = dropEvent.getDestList().getItem(dropEvent.getDestPath());
                if (!(destItem instanceof CmsDnDTreeItem)) {
                    // do nothing
                    return;
                }
                CmsDnDTreeItem destTreeItem = (CmsDnDTreeItem)destItem;

                // this can not be done later because the most likely srcItem == destItem
                // since the inconsistency we are fixing here
                // and since we manipulate destItem (to fix it) we will loose info on srcItem
                CmsDnDListItem srcItem = dropEvent.getSrcList().getItem(DRAGGED_PLACEHOLDER_ID);
                CmsDnDTreeItem srcTreeItem = (CmsDnDTreeItem)srcItem;
                CmsDnDList<? extends CmsDnDListItem> srcList = dropEvent.getSrcList();
                String srcPath = dropEvent.getSrcPath();
                if (srcTreeItem.getTree() != null) {
                    // event is coming from a tree
                    srcList = srcTreeItem.getTree();
                    srcPath = srcTreeItem.getPath();
                    if (!dropEvent.getSrcPath().equals(dropEvent.getDestPath())
                        && srcPath.endsWith(dropEvent.getDestPath() + "/")) {
                        // we need to fix this
                        srcPath = srcPath.substring(0, srcPath.length() - dropEvent.getDestPath().length() - 1);
                        srcPath += dropEvent.getSrcPath() + "/";
                    }
                }

                // remove item from old position
                CmsDnDTreeItem oldParent = destTreeItem.getParentItem();
                destTreeItem.setParentItem(null);
                destTreeItem.setTree(null);
                oldParent.onChangeChildren();

                // insert item to new position
                destTreeItem.setParentItem(CmsDnDTreeItem.this);
                destTreeItem.setTree(getTree());
                onChangeChildren();

                if (!(srcItem instanceof CmsDnDTreeItem)) {
                    // event is coming from a list not a tree, forward event
                    getTree().fireDropEvent(
                        new CmsDnDTreeDropEvent(
                            dropEvent.getSrcList(),
                            dropEvent.getSrcPath(),
                            getTree(),
                            destTreeItem.getPath()));
                    return;
                }

                // forward event
                getTree().fireDropEvent(
                    new CmsDnDTreeDropEvent(
                        (CmsDnDList<CmsDnDListItem>)srcList,
                        srcPath,
                        getTree(),
                        destTreeItem.getPath()));
            }
        });
        m_children.setStyleName(CSS.listTreeItemChildren());
        m_panel.add(m_children);
        onChangeChildren();
        m_open = true;
        setOpen(false);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsListItem#add(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public void add(Widget w) {

        throw new UnsupportedOperationException();
    }

    /**
     * Adds a child list item.<p>
     * 
     * @param item the child to add
     * 
     * @see org.opencms.gwt.client.ui.CmsList#addItem(org.opencms.gwt.client.ui.I_CmsListItem)
     */
    public void addChild(CmsDnDTreeItem item) {

        m_children.addItem(item);
        adopt(item);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasWidgets#clear()
     */
    public void clear() {

        clearChildren();
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
     * @see org.opencms.gwt.client.ui.CmsDnDListItem#disableDnD()
     */
    @Override
    public void disableDnD() {

        m_children.setDnDEnabled(false);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsDnDListItem#enableDnD(org.opencms.gwt.client.ui.CmsDnDListHandler)
     */
    @Override
    public void enableDnD(CmsDnDListHandler handler) {

        super.enableDnD(handler);
        m_children.setDnDHandler(handler);
        m_children.setDnDEnabled(true);
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
    public CmsDnDTreeItem getChild(int index) {

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
    public CmsDnDTreeItem getChild(String itemId) {

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
     * Returns the given item position.<p>
     * 
     * @param item the item to get the position for
     * 
     * @return the item position
     */
    public int getItemPosition(CmsDnDTreeItem item) {

        return m_children.getWidgetIndex(item);
    }

    /**
     * Returns the parent item.<p>
     *
     * @return the parent item
     */
    public CmsDnDTreeItem getParentItem() {

        return m_parentItem;
    }

    /**
     * Returns the path of IDs for the this item.<p>
     * 
     * @return a path of IDs separated by slash
     */
    public String getPath() {

        StringBuffer path = new StringBuffer("/");
        CmsDnDTreeItem current = this;
        while (current != null) {
            path.insert(0, current.getId()).insert(0, "/");
            current = current.getParentItem();
        }
        return path.toString();
    }

    /**
     * Gets the tree to which this tree item belongs, or null if it does not belong to a tree.<p>
     * 
     * @return a tree or null
     */
    public CmsDnDTree<CmsDnDTreeItem> getTree() {

        return m_tree;
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
    public void insertChild(CmsDnDTreeItem item, int position) {

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
     * Will be executed when the user drags something over this item.<p>
     * 
     * Will also check that drop over is allowed
     * 
     * @return <code>true</code> if drop over is allowed
     */
    public boolean onDragOverIn() {

        getListItemWidget().getContentPanel().addStyleName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().itemActive());
        if (isOpen()) {
            return true;
        }
        m_timer = new Timer() {

            /**
             * @see com.google.gwt.user.client.Timer#run()
             */
            @Override
            public void run() {

                m_timer = null;
                setOpen(true);
            }
        };
        m_timer.schedule(1000);
        return true;
    }

    /**
     * Will be executed when the user stops dragging something over this item.<p>
     */
    public void onDragOverOut() {

        if (m_timer != null) {
            m_timer.cancel();
            m_timer = null;
        }
        getListItemWidget().getContentPanel().removeStyleName(
            I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().itemActive());
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
    public CmsDnDTreeItem removeChild(final CmsDnDTreeItem item) {

        item.setParentItem(null);
        item.setTree(null);
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
                    onChangeChildren();
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
            onChangeChildren();
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
    public CmsDnDTreeItem removeChild(int index) {

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
    public CmsDnDTreeItem removeChild(String itemId) {

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
            fireOpen();
        }
    }

    /**
     * Sets the parent item.<p>
     *
     * @param parentItem the parent item to set
     */
    public void setParentItem(CmsDnDTreeItem parentItem) {

        m_parentItem = parentItem;
    }

    /**
     * Sets the tree to which this tree item belongs.<p>
     * 
     * This is automatically called when this tree item or one of its ancestors is inserted into a tree.<p>
     * 
     * @param tree the tree into which the item has been inserted
     */
    @SuppressWarnings("unchecked")
    public void setTree(CmsDnDTree<CmsDnDTreeItem> tree) {

        CmsDnDList<? extends CmsDnDListItem> list = m_children;
        if ((tree != null) && (tree.getDnDHandler() != null)) {
            m_children.setDnDHandler(tree.getDnDHandler());
            tree.getDnDHandler().addDragTarget((CmsDnDList<CmsDnDListItem>)list);
        } else {
            m_children.setDnDHandler(null);
            if ((m_tree != null) && (m_tree.getDnDHandler() != null)) {
                m_tree.getDnDHandler().removeDragTarget((CmsDnDList<CmsDnDListItem>)list);
            }
        }
        m_tree = tree;
        for (Widget widget : m_children) {
            if (widget instanceof CmsDnDTreeItem) {
                ((CmsDnDTreeItem)widget).setTree(tree);
            }
        }
    }

    /**
     * Adopts the given item.<p>
     * 
     * @param item the item to adopt
     */
    protected void adopt(final CmsDnDTreeItem item) {

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
    protected CmsToggleButton createOpener() {

        final CmsToggleButton opener = new CmsToggleButton();
        opener.setStyleName(CSS.listTreeItemHandler());

        opener.setUpFace("", CSS.plus());
        opener.setDownFace("", CSS.minus());
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
