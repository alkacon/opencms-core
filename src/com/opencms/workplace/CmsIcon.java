/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsIcon.java,v $
 * Date   : $Date: 2000/02/15 17:44:01 $
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

package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;

import java.util.*;

/**
 * Class for building workplace icons. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;ICON&gt;</code>.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.3 $ $Date: 2000/02/15 17:44:01 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsIcon extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants {    
            
    /**
     * Handling of the special workplace <CODE>&lt;ICON&gt;</CODE> tags.
     * <P>
     * Returns the processed code with the actual elements.
     * <P>
     * Icons can be referenced in any workplace template by <br>
     * // TODO: insert correct syntax here!
     * <CODE>&lt;ICON name="..." label="..." action="..." href="..." target="..."/&gt;</CODE>
     * 
     * @param cms A_CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;ICON&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */    
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        // Read button parameters
        String iconName = n.getAttribute(C_ICON_NAME);
        String iconLabel = n.getAttribute(C_ICON_LABEL);
        String iconAction = n.getAttribute(C_ICON_ACTION);
        String iconHref = n.getAttribute(C_ICON_HREF);
        String iconTarget = n.getAttribute(C_ICON_TARGET);
        if(iconHref == null || "".equals(iconHref)) {
            iconHref = "";
        }
        
        // Get button definition and language values
        CmsXmlWpTemplateFile icondef = getIconDefinitions(cms);
        StringBuffer iconLabelBuffer = new StringBuffer(
			lang.getLanguageValue(C_LANG_ICON + "." + iconLabel) );
		
		// Insert a html-break, if needed
		if( iconLabelBuffer.toString().indexOf("- ") != -1 ) {
			iconLabelBuffer.insert(iconLabelBuffer.toString().indexOf("- ") + 2, "<BR>");
		}
        
        // get the processed button.
        icondef.setXmlData(C_ICON_NAME, iconName);
        icondef.setXmlData(C_ICON_LABEL, iconLabelBuffer.toString());
        icondef.setXmlData(C_ICON_ACTION, iconAction);
        icondef.setXmlData(C_ICON_HREF, iconHref);
        icondef.setXmlData(C_ICON_TARGET, iconTarget);
		return icondef.getProcessedXmlDataValue("defaulticon", callingObject);
    }           
}
