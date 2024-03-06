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

package org.opencms.workplace.tools.workplace.logging;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListSearchAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * Main logging management list view.<p>
 *
 * Defines the list columns and possible actions for logging.<p>
 *
 */
public class CmsLog4JAdminDialog extends A_CmsListDialog {

    /** List column class. */
    protected static final String COLUMN_ACTIVE = "cac";

    /** Name of the list for separate log files. */
    protected static final String COLUMN_CHANGE_LOGFILE = "Seperate Log file";

    /** Name of the logger-List. */
    protected static final String COLUMN_CHANNELS = "channels";

    /** Shortcut for the DEBUG column. */
    protected static final String COLUMN_DEBUG = "chd";

    /** Shortcut for the ERROR column. */
    protected static final String COLUMN_ERROR = "che";

    /** Shortcut for the FATAL column. */
    protected static final String COLUMN_FATAL = "chf";

    /** Shortcut for the INFO column. */
    protected static final String COLUMN_INFO = "chi";

    /** Name of the parent-list. */
    protected static final String COLUMN_LOG_FILE = "Log-File";

    /** Actual level of logging. */
    protected static final String COLUMN_LOG_LEVEL = "loggerlevel";

    /** Shortcut for the OFF column. */
    protected static final String COLUMN_OFF = "cho";

    /** Name of the parent-list. */
    protected static final String COLUMN_PARENT_CHANNELS = "parent";

    /** Shortcut for the WARN column. */
    protected static final String COLUMN_WARN = "chw";

    /** Name of the logchannel Action. */
    private static final String ACTION_ACTIVATE_LOGFILE = "activlogchannel";

    /** Name of the Debug-level Action. */
    private static final String ACTION_LOGGING_LEVEL_DEBUG = "debugchannel";

    /** Name of the Error-level Action. */
    private static final String ACTION_LOGGING_LEVEL_ERROR = "errorlevel";

    /** Name of the Fatal-level Action. */
    private static final String ACTION_LOGGING_LEVEL_FATAL = "fatallevel";

    /** Name of the Info-level Action. */
    private static final String ACTION_LOGGING_LEVEL_INFO = "infochannel";

    /** Name of the Off-level Action. */
    private static final String ACTION_LOGGING_LEVEL_OFF = "offlevel";

