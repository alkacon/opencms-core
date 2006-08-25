/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/Messages.java,v $
 * Date   : $Date: 2006/08/25 08:13:10 $
 * Version: $Revision: 1.15.4.2 $
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

package org.opencms.workplace.commons;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;
import org.opencms.util.CmsStringUtil;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Jan Baudisch 
 * 
 * @version $Revision: 1.15.4.2 $ 
 * 
 * @since 6.0.0 
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_AVAILABILITY_BAD_TIMEWINDOW_0 = "ERR_AVAILABILITY_BAD_TIMEWINDOW_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_AVAILABILITY_MULTI_0 = "ERR_AVAILABILITY_MULTI_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_BUILDING_RESTYPE_LIST_1 = "ERR_BUILDING_RESTYPE_LIST_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CHACC_DELETE_ENTRY_0 = "ERR_CHACC_DELETE_ENTRY_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CHACC_MODIFY_ENTRY_0 = "ERR_CHACC_MODIFY_ENTRY_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CHANGE_LINK_TARGET_0 = "ERR_CHANGE_LINK_TARGET_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CHPWD_NO_MATCH_0 = "ERR_CHPWD_NO_MATCH_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_COPY_MULTI_0 = "ERR_COPY_MULTI_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_COPY_MULTI_TARGET_NOFOLDER_1 = "ERR_COPY_MULTI_TARGET_NOFOLDER_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_COPY_ONTO_ITSELF_1 = "ERR_COPY_ONTO_ITSELF_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_DELETE_MULTI_0 = "ERR_DELETE_MULTI_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_FILE_SIZE_TOO_LARGE_1 = "ERR_FILE_SIZE_TOO_LARGE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_GET_LINK_TARGET_1 = "ERR_GET_LINK_TARGET_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_GET_RESTYPE_1 = "ERR_GET_RESTYPE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INVALID_NEW_PASS_0 = "ERR_INVALID_NEW_PASS_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INVALID_OLD_PASS_0 = "ERR_INVALID_OLD_PASS_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INVALID_PROP_0 = "ERR_INVALID_PROP_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_LOCK_MULTI_0 = "ERR_LOCK_MULTI_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MISSING_FIELDS_0 = "ERR_MISSING_FIELDS_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MISSING_GROUP_OR_USER_NAME_0 = "ERR_MISSING_GROUP_OR_USER_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MODIFY_INTERNAL_FLAG_0 = "ERR_MODIFY_INTERNAL_FLAG_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MOVE_FAILED_TARGET_EXISTS_2 = "ERR_MOVE_FAILED_TARGET_EXISTS_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MOVE_MULTI_0 = "ERR_MOVE_MULTI_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MOVE_MULTI_TARGET_NOFOLDER_1 = "ERR_MOVE_MULTI_TARGET_NOFOLDER_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MOVE_ONTO_ITSELF_1 = "ERR_MOVE_ONTO_ITSELF_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PARSE_EXPIREDATE_1 = "ERR_PARSE_EXPIREDATE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PARSE_RELEASEDATE_1 = "ERR_PARSE_RELEASEDATE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PARSE_TIMESTAMP_1 = "ERR_PARSE_TIMESTAMP_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PERMISSION_SELECT_TYPE_0 = "ERR_PERMISSION_SELECT_TYPE_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PUBLISH_MULTI_UNLOCK_0 = "ERR_PUBLISH_MULTI_UNLOCK_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_REDIRECT_XMLPAGE_DIALOG_1 = "ERR_REDIRECT_XMLPAGE_DIALOG_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_RESOURCE_DELETED_2 = "ERR_RESOURCE_DELETED_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_RESOURCE_DOES_NOT_EXIST_3 = "ERR_RESOURCE_DOES_NOT_EXIST_3";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_RESOURCE_OUTSIDE_TIMEWINDOW_1 = "ERR_RESOURCE_OUTSIDE_TIMEWINDOW_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_TOUCH_MULTI_0 = "ERR_TOUCH_MULTI_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UNDELETE_MULTI_0 = "ERR_UNDELETE_MULTI_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UNDO_CHANGES_MULTI_0 = "ERR_UNDO_CHANGES_MULTI_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UNLOCK_MULTI_0 = "ERR_UNLOCK_MULTI_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_FILE_NOT_FOUND_0 = "ERR_UPLOAD_FILE_NOT_FOUND_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_AVAILABILITY_0 = "GUI_AVAILABILITY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_AVAILABILITY_ENABLE_NOTIFICATION_0 = "GUI_AVAILABILITY_ENABLE_NOTIFICATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_AVAILABILITY_MODIFY_SIBLINGS_0 = "GUI_AVAILABILITY_MODIFY_SIBLINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_AVAILABILITY_NO_RESPONSIBLES_0 = "GUI_AVAILABILITY_NO_RESPONSIBLES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_AVAILABILITY_NOTIFICATION_MULTI_2 = "GUI_AVAILABILITY_NOTIFICATION_MULTI_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_AVAILABILITY_NOTIFICATION_SETTINGS_1 = "GUI_AVAILABILITY_NOTIFICATION_SETTINGS_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_AVAILABILITY_NOTIFICATION_SUBRES_0 = "GUI_AVAILABILITY_NOTIFICATION_SUBRES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_AVAILABILITY_RESET_EXPIRE_0 = "GUI_AVAILABILITY_RESET_EXPIRE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_AVAILABILITY_RESET_RELEASE_0 = "GUI_AVAILABILITY_RESET_RELEASE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_AVAILABILITY_RESPONSIBLES_0 = "GUI_AVAILABILITY_RESPONSIBLES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CALENDAR_CHOOSE_DATE_0 = "GUI_CALENDAR_CHOOSE_DATE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CHLINK_1 = "GUI_CHLINK_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CHNAV_1 = "GUI_CHNAV_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CHNAV_INSERT_AFTER_0 = "GUI_CHNAV_INSERT_AFTER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CHNAV_NO_CHANGE_0 = "GUI_CHNAV_NO_CHANGE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CHNAV_POS_CURRENT_1 = "GUI_CHNAV_POS_CURRENT_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CHNAV_POS_FIRST_0 = "GUI_CHNAV_POS_FIRST_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CHNAV_POS_LAST_0 = "GUI_CHNAV_POS_LAST_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CHTYPE_1 = "GUI_CHTYPE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CHTYPE_PLEASE_SELECT_0 = "GUI_CHTYPE_PLEASE_SELECT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COMMENTIMAGES_TITLE_1 = "GUI_COMMENTIMAGES_TITLE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONFIRMED_EXPIRATION_1 = "GUI_CONFIRMED_EXPIRATION_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONFIRMED_NOTIFICATION_INTERVAL_1 = "GUI_CONFIRMED_NOTIFICATION_INTERVAL_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONFIRMED_OUTDATED_RESOURCE_1 = "GUI_CONFIRMED_OUTDATED_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONFIRMED_RELEASE_1 = "GUI_CONFIRMED_RELEASE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPY_ALL_0 = "GUI_COPY_ALL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPY_ALL_NO_SIBLINGS_0 = "GUI_COPY_ALL_NO_SIBLINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPY_AS_NEW_0 = "GUI_COPY_AS_NEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPY_CONFIRM_OVERWRITE_2 = "GUI_COPY_CONFIRM_OVERWRITE_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPY_CREATE_SIBLINGS_0 = "GUI_COPY_CREATE_SIBLINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPY_KEEP_PERMISSIONS_0 = "GUI_COPY_KEEP_PERMISSIONS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPY_MULTI_2 = "GUI_COPY_MULTI_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPY_MULTI_CREATE_SIBLINGS_0 = "GUI_COPY_MULTI_CREATE_SIBLINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPY_MULTI_OVERWRITE_0 = "GUI_COPY_MULTI_OVERWRITE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPY_RESOURCE_1 = "GUI_COPY_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPY_TO_0 = "GUI_COPY_TO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPYTOPROJECT_NOPART_1 = "GUI_COPYTOPROJECT_NOPART_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPYTOPROJECT_PART_1 = "GUI_COPYTOPROJECT_PART_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPYTOPROJECT_PROJECT_CONFIRMATION_2 = "GUI_COPYTOPROJECT_PROJECT_CONFIRMATION_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPYTOPROJECT_RESOURCES_0 = "GUI_COPYTOPROJECT_RESOURCES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_COPYTOPROJECT_TITLE_0 = "GUI_COPYTOPROJECT_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CREATE_SIBLING_0 = "GUI_CREATE_SIBLING_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DELETE_ALL_SIBLINGS_0 = "GUI_DELETE_ALL_SIBLINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DELETE_CONFIRMATION_0 = "GUI_DELETE_CONFIRMATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DELETE_MULTI_2 = "GUI_DELETE_MULTI_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DELETE_MULTI_CONFIRMATION_0 = "GUI_DELETE_MULTI_CONFIRMATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DELETE_PRESERVE_SIBLINGS_0 = "GUI_DELETE_PRESERVE_SIBLINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DELETE_RELATIONS_CONCLUSION_MULTI_0 = "GUI_DELETE_RELATIONS_CONCLUSION_MULTI_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DELETE_RELATIONS_CONCLUSION_SINGLE_0 = "GUI_DELETE_RELATIONS_CONCLUSION_SINGLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DELETE_RELATIONS_INTRO_MULTI_0 = "GUI_DELETE_RELATIONS_INTRO_MULTI_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DELETE_RELATIONS_INTRO_SINGLE_0 = "GUI_DELETE_RELATIONS_INTRO_SINGLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DELETE_RELATIONS_NOT_ALLOWED_0 = "GUI_DELETE_RELATIONS_NOT_ALLOWED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DELETE_RELATIONS_TITLE_0 = "GUI_DELETE_RELATIONS_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DELETE_RESOURCE_1 = "GUI_DELETE_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_DELETE_WARNING_SIBLINGS_0 = "GUI_DELETE_WARNING_SIBLINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDIT_POINTER_LINK_URL_0 = "GUI_EDIT_POINTER_LINK_URL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_GROUPSELECTION_INTRO_TITLE_0 = "GUI_GROUPSELECTION_INTRO_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_GROUPSELECTION_LIST_ACTION_SELECT_HELP_0 = "GUI_GROUPSELECTION_LIST_ACTION_SELECT_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_GROUPSELECTION_LIST_ACTION_SELECT_NAME_0 = "GUI_GROUPSELECTION_LIST_ACTION_SELECT_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_GROUPSELECTION_LIST_COLS_ICON_0 = "GUI_GROUPSELECTION_LIST_COLS_ICON_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_GROUPSELECTION_LIST_COLS_ICON_HELP_0 = "GUI_GROUPSELECTION_LIST_COLS_ICON_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_GROUPSELECTION_LIST_COLS_NAME_0 = "GUI_GROUPSELECTION_LIST_COLS_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_GROUPSELECTION_LIST_ICON_HELP_0 = "GUI_GROUPSELECTION_LIST_ICON_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_GROUPSELECTION_LIST_ICON_NAME_0 = "GUI_GROUPSELECTION_LIST_ICON_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_GROUPSELECTION_LIST_NAME_0 = "GUI_GROUPSELECTION_LIST_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_GROUPSELECTION_USER_TITLE_1 = "GUI_GROUPSELECTION_USER_TITLE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_0 = "GUI_HISTORY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_COLS_COMPARE_0 = "GUI_HISTORY_COLS_COMPARE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_COLS_DATE_LAST_MODIFIED_0 = "GUI_HISTORY_COLS_DATE_LAST_MODIFIED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_COLS_DATE_PUBLISHED_0 = "GUI_HISTORY_COLS_DATE_PUBLISHED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_COLS_FILE_TYPE_0 = "GUI_HISTORY_COLS_FILE_TYPE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_COLS_RESOURCE_PATH_0 = "GUI_HISTORY_COLS_RESOURCE_PATH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_COLS_RESTORE_0 = "GUI_HISTORY_COLS_RESTORE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_COLS_SIZE_0 = "GUI_HISTORY_COLS_SIZE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_COLS_USER_0 = "GUI_HISTORY_COLS_USER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_COLS_VERSION_0 = "GUI_HISTORY_COLS_VERSION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_COLS_VERSION1_0 = "GUI_HISTORY_COLS_VERSION1_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_COLS_VERSION2_0 = "GUI_HISTORY_COLS_VERSION2_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_COLS_VIEW_0 = "GUI_HISTORY_COLS_VIEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_COMPARE_0 = "GUI_HISTORY_COMPARE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_CONFIRMATION_0 = "GUI_HISTORY_CONFIRMATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_FIRST_VERSION_0 = "GUI_HISTORY_FIRST_VERSION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_PREVIEW_0 = "GUI_HISTORY_PREVIEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_RESTORE_VERSION_0 = "GUI_HISTORY_RESTORE_VERSION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_HISTORY_SECOND_VERSION_0 = "GUI_HISTORY_SECOND_VERSION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_ADRESS_0 = "GUI_INPUT_ADRESS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_ADD_0 = "GUI_LABEL_ADD_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_CREATED_BY_0 = "GUI_LABEL_CREATED_BY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_DATE_CREATED_0 = "GUI_LABEL_DATE_CREATED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_DATE_EXPIRED_0 = "GUI_LABEL_DATE_EXPIRED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_DATE_LAST_MODIFIED_0 = "GUI_LABEL_DATE_LAST_MODIFIED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_DATE_RELEASED_0 = "GUI_LABEL_DATE_RELEASED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_DELETE_0 = "GUI_LABEL_DELETE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_DESCRIPTION_0 = "GUI_LABEL_DESCRIPTION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_DETAILS_0 = "GUI_LABEL_DETAILS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_EMAIL_0 = "GUI_LABEL_EMAIL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_EXTENDED_0 = "GUI_LABEL_EXTENDED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_FALSE_0 = "GUI_LABEL_FALSE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_FIRSTNAME_0 = "GUI_LABEL_FIRSTNAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_GROUP_0 = "GUI_LABEL_GROUP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_LANGUAGE_0 = "GUI_LABEL_LANGUAGE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_LASTNAME_0 = "GUI_LABEL_LASTNAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_LOCKED_BY_0 = "GUI_LABEL_LOCKED_BY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_NAME_0 = "GUI_LABEL_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_NAVTEXT_0 = "GUI_LABEL_NAVTEXT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_OK_0 = "GUI_LABEL_OK_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_PERMALINK_0 = "GUI_LABEL_PERMALINK_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_PERMISSIONS_0 = "GUI_LABEL_PERMISSIONS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_RESPONSIBLE_0 = "GUI_LABEL_RESPONSIBLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_SEARCH_0 = "GUI_LABEL_SEARCH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_SET_0 = "GUI_LABEL_SET_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_SIMPLE_0 = "GUI_LABEL_SIMPLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_SIZE_0 = "GUI_LABEL_SIZE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_STATE_0 = "GUI_LABEL_STATE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_SUMMARY_0 = "GUI_LABEL_SUMMARY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_TIMEWARP_0 = "GUI_LABEL_TIMEWARP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_TITLE_0 = "GUI_LABEL_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_TRUE_0 = "GUI_LABEL_TRUE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_TYPE_0 = "GUI_LABEL_TYPE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_UNLIMITED_0 = "GUI_LABEL_UNLIMITED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_USER_0 = "GUI_LABEL_USER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_USER_LAST_MODIFIED_0 = "GUI_LABEL_USER_LAST_MODIFIED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_VERSION_0 = "GUI_LABEL_VERSION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_WORKFLOW_STATUS_0 = "GUI_LABEL_WORKFLOW_STATUS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOCK_CHANGE_CONFIRMATION_0 = "GUI_LOCK_CHANGE_CONFIRMATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOCK_CONFIRMATION_0 = "GUI_LOCK_CONFIRMATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOCK_INFO_LOCKEDSUBRESOURCES_0 = "GUI_LOCK_INFO_LOCKEDSUBRESOURCES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOCK_MULTI_INFO_LOCKEDSUBRESOURCES_0 = "GUI_LOCK_MULTI_INFO_LOCKEDSUBRESOURCES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOCK_MULTI_LOCK_2 = "GUI_LOCK_MULTI_LOCK_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOCK_MULTI_LOCK_CONFIRMATION_0 = "GUI_LOCK_MULTI_LOCK_CONFIRMATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOCK_MULTI_STEAL_2 = "GUI_LOCK_MULTI_STEAL_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOCK_MULTI_UNLOCK_2 = "GUI_LOCK_MULTI_UNLOCK_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOCK_MULTI_UNLOCK_CONFIRMATION_0 = "GUI_LOCK_MULTI_UNLOCK_CONFIRMATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOCK_RESOURCE_1 = "GUI_LOCK_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOCK_STEAL_1 = "GUI_LOCK_STEAL_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOCK_UNLOCK_1 = "GUI_LOCK_UNLOCK_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LOCK_UNLOCK_CONFIRMATION_0 = "GUI_LOCK_UNLOCK_CONFIRMATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_MESSAGE_WAIT_0 = "GUI_MESSAGE_WAIT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_MOVE_MULTI_2 = "GUI_MOVE_MULTI_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_MOVE_RESOURCE_1 = "GUI_MOVE_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_MOVE_TO_0 = "GUI_MOVE_TO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_MULTI_RESOURCELIST_TITLE_0 = "GUI_MULTI_RESOURCELIST_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEW_INVITATION_0 = "GUI_NEW_INVITATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NO_MORE_NOTIFICATIONS_0 = "GUI_NO_MORE_NOTIFICATIONS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NOTIFICATION_INTERVAL_0 = "GUI_NOTIFICATION_INTERVAL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NOTIFICATION_SETTINGS_0 = "GUI_NOTIFICATION_SETTINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_0 = "GUI_PERMISSION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_ADD_ACE_0 = "GUI_PERMISSION_ADD_ACE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_ALLOWED_0 = "GUI_PERMISSION_ALLOWED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_AVAILABLE_GROUPS_0 = "GUI_PERMISSION_AVAILABLE_GROUPS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_AVAILABLE_USERS_0 = "GUI_PERMISSION_AVAILABLE_USERS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_BEQUEATH_SUBFOLDER_0 = "GUI_PERMISSION_BEQUEATH_SUBFOLDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_CHANGE_1 = "GUI_PERMISSION_CHANGE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_DENIED_0 = "GUI_PERMISSION_DENIED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_ERROR_0 = "GUI_PERMISSION_ERROR_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_INHERIT_ON_SUBFOLDERS_0 = "GUI_PERMISSION_INHERIT_ON_SUBFOLDERS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_INHERITED_FROM_1 = "GUI_PERMISSION_INHERITED_FROM_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_INTERNAL_0 = "GUI_PERMISSION_INTERNAL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_OVERWRITE_INHERITED_0 = "GUI_PERMISSION_OVERWRITE_INHERITED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_SELECT_VIEW_0 = "GUI_PERMISSION_SELECT_VIEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_TITLE_0 = "GUI_PERMISSION_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_TYPE_CONTROL_0 = "GUI_PERMISSION_TYPE_CONTROL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_TYPE_DIRECT_PUBLISH_0 = "GUI_PERMISSION_TYPE_DIRECT_PUBLISH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_TYPE_READ_0 = "GUI_PERMISSION_TYPE_READ_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_TYPE_VIEW_0 = "GUI_PERMISSION_TYPE_VIEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_TYPE_WRITE_0 = "GUI_PERMISSION_TYPE_WRITE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PERMISSION_USER_0 = "GUI_PERMISSION_USER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_0 = "GUI_PREF_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_BUTTON_STYLE_0 = "GUI_PREF_BUTTON_STYLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_BUTTON_STYLE_DIRECT_EDIT_0 = "GUI_PREF_BUTTON_STYLE_DIRECT_EDIT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_BUTTON_STYLE_EDITOR_0 = "GUI_PREF_BUTTON_STYLE_EDITOR_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_BUTTON_STYLE_EXPLORER_0 = "GUI_PREF_BUTTON_STYLE_EXPLORER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_BUTTONSTYLE_IMG_0 = "GUI_PREF_BUTTONSTYLE_IMG_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_BUTTONSTYLE_IMGTXT_0 = "GUI_PREF_BUTTONSTYLE_IMGTXT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_BUTTONSTYLE_TXT_0 = "GUI_PREF_BUTTONSTYLE_TXT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_CHPWD_0 = "GUI_PREF_CHPWD_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_COPY_AS_NEW_0 = "GUI_PREF_COPY_AS_NEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_COPY_AS_SIBLING_0 = "GUI_PREF_COPY_AS_SIBLING_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_COPY_AS_SIBLINGS_0 = "GUI_PREF_COPY_AS_SIBLINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_CURRENT_PWD_0 = "GUI_PREF_CURRENT_PWD_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_DEFAULTS_TASKS_0 = "GUI_PREF_DEFAULTS_TASKS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_DELETE_SIBLINGS_0 = "GUI_PREF_DELETE_SIBLINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_DIALOG_COPY_FILE_0 = "GUI_PREF_DIALOG_COPY_FILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_DIALOG_COPY_FOLDER_0 = "GUI_PREF_DIALOG_COPY_FOLDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_DIALOG_DELETE_FILE_0 = "GUI_PREF_DIALOG_DELETE_FILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_DIALOG_DIRECT_PUBLISH_0 = "GUI_PREF_DIALOG_DIRECT_PUBLISH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_EDITOR_BEST_0 = "GUI_PREF_EDITOR_BEST_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_FILES_PER_PAGE_0 = "GUI_PREF_FILES_PER_PAGE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_MSG_ACCEPTED_0 = "GUI_PREF_MSG_ACCEPTED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_MSG_COMPLETED_0 = "GUI_PREF_MSG_COMPLETED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_MSG_FORWARDED_0 = "GUI_PREF_MSG_FORWARDED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_MSG_MEMBERS_0 = "GUI_PREF_MSG_MEMBERS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_NEW_PWD_0 = "GUI_PREF_NEW_PWD_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_PANEL_DIALOGS_0 = "GUI_PREF_PANEL_DIALOGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_PANEL_EDITORS_0 = "GUI_PREF_PANEL_EDITORS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_PANEL_EXPLORER_0 = "GUI_PREF_PANEL_EXPLORER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_PANEL_USER_0 = "GUI_PREF_PANEL_USER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_PANEL_WORKPLACE_0 = "GUI_PREF_PANEL_WORKPLACE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_PERMISSIONS_EXPAND_INHERITED_0 = "GUI_PREF_PERMISSIONS_EXPAND_INHERITED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_PERMISSIONS_EXPAND_USER_0 = "GUI_PREF_PERMISSIONS_EXPAND_USER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_PERMISSIONS_FOLDERS_INHERIT_0 = "GUI_PREF_PERMISSIONS_FOLDERS_INHERIT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_PERSONAL_DATA_0 = "GUI_PREF_PERSONAL_DATA_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_PRESERVE_SIBLINGS_0 = "GUI_PREF_PRESERVE_SIBLINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_PRESERVE_SIBLINGS_RESOURCES_0 = "GUI_PREF_PRESERVE_SIBLINGS_RESOURCES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_PUBLISH_ONLY_SELECTED_0 = "GUI_PREF_PUBLISH_ONLY_SELECTED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_PUBLISH_SIBLINGS_0 = "GUI_PREF_PUBLISH_SIBLINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_PWD_REPEAT_0 = "GUI_PREF_PWD_REPEAT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_REPORT_TYPE_0 = "GUI_PREF_REPORT_TYPE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_RESTRICT_EXPLORER_VIEW_0 = "GUI_PREF_RESTRICT_EXPLORER_VIEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_SETTINGS_DEFAULT_0 = "GUI_PREF_SETTINGS_DEFAULT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_SETTINGS_DISPLAY_0 = "GUI_PREF_SETTINGS_DISPLAY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_SETTINGS_EDITORS_0 = "GUI_PREF_SETTINGS_EDITORS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_SETTINGS_GENERAL_0 = "GUI_PREF_SETTINGS_GENERAL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_SETTINGS_PERMISSIONS_0 = "GUI_PREF_SETTINGS_PERMISSIONS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_SETTINGS_STARTUP_0 = "GUI_PREF_SETTINGS_STARTUP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_SHOW_ALL_PROJECTS_0 = "GUI_PREF_SHOW_ALL_PROJECTS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_SHOW_LOCK_DIALOG_0 = "GUI_PREF_SHOW_LOCK_DIALOG_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_STARTUP_FILTER_0 = "GUI_PREF_STARTUP_FILTER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_STARTUP_FOLDER_0 = "GUI_PREF_STARTUP_FOLDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_STARTUP_PROJECT_0 = "GUI_PREF_STARTUP_PROJECT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_STARTUP_SITE_0 = "GUI_PREF_STARTUP_SITE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_STARTUP_VIEW_0 = "GUI_PREF_STARTUP_VIEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PREF_USE_UPLOAD_APPLET_0 = "GUI_PREF_USE_UPLOAD_APPLET_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTIES_1 = "GUI_PROPERTIES_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTIES_ACTIVE_0 = "GUI_PROPERTIES_ACTIVE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTIES_EDIT_1 = "GUI_PROPERTIES_EDIT_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTIES_INDIVIDUAL_0 = "GUI_PROPERTIES_INDIVIDUAL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTIES_OF_1 = "GUI_PROPERTIES_OF_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTIES_SHARED_0 = "GUI_PROPERTIES_SHARED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTY_0 = "GUI_PROPERTY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTY_ADD_TO_NAV_0 = "GUI_PROPERTY_ADD_TO_NAV_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTY_ADVANCED_0 = "GUI_PROPERTY_ADVANCED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTY_ADVANCED_NO_PROPDEFS_0 = "GUI_PROPERTY_ADVANCED_NO_PROPDEFS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTY_DEFINE_0 = "GUI_PROPERTY_DEFINE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTY_FINISH_0 = "GUI_PROPERTY_FINISH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTY_NEW_0 = "GUI_PROPERTY_NEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTY_NEW_DEF_1 = "GUI_PROPERTY_NEW_DEF_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTY_USED_0 = "GUI_PROPERTY_USED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTY_VALUE_0 = "GUI_PROPERTY_VALUE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_ALLSIBLINGS_0 = "GUI_PUBLISH_ALLSIBLINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_CONFIRMATION_3 = "GUI_PUBLISH_CONFIRMATION_3";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_CONTINUE_BROKEN_LINKS_0 = "GUI_PUBLISH_CONTINUE_BROKEN_LINKS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_MULTI_2 = "GUI_PUBLISH_MULTI_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_MULTI_SUBRESOURCES_0 = "GUI_PUBLISH_MULTI_SUBRESOURCES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_PROJECT_0 = "GUI_PUBLISH_PROJECT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_PROJECT_CONFIRMATION_1 = "GUI_PUBLISH_PROJECT_CONFIRMATION_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_RELEASE_LOCKS_1 = "GUI_PUBLISH_RELEASE_LOCKS_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_RESOURCE_1 = "GUI_PUBLISH_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_SUBRESOURCES_0 = "GUI_PUBLISH_SUBRESOURCES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_UNLOCK_CONFIRMATION_0 = "GUI_PUBLISH_UNLOCK_CONFIRMATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_RENAMEIMAGES_DEFAULT_PREFIX_0 = "GUI_RENAMEIMAGES_DEFAULT_PREFIX_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_RENAMEIMAGES_INFO_IMAGECOUNT_2 = "GUI_RENAMEIMAGES_INFO_IMAGECOUNT_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_RENAMEIMAGES_PLACES_0 = "GUI_RENAMEIMAGES_PLACES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_RENAMEIMAGES_PREFIX_0 = "GUI_RENAMEIMAGES_PREFIX_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_RENAMEIMAGES_REMOVETITLE_0 = "GUI_RENAMEIMAGES_REMOVETITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_RENAMEIMAGES_STARTCOUNT_0 = "GUI_RENAMEIMAGES_STARTCOUNT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_RENAMEIMAGES_TITLE_1 = "GUI_RENAMEIMAGES_TITLE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_REPLACE_FILE_1 = "GUI_REPLACE_FILE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SECURE_0 = "GUI_SECURE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SECURE_EXPORT_0 = "GUI_SECURE_EXPORT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SECURE_EXPORT_RESOURCE_1 = "GUI_SECURE_EXPORT_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SECURE_EXPORT_SETTINGS_0 = "GUI_SECURE_EXPORT_SETTINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SECURE_EXPORTED_NOT_INTERN_0 = "GUI_SECURE_EXPORTED_NOT_INTERN_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SECURE_EXPORTNAME_0 = "GUI_SECURE_EXPORTNAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SECURE_EXPORTUSER_NO_PERMISSION_0 = "GUI_SECURE_EXPORTUSER_NO_PERMISSION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SECURE_INHERIT_FROM_2 = "GUI_SECURE_INHERIT_FROM_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SECURE_INTERN_NO_EXPORT_0 = "GUI_SECURE_INTERN_NO_EXPORT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SECURE_INTERN_SETTINGS_0 = "GUI_SECURE_INTERN_SETTINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SECURE_NO_SERVER_0 = "GUI_SECURE_NO_SERVER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SECURE_NOT_SET_0 = "GUI_SECURE_NOT_SET_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SECURE_ONLINE_ADDRESS_0 = "GUI_SECURE_ONLINE_ADDRESS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SYNC_FOLDERS_AND_FILES_0 = "GUI_SYNC_FOLDERS_AND_FILES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SYNCHRONIZATION_INFO_0 = "GUI_SYNCHRONIZATION_INFO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_FILTER_A1_0 = "GUI_TASK_FILTER_A1_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_FILTER_A2_0 = "GUI_TASK_FILTER_A2_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_FILTER_A3_0 = "GUI_TASK_FILTER_A3_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_FILTER_B1_0 = "GUI_TASK_FILTER_B1_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_FILTER_B2_0 = "GUI_TASK_FILTER_B2_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_FILTER_B3_0 = "GUI_TASK_FILTER_B3_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_FILTER_C1_0 = "GUI_TASK_FILTER_C1_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_FILTER_C2_0 = "GUI_TASK_FILTER_C2_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_FILTER_C3_0 = "GUI_TASK_FILTER_C3_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_FILTER_D1_0 = "GUI_TASK_FILTER_D1_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_FILTER_D2_0 = "GUI_TASK_FILTER_D2_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_FILTER_D3_0 = "GUI_TASK_FILTER_D3_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TOUCH_0 = "GUI_TOUCH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TOUCH_MODIFY_SUBRESOURCES_0 = "GUI_TOUCH_MODIFY_SUBRESOURCES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TOUCH_MULTI_2 = "GUI_TOUCH_MULTI_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TOUCH_NEW_TIMESTAMP_0 = "GUI_TOUCH_NEW_TIMESTAMP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TOUCH_RESOURCE_1 = "GUI_TOUCH_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNDELETE_CONFIRMATION_0 = "GUI_UNDELETE_CONFIRMATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNDELETE_MULTI_2 = "GUI_UNDELETE_MULTI_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNDELETE_MULTI_CONFIRMATION_0 = "GUI_UNDELETE_MULTI_CONFIRMATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNDELETE_RESOURCE_1 = "GUI_UNDELETE_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNDO_CHANGES_1 = "GUI_UNDO_CHANGES_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNDO_CHANGES_CONTENT_MULTI_SUBRESOURCES_0 = "GUI_UNDO_CHANGES_CONTENT_MULTI_SUBRESOURCES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNDO_CHANGES_CONTENT_SUBRESOURCES_0 = "GUI_UNDO_CHANGES_CONTENT_SUBRESOURCES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNDO_CHANGES_MOVE_MULTI_SUBRESOURCES_0 = "GUI_UNDO_CHANGES_MOVE_MULTI_SUBRESOURCES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNDO_CHANGES_MOVE_SUBRESOURCES_0 = "GUI_UNDO_CHANGES_MOVE_SUBRESOURCES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNDO_CHANGES_MULTI_2 = "GUI_UNDO_CHANGES_MULTI_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNDO_CHANGES_RECURSIVE_MULTI_SUBRESOURCES_0 = "GUI_UNDO_CHANGES_RECURSIVE_MULTI_SUBRESOURCES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNDO_CHANGES_RECURSIVE_SUBRESOURCES_0 = "GUI_UNDO_CHANGES_RECURSIVE_SUBRESOURCES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNDO_CHANGES_RECURSIVE_TITLE_0 = "GUI_UNDO_CHANGES_RECURSIVE_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNDO_CONFIRMATION_0 = "GUI_UNDO_CONFIRMATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNDO_LASTMODIFIED_INFO_3 = "GUI_UNDO_LASTMODIFIED_INFO_3";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNDO_MOVE_OPERATION_INFO_2 = "GUI_UNDO_MOVE_OPERATION_INFO_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USERSELECTION_GROUP_BLOCK_1 = "GUI_USERSELECTION_GROUP_BLOCK_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USERSELECTION_INTRO_TITLE_1 = "GUI_USERSELECTION_INTRO_TITLE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USERSELECTION_LIST_ACTION_SELECT_HELP_0 = "GUI_USERSELECTION_LIST_ACTION_SELECT_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USERSELECTION_LIST_ACTION_SELECT_NAME_0 = "GUI_USERSELECTION_LIST_ACTION_SELECT_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USERSELECTION_LIST_COLS_FULLNAME_0 = "GUI_USERSELECTION_LIST_COLS_FULLNAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USERSELECTION_LIST_COLS_ICON_0 = "GUI_USERSELECTION_LIST_COLS_ICON_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USERSELECTION_LIST_COLS_ICON_HELP_0 = "GUI_USERSELECTION_LIST_COLS_ICON_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USERSELECTION_LIST_COLS_LOGIN_0 = "GUI_USERSELECTION_LIST_COLS_LOGIN_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USERSELECTION_LIST_ICON_HELP_0 = "GUI_USERSELECTION_LIST_ICON_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USERSELECTION_LIST_ICON_NAME_0 = "GUI_USERSELECTION_LIST_ICON_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USERSELECTION_LIST_NAME_0 = "GUI_USERSELECTION_LIST_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USERSELECTION_TYPE_SYSTEM_0 = "GUI_USERSELECTION_TYPE_SYSTEM_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_USERSELECTION_TYPE_WEB_0 = "GUI_USERSELECTION_TYPE_WEB_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_COMPUTING_PUBRES_FAILED_0 = "LOG_COMPUTING_PUBRES_FAILED_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DISPLAY_UNLOCK_INF_FAILED_0 = "LOG_DISPLAY_UNLOCK_INF_FAILED_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERROR_INCLUDE_FAILED_1 = "LOG_ERROR_INCLUDE_FAILED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_PROJECT_NAME_FAILED_0 = "LOG_SET_PROJECT_NAME_FAILED_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.commons.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /**Postfix to create task key. */
    private static final String TASK_POSTFIX = "_0";

    /**Prefix to create task key. */
    private static final String TASK_PREFIX = "GUI_";

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
     *  Convert old key like task.filter.a1 to new GUI_TASK_FILTER_A1_0.
     * @param oldTaskKey key like "task.filter.a1"
     * @return converted key as String
     */
    public static String getTaskKey(String oldTaskKey) {

        StringBuffer sb = new StringBuffer(TASK_PREFIX);
        sb.append(CmsStringUtil.substitute(oldTaskKey, ".", "_").toUpperCase());
        sb.append(TASK_POSTFIX);
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
