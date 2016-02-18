/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C)  Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.shared;

/**
 * A class which contains constants for menu command parameters.<p>
 */
public final class CmsMenuCommandParameters {

    /** Parameter key for dialog height. */
    public static final String PARAM_DIALOG_HEIGHT = "dialogHeight";

    /** Parameter key for dialog URI. */
    public static final String PARAM_DIALOG_URI = "dialogUri";

    /** Parameter key for dialog width. */
    public static final String PARAM_DIALOG_WIDTH = "dialogWidth";

    /** Parameter key for file names. */
    public static final String PARAM_FILENAME = "filename";

    /** Parameter for 'reload on edit'. */
    public static final String PARAM_RELOAD = "reload";

    /** Parameter to open the edit dialog in the same window, not using any overlays and iFrames. */
    public static final String PARAM_USE_SELF = "useSelf";

    /**
     * Hidden constructor.<p>
     */
    private CmsMenuCommandParameters() {

        // does nothing
    }

}
