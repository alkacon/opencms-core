/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplace.java,v $
 * Date   : $Date: 2003/08/03 09:42:42 $
 * Version: $Revision: 1.19 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.opencms.workplace;

import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManager;

import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsRequestContext;
import com.opencms.file.CmsResource;
import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.util.Encoder;
import com.opencms.util.LinkSubstitution;
import com.opencms.workplace.I_CmsWpConstants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

/**
 * Master class for the JSP based workplace which provides default methods and
 * session handling for all JSP workplace classes.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.19 $
 * 
 * @since 5.1
 */
public abstract class CmsWorkplace {
    
    protected static final String C_SESSION_WORKPLACE_SETTINGS = "__CmsWorkplace.WORKPLACE_SETTINGS";
    protected static final String C_SESSION_WORKPLACE_CLASS    = "__CmsWorkplace.WORKPLACE_CLASS";
    
    /** Path definitions for workplace */
    protected static final String C_PATH_WORKPLACE = I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "jsp/";
    protected static final String C_PATH_DIALOGS = C_PATH_WORKPLACE + "dialogs/";
    protected static final String C_PATH_DIALOG_COMMON = C_PATH_DIALOGS + "common/";
    
    protected static final String C_FILE_EXPLORER_FILELIST = C_PATH_WORKPLACE + "explorer_files.html";
    protected static final String C_FILE_DIALOG_SCREEN_WAIT = C_PATH_DIALOG_COMMON + "wait.html";
    protected static final String C_FILE_DIALOG_SCREEN_ERROR = C_PATH_DIALOG_COMMON + "error.html";
    protected static final String C_FILE_DIALOG_SCREEN_CONFIRM = C_PATH_DIALOG_COMMON + "confirmation.html";
   
    private static String m_file_explorer_filelist; 
    
    private CmsJspActionElement m_jsp;
    private CmsObject m_cms;
    private HttpSession m_session;
    private CmsWorkplaceSettings m_settings;
    private String m_resourceUri = null;
    
    public static final int HTML_START = 0;
    public static final int HTML_END = 1;
    
    public static final boolean DEBUG = false;
        
    /**
     * Public constructor.<p>
     * 
     * @param jsp the initialized JSP context
     */    
    public CmsWorkplace(CmsJspActionElement jsp) {
        m_jsp = jsp;        
        m_cms = m_jsp.getCmsObject();                
        m_session = m_jsp.getRequest().getSession();
        
        // get / create the workplace settings 
        m_settings = (CmsWorkplaceSettings)m_session.getAttribute(C_SESSION_WORKPLACE_SETTINGS);
        if (m_settings == null) {
            // create the settings object
            m_settings = new CmsWorkplaceSettings();
            initWorkplaceSettings(m_cms, m_settings);
            storeSettings(m_session, m_settings);
        }
        
        // check request for changes in the workplace settings
        initWorkplaceRequestValues(m_settings, m_jsp.getRequest());        
        
        // set cms context accordingly
        initWorkplaceCmsContext(m_settings, m_cms);
    }    
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */    
    public CmsWorkplace(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }
    
    /**
     * Get a localized key value for the workplace.<p>
     * 
     * @param keyName name of the key
     * @return a localized key value
     */
    public String key(String keyName) {
        return m_settings.getMessages().key(keyName);
    } 
    
    /**
     * Returns the current workplace encoding.<p>
     * 
     * @return the current workplace encoding
     */
    public String getEncoding() {
        return  m_settings.getMessages().getEncoding();
    }
    
    /**
     * Stores the settings in the given session.<p>
     * 
     * @param session the session to store the settings in
     * @param settings the settings
     */
    static synchronized void storeSettings(HttpSession session, CmsWorkplaceSettings settings) {
        // save the workplace settings in the session
        session.setAttribute(C_SESSION_WORKPLACE_SETTINGS, settings);        
    }
    
