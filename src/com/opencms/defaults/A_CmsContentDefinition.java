/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/Attic/A_CmsContentDefinition.java,v $
 * Date   : $Date: 2004/10/07 16:00:11 $
 * Version: $Revision: 1.26 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsUUID;

import com.opencms.defaults.master.CmsPlausibilizationException;
import com.opencms.template.I_CmsContent;

import java.lang.reflect.Method;
import java.util.Vector;

/**
 * Abstract class for the content definition
 * Creation date: (27.10.00 10:04:42)
 * 
 * @author Michael Knoll
 * @version $Revision: 1.26 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public abstract class A_CmsContentDefinition implements I_CmsContent {

    /**
     * The owner  of this resource.
     */
    private CmsUUID m_userId;

    /**
     * The group  of this resource.
     */
    private String m_group;

    /**
     * The access flags of this resource.
     */
    private int m_accessFlags;

    /**
     * Applies the filter method.<p>
     * 
     * @param cms the cms object
     * @param filterMethod the filter method
     * @return an Vector containing the method
     * @throws Exception if something goes wrong
     */
    public static Vector applyFilter(CmsObject cms, CmsFilterMethod filterMethod) throws Exception {

        return applyFilter(cms, filterMethod, null);
    }

    /**
     * applies the filter through the method object and the user parameters.<p>
     * 
     * @param cms the cms object
     * @param filterMethod the filter method
     * @param userParameter additional filter parameter
     * @return a vector with the filtered content
     * @throws Exception if something goes wrong
     */
    public static Vector applyFilter(CmsObject cms, CmsFilterMethod filterMethod, String userParameter)
    throws Exception {

        Method method = filterMethod.getFilterMethod();
        Object[] defaultParams = filterMethod.getDefaultParameter();
        Vector allParameters = new Vector();
        Object[] allParametersArray;
        Class[] paramTypes = method.getParameterTypes();

        if ((paramTypes.length > 0) && (paramTypes[0] == CmsObject.class)) {
            allParameters.addElement(cms);
        }

        for (int i = 0; i < defaultParams.length; i++) {
            allParameters.addElement(defaultParams[i]);
        }

        if (filterMethod.hasUserParameter()) {
            allParameters.addElement(userParameter);
        }

        allParametersArray = new Object[allParameters.size()];
        allParameters.copyInto(allParametersArray);
        return (Vector)method.invoke(null, allParametersArray);
    }

    /**
     * Checks the current values of a content definition.<p>
     * 
     * @param finalcheck flag to indicate the check when leaving the dialogue
     * @throws CmsPlausibilizationException if a value check fails
     */
    public void check(boolean finalcheck) throws CmsPlausibilizationException {

        // do nothing here, just an empty method for compatibility reasons.
    }

    /**
     * abstract delete method
     * for delete instance of content definition
     * must be overwritten in your content definition
     * 
     * @param cms the cms object
     * @throws Exception if something goes wrong
     */
    public abstract void delete(CmsObject cms) throws Exception;

    /**
     * Gets the getXXX methods
     * You have to override this method in your content definition.<p>
     * 
     * @param cms the cms object
     * @return a Vector with the filed methods.
     */
    public static Vector getFieldMethods(CmsObject cms) {

        return new Vector();
    }

    /**
     * Gets the headlines of the table
     * You have to override this method in your content definition.<p>
     * 
     * @param cms the cms object
     * @return a Vector with the colum names.
     */
    public static Vector getFieldNames(CmsObject cms) {

        return new Vector();
    }

    /**
     * Gets the filter methods.
     * You have to override this method in your content definition.<p>
     * 
     * @param cms the cms object
     * @return a Vector of FilterMethod objects containing the methods, names and default parameters
     */
    public static Vector getFilterMethods(CmsObject cms) {

        return new Vector();
    }

    /**
     * Gets the lockstates
     * You have to override this method in your content definition, if you have overwritten
     * the isLockable method with true.<p>
     * 
     * @return a int with the lockstate
     */
    public CmsUUID getLockstate() {

        return CmsUUID.getNullUUID();
    }

    /**
     * gets the unique Id of a content definition instance.<p>
     * 
     * @param cms the cms object
     * @return a string with the Id
     */
    public abstract String getUniqueId(CmsObject cms);

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
     * Sets the lockstates
     * You have to override this method in your content definition,
     * if you have overwritten the isLockable method with true.<p>
     * 
     * @param lockedByUserId the id of the user who is locking the content
     */
    public void setLockstate(CmsUUID lockedByUserId) {

    }

    /**
     * abstract write method
     * must be overwritten in content definition.<p>
     * 
     * @param cms ths cms object
     * @throws Exception if something goes wrong
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
     * Set the owner of the CD.<p>
     * 
     * @param userId the id of the owner
     */
    public void setOwner(CmsUUID userId) {

        m_userId = userId;
    }

    /**
     * get the owner of the CD
     * @return id of the owner (int)
     */
    public CmsUUID getOwner() {

        return m_userId;
    }

    /**
     * set the group of the CD
     * @param group the group ID
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
     * @param accessFlags the accessFlag
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
     * has the current user the right to read the CD.<p>
     * 
     * @param cms the cms object
     * @return a boolean
     * @throws CmsException if something goes wrong
     */
    protected boolean hasReadAccess(CmsObject cms) throws CmsException {

        CmsUser currentUser = cms.getRequestContext().currentUser();

        if (!accessOther(I_CmsConstants.C_ACCESS_PUBLIC_READ)
            && !accessOwner(cms, currentUser, CmsPermissionSet.PERMISSION_READ)
            && !accessGroup(cms, currentUser, I_CmsConstants.C_ACCESS_GROUP_READ)) {
            return false;
        }
        return true;
    }

    /**
     * has the current user the right to write the CD.<p>
     * 
     * @param cms the cms object
     * @return a boolean
     * @throws CmsException if something goes wrong
     */
    public boolean hasWriteAccess(CmsObject cms) throws CmsException {

        CmsUser currentUser = cms.getRequestContext().currentUser();
        // check, if the resource is locked by the current user

        if (isLockable() && (!getLockstate().equals(currentUser.getId()))) {
            // resource is not locked by the current user, no writing allowed
            return false;
        }

        // check the rights for the current resource
        if (!(accessOther(I_CmsConstants.C_ACCESS_PUBLIC_WRITE)
            || accessOwner(cms, currentUser, CmsPermissionSet.PERMISSION_WRITE) || accessGroup(
            cms,
            currentUser,
            I_CmsConstants.C_ACCESS_GROUP_WRITE))) {
            // no write access to this resource!
            return false;
        }
        return true;
    }

    /**
     * Checks, if the owner may access this resource.<p>
     *
     * @param cms the cmsObject
     * @param currentUser The user who requested this method
     * @param flags The flags to check
     *
     * @return wether the user has access, or not.
     * @throws CmsException if something goes wrong
     */
    protected boolean accessOwner(CmsObject cms, CmsUser currentUser, int flags) throws CmsException {
        
        // is the resource owned by this user?
        if (currentUser.getId().equals(getOwner())) {
            if ((getAccessFlags() & flags) == flags) {
                return true;
            }
        }
        
        // the resource isn't accesible by the user.
        return false;
    }

    /**
     * Checks, if the group may access this resource.<p>
     *
     * @param cms the cmsObject
     * @param currentUser The user who requested this method
     * @param flags The flags to check
     *
     * @return wether the user has access, or not
     * @throws CmsException if something goes wrong
     */
    protected boolean accessGroup(CmsObject cms, CmsUser currentUser, int flags) throws CmsException {

        // is the user in the group for the resource?
        if (cms.userInGroup(currentUser.getName(), getGroup())) {
            if ((getAccessFlags() & flags) == flags) {
                return true;
            }
        }
        
        // the resource isn't accesible by the user.
        return false;
    }

    /**
     * Checks, if others may access this resource.<p>
     *
     * @param flags The flags to check
     * @return wether the user has access, or not.
     * @throws CmsException if something goes wrong
     */
    protected boolean accessOther(int flags) throws CmsException {

        return ((getAccessFlags() & flags) == flags);
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