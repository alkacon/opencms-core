package com.opencms.workplace;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminProjectResentFiles.java,v $
 * Date   : $Date: 2000/08/08 14:08:30 $
 * Version: $Revision: 1.10 $
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
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;
import java.io.*;
import javax.servlet.http.*;

/**
 * News administration template class
 * <p>
 * Used both for displaying news administration overviews and
 * editing news.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.10 $ $Date: 2000/08/08 14:08:30 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsAdminProjectResentFiles extends CmsWorkplaceDefault implements I_CmsConstants, I_CmsFileListUsers {
		
	/**
	 * Checks if a resource should be added to the vector. If the check was true
	 * it adds the resource.
	 * 
	 * @param resources The Vector with the resources to return.
	 * @param filter The filter to check the state.
	 * @param resource The resource to check and add.
	 */
	private void addResource(Vector resources, String filter, CmsResource resource) {
		if("all".equals(filter)) {
			// add in all cases
			resources.addElement(resource);
		} else if(("new".equals(filter)) && (resource.getState() == C_STATE_NEW)) {
			// add if resource is new
			resources.addElement(resource);
		} else if(("changed".equals(filter)) && (resource.getState() == C_STATE_CHANGED)) {
			// add if resource is changed
			resources.addElement(resource);
		} else if(("deleted".equals(filter)) && (resource.getState() == C_STATE_DELETED)){
			// add if resource is deleted
			resources.addElement(resource);
		} else if(("locked".equals(filter))&& (resource.isLocked())) {
			// add if resource is locked
			resources.addElement(resource);
		}
	}
	 /** 
	  * Check if this resource should be displayed in the filelist.
	  * @param cms The CmsObject
	  * @param res The resource to be checked.
	  * @return True or false.
	  * @exception CmsException if something goes wrong.
	  */
	 private boolean checkAccess(CmsObject cms, CmsResource res)
	 throws CmsException {
		 boolean access=false;
		 int accessflags=res.getAccessFlags();
		 
		 // First check if the user may have access by one of his groups.
		 boolean groupAccess = false;
		 Enumeration allGroups = cms.getGroupsOfUser(cms.getRequestContext().currentUser().getName()).elements();
		 while((!groupAccess) && allGroups.hasMoreElements()) {
			 groupAccess = cms.readGroup(res).equals((CmsGroup)allGroups.nextElement());
		 }
		 
		 if ( ((accessflags & C_ACCESS_PUBLIC_VISIBLE) > 0) ||
			  (cms.readOwner(res).equals(cms.getRequestContext().currentUser()) && (accessflags & C_ACCESS_OWNER_VISIBLE) > 0) ||
			  (groupAccess && (accessflags & C_ACCESS_GROUP_VISIBLE) > 0) ||
			  (cms.getRequestContext().currentUser().getName().equals(C_USER_ADMIN))) {
			 access=true;
		 }
		 return access;
	 } 
	/**
	 * Gets the content of a defined section in a given template file and its subtemplates
	 * with the given parameters. 
	 * 
	 * @see getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters)
	 * @param cms CmsObject Object for accessing system resources.
	 * @param templateFile Filename of the template file.
	 * @param elementName Element name of this template in our parent template.
	 * @param parameters Hashtable with all template class parameters.
	 * @param templateSelector template section that should be processed.
	 */
	public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
		
		// get the session
		I_CmsSession session = cms.getRequestContext().getSession(true);
		
		// load the template file
		CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
		String filter = (String)parameters.get("filter");
		String action = (String)parameters.get("action");
		
		xmlTemplateDocument.setData("onload", "");
		
		if("restoreproject".equals(action)) {
			// restore the old project..
			Integer oldId = (Integer) session.getValue("oldProjectId");
			
			if(oldId != null) {
				cms.getRequestContext().setCurrentProject(oldId.intValue());
			}
			
			
			session.removeValue("oldProjectId");
			
			// redirect to the needed headfile.
			try {
				cms.getRequestContext().getResponse().sendCmsRedirect( getConfigFile(cms).getWorkplaceActionPath() + "administration_head_5.html");
			} catch(IOException exc) {
				throw new CmsException("Could not redirect to administration_head_5.html", exc);
			}
			
			//return "".getBytes();
			return null;
		}
		
		if(filter == null) {
			// this is the first time, this page is called
			filter = "all";

			if(session.getValue("oldProjectId") == null) {
				int projectId = Integer.parseInt((String)parameters.get("projectid"));
			
				session.putValue("oldProjectId", new Integer(cms.getRequestContext().currentProject().getId()));
		
				// set this project temporarly
				cms.getRequestContext().setCurrentProject(projectId);
			
				// update the head-frame
				xmlTemplateDocument.setData("onload", "window.top.body.admin_head.location.href='administration_head_4.html';");
			}
		}
		
		// store the chosen filter into the session
		session.putValue("filter", filter);

		// Finally start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
	}
	/**
	 * From interface <code>I_CmsFileListUsers</code>.
	 * <P>    
	 * Fills all customized columns with the appropriate settings for the given file 
	 * list entry. Any column filled by this method may be used in the customized template
	 * for the file list.
	 * @param cms Cms object for accessing system resources.
	 * @param filelist Template file containing the definitions for the file list together with
	 * the included customized defintions.
	 * @param res CmsResource Object of the current file list entry.
	 * @param lang Current language file.
	 * @exception CmsException if access to system resources failed.
	 * @see I_CmsFileListUsers
	 */
	public void getCustomizedColumnValues(CmsObject cms, CmsXmlWpTemplateFile filelistTemplate, CmsResource res, CmsXmlLanguageFile lang) 
		throws CmsException {
		String name = res.getName();
		String path = res.getPath();
		String link = "#";
		String servlets=((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServletPath();
		
		if( res.isFile() && ( res.getState() != C_STATE_DELETED) ) {
			link = "javascript:openwinfull('" + servlets + res.getAbsolutePath() + "', 'preview', 0, 0);";
		}		
		
		filelistTemplate.setData("LINK_VALUE", link);
		filelistTemplate.setData("SHORTNAME_VALUE", name);
		filelistTemplate.setData("PATH_VALUE", path);        
	}
   /** 
	* From interface <code>I_CmsFileListUsers</code>.
	* <P>    
	* Collects all folders and files that are displayed in the file list.
	* @param cms The CmsObject.
	* @return A vector of folder and file objects.
	* @exception Throws CmsException if something goes wrong.
	*/
	public Vector getFiles(CmsObject cms) 
		throws CmsException {
		Vector resources = new Vector();
		// get the session
		I_CmsSession session = cms.getRequestContext().getSession(true);
		
		String filter = (String) session.getValue("filter");
		if(filter == null) {
			filter = "all";
		}
		
		// get all ressources, that are changed in this project
		getFiles(cms, C_ROOT, resources, filter);
		
		return resources;
	}
	/**
	 * Get all changed files in a project.
	 * 
	 * @param cms The cms object.
	 * @param folderName The name of the folder to start.
	 * @param resources A Vector to store the result into.
	 * @param filter The filter-value, to select the files.
	 */
	private void getFiles(CmsObject cms, String folderName, Vector resources, String filter) 
		throws CmsException {
		
		CmsResource resource;
		Vector resourcesToTest;
		
		// get resources
		resourcesToTest = cms.readFileHeaders(cms.getRequestContext().currentProject().getId());
		
		//copy the values into the resources, if they where changed
		for (int i=0;i<resourcesToTest.size();i++) {
			resource = (CmsResource)resourcesToTest.elementAt(i);
			if(resource.getState() != C_STATE_UNCHANGED) {
				addResource(resources, filter, resource);
			}
		}
	}
	/**
	 * Indicates if the results of this class are cacheable.
	 * 
	 * @param cms CmsObject Object for accessing system resources
	 * @param templateFile Filename of the template file 
	 * @param elementName Element name of this template in our parent template.
	 * @param parameters Hashtable with all template class parameters.
	 * @param templateSelector template section that should be processed.
	 * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
	 */
	public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
		return false;
	}
	/**
	 * From interface <code>I_CmsFileListUsers</code>.
	 * <P>    
	 * Used to modify the bit pattern for hiding and showing columns in
	 * the file list.
	 * @param cms Cms object for accessing system resources.
	 * @param prefs Old bit pattern.
	 * @return New modified bit pattern.
	 * @see I_CmsFileListUsers
	 */
	public int modifyDisplayedColumns(CmsObject cms, int prefs) {
		prefs = ((prefs & C_FILELIST_NAME) == 0) ? prefs : (prefs - C_FILELIST_NAME);
		prefs = ((prefs & C_FILELIST_TITLE) == 0) ? prefs : (prefs - C_FILELIST_TITLE);
		return prefs;
	}
}
