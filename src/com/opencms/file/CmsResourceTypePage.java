/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceTypePage.java,v $
* Date   : $Date: 2003/07/03 13:29:45 $
* Version: $Revision: 1.59 $
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
import com.opencms.flex.util.CmsUUID;
import com.opencms.linkmanagement.CmsPageLinks;
import com.opencms.template.CmsXmlControlFile;
import com.opencms.workplace.I_CmsWpConstants;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
//import org.opencms.db.generic.linkmanagement.*;

/**
 * Implementation of a resource type for "editable content pages" in OpenCms.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.59 $ $Date: 2003/07/03 13:29:45 $
 */
public class CmsResourceTypePage implements I_CmsResourceType, Serializable, I_CmsConstants, I_CmsWpConstants {

    /** Definition of the class */
    private static final String C_CLASSNAME = "com.opencms.template.CmsXmlTemplate";

    /** String to save the combined /default/vfs/ path */
    private static final String C_DEFVFS = C_FOLDER_SEPARATOR + C_DEFAULT_SITE + C_FOLDER_SEPARATOR + C_ROOTNAME_VFS;
    
     /**
      * The id of resource type.
      */
    private int m_resourceType;

    /**
     * The id of the launcher used by this resource.
     */
    private int m_launcherType;

    /**
     * The resource type name.
     */
    private String m_resourceTypeName;

    /**
     * The class name of the Java class launched by the launcher.
     */
    private String m_launcherClass;
        
    /** Internal debug flag */
    private static final int DEBUG = 0;    


    /**
     * inits a new CmsResourceType object.
     *
     * @param resourceType The id of the resource type.
     * @param launcherType The id of the required launcher.
     * @param resourceTypeName The printable name of the resource type.
     * @param launcherClass The Java class that should be invoked by the launcher.
     * This value is <b> null </b> if the default invokation class should be used.
     */
    public void init(int resourceType, int launcherType,
                           String resourceTypeName, String launcherClass){

        m_resourceType=resourceType;
        m_launcherType=launcherType;
        m_resourceTypeName=resourceTypeName;
        m_launcherClass=launcherClass;
    }
    
    /**
     * Returns the default body start string for a new XML template.
     * @return the default body start string for a new XML template 
     */
    public static String getDefaultBodyStart() {
       return "<?xml version=\"1.0\" encoding=\""
            + A_OpenCms.getDefaultEncoding()
            + "\"?>\n<XMLTEMPLATE>\n<TEMPLATE>\n<![CDATA[\n";
    }
    
    /**
     * Returns the default body end string for a new XML template.
     * @return the default body end string for a new XML template 
     */    
    public static String getDefaultBodyEnd() {
        return "]]></TEMPLATE>\n</XMLTEMPLATE>";
    }
    
    /**
     * Returns the name of the Java class loaded by the launcher.
     * This method returns <b>null</b> if the default class for this type is used.
     *
     * @return the name of the Java class.
     */
     public String getLauncherClass() {
         if ((m_launcherClass == null) || (m_launcherClass.length()<1)) {
            return C_UNKNOWN_LAUNCHER;
         } else {
            return m_launcherClass;
         }
     }
     /**
     * Returns the launcher type needed for this resource-type.
     *
     * @return the launcher type for this resource-type.
     */
     public int getLauncherType() {
         return m_launcherType;
     }
    /**
     * Returns the name for this resource-type.
     *
     * @return the name for this resource-type.
     */
     public String getResourceTypeName() {
         return m_resourceTypeName;
     }
    /**
     * Returns the type of this resource-type.
     *
     * @return the type of this resource-type.
     */
    public int getResourceType() {
         return m_resourceType;
     }
    /**
     * Returns a string-representation for this object.
     * This can be used for debugging.
     *
     * @return string-representation for this object.
     */
     public String toString() {
        StringBuffer output=new StringBuffer();
        output.append("[ResourceType]:");
        output.append(m_resourceTypeName);
        output.append(" , Id=");
        output.append(m_resourceType);
        output.append(" , launcherType=");
        output.append(m_launcherType);
        output.append(" , launcherClass=");
        output.append(m_launcherClass);
        return output.toString();
      }

