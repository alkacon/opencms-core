/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsNewResource.java,v $
* Date   : $Date: 2001/07/31 15:50:19 $
* Version: $Revision: 1.12 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import javax.servlet.http.*;
import java.util.*;

/**
 * Template class for displaying the new resource screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.12 $ $Date: 2001/07/31 15:50:19 $
 */

public class CmsNewResource extends CmsWorkplaceDefault implements I_CmsWpConstants,I_CmsConstants {
    
    
    /** Vector containing all names of the radiobuttons */
    private Vector m_names = null;
    
    
    /** Vector containing all links attached to the radiobuttons */
    private Vector m_values = null;
    
    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the new resource template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The lock template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearray containing the processed data of the template.
     * @exception Throws CmsException if something goes wrong.
     */
    
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, 
            Hashtable parameters, String templateSelector) throws CmsException {
        
        // TODO: check, if this is neede: I_CmsSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);           
        // the template to be displayed
        String template = null;
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        
        // process the selected template 
        return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
    }
    
    /**
     * Gets the resources displayed in the Radiobutton group on the new resource dialog.
     * @param cms The CmsObject.
     * @param lang The langauge definitions.
     * @param names The names of the new rescources (used for optional images).
     * @param values The links that are connected with each resource.
     * @param descriptions Description that will be displayed for the new resource.
     * @param parameters Hashtable of parameters (not used yet).
     * @returns The vectors names and values are filled with the information found in the 
     * workplace.ini.
     * @exception Throws CmsException if something goes wrong.
     */
    
    public void getResources(CmsObject cms, CmsXmlLanguageFile lang, Vector names, 
            Vector values, Vector descriptions, Hashtable parameters) throws CmsException {
        
        // Check if the list of available resources is not yet loaded from the workplace.ini
        if(m_names == null || m_values == null) {
            m_names = new Vector();
            m_values = new Vector();
            CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
            configFile.getWorkplaceIniData(m_names, m_values, "NEWRESOURCES", "RESOURCE");
        }
        
        // OK. Now m_names and m_values contain all available        
        // resource information.        
        // Loop through the vectors and fill the result vectors.
        int numViews = m_names.size();
        for(int i = 0;i < numViews;i++) {
            String loopValue = (String)m_values.elementAt(i);
            String loopName = (String)m_names.elementAt(i);
            values.addElement(loopValue);
            names.addElement("file_" + loopName);
            descriptions.addElement(lang.getLanguageValue("fileicon." + loopName));
        }
    }
}
