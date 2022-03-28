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

package org.opencms.ui.apps.logfile;

import org.opencms.main.CmsLog;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.CmsContextMenu.ContextMenuItem;
import org.opencms.ui.contextmenu.CmsContextMenu.ContextMenuItemClickEvent;
import org.opencms.ui.contextmenu.CmsContextMenu.ContextMenuItemClickListener;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.LoggerConfig;

import com.vaadin.event.MouseEvents;
import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.data.util.filter.Or;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.v7.ui.Table;

/**
 * Class for table to display and edit Log channels.<p>
 */
@SuppressWarnings("deprecation")
public class CmsLogChannelTable extends Table {

    /**
     * Table column generator for Level-buttons.<p>
     * */
    class LevelIcon implements Table.ColumnGenerator {

        /**vaadin serial id. */
        private static final long serialVersionUID = 7258796583481183276L;

        /**
         * @see com.vaadin.ui.Table.ColumnGenerator#generateCell(com.vaadin.ui.Table, java.lang.Object, java.lang.Object)
         */
        public Object generateCell(Table source, final Object itemId, Object columnId) {

            return ((LoggerLevel)(source.getItem(itemId).getItemProperty(columnId).getValue())).getLevelString();
        }

    }

    /**
     * Enumeration of Table Columns.<p>
     */
    enum TableColumn {

        /**Channel column.*/
        Channel(Messages.GUI_LOGFILE_LOGSETTINGS_CHANNEL_0, String.class, ""),

        /**Log file column. */
        File(Messages.GUI_LOGFILE_LOGSETTINGS_FILE_0, String.class, ""),

        /**Icon column.*/
        Icon(null, Resource.class, new CmsCssIcon(OpenCmsTheme.ICON_LOG)),

        /**Level column.*/
        Level(Messages.GUI_LOGFILE_LOGSETTINGS_LEVEL_0, LoggerLevel.class, null),

        /**Parent channel column.*/
        ParentChannel(Messages.GUI_LOGFILE_LOGSETTINGS_PARENT_CHANNEL_0, String.class, "");

        /**Default value for column.*/
        private Object m_defaultValue;

        /**Header Message key.*/
        private String m_headerMessage;

        /**Type of column property.*/
        private Class<?> m_type;

        /**
         * constructor.
         *
         * @param headerMessage key
         * @param type to property
         * @param defaultValue of column
         */
        TableColumn(String headerMessage, Class<?> type, Object defaultValue) {

            m_headerMessage = headerMessage;
            m_type = type;
            m_defaultValue = defaultValue;
        }

        /**
         * Returns list of all properties with non-empty header.<p>
         *
         * @return list of properties
         */
        static List<TableColumn> withHeader() {

            List<TableColumn> props = new ArrayList<TableColumn>();

            for (TableColumn prop : TableColumn.values()) {
                if (prop.m_headerMessage != null) {
                    props.add(prop);
                }
            }
            return props;
        }

        /**
         * Returns the default value of property.<p>
         *
         * @return object
         */
        Object getDefaultValue() {

            return m_defaultValue;
        }

        /**
         * Returns localized header.<p>
         *
         * @return string for header
         */
        String getLocalizedMessage() {

            if (m_headerMessage == null) {
                return "";
            }
            return CmsVaadinUtils.getMessageText(m_headerMessage);
        }

        /**
         * Returns tye of value for given property.<p>
         *
         * @return type
         */
        Class<?> getType() {

            return m_type;
        }

    }

    /**
     * Enumeration of Logger Level and corresponging icon paths.<p>
     */
    private enum LoggerLevel {

        /**Debug level.*/
        Debug(Level.DEBUG, OpenCmsTheme.TABLE_COLUMN_BOX_RED, null),

        /**Error level. */
        Error(Level.ERROR, OpenCmsTheme.TABLE_COLUMN_BOX_CYAN, "Default"),

        /**Fatal level.*/
        Fatal(Level.FATAL, OpenCmsTheme.TABLE_COLUMN_BOX_BLUE_LIGHT, null),

        /**Info level. */
        Info(Level.INFO, OpenCmsTheme.TABLE_COLUMN_BOX_ORANGE_DARK, null),

        /**Off level. */
        Off(Level.OFF, OpenCmsTheme.TABLE_COLUMN_BOX_GRAY, null),

        /**Warning level. */
        Warn(Level.WARN, OpenCmsTheme.TABLE_COLUMN_BOX_ORANGE, null);

        /**Caption for logger level.*/
        private String m_caption;

        /**CSS class.*/
        private String m_css;

        /**Corresponging log4j Level.*/
        private Level m_level;

        /**
         * constructor.<p>
         *
         * @param level of logger
         * @param css class
         * @param caption for the level
         */
        private LoggerLevel(Level level, String css, String caption) {

            m_css = css;
            m_level = level;
            m_caption = caption;
        }

