package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.file.*;
import com.opencms.core.*;

import java.util.*;

/**
 * Interface for all workplace elements.
 * @author Alexander Lucas
 * @version $Revision: 1.3 $ $Date: 2000/01/25 17:19:29 $
 */
public interface I_CmsWpElement {
    
    /**
     * Method for handling any corresponding XML tag.
     * 
     */
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException;    
}
