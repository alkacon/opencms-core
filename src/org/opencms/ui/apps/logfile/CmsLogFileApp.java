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
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.util.CmsStringUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Main class of Log managment app.<p>
 */
public class CmsLogFileApp extends A_CmsWorkplaceApp {

    /**Log folder path.*/
    protected static final String LOG_FOLDER = OpenCms.getSystemInfo().getLogFileRfsPath() == null
    ? ""
    : OpenCms.getSystemInfo().getLogFileRfsPath().substring(
        0,
        OpenCms.getSystemInfo().getLogFileRfsPath().lastIndexOf("/") + 1);

    /**Path to channel settings view.*/
    static String PATH_LOGCHANNEL = "log-channel";

    /**Logger.*/
    private static Log LOG = CmsLog.getLog(CmsLogFileApp.class);

    /**The log file view layout.*/
    protected CmsLogFileView m_fileView;

    /** The file table filter input. */
    private TextField m_tableFilter;

    /**
     * Simple function to get the prefix of an logchannel name.<p>
     *
     * @param logname the full name of the logging channel
     *
     * @return a string array with different package prefixes
     */
    protected static String[] buildsufix(String logname) {

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
     * Returns the file name or <code>null</code> associated with the given appender.<p>
     *
     * @param app the appender
     *
     * @return the file name
     */
    protected static String getFileName(Appender app) {

        String result = null;
        Method getFileName;
        try {
            getFileName = app.getClass().getDeclaredMethod("getFileName", (Class<?>[])null);

            result = (String)getFileName.invoke(app, (Object[])null);
        } catch (Exception e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        return result;
    }

    /**
     * Help function to get all loggers from LogManager.<p>
     *
     * @return List of Logger
     */
    protected static List<Logger> getLoggers() {

        // list of all loggers
        List<Logger> definedLoggers = new ArrayList<Logger>();
        // list of all parent loggers
        List<Logger> packageLoggers = new ArrayList<Logger>();
        for (Logger log : ((LoggerContext)LogManager.getContext(false)).getLoggers()) {
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
                    Logger temp_logger = (Logger)LogManager.getLogger(
                        prefix[i] + "." + temp.substring(0, temp.indexOf(".")));
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
     * Checks whether the given log appender has a getFileName method to identify file based appenders.<p>
     * As since log4j 2.0 file appenders don't have one common super class that allows access to the file name,
     * but they all implement a method 'getFileName'.<p>
     *
     * @param appender the appender to check
     *
     * @return in case of a file based appender
     */
    protected static boolean isFileAppender(Appender appender) {

        boolean result = false;
        try {
            Method getFileNameMethod = appender.getClass().getDeclaredMethod("getFileName", (Class<?>[])null);
            result = getFileNameMethod != null;

        } catch (Exception e) {
            LOG.debug(e.getLocalizedMessage(), e);
        }
        return result;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();

        //Check if state is empty -> start
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_LOGFILE_VIEW_TOOL_NAME_0));
            return crumbs;
        }
        if (state.equals(PATH_LOGCHANNEL)) {
            crumbs.put(
                CmsLogFileConfiguration.APP_ID,
                CmsVaadinUtils.getMessageText(Messages.GUI_LOGFILE_VIEW_TOOL_NAME_0));
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_LOGFILE_LOGSETTINGS_TOOL_NAME_0));
            return crumbs;
        }

        return new LinkedHashMap<String, String>();
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        if (m_tableFilter != null) {
            m_infoLayout.removeComponent(m_tableFilter);
            m_tableFilter = null;
        }

        if (state.isEmpty()) {
            m_rootLayout.setMainHeightFull(false);
            m_fileView = new CmsLogFileView(this);
            addDownloadButton(m_fileView);
            addSettingsButton();
            addChannelButton();
            addRefreshButton();
            return m_fileView;
        }

        m_uiContext.clearToolbarButtons();

        if (state.equals(PATH_LOGCHANNEL)) {
            m_rootLayout.setMainHeightFull(true);
            final CmsLogChannelTable channelTable = new CmsLogChannelTable();
            m_tableFilter = new TextField();
            m_tableFilter.setIcon(FontOpenCms.FILTER);
            m_tableFilter.setInputPrompt(
                Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
            m_tableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
            m_tableFilter.setWidth("200px");
            m_tableFilter.addTextChangeListener(new TextChangeListener() {

                private static final long serialVersionUID = 1L;

                public void textChange(TextChangeEvent event) {

                    channelTable.filterTable(event.getText());
                }
            });
            m_infoLayout.addComponent(m_tableFilter);
            return channelTable;
        }

        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

    /**
     * Button to open channel settings path.<p>
     */
    private void addChannelButton() {

        Button button = CmsToolBar.createButton(
            FontOpenCms.LOG,
            CmsVaadinUtils.getMessageText(Messages.GUI_LOGFILE_LOGSETTINGS_TOOL_NAME_0));
        button.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                openSubView(PATH_LOGCHANNEL, true);
            }
        });
        m_uiContext.addToolbarButton(button);

    }

    /**
     * Adds the download button.
     *
     * @param view layout which displays the log file
     */
    private void addDownloadButton(final CmsLogFileView view) {

        Button button = CmsToolBar.createButton(
            FontOpenCms.DOWNLOAD,
            CmsVaadinUtils.getMessageText(Messages.GUI_LOGFILE_DOWNLOAD_0));
        button.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                Window window = CmsBasicDialog.prepareWindow(CmsBasicDialog.DialogWidth.wide);
                window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_LOGFILE_DOWNLOAD_0));
                window.setContent(new CmsLogDownloadDialog(window, view.getCurrentFile()));
                A_CmsUI.get().addWindow(window);
            }
        });
        m_uiContext.addToolbarButton(button);
    }

    /**
     * Button to refresh the file view.<p>
     */
    private void addRefreshButton() {

        Button button = CmsToolBar.createButton(
            FontOpenCms.RESET,
            CmsVaadinUtils.getMessageText(Messages.GUI_LOGFILE_REFRESH_FILEVIEW_0));
        button.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                m_fileView.updateView();

            }
        });
        m_uiContext.addToolbarButton(button);
    }

    /**
     * Button to open log file view settings dialog.<p>
     */
    private void addSettingsButton() {

        Button button = CmsToolBar.createButton(
            FontOpenCms.SETTINGS,
            CmsVaadinUtils.getMessageText(Messages.GUI_LOGFILE_LOGSETTINGS_TOOL_NAME_SHORT_0));
        button.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                Window window = CmsBasicDialog.prepareWindow(CmsBasicDialog.DialogWidth.wide);
                window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_LOGFILE_LOGVIEW_SETTINGS_SHORT_0));
                window.setContent(new CmsLogFileViewSettings(window));
                window.addCloseListener(new CloseListener() {

                    private static final long serialVersionUID = -7058276628732771106L;

                    public void windowClose(CloseEvent e) {

                        m_fileView.updateView();
                    }
                });
                A_CmsUI.get().addWindow(window);
            }
        });
        m_uiContext.addToolbarButton(button);
    }
}
