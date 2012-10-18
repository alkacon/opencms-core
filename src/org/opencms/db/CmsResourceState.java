/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.file.CmsResource;

import java.io.Serializable;

/**
 *  Enumeration class for the different resource states.<p>
 * 
 * @since 6.5.3 
 */
public class CmsResourceState implements Serializable {

    /** Indicates if a resource has been changed in the offline version when compared to the online version. */
    public static final CmsResourceState STATE_CHANGED = new CmsResourceState(1, 'C');

    /** Indicates if a resource has been deleted in the offline version when compared to the online version. */
    public static final CmsResourceState STATE_DELETED = new CmsResourceState(3, 'D');

    /**
     * Special state value that indicates the current state must be kept on a resource,
     * this value must never be written to the database.
     */
    public static final CmsResourceState STATE_KEEP = new CmsResourceState(99, '_');

    /** Indicates if a resource is new in the offline version when compared to the online version. */
    public static final CmsResourceState STATE_NEW = new CmsResourceState(2, 'N');

    /** Indicates if a resource is unchanged in the offline version when compared to the online version. */
    public static final CmsResourceState STATE_UNCHANGED = new CmsResourceState(0, 'U');

    /** serializable version id. */
    private static final long serialVersionUID = -2704354453252295414L;

    /** The state abbreviation character. */
    private char m_abbrev;

    /** The integer state representation. */
    private int m_state;

    /**
     * Default constructor for serialization.<p>
     * 
     * @deprecated Don't use this constructor! 
     * It is only used to transfer the resource state via RPC call.
     */
    @Deprecated
    protected CmsResourceState() {

        // noop
    }

    /**
     * protected constructor.<p>
     * 
     * @param state an integer representing the state 
     * @param abbrev an abbreviation character
     */
    protected CmsResourceState(int state, char abbrev) {

        m_state = state;
        m_abbrev = abbrev;
    }

    /**
     * Returns the resource state object from the resource state integer.<p>
     * 
     * @param state the resource state integer
     * 
     * @return the resource state object
     */
    public static CmsResourceState valueOf(int state) {

        switch (state) {
            case 0:
                return CmsResource.STATE_UNCHANGED;
            case 1:
                return CmsResource.STATE_CHANGED;
            case 2:
                return CmsResource.STATE_NEW;
            case 3:
                return CmsResource.STATE_DELETED;
            case 99:
            default:
                return CmsResource.STATE_KEEP;
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        return (obj instanceof CmsResourceState) && (((CmsResourceState)obj).m_state == m_state);
    }

    /**
     * Returns resource state abbreviation.<p>
     * 
     * @return resource state abbreviation
     */
    public char getAbbreviation() {

        return m_abbrev;
    }

    /**
     * Returns the resource state integer for this resource state object.<p>
     * 
     * @return the resource state integer for this resource state object
     */
    public int getState() {

        return m_state;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return new Integer(m_state).hashCode();
    }

    /**
     * Returns if this is {@link CmsResource#STATE_CHANGED}.<p>
     * 
     * @return if this is {@link CmsResource#STATE_CHANGED}
     */
    public boolean isChanged() {

        return (this.equals(CmsResource.STATE_CHANGED));
    }

    /**
     * Returns if this is {@link CmsResource#STATE_DELETED}.<p>
     * 
     * @return if this is {@link CmsResource#STATE_DELETED}
     */
    public boolean isDeleted() {

        return (this.equals(CmsResource.STATE_DELETED));
    }

    /**
     * Returns if this is {@link CmsResource#STATE_KEEP}.<p>
     * 
     * @return if this is {@link CmsResource#STATE_KEEP}
     */
    public boolean isKeep() {

        return (this.equals(CmsResource.STATE_KEEP));
    }

    /**
     * Returns if this is {@link CmsResource#STATE_NEW}.<p>
     * 
     * @return if this is {@link CmsResource#STATE_NEW}
     */
    public boolean isNew() {

        return (this.equals(CmsResource.STATE_NEW));
    }

    /**
     * Returns if this is {@link CmsResource#STATE_UNCHANGED}.<p>
     * 
     * @return if this is {@link CmsResource#STATE_UNCHANGED}
     */
    public boolean isUnchanged() {

        return (this.equals(CmsResource.STATE_UNCHANGED));
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return String.valueOf(getState());
    }
}