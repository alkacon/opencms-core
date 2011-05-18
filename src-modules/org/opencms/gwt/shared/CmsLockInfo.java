/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/shared/Attic/CmsLockInfo.java,v $
 * Date   : $Date: 2011/05/18 13:25:57 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean for storing information about whether a resource could be locked or not, and if not, why.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsLockInfo implements IsSerializable {

    /** 
     * An enum indicating the success or type of failure of a locking operation.<p>
     */
    public enum State {
        /** The resource was already locked by another user. */
        locked,
        /** The resource has changed in the meantime. */
        changed,
        /** The resource was successfully locked. */
        success,
        /** Some other error occurred. */
        other;
    }

    /** The state indicating whether (and how) the locking operation succeeded or failed. */
    private State m_state;

    /** A user name. */
    private String m_user;

    /** An additional error message. */
    private String m_exceptionMessage;

    /** The name of the resource which we tried to lock. */
    private String m_resourceName;

    /**
     * Creates a new lock info bean.<p>
     * 
     * @param resourceName the name of the resource 
     * @param state the state of the locking operation 
     * @param user a user name 
     * @param exceptionMessage an additional error message 
     */
    public CmsLockInfo(String resourceName, State state, String user, String exceptionMessage) {

        m_resourceName = resourceName;
        m_state = state;
        m_user = user;
        m_exceptionMessage = exceptionMessage;
    }

    /**
     * Empty constructor for serialization.<p>
     */
    protected CmsLockInfo() {

        // do nothing
    }

    /**
     * Creates a new info bean for a resource which has changed since it was opened.<p>
     * 
     * @param resourceName the resource name 
     * @param user the user by which it was changed 
     * @return the new lock info bean 
     */
    public static CmsLockInfo forChangedResource(String resourceName, String user) {

        return new CmsLockInfo(resourceName, State.changed, user, null);
    }

    /**
     * Creates a new info bean for other types of errors.<p>
     * 
     * @param resourceName the resource name 
     * @param errorMessage the additional error message 
     * 
     * @return the new lock info bean 
     */
    public static CmsLockInfo forError(String resourceName, String errorMessage) {

        return new CmsLockInfo(resourceName, State.other, null, errorMessage);
    }

    /**
     * Returns a lock info bean for  a resource locked by another user.<p>
     * 
     * @param resourceName the resource name 
     * @param lockUser the other user 
     * 
     * @return the new lock info bean 
     */
    public static CmsLockInfo forLockedResource(String resourceName, String lockUser) {

        return new CmsLockInfo(resourceName, State.locked, lockUser, null);
    }

    /**
     * Returns a lock info bean for a successful lock operation.<p>
     * 
     * @return the new lock info bean 
     */
    public static CmsLockInfo forSuccess() {

        return new CmsLockInfo(null, State.success, null, null);
    }

    /**
     * Returns true if the locking succeeded.<p>
     * 
     * @return true if the locking succeeded
     */
    public boolean couldLock() {

        return m_state == State.success;
    }

    /**
     * Returns the additional error message.<p>
     *  
     * @return the additional error message 
     */
    public String getErrorMessage() {

        return m_exceptionMessage;
    }

    /**
     * Returns the resource name.<p>
     * 
     * @return the resource name 
     */
    public String getResourceName() {

        return m_resourceName;
    }

    /** 
     * Returns the state of the locking operation.<p>
     * 
     * @return the state of the locking operation 
     */
    public State getState() {

        return m_state;
    }

    /**
     * Returns the user name.<p>
     * 
     * @return a user name 
     */
    public String getUser() {

        return m_user;
    }

}
