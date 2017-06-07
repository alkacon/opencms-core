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

package org.opencms.ugc.shared;

/**
 * Shared constants used by the client- and server-side components of the form editing module.<p>
 */
public class CmsUgcConstants {

    /** Enum representing the different types of errors. The string values of these enum values are intended for use in JavaScript error handling functions. */
    public enum ErrorCode {
        /** Invalid action error. */
        errInvalidAction,

        /** Configuration error. */
        errConfiguration,

        /** Error when trying to upload a file and uploads are not allowed. */
        errNoUploadAllowed,

        /** Error when the  maximum number of contents in the content directory is reached. */
        errMaxContentsExceeded,

        /** Error when the maximum number of entries in the form session request queue is reached. */
        errMaxQueueLengthExceeded,

        /** Error when user tries to upload a file with a file extension which is not among the configured file extensions. */
        errInvalidExtension,

        /** Validation error. */
        errValidation,

        /** Miscellaneous other errors. */
        errMisc,

        /** Error when uploaded files exceed the configured maximum upload size. */
        errMaxUploadSizeExceeded;

    }

    /** The request parameter used for identifying form submits. */
    public static final String PARAM_FORM_DATA_ID = "formDataId";

    /** Name of the parameter containing the session id. */
    public static final String PARAM_SESSION_ID = "formSessionId";

    /** Javascript attribute name. */
    public static final String JS_ATTR_ERROR = "error";

    /** JavaScript attribute name. */
    public static final String JS_ATTR_VALIDATION_ERRORS = "validationErrors";

}