    /**
    * Changes the group of a resource.
    * <br>
    * Only the group of a resource in an offline project can be changed. The state
    * of the resource is set to CHANGED (1).
    * If the content of this resource is not existing in the offline project already,
    * it is read from the online project and written into the offline project.
    * <p>
    * <B>Security:</B>
    * Access is granted, if:
    * <ul>
    * <li>the user has access to the project</li>
    * <li>the user is owner of the resource or is admin</li>
    * <li>the resource is locked by the callingUser</li>
    * </ul>
    *
    * @param filename the complete path to the resource.
    * @param newGroup the name of the new group for this resource.
    * @param chRekursive only used by folders.
    *
    * @throws CmsException if operation was not successful.
    */
    public void chgrp(CmsObject cms, String filename, String newGroup, boolean chRekursive) throws CmsException{
// TODO: remove this
/*
        CmsFile file = cms.readFile(filename);
        // check if the current user has the right to change the group of the
        // resource. Only the owner of a file and the admin are allowed to do this.
        if ((cms.getRequestContext().currentUser().equals(cms.readOwner(file))) ||
            (cms.userInGroup(cms.getRequestContext().currentUser().getName(), C_GROUP_ADMIN))){
            cms.doChgrp(filename, newGroup);
            //check if the file type name is page
            String bodyPath = checkBodyPath(cms, (CmsFile)file);
            if (bodyPath != null){
                cms.doChgrp(bodyPath, newGroup);
            }
        }
*/
    }

    /**
    * Changes the flags of a resource.
    * <br>
    * Only the flags of a resource in an offline project can be changed. The state
    * of the resource is set to CHANGED (1).
    * If the content of this resource is not existing in the offline project already,
    * it is read from the online project and written into the offline project.
    * The user may change the flags, if he is admin of the resource.
    * <p>
    * <B>Security:</B>
    * Access is granted, if:
    * <ul>
    * <li>the user has access to the project</li>
    * <li>the user can write the resource</li>
    * <li>the resource is locked by the callingUser</li>
    * </ul>
    *
    * @param filename the complete path to the resource.
    * @param flags the new flags for the resource.
    * @param chRekursive only used by folders.
    *
    * @throws CmsException if operation was not successful.
    * for this resource.
    */
    public void chmod(CmsObject cms, String filename, int flags, boolean chRekursive) throws CmsException{
// TODO: remove this
/*
        CmsFile file = cms.readFile(filename);
        // check if the current user has the right to change the group of the
        // resource. Only the owner of a file and the admin are allowed to do this.
        if ((cms.getRequestContext().currentUser().equals(cms.readOwner(file))) ||
           (cms.userInGroup(cms.getRequestContext().currentUser().getName(), C_GROUP_ADMIN))){

            // modify the access flags
            cms.doChmod(filename, flags);

            String bodyPath = checkBodyPath(cms, (CmsFile)file);
            if (bodyPath != null){
                // set the internal read flag if nescessary
                if ((flags & C_ACCESS_INTERNAL_READ) ==0 ) {
                    flags += C_ACCESS_INTERNAL_READ;
                }
                cms.doChmod(bodyPath, flags);
            }
        }
*/
        }

    /**
    * Changes the owner of a resource.
    * <br>
    * Only the owner of a resource in an offline project can be changed. The state
    * of the resource is set to CHANGED (1).
    * If the content of this resource is not existing in the offline project already,
    * it is read from the online project and written into the offline project.
    * The user may change this, if he is admin of the resource.
    * <p>
    * <B>Security:</B>
    * Access is granted, if:
    * <ul>
    * <li>the user has access to the project</li>
    * <li>the user is owner of the resource or the user is admin</li>
    * <li>the resource is locked by the callingUser</li>
    * </ul>
    *
    * @param filename the complete path to the resource.
    * @param newOwner the name of the new owner for this resource.
    * @param chRekursive only used by folders.
    *
    * @throws CmsException if operation was not successful.
    */
    public void chown(CmsObject cms, String filename, String newOwner, boolean chRekursive) throws CmsException{
// TODO: remove this
/*
        CmsFile file = cms.readFile(filename);
        // check if the current user has the right to change the group of the
        // resource. Only the owner of a file and the admin are allowed to do this.
        if ((cms.getRequestContext().currentUser().equals(cms.readOwner(file))) ||
            (cms.userInGroup(cms.getRequestContext().currentUser().getName(), C_GROUP_ADMIN))){
            cms.doChown(filename, newOwner);
            //check if the file type name is page
            String bodyPath = checkBodyPath(cms, (CmsFile)file);
            if (bodyPath != null){
                cms.doChown(bodyPath, newOwner);
            }
        }
*/
        }
    
