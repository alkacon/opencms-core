/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsFrameset.java,v $
 * Date   : $Date: 2005/03/04 15:11:32 $
 * Version: $Revision: 1.61 $
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
package org.opencms.workplace;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsBroadcastMessage;
import org.opencms.main.CmsBroadcastMessageQueue;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManager;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides methods for building the main framesets of the OpenCms Workplace.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/views/top.html
 * <li>/views/top_foot.html
 * <li>/views/top_head.html
 * </ul>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.61 $
 * 
 * @since 5.1
 */
public class CmsFrameset extends CmsWorkplace {
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsFrameset(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected synchronized void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // check if the user requested a project change
        String newProject = request.getParameter("wpProject");
        if (newProject != null) {
            settings.setProject(Integer.parseInt(newProject));
        }                   

        // check if the user requested a view change
        String newView = request.getParameter("wpView");
        if (newView != null) {
            settings.setViewUri(newView);
            // TODO: This is a workaround to make dialogs work in the legacy XMLTemplate views
            settings.getFrameUris().put("body", newView);
            settings.getFrameUris().put("admin_content", "/system/workplace/action/administration_content_top.html");
        }
        
        // check if the user requested a site change
        String newSite = request.getParameter("wpSite");
        if (newSite != null) {
            settings.setSite(newSite);
        }        
    } 
    
