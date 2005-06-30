/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/modules/CmsModulesUploadFromHttp.java,v $
 * Date   : $Date: 2005/06/30 10:57:18 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleDependency;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.module.CmsModuleManager;
import org.opencms.workplace.administration.A_CmsImportFromHttp;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Class to upload a module with HTTP upload.<p>
 * 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.0.0 
 */
public class CmsModulesUploadFromHttp extends A_CmsImportFromHttp {

    /** The dialog URI. */
    public static final String DIALOG_URI = PATH_WORKPLACE + "admin/modules/modules_import.html";

    /** Modulename parameter. */
    public static final String PARAM_MODULE = "module";

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsModulesUploadFromHttp(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsModulesUploadFromHttp(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.administration.A_CmsImportFromHttp#actionCommit()
     */
    public void actionCommit() throws IOException, ServletException {

        try {
            copyFileToServer(OpenCms.getSystemInfo().getPackagesRfsPath()
                + File.separator
                + CmsSystemInfo.FOLDER_MODULES);
        } catch (CmsException e) {
            // error copying the file to the OpenCms server
            setException(e);
            return;
        }
        /// copied
        List errors = new ArrayList();
        CmsModule module = null;
        try {
            String importpath = OpenCms.getSystemInfo().getPackagesRfsPath();
            importpath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
                importpath + "modules/" + getParamImportfile());
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
                errors.add(new CmsRuntimeException(Messages.get().container(
                    Messages.ERR_ACTION_MODULE_DEPENDENCY_2,
                    getParamImportfile(),
                    new String(dep))));
            }

        } catch (CmsConfigurationException e) {
            errors.add(new CmsRuntimeException(Messages.get().container(
                Messages.ERR_ACTION_MODULE_UPLOAD_1,
                getParamImportfile()), e));
        }

        if (errors.isEmpty()) {

            // refresh the list
            Map objects = (Map)getSettings().getListObject();
            if (objects != null) {
                objects.remove(CmsModulesList.class.getName());
            }

            // redirect
            Map param = new HashMap();
            param.put(CmsModulesList.PARAM_MODULE, getParamImportfile());
            param.put(PARAM_STYLE, "new");
            param.put(PARAM_CLOSELINK, CmsToolManager.linkForToolPath(getJsp(), "/modules"));
            if (OpenCms.getModuleManager().hasModule(module.getName())) {
                param.put(CmsModulesUploadFromServer.PARAM_MODULENAME, module.getName());
                getToolManager().jspForwardPage(this, CmsModulesUploadFromServer.REPLACE_ACTION_REPORT, param);
            } else {
                getToolManager().jspForwardPage(this, CmsModulesUploadFromServer.IMPORT_ACTION_REPORT, param);
            }
        }

    }

    /**
     * @see org.opencms.workplace.administration.A_CmsImportFromHttp#getDialogReturnUri()
     */
    public String getDialogReturnUri() {

        return DIALOG_URI;
    }

    /**
     * @see org.opencms.workplace.administration.A_CmsImportFromHttp#getImportMessage()
     */
    public String getImportMessage() {

        return key(Messages.GUI_MODULES_IMPORT_FILE_0);
    }

    /**
     * @see org.opencms.workplace.administration.A_CmsImportFromHttp#getStarttext()
     */
    public String getStarttext() {

        return key(Messages.GUI_MODULES_IMPORT_BLOCK_0);
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        addMessages(org.opencms.workplace.Messages.get().getBundleName());
        addMessages(org.opencms.workplace.tools.Messages.get().getBundleName());
    }
}