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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.contextmenu;

import org.opencms.util.A_CmsModeIntEnumeration;

/**
 * The visibility modes of a context menu item in the explorer view.<p>
 *
 * @since 6.5.6
 */
public final class CmsMenuItemVisibilityMode extends A_CmsModeIntEnumeration {

    /** Menu item visibility: active.  */
    public static final CmsMenuItemVisibilityMode VISIBILITY_ACTIVE = new CmsMenuItemVisibilityMode(1);

    /** Menu item visibility: inactive.  */
    public static final CmsMenuItemVisibilityMode VISIBILITY_INACTIVE = new CmsMenuItemVisibilityMode(2);

    /** Menu item visibility: invisible.  */
    public static final CmsMenuItemVisibilityMode VISIBILITY_INVISIBLE = new CmsMenuItemVisibilityMode(3);

    /** Menu item visibility: invisible, use next best menu item with same ID.  */
    public static final CmsMenuItemVisibilityMode VISIBILITY_USE_NEXT = new CmsMenuItemVisibilityMode(4);

    /** Serializable version id. */
    private static final long serialVersionUID = 2526260041565757791L;

    /** The name of the message key for the visibility mode. */
    private String m_messageKey;

    /** The prioritization flag. */
    private boolean m_prioritized;

    /**
     * Private constructor.<p>
     *
     * @param mode the menu item visibility mode integer representation
     */
    private CmsMenuItemVisibilityMode(int mode) {

        super(mode);
    }

    /**
     * Utilitiy method that returns 'active' if the parameter is true, otherwise inactive.<p>
     *
     * @param active - whether return value should be 'active'
     *
     * @return the visibility
     */
    public static CmsMenuItemVisibilityMode activeInactive(boolean active) {

        if (active) {
            return VISIBILITY_ACTIVE;
        } else {
            return VISIBILITY_INACTIVE;
        }
    }

    /**
     * Utility method that returns 'active' if the parameter is true, otherwise invisible.<p>
     *
     * @param active - whether return value should be 'active' rather than 'invisible'
     *
     * @return the visibility
     */
    public static CmsMenuItemVisibilityMode activeInvisible(boolean active) {

        if (active) {
            return VISIBILITY_ACTIVE;
        } else {
            return VISIBILITY_INVISIBLE;
        }
    }

    /**
     * Returns the menu item visibility mode for the given mode value.<p>
     *
     * This is used only for serialization and should not be accessed for other purposes.<p>
     *
     * @param type the mode value to get the item visibility mode for
     *
     * @return the menu item visibility mode for the given mode value
     */
    public static CmsMenuItemVisibilityMode valueOf(int type) {

        switch (type) {
            case 1:
                return VISIBILITY_ACTIVE;
            case 2:
                return VISIBILITY_INACTIVE;
            case 3:
                return VISIBILITY_INVISIBLE;
            default:
                return VISIBILITY_INVISIBLE;
        }
    }

    /**
     * Adds the name of the message key for the visibility mode.<p>
     *
     * @param messageKey the name of the message key for the visibility mode
     * @return an extended visibility mode containing the message key
     */
    public CmsMenuItemVisibilityMode addMessageKey(String messageKey) {

        CmsMenuItemVisibilityMode mode = clone();
        mode.m_messageKey = messageKey;
        return mode;
    }

    /**
     * Returns the name of the message key for the visibility mode.<p>
     *
     * Is usually used as description for the inactive visibility modes.<p>
     *
     * @return the name of the message key for the visibility mode
     */
    public String getMessageKey() {

        return m_messageKey;
    }

    /**
     * Returns if the mode is set to {@link #VISIBILITY_ACTIVE}.<p>
     *
     * @return true if the mode is set to {@link #VISIBILITY_ACTIVE}, otherwise false
     */
    public boolean isActive() {

        return getMode() == VISIBILITY_ACTIVE.getMode();
    }

    /**
     * Returns if the mode is set to {@link #VISIBILITY_INACTIVE}.<p>
     *
     * @return true if the mode is set to {@link #VISIBILITY_INACTIVE}, otherwise false
     */
    public boolean isInActive() {

        return getMode() == VISIBILITY_INACTIVE.getMode();
    }

    /**
     * Returns if the mode is set to {@link #VISIBILITY_INVISIBLE} or {@link #VISIBILITY_USE_NEXT}.<p>
     *
     * @return true if the mode is set to {@link #VISIBILITY_INVISIBLE} or {@link #VISIBILITY_USE_NEXT}, otherwise false
     */
    public boolean isInVisible() {

        return (getMode() == VISIBILITY_INVISIBLE.getMode()) || (getMode() == VISIBILITY_USE_NEXT.getMode());
    }

    /**
     * Returns the prioritization flag.<p>
     *
     * @return prioritization flag
     */
    public boolean isPrioritized() {

        return m_prioritized;
    }

    /**
     * Returns true if this item is invisible, but the next best item with the same ID should be used instead.
     *
     * @return true if this item is invisible, but the next best item with the same ID should be used instead
     */
    public boolean isUseNext() {

        return getMode() == VISIBILITY_USE_NEXT.getMode();
    }

    /**
     * Returns a prioritized instance of the visibility mode.<p>
     *
     * @param prioritized <code>true</code> to prioritize
     *
     * @return the new visibility mode instance
     */
    public CmsMenuItemVisibilityMode prioritize(boolean prioritized) {

        if (m_prioritized != prioritized) {
            CmsMenuItemVisibilityMode result = clone();
            result.m_prioritized = prioritized;
            return result;
        } else {
            return this;
        }
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    protected CmsMenuItemVisibilityMode clone() {

        return new CmsMenuItemVisibilityMode(getMode());
    }

}
