/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/synchronize/Messages.java,v $
 * Date   : $Date: 2005/06/22 10:38:11 $
 * Version: $Revision: 1.6 $
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

package org.opencms.synchronize;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Jan Baudisch 
 * @since 5.7.3
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CREATE_DIR_1 = "ERR_CREATE_DIR_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CREATE_FILE_1 = "ERR_CREATE_FILE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_EXISTENT_FILE_1 = "ERR_EXISTENT_FILE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_IMPORT_1 = "ERR_IMPORT_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INIT_SYNC_0 = "ERR_INIT_SYNC_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_IO_WRITE_SYNCLIST_0 = "ERR_IO_WRITE_SYNCLIST_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_NO_RFS_DESTINATION_0 = "ERR_NO_RFS_DESTINATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_NO_VFS_SOURCE_0 = "ERR_NO_VFS_SOURCE_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_READ_SYNC_LIST_0 = "ERR_READ_SYNC_LIST_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_READING_FILE_1 = "ERR_READING_FILE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_RFS_DESTINATION_NO_WRITE_1 = "ERR_RFS_DESTINATION_NO_WRITE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_RFS_DESTINATION_NOT_THERE_1 = "ERR_RFS_DESTINATION_NOT_THERE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_WRITE_FILE_0 = "ERR_WRITE_FILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_EXTERNAL_TRANSLATION_1 = "LOG_EXTERNAL_TRANSLATION_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SYNCHRONIZE_EXPORT_FAILED_1 = "LOG_SYNCHRONIZE_EXPORT_FAILED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SYNCHRONIZE_UPDATE_FAILED_1 = "LOG_SYNCHRONIZE_UPDATE_FAILED_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_DEL_FILE_0 = "RPT_DEL_FILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_DEL_FOLDER_0 = "RPT_DEL_FOLDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_DEL_FS_FILE_0 = "RPT_DEL_FS_FILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_DEL_FS_FOLDER_0 = "RPT_DEL_FS_FOLDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_EXPORT_FILE_0 = "RPT_EXPORT_FILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_EXPORT_FOLDER_0 = "RPT_EXPORT_FOLDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_FROM_FS_TO_0 = "RPT_FROM_FS_TO_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_IMPORT_FILE_0 = "RPT_IMPORT_FILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_IMPORT_FOLDER_0 = "RPT_IMPORT_FOLDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_SKIPPING_0 = "RPT_SKIPPING_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_TO_FS_AS_0 = "RPT_TO_FS_AS_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATE_FILE_0 = "RPT_UPDATE_FILE_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.synchronize.messages";

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