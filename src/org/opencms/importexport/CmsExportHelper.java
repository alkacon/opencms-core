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

package org.opencms.importexport;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.file.CmsFile;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsXmlSaxWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.dom4j.io.SAXWriter;
import org.xml.sax.SAXException;

/**
 * Wrapper to write exported OpenCms resources either to a .ZIP file or to the file system.<p>
 *
 * @since 7.5.1
 */
public class CmsExportHelper {

    /** Length that can be safely written to ZIP output. */
    private static final int SUB_LENGTH = 4096;

    /** The main export path. */
    private String m_exportPath;

    /** The export ZIP stream to write resources to. */
    private ZipOutputStream m_exportZipStream;

    /** Indicates if the resources are exported in one export .ZIP file or as individual files. */
    private boolean m_isExportAsFiles;

    /** The SAX writer for the Manifest file. */
    private SAXWriter m_saxWriter;

    /**
     * Creates a new export helper.<p>
     *
     * @param exportPath the export path
     * @param exportAsFiles indicates if the resources should be exported as individual files or in one big ZIP file
     * @param validateXml indicates of the manifest.xml should be validated
     *
     * @throws SAXException in case of issues creating the manifest.xml
     * @throws IOException in case of file access issues
     */
    public CmsExportHelper(String exportPath, boolean exportAsFiles, boolean validateXml)
    throws SAXException, IOException {

        m_exportPath = exportPath;
        m_isExportAsFiles = exportAsFiles;

        removeOldExport(exportPath);

        Writer writer;
        if (m_isExportAsFiles) {
            m_exportPath = m_exportPath + "/";
            // write to file system directly
            String fileName = getRfsFileName(CmsImportExportManager.EXPORT_MANIFEST);
            File rfsFile = new File(fileName);
            rfsFile.getParentFile().mkdirs();
            rfsFile.createNewFile();
            writer = new FileWriter(rfsFile);
        } else {
            // create the export ZIP stream
            m_exportZipStream = new ZipOutputStream(new FileOutputStream(m_exportPath));
            // delegate writing to a String writer
            writer = new StringWriter(SUB_LENGTH);
        }

        // generate the SAX XML writer
        CmsXmlSaxWriter saxHandler = new CmsXmlSaxWriter(writer, OpenCms.getSystemInfo().getDefaultEncoding());
        saxHandler.setEscapeXml(true);
        saxHandler.setEscapeUnknownChars(true);
        // start the document
        saxHandler.startDocument();

        // set the doctype if needed
        if (validateXml) {
            saxHandler.startDTD(
                CmsImportExportManager.N_EXPORT,
                null,
                CmsConfigurationManager.DEFAULT_DTD_PREFIX + CmsImportVersion7.DTD_FILENAME);
            saxHandler.endDTD();
        }
        // initialize the dom4j writer object
        m_saxWriter = new SAXWriter(saxHandler, saxHandler);
    }

    /**
     * Returns the SAX writer for the Manifest file.<p>
     *
     * @return the SAX writer for the Manifest file
     */
    public SAXWriter getSaxWriter() {

        return m_saxWriter;
    }

    /**
     * Writes a single OpenCms VFS file to the export.<p>
     *
     * @param file the OpenCms VFS file to write
     * @param name the name of the file in the export
     *
     * @throws IOException in case of file access issues
     */
    public void writeFile(CmsFile file, String name) throws IOException {

        if (m_isExportAsFiles) {
            writeFile2Rfs(file, name);
        } else {
            writeFile2Zip(file, name);
        }
    }

    /**
     * Writes the OpenCms manifest.xml file to the export.<p>
     *
     * @param xmlSaxWriter the SAX writer to use
     *
     * @throws SAXException in case of issues creating the manifest.xml
     * @throws IOException in case of file access issues
     */
    public void writeManifest(CmsXmlSaxWriter xmlSaxWriter) throws IOException, SAXException {

        if (m_isExportAsFiles) {
            writeManifest2Rfs(xmlSaxWriter);
        } else {
            writeManifest2Zip(xmlSaxWriter);
        }
    }

    /**
     * Returns the RFS file name for the given OpenCms VFS file name.<p>
     *
     * @param name the OpenCms VFS file name
     *
     * @return the RFS file name for the given OpenCms VFS file name
     */
    protected String getRfsFileName(String name) {

        return m_exportPath + name;
    }

