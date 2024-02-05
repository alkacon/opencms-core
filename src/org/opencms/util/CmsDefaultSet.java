/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A utility class used for keeping track of a set of objects. You can explicitly include or exclude objects,
 * and define a default membership value for those objects whose membership hasn't been explicitly set.<p>
 *
 * @param <T> the element type
 */
public class CmsDefaultSet<T> implements IsSerializable {

    /** The default membership value for objects. */
    private boolean m_defaultMembership;

    /** The map for keeping track of the explicitly set memberships. */
    private HashMap<T, Boolean> m_membershipMap = new HashMap<T, Boolean>();

    /** Flag which controls whether this object can be modified. */
    private boolean m_frozen;

    /**
     * Checks that this object isn't frozen.<p>
     */
    public void checkNotFrozen() {

        if (m_frozen) {
            throw new IllegalStateException("Can't modify frozen default set.");
        }
    }

    /**
     * Returns true if the given object is a member of this set.<p>
     *
     * @param value the value to check
     *
     * @return true  if the value is a member
     */
    public boolean contains(T value) {

        Boolean isMember = m_membershipMap.get(value);
        if (isMember != null) {
            return isMember.booleanValue();
        } else {
            return m_defaultMembership;
        }
    }

    /**
     * Makes the object unmodifiable.<p>
     */
    public void freeze() {

        m_frozen = true;
    }

    /**
     * Gets the map internally used for storing the membership statuses.<p>
     *
     * @return the membership map
     */
    public Map<T, Boolean> getBaseMap() {

        return Collections.unmodifiableMap(m_membershipMap);
    }

    /**
     * Gets the default membership value.<p>
     *
     * @return the default membership value
     */
    public boolean getDefaultMembership() {

        return m_defaultMembership;
    }

    /***
     * Sets the membership of an object.<p>
     *
     * @param value the object
     * @param isMember true if the object should be a member, otherwise false
     */
    public void setContains(T value, boolean isMember) {

        checkNotFrozen();
        m_membershipMap.put(value, Boolean.valueOf(isMember));
    }

    /**
     * Sets the default membership value.<p>
     *
     * @param defaultMembership the new value
     */
    public void setDefaultMembership(boolean defaultMembership) {

        checkNotFrozen();
        m_defaultMembership = defaultMembership;
    }
}
