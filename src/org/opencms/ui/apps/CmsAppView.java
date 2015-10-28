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
import org.opencms.ui.I_CmsAppView;
import org.opencms.ui.Messages;
import org.opencms.ui.apps.CmsWorkplaceAppManager.NavigationState;
import org.opencms.ui.components.CmsAppViewLayout;
import org.opencms.ui.components.I_CmsWindowCloseListener;
import org.opencms.ui.components.OpenCmsTheme;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Displays the selected app.<p>
 */
public class CmsAppView implements ViewChangeListener, I_CmsWindowCloseListener, I_CmsAppView, Handler {

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

    /** The history back action. */
    private static final Action ACTION_HISTORY_BACK = new ShortcutAction(
        "Alt+ArrowLeft",
        ShortcutAction.KeyCode.ARROW_LEFT,
        new int[] {ShortcutAction.ModifierKey.ALT});

    /** The history forward action. */
    private static final Action ACTION_HISTORY_FORWARD = new ShortcutAction(
        "Alt+ArrowRight",
        ShortcutAction.KeyCode.ARROW_RIGHT,
        new int[] {ShortcutAction.ModifierKey.ALT});

    /** The serial version id. */
    private static final long serialVersionUID = -8128528863875050216L;

    /** The default shortcut actions. */
    private Map<Action, Runnable> m_defaultActions;

    /** The app shortcut actions. */
    private Map<Action, Runnable> m_appActions;

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
        m_defaultActions = new HashMap<Action, Runnable>();
        m_defaultActions.put(ACTION_HISTORY_BACK, new Runnable() {

            public void run() {

                ((CmsAppWorkplaceUi)UI.getCurrent()).historyBack();
            }
        });
        m_defaultActions.put(ACTION_HISTORY_FORWARD, new Runnable() {

            public void run() {

                ((CmsAppWorkplaceUi)UI.getCurrent()).historyForward();
            }
        });
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

        disableGlobalShortcuts();
        if (m_app instanceof ViewChangeListener) {
            return ((ViewChangeListener)m_app).beforeViewChange(event);
        }
        return true;
    }

    /**
     * @see org.opencms.ui.I_CmsAppView#createComponent()
     */
    public Component createComponent() {

        if (m_app == null) {
            return reinitComponent();
        }
        return null;
    }

    /**
     * @see org.opencms.ui.I_CmsAppView#disableGlobalShortcuts()
     */
    public void disableGlobalShortcuts() {

        UI.getCurrent().removeActionHandler(this);
    }

    /**
     * @see org.opencms.ui.I_CmsAppView#enableGlobalShortcuts()
     */
    public void enableGlobalShortcuts() {

        // to avoid multiple action handler registration, remove this first
        UI.getCurrent().removeActionHandler(this);
        UI.getCurrent().addActionHandler(this);
    }

    /**
     * @see org.opencms.ui.I_CmsAppView#enter(java.lang.String)
     */
    public void enter(String newState) {

        if (newState.startsWith(NavigationState.PARAM_SEPARATOR)) {
            newState = newState.substring(1);
        }
        m_app.onStateChange(newState);
        if (m_app instanceof I_CmsHasShortcutActions) {
            m_appActions = ((I_CmsHasShortcutActions)m_app).getShortcutActions();
        }
        UI.getCurrent().addActionHandler(this);
    }

    /**
     * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public void enter(ViewChangeEvent event) {

        String newState = event.getParameters();
        enter(newState);
    }

    /**
     * @see com.vaadin.event.Action.Handler#getActions(java.lang.Object, java.lang.Object)
     */
    public Action[] getActions(Object target, Object sender) {

        if (m_appActions != null) {
            Set<Action> actions = new HashSet<Action>(m_defaultActions.keySet());
            actions.addAll(m_appActions.keySet());
            return actions.toArray(new Action[actions.size()]);
        }
        return m_defaultActions.keySet().toArray(new Action[m_defaultActions.size()]);
    }

    /**
     * @see com.vaadin.event.Action.Handler#handleAction(com.vaadin.event.Action, java.lang.Object, java.lang.Object)
     */
    public void handleAction(Action action, Object sender, Object target) {

        if ((m_appActions != null) && m_appActions.containsKey(action)) {
            m_appActions.get(action).run();
        } else if (m_defaultActions.containsKey(action)) {
            m_defaultActions.get(action).run();
        }
    }

    /**
     * @see org.opencms.ui.components.I_CmsWindowCloseListener#onWindowClose()
     */
    public void onWindowClose() {

        if (m_app instanceof I_CmsWindowCloseListener) {
            ((I_CmsWindowCloseListener)m_app).onWindowClose();
        }
        disableGlobalShortcuts();
    }

    /**
     * @see org.opencms.ui.I_CmsAppView#reinitComponent()
     */
    public Component reinitComponent() {

        if (m_app != null) {
            beforeViewChange(
                new ViewChangeEvent(CmsAppWorkplaceUi.get().getNavigator(), this, this, m_appConfig.getId(), ""));
        }
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
}
