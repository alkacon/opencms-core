/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsList.java,v $
 * Date   : $Date: 2010/03/09 15:59:01 $
 * Version: $Revision: 1.3 $
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

package org.opencms.gwt.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * A very basic list implementation to hold {@link CmsListItem}.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsList extends Composite {

    // TODO: add sorting functions

    /** The list panel. */
    protected HTMLPanel m_list;

    /** The list panel root element id. */
    private String m_elementId;

    /**
     * Constructor.<p>
     */
    public CmsList() {

        m_list = new HTMLPanel("ul", "");
        m_elementId = HTMLPanel.createUniqueId();
        m_list.getElement().setId(m_elementId);
        initWidget(m_list);
    }

    /**
     * Adds an item to the list.<p>
     * 
     * @param item the item to add
     */
    public void addItem(CmsListItem item) {

        m_list.add(item, m_elementId);
    }

    /**
     * Clears the list.<p>
     */
    public void clearList() {

        m_list.clear();
    }

    /**
     * Removes an item from the list.<p>
     * 
     * @param item the item to remove
     */
    public void removeItem(CmsListItem item) {

        m_list.remove(item);
    }
}
