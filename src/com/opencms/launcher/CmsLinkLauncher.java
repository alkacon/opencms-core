package com.opencms.launcher;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/launcher/Attic/CmsLinkLauncher.java,v $
 * Date   : $Date: 2000/08/08 14:08:28 $
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

import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;

import org.w3c.dom.*;
import org.xml.sax.*;
		
import java.util.*;      
import javax.servlet.http.*;

/**
 * OpenCms launcher class for starting template classes implementing
 * the I_CmsDumpTemplate interface.
 * This can be used for plain text files or files containing graphics.
 * <P>
 * If no other start template class is given, CmsDumpTemplate will
 * be used to create output.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.5 $ $Date: 2000/08/08 14:08:28 $
 */
public class CmsLinkLauncher extends A_CmsLauncher { 	
		
 
	/**
	 * Gets the ID that indicates the type of the launcher.
	 * @return launcher ID
	 */
	public int getLauncherId() {
	    return C_TYPE_LINK;
	}
	/**
 	 * Unitary method to start generating the output.
 	 * Every launcher has to implement this method.
 	 * In it possibly the selected file will be analyzed, and the
 	 * Canonical Root will be called with the appropriate 
 	 * template class, template file and parameters. At least the 
 	 * canonical root's output must be written to the HttpServletResponse.
 	 * 
	 * @param cms CmsObject Object for accessing system resources
	 * @param file CmsFile Object with the selected resource to be shown
	 * @param startTemplateClass Name of the template class to start with.
	 * @param openCms a instance of A_OpenCms for redirect-needs
	 * @exception CmsException
	 */	
	protected void launch(CmsObject cms, CmsFile file, String startTemplateClass, A_OpenCms openCms) throws CmsException {
		
		String link=new String(file.getContents());
		
		CmsFile linkFile = cms.readFile(link);
		
		openCms.showResource(cms, linkFile);
	}
}  
