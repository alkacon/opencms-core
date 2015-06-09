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
import org.opencms.ui.I_CmsComponentFactory;
import org.opencms.ui.apps.CmsWorkplaceAppManager.NavigationState;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class CmsAppView implements View, I_CmsComponentFactory {

    class AppViewComponent extends CustomComponent {

        public AppViewComponent(I_CmsWorkplaceApp app) {

            HorizontalSplitPanel hp = new HorizontalSplitPanel();
            hp.setSplitPosition(300, Sizeable.UNITS_PIXELS);
            Component menu = createMenu();
            hp.setFirstComponent(menu);
            hp.setSecondComponent(app);

            setCompositionRoot(hp);
            hp.setHeight("100%");
            setHeight("100%");
        }
    }

    private I_CmsWorkplaceAppConfiguration m_appConfig;

    private I_CmsWorkplaceApp m_app;

    public CmsAppView(I_CmsWorkplaceAppConfiguration appConfig) {

        m_appConfig = appConfig;
    }

    public static CmsAppWorkplaceUi getWorkplaceUi() {

        CmsAppWorkplaceUi ui = (CmsAppWorkplaceUi)A_CmsUI.get();
        return ui;
    }

    public Component createComponent() {

        if (m_app == null) {
            m_app = m_appConfig.getAppInstance();
            return new AppViewComponent(m_app);
        }
        return null;
    }

    public void enter(ViewChangeEvent event) {

        String newState = event.getParameters();
        if (newState.startsWith(NavigationState.PARAM_SEPARATOR)) {
            newState = newState.substring(1);
        }
        m_app.onStateChange(newState);
    }

    protected Component createMenu() {

        VerticalLayout result = new VerticalLayout();
        result.addComponent(new Label("Menu"));
        Button homeButton = new Button("Home");
        result.addComponent(homeButton);
        homeButton.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                CmsAppWorkplaceUi ui = getWorkplaceUi();
                ui.showHome();
            }
        });
        return result;

    }
}
