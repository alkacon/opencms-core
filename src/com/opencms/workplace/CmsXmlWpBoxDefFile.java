/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsXmlWpBoxDefFile.java,v $
* Date   : $Date: 2005/02/18 14:23:16 $
* Version: $Revision: 1.18 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001-2005  The OpenCms Group
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
import org.opencms.workplace.*;

import com.opencms.template.A_CmsXmlContent;

/**
 *  Content definition for the workplace messagebox element definition file.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.18 $ $Date: 2005/02/18 14:23:16 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 **/

public class CmsXmlWpBoxDefFile extends A_CmsXmlContent implements I_CmsWpConstants {
    
    /**
     * Default constructor.
     */
    
    public CmsXmlWpBoxDefFile() throws CmsException {
        super();
    }
    
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */
    
    public CmsXmlWpBoxDefFile(CmsObject cms, CmsFile file) throws CmsException {
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
    
    public CmsXmlWpBoxDefFile(CmsObject cms, String filename) throws CmsException {
        super();
        init(cms, filename);
    }
    
    /**
     * Gets a description of this content type.
     * @return Content type description.
     */
    
    public String getContentDescription() {
        return "OpenCms workplace messagebox";
    }
    
    public String getErrorpage(String title, String message, String reason, 
            String suggestion, String link, String msgReason, String msgButton) throws CmsException {
        setData(C_ERROR_TITLE, title);
        setData(C_ERROR_MESSAGE, message);
        setData(C_ERROR_REASON, reason);
        setData(C_ERROR_SUGGESTION, suggestion);
        setData(C_ERROR_LINK, link);
        setData(C_ERROR_MSG_REASON, msgReason);
        setData(C_ERROR_MSG_BUTTON, msgButton);
        return getProcessedDataValue(C_TAG_ERRORPAGE);
    }
    
    /**
     * Gets the processed data for a errorbox.
     * @param title The title of this errorbox.
     * @param message The message of this errorbox.
     * @param reason The reason of this errorbox.
     * @param suggestion The suggestion of this errorbox.
     * @param link The reference where this errorbox forwards to.
     * @param msgReason Fixed reason text in errorbox.
     * @param msgButton Fixed button text.
     * @return Processed errorbox.
     * @throws CmsException
     */
    
    /**
     * Gets the processed data for a messagebox.
     * @param title The title of this messagebox.
     * @param message1 The first message of this messagebox.
     * @param message2 The second message of this messagebox.
     * @param button1 The first button of this messagebox.
     * @param button2 The second button of this messagebox.
     * @param link1 The link of button1 of this messagebox.
     * @param link2 The link of button2 of this messagebox.
     * @return Processed messagebox.
     * @throws CmsException
     */
    
    public String getMessagebox(String title, String message1, String message2, 
            String button1, String button2, String link1, String link2) throws CmsException {
        setData(C_MESSAGE_TITLE, title);
        setData(C_MESSAGE_MESSAGE1, message1);
        setData(C_MESSAGE_MESSAGE2, message2);
        setData(C_MESSAGE_BUTTON1, button1);
        setData(C_MESSAGE_BUTTON2, button2);
        setData(C_MESSAGE_LINK1, link1);
        setData(C_MESSAGE_LINK2, link2);
        return getProcessedDataValue(C_TAG_MESSAGEBOX);
    }
    
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    
    public String getXmlDocumentTagName() {
        return "WP_MESSAGE";
    }
}
