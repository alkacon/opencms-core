
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/I_CmsWpElement.java,v $
* Date   : $Date: 2001/01/24 09:43:31 $
* Version: $Revision: 1.11 $
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

import org.w3c.dom.*;
import org.xml.sax.*;
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;
import java.util.*;

/**
 * Interface for all workplace elements.
 * <P>
 * Any class called by CmsXmlTemplateFile for handling special workplace
 * XML tags (e.g. <code>&lt;BUTTON&gt;</code> or <code>&lt;LABEL&gt;</code>)
 * has to implement this interface.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.11 $ $Date: 2001/01/24 09:43:31 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public interface I_CmsWpElement {
    
    /**
     * Method for handling the corresponding special workplace XML tag and generating the
     * appropriate output.
     * 
     * @param cms CmsObject Object for accessing resources.
     * @param n XML element containing the current tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed special workplace XML tag.
     * @exception CmsException 
     */
    
    public Object handleSpecialWorkplaceTag(CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, 
            Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException;
}
