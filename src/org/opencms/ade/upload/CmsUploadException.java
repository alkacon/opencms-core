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

package org.opencms.ade.upload;

/**
 * This exception makes it possible to handle expected upload errors in another way than
 * unexpected errors.<p>
 *
 * It is supposed to be used when an expected upload exception occurred.<p>
 *
 * For example we can send a message like "file size limit exceeded" so the user knows
 * that he selected a file is responsible for the error. In other cases it does not make
 * sense to confuse the user with error information he won't understand like an encoding
 * error, ...<p>
 *
 * @since 8.0.0
 */
public class CmsUploadException extends RuntimeException {

    /** The serial version UID. */
    private static final long serialVersionUID = 5436746014936990102L;

    /**
     * Public constructor that sets the error message.<p>
     *
     * @param message the message
     */
    public CmsUploadException(String message) {

        super(message);
    }

    /**
     * Public constructor that sets the error message and the cause.<p>
     *
     * @param message the message
     * @param cause the cause
     */
    public CmsUploadException(String message, Throwable cause) {

        super(message, cause);
    }

}
