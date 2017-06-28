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

import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.util.CmsRfsException;
import org.opencms.util.CmsRfsFileViewer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Class for the view of log files.<p>
 */
public class CmsLogFileView extends VerticalLayout {

    /**vaadin serial id.*/
    private static final long serialVersionUID = -6323034856756469160L;

    /**Vaadin component. */
    private ComboBox m_charset;

    /**Vaadin component. */
    protected FileDownloader m_fileDownloader;

    /**Vaadin component. */
    private Label m_fileContent;

    /**Vaadin component. */
    private ComboBox m_logfile;

    /**RfsFileView holding data for log to show. */
    private CmsRfsFileViewer m_logView;

    /**Vaadin component. */
    private TextField m_size;

    /**
     * constructor.<p>
     *
     * @param app which uses this view
     */
    protected CmsLogFileView(final CmsLogFileApp app) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        List<Logger> allLogger = CmsLogFileApp.getLoggers();
        List<FileAppender> allAppender = new ArrayList<FileAppender>();

        allLogger.add(0, LogManager.getRootLogger());

        for (Logger logger : allLogger) {

            @SuppressWarnings("unchecked")
            List<Appender> appenders = Collections.list(logger.getAllAppenders());
            for (Appender appen : appenders) {
                if (appen instanceof FileAppender) {
                    if (!allAppender.contains(appen)) {
                        allAppender.add((FileAppender)appen);
                    }
                }
            }
        }

        SortedMap<String, Charset> csMap = Charset.availableCharsets();
        // default charset: see http://java.sun.com/j2se/corejava/intl/reference/faqs/index.html#default-encoding
        // before java 1.5 there is no other way (System property "file.encoding" is implementation detail not in vmspec.
        Charset defaultCs = Charset.forName(new OutputStreamWriter(new ByteArrayOutputStream()).getEncoding());
        Charset cs;
        Iterator<Charset> it = csMap.values().iterator();
        while (it.hasNext()) {
            cs = it.next();
            m_charset.addItem(cs);
        }

        m_charset.select(defaultCs);
        m_charset.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1899253995224124911L;

            public void valueChange(ValueChangeEvent event) {

                updateView();
            }
        });

        for (File file : new File(CmsLogFileApp.LOG_FOLDER).listFiles()) {
            if (!file.getAbsolutePath().endsWith(".zip")) {
                m_logfile.addItem(file.getAbsolutePath());
            }
        }

        m_logfile.setFilteringMode(FilteringMode.CONTAINS);

        m_logView = (CmsRfsFileViewer)OpenCms.getWorkplaceManager().getFileViewSettings().clone();

        selectLogFile(allAppender, m_logView.getFilePath());
        m_size.setValue(String.valueOf(m_logView.getWindowSize()));

        m_logfile.setNullSelectionAllowed(false);
        m_logfile.setNewItemsAllowed(false);

        m_logfile.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1899253995224124911L;

            public void valueChange(ValueChangeEvent event) {

                updateView();
            }
        });

        m_size.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 3396575545135969283L;

            public void valueChange(ValueChangeEvent event) {

                updateView();
            }
        });

        try {
            m_fileContent.setContentMode(ContentMode.HTML);
            String content = "<pre>";
            content += m_logView.readFilePortion();
            content += "</pre>";
            m_fileContent.setValue(content);
            m_fileContent.setHeight("700px");
            m_fileContent.addStyleName("v-scrollable");
            m_fileContent.addStyleName("o-report");

        } catch (CmsRfsException e) {
            //
        }
    }

    /**
     * Updates the log file view after changes.<p>
     */
    protected void updateView() {

        try {
            m_logView.setFilePath((String)m_logfile.getValue());
            m_logView.setWindowSize(Integer.valueOf(m_size.getValue()).intValue());
            m_logView.setFileEncoding(((Charset)m_charset.getValue()).name());
            String content = "<pre>";
            content += m_logView.readFilePortion();
            content += "</pre>";
            m_fileContent.setValue(content);
        } catch (CmsRfsException e) {
            //
        }

    }

    /**
     * Selects the currently set log file.<p>
     *
     * @param appender all given appender
     * @param filePath of log file
     */
    private void selectLogFile(List<FileAppender> appender, String filePath) {

        for (FileAppender app : appender) {
            if (app.getFile().equals(filePath)) {
                m_logfile.select(app.getFile());
                return;
            }
        }
        m_logfile.select(appender.get(0).getFile()); //Default, take file from root appender
    }
}
