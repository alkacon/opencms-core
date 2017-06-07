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

package org.opencms.workplace.list;

import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Explorer dialog for the project files view.<p>
 *
 * @since 6.0.0
 */
public class CmsListExplorerFrameset extends CmsExplorerDialog {

    /** Page parameter name. */
    public static final String PARAM_PAGE = "explorer_page";

    /** Title uri parameter name. */
    public static final String PARAM_TITLE_URI = "title_uri";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsListExplorerFrameset.class);

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
        String titleUri = CmsEncoder.escapeXml(getJsp().getRequest().getParameter(PARAM_TITLE_URI));
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(titleUri)) {
            titleUri = CmsToolManager.ADMINVIEW_ROOT_LOCATION + "/list-title.jsp";
        }
        String titleSrc = getFrameSource("tool_title", getJsp().link(titleUri + "?" + params));
        String contentSrc = getFrameSource(
            "tool_content",
            getJsp().link(CmsToolManager.ADMINVIEW_ROOT_LOCATION + "/list-explorer.jsp") + "?" + params);
        StringBuffer html = new StringBuffer(1024);
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("\t<head>\n");
        html.append("\t\t<meta HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=\"").append(
            getEncoding()).append("\">\n");
        String uplink = "/opencms_a/opencms/system/workplace/views/admin/admin-main.jsp?path=/projects/files&action=initial&projectid=fa9f561f-be30-11e2-bac3-21ebf444feef&showexplorer=true";
        html.append(
            "<script type='text/javascript'>var uplink = \""
                + CmsStringUtil.escapeJavaScript(uplink)
                + "\";</script>\n");
        html.append("\t\t<title>\n");
        html.append(
            "\t\t\t").append(
                key(
                    org.opencms.workplace.Messages.GUI_LABEL_WPTITLE_1,
                    new Object[] {getSettings().getUser().getFullName()})).append("\n");
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
    @Override
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
        String listLevelLink = CmsToolManager.linkForToolPath(
            getJsp(),
            toolPath,
            getToolManager().resolveAdminTool(rootKey, toolPath).getHandler().getParameters(this));
        listLevelLink = CmsRequestUtil.appendParameter(
            listLevelLink,
            A_CmsListExplorerDialog.PARAM_SHOW_EXPLORER,
            Boolean.FALSE.toString());
        String parentName = getToolManager().resolveAdminTool(rootKey, parentPath).getHandler().getName();

        html.append(getToolManager().generateNavBar(toolPath, this));

        // check if page switch needed
        int items = 0;
        try {
            items = getSettings().getCollector().getResults(getCms()).size();
        } catch (CmsException e) {
            // ignore
            LOG.error(e.getLocalizedMessage(), e);
        }

        int size = (int)Math.ceil((double)items / getSettings().getUserSettings().getExplorerFileEntries());
        // build title
        html.append("<div class='screenTitle'>\n");
        html.append("\t<table width='100%' cellspacing='0'>\n");
        html.append("\t\t<tr>\n");
        html.append("\t\t\t<td>\n");
        html.append(getAdminTool().getHandler().getName());
        html.append("\n\t\t\t</td>\n");
        html.append("\t\t\t<td class='uplevel'>\n");
        html.append("<form name='title-form' method='post' target='_parent' action='");
        html.append(getJsp().link(A_CmsListExplorerDialog.PATH_EXPLORER_LIST)).append("'>\n");
        html.append(getFormContent());
        // if page switch needed
        if (size > 1) {
            html.append("<select name='").append(PARAM_PAGE);
            html.append("' class='location' onchange='this.form.submit()'>\n");
            html.append(
                CmsHtmlList.htmlPageSelector(
                    size,
                    getSettings().getUserSettings().getExplorerFileEntries(),
                    items,
                    getSettings().getExplorerPage(),
                    getLocale()));
            html.append("</select>\n");
        }
        // list view button
        CmsMessages messages = Messages.get().getBundle(getLocale());
        html.append(A_CmsHtmlIconButton.defaultButtonHtml(
            CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
            "id-list-switch",
            messages.key(Messages.GUI_LIST_ACTION_LIST_SWITCH_NAME_0),
            messages.key(Messages.GUI_LIST_ACTION_LIST_SWITCH_HELP_0),
            true,
            "list/list.png",
            null,
            "openPage('" + listLevelLink + "');"));
        html.append("\n");
        // uplevel button only if needed
        if (!toolPath.equals(getParentPath())) {
            html.append(A_CmsHtmlIconButton.defaultButtonHtml(
                CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
                "id-up-level",
                org.opencms.workplace.tools.Messages.get().getBundle(getLocale()).key(
                    org.opencms.workplace.tools.Messages.GUI_ADMIN_VIEW_UPLEVEL_0),
                parentName,
                true,
                "admin/images/up.png",
                null,
                "openPage('" + upLevelLink + "');"));
        }
        html.append("</form>");
        html.append("\n\t\t\t</td>\n");
        html.append("\t\t</tr>\n");
        html.append("\t</table>\n");
        html.append("</div>\n");

        String code = html.toString().replaceAll("openPage\\('([^']+)'\\)", "openPageIn('$1', parent)");
        return CmsToolMacroResolver.resolveMacros(code, this);
    }

    /**
     * Performs the dialog actions depending on the initialized action and displays the dialog form.<p>
     *
     * @throws ServletException if forwarding explorer view fails
     * @throws IOException if forwarding explorer view fails
     */
    public void displayDialog() throws IOException, ServletException {

        getSettings().setExplorerMode(CmsExplorer.VIEW_LIST);
        Map<String, String[]> addParams = new HashMap<String, String[]>();
        HttpServletRequest req = getJsp().getRequest();
        String addParamsVal = req.getParameter(CmsExplorer.PARAMETER_CONTEXTMENUPARAMS);
        if (addParamsVal != null) {
            addParams.put(CmsExplorer.PARAMETER_CONTEXTMENUPARAMS, new String[] {addParamsVal});
        }
        getToolManager().jspForwardPage(this, FILE_EXPLORER_FILELIST, addParams);
    }

    /**
     * Validates the needed parameters and display the frameset.<p>
     *
     * @throws IOException in case of errros displaying to the required page
     */
    public void displayFrameSet() throws IOException {

        if (getJsp().getRequest().getParameter(CmsListExplorerFrameset.PARAM_PAGE) != null) {
            int page = Integer.parseInt(getJsp().getRequest().getParameter(CmsListExplorerFrameset.PARAM_PAGE));
            getSettings().setExplorerPage(page);

            if (getSettings().getCollector() instanceof I_CmsListResourceCollector) {
                I_CmsListResourceCollector collector = (I_CmsListResourceCollector)getSettings().getCollector();
                collector.setPage(page);
            }
        }
        JspWriter out = getJsp().getJspContext().getOut();
        out.print(defaultActionHtml());
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#paramsAsHidden(java.util.Collection)
     */
    @Override
    public String paramsAsHidden(Collection<String> excludes) {

        StringBuffer result = new StringBuffer(512);
        Map<String, String[]> params = CmsCollectionsGenericWrapper.map(getJsp().getRequest().getParameterMap());
        params = new HashMap<String, String[]>(params);
        params.remove(CmsListExplorerFrameset.PARAM_PAGE);
        Iterator<Map.Entry<String, String[]>> it = params.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String[]> entry = it.next();
            String param = entry.getKey();
            if ((excludes == null) || (!excludes.contains(param))) {
                String[] value = entry.getValue();
                for (int i = 0; i < value.length; i++) {
                    result.append("<input type=\"hidden\" name=\"");
                    result.append(param);
                    result.append("\" value=\"");
                    result.append(CmsEncoder.encode(value[i], getCms().getRequestContext().getEncoding()));
                    result.append("\">\n");
                }
            }
        }
        return result.toString();
    }

    /**
     * Returns the form contents.<p>
     *
     * @return the form contents
     */
    protected String getFormContent() {

        return paramsAsHidden(Collections.singleton(PARAM_PAGE));
    }

    /**
     * @see org.opencms.workplace.CmsDialog#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        setParamStyle(CmsToolDialog.STYLE_NEW);
        super.initWorkplaceRequestValues(settings, request);
    }

}
