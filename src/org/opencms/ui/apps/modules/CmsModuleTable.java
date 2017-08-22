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
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsAttributeAwareApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.modules.edit.CmsEditModuleForm;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.components.CmsInfoButton;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.ui.util.table.CmsBeanTableBuilder;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Overview list for module information.<p>
 */
public class CmsModuleTable extends Table {

    /**
     * Context menu entry for deleting a module.<p>
     */
    class DeleteModuleEntry implements I_CmsSimpleContextMenuEntry<String> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        @SuppressWarnings("synthetic-access")
        @Override
        public void executeAction(final String context) {

            try {
                final CmsObject cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                final CmsModule module = OpenCms.getModuleManager().getModule(context);

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
                                    OpenCms.getModuleManager().deleteModule(cms, context, false, getReport());
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
                                    m_app.openReport(
                                        CmsModuleApp.States.DELETE_REPORT,
                                        thread,
                                        CmsVaadinUtils.getMessageText(
                                            Messages.GUI_MODULES_REPORT_DELETE_MODULE_1,
                                            context));
                                }
                            }, CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_TITLE_SITE_SELECT_0));

                        } else {
                            m_app.openReport(
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
        public CmsMenuItemVisibilityMode getVisibility(String context) {

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     * Context menu entry for editng a module.
     */
    class EditModuleEntry implements I_CmsSimpleContextMenuEntry<String> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        @Override
        public void executeAction(String context) {

            CmsModule module = OpenCms.getModuleManager().getModule(context);
            editModule(
                module,
                false,
                CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_TITLE_EDIT_MODULE_1, module.getName()));
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
        public CmsMenuItemVisibilityMode getVisibility(String context) {

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     * Context menu entry for editng a module.
     */
    class ExplorerEntry implements I_CmsSimpleContextMenuEntry<String> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        @Override
        public void executeAction(String context) {

            String path = getModuleFolder(context);
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
        public CmsMenuItemVisibilityMode getVisibility(String context) {

            if (getModuleFolder(context) != null) {
                return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
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
    class ExportModuleEntry implements I_CmsSimpleContextMenuEntry<String> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        @Override
        public void executeAction(final String context) {

            final CmsObject cms = A_CmsUI.getCmsObject();

            final A_CmsReportThread thread = new A_CmsReportThread(cms, "Export module " + context) {

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
                        CmsModuleImportExportHandler handler = CmsModuleImportExportHandler.getExportHandler(
                            cms,
                            OpenCms.getModuleManager().getModule(context),
                            CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_REPORT_EXPORT_1, context));
                        OpenCms.getImportExportManager().exportData(cms, handler, getReport());
                    } catch (Exception e) {
                        getReport().println(e);
                        LOG.error(e.getLocalizedMessage(), e);
                    }

                }
            };
            CmsModule module = OpenCms.getModuleManager().getModule(context);
            if (module.hasModuleResourcesWithUndefinedSite()) {
                CmsSiteSelectDialog.openDialogInWindow(new CmsSiteSelectDialog.I_Callback() {

                    @Override
                    public void onCancel() {

                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onSiteSelect(String site) {

                        cms.getRequestContext().setSiteRoot(site);
                        m_app.openReport(
                            CmsModuleApp.States.EXPORT_REPORT,
                            thread,
                            CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_REPORT_EXPORT_1, context));
                    }
                }, CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_TITLE_SITE_SELECT_0));

            } else {
                m_app.openReport(
                    CmsModuleApp.States.EXPORT_REPORT,
                    thread,
                    CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_REPORT_EXPORT_1, context));
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
        public CmsMenuItemVisibilityMode getVisibility(String context) {

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     * Context menu entry for displaying the type list.<p>
     */
    class ModuleInfoEntry
    implements I_CmsSimpleContextMenuEntry<String>,
    org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        @Override
        public void executeAction(String module) {

            Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
            CmsModuleInfoDialog typeList = new CmsModuleInfoDialog(module);
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
        public CmsMenuItemVisibilityMode getVisibility(String context) {

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModuleTable.class);

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** The module manager app instance. */
    protected CmsModuleApp m_app;

    /** The table builder. */
    protected CmsBeanTableBuilder<CmsModuleRow> m_tableBuilder;

    /** The row counter label. */
    private CmsInfoButton m_counter;

    /** The context menu. */
    private CmsContextMenu m_menu = new CmsContextMenu();

    /** The search box. */
    private TextField m_searchBox = new TextField();

    /**
     * Creates a new instance.<p>
     *
     * @param app the module manager app instance.<p>
     *
     * @param rows the module rows
     */
    public CmsModuleTable(CmsModuleApp app, List<CmsModuleRow> rows) {
        m_menu.setAsTableContextMenu(this);
        m_app = app;
        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void itemClick(ItemClickEvent event) {

                CmsModuleTable.this.onItemClick(event);
            }
        });
        m_searchBox.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void textChange(TextChangeEvent event) {

                String filterString = event.getText();

                Container.Filterable container = (Container.Filterable)getContainerDataSource();
                container.removeAllContainerFilters();
                container.addContainerFilter(m_tableBuilder.getDefaultFilter(filterString));
                if ((getValue() != null)) {
                    setCurrentPageFirstItemId(getValue());
                }
            }
        });
        m_searchBox.setIcon(FontOpenCms.FILTER);
        m_searchBox.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        m_searchBox.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_searchBox.setWidth("200px");

        Map<String, Object> attributes = Maps.newHashMap();

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);

        hl.addComponent(m_searchBox);
        // hl.setComponentAlignment(m_counter, Alignment.MIDDLE_LEFT);
        attributes.put(A_CmsAttributeAwareApp.ATTR_INFO_COMPONENT, hl);
        attributes.put(A_CmsAttributeAwareApp.ATTR_MAIN_HEIGHT_FULL, Boolean.TRUE);
        List<Component> buttons = Lists.newArrayList();
        Button addModule = CmsToolBar.createButton(
            FontOpenCms.WAND,
            CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_BUTTON_NEW_MODULE_0));
        addModule.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {

                CmsModule module = new CmsModule();
                module.setSite("/");
                editModule(module, true, CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_TITLE_NEW_MODULE_0));
            }
        });
        buttons.add(addModule);

        Button importButton = CmsToolBar.createButton(
            CmsModuleApp.Icons.IMPORT,
            CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_BUTTON_IMPORT_0));
        importButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {

                m_app.openSubView(CmsModuleApp.States.IMPORT, true);
            }
        });
        buttons.add(importButton);
        m_counter = new CmsInfoButton(Collections.singletonMap("Test", "Test"));
        m_counter.setWindowCaption("Module statistics");
        m_counter.setDescription("Module statistics");
        buttons.add(m_counter);
        attributes.put(CmsModuleApp.Attributes.BUTTONS, buttons);
        setData(attributes);
        CmsBeanTableBuilder<CmsModuleRow> builder = CmsBeanTableBuilder.newInstance(CmsModuleRow.class);
        m_tableBuilder = builder;
        builder.buildTable(this, rows);
        setCellStyleGenerator(builder.getDefaultCellStyleGenerator());
        setItemIconPropertyId("icon");
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);
        setSelectable(true);
        setMultiSelect(false);
        sort(new Object[] {"name"}, new boolean[] {true});
        updateCounter();
    }

    /**
     * Opens the module editor for the given module.<p>
     *
     * @param module the edited module
     * @param isNew true if this is a new module
     * @param caption the caption for the module editor dialog
     */
    public void editModule(CmsModule module, boolean isNew, String caption) {

        Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        CmsEditModuleForm form = new CmsEditModuleForm(module, isNew, new Runnable() {

            @Override
            public void run() {

                reload();
            }
        });
        window.setContent(form);
        window.setCaption(caption);
        A_CmsUI.get().addWindow(window);
        window.center();
    }

    /**
     * Reloads the table data.<p>
     */
    public void reload() {

        List<CmsModule> modules = OpenCms.getModuleManager().getAllInstalledModules();
        @SuppressWarnings("unchecked")
        BeanItemContainer<CmsModuleRow> container = (BeanItemContainer<CmsModuleRow>)getContainerDataSource();
        container.removeAllItems();
        List<CmsModuleRow> newRows = Lists.newArrayList();
        for (CmsModule module : modules) {
            CmsModuleRow row = new CmsModuleRow(module);
            newRows.add(row);
        }
        container.addAll(newRows);
        sort();
        updateCounter();
    }

    /**
     * Returns the available menu entries.<p>
     *
     * @return the menu entries
     */
    List<I_CmsSimpleContextMenuEntry<String>> getMenuEntries() {

        List<I_CmsSimpleContextMenuEntry<String>> result = Lists.newArrayList();

        result.add(new ModuleInfoEntry());
        result.add(new EditModuleEntry());
        result.add(new DeleteModuleEntry());
        result.add(new ExportModuleEntry());
        result.add(new ExplorerEntry());
        return result;
    }

    /**
     * Handles the table item clicks.<p>
     *
     * @param event the click event
     */
    void onItemClick(ItemClickEvent event) {

        if (!event.isCtrlKey() && !event.isShiftKey()) {
            if (event.getButton().equals(MouseButton.RIGHT) || (event.getPropertyId() == null)) {
                CmsModuleRow moduleRow = (CmsModuleRow)(event.getItemId());
                select(moduleRow);
                m_menu.setEntries(getMenuEntries(), moduleRow.getModule().getName());
                m_menu.openForTable(event, this);
            } else if (event.getButton().equals(MouseButton.LEFT) && "name".equals(event.getPropertyId())) {
                BeanItem<?> item = (BeanItem<?>)event.getItem();
                CmsModuleRow row = (CmsModuleRow)(item.getBean());
                (new ModuleInfoEntry()).executeAction(row.getModule().getName());
            }
        }
    }

    /**
     * Updates the row counter.<p>
     */
    private void updateCounter() {

        m_counter.replaceData(
            Collections.singletonMap(
                CmsVaadinUtils.getMessageText(Messages.GUI_MODULES_STATISTICS_ROW_COUNT_0),
                String.valueOf(getContainerDataSource().size())));
    }

}
