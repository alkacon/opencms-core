/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsInput.java,v $
* Date   : $Date: 2004/07/08 15:21:06 $
* Version: $Revision: 1.19 $
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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;

import com.opencms.template.A_CmsXmlContent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

import org.w3c.dom.Element;

/**
 * Class for building workplace input fields. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;INPUT&gt;</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.19 $ $Date: 2004/07/08 15:21:06 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsInput extends A_CmsWpElement {
    
    /**
     * Handling of the <CODE>&lt;INPUT&gt;</CODE> tags.
     * <P>
     * Reads the code of a input field from the input definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Input fields can be referenced in any workplace template by <br>
     * <CODE>&lt;INPUT name="..." action="..." alt="..."/&gt;</CODE>
     * 
     * @param cms CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;INPUT&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @throws CmsException
     */
    
    public Object handleSpecialWorkplaceTag(CmsObject cms, Element n, A_CmsXmlContent doc, 
            Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        String styleClass = n.getAttribute(C_INPUT_CLASS);
        String name = n.getAttribute(C_INPUT_NAME);
        String size = n.getAttribute(C_INPUT_SIZE);
        String length = n.getAttribute(C_INPUT_LENGTH);
        String value = n.getAttribute(C_INPUT_VALUE);
        String method = n.getAttribute(C_INPUT_METHOD);
        String action = n.getAttribute(C_INPUT_ACTION);
        if((method != null) && (method.length() != 0)) {
            
            // call the method for generating value
            Method valueMethod = null;
            try {
                valueMethod = callingObject.getClass().getMethod(method, new Class[] {
                    CmsObject.class, CmsXmlLanguageFile.class, Hashtable.class
                });
                value = (String)valueMethod.invoke(callingObject, new Object[] {
                    cms, lang, parameters
                });
            }
            catch(NoSuchMethodException exc) {
                
                // The requested method was not found.
                throwException("Could not find method " + method + " in calling class " 
                        + callingObject.getClass().getName() 
                        + " for generating input value content.", CmsException.C_NOT_FOUND);
            }
            catch(InvocationTargetException targetEx) {
                
                // the method could be invoked, but throwed a exception                
                // itself. Get this exception and throw it again.              
                Throwable e = targetEx.getTargetException();
                if(!(e instanceof CmsException)) {
                    
                    throwException("User method " + method + " in calling class " 
                            + callingObject.getClass().getName() + " throwed an exception. " 
                            + e, CmsException.C_UNKNOWN_EXCEPTION);
                }
                else {
                    
                    // This is a CmsException                    
                    // Error printing should be done previously.
                    throw (CmsException)e;
                }
            }
            catch(Exception exc2) {
                throwException("User method " + method + " in calling class " 
                        + callingObject.getClass().getName() 
                        + " was found but could not be invoked. " 
                        + exc2, CmsException.C_XML_NO_USER_METHOD);
            }
        }
        if(value == null) {
            value = "";
        }
        CmsXmlWpInputDefFile inputdef = getInputDefinitions(cms);
        String result = inputdef.getInput(styleClass, name, size, length, value, action);
        return result;
    }
}
