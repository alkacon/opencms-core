package com.opencms.web;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/web/Attic/CmsSimpleNavFile.java,v $
 * Date   : $Date: 2000/08/08 14:08:29 $
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
import com.opencms.examples.*;

import java.util.*;


/**
 * Content definition for minfact CeBIT navigation definition file.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.4 $ $Date: 2000/08/08 14:08:29 $
 */
 public class CmsSimpleNavFile extends CmsExampleNavFile {

	/**
	 * Default constructor.
	 */
	public CmsSimpleNavFile() throws CmsException {
		super();
	}
	/**
	 * Constructor for creating a new object containing the content
	 * of the given filename.
	 * 
	 * @param cms CmsObject object for accessing system resources.
	 * @param filename Name of the body file that shoul be read.
	 */        
	public CmsSimpleNavFile(CmsObject cms, CmsFile file) throws CmsException {
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
	public CmsSimpleNavFile(CmsObject cms, String filename) throws CmsException {
		super();            
		init(cms, filename);
	}
	/**
	 * Gets a description of this content type.
	 * @return Content type description.
	 */
	public String getContentDescription() {
		return "OpenCms navigation template for mindfact CeBIT nav";
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
		return getDataValue("startseqcurr") + title + getDataValue("endseqcurr") + "\n";
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
		// Not used in this navigation
		return "";
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
		return getDataValue("startseq") + link + getDataValue("middleseq") + title + getDataValue("endseq") + "\n";
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
		// Not used in this navigation
		return "";
	}
}
