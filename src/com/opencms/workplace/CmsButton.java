package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;

import java.util.*;

public class CmsButton implements I_CmsWpElement {    
    
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
    
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, Hashtable parameters) throws CmsException {
        String buttonName = n.getAttribute("name");
        String buttonAction = n.getAttribute("action");
        String buttonAlt = n.getAttribute("alt");
                
        CmsXmlWpButtonsDefFile buttondef = new CmsXmlWpButtonsDefFile(cms, "/system/workplace/templates/ButtonTemplate");
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms, "/system/workplace/config/language/de.txt");

        buttonAlt = lang.getLanguageValue("button." + buttonAlt);
        
        String result = buttondef.getButton(buttonName, buttonAction, buttonAlt);
        return result; 
    }                    
}
