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

import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsComponentFactory;
import org.opencms.ui.apps.CmsWorkplaceAppManager.NavigationState;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;

public class CmsAppWorkplaceUi extends A_CmsUI implements ViewDisplay, ViewProvider {

    private I_CmsWorkplaceAppConfiguration m_currentApp;

    public static final String VIEW_HOME = "home";

    public void changeCurrentAppState(String state) {

        String newFragment = "!"
            + getViewName(getPage().getUriFragment())
            + "/"
            + NavigationState.PARAM_SEPARATOR
            + state;
        getPage().setUriFragment(newFragment, false);
    }

    public View getView(String viewName) {

        if (viewName.startsWith(VIEW_HOME)) {
            return new CmsHomeView();
        } else {
            I_CmsWorkplaceAppConfiguration appConfig = OpenCms.getWorkplaceAppManager().getAppConfiguration(viewName);
            if (appConfig != null) {
                return new CmsAppView(appConfig);
            }
        }
        return null;
    }

    public String getViewName(String viewAndParameters) {

        NavigationState state = new NavigationState(viewAndParameters);
        return state.getViewName();
    }

    public void showApp(I_CmsWorkplaceAppConfiguration appConfig) {

        getNavigator().navigateTo(appConfig.getAppPath());
    }

    public void showHome() {

        getNavigator().navigateTo("home");
    }

    public void showView(View view) {

        Component component = null;
        if (view instanceof I_CmsComponentFactory) {
            component = ((I_CmsComponentFactory)view).createComponent();
        } else if (view instanceof Component) {
            component = (Component)view;
        }
        if (component != null) {
            setContent(component);
        }
    }

    @Override
    protected void init(VaadinRequest request) {

        OpenCms.getWorkplaceAppManager().loadApps();
        Navigator navigator = new Navigator(this, new Navigator.UriFragmentManager(getPage()), this);
        navigator.addProvider(this);
        showHome();
    }

}
