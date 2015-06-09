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

import org.opencms.ui.A_CmsUI;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class CmsAppView extends CustomComponent implements View {

    private I_CmsWorkplaceAppConfiguration m_appConfig;

    public CmsAppView(I_CmsWorkplaceAppConfiguration appConfig) {

        m_appConfig = appConfig;
        HorizontalSplitPanel hp = new HorizontalSplitPanel();
        Component menu = createMenu();
        hp.setFirstComponent(menu);
        hp.setSecondComponent(appConfig.getAppInstance());

        setCompositionRoot(hp);
        hp.setHeight("100%");
        setHeight("100%");
    }

    public void enter(ViewChangeEvent event) {

    }

    protected Component createMenu() {

        VerticalLayout result = new VerticalLayout();
        result.addComponent(new Label("Menu"));
        Button homeButton = new Button("Home");
        result.addComponent(homeButton);
        homeButton.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                CmsAppWorkplaceUi ui = (CmsAppWorkplaceUi)A_CmsUI.get();
                ui.showHome();
            }
        });
        return result;

    }
}
