/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsInitMySqlFillDefaults.java,v $
 * Date   : $Date: 2000/06/05 13:37:54 $
 * Version: $Revision: 1.22 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This class helps to fill the cms with some default database-values like
 * anonymous user.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @version $Revision: 1.22 $ $Date: 2000/06/05 13:37:54 $
 */
public class CmsInitMySqlFillDefaults extends A_CmsInit implements I_CmsConstants {
	
	/**
	 * The init - Method fills in this case the database with some initial values.
	 * 
	 * @param propertyDriver The driver-classname of the jdbc-driver.
	 * @param propertyConnectString the conectionstring to the database 
	 * for the propertys.
	 * 
	 * @return The resource-borker, this resource-broker has acces to the
	 * network of created classes.
	 */
	public I_CmsResourceBroker init( String propertyDriver, 
									 String propertyConnectString )
		throws Exception {
		
			// TODO: write the SQLDriver and the connectString to the propertys

		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsInitMySqlFillDefaults] initialized rb-network and filling in some defaults...");
		}
		
		I_CmsRbUserGroup userRb = new CmsRbUserGroup( 
			new CmsAccessUserGroup(
				new CmsAccessUserMySql(propertyDriver, propertyConnectString),
				new CmsAccessUserInfoMySql(propertyDriver, propertyConnectString),
				new CmsAccessGroupMySql(propertyDriver, propertyConnectString)));
			
		I_CmsRbTask taskRb = new CmsRbTask( 
			new CmsAccessTask(propertyDriver, propertyConnectString ) );

		userRb.addGroup(C_GROUP_GUEST, "the guest-group", C_FLAG_ENABLED, null);
		CmsGroup adminGroup = userRb.addGroup(C_GROUP_ADMIN, "the admin-group", 
												C_FLAG_ENABLED|C_FLAG_GROUP_PROJECTMANAGER, null);
		userRb.addGroup(C_GROUP_PROJECTLEADER, "the projectmanager-group", 
						C_FLAG_ENABLED|C_FLAG_GROUP_PROJECTMANAGER|C_FLAG_GROUP_PROJECTCOWORKER|C_FLAG_GROUP_ROLE,
						null);
		userRb.addGroup(C_GROUP_USERS, "the users-group to access the workplace", 
						C_FLAG_ENABLED|C_FLAG_GROUP_ROLE|C_FLAG_GROUP_PROJECTCOWORKER, C_GROUP_GUEST);
			
		userRb.addUser(C_USER_GUEST, "", C_GROUP_GUEST, "the guest-user", 
					   new Hashtable(), C_FLAG_ENABLED);
		CmsUser admin = userRb.addUser(C_USER_ADMIN, "admin", C_GROUP_ADMIN, 
										 "the admin-user", new Hashtable(), 
										 C_FLAG_ENABLED);
			
		I_CmsRbProject projectRb = new CmsRbProject(
			new CmsAccessProjectMySql(propertyDriver, propertyConnectString));
			
		CmsTask task = taskRb.createProject(admin, C_PROJECT_ONLINE, adminGroup, 
											  new java.sql.Timestamp(new Date().getTime()), 
											  C_TASK_PRIORITY_NORMAL);

		CmsProject project = projectRb.createProject(C_PROJECT_ONLINE_ID, C_PROJECT_ONLINE, "the online-project", task,
													   userRb.readUser(C_USER_ADMIN), 
													   userRb.readGroup(C_GROUP_GUEST), 
													   userRb.readGroup(C_GROUP_PROJECTLEADER),
													   C_FLAG_ENABLED);
			
			
		I_CmsRbSystemProperty propertyRb = new CmsRbSystemProperty(
			new CmsAccessSystemPropertyMySql(propertyDriver, propertyConnectString));

		// the resourceType "folder" is needed always - so adding it
		Hashtable resourceTypes = new Hashtable(1);
		resourceTypes.put(C_TYPE_FOLDER_NAME, new CmsResourceType(C_TYPE_FOLDER, 0, 
																  C_TYPE_FOLDER_NAME, ""));
			
		// sets the last used index of resource types.
		resourceTypes.put(C_TYPE_LAST_INDEX, new Integer(C_TYPE_FOLDER));
			
        // add the mime-types to the database
		propertyRb.addProperty( C_SYSTEMPROPERTY_RESOURCE_TYPE, resourceTypes );
            
            
        // set the mimetypes
        propertyRb.addProperty(C_SYSTEMPROPERTY_MIMETYPES,initMimetypes());
			
		// create the root-mountpoint
		Hashtable mount = new Hashtable(1);
		mount.put("/", new CmsMountPoint("/", propertyDriver, 
										 propertyConnectString,
										 "The root-mountpoint"));
			
		propertyRb.addProperty( C_SYSTEMPROPERTY_MOUNTPOINT, mount );

		// read all mountpoints from the properties.
		CmsMountPoint mountPoint;
		Hashtable mountedAccessModules = new Hashtable();
		Enumeration keys = mount.keys();
		Object key;
			
		// walk throug all mount-points.
		while(keys.hasMoreElements()) {
			key = keys.nextElement();
			mountPoint = (CmsMountPoint) mount.get(key);
					
			// select the right access-module for the mount-point
			if( mountPoint.getMountpointType() == C_MOUNTPOINT_MYSQL ) {
				mountedAccessModules.put(key, new CmsAccessFileMySql(mountPoint));
			} else {
				mountedAccessModules.put(key, new CmsAccessFileFilesystem(mountPoint));
			}
		}
		I_CmsAccessFile accessFile = new CmsAccessFile(mountedAccessModules);
			
		// create the root-folder
		accessFile.createFolder(admin, project, C_ROOT, 0);
									
		I_CmsRbFile fileRb = new CmsRbFile(accessFile);

		I_CmsRbMetadefinition metadefinitionRb = 
			new CmsRbMetadefinition(
				new CmsAccessMetadefinitionMySql(propertyDriver, propertyConnectString));
			
       
            
            
		if(A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsInitMySqlFillDefaults] rb-network initialized");
		}
		
		return new CmsResourceBroker(userRb, fileRb, metadefinitionRb, 
									 propertyRb, projectRb, taskRb);
	}
    
    /**
	 * Inits all mimetypes.
	 * The mimetype-data should be stored in the database. But now this data
	 * is putted directly here.
	 * 
	 * @return Returns a hashtable with all mimetypes.
	 */
	private Hashtable initMimetypes() {
		Hashtable mt=new Hashtable();
		mt.put( "ez", "application/andrew-inset" );
		mt.put( "hqx", "application/mac-binhex40" );
		mt.put( "cpt", "application/mac-compactpro" );
		mt.put( "doc", "application/msword" );
		mt.put( "bin", "application/octet-stream" );
		mt.put( "dms", "application/octet-stream" );
		mt.put( "lha", "application/octet-stream" );
		mt.put( "lzh", "application/octet-stream" );
		mt.put( "exe", "application/octet-stream" );
		mt.put( "class", "application/octet-stream" );
		mt.put( "oda", "application/oda" );
		mt.put( "pdf", "application/pdf" );
		mt.put( "ai", "application/postscript" );
		mt.put( "eps", "application/postscript" );
		mt.put( "ps", "application/postscript" );
		mt.put( "rtf", "application/rtf" );
		mt.put( "smi", "application/smil" );
		mt.put( "smil", "application/smil" );
		mt.put( "mif", "application/vnd.mif" );
		mt.put( "xls", "application/vnd.ms-excel" );
		mt.put( "ppt", "application/vnd.ms-powerpoint" );
		mt.put( "bcpio", "application/x-bcpio" );
		mt.put( "vcd", "application/x-cdlink" );
		mt.put( "pgn", "application/x-chess-pgn" );
		mt.put( "cpio", "application/x-cpio" );
		mt.put( "csh", "application/x-csh" );
		mt.put( "dcr", "application/x-director" );
		mt.put( "dir", "application/x-director" );
		mt.put( "dxr", "application/x-director" );
		mt.put( "dvi", "application/x-dvi" );
		mt.put( "spl", "application/x-futuresplash" );
		mt.put( "gtar", "application/x-gtar" );
		mt.put( "hdf", "application/x-hdf" );
		mt.put( "js", "application/x-javascript" );
		mt.put( "skp", "application/x-koan" );
		mt.put( "skd", "application/x-koan" );
		mt.put( "skt", "application/x-koan" );
		mt.put( "skm", "application/x-koan" );
		mt.put( "latex", "application/x-latex" );
		mt.put( "nc", "application/x-netcdf" );
		mt.put( "cdf", "application/x-netcdf" );
		mt.put( "sh", "application/x-sh" );
		mt.put( "shar", "application/x-shar" );
		mt.put( "swf", "application/x-shockwave-flash" );
		mt.put( "sit", "application/x-stuffit" );
		mt.put( "sv4cpio", "application/x-sv4cpio" );
		mt.put( "sv4crc", "application/x-sv4crc" );
		mt.put( "tar", "application/x-tar" );
		mt.put( "tcl", "application/x-tcl" );
		mt.put( "tex", "application/x-tex" );
		mt.put( "texinfo", "application/x-texinfo" );
		mt.put( "texi", "application/x-texinfo" );
		mt.put( "t", "application/x-troff" );
		mt.put( "tr", "application/x-troff" );
		mt.put( "roff", "application/x-troff" );
		mt.put( "man", "application/x-troff-man" );
		mt.put( "me", "application/x-troff-me" );
		mt.put( "ms", "application/x-troff-ms" );
		mt.put( "ustar", "application/x-ustar" );
		mt.put( "src", "application/x-wais-source" );
		mt.put( "zip", "application/zip" );
		mt.put( "au", "audio/basic" );
		mt.put( "snd", "audio/basic" );
		mt.put( "mid", "audio/midi" );
		mt.put( "midi", "audio/midi" );
		mt.put( "kar", "audio/midi" );
		mt.put( "mpga", "audio/mpeg" );
		mt.put( "mp2", "audio/mpeg" );
		mt.put( "mp3", "audio/mpeg" );
		mt.put( "aif", "audio/x-aiff" );
		mt.put( "aiff", "audio/x-aiff" );
		mt.put( "aifc", "audio/x-aiff" );
		mt.put( "ram", "audio/x-pn-realaudio" );
		mt.put( "rm", "audio/x-pn-realaudio" );
		mt.put( "rpm", "audio/x-pn-realaudio-plugin" );
		mt.put( "ra", "audio/x-realaudio" );
		mt.put( "wav", "audio/x-wav" );
		mt.put( "pdb", "chemical/x-pdb" );
		mt.put( "xyz", "chemical/x-pdb" );
		mt.put( "bmp", "image/bmp" );
		mt.put( "wbmp", "image/vnd.wap.wbmp" );
		mt.put( "gif", "image/gif" );
		mt.put( "ief", "image/ief" );
		mt.put( "jpeg", "image/jpeg" );
		mt.put( "jpg", "image/jpeg" );
		mt.put( "jpe", "image/jpeg" );
		mt.put( "png", "image/png" );
		mt.put( "tiff", "image/tiff" );
		mt.put( "tif", "image/tiff" );
		mt.put( "ras", "image/x-cmu-raster" );
		mt.put( "pnm", "image/x-portable-anymap" );
		mt.put( "pbm", "image/x-portable-bitmap" );
		mt.put( "pgm", "image/x-portable-graymap" );
		mt.put( "ppm", "image/x-portable-pixmap" );
		mt.put( "rgb", "image/x-rgb" );
		mt.put( "xbm", "image/x-xbitmap" );
		mt.put( "xpm", "image/x-xpixmap" );
		mt.put( "xwd", "image/x-xwindowdump" );
		mt.put( "igs", "model/iges" );
		mt.put( "iges", "model/iges" );
		mt.put( "msh", "model/mesh" );
		mt.put( "mesh", "model/mesh" );
		mt.put( "silo", "model/mesh" );
		mt.put( "wrl", "model/vrml" );
		mt.put( "vrml", "model/vrml" );
		mt.put( "css", "text/css" );
		mt.put( "html", "text/html" );
		mt.put( "htm", "text/html" );
		mt.put( "asc", "text/plain" );
		mt.put( "txt", "text/plain" );
		mt.put( "rtx", "text/richtext" );
		mt.put( "rtf", "text/rtf" );
		mt.put( "sgml", "text/sgml" );
		mt.put( "sgm", "text/sgml" );
		mt.put( "tsv", "text/tab-separated-values" );
		mt.put( "etx", "text/x-setext" );
		mt.put( "xml", "text/xml" );
		mt.put( "wml", "text/vnd.wap.wml" );
		mt.put( "mpeg", "video/mpeg" );
		mt.put( "mpg", "video/mpeg" );
		mt.put( "mpe", "video/mpeg" );
		mt.put( "qt", "video/quicktime" );
		mt.put( "mov", "video/quicktime" );
		mt.put( "avi", "video/x-msvideo" );
		mt.put( "movie", "video/x-sgi-movie" );
		mt.put( "ice", "x-conference/x-cooltalk" );
        return mt;
    }
}
