package com.opencms.file;

import java.util.*;

import com.opencms.rb.*;
import com.opencms.core.*;

/**
 * The class which implements this Interface gains access to the OpenCms. 
 * <p>
 * The CmsObject encapsulates user identifaction and client request and is
 *  the central object to transport information in the Cms Servlet.
 * <p>
 * All operations on the CmsObject are forwarded to the class which implements
 * I_CmsRessourceBroker to ensures user authentification in all operations.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/03 11:57:10 $ 
 * 
 */
interface I_CmsObjectBase {	
	
	// OK
	// initialising the rb - only at start time of the CmsServlet
	public void init(I_CmsRessourceBroker ressourceBroker);
	// initialising the CmsObject - for each request
	public void init(I_CmsUser user, String host, String url, String uri);
	
	// OK
	// information about the request
	public String getUri();	
	public String getUrl();
	public String getHost();	
	public I_CmsFolder currentFolder();	
	public I_CmsFolder rootFolder();
	public I_CmsUser currentUser();	
	public I_CmsGroup userDefaultGroup();
	public I_CmsGroup userCurrentGroup();
	public I_CmsUser anonymousUser();
	public Vector getAllFileTypes();
	public  boolean isAdmin();
	
	// OK
	// file creating, reading, writing...
	// all is done widthin the online-project
	public I_CmsFile createFile(String filename, byte[] contents, I_CmsResourceType type);
	public I_CmsFile createFile(I_CmsFile folder, String filename, byte[] contents, I_CmsResourceType type);
	public I_CmsFile readFile(String filename);
	public I_CmsFile readFileHeader(String filename);
	public void writeFile(I_CmsFile file);
	public void writeFileHeader(I_CmsFile file);
	public void renameFile(I_CmsFile file, String newname);
	public void deleteFile(I_CmsFile file);	
	public void deleteFile(String filename);	
	public void copyFile(String source, String destination);	
	public void moveFile(String source, String destination);	

	// OK
	// folder creating, reading writing...
	// all is done widthin the online-project
	public I_CmsFolder createFolder(String foldername);	
	public I_CmsFolder createFolder(I_CmsFolder folder, String foldername);
	public I_CmsFolder readFolder(String foldername);
	public void deleteFolder(I_CmsFolder folder);
	public void deleteFolder(String foldername);
	public Vector getSubFolders(String foldername);
	public Vector getSubFolders(I_CmsFolder folder);	
	public Vector getFilesInFolder(String foldername);
	public Vector getFilesInFolder(I_CmsFolder folder);	
	
	// OK
	// test access to files
	public boolean accessFile(String filename);
	public boolean isReadable(String filename);	
	public boolean isWriteable(String filename);
	public boolean isViewable(String filename);
	public boolean isInternal(String filename);	
	public boolean fileExists(String filename);
	
	// ?? what means this?
    public boolean adminResource(I_CmsResource resource);
	public boolean userIsAdminOf(I_CmsResource resource);	
	public boolean userIsAdminOf(I_CmsGroup group);
	
	// OK
	public void chmod(I_CmsResource resource, int flags);
	public void chown(I_CmsResource resource, I_CmsUser newOwner);
	public void chgrp(I_CmsResource resource, I_CmsGroup newGroup);
	
	// OK
	// group and user stuff
	public Vector getUsers();
	public Vector getGroups();	
	public I_CmsUser addUser(String name, String password, I_CmsGroup group, String description, int flags);
	public void deleteUser(I_CmsUser deluser);
	public void deleteUser(String username);	
	public I_CmsUser readUser(String username);	
	public I_CmsUser readUser(String username, String password);	
	public I_CmsUser loginUser(String username, String password);
	public I_CmsGroup addGroup(String name, String description, int flags);
	public void deleteGroup(I_CmsGroup delgroup);	
	public void deleteGroup(String groupname);
	public I_CmsGroup readGroup(String groupname);	
	public void addUserToGroup(I_CmsUser user, I_CmsGroup group);	
	public void removeUserFromGroup(I_CmsUser user, I_CmsGroup group);
	public Vector getUsersOfGroup(I_CmsGroup group);
	public Vector getGroupsOfUser(I_CmsUser user);
	public boolean userInGroup(I_CmsUser user, I_CmsGroup group);
		
	// Stuff about metadef and metainfo
	// ?? whats about metadef_types?? (normal|optional|mandatory)
	public I_CmsMetaDefinition readMetaDefinition(String name, I_CmsResourceType type);
	public void writeMetaDefinition(String name, I_CmsResourceType type);
	public void deleteMetaDefinition(String name, I_CmsResourceType type);	
	public Vector getAllMetaDefinitions(I_CmsResourceType type);	
	public void updateMetaInformation(I_CmsMetaInformation metainfo);		
	public String readMetaInformationValue(String name, String meta);	
	public void writeMetaInformationValue(String name, String meta, String value);
	public void writeMetaInformation(I_CmsResource resource, I_CmsMetaInformation metainfo);	
	public Vector getAllMetaInformations(String name);
	public void deleteAllMetaInformations(I_CmsResource resource);

	// OK
	// Scheduler-stuff
	public void writeScheduleTask(I_CmsScheduleTask scheduleTask);
	public void deleteScheduleTask(I_CmsScheduleTask scheduleTask);
	public Vector readAllScheduleTasks();
}
