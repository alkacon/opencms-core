/*
* File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/Attic/CmsImportModuledata.java,v $
* Date   : $Date: 2003/09/05 12:22:25 $
* Version: $Revision: 1.6 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
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

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.defaults.master.CmsMasterContent;
import com.opencms.defaults.master.CmsMasterDataSet;
import com.opencms.defaults.master.CmsMasterMedia;
import com.opencms.file.CmsObject;
import com.opencms.flex.util.CmsUUID;
import org.opencms.report.I_CmsReport;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.util.Encoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.ZipEntry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Holds the functionaility to import resources from the filesystem
 * or a zip file into the OpenCms COS.
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com) 
 * 
 * @version $Revision: 1.6 $ $Date: 2003/09/05 12:22:25 $
 */
public class CmsImportModuledata extends CmsImport implements Serializable {

    /**
     * Constructs a new import object which imports the module data from an OpenCms 
     * export zip file or a folder in the "real" file system.<p>
     *
     * @param cms the current cms object
     * @param importFile the file or folder to import from
     * @param importPath the path in the cms VFS to import into
     * @param report a report object to output the progress information to
     * @throws CmsException if something goes wrong
     */
    public CmsImportModuledata(CmsObject cms, String importFile, String importPath, I_CmsReport report) throws CmsException {
        // set member variables
        m_cms = cms;
        m_importFile = importFile;
        m_importPath = importPath;
        m_report = report;
        m_importingChannelData = true;
    }

