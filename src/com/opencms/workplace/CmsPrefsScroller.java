/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsPrefsScroller.java,v $
* Date   : $Date: 2005/02/18 15:18:51 $
* Version: $Revision: 1.13 $
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
 * Class for building workplace preferences scroller windows. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;PREFSSCROLLER&gt;</code>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.13 $ $Date: 2005/02/18 15:18:51 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsPrefsScroller extends A_CmsWpElement {
    
    
    /** XML tag attribute used for the filling method */
    private static final String C_WPTAG_ATTR_METHOD = "method";
    
    
    /** XML tag attribute used for the title */
    private static final String C_WPTAG_ATTR_TITLE = "title";
    
    
    /** XML datablock tag used for setting the title */
    private static final String C_TAG_PREFSSCROLLER_TITLE = "scrollertitle";
    
    
    /** XML datablock tag used for setting the title */
    private static final String C_TAG_PREFSSCROLLER_CONTENT = "scrollercontent";
    
    
    /** XML datablock tag used for processing and getting the complete prefs scroller */
    private static final String C_TAG_PREFSSCROLLER_COMPLETE = "scrollerwin";
    
    /**
     * Handling of the <CODE>&lt;PREFSSCROLLER&gt;</CODE> tags.
     * <P>
     * Reads the code of a preferences scroller from the prefsscroller definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Preferences scroller windows can be created in any workplace template by <br>
     * <code>&lt;PREFSSCROLLER method="..." title="..."&gt;</code><br>
     * 
     * The given method will be looked up in the calling object and then 
     * called using reflection API. The preferences scroller will be filled 
     * with the return value of this method.
     * <P>
     * The title of the preferences scroller will be looked up in the
     * "title" section of the current language file using the given 
     * tag name.
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
        String methodName = n.getAttribute(C_WPTAG_ATTR_METHOD);
        String title = n.getAttribute(C_WPTAG_ATTR_TITLE);
        
        // prefs scroller definition file
        CmsXmlWpTemplateFile prefsscrollerdef = getPrefsScrollerDefinitions(cms);
        
        // call the method for generating listbox elements
        Method fillMethod = null;
        String fillResult = null;
        try {
            fillMethod = callingObject.getClass().getMethod(methodName, new Class[] {
                CmsObject.class, A_CmsXmlContent.class, CmsXmlLanguageFile.class, 
                Hashtable.class, Object.class
            });
            fillResult = (String)fillMethod.invoke(callingObject, new Object[] {
                cms, doc, lang, parameters, callingObject
            });
        }
        catch(NoSuchMethodException exc) {
            
            // The requested method was not found.
            throwException("Could not find prefs scroller fill method " + methodName 
                    + " in calling class " + callingObject.getClass().getName() + " for generating select box content.", 
                    CmsException.C_NOT_FOUND);
        }
        catch(InvocationTargetException targetEx) {
            
            // the method could be invoked, but throwed a exception            
            // itself. Get this exception and throw it again.              
            Throwable e = targetEx.getTargetException();
            if(!(e instanceof CmsException)) {
                
                throwException("Prefs scroller fill method " + methodName + " in calling class " 
                        + callingObject.getClass().getName() + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
            }
            else {
                
                // This is a CmsException                
                // Error printing should be done previously.
                throw (CmsException)e;
            }
        }
        catch(Exception exc2) {
            throwException("Prefs scroller fill method " + methodName + " in calling class " 
                    + callingObject.getClass().getName() + " was found but could not be invoked. " 
                    + exc2, CmsException.C_XML_NO_USER_METHOD);
        }
        prefsscrollerdef.setData(C_TAG_PREFSSCROLLER_TITLE, lang.getLanguageValue(C_LANG_TITLE + "." + title));
        prefsscrollerdef.setData(C_TAG_PREFSSCROLLER_CONTENT, fillResult);
        return prefsscrollerdef.getProcessedDataValue(C_TAG_PREFSSCROLLER_COMPLETE, callingObject, parameters);
    }
}
