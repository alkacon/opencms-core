/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/check/CmsContentCheckFilesDialog.java,v $
 * Date   : $Date: 2005/10/19 08:33:28 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.workplace.tools.content.check;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.explorer.CmsExplorer;
import org.opencms.workplace.tools.CmsExplorerDialog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Explorer dialog for the content check files view.<p>
 * 
 * @author Michael Emmerich
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.1.0 
 */
public class CmsContentCheckFilesDialog extends CmsExplorerDialog {

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsContentCheckFilesDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsContentCheckFilesDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Generates the dialog starting html code.<p>
     * 
     * @return html code
     */
    public String defaultActionHtml() {

        String params = allParamsAsRequest();
        if (params.indexOf("projectid=") < 0) {
            params += "&projectid=" + getCms().getRequestContext().currentProject().getId();
        }
        String titleSrc = getFrameSource("tool_title", getJsp().link(
            "/system/workplace/admin/contenttools/check/result-fs-title.html")
            + "?"
            + params);
        String contentSrc = getFrameSource("tool_content", getJsp().link(
            "/system/workplace/admin/projects/project_files.html")
            + "?"
            + params);
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

        getSettings().setExplorerMode(CmsExplorer.VIEW_PROJECT);
        try {
            getSettings().setExplorerProjectId(getCms().getRequestContext().currentProject().getId());
        } catch (Exception e) {
            // ignore
        }

        Map params = new HashMap();
        params.put(CmsExplorer.PARAMETER_MODE, getSettings().getExplorerMode());
        params.put(CmsExplorer.PARAMETER_PROJECTID, new Integer(getSettings().getExplorerProjectId()));
        getToolManager().jspForwardPage(this, FILE_EXPLORER_FILELIST, params);
    }

    /**
     * Validates the needed parameters and display the frameset.<p>
     * 
     * @throws IOException in case of errros displaying to the required page
     */
    public void displayFrameSet() throws IOException {

        JspWriter out = getJsp().getJspContext().getOut();
        out.print(defaultActionHtml());
    }

    /**
     * @see org.opencms.workplace.tools.CmsExplorerDialog#defineWidgets()
     */
    protected void defineWidgets() {

        // noop 
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(org.opencms.workplace.tools.content.Messages.get().getBundleName());
        addMessages("org.opencms.workplace.workplace");
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);

    }
}
