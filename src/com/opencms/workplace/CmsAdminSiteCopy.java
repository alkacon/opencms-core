package com.opencms.workplace;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminSiteCopy.java,v $
 * Date   : $Date: 2000/10/16 12:59:34 $
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

import javax.servlet.http.*;

 /**
 * Template class for displaying a sitematrix
 * Creation date: (09/22/00 13:32:48)
 * @author: Finn Nielsen
 */
public class CmsAdminSiteCopy extends CmsWorkplaceDefault {
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
public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException
{
	CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
	I_CmsSession session = cms.getRequestContext().getSession(true);
	
	//
	if (parameters.get("initial") != null)
		session.removeValue("SITECOPY_PROJECT_ID");

	//
	if (parameters.get("submitform") != null && parameters.get("submitform").equals("true"))
	{
		int projectId = Integer.parseInt((String) parameters.get("project"));

		if (projectId == cms.getRequestContext().currentProject().getId() || cms.getRequestContext().currentProject().getId() == cms.onlineProject().getId())
		{
			templateSelector = "error";
			xmlTemplateDocument.setData("details", "");
			return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
		}
		session.putValue("SITECOPY_PROJECT_ID", ""+projectId);
		templateSelector = "wait";
	}

	//
	if (parameters.get("action") != null && parameters.get("action").equals("start"))
	{
		try
		{
			cms.copyProjectToProject(cms.readProject(Integer.parseInt((String) session.getValue("SITECOPY_PROJECT_ID"))));
			session.removeValue("SITECOPY_PROJECT_ID");
			templateSelector = "done";
		}
		catch (CmsException e)
		{
			xmlTemplateDocument.setData("details", Utils.getStackTrace(e));
			templateSelector = "error";
		}
	}

	//
	return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
}
/**
 * Gets all projects of the currently logged in user.
 * <P>
 * The given vectors <code>names</code> and <code>values</code> will 
 * be filled with the appropriate information to be used for building
 * a select box.
 * <P>
 * Both <code>names</code> and <code>values</code> will contain
 * the project names after returning from this method.
 * <P>
 * 
 * @param cms CmsObject Object for accessing system resources.
 * @param lang reference to the currently valid language file
 * @param names Vector to be filled with the appropriate values in this method.
 * @param values Vector to be filled with the appropriate values in this method.
 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
 * @return Index representing the user's current project in the vectors.
 * @exception CmsException
 */
public Integer getProjects(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws CmsException
{
	// Get all project information
	CmsRequestContext reqCont = cms.getRequestContext();
	CmsProject currentProject = reqCont.currentProject();
	String newSite = (String) parameters.get("site");
	int currentSiteId = cms.getSite(cms.onlineProject().getId()).getId();
	int currentProjectId = currentProject.getId();
	Vector allProjects = null;
	// Check if the user requested a site change
	try
	{
		if (newSite != null && !("".equals(newSite)) && currentSiteId != Integer.parseInt(newSite))
			reqCont.setCurrentProject(cms.getSiteBySiteId(Integer.parseInt(newSite)).getOnlineProjectId());

		// Get all project for the site choosen
		allProjects = cms.getAllAccessibleProjects();
	}
	finally
	{
		// set back current project
		reqCont.setCurrentProject(currentProjectId);
	}

	// Now loop through all projects and fill the result vectors
	for (int i = 0; i < allProjects.size(); i++)
	{
		CmsProject project = (CmsProject) allProjects.elementAt(i);
		if (currentProjectId != project.getId())
		{
			values.addElement("" + project.getId());
			names.addElement(project.getName());
		}
	}
	return new Integer(0);
}
/**
 * Gets all sites
 * <P>
 * The given vectors <code>names</code> and <code>values</code> will 
 * be filled with the appropriate information to be used for building
 * a select box.
 * <P>
 * Both <code>names</code> and <code>values</code> will contain
 * the site names after returning from this method.
 * <P>
 * 
 * @param cms CmsObject Object for accessing system resources.
 * @param lang reference to the currently valid language file
 * @param names Vector to be filled with the appropriate values in this method.
 * @param values Vector to be filled with the appropriate values in this method.
 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
 * @return Index representing the user's current project in the vectors.
 * @exception CmsException
 */
public Integer getSites(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws CmsException
{
	// Get all project information
	CmsRequestContext reqCont = cms.getRequestContext();
	Vector allSites = cms.getAllSites();
	String sitechosen = (String)parameters.get("site");
	int currentSite = (sitechosen == null) ? cms.getSite(cms.onlineProject().getId()).getId() : Integer.parseInt(sitechosen);

	// Now loop through all sites and fill the result vectors
	int currentSiteNum = 0;

	//
	for (int i = 0; i < allSites.size(); i++)
	{
		CmsSite site = (CmsSite) allSites.elementAt(i);
		values.addElement(""+site.getId());
		names.addElement(site.getName());
		if (currentSite == site.getId())
			currentSiteNum = i;
	}
	return new Integer(currentSiteNum);
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
public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector)
{
	return false;
}
}
