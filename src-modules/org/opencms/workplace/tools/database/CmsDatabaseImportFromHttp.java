/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/database/CmsDatabaseImportFromHttp.java,v $
 * Date   : $Date: 2005/06/19 10:57:05 $
 * Version: $Revision: 1.3 $
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

package org.opencms.workplace.tools.database;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.administration.A_CmsImportFromHttp;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Class to upload a zip file containing VFS resources with HTTP upload.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 * @since 6.0
 */
public class CmsDatabaseImportFromHttp extends A_CmsImportFromHttp {

    /** The dialog URI. */
    public static final String DIALOG_URI = C_PATH_WORKPLACE + "admin/database/importhttp.html";

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDatabaseImportFromHttp(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDatabaseImportFromHttp(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.administration.A_CmsImportFromHttp#actionCommit()
     */
    public void actionCommit() throws IOException, ServletException {

        try {
            copyFileToServer(OpenCms.getSystemInfo().getPackagesRfsPath());
        } catch (CmsException e) {
            // error copying the file to the OpenCms server
            setException(e);
            return;
        }
        Map params = new HashMap();
        params.put(PARAM_FILE, getParamImportfile());
        // set style to display report in correct layout
        params.put(PARAM_STYLE, "new");
        // set close link to get back to overview after finishing the import
        params.put(PARAM_CLOSELINK, CmsToolManager.linkForToolPath(getJsp(), "/database"));
        // redirect to the report output JSP
        getToolManager().jspForwardPage(this, CmsDatabaseImportFromServer.IMPORT_ACTION_REPORT, params);
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

        return key(Messages.GUI_DATABASE_IMPORT_FILE_0);
    }

    /**
     * @see org.opencms.workplace.administration.A_CmsImportFromHttp#getStarttext()
     */
    public String getStarttext() {

        return key(Messages.GUI_DATABASE_IMPORT_BLOCK_0);
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