
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsXmlWpInputDefFile.java,v $
* Date   : $Date: 2001/01/24 09:43:31 $
* Version: $Revision: 1.13 $
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
 * Content definition for the workplace input element definition file.
 * 
 * @author Michael Emmerich
 * @author Alexander Lucas
 * @version $Revision: 1.13 $ $Date: 2001/01/24 09:43:31 $
 */

public class CmsXmlWpInputDefFile extends A_CmsXmlContent implements I_CmsLogChannels,I_CmsWpConstants {
    
    /**
     * Default constructor.
     */
    
    public CmsXmlWpInputDefFile() throws CmsException {
        super();
    }
    
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */
    
    public CmsXmlWpInputDefFile(CmsObject cms, CmsFile file) throws CmsException {
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
    
    public CmsXmlWpInputDefFile(CmsObject cms, String filename) throws CmsException {
        super();
        init(cms, filename);
    }
    
    /**
     * Gets a description of this content type.
     * @return Content type description.
     */
    
    public String getContentDescription() {
        return "OpenCms workplace inputs";
    }
    
    /**
     * Gets the processed data for a input field.
     * @param styleClass The style class of this input field.
     * @param name The name of this input field.
     * @param size The size of this input field
     * @param length The input length of this input field.
     * @param value The value of this input field.
     * @param action The action of this input field.
     * @return Processed input field.
     * @exception CmsException
     */
    
    public String getInput(String styleClass, String name, String size, String length, 
            String value, String action) throws CmsException {
        setData(C_INPUT_CLASS, styleClass);
        setData(C_INPUT_NAME, name);
        setData(C_INPUT_SIZE, size);
        setData(C_INPUT_LENGTH, length);
        setData(C_INPUT_VALUE, value);
        setData(C_INPUT_ACTION, action);
        return getProcessedDataValue(C_TAG_INPUTFIELD);
    }
    
    /**
     * Gets the processed data for a password field.
     * @param styleClass The style class of this password field.
     * @param name The name of this password field.
     * @param size The size of this password field
     * @param length The input length of this password field.
     * @return Processed password field.
     * @exception CmsException
     */
    
    public String getPassword(String styleClass, String name, String size, String length) throws CmsException {
        setData(C_INPUT_CLASS, styleClass);
        setData(C_INPUT_NAME, name);
        setData(C_INPUT_SIZE, size);
        setData(C_INPUT_LENGTH, length);
        return getProcessedDataValue(C_TAG_PASSWORD);
    }
    
    public String getSelectBoxEnd() throws CmsException {
        return getProcessedDataValue(C_TAG_SELECTBOX_END);
    }
    
    public String getSelectBoxOption(String name, String value) throws CmsException {
        setData(C_SELECTBOX_OPTIONNAME, name);
        setData(C_SELECTBOX_OPTIONVALUE, value);
        return getProcessedDataValue(C_TAG_SELECTBOX_OPTION);
    }
    
    public String getSelectBoxSelOption(String name, String value) throws CmsException {
        setData(C_SELECTBOX_OPTIONNAME, name);
        setData(C_SELECTBOX_OPTIONVALUE, value);
        return getProcessedDataValue(C_TAG_SELECTBOX_SELOPTION);
    }
    
    public String getSelectBoxStart(String classname, String name, String width, 
            String onchange, String size) throws CmsException {
        if(classname == null || "".equals(classname)) {
            setData(C_SELECTBOX_CLASS, "");
        }
        else {
            setData(C_SELECTBOX_CLASSNAME, classname);
            setData(C_SELECTBOX_CLASS, getProcessedData(C_TAG_SELECTBOX_CLASS));
        }
        setData(C_SELECTBOX_NAME, name);
        setData(C_SELECTBOX_WIDTH, width);
        setData(C_SELECTBOX_ONCHANGE, onchange);
        setData(C_SELECTBOX_SIZE, size);
        return getProcessedDataValue(C_TAG_SELECTBOX_START);
    }
    
    public String getSelectBoxStartDiv(String classname, String name, String width, 
            String onchange, String size) throws CmsException {
        if(classname == null || "".equals(classname)) {
            setData(C_SELECTBOX_CLASS, "");
        }
        else {
            setData(C_SELECTBOX_CLASSNAME, classname);
            setData(C_SELECTBOX_CLASS, getProcessedData(C_TAG_SELECTBOX_CLASS));
        }
        setData(C_SELECTBOX_NAME, name);
        setData(C_SELECTBOX_WIDTH, width);
        setData(C_SELECTBOX_ONCHANGE, onchange);
        setData(C_SELECTBOX_SIZE, size);
        return getProcessedDataValue(C_TAG_SELECTBOX_START_DIV);
    }
    
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    
    public String getXmlDocumentTagName() {
        return "WP_INPUTS";
    }
}
