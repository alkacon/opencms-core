/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsInput.java,v $
 * Date   : $Date: 2000/03/27 09:53:43 $
 * Version: $Revision: 1.8 $
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
import java.lang.reflect.*;

/**
 * Class for building workplace input fields. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;INPUT&gt;</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.8 $ $Date: 2000/03/27 09:53:43 $
 */
public class CmsInput extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants  {    
    
    /**
     * Handling of the <CODE>&lt;INPUT&gt;</CODE> tags.
     * <P>
     * Reads the code of a input field from the input definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Input fields can be referenced in any workplace template by <br>
     * // TODO: insert correct syntax here!
     * <CODE>&lt;INPUT name="..." action="..." alt="..."/&gt;</CODE>
     * 
     * @param cms A_CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;INPUT&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */    
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        String styleClass= n.getAttribute(C_INPUT_CLASS);
        String name=n.getAttribute(C_INPUT_NAME);
        String size=n.getAttribute(C_INPUT_SIZE);
        String length=n.getAttribute(C_INPUT_LENGTH);
        String value=n.getAttribute(C_INPUT_VALUE);
        String method=n.getAttribute(C_INPUT_METHOD);
        String action=n.getAttribute(C_INPUT_ACTION);
		
		if( (method != null) && (method.length() != 0) ) {
			// call the method for generating value
			Method valueMethod = null;
			try {
			    valueMethod = callingObject.getClass().getMethod(method, new Class[] {A_CmsObject.class, CmsXmlLanguageFile.class, Hashtable.class});
			    value = (String)valueMethod.invoke(callingObject, new Object[] {cms, lang, parameters});
			} catch(NoSuchMethodException exc) {
			    // The requested method was not found.
			    throwException("Could not find method " + method + " in calling class " + callingObject.getClass().getName() + " for generating input value content.", CmsException.C_NOT_FOUND);
			} catch(InvocationTargetException targetEx) {
			    // the method could be invoked, but throwed a exception
			    // itself. Get this exception and throw it again.              
			    Throwable e = targetEx.getTargetException();
			    if(!(e instanceof CmsException)) {
			        // Only print an error if this is NO CmsException
			        e.printStackTrace();
			        throwException("User method " + method + " in calling class " + callingObject.getClass().getName() + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
			    } else {
			        // This is a CmsException
			        // Error printing should be done previously.
			        throw (CmsException)e;
			    }
			} catch(Exception exc2) {
			    throwException("User method " + method + " in calling class " + callingObject.getClass().getName() + " was found but could not be invoked. " + exc2, CmsException.C_XML_NO_USER_METHOD);
			}
		}
		
		if(value==null) {
			value = "";
		}
        
        CmsXmlWpInputDefFile inputdef = getInputDefinitions(cms); 
        String result = inputdef.getInput(styleClass,name,size,length,value,action);

        return result; 
    }                    
}
