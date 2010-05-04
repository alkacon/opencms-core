/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/draganddrop/Attic/I_CmsDragTarget.java,v $
 * Date   : $Date: 2010/05/04 13:40:56 $
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
 * @version $Revision: 1.4 $
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
     * Inserts a child widget at the specified position before the specified
     * index. Setting a position of <code>(-1, -1)</code> will cause the child
     * widget to be positioned statically. If the widget is already a child of
     * this panel, it will be moved to the specified index.
     * 
     * @param w the child widget to be inserted
     * @param left the widget's left position
     * @param top the widget's top position
     * @param beforeIndex the index before which it will be inserted
     * 
     * @throws IndexOutOfBoundsException if <code>beforeIndex</code> is out of
     *           range
     *
     * @see com.google.gwt.user.client.ui.AbsolutePanel#insert(Widget, int, int, int)
     */
    void insert(Widget w, int left, int top, int beforeIndex);
}
