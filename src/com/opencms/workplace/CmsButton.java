/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsButton.java,v $
 * Date   : $Date: 2000/04/06 08:31:26 $
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

package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;

import java.util.*;
import java.lang.reflect.*;

/**
 * Class for building workplace buttons. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;BUTTON&gt;</code>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.10 $ $Date: 2000/04/06 08:31:26 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsButton extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants {    
            
    /**
     * Handling of the special workplace <CODE>&lt;BUTTON&gt;</CODE> tags.
     * <P>
     * Reads the code of a button from the buttons definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Buttons can be referenced in any workplace template by <br>
     * <CODE>&lt;BUTTON name="..." action="..." alt="..."/&gt;</CODE>
     * 
     * @param cms A_CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;BUTTON&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */    
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
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
        boolean activate=true;
        
        if(buttonMethod != null && ! "".equals(buttonMethod))
        {
            Method callMethod = null;
            try {
                callMethod = callingObject.getClass().getMethod(buttonMethod, new Class[] {A_CmsObject.class, CmsXmlLanguageFile.class, Hashtable.class});
                activate = ((Boolean)callMethod.invoke(callingObject, new Object[] {cms, lang, parameters})).booleanValue();
            } catch(NoSuchMethodException exc) {
            // The requested method was not found.
            throwException("Could not find button activation method " + buttonMethod + " in calling class " + callingObject.getClass().getName() + " for generating icon.", CmsException.C_NOT_FOUND);
            } catch(InvocationTargetException targetEx) {
                // the method could be invoked, but throwed a exception
                // itself. Get this exception and throw it again.              
                Throwable e = targetEx.getTargetException();
                if(!(e instanceof CmsException)) {
                    // Only print an error if this is NO CmsException
                    e.printStackTrace();
                    throwException("Button activation method " + buttonMethod + " in calling class " + callingObject.getClass().getName() + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
                } else {
                    // This is a CmsException
                    // Error printing should be done previously.
                    throw (CmsException)e;
                }
            } catch(Exception exc2) {
                throwException("Button activation method " + buttonMethod + " in calling class " + callingObject.getClass().getName() + " was found but could not be invoked. " + exc2, CmsException.C_UNKNOWN_EXCEPTION);
            }
        }
        
        
        // Get button definition and language values
        CmsXmlWpButtonsDefFile buttondef = getButtonDefinitions(cms);
        buttonAlt = lang.getLanguageValue(C_LANG_BUTTON + "." + buttonAlt);
        
        // get the processed button.
        if(activate) {
            return buttondef.getButton(buttonName, buttonAction, buttonAlt, buttonHref, callingObject);
        } else {
            return buttondef.getDeactivatedButton(buttonName, buttonAction, buttonAlt, buttonHref, callingObject);
        }
    }           
}
