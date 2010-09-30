/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/I_CmsDropContainer.java,v $
 * Date   : $Date: 2010/09/30 13:32:25 $
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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.gwt.client.dnd.I_CmsDropTarget;

import com.google.gwt.user.client.ui.Widget;

/**
 * Interface for container page drop targets like containers and sub-containers.<p>
 */
public interface I_CmsDropContainer extends I_CmsDropTarget {

    /**
     * Adds a new child widget.<p>
     * 
     * @param w the widget
     * 
     * @see com.google.gwt.user.client.ui.HasWidgets#add(com.google.gwt.user.client.ui.Widget)
     */
    void add(Widget w);

    /**
     * Returns the container id.<p>
     *
     * @return the container id
     */
    String getContainerId();

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
     * Puts a highlighting border around the container content.<p>
     */
    void highlightContainer();

    /**
     * Inserts a child widget before the specified index.
     * If the widget is already a child of this panel, it will be moved to the specified index.<p>
     * 
     * @param w the new child
     * @param beforeIndex the before index
     */
    void insert(Widget w, int beforeIndex);

    /**
     * Refreshes position and dimension of the highlighting border. Call when anything changed during the drag process.<p>
     */
    void refreshHighlighting();

    /**
     * Removes the highlighting border.<p>
     */
    void removeHighlighting();

}
