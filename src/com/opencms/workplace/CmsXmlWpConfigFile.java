package com.opencms.workplace;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsXmlWpConfigFile.java,v $
 * Date   : $Date: 2000/11/02 14:29:09 $
 * Version: $Revision: 1.23 $
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
import com.opencms.template.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import java.util.*;
import javax.servlet.http.*;

/**
 * Content definition for "/workplace/workplace.ini".
 * 
 * @author Alexander Lucas
 * @author Michael Emmerich
 * @version $Revision: 1.23 $ $Date: 2000/11/02 14:29:09 $
 */
public class CmsXmlWpConfigFile extends A_CmsXmlContent implements I_CmsLogChannels, I_CmsConstants {
	/**
	 * Default constructor.
	 */
	public CmsXmlWpConfigFile() throws CmsException {
		super();
	}
	/**
	 * Constructor for creating a new config file object containing the content
	 * of the actual system config file.
	 * <P>
	 * The position of the workplace.ini is defined in I_CmsConstants.
	 * 
	 * @param cms CmsObject object for accessing system resources.
	 */        
	 public CmsXmlWpConfigFile(CmsObject cms) throws CmsException {
		super();
		try {
			init(cms, C_WORKPLACE_INI);
		}catch(Exception e) {
			e.printStackTrace();
			throwException("Could not read configuration file \"workplace.ini\".", CmsException.C_NOT_FOUND);
		}        
	}
	/**
	 * Constructor for creating a new object containing the content
	 * of the given CmsFile object.
	 * 
	 * @param cms CmsObject object for accessing system resources.
	 * @param filename Name of the body file that shoul be read.
	 */        
	public CmsXmlWpConfigFile(CmsObject cms, CmsFile file) throws CmsException {
		super();
		init(cms, file);
	}
	/**
	 * Constructor for creating a new object containing the content
	 * of the given filename.
	 * 
	 * @param cms CmsObject object for accessing system resources.
	 * @param filename Name of the body file that shoul be read.
	 */        
	public CmsXmlWpConfigFile(CmsObject cms, String filename) throws CmsException {
		super();
		init(cms, filename);
	}
	/**
	 * Gets the path for common picture files.
	 * @return Path for picture files.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getCommonPicturePath() throws CmsException {
		return getDataValue("path.commonpictures");
	}
	/**
	 * Gets the URL where common template pics reside.
	 * If the path is empty, the workplace picture path prfeixed
	 * by the servlet path will be returned
	 * @return Path for the "pics" mountpoint.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getCommonPictureUrl() throws CmsException {
		String s = getDataValue("path.commonpicsurl");
		if(s == null || "".equals(s)) {
			s = ((HttpServletRequest)m_cms.getRequestContext().getRequest().getOriginalRequest()).getServletPath() + getDataValue("path.commonpictures");
		}
		return s;
	}
	/**
	 * Gets the path for OpenCms common templates.
	 * @return Path for OpenCms common templates.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getCommonTemplatePath() throws CmsException {
		return getDataValue("path.commontemplates");
	}
	/**
	 * Gets a description of this content type.
	 * @return Content type description.
	 */
	public String getContentDescription() {
		return "OpenCms workplace configuration file";
	}
	/**
	 * Overridden internal method for getting datablocks.
	 * This method first checkes, if the requested value exists.
	 * Otherwise it throws an exception of the type C_XML_TAG_MISSING.
	 * 
	 * @param tag requested datablock.
	 * @return Value of the datablock.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getDataValue(String tag) throws CmsException {
		String result = null;
		if(!hasData(tag)) {
			String errorMessage = "Mandatory tag \"" + tag + "\" missing in workplace definition file.";
			if(A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
			}
			throw new CmsException(errorMessage, CmsException.C_XML_TAG_MISSING);     
		} else {
			result = super.getDataValue(tag);
		}
		return result;
	}
	/**
	 * Gets the default mail sender.
	 * @return Mail address of the default mail sender.
	 * @deprecated Not used any more. Mail properties are now stored in the registry. Please use <code>CmsRegistry.getSystemValue("defaultmailsender")</code> instead.
	 * @see com.opencms.file.CmsRegistry#getSystemValue
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getDefaultMailSender() throws CmsException {
		return getDataValue("mail.defaultsender");
	}
	/**
	 * Gets the path at which the folders with the download galleries are
	 * @return Path for download galleries.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getDownGalleryPath() throws CmsException {
		return getDataValue("path.downloadgallery");
	}
	/**
	 * Gets the path for help files.
	 * @return Path for help files.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getHelpPath() throws CmsException {
		return getDataValue("path.help");
	}
	/**
	 * Gets the path for OpenCms language files.
	 * @return Path for language files.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getLanguagePath() throws CmsException {      
		return getDataValue("path.language");                    
	}
	/**
	 * Gets the mailserver.
	 * @return The mailserver name.
	 * @deprecated Not used any more. Mail properties are now stored in the registry. Please use <code>CmsRegistry.getSystemValue("smtpserver")</code> instead.
	 * @see com.opencms.file.CmsRegistry#getSystemValue
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getMailServer() throws CmsException {
		return getDataValue("mail.server");
	}
    /**
	 * Gets the path at which the folders with the picture galleries are
	 * @return Path for picture galleries.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getPicGalleryPath() throws CmsException {
		return getDataValue("path.picgallery");
	}
	/**
	 * Gets the path for OpenCms workplace stylesheet files.
	 * @return Path for OpenCms workplace stylesheet files.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getStylesheetPath() throws CmsException {
		return getDataValue("path.stylesheet");
	}
	/**
	 * Gets the path for OpenCms workplace action files.
	 * @return Path for OpenCms workplace action files.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getWorkplaceActionPath() throws CmsException {
		return getDataValue("path.wpaction");
	}
	/**
	 * Gets the path for OpenCms workplace administration files.
	 * @return Path for OpenCms workplace administration files.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getWorkplaceAdministrationPath() throws CmsException {
		return getDataValue("path.administration");
	}
	/**
	 * Gets the path for OpenCms element templates like ButtonTemplate.
	 * @return Path for OpenCms workplaces templates.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getWorkplaceElementPath() throws CmsException {
		return getDataValue("path.wpelements");
	}
	/**
	 * Gets the available workplace views defined in the config file.
	 * Names of the views will be stored in <code>names</code>,
	 * the corresponding URL will be stored in <code>values</code>.
	 * 
	 * @param names Vector to be filled with the appropriate values in this method.
	 * @param values Vector to be filled with the appropriate values in this method.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
   /* public void getViews(Vector names, Vector values) throws CmsException {
		
		// Check the tag "WORKPLACEVIEWS" in the config file
		if(!hasData("workplaceviews")) {
			throwException("Tag \"workplaceviews\" missing in workplace configuration file.", CmsException.C_XML_TAG_MISSING);
		}
		Element viewsElement = getData("workplaceviews");
		
		// Now get a NodeList of all available views
		NodeList allViews = viewsElement.getElementsByTagName("VIEW");

		// Check the existance of at least one view.
		int numViews = allViews.getLength();        
		if(numViews == 0) {
			throwException("No views defined workplace configuration file.", CmsException.C_XML_TAG_MISSING);
		}
				
		// Everything is fine.
		// Now loop through the available views and fill the result
		// vectors.
		for(int i=0; i<numViews; i++) {
			Element currentView = (Element)allViews.item(i);
			String name = currentView.getAttribute("name");
			if(name == null || "".equals(name)) {
				name = "View " + i;
			}
			String link = getTagValue(currentView);
			if(link == null || "".equals(link)) {
				throwException("View \"" + name + "\" has no value defined workplace configuration file.", CmsException.C_XML_TAG_MISSING);
			}
			names.addElement(name);
			values.addElement(link);
		}
	} */
	 
