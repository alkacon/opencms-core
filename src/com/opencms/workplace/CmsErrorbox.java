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
 * @version $Revision: 1.3 $ $Date: 2000/01/27 15:00:56 $
 */
public class CmsErrorbox extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants  {    
    
    /**
     * Handling of the <CODE>&lt;ERRORBOX&gt;</CODE> tags.
     * Calls the user method <code>elementTag</code> that has to be
     * defined in the XML template class. 
     * 
     * @param XML element containing the <code>&lt;ERRORBOX&gt;</code> tag.
     * @param callingObject Reference to the object requesting the node processing.
     * @param userObj Customizable user object that will be passed through to handling and user methods.
     * @return Result of user method <code>templateElement()</code>.
     * @exception CmsException
     */
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
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
                                             errorSuggestion,errorLink,reason,button);
        return result; 

    }                    
}
