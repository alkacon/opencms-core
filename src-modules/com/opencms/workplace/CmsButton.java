/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/workplace/Attic/CmsButton.java,v $
* Date   : $Date: 2005/05/19 07:15:14 $
* Version: $Revision: 1.2 $
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

import com.opencms.legacy.CmsLegacyException;
import com.opencms.template.A_CmsXmlContent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

import org.w3c.dom.Element;

/**
 * Class for building workplace buttons. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;BUTTON&gt;</code>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.2 $ $Date: 2005/05/19 07:15:14 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsButton extends A_CmsWpElement {
    
    /**
     * Handling of the special workplace <CODE>&lt;BUTTON&gt;</CODE> tags.
     * <P>
     * Reads the code of a button from the buttons definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Buttons can be referenced in any workplace template by <br>
     * <CODE>&lt;BUTTON name="..." action="..." alt="..."/&gt;</CODE>
     * 
     * @param cms CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;BUTTON&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @throws CmsException
     */
    
    public Object handleSpecialWorkplaceTag(CmsObject cms, Element n, A_CmsXmlContent doc, 
            Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        
        // Read button parameters
        String buttonName = n.getAttribute(C_BUTTON_NAME);
        String buttonAction = n.getAttribute(C_BUTTON_ACTION);
        String buttonAlt = n.getAttribute(C_BUTTON_ALT);
        String buttonHref = n.getAttribute(C_BUTTON_HREF);
        String buttonMethod = n.getAttribute(C_BUTTON_METHOD);
        if(buttonHref == null || "".equals(buttonHref)) {
            buttonHref = "";
        }
        
        // call the method for activation decision
        boolean activate = true;
        if(buttonMethod != null && !"".equals(buttonMethod)) {
            Method callMethod = null;
            try {
                callMethod = callingObject.getClass().getMethod(buttonMethod, new Class[] {
                    CmsObject.class, CmsXmlLanguageFile.class, Hashtable.class
                });
                activate = ((Boolean)callMethod.invoke(callingObject, new Object[] {
                    cms, lang, parameters
                })).booleanValue();
            }
            catch(NoSuchMethodException exc) {
                
                // The requested method was not found.
                throwException("Could not find button activation method " + buttonMethod 
                        + " in calling class " + callingObject.getClass().getName() 
                        + " for generating icon.", CmsLegacyException.C_NOT_FOUND);
            }
            catch(InvocationTargetException targetEx) {
                
                // the method could be invoked, but throwed a exception                
                // itself. Get this exception and throw it again.              
                Throwable e = targetEx.getTargetException();
                if(!(e instanceof CmsException)) {
                    
                    throwException("Button activation method " + buttonMethod 
                            + " in calling class " + callingObject.getClass().getName() 
                            + " throwed an exception. " + e);
                }
                else {
                    
                    // This is a CmsException                    
                    // Error printing should be done previously.
                    throw (CmsException)e;
                }
            }
            catch(Exception exc2) {
                throwException("Button activation method " + buttonMethod 
                        + " in calling class " + callingObject.getClass().getName() 
                        + " was found but could not be invoked. " + exc2);
            }
        }
        
        // Get button definition and language values
        CmsXmlWpButtonsDefFile buttondef = getButtonDefinitions(cms);
        buttonAlt = lang.getLanguageValue(C_LANG_BUTTON + "." + buttonAlt);
        
        // get the processed button.
        if(activate) {
            return buttondef.getButton(buttonName, buttonAction, buttonAlt, buttonHref, 
                    callingObject);
        }
        else {
            return buttondef.getDeactivatedButton(buttonName, buttonAction, buttonAlt, 
                    buttonHref, callingObject);
        }
    }
}