    /**
     * Removes the old export output, which may be an existing file or directory.<p>
     *
     * @param exportPath the export output path
     */
    protected void removeOldExport(String exportPath) {

        File output = new File(exportPath);
        if (output.exists()) {
            // the output already exists
            if (output.isDirectory()) {
                // purge the complete directory
                CmsFileUtil.purgeDirectory(output);
            } else {
                // remove the existing file
                if (m_isExportAsFiles) {
                    // in case we write to a file we can just overwrite,
                    // but for a folder we must remove an existing file
                    output.delete();
                }
            }
        }
    }

    /**
     * Writes a single OpenCms VFS file to the RFS export.<p>
     *
     * @param file the OpenCms VFS file to write
     * @param name the name of the file in the export
     *
     * @throws IOException in case of file access issues
     */
    protected void writeFile2Rfs(CmsFile file, String name) throws IOException {

        String fileName = getRfsFileName(name);
        File rfsFile = new File(fileName);
        if (!rfsFile.getParentFile().exists()) {
            rfsFile.getParentFile().mkdirs();
        }
        rfsFile.createNewFile();
        FileOutputStream rfsFileOut = new FileOutputStream(rfsFile);
        rfsFileOut.write(file.getContents());
        rfsFileOut.close();
    }

    /**
     * Writes a single OpenCms VFS file to the ZIP export.<p>
     *
     * @param file the OpenCms VFS file to write
     * @param name the name of the file in the export
     *
     * @throws IOException in case of file access issues
     */
    protected void writeFile2Zip(CmsFile file, String name) throws IOException {

        ZipEntry entry = new ZipEntry(name);
        // save the time of the last modification in the zip
        entry.setTime(file.getDateLastModified());
        m_exportZipStream.putNextEntry(entry);
        m_exportZipStream.write(file.getContents());
        m_exportZipStream.closeEntry();
    }

    /**
     * Writes the OpenCms manifest.xml file to the RFS export.<p>
     *
     * In case of the RFS export the file is directly written to a file output stream,
     * so calling this method just closes the XML and finishes the stream.<p>
     *
     * @param xmlSaxWriter the SAX writer to use
     *
     * @throws SAXException in case of issues creating the manifest.xml
     * @throws IOException in case of issues closing the file writer
     */
    protected void writeManifest2Rfs(CmsXmlSaxWriter xmlSaxWriter) throws SAXException, IOException {

        // close the document - this will also trigger flushing the contents to the file system
        xmlSaxWriter.endDocument();
        xmlSaxWriter.getWriter().close();
    }

    /**
     * Writes the OpenCms manifest.xml file to the ZIP export.<p>
     *
     * In case of the ZIP export the manifest is written to an internal StringBuffer
     * first, which is then stored in the ZIP file when this method is called.<p>
     *
     * @param xmlSaxWriter the SAX writer to use
     *
     * @throws SAXException in case of issues creating the manifest.xml
     * @throws IOException in case of file access issues
     */
    protected void writeManifest2Zip(CmsXmlSaxWriter xmlSaxWriter) throws IOException, SAXException {

        // close the document
        xmlSaxWriter.endDocument();
        xmlSaxWriter.getWriter().close();

        // create ZIP entry for the manifest XML document
        ZipEntry entry = new ZipEntry(CmsImportExportManager.EXPORT_MANIFEST);
        m_exportZipStream.putNextEntry(entry);

        // complex substring operation is required to ensure handling for very large export manifest files
        StringBuffer result = ((StringWriter)xmlSaxWriter.getWriter()).getBuffer();
        int steps = result.length() / SUB_LENGTH;
        int rest = result.length() % SUB_LENGTH;
        int pos = 0;
        for (int i = 0; i < steps; i++) {
            String sub = result.substring(pos, pos + SUB_LENGTH);
            m_exportZipStream.write(sub.getBytes(OpenCms.getSystemInfo().getDefaultEncoding()));
            pos += SUB_LENGTH;
        }
        if (rest > 0) {
            String sub = result.substring(pos, pos + rest);
            m_exportZipStream.write(sub.getBytes(OpenCms.getSystemInfo().getDefaultEncoding()));
        }

        // close the zip entry for the manifest XML document
        m_exportZipStream.closeEntry();

        // finally close the zip stream
        m_exportZipStream.close();
    }
}