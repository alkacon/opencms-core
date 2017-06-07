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

package org.opencms.ugc;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Messages for the package org.opencms.editors.usergenerated.<p>
 */
public class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_TOO_MANY_CONTENTS_1 = "ERR_TOO_MANY_CONTENTS_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_TOO_BIG_1 = "ERR_UPLOAD_TOO_BIG_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_FILE_EXTENSION_NOT_ALLOWED_1 = "ERR_UPLOAD_FILE_EXTENSION_NOT_ALLOWED_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_NO_UPLOADS_ALLOWED_0 = "ERR_NO_UPLOADS_ALLOWED_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CANT_EDIT_MULTIPLE_CONTENTS_IN_SESSION_0 = "ERR_CANT_EDIT_MULTIPLE_CONTENTS_IN_SESSION_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INVALID_FILE_NAME_TO_LOAD_1 = "ERR_INVALID_FILE_NAME_TO_LOAD_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_FORM_SESSION_ALREADY_FINISHED_0 = "ERR_FORM_SESSION_ALREADY_FINISHED_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_WAIT_QUEUE_EXCEEDED_0 = "ERR_WAIT_QUEUE_EXCEEDED_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.ugc.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private Messages() {

        // hide the constructor
    }

    /**
     * Returns an instance of this localized message accessor.<p>
     *
     * @return an instance of this localized message accessor
     */
    public static I_CmsMessageBundle get() {

        return INSTANCE;
    }

    /**
     * Returns the bundle name for this OpenCms package.<p>
     *
     * @return the bundle name for this OpenCms package
     */
    public String getBundleName() {

        return BUNDLE_NAME;
    }
}
