package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * Class for building workplace buttons. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;BUTTON&gt;</code>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.4 $ $Date: 2000/02/02 10:30:08 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsSelectBox extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants {    
            
    /**
     * Handling of the special workplace <CODE>&lt;SELECTBOX&gt;</CODE> tags.
     * <P>
     * Reads the code of a selectbox from the input definition file
     * and returns the processed code with the actual elements.
     * <P>
     * Select boxes can be referenced in any workplace template by <br>
     * // TODO: insert correct syntax here!
     * <CODE>&lt;SELECTBOX name="..." action="..." alt="..."/&gt;</CODE>
     * 
     * @param cms A_CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;SELECTBOX&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */    
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        
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
        
        // Get input definition file
        CmsXmlWpInputDefFile inputdef = getInputDefinitions(cms); 
        
        // get the processed selectbox start.
        result.append(inputdef.getSelectBoxStart(selectClass, selectName, selectWidth, selectOnchange));
        
        // call the method for generating listbox elements
        Method groupsMethod = null;
        int selectedOption = 0;
        try {
            groupsMethod = callingObject.getClass().getMethod(selectMethod, new Class[] {A_CmsObject.class, CmsXmlLanguageFile.class, Vector.class, Vector.class});
            selectedOption = ((Integer)groupsMethod.invoke(callingObject, new Object[] {cms, lang, values, names})).intValue();
        } catch(NoSuchMethodException exc) {
            // The requested method was not found.
            throwException("Could not find method " + selectMethod + " in calling class " + callingObject.getClass().getName() + " for generating select box content.", CmsException.C_NOT_FOUND);
        } catch(InvocationTargetException targetEx) {
            // the method could be invoked, but throwed a exception
            // itself. Get this exception and throw it again.              
            Throwable e = targetEx.getTargetException();
            if(!(e instanceof CmsException)) {
                // Only print an error if this is NO CmsException
                e.printStackTrace();
                throwException("User method " + selectMethod + " in calling class " + callingObject.getClass().getName() + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
            } else {
                // This is a CmsException
                // Error printing should be done previously.
                throw (CmsException)e;
            }
        } catch(Exception exc2) {
            throwException("User method " + selectMethod + " in calling class " + callingObject.getClass().getName() + " was found but could not be invoked. " + exc2, CmsException.C_XML_NO_USER_METHOD);
        }
                      
        // check the returned elements and put them into option tags.
        // The element with index "selectedOption" has to get the "selected" tag.
        int numValues = values.size();
        int numNames = names.size();
        
        for(int i=0; i<numValues; i++) {
            if(i == selectedOption) {
                result.append(inputdef.getSelectBoxSelOption((String)values.elementAt(i), (String)names.elementAt(i)));
            } else {
                result.append(inputdef.getSelectBoxOption((String)values.elementAt(i), (String)names.elementAt(i)));
            }                        
        }
        
        // get the processed selectbox end sequence.
        result.append(inputdef.getSelectBoxEnd());        
        return result.toString(); 
    }           
}
