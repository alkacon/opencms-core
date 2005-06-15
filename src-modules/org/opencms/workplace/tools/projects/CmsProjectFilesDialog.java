/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/projects/CmsProjectFilesDialog.java,v $
 * Date   : $Date: 2005/06/15 09:27:04 $
 * Version: $Revision: 1.4 $
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

import org.opencms.db.CmsUserSettings;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsExplorer;
import org.opencms.workplace.tools.CmsExplorerDialog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Explorer dialog for the project files view.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * @version $Revision: 1.4 $
 * @since 5.7.3
 */
public class CmsProjectFilesDialog extends CmsExplorerDialog {

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

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the dialog actions depending on the initialized action and displays the dialog form.<p>
     * 
     * @throws Exception if writing to the JSP out fails
     */
    public void displayDialog() throws Exception {

        if (getAction() == ACTION_CANCEL) {
            return;
        }
        getSettings().setExplorerMode(CmsExplorer.C_VIEW_PROJECT);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamProjectid())) {
            getSettings().setExplorerProjectId(new Integer(getParamProjectid()).intValue());
        }
        try {
            String filter = new CmsUserSettings(getCms().getRequestContext().currentUser()).getProjectSettings().getProjectFilesMode().toString();
            getSettings().setExplorerProjectFilter(filter);
        } catch (Exception e) {
            // ignore, if user has no project settings
        }

        getJsp().getResponse().sendRedirect(getJsp().link(C_FILE_EXPLORER_FILELIST));
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
}
