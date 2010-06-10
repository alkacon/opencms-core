/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/draganddrop/Attic/A_CmsSortingDragHandler.java,v $
 * Date   : $Date: 2010/06/10 12:56:28 $
 * Version: $Revision: 1.2 $
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

import java.util.Iterator;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Abstract drag and drop handler implementation covering the most part off a drag and drop process.<p>
 * 
 * @param <E> the draggable element type
 * @param <T> the drag target type
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public abstract class A_CmsSortingDragHandler<E extends I_CmsDragElement<T>, T extends I_CmsSortableDragTarget>
extends A_CmsDragHandler<E, T> {

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsDragHandler#sortTarget()
     */
    @Override
    protected void sortTarget() {

        Iterator<Widget> it = m_currentTarget.iterator();
        while (it.hasNext()) {
            Widget child = it.next();
            Element element = child.getElement();

            String positioning = element.getStyle().getPosition();
            if ((positioning.equals(Position.ABSOLUTE.getCssName()) || positioning.equals(Position.FIXED.getCssName()))
                || !child.isVisible()
                || (m_placeholder == child)) {
                // only take visible and not 'position:absolute' elements into account, also ignore the place-holder
                continue;
            }

            // check if the mouse pointer is within the width of the element 
            int left = getRelativeX(element);
            if ((left <= 0) || (left >= element.getOffsetWidth())) {
                continue;
            }

            // check if the mouse pointer is within the height of the element 
            int top = getRelativeY(element);
            int height = element.getOffsetHeight();
            if ((top <= 0) || (top >= height)) {
                continue;
            }

            int index = m_currentTarget.getWidgetIndex(child);

            // check if the mouse pointer is within the upper half of the element,
            // only act if the place-holder index has to be changed
            if (top < height / 2) {
                if (m_currentTarget.getWidgetIndex(m_placeholder) != index) {
                    m_currentTarget.insert(m_placeholder, index);
                    targetSortChangeAction();
                }
            } else {
                if (m_currentTarget.getWidgetIndex(m_placeholder) != index + 1) {
                    m_currentTarget.insert(m_placeholder, index + 1);
                    targetSortChangeAction();
                }
            }
            return;
        }
    }
}
