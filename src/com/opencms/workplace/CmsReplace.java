/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsReplace.java,v $
 * Date   : $Date: 2003/06/13 15:13:13 $
 * Version: $Revision: 1.5 $
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

package com.opencms.workplace;

import org.opencms.workplace.CmsWorkplaceAction;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class is invoked for the workplace "replace" function in the context menu.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.5 $
 */
public final class CmsReplace extends CmsWorkplaceDefault implements I_CmsConstants, I_CmsWpConstants {

	private static final String C_PARA_NEXT_STEP = "STEP";

	/** Internal debugging flag */
	private static final int DEBUG = 0;

	/** The name of the old resource which will be replaced */
	private String m_OldResourceName;

	/** The type of the old resource which will be replaced */
	private String m_OldResourceType;

	/** The next screen/step to process */
	private String m_NextStep;

	/** The name of the new resource which will be uploaded */
	private String m_UploadResourceName;

	/** The type of the new resource which will be uploaded */
	private String m_UploadResourceType;

	/** The content of the new resource which will be uploaded */
	private byte[] m_UploadResourceContent;

    /** The XML template document */
	private CmsXmlWpTemplateFile m_XmlTemplateDocument;
    
    /** The name of the template section that is put out as HTML */
	private String m_TemplateSection;

	public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
		// initialize the XML template
		this.initTemplate(cms, parameters, templateFile);

		// read the submitted input parameters
		this.readInput(cms, parameters);

