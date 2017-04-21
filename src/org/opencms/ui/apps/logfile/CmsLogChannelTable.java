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

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.CmsContextMenu.ContextMenuItem;
import org.opencms.ui.contextmenu.CmsContextMenu.ContextMenuItemClickEvent;
import org.opencms.ui.contextmenu.CmsContextMenu.ContextMenuItemClickListener;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Image;
import com.vaadin.ui.Table;

/**
 * Class for table to display and edit Log channels.<p>
 */
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

            Image image = new Image(
                "",
                new ExternalResource(
                    OpenCmsTheme.getImageLink(
                        ((LoggerLevel)source.getItem(itemId).getItemProperty(
                            TableColumn.Level).getValue()).getPath())));
            image.addClickListener(new ClickListener() {

                private static final long serialVersionUID = -7281805014773351493L;

                public void click(ClickEvent event) {

                    onItemClick(event, itemId, TableColumn.Level);

                }
            });
            return image;
        }

    }

    /**
     * Enumeration of Table Columns.<p>
     */
    enum TableColumn {

        /**Level column.*/
        Level(Messages.GUI_LOGFILE_LOGSETTINGS_LEVEL_0, LoggerLevel.class, null),

        /**Channel column.*/
        Channel(Messages.GUI_LOGFILE_LOGSETTINGS_CHANNEL_0, String.class, ""),

        /**Parent channel column.*/
        ParentChannel(Messages.GUI_LOGFILE_LOGSETTINGS_PARENT_CHANNEL_0, String.class, ""),

        /**Log file column. */
        File(Messages.GUI_LOGFILE_LOGSETTINGS_FILE_0, String.class, "");

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

        /**Fatal level.*/
        Fatal(Level.FATAL, "apps/log/levelbuttons/log_button_fatal_a_2.png"),

        /**Error level. */
        Error(Level.ERROR, "apps/log/levelbuttons/log_button_error_a_2.png"),

        /**Warning level. */
        Warn(Level.WARN, "apps/log/levelbuttons/log_button_warn_a_2.png"),

        /**Info level. */
        Info(Level.INFO, "apps/log/levelbuttons/log_button_info_a_2.png"),

        /**Debug level.*/
        Debug(Level.DEBUG, "apps/log/levelbuttons/log_button_debug_a_2.png"),

        /**Off level. */
        Off(Level.OFF, "apps/log/levelbuttons/log_button_off_a_2.png");

        /**Path to icon.*/
        private String m_path;

        /**Corresponging log4j Level.*/
        private Level m_level;

        /**
         * constructor.<p>
         *
         * @param level of logger
         * @param path to icon
         */
        private LoggerLevel(Level level, String path) {
            m_path = path;
            m_level = level;
        }

        /**
         * Returns LoggerLevel object from given logger.<p>
         *
         * @param logger to fing enumeration object for
         * @return LoggerLevel
         */
        protected static LoggerLevel fromLogger(Logger logger) {

            for (LoggerLevel item : LoggerLevel.values()) {
                if (item.getLevel().equals(logger.getEffectiveLevel())) {
                    return item;
                }
            }
            return null;
        }

        /**
         * Returns level. <p>
         * @return log4j Level
         */
        protected Level getLevel() {

            return m_level;
        }

        /**
         * Returns path to icon.<p>
         *
         * @return path to icon
         */
        protected String getPath() {

            return m_path;
        }
    }

    /**vaadin serial id.*/
    private static final long serialVersionUID = 5467369614234190999L;

    /** The prefix of opencms classes. */
    private static final String OPENCMS_CLASS_PREFIX = "org.opencms";

    /**Container holding table data. */
    private IndexedContainer m_container;

    /**Context menu. */
    private CmsContextMenu m_menu;

    /**
     * constructor.<p>
     */
    protected CmsLogChannelTable() {
        m_container = new IndexedContainer();

        setContainerDataSource(m_container);

        for (TableColumn prop : TableColumn.values()) {
            m_container.addContainerProperty(prop, prop.getType(), prop.getDefaultValue());
            setColumnHeader(prop, prop.getLocalizedMessage());
        }

        setVisibleColumns(TableColumn.Level, TableColumn.Channel, TableColumn.ParentChannel, TableColumn.File);

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
                return null;
            }
        });

        addGeneratedColumn(TableColumn.Level, new LevelIcon());

        fillTable();
    }

    /**
     * Filters the table according to given search string.<p>
     *
     * @param search string to be looked for.
     */
    public void filterTable(String search) {

        m_container.removeAllContainerFilters();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(search)) {
            m_container.addContainerFilter(
                new Or(
                    new SimpleStringFilter(TableColumn.Channel, search, true, false),
                    new SimpleStringFilter(TableColumn.ParentChannel, search, true, false),
                    new SimpleStringFilter(TableColumn.File, search, true, false)));
        }
    }

    /**
     * Simple check if the logger has the global log file <p> or a single one.
     *
     * @param logchannel the channel that has do be checked
     * @return true if the the log channel has a single log file
     * */
    protected boolean isloggingactivated(Logger logchannel) {

        boolean check = false;
        @SuppressWarnings("unchecked")
        List<Appender> appenders = Collections.list(logchannel.getAllAppenders());
        Iterator<Appender> app = appenders.iterator();
        while (app.hasNext()) {
            check = app.next().getName().equals(logchannel.getName());
        }
        return check;
    }

    /**
     * Toggles the log file of a given log channel.<p>
     *
     * @param logchannel to set or remove log file to
     */
    protected void toggleOwnFile(Logger logchannel) {

        String filepath = "";

        Layout layout = null;
        // if the button is activated check the value of the button
        // the button was active
        if (isloggingactivated(logchannel)) {
            // remove the private Appender from logger
            logchannel.removeAllAppenders();
            // activate the heredity so the logger get the appender from parent logger
            logchannel.setAdditivity(true);

        }
        // the button was inactive
        else {
            @SuppressWarnings("unchecked")
            List<Appender> rootAppenders = Collections.list(Logger.getRootLogger().getAllAppenders());
            // get the layout and file path from root logger
            for (Appender appender : rootAppenders) {
                if (appender instanceof FileAppender) {
                    FileAppender fapp = (FileAppender)appender;
                    filepath = fapp.getFile().substring(0, fapp.getFile().lastIndexOf(File.separatorChar));
                    layout = fapp.getLayout();
                    break;
                }
            }

            @SuppressWarnings("unchecked")
            List<Appender> appenders = Collections.list(logchannel.getAllAppenders());
            // check if the logger has an Appender get his layout
            for (Appender appender : appenders) {
                if (appender instanceof FileAppender) {
                    FileAppender fapp = (FileAppender)appender;
                    layout = fapp.getLayout();
                    break;
                }
            }
            String logfilename = "";
            String temp = logchannel.getName();
            // check if the logger name begins with "org.opencms"
            if (logchannel.getName().contains(OPENCMS_CLASS_PREFIX)) {
                // remove the prefix "org.opencms" from logger name to generate the file name
                temp = temp.replace(OPENCMS_CLASS_PREFIX, "");
                // if the name has suffix
                if (temp.length() >= 1) {
                    logfilename = filepath + File.separator + "opencms-" + temp.substring(1).replace(".", "-") + ".log";
                }
                // if the name has no suffix
                else {
                    logfilename = filepath + File.separator + "opencms" + temp.replace(".", "-") + ".log";
                }
            }
            // if the logger name not begins with "org.opencms"
            else {
                logfilename = filepath + File.separator + "opencms-" + temp.replace(".", "-") + ".log";
            }

            FileAppender fapp = null;
            try {
                // create new FileAppender for separate log file
                fapp = new FileAppender(layout, logfilename, true);
                // set the log file e.g.: "C:\tomcat6\webapps\opencms\WEB-INF\logs"
                fapp.setName(logchannel.getName());
            } catch (IOException e) {
                //
            }
            // deactivate the heredity so the logger get no longer the appender from parent logger
            logchannel.setAdditivity(false);
            // remove all active Appenders from logger
            logchannel.removeAllAppenders();
            // add the new created Appender to the logger
            logchannel.addAppender(fapp);
        }
        updateFile();
    }

    /**
     * Sets a given Level to a Set of Loggers.<p>
     *
     * @param clickedLevel to be set
     * @param clickedLogger to get level changed
     */
    void changeLoggerLevel(LoggerLevel clickedLevel, Set<Logger> clickedLogger) {

        for (Logger loggerItem : clickedLogger) {
            loggerItem.setLevel(clickedLevel.getLevel());
        }
        updateLevel();
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
            if (event.getButton().equals(MouseButton.RIGHT) || (propertyId == TableColumn.Level)) {
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
            ContextMenuItem item = m_menu.addItem(level.name());
            item.setData(loggerSet);
            item.addItemClickListener(new ContextMenuItemClickListener() {

                public void contextMenuItemClicked(ContextMenuItemClickEvent event) {

                    changeLoggerLevel(currentLevel, loggerSet);

                }
            });
            if (loggerSet.size() == 1) {
                if (level.getLevel().equals(loggerSet.iterator().next().getEffectiveLevel())) {
                    item.setIcon(FontOpenCms.CHECK_SMALL);
                }
            }
        }
        if (loggerSet.size() == 1) {
            String message = CmsVaadinUtils.getMessageText(Messages.GUI_LOGFILE_LOGSETTINGS_NEWFILE_0);
            if (isloggingactivated(loggerSet.iterator().next())) {
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

        List<Logger> loggerList = CmsLogFileApp.getLoggers();

        for (Logger logger : loggerList) {
            Item item = m_container.addItem(logger);
            item.getItemProperty(TableColumn.Channel).setValue(logger.getName());
            item.getItemProperty(TableColumn.ParentChannel).setValue(logger.getParent().getName());
            item.getItemProperty(TableColumn.File).setValue(getLogFiles(logger));
            item.getItemProperty(TableColumn.Level).setValue(LoggerLevel.fromLogger(logger));
        }
    }

    /**
     * Returns log files to given Logger.<p>
     *
     * @param logger to read files for
     * @return path of file
     */
    private String getLogFiles(Logger logger) {

        String test = "";
        @SuppressWarnings("unchecked")
        List<Appender> appenders = Collections.list(logger.getAllAppenders());
        Iterator<Appender> appendersIt = appenders.iterator();
        int count = 0;
        // select the Appender from logger
        while (appendersIt.hasNext()) {
            Appender appender = appendersIt.next();
            // only use file appenders
            if (appender instanceof FileAppender) {
                FileAppender fapp = (FileAppender)appender;
                String temp = "";
                temp = fapp.getFile().substring(fapp.getFile().lastIndexOf(File.separatorChar) + 1);
                test = test + temp;
                count++;
                break;
            }
        }
        @SuppressWarnings("unchecked")
        List<Appender> parentAppenders = Collections.list(logger.getParent().getAllAppenders());
        Iterator<Appender> parentAppendersIt = parentAppenders.iterator();
        // if no Appender found from logger, select the Appender from parent logger
        if (count == 0) {
            while (parentAppendersIt.hasNext()) {
                Appender appender = parentAppendersIt.next();
                // only use file appenders
                if (appender instanceof FileAppender) {
                    FileAppender fapp = (FileAppender)appender;
                    String temp = "";
                    temp = fapp.getFile().substring(fapp.getFile().lastIndexOf(File.separatorChar) + 1);
                    test = test + temp;
                    count++;
                    break;
                }
            }
        }

        if (count == 0) {
            @SuppressWarnings("unchecked")
            List<Appender> rootAppenders = Collections.list(Logger.getRootLogger().getAllAppenders());
            Iterator<Appender> rootAppendersIt = rootAppenders.iterator();
            // if no Appender found from parent logger, select the Appender from root logger
            while (rootAppendersIt.hasNext()) {
                Appender appender = rootAppendersIt.next();
                // only use file appenders
                if (appender instanceof FileAppender) {
                    FileAppender fapp = (FileAppender)appender;
                    String temp = "";
                    temp = fapp.getFile().substring(fapp.getFile().lastIndexOf(File.separatorChar) + 1);
                    test = test + temp;
                    break;
                }
            }

        }
        return test;
    }

    /**
     * Updates table after change of file.<p>
     */
    private void updateFile() {

        @SuppressWarnings("unchecked")
        List<Logger> logger = (List<Logger>)m_container.getItemIds();

        for (Logger item : logger) {
            m_container.getItem(item).getItemProperty(TableColumn.File).setValue(getLogFiles(item));
        }
        resetPageBuffer();
        refreshRenderedCells();
        refreshRowCache();
    }

    /**
     * Updates table after change of Level.<p>
     */
    private void updateLevel() {

        @SuppressWarnings("unchecked")
        List<Logger> logger = (List<Logger>)m_container.getItemIds();

        for (Logger item : logger) {
            m_container.getItem(item).getItemProperty(TableColumn.Level).setValue(LoggerLevel.fromLogger(item));
        }
        resetPageBuffer();
        refreshRenderedCells();
        refreshRowCache();
    }
}
