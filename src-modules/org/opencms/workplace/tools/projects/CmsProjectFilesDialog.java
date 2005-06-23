/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/projects/CmsProjectFilesDialog.java,v $
 * Date   : $Date: 2005/06/23 10:11:48 $
 * Version: $Revision: 1.8 $
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

import org.opencms.db.CmsProjectResourcesDisplayMode;
import org.opencms.db.CmsUserProjectSettings;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.explorer.CmsExplorer;
import org.opencms.workplace.tools.CmsExplorerDialog;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Explorer dialog for the project files view.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.0.0 
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
     * Generates the dialog starting html code.<p>
     * 
     * @return html code
     */
    public String defaultActionHtml() {

        String titleSrc = getFrameSource("tool_title", getJsp().link(
            CmsToolManager.C_ADMINVIEW_ROOT_LOCATION + "/tool-title.html")
            + "?"
            + allParamsAsRequest());
        String contentSrc = getFrameSource("tool_content", getJsp().link(
            "/system/workplace/admin/projects/project_files.html")
            + "?"
            + allParamsAsRequest());
        StringBuffer html = new StringBuffer(1024);
        html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">\n");
        html.append("<html>\n");
        html.append("\t<head>\n");
        html.append("\t\t<meta HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=\"").append(getEncoding()).append(
            "\">\n");
        html.append("\t\t<title>\n");
        html.append("\t\t\t").append(key("label.wptitle")).append(" ").append(getSettings().getUser().getName()).append(
            "@").append(getJsp().getRequest().getServerName()).append("\n");
        html.append("\t\t</title>\n");
        html.append("\t</head>\n");
        html.append("\t<frameset rows='57,*' border='0' frameborder='0' framespacing='0'>\n");
        html.append("\t\t<frame ").append(titleSrc).append(" frameborder='0' border='0' noresize scrolling='no'>\n");
        html.append("\t\t<frame ").append(contentSrc).append(
            " frameborder='0' border='0' noresize scrolling='auto' framespacing='0' marginheight='2' marginwidth='2' >\n");
        html.append("\t</frameset>\n");
        html.append("</html>\n");
        return html.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#displayDialog()
     */
    public void displayDialog() throws IOException, ServletException {

        displayExplorerView();
    }

    /**
     * Performs the dialog actions depending on the initialized action and displays the dialog form.<p>
     * 
     * @throws ServletException if forwarding explorer view fails
     * @throws IOException if forwarding explorer view fails
     */
    public void displayExplorerView() throws IOException, ServletException {

        getSettings().setExplorerMode(CmsExplorer.C_VIEW_PROJECT);
        try {
            getSettings().setExplorerProjectId(new Integer(getParamProjectid()).intValue());
        } catch (Exception e) {
            // ignore
        }
        boolean modeSet = false;
        try {
            CmsUser user = getCms().getRequestContext().currentUser();
            if (user != null) {
                CmsUserSettings settings = new CmsUserSettings(user);
                CmsUserProjectSettings prjSettings = settings.getProjectSettings();
                if (prjSettings != null) {
                    CmsProjectResourcesDisplayMode filter = prjSettings.getProjectFilesMode();
                    if (filter != null) {
                        getSettings().setExplorerProjectFilter(filter.toString());
                        modeSet = true;
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        if (!modeSet) {
            // set default value if user has no settings
            getSettings().setExplorerProjectFilter(CmsProjectResourcesDisplayMode.ALL_CHANGES.toString());
        }
        Map params = new HashMap();
        params.put(CmsExplorer.C_PARAMETER_PROJECTFILTER, getSettings().getExplorerProjectFilter());
        params.put(CmsExplorer.C_PARAMETER_PROJECTID, new Integer(getSettings().getExplorerProjectId()));
        getToolManager().jspForwardPage(this, C_FILE_EXPLORER_FILELIST, params);
    }

    /**
     * Validates the needed parameters and display the frameset.<p>
     * 
     * @throws JspException if close action fail
     * @throws IOException in case of errros displaying to the required page
     */
    public void displayFrameSet() throws JspException, IOException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamProjectid())
            || CmsStringUtil.isEmptyOrWhitespaceOnly(getParamProjectname())) {
            setAction(ACTION_CANCEL);
            actionCloseDialog();
            return;
        }

        JspWriter out = getJsp().getJspContext().getOut();
        out.print(defaultActionHtml());
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
     * @see org.opencms.workplace.tools.CmsExplorerDialog#defineWidgets()
     */
    protected void defineWidgets() {

        // retrieve the stored project id
        Object o = getDialogObject();
        if (o != null) {
            setParamProjectid(o.toString());
        }
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        // save the current params
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamProjectid())) {
            setDialogObject(getParamProjectid());
        }
    }
}