    /**
     * Change the timestamp of a page.
     * 
     * @param resourceName the name of the resource to change
     * @param timestamp timestamp the new timestamp of the changed resource
     * @param boolean flag to touch recursively all sub-resources in case of a folder
     */  
    public void touch( CmsObject cms, String resourceName, long timestamp, boolean touchRecursive ) throws CmsException{
        // create a valid resource
        CmsFile file = cms.readFile( resourceName );
        
            // touch the page itself
            cms.doTouch( resourceName, timestamp );
            
            // touch its counterpart under content/bodies
            String bodyPath = this.checkBodyPath( cms, (CmsFile)file );
            if (bodyPath!=null) {
                cms.doTouch( bodyPath, timestamp );
            }            
        }

    /**
    * Changes the resourcetype of a resource.
    * <br>
    * Only the resourcetype of a resource in an offline project can be changed. The state
    * of the resource is set to CHANGED (1).
    * If the content of this resource is not exisiting in the offline project already,
    * it is read from the online project and written into the offline project.
    * The user may change this, if he is admin of the resource.
    * <p>
    * <B>Security:</B>
    * Access is granted, if:
    * <ul>
    * <li>the user has access to the project</li>
    * <li>the user is owner of the resource or is admin</li>
    * <li>the resource is locked by the callingUser</li>
    * </ul>
    *
    * @param filename the complete path to the resource.
    * @param newType the name of the new resourcetype for this resource.
    *
    * @throws CmsException if operation was not successful.
    */
    public void chtype(CmsObject cms, String filename, String newType) throws CmsException{
        CmsFile file = cms.readFile(filename);
        // check if the current user has the right to change the group of the
        // resource. Only the owner of a file and the admin are allowed to do this.
        if ((cms.getRequestContext().currentUser().equals(cms.readOwner(file))) ||
            (cms.userInGroup(cms.getRequestContext().currentUser().getName(), C_GROUP_ADMIN))){
            cms.doChtype(filename, newType);
            //check if the file type name is page
            String bodyPath = checkBodyPath(cms, (CmsFile)file);
            if (bodyPath != null){
                cms.doChtype(bodyPath, newType);
            }
        }
    }


    /**
    * Copies a Resource.
    *
    * @param source the complete path of the sourcefile.
    * @param destination the complete path of the destinationfolder.
    * @param keepFlags <code>true</code> if the copy should keep the source file's flags,
    *        <code>false</code> if the copy should get the user's default flags.
    *
    * @throws CmsException if the file couldn't be copied, or the user
    * has not the appropriate rights to copy the file.
    */
    public void copyResource(CmsObject cms, String source, String destination, boolean keepFlags) throws CmsException{
        // Read and parse the source page file
        CmsFile file = cms.readFile(source);
        CmsXmlControlFile hXml=new CmsXmlControlFile(cms, file);

        // Check the path of the body file.
        // Don't use the checkBodyPath method here to avaoid overhead.
        String bodyPath=(C_VFS_PATH_BODIES.substring(0, C_VFS_PATH_BODIES.lastIndexOf("/")))+(source);
        String body = hXml.getElementTemplate("body");
        body = hXml.validateBodyPath(cms, body, file);
        String bodyXml=cms.getRequestContext().getDirectoryTranslator().translateResource(C_DEFVFS + body);        

        if ((C_DEFVFS + bodyPath).equals(bodyXml)){

            // Evaluate some path information
            String destinationFolder = destination.substring(0,destination.lastIndexOf("/")+1);
            checkFolders(cms, destinationFolder);
            String newbodyPath=(C_VFS_PATH_BODIES.substring(0, C_VFS_PATH_BODIES.lastIndexOf("/")))+ destination;

            // we don't want to use the changeContent method here
            // to avoid overhead by copying, readig, parsing, setting XML and writing again.
            // Instead, we re-use the already parsed XML content of the source
            hXml.setElementTemplate("body", newbodyPath);
            cms.doCopyFile(source, destination);
            CmsFile newPageFile = cms.readFile(destination);
            newPageFile.setContents(hXml.getXmlText().getBytes());
            cms.writeFile(newPageFile);

            // Now the new page file is created. Copy the body file
            cms.doCopyFile(bodyPath, newbodyPath);
            // linkmanagement: copy the links of the page
            cms.createLinkEntrys(newPageFile.getResourceId(), cms.readLinkEntrys(file.getResourceId()));
        } else {
            // The body part of the source was not found at
            // the default place. Leave it there, don't make
            // a copy and simply make a copy of the page file.
            // So the new page links to the old body.
            cms.doCopyFile(source, destination);
        }
        // set access flags, if neccessary
        if(!keepFlags) {
            setDefaultFlags(cms, destination);
        }
    }

