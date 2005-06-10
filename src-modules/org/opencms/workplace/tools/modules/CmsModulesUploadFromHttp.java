/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/modules/CmsModulesUploadFromHttp.java,v $
 * Date   : $Date: 2005/06/10 15:14:54 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.modules;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.workplace.administration.A_CmsImportFromHttp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Class to upload a module with HTTP upload.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 * @since 6.0
 */
public class CmsModulesUploadFromHttp extends A_CmsImportFromHttp {

    /** The dialog URI. */
    public static final String DIALOG_URI = C_PATH_WORKPLACE + "admin/modules/modules_import.html";

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
     * Imports the selected file from the server.<p>
     */
    public void actionCommit() {

        try {
            copyFileToServer(OpenCms.getSystemInfo().getPackagesRfsPath()
                + File.separator
                + I_CmsConstants.C_MODULE_PATH);
        } catch (CmsException e) {
            // error copying the file to the OpenCms server
            setException(e);
            return;
        }
        try {
            Map params = new HashMap();
            params.put(PARAM_MODULE, getParamImportfile());
            // set style to display report in correct layout
            params.put(PARAM_STYLE, "new");
            // set close link to get back to overview after finishing the import
            params.put(PARAM_CLOSELINK, getToolManager().linkForPath(getJsp(), "/modules", null));
            // redirect to the report output JSP
            getToolManager().jspRedirectPage(this, CmsModulesUploadFromServer.IMPORT_ACTION_REPORT, params);
        } catch (IOException e) {
            throw new CmsRuntimeException(Messages.get().container(
                Messages.ERR_ACTION_MODULE_UPLOAD_1,
                getParamImportfile()), e);
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