/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/I_CmsRbMetadefinition.java,v $
 * Date   : $Date: 2000/06/05 13:37:56 $
 * Version: $Revision: 1.11 $
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
 * This public class describes a resource broker for metadefinitions in 
 * the Cms.<BR/>
 * <B>All</B> Methods get a first parameter: CmsUser. It is the current user. This 
 * is for security-reasons, to check if this current user has the rights to call the
 * method.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.11 $ $Date: 2000/06/05 13:37:56 $
 */
interface I_CmsRbMetadefinition {
	
	/**
	 * Reads a metadefinition for the given resource type.
	 * 
	 * @param name The name of the metadefinition to read.
	 * @param type The resource type for which the metadefinition is valid.
	 * 
	 * @return metadefinition The metadefinition that corresponds to the overgiven
	 * arguments - or null if there is no valid metadefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsPropertydefinition readMetadefinition(String name, CmsResourceType type)
		throws CmsException;
	
	/**
	 * Reads a metadefinition for the given resource type.
	 * 
	 * @param name The name of the metadefinition to read.
	 * @param type The resource type for which the metadefinition is valid.
	 * 
	 * @return metadefinition The metadefinition that corresponds to the overgiven
	 * arguments - or null if there is no valid metadefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsPropertydefinition readMetadefinition(String name, int type)
		throws CmsException;
							
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param resourcetype The resource type to read the metadefinitions for.
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllMetadefinitions(CmsResourceType resourcetype)
		throws CmsException;
	
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param resourcetype The resource type to read the metadefinitions for.
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllMetadefinitions(int resourcetype)
		throws CmsException;
	
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param resourcetype The resource type to read the metadefinitions for.
	 * @param type The type of the metadefinition (normal|mandatory|optional).
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllMetadefinitions(CmsResourceType resourcetype, int type)
		throws CmsException;

	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param resourcetype The resource type to read the metadefinitions for.
	 * @param type The type of the metadefinition (normal|mandatory|optional).
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllMetadefinitions(int resourcetype, int type)
		throws CmsException;
	
	/**
	 * Creates the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param name The name of the metadefinition to overwrite.
	 * @param resourcetype The resource-type for the metadefinition.
	 * @param type The type of the metadefinition (normal|mandatory|optional)
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsPropertydefinition createMetadefinition(String name, 
													CmsResourceType resourcetype, 
													int type)
		throws CmsException;
		
	/**
	 * Delete the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param metadef The metadef to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void deleteMetadefinition(CmsPropertydefinition metadef)
		throws CmsException;
	
	/**
	 * Updates the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param metadef The metadef to be deleted.
	 * 
	 * @return The metadefinition, that was written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsPropertydefinition writeMetadefinition(CmsPropertydefinition metadef)
		throws CmsException;
	
	// now the stuff for metainformations

	/**
	 * Returns a Metainformation of a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be read.
	 * 
	 * @return metainfo The metainfo as string.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public String readMetainformation(CmsResource resource, String meta)
		throws CmsException;	

	/**
	 * Returns a Metainformation of a file or folder.
	 * 
	 * @param meta The Metadefinition-name of which the Metainformation has to be read.
	 * @param projectId The id of the project.
	 * @param path The path of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @return metainfo The metainfo as string or null if the metainfo not exists.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public String readMetainformation(String meta, int projectId, String path, 
									  int resourceType)
		throws CmsException;
	
	/**
	 * Writes a Metainformation for a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be set.
	 * @param value The value for the metainfo to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetainformation(CmsResource resource, String meta,
											  String value)
		throws CmsException;

	/**
	 * Writes a Metainformation for a file or folder.
	 * 
	 * @param meta The Metadefinition-name of which the Metainformation has to be read.
	 * @param value The value for the metainfo to be set.
	 * @param projectId The id of the project.
	 * @param path The path of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetainformation(String meta, String value, int projectId, 
									 String path, int resourceType)
		throws CmsException;
	
	/**
	 * Writes a couple of Metainformation for a file or folder.
	 * 
	 * @param metainfos A Hashtable with Metadefinition- metainfo-pairs as strings.
	 * @param projectId The id of the project.
	 * @param path The path of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetainformations(Hashtable metainfos, int projectId, 
									  String path, int resourceType)
		throws CmsException;
	
	/**
	 * Writes a couple of Metainformation for a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * @param metainfos A Hashtable with Metadefinition- metainfo-pairs as strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetainformations(CmsResource resource, Hashtable metainfos)
		throws CmsException;

	/**
	 * Returns a list of all Metainformations of a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * 
	 * @return Vector of Metainformation as Strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Hashtable readAllMetainformations(CmsResource resource)
		throws CmsException;
	
	/**
	 * Returns a list of all Metainformations of a file or folder.
	 * 
	 * @param projectId The id of the project.
	 * @param path The path of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @return Vector of Metainformation as Strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Hashtable readAllMetainformations(int projectId, String path, 
											 int resourceType)
		throws CmsException;
	
	/**
	 * Deletes all Metainformation for a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteAllMetainformations(CmsResource resource)
		throws CmsException;

	/**
	 * Deletes all Metainformation for a file or folder.
	 * 
	 * @param projectId The id of the project.
	 * @param path The path of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteAllMetainformations(int projectId, String path)
		throws CmsException;
	
	/**
	 * Deletes a Metainformation for a file or folder.
	 * 
	 * @param resource The resource of which the Metainformation has to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteMetainformation(CmsResource resource, String meta)
		throws CmsException;     	

	/**
	 * Deletes a Metainformation for a file or folder.
	 * 
	 * @param meta The Metadefinition-name of which the Metainformation has to be read.
	 * @param projectId The id of the project.
	 * @param path The path of the resource.
	 * @param resourceType The Type of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteMetainformation(String meta, int projectId, String path, 
									  int resourceType)
		throws CmsException;

	/**
	 * Deletes all Metainformations for a project.
	 * 
	 * @param project The project to delete.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteProject(CmsProject project)
		throws CmsException;
}