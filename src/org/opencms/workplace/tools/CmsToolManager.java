/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/CmsToolManager.java,v $
 * Date   : $Date: 2005/10/26 09:17:34 $
 * Version: $Revision: 1.40.2.6 $
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

package org.opencms.workplace.tools;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsIdentifiableObjectContainer;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;

/**
 * Manages the registered tools, actualizing its state every time the workplace is reinitialize.<p>
 * 
 * Manages also the configuration settings for the administration view, and provides
 * several tool related methods.<p>
 *
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.40.2.6 $ 
 * 
 * @since 6.0.0 
 */
public class CmsToolManager {

    /**  Root location for the tools. */
    public static final String ADMINTOOLS_ROOT_LOCATION = CmsWorkplace.PATH_WORKPLACE + "admin";

    /**  Root location of the administration view. */
    public static final String ADMINVIEW_ROOT_LOCATION = CmsWorkplace.PATH_WORKPLACE + "views/admin";

    /**  Property definition name to look for. */
    public static final String HANDLERCLASS_PROPERTY = "admintoolhandler-class";

    /**  Navegation bar separator (html code). */
    public static final String NAVBAR_SEPARATOR = "\n&nbsp;&gt;&nbsp;\n";

    /**  Tool path separator. */
    public static final String TOOLPATH_SEPARATOR = "/";

