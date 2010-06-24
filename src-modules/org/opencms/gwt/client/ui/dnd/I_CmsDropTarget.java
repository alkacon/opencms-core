/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/dnd/Attic/I_CmsDropTarget.java,v $
 * Date   : $Date: 2010/06/24 09:05:26 $
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

package org.opencms.gwt.client.ui.dnd;

import com.google.gwt.dom.client.Element;

/**
 * The drag target widget.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $
 *  
 * @since 8.0.0 
 */
public interface I_CmsDropTarget {

    /**
     * Checks if this target activates on the given the dragging coordinates.<p>
     * 
     * @param x the horizontal position
     * @param y the vertical position
     * 
     * @return <code>true</code> if this target activates, or <code>false</code> if not 
     */
    boolean check(int x, int y);

    /**
     * Returns this widget's element.<p>
     * 
     * @return this widget's element
     * 
     * @see com.google.gwt.user.client.ui.UIObject#getElement()
     */
    Element getElement();

    /**
     * Will be executed just before the drop event is fired.<p>
     */
    void onDrop();

    /**
     * Removes the current place holder.<p>
     */
    void removePlaceholder();

    /**
     * Inserts the place holder for the given draggable in the right position on this target given the dragging coordinates.<p>
     * 
     * The returned position of the place holder, should include all needed info to execute the drop action.<p>
     * 
     * @param x the horizontal position
     * @param y the vertical position
     * @param event the current tentative drop event
     * 
     * @return the place holder position
     */
    CmsDropPosition setPlaceholder(int x, int y, CmsDropEvent event);
}