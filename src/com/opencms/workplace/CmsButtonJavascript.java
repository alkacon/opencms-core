/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsButtonJavascript.java,v $
* Date   : $Date: 2005/02/18 15:18:51 $
* Version: $Revision: 1.13 $
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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;

import com.opencms.template.A_CmsXmlContent;

import java.util.Hashtable;

import org.w3c.dom.Element;

/**
 * Class for building workplace javascript buttons. <BR>
 * Those buttons are embedded in JavaScript Code.
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;JAVASCRIPTBUTTON&gt;</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.13 $ $Date: 2005/02/18 15:18:51 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsButtonJavascript extends A_CmsWpElement {
    
    /**
     * Handling of the special workplace <CODE>&lt;JAVASCRIPTBUTTON&gt;</CODE> tags.
     * <P>
     * Reads the code of a javascript button from the buttons definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Buttons can be referenced in any workplace template by <br>
     * <CODE>&lt;JAVASCRIPTBUTTON name="..." action="..." alt="..."/&gt;</CODE>
     * 
     * @param cms CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;JAVASCRIPTBUTTON&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @throws CmsException
     */
    
    public Object handleSpecialWorkplaceTag(CmsObject cms, Element n, A_CmsXmlContent doc, 
            Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        
        // Read button parameters
        String buttonName = n.getAttribute(C_BUTTON_NAME);
        String buttonAction = n.getAttribute(C_BUTTON_ACTION);
        String buttonAlt = n.getAttribute(C_BUTTON_ALT);
        String buttonHref = n.getAttribute(C_BUTTON_HREF);
        if(buttonHref == null || "".equals(buttonHref)) {
            buttonHref = "";
        }
        
        // Get button definition and language values
        CmsXmlWpButtonsDefFile buttondef = getButtonDefinitions(cms);
        buttonAlt = lang.getLanguageValue(C_LANG_BUTTON + "." + buttonAlt);
        
        // get the processed button.
        String result = buttondef.getJavascriptButton(buttonName, buttonAction, 
                buttonAlt, buttonHref, callingObject);
        return result;
    }
}