    /** Location of the admin view jsp page. */
    public static final String VIEW_JSPPAGE_LOCATION = ADMINVIEW_ROOT_LOCATION + "/admin-main.html";

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsToolManager.class);

    /** List of all available tools. */
    private final CmsIdentifiableObjectContainer m_tools = new CmsIdentifiableObjectContainer(true, false);

    /** List of all available urls and related tool paths. */
    private final CmsIdentifiableObjectContainer m_urls = new CmsIdentifiableObjectContainer(false, false);

    /**
     * Default Constructor, called by the <code>{@link org.opencms.workplace.CmsWorkplaceManager#initialize(CmsObject)}</code> method.<p>
     * 
     * @param cms the cms context
     */
    public CmsToolManager(CmsObject cms) {

        if (!cms.existsResource(VIEW_JSPPAGE_LOCATION)) {
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_TOOLMANAGER_NOT_CREATED_0));
            }
            return;
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_TOOLMANAGER_CREATED_0));
        }
        try {
            List handlers = new ArrayList();

            I_CmsToolHandler rootHandler = new CmsRootToolHandler();
            rootHandler.setup(cms, VIEW_JSPPAGE_LOCATION);
            handlers.add(rootHandler);

            // look in every file under C_ADMINTOOLS_ROOT_LOCATION for valid
            // admin tools and register them
            List resources = cms.readResourcesWithProperty(ADMINTOOLS_ROOT_LOCATION, HANDLERCLASS_PROPERTY);
            Iterator itRes = resources.iterator();
            while (itRes.hasNext()) {
                CmsResource res = (CmsResource)itRes.next();
                CmsProperty prop = cms.readPropertyObject(res.getRootPath(), HANDLERCLASS_PROPERTY, false);
                if (!prop.isNullProperty()) {
                    try {
                        // instantiate the handler
                        Class handlerClass = Class.forName(prop.getValue());
                        I_CmsToolHandler handler = (I_CmsToolHandler)handlerClass.newInstance();
                        handler.setup(cms, res.getRootPath());

                        // keep for later use
                        handlers.add(handler);
                        // log success
                        if (CmsLog.INIT.isDebugEnabled()) {
                            if (!handler.getLink().equals(VIEW_JSPPAGE_LOCATION)) {
                                CmsLog.INIT.debug(Messages.get().key(
                                    Messages.INIT_TOOLMANAGER_NEWTOOL_FOUND_2,
                                    handler.getPath(),
                                    handler.getLink()));
                            } else {
                                CmsLog.INIT.debug(Messages.get().key(
                                    Messages.INIT_TOOLMANAGER_NEWTOOL_FOUND_2,
                                    handler.getPath(),
                                    res.getRootPath()));
                            }
                        }
                    } catch (Exception e) {
                        // log failure
                        if (CmsLog.INIT.isWarnEnabled()) {
                            CmsLog.INIT.warn(Messages.get().key(
                                Messages.INIT_TOOLMANAGER_TOOL_SETUP_ERROR_1,
                                res.getRootPath()), e);
                        }
                    }
                }
            }
            registerHandlerList(cms, 1, handlers);
        } catch (CmsException e) {
            // log failure
            if (CmsLog.INIT.isErrorEnabled()) {
                CmsLog.INIT.error(Messages.get().key(Messages.INIT_TOOLMANAGER_SETUP_ERROR_0), e);
            }
        }

    }

    /**
     * Returns the OpenCms link for the given tool path which requires no parameters.<p>
     * 
     * @param jsp the jsp action element
     * @param toolPath the tool path
     * 
     * @return the OpenCms link for the given tool path which requires parameters
     */
    public static String linkForToolPath(CmsJspActionElement jsp, String toolPath) {

        StringBuffer result = new StringBuffer();
        result.append(jsp.link(VIEW_JSPPAGE_LOCATION));
        result.append('?');
        result.append(CmsToolDialog.PARAM_PATH);
        result.append('=');
        result.append(CmsEncoder.encode(toolPath));
        return result.toString();
    }

    /**
     * Returns the OpenCms link for the given tool path which requires parameters.<p>
     * 
     * Please note: Don't overuse the parameter map because this will likely introduce issues 
     * with encoding. If possible, don't pass parameters at all, or only very simple parameters
     * with no special chars that can easily be parsed.<p>
     * 
     * @param jsp the jsp action element
     * @param toolPath the tool path
     * @param params the map of required tool parameters
     * 
     * @return the OpenCms link for the given tool path which requires parameters
     */
    public static String linkForToolPath(CmsJspActionElement jsp, String toolPath, Map params) {

        if (params == null) {
            // no parameters - take the shortcut
            return linkForToolPath(jsp, toolPath);
        }
        params.put(CmsToolDialog.PARAM_PATH, toolPath);
        return CmsRequestUtil.appendParameters(jsp.link(VIEW_JSPPAGE_LOCATION), params, true);
    }

    /**
     * Returns the navegation bar html code for the given tool path.<p>
     * 
     * @param toolPath the path
     * @param wp the jsp page
     * 
     * @return the html code
     */
    public String generateNavBar(String toolPath, CmsWorkplace wp) {

        if (toolPath.equals(getRootToolPath(wp))) {
            return "<div class='pathbar'>&nbsp;</div>\n";
        }
        CmsTool adminTool = resolveAdminTool(toolPath);
        String html = A_CmsHtmlIconButton.defaultButtonHtml(
            wp.getJsp(),
            CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
            "nav" + adminTool.getId(),
            adminTool.getHandler().getShortName(),
            null,
            false,
            null,
            null,
            null);
        String parent = toolPath;
        while (!parent.equals(getRootToolPath(wp))) {
            parent = getParent(wp, parent);
            adminTool = resolveAdminTool(parent);

            String id = "nav" + adminTool.getId();
            String link = linkForToolPath(wp.getJsp(), parent, adminTool.getHandler().getParameters(wp));
            String onClic = "openPage('" + link + "');";
            String buttonHtml = A_CmsHtmlIconButton.defaultButtonHtml(
                wp.getJsp(),
                CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
                id,
                adminTool.getHandler().getName(),
                adminTool.getHandler().getHelpText(),
                true,
                null,
                null,
                onClic);
            html = buttonHtml + NAVBAR_SEPARATOR + html;
        }

        return CmsToolMacroResolver.resolveMacros("<div class='pathbar'>\n" + html + "&nbsp;</div>\n", wp);
    }

    /**
     * Returns the current tool.<p>
     * 
     * @param wp the workplace object
     *
     * @return the current tool 
     */
    public CmsTool getCurrentTool(CmsWorkplace wp) {

        return resolveAdminTool(getCurrentToolPath(wp));
    }

    /**
     * Returns the current tool path.<p>
     *
     * @param wp the workplace object
     *
     * @return the current tool path
     */
    public String getCurrentToolPath(CmsWorkplace wp) {

        CmsToolUserData userData = getUserData(wp);
        String path = getRootToolPath(wp);
        if (userData != null) {
            path = userData.getCurrentToolPath();
        }
        return path;
    }

    /**
     * Returns the path to the parent of the tool identified by the given tool path.<p>
     * 
     * The parent of the root is the same root.<p>
     * 
     * @param wp the workplace object
     * @param toolPath the abstract tool path
     * 
     * @return his parent
     */
    public String getParent(CmsWorkplace wp, String toolPath) {

        if (toolPath.equals(getRootToolPath(wp))) {
            return toolPath;
        }
        int pos = toolPath.lastIndexOf(TOOLPATH_SEPARATOR);
        return pos <= 0 ? TOOLPATH_SEPARATOR : toolPath.substring(0, pos);
    }

    /**
     * Returns the root tool path for the active user.<p>
     * 
     * @param wp the workplace object
     * 
     * @return the root tool path for the active user
     */
    public String getRootToolPath(CmsWorkplace wp) {

        CmsToolUserData userData = getUserData(wp);
        String path = TOOLPATH_SEPARATOR;
        if (userData != null) {
            path = userData.getRootTool();
        }
        return path;
    }

    /**
     * Returns a list with all registered tools.<p>
     * 
     * @return list if <code>{@link CmsTool}</code>
     */
    public List getToolHandlers() {

        return m_tools.elementList();
    }

    /**
     * Returns <code>true</code> if there is at least one tool registered using the given url.<p>
     * 
     * @param url the url of the tool
     * 
     * @return <code>true</code> if there is at least one tool registered using the given url
     */
    public boolean hasToolPathForUrl(String url) {

        List toolPaths = (List)m_urls.getObject(url);
        return (toolPaths != null && !toolPaths.isEmpty());
    }

    /**
     * Returns a list of all tools in the given path.<p>
     * 
     * @param toolPath the path
     * @param includeSubtools if the tools in subfolders should be also returned
     * 
     * @return a list of abstract tools paths
     */
    public List getToolsForPath(String toolPath, boolean includeSubtools) {

        List toolList = new ArrayList();

        Iterator itTools = m_tools.elementList().iterator();
        while (itTools.hasNext()) {
            String tool = ((CmsTool)itTools.next()).getHandler().getPath();
            if (tool.equals(TOOLPATH_SEPARATOR)) {
                continue;
            }
            // filter for path
            if (toolPath.equals(TOOLPATH_SEPARATOR) || tool.startsWith(toolPath + TOOLPATH_SEPARATOR)) {
                // filter sub tree
                if (includeSubtools || tool.indexOf(TOOLPATH_SEPARATOR, toolPath.length() + 1) < 0) {
                    toolList.add(tool);
                }
            }
        }
        return toolList;
    }

    /**
     * Returns the <code>{@link CmsToolUserData}</code> object for a given user.<p>
     *
     * @param wp the workplace object
     *
     * @return the current user data
     */
    public CmsToolUserData getUserData(CmsWorkplace wp) {

        CmsToolUserData userData = wp.getSettings().getToolUserData();
        if (userData == null) {
            userData = new CmsToolUserData();
            userData.setCurrentToolPath(TOOLPATH_SEPARATOR);
            userData.setRootTool(TOOLPATH_SEPARATOR);
            wp.getSettings().setToolUserData(userData);
        }
        return userData;
    }

    /**
     * This method initializes the tool manager for the current user.<p>
     * 
     * @param wp the jsp page comming from
     * @param toolPath the current tool path
     * @param rootToolPath the root tool path
     */
    public synchronized void initParams(CmsToolDialog wp, String toolPath, String rootToolPath) {

        setCurrentToolPath(wp, toolPath);
        setRootToolPath(wp, rootToolPath);

        // if the current tool path is not under the current root, set the current root as the current tool
        if (!getCurrentToolPath(wp).startsWith(getRootToolPath(wp))) {
            setCurrentToolPath(wp, getRootToolPath(wp));
        }
        wp.setParamPath(getCurrentToolPath(wp));
        wp.setParamRoot(getRootToolPath(wp));
    }

    /**
     * Redirects to the given page with the given parameters.<p>
     * 
     * @param wp the workplace object
     * @param pagePath the path to the page to redirect to
     * @param params the parameters to send
     * 
     * @throws IOException in case of errors during forwarding
     * @throws ServletException in case of errors during forwarding
     */
    public void jspForwardPage(CmsWorkplace wp, String pagePath, Map params) throws IOException, ServletException {

        Map newParams = new HashMap();
        // add query parameters to the parameter map if required
        if (pagePath.indexOf("?") > 0) {
            String query = pagePath.substring(pagePath.indexOf("?"));
            pagePath = pagePath.substring(0, pagePath.indexOf("?"));
            Map reqParameters = CmsRequestUtil.createParameterMap(query);
            newParams.putAll(reqParameters);
        }
        if (params != null) {
            newParams.putAll(params);
        }

        // put close link if not set
        if (!newParams.containsKey(CmsDialog.PARAM_CLOSELINK)) {
            Map argMap = resolveAdminTool(getCurrentToolPath(wp)).getHandler().getParameters(wp);
            newParams.put(CmsDialog.PARAM_CLOSELINK, linkForToolPath(wp.getJsp(), getCurrentToolPath(wp), argMap));
        }
        wp.setForwarded(true);
        // forward to the requested page uri
        CmsRequestUtil.forwardRequest(
            wp.getJsp().link(pagePath),
            CmsRequestUtil.createParameterMap(newParams),
            wp.getJsp().getRequest(),
            wp.getJsp().getResponse());
    }

    /**
     * Redirects to the given tool with the given parameters.<p>
     * 
     * @param wp the workplace object
     * @param toolPath the path to the tool to redirect to
     * @param params the parameters to send
     * 
     * @throws IOException in case of errors during forwarding
     * @throws ServletException in case of errors during forwarding
     */
    public void jspForwardTool(CmsWorkplace wp, String toolPath, Map params) throws IOException, ServletException {

        Map newParams;
        if (params == null) {
            newParams = new HashMap();
        } else {
            newParams = new HashMap(params);
        }
        // update path param
        newParams.put(CmsToolDialog.PARAM_PATH, toolPath);
        jspForwardPage(wp, VIEW_JSPPAGE_LOCATION, newParams);
    }

    /**
     * Returns the admin tool corresponding to the given abstract path.<p>
     * 
     * @param toolPath the path
     * 
     * @return the corresponding tool, or <code>null</code> if not found
     */
    public CmsTool resolveAdminTool(String toolPath) {

        return (CmsTool)m_tools.getObject(toolPath);
    }

    /**
     * Sets the current tool path.<p>
     * 
     * @param wp the workplace object
     * @param currentToolPath the current tool path to set
     */
    public void setCurrentToolPath(CmsWorkplace wp, String currentToolPath) {

        // use last used path if param empty
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(currentToolPath) || currentToolPath.trim().equals("null")) {
            currentToolPath = getCurrentToolPath(wp);
        }
        currentToolPath = repairPath(wp, currentToolPath);
        // use it
        CmsToolUserData userData = getUserData(wp);
        userData.setCurrentToolPath(currentToolPath);
    }

    /**
     * Sets the root tool path.<p>
     * 
     * @param wp the workplace object
     * @param rootToolPath the root tool path to set
     */
    public void setRootToolPath(CmsWorkplace wp, String rootToolPath) {

        // use last used root if param empty
        if (CmsStringUtil.isEmpty(rootToolPath) || rootToolPath.trim().equals("null")) {
            rootToolPath = getRootToolPath(wp);
        }
        rootToolPath = repairPath(wp, rootToolPath);
        // set it
        CmsToolUserData userData = getUserData(wp);
        userData.setRootTool(rootToolPath);
    }

    /**
     * Registers a new tool at a given install point.<p>
     * 
     * @param cms the cms context object
     * @param handler the handler to install
     */
    private void registerAdminTool(CmsObject cms, I_CmsToolHandler handler) {

        String link = handler.getLink();
        if (link.indexOf("?") > 0) {
            link = link.substring(0, link.indexOf("?"));
        }
        // check visibility
        if (!cms.existsResource(link)) {
            return;
        }

        //validate path
        if (!validatePath(handler.getPath(), false)) {
            // log failure
            if (CmsLog.INIT.isWarnEnabled()) {
                CmsLog.INIT.warn(Messages.get().key(
                    Messages.INIT_TOOLMANAGER_INCONSISTENT_PATH_2,
                    handler.getPath(),
                    handler.getLink()));
            }
            return;
        }

        String id = "tool" + m_tools.elementList().size();
        CmsTool tool = new CmsTool(id, handler);

        try {
            // try to find problems in custom tools
            handler.isEnabled(cms);
            handler.isVisible(cms);
        } catch (Throwable ex) {
            CmsLog.INIT.warn(Messages.get().key(
                Messages.INIT_TOOLMANAGER_INSTALL_ERROR_2,
                handler.getPath(),
                handler.getLink()));
            return;
        }

        try {
            // try to register, can fail if path is already used by another tool
            m_tools.addIdentifiableObject(handler.getPath(), tool);
            // just for fast association of links with tools
            m_urls.addIdentifiableObject(link, handler.getPath());
        } catch (Throwable ex) {
            CmsLog.INIT.warn(Messages.get().key(
                Messages.INIT_TOOLMANAGER_DUPLICATED_ERROR_3,
                handler.getPath(),
                handler.getLink(),
                resolveAdminTool(handler.getPath()).getHandler().getLink()));
        }
    }

    /**
     * Registers all tool handlers recursively.<p> 
     * 
     * @param cms the cms context object
     * @param len the recursion level
     * @param handlers the list of handlers to register
     */
    private void registerHandlerList(CmsObject cms, int len, List handlers) {

        boolean found = false;
        Iterator it = handlers.iterator();
        while (it.hasNext()) {
            I_CmsToolHandler handler = (I_CmsToolHandler)it.next();
            int myLen = CmsStringUtil.splitAsArray(handler.getPath(), TOOLPATH_SEPARATOR).length;
            if ((len == myLen && !handler.getPath().equals(TOOLPATH_SEPARATOR))
                || (len == 1 && handler.getPath().equals(TOOLPATH_SEPARATOR))) {
                found = true;
                registerAdminTool(cms, handler);
            }
        }
        if (found) {
            registerHandlerList(cms, len + 1, handlers);
        }

    }

    /**
     * Given a string a valid and visible tool path is computed.<p>
     * 
     * @param wp the workplace object
     * @param path the path to repair
     * 
     * @return a valida and visible tool path
     */
    private String repairPath(CmsWorkplace wp, String path) {

        // navigate until to reach a valid path
        while (!validatePath(path, true)) {
            // log failure
            LOG.warn(Messages.get().key(wp.getLocale(), Messages.LOG_MISSING_ADMIN_TOOL_1, new Object[] {path}));
            // try parent
            path = getParent(wp, path);
        }
        // navigate until to reach a valid tool
        while (resolveAdminTool(path) == null) {
            // log failure
            LOG.warn(Messages.get().key(wp.getLocale(), Messages.LOG_MISSING_ADMIN_TOOL_1, new Object[] {path}));
            // try parent
            path = getParent(wp, path);
        }

        // navegate until to reach an enabled path
        CmsTool aTool = resolveAdminTool(path);
        while (!aTool.getHandler().isEnabled(wp.getCms())) {
            if (aTool.getHandler().getLink().equals(VIEW_JSPPAGE_LOCATION)) {
                // just grouping
                break;
            }
            path = getParent(wp, path);
            aTool = resolveAdminTool(path);
        }

        return path;
    }

    /**
     * Tests if the full tool path is available.<p>
     * 
     * @param toolPath the path
     * @param full if <code>true</code> the whole path is checked, if not the last part is not checked (for new tools)
     * 
     * @return if valid or not
     */
    private boolean validatePath(String toolPath, boolean full) {

        if (toolPath.equals(TOOLPATH_SEPARATOR)) {
            return true;
        }
        if (!toolPath.startsWith(TOOLPATH_SEPARATOR)) {
            return false;
        }
        List groups = CmsStringUtil.splitAsList(toolPath, TOOLPATH_SEPARATOR);
        Iterator itGroups = groups.iterator();
        String subpath = "";
        while (itGroups.hasNext()) {
            String group = (String)itGroups.next();
            if (subpath.length() != TOOLPATH_SEPARATOR.length()) {
                subpath += TOOLPATH_SEPARATOR + group;
            } else {
                subpath += group;
            }
            if (itGroups.hasNext() || full) {
                try {
                    // just check if the tool is available
                    resolveAdminTool(subpath).toString();
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return true;
    }
}