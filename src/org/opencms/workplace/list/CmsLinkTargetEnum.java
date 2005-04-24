/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/Attic/CmsLinkTargetEnum.java,v $
 * Date   : $Date: 2005/04/24 11:20:31 $
 * Version: $Revision: 1.2 $
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

package org.opencms.workplace.list;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Wrapper class for
 * the different types of link targets.<p>
 * 
 * The possibles values are:<br>
 * <ul>
 *   <il>SameTarget</il>
 *   <il>NewTarget</il>
 *   <il>ParentTarget</il>
 *   <il>TopTarget</il>
 *   <il>NamedTarget</il>
 * </ul>
 * <p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.2 $
 * @since 5.7.3
 */
public final class CmsLinkTargetEnum {

    /**
     * Constant for a named frame target.<p>
     */
    public static final CmsLinkTargetEnum TARGET_NAMED = new CmsLinkTargetEnum(4, "");

    /**
     * Constant for a new window target.<p>
     */
    public static final CmsLinkTargetEnum TARGET_NEW = new CmsLinkTargetEnum(1, "_blank");

    /**
     * Constant for the parent frame target.<p>
     */
    public static final CmsLinkTargetEnum TARGET_PARENT = new CmsLinkTargetEnum(2, "_parent");

    /**
     * Constant for the same frame target.<p>
     */
    public static final CmsLinkTargetEnum TARGET_SELF = new CmsLinkTargetEnum(0, "_this");

    /**
     * Constant for the top frame target.<p>
     */
    public static final CmsLinkTargetEnum TARGET_TOP = new CmsLinkTargetEnum(3, "_top");

    /** Array constant holding all link targets. */
    private static final CmsLinkTargetEnum[] C_VALUES = {TARGET_SELF, TARGET_NEW, TARGET_PARENT, TARGET_TOP, TARGET_NAMED};

    /**
     * List of mode constants.<p>
     */
    public static final List VALUES = Collections.unmodifiableList(Arrays.asList(C_VALUES));

    /** Name of the frame. */
    private final String m_frame;

    /** Internal identifier. */
    private final int m_type;

    /**
     * Private constructor.<p>
     * 
     * @param type the unique identifier
     * @param frame the name of the frame
     */
    private CmsLinkTargetEnum(int type, String frame) {

        m_type = type;
        m_frame = frame;
    }

    /**
     * Returns a new named frame link target.<p>
     * 
     * @param frameName the name of the target frame
     * 
     * @return the new target object
     */
    public static CmsLinkTargetEnum newNamedTarget(String frameName) {

        return new CmsLinkTargetEnum(TARGET_NAMED.getType(), frameName);
    }

    /**
     * Parses an integer into an element of this enumeration.<p>
     *
     * @param value the type to parse
     * 
     * @return the enumeration element
     */
    public static CmsLinkTargetEnum valueOf(int value) {

        Iterator iter = VALUES.iterator();
        while (iter.hasNext()) {
            CmsLinkTargetEnum target = (CmsLinkTargetEnum)iter.next();
            if (value == target.getType()) {
                return target;
            }
        }
        throw new IllegalArgumentException(Messages.get().key(
            Messages.ERR_LIST_ENUM_PARSE_2,
            new Integer(value),
            CmsLinkTargetEnum.class.getName()));
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object that) {

        if (!(that instanceof CmsLinkTargetEnum)) {
            return false;
        }
        return this.m_frame.equals(((CmsLinkTargetEnum)that).m_frame);
    }

    /**
     * Returns the integer type.<p>
     * 
     * @return the type
     */
    public int getType() {

        return m_type;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return this.m_frame.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return m_frame;
    }
}