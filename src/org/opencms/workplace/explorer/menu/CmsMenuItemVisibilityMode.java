/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/menu/CmsMenuItemVisibilityMode.java,v $
 * Date   : $Date: 2011/03/23 14:51:51 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.explorer.menu;

import org.opencms.util.A_CmsModeIntEnumeration;

/**
 * The visibility modes of a context menu item in the explorer view.<p>
 * 
 * @author Andreas Zahner  
 * 
 * @version $Revision: 1.9 $ 
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

    /** Serializable version id. */
    private static final long serialVersionUID = 2526260041565757791L;

    /** The name of the message key for the visibility mode. */
    private String m_messageKey;

    /**
     * Private constructor.<p>
     * 
     * @param mode the menu item visibility mode integer representation
     */
    private CmsMenuItemVisibilityMode(int mode) {

        super(mode);
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

        CmsMenuItemVisibilityMode mode = (CmsMenuItemVisibilityMode)clone();
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
     * Returns if the mode is set to {@link #VISIBILITY_INVISIBLE}.<p>
     * 
     * @return true if the mode is set to {@link #VISIBILITY_INVISIBLE}, otherwise false
     */
    public boolean isInVisible() {

        return getMode() == VISIBILITY_INVISIBLE.getMode();
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() {

        return new CmsMenuItemVisibilityMode(this.getMode());
    }

}
