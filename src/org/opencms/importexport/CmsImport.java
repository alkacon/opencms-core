/*
* File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/CmsImport.java,v $
* Date   : $Date: 2003/08/28 12:56:26 $
* Version: $Revision: 1.6 $
*
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.main.OpenCms;

import com.opencms.boot.CmsBase;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.flex.CmsEvent;
import com.opencms.flex.I_CmsEventListener;
import com.opencms.report.I_CmsReport;
import com.opencms.template.A_CmsXmlContent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Holds the functionaility to import resources from the filesystem
 * or a zip file into the OpenCms VFS.
 *
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.6 $ $Date: 2003/08/28 12:56:26 $
 */
public class CmsImport implements Serializable {


    /**
     * list to store all file modifiaction interface implementations
     */
    protected static List m_ImportImplementations;
   
    /** The algorithm for the message digest */
    public static final String C_IMPORT_DIGEST = "MD5";

    /** The cms contect to do the operations on the VFS/COS with */
    protected CmsObject m_cms;

    /** The xml manifest-file */
    protected Document m_docXml;

    /** The object to report the log messages */
    protected I_CmsReport m_report = null;

    /** The import-resource (folder) to load resources from */
    protected File m_importResource = null;

    /**  The import-resource (zip) to load resources from */
    protected ZipFile m_importZip = null;

    /** Indicates if module data is being imported */
    protected boolean m_importingChannelData;

    /** The import-file to load resources from */
    protected String m_importFile;

    /** The import-path to write resources into the cms */
    protected String m_importPath;

    /**
     * The version of this import, noted in the info tag of the manifest.xml.<p>
     * 
     * 0 indicates an export file without a version number, that is before version 4.3.23 of OpenCms.<br>
     * 1 indicates an export file of OpenCms with a version before 5.0.0
     * 2 indicates an export file of OpenCms with a version before 5.1.2
     * 3 indicates an export file of OpenCms with a version before 5.1.6
     * 4 indicates an export file of OpenCms with a version after 5.1.6
     */
    protected int m_importVersion = 0;


    /** Digest for taking a fingerprint of the files */
    protected MessageDigest m_digest = null;


    /**
     * Constructs a new uninitialized import, required for the module data import.<p>
     * 
     * @see CmsImportModuledata
     */
    public CmsImport()  { }

    /**
     * Constructs a new import object which imports the resources from an OpenCms 
     * export zip file or a folder in the "real" file system.<p>
     *
     * @param cms the current cms object
     * @param importFile the file or folder to import from
     * @param importPath the path in the cms VFS to import into
     * @param report a report object to output the progress information to
     * @throws CmsException if something goes wrong
     */
    public CmsImport(CmsObject cms, String importFile, String importPath, I_CmsReport report) throws CmsException {
        // set member variables
        m_cms = cms;
        m_importFile = importFile;
        m_importPath = importPath;
        m_report = report;
        m_importingChannelData = false;
        // try to get all import implementations
        // This has only made once.
        if (m_ImportImplementations == null) {
            m_ImportImplementations=m_cms.getRegistry().getImportClasses();
        }
    }

