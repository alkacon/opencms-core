/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/CmsToolDialog.java,v $
 * Date   : $Date: 2005/02/16 11:43:02 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Just a help class that encapsulates all the code for the "new" 
 * style of the administration dialogs.<p>
 * 
 * @author <a href="mailto:m.moossen@alkacon.com">Michael Moossen</a> 
 * @version $Revision: 1.1 $
 * @since 6.0
 */
public class CmsToolDialog extends CmsWorkplace {

    /** Request parameter name for the tool path. */
    public static final String PARAM_PATH = "path";
    /** Request parameter name for the root tool path. */
    public static final String PARAM_ROOT = "root";
    /** Request parameter name for the style type. */
    public static final String PARAM_STYLE = "style";

    private String m_paramPath;
    private String m_paramRoot;
    private String m_paramStyle;

    /**
     * Default Ctor.<p>
     * 
     * @param jsp the jsp action element
     */
    public CmsToolDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Builds the standard javascript for submitting the dialog.<p>
     * 
     * Should only be used by the <code>{@link CmsDialog#dialogScriptSubmit()}</code> method.<p>
     * 
     * @return the standard javascript for submitting the dialog
     */
    public String dialogScriptSubmit() {

        StringBuffer html = new StringBuffer(512);
        html.append(CmsStringUtil.code("function submitAction(actionValue, theForm, formName) {"));
        html.append(CmsStringUtil.code(1, "if (theForm == null) {"));
        html.append(CmsStringUtil.code(2, "theForm = document.forms[formName];"));
        html.append(CmsStringUtil.code(1, "}"));
        html.append(CmsStringUtil.code(1, "theForm." + CmsDialog.PARAM_FRAMENAME + ".value = window.name;"));
        html.append(CmsStringUtil.code(1, "if (actionValue == '" + CmsDialog.DIALOG_OK + "') {"));
        html.append(CmsStringUtil.code(2, "loadingOn();"));
        html.append(CmsStringUtil.code(2, "return true;"));
        html.append(CmsStringUtil.code(1, "}"));
        html.append(CmsStringUtil.code(1, "theForm." + CmsDialog.PARAM_ACTION + ".value = actionValue;"));
        html.append(CmsStringUtil.code(1, "submitForm(theForm);"));
        html.append(CmsStringUtil.code(1, "return true;"));
        html.append(CmsStringUtil.code("}"));
        return html.toString();
    }

    /**
     * This method generates the standard new style dialog title row, and tool grouping.<p>
     * 
     * It is called by the <code>{@link org.opencms.workplace.CmsDialog#dialog(int, String)}</code> method.<p>
     * 
     * @return a dialog window start / end segment
     */
    public String dialogTitle() {

        StringBuffer html = new StringBuffer(512);
        String toolPath = getCurrentToolPath();
        String parentPath = getParentPath();
        String upLevelLink = getToolManager().cmsLinkForPath(getJsp(), parentPath);
        String parentName = getToolManager().resolveAdminTool(getCms(), parentPath).getName();

        html.append(CmsStringUtil.code(4, getToolManager().generateNavBar(toolPath, this)));
        // build titel
        html.append(CmsStringUtil.code(4, "<div class='screenTitle'>"));
        html.append(CmsStringUtil.code(5, "<table width='100%' cellspacing='0'>"));
        html.append(CmsStringUtil.code(6, "<tr>"));
        html.append(CmsStringUtil.code(7, "<td>" + resolveMacros(getAdminTool().getName()) + "</td>"));
        // uplevel button only if needed
        if (getParentPath() != toolPath) {
            html.append(CmsStringUtil.code(7, "<td class='uplevel'>"));
            html.append(CmsStringUtil.code(8, "<div class='commonButton' id='id-up-level' title='"
                + key("admin.view.uplevel")
                + "' onMouseOver=\"mouseHelpEvent('"
                + "uplevel-tip"
                + "', true);\" onMouseOut=\"mouseHelpEvent('"
                + "uplevel-tip"
                + "', false);\" onClick=\"openPage('"
                + upLevelLink
                + "'); \">"));
            html.append(CmsStringUtil.code(9, "<button name='name_up_level'>"
                + key("admin.view.uplevel")
                + "</button><span style=\'background-image: url("
                + getSkinUri()
                + "buttons/up.gif"
                + ")\' >"
                + key("admin.view.uplevel")
                + "</span>"));
            html.append(CmsStringUtil.code(9, "<div class='tip' id='" + "uplevel-tip" + "'>"));
            html.append(CmsStringUtil.code(10, resolveMacros(parentName)));
            html.append(CmsStringUtil.code(9, "</div>"));
            html.append(CmsStringUtil.code(8, "</div>"));
            html.append(CmsStringUtil.code(7, "</td>"));
        }
        html.append(CmsStringUtil.code(6, "</tr>"));
        html.append(CmsStringUtil.code(5, "</table>"));
        html.append(CmsStringUtil.code(4, "</div>"));
        return html.toString();
    }

    /**
     * Returns the tool manager.<p>
     * 
     * @return the tool manager
     */
    public CmsToolManager getToolManager() {

        return OpenCms.getWorkplaceManager().getToolManager();
    }

    /**
     * Returns the admin tool.<p>
     *
     * @return the admin tool
     */
    public CmsTool getAdminTool() {

        return getToolManager().getCurrentTool(getCms());
    }

    /**
     * Returns the current tool path.<p>
     *
     * @return the current tool path
     */
    public String getCurrentToolPath() {

        return getToolManager().getCurrentToolPath(getCms());
    }

    /**
     * Returns the path parameter value.<p>
     *
     * @return the path parameter value
     */
    public String getParamPath() {

        return m_paramPath;
    }

    /**
     * Returns the root parameter value.<p>
     *
     * @return the root parameter value
     */
    public String getParamRoot() {

        return m_paramRoot;
    }

    /**
     * Returns the style parameter value.<p>
     *
     * @return the style parameter value
     */
    public String getParamStyle() {

        return m_paramStyle;
    }

    /**
     * Returns the path to the parent tool.<p>
     * 
     * @return tha path to the parent tool
     */
    public String getParentPath() {

        return getToolManager().getParent(getCms(), getCurrentToolPath());
    }

    /**
     * Builds an block area for icons.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param headline the headline String for the block

     * @return block area start / end segment
     * 
     * @see CmsDialog#dialogBlock(int, String, boolean)
     */
    public String iconsBlockArea(int segment, String headline) {

        StringBuffer retValue = new StringBuffer(512);
        if (segment == HTML_START) {
            retValue.append(CmsStringUtil.code("<p>&nbsp;</p>"));
            retValue.append(CmsStringUtil.code("<!-- icons block area start -->"));
            retValue.append(CmsStringUtil
                .code("<div class=\"dialogblockborder dialogblockborderheadline iconblock\" unselectable=\"on\" >"));
            retValue.append(CmsStringUtil.code(1, "<div class=\"dialogblock\" unselectable=\"on\">"));
            retValue.append(CmsStringUtil.code(2, "<span class=\"dialogblockhead\" unselectable=\"on\">"));
            retValue.append(CmsStringUtil.code(3, headline));
            retValue.append(CmsStringUtil.code(2, "</span>"));
            retValue.append(CmsStringUtil.code(
                2,
                "<table class='toolsArea' width='100%' cellspacing='0' cellpadding='0' border='0'>"));
            retValue.append(CmsStringUtil.code(3, "<tr><td>"));
        } else {
            retValue.append(CmsStringUtil.code(3, "</td></tr>"));
            retValue.append(CmsStringUtil.code(2, "</table>"));
            retValue.append(CmsStringUtil.code(1, "</div>"));
            retValue.append(CmsStringUtil.code("</div>"));
            retValue.append(CmsStringUtil.code("<!-- icons block area end -->"));
        }
        return retValue.toString();
    }

    /**
     * Builds the end HTML for a block area with border in the dialog content area.<p>
     * 
     * @return block area end segment
     * 
     * @see CmsDialog#dialogBlockEnd()
     */
    public String iconsBlockAreaEnd() {

        return iconsBlockArea(HTML_END, null);
    }

    /**
     * Builds the start HTML for a block area with border and optional subheadline in the dialog content area.<p>
     * 
     * @param headline the headline String for the block
     * 
     * @return block area start segment
     * 
     * @see CmsDialog#dialogBlockStart(String)
     */
    public String iconsBlockAreaStart(String headline) {

        return iconsBlockArea(HTML_START, headline);
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#pageBody(int, java.lang.String, java.lang.String)
     */
    public String pageBody(int segment, String className, String parameters) {

        if (!useNewStyle()) {
            return super.pageBody(segment, className, parameters);
        } else {
            Map data = CmsStringUtil.extendAttribute(parameters, "onLoad", "bodyLoad();");
            String onLoad = (String)data.get("value");
            String myPars = (String)data.get("text");
            data = CmsStringUtil.extendAttribute(parameters, "onUnload", "bodyUnload();");
            String onUnload = (String)data.get("value");
            myPars = (String)data.get("text");
            if (segment == HTML_START) {
                StringBuffer html = new StringBuffer(512);
                html.append(CmsStringUtil.code(1, "<body onLoad="
                    + onLoad
                    + " onUnload="
                    + onUnload
                    + (CmsStringUtil.isNotEmpty(className) ? " class='" + className + "'" : "")
                    + (CmsStringUtil.isNotEmpty(myPars) ? " " + myPars : "")
                    + ">"));
                html.append(CmsStringUtil.code(2, "<a href='#' name='top' id='top'></a>"));
                html.append(CmsStringUtil.code(
                    2,
                    "<table border='0' cellspacing='0' cellpadding='0' id='loaderContainer' onClick='return false;'>"));
                html.append(CmsStringUtil.code(3, "<tr><td id='loaderContainerH'><div id='loader'>"));
                html.append(CmsStringUtil.code(
                    4,
                    "<table border='0' cellpadding='0' cellspacing='0' width='100%'><tr><td>"));
                html.append(CmsStringUtil.code(5, "<p><img src='"
                    + getSkinUri()
                    + "commons/wait.gif"
                    + "' height='32' width='32' alt=''/>"));
                html.append(CmsStringUtil.code(5, "<strong>" + key("admin.view.loading") + "</strong></p>"));
                html.append(CmsStringUtil.code(4, "</td></tr></table>"));
                html.append(CmsStringUtil.code(3, "</div></td></tr>"));
                html.append(CmsStringUtil.code(2, "</table>"));
                html.append(CmsStringUtil.code(
                    2,
                    "<table width='100%' cellspacing='0' cellpadding='0' border='0'><tr><td id='screenH'>"));
                return html.toString();
            } else {
                StringBuffer html = new StringBuffer(128);
                html.append(CmsStringUtil.code(2, "</td></tr></table>"));
                html.append(CmsStringUtil.code(1, "</body>"));
                return html.toString();
            }
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#pageHtmlStyle(int, java.lang.String, java.lang.String)
     */
    public String pageHtmlStyle(int segment, String title, String stylesheet) {

        if (!useNewStyle() || segment != HTML_START) {
            return super.pageHtmlStyle(segment, title, stylesheet);
        }

        StringBuffer html = new StringBuffer(512);
        html
            .append(CmsStringUtil
                .code("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"));
        html.append(CmsStringUtil.code("<html>"));
        html.append(CmsStringUtil.code(1, "<head>"));
        if (title != null) {
            html.append(CmsStringUtil.code(2, "<title>" + resolveMacros(title) + "</title>"));
        }
        html.append(CmsStringUtil.code(2, "<meta http-equiv='Content-Type' content='text/html; charset="
            + getEncoding()
            + "' >"));
        html.append(CmsStringUtil.code(2, "<link rel='stylesheet' type='text/css' href='"
            + getStyleUri(getJsp())
            + "new_admin.css"
            + "'>"));
        html.append(CmsStringUtil.code(2, "<script language='javascript' type='text/javascript' src='"
            + getSkinUri()
            + "admin/javascript/general.js"
            + "'></script>"));
        html.append(CmsStringUtil.code(1, "</head>"));
        html.append(CmsStringUtil.code(1, "<style type='text/css'>"));
        html.append(CmsStringUtil.code(2, "#loaderContainer td {"));
        html.append(CmsStringUtil.code(3, "background-image: url("
            + getSkinUri()
            + "admin/images/semi-transparent.gif"
            + ");"));
        html.append(CmsStringUtil.code(2, "}"));
        html.append(CmsStringUtil.code(1, "</style>"));
        html.append(CmsStringUtil.code(1, "<script language='javascript' type='text/javascript'><!--"));
        html.append(CmsStringUtil.code(2, "function bodyLoad() {"));
        html.append(CmsStringUtil.code(3, "setContext(\""
            + CmsStringUtil.escapeJavaScript(resolveMacros(getAdminTool().getHelpText()))
            + "\");"));
        html.append(CmsStringUtil.code(3, "setActiveItemByName(\"" + getCurrentToolPath() + "\");"));
        html.append(CmsStringUtil.code(3, "loadingOff();"));
        html.append(CmsStringUtil.code(
            3,
            "document.getElementById('loaderContainerH').height = document.getElementById('screenH').offsetHeight;"));
        html.append(CmsStringUtil.code(2, "}"));
        html.append(CmsStringUtil.code(2, "function bodyUnload() {"));
        html.append(CmsStringUtil.code(3, "loadingOn();"));
        html.append(CmsStringUtil.code(2, "}"));
        html.append(CmsStringUtil.code(1, "// --></script>"));
        return html.toString();
    }

    /**
     * Sets the path parameter value.<p>
     *
     * @param paramPath the path parameter value to set
     */
    public void setParamPath(String paramPath) {

        m_paramPath = paramPath;
    }

    /**
     * Sets the root parameter value.<p>
     *
     * @param paramRoot the root parameter value to set
     */
    public void setParamRoot(String paramRoot) {

        m_paramRoot = paramRoot;
    }

    /**
     * Sets the style parameter value.<p>
     *
     * @param paramStyle the style parameter value to set
     */
    public void setParamStyle(String paramStyle) {

        m_paramStyle = paramStyle;
    }

    /**
     * Tests if we are working with the new administration dialog style.<p>
     * 
     * The default is the new style, this param is not intented for external use.<p>
     * 
     * @return <code>true</code> if using the new style
     */
    public boolean useNewStyle() {

        return getParamStyle() != null && getParamStyle().equals("new");
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        fillParamValues(request);
    }

}