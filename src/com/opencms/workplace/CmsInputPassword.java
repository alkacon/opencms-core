
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsInputPassword.java,v $
* Date   : $Date: 2001/01/24 09:43:28 $
* Version: $Revision: 1.7 $
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

import org.w3c.dom.*;
import org.xml.sax.*;
import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;
import java.util.*;

/**
 * Class for building workplace password fields. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;PASSWORD&gt;</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.7 $ $Date: 2001/01/24 09:43:28 $
 */

public class CmsInputPassword extends A_CmsWpElement implements I_CmsWpElement,I_CmsWpConstants {
    
    /**
     * Handling of the <CODE>&lt;PASSWORD&gt;</CODE> tags.
     * <P>
     * Reads the code of a password input field from the input definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Password input fields can be referenced in any workplace template by <br>
     * // TODO: insert correct syntax here!
     * <CODE>&lt;PASSWORD name="..." action="..." alt="..."/&gt;</CODE>
     * 
     * @param cms CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;PASSWORD&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
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
