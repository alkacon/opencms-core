/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsDefaultUsers.java,v $
 * Date   : $Date: 2004/07/07 18:01:09 $
 * Version: $Revision: 1.19 $
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
 
package org.opencms.db;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsCore;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Provides access to the names of the OpenCms default users and groups.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.19 $ $Date: 2004/07/07 18:01:09 $
 * @since 5.1.5
 */
public class CmsDefaultUsers {

    /** Default name for the "Admin" user. */
    public static final String C_DEFAULT_USER_ADMIN = "Admin";
    
    /** Default name for the "Export" user. */
    public static final String C_DEFAULT_USER_EXPORT = "Export";
    
    /** Default name for the "Guest" user. */
    public static final String C_DEFAULT_USER_GUEST = "Guest";
    
    /** Default name for the "Administrators" group. */
    public static final String C_DEFAULT_GROUP_ADMINISTRATORS = "Administrators";

    /** Default name for the "Projectmanagers" group. */
    public static final String C_DEFAULT_GROUP_PROJECTMANAGERS = "Projectmanagers";

    /** Default name for the "Guests" group. */
    public static final String C_DEFAULT_GROUP_GUESTS = "Guests";
    
    /** Default name for the "Users" group. */
    public static final String C_DEFAULT_GROUP_USERS = "Users";
    
    // member variables    
    private String m_groupAdministrators;
    private String m_groupGuests;
    private String m_groupProjectmanagers;    
    private String m_groupUsers;
    private String m_userAdmin;
    private String m_userExport;
    private String m_userGuest;
    
    /**
     * Constructor that initializes all names with default values.<p>
     * 
     * See the constants of this class for the defaule values that are uses.<p> 
     */
    public CmsDefaultUsers() {
        m_userAdmin = C_DEFAULT_USER_ADMIN;
        m_userGuest = C_DEFAULT_USER_GUEST;
        m_userExport = C_DEFAULT_USER_EXPORT;        
        m_groupAdministrators = C_DEFAULT_GROUP_ADMINISTRATORS;
        m_groupProjectmanagers = C_DEFAULT_GROUP_PROJECTMANAGERS;
        m_groupUsers = C_DEFAULT_GROUP_USERS;
        m_groupGuests = C_DEFAULT_GROUP_GUESTS;
    } 
    
    /**
     * Public constructor with name array.<p>
     * 
     * The order of names in the array must be:<ol>
     * <li>Name of the default admin user
     * <li>Name of the guest user
     * <li>Name of the export user
     * <li>Name of the administrators group
     * <li>Name of the project managers group
     * <li>Name of the users group
     * <li>Name of the guests group</ol>
     * 
     * @param names the name array
     */
    public CmsDefaultUsers(String[] names) {
        if ((names == null) || (names.length != 7)) {
            throw new RuntimeException("CmsDefaultUsers(): Exactly 7 user / group names are required");
        }
        m_userAdmin = names[0].trim();
        m_userGuest = names[1].trim();
        m_userExport = names[2].trim();
        m_groupAdministrators = names[3].trim();
        m_groupProjectmanagers = names[4].trim();
        m_groupUsers = names[5].trim();
        m_groupGuests = names[6].trim();
    } 

    /**
     * Initializes the default user configuration with the OpenCms system configuration.<p>
     * 
     * @param configuration the OpenCms configuration
     * @return the initialized default user configuration 
     * @throws Exception if something goes wrong
     */
    public static CmsDefaultUsers initialize(ExtendedProperties configuration) throws Exception {
        CmsDefaultUsers defaultUsers = null;
        // Read the default user configuration
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {            
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Default user names   : checking...");
        }        
        try {
            String[] defaultUserArray = configuration.getStringArray("db.default.users");
            defaultUsers = new CmsDefaultUsers(defaultUserArray);        
        } catch (Exception e) {
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isFatalEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).fatal(OpenCmsCore.C_MSG_CRITICAL_ERROR + "6", e);
            }
            throw e;
        }
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {            
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Admin user           : " + defaultUsers.getUserAdmin());
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Guest user           : " + defaultUsers.getUserGuest());
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export user          : " + defaultUsers.getUserExport());
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Administrators group : " + defaultUsers.getGroupAdministrators());
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Projectmanagers group: " + defaultUsers.getGroupProjectmanagers());
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Users group          : " + defaultUsers.getGroupUsers());
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Guests group         : " + defaultUsers.getGroupGuests());
        }        
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {            
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Default user names   : initialized");
        }          
        
        return defaultUsers;         
    }    
  
    /**
     * Returns the name of the administrators group.<p>
     * 
     * @return the name of the administrators group
     */ 
    public String getGroupAdministrators() {
        return m_groupAdministrators;
    }

    /**
     * Returns the name of the guests group.<p>
     * 
     * @return the name of the guests group
     */
    public String getGroupGuests() {
        return m_groupGuests;
    }

    /**
     * Returns the name of the project managers group.<p>
     * 
     * @return the name of the project managers group
     */
    public String getGroupProjectmanagers() {
        return m_groupProjectmanagers;
    }

    /**
     * Returns the name of the users group.<p>
     * 
     * @return the name of the users group
     */
    public String getGroupUsers() {
        return m_groupUsers;
    }

    /**
     * Returns the name of the default administrator user.<p>
     * 
     * @return the name of the default administrator user
     */
    public String getUserAdmin() {
        return m_userAdmin;
    }
      
    /**
     * Returns the name of the user used to generate the static export.<p>
     * 
     * @return the name of the user used to generate the static export
     */
    public String getUserExport() {
        return m_userExport;
    }

    /**
     * Returns the name of the default guest user.<p>
     * 
     * @return the name of the default guest user
     */
    public String getUserGuest() {
        return m_userGuest;
    }
    
    /**
     * Checks if a given group name is the name of one of the OpenCms default groups.<p>
     *
     * @param groupName the group name to check
     * @return <code>true</code> if group name is one of OpenCms default groups, <code>false</code> if it is not
     * or if <code>groupName</code> is <code>null</code> or an empty string (no trim)
     * 
     * @see #getGroupAdministrators()
     * @see #getGroupProjectmanagers()
     * @see #getGroupUsers()
     * @see #getGroupGuests()
     */
    public boolean isDefaultGroup(String groupName) {
        if ((groupName == null) || (groupName.length() == 0)) {
            return false;
        }

        return m_groupAdministrators.equals(groupName) 
            || m_groupProjectmanagers.equals(groupName) 
            || m_groupUsers.equals(groupName) 
            || m_groupGuests.equals(groupName);
    }
}
