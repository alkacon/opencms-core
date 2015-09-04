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
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;

import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

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
     * Replaces the ui content with a single dialog.<p>
     *
     * TODO: In the future this should only handle window creation, refactor dialog contents to CmsBasicDialog
     *
     * @param caption the caption
     * @param component the dialog content
     */
    public void setContentToDialog(String caption, Component component) {

        setContent(new Label());
        Window window = CmsBasicDialog.prepareWindow(DialogWidth.narrow);
        CmsBasicDialog dialog = new CmsBasicDialog();
        VerticalLayout result = new VerticalLayout();
        dialog.setContent(result);
        window.setContent(dialog);
        window.setCaption(caption);
        window.setClosable(false);
        addWindow(window);
        window.center();
        if (component instanceof I_CmsHasButtons) {
            I_CmsHasButtons hasButtons = (I_CmsHasButtons)component;
            for (Button button : hasButtons.getButtons()) {
                dialog.addButton(button);
            }

        }
        result.addComponent(component);

    }

    /**
     * Displays an error message in a centered box.<p>
     *
     * @param error the error message to display
     */
    public void setError(String error) {

        setContentToDialog("Error", new Label(error));
    }
}
