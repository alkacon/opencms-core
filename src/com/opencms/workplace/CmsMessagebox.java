/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsMessagebox.java,v $
 * Date   : $Date: 2000/02/22 11:16:44 $
 * Version: $Revision: 1.5 $
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
 * Class for building workplace message boxes. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;MESSAGEBOX&gt;</code>.
 * 
 * @author Michael Emmerich
 * @author Michaela Schleich
 * @version $Revision: 1.5 $ $Date: 2000/02/22 11:16:44 $
 */
public class CmsMessagebox extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants  {    
    
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
     * Handling of the <CODE>&lt;MESSAGEBOX&gt;</CODE> tags.
     * <P>
     * Reads the code of a error box from the errors definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Error boxes can be referenced in any workplace template by <br>
     * // TODO: insert correct syntax here!
     * <CODE>&lt;MESSAGEBOX name="..." action="..." alt="..."/&gt;</CODE>
     * 
     * @param cms A_CmsObject Object for accessing resources.
     * @param An XML element containing the <code>&lt;MESSAGEBOX&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        // collect all required data
		Node helpfilename=null;
		String helpname=null;
		
        String messageTitle = n.getAttribute(C_MESSAGE_TITLE);
        String messageMessage1= n.getAttribute(C_MESSAGE_MESSAGE1);
        String messageMessage2= n.getAttribute(C_MESSAGE_MESSAGE2);     
        String messageButton1= n.getAttribute(C_MESSAGE_BUTTON1);
        String messageButton2= n.getAttribute(C_MESSAGE_BUTTON2);
        String messageLink1 = n.getAttribute(C_MESSAGE_LINK1);
        String messageLink2 = n.getAttribute(C_MESSAGE_LINK2);
		if(n.hasChildNodes()) {
			helpfilename=n.getFirstChild();
			helpname=helpfilename.getNodeValue();
		}
        
        CmsXmlWpBoxDefFile boxdef = getBoxDefinitions(cms);
        
        // get the data from the language file
        messageTitle = lang.getLanguageValue(messageTitle);
		if(helpfilename!=null) {
			messageTitle=messageTitle+": "+helpname;
		}
		messageMessage1= lang.getLanguageValue(messageMessage1);
        messageMessage2= lang.getLanguageValue(messageMessage2);
        messageButton1= lang.getLanguageValue(messageButton1);
        messageButton2= lang.getLanguageValue(messageButton2);
       
               
        // build errorbox
        String result = boxdef.getMessagebox(messageTitle,messageMessage1,messageMessage2,
                                             messageButton1,messageButton2,
                                             messageLink1,messageLink2);
        return result; 
    }                    
}
