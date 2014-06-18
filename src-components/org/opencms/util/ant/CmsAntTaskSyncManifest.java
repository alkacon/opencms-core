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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.util.ant;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import org.dom4j.Document;
import org.dom4j.Node;
import org.xml.sax.InputSource;

/**
 * Ant task for synchronizing local directory into a manifest xml file.<p>
 * 
 * @since 8.0.0
 */
public class CmsAntTaskSyncManifest extends Task {

    /** Base directory (for example, <tt>modules/org.opencms.ade.containerpage/resources</tt>). */
    private String m_base; // required

    /** Source directory (for example, <tt>modules/org.opencms.ade.containerpage/resources/system/modules/org.opencms.ade.containerpage/resources/containerpage</tt>). */
    private String m_directory; // required

    /** Absolute path to the manifest file. */
    private String m_dstManifestFile; // required

    /** The directory prefix. */
    private String m_prefix;

    /**
     * Absolute path to the manifest file.
     */
    private String m_srcManifestFile; // required

    /**
     * Default constructor.<p>
     */
    public CmsAntTaskSyncManifest() {

        super();
    }

    /**
     * Test case.<p>
     * 
     * @param args not used
     */
    public static void main(String[] args) {

        CmsAntTaskSyncManifest task = new CmsAntTaskSyncManifest();
        final String BASE_PATH = "../modules/org.opencms.ade.test/resources";
        task.setBase(BASE_PATH);
        task.setDirectory(BASE_PATH + "system/modules/org.opencms.ade.test/resources/test");
        task.setSrcManifestFile(BASE_PATH + "manifest.xml");
        task.setDstManifestFile(BASE_PATH + "manifest.synched.xml");
        task.execute();
    }

    /**
     * Run the task.<p>
     * 
     * Sets the given property to <code>__ABORT__</code> if canceled, or to a list of selected
     * modules if not.<p>
     * 
     * @throws BuildException if something goes wrong
     * 
     * @see org.apache.tools.ant.Task#execute()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws BuildException {

        try {
            // read xml
            Document doc;
            log("Reading manifest: " + getSrcManifestFile(), Project.MSG_VERBOSE);
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(getSrcManifestFile()));
            try {
                doc = CmsXmlUtils.unmarshalHelper(new InputSource(in), null);
            } finally {
                in.close();
            }

            // get directory content
            String directory = getDirectory();
            log("Dir to synch: " + directory, Project.MSG_VERBOSE);

            String[] files = new File(directory).list();
            // XXX: If empty directory, a nice null pointer exception is thrown
            log("Files found to sync: " + Arrays.toString(files), Project.MSG_VERBOSE);
            for (int i = 0; i < files.length; i++) {
                File file = new File(directory, files[i]);
                if (!file.isDirectory()) {
                    continue;
                }
                // prepare new file array
                String[] children = file.list();
                String[] newFiles = new String[files.length + children.length];
                System.arraycopy(files, 0, newFiles, 0, i + 1);
                for (int j = 0; j < children.length; j++) {
                    newFiles[j + i + 1] = files[i] + '/' + children[j];
                }
                if ((i + 1) < files.length) {
                    System.arraycopy(files, i + 1, newFiles, i + 1 + children.length, files.length - (i + 1));
                }
                files = newFiles;
            }

            // delete superfluous entries
            String prefix = m_prefix != null ? m_prefix : new File(directory).getAbsolutePath().substring(
                new File(getBase()).getAbsolutePath().length() + 1).replace('\\', '/');

            List<Node> xmlFiles = doc.selectNodes("export/files/file/destination[starts-with(.,'" + prefix + "/')]");
            for (Node xmlFile : xmlFiles) {
                String path = xmlFile.getText();
                File file = new File(getDirectory() + path.substring(prefix.length()));
                if (!file.exists()) {
                    log("Removing old entry for non-existing file '" + path + "'", Project.MSG_DEBUG);
                    CmsSetupXmlHelper.setValue(doc, getFileXpath(path), null);
                }
            }

            // create missing entries
            for (int i = 0; i < files.length; i++) {
                String destination = prefix + '/' + files[i];
                if (destination.endsWith("/CVS") || destination.contains("/CVS/")) {
                    // ignore cvs data
                    continue;
                }
                String xpath = getFileXpath(destination);
                if (CmsSetupXmlHelper.getValue(doc, xpath) != null) {
                    // entry already present
                    log("Skipping existing entry '" + xpath + "'", Project.MSG_DEBUG);
                    continue;
                }
                // create entry
                String prevDest = prefix + (i == 0 ? "" : '/' + files[i - 1]);
                log("Adding new entry for file '" + destination + "'", Project.MSG_DEBUG);
                createEntry(doc, prevDest, destination);
            }

            // write xml
            log("Writing manifest: " + getDstManifestFile(), Project.MSG_VERBOSE);
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(getDstManifestFile()));
            try {
                CmsXmlUtils.marshal(doc, out, "UTF-8");
            } finally {
                out.close();
            }
        } catch (Exception e) {
            throw new BuildException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Returns the required base path.<p>
     * 
     * @return for example,
     *         <tt>modules/org.opencms.ade.containerpage/resources</tt>
     */
    public String getBase() {

        return m_base;
    }

