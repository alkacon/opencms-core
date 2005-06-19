/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/Attic/CmsProjectFilesModeEnum.java,v $
 * Date   : $Date: 2005/06/19 10:57:06 $
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

package org.opencms.file;

import org.opencms.main.CmsIllegalArgumentException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Wrapper class for
 * the different types of project files view.<p>
 * 
 * The possibles values are:<br>
 * <ul>
 *   <li>ALL_CHANGES</li>
 *   <li>NEW_FILES</li>
 *   <li>DELETED_FILES</li>
 *   <li>MODIFIED_FILES</li>
 * </ul>
 * <p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.2 $
 * @since 5.7.3
 */
public final class CmsProjectFilesModeEnum implements Serializable {

    /** Constant for the all changes view. */
    public static final CmsProjectFilesModeEnum ALL_CHANGES = new CmsProjectFilesModeEnum("all");

    /** Constant for the deleted files only view.  */
    public static final CmsProjectFilesModeEnum DELETED_FILES = new CmsProjectFilesModeEnum("deleted");

    /** Constant for the modified files only view.     */
    public static final CmsProjectFilesModeEnum MODIFIED_FILES = new CmsProjectFilesModeEnum("changed");

    /** Constant for the new files only view. */
    public static final CmsProjectFilesModeEnum NEW_FILES = new CmsProjectFilesModeEnum("new");

    /** Array constant for all available align types. */
    private static final CmsProjectFilesModeEnum[] C_VALUES = {ALL_CHANGES, NEW_FILES, DELETED_FILES, MODIFIED_FILES};

    /** List of mode constants. */
    public static final List VALUES = Collections.unmodifiableList(Arrays.asList(C_VALUES));

    /** Internal representation. */    
    private final String m_mode;
    
    // TODO: Decide what to do with this class
    int todo = 0;

    /**
     * Private constructor.<p>
     * 
     * @param mode the view mode
     */
    private CmsProjectFilesModeEnum(String mode) {

        m_mode = mode;
    }

    /**
     * Parses an string into an element of this enumeration.<p>
     *
     * @param value the mode to parse
     * 
     * @return the enumeration element
     * 
     * @throws CmsIllegalArgumentException if the given value could not be matched against a 
     *         <code>CmsListColumnAlignEnum</code> type.
     */
    public static CmsProjectFilesModeEnum valueOf(String value) throws CmsIllegalArgumentException {

        Iterator iter = VALUES.iterator();
        while (iter.hasNext()) {
            CmsProjectFilesModeEnum target = (CmsProjectFilesModeEnum)iter.next();
            if (value.equals(target.getMode())) {
                return target;
            }
        }
        throw new CmsIllegalArgumentException(Messages.get().container(
            Messages.ERR_MODE_ENUM_PARSE_2,
            new Integer(value),
            CmsProjectFilesModeEnum.class.getName()));
    }

    /**
     * Returns the mode string.<p>
     * 
     * @return the mode string
     */
    public String getMode() {

        return m_mode;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        return m_mode;
    }
}