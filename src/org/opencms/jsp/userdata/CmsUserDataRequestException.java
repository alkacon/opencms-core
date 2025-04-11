/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.userdata;

/**
 * Special exception for user data requests.
 */
public class CmsUserDataRequestException extends Exception {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.
     *
     * @param message the message
     */
    public CmsUserDataRequestException(String message) {

        super(message);
    }

    /**
     * Creates a new instance.
     *
     * @param message the message
     * @param t the cause
     */
    public CmsUserDataRequestException(String message, Throwable t) {

        super(message, t);
    }

    /**
     * Creates a new instance.
     *
     * @param t the cause
     */
    public CmsUserDataRequestException(Throwable t) {

        super(t);
    }

}