    /**
     * Imports the resources and writes them to the cms VFS, even if there 
     * already exist files with the same name.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    public void importResources() throws CmsException {
        importResources(null, null, null, null, null);
    }

    /**
     * Imports the resources for a module.<p>
     * 
     * @param excludeList filenames of files and folders which should not 
     *      be (over)written in the virtual file system (not used when null)
     * @param writtenFilenames filenames of the files and folder which have actually been 
     *      successfully written (not used when null)
     * @param fileCodes code of the written files (for the registry)
     *      (not used when null)
     * @param propertyName name of a property to be added to all resources
     * @param propertyValue value of that property
     * @throws CmsException if something goes wrong
     */
    public void importResources(Vector excludeList, Vector writtenFilenames, Vector fileCodes, String propertyName, String propertyValue) throws CmsException {
        // initialize the import
        boolean run=false;
        openImportFile();              
        try {
            // now find the correct import implementation         
            Iterator i=m_ImportImplementations.iterator();
            while (i.hasNext()) {
                 I_CmsImport imp=((I_CmsImport)i.next());
                    if (imp.getVersion()==m_importVersion) {
                        // this is the correct import version, so call it for the import process
                        imp.importResources(m_cms, m_importPath, m_report, 
                                            m_digest, m_importResource, m_importZip, m_docXml, excludeList, writtenFilenames, fileCodes, propertyName, propertyValue);
                        run=true;
                        break;                    
                    }
            }   
            if (!run) {
                m_report.println(m_report.key("report.import_db_noclass"), I_CmsReport.C_FORMAT_WARNING);               
            }
        } catch (CmsException e) {
            throw e;
        } finally {
            // close the import file
            closeImportFile();
            OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES, Collections.EMPTY_MAP, false));
        }
    }

    /**
     * Initalizes the import.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void openImportFile() throws CmsException {
        // create the digest
        createDigest();

        // open the import resource
        getImportResource();

        // read the xml-config file
        getXmlConfigFile();

        // try to read the export version nummber
        try {
            m_importVersion = Integer.parseInt(getTextNodeValue((Element)m_docXml.getElementsByTagName(I_CmsConstants.C_EXPORT_TAG_INFO).item(0), I_CmsConstants.C_EXPORT_TAG_VERSION));
        } catch (Exception e) {
            //ignore the exception, the export file has no version nummber (version 0).
        }
        m_report.println("Import Version "+m_importVersion, I_CmsReport.C_FORMAT_NOTE);
    }

    /**
     * Closes the import file.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void closeImportFile() throws CmsException {
        if (m_importZip != null) {
            try {
                m_importZip.close();
            } catch (IOException exc) {
                m_report.println(exc);
                throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
            }
        }
    }

    /**
     * Read infos from the properties and create a MessageDigest.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    private void createDigest() throws CmsException {
        // Configurations config = m_cms.getConfigurations();

        String digest = C_IMPORT_DIGEST;
        // create the digest
        try {
            m_digest = MessageDigest.getInstance(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new CmsException("Could'nt create MessageDigest with algorithm " + digest);
        }
    }


    /**
     * Returns a list of files which are both in the import and in the virtual file system.<p>
     * 
     * @return Vector of Strings, complete path of the files
     * @throws CmsException if something goes wrong
     */
    public Vector getConflictingFilenames() throws CmsException {
        NodeList fileNodes;
        Element currentElement;
        String source, destination;
        Vector conflictNames = new Vector();
        try {
            // get all file-nodes
            fileNodes = m_docXml.getElementsByTagName(I_CmsConstants.C_EXPORT_TAG_FILE);

            // walk through all files in manifest
            for (int i = 0; i < fileNodes.getLength(); i++) {
                currentElement = (Element)fileNodes.item(i);
                source = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_SOURCE);
                destination = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DESTINATION);
                if (source != null) {
                    // only consider files
                    boolean exists = true;
                    try {
                        CmsResource res = m_cms.readFileHeader(m_importPath + destination);
                        if (res.getState() == I_CmsConstants.C_STATE_DELETED) {
                            exists = false;
                        }
                    } catch (CmsException e) {
                        exists = false;
                    }
                    if (exists) {
                        conflictNames.addElement(m_importPath + destination);
                    }
                }
            }
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
        if (m_importZip != null) {
            try {
                m_importZip.close();
            } catch (IOException exc) {
                throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
            }
        }
        return conflictNames;
    }

    /**
     * Returns a byte array containing the content of the file.<p>
     *
     * @param filename the name of the file to read
     * @return a byte array containing the content of the file
     */
    protected byte[] getFileBytes(String filename) {
        try {
            // is this a zip-file?
            if (m_importZip != null) {
                // yes
                ZipEntry entry = m_importZip.getEntry(filename);
                InputStream stream = m_importZip.getInputStream(entry);

                int charsRead = 0;
                int size = new Long(entry.getSize()).intValue();
                byte[] buffer = new byte[size];
                while (charsRead < size) {
                    charsRead += stream.read(buffer, charsRead, size - charsRead);
                }
                stream.close();
                return buffer;
            } else {
                // no - use directory
                File file = new File(m_importResource, filename);
                FileInputStream fileStream = new FileInputStream(file);

                int charsRead = 0;
                int size = new Long(file.length()).intValue();
                byte[] buffer = new byte[size];
                while (charsRead < size) {
                    charsRead += fileStream.read(buffer, charsRead, size - charsRead);
                }
                fileStream.close();
                return buffer;
            }
        } catch (FileNotFoundException fnfe) {
            m_report.println(fnfe);
        } catch (IOException ioe) {
            m_report.println(ioe);
        }
        // this will only be returned in case there was an exception
        return "".getBytes();
    }

    /**
     * Gets the import resource and stores it in object-member.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void getImportResource() throws CmsException {
        try {
            // get the import resource
            m_importResource = new File(CmsBase.getAbsolutePath(m_importFile));

            // if it is a file it must be a zip-file
            if (m_importResource.isFile()) {
                m_importZip = new ZipFile(m_importResource);
            }
        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Returns a Vector of resource names that are needed to create a project for this import.<p>
     * 
     * It calls the method getConflictingFileNames if needed, to calculate these resources.
     * 
     * @return a Vector of resource names that are needed to create a project for this import
     * @throws CmsException if something goes wrong
     */
    public Vector getResourcesForProject() throws CmsException {
        NodeList fileNodes;
        Element currentElement;
        String destination;
        Vector resources = new Vector();
        try {
            if (m_importingChannelData) {
                m_cms.getRequestContext().saveSiteRoot();
                m_cms.setContextToCos();
            }

            // get all file-nodes
            fileNodes = m_docXml.getElementsByTagName(I_CmsConstants.C_EXPORT_TAG_FILE);
            // walk through all files in manifest
            for (int i = 0; i < fileNodes.getLength(); i++) {
                currentElement = (Element)fileNodes.item(i);
                destination = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DESTINATION);

                // get the resources for a project
                try {
                    String resource = destination.substring(0, destination.indexOf("/", 1) + 1);
                    resource = m_importPath + resource;
                    // add the resource, if it dosen't already exist
                    if ((!resources.contains(resource)) && (!resource.equals(m_importPath))) {
                        try {
                            m_cms.readFolder(resource);
                            // this resource exists in the current project -> add it
                            resources.addElement(resource);
                        } catch (CmsException exc) {
                            // this resource is missing - we need the root-folder
                            resources.addElement(I_CmsConstants.C_ROOT);
                        }
                    }
                } catch (StringIndexOutOfBoundsException exc) {
                    // this is a resource in root-folder: ignore the excpetion
                }
            }
        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            if (m_importingChannelData) {
                m_cms.getRequestContext().restoreSiteRoot();
            }
        }
        if (m_importZip != null) {
            try {
                m_importZip.close();
            } catch (IOException exc) {
                m_report.println(exc);
                throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
            }
        }
        if (resources.contains(I_CmsConstants.C_ROOT)) {
            // we have to import root - forget the rest!
            resources.removeAllElements();
            resources.addElement(I_CmsConstants.C_ROOT);
        }
        return resources;
    }

    /**
     * Returns the text for a node.
     *
     * @param elem the parent element
     * @param tag the tagname to get the value from
     * @return the value of the tag
     */
    protected String getTextNodeValue(Element elem, String tag) {
        try {
            return elem.getElementsByTagName(tag).item(0).getFirstChild().getNodeValue();
        } catch (Exception exc) {
            // ignore the exception and return null
            return null;
        }
    }

    /**
     * Gets the xml-config file from the import resource and stores it in object-member.
     * Checks whether the import is from a module file
     * 
     * @throws CmsException if something goes wrong
     */
    private void getXmlConfigFile() throws CmsException {
        try {
            InputStream in = new ByteArrayInputStream(getFileBytes(I_CmsConstants.C_EXPORT_XMLFILENAME));
            try {
                m_docXml = A_CmsXmlContent.getXmlParser().parse(in);
            } finally {
                try {
                    in.close();
                } catch (Exception e) { }
            }
        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

}