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
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Window;

/**
 * Class for the Download dialog.<p>
 */
public class CmsLogDownloadDialog extends CmsBasicDialog {

    /**vaadin serial id.*/
    private static final long serialVersionUID = -7447640082260176245L;

    /**Path to zip file.*/
    private static final String ZIP_PATH = CmsLogFileApp.LOG_FOLDER + "logs.zip";

    /**Logger.*/
    private static Log LOG = CmsLog.getLog(CmsLogDownloadDialog.class.getName());

    /**Vaadin component.*/
    private ComboBox m_file;

    /**Vaadin component.*/
    private Button m_ok;

    /**Vaadin component.*/
    private Button m_cancel;

    /**Vaadin component.**/
    private CheckBox m_donwloadAll;

    /**File downloader. */
    protected FileDownloader m_downloader;

    /**total size of log files in MB.*/
    private double m_totalSize;

    /**
     * public constructor.<p>
     *
     * @param window to hold the dialog
     * @param filePath path of currently shown file
     */
    public CmsLogDownloadDialog(final Window window, String filePath) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_totalSize = 0;
        for (File file : new File(CmsLogFileApp.LOG_FOLDER).listFiles()) {
            if (!file.getAbsolutePath().endsWith(".zip")) {
                m_file.addItem(file.getAbsolutePath());
                m_totalSize += file.length() / (1024 * 1024);
            }
        }

        m_donwloadAll.setVisible(m_totalSize < 150);

        m_file.setFilteringMode(FilteringMode.CONTAINS);
        m_file.setNullSelectionAllowed(false);
        m_file.setNewItemsAllowed(false);

        m_file.select(filePath);

        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 4336654148546091114L;

            public void buttonClick(ClickEvent event) {

                window.close();

            }

        });
        m_downloader = new FileDownloader(getDownloadResource());
        m_downloader.extend(m_ok);

        ValueChangeListener listener = new ValueChangeListener() {

            private static final long serialVersionUID = -1127012396158402096L;

            public void valueChange(ValueChangeEvent event) {

                m_downloader.setFileDownloadResource(getDownloadResource());
                setComboBoxEnable();

            }
        };

        m_file.addValueChangeListener(listener);
        m_donwloadAll.addValueChangeListener(listener);

    }

    /**
     * Creates log-file resource for download.<p>
     *
     * @return vaadin resource
     */
    protected Resource getDownloadResource() {

        String pathToDownload = (String)m_file.getValue();

        if (m_donwloadAll.getValue().booleanValue()) {
            pathToDownload = ZIP_PATH;
            writeZipFile();
        }

        final File downloadFile = new File(pathToDownload);

        return new StreamResource(new StreamResource.StreamSource() {

            private static final long serialVersionUID = -8868657402793427460L;

            public InputStream getStream() {

                try {
                    return new FileInputStream(downloadFile);
                } catch (FileNotFoundException e) {
                    return null;
                }

            }
        }, downloadFile.getName());
    }

    /**
     * En/ disables the combo box for files.<p>
     */
    protected void setComboBoxEnable() {

        m_file.setEnabled(!m_donwloadAll.getValue().booleanValue());
    }

    /**
     * Adds a file to a zip-stream.<p>
     *
     * @param directory to be zipped
     * @param file to be added zo zip
     * @param zos zip-stream
     * @throws IOException exception
     */
    private void addToZip(File directory, File file, ZipOutputStream zos) throws IOException {

        FileInputStream fis = new FileInputStream(file);
        String zipFilePath = file.getCanonicalPath().substring(
            directory.getCanonicalPath().length() + 1,
            file.getCanonicalPath().length());
        ZipEntry zipEntry = new ZipEntry(zipFilePath);
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }
        zos.closeEntry();
        fis.close();
    }

    /**
     * Writes the zip file with all logs.<p>
     */
    private void writeZipFile() {

        try {
            FileOutputStream fos = new FileOutputStream(ZIP_PATH);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (File file : new File(CmsLogFileApp.LOG_FOLDER).listFiles()) {
                if (!file.isDirectory() & !ZIP_PATH.equals(file.getAbsolutePath())) {
                    addToZip(new File(CmsLogFileApp.LOG_FOLDER), file, zos);
                }
            }
            zos.close();
            fos.close();

        } catch (IOException e) {
            LOG.error("unable to build zip file", e);
        }
    }
}
