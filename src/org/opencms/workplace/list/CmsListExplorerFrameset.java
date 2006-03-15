/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsListExplorerFrameset.java,v $
 * Date   : $Date: 2006/03/15 10:19:55 $
 * Version: $Revision: 1.1.2.2 $
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

package org.opencms.workplace.list;

import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.explorer.CmsExplorer;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsExplorerDialog;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;
import org.opencms.workplace.tools.CmsTool;
import org.opencms.workplace.tools.CmsToolDialog;
import org.opencms.workplace.tools.CmsToolMacroResolver;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsListExplorerFrameset extends CmsExplorerDialog {

    /** Page parameter name. */
    public static final String PARAM_PAGE = "explorer_page";

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsListExplorerFrameset(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsListExplorerFrameset(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Generates the dialog starting html code.<p>
     * 
     * @return html code
     */
    public String defaultActionHtml() {

        String params = allParamsAsRequest();
        String titleSrc = getFrameSource("tool_title", getJsp().link(
            CmsToolManager.ADMINVIEW_ROOT_LOCATION + "/list-title.html")
            + "?"
            + params);
        String contentSrc = getFrameSource("tool_content", getJsp().link(
            CmsToolManager.ADMINVIEW_ROOT_LOCATION + "/list-explorer.html")
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
     * @see org.opencms.workplace.tools.CmsToolDialog#dialogTitle()
     */
    public String dialogTitle() {

        StringBuffer html = new StringBuffer(512);
        String toolPath = getCurrentToolPath();
        String parentPath = getParentPath();
        String rootKey = getToolManager().getCurrentRoot(this).getKey();
        CmsTool parentTool = getToolManager().resolveAdminTool(rootKey, parentPath);
        String upLevelLink = CmsToolManager.linkForToolPath(
            getJsp(),
            parentPath,
            parentTool.getHandler().getParameters(this));
        String listLevelLink = CmsToolManager.linkForToolPath(getJsp(), toolPath, getToolManager().resolveAdminTool(
            rootKey,
            toolPath).getHandler().getParameters(this));
        String parentName = getToolManager().resolveAdminTool(rootKey, parentPath).getHandler().getName();

        html.append(getToolManager().generateNavBar(toolPath, this));

        // check if page switch needed
        int size = 1;
        try {
            size = getSettings().getCollector().getResults(getCms()).size()
                / getSettings().getUserSettings().getExplorerFileEntries()
                + 1;
        } catch (CmsException e) {
            // ignore
        }
        // build title
        html.append("<div class='screenTitle'>\n");
        html.append("\t<table width='100%' cellspacing='0'>\n");
        html.append("\t\t<tr>\n");
        html.append("\t\t\t<td>\n");
        html.append(getAdminTool().getHandler().getName());
        html.append("\n\t\t\t</td>\n");
        html.append("\t\t\t<td class='uplevel'>\n");
        // if page switch needed
        if (size > 1) {
            html.append("<form name='page_switch' method='post' target='admin_content' action='");
            html.append(getJsp().link(A_CmsListExplorerDialog.PATH_EXPLORER_LIST)).append("'>\n");
        }
        // list view button
        html.append(A_CmsHtmlIconButton.defaultButtonHtml(
            getJsp(),
            CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
            "id-list-switch",
            Messages.get().key(getLocale(), Messages.GUI_LIST_ACTION_LIST_SWITCH_NAME_0, null),
            Messages.get().key(getLocale(), Messages.GUI_LIST_ACTION_LIST_SWITCH_HELP_0, null),
            true,
            "list/list.png",
            null,
            "openPage('" + listLevelLink + "');"));
        html.append("\n");
        // if page switch needed
        if (size > 1) {
            Map params = new HashMap(getJsp().getRequest().getParameterMap());
            params.remove(CmsListExplorerFrameset.PARAM_PAGE);
            Iterator it = params.keySet().iterator();
            while (it.hasNext()) {
                String param = (String)it.next();
                String[] value = (String[])params.get(param);
                for (int i = 0; i < value.length; i++) {
                    html.append("<input type=\"hidden\" name=\"");
                    html.append(param);
                    html.append("\" value=\"");
                    String encoded = CmsEncoder.encode(value[i], getCms().getRequestContext().getEncoding());
                    html.append(encoded);
                    html.append("\">\n");
                }
            }

            html.append("<span class='menu'>&nbsp;&nbsp;Page:&nbsp;</span>\n");
            html.append("<select name='explorer_page' class='location' onchange='this.form.submit()'>\n");
            for (int i = 1; i <= size; i++) {
                String selected = "";
                if (getSettings().getExplorerPage() == i) {
                    selected = " selected";
                }
                html.append("<option value='").append(i).append("'").append(selected).append(">");
                html.append(i).append("</option>\n");
            }
            html.append("</select>\n");
        }
        // uplevel button only if needed
        if (getParentPath() != toolPath) {
            html.append(A_CmsHtmlIconButton.defaultButtonHtml(
                getJsp(),
                CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
                "id-up-level",
                org.opencms.workplace.tools.Messages.get().key(
                    getLocale(),
                    org.opencms.workplace.tools.Messages.GUI_ADMIN_VIEW_UPLEVEL_0,
                    null),
                parentName,
                true,
                "admin/images/up.png",
                null,
                "openPage('" + upLevelLink + "');"));
        }
        // if page switch needed
        if (size > 1) {
            html.append("</form>");
        }
        html.append("\n\t\t\t</td>\n");
        html.append("\t\t</tr>\n");
        html.append("\t</table>\n");
        html.append("</div>\n");
        return CmsToolMacroResolver.resolveMacros(html.toString(), this);
    }

    /**
     * Performs the dialog actions depending on the initialized action and displays the dialog form.<p>
     * 
     * @throws ServletException if forwarding explorer view fails
     * @throws IOException if forwarding explorer view fails
     */
    public void displayDialog() throws IOException, ServletException {

        getSettings().setExplorerMode(CmsExplorer.VIEW_LIST);
        getToolManager().jspForwardPage(this, FILE_EXPLORER_FILELIST, null);
    }

    /**
     * Validates the needed parameters and display the frameset.<p>
     * 
     * @throws JspException if close action fail
     * @throws IOException in case of errros displaying to the required page
     */
    public void displayFrameSet() throws JspException, IOException {

        try {
            // validate parameters
        } catch (Exception e) {
            setAction(ACTION_CANCEL);
            actionCloseDialog();
            return;
        }

        if (getJsp().getRequest().getParameter(CmsListExplorerFrameset.PARAM_PAGE) != null) {
            int page = Integer.parseInt(getJsp().getRequest().getParameter(CmsListExplorerFrameset.PARAM_PAGE));
            getSettings().setExplorerPage(page);
        }
        JspWriter out = getJsp().getJspContext().getOut();
        out.print(defaultActionHtml());
    }

    /**
     * @see org.opencms.workplace.CmsDialog#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.CmsDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        setParamStyle(CmsToolDialog.STYLE_NEW);
        super.initWorkplaceRequestValues(settings, request);
    }

}
