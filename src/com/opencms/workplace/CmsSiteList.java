package com.opencms.workplace;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsSiteList.java,v $
 * Date   : $Date: 2000/09/19 14:36:20 $
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
 * Template class for displaying a site list
 * Creation date: (09/19/00 13:41:48)
 * @author: Finn Nielsen
 */
public class CmsSiteList extends com.opencms.template.CmsXmlTemplate {
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
public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector)
throws CmsException
{
	CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
	StringBuffer list = new StringBuffer();
	CmsSite site = null;

	Vector sites = cms.getAllSites();
	for (int i = 0; i < sites.size(); i++)
	{
		site = (CmsSite)sites.elementAt(i);

		xmlTemplateDocument.setData("id", ""+site.getId());
		xmlTemplateDocument.setData("name", site.getName());
		xmlTemplateDocument.setData("description", site.getDescription());
			
		list.append(xmlTemplateDocument.getProcessedData("row"));
	}
	
	xmlTemplateDocument.setData("list", list.toString())	;
	return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
}
}
