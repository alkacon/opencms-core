/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/CmsToolManager.java,v $
 * Date   : $Date: 2005/04/14 13:11:15 $
 * Version: $Revision: 1.3 $
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
import org.opencms.file.CmsResourceFilter;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsNamedObject;
import org.opencms.util.CmsNamedObjectContainer;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.I_CmsNamedObject;
import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Manages the configuration settings for the administration view.<p>
 * 
 * It mantains a <code>{@link CmsToolUserData}</code> object for each logged-in user.
 * This information is updated everytime an admin tool is displayed, mainly, due to 
 * i18n of group names. <p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.3 $
 * @since 5.7.3
 */
public class CmsToolManager {

    /**  This is the root location of the administration view. */
    public static final String C_ADMIN_ROOT = CmsWorkplace.C_PATH_WORKPLACE + "views/admin";

    /**  This is the property definition name to look for. */
    public static final String C_HANDLERCLASS_PROPERTY = "admintoolhandler-class";

    /**  This is the navegation bar separator. */
    public static final String C_NAVBAR_SEPARATOR = "&nbsp;&gt;&nbsp;";

    /**  This is the tool path separator. */
    public static final String C_TOOLPATH_SEPARATOR = "/";

    /**  This is the root location for the tools. */
    public static final String C_TOOLS_ROOT = CmsWorkplace.C_PATH_WORKPLACE + "admin";

    /** The location of the admin view jsp page. */
    public static final String C_VIEW_JSPPAGE_LOCATION = C_ADMIN_ROOT + "/admin-main.html";

    private List m_handlers = new ArrayList();

    private final CmsNamedObjectContainer m_userDataContainer = new CmsNamedObjectContainer(true, false);