    /**
     * Initializes the current users workplace settings by reading the values 
     * from the users preferences.<p>
     * 
     * This method is synchronized to ensure that the settings are
     * initialized only once for a user.
     * 
     * @param cms the cms object for the current user
     * @param settings the current workplace settings
     * @return initialized object with the current users workplace settings 
     */    
    static synchronized CmsWorkplaceSettings initWorkplaceSettings(CmsObject cms, CmsWorkplaceSettings settings) {                
        // initialize the current user language
        String language = null;               
        Hashtable startSettings =
            (Hashtable)cms.getRequestContext().currentUser().getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_STARTSETTINGS);  
        // try to read it form the user additional info
        if (startSettings != null) {
            language = (String)startSettings.get(I_CmsConstants.C_START_LANGUAGE);
        }    
        // no startup language in user settings found, so check the users browser locale settings
        if (language == null) {
            Vector languages = cms.getRequestContext().getAcceptedLanguages();
            int numlangs = languages.size();
            for (int i = 0; i < numlangs; i++) {
                String lang = (String)languages.elementAt(i);
                try {
                    cms.readFolder(I_CmsWpConstants.C_VFS_PATH_LOCALES + lang);
                    // if we get past that readFolder() the language is supported
                    language = lang;
                    break;
                } catch (CmsException e) {
                    // browser language is not supported in OpenCms, continue looking
                }
            }
        }
        // if no language was found so far, use the default language
        if (language == null) {
            language = I_CmsWpConstants.C_DEFAULT_LANGUAGE;
        }
        
        // save language in settings
        settings.setLanguage(language);        
        
        // initialize messages and also store them in settings
        CmsWorkplaceMessages messages = new CmsWorkplaceMessages(cms, language);
        settings.setMessages(messages);        
        
        // save current workplace user
        settings.setUser(cms.getRequestContext().currentUser());

        // save current project
        settings.setProject(cms.getRequestContext().currentProject().getId());
        
        // save current site
        String siteRoot = cms.getRequestContext().getSiteRoot();
        boolean access = false;
        CmsResource res = null;
        try {
            res = cms.readFileHeader("/");   
            access = cms.hasPermissions(res, I_CmsConstants.C_VIEW_ACCESS);
        } catch (CmsException e) {
            // error reading site root, in this case we will use a readable default
        }
        if ((res == null) || !access) {
            List sites = CmsSiteManager.getAvailableSites(cms, true);
            if (sites.size() > 0) {
                siteRoot = ((CmsSite)sites.get(0)).getSiteRoot();
                cms.getRequestContext().setSiteRoot(siteRoot);
            }
        }            
        settings.setSite(siteRoot);
        
        // check out the user information for a default view that might be stored there
        if (startSettings != null) {
            settings.setCurrentView(LinkSubstitution.getLinkSubstitution(cms, (String)startSettings.get(I_CmsConstants.C_START_VIEW)));
        }
                  
