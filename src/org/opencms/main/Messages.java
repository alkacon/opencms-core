/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/Messages.java,v $
 * Date   : $Date: 2005/05/31 14:39:21 $
 * Version: $Revision: 1.14 $
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

package org.opencms.main;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.7.3
 */
public final class Messages extends A_CmsMessageBundle {

    /** The copyright message for OpenCms. */
    public static final String[] COPYRIGHT_BY_ALKACON = {
        "",
        "Copyright (c) 2002-2005 Alkacon Software",
        "OpenCms comes with ABSOLUTELY NO WARRANTY",
        "This is free software, and you are welcome to",
        "redistribute it under certain conditions.",
        "Please see the GNU Lesser General Public Licence for",
        "further details.",
        ""};

    /** Message contant for key in the resource bundle. */
    public static final String ERR_ALREADY_INITIALIZED_0 = "ERR_ALREADY_INITIALIZED_0";
    
    /** Message contant for key in the resource bundle. */
    public static final String ERR_CONTEXT_INFO_FROZEN_0 = "ERR_CONTEXT_INFO_FROZEN_0";
   
    /** Message contant for key in the resource bundle. */
    public static final String ERR_CRITICAL_CLASS_CREATION_1 = "ERR_CRITICAL_CLASS_CREATION_1";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_ADMINCMS_0 = "ERR_CRITICAL_INIT_ADMINCMS_0";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_DATABASE_0 = "ERR_CRITICAL_INIT_DATABASE_0";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_ENCODING_1 = "ERR_CRITICAL_INIT_ENCODING_1";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_FOLDER_0 = "ERR_CRITICAL_INIT_FOLDER_0";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_GENERIC_1 = "ERR_CRITICAL_INIT_GENERIC_1";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_MANAGERS_0 = "ERR_CRITICAL_INIT_MANAGERS_0";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_PROP_0 = "ERR_CRITICAL_INIT_PROP_0";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_PROPFILE_1 = "ERR_CRITICAL_INIT_PROPFILE_1";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_SERVLET_0 = "ERR_CRITICAL_INIT_SERVLET_0";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_WIZARD_0 = "ERR_CRITICAL_INIT_WIZARD_0";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_XML_0 = "ERR_CRITICAL_INIT_XML_0";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_CRITICAL_NO_DB_CONTEXT_0 = "ERR_CRITICAL_NO_DB_CONTEXT_0";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_ILLEGAL_ARG_2 = "ERR_ILLEGAL_ARG_2";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_ILLEGAL_ARG_3 = "ERR_ILLEGAL_ARG_3";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_ILLEGAL_ARG_4 = "ERR_ILLEGAL_ARG_4";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_ILLEGAL_ARG_5 = "ERR_ILLEGAL_ARG_5";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_INVALID_INIT_USER_2 = "ERR_INVALID_INIT_USER_2";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_READ_INTERNAL_RESOURCE_1 = "ERR_READ_INTERNAL_RESOURCE_1";

    /** Message contant for key in the resource bundle. */
    public static final String ERR_SHOW_ERR_HANDLER_RESOURCE_2 = "ERR_SHOW_ERR_HANDLER_RESOURCE_2";
    
    /** Message contant for key in the resource bundle. */
    public static final String ERR_UNKNOWN_MODULE_1 = "ERR_UNKNOWN_MODULE_1";
    
