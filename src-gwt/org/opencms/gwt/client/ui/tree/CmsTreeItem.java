/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsToggleButton;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsListTreeCss;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsStyleVariable;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
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
 * @since 8.0.0
 */
public class CmsTreeItem extends CmsListItem {

    /** The duration of the animations. */
    public static final int ANIMATION_DURATION = 200;

    /** The CSS bundle used for this widget. */
    private static final I_CmsListTreeCss CSS = I_CmsLayoutBundle.INSTANCE.listTreeCss();

    /** The width of the opener. */
    private static final int OPENER_WIDTH = 16;

    /** The children list. */
    protected CmsList<CmsTreeItem> m_children;

    /** The element showing the open/close icon. */
    protected CmsToggleButton m_opener;

    /** Flag to indicate if drag'n drop is enabled. 3-states: if <code>null</code> the tree decides. */
    private Boolean m_dropEnabled;

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
     * Creates a new list tree item containing a main widget and a check box.<p>
     * 
     * @param showOpeners if true, show open/close icons
     * @param checkbox the check box 
     * @param mainWidget the main widget 
     */
    public CmsTreeItem(boolean showOpeners, CmsCheckBox checkbox, Widget mainWidget) {

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
    public CmsTreeItem(boolean showOpeners, Widget mainWidget) {

        this(showOpeners);
        addMainWidget(mainWidget);
        initContent();
        if (!showOpeners) {
            hideOpeners();
        }
    }

    /**
     * Creates a new tree item with a 24px wide icon.<p>
     *  
     * @param showOpeners
     * @param mainWidget
     * @param icon
     */
    public CmsTreeItem(boolean showOpeners, Widget mainWidget, String icon) {

        this(showOpeners);
        addMainWidget(mainWidget);
        Label label = new Label();
        label.addStyleName(icon);
        addDecoration(label, 28, true);
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
    protected CmsTreeItem(boolean showOpeners) {

        super();
        m_styleVar = new CmsStyleVariable(this);
        m_leafStyleVar = new CmsStyleVariable(this);
        m_opener = createOpener();
        addDecoration(m_opener, showOpeners ? OPENER_WIDTH : 0, true);
        m_children = new CmsList<CmsTreeItem>();
        m_children.setStyleName(CSS.listTreeItemChildren());
        m_panel.add(m_children);
        onChangeChildren();
        m_open = true;
        setOpen(false);
    }

    /**
     * Returns the last opened item of a tree fragment.<p>
     * 
     * @param item the tree item
     * @param stopLevel the level to stop at, set -1 to go to the very last opened item
     * @param requiresDropEnabled <code>true</code> if it is required the returned element to be drop enabled
     * 
     * @return the last visible item of a tree fragment
     */
    protected static CmsTreeItem getLastOpenedItem(CmsTreeItem item, int stopLevel, boolean requiresDropEnabled) {

        if (stopLevel != -1) {
            // stop level is set
            int currentLevel = getPathLevel(item.getPath());
            if (currentLevel > stopLevel) {
                // we are past the stop level, prevent further checks
                stopLevel = -1;
            } else if (currentLevel == stopLevel) {
                // matches stop level
                return item;
            }
        }
        if (item.getChildCount() > 0) {
            int childIndex = item.getChildCount() - 1;
            CmsTreeItem child = item.getChild(childIndex);
            if (requiresDropEnabled) {
                while (!child.isDropEnabled()) {
                    childIndex--;
                    if (childIndex < 0) {
                        return item;
                    }
                    child = item.getChild(childIndex);
                }
            }

            if (child.isOpen()) {
                return CmsTreeItem.getLastOpenedItem(child, stopLevel, requiresDropEnabled);
            }
        }
        return item;
    }

    /**
     * Method determining the path level by counting the number of '/'.<p>
     * Example: '/xxx/xxx/' has a path-level of 2.<p>
     * 
     * @param path the path to test
     * 
     * @return the path level
     */
    protected static native int getPathLevel(String path)/*-{
        return path.match(/\//g).length - 1;
    }-*/;

    /**
     * Unsupported operation.<p>
     * 
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
    public void addChild(CmsTreeItem item) {

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
     * Closes all empty child entries.<p>
     */
    public void closeAllEmptyChildren() {

        for (Widget child : m_children) {
            if (child instanceof CmsTreeItem) {
                CmsTreeItem item = (CmsTreeItem)child;
                if (item.isOpen()) {
                    if (item.getChildCount() == 0) {
                        item.setOpen(false);
                    } else {
                        item.closeAllEmptyChildren();
                    }
                }
            }
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

        CmsTreeItem result = m_children.getItem(itemId);
        return result;
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
     * Returns the children of this list item.<p>
     * 
     * @return the children list
     */
    public CmsList<? extends I_CmsListItem> getChildren() {

        return m_children;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#getDragHelper(I_CmsDropTarget)
     */
    @Override
    public Element getDragHelper(I_CmsDropTarget target) {

        // disable animation to get a drag helper without any visible children
        boolean isAnimated = getTree().isAnimationEnabled();
        getTree().setAnimationEnabled(false);
        setOpen(false);
        getTree().setAnimationEnabled(isAnimated);
        return super.getDragHelper(target);
    }

    /**
     * Returns the given item position.<p>
     * 
     * @param item the item to get the position for
     * 
     * @return the item position
     */
    public int getItemPosition(CmsTreeItem item) {

        return m_children.getWidgetIndex(item);
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
     * @see org.opencms.gwt.client.dnd.I_CmsDraggable#onDragCancel()
     */
    @Override
    public void onDragCancel() {

        CmsTreeItem parent = getParentItem();
        if (parent != null) {
            parent.insertChild(this, parent.getItemPosition(this));
        }
        super.onDragCancel();
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsListItem#getParentTarget()
     */
    @Override
    public I_CmsDropTarget getParentTarget() {

        return getTree();
    }

    /**
     * Returns the path of IDs for the this item.<p>
     * 
     * @return a path of IDs separated by slash
     */
    public String getPath() {

        StringBuffer path = new StringBuffer("/");
        CmsTreeItem current = this;
        while (current != null) {
            path.insert(0, current.getId()).insert(0, "/");
            current = current.getParentItem();
        }
        String result = path.toString();
        if (result.startsWith("//")) {
            // This happens if the root item has an empty id.
            // In that case, we cut off the first slash. 
            result = result.substring(1);
        }
        return result;
    }

    /**
     * Gets the tree to which this tree item belongs, or null if it does not belong to a tree.<p>
     * 
     * @return a tree or <code>null</code>
     */
    public CmsTree<CmsTreeItem> getTree() {

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
    public void insertChild(CmsTreeItem item, int position) {

        m_children.insert(item, position);
        adopt(item);
    }

    /**
     * Checks if dropping is enabled.<p>
     *
     * @return <code>true</code> if dropping is enabled
     */
    public boolean isDropEnabled() {

        if (m_dropEnabled != null) {
            return m_dropEnabled.booleanValue();
        }
        CmsTree<?> tree = getTree();
        if (tree == null) {
            return false;
        }
        return tree.isDropEnabled();
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
     * Positions the drag and drop placeholder as a sibling or descendant of this element.<p> 
     * 
     * @param x the cursor client x position
     * @param y the cursor client y position
     * @param placeholder the placeholder
     * @param orientation the drag and drop orientation
     * 
     * @return the placeholder index
     */
    public int repositionPlaceholder(int x, int y, Element placeholder, Orientation orientation) {

        I_CmsDraggable draggable = null;
        if (getTree().getDnDHandler() != null) {
            draggable = getTree().getDnDHandler().getDraggable();
            //            if (draggable == this) {
            //                // can't drop item on itself, keeping previous position
            //                return getTree().getPlaceholderIndex();
            //            }
        }
        Element itemElement = getListItemWidget().getElement();
        // check if the mouse pointer is within the height of the element 
        int top = CmsDomUtil.getRelativeY(y, itemElement);
        int height = itemElement.getOffsetHeight();
        int index;
        String parentPath;
        boolean isParentDndEnabled;
        CmsTreeItem parentItem = getParentItem();
        if (parentItem == null) {
            index = getTree().getItemPosition(this);
            parentPath = "/";
            isParentDndEnabled = getTree().isRootDropEnabled();
        } else {
            index = parentItem.getItemPosition(this);
            parentPath = getParentItem().getPath();
            isParentDndEnabled = getParentItem().isDropEnabled();
        }

        if (top < height) {
            // the mouse pointer is within the widget
            int diff = x - getListItemWidget().getAbsoluteLeft();
            if ((draggable != this) && isDropEnabled() && (diff > 0) && (diff < 32)) {
                // over icon
                getTree().setOpenTimer(this);
                m_children.getElement().insertBefore(placeholder, m_children.getElement().getFirstChild());
                getTree().setPlaceholderPath(getPath());
                return 0;
            }
            getTree().cancelOpenTimer();

            // In this case try to drop on the parent
            if (!isParentDndEnabled) {
                // we are not allowed to drop here
                // keeping old position
                return getTree().getPlaceholderIndex();
            }
            int originalPathLevel = -1;
            if (draggable instanceof CmsTreeItem) {
                originalPathLevel = getPathLevel(((CmsTreeItem)draggable).getPath()) - 1;
            }
            if (shouldInsertIntoSiblingList(originalPathLevel, parentItem, index)) {
                @SuppressWarnings("null")
                CmsTreeItem previousSibling = parentItem.getChild(index - 1);
                if (previousSibling.isOpen()) {
                    // insert as last into the last opened of the siblings tree fragment
                    return CmsTreeItem.getLastOpenedItem(previousSibling, originalPathLevel, true).insertPlaceholderAsLastChild(
                        placeholder);
                }
            }
            // insert place holder at the parent before the current item
            getElement().getParentElement().insertBefore(placeholder, getElement());
            getTree().setPlaceholderPath(parentPath);
            return index;
        } else if ((draggable != this) && isOpen()) {
            getTree().cancelOpenTimer();
            // the mouse pointer is on children
            for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
                CmsTreeItem child = getChild(childIndex);
                Element childElement = child.getElement();

                boolean over = false;
                switch (orientation) {
                    case HORIZONTAL:
                        over = CmsDomUtil.checkPositionInside(childElement, x, -1);
                        break;
                    case VERTICAL:
                        over = CmsDomUtil.checkPositionInside(childElement, -1, y);
                        break;
                    case ALL:
                    default:
                        over = CmsDomUtil.checkPositionInside(childElement, x, y);
                }
                if (over) {
                    return child.repositionPlaceholder(x, y, placeholder, orientation);
                }
            }
        }
        getTree().cancelOpenTimer();
        // keeping old position
        return getTree().getPlaceholderIndex();
    }

    /**
     * Enables/disables dropping.<p>
     *
     * @param enabled <code>true</code> to enable, or <code>false</code> to disable
     */
    public void setDropEnabled(boolean enabled) {

        if ((m_dropEnabled != null) && (m_dropEnabled.booleanValue() == enabled)) {
            return;
        }
        m_dropEnabled = Boolean.valueOf(enabled);
    }

    /**
     * Sets the tree item style to leaf, hiding the list opener.<p>
     * 
     * @param isLeaf <code>true</code> to set to leaf style
     */
    public void setLeafStyle(boolean isLeaf) {

        if (isLeaf) {
            m_leafStyleVar.setValue(CSS.listTreeItemLeaf());
        } else {
            m_leafStyleVar.setValue(CSS.listTreeItemInternal());
        }
    }

    /**
     * Opens or closes this tree item (i.e. shows or hides its descendants).<p>
     * 
     * @param open if <code>true</code>, open the tree item, else close it
     */
    public void setOpen(boolean open) {

        if (m_open == open) {
            return;
        }
        m_open = open;
        executeOpen();

        //        if ((m_tree != null) && m_tree.isAnimationEnabled()) {
        //            Command openCallback = new Command() {
        //
        //                /**
        //                 * @see com.google.gwt.user.client.Command#execute()
        //                 */
        //                public void execute() {
        //
        //                    executeOpen();
        //                }
        //            };
        //            if (m_open) {
        //                CmsSlideAnimation.slideIn(m_children.getElement(), openCallback, ANIMATION_DURATION);
        //            } else {
        //                CmsSlideAnimation.slideOut(m_children.getElement(), openCallback, ANIMATION_DURATION);
        //            }
        //        } else {
        //            executeOpen();
        //        }
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
     * Shows the open/close icons for this tree item and its descendants.<p>
     */
    public void showOpeners() {

        removeStyleName(CSS.listTreeItemNoOpeners());
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
    protected CmsToggleButton createOpener() {

        final CmsToggleButton opener = new CmsToggleButton();
        opener.setStyleName(CSS.listTreeItemOpener());
        opener.setUpFace("", CSS.plus());
        opener.setDownFace("", CSS.minus());
        opener.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent e) {

                setOpen(opener.isDown());
                e.stopPropagation();
                e.preventDefault();
            }
        });
        return opener;
    }

    /**
     * Executes the open call.<p>
     */
    protected void executeOpen() {

        m_styleVar.setValue(m_open ? CSS.listTreeItemOpen() : CSS.listTreeItemClosed());
        m_children.getElement().getStyle().clearDisplay();
        if (m_opener.isDown() != m_open) {
            m_opener.setDown(m_open);
        }
        if (m_open) {
            fireOpen();
        }
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

        return new Image(I_CmsImageBundle.INSTANCE.minusImage());
    }

    /**
     * The '+' image.<p>
     * 
     * @return the plus image 
     */
    protected Image getPlusImage() {

        return new Image(I_CmsImageBundle.INSTANCE.plusImage());
    }

    /**
     * Inserts the placeholder element as last child of the children list.
     * Setting it's path as the current placeholder path and returning the new index.<p>
     * 
     * @param placeholder the placeholder element
     * 
     * @return the new index
     */
    protected int insertPlaceholderAsLastChild(Element placeholder) {

        m_children.getElement().appendChild(placeholder);
        getTree().setPlaceholderPath(getPath());
        return getChildCount();
    }

    /**
     * Helper method which is called when the list of children changes.<p> 
     */
    protected void onChangeChildren() {

        int count = getChildCount();
        setLeafStyle(count == 0);
    }

    /**
     * Determines if the draggable should be inserted into the previous siblings children list.<p>
     * 
     * @param originalPathLevel the original path level
     * @param parent the parent item
     * @param index the current index
     * 
     * @return <code>true</code> if the item should be inserted into the previous siblings children list
     */
    private boolean shouldInsertIntoSiblingList(int originalPathLevel, CmsTreeItem parent, int index) {

        if ((index <= 0) || (parent == null)) {
            return false;
        }
        return originalPathLevel != getPathLevel(parent.getPath());
    }
}
