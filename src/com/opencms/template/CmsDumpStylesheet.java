

/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsDumpStylesheet.java,v $
* Date   : $Date: 2001/01/24 09:42:40 $
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

package com.opencms.template;

import com.opencms.file.*;
import com.opencms.core.*;
import javax.servlet.http.*;
import java.util.*;

/**
 * Dump the correct stylesheet for the current browser.
 * 
 * @author Alexander Lucas <alexander.lucas@framfab.de>
 * @version $Revision: 1.2 $ $Date: 2001/01/24 09:42:40 $
 */
public class CmsDumpStylesheet extends CmsDumpTemplate {
    
    /**
     * Get the content and log this in the vector described above.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * 
     * @return Array of bytes that contains the page.
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters) throws CmsException {
        HttpServletRequest orgReq = (HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest();
        int dotIdx = templateFile.lastIndexOf(".");
        String pre = templateFile.substring(0, dotIdx);
        String post = templateFile.substring(dotIdx);
        
        // Get the user's browser
        String browser = orgReq.getHeader("user-agent");
        if(browser.indexOf("MSIE") > -1) {
            templateFile = pre + "-ie" + post;
        }
        else {
            templateFile = pre + "-ns" + post;
        }
        return super.getContent(cms, templateFile, elementName, parameters);
    }
    
    /**
     * Get the content and log this in the vector described above.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName <em>not used here</em>.
     * @param parameters <em>not used here</em>.
     * @param templateSelector <em>not used here</em>.
     * @return Unprocessed content of the given template file.
     * @exception CmsException 
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        
        // ignore the templateSelector since we only dump the template
        return getContent(cms, templateFile, elementName, parameters);
    }
    
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
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }
}
