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
interface I_CmsObjectAdvanced extends I_CmsObjectBase {	

	// OK
	// something to handle projects (versions of resources)
	public I_CmsProject readProject(String name);
	public I_CmsProject readOnlineProject();
	public I_CmsProject createProject(String name, String description, I_CmsUser user, I_CmsGroup group);
	public I_CmsProject writeProject(I_CmsProject project);
	
	// OK
	// file creating, reading, writing...
	public I_CmsFile createFile(I_CmsProject project, String filename, byte[] contents, I_CmsResourceType type);
	public I_CmsFile createFile(I_CmsProject project, I_CmsFile folder, String filename, byte[] contents, I_CmsResourceType type);
	public I_CmsFile readFile(I_CmsProject project, String filename);
	public I_CmsFile readFileHeader(I_CmsProject project, String filename);
	public void writeFile(I_CmsProject project, I_CmsFile file);
	public void writeFileHeader(I_CmsProject project, I_CmsFile file);
	public void renameFile(I_CmsProject project, I_CmsFile file, String newname);
	public void deleteFile(I_CmsProject project, I_CmsFile file);	
	public void deleteFile(I_CmsProject project, String filename);	
	public void copyFile(I_CmsProject project, String source, String destination);	
	public void moveFile(I_CmsProject project, String source, String destination);	

	// OK
	// folder creating, reading writing...
	// all is done widthin the online-project
	public I_CmsFolder createFolder(I_CmsProject project, String foldername);	
	public I_CmsFolder createFolder(I_CmsProject project, I_CmsFolder folder, String foldername);
	public I_CmsFolder readFolder(I_CmsProject project, String foldername);
	public void deleteFolder(I_CmsProject project, I_CmsFolder folder);
	public void deleteFolder(I_CmsProject project, String foldername);
	public Vector getSubFolders(I_CmsProject project, String foldername);
	public Vector getSubFolders(I_CmsProject project, I_CmsFolder folder);	
	public Vector getFilesInFolder(I_CmsProject project, String foldername);
	public Vector getFilesInFolder(I_CmsProject project, I_CmsFolder folder);	
	
	// OK
	// test access to files
	public boolean accessFile(I_CmsProject project, String filename);
	public boolean isReadable(I_CmsProject project, String filename);	
	public boolean isWriteable(I_CmsProject project, String filename);
	public boolean isViewable(I_CmsProject project, String filename);
	public boolean isInternal(I_CmsProject project, String filename);	
	public boolean fileExists(I_CmsProject project, String filename);
	
	// ?? what means this?
    public boolean adminResource(I_CmsProject project, I_CmsResource resource);
	public boolean userIsAdminOf(I_CmsProject project, I_CmsResource resource);	

	// OK
	public void chmod(I_CmsProject project, I_CmsResource resource, int flags);
	public void chown(I_CmsProject project, I_CmsResource resource, I_CmsUser newOwner);
	public void chgrp(I_CmsProject project, I_CmsResource resource, I_CmsGroup newGroup);
	
		
	// Stuff about metadef and metainfo
	// ?? whats about metadef_types?? (normal|optional|mandatory)
	public void updateMetaInformation(I_CmsProject project, I_CmsMetaInformation metainfo);		
	public String readMetaInformationValue(I_CmsProject project, String name, String meta);	
	public void writeMetaInformationValue(I_CmsProject project, String name, String meta, String value);
	public void writeMetaInformation(I_CmsProject project, I_CmsResource resource, I_CmsMetaInformation metainfo);	
	public Vector getAllMetaInformations(I_CmsProject project, String resourceName);
	public void deleteAllMetaInformations(I_CmsProject project, I_CmsResource resource);
}
