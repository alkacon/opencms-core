/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsExportModuledata.java,v $
* Date   : $Date: 2003/03/19 08:43:10 $
* Version: $Revision: 1.14 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package com.opencms.file;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.defaults.master.CmsMasterContent;
import com.opencms.defaults.master.CmsMasterDataSet;
import com.opencms.defaults.master.CmsMasterMedia;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.util.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
/**
 * This class holds the functionaility to export channels and modulemasters from the cms
 * to the filesystem.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.14 $ $Date: 2003/03/19 08:43:10 $
 */
public class CmsExportModuledata implements I_CmsConstants, Serializable{

    /**
     * The tags for the manifest
     */
    public static String C_EXPORT_TAG_CHANNELS = "channels";
    public static String C_EXPORT_TAG_MASTERS = "masters";
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

    /**
     * The export-zipfile to store resources to
     */
    private String m_exportFile;

    /**
     * The export-stream (zip) to store resources to
     */
    private ZipOutputStream m_exportZipStream = null;

    /**
     * The cms-object to do the operations.
     */
    private CmsObject m_cms;

    /**
     * The xml manifest-file.
     */
    private Document m_docXml;

    /**
     * The xml-element to store fileinformations to.
     */
    private Element m_filesElement;

    /**
     * The xml-element to store mastersinformations to.
     */
    private Element m_mastersElement;

    /**
     * Cache for previously added super folders
     */
    private Vector m_superChannels;

    /**
     * The channelid and the resourceobject of the exported channels
     */
    private Hashtable m_channelIds = new Hashtable();
    
    /**
     * 
     */
    private Vector m_exportedMasters = new Vector();