		if ("1".equals(m_NextStep)) {
			// the resource was uploaded, switch to the next screen to select its type
			m_TemplateSection = "step1";
		}
		else if ("2".equals(m_NextStep)) {
			// the type of the new resource was selected, so replace the old with the new resource
			//cms.lockResource(m_OldResourceName, true);
			cms.replaceResource(m_OldResourceName, m_UploadResourceType, null, m_UploadResourceContent);
			//cms.unlockResource( m_OldResourceName ); 
            
            // leave the session clean
            this.clearSessionValues(cms.getRequestContext().getSession(true));   

			try {
                // send the user back to the file listing finally
				cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath() + CmsWorkplaceAction.getExplorerFileUri(cms));
			}
			catch (Exception ex) {
				throw new CmsException("Redirect failed: " + getConfigFile(cms).getWorkplaceActionPath() + CmsWorkplaceAction.getExplorerFileUri(cms), CmsException.C_UNKNOWN_EXCEPTION, ex);
			}
		}

		// pump the template with the input form out on the first submission   
		return startProcessing(cms, m_XmlTemplateDocument, "", parameters, m_TemplateSection);
	}

	private void readInput(CmsObject cms, Hashtable theParameters) throws CmsException {
		I_CmsSession session = cms.getRequestContext().getSession(true);

		// reset all member values first
		m_OldResourceName = null;
		m_OldResourceType = null;
		m_UploadResourceName = null;
		m_UploadResourceType = null;
		m_UploadResourceContent = null;
		m_UploadResourceType = null;
		m_NextStep = null;

		//////////////////////////////////////////////////////////////////////

		// clear session values on the first load
		if (theParameters.get(I_CmsWpConstants.C_PARA_INITIAL) != null) {
			this.clearSessionValues(session);
		}

		//////////////////////////////////////////////////////////////////////      

		// save the name of the old resource in the session
		m_OldResourceName = (String) theParameters.get(I_CmsWpConstants.C_PARA_FILE);
		if (m_OldResourceName != null) {
			session.putValue(I_CmsWpConstants.C_PARA_FILE, m_OldResourceName);

			// preserve the type of the old resource depending on the file extension as well
			Hashtable fileExtensions = cms.readFileExtensions();
			String uploadFilenameExtension = m_OldResourceName.substring(m_OldResourceName.lastIndexOf('.') + 1).toLowerCase();

			if (fileExtensions != null) {
				m_OldResourceType = (String) fileExtensions.get(uploadFilenameExtension);
			}

			if (m_OldResourceType == null) {
				// the resource type should be at least the empty string
				m_OldResourceType = "";
			}

			session.putValue("OLD_TYPE", m_OldResourceType);
		}
		else {
			m_OldResourceName = (String) session.getValue(I_CmsWpConstants.C_PARA_FILE);
			m_OldResourceType = (String) session.getValue("OLD_TYPE");
		}

		//////////////////////////////////////////////////////////////////////

		// get the next step parameter that tells this class what to do next...
		m_NextStep = (String) theParameters.get(CmsReplace.C_PARA_NEXT_STEP);

		//////////////////////////////////////////////////////////////////////

		// get both the name and content of the new resource and save it in the session
		Enumeration allUploadedFilenames = cms.getRequestContext().getRequest().getFileNames();
		while (allUploadedFilenames.hasMoreElements()) {
			m_UploadResourceName = (String) allUploadedFilenames.nextElement();
		}

		if (m_UploadResourceName != null) {
			session.putValue("NEW_RESOURCE", m_UploadResourceName);

			m_UploadResourceContent = cms.getRequestContext().getRequest().getFile(m_UploadResourceName);
			session.putValue(I_CmsWpConstants.C_PARA_FILECONTENT, m_UploadResourceContent);
		}
		else {
			m_UploadResourceName = (String) session.getValue("NEW_RESOURCE");
			m_UploadResourceContent = (byte[]) session.getValue(I_CmsWpConstants.C_PARA_FILECONTENT);
		}
		m_XmlTemplateDocument.setData("NEW_RESOURCE", m_UploadResourceName);

		//////////////////////////////////////////////////////////////////////

		// get the type of the new resource
		m_UploadResourceType = (String) theParameters.get(I_CmsWpConstants.C_PARA_NEWTYPE);
		if (m_UploadResourceType != null) {
			session.putValue(I_CmsWpConstants.C_PARA_NEWTYPE, m_UploadResourceType);
		}
		else {
			m_UploadResourceType = (String) session.getValue(I_CmsWpConstants.C_PARA_NEWTYPE);
		}

		//////////////////////////////////////////////////////////////////////      

		if (DEBUG > 0) {
			System.out.println("\nnext step: " + m_NextStep);
			System.out.println("old resource: " + m_OldResourceName);
			System.out.println("old type: " + m_OldResourceType);
			System.out.println("new resource: " + m_UploadResourceName);
            System.out.println("new type: " + m_UploadResourceType);
		}
	}

	private void initTemplate(CmsObject cms, Hashtable theParameters, String theTemplateFile) throws CmsException {
		// reset all values first
		m_XmlTemplateDocument = null;
		m_TemplateSection = null;

		// create the XML output template file
		m_XmlTemplateDocument = new CmsXmlWpTemplateFile(cms, theTemplateFile);
	}

	/**
	 * Removes the values cached in the session.
	 */
	private void clearSessionValues(I_CmsSession theSession) {
		// remove all session values
		theSession.removeValue(I_CmsWpConstants.C_PARA_FILE); // name of the old resource
		theSession.removeValue("OLD_TYPE"); // type of the old resource
		theSession.removeValue("NEW_RESOURCE"); // name of the new resource
		theSession.removeValue(I_CmsWpConstants.C_PARA_FILECONTENT); // content of the new resource        
	}

	/**
	 * The element cache, programmer's best friend.
	 */
	public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
		return false;
	}

	/**
	 * Prepares for a group of radio buttons the field names, field values and
	 * icon names. Additionally, the index of the checked radio button is set
	 * depending on the file type that we upload.
	 * 
	 * @param cms The CmsObject
	 * @param language The langauge definitions
	 * @param names The names of the resource types
	 * @param values The values of the resource types
	 * @param parameters Hashtable of parameters (not used yet)
	 * @param descriptions Description that will be displayed for the new resource
	 * @return the index of the checked radio button, -1 if none is checked
	 * @throws Throws CmsException if something goes wrong.
	 */
	public int getResources(CmsObject cms, CmsXmlLanguageFile language, Vector resourceTypeIconNames, Vector resourceTypeFieldValues, Vector resourceTypeFieldNames, Hashtable parameters) throws CmsException {
		cms.getRequestContext().getSession(true);
		Vector resourceTypeNames = new Vector();
		Vector resourceTypeValues = new Vector();
		int checkedRadioIndex = 0;

		CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
		configFile.getWorkplaceIniData(resourceTypeNames, resourceTypeValues, "RESOURCETYPES", "RESOURCE");

		// robustness, robustness, robustness...
		if (resourceTypeIconNames == null) {
			resourceTypeIconNames = new Vector();
		}

		if (resourceTypeFieldValues == null) {
			resourceTypeFieldValues = new Vector();
		}

		if (resourceTypeFieldNames == null) {
			resourceTypeFieldNames = new Vector();
		}

		int count = resourceTypeNames.size();
		for (int i = 0; i < count; i++) {
			String currentResourceTypeFieldValue = (String) resourceTypeValues.elementAt(i);
			String currentResourceTypeName = (String) resourceTypeNames.elementAt(i);

			// submit value of the radio button for the current resource type
			resourceTypeFieldValues.addElement(currentResourceTypeFieldValue);

			// dito, name of the icon
			resourceTypeIconNames.addElement("file_" + currentResourceTypeName);

			// dito, clear text name of the current resoure type
			String currentResourceTypeFieldName = null;

			if (language != null) {
				currentResourceTypeFieldName = language.getLanguageValue("fileicon." + currentResourceTypeName);
			}
			else {
				currentResourceTypeFieldName = currentResourceTypeName;
			}

			resourceTypeFieldNames.addElement(currentResourceTypeFieldName);

			// prove if the current resource type matches the resource type of
			// the old resource to make a preselection by checking the
			// right radio button...
			if (m_OldResourceType.equals(currentResourceTypeName)) {
				checkedRadioIndex = i;
			}
		}

		return checkedRadioIndex;
	}

}
