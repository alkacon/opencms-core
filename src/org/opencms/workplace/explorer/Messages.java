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

package org.opencms.workplace.explorer;

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
    public static final String ERR_CREATE_FOLDER_0 = "ERR_CREATE_FOLDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CREATE_LINK_0 = "ERR_CREATE_LINK_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_NEW_RES_CONSTRUCTOR_NOT_FOUND_1 = "ERR_NEW_RES_CONSTRUCTOR_NOT_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_NEW_RES_HANDLER_CLASS_NOT_FOUND_1 = "ERR_NEW_RES_HANDLER_CLASS_NOT_FOUND_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_TABLE_IMPORT_FAILED_0 = "ERR_TABLE_IMPORT_FAILED_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_FILE_0 = "ERR_UPLOAD_FILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_FILE_NOT_FOUND_0 = "ERR_UPLOAD_FILE_NOT_FOUND_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_FILE_SIZE_TOO_HIGH_1 = "ERR_UPLOAD_FILE_SIZE_TOO_HIGH_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UPLOAD_STORE_CLIENT_FOLDER_1 = "ERR_UPLOAD_STORE_CLIENT_FOLDER_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_BACK_0 = "GUI_BUTTON_BACK_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_CONTINUE_0 = "GUI_BUTTON_CONTINUE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_ENDWIZARD_0 = "GUI_BUTTON_ENDWIZARD_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_NEW_0 = "GUI_BUTTON_NEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_NEWRESOURCE_UPLOAD_UNZIP_0 = "GUI_BUTTON_NEWRESOURCE_UPLOAD_UNZIP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_NEXTSCREEN_0 = "GUI_BUTTON_NEXTSCREEN_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_PARENT_0 = "GUI_BUTTON_PARENT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_SEARCH_0 = "GUI_BUTTON_SEARCH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_BUTTON_UPLOAD_0 = "GUI_BUTTON_UPLOAD_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_ALTBELONGTO_0 = "GUI_EXPLORER_ALTBELONGTO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_ABOUT_0 = "GUI_EXPLORER_CONTEXT_ABOUT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_ACCESS_0 = "GUI_EXPLORER_CONTEXT_ACCESS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_ADE_ADVANCED_0 = "GUI_EXPLORER_CONTEXT_ADE_ADVANCED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_ADVANCED_0 = "GUI_EXPLORER_CONTEXT_ADVANCED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_ADVANCED_HISTORY_0 = "GUI_EXPLORER_CONTEXT_ADVANCED_HISTORY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_ADVANCED_PROPERTIES_0 = "GUI_EXPLORER_CONTEXT_ADVANCED_PROPERTIES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_ALIAS_DIALOG_0 = "GUI_EXPLORER_CONTEXT_ALIAS_DIALOG_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_AVAILABILITY_0 = "GUI_EXPLORER_CONTEXT_AVAILABILITY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_CATEGORIES_0 = "GUI_EXPLORER_CONTEXT_CATEGORIES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_CHNAV_0 = "GUI_EXPLORER_CONTEXT_CHNAV_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_COMMENTIMAGES_0 = "GUI_EXPLORER_CONTEXT_COMMENTIMAGES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_COPY_0 = "GUI_EXPLORER_CONTEXT_COPY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_COPY_MOVE_0 = "GUI_EXPLORER_CONTEXT_COPY_MOVE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_COPYTOPROJECT_0 = "GUI_EXPLORER_CONTEXT_COPYTOPROJECT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_DELETE_0 = "GUI_EXPLORER_CONTEXT_DELETE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_EDIT_0 = "GUI_EXPLORER_CONTEXT_EDIT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_EDIT_CONFIG_0 = "GUI_EXPLORER_CONTEXT_EDIT_CONFIG_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_EDIT_SITE_CONFIG_0 = "GUI_EXPLORER_CONTEXT_EDIT_SITE_CONFIG_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_EDITCONTROLFILE_0 = "GUI_EXPLORER_CONTEXT_EDITCONTROLFILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_EDITLINK_0 = "GUI_EXPLORER_CONTEXT_EDITLINK_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_EDITSOURCE_0 = "GUI_EXPLORER_CONTEXT_EDITSOURCE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_GO_TO_PARENT_0 = "GUI_EXPLORER_CONTEXT_GO_TO_PARENT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_HISTORY_0 = "GUI_EXPLORER_CONTEXT_HISTORY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_LINKRELATIONFROM_0 = "GUI_EXPLORER_CONTEXT_LINKRELATIONFROM_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_LINKRELATIONTO_0 = "GUI_EXPLORER_CONTEXT_LINKRELATIONTO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_LOCK_0 = "GUI_EXPLORER_CONTEXT_LOCK_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_LOCK_REPORT_0 = "GUI_EXPLORER_CONTEXT_LOCK_REPORT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_LOCKS_0 = "GUI_EXPLORER_CONTEXT_LOCKS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_LOGOUT_0 = "GUI_EXPLORER_CONTEXT_LOGOUT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_MOVE_0 = "GUI_EXPLORER_CONTEXT_MOVE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_MOVE_MULTI_0 = "GUI_EXPLORER_CONTEXT_MOVE_MULTI_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_MULTIFILE_PROPERTY_0 = "GUI_EXPLORER_CONTEXT_MULTIFILE_PROPERTY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_OPENGALLERY_0 = "GUI_EXPLORER_CONTEXT_OPENGALLERY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_OVERRIDELOCK_0 = "GUI_EXPLORER_CONTEXT_OVERRIDELOCK_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_PAGE_INFO_0 = "GUI_EXPLORER_CONTEXT_PAGE_INFO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_PAGEEDIT_0 = "GUI_EXPLORER_CONTEXT_PAGEEDIT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_PREVIEW_0 = "GUI_EXPLORER_CONTEXT_PREVIEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_PROPERTY_0 = "GUI_EXPLORER_CONTEXT_PROPERTY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_PUBLISH_0 = "GUI_EXPLORER_CONTEXT_PUBLISH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_PUBLISH_SCHEDULED_0 = "GUI_EXPLORER_CONTEXT_PUBLISH_SCHEDULED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_REFRESH_0 = "GUI_EXPLORER_CONTEXT_REFRESH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_RELATIONS_0 = "GUI_EXPLORER_CONTEXT_RELATIONS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_RENAME_0 = "GUI_EXPLORER_CONTEXT_RENAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_RENAMEIMAGES_0 = "GUI_EXPLORER_CONTEXT_RENAMEIMAGES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_REPLACE_0 = "GUI_EXPLORER_CONTEXT_REPLACE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_RESOURCE_INFO_0 = "GUI_EXPLORER_CONTEXT_RESOURCE_INFO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_SECURE_0 = "GUI_EXPLORER_CONTEXT_SECURE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_SEO_0 = "GUI_EXPLORER_CONTEXT_SEO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_SHOW_DELETED_0 = "GUI_EXPLORER_CONTEXT_SHOW_DELETED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_SHOW_WORKPLACE_0 = "GUI_EXPLORER_CONTEXT_SHOW_WORKPLACE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_SHOWSIBLINGS_0 = "GUI_EXPLORER_CONTEXT_SHOWSIBLINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_TOUCH_0 = "GUI_EXPLORER_CONTEXT_TOUCH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_TYPE_0 = "GUI_EXPLORER_CONTEXT_TYPE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_UNDELETE_0 = "GUI_EXPLORER_CONTEXT_UNDELETE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_UNDOCHANGES_0 = "GUI_EXPLORER_CONTEXT_UNDOCHANGES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_UNLOCK_0 = "GUI_EXPLORER_CONTEXT_UNLOCK_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_CONTEXT_USERSETTINGS_0 = "GUI_EXPLORER_CONTEXT_USERSETTINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_LOCKEDBY_0 = "GUI_EXPLORER_LOCKEDBY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_LOCKEDIN_0 = "GUI_EXPLORER_LOCKEDIN_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_NAME_0 = "GUI_EXPLORER_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_STATE0_0 = "GUI_EXPLORER_STATE0_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_STATE1_0 = "GUI_EXPLORER_STATE1_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_STATE2_0 = "GUI_EXPLORER_STATE2_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EXPLORER_STATE3_0 = "GUI_EXPLORER_STATE3_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_ADRESS_0 = "GUI_INPUT_ADRESS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_CACHE_0 = "GUI_INPUT_CACHE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_COPYRIGHT_0 = "GUI_INPUT_COPYRIGHT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_DATECREATED_0 = "GUI_INPUT_DATECREATED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_DATEEXPIRED_0 = "GUI_INPUT_DATEEXPIRED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_DATELASTMODIFIED_0 = "GUI_INPUT_DATELASTMODIFIED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_DATERELEASED_0 = "GUI_INPUT_DATERELEASED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_LOCKEDBY_0 = "GUI_INPUT_LOCKEDBY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_NAME_0 = "GUI_INPUT_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_NAVTEXT_0 = "GUI_INPUT_NAVTEXT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_PAGE_0 = "GUI_INPUT_PAGE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_PATH_0 = "GUI_INPUT_PATH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_PERMISSIONS_0 = "GUI_INPUT_PERMISSIONS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_SIZE_0 = "GUI_INPUT_SIZE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_STATE_0 = "GUI_INPUT_STATE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_TITLE_0 = "GUI_INPUT_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_TYPE_0 = "GUI_INPUT_TYPE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_USERCREATED_0 = "GUI_INPUT_USERCREATED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_INPUT_USERLASTMODIFIED_0 = "GUI_INPUT_USERLASTMODIFIED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_LOADING_0 = "GUI_LABEL_LOADING_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LABEL_SITE_0 = "GUI_LABEL_SITE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_MODELFILES_DETAIL_HIDE_DESCRIPTION_HELP_0 = "GUI_MODELFILES_DETAIL_HIDE_DESCRIPTION_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_MODELFILES_DETAIL_HIDE_DESCRIPTION_NAME_0 = "GUI_MODELFILES_DETAIL_HIDE_DESCRIPTION_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_MODELFILES_DETAIL_SHOW_DESCRIPTION_HELP_0 = "GUI_MODELFILES_DETAIL_SHOW_DESCRIPTION_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_MODELFILES_DETAIL_SHOW_DESCRIPTION_NAME_0 = "GUI_MODELFILES_DETAIL_SHOW_DESCRIPTION_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_MODELFILES_LABEL_DESCRIPTION_0 = "GUI_MODELFILES_LABEL_DESCRIPTION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWFILE_EDITPROPERTIES_0 = "GUI_NEWFILE_EDITPROPERTIES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWFOLDER_CREATEINDEX_0 = "GUI_NEWFOLDER_CREATEINDEX_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWFOLDER_EDITPROPERTIES_0 = "GUI_NEWFOLDER_EDITPROPERTIES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWFOLDER_LIST_NO_INDEX_0 = "GUI_NEWFOLDER_LIST_NO_INDEX_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWFOLDER_OPTIONS_0 = "GUI_NEWFOLDER_OPTIONS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWFOLDER_SELECT_INDEX_TYPE_0 = "GUI_NEWFOLDER_SELECT_INDEX_TYPE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_0 = "GUI_NEWRESOURCE_0";

    /**
     * Message constant for key in the resource bundle.<p>
     *
     * @deprecated use {@link #GUI_NEWRESOURCE_APPENDSUFFIX_HTML_1} with {@link CmsNewResource#getSuffixHtml()} instead
     */
    @Deprecated
    public static final String GUI_NEWRESOURCE_APPENDSUFFIX_HTML_0 = "GUI_NEWRESOURCE_APPENDSUFFIX_HTML_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_APPENDSUFFIX_HTML_1 = "GUI_NEWRESOURCE_APPENDSUFFIX_HTML_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_CONVERSION_DELIM_BEST_0 = "GUI_NEWRESOURCE_CONVERSION_DELIM_BEST_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_CONVERSION_DELIM_COMMA_0 = "GUI_NEWRESOURCE_CONVERSION_DELIM_COMMA_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_CONVERSION_DELIM_SEMICOLON_0 = "GUI_NEWRESOURCE_CONVERSION_DELIM_SEMICOLON_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_CONVERSION_DELIM_TAB_0 = "GUI_NEWRESOURCE_CONVERSION_DELIM_TAB_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_CONVERSION_DELIMITER_0 = "GUI_NEWRESOURCE_CONVERSION_DELIMITER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_CONVERSION_NOSTYLE_0 = "GUI_NEWRESOURCE_CONVERSION_NOSTYLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_CONVERSION_SETTINGS_0 = "GUI_NEWRESOURCE_CONVERSION_SETTINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_CONVERSION_XSLTFILE_0 = "GUI_NEWRESOURCE_CONVERSION_XSLTFILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_DETAIL_HIDE_DESCRIPTION_HELP_0 = "GUI_NEWRESOURCE_DETAIL_HIDE_DESCRIPTION_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_DETAIL_HIDE_DESCRIPTION_NAME_0 = "GUI_NEWRESOURCE_DETAIL_HIDE_DESCRIPTION_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_DETAIL_SHOW_DESCRIPTION_HELP_0 = "GUI_NEWRESOURCE_DETAIL_SHOW_DESCRIPTION_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_DETAIL_SHOW_DESCRIPTION_NAME_0 = "GUI_NEWRESOURCE_DETAIL_SHOW_DESCRIPTION_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_FILE_0 = "GUI_NEWRESOURCE_FILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_FOLDER_0 = "GUI_NEWRESOURCE_FOLDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_LIST_COLS_ICON_0 = "GUI_NEWRESOURCE_LIST_COLS_ICON_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_LIST_COLS_NAME_0 = "GUI_NEWRESOURCE_LIST_COLS_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_LIST_COLS_SELECT_0 = "GUI_NEWRESOURCE_LIST_COLS_SELECT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_LIST_COLS_URI_0 = "GUI_NEWRESOURCE_LIST_COLS_URI_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_LIST_SELECT_NAME_0 = "GUI_NEWRESOURCE_LIST_SELECT_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_PASTE_CSV_0 = "GUI_NEWRESOURCE_PASTE_CSV_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_PASTE_DATA_0 = "GUI_NEWRESOURCE_PASTE_DATA_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_POINTER_0 = "GUI_NEWRESOURCE_POINTER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_SELECT_FILE_0 = "GUI_NEWRESOURCE_SELECT_FILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_SELECT_TYPE_0 = "GUI_NEWRESOURCE_SELECT_TYPE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_SIBLING_0 = "GUI_NEWRESOURCE_SIBLING_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_TABLE_0 = "GUI_NEWRESOURCE_TABLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_UPLOAD_0 = "GUI_NEWRESOURCE_UPLOAD_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_UPLOAD_TYPE_0 = "GUI_NEWRESOURCE_UPLOAD_TYPE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_XMLCONTENT_0 = "GUI_NEWRESOURCE_XMLCONTENT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_XMLCONTENT_CHOOSEMODEL_0 = "GUI_NEWRESOURCE_XMLCONTENT_CHOOSEMODEL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_XMLCONTENT_NO_MODEL_0 = "GUI_NEWRESOURCE_XMLCONTENT_NO_MODEL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWRESOURCE_XMLPAGE_0 = "GUI_NEWRESOURCE_XMLPAGE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NEWSIBLING_NAME_0 = "GUI_NEWSIBLING_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NO_EDIT_REASON_HISTORY_0 = "GUI_NO_EDIT_REASON_HISTORY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NO_EDIT_REASON_LOCK_1 = "GUI_NO_EDIT_REASON_LOCK_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NO_EDIT_REASON_PERMISSION_0 = "GUI_NO_EDIT_REASON_PERMISSION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_POINTER_LINKTO_0 = "GUI_POINTER_LINKTO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PUBLISH_TOOLTIP_0 = "GUI_PUBLISH_TOOLTIP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_RESOURCE_KEEPPROPERTIES_0 = "GUI_RESOURCE_KEEPPROPERTIES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_RESOURCE_NAME_0 = "GUI_RESOURCE_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SELECT_DEFAULTBODY_0 = "GUI_SELECT_DEFAULTBODY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SELECT_TEMPLATE_0 = "GUI_SELECT_TEMPLATE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TITLE_EXPLORERTREE_0 = "GUI_TITLE_EXPLORERTREE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TITLE_LOCKED_0 = "GUI_TITLE_LOCKED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TITLE_NEWEXTENSION_0 = "GUI_TITLE_NEWEXTENSION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TITLE_NEWEXTENSIONFOR_0 = "GUI_TITLE_NEWEXTENSIONFOR_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TITLE_NEWFILEOTHER_0 = "GUI_TITLE_NEWFILEOTHER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TITLE_NEWPROPDEFFOR_0 = "GUI_TITLE_NEWPROPDEFFOR_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TITLE_NEWPROPERTYINFO_0 = "GUI_TITLE_NEWPROPERTYINFO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_ACTION_COUNT_0 = "GUI_UPLOADAPPLET_ACTION_COUNT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_ACTION_CREATE_0 = "GUI_UPLOADAPPLET_ACTION_CREATE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_ACTION_ERROR_0 = "GUI_UPLOADAPPLET_ACTION_ERROR_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_ACTION_OVERWRITECHECK_0 = "GUI_UPLOADAPPLET_ACTION_OVERWRITECHECK_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_ACTION_SELECT_0 = "GUI_UPLOADAPPLET_ACTION_SELECT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_ACTION_UPLOAD_0 = "GUI_UPLOADAPPLET_ACTION_UPLOAD_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_ERROR_CERT_MESSAGE_0 = "GUI_UPLOADAPPLET_ERROR_CERT_MESSAGE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_ERROR_CERT_TITLE_0 = "GUI_UPLOADAPPLET_ERROR_CERT_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_ERROR_LINE1_0 = "GUI_UPLOADAPPLET_ERROR_LINE1_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_ERROR_TITLE_0 = "GUI_UPLOADAPPLET_ERROR_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_MESSAGE_ADDING_0 = "GUI_UPLOADAPPLET_MESSAGE_ADDING_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_MESSAGE_ERROR_SIZE_0 = "GUI_UPLOADAPPLET_MESSAGE_ERROR_SIZE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_MESSAGE_ERROR_ZIP_0 = "GUI_UPLOADAPPLET_MESSAGE_ERROR_ZIP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_MESSAGE_NOPREVIEW_0 = "GUI_UPLOADAPPLET_MESSAGE_NOPREVIEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_MESSAGE_UPLOAD_0 = "GUI_UPLOADAPPLET_MESSAGE_UPLOAD_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_OVERWRITE_DIALOG_CANCEL_0 = "GUI_UPLOADAPPLET_OVERWRITE_DIALOG_CANCEL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_OVERWRITE_DIALOG_INTRO_0 = "GUI_UPLOADAPPLET_OVERWRITE_DIALOG_INTRO_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_OVERWRITE_DIALOG_OK_0 = "GUI_UPLOADAPPLET_OVERWRITE_DIALOG_OK_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOADAPPLET_OVERWRITE_DIALOG_TITLE_0 = "GUI_UPLOADAPPLET_OVERWRITE_DIALOG_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ADD_ACCESS_ENTRY_2 = "LOG_ADD_ACCESS_ENTRY_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ADD_MENU_ENTRY_2 = "LOG_ADD_MENU_ENTRY_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ADD_MENU_SEPARATOR_1 = "LOG_ADD_MENU_SEPARATOR_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ADD_PROP_1 = "LOG_ADD_PROP_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CREATE_CONTEXT_MENU_1 = "LOG_CREATE_CONTEXT_MENU_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MISSING_ACCESS_ENTRY_1 = "LOG_MISSING_ACCESS_ENTRY_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MISSING_SETTINGS_ENTRY_1 = "LOG_MISSING_SETTINGS_ENTRY_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_READ_GROUPS_OF_USER_FAILED_1 = "LOG_READ_GROUPS_OF_USER_FAILED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_REDIRECT_XMLPAGE_FAILED_1 = "LOG_REDIRECT_XMLPAGE_FAILED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_AUTO_NAV_1 = "LOG_SET_AUTO_NAV_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_AUTO_TITLE_1 = "LOG_SET_AUTO_TITLE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_ICON_1 = "LOG_SET_ICON_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_INFO_1 = "LOG_SET_INFO_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_KEY_1 = "LOG_SET_KEY_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_NAME_1 = "LOG_SET_NAME_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_NEW_RESOURCE_DESCRIPTION_IMAGE_1 = "LOG_SET_NEW_RESOURCE_DESCRIPTION_IMAGE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_NEW_RESOURCE_ORDER_1 = "LOG_SET_NEW_RESOURCE_ORDER_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_NEW_RESOURCE_URI_1 = "LOG_SET_NEW_RESOURCE_URI_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_PROP_DEFAULTS_2 = "LOG_SET_PROP_DEFAULTS_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_REFERENCE_1 = "LOG_SET_REFERENCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_SET_TITLE_KEY_1 = "LOG_SET_TITLE_KEY_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_WRONG_MENU_SEP_ORDER_0 = "LOG_WRONG_MENU_SEP_ORDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_WRONG_ORDER_CONTEXT_MENU_1 = "LOG_WRONG_ORDER_CONTEXT_MENU_1";

    /** Name of the resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.explorer.messages";

    /** End part of constant for creation NEWTITLE key. */
    private static final String GUI_STATE_POSTFIX = "_0";

    /**The  first part of constant for creation NEWTITLE key. */
    private static final String GUI_STATE_PREFIX = "GUI_EXPLORER_STATE";

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
     * Create state message key for resource name.<p>
     *
     * @param state resource state
     *
     * @return title message key to resource state
     *
     * @see org.opencms.file.CmsResource#getState()
     */
    public static String getStateKey(CmsResourceState state) {

        StringBuffer sb = new StringBuffer(GUI_STATE_PREFIX);
        sb.append(state.getState());
        sb.append(GUI_STATE_POSTFIX);
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