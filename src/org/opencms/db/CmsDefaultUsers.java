/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsDefaultUsers.java,v $
 * Date   : $Date: 2004/11/05 18:15:11 $
 * Version: $Revision: 1.20 $
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
import org.opencms.util.CmsStringUtil;

/**
 * Provides access to the names of the OpenCms default users and groups.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Armen Markarian (a.markarian@alkacon.com)
 * 
 * @version $Revision: 1.20 $ $Date: 2004/11/05 18:15:11 $
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
     * Public constructor. <p>
     * 
     * @param userAdmin the name of the default admin user
     * @param userGuest the name of the guest user
     * @param userExport the name of the export user
     * @param groupAdministrators the name of the administrators group
     * @param groupProjectmanagers the name of the project managers group
     * @param groupUsers the name of the users group
     * @param groupGuests the name of the guests group
     */
    public CmsDefaultUsers(
        String userAdmin, 
        String userGuest,
        String userExport,
        String groupAdministrators,
        String groupProjectmanagers,
        String groupUsers,
        String groupGuests) {
        // check if all required user and group names are not null or empty
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {            
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Default user names   : checking...");
        }
        if (
            CmsStringUtil.isEmpty(userAdmin) 
         || CmsStringUtil.isEmpty(userGuest)
         || CmsStringUtil.isEmpty(userExport)
         || CmsStringUtil.isEmpty(groupAdministrators)
         || CmsStringUtil.isEmpty(groupProjectmanagers)
         || CmsStringUtil.isEmpty(groupUsers)
         || CmsStringUtil.isEmpty(groupGuests)) {
            throw new RuntimeException("CmsDefaultUsers(): Exactly 7 user / group names are required");
        }
        // set members
        m_userAdmin = userAdmin.trim();
        m_userGuest = userGuest.trim();
        m_userExport = userExport.trim();
        m_groupAdministrators = groupAdministrators.trim();
        m_groupProjectmanagers = groupProjectmanagers.trim();
        m_groupUsers = groupUsers.trim();
        m_groupGuests = groupGuests.trim();
        
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {            
          OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Admin user           : " + getUserAdmin());
          OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Guest user           : " + getUserGuest());
          OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Export user          : " + getUserExport());
          OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Administrators group : " + getGroupAdministrators());
          OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Projectmanagers group: " + getGroupProjectmanagers());
          OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Users group          : " + getGroupUsers());
          OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Guests group         : " + getGroupGuests());
      }        
      if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {            
          OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Default user names   : initialized");
      } 
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
