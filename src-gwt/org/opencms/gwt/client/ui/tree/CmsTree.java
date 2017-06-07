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

import org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.logical.shared.HasOpenHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasAnimation;

/**
 * A tree of list items.<p>
 *
 * @param <I> the specific tree item implementation
 *
 * @since 8.0.0
 */
public class CmsTree<I extends CmsTreeItem> extends CmsList<I>
implements HasOpenHandlers<I>, HasCloseHandlers<I>, HasAnimation {

    /**
     * Timer to set sub item list visible.<p>
     */
    private class OpenTimer extends Timer {

        /** The tree item. */
        private CmsTreeItem m_item;

        /**
         * Constructor.<p>
         *
         * @param item the tree item
         */
        protected OpenTimer(CmsTreeItem item) {

            m_item = item;
        }

        /**
         * @see com.google.gwt.user.client.Timer#run()
         */
        @Override
        public void run() {

            m_item.setOpen(true);
            removeOpenTimer();
        }

        /**
         * Checks if the timer is running for the given tree item.<p>
         *
         * @param item the tree item to check
         * @return <code>true</code> if the given item matches the timer item
         */
        protected boolean checkTimer(CmsTreeItem item) {

            return item == m_item;
        }

    }

    /** The event bus for the tree. */
    protected SimpleEventBus m_eventBus;

    /** Flag to indicate is animations are enabled or not. */
    private boolean m_animate;

    /** The open timer if one is running. */
    private OpenTimer m_openTimer;

    /** The parent path of the current placeholder. */
    private String m_placeholderPath;

    /** Flag to indicate if dropping on root level is enabled or not. */
    private boolean m_rootDropEnabled;

    /**
     * Constructor.<p>
     */
    public CmsTree() {

        m_animate = false;
        m_eventBus = new SimpleEventBus();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasCloseHandlers#addCloseHandler(com.google.gwt.event.logical.shared.CloseHandler)
     */
    public HandlerRegistration addCloseHandler(CloseHandler<I> handler) {

        return m_eventBus.addHandlerToSource(CloseEvent.getType(), this, handler);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasOpenHandlers#addOpenHandler(com.google.gwt.event.logical.shared.OpenHandler)
     */
    public HandlerRegistration addOpenHandler(final OpenHandler<I> handler) {

        return m_eventBus.addHandlerToSource(OpenEvent.getType(), this, handler);
    }

    /**
     * Cancels the open timer if present.<p>
     */
    public void cancelOpenTimer() {

        if (m_openTimer != null) {
            m_openTimer.cancel();
            m_openTimer = null;
        }
    }

    /**
     * Closes all empty entries.<p>
     */
    public void closeAllEmpty() {

        CmsDebugLog.getInstance().printLine("closing all empty");
        int childCount = getWidgetCount();
        for (int index = 0; index < childCount; index++) {
            CmsTreeItem item = getItem(index);
            if (item.isOpen()) {
                item.closeAllEmptyChildren();
            }
        }
    }

    /**
     * Fires the close event for an item.<p>
     *
     * @param item the item for which to fire the close event
     */
    public void fireClose(I item) {

        CloseEvent.fire(this, item);
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#fireEvent(com.google.gwt.event.shared.GwtEvent)
     */
    @Override
    public void fireEvent(GwtEvent<?> event) {

        m_eventBus.fireEventFromSource(event, this);
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
     * Returns the placeholder path.<p>
     *
     * @return the path
     */
    public String getPlaceholderPath() {

        return m_placeholderPath;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasAnimation#isAnimationEnabled()
     */
    public boolean isAnimationEnabled() {

        return m_animate;
    }

    /**
     * Returns if dropping on root level is enabled or not.<p>
     *
     * @return <code>true</code> if dropping on root level is enabled
     */
    public boolean isRootDropEnabled() {

        return m_rootDropEnabled;
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsList#removePlaceholder()
     */
    @Override
    public void removePlaceholder() {

        super.removePlaceholder();
        m_placeholderPath = null;
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsList#repositionPlaceholder(int, int, Orientation)
     */
    @Override
    public void repositionPlaceholder(int x, int y, Orientation orientation) {

        int widgetCount = getWidgetCount();
        for (int index = 0; index < widgetCount; index++) {
            CmsTreeItem item = getItem(index);
            Element itemElement = item.getElement();
            boolean over = false;
            switch (orientation) {
                case HORIZONTAL:
                    over = CmsDomUtil.checkPositionInside(itemElement, x, -1);
                    break;
                case VERTICAL:
                    over = CmsDomUtil.checkPositionInside(itemElement, -1, y);
                    break;
                case ALL:
                default:
                    over = CmsDomUtil.checkPositionInside(itemElement, x, y);
            }

            if (over) {
                m_placeholderIndex = item.repositionPlaceholder(x, y, m_placeholder, orientation);
                return;
            }
            if (isDNDTakeAll() && (index == (widgetCount - 1))) {
                // last item of the list, no matching item was found and take-all is enabled
                // check if cursor position is above or below
                int relativeTop = CmsDomUtil.getRelativeY(y, getElement());
                int elementHeight = getElement().getOffsetHeight();
                if (relativeTop <= 0) {
                    if (isRootDropEnabled()) {
                        getElement().insertBefore(m_placeholder, getItem(0).getElement());
                        setPlaceholderPath("/");
                        m_placeholderIndex = 0;
                    }
                } else {
                    if (relativeTop > elementHeight) {
                        // insert as last into last opened tree-item
                        if (item.isOpen() && (item.getChildCount() > 0)) {
                            int originalPathLevel = -1;
                            if ((getDnDHandler() != null) && (getDnDHandler().getDraggable() instanceof CmsTreeItem)) {
                                originalPathLevel = CmsTreeItem.getPathLevel(
                                    ((CmsTreeItem)getDnDHandler().getDraggable()).getPath()) - 1;
                            }
                            // insert into the tree as last visible item
                            CmsTreeItem lastOpened = CmsTreeItem.getLastOpenedItem(item, originalPathLevel, true);
                            m_placeholderIndex = lastOpened.insertPlaceholderAsLastChild(m_placeholder);
                        } else if (isRootDropEnabled()) {
                            getElement().insertAfter(m_placeholder, itemElement);
                            setPlaceholderPath("/");
                            m_placeholderIndex = widgetCount;
                        }
                    }
                }
            }
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
     * Sets a timer to set a tree item open.<p>
     *
     * @param item the item to open
     */
    public void setOpenTimer(CmsTreeItem item) {

        if (item.isOpen()) {
            return;
        }
        if (m_openTimer != null) {
            if (m_openTimer.checkTimer(item)) {
                return;
            }
            m_openTimer.cancel();
        }
        m_openTimer = new OpenTimer(item);
        m_openTimer.schedule(100);
    }

    /**
     * Sets the drop on root enabled.<p>
     *
     * @param rootDropEnabled <code>true</code> to enable dropping on root level
     */
    public void setRootDropEnabled(boolean rootDropEnabled) {

        m_rootDropEnabled = rootDropEnabled;
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
     * Sets the timer reference to <code>null</code>.<p>
     */
    protected void removeOpenTimer() {

        m_openTimer = null;
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsList#setPlaceholder(com.google.gwt.dom.client.Element)
     */
    @Override
    protected void setPlaceholder(Element placeholder) {

        super.setPlaceholder(placeholder);
    }

    /**
     * Sets the placeholder path.<p>
     *
     * @param path the path
     */
    protected void setPlaceholderPath(String path) {

        m_placeholderPath = path;
    }
}
