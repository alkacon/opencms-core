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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace;

import org.opencms.db.CmsResourceState;
import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 6.0.0
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_DEFAULT_TEMPLATE_ADE_WARNING_0 = "ERR_DEFAULT_TEMPLATE_ADE_WARNING_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_DEFAULT_TEMPLATE_WARNING_0 = "ERR_DEFAULT_TEMPLATE_WARNING_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INITIALIZE_WORKPLACE_0 = "ERR_INITIALIZE_WORKPLACE_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_JSON_MISSING_PARAMETER_1 = "ERR_JSON_MISSING_PARAMETER_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PROJECT_NOT_ACCESSIBLE_2 = "ERR_PROJECT_NOT_ACCESSIBLE_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SERVER_EXCEPTION_1 = "ERR_SERVER_EXCEPTION_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SITE_NOT_ACCESSIBLE_2 = "ERR_SITE_NOT_ACCESSIBLE_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_WORKPLACE_DIALOG_0 = "ERR_WORKPLACE_DIALOG_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_WORKPLACE_DIALOG_PARAMS_1 = "ERR_WORKPLACE_DIALOG_PARAMS_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_WORKPLACE_LOCK_RESOURCE_1 = "ERR_WORKPLACE_LOCK_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_WORKPLACE_SERVER_CHECK_FAILED_0 = "ERR_WORKPLACE_SERVER_CHECK_FAILED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_AJAX_REPORT_ERROR_0 = "GUI_AJAX_REPORT_ERROR_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_AJAX_REPORT_GIVEUP_0 = "GUI_AJAX_REPORT_GIVEUP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_AJAX_REPORT_WAIT_0 = "GUI_AJAX_REPORT_WAIT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_EXIT_0 = "GUI_BUTTON_EXIT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_HELP_0 = "GUI_BUTTON_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_PREFERENCES_0 = "GUI_BUTTON_PREFERENCES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_PUBLISH_0 = "GUI_BUTTON_PUBLISH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_PUBLISHQUEUE_0 = "GUI_BUTTON_PUBLISHQUEUE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_RELOAD_0 = "GUI_BUTTON_RELOAD_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_SYNCFOLDER_0 = "GUI_BUTTON_SYNCFOLDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CALENDAR_CHOOSE_DATE_0 = "GUI_CALENDAR_CHOOSE_DATE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CALENDAR_DATE_FORMAT_0 = "GUI_CALENDAR_DATE_FORMAT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CALENDAR_TIME_FORMAT_0 = "GUI_CALENDAR_TIME_FORMAT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CALENDAR_TIMEFORMAT_0 = "GUI_CALENDAR_TIMEFORMAT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_ADDNEW_0 = "GUI_DIALOG_BUTTON_ADDNEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_ADVANCED_0 = "GUI_DIALOG_BUTTON_ADVANCED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_BACK_0 = "GUI_DIALOG_BUTTON_BACK_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_CANCEL_0 = "GUI_DIALOG_BUTTON_CANCEL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_CLOSE_0 = "GUI_DIALOG_BUTTON_CLOSE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_CONTINUE_0 = "GUI_DIALOG_BUTTON_CONTINUE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_DELETE_0 = "GUI_DIALOG_BUTTON_DELETE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_DETAIL_0 = "GUI_DIALOG_BUTTON_DETAIL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_DISCARD_0 = "GUI_DIALOG_BUTTON_DISCARD_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_DOWNLOAD_0 = "GUI_DIALOG_BUTTON_DOWNLOAD_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_EDIT_0 = "GUI_DIALOG_BUTTON_EDIT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_OK_0 = "GUI_DIALOG_BUTTON_OK_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_RESET_0 = "GUI_DIALOG_BUTTON_RESET_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_SEARCH_0 = "GUI_DIALOG_BUTTON_SEARCH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DIALOG_BUTTON_SET_0 = "GUI_DIALOG_BUTTON_SET_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDITOR_WIDGET_OPTIONALELEMENT_0 = "GUI_EDITOR_WIDGET_OPTIONALELEMENT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDITOR_WIDGET_VALIDATION_ERROR_2 = "GUI_EDITOR_WIDGET_VALIDATION_ERROR_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDITOR_WIDGET_VALIDATION_ERROR_TITLE_0 = "GUI_EDITOR_WIDGET_VALIDATION_ERROR_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDITOR_WIDGET_VALIDATION_WARNING_2 = "GUI_EDITOR_WIDGET_VALIDATION_WARNING_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_ERR_RESOURCE_PERMISSIONS_2 = "GUI_ERR_RESOURCE_PERMISSIONS_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_STATE0_0 = "GUI_EXPLORER_STATE0_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_STATE1_0 = "GUI_EXPLORER_STATE1_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_STATE2_0 = "GUI_EXPLORER_STATE2_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_STATE3_0 = "GUI_EXPLORER_STATE3_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_STATENIP_0 = "GUI_EXPLORER_STATENIP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_BROADCAST_FROM_SYSTEM_0 = "GUI_LABEL_BROADCAST_FROM_SYSTEM_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_BROADCASTMESSAGEFROM_0 = "GUI_LABEL_BROADCASTMESSAGEFROM_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_LOGINADDRESS_0 = "GUI_LABEL_LOGINADDRESS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_LOGINTIME_0 = "GUI_LABEL_LOGINTIME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_OU_0 = "GUI_LABEL_OU_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_PROJECT_0 = "GUI_LABEL_PROJECT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_SITE_0 = "GUI_LABEL_SITE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_USER_0 = "GUI_LABEL_USER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_VIEW_0 = "GUI_LABEL_VIEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_WPTITLE_1 = "GUI_LABEL_WPTITLE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_BUTTON_0 = "GUI_LOGIN_BUTTON_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_BUTTON_ALREADY_IN_0 = "GUI_LOGIN_BUTTON_ALREADY_IN_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_FAILED_0 = "GUI_LOGIN_FAILED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_FAILED_DISABLED_0 = "GUI_LOGIN_FAILED_DISABLED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_FAILED_NO_TARGET_PERMISSIONS_1 = "GUI_LOGIN_FAILED_NO_TARGET_PERMISSIONS_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_FAILED_NO_WORKPLACE_PERMISSIONS_0 = "GUI_LOGIN_FAILED_NO_WORKPLACE_PERMISSIONS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_FAILED_TEMP_DISABLED_0 = "GUI_LOGIN_FAILED_TEMP_DISABLED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_FAILED_WITH_MESSAGE_1 = "GUI_LOGIN_FAILED_WITH_MESSAGE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_HEADLINE_0 = "GUI_LOGIN_HEADLINE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_HEADLINE_ALREADY_IN_0 = "GUI_LOGIN_HEADLINE_ALREADY_IN_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_HEADLINE_SELECTED_ORGUNIT_1 = "GUI_LOGIN_HEADLINE_SELECTED_ORGUNIT_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_MESSAGE_0 = "GUI_LOGIN_MESSAGE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_MESSAGE_ALREADY_IN_0 = "GUI_LOGIN_MESSAGE_ALREADY_IN_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_NO_DATA_0 = "GUI_LOGIN_NO_DATA_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_NO_NAME_0 = "GUI_LOGIN_NO_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_NO_PASSWORD_0 = "GUI_LOGIN_NO_PASSWORD_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_NOSCRIPT_1 = "GUI_LOGIN_NOSCRIPT_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_OPENCMS_IS_FREE_SOFTWARE_0 = "GUI_LOGIN_OPENCMS_IS_FREE_SOFTWARE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_ORGUNIT_0 = "GUI_LOGIN_ORGUNIT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_ORGUNIT_SEARCH_0 = "GUI_LOGIN_ORGUNIT_SEARCH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_ORGUNIT_SEARCH_NORESULTS_0 = "GUI_LOGIN_ORGUNIT_SEARCH_NORESULTS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_ORGUNIT_SELECT_OFF_0 = "GUI_LOGIN_ORGUNIT_SELECT_OFF_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_ORGUNIT_SELECT_ON_0 = "GUI_LOGIN_ORGUNIT_SELECT_ON_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_PASSWORD_0 = "GUI_LOGIN_PASSWORD_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_PCTYPE_PRIVATE_0 = "GUI_LOGIN_PCTYPE_PRIVATE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_PCTYPE_PUBLIC_0 = "GUI_LOGIN_PCTYPE_PUBLIC_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_RIGHTS_RESERVED_0 = "GUI_LOGIN_RIGHTS_RESERVED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_SECURITY_0 = "GUI_LOGIN_SECURITY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_SUCCESS_WITH_MESSAGE_2 = "GUI_LOGIN_SUCCESS_WITH_MESSAGE_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_TITLE_0 = "GUI_LOGIN_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_TRADEMARKS_0 = "GUI_LOGIN_TRADEMARKS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_UNKNOWN_RESOURCE_1 = "GUI_LOGIN_UNKNOWN_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_UNSUPPORTED_BROWSER_0 = "GUI_LOGIN_UNSUPPORTED_BROWSER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOGIN_USERNAME_0 = "GUI_LOGIN_USERNAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SHARED_TITLE_0 = "GUI_SHARED_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_ADD_DIALOG_HANDLER_2 = "INIT_ADD_DIALOG_HANDLER_2";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_ADD_EXPORT_POINT_2 = "INIT_ADD_EXPORT_POINT_2";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_ADD_MENURULE_1 = "INIT_ADD_MENURULE_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_ADD_TYPE_SETTING_1 = "INIT_ADD_TYPE_SETTING_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_ADD_TYPE_SETTING_FAILED_1 = "INIT_ADD_TYPE_SETTING_FAILED_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_ADV_PROP_DIALOG_HIDE_TABS_0 = "INIT_ADV_PROP_DIALOG_HIDE_TABS_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_ADV_PROP_DIALOG_SHOW_TABS_0 = "INIT_ADV_PROP_DIALOG_SHOW_TABS_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_AUTO_LOCK_DISABLED_0 = "INIT_AUTO_LOCK_DISABLED_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_AUTO_LOCK_ENABLED_0 = "INIT_AUTO_LOCK_ENABLED_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_DEFAULT_LOCALE_1 = "INIT_DEFAULT_LOCALE_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_DEFAULT_USER_SETTINGS_1 = "INIT_DEFAULT_USER_SETTINGS_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_DIRECT_EDIT_PROVIDER_1 = "INIT_DIRECT_EDIT_PROVIDER_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_EDITOR_ACTION_CLASS_1 = "INIT_EDITOR_ACTION_CLASS_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_EDITOR_CSSHANDLER_CLASS_1 = "INIT_EDITOR_CSSHANDLER_CLASS_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_EDITOR_DISPLAY_OPTS_1 = "INIT_EDITOR_DISPLAY_OPTS_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_EDITOR_HANDLER_CLASS_1 = "INIT_EDITOR_HANDLER_CLASS_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_EDITOR_PRE_ACTION_2 = "INIT_EDITOR_PRE_ACTION_2";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_LABEL_LINKS_IN_FOLDER_1 = "INIT_LABEL_LINKS_IN_FOLDER_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_MAX_FILE_UPLOAD_SIZE_1 = "INIT_MAX_FILE_UPLOAD_SIZE_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_MAX_FILE_UPLOAD_SIZE_UNLIMITED_0 = "INIT_MAX_FILE_UPLOAD_SIZE_UNLIMITED_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_NONCRIT_ERROR_0 = "INIT_NONCRIT_ERROR_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_PROP_ON_STRUCT_FALSE_0 = "INIT_PROP_ON_STRUCT_FALSE_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_PROP_ON_STRUCT_TRUE_0 = "INIT_PROP_ON_STRUCT_TRUE_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_REMOVE_EXPLORER_TYPE_SETTING_1 = "INIT_REMOVE_EXPLORER_TYPE_SETTING_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_USER_MANAGEMENT_ICON_DISABLED_0 = "INIT_USER_MANAGEMENT_ICON_DISABLED_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_USER_MANAGEMENT_ICON_ENABLED_0 = "INIT_USER_MANAGEMENT_ICON_ENABLED_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_VFS_ACCESS_INITIALIZED_0 = "INIT_VFS_ACCESS_INITIALIZED_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_WORKPLACE_INITIALIZE_START_0 = "INIT_WORKPLACE_INITIALIZE_START_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_WORKPLACE_LOCALIZED_1 = "INIT_WORKPLACE_LOCALIZED_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_WORKPLACE_VIEW_1 = "INIT_WORKPLACE_VIEW_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_XMLCONTENT_AUTOCORRECT_DISABLED_0 = "INIT_XMLCONTENT_AUTOCORRECT_DISABLED_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_XMLCONTENT_AUTOCORRECT_ENABLED_0 = "INIT_XMLCONTENT_AUTOCORRECT_ENABLED_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DIALOG_HANDLER_CLASS_2 = "LOG_DIALOG_HANDLER_CLASS_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_EVENT_CLEAR_CACHES_0 = "LOG_EVENT_CLEAR_CACHES_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_INCLUDE_ERRORPAGE_FAILED_0 = "LOG_INCLUDE_ERRORPAGE_FAILED_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_INVALID_EDITOR_CSSHANDLER_1 = "LOG_INVALID_EDITOR_CSSHANDLER_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_INVALID_EDITOR_PRE_ACTION_1 = "LOG_INVALID_EDITOR_PRE_ACTION_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_INVALID_SYNCHRONIZE_EXCLUDE_PATTERN_1 = "LOG_INVALID_SYNCHRONIZE_EXCLUDE_PATTERN_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_LOGIN_NO_STARTUP_PROJECT_2 = "LOG_LOGIN_NO_STARTUP_PROJECT_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_NO_TEMP_FILE_PROJECT_0 = "LOG_NO_TEMP_FILE_PROJECT_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_PARAM_RESOURCE_2 = "LOG_PARAM_RESOURCE_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_READING_VIEW_FOLDER_FAILED_1 = "LOG_READING_VIEW_FOLDER_FAILED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_PARAM_2 = "LOG_SET_PARAM_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_THREAD_CREATION_FAILED_1 = "LOG_THREAD_CREATION_FAILED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_UNSUPPORTED_ENCODING_SET_1 = "LOG_UNSUPPORTED_ENCODING_SET_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_USERAGREEMENT_SHOW_1 = "LOG_USERAGREEMENT_SHOW_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_USERAGREEMENT_WRONG_VERSION_2 = "LOG_USERAGREEMENT_WRONG_VERSION_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_WORKPLACE_INIT_NO_LOCALES_1 = "LOG_WORKPLACE_INIT_NO_LOCALES_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_WORKPLACE_INIT_NO_VIEWS_1 = "LOG_WORKPLACE_INIT_NO_VIEWS_1";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /**The part of  state constant. */
    private static final String STATE_POSTFIX = "_0";

    /**The part of  state constant. */
    private static final String STATE_PREFIX = "GUI_EXPLORER_STATE";

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
     * Create constant name.
     * @param state  STATE_UNCHANGED, STATE_CHANGED, STATE_NEW or STATE_DELETED.
     * @return cconstanname as String
     */
    public static String getStateKey(CmsResourceState state) {

        StringBuffer sb = new StringBuffer(STATE_PREFIX);
        sb.append(state);
        sb.append(STATE_POSTFIX);
        return sb.toString();

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