package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;

import java.util.*;


/**
 * Class for building workplace error boxes. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;LABEL&gt;</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 2000/01/26 19:22:58 $
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
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        String errorTitle = n.getAttribute(C_ERROR_TITLE);
        String errorMessage = n.getAttribute(C_ERROR_MESSAGE);
        String errorReason = n.getAttribute(C_ERROR_REASON);
        String errorSuggestion = n.getAttribute(C_ERROR_SUGGESTION);
        String errorLink = n.getAttribute(C_ERROR_LINK);
        
        CmsXmlWpErrorDefFile errordef = getErrorDefinitions(cms);
         
        String result = errordef.getErrorbox(errorTitle,errorMessage,errorReason,
                                             errorSuggestion,errorLink);
        return result; 

    }                    
}
