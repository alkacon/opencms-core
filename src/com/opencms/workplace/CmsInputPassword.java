/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsInputPassword.java,v $
* Date   : $Date: 2005/02/18 14:23:15 $
* Version: $Revision: 1.15 $
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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;

import com.opencms.template.A_CmsXmlContent;

import java.util.Hashtable;

import org.w3c.dom.Element;

/**
 * Class for building workplace password fields. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;PASSWORD&gt;</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.15 $ $Date: 2005/02/18 14:23:15 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsInputPassword extends A_CmsWpElement {
    
    /**
     * Handling of the <CODE>&lt;PASSWORD&gt;</CODE> tags.
     * <P>
     * Reads the code of a password input field from the input definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Password input fields can be referenced in any workplace template by <br>
     * <CODE>&lt;PASSWORD name="..." action="..." alt="..."/&gt;</CODE>
     * 
     * @param cms CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;PASSWORD&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @throws CmsException
     */
    
    public Object handleSpecialWorkplaceTag(CmsObject cms, Element n, A_CmsXmlContent doc, 
            Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        String styleClass = n.getAttribute(C_INPUT_CLASS);
        String name = n.getAttribute(C_INPUT_NAME);
        String size = n.getAttribute(C_INPUT_SIZE);
        String length = n.getAttribute(C_INPUT_LENGTH);
        CmsXmlWpInputDefFile inputdef = getInputDefinitions(cms);
        String result = inputdef.getPassword(styleClass, name, size, length);
        return result;
    }
}
