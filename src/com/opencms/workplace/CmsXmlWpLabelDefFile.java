
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsXmlWpLabelDefFile.java,v $
* Date   : $Date: 2001/01/24 09:43:31 $
* Version: $Revision: 1.8 $
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

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.util.*;

/**
 *  Content definition for the workplace label element definition file.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.8 $ $Date: 2001/01/24 09:43:31 $
 **/

public class CmsXmlWpLabelDefFile extends A_CmsXmlContent implements I_CmsLogChannels,I_CmsWpConstants {
    
    /**
     * Default constructor.
     */
    
    public CmsXmlWpLabelDefFile() throws CmsException {
        super();
    }
    
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */
    
    public CmsXmlWpLabelDefFile(CmsObject cms, CmsFile file) throws CmsException {
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
    
    public CmsXmlWpLabelDefFile(CmsObject cms, String filename) throws CmsException {
        super();
        init(cms, filename);
    }
    
    /**
     * Gets a description of this content type.
     * @return Content type description.
     */
    
    public String getContentDescription() {
        return "OpenCms workplace labels";
    }
    
    /**
     * Gets the processed data for a label.
     * @param value The value of this label.
     * @return Processed label.
     * @exception CmsException
     */
    
    public String getLabel(String value) throws CmsException {
        setData(C_LABEL_VALUE, value);
        return getProcessedDataValue(C_TAG_LABEL);
    }
    
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    
    public String getXmlDocumentTagName() {
        return "WP_LABELS";
    }
}
