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
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.I_CmsCRUDApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsAppViewLayout;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.util.CmsLog4jUtil;
import org.opencms.util.CmsRfsException;
import org.opencms.util.CmsRfsFileViewer;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.FileAppender.Builder;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;

import com.google.common.collect.ComparisonChain;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Main class of Log management app.<p>
 */
public class CmsLogFileApp extends A_CmsWorkplaceApp implements I_CmsCRUDApp<Logger> {

    /**Log folder path.*/
    protected static final String LOG_FOLDER =
            OpenCms.getSystemInfo().getLogFileRfsFolder() == null ?
                    "" : OpenCms.getSystemInfo().getLogFileRfsFolder();

    /**Path to channel settings view.*/
    protected static String PATH_LOGCHANNEL = "log-channel";

    /**Logger.*/
    private static Log LOG = CmsLog.getLog(CmsLogFileApp.class);

    /** The prefix of opencms classes. */
    private static final String OPENCMS_CLASS_PREFIX = "org.opencms";

    /**The log file view layout.*/
    protected CmsLogFileView m_fileView;

    /**The log-channel table. */
    protected CmsLogChannelTable m_table;

    /** The file table filter input. */
    private TextField m_tableFilter;

    /**
     * Gets the direct log filename of a given logger or null if no file is defined.<p>
     *
     * @param logger to be checked
     * @return Log file name or null
     */
    public static String getDirectLogFile(Logger logger) {

        LoggerConfig conf = logger.get();
        while (conf != null) {
            for (Appender appender : conf.getAppenders().values()) {
                if (CmsLogFileApp.isFileAppender(appender)) {
                    String path = CmsLogFileApp.getFileName(appender);
                    String name = path.substring(path.lastIndexOf(File.separatorChar) + 1);
                    return name;
                }
            }
            conf = conf.getParent();
        }
        return null;
    }

