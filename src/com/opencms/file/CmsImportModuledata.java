/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsImportModuledata.java,v $
* Date   : $Date: 2002/02/18 09:48:25 $
* Version: $Revision: 1.2 $
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

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.lang.reflect.*;
import java.security.*;
import java.text.*;
import com.opencms.boot.*;
import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.template.*;
import com.opencms.defaults.master.*;
import org.w3c.dom.*;
import source.org.apache.java.util.*;

/**
 * This class holds the functionaility to import resources from the filesystem
 * into the cms.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.2 $ $Date: 2002/02/18 09:48:25 $
 */
public class CmsImportModuledata implements I_CmsConstants, Serializable {

    /**
     * The algorithm for the message digest
     */
    public static final String C_IMPORT_DIGEST="MD5";

    /**
     * The import-file to load resources from
     */
    private String m_importFile;

    /**
     * The import-resource (folder) to load resources from
     */
    private File m_importResource = null;

    /**
     * The version of this import, noted in the info tag of the manifest.xml.
     * 0 if the import file dosent have a version nummber (that is befor version
     * 4.3.23 of OpenCms).
     */
    private int m_importVersion = 0;

    /**
     * The import-resource (zip) to load resources from
     */
    private ZipFile m_importZip = null;

    /**
     * The import-path to write resources into the cms.
     */
    private String m_importPath;

    /**
     * The cms-object to do the operations.
     */
    private CmsObject m_cms;

    /**
     * The xml manifest-file.
     */
    private Document m_docXml;

    /**
     * This constructs a new CmsImportModuledata-object which imports the moduledata.
     *
     * @param importFile the file or folder to import from.
     * @param importPath the path to the cms to import into.
     * @exception CmsException the CmsException is thrown if something goes wrong.
     */
    public CmsImportModuledata(String importFile, String importPath, CmsObject cms)
        throws CmsException {

        m_importFile = importFile;
        m_importPath = importPath;
        m_cms = cms;

        // open the import resource
        getImportResource();

        // read the xml-config file
        getXmlConfigFile();

        // try to read the export version nummber
        try{
            m_importVersion = Integer.parseInt(
                getTextNodeValue((Element)m_docXml.getElementsByTagName(
                    C_EXPORT_TAG_INFO).item(0) , C_EXPORT_TAG_VERSION));
        }catch(Exception e){
            //ignore the exception, the export file has no version number (version 0).
        }
    }

    /**
     * Imports the moduledata and writes them to the cms even if there already exist conflicting files
     */
    public void importModuledata() throws CmsException {
        try{
            // first import the channels
            importChannels(null, null, null, null, null);
            // now import the moduledata
            importModuleMasters();
        } catch (CmsException e){
            throw e;
        } finally {
            if (m_importZip != null){
                try{
                    m_importZip.close();
                } catch (IOException exc) {
                    throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
                }
            }
        }
    }

