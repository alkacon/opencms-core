/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsErrorbox.java,v $
 * Date   : $Date: 2000/02/19 11:57:08 $
 * Version: $Revision: 1.6 $
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
 * Class for building workplace error boxes. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;ERRORBOX&gt;</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.6 $ $Date: 2000/02/19 11:57:08 $
 */
public class CmsErrorbox extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants  {    
    
    /**
     * Handling of the <CODE>&lt;ERRORBOX&gt;</CODE> tags.
     * <P>
     * Reads the code of a error box from the errors definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Error boxes can be referenced in any workplace template by <br>
     * // TODO: insert correct syntax here!
     * <CODE>&lt;ERRORBOX name="..." action="..." alt="..."/&gt;</CODE>
     * 
     * @param cms A_CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;ERRORBOX&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        // collect all required data
        String errorTitle = n.getAttribute(C_ERROR_TITLE);
        String errorMessage = n.getAttribute(C_ERROR_MESSAGE);
        String errorReason = n.getAttribute(C_ERROR_REASON);
        String errorSuggestion = n.getAttribute(C_ERROR_SUGGESTION);
        String errorLink = n.getAttribute(C_ERROR_LINK);
        
        String reason;
        String button;
        
        CmsXmlWpErrorDefFile errordef = getErrorDefinitions(cms);
        
        // get the data from the language file
        errorTitle = lang.getLanguageValue(errorTitle);
        errorMessage = lang.getLanguageValue(errorMessage);
        errorReason = lang.getLanguageValue(errorReason);
        errorSuggestion = lang.getLanguageValue(errorSuggestion);
        reason=lang.getLanguageValue("message.reason");
        button=lang.getLanguageValue("button.ok");
        
        // build errorbox
        String result = errordef.getErrorbox(errorTitle,errorMessage,errorReason,
                                             errorSuggestion,errorLink,reason,button,callingObject);
        return result; 

    }                    
}
