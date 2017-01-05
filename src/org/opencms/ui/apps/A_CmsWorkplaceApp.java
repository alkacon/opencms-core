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

import org.opencms.main.OpenCms;
import org.opencms.ui.components.CmsToolLayout;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

/**
 * Super class for workplace apps to help implementing the app navigation and layout.<p>
 */
public abstract class A_CmsWorkplaceApp implements I_CmsWorkplaceApp {

    /**
     * An app navigation entry.<p>
     */
    public static class NavEntry {

        /** The entry description. */
        private String m_description;

        /** The entry icon. */
        private Resource m_icon;

        /** The localized entry name. */
        private String m_name;

        /** The target state. */
        private String m_targetState;

        /**
         * Constructor.<p>
         *
         * @param name the entry name
         * @param description the description
         * @param icon the icon
         * @param targetState the target state
         */
        public NavEntry(String name, String description, Resource icon, String targetState) {
            m_name = name;
            m_description = description;
            m_icon = icon;
            m_targetState = targetState;
        }

        /**
         * Returns the description.<p>
         *
         * @return the description
         */
        public String getDescription() {

            return m_description;
        }

        /**
         * Returns the icon.<p>
         *
         * @return the icon
         */
        public Resource getIcon() {

            return m_icon;
        }

        /**
         * Returns the entry name.<p>
         *
         * @return the entry name
         */
        public String getName() {

            return m_name;
        }

        /**
         * Returns the target state.<p>
         *
         * @return the target state
         */
        public String getTargetState() {

            return m_targetState;
        }
    }

    /** State parameter value separator. */
    public static final String PARAM_ASSIGN = "::";

    /** State parameter separator. */
    public static final String PARAM_SEPARATOR = "!!";

    /** The app info layout containing the bread crumb navigation as first component. */
    protected HorizontalLayout m_infoLayout;

    /** The root layout. */
    protected CmsToolLayout m_rootLayout;

    /** The app UI context. */
    protected I_CmsAppUIContext m_uiContext;

    /** The bread crumb navigation. */
    private Label m_breadCrumb;

    /**
     * Constructor.<p>
     */
    protected A_CmsWorkplaceApp() {

        m_rootLayout = new CmsToolLayout();
        m_rootLayout.setSizeFull();
    }

    /**
     * Adds a parameter value to the given state.<p>
     *
     * @param state the state
     * @param paramName the parameter name
     * @param value the parameter value
     *
     * @return the state
     */
    public static String addParamToState(String state, String paramName, String value) {

        return state + PARAM_SEPARATOR + paramName + PARAM_ASSIGN + value;
    }

