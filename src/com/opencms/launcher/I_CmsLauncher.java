/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/launcher/Attic/I_CmsLauncher.java,v $
 * Date   : $Date: 2000/05/10 16:46:29 $
 * Version: $Revision: 1.5 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.launcher;

import com.opencms.file.*;
import com.opencms.core.*;

import javax.servlet.http.*;

/**
 * Common interface for OpenCms launchers.
 * Classes for each customized launcher have to be implemtented.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.5 $ $Date: 2000/05/10 16:46:29 $
 */
public interface I_CmsLauncher { 
    
	/** Constants used as launcher IDs */
    public final static int 
   		C_TYPE_DUMP = 1,
		C_TYPE_JAVASCRIPT = 2,
		C_TYPE_XML = 3,
    	C_TYPE_LINK = 4;

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

