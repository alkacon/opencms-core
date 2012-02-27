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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * Main logging management list view.<p>
 * 
 * Defines the list columns and possible actions for logging.<p>
 * 
 */
public class CmsLog4JAdminDialog extends A_CmsListDialog {

    /** Name of the Debug-level Action. */
    public static final String ACTION_LOGGING_LEVEL_DEBUG = "debugchannel";

    /** Name of the Error-level Action. */
    public static final String ACTION_LOGGING_LEVEL_ERROR = "errorlevel";

    /** Name of the Fatal-level Action. */
    public static final String ACTION_LOGGING_LEVEL_FATAL = "fatallevel";

    /** Name of the Info-level Action. */
    public static final String ACTION_LOGGING_LEVEL_INFO = "infochannel";

    /** Name of the Off-level Action. */
    public static final String ACTION_LOGGING_LEVEL_OFF = "offlevel";

    /** Name of the Warn-level Action. */
    public static final String ACTION_LOGGING_LEVEL_WARN = "warnchannel";

    /** Name of the logchannel Action. */
    public static final String ACTION_ACTIVATE_LOGFILE = "activlogchannel";

    /** List column class. */
    public static final String LIST_COLUMN_ACTIVE = "cac";

    /** Name of the logger-List. */
    public static final String LIST_COLUMN_LOGGERS = "logger";

    /** Name of the parent-list. */
    public static final String LIST_COLUMN_PARENT = "parent";

    /** Name of the parent-list. */
    public static final String LIST_APPEND_LOGGER = "Log-File";

    /** The prefix of opencms classes. */
    public static final String OPENCMS_CLASS_PREFIX = "org.opencms";

    /** Name of the list for separate log files. */
    public static final String LIST_CHANGE_LOGFILE = "Seperate Log file";

    /** Actual level of logging. */
    public static final String LIST_COLUMN_PARENT_LOGGER_LEVEL = "loggerlevel";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "buttons/";

    /** Path to the Debug-Icon(active). */
    public static final String PATH_DEBUG_ACTIVE = PATH_BUTTONS + "log_button_debug_a_2.png";

    /** Path to the Debug-Icon(inactive). */
    public static final String PATH_DEBUG_INACTIVE = PATH_BUTTONS + "log_button_debug_i_2.png";

    /** Path to the Error-Icon(active). */
    public static final String PATH_ERROR_ACTIVE = PATH_BUTTONS + "log_button_error_a_2.png";

    /** Path to the Error-Icon(inactive). */
    public static final String PATH_ERROR_INACTIVE = PATH_BUTTONS + "log_button_error_i_2.png";

    /** Path to the Fatal-Icon(active). */
    public static final String PATH_FATAL_ACTIVE = PATH_BUTTONS + "log_button_fatal_a_2.png";

    /** Path to the Fatal-Icon(inactive). */
    public static final String PATH_FATAL_INACTIVE = PATH_BUTTONS + "log_button_fatal_i_2.png";

    /** Path to the Info-Icon(active). */
    public static final String PATH_INFO_ACTIVE = PATH_BUTTONS + "log_button_info_a_2.png";

    /** Path to the Info-Icon(inactive). */
    public static final String PATH_INFO_INACTIVE = PATH_BUTTONS + "log_button_info_i_2.png";

    /** Path to the Off-Icon(active). */
    public static final String PATH_OFF_ACTIVE = PATH_BUTTONS + "log_button_off_a_2.png";

    /** Path to the Off-Icon(inactive). */
    public static final String PATH_OFF_INACTIVE = PATH_BUTTONS + "log_button_off_i_2.png";

    /** Path to the Warning-Icon(active). */
    public static final String PATH_WARN_ACTIVE = PATH_BUTTONS + "log_button_warn_a_2.png";

    /** Path to the Warning-Icon(inactive). */
    public static final String PATH_WARN_INACTIVE = PATH_BUTTONS + "log_button_warn_i_2.png";

    /** Path to the File-Icon(active). */
    public static final String PATH_FILE_ACTIVE = PATH_BUTTONS + "log_button_file_on_2.png";

    /** Path to the File-Icon(inactive). */
    public static final String PATH_FILE_INACTIVE = PATH_BUTTONS + "log_button_file_off_2.png";

    /** Shortcut for the DEBUG column. */
    public static final String COLUMN_DEBUG = "chd";

    /** Shortcut for the INFO column. */
    public static final String COLUMN_INFO = "chi";

