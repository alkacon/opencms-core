/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/CmsHtmlIconButtonStyleEnum.java,v $
 * Date   : $Date: 2005/06/23 08:12:45 $
 * Version: $Revision: 1.4 $
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

package org.opencms.workplace.tools;

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.workplace.list.Messages;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Wrapper class for
 * the different style of icon buttons.<p>
 * 
 * The possibles values are:<br>
 * <ul>
 *   <li>BigIconText</li>
 *   <li>SmallIconText</li>
 *   <li>SmallIconOnly</li>
 * </ul>
 * <p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsHtmlIconButtonStyleEnum {

    /** Constant for ascending ordering. */
    public static final CmsHtmlIconButtonStyleEnum BIG_ICON_TEXT = new CmsHtmlIconButtonStyleEnum("bigicontext");

    /** Constant for none ordering. */
    public static final CmsHtmlIconButtonStyleEnum SMALL_ICON_ONLY = new CmsHtmlIconButtonStyleEnum("smallicononly");

    /** Constant for descending ordering. */
    public static final CmsHtmlIconButtonStyleEnum SMALL_ICON_TEXT = new CmsHtmlIconButtonStyleEnum("smallicontext");

    /** List of ordering constants.     */
    public static final List VALUES = Collections.unmodifiableList(Arrays.asList(C_VALUES));

    /** Array constant for column sorting. */
    private static final CmsHtmlIconButtonStyleEnum[] C_VALUES = {BIG_ICON_TEXT, SMALL_ICON_TEXT, SMALL_ICON_ONLY};

    /** Internal representation. */
    private final String m_style;

    /**
     * Private constructor.<p>
     * 
     * @param style internal representation
     */
    private CmsHtmlIconButtonStyleEnum(String style) {

        m_style = style;
    }

    /**
     * Parses an string into an element of this enumeration.<p>
     *
     * @param value the style to parse
     * 
     * @return the enumeration element
     * 
     * @throws CmsIllegalArgumentException if the given instance for the argument is not found
     */
    public static CmsHtmlIconButtonStyleEnum valueOf(String value) throws CmsIllegalArgumentException {

        Iterator iter = VALUES.iterator();
        while (iter.hasNext()) {
            CmsHtmlIconButtonStyleEnum target = (CmsHtmlIconButtonStyleEnum)iter.next();
            if (value == target.getStyle()) {
                return target;
            }
        }
        throw new CmsIllegalArgumentException(Messages.get().container(
            Messages.ERR_LIST_ENUM_PARSE_2,
            new Integer(value),
            CmsHtmlIconButtonStyleEnum.class.getName()));
    }

    /**
     * Returns the style string.<p>
     * 
     * @return the style string
     */
    public String getStyle() {

        return m_style;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return m_style;
    }

}