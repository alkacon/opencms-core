/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/Attic/A_CmsContentDefinition.java,v $
* Date   : $Date: 2003/04/01 13:29:17 $
* Version: $Revision: 1.13 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.defaults;

import com.opencms.template.*;
import com.opencms.file.*;

import java.util.*;
import java.lang.reflect.*;
import com.opencms.core.*;
import com.opencms.core.exceptions.*;

/**
 * Abstract class for the content definition
 * Creation date: (27.10.00 10:04:42)
 * 
 * @author Michael Knoll
 * @version $Revision: 1.13 $
 */
public abstract class A_CmsContentDefinition implements I_CmsContent, I_CmsConstants {

/**
 * The owner  of this resource.
 */
 private int m_user;

/**
 * The group  of this resource.
 */
 private String m_group;

/**
 * The access flags of this resource.
 */
 private int m_accessFlags;


/**
 * applies the filter method
 * @return an Vector containing the method
 */
public static Vector applyFilter(CmsObject cms, CmsFilterMethod filterMethod) throws Exception {
    return applyFilter(cms, filterMethod, null);
}

/**
 * applies the filter through the method object and the user parameters
 * @return a vector with the filtered content
 */
public static Vector applyFilter(CmsObject cms, CmsFilterMethod filterMethod, String userParameter) throws Exception {
    Method method = filterMethod.getFilterMethod();
    Object[] defaultParams = filterMethod.getDefaultParameter();
    Vector allParameters = new Vector();
    Object[] allParametersArray;
    Class[] paramTypes = method.getParameterTypes();

    if( (paramTypes.length > 0) && (paramTypes[0] == CmsObject.class) ) {
        allParameters.addElement(cms);
    }

    for(int i = 0; i < defaultParams.length; i++) {
        allParameters.addElement(defaultParams[i]);
    }

    if (filterMethod.hasUserParameter()) {
        allParameters.addElement(userParameter);
    }

    allParametersArray = new Object[allParameters.size()];
    allParameters.copyInto(allParametersArray);
    return (Vector) method.invoke(null, allParametersArray);
}


