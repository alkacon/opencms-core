package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;

import java.util.*;

/**
 * Class for building workplace submit buttons. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;SUBMITBUTTON&gt;</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 2000/01/26 13:52:40 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsButtonSubmit extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants {    
            
    /**
     * Handling of the special workplace <CODE>&lt;SUBMITBUTTON&gt;</CODE> tags.
     * <P>
     * Reads the code of a button from the buttons definition file
     * and returns the processed code with the actual elements.
     * <P>
     * 
     * @param cms A_CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;BUTTON&gt;</code> tag.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */    
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        // Read button parameters
        String buttonName = n.getAttribute(C_BUTTON_NAME);
        String buttonAction = n.getAttribute(C_BUTTON_ACTION);
        String buttonValue = n.getAttribute(C_BUTTON_VALUE);
        String buttonStyle = n.getAttribute(C_BUTTON_STYLE);
        String buttonWidth = n.getAttribute(C_BUTTON_WIDTH);
        
        // Get button definition and language values
        CmsXmlWpButtonsDefFile buttondef = getButtonDefinitions(cms);
        buttonValue = lang.getLanguageValue(C_LANG_BUTTON + "." + buttonValue);
        
        // get the processed button.
        String result = buttondef.getButtonSubmit(buttonName, buttonAction, buttonValue,
                                                  buttonStyle, buttonWidth);
        return result; 
    }           
}
