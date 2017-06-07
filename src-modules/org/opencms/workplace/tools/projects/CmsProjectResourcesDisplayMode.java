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

package org.opencms.workplace.tools.projects;

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
 *   <li>{@link #ALL_CHANGES}</li>
 *   <li>{@link #NEW_FILES}</li>
 *   <li>{@link #DELETED_FILES}</li>
 *   <li>{@link #MODIFIED_FILES}</li>
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public final class CmsProjectResourcesDisplayMode implements Serializable {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 923124162399716633L;

    /** Constant for the all changes view. */
    public static final CmsProjectResourcesDisplayMode ALL_CHANGES = new CmsProjectResourcesDisplayMode("all");

    /** Constant for the deleted files only view.  */
    public static final CmsProjectResourcesDisplayMode DELETED_FILES = new CmsProjectResourcesDisplayMode("deleted");

    /** Constant for the modified files only view.     */
    public static final CmsProjectResourcesDisplayMode MODIFIED_FILES = new CmsProjectResourcesDisplayMode("changed");

    /** Constant for the new files only view. */
    public static final CmsProjectResourcesDisplayMode NEW_FILES = new CmsProjectResourcesDisplayMode("new");

    /** Array constant for all available align types. */
    private static final CmsProjectResourcesDisplayMode[] VALUE_ARRAY = {
        ALL_CHANGES,
        NEW_FILES,
        DELETED_FILES,
        MODIFIED_FILES};

    /** List of mode constants. */
    public static final List VALUES = Collections.unmodifiableList(Arrays.asList(VALUE_ARRAY));

    /** Internal representation. */
    private final String m_mode;

    /**
     * Private constructor.<p>
     *
     * @param mode the view mode
     */
    private CmsProjectResourcesDisplayMode(String mode) {

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
    public static CmsProjectResourcesDisplayMode valueOf(String value) throws CmsIllegalArgumentException {

        Iterator iter = VALUES.iterator();
        while (iter.hasNext()) {
            CmsProjectResourcesDisplayMode target = (CmsProjectResourcesDisplayMode)iter.next();
            if (value.equals(target.getMode())) {
                return target;
            }
        }
        throw new CmsIllegalArgumentException(
            org.opencms.db.Messages.get().container(
                org.opencms.db.Messages.ERR_MODE_ENUM_PARSE_2,
                value,
                CmsProjectResourcesDisplayMode.class.getName()));
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
    @Override
    public String toString() {

        return m_mode;
    }
}