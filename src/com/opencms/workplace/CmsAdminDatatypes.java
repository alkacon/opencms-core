/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminDatatypes.java,v $
 * Date   : $Date: 2000/04/10 09:20:16 $
 * Version: $Revision: 1.2 $
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

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;

import java.util.*;
import java.io.*;

/**
 * Template class for displaying OpenCms workplace datatype administration.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.2 $ $Date: 2000/04/10 09:20:16 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsAdminDatatypes extends CmsWorkplaceDefault implements I_CmsConstants {
	
    /** XML datablock tag used for setting the resource type name */
    private static final String C_TAG_RESTYPE = "restype";

    /** XML datablock tag used for setting all collected entries */
    private static final String C_TAG_ALLENTRIES = "allentries";

    /** XML datablock tag used for getting a processed resource type entry */
    private static final String C_TAG_RESTYPEENTRY = "restypeentry";

    /** XML datablock tag used for getting a processed separator entry */
    private static final String C_TAG_SEPARATORENTRY = "separatorentry";

    /** XML datablock tag used for getting the complete and processed content to be returned */
    private static final String C_TAG_SCROLLERCONTENT = "scrollercontent";
    
    
    /**
     * Indicates if the results of this class are cacheable.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }    

    /**
     * Used by the <code>&lt;PREFSSCROLLER&gt;</code> tag for getting
     * the content of the scroller window.
     * <P>
     * Gets all available resource types and returns a list
     * using the datablocks defined in the own template file.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param lang reference to the currently valid language file
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the user's current filter view in the vectors.
     * @exception CmsException
     */
    public String getDatatypes(A_CmsObject cms, A_CmsXmlContent doc, CmsXmlLanguageFile lang, Hashtable parameters, Object callingObj) 
		throws CmsException {
		
        StringBuffer result = new StringBuffer();
        
        CmsXmlTemplateFile templateFile = (CmsXmlTemplateFile)doc;
        Enumeration allResTypes = cms.getAllResourceTypes().elements();
        
        // Loop through all resource types
        while(allResTypes.hasMoreElements()) {
            A_CmsResourceType currResType = (A_CmsResourceType)allResTypes.nextElement();
            
            templateFile.setData(C_TAG_RESTYPE, currResType.getResourceName());
            result.append(templateFile.getProcessedDataValue(C_TAG_RESTYPEENTRY, callingObj));
            result.append(templateFile.getProcessedDataValue(C_TAG_SEPARATORENTRY, callingObj));
        }
     
        templateFile.setData(C_TAG_ALLENTRIES, result.toString());        
        return templateFile.getProcessedDataValue(C_TAG_SCROLLERCONTENT, callingObj);
    }        

}
