package com.opencms.workplace;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdministration.java,v $
 * Date   : $Date: 2000/10/31 13:11:29 $
 * Version: $Revision: 1.6 $
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
import java.lang.reflect.*;

import javax.servlet.http.*;


/**
 * This class is used to display the administration view.
 * 
 * Creation date: (09.08.00 14:01:21)
 * @author: Hanjo Riege
 * @version $Name:  $ $Revision: 1.6 $ $Date: 2000/10/31 13:11:29 $
 */
public class CmsAdministration extends CmsWorkplaceDefault implements I_CmsConstants {

	
	/** The number of elements per row */
	private static int C_ELEMENT_PER_ROW = 4;
	
	private static String C_ADMIN_PATH = "system/workplace/action/administration_content_top.html";

   /**
	* Returns the complete Icon.  
	*  
	* @param cms CmsObject Object for accessing system resources
	* @param templateDocument contains the icondefinition.
	* @param parameters Hashtable containing all user parameters.
	* @param lang CmsXmlLanguageFile conataining the currently valid language file.
	* @param picName the basic name of the picture.
	* @param sender the name of the icon incl. path.
	* @param languageKey the the key for the languagefile.
	* @param iconActiveMethod the method for decision if icon is active, or null.
	* @param iconVisibleMethod the method for decision if icon is visible,  or null. 
	*/
	private String generateIcon(CmsObject cms, CmsXmlTemplateFile templateDocument, 
								Hashtable parameters, CmsXmlLanguageFile lang, String picName, 
								String sender, String languageKey,  
								String iconActiveMethod, String iconVisibleMethod ) throws CmsException {
	
	    // call the method for activation decision
		boolean activate=true;
		
		if(iconActiveMethod != null && ! "".equals(iconActiveMethod))
		{
			CmsTemplateClassManager callingObject = new CmsTemplateClassManager();
			String className = iconActiveMethod.substring(0, iconActiveMethod.lastIndexOf("."));
			iconActiveMethod = iconActiveMethod.substring(iconActiveMethod.lastIndexOf(".")+1);
			Method groupsMethod = null;
			try {
				Object o = callingObject.getClassInstance(cms, className);
				groupsMethod = o.getClass().getMethod(iconActiveMethod, new Class[] {CmsObject.class, CmsXmlLanguageFile.class, Hashtable.class});
				activate = ((Boolean)groupsMethod.invoke(o, new Object[] {cms, lang, parameters})).booleanValue();
			} catch(NoSuchMethodException exc) {
			// The requested method was not found.
			throwException("Could not find icon activation method " + iconActiveMethod + " in calling class " + className + " for generating icon.", CmsException.C_NOT_FOUND);
			} catch(InvocationTargetException targetEx) {
				// the method could be invoked, but throwed a exception
				// itself. Get this exception and throw it again.              
				Throwable e = targetEx.getTargetException();
				if(!(e instanceof CmsException)) {
					// Only print an error if this is NO CmsException
					e.printStackTrace();
					throwException("Icon activation method " + iconActiveMethod + " in calling class " + className + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
				} else {
					// This is a CmsException
					// Error printing should be done previously.
					throw (CmsException)e;
				}
			} catch(Exception exc2) {
				throwException("Icon activation method " + iconActiveMethod + " in calling class " + className + " was found but could not be invoked. " + exc2, CmsException.C_UNKNOWN_EXCEPTION);
			}
		}
		
		// call the method for the visibility decision
		boolean visible=true; 
		
		if(iconVisibleMethod != null && ! "".equals(iconVisibleMethod))
		{
			CmsTemplateClassManager callingObject = new CmsTemplateClassManager();
			String className = iconVisibleMethod.substring(0, iconVisibleMethod.lastIndexOf("."));
			iconVisibleMethod = iconVisibleMethod.substring(iconVisibleMethod.lastIndexOf(".")+1);
			Method groupsMethod = null;
			try {
				Object o = callingObject.getClassInstance(cms, className);
				groupsMethod = o.getClass().getMethod(iconVisibleMethod, new Class[] {CmsObject.class, CmsXmlLanguageFile.class, Hashtable.class});
				visible = ((Boolean)groupsMethod.invoke(o, new Object[] {cms, lang, parameters})).booleanValue();
			} catch(NoSuchMethodException exc) {
			// The requested method was not found.
			throwException("Could not find icon activation method " + iconVisibleMethod + " in calling class " + className + " for generating icon.", CmsException.C_NOT_FOUND);
			} catch(InvocationTargetException targetEx) {
				// the method could be invoked, but throwed a exception
				// itself. Get this exception and throw it again.              
				Throwable e = targetEx.getTargetException();
				if(!(e instanceof CmsException)) {
					// Only print an error if this is NO CmsException
					e.printStackTrace();
					throwException("Icon activation method " + iconVisibleMethod + " in calling class " + className + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
				} else {
					// This is a CmsException
					// Error printing should be done previously.
					throw (CmsException)e;
				}
			} catch(Exception exc2) {
				throwException("Icon activation method " + iconVisibleMethod + " in calling class " + className + " was found but could not be invoked. " + exc2, CmsException.C_UNKNOWN_EXCEPTION);
			}
		}
		templateDocument.setData("linkTo",this.getServletPath(cms,"",null,null)+C_ADMIN_PATH+"?"+"sender="+sender);
		StringBuffer iconLabelBuffer = new StringBuffer(lang.getLanguageValue(languageKey));
		// Insert a html-break, if needed
		if( iconLabelBuffer.toString().indexOf("- ") != -1 ) {
			iconLabelBuffer.insert(iconLabelBuffer.toString().indexOf("- ") + 2, "<BR>");
		}
		templateDocument.setData("linkName",iconLabelBuffer.toString());
						
		if (visible){
			if(activate) {
				templateDocument.setData("picture",(String)picsUrl(cms, "", null, null) + picName + ".gif");
				return templateDocument.getProcessedDataValue("defaulticon");
			} else {
				templateDocument.setData("picture",(String)picsUrl(cms, "", null, null) + picName + "_in.gif");
				return templateDocument.getProcessedDataValue("deactivatedicon");
			}
		} else {
			return templateDocument.getProcessedDataValue("noicon");
		}
	} // of generateIcon
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
			A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "getting content of element " + ((elementName==null)?"<root>":elementName));
			A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "template file is: " + templateFile);
			A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
		}
		I_CmsSession session = cms.getRequestContext().getSession(true);
		CmsXmlWpTemplateFile templateDocument = new CmsXmlWpTemplateFile(cms,templateFile);
		CmsXmlLanguageFile lang = templateDocument.getLanguageFile();   