    /**
    * Copies a resource from the online project to a new, specified project.
    * <br>
    * Copying a resource will copy the file header or folder into the specified
    * offline project and set its state to UNCHANGED.
    *
    * @param resource the name of the resource.
    * @throws CmsException if operation was not successful.
    */
    public void copyResourceToProject(CmsObject cms, String resourceName) throws CmsException {
        //String resourceName = linkManager.getResourceName(resourceId);
        CmsFile file = cms.readFile(resourceName, true);
        cms.doCopyResourceToProject(resourceName);
        //check if the file type name is page
        String bodyPath = checkBodyPath(cms, (CmsFile)file);
        if (bodyPath != null){
            cms.doCopyResourceToProject(bodyPath);
        }
    }

    /**
     * Creates a new resource
     *
     * @param cms The CmsObject
     * @param folder The name of the parent folder
     * @param name The name of the file
     * @param properties The properties of the file
     * @param contents The file content
     * @param parameter an object (e.g. a HashMap) holding parameters (e.g. key/value coded) to create the new resource
     *
     * @throws CmsException if operation was not successful.
     */
    public CmsResource createResource(CmsObject cms, String newPageName, Map properties, byte[] contents, Object parameter) throws CmsException{

        String folderName = newPageName.substring(0, newPageName.lastIndexOf(C_FOLDER_SEPARATOR, newPageName.length())+1);
        String pageName = newPageName.substring(folderName.length(), newPageName.length());

        // Scan for mastertemplates
        Vector allMasterTemplates = cms.getFilesInFolder(C_VFS_PATH_DEFAULT_TEMPLATES);

        // Select the first mastertemplate as default
        String masterTemplate = "";
        if(allMasterTemplates.size() > 0) {
            masterTemplate = cms.readAbsolutePath((CmsFile)allMasterTemplates.elementAt(0));
        }

        // Evaluate the absolute path to the new body file
        String bodyFolder =(C_VFS_PATH_BODIES.substring(0, C_VFS_PATH_BODIES.lastIndexOf("/"))) + folderName;

        // Create the new page file
        CmsFile file = cms.doCreateFile(newPageName, "".getBytes(), m_resourceTypeName, properties);
        cms.doLockResource(newPageName, true);
        CmsXmlControlFile pageXml = new CmsXmlControlFile(cms, file);
        pageXml.setTemplateClass(C_CLASSNAME);
        pageXml.setMasterTemplate(masterTemplate);
        pageXml.setElementClass("body", C_CLASSNAME);
        pageXml.setElementTemplate("body", bodyFolder + pageName);
        pageXml.write();

        // Check, if the body path exists and create missing folders, if neccessary
        checkFolders(cms, folderName);

        // Create the new body file
        CmsFile bodyFile = cms.doCreateFile(bodyFolder + pageName, (getDefaultBodyStart() + new String(contents) + getDefaultBodyEnd()).getBytes(), I_CmsConstants.C_TYPE_PLAIN_NAME, new Hashtable());
        cms.doLockResource(bodyFolder + pageName, true);
        int flags = bodyFile.getAccessFlags();
        if ((flags & C_ACCESS_INTERNAL_READ) ==0 ) {
            flags += C_ACCESS_INTERNAL_READ;
        }
        cms.chmod(cms.readAbsolutePath(bodyFile), flags);
        // linkmanagement: create the links of the new page (for the case that the content was not empty
        if(contents.length > 1){
            CmsPageLinks linkObject = cms.getPageLinks(newPageName);
            cms.createLinkEntrys(linkObject.getResourceId(), linkObject.getLinkTargets());
        }
        return file;
    }

    public CmsResource createResourceForTemplate(CmsObject cms, String newPageName, Hashtable properties, byte[] contents, String masterTemplate) throws CmsException{
        CmsFile resource = (CmsFile)this.createResource(cms, newPageName, properties, contents, null);
        CmsXmlControlFile pageXml = new CmsXmlControlFile(cms, resource);
        pageXml.setMasterTemplate(masterTemplate);
        pageXml.write();
        return resource;
    }