    /**
     * Returns the required directory path.<p>
     * 
     * @return for example,
     *         <tt>modules/org.opencms.ade.containerpage/resources/system/modules/org.opencms.ade.containerpage/resources/containerpage</tt>
     */
    public String getDirectory() {

        return m_directory;
    }

    /**
     * Returns the absolute path where the synched manifest will be written.<p>
     * 
     * @return absolute path where the synched manifest will be written
     */
    public String getDstManifestFile() {

        return m_dstManifestFile;
    }

    /**
     * Returns the directory prefix.<p>
     *
     * @return the directory prefix
     */
    public String getPrefix() {

        return m_prefix;
    }

    /**
     * Returns the absolute path to the source manifest file.<p>
     * 
     * @return absolute path to the source manifest file
     */
    public String getSrcManifestFile() {

        return m_srcManifestFile;
    }

    /**
     * Sets the required base path.<p>
     * 
     * @param base the required base path to set
     */
    public void setBase(String base) {

        m_base = base;
    }

    /**
     * Sets the required directory path.<p>
     * 
     * @param directory the required directory path to set
     */
    public void setDirectory(String directory) {

        m_directory = directory;
    }

    /**
     * Sets the absolute path where the synched manifest will be written.<p>
     * 
     * @param dstManifestFile absolute path where the synched manifest will be written
     */
    public void setDstManifestFile(String dstManifestFile) {

        m_dstManifestFile = dstManifestFile;
    }

    /**
     * Sets the directory prefix.<p>
     *
     * @param prefix the directory prefix to set
     */
    public void setPrefix(String prefix) {

        m_prefix = prefix;
    }

    /**
     * Sets the absolute path to the source manifest file.<p>
     * 
     * @param srcManifestFile absolute path to the source manifest file
     */
    public void setSrcManifestFile(String srcManifestFile) {

        m_srcManifestFile = srcManifestFile;
    }

    /**
     * Creates a new XML entry for the given destination path.<p>
     * 
     * The node will be created just after the given previous destination path.<p>
     * 
     * @param doc the xml document to modify
     * @param prevDest the previous destination
     * @param destination the destination path
     */
    private void createEntry(Document doc, String prevDest, String destination) {

        String xpath = getFileXpath(destination);
        String type = getType(destination);
        if (!type.equals("folder")) {
            xpath = "export/files/file[source[text()='" + destination + "']]";
        }
        CmsSetupXmlHelper.setValue(doc, getFileXpath(prevDest), null, xpath.substring("export/files/".length()));
        if (!type.equals("folder")) {
            CmsSetupXmlHelper.setValue(doc, xpath + "/destination", destination);
        }
        CmsSetupXmlHelper.setValue(doc, xpath + "/type", type);
        CmsSetupXmlHelper.setValue(doc, xpath + "/uuidstructure", new CmsUUID().toString());
        CmsSetupXmlHelper.setValue(doc, xpath + "/uuidresource", new CmsUUID().toString());
        CmsSetupXmlHelper.setValue(
            doc,
            xpath + "/datelastmodified",
            CmsDateUtil.getHeaderDate(System.currentTimeMillis()));
        CmsSetupXmlHelper.setValue(doc, xpath + "/userlastmodified", "Admin");
        CmsSetupXmlHelper.setValue(doc, xpath + "/datecreated", CmsDateUtil.getHeaderDate(System.currentTimeMillis()));
        CmsSetupXmlHelper.setValue(doc, xpath + "/usercreated", "Admin");
        CmsSetupXmlHelper.setValue(doc, xpath + "/flags", "0");
        CmsSetupXmlHelper.setValue(doc, xpath + "/properties", "");
        CmsSetupXmlHelper.setValue(doc, xpath + "/relations", "");
        CmsSetupXmlHelper.setValue(doc, xpath + "/accesscontrol", "");
    }

    /**
     * Retrieves the xpath for the given destination path.<p>
     * 
     * @param destination the destination path
     * 
     * @return the xpath for the given destination path
     */
    private String getFileXpath(String destination) {

        return "export/files/file[destination[text()='" + destination + "']]";
    }

    /**
     * Retrieves the type for the given destination path.<p>
     * 
     * @param destination the destination path
     * 
     * @return the type for the given destination path
     */
    private String getType(String destination) {

        if (destination.endsWith(".jpg") || destination.endsWith(".gif") || destination.endsWith(".png")) {
            return "image";
        }
        // use binary type for GWT specific files (*.cache.html, hosted.html, *.nocache.js, *.rpc) to avoid their content being indexed or editable
        if (destination.endsWith(".jar")
            || destination.endsWith(".class")
            || destination.endsWith(".cache.html")
            || destination.endsWith("hosted.html")
            || destination.endsWith(".nocache.js")
            || destination.endsWith(".rpc")) {
            return "binary";
        }
        if (destination.endsWith(".jsp")) {
            return "jsp";
        }
        if (destination.endsWith(".properties")
            || destination.endsWith(".xsd")
            || destination.endsWith(".txt")
            || destination.endsWith(".java")
            || destination.endsWith(".html")
            || destination.endsWith(".js")
            || destination.endsWith(".xml")
            || destination.endsWith(".css")) {
            return "plain";
        }
        return "folder";
    }
}
