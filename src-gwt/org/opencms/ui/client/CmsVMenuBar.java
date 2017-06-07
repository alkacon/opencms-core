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

package org.opencms.ui.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.vaadin.client.ui.VMenuBar;

/**
 * Extending the VAADIN menu bar only to add a corner element pointing at the opening button.<p>
 */
public class CmsVMenuBar extends VMenuBar {

    /** The pointy corner CSS class. */
    protected static final String CORNER_CLASS = "o-toolbar-menu-corner";

    /** The additional offset. */
    private static final int OFFSET = -4;

    /**
     * Constructor.<p>
     */
    CmsVMenuBar() {
        super();
    }

    /**
     * Constructor.<p>
     *
     * @param submenu if this is a sub-menu
     * @param parentmenu the parent menu
     */
    CmsVMenuBar(boolean submenu, VMenuBar parentmenu) {
        super(submenu, parentmenu);
    }

    /**
     * @see com.vaadin.client.ui.VMenuBar#showChildMenuAt(com.vaadin.client.ui.VMenuBar.CustomMenuItem, int, int)
     */
    @Override
    protected void showChildMenuAt(CustomMenuItem item, int top, int left) {

        super.showChildMenuAt(item, top, left);
        if (!subMenu) {
            int popupLeft = popup.getPopupLeft();
            int dif = (left - popupLeft) + OFFSET;
            Element corner = Document.get().createDivElement();
            corner.setClassName(CORNER_CLASS);
            corner.getStyle().setLeft(dif, Unit.PX);
            popup.getElement().appendChild(corner);
        }
    }
}
