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
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsComponentFactory;
import org.opencms.ui.Messages;
import org.opencms.ui.apps.CmsWorkplaceAppManager.NavigationState;
import org.opencms.ui.components.CmsAppViewLayout;
import org.opencms.ui.components.I_CmsWindowCloseListener;
import org.opencms.ui.components.OpenCmsTheme;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Displays the selected app.<p>
 */
public class CmsAppView implements View, ViewChangeListener, I_CmsWindowCloseListener, I_CmsComponentFactory {

    /**
     * Used in case the requested app can not be displayed to the current user.<p>
     */
    protected class NotAvailableApp implements I_CmsWorkplaceApp {

        /**
         * @see org.opencms.ui.apps.I_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
         */
        public void initUI(I_CmsAppUIContext context) {

            Label label = new Label(CmsVaadinUtils.getMessageText(Messages.GUI_APP_NOT_AVAILABLE_0));
            label.addStyleName(ValoTheme.LABEL_H2);
            label.addStyleName(OpenCmsTheme.LABEL_ERROR);
            VerticalLayout content = new VerticalLayout();
            content.setMargin(true);
            content.addComponent(label);
            context.setAppContent(content);
        }

        /**
         * @see org.opencms.ui.apps.I_CmsWorkplaceApp#onStateChange(java.lang.String)
         */
        public void onStateChange(String state) {

            // nothing to do
        }

    }

    /** The serial version id. */
    private static final long serialVersionUID = -8128528863875050216L;

    /** The app configuration. */
    private I_CmsWorkplaceAppConfiguration m_appConfig;

    /** The current app. */
    private I_CmsWorkplaceApp m_app;

    /**
     * Constructor.<p>
     *
     * @param appConfig the app configuration
     */
    public CmsAppView(I_CmsWorkplaceAppConfiguration appConfig) {

        m_appConfig = appConfig;
    }

    /**
     * Returns the workplace UI.<p>
     *
     * @return the workplace UI
     */
    public static CmsAppWorkplaceUi getWorkplaceUi() {

        CmsAppWorkplaceUi ui = (CmsAppWorkplaceUi)A_CmsUI.get();
        return ui;
    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#afterViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public void afterViewChange(ViewChangeEvent event) {

        if (m_app instanceof ViewChangeListener) {
            ((ViewChangeListener)m_app).afterViewChange(event);
        }
    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#beforeViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public boolean beforeViewChange(ViewChangeEvent event) {

        if (m_app instanceof ViewChangeListener) {
            return ((ViewChangeListener)m_app).beforeViewChange(event);
        }
        return true;
    }

    /**
     * @see org.opencms.ui.I_CmsComponentFactory#createComponent()
     */
    public Component createComponent() {

        if (m_app == null) {
            if (!m_appConfig.getVisibility(A_CmsUI.getCmsObject()).isActive()) {
                m_app = new NotAvailableApp();
            } else {
                m_app = m_appConfig.getAppInstance();
            }
            CmsAppViewLayout layout = new CmsAppViewLayout();
            layout.setAppTitle(m_appConfig.getName(UI.getCurrent().getLocale()));
            m_app.initUI(layout);
            return layout;
        }
        return null;
    }

    /**
     * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public void enter(ViewChangeEvent event) {

        String newState = event.getParameters();
        if (newState.startsWith(NavigationState.PARAM_SEPARATOR)) {
            newState = newState.substring(1);
        }
        m_app.onStateChange(newState);

    }

    /**
     * @see org.opencms.ui.components.I_CmsWindowCloseListener#onWindowClose()
     */
    public void onWindowClose() {

        if (m_app instanceof I_CmsWindowCloseListener) {
            ((I_CmsWindowCloseListener)m_app).onWindowClose();
        }
    }
}
