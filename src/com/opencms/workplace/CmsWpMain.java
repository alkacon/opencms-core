/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsWpMain.java,v $
* Date   : $Date: 2005/02/18 14:23:15 $
* Version: $Revision: 1.71 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001-2005  The OpenCms Group
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

package com.opencms.workplace;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.template.CmsXmlTemplateFile;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Template class for displaying OpenCms workplace main screen.
 * <P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Alexander Lucas
 * @author Michael Emmerich
 * @version $Revision: 1.71 $ $Date: 2005/02/18 14:23:15 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsWpMain extends CmsWorkplaceDefault {
        
    /**
     * The name of the tag for sync button.
     */
    static final String C_SYNC_BUTTON = "SYNC";

    /**
     * The name of the tag for disabled sync button.
     */
    static final String C_SYNC_BUTTON_DISABLED = "SYNC_DISABLED";

    
    public CmsWpMain() {
        super();
        
        m_SelectedPrjIndex = 0;
        m_ProjectIds = null;
        m_ProjectNames = null;
    }
        
	/**
	 * Gets the content of a defined section in a given template file and its subtemplates
	 * with the given parameters.
	 *
	 * @see #getContent(CmsObject, String, String, Hashtable, String)
	 * @param cms CmsObject Object for accessing system resources.
	 * @param templateFile Filename of the template file.
	 * @param elementName Element name of this template in our parent template.
	 * @param parameters Hashtable with all template class parameters.
	 * @param templateSelector template section that should be processed.
	 */

	public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
		if (OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
			OpenCms.getLog(this).debug("[CmsXmlTemplate] getting content of element " + ((elementName == null) ? "<root>" : elementName));
			OpenCms.getLog(this).debug("[CmsXmlTemplate] template file is: " + templateFile);
			OpenCms.getLog(this).debug("[CmsXmlTemplate] selected template section is: " + ((templateSelector == null) ? "<default>" : templateSelector));
		}

		I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
		CmsRequestContext reqCont = cms.getRequestContext();
//		String newGroup = (String) parameters.get("group");
		String newProject = (String) parameters.get("project");
		String newView = (String) parameters.get(C_PARA_VIEW);
		CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);

		// Check if the user requested a group change
//		if (newGroup != null && !("".equals(newGroup))) {
//			if (!(newGroup.equals(reqCont.currentGroup().getName()))) {
//				reqCont.setCurrentGroup(newGroup);
//			}
//		}

		// Check if the user requested a project change
		if (newProject != null && !("".equals(newProject))) {
			if (!(Integer.parseInt(newProject) == reqCont.currentProject().getId())) {
				reqCont.setCurrentProject(cms.readProject(Integer.parseInt(newProject)));
			}
		}

		// Check if the user requested a new view
		if (newView != null && !("".equals(newView))) {
			session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
			session.putValue(C_PARA_VIEW, newView);
		}

		// set the publishProject Button to enable if user has the right to publish the project
		if (templateFile.equalsIgnoreCase(C_VFS_PATH_DEFAULT_INTERNAL + "head")) {
			if ((cms.isAdmin() || cms.isManagerOfProject()) && (!reqCont.currentProject().isOnlineProject())) {
				xmlTemplateDocument.setData("publish", xmlTemplateDocument.getProcessedDataValue("PUBLISH_ENABLED", this));
			}
			else {
				xmlTemplateDocument.setData("publish", xmlTemplateDocument.getProcessedDataValue("PUBLISH_DISABLED", this));
			}
		}

		// set the sync button to enabled if no entries for synchronisation in registry
//		if (templateFile.equalsIgnoreCase(C_VFS_PATH_DEFAULT_INTERNAL + "head")) {
//			String syncpath = null;
//			syncpath = cms.getRegistry().getSystemValue(I_CmsConstants.C_SYNCHRONISATION_PROJECT);
//			if (syncpath == null) {
				xmlTemplateDocument.setData(C_SYNC_BUTTON, xmlTemplateDocument.getProcessedDataValue(C_SYNC_BUTTON_DISABLED, this));
