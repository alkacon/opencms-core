/*
* File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.legacy/src/com/opencms/defaults/Attic/CmsXmlFormTemplate.java,v $
* Date   : $Date: 2005/05/16 17:45:08 $
* Version: $Revision: 1.1 $
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

package com.opencms.defaults;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;

import com.opencms.template.CmsXmlTemplate;
import com.opencms.template.CmsXmlTemplateFile;

import java.util.Hashtable;

/**
 * Common template class for displaying OpenCms workplace screens.
 * <P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * <P>
 * Most special workplace classes may extend this class.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2005/05/16 17:45:08 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsXmlFormTemplate extends CmsXmlTemplate {
    
    /**
     * Reads in the template file and starts the XML parser for the expected
     * content type <class>CmsXmlFormTemplateFile</code>.<p>
     * 
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template
     * @param parameters Hashtable with all template class parameters
     * @param templateSelector template section that should be processed
     * @return the template document
     * @throws CmsException if something goes wrong
     */
    public CmsXmlTemplateFile getOwnTemplateFile(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        CmsXmlFormTemplateFile xmlTemplateDocument = new CmsXmlFormTemplateFile(cms, templateFile);
        return xmlTemplateDocument;
    }
}
