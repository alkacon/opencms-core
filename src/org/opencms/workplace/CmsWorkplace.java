/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplace.java,v $
 * Date   : $Date: 2004/05/19 16:20:54 $
 * Version: $Revision: 1.74 $
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

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManager;
import org.opencms.util.CmsStringSubstitution;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;

/**
 * Master class for the JSP based workplace which provides default methods and
 * session handling for all JSP workplace classes.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.74 $
 * 
 * @since 5.1
 */
public abstract class CmsWorkplace {
    
    /** Key name for the request attribute to reload the folder tree view */
    protected static final String C_REQUEST_ATTRIBUTE_RELOADTREE = "__CmsWorkplace.RELOADTREE";
    
    /** Key name for the session workplace settings */
    public static final String C_SESSION_WORKPLACE_SETTINGS = "__CmsWorkplace.WORKPLACE_SETTINGS";
    
    /** Key name for the session workplace class */
    protected static final String C_SESSION_WORKPLACE_CLASS    = "__CmsWorkplace.WORKPLACE_CLASS";
    
    /** Constant for the JSP workplace path */
    protected static final String C_PATH_WORKPLACE = I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "jsp/";
    
    /** Constant for the JSP dialogs path */
    protected static final String C_PATH_DIALOGS = C_PATH_WORKPLACE + "dialogs/";
    
    /** Constant for the JSP common files (e.g. error page) path */
    protected static final String C_PATH_DIALOG_COMMON = C_PATH_DIALOGS + "common/";
    
    /** Constant for the JSP explorer filelist file */
    protected static final String C_FILE_EXPLORER_FILELIST = C_PATH_WORKPLACE + "explorer_files.html";
    
    /** Constant for the JSP common wait screen */
    protected static final String C_FILE_DIALOG_SCREEN_WAIT = C_PATH_DIALOG_COMMON + "wait.html";
    
    /** Constant for the JSP common error dialog */
    protected static final String C_FILE_DIALOG_SCREEN_ERROR = C_PATH_DIALOG_COMMON + "error.html";
    
    /** Constant for the JSP common confirmation dialog */
    protected static final String C_FILE_DIALOG_SCREEN_CONFIRM = C_PATH_DIALOG_COMMON + "confirmation.html";
    
    /** Constant for the JSP common report page */
    protected static final String C_FILE_REPORT_OUTPUT = C_PATH_DIALOG_COMMON + "report.html";
    
    /** Constant for the JSP common close dialog page */
    protected static final String C_FILE_DIALOG_CLOSE = C_PATH_DIALOG_COMMON + "closedialog.html";
    
    /** Helper variable to store the id of the current project */
    private int m_currentProjectId = -1;
    
    /** Helper variable to deliver the html start part */
    public static final int HTML_START = 0;

    /** Helper variable to deliver the html end part */
    public static final int HTML_END = 1;
    
    /** The debug flag */
    public static final boolean DEBUG = false;
       
    /** The URI to the skin resources */
    private static String m_skinUri;    

