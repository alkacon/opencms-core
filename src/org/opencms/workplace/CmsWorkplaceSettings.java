/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplaceSettings.java,v $
 * Date   : $Date: 2003/07/30 11:56:16 $
 * Version: $Revision: 1.10 $
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


/**
 * Object to conveniently access and modify the state of the workplace for a user,
 * will be stored in the session of a user.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.10 $
 * 
 * @since 5.1
 */
public class CmsWorkplaceSettings {
    
    private String m_language;
    private CmsWorkplaceMessages m_messages;
    private int m_project;
    private String m_group;
    private CmsUser m_user; 
    private String m_currentView;
    private String m_explorerFolder;
    private String m_explorerMode;
    private int m_explorerPage = 1;
    private int m_explorerChecksum = -1;
    private String m_explorerFlaturl;
    private String m_permissionDetailView;
    private String m_currentSite = I_CmsConstants.C_VFS_DEFAULT;
        
    /**
     * Constructor, only package visible.<p>
     */
    CmsWorkplaceSettings() { }
    
    /**
     * Returns the currently selected user language.<p>
     * 
     * @return the currently selected user language
     */
    public synchronized String getLanguage() {
        return m_language;
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
     * Returns the initialized workplace messages for the current user.<p>
     * 
     * @return the initialized workplace messages for the current user
     */
    public CmsWorkplaceMessages getMessages() {
        return m_messages;
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
     * Returns the current view selected in the workplace.<p>
     * 
     * @return the current view selected in the workplace 
     */
    public synchronized String getCurrentView() {
        return m_currentView;
    }

    /**
     * Sets the view for the workplace.<p>
     * 
     * @param string the view for the workplace
     */
    public synchronized void setCurrentView(String string) {
        m_currentView = string;
    }
    

    /**
     * Returns the current site for the user.<p>
     * 
     * @return the current site for the user 
     */
    public synchronized String getSite() {
        return m_currentSite;
    }
    
    /**
     * Sets the current site for the user.<p>
     * 
     * @param value the current site for the user
     */
    public synchronized void setSite(String value) {
        m_currentSite = value;
    }

    /**
     * Returns the currently selected default group of the workplace user.<p>
     * 
     * @return the currently selected default group of the workplace user
     */
    public synchronized String getGroup() {
        return m_group;
    }

    /**
     * Sets the default group of the workplace user.<p>
     * 
     * @param group the default group of the workplace user
     */
    public synchronized void setGroup(String group) {
        m_group = group;
    }

    /**
     * Returns the currently selected project of the workplace user.<p> 
     * 
     * @return the currently selected project of the workplace user
     */
    public synchronized int getProject() {
        return m_project;
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
     * Returns the current workplace user.<p>
     * 
     * @return the current workplace user
     */
    public synchronized CmsUser getUser() {
        return m_user;
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
     * Returns the current resource to be displayed in the explorer.<p>
     * 
     * @return the current resource to be displayed in the explorer
     */
    public synchronized String getExplorerResource() {
        return m_explorerFolder;
    }
    
    /**
     * Sets the current resource to be displayed in the explorer.<p>
     * 
     * @param value the current resource to be displayed in the explorer
     */
    public synchronized void setExplorerResource(String value) {
        m_explorerFolder = value;
    }
    
    /**
     * Returns the current explorer mode.<p> 
     * 
     * @return the current explorer mode
     */
    public synchronized String getExplorerMode() {
        return m_explorerMode;
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
     * Returns the currently selected page in the explorer view.<p>
     * 
     * @return the currently selected page in the explorer view
     */
    public synchronized int getExplorerPage() {
        return m_explorerPage;
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
     * Returns the explorer checksum.<p>
     * 
     * @return the explorer checksum
     */
    public int getExplorerChecksum() {
        return m_explorerChecksum;
    }

    /**
     * Sets the explorer checksum.<p>
     * 
     * @param value the explorer checksum
     */
    public void setExplorerChecksum(int value) {
        m_explorerChecksum = value;
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
     * Sets the explorer flat url.<p>
     * 
     * @param value the explorer flat url
     */
    public void setExplorerFlaturl(String value) {
        m_explorerFlaturl = value;
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
     * Sets the current detail grade of the view.<p>
     * 
     * @param value the current details.
     */
    public void setPermissionDetailView(String value) {
        m_permissionDetailView = value;
    }
        
}
