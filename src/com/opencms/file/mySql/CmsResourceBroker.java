package com.opencms.file.mySql;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/mySql/Attic/CmsResourceBroker.java,v $
 * Date   : $Date: 2000/09/15 13:28:23 $
 * Version: $Revision: 1.40 $
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

import javax.servlet.http.*;
import java.util.*;
import java.net.*;
import java.io.*;
import source.org.apache.java.io.*;
import source.org.apache.java.util.*;

import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.template.*;


/**
 * This is THE resource broker. It merges all resource broker
 * into one public class. The interface is local to package. <B>All</B> methods
 * get additional parameters (callingUser and currentproject) to check the security-
 * police.
 * 
 * @author Andreas Schouten
 * @author Michaela Schleich
 * @author Michael Emmerich
 * @author Anders Fugmann
 * @version $Revision: 1.40 $ $Date: 2000/09/15 13:28:23 $
 */
public class CmsResourceBroker extends com.opencms.file.genericSql.CmsResourceBroker {
	
	/**
	 * Constant to count the file-system changes.
	 */
	private long m_fileSystemChanges = 0;
	
	/**
	 * Hashtable with resource-types.
	 */
	private Hashtable m_resourceTypes = null;

	
	/**
	 * The configuration of the property-file.
	 */
	private Configurations m_configuration = null;

	/**
	 * The access-module.
	 */
	private CmsDbAccess m_dbAccess = null;
	
	/**
	* The registry
	*/
	private I_CmsRegistry m_registry=null;
	
	/**
	 *  Define the caches
	*/    
	private CmsCache m_userCache=null;
	private CmsCache m_groupCache=null;
	private CmsCache m_usergroupsCache=null;
	private CmsCache m_resourceCache=null;
	private CmsCache m_subresCache = null;
	private CmsCache m_projectCache=null;
	private CmsCache m_propertyCache=null;
	private CmsCache m_propertyDefCache=null;
	private CmsCache m_propertyDefVectorCache=null;
	private String m_refresh=null;

/**
 * return the correct DbAccess class.
 * This method should be overloaded by all other Database Drivers 
 * Creation date: (09/15/00 %r)
 * @return com.opencms.file.genericSql.CmsDbAccess
 * @param configurations source.org.apache.java.util.Configurations
 * @exception com.opencms.core.CmsException Thrown if CmsDbAccess class could not be instantiated. 
 */
public com.opencms.file.genericSql.CmsDbAccess createDbAccess(Configurations configurations) throws CmsException
{
	return new com.opencms.file.mySql.CmsDbAccess(configurations);
}
/**
 * Reads the group (role) of a task from the OpenCms.
 * 
 * <B>Security:</B>
 * All users are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param task The task to read from.
 * @return The group of a resource.
 * 
 * @exception CmsException Throws CmsException if operation was not succesful.
 */
public CmsGroup readGroup(CmsUser currentUser, CmsProject currentProject, CmsTask task) throws CmsException
{
	return m_dbAccess.readGroup(task.getRole());
}
/**
 * Reads a project from the Cms.
 * 
 * <B>Security</B>
 * All users are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param task The task to read the project of.
 * 
 * @exception CmsException Throws CmsException if something goes wrong.
 */
public CmsProject readProject(CmsUser currentUser, CmsProject currentProject, CmsTask task) throws CmsException
{
	// read the parent of the task, until it has no parents.
	while (task.getParent() != 0)
	{
		task = readTask(currentUser, currentProject, task.getParent());
	}
	return m_dbAccess.readProject(task);
}
}
