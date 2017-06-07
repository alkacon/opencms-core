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

package org.opencms.gwt.client.ui.tree;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.input.CmsCheckBox;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Tree item for lazily loaded list trees.<p>
 *
 * @since 8.0.0
 */
public class CmsLazyTreeItem extends CmsTreeItem {

    /** Enum for indicating the load state of a tree item. */
    public enum LoadState {

        /** children have been loaded. */
        LOADED,

        /** loading children. */
        LOADING,

        /** children haven't been loaded. */
        UNLOADED;
    }

    /**
     * Helper tree item which displays a "loading" message.<p>
     */
    protected class LoadingItem extends CmsTreeItem {

        /**
         * Constructs a new instance.<p>
         */
        public LoadingItem() {

            super(true, new Label(Messages.get().key(Messages.GUI_LOADING_0)));
        }
    }

    /** The loading item. */
    private LoadingItem m_loadingItem = new LoadingItem();

    /** The load state of this tree item. */
    private LoadState m_loadState = LoadState.UNLOADED;

    /** Flag to show a load item while children are being loaded. */
    private boolean m_useLoadItem;

    /**
     * Constructs a new lazy tree item with a main widget and a check box.<p>
     *
     * @param checkbox the check box
     * @param widget the main widget
     * @param useLoadItem <code>true</code> to show a load item while children are being loaded
     */
    public CmsLazyTreeItem(CmsCheckBox checkbox, Widget widget, boolean useLoadItem) {

        super(true, checkbox, widget);
        m_useLoadItem = useLoadItem;
    }

    /**
     * Constructs a new lazy tree item with a main widget.<p>
     *
     * @param widget the main widget
     * @param useLoadItem <code>true</code> to show a load item while children are being loaded
     */
    public CmsLazyTreeItem(Widget widget, boolean useLoadItem) {

        super(true, widget);
        m_useLoadItem = useLoadItem;
    }

    /**
     * Gets the load state of the tree item.<p>
     *
     * @return a load state
     */
    public LoadState getLoadState() {

        return m_loadState;
    }

    /**
     * Returns if tree item children have been loaded.<p>
     *
     * @return <code>true</code> if tree item children have been loaded
     */
    public boolean isLoaded() {

        return LoadState.LOADED == m_loadState;
    }

    /**
     * This method should be called when the item's children have finished loading.<p>
     */
    public void onFinishLoading() {

        m_loadState = LoadState.LOADED;
        if (m_useLoadItem) {
            m_loadingItem.removeFromParent();
        }
        onChangeChildren();
    }

    /**
     * This method is called when the tree item's children start being loaded.<p>
     */
    public void onStartLoading() {

        m_loadState = LoadState.LOADING;
        if (m_useLoadItem) {
            addChild(m_loadingItem);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.tree.CmsTreeItem#onChangeChildren()
     */
    @Override
    protected void onChangeChildren() {

        if (m_loadState == LoadState.LOADED) {
            super.onChangeChildren();
        }
    }

}
