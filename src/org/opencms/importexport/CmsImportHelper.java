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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.importexport;

import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.xml.CmsXmlEntityResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;

/**
 * Import helper.<p>
 *
 * @since 7.0.4
 */
public class CmsImportHelper {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsImport.class);

    /** The folder, or <code>null</code> if a zip file.*/
    private File m_folder;

    /** The import parameters to use. */
    private CmsImportParameters m_params;

    /** The zip file, or <code>null</code> if a folder.*/
    private ZipFile m_zipFile;

    /**
     * Constructor.<p>
     *
     * @param parameters the import parameters to use
     */
    public CmsImportHelper(CmsImportParameters parameters) {

        m_params = parameters;
    }

    /**
     * Adds a new DTD system id prefix mapping for internal resolution
     * of external URLs.<p>
     *
     * @param dtdSystemLocation the internal system location of the DTD file, e.g. <code>org/opencms/configuration/</code>
     * @param dtdFilename the name of the DTD file, e.g. <code>opencms-configuration.dtd</code>
     * @param dtdUrlPrefix the external system id prefix of the DTD file, e.g. <code>http://www.opencms.org/dtd/6.0/</code>
     *
     * @see org.opencms.configuration.I_CmsXmlConfiguration
     */
    public void cacheDtdSystemId(String dtdSystemLocation, String dtdFilename, String dtdUrlPrefix) {

        if (dtdSystemLocation != null) {
            try {
                String file = CmsFileUtil.readFile(dtdSystemLocation + dtdFilename, CmsEncoder.ENCODING_UTF_8);
                CmsXmlEntityResolver.cacheSystemId(
                    dtdUrlPrefix + dtdFilename,
                    file.getBytes(CmsEncoder.ENCODING_UTF_8));
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        org.opencms.configuration.Messages.get().getBundle().key(
                            org.opencms.configuration.Messages.LOG_CACHE_DTD_SYSTEM_ID_1,
                            new Object[] {dtdUrlPrefix + dtdFilename + " --> " + dtdSystemLocation + dtdFilename}));
                }
            } catch (IOException e) {
                LOG.error(
                    org.opencms.configuration.Messages.get().getBundle().key(
                        org.opencms.configuration.Messages.LOG_CACHE_DTD_SYSTEM_ID_FAILURE_1,
                        new Object[] {dtdSystemLocation + dtdFilename}),
                    e);
            }
        }
    }

    /**
     * Closes the zip file.<p>
     */
    public void closeFile() {

        if (getZipFile() != null) {
            try {
                getZipFile().close();
            } catch (IOException e) {
                CmsMessageContainer message = Messages.get().container(
                    Messages.ERR_IMPORTEXPORT_ERROR_CLOSING_ZIP_ARCHIVE_1,
                    getZipFile().getName());
                if (LOG.isDebugEnabled()) {
                    LOG.debug(message.key(), e);
                }
            }
        }
    }

    /**
     * Returns a byte array containing the content of the file.<p>
     *
     * @param filename the name of the file to read, relative to the folder or zip file
     *
     * @return a byte array containing the content of the file
     *
     * @throws CmsImportExportException if something goes wrong
     */
    public byte[] getFileBytes(String filename) throws CmsImportExportException {

        try {
            // is this a zip-file?
            if (getZipFile() != null) {

                ZipEntry entry = getZipEntry(filename);
                InputStream stream = getZipFile().getInputStream(entry);
                int size = Long.valueOf(entry.getSize()).intValue();
                return CmsFileUtil.readFully(stream, size);
            } else {
                // no - use directory
                File file = getFile(filename);
                return CmsFileUtil.readFile(file);
            }
        } catch (FileNotFoundException fnfe) {
            CmsMessageContainer msg = Messages.get().container(Messages.ERR_IMPORTEXPORT_FILE_NOT_FOUND_1, filename);
            if (LOG.isErrorEnabled()) {
                LOG.error(msg.key(), fnfe);
            }
            throw new CmsImportExportException(msg, fnfe);
        } catch (IOException ioe) {
            CmsMessageContainer msg = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_READING_FILE_1,
                filename);
            if (LOG.isErrorEnabled()) {
                LOG.error(msg.key(), ioe);
            }
            throw new CmsImportExportException(msg, ioe);
        }
    }

    public long getFileModification(String filename) throws CmsImportExportException {

        long modificationTime = 0;

        try {
            // is this a zip-file?
            if (getZipFile() != null) {
                // yes
                ZipEntry entry = getZipEntry(filename);
                modificationTime = entry.getTime();
            } else {
                // no - use directory
                File file = getFile(filename);
                modificationTime = file.lastModified();
            }
            if (modificationTime < 0) {
                return 0;
            } else {
                return modificationTime;
            }
        } catch (IOException ioe) {
            CmsMessageContainer msg = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_READING_FILE_1,
                filename);
            if (LOG.isErrorEnabled()) {
                LOG.error(msg.key(), ioe);
            }
            throw new CmsImportExportException(msg, ioe);
        }
    }

    /**
     * Returns the name of the import file, without zip extension.<p>
     *
     * @return the name of the import file, without zip extension
     */
    public String getFileName() {

        String fileName = m_params.getPath().replace('\\', '/');
        String zipName = fileName.substring(fileName.lastIndexOf('/') + 1);
        String result;

        if (zipName.toLowerCase().endsWith(".zip")) {
            result = zipName.substring(0, zipName.lastIndexOf('.'));
            int pos = result.lastIndexOf('_');
            if (pos > 0) {
                result = result.substring(0, pos);
            }
        } else {
            result = zipName;
        }
        return result;
    }

    /**
     * Returns a stream for the content of the file.<p>
     *
     * @param fileName the name of the file to stream, relative to the folder or zip file
     *
     * @return an input stream for the content of the file, remember to close it after using
     *
     * @throws CmsImportExportException if something goes wrong
     */
    public InputStream getFileStream(String fileName) throws CmsImportExportException {

        try {
            InputStream stream = null;
            // is this a zip-file?
            if (getZipFile() != null) {
                // yes
                ZipEntry entry = getZipFile().getEntry(fileName);
                // path to file might be relative, too
                if ((entry == null) && fileName.startsWith("/")) {
                    entry = getZipFile().getEntry(fileName.substring(1));
                }
                if (entry == null) {
                    throw new ZipException(
                        Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_FILE_NOT_FOUND_IN_ZIP_1, fileName));
                }

                stream = getZipFile().getInputStream(entry);
            } else {
                // no - use directory
                File file = new File(getFolder(), CmsImportExportManager.EXPORT_MANIFEST);
                stream = new FileInputStream(file);
            }
            return stream;
        } catch (Exception ioe) {
            CmsMessageContainer msg = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_READING_FILE_1,
                fileName);
            if (LOG.isErrorEnabled()) {
                LOG.error(msg.key(), ioe);
            }
            throw new CmsImportExportException(msg, ioe);
        }
    }

    /**
     * Returns the RFS folder to import from.<p>
     *
     * @return the RFS folder to import from
     */
    public File getFolder() {

        return m_folder;
    }

    /**
     * Returns the class system location.<p>
     *
     * i.e: <code>org/opencms/importexport</code><p>
     *
     * @param clazz the class to get the location for
     *
     * @return the class system location
     */
    public String getLocation(Class<?> clazz) {

        String filename = clazz.getName().replace('.', '/');
        int pos = filename.lastIndexOf('/') + 1;
        return (pos > 0 ? filename.substring(0, pos) : "");
    }

    /**
     * Returns the RFS zip file to import from.<p>
     *
     * @return the RFS zip file to import from
     */
    public ZipFile getZipFile() {

        return m_zipFile;
    }

    /**
     * Opens the import file.<p>
     *
     * @throws IOException if the file could not be opened
     */
    public void openFile() throws IOException {

        // get the import resource
        m_folder = new File(OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(m_params.getPath()));

        // if it is a file it must be a zip-file
        if (m_folder.isFile()) {
            m_zipFile = new ZipFile(m_params.getPath());
            m_folder = null;
        }
    }

    /** Returns the file for the provided filename.
     * @param filename name of the file
     * @return the file.
     */
    protected File getFile(String filename) {

        return new File(getFolder(), filename);
    }

    /** Returns the zip entry for a file in the archive.
     * @param filename the file name
     * @return the zip entry for the file with the provided name
     * @throws ZipException thrown if the file is not in the zip archive
     */
    protected ZipEntry getZipEntry(String filename) throws ZipException {

        // yes
        ZipEntry entry = getZipFile().getEntry(filename);
        // path to file might be relative, too
        if ((entry == null) && filename.startsWith("/")) {
            entry = m_zipFile.getEntry(filename.substring(1));
        }
        if (entry == null) {
            throw new ZipException(
                Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_FILE_NOT_FOUND_IN_ZIP_1, filename));
        }
        return entry;
    }
}