    private static String m_file_explorer_filelist;     
    private CmsJspActionElement m_jsp;
    private CmsObject m_cms;
    private HttpSession m_session;
    private CmsWorkplaceSettings m_settings;
    private String m_resourceUri;
    private List m_multiPartFileItems;
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp the initialized JSP context
     */    
    public CmsWorkplace(CmsJspActionElement jsp) {
        if (jsp != null) {       
            m_jsp = jsp;        
            m_cms = m_jsp.getCmsObject();                
            m_session = m_jsp.getRequest().getSession();
            
            // get / create the workplace settings 
            m_settings = (CmsWorkplaceSettings)m_session.getAttribute(C_SESSION_WORKPLACE_SETTINGS);
            if (m_settings == null) {
                // create the settings object
                m_settings = new CmsWorkplaceSettings();
                initWorkplaceSettings(m_cms, m_settings, false);
                storeSettings(m_session, m_settings);
            }
            
            // check request for changes in the workplace settings
            initWorkplaceRequestValues(m_settings, m_jsp.getRequest());        
            
            // set cms context accordingly
            initWorkplaceCmsContext(m_settings, m_cms);
        }
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
     * Get a localized short key value for the workplace.<p>
     * 
     * @param keyName name of the key
     * @return a localized short key value
     */
    public String shortKey(String keyName) {
        String value = key(keyName + CmsMessages.C_KEY_SHORT_SUFFIX);
        if (value.startsWith(CmsMessages.C_UNKNOWN_KEY_EXTENSION)) {
            // short key value not found, return default key value
            return key(keyName);
        }
        return value;
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
     * @param update flag indicating if settings are only updated (user preferences)
     * @return initialized object with the current users workplace settings 
     */    
    static synchronized CmsWorkplaceSettings initWorkplaceSettings(CmsObject cms, CmsWorkplaceSettings settings, boolean update) {                
        // save current workplace user & user settings object
        CmsUser user;
        if (update) {
            try {
                // read the user from db to avoid side effects in preferences dialog after publishing
                user = cms.readUser(cms.getRequestContext().currentUser().getId());
            } catch (CmsException e) {
                user = cms.getRequestContext().currentUser();
            }
        } else {
            user = cms.getRequestContext().currentUser();
        }
        settings.setUser(user);
        settings.setUserSettings(new CmsUserSettings(user));
        
        // save current project
        settings.setProject(cms.getRequestContext().currentProject().getId());
  
        // initialize messages and also store them in settings
        CmsWorkplaceMessages messages = new CmsWorkplaceMessages(cms, settings.getUserSettings().getLocale());
        settings.setMessages(messages);    
                        
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
        
        // get the default view from the user settings
        settings.setViewUri(OpenCms.getLinkManager().substituteLink(cms, settings.getUserSettings().getStartView()));
        
        // save the visible resource types for the current user
        settings.setResourceTypes(initWorkplaceResourceTypes(cms));
                  
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
                reqCont.setCurrentProject(cms.readProject(settings.getProject()));
            } catch (CmsException e) {
                // do nothing
            }                    
        }
        
        // check site setting
        if (!(settings.getSite().equals(reqCont.getSiteRoot()))) {
            reqCont.setSiteRoot(settings.getSite());
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
     * Returns the current users locale setting.<p>
     * 
     * This is a convenience method that just 
     * executes the following code: 
     * <code>getCms().getRequestContext().getLocale()</code>.<p>
     * 
     * @return the current users locale setting
     */
    public Locale getLocale() {
        return getCms().getRequestContext().getLocale();
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
        if (m_resourceUri == null) {
            m_resourceUri = OpenCms.getSystemInfo().getContextPath() + I_CmsWpConstants.C_SYSTEM_PICS_EXPORT_PATH;          
        }
        return m_resourceUri;
    }
    
    /**
     * Returns the path to the skin resources.<p>
     * 
     * @return the path to the skin resources
     */
    public static String getSkinUri() {
        if (m_skinUri == null) {
            m_skinUri = OpenCms.getSystemInfo().getContextPath() + "/skins/modern/";
        }        
        return m_skinUri;      
    }
    
    /**
     * Generates a html select box out of the provided values.<p>
     * 
     * @param parameters a string that will be inserted into the initial select tag,
     *      if null no parameters will be inserted
     * @param options the options 
     * @param values the option values, if null the select will have no value attributes
     * @param selected the index of the pre-selected option, if -1 no option is pre-selected
     * @return a formatted html String representing a html select box
     */
    public String buildSelect(String parameters, List options, List values, int selected) {
        return buildSelect(parameters, options, values, selected, true);
    }
    
    /**
     * Generates a html select box out of the provided values.<p>
     * 
     * @param parameters a string that will be inserted into the initial select tag,
     *      if null no parameters will be inserted
     * @param options the options 
     * @param values the option values, if null the select will have no value attributes
     * @param selected the index of the pre-selected option, if -1 no option is pre-selected
     * @param useLineFeed if true, adds some formatting "\n" to the output String
     * @return a String representing a html select box
     */
    public String buildSelect(String parameters, List options, List values, int selected, boolean useLineFeed) {
        StringBuffer result = new StringBuffer(1024);
        result.append("<select ");
        if (parameters != null) {
            result.append(parameters);
        }
        result.append(">");
        if (useLineFeed) {
            result.append("\n");
        }
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
                if (i == selected) {
                    result.append(" selected=\"selected\"");
                }
                result.append(">");  
                result.append(options.get(i));
                result.append("</option>");
                if (useLineFeed) {
                    result.append("\n");
                }
            } else {
                result.append("<option value=\"");
                result.append(value);
                result.append("\""); 
                if (i == selected) {
                    result.append(" selected=\"selected\"");
                }
                result.append(">");                
                result.append(options.get(i));
                result.append("</option>");
                if (useLineFeed) {
                    result.append("\n");
                }
            }       
        }        
        result.append("</select>");
        if (useLineFeed) {
            result.append("\n");
        }
        return result.toString();
    }
    
