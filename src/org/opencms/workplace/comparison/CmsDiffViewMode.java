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

package org.opencms.workplace.comparison;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsIllegalArgumentException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Wrapper class for the different types of diff modes.<p>
 *
 * The possibles values are:<br>
 * <ul>
 * <li>{@link #ALL}</li>
 * <li>{@link #DIFF_ONLY}</li>
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public final class CmsDiffViewMode implements Serializable {

    /** Constant for viewing all lines. */
    public static final CmsDiffViewMode ALL = new CmsDiffViewMode(
        "all",
        Messages.get().container(Messages.GUI_DIFF_MODE_DIFFONLY_NAME_0));

    /** Constant for viewing only the different lines. */
    public static final CmsDiffViewMode DIFF_ONLY = new CmsDiffViewMode(
        "diff_only",
        Messages.get().container(Messages.GUI_DIFF_MODE_ALL_NAME_0));

    /** uid for serialization. */
    private static final long serialVersionUID = -9107946096096683776L;

    /** Array constant for all available align types. */
    private static final CmsDiffViewMode[] VALUE_ARRAY = {ALL, DIFF_ONLY};

    /** List of mode constants. */
    public static final List<CmsDiffViewMode> VALUES = Collections.unmodifiableList(Arrays.asList(VALUE_ARRAY));

    /** Internal representation. */
    private final String m_mode;

    /** Name to show. */
    private final CmsMessageContainer m_name;

    /**
     * Private constructor.
     * <p>
     *
     * @param mode the view mode
     * @param name the name to show
     */
    private CmsDiffViewMode(String mode, CmsMessageContainer name) {

        m_mode = mode;
        m_name = name;
    }

    /**
     * Parses an string into an element of this enumeration.<p>
     *
     * @param value the mode to parse
     *
     * @return the enumeration element
     *
     * @throws CmsIllegalArgumentException if the given value could not be matched against an
     *             element of this type.
     */
    public static CmsDiffViewMode valueOf(String value) throws CmsIllegalArgumentException {

        if (value == null) {
            return null;
        }
        Iterator<CmsDiffViewMode> iter = VALUES.iterator();
        while (iter.hasNext()) {
            CmsDiffViewMode target = iter.next();
            if (value.equals(target.getMode())) {
                return target;
            }
        }
        throw new CmsIllegalArgumentException(
            org.opencms.db.Messages.get().container(
                org.opencms.db.Messages.ERR_MODE_ENUM_PARSE_2,
                value,
                CmsDiffViewMode.class.getName()));
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
     * Returns the name to show.<p>
     *
     * @return the name to show
     */
    public CmsMessageContainer getName() {

        return m_name;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return m_mode;
    }
}