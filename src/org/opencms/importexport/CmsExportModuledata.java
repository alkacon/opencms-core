/*
* File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/Attic/CmsExportModuledata.java,v $
* Date   : $Date: 2003/09/16 12:06:10 $
* Version: $Revision: 1.7 $
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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.defaults.master.CmsMasterContent;
import com.opencms.defaults.master.CmsMasterDataSet;
import com.opencms.defaults.master.CmsMasterMedia;
import com.opencms.file.CmsObject;
import org.opencms.report.I_CmsReport;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.util.Utils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Holds the functionaility to export channels and modulemasters from the cms
 * to the filesystem.
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.7 $ $Date: 2003/09/16 12:06:10 $
 */
public class CmsExportModuledata extends CmsExport implements Serializable {

    // the tags for the manifest or dataset xml files
    public static String C_EXPORT_TAG_MASTER = "master";
    public static String C_EXPORT_TAG_MASTER_SUBID = "sub_id";
    public static String C_EXPORT_TAG_MASTER_DATASET = "dataset";
    public static String C_EXPORT_TAG_MASTER_CHANNELREL = "channelrelations";
    public static String C_EXPORT_TAG_MASTER_CHANNELNAME = "channelname";
    public static String C_EXPORT_TAG_MASTER_MEDIASET = "mediaset";
    public static String C_EXPORT_TAG_MASTER_MEDIA = "media";
    public static String C_EXPORT_TAG_MASTER_USER = "user_name";
    public static String C_EXPORT_TAG_MASTER_GROUP = "group_name";
    public static String C_EXPORT_TAG_MASTER_ACCESSFLAGS = "access_flags";
    public static String C_EXPORT_TAG_MASTER_PUBLICATIONDATE = "publication_date";
    public static String C_EXPORT_TAG_MASTER_PURGEDATE = "purge_date";
    public static String C_EXPORT_TAG_MASTER_FLAGS = "flags";
    public static String C_EXPORT_TAG_MASTER_FEEDID = "feed_id";
    public static String C_EXPORT_TAG_MASTER_FEEDREFERENCE = "feed_reference";
    public static String C_EXPORT_TAG_MASTER_FEEDFILENAME = "feed_filename";
    public static String C_EXPORT_TAG_MASTER_TITLE = "title";
    public static String C_EXPORT_TAG_MASTER_DATABIG = "data_big_";
    public static String C_EXPORT_TAG_MASTER_DATAMEDIUM = "data_medium_";
    public static String C_EXPORT_TAG_MASTER_DATASMALL = "data_small_";
    public static String C_EXPORT_TAG_MASTER_DATAINT = "data_int_";
    public static String C_EXPORT_TAG_MASTER_DATAREFERENCE = "data_reference_";
    public static String C_EXPORT_TAG_MASTER_DATADATE = "data_date_";
    public static String C_EXPORT_TAG_MEDIA_POSITION = "media_position";
    public static String C_EXPORT_TAG_MEDIA_WIDTH = "media_width";
    public static String C_EXPORT_TAG_MEDIA_HEIGHT = "media_height";
    public static String C_EXPORT_TAG_MEDIA_SIZE = "media_size";
    public static String C_EXPORT_TAG_MEDIA_MIMETYPE = "media_mimetype";
    public static String C_EXPORT_TAG_MEDIA_TYPE = "media_type";
    public static String C_EXPORT_TAG_MEDIA_TITLE = "media_title";
    public static String C_EXPORT_TAG_MEDIA_NAME = "media_name";
    public static String C_EXPORT_TAG_MEDIA_DESCRIPTION = "media_description";
    public static String C_EXPORT_TAG_MEDIA_CONTENT = "media_content";

    /** Holds information about contents that have already been exported */
    private Vector m_exportedMasters = new Vector();

