/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsList.java,v $
 * Date   : $Date: 2010/03/18 09:31:16 $
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsListTreeCss;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A very basic list implementation to hold {@link CmsListItemWidget}.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public class CmsList extends ComplexPanel {

    // TODO: add sorting functions

    /** The css bundle used for this widget. */
    private static final I_CmsListTreeCss CSS = I_CmsLayoutBundle.INSTANCE.listTreeCss();

    /** The list panel. */
    //    protected HTMLPanel m_list;

    /**
     * Constructor.<p>
     */
    public CmsList() {

        setElement(DOM.createElement("ul"));
        setStyleName(CSS.listTreeItemChildren());
    }

    static {
        CSS.ensureInjected();
    }

    /**
     * @see com.google.gwt.user.client.ui.Panel#add(com.google.gwt.user.client.ui.Widget)
     */
    public void add(Widget widget) {

        assert widget instanceof CmsListItem;
        super.add(widget, this.getElement());
    }

    /**
     * Adds an item to the list.<p>
     * 
     * @param item the item to add
     */
    public void addItem(CmsListItem item) {

        add(item);
    }

    /**
     * Clears the list.<p>
     */
    public void clearList() {

        this.clear();
    }

    /**
     * Removes an item from the list.<p>
     * 
     * @param item the item to remove
     */
    public void removeItem(CmsListItem item) {

        this.remove(item);
    }
}