        return settings;   
    }
    
    /**
     * Analyzes the request for workplace parameters and adjusts the workplace
     * settings accordingly.<p> 
     * 
     * @param settings the workplace settings
     * @param request the current request
     */
    protected abstract void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request);
        
    /**
     * Sets the cms request context and other cms related settings to the 
     * values stored int the workplace settings.<p>
     * 
     * @param settings the workplace settings
     * @param cms the current cms object
     */
    private void initWorkplaceCmsContext(CmsWorkplaceSettings settings, CmsObject cms) {

        CmsRequestContext reqCont = cms.getRequestContext();

        // check project setting        
        if (settings.getProject() != reqCont.currentProject().getId()) {
            try {                
                reqCont.setCurrentProject(settings.getProject());
            } catch (CmsException e) {
                // do nothing
            }                    
        }
        
        // check site setting
        if (!(settings.getSite().equals(reqCont.getSiteRoot()))) {
            reqCont.setSiteRoot(settings.getSite());
            settings.setExplorerChecksum(-2);
            settings.setExplorerResource("/");
        }        
    }
    
    /**
     * Returns the initialized cms object for the current user.<p>
     * 
     * @return the initialized cms object for the current user
     */
    public CmsObject getCms() {
        return m_cms;
    }

    /**
     * Returns the JSP action element.<p>
     * 
     * @return the JSP action element
     */
    public CmsJspActionElement getJsp() {
        return m_jsp;
    }

    /**
     * Returns the current user http session.<p>
     * 
     * @return the current user http session
     */
    public HttpSession getSession() {
        return m_session;
    }

    /**
     * Returns the current users workplace settings.<p>
     * 
     * @return the current users workplace settings
     */
    public CmsWorkplaceSettings getSettings() {
        return m_settings;
    }
    
    /**
     * Returns the path to the workplace static resources.<p>
     * 
     * Workplaces static resources are images, css files etc.
     * These are exported during the installation of OpenCms,
     * and are usually only read from this exported location to 
     * avoid the overhaead of accessing the database later.<p> 
     * 
     * @return the path to the workplace static resources
     */
    public String getResourceUri() {
        if (m_resourceUri != null) return m_resourceUri;
        synchronized (this) {
            boolean useVfs = true;
            // check registry for setting of workplace images
            try {
                useVfs = (new Boolean(A_OpenCms.getRegistry().getSystemValue("UseWpPicturesFromVFS"))).booleanValue();
            } catch (CmsException e) {
                // by default (useVfs == true) we assume that we want to use exported resources
            }            
            if (useVfs) {
                m_resourceUri = m_cms.getRequestContext().getRequest().getServletUrl() + I_CmsWpConstants.C_VFS_PATH_SYSTEMPICS;
            } else {
                m_resourceUri = m_cms.getRequestContext().getRequest().getWebAppUrl() + I_CmsWpConstants.C_SYSTEM_PICS_EXPORT_PATH;
            }            
        }
        return m_resourceUri;
    }
    
    /**
     * Returns the path to the currently selected skin.<p>
     * 
     * @return the path to the currently selected skin
     */
    public String getSkinUri() {
        return m_cms.getRequestContext().getRequest().getWebAppUrl() + "/skins/modern/";        
    }    
    
    /**
     * Generates a html select box out of the provided values.<p>
     * 
     * @param parameters a string that will be inserted into the initial select tag,
     *      if null no parameters will be inserted
     * @param options the options 
     * @param values the option values, if null the select will have no value attributes
     * @param selected the index of the pre-selected option, if -1 no option is pre-selected
     * @return a String representing a html select box
     */
    public String buildSelect(String parameters, List options, List values, int selected) {
        StringBuffer result = new StringBuffer(1024);
        result.append("<select ");
        if (parameters != null) result.append(parameters);
        result.append(">\n");
        int length = options.size();
        String value = null;
        for (int i=0; i<length; i++) {
            if (values != null) {
                try {
                    value = (String)values.get(i);
                } catch (Exception e) {
                    // lists are not properly initialized, just don't use the value
                    value = null;
                }                
            }
            if (value == null) {
                result.append("<option");
                if (i == selected) result.append(" selected");   
                result.append(">");  
                result.append(options.get(i));
                result.append("</option>\n");
            } else {
                result.append("<option value=\"");
                result.append(value);
                result.append("\""); 
                if (i == selected) result.append(" selected");
                result.append(">");                
                result.append(options.get(i));
                result.append("</option>\n");                
            }       
        }        
        result.append("</select>\n");                
        return result.toString();
    }
    
    /**
     * Builds the start html of the page, including setting of DOCTYPE and 
     * inserting a header with the content-type.<p>
     * 
     * @param title the content for the title tag
     * @return the start html of the page
     */
    public String htmlStart(String title) {
        return pageHtml(HTML_START, title);
    } 
    
    /**
     * Builds the end html of the page.<p>
     * 
     * @return the end html of the page
     */
    public String htmlEnd() {
        return pageHtml(HTML_END, null);
    } 
                    
    /**
     * Returns the default html for a workplace page, including setting of DOCTYPE and 
     * inserting a header with the content-type.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param title the title of the page, if null no title tag is inserted
     * @return the default html for a workplace page
     */
    public String pageHtml(int segment, String title) {
        if (segment == HTML_START) {
            StringBuffer result = new StringBuffer(512);
            result.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n");
            result.append("<html>\n<head>\n");
            if (title != null) {
                result.append("<title>");
                result.append(title);
                result.append("</title>\n");
            }
            result.append("<meta HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=");
            result.append(getEncoding());
            result.append("\">\n");
            result.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
            result.append(getSkinUri());
            result.append("files/css_workplace.css\">\n");
            return result.toString();
        } else {
            return "</html>";
        }
    }   
    
    /**
     * Builds the start html of the body.<p>
     * 
     * @param className optional class attribute to add to the body tag
     * @return the start html of the body
     */    
    public String bodyStart(String className) {
        return pageBody(HTML_START, className, null);
    }
    
    /**
     * Builds the start html of the body.<p>
     * 
     * @param className optional class attribute to add to the body tag
     * @param parameters optional parameters to add to the body tag
     * @return the start html of the body
     */    
    public String bodyStart(String className, String parameters) {
        return pageBody(HTML_START, className, parameters);
    }        
    
    /**
     * Builds the end html of the body.<p>
     * 
     * @return the end html of the body
     */
    public String bodyEnd() {
        return pageBody(HTML_END, null, null);
    }
    
    /**
     * Builds the html of the body.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param className optional class attribute to add to the body tag
     * @param parameters optional parameters to add to the body tag
     * @return the html of the body
     */
    public String pageBody(int segment, String className, String parameters) {
        if (segment == HTML_START) {
            StringBuffer result = new StringBuffer(128);
            result.append("</head>\n<body unselectable=\"on\"");
            if (className != null) {
                result.append(" class=\"");
                result.append(className);
                result.append("\"");
            }
            if (parameters != null) {
                result.append(" ");
                result.append(parameters);
            }
            result.append(">\n");            
            return result.toString();
        } else {
            return "</body>";
        }        
    }  
    
    /**
     * Returns a list of all methods of the current class instance that 
     * start with "getParam" and have no parameters.<p> 
     * 
     * @return a list of all methods of the current class instance that 
     * start with "getParam" and have no parameters
     */
    private List paramGetMethods() {
        List list = new ArrayList();
        Method[] methods = this.getClass().getMethods();
        int length = methods.length;
        for (int i=0; i<length; i++) {
            Method method = methods[i];
            if (method.getName().startsWith("getParam") && (method.getParameterTypes().length == 0)) {
                if (DEBUG) System.err.println("getMethod: " + method.getName());
                list.add(method);
            }
        }        
        return list;
    }

    /**
     * Returns a list of all methods of the current class instance that 
     * start with "setParam" and have exactle on String parameter.<p> 
     * 
     * @return a list of all methods of the current class instance that 
     * start with "setParam" and have exactle on String parameter
     */
    private List paramSetMethods() {
        List list = new ArrayList();
        Method[] methods = this.getClass().getMethods();
        int length = methods.length;
        for (int i=0; i<length; i++) {
            Method method = methods[i];
            if (method.getName().startsWith("setParam") 
            && (method.getParameterTypes().length == 1)
            && (method.getParameterTypes()[0].equals(java.lang.String.class))) {
                if (DEBUG) System.err.println("setMethod: " + method.getName());
                list.add(method);
            }
        }        
        return list;
    }
    
    /**
     * Fills all class parameter values from the data provided in the current request.<p>
     *  
     * All methods that start with "setParam" are possible candidates to be
     * automatically filled. The remaining part of the method name is converted
     * to lower case. Then a parameter of this name is searched in the request parameters.
     * If the parameter is found, the "setParam" method is automatically invoked 
     * by reflection with the value of the parameter.<p>
     * 
     * @param request the current JSP request
     */
    public void fillParamValues(HttpServletRequest request)  {
        List methods = paramSetMethods();
        Iterator i = methods.iterator();
        while (i.hasNext()) {
            Method m = (Method)i.next();
            String name = m.getName().substring(8).toLowerCase();
            String value = request.getParameter(name);
            if ("".equals(value)) value = null;
            if (value != null) value = Encoder.decode(value);
            try {
                if (DEBUG && (value != null)) System.err.println("setting " + m.getName() + " with value '" + value + "'");
                m.invoke(this, new Object[] {value});
            } catch (InvocationTargetException ite) {
            } catch (IllegalAccessException eae) {
            }
        }        
    }
    
    /**
     * Returns the values of all parameter methods of this workplace class instance.<p>
     * 
     * @return the values of all parameter methods of this workplace class instance
     */
    private Map paramValues() {
        List methods = paramGetMethods();
        Map map = new HashMap(methods.size());
        Iterator i = methods.iterator();
        while (i.hasNext()) {
            Method m = (Method)i.next();
            Object o = null;
            try {
                o = m.invoke(this, new Object[0]);
            } catch (InvocationTargetException ite) {
            } catch (IllegalAccessException eae) {
            }
            if (o != null) {
                map.put(m.getName().substring(8).toLowerCase(), o);            
            }
        }
        return map;
    }
        
    /**
     * Returns all initialized parameters of the current workplace class in the
     * form of a parameter map, i.e. the values are arrays.<p>
     * 
     * @return all initialized parameters of the current workplace class in the
     * form of a parameter map
     */
    public Map paramsAsParameterMap() {
        Map params = paramValues();
        Map result = new HashMap(params.size());
        Iterator i = params.keySet().iterator();
        while (i.hasNext()) {
            String param = (String)i.next();
            String value = params.get(param).toString();
            result.put(param, new String[] {value});
        }
        return result;
    }  
    
    /**
     * Returns all initialized parameters of the current workplace class 
     * as hidden field tags that can be inserted in a form.<p>
     * 
     * @return all initialized parameters of the current workplace class
     * as hidden field tags that can be inserted in a html form
     */
    public String paramsAsHidden() {
        StringBuffer result = new StringBuffer(512);
        Map params = paramValues();
        Iterator i = params.keySet().iterator();
        while (i.hasNext()) {
            String param = (String)i.next();
            Object value = params.get(param);
            result.append("<input type=\"hidden\" name=\"");
            result.append(param);
            result.append("\" value=\"");
            result.append(Encoder.encode(value.toString()));
            result.append("\">\n");
        }        
        return result.toString();
    }
    
    /**
     * Returns all initialized parameters of the current workplace class 
     * as request parameters, i.e. in the form <code>key1=value1&key2=value2</code> etc.
     * 
     * @return all initialized parameters of the current workplace class 
     * as request parameters
     */
    public String paramsAsRequest() {
        StringBuffer result = new StringBuffer(512);
        Map params = paramValues();
        Iterator i = params.keySet().iterator();
        while (i.hasNext()) {
            String param = (String)i.next();
            Object value = params.get(param);
            result.append(param);
            result.append("=");
            result.append(Encoder.encode(value.toString()));
            if (i.hasNext()) result.append("&");
        }        
        return result.toString();
    }    
    
    /**
     * Returns true if the currently processed element is an included sub element.<p>
     * 
     * @return true if the currently processed element is an included sub element
     */
    public boolean isSubElement() {
        return !getJsp().getRequestContext().getUri().equals(getJsp().info("opencms.request.element.uri"));
    }    

    /**
     * Returns the uri (including context path) to the explorer file list.<p>
     * 
     * @return the uri (including context path) to the explorer file list
     */    
    public String getExplorerFileListFullUri() {
        if (m_file_explorer_filelist != null) return m_file_explorer_filelist;
        synchronized (this) {
            m_file_explorer_filelist = LinkSubstitution.getLinkSubstitution(getCms(), C_FILE_EXPLORER_FILELIST);            
        }
        return m_file_explorer_filelist;
    }
    
    /**
     * Returns the empty String "" if the provided value is null, otherwise just returns 
     * the provided value.<p>
     * 
     * Use this method in forms if a getParamXXX method is used, but a String (not null)
     * is required.
     * 
     * @param value the String to check
     * @return the empty String "" if the provided value is null, otherwise just returns 
     * the provided value
     */
    public String nullToEmpty(String value) {
        if (value != null) return value;
        return "";
    }
}
