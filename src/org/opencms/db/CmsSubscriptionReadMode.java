/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

import org.opencms.util.A_CmsModeIntEnumeration;

/**
 * The read modes to get subscribed resources of a user or group.<p>
 *
 * @since 8.0
 */
public final class CmsSubscriptionReadMode extends A_CmsModeIntEnumeration {

    /** String representation of the read mode: all. */
    public static final String MODE_NAME_ALL = "all";

    /** String representation of the read mode: unvisited. */
    public static final String MODE_NAME_UNVISITED = "unvisited";

    /** String representation of the read mode: visited. */
    public static final String MODE_NAME_VISITED = "visited";

    /** Subscription read mode: all.  */
    public static final CmsSubscriptionReadMode ALL = new CmsSubscriptionReadMode(1);

    /** Subscription read mode: unvisited.  */
    public static final CmsSubscriptionReadMode UNVISITED = new CmsSubscriptionReadMode(2);

    /** Subscription read mode: visited.  */
    public static final CmsSubscriptionReadMode VISITED = new CmsSubscriptionReadMode(3);

    /** Serializable version id. */
    private static final long serialVersionUID = 7547476104782346902L;

    /**
     * Private constructor.<p>
     *
     * @param mode the subscription read mode integer representation
     */
    private CmsSubscriptionReadMode(int mode) {

        super(mode);
    }

    /**
     * Returns the subscription read mode for the given mode name.<p>
     *
     * @param modeName the subscription read mode name to get the read mode for
     *
     * @return the subscription read mode for the given mode name
     */
    public static CmsSubscriptionReadMode modeForName(String modeName) {

        if (MODE_NAME_ALL.equals(modeName)) {
            return ALL;
        } else if (MODE_NAME_VISITED.equals(modeName)) {
            return VISITED;
        }
        return UNVISITED;
    }

    /**
     * Returns the subscription read mode for the given mode value.<p>
     *
     * This is used only for serialization and should not be accessed for other purposes.<p>
     *
     * @param type the subscription read mode value to get the read mode for
     *
     * @return the subscription read mode for the given mode value
     */
    public static CmsSubscriptionReadMode valueOf(int type) {

        switch (type) {
            case 1:
                return ALL;
            case 2:
                return UNVISITED;
            case 3:
                return VISITED;
            default:
                return UNVISITED;
        }
    }

    /**
     * Returns if the mode is set to {@link #ALL}.<p>
     *
     * @return true if the mode is set to {@link #ALL}, otherwise false
     */
    public boolean isAll() {

        return getMode() == ALL.getMode();
    }

    /**
     * Returns if the mode is set to {@link #UNVISITED}.<p>
     *
     * @return true if the mode is set to {@link #UNVISITED}, otherwise false
     */
    public boolean isUnVisited() {

        return getMode() == UNVISITED.getMode();
    }

    /**
     * Returns if the mode is set to {@link #VISITED}.<p>
     *
     * @return true if the mode is set to {@link #VISITED}, otherwise false
     */
    public boolean isVisited() {

        return getMode() == VISITED.getMode();
    }

    /**
     *
     * @see org.opencms.util.A_CmsModeIntEnumeration#toString()
     */
    @Override
    public String toString() {

        switch (getMode()) {
            case 1:
                return MODE_NAME_ALL;
            case 2:
                return MODE_NAME_UNVISITED;
            case 3:
                return MODE_NAME_VISITED;
            default:
                return MODE_NAME_UNVISITED;
        }
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() {

        return new CmsSubscriptionReadMode(getMode());
    }

}
