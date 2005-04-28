/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/CmsToolDialog.java,v $
 * Date   : $Date: 2005/04/28 09:52:17 $
 * Version: $Revision: 1.7 $
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

package org.opencms.workplace.tools;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsReport;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.A_CmsHtmlIconButton;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Helper class that encapsulates all the code for the "new" 
 * style of the administration dialogs.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.7 $
 * @since 5.7.3
 */
public class CmsToolDialog extends CmsWorkplace {

    /** Request parameter name for the tool path. */
    public static final String PARAM_PATH = "path";

    /** Request parameter name for the root tool path. */
    public static final String PARAM_ROOT = "root";

    /** Request parameter name for the style type. */
    public static final String PARAM_STYLE = "style";

    /** Path parameter value. */
    private String m_paramPath;

    /** Root parameter value. */
    private String m_paramRoot;

    /** Style parameter value. */
    private String m_paramStyle;

    /**
     * Default Constructor.<p>
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
        html.append("function submitAction(actionValue, theForm, formName) {\n");
        html.append("\tif (theForm == null) {\n");
        html.append("\t\ttheForm = document.forms[formName];\n");
        html.append("\t}\n");
        html.append("\ttheForm." + CmsDialog.PARAM_FRAMENAME + ".value = window.name;\n");
        html.append("\tif (actionValue == '" + CmsDialog.DIALOG_OK + "') {\n");
        if (!(this instanceof CmsReport)) {
            html.append("\t\tloadingOn();\n");
        }
        html.append("\t\treturn true;\n");
        html.append("\t}\n");
        html.append("\ttheForm." + CmsDialog.PARAM_ACTION + ".value = actionValue;\n");
        html.append("\tsubmitForm(theForm);\n");
        html.append("\treturn true;\n");
        html.append("}\n");
        return html.toString();
    }

    /**
     * Generates the standard new style dialog title row, and tool grouping.<p>
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

        html.append(getToolManager().generateNavBar(toolPath, this));
        // build title
        html.append("<div class='screenTitle'>\n");
        html.append("\t<table width='100%' cellspacing='0'>\n");
        html.append("\t\t<tr>\n");
        html.append("\t\t\t<td>\n");
        html.append(getAdminTool().getName());
        html.append("\n\t\t\t</td>");
        // uplevel button only if needed
        if (getParentPath() != toolPath) {
            html.append("\t\t\t<td class='uplevel'>\n\t\t\t\t");
            String onClic = "openPage('" + upLevelLink + "');";
            html.append(A_CmsHtmlIconButton.defaultButtonHtml(
                "id-up-level",
                key("admin.view.uplevel"),
                parentName,
                true,
                "admin/images/up.gif",
                onClic));
            html.append("\n\t\t\t</td>\n");
        }
        html.append("\t\t</tr>\n");
        html.append("\t</table>\n");
        html.append("</div>\n");
        return resolveMacros(html.toString());
    }
    
    /**
     * Returns the admin tool.<p>
     *
     * @return the admin tool
     */
    public CmsTool getAdminTool() {

        CmsTool ret =  getToolManager().getCurrentTool(getCms());
        if (ret == null) {
            ret = getToolManager().getCurrentTool(getCms()); 
        }
        return ret;
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
     * Returns the tool manager.<p>
     * 
     * @return the tool manager
     */
    public CmsToolManager getToolManager() {

        return OpenCms.getWorkplaceManager().getToolManager();
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
            retValue.append("<p>&nbsp;</p>\n");
            retValue.append("<!-- icons block area start -->\n");
            retValue.append("<div class=\"dialogblockborder dialogblockborderheadline iconblock\" unselectable=\"on\" >\n");
            retValue.append("\t<div class=\"dialogblock\" unselectable=\"on\">\n");
            retValue.append("\t\t<span class=\"dialogblockhead\" unselectable=\"on\">\n");
            retValue.append(headline);
            retValue.append("\t\t</span>\n");
            retValue.append("\t\t<table class='toolsArea' width='100%' cellspacing='0' cellpadding='0' border='0'>\n");
            retValue.append("\t\t\t<tr><td>\n");
        } else {
            retValue.append("\t\t\t</td></tr>\n");
            retValue.append("\t\t</table>\n");
            retValue.append("\t</div>\n");
            retValue.append("</div>\n");
            retValue.append("<!-- icons block area end -->\n");
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
            data = CmsStringUtil.extendAttribute(myPars, "onUnload", "bodyUnload();");
            String onUnload = (String)data.get("value");
            myPars = (String)data.get("text");
            if (segment == HTML_START) {
                StringBuffer html = new StringBuffer(512);
                html.append("<body onLoad=");
                html.append(onLoad);
                html.append(" onUnload=");
                html.append(onUnload);
                html.append(CmsStringUtil.isNotEmpty(className) ? " class='" + className + "'" : "");
                html.append(CmsStringUtil.isNotEmpty(myPars) ? " " + myPars : "");
                html.append(">\n");
                html.append("\t<table border='0' cellspacing='0' cellpadding='0' id='loaderContainer' onClick='return false;'>\n");
                html.append("\t\t<tr><td id='loaderContainerH'><div id='loader'>\n");
                html.append("\t\t\t<table border='0' cellpadding='0' cellspacing='0' width='100%'><tr><td>\n");
                html.append("\t\t\t\t<p><img src='");
                html.append(getSkinUri());
                html.append("commons/wait.gif");
                html.append("' height='32' width='32' alt=''/>\n");
                html.append("\t\t\t\t<strong>");
                html.append(key("admin.view.loading"));
                html.append("</strong></p>\n");
                html.append("\t\t\t</td></tr></table>\n");
                html.append("\t\t</div></td></tr>\n");
                html.append("\t</table>\n");
                html.append("\t<table width='100%' cellspacing='0' cellpadding='0' border='0'><tr><td id='screenH'>\n");
                return html.toString();
            } else {
                StringBuffer html = new StringBuffer(128);
                html.append("\t</td></tr></table>\n");
                html.append("</body>");
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
        html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n");
        html.append("<html>\n");
        html.append("\t<head>\n");
        if (title != null) {
            html.append("\t\t<title>");
            html.append(title);
            html.append("</title>\n");
        }
        html.append("\t\t<meta http-equiv='Content-Type' content='text/html; charset=");
        html.append(getEncoding());
        html.append("' >\n");
        html.append("\t\t<link rel='stylesheet' type='text/css' href='");
        html.append(getStyleUri(getJsp()));
        html.append("new_admin.css'>\n");
        html.append("\t\t<script language='javascript' type='text/javascript' src='");
        html.append(getSkinUri());
        html.append("admin/javascript/general.js'></script>\n");
        html.append("\t</head>\n");
        html.append("\t<script language='javascript' type='text/javascript'><!--\n");
        html.append("\t\tfunction bodyLoad() {\n");
        html.append("\t\t\tsetContext(\"");
        html.append(CmsStringUtil.escapeJavaScript(resolveMacros(getAdminTool().getHelpText())));
        html.append("\");\n");
        html.append("\t\t\tsetActiveItemByName(\"");
        html.append(getCurrentToolPath());
        html.append("\");\n");
        html.append("\t\t\tloadingOff('');\n");
        html.append("\t\t\tdocument.getElementById('loaderContainerH').height = document.getElementById('screenH').offsetHeight;\n");
        html.append("\t\t}\n");
        html.append("\t\tfunction bodyUnload() {\n");
        if (!(this instanceof CmsReport)) {
            html.append("\t\t\tloadingOn('');\n");
        }
        html.append("\t\t}\n");
        html.append("\t// --></script>\n");
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