        /**
         * Returns LoggerLevel object from given logger.<p>
         *
         * @param logger to fing enumeration object for
         * @return LoggerLevel
         */
        protected static LoggerLevel fromLogger(Logger logger) {

            for (LoggerLevel item : LoggerLevel.values()) {
                if (item.getLevel().equals(logger.getLevel())) {
                    return item;
                }
            }
            return null;
        }

        /**
         * Returns path to icon.<p>
         *
         * @return path to icon
         */
        protected String getCssClass() {

            return m_css;
        }

        /**
         * Returns level. <p>
         * @return log4j Level
         */
        protected Level getLevel() {

            return m_level;
        }

        /**
         * Returns the string representation for level.<p>
         *
         * @return string
         */
        protected String getLevelString() {

            if (m_caption == null) {
                String out = m_level.toString();
                return out.substring(0, 1).toUpperCase() + out.substring(1).toLowerCase();
            }
            return m_caption;
        }

        /**
         * Returns an extenden string representation with log level name added in case of having caption set.<p>
         *
         * @return string
         */
        protected String getLevelStringComplet() {

            if (m_caption == null) {
                return getLevelString();
            }
            String level = m_level.toString();
            level = level.substring(0, 1).toUpperCase() + level.substring(1).toLowerCase();
            return m_caption + " (" + level + ")";
        }

    }

    /** Channel name for logging logger configuration changes. */
    public static final String LOGCHANGES_NAME = "logchanges";

    /** Logger for logging logger configuration changes. */
    private static final Log LOGCHANGES = CmsLog.getLog(LOGCHANGES_NAME);

    /**vaadin serial id.*/
    private static final long serialVersionUID = 5467369614234190999L;

    /**Container holding table data. */
    private IndexedContainer m_container;

    /**Context menu. */
    private CmsContextMenu m_menu;

    /**Instance of the app */
    private CmsLogFileApp m_app;

    /**
     * constructor.<p>
     * @param app the app instance
     */
    protected CmsLogChannelTable(CmsLogFileApp app) {

        m_app = app;
        m_container = new IndexedContainer();

        setContainerDataSource(m_container);

        for (TableColumn prop : TableColumn.values()) {
            m_container.addContainerProperty(prop, prop.getType(), prop.getDefaultValue());
            setColumnHeader(prop, prop.getLocalizedMessage());
        }

        setVisibleColumns(TableColumn.Level, TableColumn.Channel, TableColumn.ParentChannel, TableColumn.File);

        setItemIconPropertyId(TableColumn.Icon);
        setColumnWidth(null, 40);
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);

        setColumnWidth(TableColumn.Level, 80);

