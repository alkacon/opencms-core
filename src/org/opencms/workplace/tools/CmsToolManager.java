/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/CmsToolManager.java,v $
 * Date   : $Date: 2005/05/30 15:50:45 $
 * Version: $Revision: 1.19 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsIdentifiableObjectContainer;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Manages the registered tools, actualizing its state every time the workplace is reinitialize.<p>
 * 
 * Manages also the configuration settings for the administration view, and provides
 * several tool related methods.<p>
 *
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.19 $
 * @since 5.7.3
 */
public class CmsToolManager {

    /**  Root location for the tools. */
    public static final String C_ADMINTOOLS_ROOT_LOCATION = CmsWorkplace.C_PATH_WORKPLACE + "admin";

    /**  Root location of the administration view. */
    public static final String C_ADMINVIEW_ROOT_LOCATION = CmsWorkplace.C_PATH_WORKPLACE + "views/admin";

    /**  Property definition name to look for. */
    public static final String C_HANDLERCLASS_PROPERTY = "admintoolhandler-class";

    /**  Navegation bar separator (html code). */
    public static final String C_NAVBAR_SEPARATOR = "\n&nbsp;&gt;&nbsp;\n";

    /**  Tool path separator. */
    public static final String C_TOOLPATH_SEPARATOR = "/";

    /** Location of the admin view jsp page. */
    public static final String C_VIEW_JSPPAGE_LOCATION = C_ADMINVIEW_ROOT_LOCATION + "/admin-main.html";

    /** List of all available tools. */
    private final CmsIdentifiableObjectContainer m_tools = new CmsIdentifiableObjectContainer(true, false);

    /** List of all available urls and related tool paths. */
    private final CmsIdentifiableObjectContainer m_urls = new CmsIdentifiableObjectContainer(true, false);

