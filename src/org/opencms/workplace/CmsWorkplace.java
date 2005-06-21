/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplace.java,v $
 * Date   : $Date: 2005/06/21 15:50:00 $
 * Version: $Revision: 1.131 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManager;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsTree;
import org.opencms.workplace.help.CmsHelpTemplateBean;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Master class for the JSP based workplace which provides default methods and
 * session handling for all JSP workplace classes.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.131 $
 * 
 * @since 5.1
 */
public abstract class CmsWorkplace {

    /** Constant for the JSP explorer filelist file. */
    public static final String C_FILE_EXPLORER_FILELIST = I_CmsWpConstants.C_VFS_PATH_WORKPLACE
        + "views/explorer/explorer_files.jsp";

    /** Constant for the JSP dialogs path. */
    public static final String C_PATH_DIALOGS = I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "commons/";

    /** Constant for the JSP workplace path. */
    public static final String C_PATH_WORKPLACE = I_CmsWpConstants.C_VFS_PATH_WORKPLACE;

    /** The debug flag. */
    public static final boolean DEBUG = false;

    /** Helper variable to deliver the html end part. */
    public static final int HTML_END = 1;

    /** Helper variable to deliver the html start part. */
    public static final int HTML_START = 0;

    /** Constant for the JSP common files (e.g. error page) path. */
    protected static final String C_DIALOG_PATH_COMMON = C_PATH_DIALOGS + "includes/";

    /** Constant for the JSP common close dialog page. */
    protected static final String C_FILE_DIALOG_CLOSE = C_DIALOG_PATH_COMMON + "closedialog.jsp";

    /** Constant for the JSP common confirmation dialog. */
    protected static final String C_FILE_DIALOG_SCREEN_CONFIRM = C_DIALOG_PATH_COMMON + "confirmation.jsp";

    /** Constant for the JSP common error dialog. */
    protected static final String C_FILE_DIALOG_SCREEN_ERROR = C_DIALOG_PATH_COMMON + "error.jsp";

    /** Constant for the JSP common error dialog. */
    protected static final String C_FILE_DIALOG_SCREEN_ERRORPAGE = C_DIALOG_PATH_COMMON + "errorpage.jsp";

    /** Constant for the JSP common wait screen. */
    protected static final String C_FILE_DIALOG_SCREEN_WAIT = C_DIALOG_PATH_COMMON + "wait.jsp";

    /** Constant for the JSP common report page. */
    protected static final String C_FILE_REPORT_OUTPUT = C_DIALOG_PATH_COMMON + "report.jsp";

    /** Key name for the request attribute to reload the folder tree view. */
    protected static final String C_REQUEST_ATTRIBUTE_RELOADTREE = "__CmsWorkplace.RELOADTREE";

