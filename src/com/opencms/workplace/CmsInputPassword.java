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
 * @version $Revision: 1.3 $ $Date: 2000/02/02 10:30:08 $
 */
public class CmsInputPassword extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants  {    
    
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
     * @param cms A_CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;PASSWORD&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */    
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        String styleClass= n.getAttribute(C_INPUT_CLASS);
        String name=n.getAttribute(C_INPUT_NAME);
        String size=n.getAttribute(C_INPUT_SIZE);
        String length=n.getAttribute(C_INPUT_LENGTH);

        
        CmsXmlWpInputDefFile inputdef = getInputDefinitions(cms); 
        String result = inputdef.getPassword(styleClass,name,size,length);

        return result; 
    }                    
}
