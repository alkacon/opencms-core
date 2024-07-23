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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.dnd.I_CmsNestedDropTarget;
import org.opencms.gwt.client.util.CmsPositionBean;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for container page drop targets like containers and group-containers.<p>
 */
public interface I_CmsDropContainer extends I_CmsNestedDropTarget {

    /**
     * Adds a new child widget.<p>
     *
     * @param w the widget
     *
     * @see com.google.gwt.user.client.ui.HasWidgets#add(com.google.gwt.user.client.ui.Widget)
     */
    void add(Widget w);

    /**
     * Registers a child drop target.<p>
     *
     * @param child the child
     */
    void addDndChild(I_CmsDropTarget child);

    /**
     * Adopts a container-page element registering it as a child of this container.
     * Used for elements that are already child nodes of the container-element node in DOM.<p>
     *
     * @param containerElement the element to adopt
     */
    void adoptElement(CmsContainerPageElementPanel containerElement);

    /**
     * Checks the maximum number of allowed elements and hides overflowing elements.<p>
     */
    void checkMaxElementsOnEnter();

    /**
     * Checks the maximum number of allowed elements and displays formerly hidden elements.<p>
     */
    void checkMaxElementsOnLeave();

    /**
     * Clears the list of child drop targets.<p>
     */
    void clearDnDChildren();

    /**
     * Returns the container id.<p>
     *
     * @return the container id
     */
    String getContainerId();

    /**
     * Returns the current position info.<p>
     *
     * @return the position info
     */
    CmsPositionBean getPositionInfo();

    /**
     * Gets the number of child widgets in this panel.<p>
     *
     * @return the number of child widgets
     */
    int getWidgetCount();

    /**
     * Gets the index of the specified child widget.<p>
     *
     * @param w the widget
     *
     * @return the index
     */
    int getWidgetIndex(Widget w);

    /**
     * Hides list collector direct edit buttons, if present.<p>
     */
    void hideEditableListButtons();

    /**
     * Puts a highlighting border around the container content.<p>
     */
    void highlightContainer(boolean addSeparators);

    /**
     * Puts a highlighting border around the container content using the given dimensions.<p>
     *
     * @param positionInfo the highlighting position to use
     */
    void highlightContainer(CmsPositionBean positionInfo, boolean addSeparators);

    /**
     * Inserts a child widget before the specified index.
     * If the widget is already a child of this panel, it will be moved to the specified index.<p>
     *
     * @param w the new child
     * @param beforeIndex the before index
     */
    void insert(Widget w, int beforeIndex);

    /**
     * Returns <code>true</code> if this container is a detail view only container.<p>
     *
     * @return <code>true</code> if this container is a detail view only container
     */
    boolean isDetailOnly();

    /**
     * Returns <code>true</code> if this container is being currently used to display a detail view.<p>
     *
     * @return <code>true</code> if this container is used to display a detail view
     */
    boolean isDetailView();

    /**
     * Returns if the container is editable by the current user.<p>
     *
     * @return <code>true</code> if the container is editable by the current user
     */
    boolean isEditable();

    /**
     * This is called when the elements of this container/group have been processed into CmsContainerPageElementPanels.<p>
     *
     * @param children the processed children
     */
    void onConsumeChildren(List<CmsContainerPageElementPanel> children);

    /**
     * Refreshes position and dimension of the highlighting border. Call when anything changed during the drag process.<p>
     */
    void refreshHighlighting();

    /**
     * Refreshes position and dimension of the highlighting border. Call when anything changed during the drag process.<p>
     *
     * @param positionInfo the position info to use
     */
    void refreshHighlighting(CmsPositionBean positionInfo);

    /**
     * Removes the highlighting border.<p>
     */
    void removeHighlighting();

    /**
     * Sets the placeholder visibility.<p>
     *
     * @param visible <code>true</code> to set the place holder visible
     */
    void setPlaceholderVisibility(boolean visible);

    /**
     * Shows list collector direct edit buttons (old direct edit style), if present.<p>
     */
    void showEditableListButtons();

    /**
     * Updates the cached position info.<p>
     */
    void updatePositionInfo();

}
