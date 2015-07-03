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

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Resource;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.UI;

public class CmsHomeView extends CssLayout implements View, I_CmsAppButtonProvider {

    public CmsHomeView() {

        Responsive.makeResponsive(this);
        addStyleName("opencms-responsive");
        addComponent(new CmsToolBar());

        CmsObject cms = A_CmsUI.getCmsObject();
        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        List<I_CmsWorkplaceAppConfiguration> visibleApps = Lists.newArrayList();
        for (I_CmsWorkplaceAppConfiguration appConfig : OpenCms.getWorkplaceAppManager().getWorkplaceApps()) {
            CmsAppVisibilityStatus status = appConfig.getVisibility(cms);
            if (status.isVisible()) {
                visibleApps.add(appConfig);
            }
        }
        CmsAppHierarchyBuilder hierarchyBuilder = new CmsAppHierarchyBuilder();
        for (I_CmsWorkplaceAppConfiguration app : visibleApps) {
            hierarchyBuilder.addAppConfiguration(app);
        }
        for (CmsAppCategory category : OpenCms.getWorkplaceAppManager().getCategories()) {
            hierarchyBuilder.addCategory(category);
        }

        CmsAppHierarchyPanel hierarchyPanel = new CmsAppHierarchyPanel(this);
        hierarchyPanel.fill(hierarchyBuilder.buildHierarchy(), locale);
        addComponent(hierarchyPanel);
        setWidth("100%");
    }

    public static Component createAppIconWidget(final I_CmsWorkplaceAppConfiguration appConfig, Locale locale) {

        Button button = new Button(appConfig.getName(locale));
        button.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                CmsAppWorkplaceUi ui = (CmsAppWorkplaceUi)A_CmsUI.get();
                ui.showApp(appConfig);
            }
        });
        Resource icon = appConfig.getIcon();
        button.setIcon(icon, appConfig.getName(locale));
        button.addStyleName("app");
        button.addStyleName("borderless");
        button.addStyleName("icon-align-top");
        String helpText = appConfig.getHelpText(locale);
        button.setDescription(helpText);
        return button;
    }

    public Component createAppButton(I_CmsWorkplaceAppConfiguration appConfig) {

        return createAppIconWidget(appConfig, UI.getCurrent().getLocale());
    }

    public void enter(ViewChangeEvent event) {

    }
}