	 /**
	 * Gets the available workplace tag elements defined in the config file.
	 * Names of the elements will be stored in <code>names</code>,
	 * the corresponding values will be stored in <code>values</code>.
	 * 
	 * @param names Vector to be filled with the appropriate values in this method.
	 * @param values Vector to be filled with the appropriate values in this method.
	 * @param tag The tag requested form the workplace config file.
	 * @param element The name of the emelemtn to be read.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public void getWorkplaceIniData(Vector names, Vector values, String tag, String element) throws CmsException {
		// Check the tag "tag" in the config file
		if(!hasData(tag)) {
			throwException("Tag \""+tag+"\" missing in workplace configuration file.", CmsException.C_XML_TAG_MISSING);
		}
		Element viewsElement = getData(tag);
		
		// Now get a NodeList of all available element
		NodeList allViews = viewsElement.getElementsByTagName(element);

		// Check the existance of at least one view.
		int numViews = allViews.getLength();        
		if(numViews == 0) {
			throwException("No elements defined workplace configuration file.", CmsException.C_XML_TAG_MISSING);
		}
				
		// Everything is fine.
		// Now loop through the available views and fill the result
		// vectors.
		for(int i=0; i<numViews; i++) {
			Element currentView = (Element)allViews.item(i);
			String name = currentView.getAttribute("name");
			if(name == null || "".equals(name)) {
				name = "View " + i;
			}
			String link = getTagValue(currentView);
			if(link == null || "".equals(link)) {
				throwException("View \"" + name + "\" has no value defined workplace configuration file.", CmsException.C_XML_TAG_MISSING);
			}
			names.addElement(name);
			values.addElement(link);
		}
	}
/**
 * Gets the path for OpenCms javascripts.
 * @return Path for OpenCms javascripts.
 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
 * Creation date: (11.08.00 14:06:18)
 */
public String getWorkplaceJsPath() throws com.opencms.core.CmsException {
	return getDataValue("path.wpscripts");
}
	/**
	 * Gets the main path for OpenCms workplace index page 
	 * (e.g. <code>/system/workplace/action/index.html</code>).
	 * @return Path for workplace index.html.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getWorkplaceMainPath() throws CmsException {      
		return getDataValue("path.main");                    
	}
	/**
	 * Gets the path for OpenCms workplaces templates.
	 * @return Path for OpenCms workplaces templates.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getWorkplaceTemplatePath() throws CmsException {
		return getDataValue("path.wptemplates");
	}
	/**
	 * Gets the path for system picture files.
	 * @return Path for picture files.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getWpPicturePath() throws CmsException {
		return getDataValue("path.wppictures");
	}
	/**
	 * Gets the URL where workplace pisc reside.
	 * If the path is empty, the workplace picture path prfeixed
	 * by the servlet path will be returned
	 * @return Path for the "pics" mountpoint.
	 * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
	 */
	public String getWpPictureUrl() throws CmsException {
		String s = getDataValue("path.wppicsurl");
		if(s == null || "".equals(s)) {
			s = ((HttpServletRequest)m_cms.getRequestContext().getRequest().getOriginalRequest()).getServletPath() + getDataValue("path.wppictures");
		}
		return s; 
	}
	/**
	 * Gets the expected tagname for the XML documents of this content type
	 * @return Expected XML tagname.
	 */
	public String getXmlDocumentTagName() {
		return "WORKPLACEDEF";
	}
}
