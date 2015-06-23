/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.apps;

import org.opencms.workplace.CmsWorkplace;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.declarative.Design;

/**
 * The workplace toolbar.<p>
 */
public class CmsToolBar extends CssLayout {

    /** The serial version id. */
    private static final long serialVersionUID = -4551194983054069395L;

    /** Toolbar items left. */
    private HorizontalLayout m_itemsLeft;

    /** Toolbar items right. */
    private HorizontalLayout m_itemsRight;

    /** OpenCms logo. */
    private Image m_logo;

    /**
     * Constructor.<p>
     */
    public CmsToolBar() {

        Design.read("ToolBar.html", this);
        m_logo.setSource(new ExternalResource(CmsWorkplace.getResourceUri("commons/login_logo.png")));
        m_itemsLeft.addComponent(new Button(FontAwesome.BOMB));
        m_itemsRight.addComponent(new Button(FontAwesome.BEER));

    }

}
