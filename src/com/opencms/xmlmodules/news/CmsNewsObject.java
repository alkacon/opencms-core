package com.opencms.xmlmodules.news;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/xmlmodules/news/Attic/CmsNewsObject.java,v $
 * Date   : $Date: 2000/08/08 14:08:34 $
 * Version: $Revision: 1.3 $
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
import com.opencms.util.*;
import com.opencms.xmlmodules.*;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Content definition for articles in the news module.
 * 
 * @author Matthias Schreiber
 * @version $Revision: 1.3 $ $Date: 2000/08/08 14:08:34 $
 */
 public class CmsNewsObject extends A_CmsModuleObject implements I_CmsNewsConstants { 
	
 
	 /**
	 * Default constructor.
	 */
	public CmsNewsObject() throws CmsException {
		super();
	}
	/**
	 * Constructor for creating a new object containing the content
	 * of the given filename.
	 * 
	 * @param cms A_CmsObject object for accessing system resources.
	 * @param filename Name of the body file that should be read.
	 */        
	public CmsNewsObject(CmsObject cms, CmsFile file) throws CmsException {
		super();
		init(cms, file);
	}
	/**
	 * Constructor for creating a new object containing the content
	 * of the given filename.
	 * 
	 * @param cms A_CmsObject object for accessing system resources.
	 * @param filename Name of the body file that should be read.
	 */        
	public CmsNewsObject(CmsObject cms, String filename) throws CmsException {
		super();            
		init(cms, filename);
	}
	/**
	 * Gets a description of this content type.
	 * @return Content type description.
	 */
	public String getContentDescription() {
		return "OpenCms news article";
	}
	/**
	 * Gets the external link.
	 * @return external link..
	 */
	public String getExternalLink() throws CmsException {
		String parValue = getDataValue(C_XML_EXTLINK);
		//no URL is specified 
		if (parValue != null && !"".equals(parValue)) {
			if (parValue.equals(C_DEF_PROTOCOL)) {
				parValue = "";
			}
		}
		return parValue;
	}
	/**
	 * Gets the article short text.
	 * @return Article short text.
	 */
	public String getShortText() throws CmsException {
		String parValue = Utils.removeLineBreaks(getDataValue(C_XML_SHORTTEXT));
		return parValue;
	}
	 /**
	 * Gets the expected tagname for the XML documents of this content type
	 * @return Expected XML tagname.
	 */
	public String getXmlDocumentTagName() {
		return "NEWSARTICLE";
	}
	/**
	 * Set an external link.
	 * @param url URL of the external linkk.
	 */
	public void setExternalLink(String url) {
		setData(C_XML_EXTLINK, url);
	}
	/**
	 * Set the article short text.
	 * @param text Article short text.
	 */
	public void setShortText(String text) {
		setData(C_XML_SHORTTEXT, text);
	}
}  
