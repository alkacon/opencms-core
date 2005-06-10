/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/projects/CmsProjectFilesDialog.java,v $
 * Date   : $Date: 2005/06/10 15:58:06 $
 * Version: $Revision: 1.1 $
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

package org.opencms.workplace.tools.projects;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Comment for <code>CmsProjectFilesDialog</code>.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * @version $Revision: 1.1 $
 * @since 5.7.3
 */
public class CmsProjectFilesDialog extends CmsDialog {

    /** Stores the value of the request parameter for the project id. */
    private String m_paramProjectid;

    /** Stores the value of the request parameter for the project name. */
    private String m_paramProjectname;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsProjectFilesDialog(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsProjectFilesDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);

    }

    /**
     * Performs the dialog actions depending on the initialized action and displays the dialog form.<p>
     * 
     * @throws IOException if writing to the JSP out fails
     */
    public void displayDialog() throws IOException {

        if (getAction() == ACTION_CANCEL) {
            return;
        }
        
        int projectId = new Integer(getParamProjectid()).intValue();
        StringBuffer link = new StringBuffer();
        link.append("/system/workplace/views/explorer/explorer_files.jsp?");
        link.append(CmsEditProjectDialog.PARAM_PROJECTID);
        link.append("=");
        link.append(projectId);
        link.append("&mode=projectview");

        getJsp().getResponse().sendRedirect(getJsp().link(link.toString()));
    }

    /**
     * Returns the project id parameter value.<p>
     * 
     * @return the project id parameter value
     */
    public String getParamProjectid() {

        return m_paramProjectid;
    }

    /**
     * Returns the project name parameter value.<p>
     * 
     * @return the project name parameter value
     */
    public String getParamProjectname() {

        return m_paramProjectname;
    }

    /**
     * Sets the project id parameter value.<p>
     * 
     * @param projectId the project id parameter value
     */
    public void setParamProjectid(String projectId) {

        m_paramProjectid = projectId;
    }

    /**
     * Sets the project name parameter value.<p>
     * 
     * @param projectName the project name parameter value
     */
    public void setParamProjectname(String projectName) {

        m_paramProjectname = projectName;
    }

    /**
     * @see org.opencms.workplace.CmsDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        // test the needed parameters
        try {
            getCms().readProject(new Integer(getParamProjectid()).intValue()).getName();
            getCms().readProject(getParamProjectname()).getName();
        } catch (Exception e) {
            // redirect to parent if parameters not available
            setAction(ACTION_CANCEL);
            try {
                actionCloseDialog();
            } catch (JspException e1) {
                // noop
            }
            return;
        }
    }
}
