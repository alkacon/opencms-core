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

package org.opencms.ui.apps.modules;

import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsAttributeAwareApp;
import org.opencms.ui.apps.CmsAppView;
import org.opencms.ui.apps.CmsAppView.CacheStatus;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.I_CmsCachableApp;
import org.opencms.ui.apps.Messages;
import org.opencms.util.CmsStringUtil;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vaadin.navigator.View;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;

/**
 * Main module manager app class.<p>
 */
public class CmsModuleApp extends A_CmsAttributeAwareApp implements I_CmsCachableApp {

    /**
     * Additional app attributes for the module manager.<p>
     */
    public static class Attributes {

        /** The buttons for the button bar. */
        public static final String BUTTONS = "buttons";
    }

    /**
     * Contains the icon constants for the module manager.<p>
     */
    public static class Icons {

        /** Icon for the module manager app. */
        public static Resource APP = new CmsCssIcon("oc-icon-32-module");

        /** Icon for resource info boxes. */
        public static Resource RESINFO_ICON = new CmsCssIcon("oc-icon-24-module");

        /** Icon for the 'import via http' button. */
        public static Resource IMPORT = new CmsCssIcon("oc-icon-32-module");

        /** Icon for the module list. */
        public static final Resource LIST_ICON = new CmsCssIcon("oc-icon-24-module");

    }

    /**
     * Contains the different navigation states for the module maanger.<p>
     */
    public static class States {

        /** Delete report state. */
        public static final String DELETE_REPORT = "delete";

        /** Export report state. */
        public static final String EXPORT_REPORT = "export";

        /** State for 'import via http' form. */
        public static final String IMPORT = "import";

        /** State for 'iomport via http' report. */
        public static final String IMPORT_REPORT = "import/report";

        /** Main state. */
        public static final String MAIN = "";
    }

    /** The 'module' parameter. */
    public static final String PARAM_MODULE = "module";

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The currently active report. */
    protected A_CmsReportThread m_currentReport;

    /** The currently running report thread. */
    private A_CmsReportThread m_currentReportThread;

    /** Contains labels for the running reports. */
    private IdentityHashMap<A_CmsReportThread, String> m_labels = new IdentityHashMap<A_CmsReportThread, String>();

    /** Map of running reports with the module manager app states they belong to as keys. */
    private Map<String, A_CmsReportThread> m_reports = Maps.newHashMap();

    /**
     * Creates a new instance.<p>
     */
    public CmsModuleApp() {
        // do nothing

    }

    /**
     * Gets the current report thread.<p>
     *
     * @return the current report thread
     */
    public A_CmsReportThread getReportThread() {

        return m_currentReportThread;
    }

    /**
     * Goes to the main view.<p>
     */
    public void goToMainView() {

        openSubView("", true);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCachableApp#isCachable()
     */
    public boolean isCachable() {

        return true;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCachableApp#onRestoreFromCache()
     */
    public void onRestoreFromCache() {

        // Do nothing

    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#onStateChange(java.lang.String)
     */
    @Override
    public void onStateChange(String state) {

        View view = CmsAppWorkplaceUi.get().getCurrentView();
        ((CmsAppView)view).setCacheStatus(CacheStatus.cache);
        super.onStateChange(state);

    }

    /**
     * Changes to a new sub-view and stores a report to be displayed by that subview.<p<
     *
     * @param newState the new state
     * @param thread the report thread which should be displayed in the sub view
     * @param label the label to display for the report
     */
    public void openReport(String newState, A_CmsReportThread thread, String label) {

        setReport(newState, thread);
        m_labels.put(thread, label);
        openSubView(newState, true);
    }

    /**
     * Stores a report thread isntance under a given key.<p>
     *
     * @param key the key
     * @param report the report thread instance
     */
    public void setReport(String key, A_CmsReportThread report) {

        m_reports.put(key, report);

    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        String noParamState = state.replaceFirst("!!.*$", "");
        List<String> tokens = Lists.newArrayList();
        String appId = CmsModuleAppConfiguration.APP_ID;
        tokens.add(appId);
        tokens.addAll(Arrays.asList(noParamState.split("/")));
        LinkedHashMap<String, String> breadcrumbs = new LinkedHashMap<String, String>();
        List<String> currentPath = Lists.newArrayList();
        breadcrumbs.put(appId, appId);
        String lastKey = appId;

        for (int i = 0; i < tokens.size(); i++) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(tokens.get(i))) {
                continue;
            }
            currentPath.add(tokens.get(i));
            String key = CmsStringUtil.listAsString(currentPath, "/");
            String text = null;
            if (tokens.get(i).equals(appId)) {
                text = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_NAV_MAIN_0);
            } else if (tokens.get(i).equals("delete")) {
                text = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_NAV_DELETE_MODULE_0);
            } else if (tokens.get(i).equals("export")) {
                text = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_NAV_EXPORT_MODULE_0);
            } else if (tokens.get(i).equals("import")) {
                text = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_NAV_IMPORT_HTTP_0);
            } else if (tokens.get(i).equals("server-import")) {
                text = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_NAV_IMPORT_SERVER_0);
            } else if (tokens.get(i).equals("report")) {
                text = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_NAV_REPORT_0);
            } else {
                text = tokens.get(i);
            }
            breadcrumbs.put(key, text);
            lastKey = key;
        }
        String lastLabel = breadcrumbs.remove(lastKey);
        breadcrumbs.put("", lastLabel);
        return breadcrumbs;
    }

    /**
     *
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        if (state.equals(States.IMPORT)) {
            return new CmsImportTabForm(this);
        } else if (state.equals(States.IMPORT_REPORT)
            || state.equals(States.DELETE_REPORT)
            || state.equals(States.EXPORT_REPORT)) {
            String label = getReportLabel(state);
            CmsBasicReportPage reportForm = new CmsBasicReportPage(label, m_reports.get(state), new Runnable() {

                public void run() {

                    openSubView("", true);
                }
            });
            reportForm.setHeight("100%");
            return reportForm;
        } else {
            List<CmsModuleRow> rows = Lists.newArrayList();
            for (CmsModule module : OpenCms.getModuleManager().getAllInstalledModules()) {
                CmsModuleRow row = new CmsModuleRow(module);
                rows.add(row);
            }
            m_rootLayout.setMainHeightFull(true);

            CmsModuleTable table = new CmsModuleTable(this, rows);
            return table;
        }
    }

    /**
     * Gets the label for a given report.<p>
     *
     * @param state the state for which to get the label
     * @return the label
     */
    protected String getReportLabel(String state) {

        return m_labels.get(m_reports.get(state));
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsAttributeAwareApp#updateAppAttributes(java.util.Map)
     */
    @Override
    protected void updateAppAttributes(Map<String, Object> attributes) {

        super.updateAppAttributes(attributes);
        m_uiContext.clearToolbarButtons();
        @SuppressWarnings("unchecked")
        List<Component> buttons = (List<Component>)attributes.get(Attributes.BUTTONS);
        if (buttons != null) {
            for (Component button : buttons) {
                m_uiContext.addToolbarButton(button);
            }
        }
    }

}
