/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsButtonText.java,v $
* Date   : $Date: 2001/07/31 15:50:17 $
* Version: $Revision: 1.7 $
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

import org.w3c.dom.*;
import org.xml.sax.*;
import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;
import java.util.*;

/**
 * Class for building workplace text buttons. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;TEXTBUTTON&gt;</code>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.7 $ $Date: 2001/07/31 15:50:17 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsButtonText extends A_CmsWpElement implements I_CmsWpElement,I_CmsWpConstants {
    
    /**
     * Handling of the special workplace <CODE>&lt;TEXTBUTTON&gt;</CODE> tags.
     * <P>
     * Reads the code of a button from the buttons definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Text Buttons can be referenced in any workplace template by <br>
     * // TODO: insert correct syntax here!
     * <CODE>&lt;TEXTBUTTON name="..." action="..." alt="..."/&gt;</CODE>
     * 
     * @param cms CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;TEXTBUTTON&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */
    
    public Object handleSpecialWorkplaceTag(CmsObject cms, Element n, A_CmsXmlContent doc, 
            Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        
        // Read button parameters
        String buttonName = n.getAttribute(C_BUTTON_NAME);
        String buttonAction = n.getAttribute(C_BUTTON_ACTION);
        String buttonValue = n.getAttribute(C_BUTTON_VALUE);
        String buttonStyle = n.getAttribute(C_BUTTON_STYLE);
        String buttonWidth = n.getAttribute(C_BUTTON_WIDTH);
        
        // Get button definition and language values
        CmsXmlWpButtonsDefFile buttondef = getButtonDefinitions(cms);
        buttonValue = lang.getLanguageValue(C_LANG_BUTTON + "." + buttonValue);
        
        // get the processed button.
        String result = buttondef.getButtonText(buttonName, buttonAction, buttonValue, 
                buttonStyle, buttonWidth);
        return result;
    }
}