    /**
     * Creates the time in milliseconds from the given parameter.<p>
     * 
     * @param dateString the String representation of the date
     * @param useTime true if the time should be parsed, too, otherwise false
     * @return the time in milliseconds
     * @throws ParseException if something goes wrong
     */
    public long getCalendarDate(String dateString, boolean useTime) throws ParseException {
        long dateLong = 0;
        
        // substitute some chars because calendar syntax != DateFormat syntax
        String dateFormat = key("calendar.dateformat");
        if (useTime) {
            dateFormat += " " + key("calendar.timeformat");
        }
        dateFormat = getCalendarJavaDateFormat(dateFormat);
      
        SimpleDateFormat df = new SimpleDateFormat(dateFormat);       
        dateLong = df.parse(dateString).getTime();    
        return dateLong;
    }
    
    /**
     * Parses the JS calendar date format to the java patterns of SimpleDateFormat.<p>
     * 
     * @param dateFormat the dateformat String of the JS calendar
     * @return the parsed SimpleDateFormat pattern String
     */
    public static String getCalendarJavaDateFormat(String dateFormat) {
        dateFormat = CmsStringSubstitution.substitute(dateFormat, "%", ""); // remove all "%"
        dateFormat = CmsStringSubstitution.substitute(dateFormat, "m", "{$month}");
        dateFormat = CmsStringSubstitution.substitute(dateFormat, "H", "{$hour}");
        dateFormat = dateFormat.toLowerCase();
        dateFormat = CmsStringSubstitution.substitute(dateFormat, "{$month}", "M");
        dateFormat = CmsStringSubstitution.substitute(dateFormat, "{$hour}", "H");
        dateFormat = CmsStringSubstitution.substitute(dateFormat, "m", "mm"); // minutes with two digits
        dateFormat = dateFormat.replace('e', 'd'); // day of month
        dateFormat = dateFormat.replace('i', 'h'); // 12 hour format
        dateFormat = dateFormat.replace('p', 'a'); // pm/am String
        return dateFormat;
    }
    
    /**
     * Displays a javascript calendar element with the standard "opencms" style.<p>
     * 
     * Creates the HTML javascript and stylesheet includes for the head of the page.<p>
     * 
     * @return the necessary HTML code for the js and stylesheet includes
     */
    public String calendarIncludes() {
        return calendarIncludes("opencms");
    }
    
