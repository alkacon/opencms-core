package com.opencms.examples;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/examples/Attic/CmsExampleNavFile.java,v $
 * Date   : $Date: 2000/08/08 14:08:22 $
 * Version: $Revision: 1.4 $
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

/**
 * Sample content definition for a navigation template.
 * <P>
 * This class is used to access special XML data tags
 * used in navigation templates for defining the layout
 * of the navigation.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.4 $ $Date: 2000/08/08 14:08:22 $
 */
 public class CmsExampleNavFile extends CmsXmlTemplateFile {

	/**
	 * Default constructor.
	 */
	public CmsExampleNavFile() throws CmsException {
		super();
	}
	/**
	 * Constructor for creating a new object containing the content
	 * of the given filename.
	 * 
	 * @param cms CmsObject object for accessing system resources.
	 * @param filename Name of the body file that shoul be read.
	 */        
	public CmsExampleNavFile(CmsObject cms, CmsFile file) throws CmsException {
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
	public CmsExampleNavFile(CmsObject cms, String filename) throws CmsException {
		super();            
		init(cms, filename);
	}
	/**
	 * Gets a description of this content type.
	 * @return Content type description.
	 */
	public String getContentDescription() {
		return "OpenCms navigation template";
	}
	/**
	 * Gets a navigation entry for the currenty displayed page.
	 * This entry will not be displayed as link but only as 
	 * written text.
	 * The given title will be used to display the entry.
	 * This method makes use of the special XML tags
	 * <code>&lt;STARTSEQ&gt;</code>, <code>&lt;MIDDLESEQ&gt;</code> and <code>&lt;ENDSEQ&gt;</code> tag
	 * inside the <code>&lt;CURRENTPAGE&gt;</code> tag of the template file to 
	 * determine the start, middle and end HTML sequence of this entry.
	 * 
	 * @param title Title for this entry.
	 * @exception CmsException
	 */
	public String getCurrentNavEntry(String title) throws CmsException {
		return getDataValue("currentpage.startseq") + title + getDataValue("currentpage.endseq") + "\n";
	}
	/**
	 * Gets a navigation entry for the current section (folder).
	 * The given link and title will be used to display the entry.
	 * This method makes use of the special XML tags
	 * <code>&lt;STARTSEQ&gt;</code>, <code>&lt;MIDDLESEQ&gt;</code> and <code>&lt;ENDSEQ&gt;</code> tag
	 * inside the <code>&lt;SECTION&gt;</code> tag of the template file to 
	 * determine the start, middle and end HTML sequence of each section entry.
	 * 
	 * @param link URL that should be ued for the link.
	 * @param title Title for this link
	 * @exception CmsException
	 */
	public String getCurrentSectionNavEntry(String link, String title) throws CmsException {
		return getDataValue("currentsection.startseq") + link + getDataValue("currentsection.middleseq") + title + getDataValue("currentsection.endseq") + "\n";
	}
	/**
	 * Gets a navigation entry for a standard page entry.
	 * The given link and title will be used to display the entry.
	 * This method makes use of the special XML tags
	 * <code>&lt;STARTSEQ&gt;</code>, <code>&lt;MIDDLESEQ&gt;</code> and <code>&lt;ENDSEQ&gt;</code> tag
	 * inside the <code>&lt;PAGE&gt;</code> tag of the template file to 
	 * determine the start, middle and end HTML sequence of each page entry.
	 * 
	 * @param link URL that should be ued for the link.
	 * @param title Title for this link
	 * @exception CmsException
	 */
	public String getOtherNavEntry(String link, String title) throws CmsException {
		return getDataValue("page.startseq") + link + getDataValue("page.middleseq") + title + getDataValue("page.endseq") + "\n";
	}
	/**
	 * Gets a navigation entry for a starndard section (folder).
	 * The given link and title will be used to display the entry.
	 * This method makes use of the special XML tags
	 * <code>&lt;STARTSEQ&gt;</code>, <code>&lt;MIDDLESEQ&gt;</code> and <code>&lt;ENDSEQ&gt;</code> tag
	 * inside the <code>&lt;SECTION&gt;</code> tag of the template file to 
	 * determine the start, middle and end HTML sequence of each section entry.
	 * 
	 * @param link URL that should be ued for the link.
	 * @param title Title for this link
	 * @exception CmsException
	 */
	public String getOtherSectionNavEntry(String link, String title) throws CmsException {
		return getDataValue("section.startseq") + link + getDataValue("section.middleseq") + title + getDataValue("section.endseq") + "\n";
	}
	/**
	 * Gets the expected tagname for the XML documents of this content type
	 * @return Expected XML tagname.
	 */
	public String getXmlDocumentTagName() {
		return "XMLNAVTEMPLATE";
	}
}
