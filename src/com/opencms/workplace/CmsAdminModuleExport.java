package com.opencms.workplace;

/*
 * File   : $File$
 * Date   : $Date: 2001/01/18 15:34:07 $
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

import java.util.*;

import javax.servlet.http.*;

/**
 * Template class for displaying OpenCms workplace administration module create.
 *
 * Creation date: (27.10.00 10:28:08)
 * @author: Hanjo Riege
 */
public class CmsAdminModuleExport extends CmsWorkplaceDefault implements I_CmsConstants {

/*	private final String C_PACKETNAME	= "packetname";
	private final String C_VERSION		= "version";
	private final String C_MODULENAME	= "modulename";
	private final String C_DESCRIPTION	= "description";
	private final String C_VIEW			= "view";
	private final String C_ADMINPOINT	= "adminpoint";
	private final String C_MAINTENANCE	= "maintenance";
	private final String C_AUTHOR 		= "author";
	private final String C_EMAIL 		= "email";
	private final String C_DATE 		= "date";
	private final String C_SESSION_DATA	= "module_create_data";
	*/
	private final String C_MODULE	= "module";
	private final String C_ACTION		 	= "action";
	private final String C_NAME_PARAMETER	= "module";
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
		
		CmsXmlTemplateFile templateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
		CmsRequestContext reqCont = cms.getRequestContext();   
		I_CmsRegistry reg = cms.getRegistry();	
		String step = (String)parameters.get(C_ACTION);
		String moduleName = (String)parameters.get(C_MODULE);

		if ((step != null)&&("ok".equals(step))){
			// export
			String exportName = (String)parameters.get("modulename");
			
			String[] resourcen = new String[4];
			resourcen[0] = "/system/modules/"+exportName+"/";
			resourcen[1] = "/system/classes/" + exportName.replace('.','/') +"/";
			resourcen[2] = "/moduledemos/"+exportName+"/";
			resourcen[3] = "/content/bodys/moduledemos/" + exportName + "/";

			// TODO: this is just a Hack
			for (int i=1; i<4; i++){
				try{
					cms.readFileHeader(resourcen[i]);
				}catch(CmsException e){
					System.err.println("error exporting module: couldn't add "+resourcen[i]+" to Module\n"+Utils.getStackTrace(e));
					resourcen[i] = resourcen[0];
				}
			}
			try{
				cms.readFileHeader(resourcen[0]);
			}catch(CmsException e){
				System.err.println("error exporting module: couldn't add "+resourcen[0]+" to Module\n"+"You dont have this module in this project!");
				return startProcessing(cms, templateDocument, elementName, parameters, "done");
			}
			// end hack
			
			reg.exportModule(exportName, resourcen, cms.readExportPath()+exportName + "_" + reg.getModuleVersion(exportName));
			templateSelector= "done";
		}else{
			
			// first call
			templateDocument.setData("modulename", moduleName);
		}

 	
		// Now load the template file and start the processing
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
}