    /**
     * This constructs a new CmsExportModuledata-object which exports the channels and modulemasters.
     *
     * @param cms the cms-object to work with
     * @param exportFile the filename of the zip to export to
     * @param resourcesToExport the cos folders (channels) to export
     * @param modulesToExport the modules to export
     * @param report to write the progress information to
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsExportModuledata(CmsObject cms, String exportFile, String[] resourcesToExport, String[] modulesToExport, I_CmsReport report) throws CmsException {
        m_cms = cms;
        m_exportFile = exportFile;
        m_cms = cms;
        m_report = report;

        // indicate that module date is exported to the export super class
        m_exportingModuleData = true;

        // open the export file
        openExportFile(null);

        // export the cos folders (ie. channels)               
        m_report.println(m_report.key("report.export_channels_begin"), I_CmsReport.C_FORMAT_HEADLINE);
        m_cms.getRequestContext().saveSiteRoot();

        try {
            m_cms.setContextToCos();
            // export all the resources
            exportAllResources(resourcesToExport);

        } catch (Exception e) {
            throw new CmsException("Error exporting COS channels", e);
        } finally {
            m_cms.getRequestContext().restoreSiteRoot();

        }
        m_report.println(m_report.key("report.export_channels_end"), I_CmsReport.C_FORMAT_HEADLINE);

        // get the modules to export
        Vector modules = new Vector();
        Vector moduleNames = new Vector();
        for (int i = 0; i < modulesToExport.length; i++) {
            String modName = modulesToExport[i];
            if (modName != null && !"".equals(modName)) {
                moduleNames.addElement(modulesToExport[i]);
            }
        }
        Hashtable moduleExportables = new Hashtable();
        m_cms.getRegistry().getModuleExportables(moduleExportables);
        // if there was no module selected then select all exportable modules,
        // else get only the modules from Hashtable that were selected
        if (moduleNames.size() == 0) {
            if (resourcesToExport.length > 0) {
                Enumeration modElements = moduleExportables.elements();
                while (modElements.hasMoreElements()) {
                    modules.add(modElements.nextElement());
                }
            }
        } else {
            modules = moduleNames;
        }

        // now do the export for all modules with the given channel ids
        Enumeration enumModules = modules.elements();
        while (enumModules.hasMoreElements()) {
            // get the name of the content definition class
            String classname = (String)enumModules.nextElement();
            this.exportData(classname, m_exportedChannelIds);
        }

        // close the export file
        closeExportFile();
    }

    /**
     * Exports the content definition data,
     * only content definition data from selected channels will be exported.<p>
     * 
     * @param classname name of the content definition class 
     * @param exportedChannelIds set of channels that have been exported
     * @throws CmsException if something goes wrong
     */
    private void exportData(String classname, Set exportedChannelIds) throws CmsException {
        // output something to the report for the data
        m_report.print(m_report.key("report.export_moduledata_begin"), I_CmsReport.C_FORMAT_HEADLINE);
        m_report.print("<i>" + classname + "</i>", I_CmsReport.C_FORMAT_HEADLINE);
        m_report.println(m_report.key("report.dots"), I_CmsReport.C_FORMAT_HEADLINE);

        Iterator keys = exportedChannelIds.iterator();
        // get the subId of the module
        int subId = getContentDefinition(classname, new Class[] {CmsObject.class }, new Object[] {m_cms }).getSubId();
        // the number for identifying each master
        int masterNr = 1;
        while (keys.hasNext()) {
            int channelId;
            String key = (String)keys.next();
            try {
                channelId = Integer.parseInt(key);
            } catch (NumberFormatException nfe) {
                m_report.println(nfe);
                continue;
            }
            try {
                Vector allDatasets = new Vector();
                // execute the static method readAllByChannel of the content definition class
                allDatasets = (Vector)Class.forName(classname).getMethod("readAllByChannel", new Class[] {CmsObject.class, Integer.class, Integer.class }).invoke(null, new Object[] {m_cms, new Integer(channelId), new Integer(subId)});

                for (int i = 0; i < allDatasets.size(); i++) {
                    CmsMasterDataSet curDataset = (CmsMasterDataSet)allDatasets.elementAt(i);
                    if (!m_exportedMasters.contains("" + curDataset.m_masterId)) {
                        writeExportManifestEntries(classname, curDataset, masterNr, subId);
                        m_exportedMasters.add("" + curDataset.m_masterId);
                        masterNr++;
                    }
                }
            } catch (InvocationTargetException ite) {
                m_report.println(ite);
                if (OpenCms.isLogging(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN)) {
                    OpenCms.log(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN, "[CmsExportModuledata] " + classname + ".readAllByChannel: Invocation target exception!");
                }
            } catch (NoSuchMethodException nsm) {
                m_report.println(nsm);
                if (OpenCms.isLogging(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN)) {
                    OpenCms.log(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN, "[CmsExportModuledata] " + classname + ".readAllByChannel: Requested method was not found!");
                }
            } catch (Exception e) {
                m_report.println(e);
                if (OpenCms.isLogging(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN)) {
                    OpenCms.log(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN, "[CmsExportModuledata] " + classname + ".readAllByChannel: Other exception! " + e);
                    OpenCms.log(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN, e.getMessage());
                }
            }
        }
        m_report.println(m_report.key("report.export_moduledata_end"), I_CmsReport.C_FORMAT_HEADLINE);
    }

