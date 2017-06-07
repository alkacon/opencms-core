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

package org.opencms.workplace.tools;

import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Helper class that encapsulates all the code for the "new"
 * style of the administration dialogs.<p>
 *
 * @since 6.0.0
 */
public class CmsToolDialog extends CmsWorkplace {

    /** Request parameter name for the base tool path in the navigation, should be a parent tool of path. */
    public static final String PARAM_BASE = "base";

    /** Request parameter name for the force flag. */
    public static final String PARAM_FORCE = "force";

    /** Request parameter name for the tool path, should be an accessible tool under the given root. */
    public static final String PARAM_PATH = "path";

    /** Request parameter name for the root tool path. */
    public static final String PARAM_ROOT = "root";

    /** Request parameter name for the style type. */
    public static final String PARAM_STYLE = "style";

    /** Request parameter value for the 'new' dialog style. */
    public static final String STYLE_NEW = "new";

    /** The adminProject parameter name. */
    public static final String PARAM_ADMIN_PROJECT = "adminProject";

    /** Base parameter value. */
    private String m_paramBase;

    /** Force parameter value. */
    private String m_paramForce;

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
        html.append("\ttry {\n");
        html.append("\t\ttheForm.").append(CmsDialog.PARAM_FRAMENAME).append(".value = window.name;\n");
        html.append("\t} catch (e) {}\n");
        html.append("\tif (actionValue == '" + CmsDialog.DIALOG_OK + "') {\n");
        html.append("\t\tloadingOn();\n");
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
        String rootKey = getToolManager().getCurrentRoot(this).getKey();
        String upLevelLink = computeUpLevelLink();
        String parentName = getToolManager().resolveAdminTool(rootKey, parentPath).getHandler().getName();

