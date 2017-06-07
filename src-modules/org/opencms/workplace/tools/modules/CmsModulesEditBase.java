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

import org.opencms.db.CmsExportPoint;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Base class to edit an exiting module.<p>
 *
 * @since 6.0.0
 */
public class CmsModulesEditBase extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "modules";

    /** The dialog type. */
    public static final String DIALOG_TYPE = "ModulesEdit";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Classes folder within the module. */
    public static final String PATH_CLASSES = "classes/";

    /** Elements folder within the module. */
    public static final String PATH_ELEMENTS = "elements/";

    /** Lib folder within the module. */
    public static final String PATH_LIB = "lib/";

    /** Resources folder within the module. */
    public static final String PATH_RESOURCES = "resources/";

    /** Schemas folder within the module. */
    public static final String PATH_SCHEMAS = "schemas/";

    /** Template folder within the module. */
    public static final String PATH_TEMPLATES = "templates/";

    /** The formatters folder within the module. */
    public static final String PATH_FORMATTERS = "formatters/";

    /** The module object that is edited on this dialog. */
    protected CmsModule m_module;

    /** Module name. */
    protected String m_paramModule;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsModulesEditBase(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsModulesEditBase(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the edited module.<p>
     */
    @Override
    public void actionCommit() {

        if (!hasCommitErrors()) {
            //check if we have to update an existing module or to create a new one
            Set<String> moduleNames = OpenCms.getModuleManager().getModuleNames();
            if (moduleNames.contains(m_module.getName())) {
                // update the module information
                try {
                    OpenCms.getModuleManager().updateModule(getCms(), m_module);
                } catch (CmsException e) {
                    addCommitError(e);
                }
            } else {
                try {
                    m_module = createModuleFolders((CmsModule)m_module.clone());
                    OpenCms.getModuleManager().addModule(getCms(), m_module);
                } catch (CmsException e) {
                    addCommitError(e);
                }
            }
        }

        if (!hasCommitErrors()) {
            // refresh the list
            Map<?, ?> objects = (Map<?, ?>)getSettings().getListObject();
            if (objects != null) {
                objects.remove(CmsModulesList.class.getName());
            }
        }
    }

    /**
     * @see org.opencms.workplace.CmsDialog#getCancelAction()
     */
    @Override
    public String getCancelAction() {

        // set the default action
        setParamPage(getPages().get(0));

        return DIALOG_SET;
    }

    /**
     * Gets the module parameter.<p>
     *
     * @return the module parameter
     */
    public String getParamModule() {

        return m_paramModule;
    }

    /**
     * Sets the module parameter.<p>
     * @param paramModule the module parameter
     */
    public void setParamModule(String paramModule) {

        m_paramModule = paramModule;
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        initModule();
        setKeyPrefix(KEY_PREFIX);
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
     * Initializes the module  to work with depending on the dialog state and request parameters.<p>
     */
    protected void initModule() {

        Object o;

        if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
            // this is the initial dialog call
            if (CmsStringUtil.isNotEmpty(m_paramModule)) {
                // edit an existing module, get it from manager
                o = OpenCms.getModuleManager().getModule(m_paramModule);
            } else {
                // create a new module
                o = null;
            }
        } else {
            // this is not the initial call, get module from session
            o = getDialogObject();
        }

        if (!(o instanceof CmsModule)) {
            // create a new module
            m_module = new CmsModule();

        } else {
            // reuse module stored in session
            m_module = (CmsModule)((CmsModule)o).clone();
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the module (may be changed because of the widget values)
        setDialogObject(m_module);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        String moduleName = getParamModule();
        // check module
        if (!isNewModule()) {
            CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
            if (module == null) {
                throw new Exception();
            }
        }
    }

    /**
     * Creates all module folders that are selected in the input form.<p>
     *
     * @param module the module
     *
     * @return the updated module
     *
     * @throws CmsException if somehting goes wrong
     */
    private CmsModule createModuleFolders(CmsModule module) throws CmsException {

        String modulePath = CmsWorkplace.VFS_PATH_MODULES + module.getName() + "/";
        List<CmsExportPoint> exportPoints = module.getExportPoints();
        List<String> resources = module.getResources();

        // set the createModuleFolder flag if any other flag is set
        if (module.isCreateClassesFolder()
            || module.isCreateElementsFolder()
            || module.isCreateLibFolder()
            || module.isCreateResourcesFolder()
            || module.isCreateSchemasFolder()
            || module.isCreateTemplateFolder()
            || module.isCreateFormattersFolder()) {
            module.setCreateModuleFolder(true);
        }

        // check if we have to create the module folder
        int folderId = CmsResourceTypeFolder.getStaticTypeId();
        if (module.isCreateModuleFolder()) {
            getCms().createResource(modulePath, folderId);
            // add the module folder to the resource list
            resources.add(modulePath);
            module.setResources(resources);
        }

        // check if we have to create the template folder
        if (module.isCreateTemplateFolder()) {
            String path = modulePath + PATH_TEMPLATES;
            getCms().createResource(path, folderId);
        }

        // check if we have to create the elements folder
        if (module.isCreateElementsFolder()) {
            String path = modulePath + PATH_ELEMENTS;
            getCms().createResource(path, folderId);
        }

        if (module.isCreateFormattersFolder()) {
            String path = modulePath + PATH_FORMATTERS;
            getCms().createResource(path, folderId);
        }

        // check if we have to create the schemas folder
        if (module.isCreateSchemasFolder()) {
            String path = modulePath + PATH_SCHEMAS;
            getCms().createResource(path, folderId);
        }

        // check if we have to create the resources folder
        if (module.isCreateResourcesFolder()) {
            String path = modulePath + PATH_RESOURCES;
            getCms().createResource(path, folderId);
        }

        // check if we have to create the lib folder
        if (module.isCreateLibFolder()) {
            String path = modulePath + PATH_LIB;
            getCms().createResource(path, folderId);
            CmsExportPoint exp = new CmsExportPoint(path, "WEB-INF/lib/");
            exportPoints.add(exp);
            module.setExportPoints(exportPoints);
        }

        // check if we have to create the classes folder
        if (module.isCreateClassesFolder()) {
            String path = modulePath + PATH_CLASSES;
            getCms().createResource(path, folderId);
            CmsExportPoint exp = new CmsExportPoint(path, "WEB-INF/classes/");
            exportPoints.add(exp);
            module.setExportPoints(exportPoints);

            // now create all subfolders for the package structure
            StringTokenizer tok = new StringTokenizer(m_module.getName(), ".");
            while (tok.hasMoreTokens()) {
                String folder = tok.nextToken();
                path += folder + "/";
                getCms().createResource(path, folderId);
            }
        }
        return module;
    }

    /**
     * Checks if the new module dialog has to be displayed.<p>
     *
     * @return <code>true</code> if the new module dialog has to be displayed
     */
    private boolean isNewModule() {

        return getCurrentToolPath().equals("/modules/modules_new");
    }
}