        setSelectable(true);
        setMultiSelect(true);
        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);
        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent event) {

                onItemClick(event, event.getItemId(), event.getPropertyId());
            }
        });
        setCellStyleGenerator(new CellStyleGenerator() {

            private static final long serialVersionUID = 1L;

            public String getStyle(Table source, Object itemId, Object propertyId) {

                if (TableColumn.Channel.equals(propertyId)) {
                    return " " + OpenCmsTheme.HOVER_COLUMN;
                }

                if (TableColumn.Level.equals(propertyId)) {
                    return ((LoggerLevel)source.getItem(itemId).getItemProperty(propertyId).getValue()).getCssClass();
                }

                return null;
            }
        });

        addGeneratedColumn(TableColumn.Level, new LevelIcon());

        fillTable();
    }

    /**
     * Adds a container item for the given logger.<p>
     *
     * @param logger the logger for which to generate a container item
     */
    public void addItemForLogger(Logger logger) {

        Item item = m_container.addItem(logger);
        if (item != null) {
            item.getItemProperty(TableColumn.Channel).setValue(logger.getName());
            String parentChannelName = getParentLogChannelName(logger);
            item.getItemProperty(TableColumn.ParentChannel).setValue(
                parentChannelName != null ? parentChannelName : "none");
            item.getItemProperty(TableColumn.File).setValue(m_app.getLogFile(logger));
            item.getItemProperty(TableColumn.Level).setValue(LoggerLevel.fromLogger(logger));
        }
    }

    /**
     * Filters the table according to given search string.<p>
     *
     * @param search string to be looked for.
     */
    @SuppressWarnings("unchecked")
    public void filterTable(String search) {

        m_container.removeAllContainerFilters();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(search)) {
            m_container.addContainerFilter(
                new Or(
                    new SimpleStringFilter(TableColumn.Channel, search, true, false),
                    new SimpleStringFilter(TableColumn.ParentChannel, search, true, false),
                    new SimpleStringFilter(TableColumn.File, search, true, false)));
        }
        if ((getValue() != null) & !((Set<Logger>)getValue()).isEmpty()) {
            setCurrentPageFirstItemId(((Set<Logger>)getValue()).iterator().next());
        }
    }

    /**
     * Toggles the log file of a given log channel.<p>
     *
     * @param logchannel to set or remove log file to
     */
    protected void toggleOwnFile(Logger logchannel) {

        m_app.toggleOwnFileForLogger(logchannel);
        m_app.updateTable();
    }

    /**
     * Sets a given Level to a Set of Loggers.<p>
     *
     * @param clickedLevel to be set
     * @param clickedLogger to get level changed
     */
    void changeLoggerLevel(LoggerLevel clickedLevel, Set<Logger> clickedLogger) {

        for (Logger logger : clickedLogger) {

            logLogLevelChange(logger, clickedLevel.getLevel());
            logger.setLevel(clickedLevel.getLevel());
            m_app.writeElement(logger);
        }
        m_app.updateTable();
    }

    /**
     * Handles the table item clicks, including clicks on images inside of a table item.<p>
     *
     * @param event the click event
     * @param itemId of the clicked row
     * @param propertyId column id
     */
    @SuppressWarnings("unchecked")
    void onItemClick(MouseEvents.ClickEvent event, Object itemId, Object propertyId) {

        if (!event.isCtrlKey() && !event.isShiftKey()) {

            if (event.getButton().equals(MouseButton.LEFT)) {
                setValue(null);
            }
            changeValueIfNotMultiSelect(itemId);
            // don't interfere with multi-selection using control key
            if (event.getButton().equals(MouseButton.RIGHT) || (propertyId == null)) {
                m_menu.removeAllItems();
                fillContextMenu((Set<Logger>)getValue());
                m_menu.openForTable(event, itemId, propertyId, this);
            }

        }
    }

    /**
     * Checks value of table and sets it new if needed:<p>
     * if multiselect: new itemId is in current Value? -> no change of value<p>
     * no multiselect and multiselect, but new item not selected before: set value to new item<p>
     *
     * @param itemId if of clicked item
     */
    private void changeValueIfNotMultiSelect(Object itemId) {

        @SuppressWarnings("unchecked")
        Set<String> value = (Set<String>)getValue();
        if (value == null) {
            select(itemId);
        } else if (!value.contains(itemId)) {
            setValue(null);
            select(itemId);
        }
    }

    /**
     * Fills the context menu.<p>
     *
     * @param loggerSet Set of logger to open context menu for
     */
    private void fillContextMenu(final Set<Logger> loggerSet) {

        for (LoggerLevel level : LoggerLevel.values()) {
            final LoggerLevel currentLevel = level;
            ContextMenuItem item = m_menu.addItem(level.getLevelStringComplet());
            item.setData(loggerSet);
            item.addItemClickListener(new ContextMenuItemClickListener() {

                public void contextMenuItemClicked(ContextMenuItemClickEvent event) {

                    changeLoggerLevel(currentLevel, loggerSet);

                }
            });
            if (loggerSet.size() == 1) {
                if (level.getLevel().equals(loggerSet.iterator().next().getLevel())) {
                    item.setIcon(FontOpenCms.CHECK_SMALL);
                }
            }
        }
        if (loggerSet.size() == 1) {
            String message = CmsVaadinUtils.getMessageText(Messages.GUI_LOGFILE_LOGSETTINGS_NEWFILE_0);
            if (CmsLogFileApp.isloggingactivated(loggerSet.iterator().next())) {
                message = CmsVaadinUtils.getMessageText(Messages.GUI_LOGFILE_LOGSETTINGS_REMOVEFILE_0);
            }
            ContextMenuItem item = m_menu.addItem(message);
            item.setData(loggerSet);
            item.addItemClickListener(new ContextMenuItemClickListener() {

                public void contextMenuItemClicked(ContextMenuItemClickEvent event) {

                    toggleOwnFile(loggerSet.iterator().next());

                }
            });
        }
    }

    /**
     * Populate table.<p>
     */
    private void fillTable() {

        removeAllItems();
        for (Logger logger : m_app.getAllElements()) {
            addItemForLogger(logger);
        }
    }

    /**
     * Gets the parent log channel name of a logger.
     *
     * @param logger the logger
     * @return the parent log channel name
     */
    private String getParentLogChannelName(Logger logger) {

        LoggerConfig parentConfig = null;
        if (logger.getName().equals(logger.get().getName())) {
            parentConfig = logger.get().getParent();
        } else {
            parentConfig = logger.get();
        }
        return parentConfig != null ? parentConfig.getName() : null;
    }

    /**
     * Helper method for logging user actions.
     *
     * @param message the message to write
     */
    private void log(String message) {

        String user = A_CmsUI.getCmsObject().getRequestContext().getCurrentUser().getName();
        LOGCHANGES.info("[User: " + user + "] " + message);
    }

    /**
     * Logs a log level change.
     *
     * @param logger the logger
     * @param newLevel the new level to be set on the logger
     */
    private void logLogLevelChange(Logger logger, Level newLevel) {

        String oldLevelDesc = null;
        LoggerConfig config = logger.get();
        if (logger.getName().equals(config.getName())) {
            oldLevelDesc = config.getLevel().toString();
        } else {
            oldLevelDesc = config.getLevel().toString() + " (inherited from '" + config.getName() + "')";
        }
        log("Switching channel '" + logger.getName() + "' from " + oldLevelDesc + " to " + newLevel.toString());
    }

}
