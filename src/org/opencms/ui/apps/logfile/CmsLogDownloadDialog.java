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

import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.shared.ui.combobox.FilteringMode;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.ComboBox;

/**
 * Class for the Download dialog.<p>
 */
public class CmsLogDownloadDialog extends CmsBasicDialog {

    /**
     * Helper class for generating the zip file for the log download.<p>
     */
    public static class ZipGenerator {

        /** The set of generated parent directories. */
        private Set<String> m_directories = new HashSet<>();

        /** The zip output. */
        private ZipOutputStream m_zos;

        /**
         * Creates a new instance.<p>
         *
         * @param output the output stream to write to
         */
        public ZipGenerator(OutputStream output) {

            m_zos = new ZipOutputStream(output);
        }

        /**
         * Adds a file to a zip-stream.<p>
         *
         * @param directory to be zipped
         * @param file to be added zo zip
         * @throws IOException exception
         */
        public void addToZip(File directory, File file) throws IOException {

            FileInputStream fis = new FileInputStream(file);
            String dirPath = directory.getCanonicalPath();
            String filePath = file.getCanonicalPath();
            String zipFilePath;
            if (CmsStringUtil.isPrefixPath(dirPath, filePath)) {
                zipFilePath = filePath.substring(dirPath.length() + 1, filePath.length());
            } else {
                String parentName = generateParentName(file);
                if (!m_directories.contains(parentName)) {
                    m_zos.putNextEntry(new ZipEntry(parentName));
                }
                m_directories.add(parentName);
                zipFilePath = CmsStringUtil.joinPaths(parentName, file.getName());
            }
            ZipEntry zipEntry = new ZipEntry(zipFilePath);
            m_zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                m_zos.write(bytes, 0, length);
            }
            m_zos.closeEntry();
            fis.close();
        }

        /**
         * Closes the zip stream.<p>
         *
         * @throws IOException if something goes wrong
         */
        public void close() throws IOException {

            m_zos.close();
        }

        /**
         * Generates the name of the parent directory in the zip file for a non-standard log file location.<p>
         *
         * @param file the log file
         * @return the generated parent directory name
         */
        String generateParentName(File file) {

            // we might be on Windows, so we can't just assume path is /foo/bar/baz
            List<String> pathComponents = new ArrayList<>();
            for (int i = 0; i < (file.toPath().getNameCount() - 1); i++) {
                pathComponents.add(file.toPath().getName(i).toString());
            }
            // need trailing slash when writing directory entries to ZipOutputStream
            return CmsStringUtil.listAsString(pathComponents, "_") + "/";
        }

    }

    /** Logger.*/
    private static Log LOG = CmsLog.getLog(CmsLogDownloadDialog.class.getName());

    /**vaadin serial id.*/
    private static final long serialVersionUID = -7447640082260176245L;

    /** Path to zip file.*/
    private static final String ZIP_PATH = CmsLogFileApp.LOG_FOLDER + "logs.zip";

    /**File downloader. */
    protected FileDownloader m_downloader;

    /**Vaadin component.*/
    private Button m_cancel;

    /**Vaadin component.**/
    private CheckBox m_donwloadAll;

    /**Vaadin component.*/
    private ComboBox m_file;

    /**Vaadin component.*/
    private Button m_ok;

    /**total size of log files in MB.*/
    private double m_totalSize;

    /** The download provider to be used for this dialog. */ 
    private I_CmsLogDownloadProvider m_logProvider;

    /**
     * public constructor.<p>
     *
     * @param window to hold the dialog
     * @param filePath path of currently shown file
     */
    public CmsLogDownloadDialog(final Window window, String filePath, I_CmsLogDownloadProvider logProvider) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_logProvider = logProvider;

        for (String path : m_logProvider.getLogFiles()) {
            m_file.addItem(path);
        }

        m_donwloadAll.setVisible(m_logProvider.canDownloadAllLogs());

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
        if (pathToDownload == null) {
            return null;
        }

        if (m_donwloadAll.getValue().booleanValue()) {

            return new StreamResource(
                () -> m_logProvider.readAllLogs(),
                m_logProvider.getDownloadPrefix() + "logs.zip");
        }

        return new StreamResource(
            () -> m_logProvider.readLog(pathToDownload),
            m_logProvider.getDownloadPrefix() + CmsResource.getName(pathToDownload));
    }

    /**
     * En/ disables the combo box for files.<p>
     */
    protected void setComboBoxEnable() {

        m_file.setEnabled(!m_donwloadAll.getValue().booleanValue());
    }

}
