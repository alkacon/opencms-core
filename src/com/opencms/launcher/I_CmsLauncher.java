package com.opencms.launcher;

import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;

import javax.servlet.http.*;

/**
 * Common interface for OpenCms launchers.
 * Classes for each customized launcher have to be implemtented.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2000/01/13 13:37:31 $
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
	 * @param openCms A_CmsObject Object for accessing system resources
	 * @param file CmsFile Object with the selected resource to be shown
	 * @param req Actual HttpServletRequest
     * @param resp Actual HttpServletResponse
     * @exception CmsException
	 */
    public void initlaunch(A_CmsObject openCms, CmsFile file, HttpServletRequest req, HttpServletResponse resp) throws CmsException;
	
    /**
     * Gets the ID that indicates the type of the launcher.
     * @return launcher ID
     */
    public int getLauncherId();
}
