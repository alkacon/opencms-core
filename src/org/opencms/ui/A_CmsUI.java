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

package org.opencms.ui;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsUIServlet;

import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Abstract UI class providing access to the OpenCms context.<p>
 */
public abstract class A_CmsUI extends UI {

    /** Serial version id. */
    private static final long serialVersionUID = 989182479322461838L;

    /**
     * Returns the current UI.<p>
     *
     * @return the current UI
     */
    public static A_CmsUI get() {

        return (A_CmsUI)(UI.getCurrent());
    }

    /**
     * Returns the current cms context.<p>
     *
     * @return the current cms context
     */
    public static CmsObject getCmsObject() {

        return ((CmsUIServlet)VaadinServlet.getCurrent()).getCmsObject();
    }

    /**
     * Centers a panel in the middle of the browser window and returns the vertical layout used for the panel contents.<p>
     *
     * @param width width of the panel
     * @param height height of the panel
     * @param caption the caption
     * @return the layout used for panel contents
     */
    public VerticalLayout setCenterPanel(int width, int height, String caption) {

        Panel panel = new Panel();
        panel.setCaption(caption);
        VerticalLayout layout = new VerticalLayout();
        panel.setWidth("" + width + "px");
        panel.setHeight("" + height + "px");
        layout.setSizeFull();
        VerticalLayout panelContent = new VerticalLayout();
        panel.setContent(panelContent);
        panelContent.setMargin(true);
        layout.addComponent(panel);
        layout.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
        setContent(layout);
        return panelContent;
    }

    /**
     * Displays an error message in a centered box.<p>
     *
     * @param error the error message to display
     */
    public void setError(String error) {

        setCenterPanel(400, 300, "Error").addComponent(new Label(error));
    }
}
