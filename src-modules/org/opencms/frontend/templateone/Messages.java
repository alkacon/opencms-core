/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/Messages.java,v $
 * Date   : $Date: 2005/06/23 10:47:25 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.frontend.templateone;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Achim Westermann 
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.0.0 
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CMSTEMPLATEPARTS_CLEARED_0 = "LOG_CMSTEMPLATEPARTS_CLEARED_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CMSTEMPLATEPARTS_FOUND_0 = "LOG_CMSTEMPLATEPARTS_FOUND_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CMSTEMPLATEPARTS_NOT_FOUND_0 = "LOG_CMSTEMPLATEPARTS_NOT_FOUND_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_EXT_MODULE_FILE_NOT_FOUND_1 = "LOG_EXT_MODULE_FILE_NOT_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_EXT_MODULE_PROP_VALUE_1 = "LOG_EXT_MODULE_PROP_VALUE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_INCLUDE_PART_ERR_2 = "LOG_INCLUDE_PART_ERR_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_INCLUDE_PART_FOUND_1 = "LOG_INCLUDE_PART_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_INCLUDE_PART_NOT_FOUND_1 = "LOG_INCLUDE_PART_NOT_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MICROSITE_FOLDER_NOT_FOUND_0 = "LOG_MICROSITE_FOLDER_NOT_FOUND_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MICROSITE_READ_START_FOLDER_0 = "LOG_MICROSITE_READ_START_FOLDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SEND_MAIL_CONTACT_1 = "LOG_SEND_MAIL_CONTACT_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SEND_MAIL_RECOMMENDPAGE_1 = "LOG_SEND_MAIL_RECOMMENDPAGE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_XMLCONTEN_CONFIG_NOT_FOUND_2 = "LOG_XMLCONTEN_CONFIG_NOT_FOUND_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_XSD_HEADLINES_SET_ERR_0 = "LOG_XSD_HEADLINES_SET_ERR_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.frontend.templateone.messages";

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