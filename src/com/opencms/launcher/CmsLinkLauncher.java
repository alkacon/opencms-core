/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/launcher/Attic/CmsLinkLauncher.java,v $
 * Date   : $Date: 2000/05/11 09:17:47 $
 * Version: $Revision: 1.1 $
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
 * @version $Revision: 1.1 $ $Date: 2000/05/11 09:17:47 $
 */
public class CmsLinkLauncher extends A_CmsLauncher { 	
        
    /**
 	 * Starts generating the output.
 	 * Calls the canonical root with the appropriate template class.
 	 * 
	 * @param cms A_CmsObject Object for accessing system resources
	 * @param file CmsFile Object with the selected resource to be shown
	 * @param startTemplateClass Name of the template class to start with.
     * @exception CmsException
	 */	
    protected void launch(A_CmsObject cms, CmsFile file, String startTemplateClass) throws CmsException {
        
        byte[] result = null;

        String link=new String(file.getContents());
     
        try {
            cms.getRequestContext().getResponse().sendCmsRedirect(link);
        } catch (Exception e) {
			    throw new CmsException("Redirect fails :"+ link,CmsException.C_UNKNOWN_EXCEPTION,e);
	    }   

    }

    /**
     * Gets the ID that indicates the type of the launcher.
     * @return launcher ID
     */
    public int getLauncherId() {
	    return C_TYPE_LINK;
    }
 }
