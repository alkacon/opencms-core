package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;

import java.util.*;

public class CmsButton extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants {    
            
    /**
     * Handling of the <CODE>&lt;BUTTON&gt;</CODE> tags.
     * Calls the user method <code>elementTag</code> that has to be
     * defined in the XML template class. 
     * 
     * @param n XML element containing the <code>&lt;BUTTON&gt;</code> tag.
     * @param callingObject Reference to the object requesting the node processing.
     * @param userObj Customizable user object that will be passed through to handling and user methods.
     * @return Result of user method <code>templateElement()</code>.
     * @exception CmsException
     */
    
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        String buttonName = n.getAttribute(C_BUTTON_NAME);
        String buttonAction = n.getAttribute(C_BUTTON_ACTION);
        String buttonAlt = n.getAttribute(C_BUTTON_ALT);
        
        CmsXmlWpButtonsDefFile buttondef = getButtonDefinitions(cms);
        buttonAlt = lang.getLanguageValue(C_LANG_BUTTON + "." + buttonAlt);
        
        String result = buttondef.getButton(buttonName, buttonAction, buttonAlt);
        return result; 
    }           
}