    /**
    * Deletes a resource.
    *
    * @param filename the complete path of the file.
    *
    * @throws CmsException if the file couldn't be deleted, or if the user
    * has not the appropriate rights to delete the file.
    */
    public void deleteResource(CmsObject cms, String filename) throws CmsException{
        CmsFile file = cms.readFile(filename);
        cms.doDeleteFile(filename);
        // linkmanagement: delete the links on the page
        cms.deleteLinkEntrys(file.getResourceId());
        String bodyPath = checkBodyPath(cms, (CmsFile)file);
        if (bodyPath != null){
            try{
                cms.doDeleteFile(bodyPath);
            } catch (CmsException e){
                if(e.getType() != CmsException.C_NOT_FOUND){
                    throw e;
                }
            }
        }

        // The page file contains XML.
        // So there could be some data in the parser's cache.
        // Clear it!
        String currentProject = cms.getRequestContext().currentProject().getName();
        CmsXmlControlFile.clearFileCache(currentProject + ":" + filename);
    }

    /**
    * Undeletes a resource.
    *
    * @param filename the complete path of the file.
    *
    * @throws CmsException if the file couldn't be undeleted, or if the user
    * has not the appropriate rights to undelete the file.
    */
    public void undeleteResource(CmsObject cms, String filename) throws CmsException{
        CmsFile file = cms.readFile(filename, true);
        cms.doUndeleteFile(filename);
        String bodyPath = checkBodyPath(cms, (CmsFile)file);
        if (bodyPath != null){
            try{
                cms.doUndeleteFile(bodyPath);
            } catch (CmsException e){
                if(e.getType() != CmsException.C_NOT_FOUND){
                    throw e;
                }
            }
        }

        // The page file contains XML.
        // So there could be some data in the parser's cache.
        // Clear it!
        String currentProject = cms.getRequestContext().currentProject().getName();
        CmsXmlControlFile.clearFileCache(currentProject + ":" + filename);

        // linkmanagement: create the links of the restored page
        CmsPageLinks linkObject = cms.getPageLinks(cms.readAbsolutePath(file));
        cms.createLinkEntrys(linkObject.getResourceId(), linkObject.getLinkTargets());
    }

    /**
     * When a resource has to be exported, the ID?s inside the
     * Linkmanagement-Tags have to be changed to the corresponding URL?s
     *
     * @param file is the file that has to be changed
     */
    public CmsFile exportResource(CmsObject cms, CmsFile file) throws CmsException {
        //nothing to do here, because there couldn?t be any Linkmanagement-Tags in a page-file (control-file)
        return file;
    }