    /** Shortcut for the WARN column. */
    public static final String COLUMN_WARN = "chw";

    /** Shortcut for the ERROR column. */
    public static final String COLUMN_ERROR = "che";

    /** Shortcut for the FATAL column. */
    public static final String COLUMN_FATAL = "chf";

    /** Shortcut for the OFF column. */
    public static final String COLUMN_OFF = "cho";

    /** Shortcut for the MultiAction (FATAL). */
    public static final String SET_TO_FATAL = "sf";

    /** Shortcut for the MultiAction (ERROR). */
    public static final String SET_TO_ERROR = "sr";

    /** Shortcut for the MultiAction (WARN). */
    public static final String SET_TO_WARN = "sw";

    /** Shortcut for the MultiAction (INFO). */
    public static final String SET_TO_INFO = "si";

    /** Shortcut for the MultiAction (DEBUG). */
    public static final String SET_TO_DEBUG = "sd";

    /** Shortcut for the MultiAction (OFF). */
    public static final String SET_TO_OFF = "so";

    /**
     * @param jsp
     */
    public CmsLog4JAdminDialog(CmsJspActionElement jsp) {

        super(
            jsp,
            "Log Settings",
            Messages.get().container("GUI_LOGADMIN_LIST_NAME"),
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
            Logger logchannel = LogManager.getLogger(logchannnelName);
            logchannel.setLevel(newLogchannelLevel);
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
        Logger logchannel = LogManager.getLogger(logchannnelName);
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
                logchannel.setLevel(null);
            } else {
                logchannel.setLevel(newLogchannelLevel);
            }
        }
        if (ACTION_ACTIVATE_LOGFILE.equals(getParamListAction())) {
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
                List<Appender> appenders_root = Collections.list(Logger.getRootLogger().getAllAppenders());
                Iterator<Appender> app_root = appenders_root.iterator();
                // get the layout and file path from root logger
                while (app_root.hasNext()) {
                    FileAppender fapp = (FileAppender)app_root.next();
                    filepath = fapp.getFile().substring(0, fapp.getFile().lastIndexOf(File.separatorChar));
                    layout = fapp.getLayout();
                }

                @SuppressWarnings("unchecked")
                List<Appender> appenders = Collections.list(logchannel.getAllAppenders());
                Iterator<Appender> app = appenders.iterator();
                // check if the logger has an Appender get his layout
                while (app.hasNext()) {
                    FileAppender fapp = (FileAppender)app.next();
                    layout = fapp.getLayout();
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
                    fapp = new FileAppender(layout, logfilename, true);
                    // set the log file e.g.: "C:\tomcat6\webapps\opencms\WEB-INF\logs"
                    fapp.setName(logchannnelName);

                } catch (IOException e) {
                    // TODO: Auto-generated catch block
                    e.printStackTrace();
                }
                // deactivate the heredity so the logger get no longer the appender from parent logger
                logchannel.setAdditivity(false);
                // remove all active Appenders from logger
                logchannel.removeAllAppenders();
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

        // TODO: Auto-generated method stub

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
            item.set(LIST_COLUMN_LOGGERS, logger.getName());
            Category parentLogger = logger.getParent();
            if (parentLogger == null) {
                item.set(LIST_COLUMN_PARENT, "");
            } else {
                item.set(LIST_COLUMN_PARENT, logger.getParent().getName());
            }
            item.set(LIST_COLUMN_PARENT_LOGGER_LEVEL, String.valueOf(logger.getEffectiveLevel()));

            String test = "";
            @SuppressWarnings("unchecked")
            List<Appender> appenders = Collections.list(logger.getAllAppenders());
            Iterator<Appender> app = appenders.iterator();
            int count = 0;
            // select the Appender from logger 
            while (app.hasNext()) {
                FileAppender fapp = (FileAppender)app.next();
                String temp = "";
                temp = fapp.getFile().substring(fapp.getFile().lastIndexOf(File.separatorChar) + 1);
                test = test + temp;
                count++;

            }
            @SuppressWarnings("unchecked")
            List<Appender> appenders_parent = Collections.list(logger.getParent().getAllAppenders());
            Iterator<Appender> app_parent = appenders_parent.iterator();
            // if no Appender found from logger, select the Appender from parent logger
            if (!app.hasNext() && (count == 0)) {
                while (app_parent.hasNext()) {
                    FileAppender fapp = (FileAppender)app_parent.next();
                    String temp = "";
                    temp = fapp.getFile().substring(fapp.getFile().lastIndexOf(File.separatorChar) + 1);
                    test = test + temp;
                    count++;
                }
            }

            if (!app_parent.hasNext() && (count == 0)) {
                @SuppressWarnings("unchecked")
                List<Appender> appenders_root = Collections.list(Logger.getRootLogger().getAllAppenders());
                Iterator<Appender> app_root = appenders_root.iterator();
                // if no Appender found from parent logger, select the Appender from root logger
                while (app_root.hasNext()) {
                    FileAppender fapp = (FileAppender)app_root.next();
                    String temp = "";
                    temp = fapp.getFile().substring(fapp.getFile().lastIndexOf(File.separatorChar) + 1);
                    test = test + temp;
                }

            }
            item.set(LIST_APPEND_LOGGER, test);
            items.add(item);
        }
        return items;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // add column for FATAL level [Icon has a fix width 50px]
        CmsListColumnDefinition colChangeFatalLevel = new CmsListColumnDefinition(COLUMN_FATAL);
        colChangeFatalLevel.setName(Messages.get().container("GUI_LOGADMIN_LIST_COLS_CH_LEVEL_FATAL"));
        colChangeFatalLevel.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        colChangeFatalLevel.setWidth("50");
        colChangeFatalLevel.setListItemComparator(new CmsLogLevelListItemComparator());
        colChangeFatalLevel.setPrintable(false);
        colChangeFatalLevel.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        colChangeFatalLevel.setListItemComparator(new CmsLogLevelListItemComparator());
        // generate an Action to activate the change of a log-channel
        CmsListDirectAction FatalActAction = new CmsChangeLogLevelAction(ACTION_LOGGING_LEVEL_FATAL, Level.FATAL) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                // check the Loglevel from hidden column and set the right icon
                Level actuallevel = Level.toLevel((String)getItem().get(LIST_COLUMN_PARENT_LOGGER_LEVEL));
                if (actuallevel != null) {

                    if (actuallevel.toInt() <= Priority.FATAL_INT) {
                        // set the active icon if the loglevelvalue lower than the FATALlevelvalue
                        setIconPath(PATH_FATAL_ACTIVE);
                        setHelpText(Messages.get().container("LOGADMIN_LEVEL_FATAL_HELP_ACTIV"));
                    } else {
                        // set the inactive icon if the loglevelvalue is higher than the FATALlevelvalue
                        setIconPath(PATH_FATAL_INACTIVE);
                        setHelpText(Messages.get().container("LOGADMIN_LEVEL_FATAL_HELP"));
                    }
                }
                return super.isVisible();
            }
        };
        FatalActAction.setName(Messages.get().container("GUI_JOBS_LIST_ACTION_MFATAL_NAME_0"));
        FatalActAction.setHelpText(Messages.get().container("LOGADMIN_LEVEL_FATAL_HELP"));
        colChangeFatalLevel.addDirectAction(FatalActAction);
        metadata.addColumn(colChangeFatalLevel);

        // add column for ERROR level [Icon has a fix width 50px]
        CmsListColumnDefinition colChangeErrorLevel = new CmsListColumnDefinition(COLUMN_ERROR);
        colChangeErrorLevel.setName(Messages.get().container("GUI_LOGADMIN_LIST_COLS_CH_LEVEL_ERROR"));
        colChangeErrorLevel.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        colChangeErrorLevel.setWidth("50");
        colChangeErrorLevel.setPrintable(false);
        colChangeErrorLevel.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        colChangeErrorLevel.setListItemComparator(new CmsLogLevelListItemComparator());
        // generate an Action to activate the change of a log-channel
        CmsListDirectAction ErrorActAction = new CmsChangeLogLevelAction(ACTION_LOGGING_LEVEL_ERROR, Level.ERROR) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                // check the Loglevel from hidden column and set the right icon
                Level actuallevel = Level.toLevel((String)getItem().get(LIST_COLUMN_PARENT_LOGGER_LEVEL));
                if (actuallevel != null) {
                    if ((actuallevel.toInt() <= Priority.ERROR_INT)) {
                        // set the active icon if the loglevelvalue lower than the ERRORlevelvalue
                        setIconPath(PATH_ERROR_ACTIVE);
                        setHelpText(Messages.get().container("LOGADMIN_LEVEL_ERROR_HELP_ACTIV"));
                    } else {
                        // set the inactive icon if the loglevelvalue is higher than the ERRORlevelvalue
                        setIconPath(PATH_ERROR_INACTIVE);
                        setHelpText(Messages.get().container("LOGADMIN_LEVEL_ERROR_HELP"));
                    }
                }
                return super.isVisible();
            }
        };
        ErrorActAction.setName(Messages.get().container("GUI_JOBS_LIST_ACTION_MERROR_NAME_0"));
        ErrorActAction.setHelpText(Messages.get().container("LOGADMIN_LEVEL_ERROR_HELP"));
        colChangeErrorLevel.addDirectAction(ErrorActAction);
        metadata.addColumn(colChangeErrorLevel);

        // add column for WARN level [Icon has a fix width 50px]
        CmsListColumnDefinition colChangeWarnLevel = new CmsListColumnDefinition(COLUMN_WARN);
        colChangeWarnLevel.setName(Messages.get().container("GUI_LOGADMIN_LIST_COLS_CH_LEVEL_WARN"));
        colChangeWarnLevel.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        colChangeWarnLevel.setWidth("50");
        colChangeWarnLevel.setPrintable(false);
        colChangeWarnLevel.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        colChangeWarnLevel.setListItemComparator(new CmsLogLevelListItemComparator());
        // generate an Action to activate the change of a log-channel
        CmsListDirectAction WarnActAction = new CmsChangeLogLevelAction(ACTION_LOGGING_LEVEL_WARN, Level.WARN) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                // check the Loglevel from hidden column and set the right icon
                Level actuallevel = Level.toLevel((String)getItem().get(LIST_COLUMN_PARENT_LOGGER_LEVEL));
                if (actuallevel != null) {
                    if ((actuallevel.toInt() <= Priority.WARN_INT)) {
                        // set the active icon if the loglevelvalue lower than the WARNlevelvalue
                        setIconPath(PATH_WARN_ACTIVE);
                        setHelpText(Messages.get().container("LOGADMIN_LEVEL_WARN_HELP_ACTIV"));
                    } else {
                        // set the inactive icon if the loglevelvalue is higher than the WARNlevelvalue
                        setIconPath(PATH_WARN_INACTIVE);
                        setHelpText(Messages.get().container("LOGADMIN_LEVEL_WARN_HELP"));
                    }
                }
                return super.isVisible();
            }
        };
        WarnActAction.setName(Messages.get().container("GUI_JOBS_LIST_ACTION_MWARN_NAME_0"));
        WarnActAction.setHelpText(Messages.get().container("LOGADMIN_LEVEL_WARN_HELP"));
        colChangeWarnLevel.addDirectAction(WarnActAction);
        metadata.addColumn(colChangeWarnLevel);

        // add column for INFO level [Icon has a fix width 50px]
        CmsListColumnDefinition colChangeINFOLevel = new CmsListColumnDefinition(COLUMN_INFO);
        colChangeINFOLevel.setName(Messages.get().container("GUI_LOGADMIN_LIST_COLS_CH_LEVEL_INFO"));
        colChangeINFOLevel.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        colChangeINFOLevel.setWidth("50");
        colChangeINFOLevel.setPrintable(false);
        colChangeINFOLevel.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        colChangeINFOLevel.setListItemComparator(new CmsLogLevelListItemComparator());
        // generate an Action to activate the change of a log-channel
        CmsListDirectAction InfoActAction = new CmsChangeLogLevelAction(ACTION_LOGGING_LEVEL_INFO, Level.INFO) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                // check the Loglevel from hidden column and set the right icon
                Level actuallevel = Level.toLevel((String)getItem().get(LIST_COLUMN_PARENT_LOGGER_LEVEL));
                if (actuallevel != null) {
                    if ((actuallevel.toInt() <= Priority.INFO_INT)) {
                        // set the active icon if the loglevelvalue lower than the INFOlevelvalue
                        setIconPath(PATH_INFO_ACTIVE);
                        setHelpText(Messages.get().container("LOGADMIN_LEVEL_INFO_HELP_ACTIV"));
                    } else {
                        // set the inactive icon if the loglevelvalue is higher than the INFOlevelvalue
                        setIconPath(PATH_INFO_INACTIVE);
                        setHelpText(Messages.get().container("LOGADMIN_LEVEL_INFO_HELP"));
                    }
                }
                return super.isVisible();
            }
        };
        InfoActAction.setName(Messages.get().container("GUI_JOBS_LIST_ACTION_MINFO_NAME_0"));
        InfoActAction.setHelpText(Messages.get().container("LOGADMIN_LEVEL_INFO_HELP"));
        colChangeINFOLevel.addDirectAction(InfoActAction);
        metadata.addColumn(colChangeINFOLevel);

        // add column for DEBUG level [Icon has a fix width 50px]
        CmsListColumnDefinition colChangeDebugLevel = new CmsListColumnDefinition(COLUMN_DEBUG);
        colChangeDebugLevel.setName(Messages.get().container("GUI_LOGADMIN_LIST_COLS_CH_LEVEL_DEBUG"));
        colChangeDebugLevel.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        colChangeDebugLevel.setWidth("50");
        colChangeDebugLevel.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        colChangeDebugLevel.setPrintable(false);
        colChangeDebugLevel.setListItemComparator(new CmsLogLevelListItemComparator());
        // generate an Action to activate the change of a log-channel
        CmsListDirectAction DebugActAction = new CmsChangeLogLevelAction(ACTION_LOGGING_LEVEL_DEBUG, Level.DEBUG) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                // check the Loglevel from hidden column and set the right icon
                Level actuallevel = Level.toLevel((String)getItem().get(LIST_COLUMN_PARENT_LOGGER_LEVEL));
                if (actuallevel != null) {
                    if ((actuallevel.toInt() <= Priority.DEBUG_INT)) {
                        // set the active icon if the loglevelvalue lower than the DEBUGlevelvalue
                        setHelpText(Messages.get().container("LOGADMIN_LEVEL_DEBUG_HELP_ACTIV"));
                        setIconPath(PATH_DEBUG_ACTIVE);
                    } else {
                        // set the inactive icon if the loglevelvalue is higher than the DEBUGlevelvalue
                        setIconPath(PATH_DEBUG_INACTIVE);
                        setHelpText(Messages.get().container("LOGADMIN_LEVEL_DEBUG_HELP"));
                    }
                }
                return super.isVisible();
            }
        };
        DebugActAction.setName(Messages.get().container("GUI_JOBS_LIST_ACTION_MDEBUG_NAME_0"));
        DebugActAction.setHelpText(Messages.get().container("LOGADMIN_LEVEL_DEBUG_HELP"));
        colChangeDebugLevel.addDirectAction(DebugActAction);
        metadata.addColumn(colChangeDebugLevel);

        // add column for OFF level [Icon has a fix width 50px]
        CmsListColumnDefinition colChangeOffLevel = new CmsListColumnDefinition(COLUMN_OFF);
        colChangeOffLevel.setName(Messages.get().container("GUI_LOGADMIN_LIST_COLS_CH_LEVEL_OFF"));
        colChangeOffLevel.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        colChangeOffLevel.setWidth("50");
        colChangeOffLevel.setSorteable(true);
        colChangeOffLevel.setPrintable(false);
        colChangeOffLevel.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        colChangeOffLevel.setListItemComparator(new CmsLogLevelListItemComparator());
        // generate an Action to activate the change of a log-channel
        CmsListDirectAction OffActAction = new CmsChangeLogLevelAction(ACTION_LOGGING_LEVEL_OFF, Level.OFF) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                // check the Loglevel from hidden column and set the right icon
                Level actuallevel = Level.toLevel((String)getItem().get(LIST_COLUMN_PARENT_LOGGER_LEVEL));
                if (actuallevel != null) {
                    if (actuallevel.toInt() == Priority.OFF_INT) {
                        // set the active icon if the loglevelvalue lower than the OFFlevelvalue
                        setIconPath(PATH_OFF_ACTIVE);
                        setHelpText(Messages.get().container("LOGADMIN_LEVEL_OFF_HELP_ACTIV"));

                    } else {
                        // set the inactive icon if the loglevelvalue is higher than the OFFlevelvalue
                        setIconPath(PATH_OFF_INACTIVE);
                        setHelpText(Messages.get().container("LOGADMIN_LEVEL_OFF_HELP"));
                    }
                }
                return super.isVisible();
            }
        };
        OffActAction.setName(Messages.get().container("GUI_JOBS_LIST_ACTION_MOFF_NAME_0"));
        OffActAction.setHelpText(Messages.get().container("LOGADMIN_LEVEL_OFF_HELP"));
        colChangeOffLevel.addDirectAction(OffActAction);
        metadata.addColumn(colChangeOffLevel);

        // add column for the actual log-level  
        CmsListColumnDefinition LogLevel = new CmsListColumnDefinition(LIST_COLUMN_PARENT_LOGGER_LEVEL);
        LogLevel.setName(Messages.get().container("GUI_LOGADMIN_LIST_COLS_EF_LEVEL"));
        LogLevel.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        LogLevel.setWidth("10%");
        LogLevel.setSorteable(true);
        LogLevel.setPrintable(true);
        LogLevel.setVisible(false);
        metadata.addColumn(LogLevel);

        CmsListColumnDefinition Logger = new CmsListColumnDefinition(LIST_COLUMN_LOGGERS);
        Logger.setName(Messages.get().container("GUI_LOGADMIN_LIST_COLS_LOGGER"));
        Logger.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        Logger.setSorteable(true);
        Logger.setWidth("50%");
        Logger.setPrintable(true);
        metadata.addColumn(Logger);

        CmsListColumnDefinition colParentLogger = new CmsListColumnDefinition(LIST_COLUMN_PARENT);
        colParentLogger.setName(Messages.get().container("GUI_LOGADMIN_LIST_COLS_PARENT_LOGGER"));
        colParentLogger.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        colParentLogger.setSorteable(true);
        colParentLogger.setWidth("20%");
        colParentLogger.setPrintable(false);
        metadata.addColumn(colParentLogger);

        CmsListColumnDefinition colAppendLogger = new CmsListColumnDefinition(LIST_APPEND_LOGGER);
        colAppendLogger.setName(Messages.get().container("GUI_LOGADMIN_LIST_COLS_LOG_FILE"));
        colAppendLogger.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        colAppendLogger.setSorteable(true);
        colAppendLogger.setWidth("20%");
        colAppendLogger.setPrintable(false);
        metadata.addColumn(colAppendLogger);

        CmsListColumnDefinition colChangeLogFile = new CmsListColumnDefinition(LIST_CHANGE_LOGFILE);
        colChangeLogFile.setName(Messages.get().container("GUI_LOGADMIN_LIST_COLS_CH_FILE"));
        colChangeLogFile.setAlign(CmsListColumnAlignEnum.ALIGN_LEFT);
        colChangeLogFile.setSorteable(false);
        colChangeLogFile.setWidth("25");
        colChangeLogFile.setPrintable(false);
        CmsListDirectAction activatelogchannel = new CmsListDirectAction(ACTION_ACTIVATE_LOGFILE) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                Logger logger = LogManager.getLogger((String)getItem().get(LIST_COLUMN_LOGGERS));
                if (logger != null) {
                    if (isloggingactivated(logger)) {
                        setIconPath(PATH_FILE_ACTIVE);
                        setHelpText(Messages.get().container("LOGADMIN_LEVEL_LOGFILE_HELP_ACTIV"));

                    } else {
                        setIconPath(PATH_FILE_INACTIVE);
                        setHelpText(Messages.get().container("LOGADMIN_LEVEL_LOGFILE_HELP"));
                    }
                }
                return super.isVisible();
            }
        };
        activatelogchannel.setName(Messages.get().container("GUI_JOBS_LIST_ACTION_MLOGFILE_NAME_0"));
        activatelogchannel.setHelpText(Messages.get().container("LOGADMIN_ACTIVATE_LOGFILE_HELP"));
        activatelogchannel.setIconPath(PATH_OFF_ACTIVE);
        colChangeLogFile.addDirectAction(activatelogchannel);
        metadata.addColumn(colChangeLogFile);

        CmsListSearchAction searchAction = new CmsListSearchAction(metadata.getColumnDefinition(LIST_COLUMN_LOGGERS));
        metadata.setSearchAction(searchAction);

        // add column for activation information
        CmsListColumnDefinition actInfoCol = new CmsListColumnDefinition(LIST_COLUMN_ACTIVE);
        actInfoCol.setVisible(false);

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // TODO: Auto-generated method stub

    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add the Fatal multi action
        CmsListMultiAction settoFatal = new CmsChangeLogLevelMultiAction(SET_TO_FATAL);
        settoFatal.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MFATAL_HELP_0));
        settoFatal.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MFATAL_CONF_0));
        settoFatal.setIconPath(PATH_FATAL_ACTIVE);
        settoFatal.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MFATAL_HELP_0));
        metadata.addMultiAction(settoFatal);

        // add the Error multi action
        CmsListMultiAction settoError = new CmsChangeLogLevelMultiAction(SET_TO_ERROR);
        settoError.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MERROR_HELP_0));
        settoError.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MERROR_CONF_0));
        settoError.setIconPath(PATH_ERROR_ACTIVE);
        settoError.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MERROR_HELP_0));
        metadata.addMultiAction(settoError);

        // add the Warn multi action
        CmsListMultiAction settoWarn = new CmsChangeLogLevelMultiAction(SET_TO_WARN);
        settoWarn.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MWARN_HELP_0));
        settoWarn.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MWARN_CONF_0));
        settoWarn.setIconPath(PATH_WARN_ACTIVE);
        settoWarn.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MWARN_HELP_0));
        metadata.addMultiAction(settoWarn);

        // add the Info multi action
        CmsListMultiAction settoInfo = new CmsChangeLogLevelMultiAction(SET_TO_INFO);
        settoInfo.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MINFO_HELP_0));
        settoInfo.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MINFO_CONF_0));
        settoInfo.setIconPath(PATH_INFO_ACTIVE);
        settoInfo.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MINFO_HELP_0));
        metadata.addMultiAction(settoInfo);

        // add the Debug multi action
        CmsListMultiAction settoDebug = new CmsChangeLogLevelMultiAction(SET_TO_DEBUG);
        settoDebug.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MDEBUG_HELP_0));
        settoDebug.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MDEBUG_CONF_0));
        settoDebug.setIconPath(PATH_DEBUG_ACTIVE);
        settoDebug.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MDEBUG_HELP_0));
        metadata.addMultiAction(settoDebug);

        // add the Off multi action
        CmsListMultiAction settoOff = new CmsChangeLogLevelMultiAction(SET_TO_OFF);
        settoOff.setName(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MOFF_HELP_0));
        settoOff.setConfirmationMessage(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MOFF_CONF_0));
        settoOff.setIconPath(PATH_OFF_ACTIVE);
        settoOff.setHelpText(Messages.get().container(Messages.GUI_JOBS_LIST_ACTION_MOFF_HELP_0));
        metadata.addMultiAction(settoOff);
    }

    /**
     * Help function to get all loggers from LogManager
     * 
     * @return List of Logger
     * 
     * */
    private List<Logger> getLoggers() {

        // list of all loggers
        List<Logger> definedLoggers = new ArrayList<Logger>();
        // list of all parent loggers
        List<Logger> packageLoggers = new ArrayList<Logger>();
        @SuppressWarnings("unchecked")
        List<Logger> curentloggerlist = Collections.list(LogManager.getCurrentLoggers());
        Iterator<Logger> it_curentlogger = curentloggerlist.iterator();
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
                    // generate new logger with "org.opencms" prefix and the next element between the points e.g.: "org.opencms.search"
                    Logger temp_logger = Logger.getLogger(prefix[i] + "." + temp.substring(0, temp.indexOf(".")));
                    // activate the heredity so the logger get the appender from parent logger
                    temp_logger.setAdditivity(true);
                    // add the logger to the packageLoggers list if it is not part of it
                    if (!packageLoggers.contains(temp_logger)) {
                        if (temp_logger.getName().equals("om.lkacon")) {
                            String te = "test";
                        }
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

    /** Simple check if the logger has the global log file <p>
     * or a single one.  
     * 
     * @param logchannel the channel that has do be checked
     * @return true if the the log channel has a single log file
     * */
    boolean isloggingactivated(Logger logchannel) {

        boolean check = false;
        @SuppressWarnings("unchecked")
        List<Appender> appenders = Collections.list(logchannel.getAllAppenders());
        Iterator<Appender> app = appenders.iterator();
        while (app.hasNext()) {
            check = app.next().getName().equals(logchannel.getName());
        }
        return check;
    }

    /** Simple function to set all child loggers to the same value of <p>
     * parent logger if the parent logger leves is changed.<p>
     * 
     * @param logchannel the channel that might be the parent logger
     * */
    private void isparentlogger(Logger logchannel) {

        // get all logchannels
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

    /** Simpel function to get the prefix of an logchannel name.
     * 
     * @param logname the full name of the logging channel
     * */
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
                    // if it´s the first piece of the name only it will be set
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
            String nullreturn[] = new String[1];
            nullreturn[0] = logname;
            return nullreturn;
        }
    }
}
