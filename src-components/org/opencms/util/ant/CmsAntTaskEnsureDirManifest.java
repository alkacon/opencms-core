/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/util/ant/CmsAntTaskEnsureDirManifest.java,v $
 * Date   : $Date: 2010/03/11 08:09:39 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.i18n.CmsEncoder;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.dom4j.Document;
import org.dom4j.Node;
import org.xml.sax.InputSource;

/**
 * Ant task for synchronizing local directory into a manifest xml file.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsAntTaskEnsureDirManifest extends Task {

    /** base directory. */
    private String m_base; // required

    /** source directory. */
    private String m_directory; // required

    /** absolute path to the xml file. */
    private String m_xmlFile; // required

    /**
     * Default constructor.<p>
     */
    public CmsAntTaskEnsureDirManifest() {

        super();
    }

    /**
     * Test case.<p>
     * 
     * @param args not used
     */
    public static void main(String[] args) {

        CmsAntTaskEnsureDirManifest task = new CmsAntTaskEnsureDirManifest();
        task.setBase("C:\\dev\\workspace\\OpenCms\\modules\\org.opencms.ade\\resources");
        task.setDirectory("C:\\dev\\workspace\\OpenCms\\modules\\org.opencms.ade\\resources\\system\\modules\\org.opencms.ade\\gwt");
        task.setXmlFile("C:\\dev\\workspace\\OpenCms\\modules\\org.opencms.ade\\resources\\manifest.xml");
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
    @Override
    public void execute() throws BuildException {

        try {
            // read xml
            Document doc;
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(getXmlFile()));
            try {
                doc = CmsXmlUtils.unmarshalHelper(new InputSource(in), null);
            } finally {
                in.close();
            }

            // get directory content
            String directory = getDirectory();
            String[] files = new File(directory).list();
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
                if (i + 1 < files.length) {
                    System.arraycopy(files, i + 1, newFiles, i + 1 + children.length, files.length - (i + 1));
                }
                files = newFiles;
            }

            // delete superfluous entries
            String prefix = new File(directory).getAbsolutePath().substring(
                new File(getBase()).getAbsolutePath().length() + 1).replace('\\', '/');

            List<Node> xmlFiles = CmsCollectionsGenericWrapper.list(doc.selectNodes("export/files/file/destination[starts-with(.,'"
                + prefix
                + "/')]"));
            for (Node xmlFile : xmlFiles) {
                String path = xmlFile.getText();
                File file = new File(getBase() + "/" + path);
                if (!file.exists()) {
                    CmsSetupXmlHelper.setValue(doc, getFileXpath(path), null);
                }
            }

            // create missing entries
            for (int i = 0; i < files.length; i++) {
                String destination = prefix + '/' + files[i];
                String xpath = getFileXpath(destination);
                if (CmsSetupXmlHelper.getValue(doc, xpath) != null) {
                    // entry already present
                    continue;
                }
                // create entry
                String prevDest = prefix + (i == 0 ? "" : '/' + files[i - 1]);
                createEntry(doc, prevDest, destination);
            }

            // write xml
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(getXmlFile()));
            try {
                CmsXmlUtils.marshal(doc, out, CmsEncoder.ENCODING_UTF_8);
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
     * @return the required base path
     */
    public String getBase() {

        return m_base;
    }

    /**
     * Returns the required directory path.<p>
     * 
     * @return the required directory path
     */
    public String getDirectory() {

        return m_directory;
    }

    /**
     * Returns the xmlFile absolute path.<p>
     * 
     * @return the xmlFile absolute path
     */
    public String getXmlFile() {

        return m_xmlFile;
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
     * Sets the xmlFile absolute path.<p>
     * 
     * @param xmlFile the xmlFile absolute path to set
     */
    public void setXmlFile(String xmlFile) {

        m_xmlFile = xmlFile;
    }

    /**
     * Creates a new XML entry for the given destination path.<p>
     * 
     * The node will be created just after the given previous destination path.<p>
     * 
     * @param doc the xml document to modify
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

        String extension = CmsFileUtil.getExtension(destination);
        if (extension.equalsIgnoreCase(".jpg")
            || extension.equalsIgnoreCase(".gif")
            || extension.equalsIgnoreCase(".png")) {
            return "image";
        }
        if (extension.equalsIgnoreCase(".jar")) {
            return "binary";
        }
        if (extension.equalsIgnoreCase(".html")
            || extension.equalsIgnoreCase(".js")
            || extension.equalsIgnoreCase(".xml")
            || extension.equalsIgnoreCase(".rpc")
            || extension.equalsIgnoreCase(".css")) {
            return "plain";
        }
        return "folder";
    }
}