    /**
     * Displays a javascript calendar element.<p>
     * 
     * Creates the HTML javascript and stylesheet includes for the head of the page.<p>
     * 
     * @param style the name of the used calendar style, e.g. "system", "blue"
     * @return the necessary HTML code for the js and stylesheet includes
     */
    public String calendarIncludes(String style) {
        StringBuffer result = new StringBuffer(512);
        String calendarPath = getSkinUri() + "components/js_calendar/";
        if (style == null || "".equals(style)) {
            style = "system";
        }
        result.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + calendarPath + "calendar-" + style + ".css\">\n");
        result.append("<script type=\"text/javascript\" src=\"" + calendarPath + "calendar.js\"></script>\n");
        result.append("<script type=\"text/javascript\" src=\"" + calendarPath + "lang/calendar-" + getLocale().getLanguage() + ".js\"></script>\n");
        result.append("<script type=\"text/javascript\" src=\"" + calendarPath + "calendar-setup.js\"></script>\n");
        return result.toString();
    }
    
    /**
     * Initializes a javascript calendar element to be shown on a page.<p>
     * 
     * This method must be called at the end of a HTML page, e.g. before the closing &lt;body&gt; tag.<p>
     * 
     * @param inputFieldId the ID of the input field where the date is pasted to
     * @param triggerButtonId the ID of the button which triggers the calendar
     * @param align initial position of the calendar popup element
     * @param singleClick if true, a single click selects a date and closes the calendar, otherwise calendar is closed by doubleclick
     * @param weekNumbers show the week numbers in the calendar or not
     * @param mondayFirst show monday as first day of week
     * @param dateStatusFunc name of the function which determines if/how a date should be disabled
     * @return the HTML code to initialize a calendar poup element
     */
    public String calendarInit(String inputFieldId, String triggerButtonId, String align, boolean singleClick, boolean weekNumbers, boolean mondayFirst, String dateStatusFunc) {
        return calendarInit(inputFieldId, triggerButtonId, align, singleClick, weekNumbers, mondayFirst, dateStatusFunc, false);
    }
    
    /**
     * Initializes a javascript calendar element to be shown on a page.<p>
     * 
     * This method must be called at the end of a HTML page, e.g. before the closing &lt;body&gt; tag.<p>
     * 
     * @param inputFieldId the ID of the input field where the date is pasted to
     * @param triggerButtonId the ID of the button which triggers the calendar
     * @param align initial position of the calendar popup element
     * @param singleClick if true, a single click selects a date and closes the calendar, otherwise calendar is closed by doubleclick
     * @param weekNumbers show the week numbers in the calendar or not
     * @param mondayFirst show monday as first day of week
     * @param dateStatusFunc name of the function which determines if/how a date should be disabled
     * @param showTime true if the time selector should be shown, otherwise false
     * @return the HTML code to initialize a calendar poup element
     */
    public String calendarInit(String inputFieldId, String triggerButtonId, String align, boolean singleClick, boolean weekNumbers, boolean mondayFirst, String dateStatusFunc, boolean showTime) {
        StringBuffer result = new StringBuffer(512);
        if (align == null || "".equals(align)) {
            align = "Bc";
        }
        result.append("<script type=\"text/javascript\">\n");
        result.append("<!--\n");
        result.append("\tCalendar.setup({\n");
        result.append("\t\tinputField     :    \"" + inputFieldId + "\",\n");
        result.append("\t\tifFormat       :    \"" + key("calendar.dateformat"));
        if (showTime) {
            result.append(" " + key("calendar.timeformat"));
        }
        result.append("\",\n");        
        result.append("\t\tbutton         :    \"" + triggerButtonId + "\",\n");
        result.append("\t\talign          :    \"" + align + "\",\n");
        result.append("\t\tsingleClick    :    " + singleClick + ",\n");
        result.append("\t\tweekNumbers    :    " + weekNumbers + ",\n");
        result.append("\t\tmondayFirst    :    " + mondayFirst + ",\n");
        result.append("\t\tshowsTime      :    " + showTime);
        if (showTime && key("calendar.timeformat").toLowerCase().indexOf("p") != -1) {
            result.append(",\n\t\ttimeFormat     :    \"12\"");
        }
        if (dateStatusFunc != null && !"".equals(dateStatusFunc)) {
            result.append(",\n\t\tdateStatusFunc :    " + dateStatusFunc);
        }
        result.append("\n\t});\n");

        result.append("//-->\n");
        result.append("</script>\n");
        return result.toString();
    }
    
    /**
     * Returns the html for the frame name and source and stores this information in the workplace settings.<p>
     * 
     * @param frameName the name of the frame
     * @param uri the absolute path of the frame
     * @return the html for the frame name and source
     */
    public String getFrameSource(String frameName, String uri) {
        String frameString = "name=\"" + frameName + "\" src=\"" + uri + "\"";
        int paramIndex = uri.indexOf("?");
        if (paramIndex != -1) {
            // remove request parameters from URI before putting it to Map
            uri = uri.substring(0, uri.indexOf("?"));
        }
        getSettings().getFrameUris().put(frameName, uri);
        return frameString;
    }
    
    /**
     * Checks the lock state of the resource and locks it if the autolock feature is enabled.<p>
     * 
     * @param resource the resource name which is checked
     * @throws CmsException if reading or locking the resource fails
     */
    public void checkLock(String resource) throws CmsException {
        checkLock(resource, org.opencms.lock.CmsLock.C_MODE_COMMON);
    }
    
    /**
     * Checks the lock state of the resource and locks it if the autolock feature is enabled.<p>
     * 
     * @param resource the resource name which is checked
     * @param mode flag indicating the mode (temporary or common) of a lock
     * @throws CmsException if reading or locking the resource fails
     */
    public void checkLock(String resource, int mode) throws CmsException {
        if (OpenCms.getWorkplaceManager().autoLockResources()) {
            // Autolock is enabled, check the lock state of the resource
            CmsResource res = getCms().readFileHeader(resource, CmsResourceFilter.ALL);
            if (getCms().getLock(res).isNullLock()) {
                // resource is not locked, lock it automatically
                getCms().lockResource(resource, false, mode);
            }           
        }
    }
    
    /**
     * Initializes a Map with all visible resource types for the current user.<p>
     * 
     * @param cms the CmsObject
     * @return all visible resource types in a map with the resource type id as key value
     */
    private static Map initWorkplaceResourceTypes(CmsObject cms) {
        List allResTypes = cms.getAllResourceTypes();
        Map resourceTypes = new HashMap();
        Iterator i = allResTypes.iterator();
        while (i.hasNext()) {
            // loop through all types and check which types can be displayed for the user
            I_CmsResourceType curType = (I_CmsResourceType)i.next();
            try {
                cms.readFileHeader(I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "restypes/" + curType.getResourceTypeName());
                resourceTypes.put(new Integer(curType.getResourceType()), curType);               
            } catch (CmsException e) {
                // ignore
            }
        } 
        return resourceTypes;      
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
        return pageHtmlStyle(segment, title, null);
    }
    
    /**
     * Returns the default html for a workplace page, including setting of DOCTYPE and 
     * inserting a header with the content-type, allowing the selection of an individual style sheet.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param title the title of the page, if null no title tag is inserted
     * @param stylesheet the used style sheet, usually "files/css_workplace.css"
     * @return the default html for a workplace page
     */
    public String pageHtmlStyle(int segment, String title, String stylesheet) {
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
            if (stylesheet != null) {
                result.append(stylesheet);
            } else {
                result.append("files/css_workplace.css"); 
            }
            result.append("\">\n");
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
            if (getSettings().isViewAdministration()) {
                if (className == null || "dialog".equals(className)) {
                    className = "dialogadmin";
                }
                if (parameters == null) {               
                    result.append(" onLoad=\"window.top.body.admin_head.location.href='" + getJsp().link(I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "action/administration_head.html") + "';\"");
                }
            }
            if (className != null) {
                result.append(" class=\"");
                result.append(className);
                result.append("\"");
            }
            if (parameters != null && !"".equals(parameters)) {
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
                if (DEBUG) {
                    System.err.println("getMethod: " + method.getName());
                }
                list.add(method);
            }
        }        
        return list;
    }

    /**
     * Returns a list of all methods of the current class instance that 
     * start with "setParam" and have exactly one String parameter.<p> 
     * 
     * @return a list of all methods of the current class instance that 
     * start with "setParam" and have exactly one String parameter
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
                if (DEBUG) {
                    System.err.println("setMethod: " + method.getName());
                }
                list.add(method);
            }
        }        
        return list;
    }
    
    /**
     * Decodes an individual parameter value.<p>
     * 
     * In special cases some parameters might require a different-from-default
     * encoding. This is the case if the content of the parameter was 
     * encoded using the JavaScript encodeURIComponent() method on the client,
     * which always encodes in UTF-8.<p> 
     * 
     * @param paramName the name of the parameter 
     * @param paramValue the unencoded value of the parameter
     * @return the encoded value of the parameter
     */
    protected String fillParamValuesDecode(String paramName, String paramValue) {
        if ((paramName != null) && (paramValue != null)) {
            return CmsEncoder.decode(paramValue, getCms().getRequestContext().getEncoding());
        } else {
            return null;
        }
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
        boolean isMultiPart = FileUploadBase.isMultipartContent(request);
        Map parameterMap = null;
        if (isMultiPart) {
            // this is a multipart request, create a map with all non-file parameters
            parameterMap = createParameterMapFromMultiPart(request);
        }
        List methods = paramSetMethods();
        Iterator i = methods.iterator();
        while (i.hasNext()) {
            Method m = (Method)i.next();
            String name = m.getName().substring(8).toLowerCase();
            String value = null;
            if (isMultiPart) {
                // get the parameter value from the map
                value = (String)parameterMap.get(name);
            } else {
                // get the parameter value from the request
                value = request.getParameter(name);
            }
            if ("".equals(value)) {
                value = null;
            }
            value = fillParamValuesDecode(name, value);
            try {
                if (DEBUG && (value != null)) {
                    System.err.println("setting " + m.getName() + " with value '" + value + "'");
                }
                m.invoke(this, new Object[] {value});
            } catch (InvocationTargetException ite) {
                // ignore
            } catch (IllegalAccessException eae) {
                // ignore
            }
        }        
    }
    
    
    
    /**
     * Fills the request parameters in a map if treating a multipart request.<p>
     * 
     * The created map has the parameter names as key.<p>
     * 
     * @param request the current HTTP servlet request
     * @return a map containing all non-file request parameters
     */
    protected Map createParameterMapFromMultiPart(HttpServletRequest request) {
        Map parameterMap = new HashMap();
        DiskFileUpload fu = new DiskFileUpload();
        // maximum size that will be stored in memory
        fu.setSizeThreshold(4096);
        // the location for saving data that is larger than getSizeThreshold()
        fu.setRepositoryPath(OpenCms.getSystemInfo().getWebInfRfsPath());
        try {
            setMultiPartFileItems(fu.parseRequest(request));
            Iterator i = getMultiPartFileItems().iterator();
            while (i.hasNext()) {
                FileItem item = (FileItem)i.next();
                String name = item.getFieldName();
                String value = null;
                if (name != null && item.getName() == null) {
                    // only put to map if current item is no file and not null
                    try {
                        value = item.getString(CmsEncoder.C_UTF8_ENCODING);
                    } catch (UnsupportedEncodingException e) {
                        value = item.getString();
                    }
                    parameterMap.put(name, value);
                }
            }
        } catch (FileUploadException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error parsing multipart request in workplace");
            }      
        }
        return parameterMap;
    }
    
    /**
     * Returns the values of all parameter methods of this workplace class instance.<p>
     * 
     * @return the values of all parameter methods of this workplace class instance
     */
    protected Map paramValues() {
        List methods = paramGetMethods();
        Map map = new HashMap(methods.size());
        Iterator i = methods.iterator();
        while (i.hasNext()) {
            Method m = (Method)i.next();
            Object o = null;
            try {
                o = m.invoke(this, new Object[0]);
            } catch (InvocationTargetException ite) {
                // ignore
            } catch (IllegalAccessException eae) {
                // ignore
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
            String encoded = CmsEncoder.encode(value.toString(), getCms().getRequestContext().getEncoding()); 
            result.append(encoded);
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
            result.append(CmsEncoder.encode(value.toString(), getCms().getRequestContext().getEncoding()));
            if (i.hasNext()) {
                result.append("&");
            }
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
        if (m_file_explorer_filelist != null) {
            return m_file_explorer_filelist;
        }
        synchronized (this) {
            m_file_explorer_filelist = OpenCms.getLinkManager().substituteLink(getCms(), C_FILE_EXPLORER_FILELIST);            
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
        if (value != null) {
            return value;
        }
        return "";
    }
    
    /**
     * Helper method to change back from the temporary project to the current project.<p>
     * 
     * @throws CmsException if switching back fails
     */
    protected void switchToCurrentProject() throws CmsException {
        if (m_currentProjectId != -1) {
            // switch back to the current users project
            getCms().getRequestContext().setCurrentProject(getCms().readProject(m_currentProjectId)); 
        }
    }
    
    /**
     * Helper method to change the current project to the temporary file project.<p>
     * 
     * The id of the old project is stored in a member variable to switch back.<p>
     * 
     * @return the id of the tempfileproject
     * @throws CmsException if getting the tempfileproject id fails
     */
    protected int switchToTempProject() throws CmsException {
        // store the current project id in member variable
        m_currentProjectId = getSettings().getProject();
        int tempProjectId = OpenCms.getWorkplaceManager().getTempFileProjectId();
        getCms().getRequestContext().setCurrentProject(getCms().readProject(tempProjectId));
        return tempProjectId;
    }
    
    /**
     * Generates a button for the OpenCms workplace.<p>
     * 
     * @param href the href link for the button, if none is given the button will be disabled
     * @param target the href link target for the button, if none is given the target will be same window
     * @param image the image name for the button, skin path will be automattically added as prefix
     * @param label the label for the text of the button 
     * @param type 0: image only (default), 1: image and text, 2: text only
     * 
     * @return a button for the OpenCms workplace
     */
    public String button(String href, String target, String image, String label, int type) {
        return button(href, target, image, label, type, getSkinUri() +  "buttons/");
    }
    
    /**
     * Generates a button for the OpenCms workplace.<p>
     * 
     * @param href the href link for the button, if none is given the button will be disabled
     * @param target the href link target for the button, if none is given the target will be same window
     * @param image the image name for the button, skin path will be automattically added as prefix
     * @param label the label for the text of the button 
     * @param type 0: image only (default), 1: image and text, 2: text only
     * @param imagePath the path to the image 
     * 
     * @return a button for the OpenCms workplace
     */
    public String button(String href, String target, String image, String label, int type, String imagePath) {
        StringBuffer result = new StringBuffer(512);
        
        String anchorStart =  "<a href=\"";
        if (href != null && href.toLowerCase().startsWith("javascript:")) {
            anchorStart = "<a href=\"#\" onclick=\"";
        }
    
        result.append("<td>");      
        switch (type) {     
            case 1:
            // image and text
                if (href != null) {
                    result.append(anchorStart);
                    result.append(href);
                    result.append("\" class=\"button\"");
                    if (target != null) {
                        result.append(" target=\"");
                        result.append(target);
                        result.append("\"");
                    }
                    result.append(">");
                }           
                result.append("<span unselectable=\"on\" ");
                if (href != null) {
                    result.append("class=\"norm\" onmouseover=\"className='over'\" onmouseout=\"className='norm'\" onmousedown=\"className='push'\" onmouseup=\"className='over'\"");
                } else {
                    result.append("class=\"disabled\"");
                }
                result.append("><span unselectable=\"on\" class=\"combobutton\" ");
                result.append("style=\"background-image: url('");
                result.append(imagePath);
                result.append(image);
                result.append(".gif");
                result.append("');\">");
                result.append(shortKey(label));
                result.append("</span></span>");
                if (href != null) {
                    result.append("</a>");
                }
                break;
            
            case 2:
            // text only
                if (href != null) {
                    result.append(anchorStart);
                    result.append(href);
                    result.append("\" class=\"button\"");
                    if (target != null) {
                        result.append(" target=\"");
                        result.append(target);
                        result.append("\"");
                    }
                    result.append(">");
                }           
                result.append("<span unselectable=\"on\" ");
                if (href != null) {
                    result.append("class=\"norm\" onmouseover=\"className='over'\" onmouseout=\"className='norm'\" onmousedown=\"className='push'\" onmouseup=\"className='over'\"");
                } else {
                    result.append("class=\"disabled\"");
                }
                result.append("><span unselectable=\"on\" class=\"txtbutton\">");
                result.append(shortKey(label));
                result.append("</span></span>");
                if (href != null) {
                    result.append("</a>");
                }       
                break;          
            
            default: 
            // only image
                if (href != null) {
                    result.append(anchorStart);
                    result.append(href);
                    result.append("\" class=\"button\"");
                    if (target != null) {
                        result.append(" target=\"");
                        result.append(target);
                        result.append("\"");
                    }
                    result.append(" title=\"");
                    result.append(key(label));
                    result.append("\">");
                }           
                result.append("<span unselectable=\"on\" ");
                if (href != null) {
                    result.append("class=\"norm\" onmouseover=\"className='over'\" onmouseout=\"className='norm'\" onmousedown=\"className='push'\" onmouseup=\"className='over'\"");
                } else {
                    result.append("class=\"disabled\"");
                }
                result.append("><img class=\"button\" src=\"");
                result.append(imagePath);
                result.append(image);
                result.append(".gif");
                result.append("\">");
                result.append("</span>");
                if (href != null) {
                    result.append("</a>");
                }       
                break;
        }   
        result.append("</td>\n");
        return result.toString();   
    }
    
    /**
     * Generates a horizontal button bar separator line with maximum width.<p>
     * 
     * @return a horizontal button bar separator line
     */
    public String buttonBarHorizontalLine() {
        StringBuffer result = new StringBuffer(256); 
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"maxwidth\">\n");
        result.append("<tr>\n");
        result.append("\t<td class=\"horseparator\" ><img src=\"" + getSkinUri() + "tree/empty.gif\" border=\"0\" width=\"1\" height=\"1\" alt=\"\"></td>\n");
        result.append("</tr>\n");
        result.append("</table>\n");
        return result.toString();
    }
    
    /**
     * Generates a variable button bar separator line.<p>  
     * 
     * @param leftPixel the amount of pixel left to the line
     * @param rightPixel the amount of pixel right to the line
     * @param className the css class name for the formatting
     * 
     * @return  a variable button bar separator line
     */    
    public String buttonBarLine(int leftPixel, int rightPixel, String className) {
        StringBuffer result = new StringBuffer(512);
        if (leftPixel > 0) {
            result.append(buttonBarLineSpacer(leftPixel)); 
        }
        result.append("<td><span class=\"");
        result.append(className);
        result.append("\"></span></td>\n");
        if (rightPixel > 0) {
            result.append(buttonBarLineSpacer(rightPixel)); 
        }
        return result.toString();        
    }
    
    /**
     * Generates a variable button bar separator line spacer.<p>  
     * 
     * @param pixel the amount of pixel space
     * 
     * @return a variable button bar separator line spacer
     */       
    public String buttonBarLineSpacer(int pixel) {
        StringBuffer result = new StringBuffer(128);
        result.append("<td><span class=\"norm\"><span unselectable=\"on\" class=\"txtbutton\" style=\"padding-right: 0px; padding-left: "); 
        result.append(pixel);
        result.append("px;\"></span></span></td>\n");
        return result.toString();        
    }
    
    /**
     * Generates a button bar starter tab.<p>  
     * 
     * @param leftPixel the amount of pixel left to the starter
     * @param rightPixel the amount of pixel right to the starter
     * 
     * @return a button bar starter tab
     */
    public String buttonBarStartTab(int leftPixel, int rightPixel) {
        StringBuffer result = new StringBuffer(512);
        result.append(buttonBarLineSpacer(leftPixel)); 
        result.append("<td><span class=\"starttab\"><span style=\"width:1px; height:1px\"></span></span></td>\n");
        result.append(buttonBarLineSpacer(rightPixel)); 
        return result.toString();          
    }
    
    /**
     * Generates a button bar separator.<p>  
     * 
     * @param leftPixel the amount of pixel left to the separator
     * @param rightPixel the amount of pixel right to the separator
     * 
     * @return a button bar separator
     */
    public String buttonBarSeparator(int leftPixel, int rightPixel) {
        return buttonBarLine(leftPixel, rightPixel, "separator");
    }    
    
    /**
     * Generates a button bar label.<p>
     * 
     * @param label the label to show
     * 
     * @return a button bar label
     */
    public String buttonBarLabel(String label) {
        return buttonBarLabel(label, "norm");
    }  
    
    /**
     * Generates a button bar label.<p>
     * 
     * @param label the label to show
     * @param className the css class name for the formatting
     * 
     * @return a button bar label
     */    
    public String buttonBarLabel(String label, String className) {
        StringBuffer result = new StringBuffer(128); 
        result.append("<td><span class=\"");
        result.append(className);
        result.append("\"><span unselectable=\"on\" class=\"txtbutton\">");
        result.append(key(label));
        result.append("</span></span></td>\n");
        return result.toString();
    }
        
    /**
     * Returns the html for a button bar.<p>
     * 
     * @param segment the HTML segment (START / END)
     * 
     * @return a button bar html start / end segment 
     */
    public String buttonBar(int segment) {
        return buttonBar(segment, null);
    }
    
    /**
     * Returns the html for a button bar.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param attributes optional attributes for the table tag
     * 
     * @return a button bar html start / end segment 
     */
    public String buttonBar(int segment, String attributes) {
        if (segment == HTML_START) {
            String result = "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"";
            if (attributes != null) {
                result += " " + attributes;
            }
            return result + "><tr>\n";
        } else {
            return "</tr></table>";
        }
    }
    
    /**
     * Returns the html for an invisible spacer between button bar contents like buttons, labels, etc.<p>
     * 
     * @param width the width of the invisible spacer
     * @return the html for the invisible spacer
     */
    public String buttonBarSpacer(int width) {
        StringBuffer result = new StringBuffer(128);
        result.append("<td><span class=\"norm\"><span unselectable=\"on\" class=\"txtbutton\" style=\"width: "); 
        result.append(width);
        result.append("px;\"></span></span></td>\n");
        return result.toString();
    }
    
    /**
     * Sends a http redirect to the specified URI in the OpenCms VFS.<p>
     *
     * @param location the location the response is redirected to
     * @throws IOException in case redirection fails
     */
    public void sendCmsRedirect(String location) throws IOException {
        getJsp().getResponse().sendRedirect(OpenCms.getSystemInfo().getOpenCmsContext() + location);
    }        
    /**
     * Returns a list of FileItem instances parsed from the request, in the order that they were transmitted.<p>
     * 
     * This list is automatically initialized from the createParameterMapFromMultiPart(HttpServletRequest) method.<p> 
     * 
     * @return list of FileItem instances parsed from the request, in the order that they were transmitted
     */
    public List getMultiPartFileItems() {
        return m_multiPartFileItems;
    }

    /**
     * Sets a list of FileItem instances parsed from the request, in the order that they were transmitted.<p>
     * 
     * @param fileItems a list of FileItem instances parsed from the request, in the order that they were transmitted
     */
    private void setMultiPartFileItems(List fileItems) {
        m_multiPartFileItems = fileItems;
    }

}