//		CmsXmlTemplateFile templateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
		String navPos = (String)session.getValue(C_SESSION_ADMIN_POS);
		templateDocument.setData("emptyPic", (String)picsUrl(cms,"empty.gif",null,null));
		CmsXmlWpConfigFile confFile = new CmsXmlWpConfigFile(cms); 
		String sendBy = (String)parameters.get("sender"); 
		if (sendBy == null){
			if (navPos == null){
				sendBy = confFile.getWorkplaceAdministrationPath();
			}else{
				if(!navPos.endsWith("/")){
					navPos = navPos.substring(0,navPos.indexOf("/")+1);
				}	
				sendBy = navPos;
			}
		}
		session.putValue(C_SESSION_ADMIN_POS, sendBy);
		Vector iconVector = cms.getSubFolders(sendBy);
		Vector iconVector2 = cms.getFilesInFolder(sendBy);
		int numFolders = iconVector.size();
		if (numFolders > 0){
			String iconNames[] = new String[numFolders];
			int index[] = new int[numFolders];
			String folderTitles[] = new String[numFolders];
			String folderLangKeys[] = new String[numFolders];
			String folderPos[] = new String[numFolders];
			String folderVisible[] = new String[numFolders];
			String folderActiv[] = new String[numFolders];
		
			for (int i=0; i<numFolders;i++){
				CmsResource aktIcon = (CmsResource)iconVector.elementAt(i);
				try {
					iconNames[i] = aktIcon.getAbsolutePath();
					index[i] = i;
					Hashtable propertyinfos = cms.readAllProperties(iconNames[i]);
					folderLangKeys[i] = (String) propertyinfos.get(C_PROPERTY_NAVTEXT);
					folderTitles[i]   = (String) propertyinfos.get(C_PROPERTY_TITLE);
					folderPos[i]      = (String) propertyinfos.get(C_PROPERTY_NAVPOS);
					folderVisible[i]  = (String) propertyinfos.get(C_PROPERTY_VISIBLE);
					folderActiv[i]    = (String) propertyinfos.get(C_PROPERTY_ACTIV);
				} catch( Exception exc ) {
					throw new CmsException("[" + this.getClass().getName() + "] "+exc.getMessage(),CmsException.C_SQL_ERROR, exc);
				}	
			} // end of for
			sort(iconNames, index, folderPos, numFolders);
			String completeTable = "";
			int element = 0;
			int zeile = 0;
			while( element < numFolders){
				String completeRow = "";
				while((element < numFolders) && (element < (zeile+1)*C_ELEMENT_PER_ROW)){
					int pos = index[element];
					completeRow += generateIcon(cms, templateDocument, parameters, lang, folderTitles[pos], iconNames[element], 
												folderLangKeys[pos], folderActiv[pos], folderVisible[pos]);
					element++;
				}
				templateDocument.setData("entrys", completeRow);
				completeTable += templateDocument.getProcessedDataValue("list_row");
				zeile++;
			} // of while
			templateDocument.setData("iconTable",completeTable);
		}else{
			// no Folders, just a real page
			try{
				cms.getRequestContext().getResponse().sendCmsRedirect( sendBy + "index.html?initial=true");
			} catch (Exception e) {
				throw new CmsException("Redirect fails :"+ ((CmsFile)iconVector2.elementAt(0)).getAbsolutePath(),CmsException.C_UNKNOWN_EXCEPTION,e);
			} 
			return null; 
		}
		return startProcessing(cms, templateDocument, elementName, parameters, templateSelector);
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
	* Sorts a set of arrays containing navigation information depending on 
	* their navigation positions.
	* @param filenames Array of filenames
	* @param index Array of associate Strings
	* @param positions Array of navpostions
	*/
	private void sort(String[] filenames, int[] index, String[] positions, int max){
		// Sorting algorithm
		// This method uses an bubble sort, so replace this with something more
		// efficient
	 
		for (int i=max-1;i>0;i--) {
			for (int j=0;j<i;j++) {
			  
				float a=new Float(positions[j]).floatValue();
				float b=new Float(positions[j+1]).floatValue();
				if (a > b) {
					String tempfilename= filenames[j];
					int tempindex = index[j];
					String tempposition = positions[j];
					
					filenames[j]=filenames[j+1];
					index[j]=index[j+1];
					positions[j]=positions[j+1];
					
					filenames[j+1]=tempfilename;
					index[j+1]=tempindex;
					positions[j+1]=tempposition;                    
				}
			}
		}
	}  // of sort
}