    /** Key name for the session workplace class. */
    protected static final String C_SESSION_WORKPLACE_CLASS = "__CmsWorkplace.WORKPLACE_CLASS";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsWorkplace.class);

    /** The link to the explorer file list (cached for performance reasons). */
    private static String m_file_explorer_filelist;

    /** The URI to the skin resources (cached for performance reasons). */
    private static String m_skinUri;

    /** The URI to the stylesheet resources (cached for performance reasons). */
    private static String m_styleUri;

    /** 
     * Temporary variable for easily adding new resource bundles.<p>    
     *   
     * @see #initMessages()
     * @see #addMessages(String)
     */
    private List m_bundles = new ArrayList();

    /** The current users OpenCms context. */
    private CmsObject m_cms;

    /** Helper variable to store the id of the current project. */
    private int m_currentProjectId = -1;

    /** The current JSP action element. */
    private CmsJspActionElement m_jsp;

    /** The macro resolver, this is cached to avoid multiple instance generation. */
    private CmsMacroResolver m_macroResolver;

    /** 
     * The current used message bundle. 
     * @see #initMessages()
     */
    private CmsMessages m_messages;

    /** The list of multi part file items (if available). */
    private List m_multiPartFileItems;

    /** The map of parameters read from the current request. */
    private Map m_parameterMap;

    /** The current resource URI. */
    private String m_resourceUri;

    /** The current OpenCms users http session. */
    private HttpSession m_session;

    /** The current OpenCms users workplace settings. */
    private CmsWorkplaceSettings m_settings;

    /**
     * Public constructor.<p>
     * 
     * @param jsp the initialized JSP context
     */
    public CmsWorkplace(CmsJspActionElement jsp) {

        initWorkplaceMembers(jsp);
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
    public static String buildSelect(String parameters, List options, List values, int selected, boolean useLineFeed) {

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
        for (int i = 0; i < length; i++) {
            if (values != null) {
                try {
                    value = (String)values.get(i);
                } catch (Exception e) {
                    // can usually be ignored
                    if (LOG.isInfoEnabled()) {
                        LOG.info(e.getLocalizedMessage());
                    }
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
     * Parses the JS calendar date format to the java patterns of SimpleDateFormat.<p>
     * 
     * @param dateFormat the dateformat String of the JS calendar
     * @return the parsed SimpleDateFormat pattern String
     */
    public static String getCalendarJavaDateFormat(String dateFormat) {

        dateFormat = CmsStringUtil.substitute(dateFormat, "%", ""); // remove all "%"
        dateFormat = CmsStringUtil.substitute(dateFormat, "m", "${month}");
        dateFormat = CmsStringUtil.substitute(dateFormat, "H", "${hour}");
        dateFormat = CmsStringUtil.substitute(dateFormat, "Y", "${4anno}");
        dateFormat = dateFormat.toLowerCase();
        dateFormat = CmsStringUtil.substitute(dateFormat, "${month}", "M");
        dateFormat = CmsStringUtil.substitute(dateFormat, "${hour}", "H");
        dateFormat = CmsStringUtil.substitute(dateFormat, "y", "yy");
        dateFormat = CmsStringUtil.substitute(dateFormat, "${4anno}", "yyyy");
        dateFormat = CmsStringUtil.substitute(dateFormat, "m", "mm"); // minutes with two digits
        dateFormat = dateFormat.replace('e', 'd'); // day of month
        dateFormat = dateFormat.replace('i', 'h'); // 12 hour format
        dateFormat = dateFormat.replace('p', 'a'); // pm/am String
        return dateFormat;
    }

    /**
     * Returns the full Workplace resource path to the selected resource.<p>
     * 
     * @param resourceName the name of the resource to get the resource path for
     * 
     * @return the full Workplace resource path to the selected resource
     */
    public static String getResourceUri(String resourceName) {

        StringBuffer result = new StringBuffer(256);
        result.append(getSkinUri());
        result.append(resourceName);
        return result.toString();
    }

    /**
     * Returns the path to the skin resources.<p>
     * 
     * @return the path to the skin resources
     */
    public static String getSkinUri() {

        if (m_skinUri == null) {
            m_skinUri = OpenCms.getSystemInfo().getContextPath() + "/resources/";
        }
        return m_skinUri;
    }

    /**
     * Returns the path to the cascading stylesheets.<p>
     * 
     * @param jsp the JSP context
     * @return the path to the cascading stylesheets
     */
    public static String getStyleUri(CmsJspActionElement jsp) {

        if (m_styleUri == null) {

            CmsProject project = jsp.getCmsObject().getRequestContext().currentProject();
            try {
                jsp.getCmsObject().getRequestContext().setCurrentProject(
                    jsp.getCmsObject().readProject(I_CmsConstants.C_PROJECT_ONLINE_ID));
                m_styleUri = jsp.link("/system/workplace/commons/style/");
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage());
            } finally {
                jsp.getCmsObject().getRequestContext().setCurrentProject(project);
            }
        }
        return m_styleUri;
    }

    /**
     * Returns the path to the cascading stylesheets.<p>
     * 
     * @param jsp the JSP context
     * @param filename the name of the stylesheet
     * @return the path to the cascading stylesheets
     */
    public static String getStyleUri(CmsJspActionElement jsp, String filename) {

        if (m_styleUri == null) {

            CmsProject project = jsp.getCmsObject().getRequestContext().currentProject();
            try {
                jsp.getCmsObject().getRequestContext().setCurrentProject(
                    jsp.getCmsObject().readProject(I_CmsConstants.C_PROJECT_ONLINE_ID));
                m_styleUri = jsp.link("/system/workplace/commons/style/");
            } catch (CmsException e) {
                // ins log schreiben
            } finally {
                jsp.getCmsObject().getRequestContext().setCurrentProject(project);
            }
        }
        return m_styleUri + filename;
    }

    /**
     * Updates the user settings in the given workplace settings for the current user, reading the user settings
     * from the database if required.<p>
     * 
     * @param cms the cms object for the current user
     * @param settings the workplace settings to update (if <code>null</code> a new instance is created)
     * @param update flag indicating if settings are only updated (user preferences)
     * 
     * @return the current users workplace settings
     * 
     * @see #initWorkplaceSettings(CmsObject, CmsWorkplaceSettings, boolean)
     */
    public static CmsWorkplaceSettings initUserSettings(CmsObject cms, CmsWorkplaceSettings settings, boolean update) {

        if (settings == null) {
            settings = new CmsWorkplaceSettings();
        }

        // save current workplace user & user settings object
        CmsUser user;
        if (update) {
            try {
                // read the user from db to get the latest user information if required
                user = cms.readUser(cms.getRequestContext().currentUser().getId());
            } catch (CmsException e) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
                user = cms.getRequestContext().currentUser();
            }
        } else {
            user = cms.getRequestContext().currentUser();
        }
        // store the user and it's settings in the Workplace settings
        settings.setUser(user);
        settings.setUserSettings(new CmsUserSettings(user));

        // return the result settings
        return settings;
    }

    /**
     * Updates the given workplace settings, also re-initializing
     * the state of the Workplace to the users preferences (for example setting the startup site and project).
     * 
     * The user settings will also be updated by calling <code>{@link #initUserSettings(CmsObject, CmsWorkplaceSettings, boolean)}</code>
     * before updating the workplace project, selected site etc.<p>
     * 
     * @param cms the cms object for the current user
     * @param settings the workplace settings to update (if <code>null</code> a new instance is created)
     * @param update flag indicating if settings are only updated (user preferences)
     * 
     * @return the current users initialized workplace settings
     * 
     * @see #initUserSettings(CmsObject, CmsWorkplaceSettings, boolean) 
     */
    public static synchronized CmsWorkplaceSettings initWorkplaceSettings(
        CmsObject cms,
        CmsWorkplaceSettings settings,
        boolean update) {

        // init the workplace user settings 
        settings = initUserSettings(cms, settings, update);

        // save current project
        settings.setProject(cms.getRequestContext().currentProject().getId());

        // switch to users preferred site      
        String siteRoot = settings.getUserSettings().getStartSite();
        if (siteRoot.endsWith("/")) {
            // remove trailing slash
            siteRoot = siteRoot.substring(0, siteRoot.length() - 1);
        }
        if (CmsStringUtil.isNotEmpty(siteRoot) && (CmsSiteManager.getSite(siteRoot) == null)) {
            // this is not the root site and the site is not in the list
            siteRoot = OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartSite();
            if (siteRoot.endsWith("/")) {
                // remove trailing slash
                siteRoot = siteRoot.substring(0, siteRoot.length() - 1);
            }
        }
        boolean access = false;
        CmsResource res = null;
        try {
            // check access to the site
            res = cms.readResource("/");
            access = cms.hasPermissions(res, CmsPermissionSet.ACCESS_VIEW);
        } catch (CmsException e) {
            // error reading site root, in this case we will use a readable default
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage(), e);
            }

        }
        if ((res == null) || !access) {
            List sites = CmsSiteManager.getAvailableSites(cms, true);
            if (sites.size() > 0) {
                siteRoot = ((CmsSite)sites.get(0)).getSiteRoot();
                cms.getRequestContext().setSiteRoot(siteRoot);
            }
        }
        // set the current site
        settings.setSite(siteRoot);

        // set the preferred folder to display
        settings.setExplorerResource(settings.getUserSettings().getStartFolder());

        // get the default view from the user settings
        settings.setViewUri(OpenCms.getLinkManager().substituteLink(cms, settings.getUserSettings().getStartView()));

        // save the editable resource types for the current user
        settings.setResourceTypes(initWorkplaceResourceTypes(cms));

        return settings;
    }

    /**
     * Stores the settings in the given session.<p>
     * 
     * @param session the session to store the settings in
     * @param settings the settings
     */
    static synchronized void storeSettings(HttpSession session, CmsWorkplaceSettings settings) {

        // save the workplace settings in the session
        session.setAttribute(CmsWorkplaceManager.C_SESSION_WORKPLACE_SETTINGS, settings);
    }

    /**
     * Initializes a Map with all editable resource types for the current user.<p>
     * 
     * @param cms the CmsObject
     * @return all editable resource types in a map with the resource type id as key value
     */
    private static Map initWorkplaceResourceTypes(CmsObject cms) {

        Map resourceTypes = new HashMap();
        List allResTypes = OpenCms.getResourceManager().getResourceTypes();
        for (int i = 0; i < allResTypes.size(); i++) {
            // loop through all types and check which types can be displayed and edited for the user
            I_CmsResourceType type = (I_CmsResourceType)allResTypes.get(i);
            // get the settings for the resource type
            CmsExplorerTypeSettings typeSettings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                type.getTypeName());
            if (typeSettings != null) {
                // determine if this resource type is editable for the current user
                CmsPermissionSet permissions;
                try {
                    // get permissions of the current user
                    permissions = typeSettings.getAccess().getAccessControlList().getPermissions(
                        cms.getRequestContext().currentUser(),
                        cms.getGroupsOfUser(cms.getRequestContext().currentUser().getName()));
                } catch (CmsException e) {
                    // error reading the groups of the current user
                    permissions = typeSettings.getAccess().getAccessControlList().getPermissions(
                        cms.getRequestContext().currentUser());
                    if (LOG.isWarnEnabled()) {
                        CmsLog.getLog(CmsTree.class).warn(
                            org.opencms.workplace.explorer.Messages.get().key(
                                org.opencms.workplace.explorer.Messages.LOG_READ_GROUPS_OF_USER_FAILED_1,
                                cms.getRequestContext().currentUser().getName()),
                            e);
                    }
                }
                if (permissions.getPermissionString().indexOf("+w") != -1) {
                    // user is allowed to edit this resource type
                    resourceTypes.put(new Integer(type.getTypeId()), type);
                }
            }
        }
        return resourceTypes;
    }

    /**
     * Returns all parameters of the current workplace class 
     * as hidden field tags that can be inserted in a form.<p>
     * 
     * @return all parameters of the current workplace class
     * as hidden field tags that can be inserted in a html form
     */
    public String allParamsAsHidden() {

        StringBuffer result = new StringBuffer(512);
        Map params = allParamValues();
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
     * Returns all present request parameters as String.<p>
     * 
     * The String is formatted as a parameter String ("param1=val1&param2=val2") with UTF-8 encoded values.<p>
     * 
     * @return all present request parameters as String
     */
    public String allParamsAsRequest() {

        StringBuffer retValue = new StringBuffer(512);
        HttpServletRequest request = getJsp().getRequest();
        Iterator paramNames = request.getParameterMap().keySet().iterator();
        while (paramNames.hasNext()) {
            String paramName = (String)paramNames.next();
            String paramValue = request.getParameter(paramName);
            retValue.append(paramName + "=" + CmsEncoder.encode(paramValue, getCms().getRequestContext().getEncoding()));
            if (paramNames.hasNext()) {
                retValue.append("&");
            }
        }
        return retValue.toString();
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

        return button(href, target, image, label, type, getSkinUri() + "buttons/");
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

        StringBuffer result = new StringBuffer(256);

        String anchorStart = "<a href=\"";
        if (href != null && href.toLowerCase().startsWith("javascript:")) {
            anchorStart = "<a href=\"#\" onclick=\"";
        }

        result.append("<td style=\"vertical-align: top;\">");
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
                if (image != null && image.indexOf('.') == -1) {
                    // append default suffix for button images
                    result.append(".png");
                }
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
                if (image != null && image.indexOf('.') == -1) {
                    // append default suffix for button images
                    result.append(".png");
                }
                result.append("\" alt=\"");
                result.append(key(label));
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
     * Generates a horizontal button bar separator line with maximum width.<p>
     * 
     * @return a horizontal button bar separator line
     */
    public String buttonBarHorizontalLine() {

        StringBuffer result = new StringBuffer(256);
        result.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"maxwidth\">\n");
        result.append("<tr>\n");
        result.append("\t<td class=\"horseparator\" ><img src=\"");
        result.append(getSkinUri());
        result.append("tree/empty.gif\" border=\"0\" width=\"1\" height=\"1\" alt=\"\"></td>\n");
        result.append("</tr>\n");
        result.append("</table>\n");
        return result.toString();
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
        result.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        result.append(calendarPath);
        result.append("calendar-");
        result.append(style);
        result.append(".css\">\n");
        result.append("<script type=\"text/javascript\" src=\"");
        result.append(calendarPath);
        result.append("calendar.js\"></script>\n");
        result.append("<script type=\"text/javascript\" src=\"");
        result.append(calendarPath);
        result.append("lang/calendar-");
        result.append(getLocale().getLanguage());
        result.append(".js\"></script>\n");
        result.append("<script type=\"text/javascript\" src=\"");
        result.append(calendarPath);
        result.append("calendar-setup.js\"></script>\n");
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
    public String calendarInit(
        String inputFieldId,
        String triggerButtonId,
        String align,
        boolean singleClick,
        boolean weekNumbers,
        boolean mondayFirst,
        String dateStatusFunc) {

        return calendarInit(
            inputFieldId,
            triggerButtonId,
            align,
            singleClick,
            weekNumbers,
            mondayFirst,
            dateStatusFunc,
            false);
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
    public String calendarInit(
        String inputFieldId,
        String triggerButtonId,
        String align,
        boolean singleClick,
        boolean weekNumbers,
        boolean mondayFirst,
        String dateStatusFunc,
        boolean showTime) {

        StringBuffer result = new StringBuffer(512);
        if (align == null || "".equals(align)) {
            align = "Bc";
        }
        result.append("<script type=\"text/javascript\">\n");
        result.append("<!--\n");
        result.append("\tCalendar.setup({\n");
        result.append("\t\tinputField     :    \"");
        result.append(inputFieldId);
        result.append("\",\n");
        result.append("\t\tifFormat       :    \"");
        result.append(key(Messages.GUI_CALENDAR_DATE_FORMAT_0));
        if (showTime) {
            result.append(" ");
            result.append(key(Messages.GUI_CALENDAR_TIME_FORMAT_0));
        }
        result.append("\",\n");
        result.append("\t\tbutton         :    \"");
        result.append(triggerButtonId);
        result.append("\",\n");
        result.append("\t\talign          :    \"");
        result.append(align);
        result.append("\",\n");
        result.append("\t\tsingleClick    :    ");
        result.append(singleClick);
        result.append(",\n");
        result.append("\t\tweekNumbers    :    ");
        result.append(weekNumbers);
        result.append(",\n");
        result.append("\t\tmondayFirst    :    ");
        result.append(mondayFirst);
        result.append(",\n");
        result.append("\t\tshowsTime      :    " + showTime);
        if (showTime && key("calendar.timeformat").toLowerCase().indexOf("p") != -1) {
            result.append(",\n\t\ttimeFormat     :    \"12\"");
        }
        if (dateStatusFunc != null && !"".equals(dateStatusFunc)) {
            result.append(",\n\t\tdateStatusFunc :    ");
            result.append(dateStatusFunc);
        }
        result.append("\n\t});\n");

        result.append("//-->\n");
        result.append("</script>\n");
        return result.toString();
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
            CmsResource res = getCms().readResource(resource, CmsResourceFilter.ALL);
            if (getCms().getLock(res).isNullLock()) {
                // resource is not locked, lock it automatically
                getCms().lockResource(resource, mode);
            }
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
    public void fillParamValues(HttpServletRequest request) {

        // check if this is a multipart request 
        m_multiPartFileItems = CmsRequestUtil.readMultipartFileItems(request);
        if (m_multiPartFileItems != null) {
            // this was indeed a multipart form request
            m_parameterMap = CmsRequestUtil.readParameterMapFromMultiPart(
                getCms().getRequestContext().getEncoding(),
                m_multiPartFileItems);
        } else {
            // the request was a "normal" request
            m_parameterMap = request.getParameterMap();
        }

        List methods = paramSetMethods();
        Iterator i = methods.iterator();
        while (i.hasNext()) {
            Method m = (Method)i.next();
            String name = m.getName().substring(8).toLowerCase();
            String[] values = (String[])m_parameterMap.get(name);
            String value = null;
            if (values != null) {
                // get the parameter value from the map
                value = values[0];
            }
            if (CmsStringUtil.isEmpty(value)) {
                value = null;
            }
            value = decodeParamValue(name, value);
            try {
                if (LOG.isDebugEnabled() && (value != null)) {
                    LOG.debug(Messages.get().key(Messages.LOG_SET_PARAM_2, m.getName(), value));
                }
                m.invoke(this, new Object[] {value});
            } catch (InvocationTargetException ite) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(ite.getLocalizedMessage());
                }
            } catch (IllegalAccessException eae) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(eae.getLocalizedMessage());
                }
            }
        }
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
        String dateFormat = key(Messages.GUI_CALENDAR_DATE_FORMAT_0);
        if (useTime) {
            dateFormat += " " + key(Messages.GUI_CALENDAR_TIME_FORMAT_0);
        }
        dateFormat = getCalendarJavaDateFormat(dateFormat);

        SimpleDateFormat df = new SimpleDateFormat(dateFormat);
        dateLong = df.parse(dateString).getTime();
        return dateLong;
    }

    /**
     * Returns the given timestamp as String formatted in a localized pattern.<p>
     * 
     * @param timestamp the time to format
     * @return the given timestamp as String formatted in a localized pattern
     */
    public String getCalendarLocalizedTime(long timestamp) {

        // get the current date & time 
        Locale locale = getLocale();
        TimeZone zone = TimeZone.getDefault();
        GregorianCalendar cal = new GregorianCalendar(zone, locale);
        cal.setTimeInMillis(timestamp);
        // format it nicely according to the localized pattern
        DateFormat df = new SimpleDateFormat(getCalendarJavaDateFormat(key(Messages.GUI_CALENDAR_DATE_FORMAT_0)
            + " "
            + key(Messages.GUI_CALENDAR_TIME_FORMAT_0)));
        return df.format(cal.getTime());
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
     * Returns the current workplace encoding.<p>
     * 
     * @return the current workplace encoding
     */
    public String getEncoding() {

        return OpenCms.getWorkplaceManager().getEncoding();
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
     * Returns the JSP action element.<p>
     * 
     * @return the JSP action element
     */
    public CmsJspActionElement getJsp() {

        return m_jsp;
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
     * Returns the current used message object.<p>
     * 
     * @return the current used message object
     */
    public CmsMessages getMessages() {

        return m_messages;
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
     * Returns the path to the cascading stylesheets.<p>
     * 
     * @param filename the name of the stylesheet
     * @return the path to the cascading stylesheets
     */
    public String getStyleUri(String filename) {

        return getStyleUri(getJsp(), filename);
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
     * Returns true if the online help for the users current workplace language is installed.<p>
     * 
     * @return true if the online help for the users current workplace language is installed
     */
    public boolean isHelpEnabled() {

        return getCms().existsResource(
            resolveMacros(CmsHelpTemplateBean.PATH_HELP),
            CmsResourceFilter.IGNORE_EXPIRATION);
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
     * Returns the localized resource string for a given message key,
     * checking the workplace default resources and all module bundles.<p>
     * 
     * If the key was not found, the return value is
     * <code>"??? " + keyName + " ???"</code>.<p>
     * 
     * If the key starts with <code>"help."</code> and is not found,
     * the value <code>"index.html"</code> is returned.<p>
     * 
     * @param keyName the key for the desired string 
     * @return the resource string for the given key 
     * 
     * @see CmsMessages#key(String)
     */
    public String key(String keyName) {

        return getMessages().key(keyName);
    }

    /**
     * Returns the localized resource string for a given message key,
     * with the provided replacement parameters.<p>
     * 
     * If the key was found in the bundle, it will be formatted using
     * a <code>{@link java.text.MessageFormat}</code> using the provided parameters.<p>
     * 
     * If the key was not found in the bundle, the return value is
     * <code>"??? " + keyName + " ???"</code>. This will also be returned 
     * if the bundle was not properly initialized first.
     * 
     * @param keyName the key for the desired string 
     * @param params the parameters to use for formatting
     * @return the resource string for the given key
     * 
     * @see CmsMessages#key(String) 
     */
    public String key(String keyName, Object[] params) {

        return getMessages().key(keyName, params);
    }

    /**
     * Returns the localized resource string for the given message key, 
     * checking the workplace default resources and all module bundles.<p>
     * 
     * If the key was not found, the provided default value 
     * is returned.<p>
     * 
     * @param keyName the key for the desired string 
     * @param defaultValue the default value in case the key does not exist in the bundle
     * @return the resource string for the given key it it exists, or the given default if not 
     * 
     * @see CmsMessages#key(String, String)
     */
    public String key(String keyName, String defaultValue) {

        return getMessages().key(keyName, defaultValue);
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
                    result.append(" onLoad=\"window.top.body.admin_head.location.href='");
                    result.append(getJsp().link(
                        I_CmsWpConstants.C_VFS_PATH_WORKPLACE + "action/administration_head.html"));
                    result.append("';\"");
                }
            }
            if (className != null) {
                result.append(" class=\"");
                result.append(className);
                result.append("\"");
            }
            if (CmsStringUtil.isNotEmpty(parameters)) {
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
     * @param stylesheet the used style sheet, if null the default stylesheet 'workplace.css' is inserted
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

            result.append(getStyleUri(getJsp(), stylesheet == null ? "workplace.css" : stylesheet));
            result.append("\">\n");
            return result.toString();
        } else {
            return "</html>";
        }
    }

    /**
     * Returns all initialized parameters of the current workplace class 
     * as hidden field tags that can be inserted in a form.<p>
     * 
     * @return all initialized parameters of the current workplace class
     * as hidden field tags that can be inserted in a html form
     */
    public String paramsAsHidden() {

        return paramsAsHidden(null);
    }

    /**
     * Returns all initialized parameters of the current workplace class 
     * that are not in the given exclusion list as hidden field tags that can be inserted in a form.<p>
     * 
     * @param excludes the parameters to exclude 
     * 
     * @return all initialized parameters of the current workplace class
     * that are not in the given exclusion list as hidden field tags that can be inserted in a form
     */
    public String paramsAsHidden(Collection excludes) {

        StringBuffer result = new StringBuffer(512);
        Map params = paramValues();
        Iterator i = params.keySet().iterator();
        while (i.hasNext()) {
            String param = (String)i.next();
            if ((excludes == null) || (!excludes.contains(param))) {
                Object value = params.get(param);
                result.append("<input type=\"hidden\" name=\"");
                result.append(param);
                result.append("\" value=\"");
                String encoded = CmsEncoder.encode(value.toString(), getCms().getRequestContext().getEncoding());
                result.append(encoded);
                result.append("\">\n");
            }
        }
        return result.toString();
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
     * Resolves the macros in the given String and replaces them by their localized keys.<p>
     * 
     * The following macro contexts are available in the Workplace:<ul>
     * <li>Macros based on the current users OpenCms context (obtained from the current <code>{@link CmsObject}</code>).</li>
     * <li>Localized key macros (obtained from the current <code>{@link CmsMessages}</code>).</li>
     * <li>Macros from the current JSP page context (obtained by <code>{@link #getJsp()}</code>).</li>
     * </ul>
     * 
     * @param input the input String containing the macros
     * @return the resolved String
     * 
     * @see CmsMacroResolver#resolveMacros(String)
     */
    public String resolveMacros(String input) {

        // 
        if (m_macroResolver == null) {
            // create a new macro resolver "with everything we got"
            m_macroResolver = CmsMacroResolver.newInstance()
            // initialize resolver with the objects available
                .setCmsObject(m_cms).setMessages(getMessages()).setJspPageContext(
                    (m_jsp == null) ? null : m_jsp.getJspContext());
        }
        // resolve the macros
        return m_macroResolver.resolveMacros(input);
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
     * Get a localized short key value for the workplace.<p>
     * 
     * @param keyName name of the key
     * @return a localized short key value
     */
    public String shortKey(String keyName) {

        String value = key(keyName + CmsMessages.C_KEY_SHORT_SUFFIX, (String)null);
        if (value == null) {
            // short key value not found, return "long" key value
            return key(keyName);
        }
        return value;
    }

    /**
     * Auxiliary method for initialization of messages.<p>
     * 
     * @param bundleName the bundle name to instanciate
     * 
     * @see #initMessages()
     */
    protected void addMessages(String bundleName) {

        m_bundles.add(new CmsMessages(bundleName, getLocale()));
    }

    /**
     * Returns the values of all parameter methods of this workplace class instance.<p>
     * 
     * @return the values of all parameter methods of this workplace class instance
     */
    protected Map allParamValues() {

        List methods = paramGetMethods();
        Map map = new HashMap(methods.size());
        Iterator i = methods.iterator();
        while (i.hasNext()) {
            Method m = (Method)i.next();
            Object o = null;
            try {
                o = m.invoke(this, new Object[0]);
            } catch (InvocationTargetException ite) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(ite);
                }
            } catch (IllegalAccessException eae) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(eae);
                }
            }
            if (o == null) {
                o = "";
            }
            map.put(m.getName().substring(8).toLowerCase(), o);
        }
        return map;
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
     * 
     * @return the encoded value of the parameter
     */
    protected String decodeParamValue(String paramName, String paramValue) {

        if ((paramName != null) && (paramValue != null)) {
            return CmsEncoder.decode(paramValue, getCms().getRequestContext().getEncoding());
        } else {
            return null;
        }
    }

    /**
     * Returns the map of parameters read from the current request.<p>
     *
     * This method will also handle parameters from forms
     * of type <code>multipart/form-data</code>.<p>
     * 
     * @return the map of parameters read from the current request
     */
    protected Map getParameterMap() {

        return m_parameterMap;
    }

    /**
     * Initializes the message object.<p>
     * 
     * By default the workplace messages object is used.<p>
     * 
     * You SHOULD override this method for setting the bundles you really need,
     * using the <code>{@link #addMessages(String)}</code> method.<p>
     */
    protected void initMessages() {

        // manually add the initialized workplace messages for the current user
        m_bundles.add(m_messages);
        addMessages(Messages.get().getBundleName());
    }

    /**
     * Initializes this workplace class instance.<p>
     * 
     * This method can be used in case there a workplace class was generated using
     * {@link Class#forName(java.lang.String)} to initialize the class members.<p> 
     * 
     * @param jsp the initialized JSP context
     */
    protected void initWorkplaceMembers(CmsJspActionElement jsp) {

        if (jsp != null) {
            m_jsp = jsp;
            m_cms = m_jsp.getCmsObject();
            m_session = m_jsp.getRequest().getSession();

            // get / create the workplace settings 
            m_settings = (CmsWorkplaceSettings)m_session.getAttribute(CmsWorkplaceManager.C_SESSION_WORKPLACE_SETTINGS);

            if (m_settings == null) {
                // create the settings object
                m_settings = new CmsWorkplaceSettings();
                m_settings = initWorkplaceSettings(m_cms, m_settings, false);
                storeSettings(m_session, m_settings);
            }

            // initialize messages and also store them in settings
            m_messages = OpenCms.getWorkplaceManager().getMessages(m_settings.getUserSettings().getLocale());
            initMessages();
            if (!m_bundles.isEmpty()) {
                m_messages = new CmsMultiMessages(m_bundles);
            }

            // check request for changes in the workplace settings
            initWorkplaceRequestValues(m_settings, m_jsp.getRequest());

            // set cms context accordingly
            initWorkplaceCmsContext(m_settings, m_cms);

        }
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
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(ite.getLocalizedMessage());
                }
            } catch (IllegalAccessException eae) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(eae.getLocalizedMessage());
                }
            }
            if (o != null) {
                map.put(m.getName().substring(8).toLowerCase(), o);
            }
        }
        return map;
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
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
            }
        }

        // check site setting
        if (!(settings.getSite().equals(reqCont.getSiteRoot()))) {
            // site was switched, set new site root
            reqCont.setSiteRoot(settings.getSite());
            // removed setting explorer resource to "/" to get the stored folder
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
        for (int i = 0; i < length; i++) {
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
        for (int i = 0; i < length; i++) {
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
}