    /**
     * Returns the file name or <code>null</code> associated with the given appender.<p>
     *
     * @param app the appender
     *
     * @return the file name
     */
    public static String getFileName(Appender app) {

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
     * Checks whether the given log appender has a getFileName method to identify file based appenders.<p>
     * As since log4j 2.0 file appenders don't have one common super class that allows access to the file name,
     * but they all implement a method 'getFileName'.<p>
     *
     * @param appender the appender to check
     *
     * @return in case of a file based appender
     */
    public static boolean isFileAppender(Appender appender) {

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
     * Simple check if the logger has the global log file <p> or a single one.
     *
     * @param logchannel the channel that has do be checked
     * @return true if the the log channel has a single log file
     * */
    public static boolean isloggingactivated(Logger logchannel) {

        boolean check = false;
        for (Appender appender : logchannel.getAppenders().values()) {
            check = appender.getName().equals(logchannel.getName());
        }
        return check;
    }

    /**
     * Toggles the log file.<p>
     *
     * @param logchannel to toggle log file for
     */
    public static void toggleOwnFile(Logger logchannel) {

        String filepath = "";

        Layout layout = null;
        // if the button is activated check the value of the button
        // the button was active
        if (isloggingactivated(logchannel)) {
            // remove the private Appender from logger
            for (Appender appender : logchannel.getAppenders().values()) {
                logchannel.removeAppender(appender);
            }
            // activate the heredity so the logger get the appender from parent logger
            logchannel.setAdditive(true);

        }
        // the button was inactive
        else {
            // get the layout and file path from root logger
            for (Appender appender : ((Logger)LogManager.getRootLogger()).getAppenders().values()) {
                if (CmsLogFileApp.isFileAppender(appender)) {
                    String fileName = CmsLogFileApp.getFileName(appender);
                    filepath = fileName.substring(0, fileName.lastIndexOf(File.separatorChar));
                    layout = appender.getLayout();
                    break;
                }
            }

            // check if the logger has an Appender get his layout
            for (Appender appender : logchannel.getAppenders().values()) {
                if (CmsLogFileApp.isFileAppender(appender)) {
                    layout = appender.getLayout();
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

            FileAppender fapp = ((Builder)FileAppender.<FileAppender.Builder> newBuilder().withFileName(
                logfilename).withLayout(layout).withName(logchannel.getName())).build();

            // deactivate the heredity so the logger get no longer the appender from parent logger
            logchannel.setAdditive(false);
            // remove all active Appenders from logger
            for (Appender appender : logchannel.getAppenders().values()) {
                logchannel.removeAppender(appender);
            }
            // add the new created Appender to the logger
            logchannel.addAppender(fapp);
        }

    }

    /**
     * Adds a marker entry to the currently selected log file.
     *
     * @param logFile the log file name
     */
    public void addMark(String logFile) {

        LoggerContext context = (LoggerContext)LogManager.getContext(false);
        List<Logger> loggers = new ArrayList<>(context.getLoggers());
        // Sort loggers by name to prioritize parent over child loggers
        loggers.sort((l1, l2) -> ComparisonChain.start().compare(l1.getName(), l2.getName()).result());
        boolean found = false;
        loggerLoop: for (Logger logger : loggers) {
            for (Map.Entry<String, Appender> entry : logger.getAppenders().entrySet()) {
                Appender appender = entry.getValue();
                if (logFile.equals(getFileName(entry.getValue()))) {
                    String message = "---------- Mark created by '"
                        + A_CmsUI.getCmsObject().getRequestContext().getCurrentUser().getName()
                        + "' ----------";
                    LogEvent event = Log4jLogEvent.newBuilder().setLevel(Level.INFO).setLoggerName(
                        "org.opencms").setIncludeLocation(true).setLoggerFqcn(CmsLogFileApp.class.getName()).setMessage(
                            new SimpleMessage(message)).build();

                    // break loggerLoop;
                    appender.append(event);
                    found = true;
                    break loggerLoop;
                }
            }
        }

        if (!found) {
            Notification.show(CmsVaadinUtils.getMessageText(Messages.GUI_LOGFILE_LOGFILE_NOT_ACTIVE_0));
        }

    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#createElement(java.lang.Object)
     */
    public void createElement(Logger element) {

        return;

    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#defaultAction(java.lang.String)
     */
    public void defaultAction(String elementId) {

        return;

    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#deleteElements(java.util.List)
     */
    public void deleteElements(List<String> elementId) {

        return;

    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#getAllElements()
     */
    public List<Logger> getAllElements() {

        return CmsLog4jUtil.getAllLoggers();
    }

    /**
     * Gets the available log file paths.<p>
     *
     * @return Set of paths
     */
    public Set<String> getAvailableLogFilePaths() {

        Set<File> files = CmsLogFileOptionProvider.getLogFiles();
        Set<String> res = new LinkedHashSet<String>();
        for (File f : files) {
            if (!f.getAbsolutePath().endsWith(".zip") && !f.getAbsolutePath().endsWith(".gz")) {
                res.add(f.getAbsolutePath());
            }
        }
        return res;
    }

    /**
     * Gets the default log file path.<p>
     *
     * @param logView logview
     * @return log file path
     */
    public String getDefaultLogFilePath(CmsRfsFileViewer logView) {

        List<Logger> allLogger = CmsLog4jUtil.getAllLoggers();
        List<Appender> allAppender = new ArrayList<Appender>();

        allLogger.add(0, (Logger)LogManager.getRootLogger());

        for (Logger logger : allLogger) {

            for (Appender appender : logger.getAppenders().values()) {
                if (CmsLogFileApp.isFileAppender(appender)) {
                    if (!allAppender.contains(appender)) {
                        allAppender.add(appender);
                    }

                }
            }
        }
        for (Appender app : allAppender) {

            String fileName = CmsLogFileApp.getFileName(app);
            if ((fileName != null) && fileName.equals(logView.getFilePath())) {

                return fileName;
            }
        }
        if (!allAppender.isEmpty()) {
            Appender app = allAppender.get(0);
            String fileName = CmsLogFileApp.getFileName(app);
            return fileName;
        }
        return null;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#getElement(java.lang.String)
     */
    public Logger getElement(String elementId) {

        return null;
    }

    /**
     * Gets the log file for the logger.<p>
     *
     * @param logger to get log file for
     * @return log file
     */
    public String getLogFile(Logger logger) {

        return getDirectLogFile(logger);

    }

    /**
     * Gets the portion of given log file.<p>
     *
     * @param logView to get portion with
     * @param currentFile to read
     * @return portion of log file
     * @throws CmsRfsException exception
     * @throws CmsRuntimeException exception
     */
    public String getLogFilePortion(CmsRfsFileViewer logView, String currentFile)
    throws CmsRfsException, CmsRuntimeException {

        logView.setFilePath(currentFile);
        return logView.readFilePortion();
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    @Override
    public void initUI(I_CmsAppUIContext context) {

        super.initUI(context);
    }

    /**
     * Toggles if channel has own log file.<p>
     *
     * @param logchannel to toggle
     */
    public void toggleOwnFileForLogger(Logger logchannel) {

        CmsLogFileApp.toggleOwnFile(logchannel);
    }

    /**
     * Updates the log channel table.<p>
     */
    public void updateTable() {

        if (m_table != null) {
            m_table = new CmsLogChannelTable(this);
            m_table.setSizeFull();
            m_rootLayout.setMainContent(m_table);
            m_table.filterTable(m_tableFilter.getValue());
        }
    }

    /**
     * @see org.opencms.ui.apps.I_CmsCRUDApp#writeElement(java.lang.Object)
     */
    public void writeElement(Logger logger) {

        @SuppressWarnings("resource")
        LoggerContext context = logger.getContext();
        Configuration config = context.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(logger.getName());
        LoggerConfig specificConfig = loggerConfig;
        if (!loggerConfig.getName().equals(logger.getName())) {
            specificConfig = new LoggerConfig(logger.getName(), logger.getLevel(), true);
            specificConfig.setParent(loggerConfig);
            config.addLogger(logger.getName(), specificConfig);
        }
        specificConfig.setLevel(logger.getLevel());
        context.updateLoggers();

    }

    /**
     * Button to open channel settings path.<p>
     */
    protected void addChannelButton() {

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
    protected void addDownloadButton(final CmsLogFileView view) {

        Button button = CmsToolBar.createButton(
            FontOpenCms.DOWNLOAD,
            CmsVaadinUtils.getMessageText(Messages.GUI_LOGFILE_DOWNLOAD_0));
        button.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                Window window = CmsBasicDialog.prepareWindow(CmsBasicDialog.DialogWidth.wide);
                window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_LOGFILE_DOWNLOAD_0));
                window.setContent(new CmsLogDownloadDialog(window, view.getCurrentFile(), getLogDownloadProvider()));
                A_CmsUI.get().addWindow(window);
            }
        });
        m_uiContext.addToolbarButton(button);
    }

    /**
     * Button to add a marker to the current log file.
     */
    protected void addMarkButton() {

        Button button = CmsToolBar.createButton(
            FontAwesome.PLUS,
            CmsVaadinUtils.getMessageText(Messages.GUI_LOGFILE_ADD_MARK_0));
        button.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                if ((m_fileView != null) && (m_fileView.getCurrentFile() != null)) {
                    addMark(m_fileView.getCurrentFile());
                    m_fileView.updateView();
                }
            }
        });
        m_uiContext.addToolbarButton(button);

    }

    /**
     * Adds the publish button.
     */
    protected void addPublishButton() {

        m_uiContext.addToolbarButton(CmsAppViewLayout.createPublishButton(ids -> {}));
    }

    /**
     * Button to refresh the file view.<p>
     */
    protected void addRefreshButton() {

        Button button = CmsToolBar.createButton(
            FontOpenCms.RESET,
            CmsVaadinUtils.getMessageText(Messages.GUI_LOGFILE_REFRESH_FILEVIEW_0));
        button.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                CmsLog.INIT.info(
                    "Logfile was reloaded by user "
                        + A_CmsUI.getCmsObject().getRequestContext().getCurrentUser().getName());
                m_fileView.updateView();

            }
        });
        m_uiContext.addToolbarButton(button);
    }

