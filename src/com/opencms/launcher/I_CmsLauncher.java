package com.opencms.launcher;

import com.opencms.file.*;
import com.opencms.core.*;

import javax.servlet.http.*;

/**
 * Common interface for OpenCms launchers.
 * Classes for each customized launcher have to be implemtented.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.3 $ $Date: 2000/01/14 16:17:11 $
 */
public interface I_CmsLauncher { 
    
	/** Constants used as launcher IDs */
    public final static int 
   		C_TYPE_DUMP = 1,
		C_TYPE_JAVASCRIPT = 2,
		C_TYPE_XML = 3;

	/**
	 * Start launch method called by the OpenCms system to show a resource	 
	 *  
	 * @param cms A_CmsObject Object for accessing system resources.
	 * @param file CmsFile Object with the selected resource to be shown.
	 * @param startTemplateClass Name of the template class to start with.
     * @exception CmsException
	 */
    public void initlaunch(A_CmsObject cms, CmsFile file, String startTemplateClass) throws CmsException;
	
    /**
     * Gets the ID that indicates the type of the launcher.
     * @return launcher ID
     */
    public int getLauncherId();
}
