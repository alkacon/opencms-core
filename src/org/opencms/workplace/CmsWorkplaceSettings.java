/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplaceSettings.java,v $
 * Date   : $Date: 2004/02/04 15:48:16 $
 * Version: $Revision: 1.30 $
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

import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsUser;

import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsUserSettings;
import org.opencms.main.OpenCms;

import java.util.HashMap;
import java.util.Map;

/**
 * Object to conveniently access and modify the state of the workplace for a user,
 * will be stored in the session of a user.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.30 $
 * @since 5.1
 */
public class CmsWorkplaceSettings {

    private String m_currentSite;
    private String m_explorerFlaturl;
    private String m_explorerMode;

    private int m_explorerPage;
    private String m_explorerProjectFilter;
    private int m_explorerProjectId;
    private String m_explorerResource;
    private boolean m_explorerShowLinks;

    private String m_language;
    private CmsWorkplaceMessages m_messages;
    private String m_permissionDetailView;
    private int m_project;

    private CmsPublishList m_publishList;
    private Map m_resourceTypes;
    private Map m_treeSite;
    private Map m_treeType;
    private CmsUser m_user;
    private CmsUserSettings m_userSettings;
    private String m_viewUri;

    /**
     * Constructor, only package visible.<p>
     */
    CmsWorkplaceSettings() {
        m_explorerPage = 1;
        m_treeType = new HashMap();
        m_treeSite = new HashMap();
        m_resourceTypes = new HashMap();
        m_currentSite = OpenCms.getSiteManager().getDefaultSite().getSiteRoot();
    }

    /**
     * Returns the explorer flat url.<p>
     *  
     * @return the explorer flat url
     */
    public String getExplorerFlaturl() {
        return m_explorerFlaturl;
    }

    /**
     * Returns the current explorer mode.<p> 
     * 
     * @return the current explorer mode
     */
    public String getExplorerMode() {
        return m_explorerMode;
    }

    /**
     * Returns the currently selected page in the explorer view.<p>
     * 
     * @return the currently selected page in the explorer view
     */
    public int getExplorerPage() {
        return m_explorerPage;
    }

    /**
     * Gets the explorer project filter for the project view.<p>
     * 
     * This parameter is used in the administration to filter
     * files belonging to a project.
     * 
     * @return the explorer project filter
     */
    public String getExplorerProjectFilter() {
        return m_explorerProjectFilter;
    }

    /**
     * Gets the explorer project id for the project view.<p>
     * 
     * This parameter is used in the administration to filter
     * files belonging to a selected project.
     * 
     * @return the explorer project id
     */
    public int getExplorerProjectId() {
        return m_explorerProjectId;
    }

    /**
     * Returns the current resource to be displayed in the explorer.<p>
     * 
     * @return the current resource to be displayed in the explorer
     */
    public String getExplorerResource() {
        return m_explorerResource;
    }

    /**
     * Returns if the explorer should show VFS links of a resource.<p>
     * 
     * @return true, if VFS links should be shown, otherwise false
     */
    public boolean getExplorerShowLinks() {
        return m_explorerShowLinks;
    }

    /**
     * Returns the currently selected user language.<p>
     * 
     * @return the currently selected user language
     */
    public String getLanguage() {
        return m_language;
    }

    /**
     * Returns the initialized workplace messages for the current user.<p>
     * 
     * @return the initialized workplace messages for the current user
     */
    public CmsWorkplaceMessages getMessages() {
        return m_messages;
    }

    /**
     * Returns the current detail grade of the view.<p>
     *  
     * @return value of the details.
     */
    public String getPermissionDetailView() {
        return m_permissionDetailView;
    }

    /**
     * Returns the currently selected project of the workplace user.<p> 
     * 
     * @return the currently selected project of the workplace user
     */
    public int getProject() {
        return m_project;
    }

    /**
     * Returns the publish list.<p>
     * 
     * @return the publishList
     */
    public CmsPublishList getPublishList() {
        return m_publishList;
    }

    /**
     * Returns a Map with all visible resource types for the current user, with the IDs as key values.<p>
     * 
     * @return Map with all visible resource types
     */
    public Map getResourceTypes() {
        return m_resourceTypes;
    }

    /**
     * Returns the current site for the user.<p>
     * 
     * @return the current site for the user 
     */
    public String getSite() {
        return m_currentSite;
    }

    /**
     * Returns the tree resource uri for the specified tree type
     * 
     * @param type the type of the tree
     * @return the tree resource uri for the specified tree type
     */
    public String getTreeResource(String type) {
        String result = (String) m_treeType.get(type);
        if (result == null) {
            result = "/";
        }
        return result;
    }

    /**
     * Returns the tree resource uri for the specified tree type
     * 
     * @param type the type of the tree
     * @return the tree resource uri for the specified tree type
     */
    public String getTreeSite(String type) {
        String result = (String) m_treeSite.get(type);
        return result;
    }

    /**
     * Returns the current workplace user.<p>
     * 
     * @return the current workplace user
     */
    public CmsUser getUser() {
        return m_user;
    }
    
    /**
     * Returns the current workplace user settings object.<p>
     * 
     * @return the current workplace user settings object
     */
    public CmsUserSettings getUserSettings() {
        return m_userSettings;
    }

    /**
     * Returns the current view Uri selected in the workplace.<p>
     * 
     * @return the current view Uri selected in the workplace 
     */
    public String getViewUri() {
        return m_viewUri;
    }

