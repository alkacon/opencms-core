package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;

import java.util.*;

/**
 * Interface for all workplace elements.
 * <P>
 * Any class called by CmsXmlTemplateFile for handling special workplace
 * XML tags (e.g. <code>&lt;BUTTON&gt;</code> or <code>&lt;LABEL&gt;</code>)
 * has to implement this interface.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.6 $ $Date: 2000/02/02 10:30:08 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public interface I_CmsWpElement {
    
    /**
     * Method for handling the corresponding special workplace XML tag and generating the
     * appropriate output.
     * 
     * @param cms A_CmsObject Object for accessing resources.
     * @param n XML element containing the current tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed special workplace XML tag.
     * @exception CmsException 
     */
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException;    
}
