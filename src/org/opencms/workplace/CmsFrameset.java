/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsFrameset.java,v $
 * Date   : $Date: 2004/03/12 16:00:48 $
 * Version: $Revision: 1.43 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.file.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides methods for building the main framesets of the OpenCms Workplace.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/jsp/top.html
 * <li>/jsp/top_foot.html
 * <li>/jsp/top_head.html
 * </ul>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.43 $
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
        // get all resource types
        List resTypes = getCms().getAllResourceTypes();
        Iterator i = resTypes.iterator();
        while (i.hasNext()) {
            I_CmsResourceType resType = (I_CmsResourceType)i.next();
            int resTypeId = resType.getResourceType();
            // get explorer type settings for current resource type
            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getEplorerTypeSetting(resType.getResourceTypeName());
            if (settings != null) {
                // append the context menu of the current resource type 
                result.append(settings.getContextMenu().getJSEntries(getCms(), settings, resTypeId, getSettings().getMessages()));
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
        // get the broadcast message from the session
        String message = (String)getJsp().getRequest().getSession().getAttribute(I_CmsConstants.C_SESSION_BROADCASTMESSAGE);
        if (message != null) {
            // remove the message from session
            getJsp().getRequest().getSession().removeAttribute(I_CmsConstants.C_SESSION_BROADCASTMESSAGE);
            if (!"".equals(message.trim())) {
                // create a javascript alert for the message if message is not empty
                result.append("\n<script type=\"text/javascript\">\n<!--\n");
                // the timeout gives the frameset enough time to load before the alert is shown
                result.append("setTimeout(\"alert(unescape('" + key("label.messagetoall") + ": ");
                result.append(CmsEncoder.escape(message, getCms().getRequestContext().getEncoding()) + "'));\", 2000);");
                result.append("\n//-->\n</script>");
            }
        }
        return result.toString();
    }
    
    /**
     * Returns the file settings for the Workplace explorer view.<p>
     * 
     * @return the file settings for the Workplace explorer view
     */
    public int getExplorerSettings() {
        String explorerSettings = (String)getCms().getRequestContext().currentUser().getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_EXPLORERSETTINGS);
        if (explorerSettings != null) {
            return new Integer(explorerSettings).intValue();
        } else {
            return I_CmsWpConstants.C_FILELIST_NAME + I_CmsWpConstants.C_FILELIST_TITLE + I_CmsWpConstants.C_FILELIST_TYPE + I_CmsWpConstants.C_FILELIST_DATE_LASTMODIFIED;
        }
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
        Vector allProjects;
        try {
            allProjects = getCms().getAllAccessibleProjects();
        } catch (CmsException e) {
            // should not happen
            allProjects = new Vector();
        }

        List options = new ArrayList();
        List values = new ArrayList();
        int selectedIndex = 0;        
        int maxNameLength = 0;
        
        // now loop through all projects and fill the result vectors
        int numProjects = allProjects.size();
        for (int i = 0; i < numProjects; i++) {
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
                getCms().readFileHeader(viewUri);
            } catch (CmsException e) {
                visible = false;
            }
            if (visible) {
                String loopLink = getJsp().link(viewUri);
                
                options.add(key(viewKey));
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
        Vector allGroups = new Vector();
        try {
            allGroups = getCms().getGroupsOfUser(getSettings().getUser().getName());
        } catch (CmsException e) {
            // should not happen
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
        try {
            return ((getCms().isAdmin() || getCms().isManagerOfProject())  
                && (!getCms().getRequestContext().currentProject().isOnlineProject()));
        } catch (CmsException e) {
            return false;
        }        
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
            getCms().readFolder(I_CmsWpConstants.C_VFS_PATH_HELP + getLocale() + "/");
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
        int siteCount = CmsSiteManager.getAvailableSites(getCms(), true).size();
        return (siteCount > 1);    
    }
    
}
