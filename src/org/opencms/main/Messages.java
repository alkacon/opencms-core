/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.main;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @since 6.0.0 
 */
public final class Messages extends A_CmsMessageBundle {

    /** The copyright message for OpenCms. */
    public static final String[] COPYRIGHT_BY_ALKACON = {
        "",
        "Copyright (c) 2014 Alkacon Software GmbH",
        "OpenCms comes with ABSOLUTELY NO WARRANTY",
        "This is free software, and you are welcome to",
        "redistribute it under certain conditions.",
        "Please see the GNU Lesser General Public Licence for",
        "further details.",
        ""};

    /** Message constant for key in the resource bundle. */
    public static final String ERR_ALREADY_INITIALIZED_0 = "ERR_ALREADY_INITIALIZED_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CONTEXT_INFO_FROZEN_0 = "ERR_CONTEXT_INFO_FROZEN_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CRITICAL_CLASS_CREATION_1 = "ERR_CRITICAL_CLASS_CREATION_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_ADMINCMS_0 = "ERR_CRITICAL_INIT_ADMINCMS_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_DATABASE_0 = "ERR_CRITICAL_INIT_DATABASE_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_ENCODING_1 = "ERR_CRITICAL_INIT_ENCODING_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_FOLDER_0 = "ERR_CRITICAL_INIT_FOLDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_GENERIC_1 = "ERR_CRITICAL_INIT_GENERIC_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_MANAGERS_0 = "ERR_CRITICAL_INIT_MANAGERS_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_MEMORY_MONITOR_1 = "ERR_CRITICAL_INIT_MEMORY_MONITOR_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_PROP_0 = "ERR_CRITICAL_INIT_PROP_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_PROPFILE_1 = "ERR_CRITICAL_INIT_PROPFILE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_SERVLET_0 = "ERR_CRITICAL_INIT_SERVLET_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_WIZARD_0 = "ERR_CRITICAL_INIT_WIZARD_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CRITICAL_INIT_XML_0 = "ERR_CRITICAL_INIT_XML_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CRITICAL_NO_DB_CONTEXT_0 = "ERR_CRITICAL_NO_DB_CONTEXT_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_DEREGISTERING_JDBC_DRIVER_1 = "ERR_DEREGISTERING_JDBC_DRIVER_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_ILLEGAL_ARG_2 = "ERR_ILLEGAL_ARG_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INVALID_INIT_USER_2 = "ERR_INVALID_INIT_USER_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MULTI_EXCEPTION_1 = "ERR_MULTI_EXCEPTION_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_NO_SESSIONINFO_SESSION_0 = "ERR_NO_SESSIONINFO_SESSION_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_NO_WORKPLACE_PERMISSIONS_0 = "ERR_NO_WORKPLACE_PERMISSIONS_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_NOT_A_FOLDER_1 = "ERR_NOT_A_FOLDER_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_OPENCMS_NOT_INITIALIZED_2 = "ERR_OPENCMS_NOT_INITIALIZED_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PATH_NOT_FOUND_1 = "ERR_PATH_NOT_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PERMALINK_1 = "ERR_PERMALINK_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_READ_INTERNAL_RESOURCE_1 = "ERR_READ_INTERNAL_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_REQUEST_SECURE_RESOURCE_0 = "ERR_REQUEST_SECURE_RESOURCE_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_RESOURCE_INIT_ABORTED_1 = "ERR_RESOURCE_INIT_ABORTED_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SECURE_SITE_NOT_CONFIGURED_1 = "ERR_SECURE_SITE_NOT_CONFIGURED_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SHOW_ERR_HANDLER_RESOURCE_2 = "ERR_SHOW_ERR_HANDLER_RESOURCE_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UNKNOWN_MODULE_1 = "ERR_UNKNOWN_MODULE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_AVAILABLE_METHODS_1 = "GUI_SHELL_AVAILABLE_METHODS_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_CONFIG_FILE_1 = "GUI_SHELL_CONFIG_FILE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_CURRENT_FOLDER_1 = "GUI_SHELL_CURRENT_FOLDER_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_ECHO_OFF_0 = "GUI_SHELL_ECHO_OFF_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_ECHO_ON_0 = "GUI_SHELL_ECHO_ON_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_ERR_ADDITIONAL_COMMANDS_1 = "GUI_SHELL_ERR_ADDITIONAL_COMMANDS_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_ERR_SCRIPTFILE_1 = "GUI_SHELL_ERR_SCRIPTFILE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_EXEC_METHOD_1 = "GUI_SHELL_EXEC_METHOD_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_GOODBYE_0 = "GUI_SHELL_GOODBYE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_HELP1_0 = "GUI_SHELL_HELP1_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_HELP2_0 = "GUI_SHELL_HELP2_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_HELP3_0 = "GUI_SHELL_HELP3_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_HELP4_0 = "GUI_SHELL_HELP4_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_HR_0 = "GUI_SHELL_HR_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_IMPORT_TEMP_PROJECT_NAME_0 = "GUI_SHELL_IMPORT_TEMP_PROJECT_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_IMPORTEXPORT_MODULE_HANDLER_NAME_1 = "GUI_SHELL_IMPORTEXPORT_MODULE_HANDLER_NAME_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_LIST_MODULES_1 = "GUI_SHELL_LIST_MODULES_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_LOCALES_AVAILABLE_0 = "GUI_SHELL_LOCALES_AVAILABLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_LOGIN_1 = "GUI_SHELL_LOGIN_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_LOGIN_FAILED_0 = "GUI_SHELL_LOGIN_FAILED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_LS_2 = "GUI_SHELL_LS_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_MATCH_SEARCHSTRING_1 = "GUI_SHELL_MATCH_SEARCHSTRING_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_METHOD_NOT_FOUND_1 = "GUI_SHELL_METHOD_NOT_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_NO_HOME_FOLDER_FOUND_0 = "GUI_SHELL_NO_HOME_FOLDER_FOUND_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_NO_HOME_FOLDER_SPECIFIED_0 = "GUI_SHELL_NO_HOME_FOLDER_SPECIFIED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_SETLOCALE_2 = "GUI_SHELL_SETLOCALE_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_SETLOCALE_POST_1 = "GUI_SHELL_SETLOCALE_POST_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_START_DIR_LINE1_0 = "GUI_SHELL_START_DIR_LINE1_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_START_DIR_LINE2_0 = "GUI_SHELL_START_DIR_LINE2_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_USAGE_1 = "GUI_SHELL_USAGE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_VERSION_1 = "GUI_SHELL_VERSION_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_WEB_INF_PATH_1 = "GUI_SHELL_WEB_INF_PATH_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_WELCOME_0 = "GUI_SHELL_WELCOME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHELL_WRONG_USAGE_0 = "GUI_SHELL_WRONG_USAGE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SOLR_ERROR_HTML_1 = "GUI_SOLR_ERROR_HTML_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SOLR_INDEX_NOT_FOUND_1 = "GUI_SOLR_INDEX_NOT_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SOLR_NOT_LOGGED_IN_0 = "GUI_SOLR_NOT_LOGGED_IN_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SOLR_UNEXPECTED_ERROR_0 = "GUI_SOLR_UNEXPECTED_ERROR_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_ADDED_REQUEST_HANDLER_2 = "INIT_ADDED_REQUEST_HANDLER_2";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_CURRENT_RUNLEVEL_1 = "INIT_CURRENT_RUNLEVEL_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_DOT_0 = "INIT_DOT_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_ERR_LOAD_HTML_PROPERTY_FILE_1 = "INIT_ERR_LOAD_HTML_PROPERTY_FILE_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_ETHERNET_ADDRESS_1 = "INIT_ETHERNET_ADDRESS_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_FILE_ENCODING_1 = "INIT_FILE_ENCODING_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_FLEX_CACHE_ERROR_1 = "INIT_FLEX_CACHE_ERROR_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_FLEX_CACHE_FINISHED_0 = "INIT_FLEX_CACHE_FINISHED_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_FLEX_CACHE_STARTING_0 = "INIT_FLEX_CACHE_STARTING_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_JAVA_VM_1 = "INIT_JAVA_VM_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_LINE_0 = "INIT_LINE_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_LOG_FILE_1 = "INIT_LOG_FILE_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_OPENCMS_CONTEXT_1 = "INIT_OPENCMS_CONTEXT_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_OPENCMS_ENCODING_1 = "INIT_OPENCMS_ENCODING_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_OPENCMS_STOPPED_1 = "INIT_OPENCMS_STOPPED_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_OPENCMS_VERSION_1 = "INIT_OPENCMS_VERSION_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_OPERATING_SYSTEM_1 = "INIT_OPERATING_SYSTEM_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_PROPERTY_FILE_1 = "INIT_PROPERTY_FILE_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_REQUEST_HANDLER_CLASS_1 = "INIT_REQUEST_HANDLER_CLASS_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_RUNLEVEL_CHANGE_2 = "INIT_RUNLEVEL_CHANGE_2";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_SERVLET_CONTAINER_1 = "INIT_SERVLET_CONTAINER_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_SERVLET_PATH_1 = "INIT_SERVLET_PATH_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_SHUTDOWN_START_1 = "INIT_SHUTDOWN_START_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_SHUTDOWN_TIME_1 = "INIT_SHUTDOWN_TIME_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_STARTUP_TIME_1 = "INIT_STARTUP_TIME_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_SYSTEM_RUNNING_1 = "INIT_SYSTEM_RUNNING_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_WEBAPP_NAME_1 = "INIT_WEBAPP_NAME_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_WEBINF_PATH_1 = "INIT_WEBINF_PATH_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CONSOLE_TOTAL_RUNTIME_1 = "LOG_CONSOLE_TOTAL_RUNTIME_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DEBUG_EVENT_1 = "LOG_DEBUG_EVENT_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DEBUG_EVENT_COMPLETE_1 = "LOG_DEBUG_EVENT_COMPLETE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DEBUG_EVENT_END_LISTENER_3 = "LOG_DEBUG_EVENT_END_LISTENER_3";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DEBUG_EVENT_LISTENERS_3 = "LOG_DEBUG_EVENT_LISTENERS_3";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DEBUG_EVENT_NO_LISTENER_1 = "LOG_DEBUG_EVENT_NO_LISTENER_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DEBUG_EVENT_START_LISTENER_3 = "LOG_DEBUG_EVENT_START_LISTENER_3";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DEBUG_EVENT_VALUE_3 = "LOG_DEBUG_EVENT_VALUE_3";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DEBUG_NO_EVENT_VALUE_1 = "LOG_DEBUG_NO_EVENT_VALUE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DUPLICATE_REQUEST_HANDLER_1 = "LOG_DUPLICATE_REQUEST_HANDLER_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_ADE_MANAGER_SHUTDOWN_1 = "LOG_ERROR_ADE_MANAGER_SHUTDOWN_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_DERIGISTERING_JDBC_DRIVER_1 = "LOG_ERROR_DERIGISTERING_JDBC_DRIVER_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_EXPORT_1 = "LOG_ERROR_EXPORT_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_EXPORT_SHUTDOWN_1 = "LOG_ERROR_EXPORT_SHUTDOWN_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_GENERIC_0 = "LOG_ERROR_GENERIC_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_GWTSERVICE_SHUTDOWN_2 = "LOG_ERROR_GWTSERVICE_SHUTDOWN_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_MEMORY_MONITOR_SHUTDOWN_1 = "LOG_ERROR_MEMORY_MONITOR_SHUTDOWN_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_MODULE_SHUTDOWN_1 = "LOG_ERROR_MODULE_SHUTDOWN_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_PUBLISH_SHUTDOWN_1 = "LOG_ERROR_PUBLISH_SHUTDOWN_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_READING_AUTH_PROP_2 = "LOG_ERROR_READING_AUTH_PROP_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_RESOURCE_SHUTDOWN_1 = "LOG_ERROR_RESOURCE_SHUTDOWN_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_SCHEDULE_SHUTDOWN_1 = "LOG_ERROR_SCHEDULE_SHUTDOWN_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_SEARCH_MANAGER_SHUTDOWN_1 = "LOG_ERROR_SEARCH_MANAGER_SHUTDOWN_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_SECURITY_SHUTDOWN_1 = "LOG_ERROR_SECURITY_SHUTDOWN_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_SESSION_MANAGER_SHUTDOWN_1 = "LOG_ERROR_SESSION_MANAGER_SHUTDOWN_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_SITEMAP_MANAGER_SHUTDOWN_1 = "LOG_ERROR_SITEMAP_MANAGER_SHUTDOWN_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_THREAD_SHUTDOWN_1 = "LOG_ERROR_THREAD_SHUTDOWN_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_VFSBUNDLE_MANAGER_SHUTDOWN_1 = "LOG_ERROR_VFSBUNDLE_MANAGER_SHUTDOWN_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_WRITING_CONFIG_1 = "LOG_ERROR_WRITING_CONFIG_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_INIT_CMSOBJECT_IN_HANDLER_2 = "LOG_INIT_CMSOBJECT_IN_HANDLER_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_INIT_CONTEXTNAME_0 = "LOG_INIT_CONTEXTNAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_INIT_FAILURE_MESSAGE_1 = "LOG_INIT_FAILURE_MESSAGE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_INIT_INVALID_ERROR_2 = "LOG_INIT_INVALID_ERROR_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SESSION_CREATED_1 = "LOG_SESSION_CREATED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SESSION_CREATED_2 = "LOG_SESSION_CREATED_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SESSION_DESTROYED_1 = "LOG_SESSION_DESTROYED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SESSION_DESTROYED_2 = "LOG_SESSION_DESTROYED_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_DEFAULT_ENCODING_1 = "LOG_SET_DEFAULT_ENCODING_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_SERVERNAME_1 = "LOG_SET_SERVERNAME_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SHUTDOWN_CONSOLE_NOTE_2 = "LOG_SHUTDOWN_CONSOLE_NOTE_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SHUTDOWN_TRACE_0 = "LOG_SHUTDOWN_TRACE_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_STARTUP_CONSOLE_NOTE_2 = "LOG_STARTUP_CONSOLE_NOTE_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_THREADSTORE_CHECK_PUBLISH_THREAD_ERROR_0 = "LOG_THREADSTORE_CHECK_PUBLISH_THREAD_ERROR_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_THREADSTORE_CHECK_SESSIONS_ERROR_0 = "LOG_THREADSTORE_CHECK_SESSIONS_ERROR_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_THREADSTORE_CHECK_THREADS_ERROR_0 = "LOG_THREADSTORE_CHECK_THREADS_ERROR_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_THREADSTORE_DOOMED_2 = "LOG_THREADSTORE_DOOMED_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_THREADSTORE_POOL_CONTENT_2 = "LOG_THREADSTORE_POOL_CONTENT_2";

    /** Message constant for key in the resource bundle. */
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