  public void check(boolean finalcheck) throws CmsPlausibilizationException {
    // do nothing here, just an empty method for compatibility reasons.
  }


/**
 * abstract delete method
 * for delete instance of content definition
 * must be overwritten in your content definition
 */
public abstract void delete(CmsObject cms) throws Exception;

/**
 * Gets the getXXX methods
 * You have to override this method in your content definition.
 * @return a Vector with the filed methods.
 */
public  static Vector getFieldMethods(CmsObject cms) {
    return new Vector();
}
/**
 * Gets the headlines of the table
 * You have to override this method in your content definition.
 * @return a Vector with the colum names.
 */
public static Vector getFieldNames(CmsObject cms) {
    return new Vector();
}
/**
 * Gets the filter methods.
 * You have to override this method in your content definition.
 * @return a Vector of FilterMethod objects containing the methods, names and default parameters
 */
public static Vector getFilterMethods(CmsObject cms) {
    return new Vector();
}
/**
 * Gets the lockstates
 * You have to override this method in your content definition, if you have overwritten
 * the isLockable method with true.
 * @return a int with the lockstate
 */
public int getLockstate() {
    return -1;
}
/**
 * gets the unique Id of a content definition instance
 * @return a string with the Id
 */
public abstract String getUniqueId(CmsObject cms) ;
/**
 * Gets the url of the field entry
 * You have to override this method in your content definition,
 * if you wish to link url´s to the field entries
 * @return a String with the url
 */
public String getUrl() {
    return null;
}
/**
 * if the content definition objects should be lockable
 * this method has to be overwritten with value true
 * @return a boolean
 */
public static boolean isLockable() {
    return false;
}
/**
 *Sets the lockstates
 * You have to override this method in your content definition,
 * if you have overwritten the isLockable method with true.
 * @param lockstate the lockstate for the actual entry
 */
public void setLockstate(int lockstate) {
}
/**
 * abstract write method
 * must be overwritten in content definition
 */
public abstract void write(CmsObject cms) throws Exception;

/**
 * returns true if the CD is readable for the current user
 * @return true
 */
public boolean isReadable() {
    return true;
}

/**
 * returns true if the CD is writeable for the current user
 * @return true
 */
public boolean isWriteable() {
    return true;
}

/**
 * set the owner of the CD
 * @param id of the owner
 */
public void setOwner(int userId) {
    m_user = userId;
}

/**
 * get the owner of the CD
 * @return id of the owner (int)
 */
public int getOwner() {
    return m_user;
}

/**
 * set the group of the CD
 * @param the group ID
 */
public void setGroup(String group) {
    m_group = group;
}

/**
 * get the group of the CD
 * @return the group ID
 */
public String getGroup() {
    return m_group;
}

/**
 * set the accessFlag for the CD
 * @param the accessFlag
 */
public void setAccessFlags(int accessFlags) {
    m_accessFlags = accessFlags;
}

/**
 * get the accessFlag for the CD
 * @return the accessFlag
 */
public int getAccessFlags() {
    return m_accessFlags;
}

/**
 * has the current user the right to read the CD
 * @return a boolean
 */
protected boolean hasReadAccess(CmsObject cms) throws CmsException {
    CmsUser currentUser = cms.getRequestContext().currentUser();

    if ( !accessOther(C_ACCESS_PUBLIC_READ)
        && !accessOwner(cms, currentUser, C_ACCESS_OWNER_READ)
        && !accessGroup(cms, currentUser, C_ACCESS_GROUP_READ)) {
        return false;
    }
    return true;
}

/**
 * has the current user the right to write the CD
 * @return a boolean
 */
protected boolean hasWriteAccess(CmsObject cms) throws CmsException {
    CmsUser currentUser = cms.getRequestContext().currentUser();
    // check, if the resource is locked by the current user

    if( isLockable() && (getLockstate() != currentUser.getId()) ) {
        // resource is not locked by the current user, no writing allowed
        return(false);
    }

    // check the rights for the current resource
    if( ! ( accessOther(C_ACCESS_PUBLIC_WRITE) ||
            accessOwner(cms, currentUser, C_ACCESS_OWNER_WRITE) ||
            accessGroup(cms, currentUser, C_ACCESS_GROUP_WRITE) ) ) {
        // no write access to this resource!
        return false;
    }
    return true;
}

/**
 * Checks, if the owner may access this resource.
 *
 * @param cms the cmsObject
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param flags The flags to check.
 *
 * @return wether the user has access, or not.
 */
protected boolean accessOwner(CmsObject cms, CmsUser currentUser,
                                int flags) throws CmsException {
    // The Admin has always access
    if( cms.isAdmin() ) {
        return(true);
    }
    // is the resource owned by this user?
    if(getOwner() == currentUser.getId()) {
        if( (getAccessFlags() & flags) == flags ) {
            return true ;
        }
    }
    // the resource isn't accesible by the user.
    return false;
}

/**
 * Checks, if the group may access this resource.
 *
 * @param cms the cmsObject
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param flags The flags to check.
 *
 * @return wether the user has access, or not.
 */
protected boolean accessGroup(CmsObject cms, CmsUser currentUser,
                              int flags) throws CmsException {
    // is the user in the group for the resource?
    if(cms.userInGroup(currentUser.getName(), getGroup() )) {
        if( (getAccessFlags() & flags) == flags ) {
            return true;
        }
    }
    // the resource isn't accesible by the user.
    return false;
}

/**
 * Checks, if others may access this resource.
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param flags The flags to check.
 *
 * @return wether the user has access, or not.
 */
protected boolean accessOther( int flags ) throws CmsException {
    if ((getAccessFlags() & flags) == flags) {
        return true;
    } else {
        return false;
    }
}

/**
 * if the content definition objects should be displayed
 * in an extended list with projectflags and state
 * this method must be overwritten with value true
 * @return a boolean
 */
public static boolean isExtendedList() {
    return false;
}

/**
 * if the content definition objects are timecritical
 * this method must be overwritten with value true.
 * @return a boolean
 */
public boolean isTimedContent() {
    return false;
}
}
