package com.opencms.launcher;

import com.opencms.core.*;
import com.opencms.file.*;

/** 
 * Document the purpose of this class.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2000/01/21 10:33:44 $
 */
class CmsJavascriptLauncher extends A_CmsLauncher {

    /**
 	 * Unitary method to start generating the output.
 	 * Every launcher has to implement this method.
 	 * In it possibly the selected file will be analyzed, and the
 	 * Canonical Root will be called with the appropriate 
 	 * template class, template file and parameters. At least the 
 	 * canonical root's output must be written to the HttpServletResponse.
 	 * 
	 * @param cms A_CmsObject Object for accessing system resources
	 * @param file CmsFile Object with the selected resource to be shown
	 * @param startTemplateClass Name of the template class to start with.
     * @exception CmsException
	 */	
    protected void launch(A_CmsObject cms, CmsFile file, String startTemplateClass) throws CmsException {
    }
    
    /**
     * Gets the ID that indicates the type of the launcher.
     * @return launcher ID
     */
    public int getLauncherId() {
	    return C_TYPE_JAVASCRIPT;
    }
}