    /**
     * Checks if the current view is the administration view.<p>
     * 
     * @return true if the current view is the administration view, otherwise false 
     */
    public boolean isViewAdministration() {
        return (getViewUri().endsWith("/system/workplace/action/administration.html") || getViewUri().endsWith("/system/workplace/action/tasks.html"));
    }

    /**
     * Checks if the current view is the explorer view.<p>
     * 
     * @return true if the current view is the explorer view, otherwise false 
     */
    public boolean isViewExplorer() {
        return getViewUri().endsWith("/system/workplace/jsp/explorer_fs.html");
    }

    /**
     * Sets the explorer flat url.<p>
     * 
     * @param value the explorer flat url
     */
    public synchronized void setExplorerFlaturl(String value) {
        m_explorerFlaturl = value;
    }

    /**
     * Sets the current explorer mode.<p>
     * 
     * @param value the current explorer mode
     */
    public synchronized void setExplorerMode(String value) {
        m_explorerMode = value;
    }

    /**
     * Sets the currently selected page in the explorer view.<p>
     * 
     * @param page the currently selected page in the explorer view
     */
    public synchronized void setExplorerPage(int page) {
        m_explorerPage = page;
    }

    /**
     * Sets the explorer project filter for the project view.<p>
     * 
     * @param value the explorer project filter
     */
    public synchronized void setExplorerProjectFilter(String value) {
        m_explorerProjectFilter = value;
    }

    /**
     * Sets the explorer project id for the project view.<p>
     * 
     * @param value the explorer project id
     */
    public synchronized void setExplorerProjectId(int value) {
        m_explorerProjectId = value;
    }

    /**
     * Sets the current resource to be displayed in the explorer.<p>
     * 
     * @param value the current resource to be displayed in the explorer
     */
    public synchronized void setExplorerResource(String value) {
        if (value == null) {
            return;
        }
        if (value.startsWith(I_CmsConstants.VFS_FOLDER_SYSTEM + "/") && (!value.startsWith(m_currentSite)) && (!"galleryview".equals(getExplorerMode()))) {
            // restrict access to /system/ 
            m_explorerResource = "/";
        } else {
            m_explorerResource = value;
        }
    }

    /**
     * Sets the explorer view to show VFS links of a resource.<p>
     * 
     * @param b true, if VFS links should be shown, otherwise false
     */
    public synchronized void setExplorerShowLinks(boolean b) {
        m_explorerShowLinks = b;
    }

    /**
     * Sets the selected user language.<p>
     * 
     * @param value the selected user language
     */
    public synchronized void setLanguage(String value) {
        m_language = value;
    }

    /**
     * Sets the workplace messages for the current user.<p>
     * 
     * @param messages the workplace messages for the current user
     */
    public synchronized void setMessages(CmsWorkplaceMessages messages) {
        m_messages = messages;
    }

    /**
     * Sets the current detail grade of the view.<p>
     * 
     * @param value the current details.
     */
    public synchronized void setPermissionDetailView(String value) {
        m_permissionDetailView = value;
    }

    /**
     * Sets the currently selected project of the workplace user.<p>
     * 
     * @param project the currently selected project of thw workplace user
     */
    public synchronized void setProject(int project) {
        m_project = project;
    }

    /**
     * Sets the publish list.<p>
     * 
     * @param publishList the publishList to set
     */
    public synchronized void setPublishList(CmsPublishList publishList) {
        m_publishList = publishList;
    }

    /**
     * Sets all visible resource types for the current user, with the IDs as key values.<p>
     * 
     * @param value Map with all visible resource types
     */
    public synchronized void setResourceTypes(Map value) {
        m_resourceTypes = value;
    }

    /**
     * Sets the current site for the user.<p>
     * 
     * @param value the current site for the user
     */
    public synchronized void setSite(String value) {
        if ((value != null) && !value.equals(m_currentSite)) {
            m_currentSite = value;
            m_treeType = new HashMap();
        }
    }

    /**
     * Sets the tree resource uri for the specified tree type.<p>
     * 
     * @param type the type of the tree
     * @param value the resource uri to set for the type
     */
    public synchronized void setTreeResource(String type, String value) {
        if (value == null) {
            return;
        }
        if (value.startsWith(I_CmsConstants.VFS_FOLDER_SYSTEM + "/") && (!value.startsWith(m_currentSite))) {
            // restrict access to /system/ 
            value = "/";
        }
        m_treeType.put(type, value);
    }

    /**
     * Sets the tree resource uri for the specified tree type.<p>
     * 
     * @param type the type of the tree
     * @param value the resource uri to set for the type
     */
    public synchronized void setTreeSite(String type, String value) {
        if (value == null) {
            return;
        }
        m_treeSite.put(type, value);
    }

    /**
     * Sets the current workplace user.<p>
     * 
     * @param user the current workplace user
     */
    public synchronized void setUser(CmsUser user) {
        m_user = user;
    }
    
    /**
     * Sets the current workplace user settings object.<p>
     * 
     * @param userSettings the current workplace user settings object
     */
    public synchronized void setUserSettings(CmsUserSettings userSettings) {
        m_userSettings = userSettings;
    }

    /**
     * Sets the view Uri for the workplace.<p>
     * 
     * @param string the view Uri for the workplace
     */
    public synchronized void setViewUri(String string) {
        m_viewUri = string;
    }

}
