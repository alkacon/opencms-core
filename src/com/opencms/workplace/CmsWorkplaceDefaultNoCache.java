package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import java.util.*;
import javax.servlet.http.*;

/**
 * Insert the type's description here.
 * Creation date: (09.08.00 16:11:21)
 * @author: Hanjo Riege
 */

public class CmsWorkplaceDefaultNoCache extends CmsWorkplaceDefault {
    
    /**
     * Indicates if the results of this class are cacheable.
     * 
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, 
            Hashtable parameters, String templateSelector) {
        return false;
    }
}