    /**
     * This constructs a new CmsExportModuledata-object which exports the channels and modulemasters.
     *
     * @param exportFile the zip-file to export to.
     * @param exportModules the modules to write into the exportFile
     * @param exportChannels the paths of channels to write into the exportFile
     * @param cms the cms-object to work with.
     * @throws CmsException the CmsException is thrown if something goes wrong.
     */
    public CmsExportModuledata(String exportFile, String[] exportChannels, String[] exportModules, CmsObject cms) throws CmsException {
        m_exportFile = exportFile;
        m_cms = cms;
        // open the import resource
        getExportResource();
        // create the xml-config file
        getXmlConfigFile();

        // get the modules to export
        Vector modules = new Vector();
        Vector moduleNames = new Vector();
        for (int i=0; i<exportModules.length; i++) {
            String modName = exportModules[i];
            if(modName != null && !"".equals(modName)){
                moduleNames.addElement(exportModules[i]);
            }
        }
        Hashtable moduleExportables = new Hashtable();
        m_cms.getRegistry().getModuleExportables(moduleExportables);
        // if there was no module selected then select all exportable modules,
        // else get only the modules from Hashtable that were selected
        if(moduleNames.size() == 0){
            if(exportChannels.length > 0){
                Enumeration modElements = moduleExportables.elements();
                while(modElements.hasMoreElements()){
                    modules.add(modElements.nextElement());
                }
            }
        } else {
             modules = moduleNames;
        }
        // get the channels for export
        Vector channelNames = new Vector();
        for (int i=0; i<exportChannels.length; i++) {
            channelNames.addElement(exportChannels[i]);
        }
        if(channelNames.size() != 0){
            // remove the possible redundancies in the list of paths
            checkRedundancies(channelNames);
        } else {
            // select the channels from the selected modules
            Enumeration enumModules = modules.elements();
            while(enumModules.hasMoreElements()){
                String classname = (String)enumModules.nextElement();
                CmsMasterContent cd = getContentDefinition(classname, new Class[]{CmsObject.class}, new Object[]{m_cms});
                String rootName = cd.getRootChannel();
                if(rootName != null && !"".equals(rootName)){
                    channelNames.add(rootName);
                }
            }
            // remove the possible redundancies in the list of paths
            checkRedundancies(channelNames);
        }
        // export the channels and return a vector with the channel-id
        exportAllChannels(channelNames);
        // now do the export for all modules with the given channel-ids
        Enumeration enumModules = modules.elements();
        while(enumModules.hasMoreElements()){
            // get the name of the content definition class
            String classname = (String)enumModules.nextElement();
            this.exportData(classname, m_channelIds);
        }
        // write the document to the zip-file
        writeXmlConfigFile( );

        try {
            m_exportZipStream.close();
        } catch(IOException exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /** Check whether some of the resources are redundant because a superfolder has also
     *  been selected and change the parameter Vectors
     *
     * @param channelNames contains the full pathnames of all channels
     */
    private void checkRedundancies(Vector channelNames) {
        int i, j;
        if (channelNames == null) {
            return;
        }
        Vector redundant = new Vector();
        int n = channelNames.size();
        if (n > 1) {
            // otherwise no check needed, because there is only one resource
            for (i = 0; i < n; i++) {
                redundant.addElement(new Boolean(false));
            }
            for (i = 0; i < n - 1; i++) {
                for (j = i + 1; j < n; j++) {
                    if (((String) channelNames.elementAt(i)).length() < ((String) channelNames.elementAt(j)).length()) {
                        if (((String) channelNames.elementAt(j)).startsWith((String) channelNames.elementAt(i))) {
                            redundant.setElementAt(new Boolean(true), j);
                        }
                    } else {
                        if (((String) channelNames.elementAt(i)).startsWith((String) channelNames.elementAt(j))) {
                            redundant.setElementAt(new Boolean(true), i);
                        }
                    }
                }
            }
            for (i = n - 1; i >= 0; i--) {
                if (((Boolean) redundant.elementAt(i)).booleanValue()) {
                    channelNames.removeElementAt(i);
                }
            }
        }
    }

    /**
     * Creates the xml-file and appends the initial tags to it.
     */
    private void getXmlConfigFile() throws CmsException {
        try {
            // creates the document
            m_docXml = A_CmsXmlContent.getXmlParser().createEmptyDocument(C_EXPORT_TAG_MODULEXPORT);

            // add the info element. it contains all infos for this export
            Element info = m_docXml.createElement(C_EXPORT_TAG_INFO);
            m_docXml.getDocumentElement().appendChild(info);
            addElement(m_docXml, info, C_EXPORT_TAG_CREATOR, m_cms.getRequestContext().currentUser().getName());
            addElement(m_docXml, info, C_EXPORT_TAG_OC_VERSION, A_OpenCms.getVersionName());
            addElement(m_docXml, info, C_EXPORT_TAG_DATE, Utils.getNiceDate(new Date().getTime()));
            addElement(m_docXml, info, C_EXPORT_TAG_PROJECT, m_cms.getRequestContext().currentProject().getName());
            addElement(m_docXml, info, C_EXPORT_TAG_VERSION, C_EXPORT_VERSION);
            // add the root element for the channels
            m_filesElement = m_docXml.createElement(C_EXPORT_TAG_CHANNELS);
            m_docXml.getDocumentElement().appendChild(m_filesElement);
            // add the root element for the masters
            m_mastersElement = m_docXml.createElement(C_EXPORT_TAG_MASTERS);
            m_docXml.getDocumentElement().appendChild(m_mastersElement);
        } catch(Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Gets the import resource and stores it in object-member.
     */
    private void getExportResource()
        throws CmsException {
        try {
            // add zip-extension, if needed
            if( !m_exportFile.toLowerCase().endsWith(".zip") ) {
                m_exportFile += ".zip";
            }

            // create the export-zipstream
            m_exportZipStream = new ZipOutputStream(new FileOutputStream(m_exportFile));

        } catch(Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Adds a element to the xml-document.
     * @param element The element to add the subelement to.
     * @param name The name of the new subelement.
     * @param value The value of the element.
     */
    private void addCdataElement(Document xmlDoc, Element element, String name, String value) {
        Element newElement = xmlDoc.createElement(name);
        element.appendChild(newElement);
        CDATASection text = xmlDoc.createCDATASection(value);
        newElement.appendChild(text);
    }
    /**
     * Adds a element to the xml-document.
     * @param element The element to add the subelement to.
     * @param name The name of the new subelement.
     * @param value The value of the element.
     */
    private void addElement(Document xmlDoc, Element element, String name, String value) {
        Element newElement = xmlDoc.createElement(name);
        element.appendChild(newElement);
        Text text = xmlDoc.createTextNode(value);
        newElement.appendChild(text);
    }

    /**
     * Exports the channels from a vector with the given channelnames
     * @param channelNames The vector with the channelNames
     * @param channelId A vector that contains the channelid of each channel
     */
    private void exportAllChannels(Vector channelNames) throws CmsException{
        for(int i = 0; i < channelNames.size(); i++){
            String curChannel = (String)channelNames.elementAt(i);
            // add the channelid to the vector of channel-ids
            m_cms.setContextToCos();
            try{
                String channelId = m_cms.readProperty(curChannel, I_CmsConstants.C_PROPERTY_CHANNELID);
                if(channelId != null){
                    if(!m_channelIds.contains(channelId)) {
                        // This channelid was not added previously. Add it now.
                        m_channelIds.put(channelId, curChannel);
                    }
                }
            } catch (CmsException e){
                throw e;
            } finally {
                m_cms.setContextToVfs();
            }
            // first add all superchannels
            addSuperChannels(curChannel);
            // get all subFolders
            // walk through all subfolders and export them
            exportChannel(curChannel);
        }
    }

    /**
     * Adds the superchannels of path to the config file,
     * starting at the top, excluding the root folder
     * @param path The path of the channel in the filesystem
     */
    private void addSuperChannels(String path) throws CmsException {
        // Initialize the "previously added folder cache"
        if(m_superChannels == null) {
            m_superChannels = new Vector();
        }
        Vector superChannels = new Vector();

        // Check, if the path is really a folder
        if(path.lastIndexOf(C_ROOT) != (path.length()-1)) {
            path = path.substring(0, path.lastIndexOf(C_ROOT)+1);
        }
        while (path.length() > C_ROOT.length()) {
            superChannels.addElement(path);
            path = path.substring(0, path.length() - 1);
            path = path.substring(0, path.lastIndexOf(C_ROOT)+1);
        }
        try{
            m_cms.setContextToCos();
            for (int i = superChannels.size()-1; i >= 0; i--) {
                String addChannel = (String)superChannels.elementAt(i);
                if(!m_superChannels.contains(addChannel)) {
                    // This super folder was NOT added previously. Add it now!
                    m_cms.setContextToCos();
                    CmsFolder channel = m_cms.readFolder(addChannel);
                    writeXmlEntrys(channel);
                    // Remember that this folder was added
                    m_superChannels.addElement(addChannel);
                }
            }
        } catch (CmsException e){
            throw e;
        } finally {
            m_cms.setContextToVfs();
        }
    }

    /**
     * Exports all subchannels of the channel
     * @param channelname The name of the channel to export
     */
     private void exportChannel(String channelname) throws CmsException{
        m_cms.setContextToCos();
        Vector subChannels = m_cms.getSubFolders(channelname);
        m_cms.setContextToVfs();
        for(int i = 0; i < subChannels.size(); i++) {
            CmsResource curChannel = (CmsResource) subChannels.elementAt(i);
            if(curChannel.getState() != C_STATE_DELETED){
                // add the channelid to the vector of channel-ids
                try{
                    m_cms.setContextToCos();
                    String channelId = m_cms.readProperty(curChannel.getAbsolutePath(), I_CmsConstants.C_PROPERTY_CHANNELID);
                    if(!m_channelIds.contains(channelId)) {
                        // This channelid was not added previously. Add it now.
                        m_channelIds.put(channelId, curChannel);
                    }
                } catch (CmsException e){
                    throw e;
                } finally {
                    m_cms.setContextToVfs();
                }
                // export this folder
                writeXmlEntrys(curChannel);
                // export all resources in this folder
                exportChannel(curChannel.getAbsolutePath());
            }
        }
     }

    /**
     * Writes the data for a resources (like acces-rights) to the manifest-xml-file.
     * @param resource The resource to get the data from.
     * @throws throws a CmsException if something goes wrong.
     */
    private void writeXmlEntrys(CmsResource resource) throws CmsException {
        System.out.print("Exporting channel: "+resource.getAbsolutePath());
        String source, type, user, group, access;

        // get all needed informations from the resource
        source = getSourceFilename(resource.getAbsolutePath());
        type = m_cms.getResourceType(resource.getType()).getResourceTypeName();
        user = m_cms.readOwner(resource).getName();
        group = m_cms.readGroup(resource).getName();
        access = resource.getAccessFlags() + "";

        // write these informations to the xml-manifest
        Element file = m_docXml.createElement(C_EXPORT_TAG_FILE);
        m_filesElement.appendChild(file);

        addElement(m_docXml, file, C_EXPORT_TAG_DESTINATION, source);
        addElement(m_docXml, file, C_EXPORT_TAG_TYPE, type);
        addElement(m_docXml, file, C_EXPORT_TAG_USER, user);
        addElement(m_docXml, file, C_EXPORT_TAG_GROUP, group);
        addElement(m_docXml, file, C_EXPORT_TAG_ACCESS, access);

        // append the node for properties
        Element properties = m_docXml.createElement(C_EXPORT_TAG_PROPERTIES);
        file.appendChild(properties);

        // read the properties
        Map fileProperties = new HashMap();
        try{
            m_cms.setContextToCos();
            fileProperties = m_cms.readProperties(resource.getAbsolutePath());
        } catch (CmsException e){
            throw e;
        } finally {
            m_cms.setContextToVfs();
        }
        Iterator i = fileProperties.keySet().iterator();

        // create xml-elements for the properties
        while(i.hasNext()) {
            // append the node for a property
            String key = (String) i.next();
            if(!key.equals(I_CmsConstants.C_PROPERTY_CHANNELID)){
                Element property = m_docXml.createElement(C_EXPORT_TAG_PROPERTY);
                properties.appendChild(property);

                String value = (String) fileProperties.get(key);
                String propertyType = m_cms.readPropertydefinition(key, type).getType() + "";

                addElement(m_docXml, property, C_EXPORT_TAG_NAME, key);
                addElement(m_docXml, property, C_EXPORT_TAG_TYPE, propertyType);
                addCdataElement(m_docXml, property, C_EXPORT_TAG_VALUE, value);
            }
        }
        System.out.println("...OK");
    }

    /**
     * Writes the xml-config file (manifest) to the zip-file.
     */
    private void writeXmlConfigFile()
        throws CmsException {
        try {
            ZipEntry entry = new ZipEntry(C_EXPORT_XMLFILENAME);
            m_exportZipStream.putNextEntry(entry);
            A_CmsXmlContent.getXmlParser().getXmlText(m_docXml, m_exportZipStream, null);
            m_exportZipStream.closeEntry();
        } catch(Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Gets the content definition class method constructor
     * @return content definition object
     */
    protected CmsMasterContent getContentDefinition(String classname, Class[] classes, Object[] objects) {
        CmsMasterContent cd = null;
        try {
            Class cdClass = Class.forName(classname);
            Constructor co = cdClass.getConstructor(classes);
            cd = (CmsMasterContent)co.newInstance(objects);
        } catch (InvocationTargetException ite) {
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO, "[CmsExportModuledata] "+classname + " contentDefinitionConstructor: Invocation target exception!");
            }
        } catch (NoSuchMethodException nsm) {
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO, "[CmsExportModuledata] "+classname + " contentDefinitionConstructor: Requested method was not found!");
            }
        } catch (InstantiationException ie) {
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO, "[CmsExportModuledata] "+classname + " contentDefinitionConstructor: the reflected class is abstract!");
            }
        } catch (Exception e) {
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO, "[CmsExportModuledata] "+classname + " contentDefinitionConstructor: Other exception! "+e);
            }
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO, e.getMessage() );
            }
        }
        return cd;
    }

    /**
     * Substrings the source-filename, so it is shrinked to the needed part for
     * import/export.
     * @param absoluteName The absolute path of the resource.
     * @return The shrinked path.
     */
    private String getSourceFilename(String absoluteName) {
        // String path = absoluteName.substring(m_exportPath.length());
        String path = absoluteName; // keep absolute name to distinguish resources
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if(path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * Export the content definitions in the specified channels.
     * @param cms The CmsObject
     * @param zipfile The zip file to store the data
     * @param manifest The document where the maindata of the master are stored
     * @param channelId The Vector that includes the exported channels
     */
    private void exportData(String classname, Hashtable exportedChannels) throws CmsException {
        // get the modulemaster for each exported channel
        System.out.println("Export Module "+classname);
        Enumeration keys = exportedChannels.keys();
        // get the subId of the module
        int subId = getContentDefinition(classname, new Class[]{CmsObject.class}, new Object[]{m_cms}).getSubId();
        // the number for identifying each master
        int masterNr = 1;
        while(keys.hasMoreElements()){
            int channelId = Integer.parseInt((String) keys.nextElement());
            try{
                Vector allDatasets = new Vector();
                // execute the static method readAllByChannel of the content definition class
                allDatasets = (Vector)Class.forName(classname).getMethod("readAllByChannel",
                                  new Class[] {CmsObject.class, Integer.class, Integer.class}).invoke(null,
                                  new Object[] {m_cms, new Integer(channelId), new Integer(subId)});

                for(int i=0; i<allDatasets.size(); i++){
                    CmsMasterDataSet curDataset = (CmsMasterDataSet)allDatasets.elementAt(i);
                    if(!m_exportedMasters.contains(""+curDataset.m_masterId)){
                        writeExportManifestEntries(classname, curDataset, masterNr, subId);
                        m_exportedMasters.add(""+curDataset.m_masterId);
                        masterNr++;
                    }
                }
            } catch (InvocationTargetException ite) {
                if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO, "[CmsExportModuledata] "+classname + ".readAllByChannel: Invocation target exception!");
                }
            } catch (NoSuchMethodException nsm) {
                if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO, "[CmsExportModuledata] "+classname + ".readAllByChannel: Requested method was not found!");
                }
            } catch (Exception e) {
                if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO, "[CmsExportModuledata] "+classname + ".readAllByChannel: Other exception! "+e);
                    A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO, e.getMessage() );
                }
            }
        }
    }

    /**
     * Write the XML-entries and Documents of a contentdefinition
     */
    private void writeExportManifestEntries(String classname, CmsMasterDataSet dataset, int masterNr, int subId)
        throws CmsException{
        // the name of the XML-file where the dataset is stored
        String dataSetFile = "dataset_"+subId+"_"+masterNr+".xml";
        // create new mastercontent for getting channels and media
        CmsMasterContent content = getContentDefinition(classname, new Class[]{CmsObject.class, CmsMasterDataSet.class}, new Object[]{m_cms, dataset});
        // write these informations to the xml-manifest
        Element master = m_docXml.createElement(C_EXPORT_TAG_MASTER);
        m_mastersElement.appendChild(master);

        addElement(m_docXml, master, C_EXPORT_TAG_MASTER_SUBID, ""+subId);
        // add the name of the datasetfile and create the datasetfile
        // with the information from the dataset
        addElement(m_docXml, master, C_EXPORT_TAG_MASTER_DATASET, dataSetFile);
        writeExportDataset(dataset, dataSetFile, masterNr, subId);
        // add the channel relation of this master
        Element channelrel = m_docXml.createElement(C_EXPORT_TAG_MASTER_CHANNELREL);
        master.appendChild(channelrel);
        Vector moduleChannels = content.getChannels();
        for(int i=0; i<moduleChannels.size(); i++){
            String channelname = (String)moduleChannels.elementAt(i);
            addElement(m_docXml, channelrel, C_EXPORT_TAG_MASTER_CHANNELNAME, channelname);
        }
        // add the mediaset
        Element mediaset = m_docXml.createElement(C_EXPORT_TAG_MASTER_MEDIASET);
        master.appendChild(mediaset);
        Vector moduleMedia = content.getMedia();
        for(int i=0; i<moduleMedia.size(); i++){
            // for each media add the name of the xml-file for the mediadata to the manifest
            // and create the files for the media information
            String mediaFile = "media_"+subId+"_"+masterNr+"_"+i+".xml";
            addElement(m_docXml, mediaset, C_EXPORT_TAG_MASTER_MEDIA, mediaFile);
            writeExportMediaset((CmsMasterMedia)moduleMedia.elementAt(i), mediaFile, masterNr, subId, i);
        }
    }

    /**
     * Write the XML-entries and Documents of the dataset of a contentdefinition
     */
    private void writeExportDataset(CmsMasterDataSet dataset, String filename, int masterNr, int subId)
        throws CmsException{
        // creates the XML-document
        Document xmlDoc = null;
        try{
            xmlDoc = A_CmsXmlContent.getXmlParser().createEmptyDocument(C_EXPORT_TAG_MODULEXPORT);
        } catch(Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
        // add the data element
        Element elementDataset = xmlDoc.createElement(C_EXPORT_TAG_MASTER_DATASET);
        xmlDoc.getDocumentElement().appendChild(elementDataset);
        // add the data of the contentdefinition
        // get the name of the owner
        String ownerName = "";
        try{
            ownerName = m_cms.readUser(dataset.m_userId).getName();
        } catch (CmsException e){
        }
        // get the name of the group
        String groupName = "";
        try{
            groupName = m_cms.readGroup(dataset.m_groupId).getName();
        } catch (CmsException e){
        }
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_USER, ownerName);
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_GROUP, groupName);
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_ACCESSFLAGS, ""+dataset.m_accessFlags);
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_PUBLICATIONDATE, Utils.getNiceDate(dataset.m_publicationDate));
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_PURGEDATE, Utils.getNiceDate(dataset.m_purgeDate));
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_FLAGS, ""+dataset.m_flags);
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_FEEDID, ""+dataset.m_feedId);
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_FEEDREFERENCE, ""+dataset.m_feedReference);
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_FEEDFILENAME, dataset.m_feedFilename);
        addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_TITLE, dataset.m_title);
        // get the values of data_big from the string array
        for(int i=0; i< dataset.m_dataBig.length; i++){
            String value = dataset.m_dataBig[i];
            String dataFile = new String();
            if(value != null && !"".equals(value)){
                // the name of the file where the value of the field is stored
                dataFile = "databig_"+subId+"_"+masterNr+"_"+i+".dat";
                writeExportContentFile(dataFile, value.getBytes());
            }
            addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_DATABIG+i, dataFile);
        }
        // get the values of data_medium from the string array
        for(int i=0; i< dataset.m_dataMedium.length; i++){
            String value = dataset.m_dataMedium[i];
            String dataFile = new String();
            if(value != null && !"".equals(value)){
                // the name of the file where the value of the field is stored
                dataFile = "datamedium_"+subId+"_"+masterNr+"_"+i+".dat";
                writeExportContentFile(dataFile, value.getBytes());
            }
            addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_DATAMEDIUM+i, dataFile);
        }
        // get the values of data_small from the string array
        for(int i=0; i< dataset.m_dataSmall.length; i++){
            String value = dataset.m_dataSmall[i];
            String dataFile = new String();
            if(value != null && !"".equals(value)){
                // the name of the file where the value of the field is stored
                dataFile = "datasmall_"+subId+"_"+masterNr+"_"+i+".dat";
                writeExportContentFile(dataFile, value.getBytes());
            }
            addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_DATASMALL+i, dataFile);
        }
        // get the values of data_int from the int array
        for(int i=0; i< dataset.m_dataInt.length; i++){
            String value = ""+dataset.m_dataInt[i];
            addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_DATAINT+i, value);
        }
        // get the values of data_reference from the int array
        for(int i=0; i< dataset.m_dataReference.length; i++){
            String value = ""+dataset.m_dataReference[i];
            addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_DATAREFERENCE+i, value);
        }
        // get the values of data_reference from the int array
        for(int i=0; i< dataset.m_dataDate.length; i++){
            String value = Utils.getNiceDate(dataset.m_dataDate[i]);
            addElement(xmlDoc, elementDataset, C_EXPORT_TAG_MASTER_DATADATE+i, value);
        }

        try {
            ZipEntry entry = new ZipEntry(filename);
            m_exportZipStream.putNextEntry(entry);
            A_CmsXmlContent.getXmlParser().getXmlText(xmlDoc, m_exportZipStream, null);
            m_exportZipStream.closeEntry();
        } catch(Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Write the XML-entries and Documents of the dataset of a contentdefinition
     */
    private void writeExportMediaset(CmsMasterMedia media, String filename, int masterNr, int subId, int mediaId)
        throws CmsException{
        // creates the XML-document
        Document xmlDoc = null;
        try{
            xmlDoc = A_CmsXmlContent.getXmlParser().createEmptyDocument(C_EXPORT_TAG_MODULEXPORT);
        } catch(Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
        // add the data element
        Element elementMedia = xmlDoc.createElement("media");
        xmlDoc.getDocumentElement().appendChild(elementMedia);
        // add the data of the contentdefinition
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_POSITION, ""+media.getPosition());
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_WIDTH, ""+media.getWidth());
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_HEIGHT, ""+media.getHeight());
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_SIZE, ""+media.getSize());
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_MIMETYPE, media.getMimetype());
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_TYPE, ""+media.getType());
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_TITLE, media.getTitle());
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_NAME, media.getName());
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_DESCRIPTION, media.getDescription());
        // now add the name of the file where the media content is stored and write this file
        String contentFilename = "mediacontent_"+subId+"_"+masterNr+"_"+mediaId+".dat";
        addElement(xmlDoc, elementMedia, C_EXPORT_TAG_MEDIA_CONTENT, contentFilename);
        writeExportContentFile(contentFilename, media.getMedia());
        // write the media xml-file
        try {
            ZipEntry entry = new ZipEntry(filename);
            m_exportZipStream.putNextEntry(entry);
            A_CmsXmlContent.getXmlParser().getXmlText(xmlDoc, m_exportZipStream, null);
            m_exportZipStream.closeEntry();
        } catch(Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Write the XML-entries and Documents of the media of a contentdefinition
     */
    private void writeExportContentFile(String filename, byte[] content){
        try{
            // store the userinfo in zip-file
            ZipEntry entry = new ZipEntry(filename);
            m_exportZipStream.putNextEntry(entry);
            m_exportZipStream.write(content);
            m_exportZipStream.closeEntry();
        } catch (IOException ioex){
            System.err.println("IOException: "+ioex.getMessage());
        }
    }
}