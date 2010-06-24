/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/dnd/Attic/CmsDropPosition.java,v $
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
import com.google.gwt.user.client.DOM;

/**
 * Drag'n drop drop position information.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public class CmsDropPosition {

    /** Some additional info, like the destination path, in a tree. */
    private String m_info;

    /** The drop animation's end left position. */
    private int m_left;

    /** The intended name to use for dropping. */
    private String m_name;

    /** The intended drop list position. */
    private int m_pos;

    /** The drop animation's end top position. */
    private int m_top;

    /**
     * Constructor.<p>
     * 
     * @param name the intended name to use for dropping
     * @param pos the intended drop list position
     * @param info some additional info, like the destination path in a tree
     * @param e the drop animation's end element
     */
    public CmsDropPosition(String name, int pos, String info, Element e) {

        m_name = name;
        m_pos = pos;
        m_info = info;
        m_top = DOM.getAbsoluteTop((com.google.gwt.user.client.Element)e);
        m_left = DOM.getAbsoluteLeft((com.google.gwt.user.client.Element)e);
    }

    /**
     * Returns the additional info.<p>
     *
     * @return the additional info
     */
    public String getInfo() {

        return m_info;
    }

    /**
     * Returns the drop animation's end left position.<p>
     * 
     * @return the drop animation's end left position
     */
    public int getLeft() {

        return m_left;
    }

    /**
     * Returns the intended drop name.<p>
     *
     * @return the intended drop name
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the intended drop position.<p>
     *
     * @return the intended drop position
     */
    public int getPosition() {

        return m_pos;
    }

    /**
     * Returns the drop animation's end top position.<p>
     * 
     * @return the drop animation's end top position
     */
    public int getTop() {

        return m_top;
    }

    /**
     * Sets the additional info.<p>
     *
     * @param info the additional info to set
     */
    public void setInfo(String info) {

        m_info = info;
    }

    /**
     * Sets the drop animation's end left position.<p>
     *
     * @param left the left position to set
     */
    public void setLeft(int left) {

        m_left = left;
    }

    /**
     * Sets the intended drop name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Sets the intended drop position.<p>
     *
     * @param pos the position to set
     */
    public void setPosition(int pos) {

        m_pos = pos;
    }

    /**
     * Sets the drop animation's end top position.<p>
     *
     * @param top the top position to set
     */
    public void setTop(int top) {

        m_top = top;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return m_pos + ":" + m_name + ":" + m_info;
    }
}
