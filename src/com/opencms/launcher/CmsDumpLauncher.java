/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/launcher/Attic/CmsDumpLauncher.java,v $
 * Date   : $Date: 2000/05/29 11:24:25 $
 * Version: $Revision: 1.7 $
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
 * @author Alexander Lucas
 * @version $Revision: 1.7 $ $Date: 2000/05/29 11:24:25 $
 */
public class CmsDumpLauncher extends A_CmsLauncher { 	
        
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
        
        byte[] result = null;

        String templateClass = startTemplateClass;
        if(templateClass == null || "".equals(templateClass)) {            
            templateClass = "com.opencms.template.CmsDumpTemplate";
        }
         
        Object tmpl = getTemplateClass(cms, templateClass);
               
        if(!(tmpl instanceof com.opencms.template.I_CmsDumpTemplate)) {
            String errorMessage = "Error in " + file.getAbsolutePath() + ": " + templateClass + " is not a Cms dump template class.";
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
            }
            throw new CmsException(errorMessage, CmsException.C_XML_WRONG_TEMPLATE_CLASS);
        }
                
        Hashtable newParameters = new Hashtable();
            
        try {
            result = this.callCanonicalRoot(cms, (com.opencms.template.I_CmsTemplate)tmpl, file, newParameters);
        } catch (CmsException e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + "There were errors while building output for template file \"" + file.getAbsolutePath() + "\" and template class \"" + templateClass + "\". See above for details.");
            }
            throw e;
        }
            
        if(result != null) {
            writeBytesToResponse(cms, result);
        }
    }

    /**
     * Gets the ID that indicates the type of the launcher.
     * @return launcher ID
     */
    public int getLauncherId() {
	    return C_TYPE_DUMP;
    }
 }
