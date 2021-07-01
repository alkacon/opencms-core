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

import org.opencms.file.CmsObject;
import org.opencms.gwt.CmsCoreService;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsAttributeAwareApp;
import org.opencms.ui.apps.CmsAppView;
import org.opencms.ui.apps.CmsAppView.CacheStatus;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.I_CmsCachableApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.modules.edit.CmsEditModuleForm;
import org.opencms.ui.components.CmsAppViewLayout;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsBasicReportPage;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsStringUtil;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vaadin.navigator.View;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

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

        /** Icon for the 'import via http' button. */
        public static Resource IMPORT = FontOpenCms.UPLOAD;

        /** Icon for the module list. */
        public static final Resource LIST_ICON = new CmsCssIcon("oc-icon-24-module");

        /** Icon for resource info boxes. */
        public static Resource RESINFO_ICON = new CmsCssIcon("oc-icon-24-module");

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

    /**
     * Context menu entry for deleting a module.<p>
     */
    class DeleteModuleEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        @SuppressWarnings("synthetic-access")
        @Override
        public void executeAction(final Set<String> context) {

            try {
                final String moduleName = context.iterator().next();
                final CmsObject cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                final CmsModule module = OpenCms.getModuleManager().getModule(moduleName);

                Runnable okAction = new Runnable() {

                    @Override
                    public void run() {

                        final A_CmsReportThread thread = new A_CmsReportThread(cms, "Delete module " + context) {

                            @Override
                            public String getReportUpdate() {

                                return getReport().getReportUpdate();
                            }

                            @Override
                            public void run() {

                                initHtmlReport(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
                                try {
                                    OpenCms.getModuleManager().deleteModule(cms, moduleName, false, getReport());
                                } catch (Exception e) {
                                    getReport().println(e);
                                    LOG.error(e.getLocalizedMessage(), e);
                                }

                            }
                        };
                        if (module.hasModuleResourcesWithUndefinedSite()) {
                            CmsSiteSelectDialog.openDialogInWindow(new CmsSiteSelectDialog.I_Callback() {

                                @Override
                                public void onCancel() {

                                    // TODO Auto-generated method stub

                                }

                                @Override
                                public void onSiteSelect(String site) {

                                    cms.getRequestContext().setSiteRoot(site);
                                    openReport(
                                        CmsModuleApp.States.DELETE_REPORT,
                                        thread,
                                        CmsVaadinUtils.getMessageText(
                                            Messages.GUI_MODULES_REPORT_DELETE_MODULE_1,
                                            context));
                                }
                            }, CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_TITLE_SITE_SELECT_0));

                        } else {
                            openReport(
                                CmsModuleApp.States.DELETE_REPORT,
                                thread,
                                CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_REPORT_DELETE_MODULE_1, context));
                        }
                    }
                };
                CmsConfirmationDialog.show(
                    CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_CONFIRM_DELETE_TITLE_1, context),
                    CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_DELETE_CONFIRMATION_0),
                    okAction);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        @Override
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_CONTEXTMENU_DELETE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        @Override
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            if (context.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }
            return visibilityCheckHasModule(context.iterator().next());
        }

    }

    /**
     * Context menu entry for editng a module.
     */
    class EditModuleEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        @Override
        public void executeAction(Set<String> context) {

            CmsModuleApp.this.editModule(context.iterator().next());

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        @Override
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_CONTEXTMENU_EDIT_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        @Override
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            if (context.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }
            return visibilityCheckHasModule(context.iterator().next());
        }

    }

    /**
     * Context menu entry for editng a module.
     */
    static class ExplorerEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        @Override
        public void executeAction(Set<String> context) {

            String path = getModuleFolder(context.iterator().next());
            if (path != null) {
                String link = CmsCoreService.getVaadinWorkplaceLink(A_CmsUI.getCmsObject(), path);
                A_CmsUI.get().getPage().setLocation(link);
                A_CmsUI.get().getPage().reload();
            }
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        @Override
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_CONTEXTMENU_EXPLORER_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        @Override
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            if (context.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }
            String moduleName = context.iterator().next();
            if (getModuleFolder(moduleName) != null) {
                return visibilityCheckHasModule(moduleName);
            } else {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }
        }

        /**
         * Tries to find the module folder under /system/modules for a given module.<p>
         *
         * @param moduleName the name of the module
         *
         * @return the module folder, or null if this module doesn't have one
         */
        private String getModuleFolder(String moduleName) {

            CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
            if (module != null) {
                for (String resource : module.getResources()) {
                    if (CmsStringUtil.comparePaths("/system/modules/" + moduleName, resource)) {
                        return resource;
                    }
                }
            }
            return null;
        }
    }

    /**
     * Context menu entry for exporting a module.
     */
    class ExportModuleEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        @Override
        public void executeAction(final Set<String> context) {

            final CmsObject cms = A_CmsUI.getCmsObject();
            final String moduleName = context.iterator().next();
            final String handlerDesc = CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_REPORT_EXPORT_1, moduleName);
            final CmsModuleImportExportHandler handler = CmsModuleImportExportHandler.getExportHandler(
                cms,
                OpenCms.getModuleManager().getModule(moduleName),
                handlerDesc);

            final A_CmsReportThread thread = new A_CmsReportThread(cms, "Export module " + moduleName) {

                @Override
                public String getReportUpdate() {

                    return getReport().getReportUpdate();
                }

                /**
                 * @see java.lang.Thread#run()
                 */
                @SuppressWarnings("synthetic-access")
                @Override
                public void run() {

                    initHtmlReport(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
                    try {
                        OpenCms.getImportExportManager().exportData(cms, handler, getReport());
                    } catch (Exception e) {
                        getReport().println(e);
                        LOG.error(e.getLocalizedMessage(), e);
                    }

                }
            };
            CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
            if (module.hasModuleResourcesWithUndefinedSite()) {
                CmsSiteSelectDialog.openDialogInWindow(new CmsSiteSelectDialog.I_Callback() {

                    @Override
                    public void onCancel() {

                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onSiteSelect(String site) {

                        cms.getRequestContext().setSiteRoot(site);
                        Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
                        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_REPORT_EXPORT_1, context));
                        CmsModuleExportDialog dialog = new CmsModuleExportDialog(handler, thread, window);
                        window.setContent(dialog);
                        A_CmsUI.get().addWindow(window);
                        //                        openReport(
                        //                            CmsModuleApp.States.EXPORT_REPORT,
                        //                            thread,
                        //                            CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_REPORT_EXPORT_1, context));
                    }
                }, CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_TITLE_SITE_SELECT_0));

            } else {
                Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
                window.setHeight("500px");
                window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_REPORT_EXPORT_1, context));
                CmsModuleExportDialog dialog = new CmsModuleExportDialog(handler, thread, window);
                window.setContent(dialog);
                A_CmsUI.get().addWindow(window);
                //                openReport(
                //                    CmsModuleApp.States.EXPORT_REPORT,
                //                    thread,
                //                    CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_REPORT_EXPORT_1, context));
            }
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        @Override
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_CONTEXTMENU_EXPORT_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        @Override
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            if (context.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }

            return visibilityCheckHasModule(context.iterator().next());
        }

    }

    /**
     * Context menu entry for displaying the type list.<p>
     */
    class ModuleInfoEntry
    implements I_CmsSimpleContextMenuEntry<Set<String>>,
    org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        @Override
        public void executeAction(Set<String> module) {

            Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);

            CmsModuleInfoDialog typeList = new CmsModuleInfoDialog(
                module.iterator().next(),
                CmsModuleApp.this::editModule);

            window.setContent(typeList);
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_TYPES_FOR_MODULE_0));
            A_CmsUI.get().addWindow(window);
            window.center();

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry.I_HasCssStyles#getStyles()
         */
        public String getStyles() {

            return ValoTheme.LABEL_BOLD;
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        @Override
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_CONTEXTMENU_LIST_TYPES_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        @Override
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            if (context.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }
            return visibilityCheckHasModule(context.iterator().next());
        }

    }

    /** The 'module' parameter. */
    public static final String PARAM_MODULE = "module";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModuleApp.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

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
     * Opens the module editor for the given module.<p>
     *
     * @param module the edited module
     * @param isNew true if this is a new module
     * @param caption the caption for the module editor dialog
     * @param callback the callback to call after the edit dialog is done
     */
    public static void editModule(CmsModule module, boolean isNew, String caption, Runnable callback) {

        Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        CmsEditModuleForm form = new CmsEditModuleForm(module, isNew, callback);
        window.setContent(form);
        window.setCaption(caption);
        A_CmsUI.get().addWindow(window);
        window.center();
    }

    /**
     * Returns VISIBILITY_ACTIVE if a module with the given name exists, and VISIBILITY_INVISIBLE otherwise. <p>
     *
     * @param name a module name
     * @return the visibility
     */
    public static CmsMenuItemVisibilityMode visibilityCheckHasModule(String name) {

        if (OpenCms.getModuleManager().hasModule(name)) {
            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        } else {
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }
    }

    /**
     * Opens the edit dialog for the given module.<p>
     *
     * @param moduleName the name of the module
     */
    public void editModule(String moduleName) {

        CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
        editModule(
            module,
            false,
            CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_TITLE_EDIT_MODULE_1, module.getName()),
            this::reload);
    }

    /**
     * Opens module edit dialog for a new module.<p>
     *
     * @param callback the callback to call after finishing
     */
    public void editNewModule(Runnable callback) {

        CmsModule module = new CmsModule();
        module.setSite("/");

        editModule(module, true, CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_TITLE_NEW_MODULE_0), callback);

    }

    /**
     * Returns the available menu entries.<p>
     *
     * @return the menu entries
     */
    public List<I_CmsSimpleContextMenuEntry<Set<String>>> getMenuEntries() {

        List<I_CmsSimpleContextMenuEntry<Set<String>>> result = Lists.newArrayList();

        result.add(new ModuleInfoEntry());
        result.add(new EditModuleEntry());
        result.add(new DeleteModuleEntry());
        result.add(new ExportModuleEntry());
        result.add(new ExplorerEntry());
        return result;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    @Override
    public void initUI(I_CmsAppUIContext context) {

        context.addPublishButton(updatedItems -> {});
        super.initUI(context);
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
     * Opens the module info dialog.<p>
     *
     * @param name the name of the module
     */
    public void openModuleInfo(Set<String> name) {

        new ModuleInfoEntry().executeAction(name);

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
            return new CmsImportTabForm(this, this::reload);
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
                return getModuleTable();
            }
    }

    /**
     * Gets the module table.<p>
     *
     * @return the module table
     */
    protected Component getModuleTable() {

        List<CmsModuleRow> rows = Lists.newArrayList();
        for (CmsModule module : OpenCms.getModuleManager().getAllInstalledModules()) {
            CmsModuleRow row = new CmsModuleRow(module);
            rows.add(row);
        }
        CmsModuleTable<CmsModuleRow> table = new CmsModuleTable<CmsModuleRow>(this, CmsModuleRow.class, rows);
        return table;
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
     * Reloads the table.
     */
    protected void reload() {

        A_CmsUI.get().reload();

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
            buttons.add(0, CmsAppViewLayout.createPublishButton(changes -> {}));
            for (Component button : buttons) {
                m_uiContext.addToolbarButton(button);
            }
        }
    }

}