        html.append(getToolManager().generateNavBar(toolPath, this));
        // build title
        html.append("<div class='screenTitle'>\n");
        html.append("\t<table width='100%' cellspacing='0'>\n");
        html.append("\t\t<tr>\n");
        html.append("\t\t\t<td>\n");
        html.append(CmsEncoder.decode(CmsToolMacroResolver.resolveMacros(getAdminTool().getHandler().getName(), this)));
        html.append("\n\t\t\t</td>");
        // uplevel button only if needed
        if (!getParentPath().equals(toolPath)) {
            html.append("\t\t\t<td class='uplevel'>\n\t\t\t\t");
            html.append(
                A_CmsHtmlIconButton.defaultButtonHtml(
                    CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
                    "id-up-level",
                    Messages.get().getBundle(getLocale()).key(Messages.GUI_ADMIN_VIEW_UPLEVEL_0),
                    parentName,
                    true,
                    "admin/images/up.png",
                    null,
                    "openPage('" + upLevelLink + "');"));
            html.append("\n\t\t\t</td>\n");
        }
        html.append("\t\t</tr>\n");
        html.append("\t</table>\n");
        html.append("</div>\n");
        return CmsToolMacroResolver.resolveMacros(html.toString(), this);
    }

    /**
     * Returns the admin tool.<p>
     *
     * @return the admin tool
     */
    public CmsTool getAdminTool() {

        return getToolManager().getCurrentTool(this);
    }

    /**
     * Returns the current tool path.<p>
     *
     * @return the current tool path
     */
    public String getCurrentToolPath() {

        return getToolManager().getCurrentToolPath(this);
    }

    /**
     * Returns the value for the base parameter.<p>
     *
     * @return the value for the base parameter
     */
    public String getParamBase() {

        return m_paramBase;
    }

    /**
     * Returns the value for the force parameter.<p>
     *
     * @return the value for the force parameter
     */
    public String getParamForce() {

        return m_paramForce;
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

        return getToolManager().getParent(this, getCurrentToolPath());
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

        StringBuffer result = new StringBuffer(512);
        if (segment == HTML_START) {
            result.append("<!-- icons block area start -->\n");
            result.append("<div class=\"dialogcontent\" unselectable=\"on\">");
            result.append("<fieldset class=\"dialogblock\">\n");
            result.append("<legend>");
            result.append("<span class=\"textbold");
            result.append("\" unselectable=\"on\">");
            result.append(headline);
            result.append("</span></legend>\n");
            result.append("\t\t<table class='toolsArea' width='100%' cellspacing='0' cellpadding='0' border='0'>\n");
            result.append("\t\t\t<tr><td>\n");
        } else {
            result.append("\t\t\t</td></tr>\n");
            result.append("\t\t</table>\n");
            result.append("</fieldset></div>\n");
            result.append("<p>&nbsp;</p>\n");
            result.append("<!-- icons block area end -->\n");
        }
        return result.toString();
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
     * Initializes the admin tool main view.<p>
     *
     * @return the new modified parameters array
     *
     * @throws CmsRoleViolationException in case the dialog is opened by a user without the necessary privileges
     */
    public Map<String, String[]> initAdminTool() throws CmsRoleViolationException {

        Map<String, String[]> params = new HashMap<String, String[]>(getParameterMap());
        // initialize
        getToolManager().initParams(this);

        // adjust parameters if called as default
        if (!useNewStyle()) {
            params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
            setParamStyle(CmsToolDialog.STYLE_NEW);
        }

        try {
            // a dialog just to access the close link parameter
            CmsDialog wp = (CmsDialog)this;
            // set close link
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(wp.getParamCloseLink())) {
                if (!getToolManager().getBaseToolPath(this).equals(getToolManager().getCurrentToolPath(this))) {
                    Map<String, String[]> args = getToolManager().resolveAdminTool(
                        getParamRoot(),
                        getParentPath()).getHandler().getParameters(wp);
                    wp.setParamCloseLink(CmsToolManager.linkForToolPath(getJsp(), getParentPath(), args));
                    params.put(CmsDialog.PARAM_CLOSELINK, new String[] {wp.getParamCloseLink()});
                }
            }
        } catch (Exception e) {
            // ignore
        }

        if (!getToolManager().getCurrentTool(this).getHandler().isEnabled(this)) {
            throw new CmsRoleViolationException(Messages.get().container(Messages.ERR_ADMIN_INSUFFICIENT_RIGHTS_0));
        }

        return params;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#pageBody(int, java.lang.String, java.lang.String)
     */
    @Override
    public String pageBody(int segment, String className, String parameters) {

        if (!useNewStyle()) {
            return super.pageBody(segment, className, parameters);
        } else {
            Map<String, String> data = CmsStringUtil.extendAttribute(parameters, "onLoad", "bodyLoad();");
            String onLoad = data.get("value");
            String myPars = data.get("text");
            data = CmsStringUtil.extendAttribute(myPars, "onUnload", "bodyUnload();");
            String onUnload = data.get("value");
            myPars = data.get("text");
            if (segment == HTML_START) {
                StringBuffer html = new StringBuffer(512);
                html.append("</head>\n");
                html.append("<body onLoad=");
                html.append(onLoad);
                html.append(" onUnload=");
                html.append(onUnload);
                html.append(CmsStringUtil.isNotEmpty(className) ? " class='" + className + "'" : "");
                html.append(CmsStringUtil.isNotEmpty(myPars) ? " " + myPars : "");
                html.append(">\n");
                html.append(
                    "\t<table border='0' cellspacing='0' cellpadding='0' id='loaderContainer' onClick='return false;'>\n");
                html.append("\t\t<tr><td id='loaderContainerH'><div id='loader'>\n");
                html.append("\t\t\t<table border='0' cellpadding='0' cellspacing='0' width='100%'><tr><td>\n");
                html.append("\t\t\t\t<p><img src='");
                html.append(getSkinUri());
                html.append("commons/wait.gif");
                html.append("' height='32' width='32' alt=''/>\n");
                html.append("\t\t\t\t<strong>");
                html.append(Messages.get().getBundle(getLocale()).key(Messages.GUI_ADMIN_VIEW_LOADING_0));
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
    @Override
    public String pageHtmlStyle(int segment, String title, String stylesheet) {

        if (!useNewStyle() || (segment != HTML_START)) {
            return super.pageHtmlStyle(segment, title, stylesheet);
        }

        StringBuffer html = new StringBuffer(512);
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta http-equiv='Content-Type' content='text/html; charset=");
        html.append(getEncoding());
        html.append("' >\n");
        if (title != null) {
            html.append("<title>");
            html.append(title);
            html.append("</title>\n");
        } else {
            // the title tag is required for valid HTML
            html.append("<title></title>\n");
        }
        html.append("<link rel='stylesheet' type='text/css' href='");
        html.append(getStyleUri(getJsp()));
        html.append("new_admin.css'>\n");
        html.append("<script type='text/javascript' src='");
        html.append(getSkinUri());
        html.append("admin/javascript/general.js'></script>\n");
        html.append("<script type='text/javascript' src='");
        html.append(getResourceUri());
        html.append("editors/xmlcontent/help.js'></script>\n\n");
        html.append("<script type='text/javascript'>\n");
        html.append("\tfunction bodyLoad() {\n");

        // add a special CSS class in case we are in the new vaadin based workplace

        html.append("\tif (this.name != \"admin_content\" && this.name != \"explorer_files\") {\n");
        html.append("\t\tvar cssClass=document.body.getAttribute(\"class\");\n");
        html.append("\t\tcssClass+=\" legacy-app\";\n");
        html.append("\t\tdocument.body.setAttribute(\"class\",cssClass);\n");
        html.append("\t}\n");

        html.append("\t\tsetContext(\"");
        html.append(CmsStringUtil.escapeJavaScript(resolveMacros(getAdminTool().getHandler().getHelpText())));
        html.append("\");\n");
        html.append("\t\tsetActiveItemByName(\"");
        html.append(getCurrentToolPath());
        html.append("\");\n");
        html.append("\t\tloadingOff();\n");
        html.append("\t\ttry {\n");
        html.append("\t\t\tdocument.getElementById('loaderContainerH').height = wHeight();\n");
        html.append("\t\t} catch (e) {}\n");
        html.append("\t}\n");
        html.append("\tfunction bodyUnload() {\n");
        html.append("\t\tloadingOn();\n");
        html.append("\t}\n");
        html.append("</script>\n");
        return html.toString();
    }

    /**
     * Sets the value of the base parameter.<p>
     *
     * @param paramBase the value of the base parameter to set
     */
    public void setParamBase(String paramBase) {

        m_paramBase = paramBase;
    }

    /**
     * Sets the value of the force parameter.<p>
     *
     * @param paramForce the value of the force parameter to set
     */
    public void setParamForce(String paramForce) {

        m_paramForce = paramForce;
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
     * The default is the new style, this parameter is not intended for external use.<p>
     *
     * @return <code>true</code> if using the new style
     */
    public boolean useNewStyle() {

        return (getParamStyle() != null) && getParamStyle().equals(CmsToolDialog.STYLE_NEW);
    }

    /**
     * Creates the link for the 'up' button.<p>
     *
     * @return the link for the 'up' button
     */
    protected String computeUpLevelLink() {

        String parentPath = getParentPath();
        String rootKey = getToolManager().getCurrentRoot(this).getKey();
        CmsTool parentTool = getToolManager().resolveAdminTool(rootKey, parentPath);
        String upLevelLink = CmsToolManager.linkForToolPath(
            getJsp(),
            parentPath,
            parentTool.getHandler().getParameters(this));
        upLevelLink = CmsRequestUtil.appendParameter(upLevelLink, PARAM_FORCE, Boolean.TRUE.toString());
        return upLevelLink;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        fillParamValues(request);
    }
}