    /**
     * Imports a resource.
     *
     * @param cms The current CmsObject.
     * @param source The sourcepath of the resource to import.
     * @param destination The destinationpath of the resource to import.
     * @param type The type of the resource to import.
     * @param user The name of the owner of the resource.
     * @param group The name of the group of the resource.
     * @param access The access flags of the resource.
     * @param properties A Hashtable with the properties of the resource.
     * The key is the name of the propertydefinition, the value is the propertyvalue.
     * @param launcherStartClass The name of the launcher startclass.
     * @param content The filecontent if the resource is of type file
     * @param importPath The name of the import path
     * 
     * @return CmsResource The imported resource.
     * 
     * @throws Throws CmsException if the resource could not be imported
     * 
     */
    public CmsResource importResource(CmsObject cms, String source, String destination, String type,
                                       String user, String group, String access, long lastmodified, 
                                       Map properties, String launcherStartClass, byte[] content, String importPath) 
                       throws CmsException {
        CmsResource importedResource = null;
        destination = importPath + destination;
        
        boolean changed = true;
        int resourceType = cms.getResourceType(type).getResourceType();
		int launcherType = cms.getResourceType(type).getLauncherType();
		if((launcherStartClass == null) || ("".equals(launcherStartClass))){
			launcherStartClass = cms.getResourceType(type).getLauncherClass();
        }
        // try to read the new owner and group
		// TODO: fix this later
        CmsUser resowner = null;
        CmsGroup resgroup = null;
		int resaccess = 0;
        try{
        	resowner = cms.readUser(user);
        } catch (CmsException e){
            if (DEBUG>0) System.err.println("[" + this.getClass().getName() + ".importResource/1] User " + user + " not found");
            if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + this.getClass().getName() + ".importResource/1] User " + user + " not found");
            }                
        	resowner = cms.getRequestContext().currentUser();	
        }
        try{
        	resgroup = cms.readGroup(group);
        } catch (CmsException e){
            if (DEBUG>0) System.err.println("[" + this.getClass().getName() + ".importResource/2] Group " + group + " not found");
            if(I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + this.getClass().getName() + ".importResource/2] Group " + group + " not found");
            }  
        	resgroup = cms.getRequestContext().currentGroup();	
        }        
		try {
			resaccess = Integer.parseInt(access);
		} catch (Exception e){
			// 
		}
        try {
            importedResource = cms.doImportResource(destination, resourceType ,properties, launcherType, 
                                             launcherStartClass, resowner.getName(), resgroup.getName(), resaccess, lastmodified, content);
            if(importedResource != null){
                changed = false;
            }
        } catch (CmsException e) {
            // an exception is thrown if the resource already exists
        }
        if(changed){
        	// if the resource already exists it must be updated
            lockResource(cms,destination, true);
            cms.doWriteResource(destination,properties,resowner.getName(), resgroup.getName(),resaccess,resourceType,content);
            importedResource = cms.readFileHeader(destination);
        }

        return importedResource;
    }
    
    /**
    * Locks a given resource.
    * <br>
    * A user can lock a resource, so he is the only one who can write this
    * resource.
    *
    * @param resource the complete path to the resource to lock.
    * @param force if force is <code>true</code>, a existing locking will be overwritten.
    *
    * @throws CmsException if the user has not the rights to lock this resource.
    * It will also be thrown, if there is a existing lock and force was set to false.
    */
    public void lockResource(CmsObject cms, String resource, boolean force) throws CmsException{
        // First read the page file.
        CmsFile pageFile = cms.readFile(resource);

        CmsResource bodyFile = null;
        String bodyPath = null;
        // Try to fetch the body file.
        try {
            bodyPath = readBodyPath(cms, pageFile);
            bodyFile = cms.readFileHeader(bodyPath);
        } catch(Exception e) {
            bodyPath = null;
            bodyFile = null;
        }
        // first lock the page file
        cms.doLockResource(resource, force);

        if(bodyFile != null) {
            // Everything with the page file is ok. We have write access. XML is valid.
            cms.doLockResource(bodyPath, force);
        }
    }

    /**
    * Moves a resource to the given destination.
    *
    * @param source the complete path of the sourcefile.
    * @param destination the complete path of the destinationfile.
    *
    * @throws CmsException if the user has not the rights to move this resource,
    * or if the file couldn't be moved.
    */
    public void moveResource(CmsObject cms, String source, String destination) throws CmsException{
        CmsFile file = cms.readFile(source);
        String bodyPath = checkBodyPath(cms, file);
        cms.doMoveResource(source, destination);
        if(bodyPath != null) {
            String hbodyPath = C_VFS_PATH_BODIES.substring(0, C_VFS_PATH_BODIES.lastIndexOf("/")) + destination;
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
    * Renames the file to the new name.
    *
    * @param oldname the complete path to the file which will be renamed.
    * @param newname the new name of the file.
    *
    * @throws CmsException if the user has not the rights
    * to rename the file, or if the file couldn't be renamed.
    */
    public void renameResource(CmsObject cms, String oldname, String newname) throws CmsException {
        // the file that should be renamed
        CmsFile file = cms.readFile(oldname);
        // the current body path as it is saved in the XML page file
        String currentBodyPath = readBodyPath(cms, file);

        // build the body path from scratch to control if the current 
        // body path in the XML page is a path where the Cms expects
        // it's body files
        int lastSlashIndex = C_VFS_PATH_BODIES.lastIndexOf("/");
        String defaultBodyPath = (C_VFS_PATH_BODIES.substring(0, lastSlashIndex)) + oldname;

        // rename the file itself
        cms.doRenameResource(oldname, newname);

        // unless somebody edited the body path by hand, rename the file in the body path additionally
        if (defaultBodyPath.equals(currentBodyPath)) {
            cms.doRenameResource(currentBodyPath, newname);
            lastSlashIndex = currentBodyPath.lastIndexOf("/") + 1;
            defaultBodyPath = currentBodyPath.substring(0, lastSlashIndex) + newname;
            changeContent(cms, file.getParent() + newname, defaultBodyPath);
        }

        // linkmanagement: delete the links of the old page and create them for the new one
        CmsUUID oldId = file.getFileId();
        CmsUUID newId = cms.readFileHeader(file.getParent() + newname).getFileId();
        cms.createLinkEntrys(newId, cms.readLinkEntrys(oldId));
        cms.deleteLinkEntrys(oldId);
    }

    /**
     * Restores a file in the current project with a version in the backup
     *
     * @param cms The CmsObject
     * @param versionId The version id of the resource
     * @param filename The name of the file to restore
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void restoreResource(CmsObject cms, int versionId, String filename) throws CmsException{
        //if(!cms.accessWrite(filename)){
        if(!cms.checkPermissions(filename, I_CmsConstants.C_WRITE_ACCESS)) {
            throw new CmsException(filename, CmsException.C_NO_ACCESS);
        }
        CmsFile file = cms.readFile(filename);
        cms.doRestoreResource(versionId, filename);
        String bodyPath = checkBodyPath(cms, (CmsFile)file);
        if (bodyPath != null){
            try{
                cms.doRestoreResource(versionId, bodyPath);
            } catch(CmsException e){
                // do not throw an exception when there is no body for this version
                // maybe only the control file was changed
                if(e.getType() == CmsException.C_NOT_FOUND){
                    if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(A_OpenCms.C_OPENCMS_INFO,"[CmsResourceTypePage] version "+versionId+" of "+bodyPath+" not found!");
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
    * Undo changes in a resource.
    * <br>
    *
    * @param resource the complete path to the resource to be restored.
    *
    * @throws CmsException if the user has not the rights
    * to write this resource.
    */
    public void undoChanges(CmsObject cms, String resource) throws CmsException{
        //if(!cms.accessWrite(resource)){
        if(!cms.checkPermissions(resource, I_CmsConstants.C_WRITE_ACCESS)) {
            throw new CmsException(resource, CmsException.C_NO_ACCESS);
        }
        CmsFile file = cms.readFile(resource);
        cms.doUndoChanges(resource);
        String bodyPath = checkBodyPath(cms, (CmsFile)file);
        if (bodyPath != null){
            cms.doUndoChanges(bodyPath);
        }
        
        // linkmanagement: create the links of the restored page
        CmsPageLinks linkObject = cms.getPageLinks(cms.readAbsolutePath(file));
        cms.createLinkEntrys(linkObject.getResourceId(), linkObject.getLinkTargets());
    }

    /**
    * Unlocks a resource.
    * <br>
    * A user can unlock a resource, so other users may lock this file.
    *
    * @param resource the complete path to the resource to be unlocked.
    *
    * @throws CmsException if the user has not the rights
    * to unlock this resource.
    */
    public void unlockResource(CmsObject cms, String resource) throws CmsException{
        // First read the page file.
        CmsFile pageFile = cms.readFile(resource);

        CmsResource bodyFile = null;
        String bodyPath = null;
        // Try to fetch the body file.
        try {
            bodyPath = readBodyPath(cms, pageFile);
            bodyFile = cms.readFileHeader(bodyPath);
        } catch(Exception e) {
            bodyPath = null;
            bodyFile = null;
        }

        cms.doUnlockResource(resource);

        if(bodyFile != null) {
            // Everything with the page file is ok. We have write access. XML is valid.
            cms.doUnlockResource(bodyPath);
        }
    }


    /**
     * method to check get the real body path from the content file
     *
     * @param cms The CmsObject, to access the XML read file.
     * @param file File in which the body path is stored. This should really
     *      be a CmsFile object an not a file header. This won't be checked for
     *      performance reasons.
     */
    private String readBodyPath(CmsObject cms, CmsFile file)
        throws CmsException{
        CmsXmlControlFile hXml=new CmsXmlControlFile(cms, file);
        String body = "";
        try{
            // Return translated path name for body
            body = hXml.getElementTemplate("body");
            body = hXml.validateBodyPath(cms, body, file);
            body = cms.getRequestContext().getDirectoryTranslator().translateResource(C_DEFVFS + body);        
            if (body.startsWith(C_DEFVFS)) body = body.substring(C_DEFVFS.length());
        } catch (CmsException exc){
            // could not read body
        }
        return body;
    }

    /**
     * method to check get the real body path from the content file
     *
     * @param cms The CmsObject, to access the XML read file.
     * @param file File in which the body path is stored.
     */
    private String checkBodyPath(CmsObject cms, CmsFile file) throws CmsException {
        // Use translated path name of body
        String result = C_VFS_PATH_BODIES.substring(0, C_VFS_PATH_BODIES.lastIndexOf("/")) + cms.readAbsolutePath(file);
        if (!result.equals(readBodyPath(cms, (CmsFile)file))){
            result = null;
        }
        return result;
    }
    
      /**
       * This method changes the path of the body file in the xml conten file
       * if file type name is page
       *
       * @param cms The CmsObject
       * @param file The XML content file
       * @param bodypath the new XML content entry
       * @throws Exception if something goes wrong.
       */
      private void changeContent(CmsObject cms, String filename, String bodypath)
          throws CmsException {
          CmsFile file=cms.readFile(filename);
          CmsXmlControlFile hXml=new CmsXmlControlFile(cms, file);
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
       * @param path The path in the CmsFilesystem where the new page should be created.
       * @throws CmsException if something goes wrong.
       */
     private void checkFolders(CmsObject cms, String path)
          throws CmsException {

          String completePath=C_VFS_PATH_BODIES;
          StringTokenizer t=new StringTokenizer(path,"/");
          String correspFolder = "/";
          // check if all folders are there
          while (t.hasMoreTokens()) {
              String foldername=t.nextToken();
              correspFolder = correspFolder+foldername+"/";
               try {
                // try to read the folder. if this fails, an exception is thrown

                cms.readFolder(completePath+foldername+"/");
              } catch (CmsException e) {
                  // the folder could not be read, so create it.
                  String orgFolder=completePath+foldername+"/";
                  orgFolder=orgFolder.substring(C_VFS_PATH_BODIES.length()-1);
                  CmsFolder newfolder=cms.doCreateFolder(completePath,foldername);
                  //CmsFolder folder = cms.readFolder(orgFolder);
                  cms.doLockResource(cms.readAbsolutePath(newfolder),false);
                  cms.cpacc(orgFolder, cms.readAbsolutePath(newfolder));
                  // TODO: remove this later
                  //cms.doChgrp(newcms.readPath(folder),cms.readGroup(folder).getName());
                  //cms.doChmod(newcms.readPath(folder),folder.getAccessFlags());
                  //cms.doChown(newcms.readPath(folder),cms.readOwner(folder).getName());
                  try{
                    CmsFolder correspondingFolder = cms.readFolder(correspFolder);
                    if(!correspondingFolder.isLocked()){
                        cms.doUnlockResource(cms.readAbsolutePath(newfolder));
                    }
                  } catch (CmsException ex){
                    // unable to unlock folder if parent folder is locked
                  }
              }
              completePath+=foldername+"/";
          }
     }

    /**
     * Set the access flags of the copied resource to the default values.
     * @param cms The CmsObject.
     * @param filename The name of the file.
     * @throws Throws CmsException if something goes wrong.
     */
    private void setDefaultFlags(CmsObject cms, String filename)
        throws CmsException {

        Hashtable startSettings=null;
        Integer accessFlags=null;
        startSettings=(Hashtable)cms.getRequestContext().currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);
        if (startSettings != null) {
            accessFlags=(Integer)startSettings.get(C_START_ACCESSFLAGS);
        }
        if (accessFlags == null) {
            accessFlags = new Integer(C_ACCESS_DEFAULT_FLAGS);
        }
        chmod(cms, filename, accessFlags.intValue(), false);
    }

    /**
     * Changes the project-id of the resource to the new project
     * for publishing the resource directly
     *
     * @param newProjectId The Id of the new project
     * @param resourcename The name of the resource to change
     */
    public void changeLockedInProject(CmsObject cms, int newProjectId, String resourcename)
        throws CmsException{
        CmsFile file = cms.readFile(resourcename, true);
        cms.doChangeLockedInProject(newProjectId, resourcename);
        String bodyPath = checkBodyPath(cms, (CmsFile)file);
        if (bodyPath != null){
            cms.doChangeLockedInProject(newProjectId, bodyPath);
        }

        // The page file contains XML.
        // So there could be some data in the parser's cache.
        // Clear it!
        String currentProject = cms.getRequestContext().currentProject().getName();
        CmsXmlControlFile.clearFileCache(currentProject + ":" + resourcename);
    }
}