    /**
     * Parses the requested parameter from the given state.<p>
     *
     * @param state the state
     * @param paramName the parameter name
     *
     * @return the parameter value
     */
    public static String getParamFromState(String state, String paramName) {

        String prefix = PARAM_SEPARATOR + paramName + PARAM_ASSIGN;
        if (state.contains(prefix)) {
            String result = state.substring(state.indexOf(prefix) + prefix.length());
            if (result.contains(PARAM_SEPARATOR)) {
                result = result.substring(0, result.indexOf(PARAM_SEPARATOR));
            }
            return result;
        }
        return null;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    public void initUI(I_CmsAppUIContext context) {

        m_uiContext = context;
        m_uiContext.showInfoArea(true);
        m_breadCrumb = new Label();
        m_breadCrumb.addStyleName(OpenCmsTheme.TOOLS_BREADCRUMB);
        m_breadCrumb.setContentMode(ContentMode.HTML);
        m_infoLayout = new HorizontalLayout();
        m_infoLayout.setSizeFull();
        m_infoLayout.setSpacing(true);
        m_infoLayout.setMargin(true);
        m_uiContext.setAppInfo(m_infoLayout);
        m_infoLayout.addComponent(m_breadCrumb);
        m_infoLayout.setExpandRatio(m_breadCrumb, 2);
        m_uiContext.setAppContent(m_rootLayout);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsWorkplaceApp#onStateChange(java.lang.String)
     */
    public void onStateChange(String state) {

        openSubView(state, false);
    }

    /**
     * Opens the requested sub view.<p>
     *
     * @param state the state
     * @param updateState <code>true</code> to update the state URL token
     */
    public void openSubView(String state, boolean updateState) {

        if (updateState) {
            CmsAppWorkplaceUi.get().changeCurrentAppState(state);
        }
        Component comp = getComponentForState(state);
        if (comp != null) {
            comp.setSizeFull();
            m_rootLayout.setMainContent(comp);
        } else {
            m_rootLayout.setMainContent(new Label("Malformed path, tool not availabel for path: " + state));
        }
        updateSubNav(getSubNavEntries(state));
        updateBreadCrumb(getBreadCrumbForState(state));
    }

    /**
     * Adds a navigation entry.<p>
     *
     * @param navEntry the navigation entry
     */
    protected void addSubNavEntry(final NavEntry navEntry) {

        Button button = m_rootLayout.addSubNavEntry(navEntry);
        button.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                openSubView(navEntry.getTargetState(), true);
            }
        });
    }

    /**
     * Returns the current bread crumb entries in an ordered map.<p>
     *
     * @param state the current state
     *
     * @return bread crumb entry name by state, in case the state is empty, the entry will be disabled
     */
    protected abstract LinkedHashMap<String, String> getBreadCrumbForState(String state);

    /**
     * Returns the app component for the given state
     *
     * @param state the state to render
     *
     * @return the app component
     */
    protected abstract Component getComponentForState(String state);

    /**
     * Returns the last path level.<p>
     *
     * @param path the path
     *
     * @return the last path level
     */
    protected String getLastPathLevel(String path) {

        path = path.trim();
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.contains("/")) {
            path = path.substring(path.lastIndexOf("/"));
        }
        return path;
    }

    /**
     * Returns the sub navigation entries.<p>
     *
     * @param state the state
     *
     * @return the sub navigation entries
     */
    protected abstract List<NavEntry> getSubNavEntries(String state);

    /**
     * Updates the bread crumb navigation.<p>
     *
     * @param breadCrumbEntries the bread crumb entries
     */
    protected void updateBreadCrumb(Map<String, String> breadCrumbEntries) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("<div>");
        appendBreadCrumbEntry(
            buffer,
            CmsAppHierarchyConfiguration.APP_ID,
            OpenCms.getWorkplaceAppManager().getAppConfiguration(CmsAppHierarchyConfiguration.APP_ID).getName(
                UI.getCurrent().getLocale()));
        if ((breadCrumbEntries != null) && !breadCrumbEntries.isEmpty()) {
            for (Entry<String, String> entry : breadCrumbEntries.entrySet()) {
                appendBreadCrumbEntry(buffer, entry.getKey(), entry.getValue());
            }
        } else {
            appendBreadCrumbEntry(
                buffer,
                null,
                OpenCms.getWorkplaceAppManager().getAppConfiguration(m_uiContext.getAppId()).getName(
                    UI.getCurrent().getLocale()));
        }
        buffer.append("</div>");
        m_breadCrumb.setValue(buffer.toString());
        //  m_rootLayout.setBreadCrumb(buffer.toString());
    }

    /**
     * Updates the sub navigation with the given entries.<p>
     *
     * @param subEntries the sub navigation entries
     */
    protected void updateSubNav(List<NavEntry> subEntries) {

        m_rootLayout.clearSubNav();
        if ((subEntries == null) || subEntries.isEmpty()) {
            m_rootLayout.setSubNavVisible(false);
        } else {
            m_rootLayout.setSubNavVisible(true);
            for (NavEntry entry : subEntries) {
                addSubNavEntry(entry);
            }
        }
    }

    /**
     * Appends a bread crumb entry.<p>
     *
     * @param buffer the string buffer to append to
     * @param target the target state
     * @param label the entry label
     */
    private void appendBreadCrumbEntry(StringBuffer buffer, String target, String label) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(target)) {
            buffer.append("<a href=\"#!").append(target).append("\">").append(label).append("</a>");
        } else {
            buffer.append("<span class=\"o-tools-breadcrumb-active\">").append(label).append("</span>");
        }
    }
}
