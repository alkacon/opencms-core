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

package org.opencms.db.log;

import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsIllegalArgumentException;

import java.util.Locale;

/**
 * Wrapper class for the different types of log entries.<p>
 *
 * The IDs are organized as following:
 * <dl>
 *   <dt>1-3</dt>
 *   <dd>user actions (login successful, login failed, resource visited)</dd>
 *   <dt>11-13</dt>
 *   <dd>publish actions (modified, new, deleted)</dd>
 *   <dt>15</dt>
 *   <dd>publish list (hide)</dd>
 *   <dt>21-24</dt>
 *   <dd>resource additional information (relations, permissions, properties)</dd>
 *   <dt>30</dt>
 *   <dd>content changes</dd>
 *   <dt>31-35</dt>
 *   <dd>resource attributes (date expired, released, last modified; type, flags)</dd>
 *   <dt>40-45</dt>
 *   <dd>structure operations (create, copy, delete, move, import)</dd>
 *   <dt>50-54</dt>
 *   <dd>resource recovery (history, restore, undelete, undo changes)</dd>
 * </dl>
 *
 * @since 8.0.0
 */
public enum CmsLogEntryType {

    /** Resource add relation entry. */
    RESOURCE_ADD_RELATION(21, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_FROM_TO_2),
    /** Resource cloned entry. */
    RESOURCE_CLONED(42, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_FROM_TO_2),
    /** Resource content modified entry. */
    RESOURCE_CONTENT_MODIFIED(30, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource copied entry. */
    RESOURCE_COPIED(41, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_FROM_TO_2),
    /** Resource created entry. */
    RESOURCE_CREATED(40, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource set date expired entry. */
    RESOURCE_DATE_EXPIRED(31, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource set date released entry. */
    RESOURCE_DATE_RELEASED(32, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource deleted entry. */
    RESOURCE_DELETED(43, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource set flags entry. */
    RESOURCE_FLAGS(35, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource hidden from publish list entry. */
    RESOURCE_HIDDEN(15, LogLevel.WARN, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** New resource deleted. */
    RESOURCE_NEW_DELETED(16, LogLevel.WARN, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource restored from history entry. */
    RESOURCE_HISTORY(50, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource imported entry. */
    RESOURCE_IMPORTED(45, LogLevel.DEBUG, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource undo changes with mov entry. */
    RESOURCE_MOVE_RESTORED(53, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_FROM_TO_2),
    /** Resource moved entry. */
    RESOURCE_MOVED(44, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_FROM_TO_2),
    /** Resource set permissions entry. */
    RESOURCE_PERMISSIONS(23, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource set properties entry. */
    RESOURCE_PROPERTIES(24, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource published deleted entry. */
    RESOURCE_PUBLISHED_DELETED(13, LogLevel.FATAL, false, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource published modified entry. */
    RESOURCE_PUBLISHED_MODIFIED(11, LogLevel.FATAL, false, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource published new entry. */
    RESOURCE_PUBLISHED_NEW(12, LogLevel.FATAL, false, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Undo changes entry. */
    RESOURCE_CHANGES_UNDONE(14, LogLevel.FATAL, false, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource remove relation entry. */
    RESOURCE_REMOVE_RELATION(22, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_AND_RELATION_FILTER_2),
    /** Resource restore deleted entry. */
    RESOURCE_RESTORE_DELETED(51, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource undo changes without move entry. */
    RESOURCE_RESTORED(54, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource set date last modified entry. */
    RESOURCE_TOUCHED(33, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource set type entry. */
    RESOURCE_TYPE(34, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1),
    /** Resource undeleted entry. */
    RESOURCE_UNDELETED(52, LogLevel.INFO, true, Messages.GUI_LOG_ENTRY_DETAIL_PATH_1);

    /**
     * Log level.<p>
     */
    private enum LogLevel {

        /** Second Highest level. */
        DEBUG,
        /** Second lowest level. */
        ERROR,
        /** Lowest level. */
        FATAL,
        /** Normal level. */
        INFO,
        /** Highest level. */
        TRACE,
        /** Less than normal level. */
        WARN;
    }

    /** Localization key for detail formatting. */
    private String m_detailKey;

    /** Internal representation. */
    private final int m_id;

    /** Flag to indicate if this type generates an entry in the user's publish list. */
    private boolean m_toPubList;

    /**
     * Public constructor.<p>
     *
     * @param id the id of the log entry type
     * @param logLevel the activation level
     * @param toPubList flag to indicate if this type generates an entry in the user's publish list
     * @param detailKey localization key for detail formatting
     */
    private CmsLogEntryType(int id, LogLevel logLevel, boolean toPubList, String detailKey) {

        m_id = id;
        m_toPubList = toPubList;
        m_detailKey = detailKey;
    }

    /**
     * Parses an <code>int</code> into a log entry type.<p>
     *
     * @param id the internal representation number to parse
     *
     * @return the enumeration element
     *
     * @throws CmsIllegalArgumentException if the given value could not be matched against a
     *         <code>{@link CmsLogEntryType}</code> object.
     */
    public static CmsLogEntryType valueOf(int id) throws CmsIllegalArgumentException {

        for (CmsLogEntryType type : CmsLogEntryType.values()) {
            if (id == type.getId()) {
                return type;
            }
        }
        throw new CmsIllegalArgumentException(
            org.opencms.db.Messages.get().container(
                org.opencms.db.Messages.ERR_MODE_ENUM_PARSE_2,
                Integer.valueOf(id),
                CmsLogEntryType.class.getName()));
    }

    /**
     * Localization key for detail formatting.<p>
     *
     * @return localization key for detail formatting
     */
    public String getDetailKey() {

        return m_detailKey;
    }

    /**
     * Returns the internal representation of this type.<p>
     *
     * @return the internal representation of this type
     */
    public int getId() {

        return m_id;
    }

    /**
     * Returns a localized name for the given log entry type.<p>
     *
     * @param messages the message bundle to use to resolve the name
     *
     * @return a localized name
     */
    public String getLocalizedName(CmsMessages messages) {

        String nameKey = "GUI_LOG_ENTRY_TYPE_" + name() + "_0";
        return messages.key(nameKey);
    }

    /**
     * Returns a localized name for the given log entry type.<p>
     *
     * @param locale the locale
     *
     * @return a localized name
     */
    public String getLocalizedName(Locale locale) {

        return getLocalizedName(Messages.get().getBundle(locale));
    }

    /**
     * Checks if this log entry type is active or not.<p>
     *
     * @return <code>true</code> if this log entry type is active or not
     */
    public boolean isActive() {

        return true;
    }

    /**
     * Check if this type generates an entry in the user's publish list.<p>
     *
     * @return <code>true</code> if this type generates an entry in the user's publish list
     */
    public boolean isToPubList() {

        return m_toPubList;
    }
}
