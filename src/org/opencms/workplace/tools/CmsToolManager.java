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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
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
 * @since 6.0.0
 */
public class CmsToolManager {

    /**  Root location of the administration view. */
    public static final String ADMINVIEW_ROOT_LOCATION = CmsWorkplace.PATH_WORKPLACE + "views/admin";

    /**  Property definition name to look for. */
    public static final String HANDLERCLASS_PROPERTY = "admintoolhandler-class";

    /**  Navigation bar separator (html code). */
    public static final String NAVBAR_SEPARATOR = "\n&nbsp;&gt;&nbsp;\n";

    /**  Tool root separator. */
    public static final String ROOT_SEPARATOR = ":";

    /**  Key for the default tool root, if there is no configured root with this a key, a new one will be configured. */
    public static final String ROOTKEY_DEFAULT = "admin";

    /**  Tool path separator. */
    public static final String TOOLPATH_SEPARATOR = "/";

    /** Location of the default admin view jsp page. */
    public static final String VIEW_JSPPAGE_LOCATION = ADMINVIEW_ROOT_LOCATION + "/admin-main.jsp";

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsToolManager.class);

    /** List of all available roots. */
    private final CmsIdentifiableObjectContainer<CmsToolRootHandler> m_roots;

    /** List of all available tools. */
    private final CmsIdentifiableObjectContainer<CmsTool> m_tools;

    /** List of all available urls and related tool paths. */
    private final CmsIdentifiableObjectContainer<String> m_urls;

    /**
     * Default constructor.<p>
     */
    public CmsToolManager() {

        m_roots = new CmsIdentifiableObjectContainer<CmsToolRootHandler>(true, false);
        m_tools = new CmsIdentifiableObjectContainer<CmsTool>(true, false);
        m_urls = new CmsIdentifiableObjectContainer<String>(false, false);
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
    public static String linkForToolPath(CmsJspActionElement jsp, String toolPath, Map<String, String[]> params) {

        if (params == null) {
            // no parameters - take the shortcut
            return linkForToolPath(jsp, toolPath);
        }
        params.put(CmsToolDialog.PARAM_PATH, new String[] {toolPath});
        return CmsRequestUtil.appendParameters(jsp.link(VIEW_JSPPAGE_LOCATION), params, true);
    }

    /**
     * Adds a new tool root to the tool manager.<p>
     *
     * @param toolRoot the tool root to add
     */
    public void addToolRoot(CmsToolRootHandler toolRoot) {

        m_roots.addIdentifiableObject(toolRoot.getKey(), toolRoot);
    }

    /**
     * Called by the <code>{@link org.opencms.workplace.CmsWorkplaceManager#initialize(CmsObject)}</code> method.<p>
     *
     * @param cms the admin cms context
     */
    public void configure(CmsObject cms) {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_TOOLMANAGER_CREATED_0));
        }
        if (m_roots.getObject(ROOTKEY_DEFAULT) == null) {
            CmsToolRootHandler defToolRoot = new CmsToolRootHandler();
            defToolRoot.setKey(ROOTKEY_DEFAULT);
            defToolRoot.setUri(CmsWorkplace.PATH_WORKPLACE + "admin/");
            defToolRoot.setName("${key." + Messages.GUI_ADMIN_VIEW_ROOT_NAME_0 + "}");
            defToolRoot.setHelpText("${key." + Messages.GUI_ADMIN_VIEW_ROOT_HELP_0 + "}");
            addToolRoot(defToolRoot);
        }
        m_tools.clear();
        m_urls.clear();
        Iterator<CmsToolRootHandler> it = getToolRoots().iterator();
        while (it.hasNext()) {
            CmsToolRootHandler toolRoot = it.next();
            if (!cms.existsResource(toolRoot.getUri())) {
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(
                        Messages.get().getBundle().key(
                            Messages.INIT_TOOLMANAGER_ROOT_SKIPPED_2,
                            toolRoot.getKey(),
                            toolRoot.getUri()));
                }
                continue;
            }
            try {
                toolRoot.setup(cms, null, toolRoot.getUri());
                configureToolRoot(cms, toolRoot);
                // log info
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(
                        Messages.get().getBundle().key(Messages.INIT_TOOLMANAGER_SETUP_1, toolRoot.getKey()));
                }
            } catch (CmsException e) {
                // log failure
                if (CmsLog.INIT.isWarnEnabled()) {
                    CmsLog.INIT.warn(
                        Messages.get().getBundle().key(Messages.INIT_TOOLMANAGER_SETUP_ERROR_1, toolRoot.getKey()),
                        e);
                }
            }
        }
    }

    /**
     * Returns the navigation bar html code for the given tool path.<p>
     *
     * @param toolPath the path
     * @param wp the jsp page
     *
     * @return the html code
     */
    public String generateNavBar(String toolPath, CmsWorkplace wp) {

        if (toolPath.equals(getBaseToolPath(wp))) {
            return "<div class='pathbar'>&nbsp;</div>\n";
        }
        CmsTool adminTool = resolveAdminTool(getCurrentRoot(wp).getKey(), toolPath);
        String html = A_CmsHtmlIconButton.defaultButtonHtml(
            CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
            "nav" + adminTool.getId(),
            adminTool.getHandler().getName(),
            null,
            false,
            null,
            null,
            null);
        String parent = toolPath;
        while (!parent.equals(getBaseToolPath(wp))) {
            parent = getParent(wp, parent);
            adminTool = resolveAdminTool(getCurrentRoot(wp).getKey(), parent);

            String id = "nav" + adminTool.getId();
            String link = linkForToolPath(wp.getJsp(), parent, adminTool.getHandler().getParameters(wp));
            String onClic = "openPage('" + link + "');";
            String buttonHtml = A_CmsHtmlIconButton.defaultButtonHtml(
                CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
                id,
                adminTool.getHandler().getName(),
                adminTool.getHandler().getHelpText(),
                true,
                null,
                null,
                onClic);
            html = "<span>" + buttonHtml + NAVBAR_SEPARATOR + "</span>" + html;
        }
        html = CmsToolMacroResolver.resolveMacros(html, wp);
        html = CmsEncoder.decode(html);
        html = CmsToolMacroResolver.resolveMacros(html, wp);
        html = "<div class='pathbar'>\n" + html + "</div>\n";
        return html;
    }

    /**
     * Returns the base tool path for the active user.<p>
     *
     * @param wp the workplace object
     *
     * @return the base tool path for the active user
     */
    public String getBaseToolPath(CmsWorkplace wp) {

        CmsToolUserData userData = getUserData(wp);
        String path = TOOLPATH_SEPARATOR;
        if (userData != null) {
            path = userData.getBaseTool(getCurrentRoot(wp).getKey());
        }
        return path;
    }

    /**
     * Returns the current user's root handler.<p>
     *
     * @param wp the workplace context
     *
     * @return the current user's root handler
     */
    public CmsToolRootHandler getCurrentRoot(CmsWorkplace wp) {

        CmsToolUserData userData = getUserData(wp);
        String root = ROOTKEY_DEFAULT;
        if (userData != null) {
            if (m_roots.getObject(userData.getRootKey()) != null) {
                root = userData.getRootKey();
            } else {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(Messages.ERR_NOT_CONFIGURED_ROOT_1, userData.getRootKey()));
                }
            }
        }
        return m_roots.getObject(root);
    }

    /**
     * Returns the current tool.<p>
     *
     * @param wp the workplace object
     *
     * @return the current tool
     */
    public CmsTool getCurrentTool(CmsWorkplace wp) {

        return resolveAdminTool(getCurrentRoot(wp).getKey(), getCurrentToolPath(wp));
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
        String path = getBaseToolPath(wp);
        if (userData != null) {
            path = userData.getCurrentToolPath(getCurrentRoot(wp).getKey());
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

        if (toolPath.equals(getBaseToolPath(wp))) {
            return toolPath;
        }
        int pos = toolPath.lastIndexOf(TOOLPATH_SEPARATOR);
        return pos <= 0 ? TOOLPATH_SEPARATOR : toolPath.substring(0, pos);
    }

    /**
     * Returns a list with all registered tools.<p>
     *
     * @return list if <code>{@link CmsTool}</code>
     */
    public List<CmsTool> getToolHandlers() {

        return m_tools.elementList();
    }

    /**
     * Returns a list of tool roots.<p>
     *
     * @return a list of {@link CmsToolRootHandler} objects
     */
    public List<CmsToolRootHandler> getToolRoots() {

        return m_roots.elementList();
    }

    /**
     * Returns a list of all tools in the given path.<p>
     *
     * @param wp the workplace context
     * @param baseTool the path
     * @param includeSubtools if the tools in subfolders should be also returned
     *
     * @return a list of {@link CmsTool} objects
     */
    public List<CmsTool> getToolsForPath(CmsWorkplace wp, String baseTool, boolean includeSubtools) {

        List<CmsTool> toolList = new ArrayList<CmsTool>();
        String rootKey = getCurrentRoot(wp).getKey();
        Iterator<CmsTool> itTools = m_tools.elementList().iterator();
        while (itTools.hasNext()) {
            CmsTool tool = itTools.next();
            String path = tool.getHandler().getPath();
            if (resolveAdminTool(rootKey, path) != tool) {
                continue;
            }
            if (path.equals(TOOLPATH_SEPARATOR)) {
                continue;
            }
            // leave out everything above the base
            if (!path.startsWith(baseTool)) {
                continue;
            }
            // filter for path
            if (baseTool.equals(TOOLPATH_SEPARATOR) || path.startsWith(baseTool + TOOLPATH_SEPARATOR)) {
                // filter sub tree
                if (includeSubtools || (path.indexOf(TOOLPATH_SEPARATOR, baseTool.length() + 1) < 0)) {
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
            userData.setRootKey(ROOTKEY_DEFAULT);
            Iterator<CmsToolRootHandler> it = getToolRoots().iterator();
            while (it.hasNext()) {
                CmsToolRootHandler root = it.next();
                userData.setCurrentToolPath(root.getKey(), TOOLPATH_SEPARATOR);
                userData.setBaseTool(root.getKey(), TOOLPATH_SEPARATOR);
            }
            wp.getSettings().setToolUserData(userData);
        }
        return userData;
    }

    /**
     * Returns <code>true</code> if there is at least one tool registered using the given url.<p>
     *
     * @param url the url of the tool
     *
     * @return <code>true</code> if there is at least one tool registered using the given url
     */
    public boolean hasToolPathForUrl(String url) {

        List<String> toolPaths = m_urls.getObjectList(url);
        return ((toolPaths != null) && !toolPaths.isEmpty());
    }

    /**
     * This method initializes the tool manager for the current user.<p>
     *
     * @param wp the jsp page coming from
     */
    public synchronized void initParams(CmsToolDialog wp) {

        setCurrentRoot(wp, wp.getParamRoot());
        setCurrentToolPath(wp, wp.getParamPath());
        setBaseToolPath(wp, wp.getParamBase());

        // if the current tool path is not under the current root, set the current root as the current tool
        if (!getCurrentToolPath(wp).startsWith(getBaseToolPath(wp))) {
            setCurrentToolPath(wp, getBaseToolPath(wp));
        }
        wp.setParamPath(getCurrentToolPath(wp));
        wp.setParamBase(getBaseToolPath(wp));
        wp.setParamRoot(getCurrentRoot(wp).getKey());
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
    public void jspForwardPage(CmsWorkplace wp, String pagePath, Map<String, String[]> params)
    throws IOException, ServletException {

        Map<String, String[]> newParams = createToolParams(wp, pagePath, params);
        if (pagePath.indexOf("?") > 0) {
            pagePath = pagePath.substring(0, pagePath.indexOf("?"));
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
    public void jspForwardTool(CmsWorkplace wp, String toolPath, Map<String, String[]> params)
    throws IOException, ServletException {

        Map<String, String[]> newParams;
        if (params == null) {
            newParams = new HashMap<String, String[]>();
        } else {
            newParams = new HashMap<String, String[]>(params);
        }
        // update path param
        newParams.put(CmsToolDialog.PARAM_PATH, new String[] {toolPath});
        jspForwardPage(wp, VIEW_JSPPAGE_LOCATION, newParams);
    }

    /**
     * Returns the admin tool corresponding to the given abstract path.<p>
     *
     * @param rootKey the tool root
     * @param toolPath the path
     *
     * @return the corresponding tool, or <code>null</code> if not found
     */
    public CmsTool resolveAdminTool(String rootKey, String toolPath) {

        return m_tools.getObject(rootKey + ROOT_SEPARATOR + toolPath);
    }

    /**
     * Sets the base tool path.<p>
     *
     * @param wp the workplace object
     * @param baseToolPath the base tool path to set
     */
    public void setBaseToolPath(CmsWorkplace wp, String baseToolPath) {

        // use last used base if param empty
        if (CmsStringUtil.isEmpty(baseToolPath) || baseToolPath.trim().equals("null")) {
            baseToolPath = getBaseToolPath(wp);
        }
        baseToolPath = repairPath(wp, baseToolPath);
        // set it
        CmsToolUserData userData = getUserData(wp);
        userData.setBaseTool(userData.getRootKey(), baseToolPath);
    }

    /**
     * Sets the current user's root key.<p>
     *
     * @param wp the workplace context
     * @param key the current user's root key to set
     */
    public void setCurrentRoot(CmsWorkplace wp, String key) {

        // use last used root if param empty
        if (CmsStringUtil.isEmpty(key) || key.trim().equals("null")) {
            key = getCurrentRoot(wp).getKey();
        }
        // set it
        CmsToolUserData userData = getUserData(wp);
        userData.setRootKey(key);
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
        userData.setCurrentToolPath(userData.getRootKey(), currentToolPath);
    }

    /**
     * Configures a whole tool root with all its tools.<p>
     *
     * @param cms the cms context
     * @param toolRoot the tool root to configure
     *
     * @throws CmsException if something goes wrong
     */
    private void configureToolRoot(CmsObject cms, CmsToolRootHandler toolRoot) throws CmsException {

        List<I_CmsToolHandler> handlers = new ArrayList<I_CmsToolHandler>();

        // add tool root handler
        handlers.add(toolRoot);

        // look in every file under the root uri for valid
        // admin tools and register them
        List<CmsResource> resources = cms.readResourcesWithProperty(toolRoot.getUri(), HANDLERCLASS_PROPERTY);
        Iterator<CmsResource> itRes = resources.iterator();
        while (itRes.hasNext()) {
            CmsResource res = itRes.next();
            CmsProperty prop = cms.readPropertyObject(res.getRootPath(), HANDLERCLASS_PROPERTY, false);
            if (!prop.isNullProperty()) {
                try {
                    // instantiate the handler
                    Class<?> handlerClass = Class.forName(prop.getValue());
                    I_CmsToolHandler handler = (I_CmsToolHandler)handlerClass.newInstance();

                    if (!handler.setup(cms, toolRoot, res.getRootPath())) {
                        // log failure
                        if (CmsLog.INIT.isWarnEnabled()) {
                            CmsLog.INIT.warn(
                                Messages.get().getBundle().key(
                                    Messages.INIT_TOOLMANAGER_TOOL_SETUP_ERROR_1,
                                    res.getRootPath()));
                        }
                    }

                    // keep for later use
                    handlers.add(handler);
                    // log success
                    if (CmsLog.INIT.isDebugEnabled()) {
                        if (!handler.getLink().equals(VIEW_JSPPAGE_LOCATION)) {
                            CmsLog.INIT.debug(
                                Messages.get().getBundle().key(
                                    Messages.INIT_TOOLMANAGER_NEWTOOL_FOUND_2,
                                    handler.getPath(),
                                    handler.getLink()));
                        } else {
                            CmsLog.INIT.debug(
                                Messages.get().getBundle().key(
                                    Messages.INIT_TOOLMANAGER_NEWTOOL_FOUND_2,
                                    handler.getPath(),
                                    res.getRootPath()));
                        }
                    }
                } catch (Exception e) {
                    // log failure
                    if (CmsLog.INIT.isWarnEnabled()) {
                        CmsLog.INIT.warn(
                            Messages.get().getBundle().key(
                                Messages.INIT_TOOLMANAGER_TOOL_SETUP_ERROR_1,
                                res.getRootPath()),
                            e);
                    }
                }
            }
        }
        registerHandlerList(cms, toolRoot, 1, handlers);
    }

    /**
     * Creates a parameter map from the given url and additional parameters.<p>
     *
     * @param wp the workplace context
     * @param url the url to create the parameter map for (extracting query params)
     * @param params additional parameter map
     *
     * @return the new parameter map
     */
    private Map<String, String[]> createToolParams(CmsWorkplace wp, String url, Map<String, String[]> params) {

        Map<String, String[]> newParams = new HashMap<String, String[]>();
        // add query parameters to the parameter map if required
        if (url.indexOf("?") > 0) {
            String query = url.substring(url.indexOf("?"));
            Map<String, String[]> reqParameters = CmsRequestUtil.createParameterMap(query);
            newParams.putAll(reqParameters);
        }
        if (params != null) {
            newParams.putAll(params);
        }

        // put close link if not set
        if (!newParams.containsKey(CmsDialog.PARAM_CLOSELINK)) {
            Map<String, String[]> argMap = resolveAdminTool(
                getCurrentRoot(wp).getKey(),
                getCurrentToolPath(wp)).getHandler().getParameters(wp);
            newParams.put(
                CmsDialog.PARAM_CLOSELINK,
                new String[] {linkForToolPath(wp.getJsp(), getCurrentToolPath(wp), argMap)});
        }
        return newParams;
    }

    /**
     * Registers a new tool at a given install point for the given tool root.<p>
     *
     * @param cms the cms context object
     * @param toolRoot the tool root
     * @param handler the handler to install
     */
    private void registerAdminTool(CmsObject cms, CmsToolRootHandler toolRoot, I_CmsToolHandler handler) {

        String link = handler.getLink();
        if (link.indexOf("?") > 0) {
            link = link.substring(0, link.indexOf("?"));
        }
        // check visibility
        if (!cms.existsResource(link)) {
            return;
        }

        //validate path
        if (!validatePath(toolRoot.getKey(), handler.getPath(), false)) {
            // log failure
            if (CmsLog.INIT.isWarnEnabled()) {
                CmsLog.INIT.warn(
                    Messages.get().getBundle().key(
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
            String message = Messages.get().getBundle().key(
                Messages.INIT_TOOLMANAGER_INSTALL_ERROR_2,
                handler.getPath(),
                handler.getLink());
            if (CmsLog.INIT.isWarnEnabled()) {
                CmsLog.INIT.warn(message);
            } else if (CmsLog.INIT.isDebugEnabled()) {
                CmsLog.INIT.debug(message, ex);
            }
            return;
        }

        try {
            // try to register, can fail if path is already used by another tool
            m_tools.addIdentifiableObject(toolRoot.getKey() + ROOT_SEPARATOR + handler.getPath(), tool);
            // just for fast association of links with tools
            m_urls.addIdentifiableObject(link, handler.getPath());
        } catch (Throwable ex) {
            CmsLog.INIT.warn(
                Messages.get().getBundle().key(
                    Messages.INIT_TOOLMANAGER_DUPLICATED_ERROR_3,
                    handler.getPath(),
                    handler.getLink(),
                    resolveAdminTool(toolRoot.getKey(), handler.getPath()).getHandler().getLink()));
        }
    }

    /**
     * Registers all tool handlers recursively for a given tool root.<p>
     *
     * @param cms the cms context object
     * @param toolRoot the tool root
     * @param len the recursion level
     * @param handlers the list of handlers to register
     */
    private void registerHandlerList(
        CmsObject cms,
        CmsToolRootHandler toolRoot,
        int len,
        List<I_CmsToolHandler> handlers) {

        boolean found = false;
        Iterator<I_CmsToolHandler> it = handlers.iterator();
        while (it.hasNext()) {
            I_CmsToolHandler handler = it.next();
            int myLen = CmsStringUtil.splitAsArray(handler.getPath(), TOOLPATH_SEPARATOR).length;
            if (((len == myLen) && !handler.getPath().equals(TOOLPATH_SEPARATOR))
                || ((len == 1) && handler.getPath().equals(TOOLPATH_SEPARATOR))) {
                found = true;
                registerAdminTool(cms, toolRoot, handler);
            }
        }
        if (found) {
            registerHandlerList(cms, toolRoot, len + 1, handlers);
        }

    }

    /**
     * Given a string a valid and visible tool path is computed.<p>
     *
     * @param wp the workplace object
     * @param path the path to repair
     *
     * @return a valid and visible tool path
     */
    private String repairPath(CmsWorkplace wp, String path) {

        String rootKey = getCurrentRoot(wp).getKey();
        // navigate until to reach a valid path
        while (!validatePath(rootKey, path, true)) {
            // log failure
            LOG.warn(Messages.get().getBundle().key(Messages.LOG_MISSING_ADMIN_TOOL_1, path));
            // try parent
            path = getParent(wp, path);
        }
        // navigate until to reach a valid tool
        while (resolveAdminTool(rootKey, path) == null) {
            // log failure
            LOG.warn(Messages.get().getBundle().key(Messages.LOG_MISSING_ADMIN_TOOL_1, path));
            // try parent
            path = getParent(wp, path);
        }

        // navigate until to reach an enabled path
        CmsTool aTool = resolveAdminTool(rootKey, path);
        while (!aTool.getHandler().isEnabled(wp)) {
            if (aTool.getHandler().getLink().equals(VIEW_JSPPAGE_LOCATION)) {
                // just grouping
                break;
            }
            path = getParent(wp, path);
            aTool = resolveAdminTool(rootKey, path);
        }

        return path;
    }

    /**
     * Tests if the full tool path is available.<p>
     *
     * @param rootKey the root tool
     * @param toolPath the path
     * @param full if <code>true</code> the whole path is checked, if not the last part is not checked (for new tools)
     *
     * @return if valid or not
     */
    private boolean validatePath(String rootKey, String toolPath, boolean full) {

        if (toolPath.equals(TOOLPATH_SEPARATOR)) {
            return true;
        }
        if (!toolPath.startsWith(TOOLPATH_SEPARATOR)) {
            return false;
        }
        List<String> groups = CmsStringUtil.splitAsList(toolPath, TOOLPATH_SEPARATOR);
        Iterator<String> itGroups = groups.iterator();
        String subpath = "";
        while (itGroups.hasNext()) {
            String group = itGroups.next();
            if (subpath.length() != TOOLPATH_SEPARATOR.length()) {
                subpath += TOOLPATH_SEPARATOR + group;
            } else {
                subpath += group;
            }
            if (itGroups.hasNext() || full) {
                try {
                    // just check if the tool is available
                    resolveAdminTool(rootKey, subpath).toString();
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return true;
    }
}