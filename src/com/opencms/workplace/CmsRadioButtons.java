/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsRadioButtons.java,v $
* Date   : $Date: 2003/07/22 00:29:22 $
* Version: $Revision: 1.14 $
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

import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;
import com.opencms.template.A_CmsXmlContent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Element;

/**
 * Class for building workplace radio buttons. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;RADIOBUTTON&gt;</code>.
 * 
 * @author Michael Emmerich
 * @author Alexander Lucas
 * @version $Revision: 1.14 $ $Date: 2003/07/22 00:29:22 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsRadioButtons extends A_CmsWpElement implements I_CmsWpElement,I_CmsWpConstants {
    
    /**
     * Handling of the special workplace <CODE>&lt;RADIOBUTTON&gt;</CODE> tags.
     * <P>
     * Reads the code of a selectbox from the input definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Select boxes can be referenced in any workplace template by <br>
     * <CODE>&lt;RADIOBUTTON name="..." action="..." alt="..."/&gt;</CODE>
     * 
     * @param cms CmsObject Object for accessing resources.
     * @param An XML element containing the <code>&lt;RADIOBUTTON&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @throws CmsException
     */
    
    public Object handleSpecialWorkplaceTag(CmsObject cms, Element n, A_CmsXmlContent doc, 
            Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        
        /** StringBuffer for the generated output */
        StringBuffer result = new StringBuffer();
        
        /** Here the different select box options will be stored */
        Vector values = new Vector();
        Vector names = new Vector();
        Vector descriptions = new Vector();
        Integer returnObject = null;
        String radioName = n.getAttribute(C_RADIO_NAME);
        String radioMethod = n.getAttribute(C_RADIO_METHOD);
        String radioOrder = n.getAttribute(C_RADIO_ORDER);
        if(radioOrder == null || ((!"row".equals(radioOrder)) && (!"col".equals(radioOrder)))) {
            radioOrder = "col";
        }
        
        // call the method for generating listbox elements
        Method groupsMethod = null;
        int selectedOption = 0;
        try {
            groupsMethod = callingObject.getClass().getMethod(radioMethod, new Class[] {
                CmsObject.class, CmsXmlLanguageFile.class, Vector.class, 
                Vector.class, Vector.class, Hashtable.class
            });
            returnObject = (Integer)groupsMethod.invoke(callingObject, new Object[] {
                cms, lang, names, values, descriptions, parameters
            });
        }
        catch(NoSuchMethodException exc) {
            
            // The requested method was not found.
            throwException("Could not find radio button method " + radioMethod 
                    + " in calling class " + callingObject.getClass().getName() 
                    + " for generating select box content.", CmsException.C_NOT_FOUND);
        }
        catch(InvocationTargetException targetEx) {
            
            // the method could be invoked, but throwed a exception            
            // itself. Get this exception and throw it again.              
            Throwable e = targetEx.getTargetException();
            if(!(e instanceof CmsException)) {
                
                throwException("Radio button method " + radioMethod + " in calling class " 
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
            throwException("Radio button method " + radioMethod + " in calling class " + callingObject.getClass().getName() 
                    + " was found but could not be invoked. " + exc2, CmsException.C_XML_NO_USER_METHOD);
        }
        
        // If the radio button method returned a value, use it for preselecting an option
        if(returnObject != null) {
            selectedOption = returnObject.intValue();
        }
        
        // process the vectors with the elelmetns of the radio buttons to be displayed.
        int numValues = values.size();
        CmsXmlWpTemplateFile radiodef = getRadioDefinitions(cms);
        for(int i = 0;i < numValues;i++) {
            
            // Set values for this radiobutton entry
            radiodef.setData(C_RADIO_RADIONAME, radioName);
            radiodef.setData(C_RADIO_NAME, (String)descriptions.elementAt(i));
            radiodef.setData(C_RADIO_LINK, (String)values.elementAt(i));
            
            // Check, if an image should be displayed
            if((String)names.elementAt(i) == null || "".equals(names.elementAt(i))) {
                radiodef.setData(C_RADIO_IMAGEENTRY, "");
            }
            else {
                radiodef.setData(C_RADIO_IMAGENAME, "ic_" + (String)names.elementAt(i) + ".gif");
                radiodef.setData(C_RADIO_IMAGEENTRY, radiodef.getProcessedDataValue("radiobuttons." 
                        + C_RADIO_IMAGEOPTION, callingObject));
            }
            
            // Check, if this should be the preselected option
            if(i == selectedOption) {
                radiodef.setData(C_RADIO_SELECTEDENTRY, radiodef.getDataValue("radiobuttons." + C_RADIO_SELECTEDOPTION));
            }
            else {
                radiodef.setData(C_RADIO_SELECTEDENTRY, "");
            }
            
            // Now get output for this option 
            if(radioOrder.equals("col")) {
                
                // Buttons should be displayed in one column 
                result.append(radiodef.getProcessedDataValue("radiobuttons.colentry", callingObject));
            }
            else {
                
                // Buttons should be displayed in a row.
                result.append(radiodef.getProcessedDataValue("radiobuttons.rowentry", callingObject));
            }
        }
        return result.toString();
    }
}
