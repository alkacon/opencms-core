
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsErrorpage.java,v $
* Date   : $Date: 2001/07/16 18:24:16 $
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

import org.w3c.dom.*;
import org.xml.sax.*;
import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;
import java.util.*;
import javax.servlet.http.*;

/**
 * Class for building workplace error pages. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;ERRORPAGE&gt;</code>.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.13 $ $Date: 2001/07/16 18:24:16 $
 */

public class CmsErrorpage extends A_CmsWpElement implements I_CmsWpElement,I_CmsWpConstants {

    /**
     * Handling of the <CODE>&lt;ERRORPAGEBOX&gt;</CODE> tags.
     * <P>
     * Reads the code of a error page box from the errors definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Error page boxes can be referenced in any workplace template by <br>
     * // TODO: insert correct syntax here!
     * <CODE>&lt;ERRORPAGEBOX name="..." action="..." alt="..."/&gt;</CODE>
     *
     * @param cms CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;ERRORPAGEBOX&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */

    public Object handleSpecialWorkplaceTag(CmsObject cms, Element n, A_CmsXmlContent doc,
            Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {

        // collect all required data
        String errorTitle = n.getAttribute(C_ERROR_TITLE);
        String errorMessage = n.getAttribute(C_ERROR_MESSAGE);
        String errorReason = n.getAttribute(C_ERROR_REASON);
        String errorSuggestion = n.getAttribute(C_ERROR_SUGGESTION);
        String errorLink = n.getAttribute(C_ERROR_LINK);
        String details = "no details";
        if(n.hasChildNodes()) {
            details = n.getFirstChild().getNodeValue();
        }
        String reason;
        String button;
        CmsXmlWpTemplateFile errordef = getErrorDefinitions(cms);
        CmsXmlWpConfigFile confFile = new CmsXmlWpConfigFile(cms);

        // get the data from the language file
        errorTitle = lang.getLanguageValue(errorTitle);
        errorMessage = lang.getLanguageValue(errorMessage);
        errorReason = lang.getLanguageValue(errorReason);
        errorSuggestion = lang.getLanguageValue(errorSuggestion);
        reason = lang.getLanguageValue("message.reason");
        button = lang.getLanguageValue("button.ok");
        String servletPath = cms.getRequestContext().getRequest().getServletUrl();
        errordef.setData("stylesheetpath", cms.getRequestContext().getRequest().getServletUrl() + "/system/workplace/css/");
        errordef.setData("jspath", cms.getRequestContext().getRequest().getServletUrl() + "/system/workplace/templates/js/");
        errordef.setData(C_ERROR_MSG_BUTTON, button);
        errordef.setData(C_ERROR_TITLE, errorTitle);
        errordef.setData(C_ERROR_MESSAGE, errorMessage);
        errordef.setData(C_ERROR_REASON, errorReason);
        errordef.setData(C_ERROR_SUGGESTION, errorSuggestion);
        errordef.setData(C_ERROR_LINK, errorLink);
        errordef.setData(C_ERROR_MSG_REASON, reason);
        errordef.setData(C_ERROR_MSG_DETAILS, details);

        // build errorpage
        String result = errordef.getProcessedDataValue(C_TAG_ERRORPAGE, callingObject, null);
        return result;
    }

    /**
     * Indicates if the results of this class are cacheable.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */

    public boolean isCacheable(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) {
        return false;
    }
}
