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
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.util.CmsRfsException;
import org.opencms.util.CmsRfsFileViewer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;

import com.vaadin.server.FileDownloader;
import com.vaadin.ui.Panel;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.shared.ui.combobox.FilteringMode;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the view of log files.<p>
 */
@SuppressWarnings("deprecation")
public class CmsLogFileView extends VerticalLayout {

    /**Session attribute to store charset setting.*/
    protected static String ATTR_FILE_VIEW_CHARSET = "log-file-char";

    /**Session attribute to store currently viewed log file.*/
    protected static String ATTR_FILE_VIEW_PATH = "log-file";

    /**Session attribute to store line number to display. */
    protected static String ATTR_FILE_VIEW_SIZE = "log-file-size";

    /**Window size.*/
    protected static int WINDOW_SIZE = 1000;

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLogFileView.class);

    /**vaadin serial id.*/
    private static final long serialVersionUID = -6323034856756469160L;

    /**Flag indicates if change event should be blocked. */
    protected boolean m_blockChangeEvent;

    /**Vaadin component. */
    protected FileDownloader m_fileDownloader;

    /**Vaadin component. */

    private Label m_fileContent;

    /**Vaadin component. */
    private ComboBox m_logfile;

    /**RfsFileView holding data for log to show. */
    private CmsRfsFileViewer m_logView;

    /**vaadin component. */
    private Panel m_panelComp;

    /**App instance. */
    private CmsLogFileApp m_app;

    /**
     * constructor.<p>
     *
     * @param app which uses this view
     */
    protected CmsLogFileView(final CmsLogFileApp app) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_app = app;

        m_logView = (CmsRfsFileViewer)OpenCms.getWorkplaceManager().getFileViewSettings().clone();
        m_logView.setAdditionalRoots(CmsLogFileOptionProvider.getAdditionalLogDirectories());
        m_logView.setWindowSize(WINDOW_SIZE);
        if (CmsVaadinUtils.getRequest().getSession().getAttribute(ATTR_FILE_VIEW_SIZE) == null) {
            CmsVaadinUtils.getRequest().getSession().setAttribute(
                ATTR_FILE_VIEW_SIZE,
                String.valueOf(m_logView.getWindowSize()));
        }
        initLogFileCombo();

        m_logfile.setFilteringMode(FilteringMode.CONTAINS);
        m_logfile.setNullSelectionAllowed(false);
        m_logfile.setNewItemsAllowed(false);

        m_logfile.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1899253995224124911L;

            public void valueChange(ValueChangeEvent event) {

                if (m_blockChangeEvent) {
                    return;
                }
                CmsVaadinUtils.getRequest().getSession().setAttribute(ATTR_FILE_VIEW_PATH, getCurrentFile());
                updateView();
            }
        });

        updateView();
        m_fileContent.addStyleName("v-scrollable");
        m_fileContent.addStyleName("o-report");

    }

    /**
     * Updates the log file view after changes.<p>
     */
    public void updateView() {

        if (CmsLogFileApp.LOG_FOLDER.isEmpty()) {
            return;
        }

        try {
            initLogFileCombo();
            m_logView.setWindowSize(getSize());
            m_logView.setFileEncoding(getChar());
            String content = "<pre style='line-height:1.1;'>";
            content += m_app.getLogFilePortion(m_logView, getCurrentFile());
            content += "</pre>";
            m_fileContent.setValue(content);
            m_panelComp.setScrollTop(100000000);
        } catch (CmsRfsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

    }

    /**
     * Gets currently shown file.<p>
     *
     * @return path of shown file
     */
    protected String getCurrentFile() {

        return (String)m_logfile.getValue();
    }

    /**
     * Gets  the char set.<p>
     *
     * @return the name of the charset
     */
    private String getChar() {

        return ((Charset)CmsVaadinUtils.getRequest().getSession().getAttribute(ATTR_FILE_VIEW_CHARSET)).name();
    }

    /**
     * Gets the size to be displayed.<p>
     *
     * @return line number
     */
    private int getSize() {

        return Integer.valueOf(
            (String)CmsVaadinUtils.getRequest().getSession().getAttribute(ATTR_FILE_VIEW_SIZE)).intValue();
    }

    /**
     * Initializes the Log file combo-box.
     */
    private void initLogFileCombo() {

        m_blockChangeEvent = true;
        m_logfile.removeAllItems();
        for (String path : m_app.getAvailableLogFilePaths()) {
            m_logfile.addItem(path);
        }

        if (CmsVaadinUtils.getRequest().getSession().getAttribute(ATTR_FILE_VIEW_CHARSET) == null) {
            Charset defaultCs = Charset.forName(new OutputStreamWriter(new ByteArrayOutputStream()).getEncoding());
            CmsVaadinUtils.getRequest().getSession().setAttribute(ATTR_FILE_VIEW_CHARSET, defaultCs);
        }

        if (CmsVaadinUtils.getRequest().getSession().getAttribute(ATTR_FILE_VIEW_PATH) != null) {
            m_logfile.select(CmsVaadinUtils.getRequest().getSession().getAttribute(ATTR_FILE_VIEW_PATH));
        } else {
            selectLogFile();
        }
        m_blockChangeEvent = false;

    }

    /**
     * Selects the currently set log file.<p>
     *
     */
    private void selectLogFile() {

        m_logfile.select(m_app.getDefaultLogFilePath(m_logView));

    }
}
