/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsDependencyIconActionType.java,v $
 * Date   : $Date: 2005/10/11 15:38:24 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.accounts;

import org.opencms.main.CmsIllegalArgumentException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Wrapper class for
 * the different types of icon actions the dependency lists.<p>
 * 
 * The possibles values are:<br>
 * <ul>
 *   <li>{@link #RESOURCE}</li>
 *   <li>{@link #GROUP}</li>
 *   <li>{@link #USER}</li>
 * </ul>
 * <p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 6.0.0
 */
public final class CmsDependencyIconActionType {

    /** Constant for the resource icon action. */
    public static final CmsDependencyIconActionType RESOURCE = new CmsDependencyIconActionType("r");

    /** Constant for the group icon action.  */
    public static final CmsDependencyIconActionType GROUP = new CmsDependencyIconActionType("g");

    /** Constant for the user icon action.     */
    public static final CmsDependencyIconActionType USER = new CmsDependencyIconActionType("u");

    /** Array constant for all available align types. */
    private static final CmsDependencyIconActionType[] VALUE_ARRAY = {
        RESOURCE,
        GROUP,
        USER};

    /** List of mode constants. */
    public static final List VALUES = Collections.unmodifiableList(Arrays.asList(VALUE_ARRAY));

    /** Internal representation. */
    private final String m_mode;

    /**
     * Private constructor.<p>
     * 
     * @param mode the view mode
     */
    private CmsDependencyIconActionType(String mode) {

        m_mode = mode;
    }

    /**
     * Parses an string into an element of this enumeration.<p>
     *
     * @param value the id to parse
     * 
     * @return the enumeration element
     * 
     * @throws CmsIllegalArgumentException if the given value could not be matched against a 
     *         <code>{@link CmsDependencyIconActionType}</code> type.
     */
    public static CmsDependencyIconActionType valueOf(String value) throws CmsIllegalArgumentException {

        Iterator iter = VALUES.iterator();
        while (iter.hasNext()) {
            CmsDependencyIconActionType target = (CmsDependencyIconActionType)iter.next();
            if (value.equals(target.getId())) {
                return target;
            }
        }
        throw new CmsIllegalArgumentException(org.opencms.db.Messages.get().container(
            org.opencms.db.Messages.ERR_MODE_ENUM_PARSE_2,
            value,
            CmsDependencyIconActionType.class.getName()));
    }

    /**
     * Returns the id string.<p>
     * 
     * @return the id string
     */
    public String getId() {

        return m_mode;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return m_mode;
    }
}