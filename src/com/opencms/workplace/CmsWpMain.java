package com.opencms.workplace;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsWpMain.java,v $
 * Date   : $Date: 2000/10/04 13:47:18 $
 * Version: $Revision: 1.26 $
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
 * Template class for displaying OpenCms workplace main screen.
 * <P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Alexander Lucas
 * @author Michael Emmerich
 * @version $Revision: 1.26 $ $Date: 2000/10/04 13:47:18 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsWpMain extends CmsWorkplaceDefault {

	private Vector m_viewNames = null;
	private Vector m_viewLinks = null;
	
	
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
		if(C_DEBUG && A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] getting content of element " + ((elementName==null)?"<root>":elementName));
			A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] template file is: " + templateFile);
			A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
		}

		I_CmsSession session = cms.getRequestContext().getSession(true);
		CmsRequestContext reqCont = cms.getRequestContext();
		String newGroup = (String)parameters.get("group");
		String newSite = (String)parameters.get("site");
		String newProject = (String)parameters.get("project");
		String newView = (String)parameters.get(C_PARA_VIEW);
		CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
		

		// Check if the user requested a group change
		if(newGroup != null && !("".equals(newGroup))) {
			if(!(newGroup.equals(reqCont.currentGroup().getName()))) {
				reqCont.setCurrentGroup(newGroup);
			}
		}                            

		// Check if the user requested a project change
		if(newProject != null && !("".equals(newProject))) {
			if(!(newProject.equals(reqCont.currentProject().getName()))) {
				reqCont.setCurrentProject(Integer.parseInt(newProject));
			}
		}
		
		// Check if the user requested a site change
		int currentSite = cms.getSite(cms.onlineProject().getId()).getId();
		if(newSite != null && !("".equals(newSite)) && !newSite.equals(""+currentSite)) {
			reqCont.setCurrentProject(cms.getSiteBySiteId(Integer.parseInt(newSite)).getOnlineProjectId());
		}
		
		// Check if the user requested a new view
		if(newView != null && !("".equals(newView))) {
			session = cms.getRequestContext().getSession(true);
			session.putValue(C_PARA_VIEW, newView);
		}
		
		// Now load the template file and start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
	}
	/**
	 * Gets all groups of the currently logged in user.
	 * <P>
	 * The given vectors <code>names</code> and <code>values</code> will 
	 * be filled with the appropriate information to be used for building
	 * a select box.
	 * <P>
	 * Both <code>names</code> and <code>values</code> will contain
	 * the group names after returning from this method.
	 * <P>
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param lang reference to the currently valid language file
	 * @param names Vector to be filled with the appropriate values in this method.
	 * @param values Vector to be filled with the appropriate values in this method.
	 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
	 * @return Index representing the user's current group in the vectors.
	 * @exception CmsException
	 */
	public Integer getGroups(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
			throws CmsException {

		// Get a vector of all of the user's groups by asking the request context
		CmsRequestContext reqCont = cms.getRequestContext();
		CmsGroup currentGroup = reqCont.currentGroup();
		Vector allGroups = cms.getGroupsOfUser(reqCont.currentUser().getName());
		
		// Now loop through all groups and fill the result vectors
		int numGroups = allGroups.size();
		int currentGroupNum = 0;
		for(int i=0; i<numGroups; i++) {
			CmsGroup loopGroup = (CmsGroup)allGroups.elementAt(i);
			String loopGroupName = loopGroup.getName();
			values.addElement(loopGroupName);
			names.addElement(loopGroupName);
			if(loopGroup.equals(currentGroup)) {
				// Fine. The group of this loop is the user's current group. Save it!
				currentGroupNum = i;
			}
		}
		return new Integer(currentGroupNum);
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
	public Integer getProjects(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
			throws CmsException {
		// Get all project information
		CmsRequestContext reqCont = cms.getRequestContext();
		String currentProject = null;
		Vector allProjects = cms.getAllAccessibleProjects();
		 
		currentProject = reqCont.currentProject().getName();
			   
		// Now loop through all projects and fill the result vectors
		int numProjects = allProjects.size();
		int currentProjectNum = 0;
		for(int i=0; i<numProjects; i++) {
			CmsProject loopProject = (CmsProject)allProjects.elementAt(i);
			String loopProjectName = loopProject.getName();
			String loopProjectId = loopProject.getId() + "";
			values.addElement(loopProjectId);
			names.addElement(loopProjectName);

			if(loopProjectName.equals(currentProject)) {
				// Fine. The project of this loop is the user's current project. Save it!
				currentProjectNum = i;
			}
		}

		return new Integer(currentProjectNum);
	}
	/**
	 * Gets all views available for this user in the workplace screen from the Registry.
	 * <P>
	 * The given vectors <code>names</code> and <code>values</code> will 
	 * be filled with the appropriate information to be used for building
	 * a select box.
	 * <P>
	 * <code>names</code> will contain language specific view descriptions
	 * and <code>values</code> will contain the correspondig URL for each
	 * of these views after returning from this method.
	 * <P>
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param lang reference to the currently valid language file
	 * @param names Vector to be filled with the appropriate values in this method.
	 * @param values Vector to be filled with the appropriate values in this method.
	 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
	 * @return Index representing the user's current workplace view in the vectors.
	 * @exception CmsException
	 */
	public Integer getRegViews(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
			throws CmsException {
		
		// Let's see if we have a session
		CmsRequestContext reqCont = cms.getRequestContext();
		I_CmsSession session = cms.getRequestContext().getSession(true);

		// try to get an existing view
		String currentView = null;
		Hashtable startSettings=null;
		// check out the user infor1ation if a default view is stored there.
		startSettings=(Hashtable)reqCont.currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);                    
		if (startSettings != null) {
			currentView = (String)startSettings.get(C_START_VIEW);
		}
		// If there is a session, let's see if it has a view stored
		if(session != null) {
			 if (session.getValue(C_PARA_VIEW) != null) {
				currentView = (String)session.getValue(C_PARA_VIEW);
			}    
		}
  
		if (currentView == null) {
			currentView="";
		}

		Vector viewNames = new Vector();
		Vector viewLinks = new Vector();
		// get the List of available views from the Registry
		int numViews = (cms.getRegistry()).getViews(viewNames,viewLinks);
		int currentViewIndex = 0;
		// Loop through the vectors and fill the resultvectors
		for(int i=0; i<numViews; i++)
		{
			String loopName = (String)viewNames.elementAt(i);	
			String loopLink = (String)viewLinks.elementAt(i);	
			boolean visible = true;
			try{
				cms.readFileHeader(loopLink);
			}catch(CmsException e){
				visible = false;
			}
			if (visible)
			{
				if(loopLink.equals(currentView))
				{
					currentViewIndex = values.size();
				}	 
				names.addElement(lang.getLanguageValue(loopName));
				values.addElement(loopLink);
			}
		}	
				
		return new Integer(currentViewIndex);
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
	int currentSite = cms.getSite(cms.onlineProject().getId()).getId();

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
	 * Gets the currently logged in user.
	 * <P>
	 * Used for displaying information in the 'foot' frame of the workplace.
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param tagcontent Additional parameter passed to the method <em>(not used here)</em>.
	 * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document <em>(not used here)</em>.  
	 * @param userObj Hashtable with parameters <em>(not used here)</em>.
	 * @return String containing the current user.
	 * @exception CmsException
	 */
	public Object getUser(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
			throws CmsException {
		CmsRequestContext reqContext = cms.getRequestContext();
		CmsUser currentUser = reqContext.currentUser();
		return currentUser.getName();
	}
	/**
	 * Gets all views available in the workplace screen.
	 * <P>
	 * The given vectors <code>names</code> and <code>values</code> will 
	 * be filled with the appropriate information to be used for building
	 * a select box.
	 * <P>
	 * <code>names</code> will contain language specific view descriptions
	 * and <code>values</code> will contain the correspondig URL for each
	 * of these views after returning from this method.
	 * <P>
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param lang reference to the currently valid language file
	 * @param names Vector to be filled with the appropriate values in this method.
	 * @param values Vector to be filled with the appropriate values in this method.
	 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
	 * @return Index representing the user's current workplace view in the vectors.
	 * @exception CmsException
	 */
	public Integer getViews(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
			throws CmsException {
		
		// Let's see if we have a session
		CmsRequestContext reqCont = cms.getRequestContext();
		I_CmsSession session = cms.getRequestContext().getSession(true);

		// try to get an existing view
		String currentView = null;
		Hashtable startSettings=null;
		// check out the user infor1ation if a default view is stored there.
		startSettings=(Hashtable)reqCont.currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);                    
		if (startSettings != null) {
			currentView = (String)startSettings.get(C_START_VIEW);
		}
		// If there is a session, let's see if it has a view stored
		if(session != null) {
			 if (session.getValue(C_PARA_VIEW) != null) {
				currentView = (String)session.getValue(C_PARA_VIEW);
			}    
		}
  
		if (currentView == null) {
			currentView="";
		}
	
		 // Check if the list of available views is not yet loaded from the workplace.ini
		if(m_viewNames == null || m_viewLinks == null) {
			m_viewNames = new Vector();
			m_viewLinks = new Vector();

			CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);            
			configFile.getWorkplaceIniData(m_viewNames, m_viewLinks,"WORKPLACEVIEWS","VIEW");            
		}
		//------- TEMPORARY: NOT display admin view if user should't see it 
		// to remove this feature delete the lines with: //--TEMPADMIN ...
		boolean adminView = true;
		boolean omittedAdmin = false;
		if (!(reqCont.isAdmin() || reqCont.isProjectManager())) {
			adminView = false; 
		}
		//-------------- ... and above 5 lines
		
		
		// OK. Now m_viewNames and m_viewLinks contail all available
		// view information.
		// Loop through the vectors and fill the result vectors.
		int currentViewIndex = 0;
		int numViews = m_viewNames.size();        
		for(int i=0; i<numViews; i++) {
			String loopValue = (String)m_viewLinks.elementAt(i);
			String loopName = (String)m_viewNames.elementAt(i);
			if (!adminView && loopName.equals("admin")){ //--TEMPADMIN
				omittedAdmin = true; 	//--TEMPADMIN
			} else { //--TEMPADMIN
				values.addElement(loopValue);
				names.addElement(lang.getLanguageValue("select." + loopName));
				if(loopValue.equals(currentView)) {
					currentViewIndex = omittedAdmin? i-1 : i; // = i 
				}
			} //--TEMPADMIN
			
		}
		return new Integer(currentViewIndex);
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
}
