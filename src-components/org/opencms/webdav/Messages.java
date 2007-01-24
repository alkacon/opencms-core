/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/webdav/Attic/Messages.java,v $
 * Date   : $Date: 2007/01/24 14:55:05 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.webdav;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Peter Bonrad 
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 6.5.6
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String DIRECTORY_FILENAME_0 = "DIRECTORY_FILENAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String DIRECTORY_LASTMODIFIED_0 = "DIRECTORY_LASTMODIFIED_0";

    /** Message constant for key in the resource bundle. */
    public static final String DIRECTORY_PARENT_1 = "DIRECTORY_PARENT_1";

    /** Message constant for key in the resource bundle. */
    public static final String DIRECTORY_SIZE_0 = "DIRECTORY_SIZE_0";

    /** Message constant for key in the resource bundle. */
    public static final String DIRECTORY_TITLE_1 = "DIRECTORY_TITLE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CLOSE_INPUT_STREAM_0 = "ERR_CLOSE_INPUT_STREAM_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CLOSE_READER_0 = "ERR_CLOSE_READER_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_DELETE_TEMP_FILE_0 = "ERR_DELETE_TEMP_FILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_DEST_PATH_EXISTS_1 = "ERR_DEST_PATH_EXISTS_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INIT_PARAM_MISSING_1 = "ERR_INIT_PARAM_MISSING_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_LIST_ITEMS_1 = "ERR_LIST_ITEMS_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MD5_NOT_AVAILABLE_0 = "ERR_MD5_NOT_AVAILABLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PARSE_DEST_HEADER_0 = "ERR_PARSE_DEST_HEADER_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_READ_INIT_PARAM_2 = "ERR_READ_INIT_PARAM_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_REP_CLASS_CONSTRUCT_1 = "ERR_REP_CLASS_CONSTRUCT_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_REP_CLASS_INSTANTIATE_1 = "ERR_REP_CLASS_INSTANTIATE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_REP_CLASS_NOT_FOUND_1 = "ERR_REP_CLASS_NOT_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SRC_DEST_EQUALS_0 = "ERR_SRC_DEST_EQUALS_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_COPY_2 = "LOG_COPY_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_COPY_SUCCESS_0 = "LOG_COPY_SUCCESS_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CREATE_SUCCESS_0 = "LOG_CREATE_SUCCESS_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DELETE_SUCCESS_0 = "LOG_DELETE_SUCCESS_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ITEM_EXISTS_1 = "LOG_ITEM_EXISTS_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ITEM_LOCKED_1 = "LOG_ITEM_LOCKED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ITEM_NOT_FOUND_1 = "LOG_ITEM_NOT_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MOVE_2 = "LOG_MOVE_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MOVE_SUCCESS_0 = "LOG_MOVE_SUCCESS_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SERVE_BYTES_2 = "LOG_SERVE_BYTES_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SERVE_ITEM_1 = "LOG_SERVE_ITEM_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SERVE_ITEM_CONTENT_LENGTH_1 = "LOG_SERVE_ITEM_CONTENT_LENGTH_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SERVE_ITEM_CONTENT_TYPE_1 = "LOG_SERVE_ITEM_CONTENT_TYPE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SERVE_ITEM_HEADER_1 = "LOG_SERVE_ITEM_HEADER_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_WEBDAV_READ_ONLY_0 = "LOG_WEBDAV_READ_ONLY_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.webdav.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /**
     * Hides the public constructor for this utility class.
     * <p>
     */
    private Messages() {

        // hide the constructor
    }

    /**
     * Returns an instance of this localized message accessor.
     * <p>
     * 
     * @return an instance of this localized message accessor
     */
    public static I_CmsMessageBundle get() {

        return INSTANCE;
    }

    /**
     * Returns the bundle name for this OpenCms package.
     * <p>
     * 
     * @return the bundle name for this OpenCms package
     */
    public String getBundleName() {

        return BUNDLE_NAME;
    }
}