    /** Message contant for key in the resource bundle. */
    public static final String INIT_ADDED_REQUEST_HANDLER_2 = "INIT_ADDED_REQUEST_HANDLER_2";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_CURRENT_RUNLEVEL_1 = "INIT_CURRENT_RUNLEVEL_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_DOT_0 = "INIT_DOT_0";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_ETHERNET_ADDRESS_1 = "INIT_ETHERNET_ADDRESS_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_FILE_ENCODING_1 = "INIT_FILE_ENCODING_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_FLEX_CACHE_ERROR_1 = "INIT_FLEX_CACHE_ERROR_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_FLEX_CACHE_FINISHED_0 = "INIT_FLEX_CACHE_FINISHED_0";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_FLEX_CACHE_STARTING_0 = "INIT_FLEX_CACHE_STARTING_0";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_JAVA_VM_1 = "INIT_JAVA_VM_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_LINE_0 = "INIT_LINE_0";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_LOG_FILE_1 = "INIT_LOG_FILE_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_OPENCMS_CONTEXT_1 = "INIT_OPENCMS_CONTEXT_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_OPENCMS_ENCODING_1 = "INIT_OPENCMS_ENCODING_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_OPENCMS_STOPPED_1 = "INIT_OPENCMS_STOPPED_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_OPENCMS_VERSION_1 = "INIT_OPENCMS_VERSION_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_OPERATING_SYSTEM_1 = "INIT_OPERATING_SYSTEM_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_PROPERTY_FILE_1 = "INIT_PROPERTY_FILE_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_REQUEST_HANDLER_CLASS_1 = "INIT_REQUEST_HANDLER_CLASS_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_RUNLEVEL_CHANGE_2 = "INIT_RUNLEVEL_CHANGE_2";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_SERVLET_CONTAINER_1 = "INIT_SERVLET_CONTAINER_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_SERVLET_PATH_1 = "INIT_SERVLET_PATH_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_SHUTDOWN_START_1 = "INIT_SHUTDOWN_START_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_SHUTDOWN_TIME_1 = "INIT_SHUTDOWN_TIME_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_STARTUP_TIME_1 = "INIT_STARTUP_TIME_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_SYSTEM_RUNNING_1 = "INIT_SYSTEM_RUNNING_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_WEBAPP_NAME_1 = "INIT_WEBAPP_NAME_1";

    /** Message contant for key in the resource bundle. */
    public static final String INIT_WEBINF_PATH_1 = "INIT_WEBINF_PATH_1";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_AUTHENTICATE_PROPERTY_2 = "LOG_AUTHENTICATE_PROPERTY_2";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_CONSOLE_TOTAL_RUNTIME_1 = "LOG_CONSOLE_TOTAL_RUNTIME_1";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_DUPLICATE_REQUEST_HANDLER_1 = "LOG_DUPLICATE_REQUEST_HANDLER_1";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_ERROR_EXPORT_SHUTDOWN_1 = "LOG_ERROR_EXPORT_SHUTDOWN_1";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_ERROR_GENERIC_0 = "LOG_ERROR_GENERIC_0";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_ERROR_MODULE_SHUTDOWN_1 = "LOG_ERROR_MODULE_SHUTDOWN_1";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_ERROR_READING_AUTH_PROP_2 = "LOG_ERROR_READING_AUTH_PROP_2";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_ERROR_SCHEDULE_SHUTDOWN_1 = "LOG_ERROR_SCHEDULE_SHUTDOWN_1";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_ERROR_SECURITY_SHUTDOWN_1 = "LOG_ERROR_SECURITY_SHUTDOWN_1";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_ERROR_THREAD_SHUTDOWN_1 = "LOG_ERROR_THREAD_SHUTDOWN_1";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_ERROR_WRITING_CONFIG_1 = "LOG_ERROR_WRITING_CONFIG_1";

/** Message contant for key in the resource bundle. */
public static final String LOG_INIT_CMSOBJECT_IN_ERROR_HANDLER_2 = "LOG_INIT_CMSOBJECT_IN_ERROR_HANDLER_2";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_INIT_FAILURE_MESSAGE_1 = "LOG_INIT_FAILURE_MESSAGE_1";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_INIT_INVALID_ERROR_2 = "LOG_INIT_INVALID_ERROR_2";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_SET_DEFAULT_ENCODING_1 = "LOG_SET_DEFAULT_ENCODING_1";
        
    /** Message contant for key in the resource bundle. */
    public static final String LOG_SET_SERVERNAME_1 = "LOG_SET_SERVERNAME_1";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_SHUTDOWN_CONSOLE_NOTE_2 = "LOG_SHUTDOWN_CONSOLE_NOTE_2";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_SHUTDOWN_TRACE_0 = "LOG_SHUTDOWN_TRACE_0";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_STARTUP_CONSOLE_NOTE_2 = "LOG_STARTUP_CONSOLE_NOTE_2";

    /** Message contant for key in the resource bundle. */
    public static final String LOG_WRONG_INIT_SEQUENCE_2 = "LOG_WRONG_INIT_SEQUENCE_2";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.main.messages";

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