    /**
     * Default Constructor, called by the <code>{@link org.opencms.workplace.CmsWorkplaceManager#initialize(CmsObject)}</code> method.<p>
     * 
     * @param cms the cms context
     */
    public CmsToolManager(CmsObject cms) {

        if (!cms.existsResource(C_VIEW_JSPPAGE_LOCATION)) {
            if (CmsLog.LOG.isInfoEnabled()) {
                CmsLog.LOG.info(Messages.get().key(Messages.INIT_TOOLMANAGER_NOT_CREATED_0));
            }
            return;
        }
        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(Messages.get().key(Messages.INIT_TOOLMANAGER_CREATED_0));
        }
        try {
            List handlers = new ArrayList();

            I_CmsToolHandler rootHandler = new CmsRootToolHandler();
            rootHandler.setup(cms, C_VIEW_JSPPAGE_LOCATION);
            handlers.add(rootHandler);

            // look in every file under C_ADMINTOOLS_ROOT_LOCATION for valid
            // admin tools and register them
            List resources = cms.readResourcesWithProperty(C_ADMINTOOLS_ROOT_LOCATION, C_HANDLERCLASS_PROPERTY);
            Iterator itRes = resources.iterator();
            while (itRes.hasNext()) {
                CmsResource res = (CmsResource)itRes.next();
                CmsProperty prop = cms.readPropertyObject(res.getRootPath(), C_HANDLERCLASS_PROPERTY, false);
                if (!prop.isNullProperty()) {
                    try {
                        // instantiate the handler
                        Class handlerClass = Class.forName(prop.getValue());
                        I_CmsToolHandler handler = (I_CmsToolHandler)handlerClass.newInstance();
                        handler.setup(cms, res.getRootPath());

                        // keep for later use
                        handlers.add(handler);
                        // log success
                        if (CmsLog.LOG.isDebugEnabled()) {
                            if (!handler.getLink().equals(C_VIEW_JSPPAGE_LOCATION)) {
                                CmsLog.LOG.debug(Messages.get().key(
                                    Messages.INIT_TOOLMANAGER_NEWTOOL_FOUND_1,
                                    handler.getLink()));
                            } else {
                                CmsLog.LOG.debug(Messages.get().key(
                                    Messages.INIT_TOOLMANAGER_NEWTOOL_FOUND_1,
                                    res.getRootPath()));
                            }
                        }
                    } catch (Exception e) {
                        // log failure
                        if (CmsLog.LOG.isWarnEnabled()) {
                            CmsLog.LOG.warn(Messages.get().key(
                                Messages.INIT_TOOLMANAGER_TOOL_SETUP_ERROR_1,
                                res.getRootPath()), e);
                        }
                    }
                }
            }
            registerHandlerList(cms, 1, handlers);
        } catch (CmsException e) {
            // log failure
            if (CmsLog.LOG.isErrorEnabled()) {
                CmsLog.LOG.error(Messages.get().key(Messages.INIT_TOOLMANAGER_SETUP_ERROR_0), e);
            }
        }

    }

    /**
     * Returns a valid link for the given tool path.<p>
     * 
     * @param jsp the jsp action element
     * @param toolPath the tool path
     * @param params a map of additional parameters
     * 
     * @return a valid OpenCms link
     */
    public String linkForPath(CmsJspActionElement jsp, String toolPath, Map params) {

        if (params == null) {
            params = new HashMap();
        }
        params.put(CmsToolDialog.PARAM_PATH, toolPath);
        return jsp.link(rewriteUrl(C_VIEW_JSPPAGE_LOCATION, params));
    }
    
    /**
     * Returns the tool path for the given url.<p>
     * 
     * @param url the url of the tool
     * 
     * @return the associated tool path
     */
    public String getToolPathForUrl(String url) {
        
        return (String)m_urls.getObject(url);
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
        String html = A_CmsHtmlIconButton.defaultButtonHtml(CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT, "nav"
            + adminTool.getId(), adminTool.getHandler().getShortName(), null, false, null, null);
        String parent = toolPath;
        while (!parent.equals(getRootToolPath(wp))) {
            parent = getParent(wp, parent);
            adminTool = resolveAdminTool(parent);

            String id = "nav" + adminTool.getId();
            String onClic = "openPage('" + linkForPath(wp.getJsp(), parent, null) + "');";
            String link = A_CmsHtmlIconButton.defaultButtonHtml(
                CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
                id,
                adminTool.getHandler().getName(),
                adminTool.getHandler().getHelpText(),
                true,
                null,
                onClic);
            html = link + C_NAVBAR_SEPARATOR + html;
        }

        return wp.resolveMacros("<div class='pathbar'>\n" + html + "&nbsp;</div>\n");
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
        int pos = toolPath.lastIndexOf(C_TOOLPATH_SEPARATOR);
        if (pos < 0) {
            pos = -1;
        }
        return pos == 0 ? C_TOOLPATH_SEPARATOR : toolPath.substring(0, pos);
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
        String path = C_TOOLPATH_SEPARATOR;
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
            if (tool.equals(C_TOOLPATH_SEPARATOR)) {
                continue;
            }
            // filter for path
            if (toolPath.equals(C_TOOLPATH_SEPARATOR) || tool.startsWith(toolPath + C_TOOLPATH_SEPARATOR)) {
                // filter sub tree
                if (includeSubtools || tool.indexOf(C_TOOLPATH_SEPARATOR, toolPath.length() + 1) < 0) {
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
            userData.setCurrentToolPath(C_TOOLPATH_SEPARATOR);
            userData.setRootTool(C_TOOLPATH_SEPARATOR);
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
    public synchronized void initParams(CmsWorkplace wp, String toolPath, String rootToolPath) {

        setCurrentToolPath(wp, toolPath);
        setRootToolPath(wp, rootToolPath);

        // if the current tool path is not under the current root, set the current root as the current tool
        if (!getCurrentToolPath(wp).startsWith(getRootToolPath(wp))) {
            setCurrentToolPath(wp, getRootToolPath(wp));
        }

    }

    /**
     * Redirects to the given tool with the given parameters.<p>
     * 
     * @param wp the workplace object
     * @param toolPath the path to the tool to redirect to
     * @param params the parameters to send
     * 
     * @throws IOException if something goes wrong
     */
    public void jspRedirectTool(CmsWorkplace wp, String toolPath, Map params) throws IOException {

        Map myParams = params;
        if (myParams == null) {
            myParams = new HashMap();
        }
        // remove path param
        if (myParams.containsKey(CmsToolDialog.PARAM_PATH)) {
            myParams.remove(CmsToolDialog.PARAM_PATH);
        }
        // put close link if not set
        if (!myParams.containsKey(CmsDialog.PARAM_CLOSELINK)) {
            myParams.put(CmsDialog.PARAM_CLOSELINK, linkForPath(wp.getJsp(), getCurrentToolPath(wp), null));
        }
        wp.getJsp().getResponse().sendRedirect(linkForPath(wp.getJsp(), toolPath, myParams));
    }

    /**
     * Redirects to the given page with the given parameters.<p>
     * 
     * @param wp the workplace object
     * @param pagePath the path to the page to redirect to
     * @param params the parameters to send
     * 
     * @throws IOException if something goes wrong
     */
    public void jspRedirectPage(CmsWorkplace wp, String pagePath, Map params) throws IOException {

        Map myParams = params;
        if (myParams == null) {
            myParams = new HashMap();
        }
        // put close link if not set
        if (!myParams.containsKey(CmsDialog.PARAM_CLOSELINK)) {
            myParams.put(CmsDialog.PARAM_CLOSELINK, linkForPath(wp.getJsp(), getCurrentToolPath(wp), null));
        }
        wp.getJsp().getResponse().sendRedirect(wp.getJsp().link(rewriteUrl(pagePath, myParams)));
    }

    /**
     * Returns a link for a given baseUrl and parameters.<p>
     * 
     * @param baseUrl the base url
     * @param params a map of additional parameters
     * 
     * @return the link
     */
    private String rewriteUrl(String baseUrl, Map params) {

        if (params == null) {
            return baseUrl;
        }
        StringBuffer link = new StringBuffer(512);
        link.append(baseUrl);
        String sep = "?";
        if (baseUrl.indexOf('?')>-1) {
            sep = "&";
        } 

        boolean first = true;
        Iterator it = params.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next().toString();
            Object value = params.get(key);
            if (value instanceof String[]) {
                for (int j = 0; j < ((String[])value).length; j++) {
                    String val = CmsEncoder.encode(((String[])value)[j]);
                    link.append(sep);
                    link.append(key);
                    link.append("=");
                    link.append(val);
                    if (first) {
                        sep = "&";
                        first = false;
                    }
                }
            } else {
                link.append(sep);
                link.append(key);
                link.append("=");
                link.append(CmsEncoder.encode(value.toString()));
                if (first) {
                    sep = "&";
                    first = false;
                }
            }
        }

        return link.toString();
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

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(currentToolPath) || currentToolPath.trim().equals("null")) {
            return;
        }
        while (!validatePath(currentToolPath)) {            
            currentToolPath = getParent(wp, currentToolPath);
        }
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

        if (CmsStringUtil.isEmpty(rootToolPath) || rootToolPath.trim().equals("null")) {
            return;
        }
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

        // check visibility
        if (!cms.existsResource(handler.getLink())) {
            return;
        }

        //validate path
        if (!validatePath(handler.getPath())) {
            // log failure
            if (CmsLog.LOG.isWarnEnabled()) {
                CmsLog.LOG.warn(Messages.get().key(Messages.INIT_TOOLMANAGER_INCONSISTENT_PATH_1, handler.getLink()));
            }
            return;
        }

        String id = "tool" + m_tools.elementList().size();
        CmsTool tool = new CmsTool(id, handler);

        // try to register
        try {
            m_tools.addIdentifiableObject(handler.getPath(), tool);
            m_urls.addIdentifiableObject(handler.getLink(), handler.getPath());
        } catch (RuntimeException ex) {
            // noop
        }
    }

    /**
     * Registers all tool handlers recursively.<p> 
     * 
     * @param cms the cms context object
     * @param len the recursion level
     */
    private void registerHandlerList(CmsObject cms, int len, List handlers) {

        boolean found = false;
        Iterator it = handlers.iterator();
        while (it.hasNext()) {
            I_CmsToolHandler handler = (I_CmsToolHandler)it.next();
            int myLen = CmsStringUtil.splitAsArray(handler.getPath(), C_TOOLPATH_SEPARATOR).length;
            if ((len == myLen && !handler.getPath().equals(C_TOOLPATH_SEPARATOR))
                || (len == 1 && handler.getPath().equals(C_TOOLPATH_SEPARATOR))) {
                found = true;
                registerAdminTool(cms, handler);
            }
        }
        if (found) {
            registerHandlerList(cms, len + 1, handlers);
        }

    }

    /**
     * Tests if the full tool path is available.<p>
     * 
     * @param toolPath the path
     * 
     * @return if valid or not
     */
    private boolean validatePath(String toolPath) {

        if (toolPath.equals(C_TOOLPATH_SEPARATOR)) {
            return true;
        }
        List folders = CmsStringUtil.splitAsList(toolPath, C_TOOLPATH_SEPARATOR);
        Iterator itFolder = folders.iterator();
        String subpath = "";
        while (itFolder.hasNext()) {
            String folder = (String)itFolder.next();
            if (subpath.length() != C_TOOLPATH_SEPARATOR.length()) {
                subpath += C_TOOLPATH_SEPARATOR + folder;
            } else {
                subpath += folder;
            }
            if (itFolder.hasNext()) {
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