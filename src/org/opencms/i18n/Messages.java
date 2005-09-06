/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/Messages.java,v $
 * Date   : $Date: 2005/09/06 15:32:58 $
 * Version: $Revision: 1.9 $
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

package org.opencms.i18n;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Achim Westermann 
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.0.0 
 */
public final class Messages extends A_CmsMessageBundle {
    
    /** Message constant for key in the resource bundle. */
    public static final String ERR_ARGUMENT_1 = "ERR_ARGUMENT_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CANT_FIND_RESOURCE_FOR_BUNDLE_2 = "ERR_CANT_FIND_RESOURCE_FOR_BUNDLE_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_ENCODING_ISSUES_1 = "ERR_ENCODING_ISSUES_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MESSAGE_BUNDLE_NOT_INITIALIZED_1 = "ERR_MESSAGE_BUNDLE_NOT_INITIALIZED_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MULTIMSG_EMPTY_LIST_0 = "ERR_MULTIMSG_EMPTY_LIST_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MULTIMSG_LOCALE_DOES_NOT_MATCH_2 = "ERR_MULTIMSG_LOCALE_DOES_NOT_MATCH_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_READ_ENCODING_PROP_1 = "ERR_READ_ENCODING_PROP_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_RESOURCE_BUNDLE_NOT_FOUND_1 = "ERR_RESOURCE_BUNDLE_NOT_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UNSUPPORTED_REQUEST_ENCODING_1 = "ERR_UNSUPPORTED_REQUEST_ENCODING_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UNSUPPORTED_VM_ENCODING_1 = "ERR_UNSUPPORTED_VM_ENCODING_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_I18N_CONFIG_ADD_LOCALE_1 = "INIT_I18N_CONFIG_ADD_LOCALE_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_I18N_CONFIG_DEFAULT_LOCALE_2 = "INIT_I18N_CONFIG_DEFAULT_LOCALE_2";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_I18N_CONFIG_LOC_HANDLER_1 = "INIT_I18N_CONFIG_LOC_HANDLER_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_I18N_CONFIG_START_0 = "INIT_I18N_CONFIG_START_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_I18N_CONFIG_VFSACCESS_0 = "INIT_I18N_CONFIG_VFSACCESS_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_I18N_DEFAULT_LOCALE_2 = "INIT_I18N_DEFAULT_LOCALE_2";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_I18N_KEEPING_DEFAULT_LOCALE_1 = "INIT_I18N_KEEPING_DEFAULT_LOCALE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CREATE_LOCALE_FAILED_1 = "LOG_CREATE_LOCALE_FAILED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ENCODING_NOT_FOUND_1 = "LOG_ENCODING_NOT_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_LOCALE_MANAGER_FLUSH_CACHE_1 = "LOG_LOCALE_MANAGER_FLUSH_CACHE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_LOCALE_NOT_FOUND_1 = "LOG_LOCALE_NOT_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MESSAGE_KEY_FOUND_2 = "LOG_MESSAGE_KEY_FOUND_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MESSAGE_KEY_FOUND_CACHED_2 = "LOG_MESSAGE_KEY_FOUND_CACHED_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MESSAGE_KEY_NOT_FOUND_1 = "LOG_MESSAGE_KEY_NOT_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_RESOLVE_MESSAGE_KEY_1 = "LOG_RESOLVE_MESSAGE_KEY_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_UNABLE_TO_SET_DEFAULT_LOCALE_2 = "LOG_UNABLE_TO_SET_DEFAULT_LOCALE_2";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.i18n.messages";

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
