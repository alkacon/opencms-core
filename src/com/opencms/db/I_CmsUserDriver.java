/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/db/Attic/I_CmsUserDriver.java,v $
 * Date   : $Date: 2003/05/23 16:26:46 $
 * Version: $Revision: 1.3 $
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
 
package com.opencms.db;

import com.opencms.core.CmsException;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsUser;
import com.opencms.flex.util.CmsUUID;

import java.util.Hashtable;
import java.util.Vector;

import source.org.apache.java.util.Configurations;

/**
 * Definitions of all required user driver methods.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.3 $ $Date: 2003/05/23 16:26:46 $
 * @since 5.1.2
 */
public interface I_CmsUserDriver {
    
    void destroy() throws Throwable, CmsException;
    CmsUser addImportUser(String name, String password, String recoveryPassword, String description, String firstname, String lastname, String email, long lastlogin, long lastused, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type) throws CmsException;
    CmsUser addUser(String name, String password, String description, String firstname, String lastname, String email, long lastlogin, long lastused, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type) throws CmsException;
    void addUserToGroup(CmsUUID userid, CmsUUID groupid) throws CmsException;
    void changeUserType(CmsUUID userId, int userType) throws CmsException;
    CmsUUID checkGroupDependence(CmsUUID groupId1, CmsUUID groupId2) throws CmsException;
    CmsUUID checkGroupDependence(Vector groups) throws CmsException;
    CmsGroup createGroup(String groupName, String description, int flags, String parentGroupName) throws CmsException;
    void deleteGroup(String delgroup) throws CmsException;
    void deleteUser(CmsUUID userId) throws CmsException;
    void deleteUser(String userName) throws CmsException;
    String digest(String value);
    Vector getChild(String groupname) throws CmsException;
    Vector getGroups() throws CmsException;
    Vector getGroupsOfUser(String name) throws CmsException;
    String getReadingpermittedGroup(int projectId, String resource) throws CmsException;
    Vector getUsers(int type) throws CmsException;
    Vector getUsers(int type, String namefilter) throws CmsException;
    Vector getUsersByLastname(String lastname, int userType, int userStatus, int wasLoggedIn, int nMax) throws CmsException;
    Vector getUsersOfGroup(String name, int type) throws CmsException;
    void init(Configurations config, String dbPoolUrl, CmsDriverManager driverManager);
    com.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl);
    boolean isUserInGroup(CmsUUID userId, CmsUUID groupId) throws CmsException;
    CmsGroup readGroup(CmsUUID groupId) throws CmsException;
    CmsGroup readGroup(String groupName) throws CmsException;
    CmsUser readUser(CmsUUID id) throws CmsException;
    CmsUser readUser(String name, int type) throws CmsException;
    CmsUser readUser(String name, String password, int type) throws CmsException;
    void recoverPassword(String userName, String recoveryPassword, String password) throws CmsException;
    void removeUserFromGroup(CmsUUID userId, CmsUUID groupId) throws CmsException;
    void setPassword(String userName, String password) throws CmsException;
    void setRecoveryPassword(String userName, String password) throws CmsException;
    void writeGroup(CmsGroup group) throws CmsException;
    void writeUser(CmsUser user) throws CmsException;
    
}