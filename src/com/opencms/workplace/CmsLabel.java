package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;

import java.util.*;


/**
 * Class for building workplace labels. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;LABEL&gt;</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.5 $ $Date: 2000/02/02 10:30:08 $
 */
public class CmsLabel extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants  {    
    
    /**
     * Handling of the <CODE>&lt;LABEL&gt;</CODE> tags.
     * <P>
     * Reads the code of a label from the label definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Labels can be referenced in any workplace template by <br>
     * // TODO: insert correct syntax here!
     * <CODE>&lt;LABEL name="..." action="..." alt="..."/&gt;</CODE>
     * 
     * @param cms A_CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;LABEL&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */    
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        String labelValue = n.getAttribute(C_LABEL_VALUE);
        String outputValue=null;
        
        CmsXmlWpLabelDefFile labeldef = getLabelDefinitions(cms);
        outputValue = lang.getLanguageValue(labelValue);
        
        String result = labeldef.getLabel(outputValue);
        return result; 
    }                    
}
