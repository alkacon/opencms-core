package com.opencms.xmlmodules.news;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/xmlmodules/news/Attic/CmsNewsAdmin.java,v $
 * Date   : $Date: 2000/08/08 14:08:34 $
 * Version: $Revision: 1.5 $
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
import com.opencms.workplace.*;
import com.opencms.xmlmodules.*;

import java.util.*;
import java.io.*;
import javax.servlet.http.*;

import org.apache.xml.serialize.*;

/**
 * Template class to handle the administration of news 
 * <p>
 * Used for both displaying a news overview and
 * editing articles.
 * 
 * @author Matthias Schreiber
 * @version $Revision: 1.5 $ $Date: 2000/08/08 14:08:34 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsNewsAdmin extends A_CmsModuleAdmin implements I_CmsNewsConstants {
	
	/**
	 * Create a content file. File flags setted in the user preferences
	 * will be overridden to system default flags.
	 * 
	 * @param cms A_CmsObject for accessing system resources.
	 * @param fileName filename to be used
	 * @return Instance of a content file.
	 * @exception CmsException
	 */
	protected A_CmsModuleObject createFile(CmsObject cms, String fileName) 
			throws CmsException { 
		String fullFilename = C_FOLDER_CONTENT + fileName;
		CmsNewsObject newsTempDoc = new CmsNewsObject();
		newsTempDoc.createNewFile(cms, fullFilename, C_TYPE_PLAIN_NAME);              
		cms.chmod(fullFilename, C_ACCESS_DEFAULT_FLAGS);
		return newsTempDoc;
	}
	/**
	 * Create a new page file for displaying a given content file.
	 * 
	 * @param cms A_CmsObject for accessing system resources.
	 * @param fileName filename to be used
	 * @param mastertemplate filename of the master template that should be used for displaying the content.
	 * @exception CmsException
	 */
	protected void createPageFile(CmsObject cms, String fileName, String mastertemplate)
			throws CmsException {
		
		// Create the folder
		cms.createFolder(C_FOLDER_PAGE, fileName);
				   
		// Create an index file in this folder
		String fullFilename = C_FOLDER_PAGE + fileName + "/index.html";        
		CmsXmlControlFile pageFile = new CmsXmlControlFile();
		pageFile.createNewFile(cms, fullFilename, C_TYPE_NEWSPAGE_NAME);
		pageFile.setTemplateClass("com.opencms.template.CmsXmlTemplate");
		pageFile.setMasterTemplate(mastertemplate);
		pageFile.setElementClass(C_BODY_ELEMENT, C_BODYTEMPLATE_CLASS);
		pageFile.setElementTemplate(C_BODY_ELEMENT, C_PATH_INTERNAL_TEMPLATES + C_BODYTEMPLATE_FILE);
		pageFile.setElementParameter(C_BODY_ELEMENT, C_PARAM_FOLDER, C_FOLDER_CONTENT);
		pageFile.setElementParameter(C_BODY_ELEMENT, C_PARAM_READ, fileName);
		pageFile.write();
		cms.chmod(fullFilename, C_ACCESS_DEFAULT_FLAGS);
	}
	/**
	 * Gets the content of a defined section in a given template file and its subtemplates
	 * with the given parameters. 
	 * 
	 * @param cms A_CmsObject Object for accessing system resources.
	 * @param templateFile Filename of the template file.
	 * @param elementName Element name of this template in our parent template.
	 * @param parameters Hashtable with all template class parameters.
	 * @param templateSelector template section that should be processed.
	 */
	public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
		
		I_CmsSession session = cms.getRequestContext().getSession(true);
		
		// read all parameters
		String file = (String)parameters.get(C_PARAM_FILE);
		if(file == null) {
			file = (String)session.getValue(C_PARAM_FILE);
		} else {
			session.putValue(C_PARAM_FILE, file);
		}

		String action = (String)parameters.get(C_PARAM_ACTION);
		if(action == null) {
			action = (String)session.getValue(C_PARAM_ACTION);
		} else {
			session.putValue(C_PARAM_ACTION, action);
		}        
		
		String newDate = (String)parameters.get(C_XML_DATE);
		String newHeadline = (String)parameters.get(C_XML_HEADLINE);
		String newShorttext = (String)parameters.get(C_XML_SHORTTEXT);
		String newText = (String)parameters.get(C_XML_TEXT);
		String newExternalLink = (String)parameters.get(C_XML_EXTLINK);
		
		// load the template file of the admin screen
		CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);

		// Only go on, if the "edit" action was requested
		if("edit".equals(action)) {                
			// Calendar object used to get the actual date
			GregorianCalendar cal = new GregorianCalendar();
		
			if(newHeadline == null && newShorttext == null && newText == null && newExternalLink == null) {
				if(file != null && ! "".equals(file)) {
					// the user wants to edit an old article
					CmsNewsObject contentFile = (CmsNewsObject)getContentFile(cms, cms.readFile(file));
					
					CmsResource contentFileObject = cms.readFileHeader(contentFile.getAbsoluteFilename());
					if (!contentFileObject.isLocked()) {
						cms.lockResource(contentFile.getAbsoluteFilename());
					}
									
					parameters.put(C_XML_DATE, Utils.getNiceShortDate(contentFile.getDate()));
					parameters.put(C_XML_HEADLINE, Encoder.escapeXml(contentFile.getHeadline()));
					parameters.put(C_XML_SHORTTEXT, Encoder.escapeXml(contentFile.getShortText()));
					parameters.put(C_XML_TEXT, contentFile.getText("\n\n",true));
					parameters.put(C_XML_EXTLINK, contentFile.getExternalLink());
					parameters.put(C_PARAM_STATE, new Boolean(contentFile.isActive()));
					xmlTemplateDocument.setData(C_XML_AUTHOR, Encoder.escapeXml(contentFile.getAuthor()));
					session.putValue(C_XML_AUTHOR, contentFile.getAuthor());
				} else {
					// the user requested a new article

					// Get the currently logged in user
					CmsUser author = cms.getRequestContext().currentUser();        
		
					// Get the String for the author
					String authorText = null;
					String initials =  getInitials(author);                               
					String firstName = author.getFirstname();
					String lastName = author.getLastname();
					if((firstName == null || "".equals(firstName)) && (lastName == null || "".equals(lastName))) {
						authorText = initials;
					} else {            
						authorText = firstName + " " + lastName;
						authorText = authorText.trim();
						authorText = authorText + " (" + initials + ")";
					}
					session.putValue(C_XML_AUTHOR, authorText);
					xmlTemplateDocument.setData(C_XML_AUTHOR, authorText);
					
					// Get the Sting for the actual date
					String dateText = Utils.getNiceShortDate(cal.getTime().getTime());
					parameters.put(C_XML_DATE, dateText);
				}
			} else {
				// this is the POST result of an user input
				
				CmsXmlTemplateFile iniFile = new CmsXmlTemplateFile(cms, C_INI_FILE);                
				CmsNewsObject contentFile = null;                 
				
				if(file == null || "".equals(file)) {
					// we have to create a new new file
									
					// Get the currently logged in user
					CmsUser author = cms.getRequestContext().currentUser();        
		
					// Build the new article filename
					String dateFileText = getDateFileText(cal);        
					String number = getNewArticleNumber(cms, dateFileText, C_FOLDER_CONTENT);
					String initials =  getInitials(author);               
					String fileName = dateFileText + "-" + number + "-" + initials.toLowerCase();
					parameters.put(C_PARAM_FILE, fileName);
					
					// create files
					contentFile = (CmsNewsObject)createFile(cms, fileName);                    
					createPageFile(cms, fileName, iniFile.getDataValue("mastertemplate"));

					// check the date parameter
					if(newDate == null || "".equals(newDate)) {
						newDate = Utils.getNiceShortDate(cal.getTime().getTime());
					}
					
					// Try creating the task
					try {
						makeTask(cms, fileName, C_FOLDER_PAGE, iniFile.getDataValue("newstask.agent"), iniFile.getDataValue("newstask.role"),"task.label.news");
					} catch(Exception e) {
						if(A_OpenCms.isLogging()) {
							A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + "Cannot create task for news article " + fileName + ". ");
							A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + e.getMessage());
						}
					}                    
				} else {
					contentFile = (CmsNewsObject)getContentFile(cms, cms.readFile(file));                
					// Touch the page file. This will mark it as "changed".
					cms.writeFileHeader((CmsFile)cms.readFileHeader(C_FOLDER_PAGE + contentFile.getFilename() + "/index.html"));
				}
				
				// Set the content
				setFileContent(contentFile, session, parameters);                                      
				
				// Session parameters are not needed any more...
				session.removeValue(C_PARAM_FILE);
				session.removeValue(C_PARAM_ACTION);
				session.removeValue(C_XML_AUTHOR);
				templateSelector = C_DONE;                
		    }
		}
		
		
		// check if the new resource button must be enabeld.
		// this is only done if the project is not the online project.
		if(xmlTemplateDocument.hasData(C_NEW_DISABLED) && xmlTemplateDocument.hasData(C_NEW_ENABLED)) {
			if (cms.getRequestContext().currentProject().equals(cms.onlineProject()) || !checkWriteAccess(cms,C_FOLDER_PAGE,C_FOLDER_CONTENT)) {
				xmlTemplateDocument.setData(C_NEW,xmlTemplateDocument.getProcessedDataValue(C_NEW_DISABLED,this));                
			} else {
				xmlTemplateDocument.setData(C_NEW,xmlTemplateDocument.getProcessedDataValue(C_NEW_ENABLED,this));       
			}
		}
		
		// Finally start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
	}
	/**
	 * Get the corresponding content file for a given page file.
	 * @param cms A_CmsObject for accessing system resources.
	 * @param file File object of the page file.
	 * @return CmsNewsContentFile object of the corresponding content file.
	 * @exception CmsException when file access failed.
	 */
	protected A_CmsModuleObject getContentFile(CmsObject cms, CmsResource file) throws CmsException {

		CmsFile contentFileObject = null;
		CmsNewsObject contentFile = null;

		// The given file object contains the page file.
		// we have to read out the article
		CmsXmlControlFile pageFile = new CmsXmlControlFile(cms, (CmsFile)file); 
		String readParam = pageFile.getElementParameter(C_BODY_ELEMENT, C_PARAM_READ);
		String folderParam = pageFile.getElementParameter(C_BODY_ELEMENT, C_PARAM_FOLDER);
		
		if(readParam != null && !"".equals(readParam)) {
			// there is a read parameter given.
			// so we know which file should be read.
			if(folderParam == null || "".equals(folderParam)) {
				folderParam = C_FOLDER_CONTENT;
			}
			try {
				contentFileObject = cms.readFile(folderParam, readParam);
			} catch(Exception e) {
				//The content file could not be read.
				contentFileObject = null;
			}
			if(contentFileObject != null) {
				contentFile = new CmsNewsObject(cms, contentFileObject);
			}
		}
		return contentFile;
	}
	/**
	 * Used for filling the input field <em>External Link</em> in the editor.
	 * @param cms Cms object for accessing system resources.
	 * @param lang Current language file.
	 * @param parameters User parameters.
	 * @return String containing the external link.
	 */
	public String getExternalLink(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) {
		String result = (String)parameters.get(C_XML_EXTLINK);
		if(result == null || "".equals(result)) {
			result = C_DEF_PROTOCOL;
		}
		return result;
	}
	/** 
	* From interface <code>I_CmsFileListUsers</code>.
	* <P>   
	* Collects all folders and files that are displayed in the file list.
	* @param cms The CmsObject.
	* @return A vector of folder and file objects.
	* @exception Throws CmsException if something goes wrong.
	*/
 
	public Vector getFiles(CmsObject cms) throws CmsException {
		Vector files = new Vector();
		
		Vector folders = cms.getSubFolders(C_FOLDER_PAGE); 
		int numFolders = folders.size();
		// Walk through the folders backwards since we
		// want to see the newest article first.
		for(int i=numFolders-1; i>=0; i--) {
			CmsResource currFolder = (CmsResource)folders.elementAt(i);
			CmsFile pageFile = null;
			try {
				pageFile = cms.readFile(currFolder.getAbsolutePath(), "index.html");
			} catch(Exception e) {
				// Oh... we expected an index file here.
				// Do nothing instead
				continue;
			}
			files.addElement(pageFile);            
		}                                                            
		return files;
	}
	/**
	 * Used for filling the input field <em>Short Text</em> in the editor.
	 * @param cms Cms object for accessing system resources.
	 * @param lang Current language file.
	 * @param parameters User parameters.
	 * @return String containing the short text of the article.
	 */
	public String getShorttext(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) {
		Hashtable parameters = (Hashtable)userObject;
		String result = (String)parameters.get(C_XML_SHORTTEXT);
		if(result == null) {
			result = "";
		}
		return result;
	}
	/**
	 * Set the content of the XML file to the given values.
	 * <p>
	 * The <code>text</code> will be separated into paragraphs, if more than one
	 * following line feeds are found.
	 * @param obj CmsNewsObject for a certain content file.
	 * @param session Current instance of I_CmsSession
	 * @param hashtable Hashtable with the user parameters
	 * @exception CmsExecption
	 */
	protected void setFileContent(CmsNewsObject obj, I_CmsSession session, Hashtable parameters)
			throws CmsException {
		Vector paragraphs = new Vector();
		obj.setAuthor((String)session.getValue(C_XML_AUTHOR));
		
		String date = (String)parameters.get(C_XML_DATE);
		if(date != null && !"".equals(date)) {	// only set date if given
			obj.setDate(date);
		}
		paragraphs = getTextParagraphs(obj,(String)parameters.get(C_XML_TEXT));
							 
		// set all other values
		obj.setHeadline((String)parameters.get(C_XML_HEADLINE));
		obj.setShortText((String)parameters.get(C_XML_SHORTTEXT));
		obj.setText(paragraphs);
		obj.setExternalLink((String)parameters.get(C_XML_EXTLINK));               
		obj.setActive(C_STATE_ACTIVE.equals((String)parameters.get(C_PARAM_STATE)));
		obj.write();
	}
}
