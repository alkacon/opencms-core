package com.opencms.workplace;

/* 
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsBackbutton.java,v $
 * Date   : $Date: 2000/08/22 13:39:28 $
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
import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;

import java.util.*;
import java.lang.reflect.*;

/**
 * Class for building workplace icons. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;ICON&gt;</code>.
 * 
 * Creation date: (09.08.00 15:21:44)
 * @author: Hanjo Riege
 * @version $Name:  $ $Revision: 1.1 $ $Date: 2000/08/22 13:39:28 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsBackbutton extends A_CmsWpElement implements I_CmsConstants, I_CmsWpElement, I_CmsWpConstants {
	

	/**
	 * Handling of the special workplace <CODE>&lt;BACKBUTTON&gt;</CODE> tags.
	 * <P>
	 * Returns the processed code with the actual elements.
	 * <P>
	 * A backbutton can be referenced in any workplace template by <br>
	 * // TODO: insert correct syntax here!
	 * <CODE>&lt;BACKBUTTON name="..." label="..." action="..." href="..." target="..."/&gt;</CODE>
	 * 
	 * @param cms CmsObject Object for accessing resources.
	 * @param n XML element containing the <code>&lt;BACKBUTTON&gt;</code> tag.
	 * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
	 * @param callingObject reference to the calling object.
	 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
	 * @param lang CmsXmlLanguageFile conataining the currently valid language file.
	 * @return Processed button.
	 * @exception CmsException
	 */    
	public Object handleSpecialWorkplaceTag(CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
		
		CmsXmlWpTemplateFile buttondef = this.getBackbuttonDefinitions(cms);
		CmsXmlWpConfigFile confFile = new CmsXmlWpConfigFile(cms); 
		I_CmsSession session = cms.getRequestContext().getSession(true);
		String navPos = (String)session.getValue(C_SESSION_ADMIN_POS);
		if ((navPos == null)||(navPos.equals(confFile.getWorkplaceAdministrationPath()))){
			// first call or on top-level => disable Button
			return buttondef.getProcessedDataValue("disable", callingObject);
		}else{
			// enable backbutton 
			String linkValue = navPos.substring(0,navPos.length()-1);
			linkValue = linkValue.substring(0,linkValue.lastIndexOf("/")+1);
			buttondef.setData("linkTo", "?sender="+linkValue);
			return buttondef.getProcessedDataValue("enable", callingObject);			
		}
		
	} //end of handleSpecialWorkplaceTag
}
