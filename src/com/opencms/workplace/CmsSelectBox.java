package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;

import java.util.*;

/**
 * Class for building workplace buttons. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;BUTTON&gt;</code>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2000/01/27 15:04:04 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsSelectBox extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants {    
            
    /**
     * Handling of the special workplace <CODE>&lt;BUTTON&gt;</CODE> tags.
     * <P>
     * Reads the code of a button from the buttons definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Buttons can to be referenced in any workplace template by <br>
     * <CODE>&lt;BUTTON name="..." action="..." alt="..."/&gt;</CODE>
     * 
     * @param cms A_CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;BUTTON&gt;</code> tag.
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */    
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        // Read button parameters
        //String buttonName = n.getAttribute(C_BUTTON_NAME);
        
        // Get button definition and language values
        CmsXmlWpInputDefFile inputdef = getInputDefinitions(cms); 
        
        // get the processed button.
        String result = inputdef.getSelectBoxHeader();
        result = result + "<option>kuckuck ";
        result = result + inputdef.getSelectBoxOption("test1", "test1");
        result = result + inputdef.getSelectBoxSelOption("test2", "test2");
        return result; 
    }           
}
