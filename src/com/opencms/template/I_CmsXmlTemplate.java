package com.opencms.template;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/I_CmsXmlTemplate.java,v $
 * Date   : $Date: 2000/08/08 14:08:29 $
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

import java.util.*;

/**
 * Interface for OpenCms XML template classes.
 * <P>
 * All methods extending the functionality of the common
 * template class interface to the special behaviour
 * of XML template classes may be defined here.
 * <P>
 * Primarily, this interface is important for the launcher, 
 * NOT for the template engine.
 * The CmsXmlLauncher can launch all templates that
 * implement the I_CmsXmlTemplate interface. 
 * <P>
 * For subtemplates, the I_CmsTemplate interface is
 * all that must be implemented, so you can load all kind
 * of templates (eg. type I_CmsDumpTemplate) as subtemplate.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.6 $ $Date: 2000/08/08 14:08:29 $
 */
public interface I_CmsXmlTemplate extends I_CmsTemplate {
	
	/**
	 * Reads in the template file and starts the XML parser for the expected
	 * content type.
	 * <P>
	 * Every extending class not using CmsXmlTemplateFile as content type
	 * should override this method.
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param templateFile Filename of the template file.
	 * @param elementName Element name of this template in our parent template.
	 * @param parameters Hashtable with all template class parameters.
	 * @param templateSelector template section that should be processed.
	 */
	public CmsXmlTemplateFile getOwnTemplateFile(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException;
	/**
	 * Handles any occurence of an "ELEMENT" tag.
	 * <P>
	 * Every XML template class should use CmsXmlTemplateFile as
	 * the interface to the XML file. Since CmsXmlTemplateFile is
	 * an extension of A_CmsXmlContent by the additional tag
	 * "ELEMENT" this user method ist mandatory.
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param tagcontent Unused in this special case of a user method. Can be ignored.
	 * @param doc Reference to the A_CmsXmlContent object the initiating XLM document.  
	 * @param userObj Hashtable with parameters.
	 * @return String or byte[] with the content of this subelement.
	 * @exception CmsException
	 */
	public Object templateElement(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
			throws CmsException;
}
