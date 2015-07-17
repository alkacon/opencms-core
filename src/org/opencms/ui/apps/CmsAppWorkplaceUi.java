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
import org.opencms.ui.components.CmsScrollPositionCss;
import org.opencms.ui.components.CmsWindowCloseExtension;
import org.opencms.ui.components.I_CmsWindowCloseListener;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;

import com.vaadin.annotations.Theme;
import com.vaadin.navigator.NavigationStateManager;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

/**
 * The workplace ui.<p>
 */
@Theme("opencms")
public class CmsAppWorkplaceUi extends A_CmsUI
implements ViewDisplay, ViewProvider, ViewChangeListener, I_CmsWindowCloseListener {

    /** The serial version id. */
    private static final long serialVersionUID = -5606711048683809028L;

    /** The current view in case it implements view change listener. */
    private View m_currentView;

    /** The navigation state manager. */
    private NavigationStateManager m_navigationStateManager;

    /**
     * Constructor.<p>
     */
    public CmsAppWorkplaceUi() {
        CmsScrollPositionCss.addTo(this, 150, 50, OpenCmsTheme.SCROLLED);
    }

    /**
     * Gets the current UI instance.<p>
     *
     * @return the current UI instance
     */
    public static CmsAppWorkplaceUi get() {

        return (CmsAppWorkplaceUi)A_CmsUI.get();
    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#afterViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public void afterViewChange(ViewChangeEvent event) {

        if ((m_currentView != null) && (m_currentView instanceof ViewChangeListener)) {
            ((ViewChangeListener)m_currentView).afterViewChange(event);
        }
    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#beforeViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public boolean beforeViewChange(ViewChangeEvent event) {

        if ((m_currentView != null) && (m_currentView instanceof ViewChangeListener)) {
            return ((ViewChangeListener)m_currentView).beforeViewChange(event);
        }
        return true;
    }

    /**
     * Call to add a new browser history entry.<p>
     *
     * @param state the current app view state
     */
    public void changeCurrentAppState(String state) {

        String completeState = m_navigationStateManager.getState();
        String view = getViewName(completeState);
        String newCompleteState = view;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(state)) {
            newCompleteState += NavigationState.PARAM_SEPARATOR + state;
        }
        m_navigationStateManager.setState(newCompleteState);

    }

    /**
     * Returns the state parameter of the current app.<p>
     *
     * @return the state parameter of the current app
     */
    public String getAppState() {

        NavigationState state = new NavigationState(m_navigationStateManager.getState());
        return state.getParams();
    }

    /**
     * @see com.vaadin.navigator.ViewProvider#getView(java.lang.String)
     */
    public View getView(String viewName) {

        I_CmsWorkplaceAppConfiguration appConfig = OpenCms.getWorkplaceAppManager().getAppConfiguration(viewName);
        if (appConfig != null) {
            return new CmsAppView(appConfig);
        }

        return null;
    }

    /**
     * @see com.vaadin.navigator.ViewProvider#getViewName(java.lang.String)
     */
    public String getViewName(String viewAndParameters) {

        NavigationState state = new NavigationState(viewAndParameters);
        return state.getViewName();
    }

    /**
     * @see org.opencms.ui.components.I_CmsWindowCloseListener#onWindowClose()
     */
    public void onWindowClose() {

        if ((m_currentView != null) && (m_currentView instanceof I_CmsWindowCloseListener)) {
            ((I_CmsWindowCloseListener)m_currentView).onWindowClose();
        }
    }

    /**
     * Navigates to the given app.<p>
     *
     * @param appConfig the app configuration
     */
    public void showApp(I_CmsWorkplaceAppConfiguration appConfig) {

        getNavigator().navigateTo(appConfig.getId());
    }

    /**
     * Navigates to the home screen.<p>
     */
    public void showHome() {

        getNavigator().navigateTo(CmsAppHierarchyConfiguration.APP_ID);
    }

    /**
     * @see com.vaadin.navigator.ViewDisplay#showView(com.vaadin.navigator.View)
     */
    public void showView(View view) {

        // remove current component form the view change listeners
        m_currentView = view;
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

    /**
    * @see com.vaadin.ui.UI#init(com.vaadin.server.VaadinRequest)
    */
    @Override
    protected void init(VaadinRequest request) {

        m_navigationStateManager = new Navigator.UriFragmentManager(getPage());
        Navigator navigator = new Navigator(this, m_navigationStateManager, this);
        navigator.addProvider(this);
        setNavigator(navigator);
        String fragment = getPage().getUriFragment();
        Page.getCurrent().addBrowserWindowResizeListener(new BrowserWindowResizeListener() {

            private static final long serialVersionUID = 1L;

            public void browserWindowResized(BrowserWindowResizeEvent event) {

                markAsDirtyRecursive();
            }
        });
        CmsWindowCloseExtension windowClose = new CmsWindowCloseExtension(UI.getCurrent());
        windowClose.addWindowCloseListener(this);
        navigator.addViewChangeListener(this);

        if (fragment != null) {
            navigator.navigateTo(fragment);
        } else {
            showHome();
        }
    }

}
