/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsDefaultUsers.java,v $
 * Date   : $Date: 2003/08/07 18:47:27 $
 * Version: $Revision: 1.1 $
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

/**
 * Provides access to the names of the OpenCms default users and groups.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $ $Date: 2003/08/07 18:47:27 $
 * @since 5.1.5
 */
public class CmsDefaultUsers {

    // member variables    
    private String m_userAdmin;
    private String m_userGuest;
    private String m_groupAdministrators;
    private String m_groupProjectmanagers;
    private String m_groupUsers;
    private String m_groupGuests;
  
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

}
