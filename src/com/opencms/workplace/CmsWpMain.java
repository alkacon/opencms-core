package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;

/**
 * Common template class for displaying OpenCms workplace main screen.
 * <P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2000/01/26 17:53:49 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsWpMain extends CmsWorkplaceDefault {


    public Object getInformation(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {
        Hashtable parameters = (Hashtable)userObj;
        A_CmsRequestContext reqContext = cms.getRequestContext();
        A_CmsUser currentUser = reqContext.currentUser();
        return currentUser.getName();
    }    

}