    /**
     * Default Constructor, called by the <code>{@link org.opencms.workplace.CmsWorkplaceManager#initialize(CmsObject)}</code> method.<p>
     * 
     * @param cms the cms context
     */
    public CmsToolManager(CmsObject cms) {

        if (!cms.existsResource(C_VIEW_JSPPAGE_LOCATION)) {
            // log failure
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Tool Manager not initialized during setup");
            }
            return;
        }
        try {            
            I_CmsToolHandler rootHandler = new CmsGenericToolHandler();
            rootHandler.setup(cms, C_VIEW_JSPPAGE_LOCATION);
            m_handlers.add(rootHandler);

            // look in every file under C_ADMIN_ROOT for valid
            // admin tools and register them
            CmsResourceFilter filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED;
            List resources = cms.readResources(C_TOOLS_ROOT, filter, true);
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

                        // keep for later registration
                        m_handlers.add(handler);
                        // log success
                        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                                ". Administration init  : new tool "
                                    + handler.getName()
                                    + "("
                                    + handler.getLink()
                                    + ")");
                        }
                    } catch (Exception e) {
                        // log failure
                        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                            OpenCms.getLog(CmsLog.CHANNEL_INIT).warn(
                                ". Administration init  : warning(" + res.getRootPath() + ")",
                                e);
                        }
                    }
                }
            }
        } catch (CmsException e) {
            // log failure
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isErrorEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).error(". Administration init  : ", e);
            }
        }

    }

    /**
     * Returns a valid OpenCms link for the given tool path.<p>
     * 
     * @param jsp the jsp action element
     * @param toolPath the tool path
     * 
     * @return a valid OpenCms link
     */
    public String cmsLinkForPath(CmsJspActionElement jsp, String toolPath) {

        return jsp.link(linkForPath(toolPath));
    }

    /**
     * Returns a valid OpenCms link for the given tool.<p>
     * 
     * @param jsp the jsp action element
     * @param adminTool the tool 
     * 
     * @return a valid OpenCms link
     */
    public String cmsLinkFromContext(CmsJspActionElement jsp, CmsTool adminTool) {

        return cmsLinkFromContext(jsp, getCurrentToolPath(jsp.getCmsObject()), adminTool);
    }

    /**
     * Returns a valid OpenCms link for the given tool.<p>
     * 
     * @param jsp the jsp action element
     * @param context the tool path context
     * @param adminTool the tool 
     * 
     * @return a valid OpenCms link
     */
    public String cmsLinkFromContext(CmsJspActionElement jsp, String context, CmsTool adminTool) {

        return cmsLinkForPath(jsp, pathFromContext(jsp, context, adminTool));
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

        if (toolPath.equals(getRootToolPath(wp.getCms()))) {
            return "<div class='pathbar'>&nbsp;</div>";
        }
        CmsTool adminTool = resolveAdminTool(wp.getCms(), toolPath);
        String html = adminTool.getName();
        String parent = toolPath;
        while (!parent.equals(getRootToolPath(wp.getCms()))) {
            parent = getParent(wp.getCms(), parent);
            adminTool = resolveAdminTool(wp.getCms(), parent);

            StringBuffer link = new StringBuffer(512);
            link.append("<a href='");
            link.append(cmsLinkForPath(wp.getJsp(), parent));
            link.append("' onClick='loadingOn();'>");
            link.append(adminTool.getName());
            link.append("</a>\n");
            html = link + C_NAVBAR_SEPARATOR + html;
        }

        return "<div class='pathbar'>&nbsp;" + html + "&nbsp;</div>";
    }

    /**
     * Returns the current tool.<p>
     * 
     * @param cms the cms context
     *
     * @return the current tool 
     */
    public CmsTool getCurrentTool(CmsObject cms) {

        return resolveAdminTool(cms, getCurrentToolPath(cms));
    }

    /**
     * Returns the current tool path.<p>
     *
     * @param cms the cms context
     *
     * @return the current tool path
     */
    public String getCurrentToolPath(CmsObject cms) {

        CmsToolUserData userData = getUserData(cms);
        String path = getRootToolPath(cms);
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
     * @param cms the cms context
     * @param toolPath the abstract tool path
     * 
     * @return his parent
     */
    public String getParent(CmsObject cms, String toolPath) {

        if (toolPath.equals(getRootToolPath(cms))) {
            return toolPath;
        }
        int pos = toolPath.lastIndexOf(C_TOOLPATH_SEPARATOR);
        if (pos < 0) {
            pos = -1;
        }
        return pos == 0 ? C_TOOLPATH_SEPARATOR : toolPath.substring(0, pos);
    }

    /**
     * Returns a list of paths for the given tool.<p>
     * 
     * @param jsp the jsp action
     * @param tool the tool
     * 
     * @return a list of abstract tools paths
     */
    public List getPathsForTool(CmsJspActionElement jsp, CmsTool tool) {

        List toolList = new ArrayList();

        Iterator itTools = getUserData(jsp.getCmsObject()).getTools().elementList().iterator();
        while (itTools.hasNext()) {
            String aTool = ((I_CmsNamedObject)itTools.next()).getName();
            // filter for path
            if (resolveAdminTool(jsp.getCmsObject(), aTool).equals(tool)) {
                toolList.add(aTool);
            }
        }
        return toolList;
    }

    /**
     * Returns the root tool path for the active user.<p>
     * 
     * @param cms the cms context
     * 
     * @return the root tool path for the active user
     */
    public String getRootToolPath(CmsObject cms) {

        CmsToolUserData userData = getUserData(cms);
        String path = C_TOOLPATH_SEPARATOR;
        if (userData != null) {
            path = userData.getRootTool();
        }
        return path;
    }

    /**
     * Returns a list of all tools in the given path.<p>
     * 
     * @param cms the cms context
     * @param toolPath the path
     * @param includeSubtools if the tools in subfolders should be also returned
     * 
     * @return a list of abstract tools paths
     */
    public List getToolsForPath(CmsObject cms, String toolPath, boolean includeSubtools) {

        List toolList = new ArrayList();

        Iterator itTools = getUserData(cms).getTools().elementList().iterator();
        while (itTools.hasNext()) {
            String tool = ((I_CmsNamedObject)itTools.next()).getName();
            // filter for path
            if (tool.startsWith(toolPath) && !tool.equals(toolPath)) {
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
     * @param cms the cms context, with the proper user
     *
     * @return the current tool path
     */
    public CmsToolUserData getUserData(CmsObject cms) {

        CmsToolUserData userData = (CmsToolUserData)m_userDataContainer.getObject(cms.getRequestContext()
            .currentUser().getId().toString());
        if (userData == null) {
            userData = new CmsToolUserData(cms.getRequestContext().currentUser());
            m_userDataContainer.addNamedObject(userData);
            userData.setCurrentToolPath(C_TOOLPATH_SEPARATOR);
            userData.setRootTool(C_TOOLPATH_SEPARATOR);
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
    public void initParams(CmsWorkplace wp, String toolPath, String rootToolPath) {

        setCurrentToolPath(wp.getCms(), toolPath);
        setRootToolPath(wp.getCms(), rootToolPath);

        // if the current tool path is not under the current root, set the current root as the current tool
        if (!getCurrentToolPath(wp.getCms()).startsWith(getRootToolPath(wp.getCms()))) {
            setCurrentToolPath(wp.getCms(), getRootToolPath(wp.getCms()));
        }
        registerHandlerList(wp);

    }

    /**
     * Returns a link for a given path.<p>
     * 
     * @param toolPath the path
     * 
     * @return the link
     */
    public String linkForPath(String toolPath) {

        return C_VIEW_JSPPAGE_LOCATION + "?" + CmsToolDialog.PARAM_PATH + "=" + toolPath;
    }

    /**
     * Returns a link for the given tool in the context.<p>
     * 
     * @param jsp the jsp action
     * @param context the context path
     * @param adminTool the tool 
     * 
     * @return a link
     */
    public String linkFromContext(CmsJspActionElement jsp, String context, CmsTool adminTool) {

        return linkForPath(pathFromContext(jsp, context, adminTool));
    }

    /**
     * Returns a link for the given tool.<p>
     * 
     * @param jsp the jsp action
     * @param context the tool path context
     * @param adminTool the tool 
     * 
     * @return a valid OpenCms link
     */
    public String pathFromContext(CmsJspActionElement jsp, String context, CmsTool adminTool) {

        String toolPath = null;
        List tools = getPathsForTool(jsp, adminTool);
        Iterator itTools = tools.iterator();
        while (itTools.hasNext()) {
            String aTool = (String)itTools.next();
            if (aTool.startsWith(context)) {
                if (aTool.substring(context.length() + 1).indexOf(C_TOOLPATH_SEPARATOR) < 0) {
                    toolPath = aTool;
                }
            }
        }
        if (toolPath == null) {
            // log failure
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).warn(
                    ". Administration link  : Error for Tool " + adminTool.getName() + ", context: " + context);
            }
        }
        return toolPath;
    }

    /**
     * Returns the admin tool corresponding to the given abstract path.<p>
     * 
     * @param cms the cms context
     * @param toolPath the path
     * 
     * @return the corresponding tool, or <code>null</code> if not found
     */
    public CmsTool resolveAdminTool(CmsObject cms, String toolPath) {

        try {
            return (CmsTool)((CmsNamedObject)getUserData(cms).getTools().getObject(toolPath)).getObject();
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Sets the current tool path.<p>
     * 
     * @param cms the cms context
     * @param currentToolPath the current tool path to set
     */
    public void setCurrentToolPath(CmsObject cms, String currentToolPath) {

        if (CmsStringUtil.isEmpty(currentToolPath) || currentToolPath.trim().equals("null")) {
            return;
        }
        CmsToolUserData userData = getUserData(cms);
        if (userData == null) {
            userData = new CmsToolUserData(cms.getRequestContext().currentUser());
            userData.setCurrentToolPath(currentToolPath);
            userData.setRootTool(getRootToolPath(cms));
            m_userDataContainer.addNamedObject(userData);
        } else {
            userData.setCurrentToolPath(currentToolPath);
        }
    }

    /**
     * Sets the root tool path.<p>
     * 
     * @param cms the cms context
     * @param rootToolPath the root tool path to set
     */
    public void setRootToolPath(CmsObject cms, String rootToolPath) {

        if (CmsStringUtil.isEmpty(rootToolPath) || rootToolPath.trim().equals("null")) {
            return;
        }
        CmsToolUserData userData = getUserData(cms);
        if (userData == null) {
            userData = new CmsToolUserData(cms.getRequestContext().currentUser());
            userData.setCurrentToolPath(getCurrentToolPath(cms));
            userData.setRootTool(rootToolPath);
            m_userDataContainer.addNamedObject(userData);
        } else {
            userData.setRootTool(rootToolPath);
        }
    }

    private void registerAdminTool(CmsWorkplace wp, CmsTool adminTool, CmsToolInstallPoint iPoint) {

        // check visibility
        if (!wp.getCms().existsResource(adminTool.getLink())) {
            return;
        }

        //validate path
        if (!validatePath(wp.getCms(), iPoint.getPath())) {
            // log failure
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).warn(
                    ". Administration init  : tool " + adminTool.getName() + " could not be installed at " + iPoint);
            }  
            return;
        }
        
        // root tool special case
        if (iPoint.getPath().equals(C_TOOLPATH_SEPARATOR)) {
            getUserData(wp.getCms()).getTools().addNamedObject(new CmsNamedObject(iPoint.getPath(), adminTool));
            return;
        }

        // locate group
        CmsToolGroup group = null;
        String groupName = wp.resolveMacros(iPoint.getGroup());

        // in the parent tool
        CmsTool parentTool = resolveAdminTool(wp.getCms(), getParent(wp.getCms(), iPoint.getPath()));
        group = parentTool.getToolGroup(groupName);
        if (group == null) {
            // if does not exist, create it
            group = new CmsToolGroup(groupName);
            parentTool.addToolGroup(group, iPoint.getPosition());
        }
        // add to group
        group.addAdminTool(adminTool, iPoint.getPosition());
        // register
        getUserData(wp.getCms()).getTools().addNamedObject(new CmsNamedObject(iPoint.getPath(), adminTool));
    }

    private void registerHandlerList(CmsWorkplace wp) {

        CmsToolUserData userData = getUserData(wp.getCms());
        synchronized (userData) {
            userData.getTools().clear();
            String tmpRoot = getRootToolPath(wp.getCms());
            setRootToolPath(wp.getCms(), C_TOOLPATH_SEPARATOR);
            registerHandlerList(wp, 1);
            setRootToolPath(wp.getCms(), tmpRoot);
        }
    }

    private void registerHandlerList(CmsWorkplace wp, int len) {

        boolean found = false;
        Iterator it = m_handlers.iterator();
        while (it.hasNext()) {
            I_CmsToolHandler handler = (I_CmsToolHandler)it.next();
            CmsTool adminTool = null;
            List installPoints = new ArrayList();
            Iterator itIPoints = handler.getInstallPoints().iterator();
            while (itIPoints.hasNext()) {
                CmsToolInstallPoint iPoint = (CmsToolInstallPoint)itIPoints.next();
                if (resolveAdminTool(wp.getCms(), iPoint.getPath()) != null) {
                    adminTool = resolveAdminTool(wp.getCms(), iPoint.getPath());
                }
                int myLen = CmsStringUtil.splitAsArray(iPoint.getPath(), C_TOOLPATH_SEPARATOR).length;
                if (len == myLen && !iPoint.getPath().equals(C_TOOLPATH_SEPARATOR)) {
                    installPoints.add(iPoint);
                }
                if (len == 1 && iPoint.getPath().equals(C_TOOLPATH_SEPARATOR)) {
                    // root tool special case
                    installPoints.add(iPoint);
                    break;
                }
            }
            if (!installPoints.isEmpty()) {
                found = true;
                if (adminTool == null) {
                    String name = handler.getName();
                    String helpText = handler.getHelpText();
                    adminTool = new CmsTool(name, handler.getIconPath(), handler.getLink(), helpText, handler
                        .isEnabled(wp.getCms()));
                }
                Iterator itFoundIPs = installPoints.iterator();
                while (itFoundIPs.hasNext()) {
                    CmsToolInstallPoint iPoint = (CmsToolInstallPoint)itFoundIPs.next();
                    registerAdminTool(wp, adminTool, iPoint);
                }
            }
        }
        if (found) {
            registerHandlerList(wp, len + 1);
        }

    }

    /**
     * Tests if the full tool path is available.<p>
     * 
     * @param toolPath the path
     */
    private boolean validatePath(CmsObject cms, String toolPath) {

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
                    resolveAdminTool(cms, subpath).toString();
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the list of registered tool handlers.<p>
     * 
     * @return a list of registered tool handlers
     */
    public List getToolHandlers() {

        return Collections.unmodifiableList(m_handlers);
    }

}