
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsXmlWpButtonsDefFile.java,v $
* Date   : $Date: 2001/01/24 09:43:31 $
* Version: $Revision: 1.14 $
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
 * Content definition for the workplace button element definition file.
 * 
 * @author Alexander Lucas
 * @author Michael Emmerich
 * @version $Revision: 1.14 $ $Date: 2001/01/24 09:43:31 $
 */

public class CmsXmlWpButtonsDefFile extends A_CmsXmlContent implements I_CmsLogChannels,I_CmsWpConstants {
    
    /**
     * Default constructor.
     */
    
    public CmsXmlWpButtonsDefFile() throws CmsException {
        super();
    }
    
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */
    
    public CmsXmlWpButtonsDefFile(CmsObject cms, CmsFile file) throws CmsException {
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
    
    public CmsXmlWpButtonsDefFile(CmsObject cms, String filename) throws CmsException {
        super();
        init(cms, filename);
    }
    
    /**
     * Gets the processed data for a button.
     * @return Processed button.
     * @exception CmsException
     */
    
    public String getButton(String name, String action, String alt, 
            String href, Object callingObject) throws CmsException {
        setData("name", name);
        setData("action", action);
        setData("alt", alt);
        setData("href", href);
        return getProcessedDataValue("defaultbutton", callingObject);
    }
    
    /**
     * Gets the processed data for a button separator.
     * @return Processed button separator.
     * @exception CmsException
     */
    
    public String getButtonSeparator(Object callingObject) throws CmsException {
        return getProcessedDataValue("buttonseparator", callingObject);
    }
    
    /**
     * Gets the processed data for a submit button.
     * @return Processed button.
     * @exception CmsException
     */
    
    public String getButtonSubmit(String name, String action, String value, 
            String style, String width) throws CmsException {
        setData(C_BUTTON_NAME, name);
        setData(C_BUTTON_ACTION, action);
        setData(C_BUTTON_VALUE, value);
        setData(C_BUTTON_STYLE, style);
        setData(C_BUTTON_WIDTH, width);
        return getProcessedDataValue("submitbutton");
    }
    
    /**
     * Gets the processed data for a text button.
     * @return Processed button.
     * @exception CmsException
     */
    
    public String getButtonText(String name, String action, String value, 
            String style, String width) throws CmsException {
        setData(C_BUTTON_NAME, name);
        setData(C_BUTTON_ACTION, action);
        setData(C_BUTTON_VALUE, value);
        setData(C_BUTTON_STYLE, style);
        setData(C_BUTTON_WIDTH, width);
        return getProcessedDataValue("textbutton");
    }
    
    /**
     * Gets a description of this content type.
     * @return Content type description.
     */
    
    public String getContentDescription() {
        return "OpenCms workplace buttons definition";
    }
    
    /**
     * Gets the processed data for a deactivated button.
     * @return Processed button.
     * @exception CmsException
     */
    
    public String getDeactivatedButton(String name, String action, String alt, String href, 
            Object callingObject) throws CmsException {
        setData("name", name);
        setData("action", action);
        setData("alt", alt);
        setData("href", href);
        return getProcessedDataValue("deactivatedbutton", callingObject);
    }
    
    /**
     * Gets the processed data for a javascript button.
     * @return Processed button.
     * @exception CmsException
     */
    
    public String getJavascriptButton(String name, String action, String alt, 
            String href, Object callingObject) throws CmsException {
        setData("name", name);
        setData("action", action);
        setData("alt", alt);
        setData("href", href);
        return getProcessedDataValue("javascriptbutton", callingObject);
    }
    
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    
    public String getXmlDocumentTagName() {
        return "WP_BUTTONS";
    }
}
