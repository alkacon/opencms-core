/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsDefaultUsers.java,v $
 * Date   : $Date: 2003/09/01 10:24:01 $
 * Version: $Revision: 1.5 $
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

import org.opencms.main.OpenCms;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.I_CmsConstants;

import java.util.HashMap;
import java.util.Map;

import source.org.apache.java.util.Configurations;

/**
 * Provides access to the names of the OpenCms default users and groups.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.5 $ $Date: 2003/09/01 10:24:01 $
 * @since 5.1.5
 */
public class CmsDefaultUsers {
    
    /** Matching group names for group name translation */
    private Map m_groupTranslations;

    // member variables    
    private String m_groupAdministrators;
    private String m_groupGuests;
    private String m_groupProjectmanagers;
    private String m_groupUsers;
    private String m_userAdmin;
    private String m_userGuest;

    /** Matching user names for user name translation */  
    private Map m_userTranslations;
      
    /**
     * Public constructor with individual names.<p>
     * 
     * @param userAdmin name of the default admin user
     * @param userGuest name of the guest user
     * @param groupAdministrators name of the adminsitrators group 
     * @param groupProjectmanagers name of the project managers group
     * @param groupUsers name of the users group
     * @param groupGuests name of the guest group
     */
    public CmsDefaultUsers(
        String userAdmin,
        String userGuest,
        String groupAdministrators,
        String groupProjectmanagers,
        String groupUsers,
        String groupGuests
    ) {
        m_userAdmin = userAdmin.trim();
        m_userGuest = userGuest.trim();
        m_groupAdministrators = groupAdministrators.trim(); 
        m_groupProjectmanagers = groupProjectmanagers.trim();
        m_groupUsers = groupUsers.trim();
        m_groupGuests = groupGuests.trim();
    }
    
    /**
     * Public constructor with name array.<p>
     * 
     * The order of names in the array must be:<ol>
     * <li>Name of the default admin user
     * <li>Name of the guest user
     * <li>Name of the administrators group
     * <li>Name of the project managers group
     * <li>Name of the users group
     * <li>Name of the guests group</ol>
     * 
     * @param names the name array
     */
    public CmsDefaultUsers(String[] names) {
        if ((names == null) || (names.length != 6)) {
            throw new RuntimeException("CmsDefaultUsers(): Exactly 6 user / group names are required");
        }
        m_userAdmin = names[0].trim();
        m_userGuest = names[1].trim();
        m_groupAdministrators = names[2].trim();
        m_groupProjectmanagers = names[3].trim();
        m_groupUsers = names[4].trim();
        m_groupGuests = names[5].trim();
    } 

    /**
     * Initializes the default user configuration with the OpenCms system configuration.<p>
     * 
     * @param conf the OpenCms configuration
     * @return the initialized default user configuration 
     * @throws Exception if something goes wrong
     */
    public static CmsDefaultUsers initialize(Configurations conf) throws Exception {
        CmsDefaultUsers defaultUsers = null;
        // Read the default user configuration
        if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {            
            OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Default user names   : checking...");
        }        
        try {
            String[] defaultUserArray = conf.getStringArray("db.default.users");
            defaultUsers = new CmsDefaultUsers(defaultUserArray);        
        } catch (Exception e) {
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, ". Critical init error/6: " + e.getMessage());
            }
            throw e;
        }
        if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {            
            OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Admin user           : " + defaultUsers.getUserAdmin());
            OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Guest user           : " + defaultUsers.getUserGuest());
            OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Administrators group : " + defaultUsers.getGroupAdministrators());
            OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Projectmanagers group: " + defaultUsers.getGroupProjectmanagers());
            OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Users group          : " + defaultUsers.getGroupUsers());
            OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Guests group         : " + defaultUsers.getGroupGuests());
        } 
        try {
            String[] translationArray = conf.getStringArray("import.name.translations");
            defaultUsers.setNameTranslations(translationArray);  
        } catch (Exception e) {
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Name translation     : non-critical error " + e.getMessage());
            }
        }        
        if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {            
            OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Default user names   : initialized");
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
     * Returns the name of the default guest user.<p>
     * 
     * @return the name of the default guest user
     */
    public String getUserGuest() {
        return m_userGuest;
    }
        
    /**
     * Initilaizes the user / group name translations.<p>
     * 
     * This feature is used when importing data from a system where the
     * user name configuration for the default (and other) users
     * was different from the running system. 
     * 
     * The format of the entrys in the array must look like this:<br>
     * <code>
     * GROUP.Users=ocusers<br>
     * USER.Admin=ocadmin<br>
     * </code>
     * In this example, the default name for the users group "Users" is
     * translated to "ocusers", and the default name for the admin user "Admin"
     * is translated to "ocadmin".<p>
     * 
     * @param translations array of name translations
     */
    private void setNameTranslations(String[] translations) {
        if ((translations == null) || (translations.length == 0)) return;
        for (int i=0; i<translations.length; i++) {
            String match = translations[i];
            if (match == null) continue;
            boolean valid = true;
            match = match.trim();
            int pos = match.indexOf(':');
            if (pos <= 0) {
                valid = false;
            }
            String ucmatch = match.toUpperCase();
            if (valid && ucmatch.startsWith(I_CmsConstants.C_EXPORT_ACEPRINCIPAL_GROUP)) {
                String name1 = match.substring(I_CmsConstants.C_EXPORT_ACEPRINCIPAL_GROUP.length(), pos);
                String name2 = match.substring(pos+1);      
                if (m_groupTranslations == null) {
                    m_groupTranslations = new HashMap();
                } 
                m_groupTranslations.put(name1, name2);  
                if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
                    OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Name translation     : group " + name1 + " to " + name2);
                }                
            } else if (valid && ucmatch.startsWith(I_CmsConstants.C_EXPORT_ACEPRINCIPAL_USER)) {
                String name1 = match.substring(I_CmsConstants.C_EXPORT_ACEPRINCIPAL_USER.length(), pos);
                String name2 = match.substring(pos+1);      
                if (m_userTranslations == null) {
                    m_userTranslations = new HashMap();
                } 
                m_userTranslations.put(name1, name2);      
                if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
                    OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Name translation     : user " + name1 + " to " + name2);
                }                             
            } else {
                if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
                    OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Name translation     : ignoring invalid entry '" + match + "'");
                }                 
            }
        }         
    }
    
    /**
     * Returns the translated name for the given group name.<p>
     * 
     * If no matching name is found, the given group name is returned.<p>
     * 
     * @param name the group name to translate
     * @return the translated name for the given group name
     */
    public String translateGroup(String name) {
        if (m_groupTranslations == null) return name;
        String match = (String)m_groupTranslations.get(name);
        if (match != null) {
            return match;
        } else {
            return name;
        }
    }    
    
    /**
     * Returns the translated name for the given user name.<p>
     * 
     * If no matching name is found, the given user name is returned.<p>
     * 
     * @param name the user name to translate
     * @return the translated name for the given user name
     */
    public String translateUser(String name) {
        if (m_userTranslations == null) return name;
        String match = (String)m_userTranslations.get(name);
        if (match != null) {
            return match;
        } else {
            return name;
        }
    }
}
