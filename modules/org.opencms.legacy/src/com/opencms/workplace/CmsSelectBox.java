/*
* File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.legacy/src/com/opencms/workplace/Attic/CmsSelectBox.java,v $
* Date   : $Date: 2005/05/16 17:44:59 $
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

package com.opencms.workplace;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;

import com.opencms.legacy.CmsLegacyException;
import com.opencms.template.A_CmsXmlContent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Element;

/**
 * Class for building workplace buttons. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;BUTTON&gt;</code>.
 * 
 * @author Alexander Lucas
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 2005/05/16 17:44:59 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsSelectBox extends A_CmsWpElement {
    
    /**
     * Handling of the special workplace <CODE>&lt;SELECTBOX&gt;</CODE> tags.
     * <P>
     * Reads the code of a selectbox from the input definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Select boxes can be referenced in any workplace template by <br>
     * <CODE>&lt;SELECTBOX name="..." action="..." alt="..."/&gt;</CODE>
     * 
     * @param cms CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;SELECTBOX&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @throws CmsException
     */
    
    public Object handleSpecialWorkplaceTag(CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, 
            Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        
        /** Here the different select box options will be stored */
        Vector values = new Vector();
        Vector names = new Vector();
        
        /** StringBuffer for the generated output */
        StringBuffer result = new StringBuffer();
        
        // Read selectbox parameters
        String selectClass = n.getAttribute(C_SELECTBOX_CLASS);
        String selectName = n.getAttribute(C_SELECTBOX_NAME);
        String selectMethod = n.getAttribute(C_SELECTBOX_METHOD);
        String selectWidth = n.getAttribute(C_SELECTBOX_WIDTH);
        String selectOnchange = n.getAttribute(C_SELECTBOX_ONCHANGE);
        String selectSize = n.getAttribute(C_SELECTBOX_SIZE);
        String selectDiv = n.getAttribute(C_SELECTBOX_DIV);
        if((selectSize == null) || (selectSize.length() == 0)) {
            selectSize = "1";
        }
        
        // Get input definition file
        CmsXmlWpInputDefFile inputdef = getInputDefinitions(cms);
        if((selectDiv == null) || (selectDiv.length() == 0)) {
            
            // get the processed selectbox start.
            result.append(inputdef.getSelectBoxStart(selectClass, selectName, selectWidth, selectOnchange, selectSize));
        }
        else {
            result.append(inputdef.getSelectBoxStartDiv(selectClass, selectName, selectWidth, selectOnchange, selectSize));
        }
        
        // call the method for generating listbox elements
        Method groupsMethod = null;
        int selectedOption = 0;
        try {
            groupsMethod = callingObject.getClass().getMethod(selectMethod, new Class[] {
                CmsObject.class, CmsXmlLanguageFile.class, Vector.class, 
                Vector.class, Hashtable.class
            });
            selectedOption = ((Integer)groupsMethod.invoke(callingObject, new Object[] {
                cms, lang, values, names, parameters
            })).intValue();
        }
        catch(NoSuchMethodException exc) {
            
            // The requested method was not found.
            throwException("Could not find method " + selectMethod + " in calling class " + callingObject.getClass().getName() 
                    + " for generating select box content.", CmsLegacyException.C_NOT_FOUND);
        }
        catch(InvocationTargetException targetEx) {
            
            // the method could be invoked, but throwed a exception            
            // itself. Get this exception and throw it again.              
            Throwable e = targetEx.getTargetException();
            if(!(e instanceof CmsException)) {
                
                // Only print an error if this is NO CmsException
                throwException("User method " + selectMethod + " in calling class " + callingObject.getClass().getName() 
                        + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
            }
            else {
                
                // This is a CmsException                
                // Error printing should be done previously.
                throw (CmsException)e;
            }
        }
        catch(Exception exc2) {
            throwException("User method " + selectMethod + " in calling class " + callingObject.getClass().getName() 
                    + " was found but could not be invoked. " + exc2, CmsLegacyException.C_XML_NO_USER_METHOD);
        }
        
        // check the returned elements and put them into option tags.        
        // The element with index "selectedOption" has to get the "selected" tag.
        int numValues = values.size();
        
        for(int i = 0;i < numValues;i++) {
            if(i == selectedOption) {
                result.append(inputdef.getSelectBoxSelOption((String)values.elementAt(i), (String)names.elementAt(i)));
            }
            else {
                result.append(inputdef.getSelectBoxOption((String)values.elementAt(i), (String)names.elementAt(i)));
            }
        }
        
        // get the processed selectbox end sequence.
        result.append(inputdef.getSelectBoxEnd());
        return result.toString();
    }
}