//			}
//			else {
//				xmlTemplateDocument.setData(CmsSyncFolder.C_SYNC_BUTTON, xmlTemplateDocument.getProcessedDataValue(CmsSyncFolder.C_SYNC_BUTTON_ENABLED, this));
//			}
//		}

		// send message, if this is the foot
		if (templateFile.equalsIgnoreCase(C_VFS_PATH_DEFAULT_INTERNAL + "foot")) {
			String message = (String) CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true).getValue(I_CmsConstants.C_SESSION_BROADCASTMESSAGE);
			if (message != null) {
				CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true).removeValue(I_CmsConstants.C_SESSION_BROADCASTMESSAGE);
				xmlTemplateDocument.setData("message", "alert(unescape('BROADCASTMESSAGE: " + CmsEncoder.escape(message, cms.getRequestContext().getEncoding()) + "'));");
			}
		}

		// test if the "help"- button has to be displayed for the user's 
		// current language in case we process the head template
		if (templateFile.equalsIgnoreCase(C_VFS_PATH_DEFAULT_INTERNAL + "head")) {
			String userLanguage = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
			xmlTemplateDocument.setData("LOCALE", "" + userLanguage);

			try {
				cms.readFolder(C_VFS_PATH_HELP + userLanguage);
				// the localized help- folder exists
				xmlTemplateDocument.setData("HELP", xmlTemplateDocument.getProcessedDataValue("HELP_ENABLED", this));
			}
			catch (CmsException e) {
				// the localized help- folder does not exist
                try {
				    xmlTemplateDocument.setData("HELP", xmlTemplateDocument.getProcessedDataValue("HELP_DISABLED", this));
                } catch (Exception ex) {
                    // probably the "head" template is old, ignore this error so the workplace can still be used
                } 
			}
		}
        
        this.fetchProjects(cms, xmlTemplateDocument);

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
	 * @throws CmsException
	 */

	public Integer getGroups(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws CmsException {

		// Get a vector of all of the user's groups by asking the request context
		CmsRequestContext reqCont = cms.getRequestContext();
//		CmsGroup currentGroup = reqCont.currentGroup();
		List allGroups = cms.getGroupsOfUser(reqCont.currentUser().getName());

		// Now loop through all groups and fill the result vectors
		int numGroups = allGroups.size();
//		int currentGroupNum = 0;
		for (int i = 0; i < numGroups; i++) {
			CmsGroup loopGroup = (CmsGroup) allGroups.get(i);
			String loopGroupName = loopGroup.getName();
			values.addElement(loopGroupName);
			names.addElement(loopGroupName);
//			if (loopGroup.equals(currentGroup)) {
//
//				// Fine. The group of this loop is the user's current group. Save it!
//				currentGroupNum = i;
//			}
		}
		return new Integer(0);
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
	 * @throws CmsException
	 */

    public Integer getProjects(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws CmsException {
        if (m_ProjectNames == null || m_ProjectIds == null) {
            new Integer(0);
        }

        values.addAll(m_ProjectIds);
        names.addAll(m_ProjectNames);

        return new Integer(m_SelectedPrjIndex);
    }
    
    private void fetchProjects(CmsObject cms, CmsXmlTemplateFile xmlTemplateDocument) throws CmsException {
        // Get all project information
        CmsRequestContext reqCont = cms.getRequestContext();
        Vector allProjects = new Vector(cms.getAllAccessibleProjects());
        int currentProjectId = reqCont.currentProject().getId();

        // Now loop through all projects and fill the result vectors
        int numProjects = allProjects.size();
        int currentProjectNum = 0;
        int currentLength = 0;
        
        int maxPrjNameLen = 0;
        m_ProjectIds = new Vector();
        m_ProjectNames = new Vector();
        
        for (int i = 0; i < numProjects; i++) {
            CmsProject loopProject = (CmsProject) allProjects.elementAt(i);
            String loopProjectName = loopProject.getName();
            String loopProjectId = loopProject.getId() + "";
            
            m_ProjectIds.addElement(loopProjectId);
            m_ProjectNames.addElement(loopProjectName);
            
            if (loopProject.getId() == currentProjectId) {
                // Fine. The project of this loop is the user's current project. Save it!
                currentProjectNum = i;
            }
            
            currentLength = loopProjectName.length();
            if (currentLength>maxPrjNameLen) {
                maxPrjNameLen = currentLength;
            }
        }
        
        m_SelectedPrjIndex = currentProjectNum;
        
        try {
            if (maxPrjNameLen <= 20) {
                xmlTemplateDocument.setData("PRJ_SELECT", xmlTemplateDocument.getProcessedDataValue("PRJ_SELECT_NORMAL", this));
            } else {
                xmlTemplateDocument.setData("PRJ_SELECT", xmlTemplateDocument.getProcessedDataValue("PRJ_SELECT_LARGE", this));
            }
        } catch (CmsException e) {
            // this exception is only caugth for backwards compatibility with older templates
            // missing the data blocks above.
        }
    }
    
    private int m_SelectedPrjIndex;
    private Vector m_ProjectIds;
    private Vector m_ProjectNames;

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
	 * @throws CmsException
	 */

	public Integer getRegViews(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws CmsException {

		// Let's see if we have a session
		CmsRequestContext reqCont = cms.getRequestContext();
		I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);

		// try to get an existing view
		String currentView = null;
		Hashtable startSettings = null;

		// check out the user infor1ation if a default view is stored there.
		startSettings = (Hashtable) reqCont.currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);
		if (startSettings != null) {
			currentView = (String) startSettings.get(C_START_VIEW);
		}

		// If there is a session, let's see if it has a view stored
		if (session != null) {
			if (session.getValue(C_PARA_VIEW) != null) {
				currentView = (String) session.getValue(C_PARA_VIEW);
			}
		}
		if (currentView == null) {
			currentView = "";
		}
		Vector viewNames = new Vector();
		Vector viewLinks = new Vector();
        int currentViewIndex = 0;

//		// get the List of available views from the Registry
//		int numViews = (cms.getRegistry()).getViews(viewNames, viewLinks);
//
//		// Loop through the vectors and fill the resultvectors
//		for (int i = 0; i < numViews; i++) {
//			String loopName = (String) viewNames.elementAt(i);
//			String loopLink = (String) viewLinks.elementAt(i);
//            
//			boolean visible = true;
//			try {
//				cms.readFileHeader(loopLink);
//			}
//			catch (CmsException e) {
//				visible = false;
//			}
//			if (visible) {
//				if (loopLink.equals(currentView)) {
//					currentViewIndex = values.size();
//				}
//				names.addElement(lang.getLanguageValue(loopName));
//				values.addElement(loopLink);
//			}
//		}
		return new Integer(currentViewIndex);
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
	 * @throws CmsException
	 */

	public Object getUser(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
		CmsRequestContext reqContext = cms.getRequestContext();
		CmsUser currentUser = reqContext.currentUser();
		return currentUser.getName();
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
