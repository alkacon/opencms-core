/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/examples/news/Attic/CmsNewsListDefFile.java,v $
 * Date   : $Date: 2000/03/15 13:52:37 $
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
package com.opencms.examples.news;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.util.*;

import java.util.*;


/**
 * Sample content definition for news list entry definitions.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2000/03/15 13:52:37 $
 */
 public class CmsNewsListDefFile extends CmsXmlTemplateFile {

    /**
     * Default constructor.
     */
    public CmsNewsListDefFile() throws CmsException {
        super();
    }
    
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsNewsListDefFile(A_CmsObject cms, CmsFile file) throws CmsException {
        super();
        init(cms, file);
    }

    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsNewsListDefFile(A_CmsObject cms, String filename) throws CmsException {
        super();            
        init(cms, filename);
    }
    
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return "NEWSLISTDEF";
    }

    /**
     * Gets a description of this content type.
     * @return Content type description.
     */
    public String getContentDescription() {
        return "OpenCms news list entry definition";
    }
            
    /**
     * Gets a processed news list entry for the given values.
     * @return News list entry.
     */
    public String getNewsListEntry(String date, String headline, String shorttext, String link) throws CmsException{
        setData("date", date);
        setData("headline", headline);
        setData("shorttext", shorttext);        
        setData("link", link);
        return getProcessedDataValue("newslistentry");
    }
 }
