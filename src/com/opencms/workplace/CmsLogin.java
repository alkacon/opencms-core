package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import javax.servlet.http.*;

import java.util.*;

/**
 * Template class for displaying the login screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.8 $ $Date: 2000/02/10 10:24:53 $
 */
public class CmsLogin extends CmsWorkplaceDefault implements I_CmsWpConstants,
                                                             I_CmsConstants {
           
    /**
     * Indicates if the results of this class are cacheable.
     * <P>
     * Complex classes that are able top include other subtemplates
     * have to check the cacheability of their subclasses here!
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param parameters Hashtable with all template class parameters.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, Hashtable parameters) {
        return false;
    }

    /**
     * Overwrtied the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the longin templated and processed the data input.
     * If the user has authentificated to the system, the login window is closed and
     * the workplace is opened. <br>
     * If the login was incorrect, an error message is displayed and the login
     * dialog is displayed again.
     * @param cms The CmsObject.
     * @param templateFile The login template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @exception Throws CmsException if something goes wrong.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, 
                             Hashtable parameters, String templateSelector)
        throws CmsException {
        String result = null;     
        String username=null;
        A_CmsUser user;
        // the template to be displayed
        String template="template";
        Hashtable preferences=new Hashtable();
        // get user name and password
        String name=(String)parameters.get("NAME");
        String password=(String)parameters.get("PASSWORD");
        // try to read this user
        if ((name != null) && (password != null)){
            try {
                username=cms.loginUser(name,password);
            } catch (CmsException e) {
              if (e.getType()==CmsException.C_NO_ACCESS) {
                    // there was an authentification error during login
                    // set user to null and switch to error template
                    username=null;     
                    template="error";
                } else {
                    throw e;
                }   
            }   
            // check if a user was found.
            if (username!= null) {
                // get a session for this user so that he is authentificated at the
                // end of this request
                HttpSession session = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsLogin] Login user " + username);
                }
                // now get the users preferences
                user=cms.readUser(username);
                preferences=(Hashtable)user.getAdditionalInfo(C_ADDITIONAL_INFO_PREFERENCES);
                // check if preferences are existing, otherwiese use defaults
                if (preferences == null) {
                    preferences=getDefaultPreferences();
                }
                session.putValue(C_ADDITIONAL_INFO_PREFERENCES,preferences);
            }
        }
       CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);          
        // this is the first time the dockument is selected, so reade the page forwarding
        if (username == null) {
            xmlTemplateDocument.clearStartup();
         }
        // process the selected template
        return startProcessing(cms,xmlTemplateDocument,"",parameters,template);
    
    }
    
    /**
     * Sets the default preferences for the current user if those values are not available.
     * @return Hashtable with default preferences.
     */
    private Hashtable getDefaultPreferences() {
        Hashtable pref=new Hashtable();
        
        // set the default columns in the filelist
        int filelist=C_FILELIST_TITLE+C_FILELIST_TYPE+C_FILELIST_CHANGED;
        // HACK
         filelist=4095;
        pref.put(C_USERPREF_FILELIST,new Integer(filelist));
        return pref;
    }
}
