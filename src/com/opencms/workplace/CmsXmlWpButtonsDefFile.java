/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsXmlWpButtonsDefFile.java,v $
* Date   : $Date: 2004/02/13 13:41:44 $
* Version: $Revision: 1.20 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


package com.opencms.workplace;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;

import com.opencms.template.A_CmsXmlContent;

/**
 * Content definition for the workplace button element definition file.
 * 
 * @author Alexander Lucas
 * @author Michael Emmerich
 * @version $Revision: 1.20 $ $Date: 2004/02/13 13:41:44 $
 */

public class CmsXmlWpButtonsDefFile extends A_CmsXmlContent {
    
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
     * @throws CmsException
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
     * @throws CmsException
     */
    
    public String getButtonSeparator(Object callingObject) throws CmsException {
        return getProcessedDataValue("buttonseparator", callingObject);
    }
    
    /**
     * Gets the processed data for a submit button.
     * @return Processed button.
     * @throws CmsException
     */
    
    public String getButtonSubmit(String name, String action, String value, 
            String style, String width) throws CmsException {
        setData(I_CmsWpConstants.C_BUTTON_NAME, name);
        setData(I_CmsWpConstants.C_BUTTON_ACTION, action);
        setData(I_CmsWpConstants.C_BUTTON_VALUE, value);
        setData(I_CmsWpConstants.C_BUTTON_STYLE, style);
        setData(I_CmsWpConstants.C_BUTTON_WIDTH, width);
        return getProcessedDataValue("submitbutton");
    }
    
    /**
     * Gets the processed data for a text button.
     * @return Processed button.
     * @throws CmsException
     */
    
    public String getButtonText(String name, String action, String value, 
            String style, String width) throws CmsException {
        setData(I_CmsWpConstants.C_BUTTON_NAME, name);
        setData(I_CmsWpConstants.C_BUTTON_ACTION, action);
        setData(I_CmsWpConstants.C_BUTTON_VALUE, value);
        setData(I_CmsWpConstants.C_BUTTON_STYLE, style);
        setData(I_CmsWpConstants.C_BUTTON_WIDTH, width);
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
     * @throws CmsException
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
     * @throws CmsException
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