    /**
     * Imports the resources and writes them to the cms.
     * param excludeList filenames of files and folders which should not be (over) written in the virtual file system
     * param writtenFilenames filenames of the files and folder which have actually been successfully written
     *       not used when null
     * param fileCodes code of the written files (for the registry)
     *       not used when null
     * param propertyName name of a property to be added to all resources
     * param propertyValue value of that property
     */
    public void importChannels(Vector excludeList, Vector writtenFilenames, Vector fileCodes, String propertyName, String propertyValue) throws CmsException {
        NodeList fileNodes, propertyNodes;
        Element currentElement, currentProperty;
        String destination, type, user, group, access;
        Hashtable properties;
        Vector types = new Vector(); // stores the file types for which the property already exists

        // first lock the resource to import
        try {
            // get all file-nodes
            fileNodes = m_docXml.getElementsByTagName(CmsExportModuledata.C_EXPORT_TAG_FILE);

            // walk through all files in manifest
            for (int i = 0; i < fileNodes.getLength(); i++) {
                currentElement = (Element) fileNodes.item(i);

                // get all information for a file-import
                destination = getTextNodeValue(currentElement, C_EXPORT_TAG_DESTINATION);
                type = getTextNodeValue(currentElement, C_EXPORT_TAG_TYPE);
                user = getTextNodeValue(currentElement, C_EXPORT_TAG_USER);
                group = getTextNodeValue(currentElement, C_EXPORT_TAG_GROUP);
                access = getTextNodeValue(currentElement, C_EXPORT_TAG_ACCESS);

                if (!inExcludeList(excludeList, m_importPath + destination)) {
                    // get all properties for this file
                    propertyNodes = currentElement.getElementsByTagName(C_EXPORT_TAG_PROPERTY);
                    // clear all stores for property information
                    properties = new Hashtable();
                    // add the module property to properties
                    if (propertyName != null && propertyValue != null && !"".equals(propertyName)) {
                        if (!types.contains(type)) {
                            types.addElement(type);
                            createPropertydefinition(propertyName, type);
                        }
                        properties.put(propertyName, propertyValue);
                    }
                    // walk through all properties
                    for (int j = 0; j < propertyNodes.getLength(); j++) {
                        currentProperty = (Element) propertyNodes.item(j);
                        // get all information for this property
                        String name = getTextNodeValue(currentProperty, C_EXPORT_TAG_NAME);
                        String value = getTextNodeValue(currentProperty, C_EXPORT_TAG_VALUE);
                        if(value == null) {
                            // create an empty property
                            value = "";
                        }
                        // store these informations
                        if ((name != null) && (value != null)) {
                            properties.put(name, value);
                            createPropertydefinition(name, type);
                        }
                    }

                    // import the specified file and write maybe put it on the lists writtenFilenames,fileCodes
                    importChannel(destination, type, user, group, access, properties, writtenFilenames, fileCodes);
                } else {
                    System.out.print("skipping " + destination);
                }
            }
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Gets the available modules in the current system
     * and imports the data for existing modules
     */
    public void importModuleMasters() throws CmsException{
        // get all available modules in this system
        Hashtable moduleExportables = new Hashtable();
        m_cms.getRegistry().getModuleExportables(moduleExportables);
        // now get the subIds of each module
        Hashtable availableModules = new Hashtable();
        Enumeration modElements = moduleExportables.elements();
        while(modElements.hasMoreElements()){
            String classname = (String)modElements.nextElement();
            // get the subId of the module
            try{
                int subId = getContentDefinition(classname, new Class[]{CmsObject.class}, new Object[]{m_cms}).getSubId();
                // put the subid and the classname into the hashtable of available modules
                availableModules.put(""+subId, classname);
            } catch (Exception e){
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

            // walk through all files in manifest
            for (int i = 0; i < masterNodes.getLength(); i++) {
                currentElement = (Element) masterNodes.item(i);
                // get the subid of the modulemaster
                subid = getTextNodeValue(currentElement, CmsExportModuledata.C_EXPORT_TAG_MASTER_SUBID);
                // check if there exists a module with this subid
                String classname = (String)availableModules.get(subid);
                if((classname != null) && !("".equals(classname.trim()))){
                    // import the dataset, the channelrelation and the media
                    importMaster(subid, classname, currentElement);
                }
            }
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * This method returns the resource-names that are needed to create a project for this import.
     * It calls the method getConflictingFileNames if needed, to calculate these resources.
     */
    public Vector getResourcesForProject() throws CmsException {
        NodeList fileNodes;
        Element currentElement, currentProperty;
        String destination, path;
        Vector resources = new Vector();
        try {
            // get all file-nodes
            fileNodes = m_docXml.getElementsByTagName(C_EXPORT_TAG_FILE);
            // walk through all files in manifest
            for (int i = 0; i < fileNodes.getLength(); i++) {
                currentElement = (Element) fileNodes.item(i);
                destination = getTextNodeValue(currentElement, C_EXPORT_TAG_DESTINATION);
                path = m_importPath + destination;

                // get the resources for a project
                try {
                    String resource = destination.substring(0, destination.indexOf("/",1) + 1);
                    resource = m_importPath + resource;
                    // add the resource, if it dosen't already exist
                    if((!resources.contains(resource)) && (!resource.equals(m_importPath))) {
                        try {
                            m_cms.setContextToCos();
                            m_cms.readFolder(resource);
                            m_cms.setContextToVfs();
                            // this resource exists in the current project -> add it
                            resources.addElement(resource);
                        } catch(CmsException exc) {
                            m_cms.setContextToVfs();
                            // this resource is missing - we need the root-folder
                            resources.addElement(C_ROOT);
                        }
                    }
                } catch(StringIndexOutOfBoundsException exc) {
                    // this is a resource in root-folder: ignore the excpetion
                }
            }
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
        if (m_importZip != null){
            try{
                m_importZip.close();
            } catch (IOException exc) {
                throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
            }
        }
        if(resources.contains(C_ROOT)) {
            // we have to import root - forget the rest!
            resources.removeAllElements();
            resources.addElement(C_ROOT);
        }
        return resources;
    }

    /**
     * Imports a channel into the cms.
     * @param destination the path to the destination-file in the cms
     * @param type the resource-type of the file
     * @param user the owner of the file
     * @param group the group of the file
     * @param access the access-flags of the file
     * @param properties a hashtable with properties for this resource
     * @param writtenFilenames filenames of the files and folder which have actually been successfully written
     *       not used when null
     * @param fileCodes code of the written files (for the registry)
     *       not used when null
     */
    private void importChannel(String destination, String type, String user, String group, String access, Hashtable properties, Vector writtenFilenames, Vector fileCodes) {
        // print out the information for shell-users
        System.out.print("Importing ");
        System.out.print(destination + " ");
        boolean success = true;
        String fullname = null;
        try {
            String path = m_importPath + destination.substring(0, destination.lastIndexOf("/") + 1);
            String name = destination.substring((destination.lastIndexOf("/") + 1), destination.length());
            m_cms.setContextToCos();
            // try to read an existing channel
            CmsResource res = null;
            try{
                res = m_cms.readFolder("/"+destination+"/");
            } catch (CmsException e) {
                // the channel does not exist, so import the channel
            }
            if(res == null){
                // get a new channelid
                int newChannelId = com.opencms.dbpool.CmsIdGenerator.nextId(this.C_TABLE_CHANNELID);
                properties.put(I_CmsConstants.C_PROPERTY_CHANNELID, newChannelId+"");
                res = m_cms.importResource("", destination, type, user, group, access,
                                            properties, "", null, m_importPath);
            }
            m_cms.setContextToVfs();
            if(res != null){
                fullname = res.getAbsolutePath();
            }
            System.out.println("OK");
        } catch (Exception exc) {
            // an error while importing the file
            success = false;
            System.out.println("Error: "+exc.toString());
        } finally {
            m_cms.setContextToVfs();
        }
        byte[] digestContent = {0};
        if (success && (fullname != null)){
            if (writtenFilenames != null){
                writtenFilenames.addElement(fullname);
            }
            if (fileCodes != null){
                fileCodes.addElement(new String(digestContent));
            }
        }
    }

    /**
     * Imports a single master
     * @param subId The subid of the module
     * @param classname The name of the module class
     * @param currentElement The current element of the xml-file
     */
    private void importMaster(String subId, String classname, Element currentElement) throws CmsException{
        CmsMasterDataSet newDataset = new CmsMasterDataSet();
        Vector channelRelations = new Vector();
        Vector masterMedia = new Vector();
        // try to get the dataset
        try{
            int subIdInt = Integer.parseInt(subId);
            newDataset = getMasterDataSet(subIdInt, currentElement);
        } catch (Exception e){
            throw new CmsException("Cannot get dataset ", e);
        }
        // try to get the channelrelations
        try{
            channelRelations = getMasterChannelRelation(currentElement);
        } catch (Exception e){
            throw new CmsException("Cannot get channelrelations ", e);
        }
        // try to get the media
        try{
            masterMedia = getMasterMedia(currentElement);
        } catch (Exception e){
            throw new CmsException("Cannot get media ", e);
        }
        // add the channels and media to the dataset
        newDataset.m_channelToAdd = channelRelations;
        newDataset.m_mediaToAdd = masterMedia;
        // create the new content definition
        CmsMasterContent newMaster = getContentDefinition(classname,
                                     new Class[] {CmsObject.class, CmsMasterDataSet.class},
                                     new Object[] {m_cms, newDataset});
        try{
            int userId = newMaster.getOwner();
            int groupId = newMaster.getGroupId();
            // first insert the new master
            newMaster.importMaster(m_cms);
            // now update the master because user and group might be changed
            newMaster.chown(m_cms, userId);
            newMaster.chgrp(m_cms,groupId);
        } catch (Exception e){
            throw new CmsException("Cannot write master ", e);
        }
    }

    /**
     * Gets the dataset for the master from the xml-file
     * @param subId The subid of the module
     * @param currentElement The current element of the xml-file
     * @return CmsMasterDataSet The dataset with the imported information
     */
    private CmsMasterDataSet getMasterDataSet(int subId, Element currentElement) throws CmsException{
        String datasetfile, username, groupname, accessFlags, publicationDate, purgeDate, flags,
                feedId, feedReference, feedFilename, title;
        // get the new dataset object
        CmsMasterDataSet newDataset = new CmsMasterDataSet();

        // get the file with the dataset of the master
        datasetfile = getTextNodeValue(currentElement, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATASET);
        Document datasetXml = this.getXmlFile(datasetfile);
        Element dataset = datasetXml.getDocumentElement();
        // get the information from the dataset and add it to the dataset
        // first add the subid
        newDataset.m_subId = subId;
        newDataset.m_masterId = C_UNKNOWN_ID;
        // get the id of the user or set the owner to the current user
        username = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_USER);
        int userId = m_cms.getRequestContext().currentUser().getId();
        try{
            if((username != null) && !("".equals(username.trim()))){
                userId = m_cms.readUser(username).getId();
            }
        } catch (Exception e){
        }
        newDataset.m_userId = userId;
        // get the id of the group or set the group to the current user
        groupname = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_GROUP);
        int groupId = m_cms.getRequestContext().currentGroup().getId();
        try{
            if((groupname != null) && !("".equals(groupname.trim()))){
                groupId = m_cms.readGroup(groupname).getId();
            }
        } catch (Exception e){
        }
        newDataset.m_groupId = groupId;
        // set the accessflags or the default flags
        accessFlags = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_ACCESSFLAGS);
        try{
            newDataset.m_accessFlags = Integer.parseInt(accessFlags);
        } catch (Exception e){
            newDataset.m_accessFlags = this.C_ACCESS_DEFAULT_FLAGS;
        }
        // set the publication date
        publicationDate = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_PUBLICATIONDATE);
        try{
            newDataset.m_publicationDate = convertDate(publicationDate);
        } catch (Exception e){
        }
        // set the purge date
        purgeDate = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_PURGEDATE);
        try{
            newDataset.m_purgeDate = convertDate(purgeDate);
        } catch (Exception e){
        }
        // set the flags
        flags = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_FLAGS);
        try{
            newDataset.m_flags = Integer.parseInt(flags);
        } catch (Exception e){
        }
        // set the feedid
        feedId = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_FEEDID);
        try{
            newDataset.m_feedId = Integer.parseInt(feedId);
        } catch (Exception e){
        }
        // set the feedreference
        feedReference = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_FEEDREFERENCE);
        try{
            newDataset.m_feedReference = Integer.parseInt(feedReference);
        } catch (Exception e){
        }
        // set the feedfilenam
        feedFilename = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_FEEDFILENAME);
        newDataset.m_feedFilename = feedFilename;
        // set the masters title
        title = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_TITLE);
        newDataset.m_title = title;
        // set the values of data_big
        for(int i=0; i< newDataset.m_dataBig.length; i++){
            String filename = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATABIG+i);
            String value = new String();
            if(filename != null && !"".equals(filename.trim())){
                // get the value from the file
                value =  new String(getFileBytes(filename));
            }
            newDataset.m_dataBig[i] = value;
        }
        // get the values of data_medium
        for(int i=0; i< newDataset.m_dataMedium.length; i++){
            String filename = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATAMEDIUM+i);
            String value = new String();
            if(filename != null && !"".equals(filename.trim())){
                // get the value from the file
                value =  new String(getFileBytes(filename));
            }
            newDataset.m_dataMedium[i] = value;
        }
        // get the values of data_small
        for(int i=0; i< newDataset.m_dataSmall.length; i++){
            String filename = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATASMALL+i);
            String value = new String();
            if(filename != null && !"".equals(filename.trim())){
                // get the value from the file
                value =  new String(getFileBytes(filename));
            }
            newDataset.m_dataSmall[i] = value;
        }
        // get the values of data_int
        for(int i=0; i< newDataset.m_dataInt.length; i++){
            String value = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATAINT+i);
            try{
                newDataset.m_dataInt[i] = new Integer(value).intValue();
            } catch (Exception e){
                newDataset.m_dataInt[i] = 0;
            }
        }
        // get the values of data_reference
        for(int i=0; i< newDataset.m_dataReference.length; i++){
            String value = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATAREFERENCE+i);
            try{
                newDataset.m_dataReference[i] = new Integer(value).intValue();
            } catch (Exception e){
                newDataset.m_dataReference[i] = 0;
            }
        }
        // get the values of data_date
        for(int i=0; i< newDataset.m_dataDate.length; i++){
            String value = getTextNodeValue(dataset, CmsExportModuledata.C_EXPORT_TAG_MASTER_DATADATE+i);
            try{
                newDataset.m_dataDate[i] = convertDate(value);
            } catch (Exception e){
                newDataset.m_dataDate[i] = 0;
            }
        }
        return newDataset;
    }

    /**
     * Gets the channelrelations for the master from the xml-file
     * @param currentElement The current element of the xml-file
     * @return Vector The vector contains the ids of all channels of the master
     */
    private Vector getMasterChannelRelation(Element currentElement){
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
     * Gets the media of the master from the xml-file
     * @param currentElement The current element of the xml-file
     * @return Vector The vector contains the media (CmsMasterMedia-Object) of the master
     */
    private Vector getMasterMedia(Element currentElement) throws CmsException{
        Vector masterMedia = new Vector();
        // get the mediafiles of the master
        NodeList mediaNodes = currentElement.getElementsByTagName(CmsExportModuledata.C_EXPORT_TAG_MASTER_MEDIA);
        // walk through all media
        for (int j = 0; j < mediaNodes.getLength(); j++) {
            // get the name of the file where the mediadata is stored
            String mediaFilename = ((Element) mediaNodes.item(j)).getFirstChild().getNodeValue();
            // try to get the information of the media
            if ((mediaFilename != null) && !("".equals(mediaFilename.trim()))) {
                CmsMasterMedia newMedia = getMediaData(mediaFilename);
                masterMedia.add(newMedia);
            }
        }
        return masterMedia;
    }

    /**
     * Gets the information for a single media from the media-file
     * @param mediaFilename The name of the xml-file that contains the media information
     * @return CmsMasterMedia The mediainformation from the media-file
     */
    private CmsMasterMedia getMediaData(String mediaFilename) throws CmsException{
        String position, width, height, size, mimetype, type, title, name, description, contentfile;
        // get the new media object
        CmsMasterMedia newMedia = new CmsMasterMedia();
        // get the file with the data of the media
        Document mediaXml = this.getXmlFile(mediaFilename);
        Element media = (Element)mediaXml.getDocumentElement();
        position = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_POSITION);
        try{
            newMedia.setPosition(Integer.parseInt(position));
        } catch (Exception e){
        }
        width = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_WIDTH);
        try{
            newMedia.setWidth(Integer.parseInt(width));
        } catch (Exception e){
        }
        height = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_HEIGHT);
        try{
            newMedia.setHeight(Integer.parseInt(height));
        } catch (Exception e){
        }
        size = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_SIZE);
        try{
            newMedia.setSize(Integer.parseInt(size));
        } catch (Exception e){
        }
        mimetype = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_MIMETYPE);
        newMedia.setMimetype(mimetype);
        type = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_TYPE);
        try{
            newMedia.setType(Integer.parseInt(type));
        } catch (Exception e){
        }
        title = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_TITLE);
        newMedia.setTitle(title);
        name = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_NAME);
        newMedia.setName(name);
        description = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_DESCRIPTION);
        newMedia.setDescription(description);
        // get the content of the media
        contentfile = getTextNodeValue(media, CmsExportModuledata.C_EXPORT_TAG_MEDIA_CONTENT);
        byte[] mediacontent = getFileBytes(contentfile);
        newMedia.setMedia(mediacontent);
        return newMedia;
    }

    /**
     * Checks whether the path is on the list of files which are excluded from the import
     *
     * @return boolean true if path is on the excludeList
     * @param excludeList list of pathnames which should not be (over) written
     * @param path a complete path of a resource
     */
    private boolean inExcludeList(Vector excludeList, String path) {
        boolean onList = false;
        if (excludeList == null) {
            return onList;
        }
        int i=0;
        while (!onList && i<excludeList.size()) {
            onList = (path.equals(excludeList.elementAt(i)));
            i++;
        }
        return onList;
    }

    /**
     * Creates missing property definitions if needed.
     *
     * @param name the name of the property.
     * @param propertyType the type of the property.
     * @param resourceType the type of the resource.
     *
     * @exception throws CmsException if something goes wrong.
     */
    private void createPropertydefinition(String name, String resourceType)
        throws CmsException {
        // does the propertydefinition exists already?
        try {
            m_cms.readPropertydefinition(name, resourceType);
        } catch(CmsException exc) {
            // no: create it
            m_cms.createPropertydefinition(name, resourceType);
        }
    }

    /**
     * Returns a byte-array containing the content of the file.
     *
     * @param filename The name of the file to read.
     * @return bytes[] The content of the file.
     */
    private byte[] getFileBytes(String filename) throws CmsException{
        try{
            // is this a zip-file?
            if(m_importZip != null) {
                // yes
                ZipEntry entry = m_importZip.getEntry(filename);
                InputStream stream = m_importZip.getInputStream(entry);

                int charsRead = 0;
                int size = new Long(entry.getSize()).intValue();
                byte[] buffer = new byte[size];
                while(charsRead < size) {
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
                while(charsRead < size) {
                    charsRead += fileStream.read(buffer, charsRead, size - charsRead);
                }
                fileStream.close();
                return buffer;
            }
        } catch (Exception e){
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION,e);
        }
    }

    /**
     * Returns a buffered reader for this resource using the importFile as root.
     *
     * @param filename The name of the file to read.
     * @return BufferedReader The filereader for this file.
     */
    private BufferedReader getFileReader(String filename)
        throws Exception{
        // is this a zip-file?
        if(m_importZip != null) {
            // yes
            ZipEntry entry = m_importZip.getEntry(filename);
            InputStream stream = m_importZip.getInputStream(entry);
            return new BufferedReader( new InputStreamReader(stream));
        } else {
            // no - use directory
            File xmlFile = new File(m_importResource, filename);
            return new BufferedReader(new FileReader(xmlFile));
        }
    }

    /**
     * Gets the import resource and stores it in object-member.
     */
    private void getImportResource()
        throws CmsException {
        try {
            // get the import resource
            m_importResource = new File(CmsBase.getAbsolutePath(m_importFile));

            // if it is a file it must be a zip-file
            if(m_importResource.isFile()) {
                m_importZip = new ZipFile(m_importResource);
            }
        } catch(Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Returns the text for this node.
     *
     * @param elem the parent-element.
     * @param tag the tagname to get the value from.
     * @return the value of the tag.
     */
    private String getTextNodeValue(Element elem, String tag) {
        try {
            return elem.getElementsByTagName(tag).item(0).getFirstChild().getNodeValue();
        } catch(Exception exc) {
            // ignore the exception and return null
            return null;
        }
    }

    /**
     * Gets the xml-config file from the import resource and stores it in object-member.
     * Checks whether the import is from a module file
     */
    private void getXmlConfigFile()
        throws CmsException {

        try {
            BufferedReader xmlReader = getFileReader(C_EXPORT_XMLFILENAME);
            m_docXml = A_CmsXmlContent.getXmlParser().parse(xmlReader);
            xmlReader.close();
         } catch(Exception exc) {

            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Gets a xml-file from the import resource
     * @param filename The name of the file to read
     * @return Document The xml-document
     */
    private Document getXmlFile(String filename)
        throws CmsException {
        Document xmlDoc;
        try {
            BufferedReader xmlReader = getFileReader(filename);
            xmlDoc = A_CmsXmlContent.getXmlParser().parse(xmlReader);
            xmlReader.close();
         } catch(Exception exc) {

            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
        return xmlDoc;
    }

    /**
	 * coverts String Date in long
	 *
	 * @param date String
	 * @return long
	 */
	private long convertDate(String date){
		java.text.SimpleDateFormat formatterFullTime = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        long adate=0;
        try{
            adate=formatterFullTime.parse(date).getTime();
        }catch(ParseException e){
        }
        return adate;
	}

    /**
     * Gets the content definition class method constructor
     * @returns content definition object
     */
    protected CmsMasterContent getContentDefinition(String classname, Class[] classes, Object[] objects) {
        CmsMasterContent cd = null;
        try {
            Class cdClass = Class.forName(classname);
            Constructor co = cdClass.getConstructor(classes);
            cd = (CmsMasterContent)co.newInstance(objects);
        } catch (InvocationTargetException ite) {
            if (com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO, "[CmsImportModuledata] "+classname + " contentDefinitionConstructor: Invocation target exception!");
            }
        } catch (NoSuchMethodException nsm) {
            if (com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO, "[CmsImportModuledata] "+classname + " contentDefinitionConstructor: Requested method was not found!");
            }
        } catch (InstantiationException ie) {
            if (com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO, "[CmsImportModuledata] "+classname + " contentDefinitionConstructor: the reflected class is abstract!");
            }
        } catch (Exception e) {
            if (com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO, "[CmsImportModuledata] "+classname + " contentDefinitionConstructor: Other exception! "+e);
            }
            if(com.opencms.core.I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO, e.getMessage() );
            }
        }
        return cd;
    }
}
