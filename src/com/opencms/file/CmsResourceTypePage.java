/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceTypePage.java,v $
 * Date   : $Date: 2003/07/18 18:20:37 $
 * Version: $Revision: 1.81 $
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
 
package com.opencms.file;

import org.opencms.loader.CmsXmlTemplateLoader;
import org.opencms.lock.CmsLock;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.flex.util.CmsUUID;
import com.opencms.launcher.I_CmsLauncher;
import com.opencms.linkmanagement.CmsPageLinks;
import com.opencms.template.CmsXmlControlFile;
import com.opencms.workplace.I_CmsWpConstants;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Describes the resource type "page".<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.81 $
 * @since 5.1
 */
public class CmsResourceTypePage implements I_CmsResourceType {

    /** The type id of this resource */
    public static final int C_RESOURCE_TYPE_ID = 1;
    
    /** The name of this resource */
    public static final String C_RESOURCE_TYPE_NAME = "page";

    /**
     * @see com.opencms.file.I_CmsResourceType#getResourceType()
     */
    public int getResourceType() {
        return C_RESOURCE_TYPE_ID;
    }

    /**
     * @see com.opencms.file.A_CmsResourceType#getResourceTypeName()
     */
    public String getResourceTypeName() {
        return C_RESOURCE_TYPE_NAME;
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#getLauncherClass()
     */
    public String getLauncherClass() {
        return CmsXmlTemplateLoader.class.getName();
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#getLauncherType()
     */
    public int getLauncherType() {
        return I_CmsLauncher.C_TYPE_XML;
    }     

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer output = new StringBuffer();
        output.append("[ResourceType]:");
        output.append(getResourceTypeName());
        output.append(" , Id=");
        output.append(getResourceType());
        output.append(" , launcherType=");
        output.append(getLauncherType());
        output.append(" , launcherClass=");
        output.append(getLauncherClass());
        return output.toString();
    }
    
    /**
     * Returns the default body start string for a new XML template.<p>
     * 
     * @return the default body start string for a new XML template 
     */
    public static String getDefaultBodyStart() {
        return "<?xml version=\"1.0\" encoding=\"" + A_OpenCms.getDefaultEncoding() + "\"?>\n<XMLTEMPLATE>\n<TEMPLATE>\n<![CDATA[\n";
    }

    /**
     * Returns the default body end string for a new XML template.<p>
     * 
     * @return the default body end string for a new XML template 
     */
    public static String getDefaultBodyEnd() {
        return "]]></TEMPLATE>\n</XMLTEMPLATE>";
    }
    
    /**
     * @see com.opencms.file.I_CmsResourceType#touch(com.opencms.file.CmsObject, java.lang.String, long, boolean)
     */
    public void touch(CmsObject cms, String resourcename, long timestamp, boolean touchRecursive) throws CmsException {
        // create a valid resource
        CmsFile file = cms.readFile(resourcename);

        // touch the page itself
        cms.doTouch(resourcename, timestamp);

        // touch its counterpart under content/bodies
        String bodyPath = this.checkBodyPath(cms, (CmsFile)file);
        if (bodyPath != null) {
            cms.doTouch(bodyPath, timestamp);
        }
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#chtype(com.opencms.file.CmsObject, java.lang.String, int)
     */
    public void chtype(CmsObject cms, String resourcename, int newType) throws CmsException {
        CmsFile file = cms.readFile(resourcename);
        // check if the current user has the right to change the group of the
        // resource. Only the owner of a file and the admin are allowed to do this.
        if ((cms.getRequestContext().currentUser().equals(cms.readOwner(file))) || (cms.userInGroup(cms.getRequestContext().currentUser().getName(), I_CmsConstants.C_GROUP_ADMIN))) {
            cms.doChtype(resourcename, newType);
            //check if the file type name is page
            String bodyPath = checkBodyPath(cms, (CmsFile)file);
            if (bodyPath != null) {
                cms.doChtype(bodyPath, newType);
            }
        }
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#createResource(com.opencms.file.CmsObject, java.lang.String, java.util.Map, byte[], java.lang.Object)
     */
    public CmsResource createResource(CmsObject cms, String resourcename, Map properties, byte[] contents, Object parameter) throws CmsException {
        String masterTemplate = (String)properties.get(I_CmsConstants.C_XML_CONTROL_TEMPLATE_PROPERTY);        
        if (masterTemplate == null) {
            masterTemplate = "";        
            // Scan for mastertemplates
            List allMasterTemplates = cms.getFilesInFolder(I_CmsWpConstants.C_VFS_PATH_DEFAULT_TEMPLATES);
    
            // Select the first mastertemplate as default
            if (allMasterTemplates.size() > 0) {
                masterTemplate = cms.readAbsolutePath((CmsFile)allMasterTemplates.get(0));
            }
        }
        CmsFile file = null;

        String folderName = resourcename.substring(0, resourcename.lastIndexOf(I_CmsConstants.C_FOLDER_SEPARATOR, resourcename.length()) + 1);
        String pageName = resourcename.substring(folderName.length(), resourcename.length());

        // Evaluate the absolute path to the new body file
        String bodyFolder = (I_CmsWpConstants.C_VFS_PATH_BODIES.substring(0, I_CmsWpConstants.C_VFS_PATH_BODIES.lastIndexOf("/"))) + folderName;

        // Create the new page file
        file = cms.doCreateFile(resourcename, "".getBytes(), getResourceTypeName(), properties);
        cms.doLockResource(resourcename, true);
        CmsXmlControlFile pageXml = new CmsXmlControlFile(cms, file);
        pageXml.setTemplateClass(I_CmsConstants.C_XML_CONTROL_DEFAULT_CLASS);
        pageXml.setMasterTemplate(masterTemplate);
        pageXml.setElementClass("body", I_CmsConstants.C_XML_CONTROL_DEFAULT_CLASS);
        pageXml.setElementTemplate("body", bodyFolder + pageName);
        pageXml.write();

        // Check, if the body path exists and create missing folders, if neccessary
        checkFolders(cms, folderName);

        // Create the new body file
        cms.doCreateFile(bodyFolder + pageName, (getDefaultBodyStart() + new String(contents) + getDefaultBodyEnd()).getBytes(), CmsResourceTypePlain.C_RESOURCE_TYPE_NAME, new Hashtable());
        cms.doLockResource(bodyFolder + pageName, true);        

        // linkmanagement: create the links of the new page (for the case that the content was not empty
        if (contents.length > 1) {
            CmsPageLinks linkObject = cms.getPageLinks(resourcename);
            cms.createLinkEntrys(linkObject.getResourceId(), linkObject.getLinkTargets());
        }
        return file;
    }
    
    /**
     * Creates a resource for the specified template.<p>
     * 
     * @param cms the cms context
     * @param resourcename the name of the resource to create
     * @param properties properties for the new resource
     * @param contents content for the new resource
     * @param masterTemplate template for the new resource
     * @return the created resource 
     * @throws CmsException if something goes wrong
     */    
    public CmsResource createResourceForTemplate(CmsObject cms, String resourcename, Hashtable properties, byte[] contents, String masterTemplate) throws CmsException {        
        CmsFile resource = (CmsFile)this.createResource(cms, resourcename, properties, contents, null);                
        CmsXmlControlFile pageXml = new CmsXmlControlFile(cms, resource);
        pageXml.setMasterTemplate(masterTemplate);
        pageXml.write();
        return resource;
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#copyResource(com.opencms.file.CmsObject, java.lang.String, java.lang.String, boolean)
     */
    public void copyResource(CmsObject cms, String resourcename, String destination, boolean keepFlags, boolean lockCopy) throws CmsException {
        // Read and parse the source page file
        CmsFile file = cms.readFile(resourcename);
        CmsXmlControlFile hXml = new CmsXmlControlFile(cms, file);

        // Check the path of the body file.
        // Don't use the checkBodyPath method here to avaoid overhead.
        String bodyPath = (I_CmsWpConstants.C_VFS_PATH_BODIES.substring(0, I_CmsWpConstants.C_VFS_PATH_BODIES.lastIndexOf("/"))) + (resourcename);
        String body = hXml.getElementTemplate("body");
        body = hXml.validateBodyPath(cms, body, file);
        String bodyXml = cms.getRequestContext().getDirectoryTranslator().translateResource(I_CmsConstants.C_VFS_DEFAULT + body);

        if ((I_CmsConstants.C_VFS_DEFAULT + bodyPath).equals(bodyXml)) {

            // Evaluate some path information
            String destinationFolder = destination.substring(0, destination.lastIndexOf("/") + 1);
            checkFolders(cms, destinationFolder);
            String newbodyPath = (I_CmsWpConstants.C_VFS_PATH_BODIES.substring(0, I_CmsWpConstants.C_VFS_PATH_BODIES.lastIndexOf("/"))) + destination;

            // we don't want to use the changeContent method here
            // to avoid overhead by copying, readig, parsing, setting XML and writing again.
            // Instead, we re-use the already parsed XML content of the source
            hXml.setElementTemplate("body", newbodyPath);
            cms.doCopyFile(resourcename, destination, lockCopy);
            CmsFile newPageFile = cms.readFile(destination);
            newPageFile.setContents(hXml.getXmlText().getBytes());
            cms.writeFile(newPageFile);

            // Now the new page file is created. Copy the body file
            cms.doCopyFile(bodyPath, newbodyPath, lockCopy);
            // linkmanagement: copy the links of the page
            cms.createLinkEntrys(newPageFile.getResourceId(), cms.readLinkEntrys(file.getResourceId()));
        } else {
            // The body part of the source was not found at
            // the default place. Leave it there, don't make
            // a copy and simply make a copy of the page file.
            // So the new page links to the old body.
            cms.doCopyFile(resourcename, destination, true);
        }
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#copyResourceToProject(com.opencms.file.CmsObject, java.lang.String)
     */
    public void copyResourceToProject(CmsObject cms, String resourcename) throws CmsException {
        //String resourceName = linkManager.getResourceName(resourceId);
        CmsFile file = cms.readFile(resourcename, true);
        cms.doCopyResourceToProject(resourcename);
        //check if the file type name is page
        String bodyPath = checkBodyPath(cms, (CmsFile)file);
        if (bodyPath != null) {
            cms.doCopyResourceToProject(bodyPath);
        }
    }
    
    /**
     * @see com.opencms.file.I_CmsResourceType#deleteResource(com.opencms.file.CmsObject, java.lang.String)
     */
    public void deleteResource(CmsObject cms, String resourcename) throws CmsException {
        CmsFile file = cms.readFile(resourcename);
        cms.doDeleteFile(resourcename);
        // linkmanagement: delete the links on the page
        cms.deleteLinkEntrys(file.getResourceId());
        String bodyPath = checkBodyPath(cms, (CmsFile)file);
        if (bodyPath != null) {
            try {
                cms.doDeleteFile(bodyPath);
            } catch (CmsException e) {
                if (e.getType() != CmsException.C_NOT_FOUND) {
                    throw e;
                }
            }
        }

        // The page file contains XML.
        // So there could be some data in the parser's cache.
        // Clear it!
        String currentProject = cms.getRequestContext().currentProject().getName();
        CmsXmlControlFile.clearFileCache(currentProject + ":" + resourcename);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#undeleteResource(com.opencms.file.CmsObject, java.lang.String)
     */
    public void undeleteResource(CmsObject cms, String resourcename) throws CmsException {
        CmsFile file = cms.readFile(resourcename, true);
        cms.doUndeleteFile(resourcename);
        String bodyPath = checkBodyPath(cms, (CmsFile)file);
        if (bodyPath != null) {
            try {
                cms.doUndeleteFile(bodyPath);
            } catch (CmsException e) {
                if (e.getType() != CmsException.C_NOT_FOUND) {
                    throw e;
                }
            }
        }

        // The page file contains XML.
        // So there could be some data in the parser's cache.
        // Clear it!
        String currentProject = cms.getRequestContext().currentProject().getName();
        CmsXmlControlFile.clearFileCache(currentProject + ":" + resourcename);

        // linkmanagement: create the links of the restored page
        CmsPageLinks linkObject = cms.getPageLinks(cms.readAbsolutePath(file));
        cms.createLinkEntrys(linkObject.getResourceId(), linkObject.getLinkTargets());
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#moveResource(com.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public void moveResource(CmsObject cms, String resourcename, String destination) throws CmsException {
        CmsFile file = cms.readFile(resourcename);
        String bodyPath = checkBodyPath(cms, file);
        cms.doMoveResource(resourcename, destination);
        if (bodyPath != null) {
            String hbodyPath = I_CmsWpConstants.C_VFS_PATH_BODIES.substring(0, I_CmsWpConstants.C_VFS_PATH_BODIES.lastIndexOf("/")) + destination;
            checkFolders(cms, destination.substring(0, destination.lastIndexOf("/")));
            cms.doMoveResource(bodyPath, hbodyPath);
            changeContent(cms, destination, hbodyPath);
        }

        // linkmanagement: delete the links of the old page and create them for the new one
        CmsUUID oldId = file.getResourceId();
        CmsUUID newId = cms.readFileHeader(destination).getResourceId();
        cms.createLinkEntrys(newId, cms.readLinkEntrys(oldId));
        cms.deleteLinkEntrys(oldId);

    }

    /**
     * @see com.opencms.file.I_CmsResourceType#renameResource(com.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public void renameResource(CmsObject cms, String resourcename, String newname) throws CmsException {
        // the file that should be renamed
        CmsFile file = cms.readFile(resourcename);
        String parent = CmsResource.getParent(resourcename);        
        // the current body path as it is saved in the XML page file
        String currentBodyPath = readBodyPath(cms, file);

        // build the body path from scratch to control if the current 
        // body path in the XML page is a path where the Cms expects
        // it's body files
        int lastSlashIndex = I_CmsWpConstants.C_VFS_PATH_BODIES.lastIndexOf("/");
        String defaultBodyPath = (I_CmsWpConstants.C_VFS_PATH_BODIES.substring(0, lastSlashIndex)) + resourcename;

        // rename the file itself
        cms.doRenameResource(resourcename, newname);

        // unless somebody edited the body path by hand, rename the file in the body path additionally
        if (defaultBodyPath.equals(currentBodyPath)) {
            cms.doRenameResource(currentBodyPath, newname);
            lastSlashIndex = currentBodyPath.lastIndexOf("/") + 1;
            defaultBodyPath = currentBodyPath.substring(0, lastSlashIndex) + newname;

            changeContent(cms, parent + newname, defaultBodyPath);
        }
        
        // linkmanagement: delete the links of the old page and create them for the new one
        CmsUUID oldId = file.getFileId();
        CmsUUID newId = cms.readFileHeader(parent + newname).getFileId();
        cms.createLinkEntrys(newId, cms.readLinkEntrys(oldId));
        cms.deleteLinkEntrys(oldId);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#restoreResource(com.opencms.file.CmsObject, int, java.lang.String)
     */
    public void restoreResource(CmsObject cms, int versionId, String resourcename) throws CmsException {        
        //if(!cms.accessWrite(filename)){
        if (!cms.hasPermissions(resourcename, I_CmsConstants.C_WRITE_ACCESS)) {
            throw new CmsException(resourcename, CmsException.C_NO_ACCESS);
        }
        CmsFile file = cms.readFile(resourcename);
        cms.doRestoreResource(versionId, resourcename);
        String bodyPath = checkBodyPath(cms, (CmsFile)file);
        if (bodyPath != null) {
            try {
                cms.doRestoreResource(versionId, bodyPath);
            } catch (CmsException e) {
                // do not throw an exception when there is no body for this version
                // maybe only the control file was changed
                if (e.getType() == CmsException.C_NOT_FOUND) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO, "[CmsResourceTypePage] version " + versionId + " of " + bodyPath + " not found!");
                    }
                } else {
                    throw e;
                }
            }
        }

        // linkmanagement: create the links of the restored page
        CmsPageLinks linkObject = cms.getPageLinks(cms.readAbsolutePath(file));
        cms.createLinkEntrys(linkObject.getResourceId(), linkObject.getLinkTargets());
    }    

    /**
     * @see com.opencms.file.I_CmsResourceType#replaceResource(com.opencms.file.CmsObject, java.lang.String, java.util.Map, byte[], int)
     */
    public void replaceResource(CmsObject cms, String resourcename, Map resourceProperties, byte[] resourceContent, int newResType) throws CmsException {
        // page files cannot be replaced yet...
    }    

    /**
     * @see com.opencms.file.I_CmsResourceType#undoChanges(com.opencms.file.CmsObject, java.lang.String)
     */
    public void undoChanges(CmsObject cms, String resourcename) throws CmsException {
        if (!cms.hasPermissions(resourcename, I_CmsConstants.C_WRITE_ACCESS)) {
            throw new CmsException(resourcename, CmsException.C_NO_ACCESS);
        }
        CmsFile file = cms.readFile(resourcename);
        cms.doUndoChanges(resourcename);
        String bodyPath = checkBodyPath(cms, (CmsFile)file);
        if (bodyPath != null) {
            cms.doUndoChanges(bodyPath);
        }

        // linkmanagement: create the links of the restored page
        CmsPageLinks linkObject = cms.getPageLinks(cms.readAbsolutePath(file));
        cms.createLinkEntrys(linkObject.getResourceId(), linkObject.getLinkTargets());
    }
    
    /**
     * @see com.opencms.file.I_CmsResourceType#lockResource(com.opencms.file.CmsObject, java.lang.String, boolean)
     */
    public void lockResource(CmsObject cms, String resourcename, boolean force) throws CmsException {
        // First read the page file.
        CmsFile pageFile = cms.readFile(resourcename);

        CmsResource bodyFile = null;
        String bodyPath = null;
        // Try to fetch the body file.
        try {
            bodyPath = readBodyPath(cms, pageFile);
            bodyFile = cms.readFileHeader(bodyPath);
        } catch (Exception e) {
            bodyPath = null;
            bodyFile = null;
        }
        // first lock the page file
        cms.doLockResource(resourcename, force);

        if (bodyFile != null) {
            // Everything with the page file is ok. We have write access. XML is valid.
            cms.doLockResource(bodyPath, force);
        }
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#unlockResource(com.opencms.file.CmsObject, java.lang.String, boolean)
     */
    public void unlockResource(CmsObject cms, String resourcename, boolean forceRecursive) throws CmsException {
        // First read the page file.
        CmsFile pageFile = cms.readFile(resourcename);

        CmsResource bodyFile = null;
        String bodyPath = null;
        // Try to fetch the body file.
        try {
            bodyPath = readBodyPath(cms, pageFile);
            bodyFile = cms.readFileHeader(bodyPath);
        } catch (Exception e) {
            bodyPath = null;
            bodyFile = null;
        }

        cms.doUnlockResource(resourcename);

        if (bodyFile != null) {
            // Everything with the page file is ok. We have write access. XML is valid.
            cms.doUnlockResource(bodyPath);
        }
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#changeLockedInProject(com.opencms.file.CmsObject, int, java.lang.String)
     */
    public void changeLockedInProject(CmsObject cms, int newProjectId, String resourcename) throws CmsException {
        CmsFile file = cms.readFile(resourcename, true);
        cms.doChangeLockedInProject(newProjectId, resourcename);
        String bodyPath = checkBodyPath(cms, (CmsFile)file);
        if (bodyPath != null) {
            cms.doChangeLockedInProject(newProjectId, bodyPath);
        }

        // The page file contains XML.
        // So there could be some data in the parser's cache.
        // Clear it!
        String currentProject = cms.getRequestContext().currentProject().getName();
        CmsXmlControlFile.clearFileCache(currentProject + ":" + resourcename);
    }

    /**
     * @see com.opencms.file.I_CmsResourceType#exportResource(com.opencms.file.CmsObject, com.opencms.file.CmsFile)
     */
    public CmsFile exportResource(CmsObject cms, CmsFile resourcename) throws CmsException {
        // nothing special to do here
        return resourcename;
    }

    /**
     * Imports a resource to the cms.<p>
     *
     * @param the current cms object
     * @param resource the resource to be imported
     * @param content the content of the resource
     * @param properties the properties of the resource
     * @param destination the name of the resource destinaition
     * @return the imported CmsResource
     * @throws CmsException if operation was not successful
     */
    public CmsResource importResource(CmsObject cms, CmsResource resource, byte[] content, Map properties, String destination) throws CmsException {
        CmsResource importedResource = null;
        boolean changed = true;

       try {
            importedResource = cms.doImportResource(resource,  content, properties,  destination);
            changed = (importedResource == null);
        } catch (CmsException e) {
            // an exception is thrown if the resource already exists
        }
        if (changed) {
        // if the resource already exists it must be updated
            lockResource(cms, destination, true);
            cms.doWriteResource(destination, properties, null, null, -1, getResourceType(), content);
            importedResource = cms.readFileHeader(destination);
        }
        return importedResource;
        }
    
    
    
    /**
     * Returns the real body path sepcified in the content of the file.<p>
     *
     * The provided file must be a CmsFile object an not a file header. 
     * This won't be checked for performance reasons.<p>
     * 
     * @param cms to access the XML file
     * @param file file in which the body path is stored 
     * @throws CmsException if something goes wrong
     * @return the real body path sepcified in the content of the file
     */
    private String readBodyPath(CmsObject cms, CmsFile file) throws CmsException {
        CmsXmlControlFile hXml = new CmsXmlControlFile(cms, file);
        String body = "";
        try {
            // Return translated path name for body
            body = hXml.getElementTemplate("body");
            body = hXml.validateBodyPath(cms, body, file);
            body = cms.getRequestContext().getDirectoryTranslator().translateResource(I_CmsConstants.C_VFS_DEFAULT + body);
            if (body.startsWith(I_CmsConstants.C_VFS_DEFAULT))
                body = body.substring(I_CmsConstants.C_VFS_DEFAULT.length());
        } catch (CmsException exc) {
            // could not read body
        }
        return body;
    }

    /**
     * Returns the real body path for the provided file.<p>
     *
     * @param cms to access the XML file
     * @param file file in which the body path is stored
     * @throws CmsException if something goes wrong
     * @return the real body path for the provided file
     */
    private String checkBodyPath(CmsObject cms, CmsFile file) throws CmsException {
        // Use translated path name of body
        String result = I_CmsWpConstants.C_VFS_PATH_BODIES.substring(0, I_CmsWpConstants.C_VFS_PATH_BODIES.lastIndexOf("/")) + cms.readAbsolutePath(file);
        if (!result.equals(readBodyPath(cms, (CmsFile)file))) {
            result = null;
        }
        return result;
    }    

    /**
     * This method changes the path of the body file in the XML file
     * if file type is "page".<p>
     *
     * @param cms the cms context
     * @param filename The XML content file
     * @param bodypath the new XML content entry
     * @throws CmsException if something goes wring
     */
    private void changeContent(CmsObject cms, String filename, String bodypath) throws CmsException {
        CmsFile file = cms.readFile(filename);
        CmsXmlControlFile hXml = new CmsXmlControlFile(cms, file);
        hXml.setElementTemplate("body", bodypath);
        hXml.write();
    }

    /**
     * This method checks if all nescessary folders are exisitng in the content body
     * folder and creates the missing ones. <br>
     * All page contents files are stored in the content body folder in a mirrored directory
     * structure of the OpenCms filesystem. Therefor it is nescessary to create the
     * missing folders when a new page document is createg.
     * @param cms The CmsObject
     * @param path The path in the CmsFilesystem where the new page should be created  
     * @throws CmsException if something goes wrong
     */
    private void checkFolders(CmsObject cms, String path) throws CmsException {

        String completePath = I_CmsWpConstants.C_VFS_PATH_BODIES;
        StringTokenizer t = new StringTokenizer(path, "/");
        String correspFolder = "/";
        // check if all folders are there
        while (t.hasMoreTokens()) {
            String foldername = t.nextToken();
            correspFolder = correspFolder + foldername + "/";
            try {
                // try to read the folder. if this fails, an exception is thrown
                cms.readFolder(completePath + foldername + "/");
            } catch (CmsException e) {
                // the folder could not be read, so create it.
                String orgFolder = completePath + foldername + "/";
                orgFolder = orgFolder.substring(I_CmsWpConstants.C_VFS_PATH_BODIES.length() - 1);
                CmsFolder newfolder = cms.doCreateFolder(completePath, foldername);
                cms.doLockResource(cms.readAbsolutePath(newfolder), false);
                cms.cpacc(orgFolder, cms.readAbsolutePath(newfolder));
                try {
                    CmsFolder correspondingFolder = cms.readFolder(correspFolder);
                    CmsLock lock = cms.getLock(correspondingFolder);
                    if (lock.isNullLock()) {
                        cms.doUnlockResource(cms.readAbsolutePath(newfolder));
                    }
                } catch (CmsException ex) {
                    // unable to unlock folder if parent folder is locked
                }
            }
            completePath += foldername + "/";
        }
    }
}
