package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;

import java.util.*;
import java.lang.reflect.*;

/**
 * Class for building workplace icons. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;ICON&gt;</code>.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.2 $ $Date: 2000/02/08 09:51:35 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsProjectlist extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants {
            
    /**
     * Handling of the special workplace <CODE>&lt;PROJECTLIST&gt;</CODE> tags.
     * <P>
     * Returns the processed code with the actual elements.
     * <P>
     * Projectöists can be referenced in any workplace template by <br>
     * // TODO: insert correct syntax here!
     * <CODE>&lt;PROJECTLIST /&gt;</CODE>
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
        // Read projectlist parameters
        String listMethod = n.getAttribute(C_PROJECTLIST_METHOD);
        
        // Get list definition and language values
        CmsXmlWpTemplateFile listdef = getProjectlistDefinitions(cms);
        
        // call the method for generating projectlist elements
        Method callingMethod = null;
		Vector list = new Vector();
        try {
            callingMethod = callingObject.getClass().getMethod(listMethod, new Class[] {A_CmsObject.class, CmsXmlLanguageFile.class});
            list = (Vector)callingMethod.invoke(callingObject, new Object[] {cms, lang});
        } catch(NoSuchMethodException exc) {
            // The requested method was not found.
            throwException("Could not find method " + listMethod + " in calling class " + callingObject.getClass().getName() + " for generating projectlist content.", CmsException.C_NOT_FOUND);
        } catch(InvocationTargetException targetEx) {
            // the method could be invoked, but throwed a exception
            // itself. Get this exception and throw it again.              
            Throwable e = targetEx.getTargetException();
            if(!(e instanceof CmsException)) {
                // Only print an error if this is NO CmsException
                e.printStackTrace();
                throwException("User method " + listMethod + " in calling class " + callingObject.getClass().getName() + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
            } else {
                // This is a CmsException
                // Error printing should be done previously.
                throw (CmsException)e;
            }
        } catch(Exception exc2) {
            throwException("User method " + listMethod + " in calling class " + callingObject.getClass().getName() + " was found but could not be invoked. " + exc2, CmsException.C_XML_NO_USER_METHOD);
        }
		
        /** StringBuffer for the generated output */
        StringBuffer result = new StringBuffer();
		String state = C_PROJECTLIST_STATE_UNLOCKED;
		String snaplock = "";
		
		for(int i = 0; i < list.size(); i++) {
			// get the actual project
			A_CmsProject project = (A_CmsProject) list.elementAt(i);
			// TODO: get the correct state of the project
			// state = ???;
			// TODO: get the correct snaplock of the project
			snaplock = listdef.getProcessedXmlDataValue(C_TAG_PROJECTLIST_SNAPLOCK,
														callingObject, parameters);
			System.err.println("Snaplock " + snaplock);
			
			// get the processed list.
			listdef.setXmlData(C_PROJECTLIST_NAME, project.getName());
			listdef.setXmlData(C_PROJECTLIST_LOCKSTATE, snaplock);
			listdef.setXmlData(C_PROJECTLIST_DESCRIPTION, project.getDescription());
			listdef.setXmlData(C_PROJECTLIST_STATE, lang.getLanguageValue(state));
			listdef.setXmlData(C_PROJECTLIST_PROJECTMANAGER, cms.readManagerGroup(project).getName());
			listdef.setXmlData(C_PROJECTLIST_PROJECTWORKER, cms.readGroup(project).getName());
			listdef.setXmlData(C_PROJECTLIST_DATECREATED, getNiceDate(project.getCreateDate()) );
			listdef.setXmlData(C_PROJECTLIST_OWNER, cms.readOwner(project).getName());
			result.append(listdef.getProcessedXmlDataValue(C_TAG_PROJECTLIST_DEFAULT, callingObject, parameters));
		}		
		return result.toString();
    }

     /**
      * Gets a formated time string form a long time value.
      * @param time The time value as a long.
      * @return Formated time string.
      */
     private String getNiceDate(long time) {
         StringBuffer niceTime=new StringBuffer();
         
         GregorianCalendar cal = new GregorianCalendar();
         cal.setTime(new Date(time));
         String day="0"+new Integer(cal.get(Calendar.DAY_OF_MONTH)).intValue();        
         String month="0"+new Integer(cal.get(Calendar.MONTH)+1).intValue(); 
         String year=new Integer(cal.get(Calendar.YEAR)).toString();
         String hour="0"+new Integer(cal.get(Calendar.HOUR)+12*cal.get(Calendar.AM_PM)).intValue();   
         String minute="0"+new Integer(cal.get(Calendar.MINUTE));   
         if (day.length()==3) {
             day=day.substring(1,3);
         }
         if (month.length()==3) {
             month=month.substring(1,3);
         }
         if (hour.length()==3) {
             hour=hour.substring(1,3);
         }
         if (minute.length()==3) {
             minute=minute.substring(1,3);
         }
         niceTime.append(day+".");
         niceTime.append(month+".");  
         niceTime.append(year+" ");
         niceTime.append(hour+":");
         niceTime.append(minute);
         return niceTime.toString();
     }
}
