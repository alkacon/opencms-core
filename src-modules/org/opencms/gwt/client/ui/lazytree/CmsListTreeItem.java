/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/lazytree/Attic/CmsListTreeItem.java,v $
 * Date   : $Date: 2010/03/22 16:16:02 $
 * Version: $Revision: 1.5 $
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

package org.opencms.gwt.client.ui.lazytree;

import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsSimpleListItem;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsListTreeCss;
import org.opencms.gwt.client.util.CmsStyleVariable;

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
 * @author Michael Moossen
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 8.0.0
 * 
 */
public class CmsListTreeItem extends CmsSimpleListItem {

    /** The CSS bundle used for this widget. */
    private static final I_CmsListTreeCss CSS = I_CmsLayoutBundle.INSTANCE.listTreeCss();

    /** The children list. */
    protected CmsList m_children;

    /** The element showing the open/close icon. */
    protected ToggleButton m_opener;

    /** The style variable controlling this tree item's leaf/non-leaf state. */
    private CmsStyleVariable m_leafStyleVar;

    /** The style variable controlling this tree item's open/closed state. */
    private CmsStyleVariable m_styleVar;

    private CmsListTree m_tree;

    /** 
     * Default constructor.<p>
     */
    public CmsListTreeItem() {

        super();
    }

    /**
     * Creates a new list tree item containing several widgets.<p>
     * 
     * @param showOpeners if true, show open/close icons
     * @param content the widgets to put into the tree item 
     */
    public CmsListTreeItem(boolean showOpeners, Widget... content) {

        this();
        if (content.length > 0) {
            // put all but the last widget into the float section
            for (int i = 0; i < content.length - 1; i++) {
                m_content.addToFloat(content[i]);
            }
            m_content.add(content[content.length - 1]);
        }
        if (!showOpeners) {
            hideOpeners();
        }
    }

    static {
        CSS.ensureInjected();
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsListItem#add(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public void add(Widget w) {

        m_content.add(w);
    }

    /**
     * Adds a child list item.<p>
     * 
     * @param item the child to add
     */
    public void addChild(CmsListItem item) {

        m_children.addItem(item);
        if (item instanceof CmsListTreeItem) {
            ((CmsListTreeItem)item).setTree(m_tree);
        }
        onChangeChildren();

    }

    /**
     * Removes all children.<p>
     */
    public void clearChildren() {

        m_children.clearList();
        onChangeChildren();
    }

    /**
     * Gets the tree to which this tree item belongs, or null if it does not belong to a tree.<p>
     * 
     * @return a tree or null
     */
    public CmsListTree getTree() {

        return m_tree;
    }

    /** 
     * Hides the open/close icons for this tree item and its descendants.<p>
     */
    public void hideOpeners() {

        addStyleName(CSS.listTreeItemNoOpeners());
    }

    /**
     * Removes an item from the list.<p>
     * 
     * @param item the item to remove
     */
    public void removeChild(CmsListItem item) {

        m_children.removeItem(item);
        onChangeChildren();
    }

    /**
     * Opens or closes this tree item (i.e. shows or hides its descendants).<p>
     * 
     * @param open if true, open the tree item, else close it
     */
    public void setOpen(boolean open) {

        m_styleVar.setValue(open ? CSS.listTreeItemOpen() : CSS.listTreeItemClosed());
        m_opener.setDown(open);
        if (open) {
            for (Widget widget : m_children) {
                ((CmsListItem)widget).updateLayout();
            }
            fireOpen();
        }
    }

    /**
     * Sets the tree to which this tree item belongs.<p>
     * 
     * This is automatically called when this tree item or one of its ancestors is inserted into a tree.
     * 
     * @param tree the tree into which the item has been inserted
     * 
     */
    public void setTree(CmsListTree tree) {

        m_tree = tree;
        for (Widget widget : m_children) {
            if (widget instanceof CmsListTreeItem) {
                ((CmsListTreeItem)widget).setTree(tree);
            }
        }
    }

    /**
     * Initializes this widget.<p>
     * 
     */
    @Override
    protected void init() {

        super.init();
        m_styleVar = new CmsStyleVariable(this);
        m_leafStyleVar = new CmsStyleVariable(this);
        m_opener = createOpener();
        m_content.addToFloat(m_opener);
        m_children = new CmsList();
        m_children.setStyleName(CSS.listTreeItemChildren());
        m_panel.add(m_children);
        onChangeChildren();
        setOpen(false);
    }

    /**
     * Creates the button for opening/closing this item.<p>
     * 
     * @return a button
     */
    private ToggleButton createOpener() {

        final ToggleButton opener = new ToggleButton();
        opener.setStyleName(CSS.listTreeItemHandler());
        opener.getUpFace().setImage(new Image(I_CmsImageBundle.INSTANCE.plus()));
        opener.getDownFace().setImage(new Image(I_CmsImageBundle.INSTANCE.minus()));
        opener.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent e) {

                setOpen(opener.isDown());
            }
        });
        return opener;
    }

    /** Fires the open event on the tree.<p> */
    private void fireOpen() {

        if (m_tree != null) {
            m_tree.fireOpen(this);
        }
    }

    /**
     * Helper method which gets the number of children.<p>
     * 
     * @return the number of children
     */
    private int getChildCount() {

        return m_children.getWidgetCount();
    }

    /**
     * Helper method which is called when the list of children changes.<p> 
     */
    private void onChangeChildren() {

        int count = getChildCount();
        m_leafStyleVar.setValue(count == 0 ? CSS.listTreeItemLeaf() : CSS.listTreeItemInternal());
    }

}
