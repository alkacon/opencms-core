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
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.logging.log4j.core.Appender;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.shared.ui.ValueChangeMode;
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
        OpenCms.getSystemInfo().getLogFileRfsPath().lastIndexOf(File.separatorChar) + 1);

    /**Path to channel settings view.*/
    static String PATH_LOGCHANNEL = "log-channel";

    /**Logger.*/
    private static Log LOG = CmsLog.getLog(CmsLogFileApp.class);

    /**The log file view layout.*/
    protected CmsLogFileView m_fileView;

    /** The file table filter input. */
    private TextField m_tableFilter;

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
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    @Override
    public void initUI(I_CmsAppUIContext context) {

        context.addPublishButton(updatedItems -> {
            // nothing to do
        });
        super.initUI(context);
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
            m_rootLayout.setMainHeightFull(true);
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
            m_tableFilter.setPlaceholder(
                Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
            m_tableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
            m_tableFilter.setWidth("200px");
            m_tableFilter.setValueChangeMode(ValueChangeMode.TIMEOUT);
            m_tableFilter.setValueChangeTimeout(400);
            m_tableFilter.addValueChangeListener(new ValueChangeListener<String>() {

                private static final long serialVersionUID = 1L;

                public void valueChange(ValueChangeEvent<String> event) {

                    channelTable.filterTable(event.getValue());
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
