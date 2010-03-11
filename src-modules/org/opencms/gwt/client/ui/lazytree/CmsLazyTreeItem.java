/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/lazytree/Attic/CmsLazyTreeItem.java,v $
 * Date   : $Date: 2010/03/11 11:26:08 $
 * Version: $Revision: 1.1 $
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

import org.opencms.gwt.client.Messages;

import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

/**
 * Lazy tree item implementation.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.client.ui.lazytree.CmsLazyTree
 */
public class CmsLazyTreeItem extends TreeItem {

    /**
     * Marker for not yet loaded nodes.<p>
     */
    private static class LoadingTreeItem extends TreeItem {

        /**
         * Default constructor.<p>
         */
        public LoadingTreeItem() {

            super(Messages.get().key(Messages.GUI_LOADING_0));
        }
    }

    /** Loading flag. */
    private boolean m_loading;

    /**
     * Creates an empty tree item.
     */
    public CmsLazyTreeItem() {

        super();
        addItem(new LoadingTreeItem());
    }

    /**
     * Constructs a tree item with the given HTML.
     * 
     * @param html the item's HTML
     */
    public CmsLazyTreeItem(String html) {

        this();
        setHTML(html);
    }

    /**
     * Constructs a tree item with the given <code>Widget</code>.
     * 
     * @param widget the item's widget
     */
    public CmsLazyTreeItem(Widget widget) {

        this();
        setWidget(widget);
    }

    /**
     * Checks if already loaded or not.<p>
     * 
     * @return <code>true</code> if already loaded
     */
    public boolean isLoaded() {

        return ((getChildCount() != 1) || !(getChild(0) instanceof LoadingTreeItem));
    }

    /**
     * Returns the loading flag.<p>
     *
     * @return the loading flag
     */
    public boolean isLoading() {

        return m_loading;
    }

    /**
     * Sets the loading flag.<p>
     * 
     * @param loading the flag to set
     */
    public void setLoading(boolean loading) {

        m_loading = loading;
    }
}
