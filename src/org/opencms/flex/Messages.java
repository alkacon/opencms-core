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

package org.opencms.flex;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @since 6.0.0 
 */
public final class Messages extends A_CmsMessageBundle {

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.flex.messages";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_ADD_COOKIE_0 = "ERR_ADD_COOKIE_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_FLEXREQUESTDISPATCHER_CLASSCAST_EXCEPTION_1 = "ERR_FLEXREQUESTDISPATCHER_CLASSCAST_EXCEPTION_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_FLEXREQUESTDISPATCHER_ERROR_LOADING_CACHE_PROPERTIES_1 = "ERR_FLEXREQUESTDISPATCHER_ERROR_LOADING_CACHE_PROPERTIES_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_FLEXREQUESTDISPATCHER_ERROR_LOADING_RESOURCE_FROM_CACHE_1 = "ERR_FLEXREQUESTDISPATCHER_ERROR_LOADING_RESOURCE_FROM_CACHE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_FLEXREQUESTDISPATCHER_ERROR_READING_RESOURCE_1 = "ERR_FLEXREQUESTDISPATCHER_ERROR_READING_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_FLEXREQUESTDISPATCHER_INCLUSION_LOOP_1 = "ERR_FLEXREQUESTDISPATCHER_INCLUSION_LOOP_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_FLEXREQUESTDISPATCHER_VFS_ACCESS_EXCEPTION_0 = "ERR_FLEXREQUESTDISPATCHER_VFS_ACCESS_EXCEPTION_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_HEADER_IFMODIFIEDSINCE_FORMAT_3 = "ERR_HEADER_IFMODIFIEDSINCE_FORMAT_3";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_FLEXRESPONSE_URI_SYNTAX_EXCEPTION_0 = "ERR_FLEXRESPONSE_URI_SYNTAX_EXCEPTION_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_FLEXCACHE_CREATED_2 = "INIT_FLEXCACHE_CREATED_2";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_ADD_ENTRY_1 = "LOG_FLEXCACHE_ADD_ENTRY_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_ADD_ENTRY_WITH_VARIATION_2 = "LOG_FLEXCACHE_ADD_ENTRY_WITH_VARIATION_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_ADD_KEY_1 = "LOG_FLEXCACHE_ADD_KEY_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_ADDED_ENTRY_1 = "LOG_FLEXCACHE_ADDED_ENTRY_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_ADDED_ENTRY_FOR_RESOURCE_WITH_VARIATION_3 = "LOG_FLEXCACHE_ADDED_ENTRY_FOR_RESOURCE_WITH_VARIATION_3";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_CLEAR_0 = "LOG_FLEXCACHE_CLEAR_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_CLEAR_ALL_0 = "LOG_FLEXCACHE_CLEAR_ALL_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_CLEAR_HALF_2 = "LOG_FLEXCACHE_CLEAR_HALF_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_CLEAR_KEYS_AND_ENTRIES_0 = "LOG_FLEXCACHE_CLEAR_KEYS_AND_ENTRIES_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_CLEAR_OFFLINE_ENTRIES_0 = "LOG_FLEXCACHE_CLEAR_OFFLINE_ENTRIES_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_CLEAR_ONLINE_ENTRIES_0 = "LOG_FLEXCACHE_CLEAR_ONLINE_ENTRIES_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_CLEAR_ONLINE_KEYS_AND_ENTRIES_0 = "LOG_FLEXCACHE_CLEAR_ONLINE_KEYS_AND_ENTRIES_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_PURGED_JSP_REPOSITORY_0 = "LOG_FLEXCACHE_PURGED_JSP_REPOSITORY_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_RECEIVED_EVENT_CLEAR_CACHE_0 = "LOG_FLEXCACHE_RECEIVED_EVENT_CLEAR_CACHE_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_RECEIVED_EVENT_CLEAR_CACHE_PARTIALLY_0 = "LOG_FLEXCACHE_RECEIVED_EVENT_CLEAR_CACHE_PARTIALLY_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_RECEIVED_EVENT_PURGE_REPOSITORY_0 = "LOG_FLEXCACHE_RECEIVED_EVENT_PURGE_REPOSITORY_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_RESOURCE_NOT_CACHEABLE_0 = "LOG_FLEXCACHE_RESOURCE_NOT_CACHEABLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHE_WILL_PURGE_JSP_REPOSITORY_0 = "LOG_FLEXCACHE_WILL_PURGE_JSP_REPOSITORY_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHEENTRY_ADDED_ENTRY_1 = "LOG_FLEXCACHEENTRY_ADDED_ENTRY_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHEENTRY_COULD_NOT_WRITE_TO_RESPONSE_1 = "LOG_FLEXCACHEENTRY_COULD_NOT_WRITE_TO_RESPONSE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHEENTRY_ENTRY_COMPLETED_1 = "LOG_FLEXCACHEENTRY_ENTRY_COMPLETED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHEENTRY_REMOVED_ENTRY_FOR_VARIATION_1 = "LOG_FLEXCACHEENTRY_REMOVED_ENTRY_FOR_VARIATION_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHEENTRY_SET_EXPIRATION_DATE_3 = "LOG_FLEXCACHEENTRY_SET_EXPIRATION_DATE_3";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHEKEY_FOUND_1 = "LOG_FLEXCACHEKEY_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHEKEY_GENERATED_1 = "LOG_FLEXCACHEKEY_GENERATED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHEKEY_KEYMATCH_CACHE_ALWAYS_0 = "LOG_FLEXCACHEKEY_KEYMATCH_CACHE_ALWAYS_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHEKEY_KEYMATCH_CACHE_NEVER_0 = "LOG_FLEXCACHEKEY_KEYMATCH_CACHE_NEVER_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHEKEY_KEYMATCH_CHECK_NO_ATTRS_0 = "LOG_FLEXCACHEKEY_KEYMATCH_CHECK_NO_ATTRS_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHEKEY_KEYMATCH_CHECK_NO_PARAMS_0 = "LOG_FLEXCACHEKEY_KEYMATCH_CHECK_NO_PARAMS_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHEKEY_NOT_FOUND_1 = "LOG_FLEXCACHEKEY_NOT_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHEKEY_PARSE_ERROR_1 = "LOG_FLEXCACHEKEY_PARSE_ERROR_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHEKEY_PARSE_FLEXKEY_3 = "LOG_FLEXCACHEKEY_PARSE_FLEXKEY_3";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCACHEKEY_PARSE_VALUES_1 = "LOG_FLEXCACHEKEY_PARSE_VALUES_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCONTROLLER_IGNORED_EXCEPTION_0 = "LOG_FLEXCONTROLLER_IGNORED_EXCEPTION_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXCONTROLLER_IGNORED_EXCEPTION_1 = "LOG_FLEXCONTROLLER_IGNORED_EXCEPTION_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXREQUEST_CREATED_NEW_REQUEST_1 = "LOG_FLEXREQUEST_CREATED_NEW_REQUEST_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXREQUEST_REUSING_FLEX_REQUEST_1 = "LOG_FLEXREQUEST_REUSING_FLEX_REQUEST_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXREQUESTDISPATCHER_ADDING_CACHE_PROPERTIES_2 = "LOG_FLEXREQUESTDISPATCHER_ADDING_CACHE_PROPERTIES_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXREQUESTDISPATCHER_INCLUDE_RESOURCE_1 = "LOG_FLEXREQUESTDISPATCHER_INCLUDE_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXREQUESTDISPATCHER_INCLUDING_EXTERNAL_TARGET_1 = "LOG_FLEXREQUESTDISPATCHER_INCLUDING_EXTERNAL_TARGET_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXREQUESTDISPATCHER_INCLUDING_TARGET_2 = "LOG_FLEXREQUESTDISPATCHER_INCLUDING_TARGET_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXREQUESTDISPATCHER_INVALID_CACHE_KEY_2 = "LOG_FLEXREQUESTDISPATCHER_INVALID_CACHE_KEY_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXREQUESTDISPATCHER_LOADING_RESOURCE_FROM_CACHE_1 = "LOG_FLEXREQUESTDISPATCHER_LOADING_RESOURCE_FROM_CACHE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXREQUESTDISPATCHER_LOADING_RESOURCE_TYPE_1 = "LOG_FLEXREQUESTDISPATCHER_LOADING_RESOURCE_TYPE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXREQUESTDISPATCHER_RESULT_1 = "LOG_FLEXREQUESTDISPATCHER_RESULT_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXREQUESTKEY_CREATED_NEW_KEY_1 = "LOG_FLEXREQUESTKEY_CREATED_NEW_KEY_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXRESPONSE_ADDING_HEADER_TO_ELEMENT_BUFFER_2 = "LOG_FLEXRESPONSE_ADDING_HEADER_TO_ELEMENT_BUFFER_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXRESPONSE_ADDING_HEADER_TO_HEADERS_2 = "LOG_FLEXRESPONSE_ADDING_HEADER_TO_HEADERS_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXRESPONSE_ADDING_HEADER_TO_PARENT_RESPONSE_2 = "LOG_FLEXRESPONSE_ADDING_HEADER_TO_PARENT_RESPONSE_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXRESPONSE_ERROR_FLUSHING_OUTPUT_STREAM_1 = "LOG_FLEXRESPONSE_ERROR_FLUSHING_OUTPUT_STREAM_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXRESPONSE_ERROR_OUTPUT_STREAM_NULL_0 = "LOG_FLEXRESPONSE_ERROR_OUTPUT_STREAM_NULL_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXRESPONSE_ERROR_WRITING_TO_OUTPUT_STREAM_0 = "LOG_FLEXRESPONSE_ERROR_WRITING_TO_OUTPUT_STREAM_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXRESPONSE_FLUSHED_1 = "LOG_FLEXRESPONSE_FLUSHED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXRESPONSE_PARSE_ERROR_IN_CACHE_KEY_2 = "LOG_FLEXRESPONSE_PARSE_ERROR_IN_CACHE_KEY_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXRESPONSE_REDIRECTWARNING_3 = "LOG_FLEXRESPONSE_REDIRECTWARNING_3";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXRESPONSE_SENDREDIRECT_1 = "LOG_FLEXRESPONSE_SENDREDIRECT_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXRESPONSE_SETTING_CONTENTTYPE_1 = "LOG_FLEXRESPONSE_SETTING_CONTENTTYPE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXRESPONSE_SETTING_HEADER_IN_ELEMENT_BUFFER_2 = "LOG_FLEXRESPONSE_SETTING_HEADER_IN_ELEMENT_BUFFER_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXRESPONSE_SETTING_HEADER_IN_HEADERS_2 = "LOG_FLEXRESPONSE_SETTING_HEADER_IN_HEADERS_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXRESPONSE_SETTING_HEADER_IN_PARENT_RESPONSE_2 = "LOG_FLEXRESPONSE_SETTING_HEADER_IN_PARENT_RESPONSE_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_FLEXRESPONSE_TOPRESPONSE_SENDREDIRECT_1 = "LOG_FLEXRESPONSE_TOPRESPONSE_SENDREDIRECT_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CLASS_INIT_FAILURE_1 = "LOG_CLASS_INIT_FAILURE_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_FLEXCACHE_DEVICE_SELECTOR_FAILURE_1 = "INIT_FLEXCACHE_DEVICE_SELECTOR_FAILURE_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_FLEXCACHE_DEVICE_SELECTOR_SUCCESS_1 = "INIT_FLEXCACHE_DEVICE_SELECTOR_SUCCESS_1";

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
