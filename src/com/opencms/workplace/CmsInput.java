package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;

import java.util.*;


/**
 * Class for building workplace input fields. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;INPUT&gt;</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 2000/01/26 10:01:51 $
 */
public class CmsInput extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants  {    
    
    /**
     * Handling of the <CODE>&lt;INPUT&gt;</CODE> tags.
     * Calls the user method <code>elementTag</code> that has to be
     * defined in the XML template class. 
     * 
     * @param n XML element containing the <code>&lt;LABEL&gt;</code> tag.
     * @param callingObject Reference to the object requesting the node processing.
     * @param userObj Customizable user object that will be passed through to handling and user methods.
     * @return Result of user method <code>templateElement()</code>.
     * @exception CmsException
     */
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        String styleClass= n.getAttribute(C_INPUT_CLASS);
        String name=n.getAttribute(C_INPUT_NAME);
        String size=n.getAttribute(C_INPUT_SIZE);
        String length=n.getAttribute(C_INPUT_LENGTH);
           
   //    CmsXmlWpLabelDefFile labeldef = getLabelDefinitions(cms);
   //     outputValue = lang.getLanguageValue(labelValue);
   //     
   //     String result = labeldef.getLabel(outputValue);
        String result = null;
        return result; 
    }                    
}
