package com.opencms.workplace;

/*
 * File   : $File$
 * Date   : $Date: 2000/11/03 15:27:41 $
 * Version: $Revision: 1.1 $
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
 * Template class for displaying the conflicting Files for a new Module.
 * Creation date: (06.09.00 09:30:25)
 * @author: Hanjo Riege
 */
public class CmsAdminModuleAdminEdit extends CmsWorkplaceDefault implements I_CmsConstants {

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
		
		CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
		I_CmsSession session = cms.getRequestContext().getSession(true);
		I_CmsRegistry reg = cms.getRegistry();	
		Hashtable sessionData = (Hashtable)session.getValue(C_SESSION_MODULE_ADMIN_DATA);
		String module = (String) sessionData.get(C_MODULE_PACKETNAME);
		xmlTemplateDocument.setData("packetname", module);
		
		Vector paraNames	= (Vector)sessionData.get(C_SESSION_MODULE_ADMIN_PROP_NAMES);
		Vector paraDescr 	= (Vector)sessionData.get(C_SESSION_MODULE_ADMIN_PROP_DESCR);
		Vector paraTyp		= (Vector)sessionData.get(C_SESSION_MODULE_ADMIN_PROP_TYP);
		Vector paraVal		= (Vector)sessionData.get(C_SESSION_MODULE_ADMIN_PROP_VAL);

		String prop 	= (String)parameters.get("prop");
		String delete 	= (String)parameters.get("delete");
		String ok		= (String)parameters.get("ok");
		
		if ((prop == null)|| ("".equals(prop))){
			// new property
			if ((ok != null) && (! "".equals(ok))){
				// read new prop
				// TODO: errror handling: check if all fields are filled out and if the value is correct
				paraNames.addElement((String)parameters.get("NAME"));
				paraDescr.addElement(getStringValue((String)parameters.get("BESCHREIBUNG")));
				paraTyp.addElement(getStringValue((String)parameters.get("TYP")));
				paraVal.addElement(getStringValue((String)parameters.get("WERT")));
				templateSelector = "done";
				
			}else{
				xmlTemplateDocument.setData("paraname", "");
				xmlTemplateDocument.setData("value", "");
				xmlTemplateDocument.setData("description", "");
				xmlTemplateDocument.setData("delybutton"," ");
			}
			
		}else{
			if ((ok != null) && (! "".equals(ok))){
				// set property 
				int i = paraNames.indexOf(prop);
				paraNames.removeElementAt(i);
				paraDescr.removeElementAt(i);
				paraTyp.removeElementAt(i);
				paraVal.removeElementAt(i);
				// TODO: errror handling: check if all fields are filled out and if the value is correct
				paraNames.addElement(prop);
				paraDescr.addElement(getStringValue((String)parameters.get("BESCHREIBUNG")));
				paraTyp.addElement(getStringValue((String)parameters.get("TYP")));
				paraVal.addElement(getStringValue((String)parameters.get("WERT")));
				templateSelector = "done";
				
			}else if ((delete != null) && (! "".equals(delete))){
				// delete property
				int i = paraNames.indexOf(prop);
				paraNames.removeElementAt(i);
				paraDescr.removeElementAt(i);
				paraTyp.removeElementAt(i);
				paraVal.removeElementAt(i);
				templateSelector = "done";
				
			}else{
				// change property
				int i = paraNames.indexOf(prop);
				xmlTemplateDocument.setData("paraname", prop);
				xmlTemplateDocument.setData("nameentry", prop);
				xmlTemplateDocument.setData("value", (String)paraVal.elementAt(i));
				xmlTemplateDocument.setData("description", (String)paraDescr.elementAt(i));
				xmlTemplateDocument.setData((String)paraTyp.elementAt(i), "selected");
				xmlTemplateDocument.setData("delybutton",xmlTemplateDocument.getProcessedDataValue("deletebutton"));
			}
		}
		
			
		// Now load the template file and start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
	}
/**
 * returns the String or "" if it is null.
 * Creation date: (29.10.00 16:05:38)
 * @return java.lang.String
 * @param param java.lang.String
 */
private String getStringValue(String param) {
	if(param == null){
		return "";
	}
	return param;
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
