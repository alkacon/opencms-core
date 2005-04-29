/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/legacy/Attic/CmsImportModuledata.java,v $
* Date   : $Date: 2005/04/29 15:54:15 $
* Version: $Revision: 1.18 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  Alkacon Software (http://www.alkacon.com)
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

package com.opencms.legacy;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.importexport.CmsImport;
import org.opencms.importexport.CmsImportExportException;
import org.opencms.importexport.I_CmsImport;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlException;

import com.opencms.defaults.master.CmsMasterContent;
import com.opencms.defaults.master.CmsMasterDataSet;
import com.opencms.defaults.master.CmsMasterMedia;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Holds the functionaility to import resources from the filesystem
 * or a zip file into the OpenCms COS.
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com) 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * 
 * @version $Revision: 1.18 $ $Date: 2005/04/29 15:54:15 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsImportModuledata extends CmsImport implements Serializable {
    
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsImportModuledata.class);

    /**
     * Constructs a new import object which imports the module data from an OpenCms 
     * export zip file or a folder in the "real" file system.<p>
     *
     * @param cms the current cms object
     * @param importFile the file or folder to import from
     * @param importPath the path in the cms VFS to import into
     * @param report a report object to output the progress information to
     */
    public CmsImportModuledata(CmsObject cms, String importFile, String importPath, I_CmsReport report) {
        // set member variables
        m_cms = cms;
        m_importFile = importFile;
        m_importPath = importPath;
        m_report = report;
        m_importingChannelData = true;
        m_importImplementations = OpenCms.getImportExportManager().getImportVersionClasses();   
     }

    /**
     * Imports the moduledata and writes them to the cms even if there already exist 
     * conflicting files.<p>
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws CmsXmlException if the manifest of the import could not be unmarshalled
     */
    public synchronized void importResources() throws CmsImportExportException, CmsXmlException {
        // initialize the import
        openImportFile();
        m_report.println("Import Version "+m_importVersion, I_CmsReport.C_FORMAT_NOTE);
        try {
            // first import the channels
            m_report.println(m_report.key("report.import_channels_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            //importAllResources(null, null, null, null, null);
            // now find the correct import implementation    
            m_cms.getRequestContext().saveSiteRoot();
            m_cms.getRequestContext().setSiteRoot(I_CmsConstants.VFS_FOLDER_CHANNELS);     
            Iterator i = m_importImplementations.iterator();
                while (i.hasNext()) {
                    I_CmsImport imp = (I_CmsImport)i.next();
                    if (imp.getVersion() == m_importVersion) {
                        // this is the correct import version, so call it for the import process
                        imp.importResources(m_cms, m_importPath, m_report, 
                                        m_digest, m_importResource, m_importZip, m_docXml, null, null, null, null, null);
                        break;                    
                     }
                 }   
            m_cms.getRequestContext().restoreSiteRoot();
            m_report.println(m_report.key("report.import_channels_end"), I_CmsReport.C_FORMAT_HEADLINE);
 
            // now import the moduledata
            m_report.println(m_report.key("report.import_moduledata_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            importModuleMasters();
            m_report.println(m_report.key("report.import_moduledata_end"), I_CmsReport.C_FORMAT_HEADLINE);
        } catch (CmsXmlException e) { 
            
            throw e;
        } catch (CmsImportExportException e) {
            
            throw e;
        } catch (CmsException e) {
            m_report.println(e);
            
            CmsMessageContainer message = Messages.get().container(Messages.ERR_COS_IMPORTEXPORT_ERROR_IMPORTING_RESOURCES_0);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message, e);
            }
            
            throw new CmsImportExportException(message, e);            
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

        // get list of legacy modules that have a publish class
        Iterator it = CmsLegacyModuleAction.getLegacyModulePublishClasses().iterator();
       
        // now get the subIds of each module
        Hashtable availableModules = new Hashtable();
        while (it.hasNext()) {
            String classname = (String)it.next();
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
        List masterNodes;
        Element currentMasterElement;
        String subid;

        try {
            // get all master-nodes
            masterNodes = m_docXml.selectNodes("//" + CmsExportModuledata.C_EXPORT_TAG_MASTER);
            int length = masterNodes.size();

            // walk through all files in manifest
            for (int i = 0; i < length; i++) {
                currentMasterElement = (Element) masterNodes.get(i);
                // get the subid of the modulemaster
                subid = CmsImport.getChildElementTextValue(currentMasterElement, CmsExportModuledata.C_EXPORT_TAG_MASTER_SUBID);
                // check if there exists a module with this subid
                String classname = (String)availableModules.get(subid);
                if ((classname != null) && !("".equals(classname.trim()))) {
                    // import the dataset, the channelrelation and the media
                    m_report.print(" ( " + (i + 1) + " / " + length + " ) ", I_CmsReport.C_FORMAT_NOTE);
                    importMaster(subid, classname, currentMasterElement);
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
     * @param masterElement the current element of the xml file
     * @throws CmsException in case something goes wrong
     */
    protected void importMaster(String subId, String classname, Element masterElement) throws CmsException {
        CmsMasterDataSet newDataset = new CmsMasterDataSet();
        Vector channelRelations = new Vector();
        Vector masterMedia = new Vector();
        
        m_report.print(m_report.key("report.importing"), I_CmsReport.C_FORMAT_NOTE);

        // try to get the dataset
        try {
            int subIdInt = Integer.parseInt(subId);
            newDataset = getMasterDataSet(subIdInt, masterElement);
        } catch (Exception e) {
            m_report.println(e);
            throw new CmsException("Cannot get dataset ", e);
        }
        
        m_report.print("'" + CmsEncoder.escapeHtml(newDataset.m_title) + "' (" + classname + ")");
        m_report.print(m_report.key("report.dots"));

        // try to get the channelrelations
        try {
            channelRelations = getMasterChannelRelation(masterElement);
        } catch (Exception e) {
            m_report.println(e);
            throw new CmsException("Cannot get channelrelations ", e);
        }
        
        // try to get the media
        try {
            masterMedia = getMasterMedia(masterElement);
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
     * @param masterElement the current element of the xml file
     * @return the dataset with the imported information
     * @throws CmsException in case something goes wrong
     */
    protected CmsMasterDataSet getMasterDataSet(int subId, Element masterElement) throws CmsException {
        String datasetfile = null, username = null, groupname = null, accessFlags = null, 
            publicationDate = null, purgeDate = null, creationDate = null, flags = null, 
            feedId = null, feedReference = null, feedFilename = null, title = null,
            master_id = null;
            
        CmsMasterDataSet newDataset = new CmsMasterDataSet();

        // get the file with the dataset of the master
        datasetfile = ((Element) masterElement.selectNodes("./" + CmsExportModuledata.C_EXPORT_TAG_MASTER_DATASET).get(0)).getTextTrim();
       
        Document datasetXml = CmsImportVersion1.getXmlDocument(getFileInputStream(datasetfile));
        Element dataset = (Element) datasetXml.getRootElement().selectNodes("./" + CmsExportModuledata.C_EXPORT_TAG_MASTER_DATASET).get(0);
        
        // get the information from the dataset and add it to the dataset
        // first add the subid
        newDataset.m_subId = subId;
                
        master_id = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_ID);
        if (master_id != null) {
            newDataset.m_masterId = new CmsUUID(master_id);
        } else {
            newDataset.m_masterId = CmsUUID.getNullUUID();
        }
        
        // get the id of the user or set the owner to the current user
        username = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_USER);
        CmsUUID userId = null;
        try {
            if ((username != null) && !("".equals(username.trim()))) {
                userId = m_cms.readUser(username).getId();
            }
        } catch (Exception e) {
            // userId will be current user
            userId = m_cms.getRequestContext().currentUser().getId();
        }
        
        newDataset.m_userId = userId;
        // get the id of the group or set the group to the current user        
        groupname = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_GROUP);

        CmsUUID groupId = CmsUUID.getNullUUID();
        try {
            if ((groupname != null) && !("".equals(groupname.trim()))) {
                groupId = m_cms.readGroup(groupname).getId();
            }
        } catch (Exception e) {
            try {
                groupId = m_cms.readGroup(OpenCms.getDefaultUsers().getGroupUsers()).getId();
            } catch (Exception e2) {
                // ignore
            }
        }

        newDataset.m_groupId = groupId;
        // set the accessflags or the default flags
        accessFlags = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_ACCESSFLAGS);
        try {
            newDataset.m_accessFlags = Integer.parseInt(accessFlags);
        } catch (Exception e) {
            newDataset.m_accessFlags = I_CmsConstants.C_ACCESS_DEFAULT_FLAGS;
        }
        // set the publication date
        publicationDate = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_PUBLICATIONDATE);
        try {
            newDataset.m_publicationDate = convertDate(publicationDate);
        } catch (Exception e) {
            // ignore
        }
        // set the purge date
        purgeDate = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_PURGEDATE);
        try {
            newDataset.m_purgeDate = convertDate(purgeDate);
        } catch (Exception e) {
            // ignore
        }
        // set the creation date if possible
        try {
        creationDate = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_CREATEDATE);
            newDataset.m_dateCreated = convertDate(creationDate);
        } catch (Exception e) {
            // ignore
        }
        // set the flags
        flags = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_FLAGS);
        try {
            newDataset.m_flags = Integer.parseInt(flags);
        } catch (Exception e) {
            // ignore
        }
        // set the feedid
        feedId = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_FEEDID);
        try {
            newDataset.m_feedId = Integer.parseInt(feedId);
        } catch (Exception e) {
            // ignore
        }
        // set the feedreference
        feedReference = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_FEEDREFERENCE);
        try {
            newDataset.m_feedReference = Integer.parseInt(feedReference);
        } catch (Exception e) {
            // ignore
        }
        // set the feedfilenam
        feedFilename = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_FEEDFILENAME);
        newDataset.m_feedFilename = feedFilename;
        // set the masters title
        title = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_TITLE);
        newDataset.m_title = title;
        // set the values of data_big
        for (int i = 0; i < newDataset.m_dataBig.length; i++) {
            String filename = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATABIG + i);
            String value = new String();
            if (filename != null && !"".equals(filename.trim())) {
                // get the value from the file
                value = new String(getFileBytes(filename));
            }
            newDataset.m_dataBig[i] = value;
        }
        // get the values of data_medium
        for (int i = 0; i < newDataset.m_dataMedium.length; i++) {
            String filename = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATAMEDIUM + i);
            String value = new String();
            if (filename != null && !"".equals(filename.trim())) {
                // get the value from the file
                value = new String(getFileBytes(filename));
            }
            newDataset.m_dataMedium[i] = value;
        }
        // get the values of data_small
        for (int i = 0; i < newDataset.m_dataSmall.length; i++) {
            String filename = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATASMALL + i);
            String value = new String();
            if (filename != null && !"".equals(filename.trim())) {
                // get the value from the file
                value = new String(getFileBytes(filename));
            }
            newDataset.m_dataSmall[i] = value;
        }
        // get the values of data_int
        for (int i = 0; i < newDataset.m_dataInt.length; i++) {
            String value = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATAINT + i);
            try {
                newDataset.m_dataInt[i] = new Integer(value).intValue();
            } catch (Exception e) {
                newDataset.m_dataInt[i] = 0;
            }
        }
        // get the values of data_reference
        for (int i = 0; i < newDataset.m_dataReference.length; i++) {
            String value = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATAREFERENCE + i);
            try {
                newDataset.m_dataReference[i] = new Integer(value).intValue();
            } catch (Exception e) {
                newDataset.m_dataReference[i] = 0;
            }
        }
        // get the values of data_date
        for (int i = 0; i < newDataset.m_dataDate.length; i++) {
            String value = CmsImport.getChildElementTextValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATADATE + i);
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
     * @param masterElement the current element of the xml file
     * @return vector containing the ids of all channels of the master
     */
    protected Vector getMasterChannelRelation(Element masterElement) {
        Vector channelRelations = new Vector();
        // get the channelnames of the master
        List channelNodes = masterElement.selectNodes("*/" + CmsExportModuledata.C_EXPORT_TAG_MASTER_CHANNELNAME);

        // walk through all channelrelations
        for (int j = 0; j < channelNodes.size(); j++) {
            // get the name of the channel
            String channelName = ((Element) channelNodes.get(j)).getTextTrim();
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
     * @param masterElement The current element of the xml file
     * @return vector containing the media (CmsMasterMedia object) of the master
     * @throws CmsException in case something goes wrong
     */
    protected Vector getMasterMedia(Element masterElement) throws CmsException {
        Vector masterMedia = new Vector();
        // get the mediafiles of the master
        List mediaNodes = masterElement.selectNodes("*/" + CmsExportModuledata.C_EXPORT_TAG_MASTER_MEDIA);
        // walk through all media
        for (int j = 0; j < mediaNodes.size(); j++) {
            // get the name of the file where the mediadata is stored
            String mediaFilename = ((Element) mediaNodes.get(j)).getTextTrim();
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
        String position = null, width = null, height = null, size = null, mimetype = null, 
            type = null, title = null, name = null, description = null, contentfile = null;
        CmsMasterMedia newMedia = null;
        Document mediaXml = null;
        Element rootElement = null;
        byte[] mediacontent = null;
        
        newMedia = new CmsMasterMedia();
        mediaXml = CmsImportVersion1.getXmlDocument(getFileInputStream(mediaFilename));
        rootElement = mediaXml.getRootElement();
        
        position = ((Element)rootElement.selectNodes("./media/" + CmsExportModuledata.C_EXPORT_TAG_MEDIA_POSITION).get(0)).getTextTrim();        
        try {
            newMedia.setPosition(Integer.parseInt(position));
        } catch (Exception e) {
            // ignore
        }
        
        width = ((Element)rootElement.selectNodes("./media/" + CmsExportModuledata.C_EXPORT_TAG_MEDIA_WIDTH).get(0)).getTextTrim();
        try {
            newMedia.setWidth(Integer.parseInt(width));
        } catch (Exception e) {
            // ignore
        }
        
        height = ((Element)rootElement.selectNodes("./media/" + CmsExportModuledata.C_EXPORT_TAG_MEDIA_HEIGHT).get(0)).getTextTrim();
        try {
            newMedia.setHeight(Integer.parseInt(height));
        } catch (Exception e) {
            // ignore
        }
        
        size = ((Element)rootElement.selectNodes("./media/" + CmsExportModuledata.C_EXPORT_TAG_MEDIA_SIZE).get(0)).getTextTrim();
        try {
            newMedia.setSize(Integer.parseInt(size));
        } catch (Exception e) {
            // ignore
        }
        
        mimetype = ((Element)rootElement.selectNodes("./media/" + CmsExportModuledata.C_EXPORT_TAG_MEDIA_MIMETYPE).get(0)).getTextTrim();
        newMedia.setMimetype(mimetype);
        
        type = ((Element)rootElement.selectNodes("./media/" + CmsExportModuledata.C_EXPORT_TAG_MEDIA_TYPE).get(0)).getTextTrim();
        try {
            newMedia.setType(Integer.parseInt(type));
        } catch (Exception e) {
            // ignore
        }
        
        title = ((Element)rootElement.selectNodes("./media/" + CmsExportModuledata.C_EXPORT_TAG_MEDIA_TITLE).get(0)).getTextTrim();
        newMedia.setTitle(title);
        
        name = ((Element)rootElement.selectNodes("./media/" + CmsExportModuledata.C_EXPORT_TAG_MEDIA_NAME).get(0)).getTextTrim();
        newMedia.setName(name);
        
        description = ((Element)rootElement.selectNodes("./media/" + CmsExportModuledata.C_EXPORT_TAG_MEDIA_DESCRIPTION).get(0)).getTextTrim();
        newMedia.setDescription(description);
        
        contentfile = ((Element)rootElement.selectNodes("./media/" + CmsExportModuledata.C_EXPORT_TAG_MEDIA_CONTENT).get(0)).getTextTrim();
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
     * @throws CmsException in case something goes wrong
     */
    private InputStream getFileInputStream(String filename) throws CmsException {
        try {
            // is this a zip-file?
            if (m_importZip != null) {
                // yes
                ZipEntry entry = m_importZip.getEntry(filename);
                return m_importZip.getInputStream(entry);
            } else {
                // no - use directory
                File xmlFile = new File(m_importResource, filename);
                return new FileInputStream(xmlFile);
            }
        } catch (Exception e) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, e);
        }
    }

    /**
     * Coverts a String Date in long.<p>
     *
     * @param date String
     * @return long converted date
     */
    private long convertDate(String date) {
       
        java.text.SimpleDateFormat formatterFullTime = new SimpleDateFormat("M/d/yy h:mm a");
        long adate = 0;
        try {
            adate = formatterFullTime.parse(date).getTime();
        } catch (ParseException e) {
            java.text.SimpleDateFormat formatterFullTimeDe = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            try {
                adate = formatterFullTimeDe.parse(date).getTime();
            } catch (ParseException e2) {
            // ignore
            }
        }


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
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Invocation target exception", ite);
            }
        } catch (NoSuchMethodException nsm) {
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Requested method was not found", nsm);
            }
        } catch (InstantiationException ie) {
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("The reflected class is abstract", ie);
            }
        } catch (Exception e) {
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Other exception", e);
            }
        }
        return cd;
    }
}
