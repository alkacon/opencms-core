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

package org.opencms.ui.apps;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsBroadcast;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinErrorHandler;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsAppView;
import org.opencms.ui.apps.CmsAppView.CacheStatus;
import org.opencms.ui.apps.CmsWorkplaceAppManager.NavigationState;
import org.opencms.ui.components.I_CmsWindowCloseListener;
import org.opencms.ui.components.extensions.CmsHistoryExtension;
import org.opencms.ui.components.extensions.CmsPollServerExtension;
import org.opencms.ui.components.extensions.CmsWindowCloseExtension;
import org.opencms.util.CmsExpiringValue;
import org.opencms.util.CmsStringUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.Buffer;
import org.apache.commons.logging.Log;

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
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

/**
 * The workplace ui.<p>
 */
@Theme("opencms")
public class CmsAppWorkplaceUi extends A_CmsUI
implements ViewDisplay, ViewProvider, ViewChangeListener, I_CmsWindowCloseListener, BrowserWindowResizeListener {

    /**
     * View which directly changes the state to the launchpad.<p>
     */
    class LaunchpadRedirectView implements View {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
         */
        public void enter(ViewChangeEvent event) {

            A_CmsUI.get().getNavigator().navigateTo(CmsAppHierarchyConfiguration.APP_ID);
        }
    }

    /** The OpenCms window title prefix. */
    public static final String WINDOW_TITLE_PREFIX = "OpenCms - ";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAppWorkplaceUi.class);

    /** The serial version id. */
    private static final long serialVersionUID = -5606711048683809028L;

    /** Launch pad redirect view. */
    protected View m_launchRedirect = new LaunchpadRedirectView();

    /** The cached views. */
    private Map<String, I_CmsAppView> m_cachedViews;

    /** The current view in case it implements view change listener. */
    private View m_currentView;

    /** The history extension. */
    private CmsHistoryExtension m_history;

    /** Cache for workplace locale. */
    private CmsExpiringValue<Locale> m_localeCache = new CmsExpiringValue<Locale>(1000);

    /** The navigation state manager. */
    private NavigationStateManager m_navigationStateManager;

    /** Currently refreshing? */
    private boolean m_refreshing;

    /**
     * Gets the current UI instance.<p>
     *
     * @return the current UI instance
     */
    public static CmsAppWorkplaceUi get() {

        return (CmsAppWorkplaceUi)A_CmsUI.get();
    }

    /**
     * Returns whether the current project is the online project.<p>
     *
     * @return <code>true</code> if the current project is the online project
     */
    public static boolean isOnlineProject() {

        return getCmsObject().getRequestContext().getCurrentProject().isOnlineProject();
    }

    /**
     * Sets the window title adding an OpenCms prefix.<p>
     *
     * @param title the window title
     */
    public static void setWindowTitle(String title) {

        get().getPage().setTitle(WINDOW_TITLE_PREFIX + title);
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

        cacheView(m_currentView);
        if ((m_currentView != null) && (m_currentView instanceof ViewChangeListener)) {
            return ((ViewChangeListener)m_currentView).beforeViewChange(event);
        }
        return true;
    }

    /**
     * @see com.vaadin.server.Page.BrowserWindowResizeListener#browserWindowResized(com.vaadin.server.Page.BrowserWindowResizeEvent)
     */
    public void browserWindowResized(BrowserWindowResizeEvent event) {

        markAsDirtyRecursive();
        if ((m_currentView != null) && (m_currentView instanceof BrowserWindowResizeListener)) {
            ((BrowserWindowResizeListener)m_currentView).browserWindowResized(event);
        }
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
     * Checks for new broadcasts.<p>
     */
    public void checkBroadcasts() {

        CmsSessionInfo info = OpenCms.getSessionManager().getSessionInfo(getHttpSession());
        Buffer queue = info.getBroadcastQueue();
        if (!queue.isEmpty()) {
            StringBuffer broadcasts = new StringBuffer();
            while (!queue.isEmpty()) {
                CmsBroadcast broadcastMessage = (CmsBroadcast)queue.remove();
                String from = broadcastMessage.getUser() != null
                ? broadcastMessage.getUser().getName()
                : CmsVaadinUtils.getMessageText(org.opencms.workplace.Messages.GUI_LABEL_BROADCAST_FROM_SYSTEM_0);
                String date = CmsVaadinUtils.getWpMessagesForCurrentLocale().getDateTime(
                    broadcastMessage.getSendTime());
                String content = broadcastMessage.getMessage();
                broadcasts.append("<p><em>").append(date).append("</em><br />");
                broadcasts.append(
                    CmsVaadinUtils.getMessageText(
                        org.opencms.workplace.Messages.GUI_LABEL_BROADCASTMESSAGEFROM_0)).append(" <b>").append(
                            from).append("</b>:<br />");
                broadcasts.append(content).append("<br /></p>");
            }
            Notification notification = new Notification(
                CmsVaadinUtils.getMessageText(Messages.GUI_BROADCAST_TITLE_0),
                broadcasts.toString(),
                Type.WARNING_MESSAGE,
                true);
            notification.setDelayMsec(-1);
            notification.show(getPage());
        }
    }

    /**
     * @see org.opencms.ui.A_CmsUI#closeWindows()
     */
    @Override
    public void closeWindows() {

        super.closeWindows();
        if (m_currentView instanceof CmsAppView) {
            ((CmsAppView)m_currentView).getComponent().closePopupViews();
        }
    }

    /**
     * @see com.vaadin.ui.UI#detach()
     */
    @Override
    public void detach() {

        clearCachedViews();
        super.detach();
    }

    /**
     * Disables the global keyboard shortcuts.<p>
     */
    public void disableGlobalShortcuts() {

        if (m_currentView instanceof I_CmsAppView) {
            ((I_CmsAppView)m_currentView).disableGlobalShortcuts();
        }
    }

    /**
     * Enables the global keyboard shortcuts.<p>
     */
    public void enableGlobalShortcuts() {

        if (m_currentView instanceof I_CmsAppView) {
            ((I_CmsAppView)m_currentView).enableGlobalShortcuts();
        }
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
     * Gets the current view.<p>
     *
     * @return the current view
     */
    public View getCurrentView() {

        return m_currentView;
    }

    /**
     * @see com.vaadin.ui.AbstractComponent#getLocale()
     */
    @Override
    public Locale getLocale() {

        Locale result = m_localeCache.get();
        if (result == null) {
            CmsObject cms = getCmsObject();
            result = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
            m_localeCache.set(result);
        }
        return result;
    }

    /**
     * @see com.vaadin.navigator.ViewProvider#getView(java.lang.String)
     */
    public View getView(String viewName) {

        if (m_cachedViews.containsKey(viewName)) {
            View view = m_cachedViews.get(viewName);
            if (view instanceof CmsAppView) {
                CmsAppView appView = (CmsAppView)view;
                if (appView.getCacheStatus() == CacheStatus.cache) {
                    appView.setRequiresRestore(true);
                    return appView;
                } else if (appView.getCacheStatus() == CacheStatus.cacheOnce) {
                    appView.setCacheStatus(CacheStatus.noCache);
                    appView.setRequiresRestore(true);
                    return appView;
                }
            }
        }
        I_CmsWorkplaceAppConfiguration appConfig = OpenCms.getWorkplaceAppManager().getAppConfiguration(viewName);
        if (appConfig != null) {
            return new CmsAppView(appConfig);
        } else {
            LOG.warn("Nonexistant view '" + viewName + "' requested");
            return m_launchRedirect;
        }
    }

    /**
     * @see com.vaadin.navigator.ViewProvider#getViewName(java.lang.String)
     */
    public String getViewName(String viewAndParameters) {

        NavigationState state = new NavigationState(viewAndParameters);
        return state.getViewName();
    }

    /**
     * Executes the history back function.<p>
     */
    public void historyBack() {

        m_history.historyBack();
    }

    /**
     * Executes the history forward function.<p>
     */
    public void historyForward() {

        m_history.historyForward();
    }

    /**
     * Called when an error occurs.<p>
     */
    public void onError() {

        // do nothing for now

    }

    /**
     * @see org.opencms.ui.components.I_CmsWindowCloseListener#onWindowClose()
     */
    public void onWindowClose() {

        if ((m_currentView != null) && (m_currentView instanceof I_CmsWindowCloseListener)) {
            ((I_CmsWindowCloseListener)m_currentView).onWindowClose();
        }
        cacheView(m_currentView);
    }

    /**
     * @see org.opencms.ui.A_CmsUI#reload()
     */
    @Override
    public void reload() {

        if (m_currentView instanceof I_CmsAppView) {
            Component component = ((I_CmsAppView)m_currentView).reinitComponent();
            setContent(component);
            ((I_CmsAppView)m_currentView).enter(getAppState());
        }
    }

    /**
     * @see com.vaadin.ui.UI#setLastHeartbeatTimestamp(long)
     */
    @Override
    public void setLastHeartbeatTimestamp(long lastHeartbeat) {

        super.setLastHeartbeatTimestamp(lastHeartbeat);

        // check for new broadcasts on every heart beat
        checkBroadcasts();
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
     * Navigates to the given app.<p>
     *
     * @param appConfig the app configuration
     * @param state the app state to call
     */
    public void showApp(I_CmsWorkplaceAppConfiguration appConfig, String state) {

        getNavigator().navigateTo(appConfig.getId() + "/" + state);
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

        closeWindows();

        // remove current component form the view change listeners
        m_currentView = view;
        Component component = null;
        if (view instanceof I_CmsAppView) {
            if (((I_CmsAppView)view).requiresRestore()) {
                ((I_CmsAppView)view).restoreFromCache();
            }
            component = ((I_CmsAppView)view).getComponent();
        } else if (view instanceof Component) {
            component = (Component)view;
        }
        initializeClientPolling(component);
        if (component != null) {
            setContent(component);
        }
    }

    /**
    * @see com.vaadin.ui.UI#init(com.vaadin.server.VaadinRequest)
    */
    @Override
    protected void init(VaadinRequest req) {

        super.init(req);
        if (!OpenCms.getRoleManager().hasRole(getCmsObject(), CmsRole.WORKPLACE_USER)) {
            Notification.show(
                CmsVaadinUtils.getMessageText(Messages.GUI_WORKPLACE_ACCESS_DENIED_TITLE_0),
                CmsVaadinUtils.getMessageText(Messages.GUI_WORKPLACE_ACCESS_DENIED_MESSAGE_0),
                Type.ERROR_MESSAGE);
            return;
        }
        getSession().setErrorHandler(new CmsVaadinErrorHandler(this));
        m_cachedViews = new HashMap<String, I_CmsAppView>();
        m_navigationStateManager = new Navigator.UriFragmentManager(getPage());
        CmsAppNavigator navigator = new CmsAppNavigator(this, m_navigationStateManager, this);
        navigator.addProvider(this);
        setNavigator(navigator);

        Page.getCurrent().addBrowserWindowResizeListener(this);
        m_history = new CmsHistoryExtension(getCurrent());
        CmsWindowCloseExtension windowClose = new CmsWindowCloseExtension(getCurrent());
        windowClose.addWindowCloseListener(this);
        navigator.addViewChangeListener(this);
        navigateToFragment();

        getReconnectDialogConfiguration().setDialogText(
            CmsVaadinUtils.getMessageText(org.opencms.ui.Messages.GUI_SYSTEM_CONNECTION_LOST_TRYING_RECONNECT_0));
        getReconnectDialogConfiguration().setDialogTextGaveUp(
            CmsVaadinUtils.getMessageText(org.opencms.ui.Messages.GUI_SYSTEM_CONNECTION_LOST_GIVING_UP_0));
    }

    /**
     * Caches the given view in case it implements the I_CmsAppView interface and is cachable.<p>
     *
     * @param view the view to cache
     */
    private void cacheView(View view) {

        if (!m_refreshing && (view instanceof I_CmsAppView) && ((I_CmsAppView)view).isCachable()) {
            m_cachedViews.put(((I_CmsAppView)view).getName(), (I_CmsAppView)view);
        }
    }

    /**
     * Clears the cached views.<p>
     */
    private void clearCachedViews() {

        m_cachedViews.clear();
    }

    /**
     * Initializes client polling to avoid session expiration.<p>
     *
     * @param component the view component
     */
    @SuppressWarnings("unused")
    private void initializeClientPolling(Component component) {

        if (component instanceof AbstractComponent) {
            new CmsPollServerExtension((AbstractComponent)component);
        }
    }

    /**
     * Navigates to the current URI fragment.<p>
     */
    private void navigateToFragment() {

        String fragment = getPage().getUriFragment();
        if (fragment != null) {
            getNavigator().navigateTo(fragment);
        } else {
            showHome();
        }
    }
}
