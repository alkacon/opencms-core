package com.opencms.workplace;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsModulelist.java,v $
 * Date   : $Date: 2000/11/06 14:35:57 $
 * Version: $Revision: 1.2 $
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

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.util.*;

import java.util.*;
import java.lang.reflect.*;

/**
 * Class for building modulelist. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;MODULELIST&gt;</code>.
 * 
 * Creation date: (31.08.00 15:16:10)
 * @author: Hanjo Riege
 * @Version: $Revision: 1.2 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsModulelist extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants {
	/**
	 * Handling of the special workplace <CODE>&lt;MODULELIST&gt;</CODE> tags.
	 * <P>
	 * Returns the processed code with the actual elements.
	 * <P>
	 * Projectlists can be referenced in any workplace template by <br>
	 * // TODO: insert correct syntax here!
	 * <CODE>&lt;MODULELIST /&gt;</CODE>
	 * 
	 * @param cms CmsObject Object for accessing resources.
	 * @param n XML element containing the <code>&lt;MODULELIST&gt;</code> tag.
	 * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
	 * @param callingObject reference to the calling object <em>(not used here)</em>.
	 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
	 * @param lang CmsXmlLanguageFile conataining the currently valid language file.
	 * @return Processed button.
	 * @exception CmsException
	 */    
	public Object handleSpecialWorkplaceTag(CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
		// Read projectlist parameters
		String listMethod = n.getAttribute(C_MODULELIST_METHOD);

		//Get the registry
		I_CmsRegistry reg = cms.getRegistry();
		
		// Get list definition and language values
		CmsXmlWpTemplateFile listdef = getModulelistDefinitions(cms);

		// call the method for generating projectlist elements
		Method callingMethod = null;
		Vector list = new Vector();
		try {
			callingMethod = callingObject.getClass().getMethod(listMethod, new Class[] {CmsObject.class, CmsXmlLanguageFile.class});
			list = (Vector)callingMethod.invoke(callingObject, new Object[] {cms, lang});
		} catch(NoSuchMethodException exc) {
			// The requested method was not found.
			throwException("Could not find method " + listMethod + " in calling class " + callingObject.getClass().getName() + " for generating projectlist content.", CmsException.C_NOT_FOUND);
		} catch(InvocationTargetException targetEx) {
			// the method could be invoked, but throwed a exception
			// itself. Get this exception and throw it again.              
			Throwable e = targetEx.getTargetException();
			if(!(e instanceof CmsException)) {
				// Only print an error if this is NO CmsException
				throwException("User method " + listMethod + " in calling class " + callingObject.getClass().getName() + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
			} else {
				// This is a CmsException
				// Error printing should be done previously.
				throw (CmsException)e;
			}
		} catch(Exception exc2) {
			throwException("User method " + listMethod + " in calling class " + callingObject.getClass().getName() + " was found but could not be invoked. " + exc2, CmsException.C_XML_NO_USER_METHOD);
		}
		
		// StringBuffer for the generated output
		StringBuffer result = new StringBuffer();
		
		for(int i = 0; i < list.size(); i++) 
		{
			String currentModule = (String)list.elementAt(i);
			listdef.setData(C_MODULELIST_NAME, currentModule);
			listdef.setData(C_MODULELIST_NICE_NAME, reg.getModuleNiceName(currentModule));
			listdef.setData(C_MODULELIST_VERSION, reg.getModuleVersion(currentModule)+"");
			listdef.setData(C_MODULELIST_AUTHOR, reg.getModuleAuthor(currentModule));
			listdef.setData(C_MODULELIST_DATECREATED, Utils.getNiceDate(reg.getModuleCreateDate(currentModule)));
			listdef.setData(C_MODULELIST_DATEUPLOADED, Utils.getNiceDate(reg.getModuleUploadDate(currentModule)));
			listdef.setData(C_MODULELIST_IDX, new Integer(i).toString());
			result.append(listdef.getProcessedDataValue(C_TAG_MODULELIST_DEFAULT, callingObject, parameters));
		}		
		return result.toString();
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
