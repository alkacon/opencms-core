package com.opencms.web;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/web/Attic/CmsSimpleNav.java,v $
 * Date   : $Date: 2000/08/08 14:08:29 $
 * Version: $Revision: 1.9 $
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

import java.util.*;
import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.template.*;
import com.opencms.examples.*;

/**
 * Template class for displaying a simple navigation
 * used for the CeBIT online application form.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.9 $ $Date: 2000/08/08 14:08:29 $
 */
public class CmsSimpleNav extends CmsExampleNav {
	
	/** Describes the folder whose navigation should be built */
	static final String C_NAVFOLDER = "/cebitlive/";
	
	/**
	 * Handles any occurence of an <code>&lt;ELEMENT&gt;</code> tag.
	 * <P>
	 * Every XML template class should use CmsXmlTemplateFile as
	 * the interface to the XML file. Since CmsXmlTemplateFile is
	 * an extension of A_CmsXmlContent by the additional tag
	 * <code>&lt;ELEMENT&gt;</code> this user method ist mandatory.
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param tagcontent Unused in this special case of a user method. Can be ignored.
	 * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
	 * @param userObj Hashtable with parameters.
	 * @return String or byte[] with the content of this subelement.
	 * @exception CmsException
	 */
	public Object getNav(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
			throws CmsException {

		return filesNav(cms, C_NAVFOLDER, (CmsSimpleNavFile)doc);        
	}
	/**
	 * Reads in the template file and starts the XML parser for the expected
	 * content type <class>CmsSimpleNavFile</code>
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param templateFile Filename of the template file.
	 * @param elementName Element name of this template in our parent template.
	 * @param parameters Hashtable with all template class parameters.
	 * @param templateSelector template section that should be processed.
	 */
	public CmsXmlTemplateFile getOwnTemplateFile(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
		CmsSimpleNavFile xmlTemplateDocument = new CmsSimpleNavFile(cms, templateFile);       
		return xmlTemplateDocument;
	}
}
