package com.opencms.workplace;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsButtonSeparator.java,v $
 * Date   : $Date: 2000/08/08 14:08:30 $
 * Version: $Revision: 1.10 $
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

/**
 * Class for building workplace button separators. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;BUTTONSEPARATOR&gt;</code>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.10 $ $Date: 2000/08/08 14:08:30 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsButtonSeparator extends A_CmsWpElement implements I_CmsWpElement {    
		
	/**
	 * Handling of the special workplace <CODE>&lt;BUTTONSEPARATOR&gt;</CODE> tags.
	 * <P>
	 * Reads the code of a button separator from the buttons definition file
	 * and returns the processed code with the actual elements.
	 * <P>
	 * Button separators can be referenced in any workplace template by <br>
	 * <CODE>&lt;BUTTONSEPARATOR/&gt;</CODE>
	 * 
	 * @param cms CmsObject Object for accessing resources.
	 * @param n XML element containing the <code>&lt;BUTTONSEPARATOR&gt;</code> tag <em>(not used here)</em>.
	 * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
	 * @param callingObject reference to the calling object.
	 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
	 * @param lang CmsXmlLanguageFile conataining the currently valid language file <em>(not used here)</em>.
	 * @return Processed button separator.
	 * @exception CmsException
	 */    
	public Object handleSpecialWorkplaceTag(CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {

		CmsXmlWpButtonsDefFile buttondef = getButtonDefinitions(cms);
		String result = buttondef.getButtonSeparator(callingObject);
		return result; 
	}
}