    /**
     * Export a single content definition.<P>
     *
     * @param classname name of the content definition class
     * @param dataset data for the content definition object instance
     * @param masterNr id of master
     * @param subId id of content definition
     * 
     * @throws CmsException if something goes wrong
     */
    private void writeExportManifestEntries(String classname, CmsMasterDataSet dataset, int masterNr, int subId) throws CmsException {
        // output something to the report for the resource
        m_report.print(m_report.key("report.exporting"), I_CmsReport.C_FORMAT_NOTE);
        m_report.print("'" + dataset.m_title + "' (id: " + dataset.m_masterId + ")");

        // the name of the XML-file where the dataset is stored
        String dataSetFile = "dataset_" + subId + "_" + masterNr + ".xml";
        // create new mastercontent for getting channels and media
        CmsMasterContent content = getContentDefinition(classname, new Class[] {CmsObject.class, CmsMasterDataSet.class }, new Object[] {m_cms, dataset });
        // write these informations to the xml-manifest
        Element master = m_docXml.createElement(C_EXPORT_TAG_MASTER);
        m_mastersElement.appendChild(master);

        addElement(m_docXml, master, C_EXPORT_TAG_MASTER_SUBID, "" + subId);
        // add the name of the datasetfile and create the datasetfile
        // with the information from the dataset
        addElement(m_docXml, master, C_EXPORT_TAG_MASTER_DATASET, dataSetFile);
        writeExportDataset(dataset, dataSetFile, masterNr, subId);
        // add the channel relation of this master
        Element channelrel = m_docXml.createElement(C_EXPORT_TAG_MASTER_CHANNELREL);
        master.appendChild(channelrel);
        Vector moduleChannels = content.getChannels();
        for (int i = 0; i < moduleChannels.size(); i++) {
            String channelname = (String)moduleChannels.elementAt(i);
            addElement(m_docXml, channelrel, C_EXPORT_TAG_MASTER_CHANNELNAME, channelname);
        }
        // add the mediaset
        Element mediaset = m_docXml.createElement(C_EXPORT_TAG_MASTER_MEDIASET);
        master.appendChild(mediaset);
        Vector moduleMedia = content.getMedia();
        for (int i = 0; i < moduleMedia.size(); i++) {
            // for each media add the name of the xml-file for the mediadata to the manifest
            // and create the files for the media information
            String mediaFile = "media_" + subId + "_" + masterNr + "_" + i + ".xml";
            addElement(m_docXml, mediaset, C_EXPORT_TAG_MASTER_MEDIA, mediaFile);
            writeExportMediaset((CmsMasterMedia)moduleMedia.elementAt(i), mediaFile, masterNr, subId, i);
        }

        m_report.print(m_report.key("report.dots"), I_CmsReport.C_FORMAT_NOTE);
        m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
    }

