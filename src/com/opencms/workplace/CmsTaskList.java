package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;

import java.util.*;
import java.lang.reflect.*;

/**
 * Class for building task list. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;tasklist&gt;</code>.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 2000/02/15 17:53:49 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsTaskList extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants {
	
    /**
     * Handling of the special workplace <CODE>&lt;TASKLIST&gt;</CODE> tags.
     * <P>
     * Returns the processed code with the actual elements.
     * <P>
     * Projectlists can be referenced in any workplace template by <br>
     * // TODO: insert correct syntax here!
     * <CODE>&lt;TASKLIST /&gt;</CODE>
     * 
     * @param cms A_CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;ICON&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */    
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        
        // Get list definition values
        CmsXmlWpTemplateFile listdef = getTaskListDefinitions(cms);
		
        String listMethod = n.getAttribute("method");

        // call the method for generating projectlist elements
        Method callingMethod = null;
		Vector list = new Vector();
        try {
            callingMethod = callingObject.getClass().getMethod(listMethod, new Class[] {A_CmsObject.class, CmsXmlLanguageFile.class});
            list = (Vector)callingMethod.invoke(callingObject, new Object[] {cms, lang});
        } catch(NoSuchMethodException exc) {
            // The requested method was not found.
            throwException("Could not find method " + listMethod + " in calling class " + callingObject.getClass().getName() + " for generating lasklist content.", CmsException.C_NOT_FOUND);
        } catch(InvocationTargetException targetEx) {
            // the method could be invoked, but throwed a exception
            // itself. Get this exception and throw it again.              
            Throwable e = targetEx.getTargetException();
            if(!(e instanceof CmsException)) {
                // Only print an error if this is NO CmsException
                throwException("User method " + listMethod + " in calling class " + callingObject.getClass().getName() + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
            } else {
                // This is a CmsException
                // Error printing should be done previously.
                throw (CmsException)e;
            }
        } catch(Exception exc2) {
            throwException("User method " + listMethod + " in calling class " + callingObject.getClass().getName() + " was found but could not be invoked. " + exc2, CmsException.C_XML_NO_USER_METHOD);
        }
		
		try {
System.err.println(">>>" + 1);
        /** StringBuffer for the generated output */
        StringBuffer result = new StringBuffer();
System.err.println(">>>" + 2);
		
		for(int i = 0; i < list.size(); i++) {
System.err.println(">>>" + 3);
			// get the actual project
			A_CmsTask task = (A_CmsTask) list.elementAt(i);
System.err.println(">>>" + 31 + " " + task);
System.err.println(">>>" + 311 + " " + task.getName());
System.err.println(">>>" + 312 + " " + listdef);
			  
			// get the processed list.
			listdef.setXmlData("task", task.getName());
System.err.println(">>>" + 32 + " " + task.getAgentUser());
			listdef.setXmlData("foruser", task.getAgentUser() + "");
System.err.println(">>>" + 33);
			listdef.setXmlData("forrole", task.getRole() + "");
System.err.println(">>>" + 34);
			listdef.setXmlData("actuator", task.getInitiatorUser() + "");
System.err.println(">>>" + 35);
			listdef.setXmlData("due", task.getTimeOut().toLocaleString());
System.err.println(">>>" + 36);
			listdef.setXmlData("from", task.getStartTime().toLocaleString());
System.err.println(">>>" + 37);
			listdef.setXmlData("project", task.getParent() + "");
			
System.err.println(">>>" + 4);
			result.append(listdef.getProcessedXmlDataValue("defaulttasklist", callingObject, parameters));
System.err.println(">>>" + 5);

		}		
		return result.toString();
			} catch(Exception exc) {
				exc.printStackTrace();
				return "";
			}
    }
}
