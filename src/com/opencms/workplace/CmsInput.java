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
 * @version $Revision: 1.4 $ $Date: 2000/02/02 10:30:08 $
 */
public class CmsInput extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants  {    
    
    /**
     * Handling of the <CODE>&lt;INPUT&gt;</CODE> tags.
     * <P>
     * Reads the code of a input field from the input definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Input fields can be referenced in any workplace template by <br>
     * // TODO: insert correct syntax here!
     * <CODE>&lt;INPUT name="..." action="..." alt="..."/&gt;</CODE>
     * 
     * @param cms A_CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;INPUT&gt;</code> tag.
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
        String result = inputdef.getInput(styleClass,name,size,length);

        return result; 
    }                    
}