    /**
     * Returns a master content definition object instance created with the reflection API.<p>
     * 
     * @param classname name of the content definition class
     * @param classes required for constructor generation 
     * @param objects instances to be used as parameters for class instance generation
     * 
     * @return a master content definition object instance created with the reflection API
     */
    private CmsMasterContent getContentDefinition(String classname, Class[] classes, Object[] objects) {
        CmsMasterContent cd = null;
        try {
            Class cdClass = Class.forName(classname);
            Constructor co = cdClass.getConstructor(classes);
            cd = (CmsMasterContent)co.newInstance(objects);
        } catch (InvocationTargetException ite) {
            m_report.println(ite);
            if (OpenCms.isLogging(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN)) {
                OpenCms.log(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN, "[CmsExportModuledata] " + classname + " contentDefinitionConstructor: Invocation target exception!");
            }
        } catch (NoSuchMethodException nsm) {
            m_report.println(nsm);
            if (OpenCms.isLogging(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN)) {
                OpenCms.log(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN, "[CmsExportModuledata] " + classname + " contentDefinitionConstructor: Requested method was not found!");
            }
        } catch (InstantiationException ie) {
            m_report.println(ie);
            if (OpenCms.isLogging(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN)) {
                OpenCms.log(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN, "[CmsExportModuledata] " + classname + " contentDefinitionConstructor: the reflected class is abstract!");
            }
        } catch (Exception e) {
            m_report.println(e);
            if (OpenCms.isLogging(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN)) {
                OpenCms.log(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN, "[CmsExportModuledata] " + classname + " contentDefinitionConstructor: Other exception! " + e);
            }
            if (OpenCms.isLogging(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN)) {
                OpenCms.log(CmsLog.C_OPENCMS_INFO, CmsLog.LEVEL_WARN, e.getMessage());
            }
        }
        return cd;
    }

