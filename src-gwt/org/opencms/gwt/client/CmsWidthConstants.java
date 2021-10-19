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

package org.opencms.gwt.client;

/**
 * Adds constants / static methods for accessing the width class breakpoints.
 */
public class CmsWidthConstants {

    /** Upper bound for 'small'.*/
    public static final int SMALL_HIGH = 767;

    /** Lower bound for 'medium'. */
    public static final int MEDIUM_LOW = 768;

    /** Upper bound for medium. */
    public static final int MEDIUM_HIGH = 1023;

    /** Lower bound for 'large'.*/
    public static final int LARGE_LOW = 1024;

    /** 'px' suffix. */
    public static final String PX = "px";

    /**
     *
     * Lower bound for 'large'.
     *
     * @return a CSS width value
     *
     * */
    public static String largeLow() {

        return LARGE_LOW + PX;
    }

    /**
     * Upper bound for medium.
     *
     * @return a CSS width value
     * */
    public static String mediumHigh() {

        return MEDIUM_HIGH + PX;
    }

    /**
     * Lower bound for 'medium'.
     *
     * @return a CSS width value
     * */
    public static String mediumLow() {

        return MEDIUM_LOW + PX;
    }

    /**
     * Upper bound for 'small'.
     *
     * @return a CSS width value
     * */
    public static String smallHigh() {

        return SMALL_HIGH + PX;
    }

}
