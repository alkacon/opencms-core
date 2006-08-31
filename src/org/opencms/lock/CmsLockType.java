/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/lock/CmsLockType.java,v $
 * Date   : $Date: 2006/08/31 08:55:32 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.lock;

/**
 * Indicates the different possible lock types.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 7.0.0
 */
public final class CmsLockType {

    /** 
     * A lock that allows the user to edit the resource's structure record, 
     * it's resource record, and its content record.<p>
     *
     * This lock is assigned to files that are locked via the context menu.
     */
    public static final CmsLockType EXCLUSIVE = new CmsLockType(4);

    /**
     * A lock that is inherited from a locked parent folder.
     */
    public static final CmsLockType INHERITED = new CmsLockType(3);

    /**
     * A lock that allows the user to edit the resource’s structure record only, 
     * but not it’s resource record nor content record.<p>
     * 
     * This lock is assigned to files if a sibling of the resource record has
     * already an exclusive lock. 
     */
    public static final CmsLockType SHARED_EXCLUSIVE = new CmsLockType(2);

    /**
     * A lock that allows the user to edit the resource’s structure record only, 
     * but not it’s resource record nor content record.<p>
     * 
     * This lock is assigned to resources that already have a shared exclusive lock,
     * and then inherit a lock because one if it's parent folders gets locked.
     */
    public static final CmsLockType SHARED_INHERITED = new CmsLockType(1);

    /** 
     * A temporary exclisive lock that allows the user to edit the resource's structure record, 
     * it's resource record, and its content record.<p>
     * 
     * This lock is identical to the {@link #EXCLUSIVE} lock, but it is automatically removed after 
     * a user is logged out.<p>
     */
    public static final CmsLockType TEMPORARY = new CmsLockType(6);

    /**
     * Type of the NULL lock obtained by {@link CmsLock#getNullLock()}.<p>
     */
    public static final CmsLockType UNLOCKED = new CmsLockType(0);

    /**
     * A lock that indicates that the resource is assigned to a task in a workflow.<p>
     */
    public static final CmsLockType WORKFLOW = new CmsLockType(5);

    /** Indicates the lock type. */
    private int m_type;

    /**
     * Hides the public constructor.<p> 
     */
    private CmsLockType() {

        // hide public constructor
    }

    /**
     * Creates a new lock type with the given name.<p>
     * 
     * @param type the type id to use
     */
    private CmsLockType(int type) {

        m_type = type;
    }

    /**
     * Returns the lock type for the given type value.<p>
     * 
     * This is used only for serialization and should not be accessed for other purposes.<p>
     *  
     * @param type the type value to get the lock type for
     * 
     * @return the lock type for the given type value
     */
    public static CmsLockType getType(int type) {

        switch (type) {
            case 1:
                return SHARED_INHERITED;
            case 2:
                return SHARED_EXCLUSIVE;
            case 3:
                return INHERITED;
            case 4:
                return EXCLUSIVE;
            case 5:
                return WORKFLOW;
            case 6:
                return TEMPORARY;
            default:
                return UNLOCKED;
        }
    }

    /**
     * Use the <code>==</code> if possibe since all instances are constants anyway.<p> 
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        return obj == this;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return m_type;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        switch (m_type) {
            case 1:
                return "shared inherited";
            case 2:
                return "shared exclusive";
            case 3:
                return "inherited";
            case 4:
                return "exclusive";
            case 5:
                return "workflow";
            case 6:
                return "temporary exclusive";
            default:
                return "unlocked";
        }
    }
}