    /**
     * Imports the moduledata and writes them to the cms even if there already exist 
     * conflicting files.<p>
     * @throws CmsException in case something goes wrong
     */
    public void importResources() throws CmsException {
        // initialize the import
        openImportFile();
        try {
            // first import the channels
            m_report.println(m_report.key("report.import_channels_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            //importAllResources(null, null, null, null, null);
            // now find the correct import implementation         
            Iterator i=m_ImportImplementations.iterator();
                while (i.hasNext()) {
                    I_CmsImport imp=((I_CmsImport)i.next());
                    if (imp.getVersion()==m_importVersion) {
                        // this is the correct import version, so call it for the import process
                        imp.importResources(m_cms, m_importPath, m_report, 
                                        m_digest, m_importResource, m_importZip, m_docXml, null, null, null, null, null);
                        break;                    
                     }
                 }   
         
            m_report.println(m_report.key("report.import_channels_end"), I_CmsReport.C_FORMAT_HEADLINE);

            // now import the moduledata
            m_report.println(m_report.key("report.import_moduledata_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            importModuleMasters();
            m_report.println(m_report.key("report.import_moduledata_end"), I_CmsReport.C_FORMAT_HEADLINE);
        } catch (CmsException e) {
            throw e;
        } finally {
            // close the import file
            closeImportFile();
        }
    }

    /**
     * Gets the available modules in the current system
     * and imports the data for existing modules.<p>
     * @throws CmsException in case something goes wrong
     */
    public void importModuleMasters() throws CmsException {
        // get all available modules in this system
        Hashtable moduleExportables = new Hashtable();
        m_cms.getRegistry().getModuleExportables(moduleExportables);
        // now get the subIds of each module
        Hashtable availableModules = new Hashtable();
        Enumeration modElements = moduleExportables.elements();
        while (modElements.hasMoreElements()) {
            String classname = (String)modElements.nextElement();
            // get the subId of the module
            try {
                int subId = getContentDefinition(classname, new Class[] {CmsObject.class }, new Object[] {m_cms }).getSubId();
                // put the subid and the classname into the hashtable of available modules
                availableModules.put("" + subId, classname);
            } catch (Exception e) {
                // do nothing
            }

        }
        // now get the moduledata for import
        NodeList masterNodes;
        Element currentElement;
        String subid;

        try {
            // get all master-nodes
            masterNodes = m_docXml.getElementsByTagName(CmsExportModuledata.C_EXPORT_TAG_MASTER);
            int length = masterNodes.getLength();

            // walk through all files in manifest
            for (int i = 0; i < length; i++) {
                currentElement = (Element)masterNodes.item(i);
                // get the subid of the modulemaster
                subid = getTextNodeValue(currentElement, CmsExportModuledata.C_EXPORT_TAG_MASTER_SUBID);
                // check if there exists a module with this subid
                String classname = (String)availableModules.get(subid);
                if ((classname != null) && !("".equals(classname.trim()))) {
                    // import the dataset, the channelrelation and the media
                    m_report.print(" ( " + (i + 1) + " / " + length + " ) ");
                    importMaster(subid, classname, currentElement);
                }
            }
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Imports a single master.<p>
     * 
     * @param subId the subid of the module
     * @param classname the name of the module class
     * @param currentElement the current element of the xml file
     * @throws CmsException in case something goes wrong
     */
    private void importMaster(String subId, String classname, Element currentElement) throws CmsException {
        // print out some information to the report
        m_report.print(m_report.key("report.importing"), I_CmsReport.C_FORMAT_NOTE);

        CmsMasterDataSet newDataset = new CmsMasterDataSet();
        Vector channelRelations = new Vector();
        Vector masterMedia = new Vector();
        // try to get the dataset
        try {
            int subIdInt = Integer.parseInt(subId);
            newDataset = getMasterDataSet(subIdInt, currentElement);
        } catch (Exception e) {
            m_report.println(e);
            throw new CmsException("Cannot get dataset ", e);
        }
        m_report.print("'" + Encoder.escapeHtml(newDataset.m_title) + "' (" + classname + ")");
        m_report.print(m_report.key("report.dots"), I_CmsReport.C_FORMAT_NOTE);
        // try to get the channelrelations
        try {
            channelRelations = getMasterChannelRelation(currentElement);
        } catch (Exception e) {
            m_report.println(e);
            throw new CmsException("Cannot get channelrelations ", e);
        }
        // try to get the media
        try {
            masterMedia = getMasterMedia(currentElement);
        } catch (Exception e) {
            m_report.println(e);
            throw new CmsException("Cannot get media ", e);
        }
        // add the channels and media to the dataset
        newDataset.m_channelToAdd = channelRelations;
        newDataset.m_mediaToAdd = masterMedia;
        // create the new content definition
        CmsMasterContent newMaster = getContentDefinition(classname, new Class[] {CmsObject.class, CmsMasterDataSet.class }, new Object[] {m_cms, newDataset });
        try {
            CmsUUID userId = newMaster.getOwner();
            CmsUUID groupId = newMaster.getGroupId();
            // first insert the new master
            newMaster.importMaster();
            // now update the master because user and group might be changed
            newMaster.chown(m_cms, userId);
            newMaster.chgrp(m_cms, groupId);
        } catch (Exception e) {
            m_report.println(e);
            throw new CmsException("Cannot write master ", e);
        }
        m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
    }

    /**
     * Gets the dataset for the master from the xml file.<p>
     * 
     * @param subId the subid of the module
     * @param currentElement the current element of the xml file
     * @return the dataset with the imported information
     * @throws CmsException in case something goes wrong
     */
    private CmsMasterDataSet getMasterDataSet(int subId, Element currentElement) throws CmsException {
        String datasetfile, username, groupname, accessFlags, publicationDate, purgeDate, flags, feedId, feedReference, feedFilename, title;
        // get the new dataset object
        CmsMasterDataSet newDataset = new CmsMasterDataSet();

        // get the file with the dataset of the master
        datasetfile = getTextNodeValue(currentElement, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATASET);
        Document datasetXml = this.getXmlFile(datasetfile);
        Element dataset = datasetXml.getDocumentElement();
        // get the information from the dataset and add it to the dataset
        // first add the subid
        newDataset.m_subId = subId;
        newDataset.m_masterId = CmsUUID.getNullUUID();
        // get the id of the user or set the owner to the current user
        username = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_USER);
        CmsUUID userId = m_cms.getRequestContext().currentUser().getId();
        try {
            if ((username != null) && !("".equals(username.trim()))) {
                userId = m_cms.readUser(username).getId();
            }
        } catch (Exception e) { }
        newDataset.m_userId = userId;
        // get the id of the group or set the group to the current user        
        groupname = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_GROUP);

        CmsUUID groupId = CmsUUID.getNullUUID();
        try {
            if ((groupname != null) && !("".equals(groupname.trim()))) {
                groupId = m_cms.readGroup(groupname).getId();
            }
        } catch (Exception e) {
            try {
                groupId = m_cms.readGroup(OpenCms.getDefaultUsers().getGroupUsers()).getId();
            } catch (Exception e2) { }
        }

        newDataset.m_groupId = groupId;
        // set the accessflags or the default flags
        accessFlags = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_ACCESSFLAGS);
        try {
            newDataset.m_accessFlags = Integer.parseInt(accessFlags);
        } catch (Exception e) {
            newDataset.m_accessFlags = I_CmsConstants.C_ACCESS_DEFAULT_FLAGS;
        }
        // set the publication date
        publicationDate = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_PUBLICATIONDATE);
        try {
            newDataset.m_publicationDate = convertDate(publicationDate);
        } catch (Exception e) { }
        // set the purge date
        purgeDate = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_PURGEDATE);
        try {
            newDataset.m_purgeDate = convertDate(purgeDate);
        } catch (Exception e) { }
        // set the flags
        flags = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_FLAGS);
        try {
            newDataset.m_flags = Integer.parseInt(flags);
        } catch (Exception e) { }
        // set the feedid
        feedId = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_FEEDID);
        try {
            newDataset.m_feedId = Integer.parseInt(feedId);
        } catch (Exception e) { }
        // set the feedreference
        feedReference = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_FEEDREFERENCE);
        try {
            newDataset.m_feedReference = Integer.parseInt(feedReference);
        } catch (Exception e) { }
        // set the feedfilenam
        feedFilename = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_FEEDFILENAME);
        newDataset.m_feedFilename = feedFilename;
        // set the masters title
        title = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_TITLE);
        newDataset.m_title = title;
        // set the values of data_big
        for (int i = 0; i < newDataset.m_dataBig.length; i++) {
            String filename = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATABIG + i);
            String value = new String();
            if (filename != null && !"".equals(filename.trim())) {
                // get the value from the file
                value = new String(getFileBytes(filename));
            }
            newDataset.m_dataBig[i] = value;
        }
        // get the values of data_medium
        for (int i = 0; i < newDataset.m_dataMedium.length; i++) {
            String filename = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATAMEDIUM + i);
            String value = new String();
            if (filename != null && !"".equals(filename.trim())) {
                // get the value from the file
                value = new String(getFileBytes(filename));
            }
            newDataset.m_dataMedium[i] = value;
        }
        // get the values of data_small
        for (int i = 0; i < newDataset.m_dataSmall.length; i++) {
            String filename = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATASMALL + i);
            String value = new String();
            if (filename != null && !"".equals(filename.trim())) {
                // get the value from the file
                value = new String(getFileBytes(filename));
            }
            newDataset.m_dataSmall[i] = value;
        }
        // get the values of data_int
        for (int i = 0; i < newDataset.m_dataInt.length; i++) {
            String value = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATAINT + i);
            try {
                newDataset.m_dataInt[i] = new Integer(value).intValue();
            } catch (Exception e) {
                newDataset.m_dataInt[i] = 0;
            }
        }
        // get the values of data_reference
        for (int i = 0; i < newDataset.m_dataReference.length; i++) {
            String value = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATAREFERENCE + i);
            try {
                newDataset.m_dataReference[i] = new Integer(value).intValue();
            } catch (Exception e) {
                newDataset.m_dataReference[i] = 0;
            }
        }
        // get the values of data_date
        for (int i = 0; i < newDataset.m_dataDate.length; i++) {
            String value = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATADATE + i);
            try {
                newDataset.m_dataDate[i] = convertDate(value);
            } catch (Exception e) {
                newDataset.m_dataDate[i] = 0;
            }
        }
        return newDataset;
    }

    /**
     * Gets the channel relations for the master from the xml file.<p>
     * 
     * @param currentElement the current element of the xml file
     * @return vector containing the ids of all channels of the master
     */
    private Vector getMasterChannelRelation(Element currentElement) {
        Vector channelRelations = new Vector();
        // get the channelnames of the master
        NodeList channelNodes = currentElement.getElementsByTagName(CmsExportModuledata.C_EXPORT_TAG_MASTER_CHANNELNAME);

        // walk through all channelrelations
        for (int j = 0; j < channelNodes.getLength(); j++) {
            // get the name of the channel
            String channelName = ((Element)channelNodes.item(j)).getFirstChild().getNodeValue();
            // try to read the channel and get its channelid
            if ((channelName != null) && !("".equals(channelName.trim()))) {
                channelRelations.addElement(channelName);
            }
        }
        return channelRelations;
    }

    /**
     * Gets the media of the master from the xml file.<p>
     * 
     * @param currentElement The current element of the xml file
     * @return vector containing the media (CmsMasterMedia object) of the master
     * @throws CmsException in case something goes wrong
     */
    private Vector getMasterMedia(Element currentElement) throws CmsException {
        Vector masterMedia = new Vector();
        // get the mediafiles of the master
        NodeList mediaNodes = currentElement.getElementsByTagName(CmsExportModuledata.C_EXPORT_TAG_MASTER_MEDIA);
        // walk through all media
        for (int j = 0; j < mediaNodes.getLength(); j++) {
            // get the name of the file where the mediadata is stored
            String mediaFilename = ((Element)mediaNodes.item(j)).getFirstChild().getNodeValue();
            // try to get the information of the media
            if ((mediaFilename != null) && !("".equals(mediaFilename.trim()))) {
                CmsMasterMedia newMedia = getMediaData(mediaFilename);
                masterMedia.add(newMedia);
            }
        }
        return masterMedia;
    }

    /**
     * Gets the information for a single media from the media file.<p>
     * 
     * @param mediaFilename the name of the xml file that contains the media information
     * @return the media information from the media file
     * @throws CmsException in case something goes wrong
     */
    private CmsMasterMedia getMediaData(String mediaFilename) throws CmsException {
        String position, width, height, size, mimetype, type, title, name, description, contentfile;
        // get the new media object
        CmsMasterMedia newMedia = new CmsMasterMedia();
        // get the file with the data of the media
        Document mediaXml = this.getXmlFile(mediaFilename);
        Element media = mediaXml.getDocumentElement();
        position = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_POSITION);
        try {
            newMedia.setPosition(Integer.parseInt(position));
        } catch (Exception e) { }
        width = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_WIDTH);
        try {
            newMedia.setWidth(Integer.parseInt(width));
        } catch (Exception e) { }
        height = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_HEIGHT);
        try {
            newMedia.setHeight(Integer.parseInt(height));
        } catch (Exception e) { }
        size = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_SIZE);
        try {
            newMedia.setSize(Integer.parseInt(size));
        } catch (Exception e) { }
        mimetype = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_MIMETYPE);
        newMedia.setMimetype(mimetype);
        type = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_TYPE);
        try {
            newMedia.setType(Integer.parseInt(type));
        } catch (Exception e) { }
        title = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_TITLE);
        newMedia.setTitle(title);
        name = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_NAME);
        newMedia.setName(name);
        description = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_DESCRIPTION);
        newMedia.setDescription(description);
        // get the content of the media
        contentfile = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_CONTENT);
        byte[] mediacontent = null;
        try {
            mediacontent = getFileBytes(contentfile);
        } catch (Exception e) {
            m_report.println(e);
        }
        newMedia.setMedia(mediacontent);
        return newMedia;
    }

    /**
     * Returns a buffered reader for this resource using the importFile as root.<p>
     *
     * @param filename the name of the file to read
     * @return the file reader for this file
     * @throws Exception in case something goes wrong
     */
    private BufferedReader getFileReader(String filename) throws Exception {
        // is this a zip-file?
        if (m_importZip != null) {
            // yes
            ZipEntry entry = m_importZip.getEntry(filename);
            InputStream stream = m_importZip.getInputStream(entry);
            return new BufferedReader(new InputStreamReader(stream));
        } else {
            // no - use directory
            File xmlFile = new File(m_importResource, filename);
            return new BufferedReader(new FileReader(xmlFile));
        }
    }

    /**
     * Gets a xml file from the import resource.<p>
     * 
     * @param filename the name of the file to read
     * @return the xml document
     * @throws CmsException in case something goes wrong
     */
    private Document getXmlFile(String filename) throws CmsException {
        Document xmlDoc;
        try {
            BufferedReader xmlReader = getFileReader(filename);
            xmlDoc = A_CmsXmlContent.getXmlParser().parse(xmlReader);
            xmlReader.close();
        } catch (Exception exc) {

            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
        return xmlDoc;
    }

    /**
     * Coverts a String Date in long.<p>
     *
     * @param date String
     * @return long converted date
     */
    private long convertDate(String date) {
        java.text.SimpleDateFormat formatterFullTime = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        long adate = 0;
        try {
            adate = formatterFullTime.parse(date).getTime();
        } catch (ParseException e) { }
        return adate;
    }


    /**
     * Gets the content definition class method constructor.<p>
     * 
     * @param classname the name of the cd class
     * @param classes types needed for cd constructor
     * @param objects objects needed for cd constructor
     * @return content definition object
     */
    protected CmsMasterContent getContentDefinition(String classname, Class[] classes, Object[] objects) {
        CmsMasterContent cd = null;
        try {
            Class cdClass = Class.forName(classname);
            Constructor co = cdClass.getConstructor(classes);
            cd = (CmsMasterContent)co.newInstance(objects);
        } catch (InvocationTargetException ite) {
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INFO)) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsImportModuledata] " + classname + " contentDefinitionConstructor: Invocation target exception!");
            }
        } catch (NoSuchMethodException nsm) {
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INFO)) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsImportModuledata] " + classname + " contentDefinitionConstructor: Requested method was not found!");
            }
        } catch (InstantiationException ie) {
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INFO)) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsImportModuledata] " + classname + " contentDefinitionConstructor: the reflected class is abstract!");
            }
        } catch (Exception e) {
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INFO)) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsImportModuledata] " + classname + " contentDefinitionConstructor: Other exception! " + e);
            }
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INFO)) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, e.getMessage());
            }
        }
        return cd;
    }
}