    /**
     * Builds the Javascript for the Workplace context menus.<p>
     * 
     * @return the Javascript for the Workplace context menus
     */
    public String buildContextMenues() {
        StringBuffer result = new StringBuffer();  
        // get all available resource types
        List allResTypes = OpenCms.getResourceManager().getResourceTypes();
        for (int i=0; i<allResTypes.size(); i++) {
            // loop through all types
            I_CmsResourceType type = (I_CmsResourceType)allResTypes.get(i);
            int resTypeId = type.getTypeId();
            // get explorer type settings for current resource type
            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName());
            if (settings != null) {
                // append the context menu of the current resource type 
                result.append(settings.getContextMenu().getJSEntries(getCms(), settings, resTypeId, getSettings().getUserSettings().getLocale()));
            }
        }         
        return result.toString();      
    }
    
    /**
     * Returns the javascript code for the broadcast message alert in the foot of the workplace.<p>
     * 
     * @return javascript code showing an alert box when the foot load
     */
    public String getBroadcastMessage() {
        StringBuffer result = new StringBuffer(512);
        String sessionId = getSession().getId();
        CmsObject cms = getCms();

        CmsBroadcastMessageQueue messageQueue = OpenCms.getSessionInfoManager().getBroadcastMessageQueue(sessionId);
        if (messageQueue.hasBroadcastMessagesPending()) {
            // create a javascript alert for the message 
            result.append("\n<script type=\"text/javascript\">\n<!--\n");
            // the timeout gives the frameset enough time to load before the alert is shown
            result.append("setTimeout(\"alert(unescape('");            
            // the user has pending messages, display them all
            while (messageQueue.hasBroadcastMessagesPending()) {
                CmsBroadcastMessage message = messageQueue.getNextBroadcastMessage();
                StringBuffer msg = new StringBuffer(256);
                msg.append('[');
                msg.append(getSettings().getMessages().getDateTime(message.getSendTime()));
                msg.append("] Message from ");
                msg.append(message.getUser().getName());
                msg.append(":\n");
                msg.append(message.getMessage());
                msg.append("\n\n");
                result.append(CmsEncoder.escape(msg.toString(), cms.getRequestContext().getEncoding()));
            }
            result.append("'));\", 2000);");   
            result.append("\n//-->\n</script>");            
        }
        return result.toString();
    }
    
    /**
     * Returns the file settings for the Workplace explorer view.<p>
     * 
     * @return the file settings for the Workplace explorer view
     */
    public int getExplorerSettings() {
        CmsUserSettings settings = new CmsUserSettings(getCms().getRequestContext().currentUser());
        int value = settings.getExplorerSettings();
        return value;
    }
    
    /**
     * Returns a html select box filled with the current users accessible sites.<p>
     * 
     * @param htmlAttributes attributes that will be inserted into the generated html 
     * @return a html select box filled with the current users accessible sites
     */
    public String getSiteSelect(String htmlAttributes) {

        List options = new ArrayList();
        List values = new ArrayList();    
        int selectedIndex = 0;                   

        List sites = CmsSiteManager.getAvailableSites(getCms(), true);
 
        Iterator i = sites.iterator();
        int pos = 0;
        while (i.hasNext()) {
            CmsSite site = (CmsSite)i.next();
            values.add(site.getSiteRoot());
            options.add(site.getTitle());
            if (site.getSiteRoot().equals(getSettings().getSite())) { 
                // this is the user's current site
                selectedIndex = pos;
            }
            pos++;
        }
        
        return buildSelect(htmlAttributes, options, values, selectedIndex);    
    }    
    
    /**
     * Returns a html select box filled with the current users accessible projects.<p>
     * 
     * @param htmlAttributes attributes that will be inserted into the generated html 
     * @param htmlWidth additional "width" html attributes
     * @return a html select box filled with the current users accessible projects
     */
    public String getProjectSelect(String htmlAttributes, String htmlWidth) {
        // get all project information
        List allProjects;
        try {
            allProjects = getCms().getAllAccessibleProjects();
        } catch (CmsException e) {
            // should usually never happen
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }            
            allProjects = Collections.EMPTY_LIST;
        }

        List options = new ArrayList();
        List values = new ArrayList();
        int selectedIndex = 0;        
        int maxNameLength = 0;
        
        // now loop through all projects and fill the result vectors
        for (int i = 0, n = allProjects.size(); i < n; i++) {
            CmsProject loopProject = (CmsProject)allProjects.get(i);
            String loopProjectName = loopProject.getName();
            String loopProjectId = Integer.toString(loopProject.getId());
            
            values.add(loopProjectId);
            options.add(loopProjectName);
            
            if (loopProject.getId() == getSettings().getProject()) {
                // this is the user's current project
                selectedIndex = i;
            }            
            // check the length of the project name, to optionallly adjust the size of the selector
            maxNameLength = Math.max(loopProjectName.length(), maxNameLength);
        }        
        if (maxNameLength <= 20) {
            StringBuffer buf = new StringBuffer(htmlAttributes.length() + htmlWidth.length() + 5);
            buf.append(htmlAttributes);
            buf.append(" ");
            buf.append(htmlWidth);
            htmlAttributes = buf.toString();
        }
         
        return buildSelect(htmlAttributes, options, values, selectedIndex);
    }    
        
    /**
     * Returns a html select box filled with the views accessible by the current user.<p>
     * 
     * @param htmlAttributes attributes that will be inserted into the generated html 
     * @return a html select box filled with the views accessible by the current user
     */    
    public String getViewSelect(String htmlAttributes) {
        
        List options = new ArrayList();
        List values = new ArrayList();
        int selectedIndex = 0;                
        
        // loop through the vectors and fill the result vectors
        Iterator i = OpenCms.getWorkplaceManager().getViews().iterator();    
        int count = -1;
        while (i.hasNext()) {
            count++;
            CmsWorkplaceView view = (CmsWorkplaceView)i.next();
            String viewKey = view.getKey();
            String viewUri = view.getUri();
            
            boolean visible = true;
            try {
                getCms().readResource(viewUri);
            } catch (CmsException e) {
                // can usually be ignored
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(e);
                }                
                visible = false;
            }
            if (visible) {
                String loopLink = getJsp().link(viewUri);
                String localizedKey = key(viewKey, viewKey);
                options.add(localizedKey);
                values.add(loopLink);

                if (loopLink.equals(getSettings().getViewUri())) {
                    selectedIndex = count;
                }
            }
        }        
  
        return buildSelect(htmlAttributes, options, values, selectedIndex);        
    }
    
    /**
     * Returns a html select box filled with groups of the current user.<p>
     * 
     * @param htmlAttributes attributes that will be inserted into the generated html 
     * @return a html select box filled with groups of the current user
     */    
    public String getGroupSelect(String htmlAttributes) {    

        // get the users groups from the request context
        List allGroups = new Vector();
        try {
            allGroups = getCms().getGroupsOfUser(getSettings().getUser().getName());
        } catch (CmsException e) {
            // should usually never happen
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }
        }

        List options = new ArrayList();
        List values = new ArrayList();        
        
        // loop through all groups and build the result vectors
        int numGroups = allGroups.size();
        for (int i = 0; i<numGroups; i++) {
            CmsGroup loopGroup = (CmsGroup)allGroups.get(i);
            String loopGroupName = loopGroup.getName();
            values.add(loopGroupName);
            options.add(loopGroupName);
        }

        return buildSelect(htmlAttributes, options, values, 0);                             
    }
    
    /**
     * Returns the last login time of the current user in localized format.<p>
     *
     * @return the last login time of the current user in localized format
     */
    public String getLoginTime() {
        return getSettings().getMessages().getDateTime(getSettings().getUser().getLastlogin());
    }

    /**
     * Returns the remote ip address of the current user.<p>
     * 
     * @return the remote ip address of the current user
     */
    public String getLoginAddress() {
        return getCms().getRequestContext().getRemoteAddress();
    }
    
    /**
     * Returns true if the user has publish permissions for the current project.<p>
     * 
     * @return true if the user has publish permissions for the current project
     */
    public boolean isPublishEnabled() {
        
        return ((getCms().isAdmin() || getCms().isManagerOfProject())  
            && (!getCms().getRequestContext().currentProject().isOnlineProject()));   
    }
    
    /**
     * Returns true if the user has enabled synchronization.<p>
     * 
     * @return true if the user has enabled synchronization
     */
    public boolean isSyncEnabled() {
        return OpenCms.getSystemInfo().getSynchronizeSettings().isSyncEnabled();
    }

    /**
     * Returns true if the online help for the users current language is installed.<p>
     * 
     * @return true if the online help for the users current language is installed
     */    
    public boolean isHelpEnabled() {
        try {
            getCms().readFolder(I_CmsWpConstants.C_VFS_PATH_HELP + getLocale() + "/", CmsResourceFilter.IGNORE_EXPIRATION);
            return true;
        } catch (CmsException e) {          
            return false;
        }        
    }
    
    /**
     * Indicates if the site selector should be shown in the top frame depending on the count of accessible sites.<p>
     * 
     * @return true if site selector should be shown, otherwise false
     */
    public boolean showSiteSelector() {
        if (getSettings().getUserSettings().getRestrictExplorerView()) {
            // restricted explorer view to site and folder, do not show site selector
            return false;    
        }
        // count available sites
        int siteCount = CmsSiteManager.getAvailableSites(getCms(), true).size();
        return (siteCount > 1);    
    }
    
}
