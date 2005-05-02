/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/Messages.java,v $
 * Date   : $Date: 2005/05/02 16:42:04 $
 * Version: $Revision: 1.1 $
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

package org.opencms.jsp;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Jan Baudisch (j.baudisch@alkacon.com)
 * @since 5.7.3
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MISSING_CMS_CONTROLLER_1 = "ERR_MISSING_CMS_CONTROLLER_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_RUNTIME_1 = "ERR_RUNTIME_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DEBUG_INTERRUPTED_EXCEPTION_1 = "LOG_DEBUG_INTERRUPTED_EXCEPTION_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERR_CONTENT_SHOW_1 = "LOG_ERR_CONTENT_SHOW_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERR_FILE_PROP_MISSING_2 = "LOG_ERR_FILE_PROP_MISSING_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERR_GEN_LINK_0 = "LOG_ERR_GEN_LINK_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERR_INFO_PROP_READ_1 = "LOG_ERR_INFO_PROP_READ_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERR_JSP_BEAN_0 = "LOG_ERR_JSP_BEAN_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERR_JSP_WRITE_0 = "LOG_ERR_JSP_WRITE_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERR_USER_PROP_READ_1 = "LOG_ERR_USER_PROP_READ_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERR_WORKPL_LABEL_READ_1 = "LOG_ERR_WORKPL_LABEL_READ_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_LOGIN_FAILED_2 = "LOG_LOGIN_FAILED_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_LOGIN_SUCCESSFUL_2 = "LOG_LOGIN_SUCCESSFUL_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_LOGOUT_FAILED_2 = "LOG_LOGOUT_FAILED_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_LOGOUT_SUCCESFUL_2 = "LOG_LOGOUT_SUCCESFUL_2";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.jsp.messages";

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
