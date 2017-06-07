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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.modules;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleDependency;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.module.CmsModuleManager;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsToolDialog;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Class to upload a module from the server.<p>
 *
 * @since 6.0.0
 */
public class CmsModulesUploadFromServer extends CmsWidgetDialog {

    /**
     * A <code>{@link Comparator}</code> for <code>{@link CmsSelectWidgetOption}</code> instances.<p>
     *
     * @since 7.0.3
     *
     */
    protected class ComparatorSelectWidgetOption implements Comparator {

        /** Collator used / wrapped. */
        private Collator m_collator = Collator.getInstance(getLocale());

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object arg0, Object arg1) {

            CmsSelectWidgetOption o1 = (CmsSelectWidgetOption)arg0;
            CmsSelectWidgetOption o2 = (CmsSelectWidgetOption)arg1;
            return m_collator.compare(o1.getOption(), o2.getOption());
        }

    }

    /** The dialog type. */
    public static final String DIALOG_TYPE = "ModulesUploadServer";

    /** The import action. */
    protected static final String IMPORT_ACTION_REPORT = "/system/workplace/admin/modules/reports/import.jsp";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Modulename parameter. */
    public static final String PARAM_MODULENAME = "modulename";

    /** The replace action. */
    protected static final String REPLACE_ACTION_REPORT = "/system/workplace/admin/modules/reports/replace.jsp";

    /** Modulename. */
    private String m_moduleupload;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsModulesUploadFromServer(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsModulesUploadFromServer(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() throws IOException, ServletException {

        List errors = new ArrayList();
        CmsModule module = null;
        try {
            String importpath = OpenCms.getSystemInfo().getPackagesRfsPath();
            importpath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
                importpath + "modules/" + m_moduleupload);
            module = CmsModuleImportExportHandler.readModuleFromImport(importpath);

            // check if all dependencies are fulfilled
            List dependencies = OpenCms.getModuleManager().checkDependencies(
                module,
                CmsModuleManager.DEPENDENCY_MODE_IMPORT);
            if (!dependencies.isEmpty()) {
                StringBuffer dep = new StringBuffer(32);
                for (int i = 0; i < dependencies.size(); i++) {
                    CmsModuleDependency dependency = (CmsModuleDependency)dependencies.get(i);
                    dep.append("\n - ");
                    dep.append(dependency.getName());
                    dep.append(" (Version: ");
                    dep.append(dependency.getVersion());
                    dep.append(")");
                }
                errors.add(
                    new CmsRuntimeException(
                        Messages.get().container(
                            Messages.ERR_ACTION_MODULE_DEPENDENCY_2,
                            m_moduleupload,
                            new String(dep))));
            }

        } catch (CmsConfigurationException e) {
            errors.add(
                new CmsRuntimeException(
                    Messages.get().container(Messages.ERR_ACTION_MODULE_UPLOAD_1, m_moduleupload),
                    e));
        }

        if ((module != null) && errors.isEmpty()) {

            // refresh the list
            Map objects = (Map)getSettings().getListObject();
            if (objects != null) {
                objects.remove(CmsModulesList.class.getName());
            }

            // redirect
            Map param = new HashMap();
            param.put(CmsModulesList.PARAM_MODULE, m_moduleupload);
            param.put(PARAM_STYLE, CmsToolDialog.STYLE_NEW);
            param.put(PARAM_CLOSELINK, CmsToolManager.linkForToolPath(getJsp(), "/modules"));
            if (OpenCms.getModuleManager().hasModule(module.getName())) {
                param.put(PARAM_MODULENAME, module.getName());
                getToolManager().jspForwardPage(this, REPLACE_ACTION_REPORT, param);
            } else {
                getToolManager().jspForwardPage(this, IMPORT_ACTION_REPORT, param);
            }
        }

        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     *
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            result.append(dialogBlockStart(key("label.uploadfromserver")));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 0));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        // close table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        List selectOptions = getModulesFromServer();

        if (selectOptions.isEmpty()) {
            // no import modules available, display message
            addWidget(
                new CmsWidgetDialogParameter(
                    this,
                    "moduleupload",
                    PAGES[0],
                    new CmsDisplayWidget(key(Messages.GUI_MODULES_IMPORT_NOT_AVAILABLE_0))));
        } else {
            // add the file select box widget
            addWidget(new CmsWidgetDialogParameter(this, "moduleupload", PAGES[0], new CmsSelectWidget(selectOptions)));
        }
    }

    /**
     * Returns the list of all modules available on the server in prepared CmsSelectWidgetOption objects.<p>
     *
     * @return List of module names in CmsSelectWidgetOption objects
     */
    private List getModulesFromServer() {

        List result = new ArrayList();

        // get the systems-exportpath
        String exportpath = OpenCms.getSystemInfo().getPackagesRfsPath();
        exportpath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(exportpath + "modules");
        File folder = new File(exportpath);

        // get a list of all files
        String[] list = folder.list();
        for (int i = 0; i < list.length; i++) {
            try {
                File diskFile = new File(exportpath, list[i]);
                // check if it is a file and ends with zip -> this is a module
                if (diskFile.isFile() && diskFile.getName().endsWith(".zip")) {
                    result.add(new CmsSelectWidgetOption(diskFile.getName()));
                } else
                    if (diskFile.isDirectory() && ((new File(diskFile + File.separator + "manifest.xml")).exists())) {
                    // this is a folder with manifest file -> this a module
                    result.add(new CmsSelectWidgetOption(diskFile.getName()));
                }
            } catch (Throwable t) {
                // ignore and continue
            }
        }

        Collections.sort(result, new ComparatorSelectWidgetOption());
        return result;
    }

    /**
     * Gets the module parameter.<p>
     *
     * @return the module parameter
     */
    public String getModuleupload() {

        return m_moduleupload;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the job (may be changed because of the widget values)
        setDialogObject(m_moduleupload);
    }

    /**
     * Sets the module parameter.<p>
     * @param module the module parameter
     */
    public void setModuleupload(String module) {

        m_moduleupload = module;
    }

}