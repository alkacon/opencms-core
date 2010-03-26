/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/draganddrop/Attic/I_CmsDragTarget.java,v $
 * Date   : $Date: 2010/03/26 09:14:40 $
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

package org.opencms.gwt.client.draganddrop;

import org.opencms.gwt.client.util.CmsPositionBean;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Interface defining all methods needed for a drag and drop target. These will mostly be called by the drag and drop handler.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public interface I_CmsDragTarget extends HasWidgets, IndexedPanel, InsertPanel {

    /**
     * The root element of this widget.<p>
     * 
     * @return the element
     */
    Element getElement();

    /**
     * Returns an position info bean.<p>
     * 
     * @return the element position data
     */
    CmsPositionBean getPositionInfo();

    /**
     * Method called by the drag handler when an element is dragged into the drag target.<p>
     * 
     * @param handler the handler instance
     */
    void onDragEnter(I_CmsDragHandler<?, ?> handler);

    /**
     * Method called by the drag handler when an element is dragged inside the drag target.<p>
     * 
     * @param handler the handler instance
     */
    void onDragInside(I_CmsDragHandler<?, ?> handler);

    /**
     * Method called by the drag handler when an element is dragged out of the drag target.<p>
     * 
     * @param handler the handler instance
     */
    void onDragLeave(I_CmsDragHandler<?, ?> handler);

    /**
     * Method called by the drag handler when an element is dropped onto the drag target.<p>
     * 
     * @param handler the handler instance
     */
    void onDrop(I_CmsDragHandler<?, ?> handler);

    /**
     * Sets the position of the specified child widget.<p>
     * 
     * @see com.google.gwt.user.client.ui.AbsolutePanel#setWidgetPosition(Widget w, int left, int top)
     * 
     * @param w the widget to position
     * @param left position left
     * @param top position top
     */
    void setWidgetPosition(Widget w, int left, int top);
}