    /**
     * Exports a content definition content in a "dataset_xxx.xml" file and a number of 
     * "datayyy_xxx.dat" files.<p>
     * 
     * @param dataset data for the content definition object instance
     * @param filename name of the zip file for the module data export
     * @param masterNr id of master
     * @param subId id of content definition
     * 
     * @throws CmsException if something goes wrong
     */
    private void writeExportDataset(CmsMasterDataSet dataset, String filename, int masterNr, int subId) throws CmsException {
        // creates the XML-document
        Document xmlDoc = null;
        try {
            xmlDoc = A_CmsXmlContent.getXmlParser().createEmptyDocument(I_CmsConstants.C_EXPORT_TAG_MODULEXPORT);
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
        // add the data element
        Element elementDataset = xmlDoc.createElement(C_EXPORT_TAG_MASTER_DATASET);
        xmlDoc.getDocumentElement().appendChild(elementDataset);
        // add the data of the contentdefinition
        // get the name of the owner
        String ownerName = "";
        try {
            ownerName = m_cms.readUser(dataset.m_userId).getName();
        } catch (CmsException e) { }
        // get the name of the group
        String groupName = "";
        try {
            groupName = m_cms.readGroup(dataset.m_groupId).getName();
        } catch (CmsException e) { }
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_USER, ownerName);
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_GROUP, groupName);
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_ACCESSFLAGS, "" + dataset.m_accessFlags);
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_PUBLICATIONDATE, Utils.getNiceDate(dataset.m_publicationDate));
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_PURGEDATE, Utils.getNiceDate(dataset.m_purgeDate));
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_FLAGS, "" + dataset.m_flags);
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_FEEDID, "" + dataset.m_feedId);
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_FEEDREFERENCE, "" + dataset.m_feedReference);
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_FEEDFILENAME, dataset.m_feedFilename);
        addCdataElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_TITLE, dataset.m_title);
        // get the values of data_big from the string array
        for (int i = 0; i < dataset.m_dataBig.length; i++) {
            String value = dataset.m_dataBig[i];
            String dataFile = new String();
            if (value != null && !"".equals(value)) {
                // the name of the file where the value of the field is stored
                dataFile = "databig_" + subId + "_" + masterNr + "_" + i + ".dat";
                writeExportContentFile(dataFile, value.getBytes());
            }
            addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_DATABIG + i, dataFile);
        }
        // get the values of data_medium from the string array
        for (int i = 0; i < dataset.m_dataMedium.length; i++) {
            String value = dataset.m_dataMedium[i];
            String dataFile = new String();
            if (value != null && !"".equals(value)) {
                // the name of the file where the value of the field is stored
                dataFile = "datamedium_" + subId + "_" + masterNr + "_" + i + ".dat";
                writeExportContentFile(dataFile, value.getBytes());
            }
            addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_DATAMEDIUM + i, dataFile);
        }
        // get the values of data_small from the string array
        for (int i = 0; i < dataset.m_dataSmall.length; i++) {
            String value = dataset.m_dataSmall[i];
            String dataFile = new String();
            if (value != null && !"".equals(value)) {
                // the name of the file where the value of the field is stored
                dataFile = "datasmall_" + subId + "_" + masterNr + "_" + i + ".dat";
                writeExportContentFile(dataFile, value.getBytes());
            }
            addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_DATASMALL + i, dataFile);
        }
        // get the values of data_int from the int array
        for (int i = 0; i < dataset.m_dataInt.length; i++) {
            String value = "" + dataset.m_dataInt[i];
            addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_DATAINT + i, value);
        }
        // get the values of data_reference from the int array
        for (int i = 0; i < dataset.m_dataReference.length; i++) {
            String value = "" + dataset.m_dataReference[i];
            addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_DATAREFERENCE + i, value);
        }
        // get the values of data_reference from the int array
        for (int i = 0; i < dataset.m_dataDate.length; i++) {
            String value = Utils.getNiceDate(dataset.m_dataDate[i]);
            addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_DATADATE + i, value);
        }

        try {
            ZipEntry entry = new ZipEntry(filename);
            m_exportZipStream.putNextEntry(entry);
            A_CmsXmlContent.getXmlParser().getXmlText(xmlDoc, m_exportZipStream, null);
            m_exportZipStream.closeEntry();
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Exports a media object, creates a "media_xxx.xml" and a "mediacontent_xxx.dat" data file.<p> 
     * 
     * @param media data for the media object instance
     * @param filename name of the xml file for the media data export
     * @param masterNr id of master
     * @param subId id of content definition
     * @param mediaId if od media object
     * 
     * @throws CmsException if something goes wrong
     */
    private void writeExportMediaset(CmsMasterMedia media, String filename, int masterNr, int subId, int mediaId) throws CmsException {
        // creates the XML-document
        Document xmlDoc = null;
        try {
            xmlDoc = A_CmsXmlContent.getXmlParser().createEmptyDocument(I_CmsConstants.C_EXPORT_TAG_MODULEXPORT);
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
        // add the data element
        Element elementMedia = xmlDoc.createElement("media");
        xmlDoc.getDocumentElement().appendChild(elementMedia);
        // add the data of the contentdefinition
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_POSITION, "" + media.getPosition());
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_WIDTH, "" + media.getWidth());
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_HEIGHT, "" + media.getHeight());
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_SIZE, "" + media.getSize());
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_MIMETYPE, media.getMimetype());
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_TYPE, "" + media.getType());
        addCdataElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_TITLE, media.getTitle());
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_NAME, media.getName());
        addCdataElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_DESCRIPTION, media.getDescription());
        // now add the name of the file where the media content is stored and write this file
        String contentFilename = "mediacontent_" + subId + "_" + masterNr + "_" + mediaId + ".dat";
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_CONTENT, contentFilename);
        writeExportContentFile(contentFilename, media.getMedia());
        // write the media xml-file
        try {
            ZipEntry entry = new ZipEntry(filename);
            m_exportZipStream.putNextEntry(entry);
            A_CmsXmlContent.getXmlParser().getXmlText(xmlDoc, m_exportZipStream, null);
            m_exportZipStream.closeEntry();
        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Writes a binary content to a "*.dat" file in the export zip.<p>
     * 
     * @param filename name of the file, usually ends with .dat
     * @param content contents to write to the file
     */
    private void writeExportContentFile(String filename, byte[] content) {
        try {
            // store the userinfo in zip-file
            ZipEntry entry = new ZipEntry(filename);
            m_exportZipStream.putNextEntry(entry);
            m_exportZipStream.write(content);
            m_exportZipStream.closeEntry();
        } catch (IOException ioex) {
            m_report.println(ioex);
            System.err.println("IOException: " + ioex.getMessage());
        }
    }
}