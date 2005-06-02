/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/Messages.java,v $
 * Date   : $Date: 2005/06/02 07:12:14 $
 * Version: $Revision: 1.4 $
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

package org.opencms.file.types;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.7.3
 */
public final class Messages extends A_CmsMessageBundle {
    
    /** Message constant for key in the resource bundle. */
    public static final String ERR_CHTYPE_FOLDER_1 = "ERR_CHTYPE_FOLDER_1";

    /** Constant for message ERR_CONFIGURATION_FROZEN_3. */
    public static final String ERR_CONFIG_FROZEN_3 = "ERR_CONFIG_FROZEN_3";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INVALID_RESTYPE_CONFIG_3 = "ERR_INVALID_RESTYPE_CONFIG_3";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INVALID_RESTYPE_CONFIG_NAME_3 = "ERR_INVALID_RESTYPE_CONFIG_NAME_3";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PROCESS_HTML_CONTENT_1 = "ERR_PROCESS_HTML_CONTENT_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_REPLACE_RESOURCE_FOLDER_1 = "ERR_REPLACE_RESOURCE_FOLDER_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_RESTORE_FOLDERS_0 = "ERR_RESTORE_FOLDERS_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_WRITE_FILE_IS_FOLDER_1 = "ERR_WRITE_FILE_IS_FOLDER_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ADD_COPY_RESOURCE_4 = "LOG_ADD_COPY_RESOURCE_4";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ADD_DFLT_PROP_2 = "LOG_ADD_DFLT_PROP_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ADD_MAPPING_TYPE_2 = "LOG_ADD_MAPPING_TYPE_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_GET_CONFIGURATION_1 = "LOG_GET_CONFIGURATION_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_INIT_CONFIGURATION_1 = "LOG_INIT_CONFIGURATION_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_INIT_CONFIGURATION_3 = "LOG_INIT_CONFIGURATION_3";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_INITIALIZE_1 = "LOG_INITIALIZE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_PROCESS_COPY_RESOURCES_3 = "LOG_PROCESS_COPY_RESOURCES_3";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.file.types.messages";

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