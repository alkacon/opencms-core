/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/launcher/Attic/CmsJavascriptLauncher.java,v $
 * Date   : $Date: 2000/05/29 11:24:25 $
 * Version: $Revision: 1.3 $
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

import com.opencms.core.*;
import com.opencms.file.*;

/** 
 * Document the purpose of this class.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.3 $ $Date: 2000/05/29 11:24:25 $
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
	 * @param openCms a instance of A_OpenCms for redirect-needs
     * @exception CmsException
	 */	
	protected void launch(A_CmsObject cms, CmsFile file, String startTemplateClass, A_OpenCms openCms) throws CmsException {
    }
    
    /**
     * Gets the ID that indicates the type of the launcher.
     * @return launcher ID
     */
    public int getLauncherId() {
	    return C_TYPE_JAVASCRIPT;
    }
}