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

package org.opencms.workplace.editors.directedit;

import org.opencms.util.CmsStringUtil;

/**
 * Constants to indicate which mode to use for placement of the HTML that generates
 * the direct edit buttons.<p>
 *
 * There are 3 basic options for the direct edit mode:
 * <ul>
 * <li>{@link #FALSE}: Direct edit is disabled.
 * <li>{@link #AUTO}: Direct edit button HTML is inserted automatically.
 * <li>{@link #MANUAL}: Direct edit button HTML is inserted manually by using &ltcms: editable mode="manual" /&gt tags.
 * </ul>
 *
 * There is one global option set for the page / template.
 * The default is {@link #AUTO}.<p>
 *
 * There is an additional constant {@link #TRUE} that means "use the default mode of the page / template".<p>
 *
 * It is possible to switch modes for an individual content loop.
 * This is intended to use with XmlContents that require special placement of the direct edit HTML
 * because the default placement does not give good results.<p>
 *
 * @since 6.2.3
 */
public final class CmsDirectEditMode {

    /** Indicates automatic placement of direct edit HTML. */
    public static final CmsDirectEditMode AUTO = new CmsDirectEditMode(2);

    /** Indicates direct edit is disabled. */
    public static final CmsDirectEditMode FALSE = new CmsDirectEditMode(0);

    /** Indicates manual placement of direct edit HTML. */
    public static final CmsDirectEditMode MANUAL = new CmsDirectEditMode(3);

    /** Indicates direct edit HTML is to be generated according to the default setting of the current page. */
    public static final CmsDirectEditMode TRUE = new CmsDirectEditMode(1);

    /** Array of mode constants. */
    private static final CmsDirectEditMode[] MODES = {FALSE, TRUE, AUTO, MANUAL};

    /** Constant to indicate editable mode is "auto", which is equivalent to "true". */
    private static final String VALUE_AUTO = "auto";

    /** Constant to indicate editable mode is "false", which means direct edit is turned off. */
    private static final String VALUE_FALSE = CmsStringUtil.FALSE;

    /** Constant to indicate editable mode is "manual". */
    private static final String VALUE_MANUAL = "manual";

    /** Constant to indicate editable mode is "true", which means use the default from the page. */
    private static final String VALUE_TRUE = CmsStringUtil.TRUE;

    /** The direct edit mode to use. */
    private int m_mode;

    /**
     * Hides the public constructor.<p>
     *
     * @param mode the mode to initialize
     */
    private CmsDirectEditMode(int mode) {

        m_mode = mode;
    }

    /**
     * Returns {@link #TRUE} in case the given value is <code>true</code>, {@link #FALSE} otherwise.<p>
     *
     * @param value the direct edit mode to get the constant for
     *
     * @return {@link #TRUE} in case the given value is <code>true</code>, {@link #FALSE} otherwise
     */
    public static CmsDirectEditMode valueOf(boolean value) {

        return value ? TRUE : FALSE;
    }

    /**
     * Returns the mode constant for the selected direct edit int mode.<p>
     *
     * The possible value are:
     * <ul>
     * <li>0: Mode is {@link #FALSE}.
     * <li>1: Mode is {@link #TRUE}.
     * <li>2: Mode is {@link #AUTO}.
     * <li>3: Mode is {@link #MANUAL}.
     * </ul>
     *
     * @param mode the direct edit int mode to get the constant for
     *
     * @return the mode constant for the selected direct edit int mode
     */
    public static CmsDirectEditMode valueOf(int mode) {

        if ((mode > 0) && (mode < MODES.length)) {
            return MODES[mode];
        }
        return FALSE;
    }

    /**
     * Returns the mode constant for the selected direct edit String mode description.<p>
     *
     * For a mode instance <code>A</code>, {@link #toString()} returns the String mode description.<p>
     *
     * @param value the direct edit String mode description to get the constant for
     *
     * @return the mode constant for the selected direct edit String mode description
     */
    public static CmsDirectEditMode valueOf(String value) {

        CmsDirectEditMode result = FALSE;
        if (CmsStringUtil.isNotEmpty(value)) {
            value = value.trim().toLowerCase();
            if (Boolean.valueOf(value).booleanValue()) {
                result = TRUE;
            } else if (VALUE_AUTO.equals(value)) {
                result = AUTO;
            } else if (VALUE_MANUAL.equals(value)) {
                result = MANUAL;
            }
        }
        return result;
    }

    /**
     * Returns this modes int value.<p>
     *
     * @return this modes int value
     *
     * @see #valueOf(int)
     */
    public int getMode() {

        return m_mode;
    }

    /**
     * Returns <code>true</code> in case this mode indicates direct edit is enabled.<p>
     *
     * Direct edit is enabled if this mode is not {@link #FALSE}, which is
     * identical to <code>{@link #getMode()} > 0</code>.
     *
     * @return <code>true</code> in case this mode indicates direct edit is enabled
     */
    public boolean isEnabled() {

        return m_mode > 0;
    }

    /**
     * @see java.lang.Object#toString()
     * @see #valueOf(String)
     */
    @Override
    public String toString() {

        switch (m_mode) {
            case 1:
                return VALUE_TRUE;
            case 2:
                return VALUE_AUTO;
            case 3:
                return VALUE_MANUAL;
            default:
                return VALUE_FALSE;
        }
    }
}