
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/Attic/CmsXmlFormTemplate.java,v $
* Date   : $Date: 2001/01/24 09:41:53 $
* Version: $Revision: 1.4 $
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

package com.opencms.defaults;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import java.util.*;
import javax.servlet.http.*;

/**
 * Common template class for displaying OpenCms workplace screens.
 * <P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * <P>
 * Most special workplace classes may extend this class.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.4 $ $Date: 2001/01/24 09:41:53 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsXmlFormTemplate extends CmsXmlTemplate {
    
    /**
     * Reads in the template file and starts the XML parser for the expected
     * content type <class>CmsXmlFormTemplateFile</code>
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public CmsXmlTemplateFile getOwnTemplateFile(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        CmsXmlFormTemplateFile xmlTemplateDocument = new CmsXmlFormTemplateFile(cms, templateFile);
        return xmlTemplateDocument;
    }
}
