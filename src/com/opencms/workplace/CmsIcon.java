/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsIcon.java,v $
* Date   : $Date: 2005/02/18 15:18:51 $
* Version: $Revision: 1.21 $
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
 * Class for building workplace icons. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;ICON&gt;</code>.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.21 $ $Date: 2005/02/18 15:18:51 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsIcon extends A_CmsWpElement {
    
    /**
     * Handling of the special workplace <CODE>&lt;ICON&gt;</CODE> tags.
     * <P>
     * Returns the processed code with the actual elements.
     * <P>
     * Icons can be referenced in any workplace template by <br>
     * <CODE>&lt;ICON name="..." label="..." action="..." href="..." target="..."/&gt;</CODE>
     * 
     * @param cms CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;ICON&gt;</code> tag.
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
        String iconName = n.getAttribute(C_ICON_NAME);
        String iconLabel = n.getAttribute(C_ICON_LABEL);
        String iconAction = n.getAttribute(C_ICON_ACTION);
        String iconHref = n.getAttribute(C_ICON_HREF);
        String iconTarget = n.getAttribute(C_ICON_TARGET);
        String iconActiveMethod = n.getAttribute(C_ICON_ACTIVE_METHOD);
        String iconVisibleMethod = n.getAttribute(C_ICON_VISIBLE_METHOD);
        if(iconHref == null || "".equals(iconHref)) {
            iconHref = "";
        }
        
        // call the method for activation decision
        boolean activate = true;
        if(iconActiveMethod != null && !"".equals(iconActiveMethod)) {
            Method groupsMethod = null;
            try {
                groupsMethod = callingObject.getClass().getMethod(iconActiveMethod, 
                        new Class[] {
                    CmsObject.class, CmsXmlLanguageFile.class, Hashtable.class
                });
                activate = ((Boolean)groupsMethod.invoke(callingObject, new Object[] {
                    cms, lang, parameters
                })).booleanValue();
            }
            catch(NoSuchMethodException exc) {
                
                // The requested method was not found.
                throwException("Could not find icon activation method " + iconActiveMethod 
                + " in calling class " + callingObject.getClass().getName() 
                + " for generating icon.", CmsException.C_NOT_FOUND);
            }
            catch(InvocationTargetException targetEx) {
                
                // the method could be invoked, but throwed a exception                
                // itself. Get this exception and throw it again.              
                Throwable e = targetEx.getTargetException();
                if(!(e instanceof CmsException)) {
                    
                    throwException("Icon activation method " + iconActiveMethod 
                            + " in calling class " + callingObject.getClass().getName() 
                            + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
                }
                else {
                    
                    // This is a CmsException                    
                    // Error printing should be done previously.
                    throw (CmsException)e;
                }
            }
            catch(Exception exc2) {
                throwException("Icon activation method " + iconActiveMethod 
                        + " in calling class " + callingObject.getClass().getName() 
                        + " was found but could not be invoked. " + exc2, CmsException.C_UNKNOWN_EXCEPTION);
            }
        }
        
        // call the method for the visibility decision
        boolean visible = true;
        if(iconVisibleMethod != null && !"".equals(iconVisibleMethod)) {
            Method groupsMethod = null;
            try {
                groupsMethod = callingObject.getClass().getMethod(iconVisibleMethod, new Class[] {
                    CmsObject.class, CmsXmlLanguageFile.class, Hashtable.class
                });
                visible = ((Boolean)groupsMethod.invoke(callingObject, new Object[] {
                    cms, lang, parameters
                })).booleanValue();
            }
            catch(NoSuchMethodException exc) {
                
                // The requested method was not found.
                throwException("Could not find icon activation method " + iconVisibleMethod 
                        + " in calling class " + callingObject.getClass().getName() 
                        + " for generating icon.", CmsException.C_NOT_FOUND);
            }
            catch(InvocationTargetException targetEx) {
                
                // the method could be invoked, but throwed a exception                
                // itself. Get this exception and throw it again.              
                Throwable e = targetEx.getTargetException();
                if(!(e instanceof CmsException)) {
                    
                    throwException("Icon activation method " + iconVisibleMethod 
                            + " in calling class " + callingObject.getClass().getName() 
                            + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
                }
                else {
                    
                    // This is a CmsException                    
                    // Error printing should be done previously.
                    throw (CmsException)e;
                }
            }
            catch(Exception exc2) {
                throwException("Icon activation method " + iconVisibleMethod 
                    + " in calling class " + callingObject.getClass().getName() 
                    + " was found but could not be invoked. " + exc2, CmsException.C_UNKNOWN_EXCEPTION);
            }
        }
        
        // Get button definition and language values
        CmsXmlWpTemplateFile icondef = getIconDefinitions(cms);
        StringBuffer iconLabelBuffer = new StringBuffer(lang.getLanguageValue(C_LANG_ICON + "." + iconLabel));
        
        // Insert a html-break, if needed
        if(iconLabelBuffer.toString().indexOf("- ") != -1) {
            iconLabelBuffer.insert(iconLabelBuffer.toString().indexOf("- ") + 2, "<BR>");
        }
        
        // get the processed button.
        icondef.setData(C_ICON_NAME, iconName);
        icondef.setData(C_ICON_LABEL, iconLabelBuffer.toString());
        icondef.setData(C_ICON_ACTION, iconAction);
        icondef.setData(C_ICON_HREF, iconHref);
        icondef.setData(C_ICON_TARGET, iconTarget);
        if(visible) {
            if(activate) {
                return icondef.getProcessedDataValue("defaulticon", callingObject);
            }
            else {
                return icondef.getProcessedDataValue("deactivatedicon", callingObject);
            }
        }
        else {
            return icondef.getProcessedDataValue("noicon", callingObject);
        }
    }
}
