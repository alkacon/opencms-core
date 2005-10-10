/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/modules/CmsModulesDelete.java,v $
 * Date   : $Date: 2005/10/10 10:53:19 $
 * Version: $Revision: 1.7.2.1 $
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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsToolDialog;
import org.opencms.workplace.tools.CmsToolManager;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides an confirm screen for module deletion.<p> 
 *
 * @author  Michael Emmerich
 * 
 * @version $Revision: 1.7.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsModulesDelete extends CmsDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "DeleteModuleConfirm";

    /** Modulename parameter. */
    public static final String PARAM_MODULE = "module";

    /** The delete action. */
    private static final String DELETE_ACTION_REPORT = "/system/workplace/admin/modules/reports/delete.html";

    /** Modulename. */
    protected String m_paramModule;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsModulesDelete(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsModulesDelete(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the re-initialization report, will be called by the JSP page.<p>
     * 
     * @throws JspException if including the error JSP element fails
     */
    public void actionReport() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        switch (getAction()) {
            case ACTION_CONFIRMED:
            default:
                try {
                    Map params = new HashMap();
                    params.put(PARAM_MODULE, getParamModule());
                    // set style to display report in correct layout
                    params.put(PARAM_STYLE, CmsToolDialog.STYLE_NEW);
                    // set close link to get back to overview after finishing the import
                    params.put(PARAM_CLOSELINK, CmsToolManager.linkForToolPath(getJsp(), "/modules"));
                    // redirect to the report output JSP
                    getToolManager().jspForwardPage(this, DELETE_ACTION_REPORT, params);

                    actionCloseDialog();
                } catch (Throwable e) {
                    // create a new Exception with custom message
                    includeErrorpage(this, e);
                }
                break;
        }
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
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_CONFIRMED);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // add the title for the dialog 
            setParamTitle(key(Messages.GUI_DELETEMODULE_ADMIN_TOOL_NAME_0));
        }
    }
}