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

package org.opencms.setup;

import org.opencms.ui.A_CmsUI;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Theme("opencms")
public class CmsSetupUI extends A_CmsUI  {

    private Window m_window;

    @Override
    protected void init(VaadinRequest request) {

        this.addStyleName("opencms");
        CmsBasicDialog dialog = new CmsBasicDialog();
        Label label = new Label("Hello world!");
        label.setWidth("500px");
        label.setHeight("300px");;
        dialog.setContent(label);
        Button button = new Button("OK");
        dialog.addButton(button);
        Window window = createWindow();
        window.setContent(dialog);
        addWindow(window);

    }

    private Window createWindow() {

        if (m_window != null) {
            m_window.close();
        }
        Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);
        window.setDraggable(false);
        window.setResizable(false);
        window.setClosable(false);
        m_window = window;
        return m_window;
    }

}