    /** Name of the Warn-level Action. */
    private static final String ACTION_LOGGING_LEVEL_WARN = "warnchannel";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLog4JAdminDialog.class);

    /** The prefix of opencms classes. */
    private static final String OPENCMS_CLASS_PREFIX = "org.opencms";

    /** Path to the list buttons. */
    private static final String PATH_BUTTONS = "buttons/";

    /** Path to the Debug-Icon(active). */
    private static final String PATH_DEBUG_ACTIVE = PATH_BUTTONS + "log_button_debug_a_2.png";

    /** Path to the Debug-Icon(inactive). */
    private static final String PATH_DEBUG_INACTIVE = PATH_BUTTONS + "log_button_debug_i_2.png";

    /** Path to the Error-Icon(active). */
    private static final String PATH_ERROR_ACTIVE = PATH_BUTTONS + "log_button_error_a_2.png";

    /** Path to the Error-Icon(inactive). */
    private static final String PATH_ERROR_INACTIVE = PATH_BUTTONS + "log_button_error_i_2.png";

    /** Path to the Fatal-Icon(active). */
    private static final String PATH_FATAL_ACTIVE = PATH_BUTTONS + "log_button_fatal_a_2.png";

    /** Path to the Fatal-Icon(inactive). */
    private static final String PATH_FATAL_INACTIVE = PATH_BUTTONS + "log_button_fatal_i_2.png";

    /** Path to the File-Icon(active). */
    private static final String PATH_FILE_ACTIVE = PATH_BUTTONS + "log_button_file_on_2.png";

    /** Path to the File-Icon(inactive). */
    private static final String PATH_FILE_INACTIVE = PATH_BUTTONS + "log_button_file_off_2.png";

    /** Path to the Info-Icon(active). */
    private static final String PATH_INFO_ACTIVE = PATH_BUTTONS + "log_button_info_a_2.png";

    /** Path to the Info-Icon(inactive). */
    private static final String PATH_INFO_INACTIVE = PATH_BUTTONS + "log_button_info_i_2.png";

    /** Path to the Off-Icon(active). */
    private static final String PATH_OFF_ACTIVE = PATH_BUTTONS + "log_button_off_a_2.png";

    /** Path to the Off-Icon(inactive). */
    private static final String PATH_OFF_INACTIVE = PATH_BUTTONS + "log_button_off_i_2.png";

    /** Path to the Warning-Icon(active). */
    private static final String PATH_WARN_ACTIVE = PATH_BUTTONS + "log_button_warn_a_2.png";

    /** Path to the Warning-Icon(inactive). */
    private static final String PATH_WARN_INACTIVE = PATH_BUTTONS + "log_button_warn_i_2.png";

    /** Shortcut for the MultiAction (DEBUG). */
    private static final String SET_TO_DEBUG = "sd";

    /** Shortcut for the MultiAction (ERROR). */
    private static final String SET_TO_ERROR = "sr";

    /** Shortcut for the MultiAction (FATAL). */
    private static final String SET_TO_FATAL = "sf";

    /** Shortcut for the MultiAction (INFO). */
    private static final String SET_TO_INFO = "si";

    /** Shortcut for the MultiAction (OFF). */
    private static final String SET_TO_OFF = "so";

    /** Shortcut for the MultiAction (WARN). */
    private static final String SET_TO_WARN = "sw";

    /**
     * Public constructor.<p>
     *
     * @param jsp the JSP action element
     */
    public CmsLog4JAdminDialog(CmsJspActionElement jsp) {

        super(
            jsp,
            "Log Settings",
            Messages.get().container(Messages.GUI_LOG_LIST_NAME_0),
            "lo",
            CmsListOrderEnum.ORDER_ASCENDING,
            null);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() throws CmsRuntimeException {

        // switch the selected loggers to the logging-level

        Level newLogchannelLevel = null;
        // get the selected logging-level
        if (getParamListAction().equals(SET_TO_ERROR)) {
            newLogchannelLevel = Level.ERROR;
        } else if (getParamListAction().equals(SET_TO_DEBUG)) {
            newLogchannelLevel = Level.DEBUG;
        } else if (getParamListAction().equals(SET_TO_FATAL)) {
            newLogchannelLevel = Level.FATAL;
        } else if (getParamListAction().equals(SET_TO_INFO)) {
            newLogchannelLevel = Level.INFO;
        } else if (getParamListAction().equals(SET_TO_WARN)) {
            newLogchannelLevel = Level.WARN;
        } else if (getParamListAction().equals(SET_TO_OFF)) {
            newLogchannelLevel = Level.OFF;
        }

        // list all selected log channels
        List<CmsListItem> list = getSelectedItems();
        Iterator<CmsListItem> itItems = list.iterator();
        // iterate about all log channels to set the above selected loglevel
        while (itItems.hasNext()) {
            String logchannnelName = itItems.next().getId();
            Configurator.setLevel(logchannnelName, newLogchannelLevel);
        }

        refreshList();

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws CmsRuntimeException {

        // switch a single log-channel to an other logging-level
        if (getSelectedItem() == null) {
            return;
        }
        // get the log-channel to change the level
        String logchannnelName = getSelectedItem().getId();
        Logger logchannel = getLoggerImpl(logchannnelName);
        if (logchannel == null) {
            return;
        }
        // get the selected logging-level with the help of the row
        Level newLogchannelLevel = null;
        if (ACTION_LOGGING_LEVEL_DEBUG.equals(getParamListAction())) {
            newLogchannelLevel = Level.DEBUG;
        } else if (ACTION_LOGGING_LEVEL_INFO.equals(getParamListAction())) {
            newLogchannelLevel = Level.INFO;
        } else if (ACTION_LOGGING_LEVEL_WARN.equals(getParamListAction())) {
            newLogchannelLevel = Level.WARN;
        } else if (ACTION_LOGGING_LEVEL_ERROR.equals(getParamListAction())) {
            newLogchannelLevel = Level.ERROR;
        } else if (ACTION_LOGGING_LEVEL_FATAL.equals(getParamListAction())) {
            newLogchannelLevel = Level.FATAL;
        } else if (ACTION_LOGGING_LEVEL_OFF.equals(getParamListAction())) {
            newLogchannelLevel = Level.OFF;
        }
        if (newLogchannelLevel != null) {
            isparentlogger(logchannel);
            if (newLogchannelLevel.equals(logchannel.getParent().getLevel())) {
                Configurator.setLevel(logchannnelName, (Level)null);
            } else {
                Configurator.setLevel(logchannnelName, newLogchannelLevel);
            }
        }
        if (ACTION_ACTIVATE_LOGFILE.equals(getParamListAction())) {
            String filepath = "";
            Layout layout = null;
            // if the button is activated check the value of the button
            // the button was active
            if (isloggingactivated(logchannel)) {
                // remove the private Appender from logger
                logchannel.getAppenders().clear();
                // activate the heredity so the logger get the appender from parent logger
                logchannel.setAdditive(true);

            }
            // the button was inactive
            else {
                Collection<Appender> rootAppenders = ((Logger)LogManager.getRootLogger()).getAppenders().values();
                // get the layout and file path from root logger
                for (Appender appender : rootAppenders) {
                    if (appender instanceof FileAppender) {
                        FileAppender fapp = (FileAppender)appender;
                        filepath = fapp.getFileName().substring(0, fapp.getFileName().lastIndexOf(File.separatorChar));
                        layout = fapp.getLayout();
                        break;
                    }
                }

                Collection<Appender> appenders = logchannel.getAppenders().values();
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
                        logfilename = filepath
                            + File.separator
                            + "opencms-"
                            + temp.substring(1).replace(".", "-")
                            + ".log";
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
                    fapp = ((FileAppender.Builder)FileAppender.newBuilder().withLayout(layout).withFileName(
                        logfilename).withAppend(true).withName(logchannnelName)).build();

                } catch (Exception e) {
                    LOG.error(Messages.get().container(Messages.LOG_CREATING_APPENDER_0), e);
                }
                // deactivate the heredity so the logger get no longer the appender from parent logger
                logchannel.setAdditive(false);
                // remove all active Appenders from logger
                logchannel.getAppenders().clear();
                // add the new created Appender to the logger
                logchannel.addAppender(fapp);
            }
        }
        refreshList();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() {

        // collect all the values for "Log-channels", "Log-channels-parents" and "Log-channels-level"
        List<CmsListItem> items = new LinkedList<CmsListItem>();
        List<Logger> loggers = getLoggers();
        Iterator<Logger> iterator = loggers.iterator();
        while (iterator.hasNext()) {
            Logger logger = iterator.next();
            CmsListItem item = getList().newItem(logger.getName());
            item.set(COLUMN_CHANNELS, logger.getName());
            Logger parentLogger = logger.getParent();
            if (parentLogger == null) {
                item.set(COLUMN_PARENT_CHANNELS, "");
            } else {
                item.set(COLUMN_PARENT_CHANNELS, logger.getParent().getName());
            }
            item.set(COLUMN_LOG_LEVEL, String.valueOf(logger.getLevel()));

            String test = "";
            Collection<Appender> appenders = logger.getAppenders().values();
            int count = 0;
            // select the Appender from logger
            for (Appender appender : appenders) {
                // only use file appenders
                if (appender instanceof FileAppender) {
                    FileAppender fapp = (FileAppender)appender;
                    String temp = "";
                    temp = fapp.getFileName().substring(fapp.getFileName().lastIndexOf(File.separatorChar) + 1);
                    test = test + temp;
                    count++;
                    break;
                }
            }
            Collection<Appender> parentAppenders = logger.getParent().getAppenders().values();
            // if no Appender found from logger, select the Appender from parent logger
            if (count == 0) {
                for (Appender appender : parentAppenders) {
                    // only use file appenders
                    if (appender instanceof FileAppender) {
                        FileAppender fapp = (FileAppender)appender;
                        String temp = "";
                        temp = fapp.getFileName().substring(fapp.getFileName().lastIndexOf(File.separatorChar) + 1);
                        test = test + temp;
                        count++;
                        break;
                    }
                }
            }

            if (count == 0) {
                Collection<Appender> rootAppenders = ((Logger)LogManager.getRootLogger()).getAppenders().values();
                // if no Appender found from parent logger, select the Appender from root logger
                for (Appender appender : rootAppenders) {
                    // only use file appenders
                    if (appender instanceof FileAppender) {
                        FileAppender fapp = (FileAppender)appender;
                        String temp = "";
                        temp = fapp.getFileName().substring(fapp.getFileName().lastIndexOf(File.separatorChar) + 1);
                        test = test + temp;
                        break;
                    }
                }

            }
            item.set(COLUMN_LOG_FILE, test);
            items.add(item);
        }
        return items;
    }

    /**
     * Simple check if the logger has the global log file <p> or a single one.
     *
     * @param logchannel the channel that has do be checked
     * @return true if the the log channel has a single log file
     * */
    protected boolean isloggingactivated(Logger logchannel) {

        return logchannel.getAppenders().containsKey(logchannel.getName());
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        //////////////////
        // FATAL COLUMN //
        //////////////////
        CmsListColumnDefinition fatalColumn = new CmsListColumnDefinition(COLUMN_FATAL);
        fatalColumn.setName(Messages.get().container(Messages.GUI_LOG_LEVEL_FATAL_0));
        fatalColumn.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        fatalColumn.setWidth("50");
        fatalColumn.setListItemComparator(new CmsLogLevelListItemComparator());
        fatalColumn.setPrintable(false);
        fatalColumn.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        fatalColumn.setListItemComparator(new CmsLogLevelListItemComparator());
        CmsListDirectAction fatalActAction = new CmsChangeLogLevelAction(ACTION_LOGGING_LEVEL_FATAL, Level.FATAL) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                Level actuallevel = Level.toLevel((String)getItem().get(COLUMN_LOG_LEVEL));
                if (actuallevel != null) {

                    if (actuallevel.intLevel() < Level.FATAL.intLevel()) {
                        setIconPath(PATH_FATAL_ACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_LEVEL_FATAL_HELP_DEACTIVATE_0));
                    } else if (actuallevel.intLevel() == Level.FATAL.intLevel()) {
                        setIconPath(PATH_FATAL_ACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_LEVEL_ACTIVE_0));
                    } else {
                        setIconPath(PATH_FATAL_INACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_LEVEL_FATAL_HELP_ACTIVATE_0));
                    }
                }
                return super.isVisible();
            }
        };
        fatalActAction.setHelpText(Messages.get().container(Messages.GUI_LOG_LEVEL_FATAL_HELP_0));
        fatalColumn.addDirectAction(fatalActAction);
        metadata.addColumn(fatalColumn);

        //////////////////
        // ERROR COLUMN //
        //////////////////
        CmsListColumnDefinition errorColumn = new CmsListColumnDefinition(COLUMN_ERROR);
        errorColumn.setName(Messages.get().container(Messages.GUI_LOG_LEVEL_ERROR_0));
        errorColumn.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        errorColumn.setWidth("50");
        errorColumn.setPrintable(false);
        errorColumn.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        errorColumn.setListItemComparator(new CmsLogLevelListItemComparator());
        CmsListDirectAction errorActAction = new CmsChangeLogLevelAction(ACTION_LOGGING_LEVEL_ERROR, Level.ERROR) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                Level actuallevel = Level.toLevel((String)getItem().get(COLUMN_LOG_LEVEL));
                if (actuallevel != null) {
                    if ((actuallevel.intLevel() < Level.ERROR.intLevel())) {
                        setIconPath(PATH_ERROR_ACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_LEVEL_ERROR_HELP_DEACTIVATE_0));
                    } else if (actuallevel.intLevel() == Level.ERROR.intLevel()) {
                        setIconPath(PATH_ERROR_ACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_LEVEL_ACTIVE_0));
                    } else {
                        setIconPath(PATH_ERROR_INACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_LEVEL_ERROR_HELP_ACTIVATE_0));
                    }
                }
                return super.isVisible();
            }
        };
        errorActAction.setHelpText(Messages.get().container(Messages.GUI_LOG_LEVEL_ERROR_HELP_0));
        errorColumn.addDirectAction(errorActAction);
        metadata.addColumn(errorColumn);

        /////////////////
        // WARN COLUMN //
        /////////////////
        CmsListColumnDefinition warnColumn = new CmsListColumnDefinition(COLUMN_WARN);
        warnColumn.setName(Messages.get().container(Messages.GUI_LOG_LEVEL_WARN_0));
        warnColumn.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        warnColumn.setWidth("50");
        warnColumn.setPrintable(false);
        warnColumn.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        warnColumn.setListItemComparator(new CmsLogLevelListItemComparator());
        CmsListDirectAction warnAction = new CmsChangeLogLevelAction(ACTION_LOGGING_LEVEL_WARN, Level.WARN) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                Level actuallevel = Level.toLevel((String)getItem().get(COLUMN_LOG_LEVEL));
                if (actuallevel != null) {
                    if ((actuallevel.intLevel() < Level.WARN.intLevel())) {
                        setIconPath(PATH_WARN_ACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_LEVEL_WARN_HELP_DEACTIVATE_0));
                    } else if (actuallevel.intLevel() == Level.WARN.intLevel()) {
                        setIconPath(PATH_WARN_ACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_LEVEL_ACTIVE_0));
                    } else {
                        setIconPath(PATH_WARN_INACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_LEVEL_WARN_HELP_ACTIVATE_0));
                    }
                }
                return super.isVisible();
            }
        };
        warnAction.setHelpText(Messages.get().container(Messages.GUI_LOG_LEVEL_WARN_HELP_0));
        warnColumn.addDirectAction(warnAction);
        metadata.addColumn(warnColumn);

        /////////////////
        // INFO COLUMN //
        /////////////////
        CmsListColumnDefinition infoColumn = new CmsListColumnDefinition(COLUMN_INFO);
        infoColumn.setName(Messages.get().container(Messages.GUI_LOG_LEVEL_INFO_0));
        infoColumn.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        infoColumn.setWidth("50");
        infoColumn.setPrintable(false);
        infoColumn.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        infoColumn.setListItemComparator(new CmsLogLevelListItemComparator());
        CmsListDirectAction infoAction = new CmsChangeLogLevelAction(ACTION_LOGGING_LEVEL_INFO, Level.INFO) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                // check the Loglevel from hidden column and set the right icon
                Level actuallevel = Level.toLevel((String)getItem().get(COLUMN_LOG_LEVEL));
                if (actuallevel != null) {
                    if ((actuallevel.intLevel() < Level.INFO.intLevel())) {
                        setIconPath(PATH_INFO_ACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_LEVEL_INFO_HELP_DEACTIVATE_0));
                    } else if (actuallevel.intLevel() == Level.INFO.intLevel()) {
                        setIconPath(PATH_INFO_ACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_LEVEL_ACTIVE_0));
                    } else {
                        setIconPath(PATH_INFO_INACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_LEVEL_INFO_HELP_ACTIVATE_0));
                    }
                }
                return super.isVisible();
            }
        };
        infoAction.setHelpText(Messages.get().container(Messages.GUI_LOG_LEVEL_INFO_HELP_0));
        infoColumn.addDirectAction(infoAction);
        metadata.addColumn(infoColumn);

        //////////////////
        // DEBUG COLUMN //
        //////////////////
        CmsListColumnDefinition debugColumn = new CmsListColumnDefinition(COLUMN_DEBUG);
        debugColumn.setName(Messages.get().container(Messages.GUI_LOG_LEVEL_DEBUG_0));
        debugColumn.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        debugColumn.setWidth("50");
        debugColumn.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        debugColumn.setPrintable(false);
        debugColumn.setListItemComparator(new CmsLogLevelListItemComparator());
        // generate an Action to activate the change of a log-channel
        CmsListDirectAction debugAction = new CmsChangeLogLevelAction(ACTION_LOGGING_LEVEL_DEBUG, Level.DEBUG) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                Level actuallevel = Level.toLevel((String)getItem().get(COLUMN_LOG_LEVEL));
                if (actuallevel != null) {
                    if (actuallevel.intLevel() < Level.DEBUG.intLevel()) {
                        setIconPath(PATH_DEBUG_ACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_LEVEL_DEBUG_HELP_DEACTIVATE_0));
                    } else if (actuallevel.intLevel() == Level.DEBUG.intLevel()) {
                        setIconPath(PATH_DEBUG_ACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_LEVEL_ACTIVE_0));
                    } else {
                        setIconPath(PATH_DEBUG_INACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_LEVEL_DEBUG_HELP_ACTIVATE_0));
                    }
                }
                return super.isVisible();
            }
        };
        debugAction.setHelpText(Messages.get().container(Messages.GUI_LOG_LEVEL_DEBUG_HELP_0));
        debugColumn.addDirectAction(debugAction);
        metadata.addColumn(debugColumn);

        /////////////////////
        // TRUN OFF COLUMN //
        /////////////////////
        CmsListColumnDefinition offColumn = new CmsListColumnDefinition(COLUMN_OFF);
        offColumn.setName(Messages.get().container(Messages.GUI_LOG_LEVEL_OFF_0));
        offColumn.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        offColumn.setWidth("50");
        offColumn.setSorteable(true);
        offColumn.setPrintable(false);
        offColumn.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        offColumn.setListItemComparator(new CmsLogLevelListItemComparator());
        CmsListDirectAction offAction = new CmsChangeLogLevelAction(ACTION_LOGGING_LEVEL_OFF, Level.OFF) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                Level actuallevel = Level.toLevel((String)getItem().get(COLUMN_LOG_LEVEL));
                if (actuallevel != null) {
                    if (actuallevel.intLevel() == Level.OFF.intLevel()) {
                        setIconPath(PATH_OFF_ACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_LEVEL_OFF_HELP_ACTIVATE_0));
                    } else {
                        setIconPath(PATH_OFF_INACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_LEVEL_OFF_HELP_DEACTIVATE_0));
                    }
                }
                return super.isVisible();
            }
        };
        offAction.setHelpText(Messages.get().container(Messages.GUI_LOG_LEVEL_OFF_HELP_0));
        offColumn.addDirectAction(offAction);
        metadata.addColumn(offColumn);

        //////////////////////////////
        // CURRENT LOG LEVEL COLUMN //
        //////////////////////////////
        CmsListColumnDefinition logLevelHidden = new CmsListColumnDefinition(COLUMN_LOG_LEVEL);
        logLevelHidden.setName(Messages.get().container(Messages.GUI_LOG_LEVEL_CURRENT_0));
        logLevelHidden.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        logLevelHidden.setWidth("10%");
        logLevelHidden.setSorteable(true);
        logLevelHidden.setPrintable(true);
        logLevelHidden.setVisible(false);
        metadata.addColumn(logLevelHidden);

        //////////////////
        // LOG CHANNELS //
        //////////////////
        CmsListColumnDefinition channels = new CmsListColumnDefinition(COLUMN_CHANNELS);
        channels.setName(Messages.get().container(Messages.GUI_LOG_CHANNELS_0));
        channels.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        channels.setSorteable(true);
        channels.setWidth("50%");
        channels.setPrintable(true);
        metadata.addColumn(channels);

        ///////////////////////////////
        // PARENT LOG CHANNEL COLUMN //
        ///////////////////////////////
        CmsListColumnDefinition parentChannel = new CmsListColumnDefinition(COLUMN_PARENT_CHANNELS);
        parentChannel.setName(Messages.get().container(Messages.GUI_LOG_PARENT_CHANNEL_0));
        parentChannel.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        parentChannel.setSorteable(true);
        parentChannel.setWidth("20%");
        parentChannel.setPrintable(false);
        metadata.addColumn(parentChannel);

        /////////////////////
        // LOG FILE COLUMN //
        /////////////////////
        CmsListColumnDefinition selectedFile = new CmsListColumnDefinition(COLUMN_LOG_FILE);
        selectedFile.setName(Messages.get().container(Messages.GUI_LOG_FILE_SELECTED_0));
        selectedFile.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        selectedFile.setSorteable(true);
        selectedFile.setWidth("20%");
        selectedFile.setPrintable(false);
        metadata.addColumn(selectedFile);

        ////////////////////////
        // CHANGE FILE COLUMN //
        ////////////////////////
        CmsListColumnDefinition changeFile = new CmsListColumnDefinition(COLUMN_CHANGE_LOGFILE);
        changeFile.setName(Messages.get().container(Messages.GUI_LOG_FILE_CHANGE_FILE_0));
        changeFile.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        changeFile.setSorteable(false);
        changeFile.setWidth("25");
        changeFile.setPrintable(false);
        CmsListDirectAction changeFileAction = new CmsListDirectAction(ACTION_ACTIVATE_LOGFILE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                Logger logger = getLoggerImpl((String)getItem().get(COLUMN_CHANNELS));
                if (logger != null) {
                    if (isloggingactivated(logger)) {
                        setIconPath(PATH_FILE_ACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_FILE_CHANGE_FILE_HELP_DEACTIVATE_0));
                    } else {
                        setIconPath(PATH_FILE_INACTIVE);
                        setName(Messages.get().container(Messages.GUI_LOG_FILE_CHANGE_FILE_HELP_ACTIVATE_0));
                    }
                }
                return super.isVisible();
            }
        };
        changeFileAction.setHelpText(Messages.get().container(Messages.GUI_LOG_FILE_CHANGE_FILE_HELP_0));
        changeFileAction.setIconPath(PATH_OFF_ACTIVE);
        changeFile.addDirectAction(changeFileAction);
        metadata.addColumn(changeFile);

        ///////////////////
        // SEARCH ACTION //
        ///////////////////
        CmsListSearchAction searchAction = new CmsListSearchAction(metadata.getColumnDefinition(COLUMN_CHANNELS));
        metadata.setSearchAction(searchAction);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // noop
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add the Fatal multi action
        CmsListMultiAction settoFatal = new CmsChangeLogLevelMultiAction(SET_TO_FATAL);
        settoFatal.setName(Messages.get().container(Messages.GUI_LOG_LEVEL_FATAL_MULTI_HELP_0));
        settoFatal.setConfirmationMessage(Messages.get().container(Messages.GUI_LOG_LEVEL_FATAL_MULTI_CONFIRMATION_0));
        settoFatal.setIconPath(PATH_FATAL_ACTIVE);
        settoFatal.setHelpText(Messages.get().container(Messages.GUI_LOG_LEVEL_FATAL_MULTI_HELP_0));
        metadata.addMultiAction(settoFatal);

        // add the Error multi action
        CmsListMultiAction settoError = new CmsChangeLogLevelMultiAction(SET_TO_ERROR);
        settoError.setName(Messages.get().container(Messages.GUI_LOG_LEVEL_ERROR_MULTI_HELP_0));
        settoError.setConfirmationMessage(Messages.get().container(Messages.GUI_LOG_LEVEL_ERROR_MULTI_CONFIRMATION_0));
        settoError.setIconPath(PATH_ERROR_ACTIVE);
        settoError.setHelpText(Messages.get().container(Messages.GUI_LOG_LEVEL_ERROR_MULTI_HELP_0));
        metadata.addMultiAction(settoError);

        // add the Warn multi action
        CmsListMultiAction settoWarn = new CmsChangeLogLevelMultiAction(SET_TO_WARN);
        settoWarn.setName(Messages.get().container(Messages.GUI_LOG_LEVEL_WARN_MULTI_HELP_0));
        settoWarn.setConfirmationMessage(Messages.get().container(Messages.GUI_LOG_LEVEL_WARN_MULTI_CONFIRMATION_0));
        settoWarn.setIconPath(PATH_WARN_ACTIVE);
        settoWarn.setHelpText(Messages.get().container(Messages.GUI_LOG_LEVEL_WARN_MULTI_HELP_0));
        metadata.addMultiAction(settoWarn);

        // add the Info multi action
        CmsListMultiAction settoInfo = new CmsChangeLogLevelMultiAction(SET_TO_INFO);
        settoInfo.setName(Messages.get().container(Messages.GUI_LOG_LEVEL_INFO_MULTI_HELP_0));
        settoInfo.setConfirmationMessage(Messages.get().container(Messages.GUI_LOG_LEVEL_INFO_MULTI_CONFIRMATION_0));
        settoInfo.setIconPath(PATH_INFO_ACTIVE);
        settoInfo.setHelpText(Messages.get().container(Messages.GUI_LOG_LEVEL_INFO_MULTI_HELP_0));
        metadata.addMultiAction(settoInfo);

        // add the Debug multi action
        CmsListMultiAction settoDebug = new CmsChangeLogLevelMultiAction(SET_TO_DEBUG);
        settoDebug.setName(Messages.get().container(Messages.GUI_LOG_LEVEL_DEBUG_MULTI_HELP_0));
        settoDebug.setConfirmationMessage(Messages.get().container(Messages.GUI_LOG_LEVEL_DEBUG_MULTI_CONFIRMATION_0));
        settoDebug.setIconPath(PATH_DEBUG_ACTIVE);
        settoDebug.setHelpText(Messages.get().container(Messages.GUI_LOG_LEVEL_DEBUG_MULTI_HELP_0));
        metadata.addMultiAction(settoDebug);

        // add the Off multi action
        CmsListMultiAction settoOff = new CmsChangeLogLevelMultiAction(SET_TO_OFF);
        settoOff.setName(Messages.get().container(Messages.GUI_LOG_LEVEL_OFF_MULTI_HELP_0));
        settoOff.setConfirmationMessage(Messages.get().container(Messages.GUI_LOG_LEVEL_OFF_MULTI_CONFIRMATION_0));
        settoOff.setIconPath(PATH_OFF_ACTIVE);
        settoOff.setHelpText(Messages.get().container(Messages.GUI_LOG_LEVEL_OFF_MULTI_HELP_0));
        metadata.addMultiAction(settoOff);
    }

    /**
     * Simple function to get the prefix of an logchannel name.<p>
     *
     * @param logname the full name of the logging channel
     *
     * @return a string array with different package prefixes
     */
    private String[] buildsufix(String logname) {

        // help String array to store all combination
        String[] prefix_temp = new String[logname.length()];
        int count = 0;
        while (logname.indexOf(".") > 1) {
            // separate the name of the logger into pieces of name and separator e.g.: "org."
            String subprefix = logname.substring(0, logname.indexOf(".") + 1);
            logname = logname.replace(subprefix, "");
            if (logname.indexOf(".") > 1) {
                if (count > 0) {
                    // build different suffixes based on the pieces separated above
                    prefix_temp[count] = prefix_temp[count - 1] + subprefix;
                } else {
                    // if it's the first piece of the name only it will be set
                    prefix_temp[count] = subprefix;

                }
            }
            count++;
        }
        // if the logger name has more then one piece
        if (count >= 1) {
            // create result string array
            String[] prefix = new String[count - 1];
            // copy all different prefixes to one array with right size
            for (int i = 0; i < (count - 1); i++) {
                prefix[i] = prefix_temp[i].substring(0, prefix_temp[i].length() - 1);
            }
            // return all different prefixes
            return prefix;
        }
        // if the logger name has only one or less piece
        else {
            // return the full logger name
            String[] nullreturn = new String[1];
            nullreturn[0] = logname;
            return nullreturn;
        }
    }

    /**
     * @param logchannnelName
     * @return
     */
    private Logger getLoggerImpl(String logchannnelName) {

        return (Logger)LogManager.getLogger(logchannnelName);
    }

    /**
     * Help function to get all loggers from LogManager.<p>
     *
     * @return List of Logger
     */
    private List<Logger> getLoggers() {

        // list of all loggers
        List<Logger> definedLoggers = new ArrayList<Logger>();
        // list of all parent loggers
        List<Logger> packageLoggers = new ArrayList<Logger>();
        LoggerContext logContext = (LoggerContext)LogManager.getContext(false);
        Iterator<Logger> it_curentlogger = logContext.getLoggers().iterator();
        // get all current loggers
        while (it_curentlogger.hasNext()) {
            // get the logger
            Logger log = it_curentlogger.next();
            String logname = log.getName();
            String[] prefix = buildsufix(logname);
            // create all possible package logger from given logger name
            for (int i = 0; i < prefix.length; i++) {
                // get the name of the logger without the prefix
                String temp = log.getName().replace(prefix[i], "");
                // if the name has suffix
                if (temp.length() > 1) {
                    temp = temp.substring(1);
                }
                if (temp.lastIndexOf(".") > 1) {
                    // generate new logger with "org.opencms" prefix and the next element
                    // between the points e.g.: "org.opencms.search"
                    Logger temp_logger = getLoggerImpl(prefix[i] + "." + temp.substring(0, temp.indexOf(".")));
                    // activate the heredity so the logger get the appender from parent logger
                    temp_logger.setAdditive(true);
                    // add the logger to the packageLoggers list if it is not part of it
                    if (!packageLoggers.contains(temp_logger)) {
                        packageLoggers.add(temp_logger);
                    }
                }
            }
            definedLoggers.add(log);

        }

        Iterator<Logger> it_logger = packageLoggers.iterator();
        // iterate about all packageLoggers
        while (it_logger.hasNext()) {
            Logger temp = it_logger.next();
            // check if the logger is part of the logger list
            if (!definedLoggers.contains(temp)) {
                // add the logger to the logger list
                definedLoggers.add(temp);
            }
        }

        // sort all loggers by name
        Collections.sort(definedLoggers, new Comparator<Object>() {

            public int compare(Logger o1, Logger o2) {

                return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
            }

            public int compare(Object obj, Object obj1) {

                return compare((Logger)obj, (Logger)obj1);
            }

        });
        // return all loggers
        return definedLoggers;
    }

    /**
     * Simple function to set all child loggers to the same value of parent
     * logger if the parent logger leves is changed.<p>
     *
     * @param logchannel the channel that might be the parent logger
     */
    private void isparentlogger(Logger logchannel) {

        // get all log channels
        List<Logger> referenz = getLoggers();
        Iterator<Logger> it_logger = referenz.iterator();
        while (it_logger.hasNext()) {
            Logger child_test = it_logger.next();
            // if the logchannel has the given logchannel as parent his loglevel is set to the parent one.
            if (logchannel.getName().equals(child_test.getParent().getName())) {
                isparentlogger(child_test);
                child_test.setLevel(null);
            }
        }
    }
}
