package com.opencms.launcher;

import com.opencms.file.*;
import com.opencms.core.*;

import javax.servlet.http.*;

/**
 * Common interface for OpenCms launchers.
 * Classes for each customized launcher have to be implemtented.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.2 $ $Date: 2000/01/13 17:51:00 $
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
	 * @param cms A_CmsObject Object for accessing system resources
	 * @param file CmsFile Object with the selected resource to be shown
     * @exception CmsException
	 */
    public void initlaunch(A_CmsObject cms, CmsFile file) throws CmsException;
	
    /**
     * Gets the ID that indicates the type of the launcher.
     * @return launcher ID
     */
    public int getLauncherId();
}
