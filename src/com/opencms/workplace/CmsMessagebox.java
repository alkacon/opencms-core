package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;

import java.util.*;


/**
 * Class for building workplace message boxes. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;MESSAGEBOX&gt;</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 2000/02/10 10:24:53 $
 */
public class CmsMessagebox extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants  {    
    
    /**
     * Handling of the <CODE>&lt;MESSAGEBOX&gt;</CODE> tags.
     * <P>
     * Reads the code of a error box from the errors definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Error boxes can be referenced in any workplace template by <br>
     * // TODO: insert correct syntax here!
     * <CODE>&lt;MESSAGEBOX name="..." action="..." alt="..."/&gt;</CODE>
     * 
     * @param cms A_CmsObject Object for accessing resources.
     * @param An XML element containing the <code>&lt;MESSAGEBOX&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        // collect all required data
        String messageTitle = n.getAttribute(C_MESSAGE_TITLE);
        String messageMessage1= n.getAttribute(C_MESSAGE_MESSAGE1);
        String messageMessage2= n.getAttribute(C_MESSAGE_MESSAGE2);     
        String messageButton1= n.getAttribute(C_MESSAGE_BUTTON1);
        String messageButton2= n.getAttribute(C_MESSAGE_BUTTON2);
        String messageLink1 = n.getAttribute(C_MESSAGE_LINK1);
        String messageLink2 = n.getAttribute(C_MESSAGE_LINK2);

        
        CmsXmlWpBoxDefFile boxdef = getBoxDefinitions(cms);
        
        // get the data from the language file
        messageTitle = lang.getLanguageValue(messageTitle);
        messageMessage1= lang.getLanguageValue(messageMessage1);
        messageMessage2= lang.getLanguageValue(messageMessage2);
        messageButton1= lang.getLanguageValue(messageButton1);
        messageButton2= lang.getLanguageValue(messageButton2);
       
               
        // build errorbox
        String result = boxdef.getMessagebox(messageTitle,messageMessage1,messageMessage2,
                                             messageButton1,messageButton2,
                                             messageLink1,messageLink2);
        return result; 
    }                    
}