    /**
     * Button to open log file view settings dialog.<p>
     */
    protected void addSettingsButton() {

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

    /**
     * Filters the table.<p>
     *
     * @param filter text to be filtered
     */
    protected void filterTable(String filter) {

        m_table.filterTable(filter);
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
        if (state.startsWith(PATH_LOGCHANNEL)) {
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

        if (!state.startsWith(PATH_LOGCHANNEL)) {
            m_rootLayout.setMainHeightFull(true);
            m_fileView = new CmsLogFileView(this);
            addPublishButton();
            addDownloadButton(m_fileView);
            addSettingsButton();
            addChannelButton();
            addMarkButton();
            addRefreshButton();
            m_table = null;
            return m_fileView;
        }

        m_uiContext.clearToolbarButtons();

        m_rootLayout.setMainHeightFull(true);
        m_table = new CmsLogChannelTable(this);
        m_tableFilter = new TextField();
        m_tableFilter.setIcon(FontOpenCms.FILTER);
        m_tableFilter.setPlaceholder(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        m_tableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_tableFilter.setWidth("200px");
        m_tableFilter.setValueChangeMode(ValueChangeMode.TIMEOUT);
        m_tableFilter.setValueChangeTimeout(400);
        m_tableFilter.addValueChangeListener(new ValueChangeListener<String>() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent<String> event) {

                filterTable(event.getValue());
            }
        });

        m_infoLayout.addComponent(m_tableFilter);
        return m_table;

    }

    /**
     * Gets the download provider for the log download dialog.
     */
    protected I_CmsLogDownloadProvider getLogDownloadProvider() {

        return new CmsDefaultLogDownloadProvider();
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

}
