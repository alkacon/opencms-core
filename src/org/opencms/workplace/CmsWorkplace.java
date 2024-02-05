/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsBroadcast;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.CmsStaticResourceHandler;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteManagerImpl;
import org.opencms.synchronize.CmsSynchronizeSettings;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.apache.commons.collections.Buffer;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;

/**
 * Master class for the JSP based workplace which provides default methods and
 * session handling for all JSP workplace classes.<p>
 *
 * @since 6.0.0
 */
public abstract class CmsWorkplace {

    /** The debug flag. */
    public static final boolean DEBUG = false;

    /** Path to the JSP workplace frame loader file. */
    public static final String JSP_WORKPLACE_URI = CmsWorkplace.VFS_PATH_VIEWS + "workplace.jsp";

    /** Request parameter name for the model file. */
    public static final String PARAM_MODELFILE = "modelfile";

    /** Request parameter name prefix for the preferred editors. */
    public static final String INPUT_DEFAULT = "default";

    /** Request parameter name for the resource list. */
    public static final String PARAM_RESOURCELIST = "resourcelist";

    /** Request parameter name for no settings in start galleries. */
    public static final String INPUT_NONE = "none";

    /** Path to system folder. */
    public static final String VFS_PATH_SYSTEM = "/system/";

    /** Path to sites folder. */
    public static final String VFS_PATH_SITES = "/sites/";

    /** Path to the workplace. */
    public static final String VFS_PATH_WORKPLACE = VFS_PATH_SYSTEM + "workplace/";

    /** Constant for the JSP dialogs path. */
    public static final String PATH_DIALOGS = VFS_PATH_WORKPLACE + "commons/";

    /** Parameter for the default locale. */
    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    /** Parameter for the default language. */
    public static final String DEFAULT_LANGUAGE = DEFAULT_LOCALE.getLanguage();

    /** Constant for the JSP common files (e.g. error page) path. */
    public static final String DIALOG_PATH_COMMON = PATH_DIALOGS + "includes/";

    /** Constant for the JSP common close dialog page. */
    public static final String FILE_DIALOG_CLOSE = DIALOG_PATH_COMMON + "closedialog.jsp";

    /** Constant for the JSP common confirmation dialog. */
    public static final String FILE_DIALOG_SCREEN_CONFIRM = DIALOG_PATH_COMMON + "confirmation.jsp";

    /** Constant for the JSP common error dialog. */
    public static final String FILE_DIALOG_SCREEN_ERROR = DIALOG_PATH_COMMON + "error.jsp";

    /** Constant for the JSP common error dialog. */
    public static final String FILE_DIALOG_SCREEN_ERRORPAGE = DIALOG_PATH_COMMON + "errorpage.jsp";

    /** Constant for the JSP common wait screen. */
    public static final String FILE_DIALOG_SCREEN_WAIT = DIALOG_PATH_COMMON + "wait.jsp";

    /** Path to workplace views. */
    public static final String VFS_PATH_VIEWS = VFS_PATH_WORKPLACE + "views/";

    /** Constant for the JSP explorer filelist file. */
    public static final String FILE_EXPLORER_FILELIST = VFS_PATH_VIEWS + "explorer/explorer_files.jsp";

    /** Constant for the JSP common report page. */
    public static final String FILE_REPORT_OUTPUT = DIALOG_PATH_COMMON + "report.jsp";

    /** Helper variable to deliver the html end part. */
    public static final int HTML_END = 1;

    /** Helper variable to deliver the html start part. */
    public static final int HTML_START = 0;

    /** The request parameter for the workplace project selection. */
    public static final String PARAM_WP_EXPLORER_RESOURCE = "wpExplorerResource";

    /** The request parameter for the workplace project selection. */
    public static final String PARAM_WP_PROJECT = "wpProject";

    /** The request parameter for the workplace site selection. */
    public static final String PARAM_WP_SITE = "wpSite";

    /** Constant for the JSP workplace path. */
    public static final String PATH_WORKPLACE = VFS_PATH_WORKPLACE;

    /** Path for file type icons relative to the resources folder. */
    public static final String RES_PATH_FILETYPES = "filetypes/";

    /** Path to exported system image folder. */
    public static final String RFS_PATH_RESOURCES = "/resources/";

    /** Directory name of content default_bodies folder. */
    public static final String VFS_DIR_DEFAULTBODIES = "default_bodies/";

    /** Directory name of content templates folder. */
    public static final String VFS_DIR_TEMPLATES = "templates/";

    /** Path to commons. */
    public static final String VFS_PATH_COMMONS = VFS_PATH_WORKPLACE + "commons/";

    /** Path to the workplace editors. */
    public static final String VFS_PATH_EDITORS = VFS_PATH_WORKPLACE + "editors/";

    /** Path to the galleries. */
    public static final String VFS_PATH_GALLERIES = VFS_PATH_SYSTEM + "galleries/";

    /** Path to locales. */
    public static final String VFS_PATH_LOCALES = VFS_PATH_WORKPLACE + "locales/";

    /** Path to modules folder. */
    public static final String VFS_PATH_MODULES = VFS_PATH_SYSTEM + "modules/";

    /** Path to system image folder. */
    public static final String VFS_PATH_RESOURCES = VFS_PATH_WORKPLACE + "resources/";

    /** Constant for the direct edit view JSP. */
    public static final String VIEW_DIRECT_EDIT = VFS_PATH_VIEWS + "explorer/directEdit.jsp";

    /** Constant for the explorer view JSP. */
    public static final String VIEW_WORKPLACE = VFS_PATH_VIEWS + "explorer/explorer_fs.jsp";

    /** Constant for the admin view JSP. */
    public static final String VIEW_ADMIN = CmsWorkplace.VFS_PATH_VIEWS + "admin/admin-fs.jsp";

    /** Key name for the request attribute to indicate a multipart request was already parsed. */
    protected static final String REQUEST_ATTRIBUTE_MULTIPART = "__CmsWorkplace.MULTIPART";

    /** Key name for the request attribute to reload the folder tree view. */
    protected static final String REQUEST_ATTRIBUTE_RELOADTREE = "__CmsWorkplace.RELOADTREE";

    /** Key name for the session workplace class. */
    protected static final String SESSION_WORKPLACE_CLASS = "__CmsWorkplace.WORKPLACE_CLASS";

    /** The "explorerview" view selection. */
    public static final String VIEW_EXPLORER = "explorerview";

    /** The "galleryview" view selection. */
    public static final String VIEW_GALLERY = "galleryview";

    /** The "list" view selection. */
    public static final String VIEW_LIST = "listview";

    /** Request parameter name for the directpublish parameter. */
    public static final String PARAM_DIRECTPUBLISH = "directpublish";

    /** Request parameter name for the publishsiblings parameter. */
    public static final String PARAM_PUBLISHSIBLINGS = "publishsiblings";

    /** Request parameter name for the relatedresources parameter. */
    public static final String PARAM_RELATEDRESOURCES = "relatedresources";

    /** Request parameter name for the subresources parameter. */
    public static final String PARAM_SUBRESOURCES = "subresources";

    /** Default value for date last modified, the release and expire date. */
    public static final String DEFAULT_DATE_STRING = "-";

    /** Absolute path to the model file dialog. */
    public static final String VFS_PATH_MODELDIALOG = CmsWorkplace.VFS_PATH_COMMONS
        + "newresource_xmlcontent_modelfile.jsp";

    /** Absolute path to thenew resource dialog. */
    public static final String VFS_PATH_NEWRESOURCEDIALOG = CmsWorkplace.VFS_PATH_COMMONS
        + "newresource_xmlcontent.jsp";

    /** Request parameter name for the new resource type. */
    public static final String PARAM_NEWRESOURCETYPE = "newresourcetype";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsWorkplace.class);

    /** The link to the explorer file list (cached for performance reasons). */
    private static String m_file_explorer_filelist;

    /** The URI to the skin resources (cached for performance reasons). */
    private static String m_skinUri;

    /** The URI to the stylesheet resources (cached for performance reasons). */
    private static String m_styleUri;

    /** The request parameter for the workplace view selection. */
    public static final String PARAM_WP_VIEW = "wpView";

    /** The request parameter for the workplace start selection. */
    public static final String PARAM_WP_START = "wpStart";

    /** The current users OpenCms context. */
    private CmsObject m_cms;

    /** Helper variable to store the id of the current project. */
    private CmsUUID m_currentProjectId;

    /** Flag for indicating that request forwarded was. */
    private boolean m_forwarded;

    /** The current JSP action element. */
    private CmsJspActionElement m_jsp;

    /** The macro resolver, this is cached to avoid multiple instance generation. */
    private CmsMacroResolver m_macroResolver;

    /**  The currently used message bundle. */
    private CmsMultiMessages m_messages;

    /** The list of multi part file items (if available). */
    private List<FileItem> m_multiPartFileItems;

    /** The map of parameters read from the current request. */
    private Map<String, String[]> m_parameterMap;

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
     * Constructor in case no page context is available.<p>
     *
     * @param cms the current user context
     * @param session the session
     */
    public CmsWorkplace(CmsObject cms, HttpSession session) {

        initWorkplaceMembers(cms, session);
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
    public static String buildSelect(
        String parameters,
        List<String> options,
        List<String> values,
        int selected,
        boolean useLineFeed) {

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
                    value = values.get(i);
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
     * Checks if permissions for roles should be editable for the current user on the resource with the given path.<p>
     *
     * @param cms the CMS context
     * @param path the path of a resource
     *
     * @return <code>true</code> if permissions for roles should be editable for the current user on the resource with the given path
     */
    public static boolean canEditPermissionsForRoles(CmsObject cms, String path) {

        return OpenCms.getRoleManager().hasRoleForResource(cms, CmsRole.VFS_MANAGER, path)
            && path.startsWith(VFS_PATH_SYSTEM);
    }

    /**
     * Returns the style sheets for the report.<p>
     *
     * @param cms the current users context
     * @return the style sheets for the report
     */
    public static String generateCssStyle(CmsObject cms) {

        StringBuffer result = new StringBuffer(128);
        result.append("<style type='text/css'>\n");
        String contents = "";
        try {
            contents = new String(
                cms.readFile(VFS_PATH_COMMONS + "style/report.css").getContents(),
                OpenCms.getSystemInfo().getDefaultEncoding());
        } catch (Exception e) {
            // ignore
        }
        if (CmsStringUtil.isEmpty(contents)) {
            // css file not found, create default styles
            result.append(
                "body       { box-sizing: border-box; -moz-box-sizing: border-box; padding: 2px; margin: 0; color: /*begin-color WindowText*/#000000/*end-color*/; background-color: /*begin-color Window*/#ffffff/*end-color*/; font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 11px; }\n");
            result.append(
                "div.main   { box-sizing: border-box; -moz-box-sizing: border-box; color: /*begin-color WindowText*/#000000/*end-color*/; white-space: nowrap; }\n");
            result.append("span.head  { color: #000099; font-weight: bold; }\n");
            result.append("span.note  { color: #666666; }\n");
            result.append("span.ok    { color: #009900; }\n");
            result.append("span.warn  { color: #990000; padding-left: 40px; }\n");
            result.append("span.err   { color: #990000; font-weight: bold; padding-left: 40px; }\n");
            result.append("span.throw { color: #990000; font-weight: bold; }\n");
            result.append("span.link1 { color: #666666; }\n");
            result.append("span.link2 { color: #666666; padding-left: 40px; }\n");
            result.append("span.link2 { color: #990000; }\n");
        } else {
            result.append(contents);
        }
        result.append("</style>\n");
        return result.toString();
    }

    /**
     * Generates the footer for the extended report view.<p>
     *
     * @return html code
     */
    public static String generatePageEndExtended() {

        StringBuffer result = new StringBuffer(128);
        result.append("</div>\n");
        result.append("</body>\n");
        result.append("</html>\n");
        return result.toString();
    }

    /**
     * Generates the footer for the simple report view.<p>
     *
     * @return html code
     */
    public static String generatePageEndSimple() {

        StringBuffer result = new StringBuffer(128);
        result.append("</td></tr>\n");
        result.append("</table></div>\n");
        result.append("</body>\n</html>");
        return result.toString();
    }

    /**
     * Generates the header for the extended report view.<p>
     *
     * @param cms the current users context
     * @param encoding the encoding string
     *
     * @return html code
     */
    public static String generatePageStartExtended(CmsObject cms, String encoding) {

        StringBuffer result = new StringBuffer(128);
        result.append("<html>\n<head>\n");
        result.append("<meta HTTP-EQUIV='Content-Type' CONTENT='text/html; charset=");
        result.append(encoding);
        result.append("'>\n");
        result.append(generateCssStyle(cms));
        result.append("</head>\n");
        result.append("<body style='overflow: auto;'>\n");
        result.append("<div class='main'>\n");
        return result.toString();
    }

    /**
     * Generates the header for the simple report view.<p>
     *
     * @param wp the workplace instance
     *
     * @return html code
     */
    public static String generatePageStartSimple(CmsWorkplace wp) {

        StringBuffer result = new StringBuffer(128);
        result.append("<html>\n<head>\n");
        result.append("<meta HTTP-EQUIV='Content-Type' CONTENT='text/html; charset=");
        result.append(wp.getEncoding());
        result.append("'>\n");
        result.append("<link rel='stylesheet' type='text/css' href='");
        result.append(wp.getStyleUri("workplace.css"));
        result.append("'>\n");
        result.append(generateCssStyle(wp.getCms()));
        result.append("</head>\n");
        result.append("<body style='background-color:/*begin-color Menu*/#f0f0f0/*end-color*/;'>\n");
        result.append("<div style='vertical-align:middle; height: 100%;'>\n");
        result.append("<table border='0' style='vertical-align:middle; height: 100%;'>\n");
        result.append("<tr><td width='40' align='center' valign='middle'><img name='report_img' src='");
        result.append(getSkinUri());
        result.append("commons/wait.gif' width='32' height='32' alt=''></td>\n");
        result.append("<td valign='middle'>");
        return result.toString();
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
            m_skinUri = OpenCms.getSystemInfo().getContextPath() + RFS_PATH_RESOURCES;
        }
        return m_skinUri;
    }

    /**
     * Returns the start site from the given user settings.<p>
     *
     * @param cms the cms context
     * @param userSettings the user settings
     *
     * @return the start site root
     */
    public static String getStartSiteRoot(CmsObject cms, CmsUserSettings userSettings) {

        String startSiteRoot = userSettings.getStartSite();
        if (startSiteRoot.endsWith("/")) {
            // remove trailing slash
            startSiteRoot = startSiteRoot.substring(0, startSiteRoot.length() - 1);
        }
        if (CmsStringUtil.isNotEmpty(startSiteRoot)
            && (OpenCms.getSiteManager().getSiteForSiteRoot(startSiteRoot) == null)) {
            // this is not the root site and the site is not in the list
            List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(
                cms,
                false,
                cms.getRequestContext().getCurrentUser().getOuFqn());
            if (sites.size() == 1) {
                startSiteRoot = sites.get(0).getSiteRoot();
            } else if (sites.size() > 1) {
                String siteRoot = null;
                String defaultSite = OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartSite();
                // check if the default start site is available to the user
                for (CmsSite site : sites) {
                    if (site.getSiteRoot().equals(defaultSite)) {
                        siteRoot = defaultSite;
                        break;
                    }
                }
                if (siteRoot == null) {
                    // list of available sites contains more than one site, but not the configured default, pick any
                    startSiteRoot = sites.get(0).getSiteRoot();
                } else {
                    startSiteRoot = siteRoot;
                }
            }
        }
        return startSiteRoot;
    }

    /**
     * Returns the start site from the given workplace settings.<p>
     *
     * @param cms the cms context
     * @param settings the workplace settings
     *
     * @return the start site root
     */
    public static String getStartSiteRoot(CmsObject cms, CmsWorkplaceSettings settings) {

        return getStartSiteRoot(cms, settings.getUserSettings());
    }

    /**
     * Returns the URI to static resources served from the class path.<p>
     *
     * @param resourceName the resource name
     *
     * @return the URI
     */
    public static String getStaticResourceUri(String resourceName) {

        return getStaticResourceUri(resourceName, null);
    }

    /**
     * Returns the URI to static resources served from the class path.<p>
     *
     * @param resourceName the resource name
     * @param versionInfo add an additional version info parameter to avoid browser caching issues
     *
     * @return the URI
     */
    public static String getStaticResourceUri(String resourceName, String versionInfo) {

        resourceName = CmsStaticResourceHandler.removeStaticResourcePrefix(resourceName);
        String uri = CmsStringUtil.joinPaths(OpenCms.getSystemInfo().getStaticResourceContext(), resourceName);
        if (versionInfo != null) {
            uri += "?v=" + versionInfo;
        }
        return uri;
    }

    /**
     * Returns the path to the cascading stylesheets.<p>
     *
     * @param jsp the JSP context
     * @return the path to the cascading stylesheets
     */
    public static String getStyleUri(CmsJspActionElement jsp) {

        if (m_styleUri == null) {

            CmsProject project = jsp.getCmsObject().getRequestContext().getCurrentProject();
            try {
                jsp.getCmsObject().getRequestContext().setCurrentProject(
                    jsp.getCmsObject().readProject(CmsProject.ONLINE_PROJECT_ID));
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
            CmsProject project = jsp.getCmsObject().getRequestContext().getCurrentProject();
            try {
                jsp.getCmsObject().getRequestContext().setCurrentProject(
                    jsp.getCmsObject().readProject(CmsProject.ONLINE_PROJECT_ID));
                m_styleUri = jsp.link("/system/workplace/commons/style/");
            } catch (CmsException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            } finally {
                jsp.getCmsObject().getRequestContext().setCurrentProject(project);
            }
        }
        return m_styleUri + filename;
    }

    /**
     * Returns the temporary file name for the given resource name.<p>
     *
     * To create a temporary file name of a resource name, the prefix char <code>'~'</code> (tilde)
     * is added to the file name after all parent folder names have been removed.<p>
     *
     * @param resourceName the resource name to return the temporary file name for
     *
     * @return the temporary file name for the given resource name
     *
     * @see CmsResource#isTemporaryFileName(String)
     * @see #isTemporaryFile(CmsResource)
     */
    public static String getTemporaryFileName(String resourceName) {

        if (resourceName == null) {
            return null;
        }
        StringBuffer result = new StringBuffer(resourceName.length() + 2);
        result.append(CmsResource.getFolderPath(resourceName));
        result.append(CmsResource.TEMP_FILE_PREFIX);
        result.append(CmsResource.getName(resourceName));
        return result.toString();
    }

    /**
     * Creates a link for the OpenCms workplace that will reload the whole workplace, switch to the explorer view, the
     * site of the given explorerRootPath and show the folder given in the explorerRootPath.
     * <p>
     *
     * @param jsp
     *            needed for link functionality.
     *
     * @param explorerRootPath
     *            a root relative folder link (has to end with '/').
     *
     * @return a link for the OpenCms workplace that will reload the whole workplace, switch to the explorer view, the
     *         site of the given explorerRootPath and show the folder given in the explorerRootPath.
     */
    public static String getWorkplaceExplorerLink(final CmsJspActionElement jsp, final String explorerRootPath) {

        return getWorkplaceExplorerLink(jsp.getCmsObject(), explorerRootPath);

    }

    /**
     * Creates a link for the OpenCms workplace that will reload the whole workplace, switch to the explorer view, the
     * site of the given explorerRootPath and show the folder given in the explorerRootPath.
     * <p>
     *
     * @param cms
     *            the cms object
     *
     * @param explorerRootPath
     *            a root relative folder link (has to end with '/').
     *
     * @return a link for the OpenCms workplace that will reload the whole workplace, switch to the explorer view, the
     *         site of the given explorerRootPath and show the folder given in the explorerRootPath.
     */
    public static String getWorkplaceExplorerLink(final CmsObject cms, final String explorerRootPath) {

        // split the root site:
        String targetSiteRoot = OpenCms.getSiteManager().getSiteRoot(explorerRootPath);
        if (targetSiteRoot == null) {
            if (OpenCms.getSiteManager().startsWithShared(explorerRootPath)) {
                targetSiteRoot = OpenCms.getSiteManager().getSharedFolder();
            } else {
                targetSiteRoot = "";
            }
        }
        String targetVfsFolder;
        if (explorerRootPath.startsWith(targetSiteRoot)) {
            targetVfsFolder = explorerRootPath.substring(targetSiteRoot.length());
            targetVfsFolder = CmsStringUtil.joinPaths("/", targetVfsFolder);
        } else {
            targetVfsFolder = "/";
            // happens in case of the shared site
        }

        StringBuilder link2Source = new StringBuilder();
        link2Source.append(JSP_WORKPLACE_URI);
        link2Source.append("?");
        link2Source.append(CmsWorkplace.PARAM_WP_EXPLORER_RESOURCE);
        link2Source.append("=");
        link2Source.append(targetVfsFolder);
        link2Source.append("&");
        link2Source.append(PARAM_WP_VIEW);
        link2Source.append("=");
        link2Source.append(
            OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                cms,
                "/system/workplace/views/explorer/explorer_fs.jsp"));
        link2Source.append("&");
        link2Source.append(PARAM_WP_SITE);
        link2Source.append("=");
        link2Source.append(targetSiteRoot);

        String result = link2Source.toString();
        result = OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, result);
        return result;
    }

    /**
     * Returns the workplace settings of the current user.<p>
     *
     * @param cms the cms context
     * @param req the request
     *
     * @return the workplace settings or <code>null</code> if the user is not logged in
     */
    public static CmsWorkplaceSettings getWorkplaceSettings(CmsObject cms, HttpServletRequest req) {

        HttpSession session = req.getSession(false);
        CmsWorkplaceSettings workplaceSettings = null;
        if (session != null) {
            // all logged in user will have a session
            workplaceSettings = (CmsWorkplaceSettings)session.getAttribute(
                CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
            // ensure workplace settings attribute is set
            if (workplaceSettings == null) {
                // creating any instance of {@link org.opencms.workplace.CmsWorkplaceSettings} and store it
                workplaceSettings = initWorkplaceSettings(cms, null, false);
                storeSettings(session, workplaceSettings);
            }
        }
        return workplaceSettings;
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
                user = cms.readUser(cms.getRequestContext().getCurrentUser().getId());
            } catch (CmsException e) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage());
                }
                user = cms.getRequestContext().getCurrentUser();
            }
        } else {
            user = cms.getRequestContext().getCurrentUser();
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
        settings.setProject(cms.getRequestContext().getCurrentProject().getUuid());
        String currentSite = cms.getRequestContext().getSiteRoot();
        // keep the current site
        settings.setSite(currentSite);

        // switch to users preferred site
        String startSiteRoot = getStartSiteRoot(cms, settings);

        try {
            CmsObject cloneCms = OpenCms.initCmsObject(cms);
            cloneCms.getRequestContext().setSiteRoot(startSiteRoot);
            String projectName = settings.getUserSettings().getStartProject();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(projectName)) {
                cloneCms.getRequestContext().setCurrentProject(cloneCms.readProject(projectName));
            }
            // check start folder:
            String startFolder = settings.getUserSettings().getStartFolder();
            if (!cloneCms.existsResource(startFolder, CmsResourceFilter.IGNORE_EXPIRATION)) {
                // self - healing:
                startFolder = "/";
                settings.getUserSettings().setStartFolder(startFolder);
            }
            settings.setSite(startSiteRoot);
            settings.setExplorerResource(startFolder, cloneCms);
        } catch (Exception e) {
            settings.getUserSettings().setStartFolder("/");
            settings.setSite(startSiteRoot);
            settings.setExplorerResource("/", null);
        } finally {
            settings.setSite(currentSite);
        }
        // get the default view from the user settings
        settings.setViewUri(OpenCms.getLinkManager().substituteLink(cms, settings.getUserSettings().getStartView()));
        return settings;
    }

    /**
     * Returns <code>true</code> if the given resource is a temporary file.<p>
     *
     * A resource is considered a temporary file it is a file where the
     * {@link CmsResource#FLAG_TEMPFILE} flag has been set, or if the file name (without parent folders)
     * starts with the prefix char <code>'~'</code> (tilde).<p>
     *
     * @param resource the resource name to check
     *
     * @return <code>true</code> if the given resource name is a temporary file
     *
     * @see #getTemporaryFileName(String)
     * @see CmsResource#isTemporaryFileName(String)
     */
    public static boolean isTemporaryFile(CmsResource resource) {

        return (resource != null)
            && ((resource.isFile()
                && (((resource.getFlags() & CmsResource.FLAG_TEMPFILE) > 0)
                    || (CmsResource.isTemporaryFileName(resource.getName())))));
    }

    /**
     * Substitutes the site title.<p>
     *
     * @param title the raw site title
     * @param locale the localel
     *
     * @return the locale specific site title
     */
    public static String substituteSiteTitleStatic(String title, Locale locale) {

        if (title.equals(CmsSiteManagerImpl.SHARED_FOLDER_TITLE)) {
            return Messages.get().getBundle(locale).key(Messages.GUI_SHARED_TITLE_0);
        }
        return title;

    }

    /**
     * Updates the user preferences after changes have been made.<p>
     *
     * @param cms the current cms context
     * @param req the current http request
     */
    public static void updateUserPreferences(CmsObject cms, HttpServletRequest req) {

        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }
        CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(
            CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
        if (settings == null) {
            return;
        }
        // keep old synchronize settings
        CmsSynchronizeSettings synchronizeSettings = settings.getUserSettings().getSynchronizeSettings();
        settings = CmsWorkplace.initWorkplaceSettings(cms, settings, true);
        settings.getUserSettings().setSynchronizeSettings(synchronizeSettings);
    }

    /**
     * Stores the settings in the given session.<p>
     *
     * @param session the session to store the settings in
     * @param settings the settings
     */
    static void storeSettings(HttpSession session, CmsWorkplaceSettings settings) {

        // save the workplace settings in the session
        session.setAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS, settings);
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
        Map<String, Object> params = allParamValues();
        Iterator<Entry<String, Object>> i = params.entrySet().iterator();
        while (i.hasNext()) {
            Entry<String, Object> entry = i.next();
            result.append("<input type=\"hidden\" name=\"");
            result.append(entry.getKey());
            result.append("\" value=\"");
            String encoded = CmsEncoder.encode(entry.getValue().toString(), getCms().getRequestContext().getEncoding());
            result.append(encoded);
            result.append("\">\n");
        }
        return result.toString();
    }

    /**
     * Returns all present request parameters as String.<p>
     *
     * The String is formatted as a parameter String (<code>param1=val1&amp;param2=val2</code>) with UTF-8 encoded values.<p>
     *
     * @return all present request parameters as String
     */
    public String allParamsAsRequest() {

        StringBuffer retValue = new StringBuffer(512);
        HttpServletRequest request = getJsp().getRequest();
        Iterator<String> paramNames = request.getParameterMap().keySet().iterator();
        while (paramNames.hasNext()) {
            String paramName = paramNames.next();
            String paramValue = request.getParameter(paramName);
            retValue.append(
                paramName + "=" + CmsEncoder.encode(paramValue, getCms().getRequestContext().getEncoding()));
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
    public String buildSelect(String parameters, List<String> options, List<String> values, int selected) {

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
        if ((href != null) && href.toLowerCase().startsWith("javascript:")) {
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
                    result.append(
                        "class=\"norm\" onmouseover=\"className='over'\" onmouseout=\"className='norm'\" onmousedown=\"className='push'\" onmouseup=\"className='over'\"");
                } else {
                    result.append("class=\"disabled\"");
                }
                result.append("><span unselectable=\"on\" class=\"combobutton\" ");
                result.append("style=\"background-image: url('");
                result.append(imagePath);
                result.append(image);
                if ((image != null) && (image.indexOf('.') == -1)) {
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
                    result.append(
                        "class=\"norm\" onmouseover=\"className='over'\" onmouseout=\"className='norm'\" onmousedown=\"className='push'\" onmouseup=\"className='over'\"");
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
                    result.append(
                        "class=\"norm\" onmouseover=\"className='over'\" onmouseout=\"className='norm'\" onmousedown=\"className='push'\" onmouseup=\"className='over'\"");
                } else {
                    result.append("class=\"disabled\"");
                }
                result.append("><img class=\"button\" src=\"");
                result.append(imagePath);
                result.append(image);
                if ((image != null) && (image.indexOf('.') == -1)) {
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
        result.append(
            "<td><span class=\"norm\"><span unselectable=\"on\" class=\"txtbutton\" style=\"padding-right: 0px; padding-left: ");
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
     * Checks the lock state of the resource and locks it if the autolock feature is enabled.<p>
     *
     * @param resource the resource name which is checked
     * @throws CmsException if reading or locking the resource fails
     */
    public void checkLock(String resource) throws CmsException {

        checkLock(resource, CmsLockType.EXCLUSIVE);
    }

    /**
     * Checks the lock state of the resource and locks it if the autolock feature is enabled.<p>
     *
     * @param resource the resource name which is checked
     * @param type indicates the mode {@link CmsLockType#EXCLUSIVE} or {@link CmsLockType#TEMPORARY}
     *
     * @throws CmsException if reading or locking the resource fails
     */
    public void checkLock(String resource, CmsLockType type) throws CmsException {

        CmsResource res = getCms().readResource(resource, CmsResourceFilter.ALL);
        CmsLock lock = getCms().getLock(res);
        boolean lockable = lock.isLockableBy(getCms().getRequestContext().getCurrentUser());

        if (OpenCms.getWorkplaceManager().autoLockResources()) {
            // autolock is enabled, check the lock state of the resource
            if (lockable) {
                // resource is lockable, so lock it automatically
                if (type == CmsLockType.TEMPORARY) {
                    getCms().lockResourceTemporary(resource);
                } else {
                    getCms().lockResource(resource);
                }
            } else {
                throw new CmsException(Messages.get().container(Messages.ERR_WORKPLACE_LOCK_RESOURCE_1, resource));
            }
        } else {
            if (!lockable) {
                throw new CmsException(Messages.get().container(Messages.ERR_WORKPLACE_LOCK_RESOURCE_1, resource));
            }
        }
    }

    /**
     * First sets site and project in the workplace settings, then fills all class parameter values from the data
     * provided in the current request.<p>
     *
     * @param settings the workplace settings
     * @param request the current request
     */
    public void fillParamValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        initSettings(settings, request);
        fillParamValues(request);
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

        m_parameterMap = null;
        // ensure a multipart request is parsed only once (for "forward" scenarios with reports)
        if (null == request.getAttribute(REQUEST_ATTRIBUTE_MULTIPART)) {
            // check if this is a multipart request
            m_multiPartFileItems = CmsRequestUtil.readMultipartFileItems(request);
            if (m_multiPartFileItems != null) {
                // this was indeed a multipart form request
                m_parameterMap = CmsRequestUtil.readParameterMapFromMultiPart(
                    getCms().getRequestContext().getEncoding(),
                    m_multiPartFileItems);
                request.setAttribute(REQUEST_ATTRIBUTE_MULTIPART, Boolean.TRUE);
            }
        }
        if (m_parameterMap == null) {
            // the request was a "normal" request
            m_parameterMap = request.getParameterMap();
        }

        List<Method> methods = paramSetMethods();
        Iterator<Method> i = methods.iterator();
        while (i.hasNext()) {
            Method m = i.next();
            String name = m.getName().substring(8).toLowerCase();
            String[] values = m_parameterMap.get(name);
            String value = null;
            if (values != null) {
                // get the parameter value from the map
                value = values[0];
            }
            if (CmsStringUtil.isEmpty(value)) {
                value = null;
            }

            // TODO: this is very dangerous since most of the dialogs does not send encoded data
            // and by decoding not encoded data the data will get corrupted, for instance '1+2' will become '1 2'.
            // we should ensure that we decode the data only if the data has been encoded
            value = decodeParamValue(name, value);
            try {
                if (LOG.isDebugEnabled() && (value != null)) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_SET_PARAM_2, m.getName(), value));
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
     * Returns the message String for the broadcast message alert of the workplace.<p>
     *
     * Caution: returns the pure message String (not escaped) or null, if no message is pending.<p>
     *
     * @return the message String for the broadcast message alert of the workplace
     */
    public String getBroadcastMessageString() {

        CmsSessionInfo sessionInfo = OpenCms.getSessionManager().getSessionInfo(getSession());
        if (sessionInfo == null) {
            return null;
        }
        String sessionId = sessionInfo.getSessionId().toString();
        Buffer messageQueue = OpenCms.getSessionManager().getBroadcastQueue(sessionId);
        if (!messageQueue.isEmpty()) {
            // create message String
            StringBuffer result = new StringBuffer(512);
            // the user has pending messages, display them all
            while (!messageQueue.isEmpty()) {
                CmsBroadcast message = (CmsBroadcast)messageQueue.remove();
                result.append('[');
                result.append(getMessages().getDateTime(message.getSendTime()));
                result.append("] ");
                result.append(key(Messages.GUI_LABEL_BROADCASTMESSAGEFROM_0));
                result.append(' ');
                if (message.getUser() != null) {
                    result.append(message.getUser().getName());
                } else {
                    // system message
                    result.append(key(Messages.GUI_LABEL_BROADCAST_FROM_SYSTEM_0));
                }
                result.append(":\n");
                result.append(message.getMessage());
                result.append("\n\n");
            }
            return result.toString();
        }
        // no message pending, return null
        return null;
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
            m_file_explorer_filelist = OpenCms.getLinkManager().substituteLink(getCms(), FILE_EXPLORER_FILELIST);
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
     * Returns the current users workplace locale settings.<p>
     *
     * @return the current users workplace locale setting
     */
    public Locale getLocale() {

        return getSettings().getUserSettings().getLocale();
    }

    /**
     * Returns the current used macro resolver instance.<p>
     *
     * @return the macro resolver
     */
    public CmsMacroResolver getMacroResolver() {

        if (m_macroResolver == null) {
            // create a new macro resolver "with everything we got"
            m_macroResolver = CmsMacroResolver.newInstance()
                // initialize resolver with the objects available
                .setCmsObject(m_cms).setMessages(getMessages()).setJspPageContext(
                    (m_jsp == null) ? null : m_jsp.getJspContext());
            m_macroResolver.setParameterMap(m_parameterMap);
        }
        return m_macroResolver;
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
    public List<FileItem> getMultiPartFileItems() {

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
            m_resourceUri = OpenCms.getSystemInfo().getContextPath() + CmsWorkplace.RFS_PATH_RESOURCES;
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
     * Sets site and project in the workplace settings with the request values of parameters
     * <code>{@link CmsWorkplace#PARAM_WP_SITE}</code> and <code>{@link CmsWorkplace#PARAM_WP_PROJECT}</code>.<p>
     *
     * @param settings the workplace settings
     * @param request the current request
     *
     * @return true, if a reload of the main body frame is required
     */
    public boolean initSettings(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // check if the user requested a project change
        String project = request.getParameter(PARAM_WP_PROJECT);
        boolean reloadRequired = false;
        if (project != null) {
            reloadRequired = true;
            try {
                getCms().readProject(new CmsUUID(project));
            } catch (Exception e) {
                // project not found, set online project
                project = String.valueOf(CmsProject.ONLINE_PROJECT_ID);
            }
            try {
                m_cms.getRequestContext().setCurrentProject(getCms().readProject(new CmsUUID(project)));
            } catch (Exception e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage(), e);
                }
            }
            settings.setProject(new CmsUUID(project));
        }

        // check if the user requested a site change
        String site = request.getParameter(PARAM_WP_SITE);
        if (site != null) {
            reloadRequired = true;
            m_cms.getRequestContext().setSiteRoot(site);
            settings.setSite(site);
        }

        // check which resource was requested
        String explorerResource = request.getParameter(PARAM_WP_EXPLORER_RESOURCE);
        if (explorerResource != null) {
            reloadRequired = true;
            settings.setExplorerResource(explorerResource, getCms());
        }

        return reloadRequired;
    }

    /**
     * Returns the forwarded flag.<p>
     *
     * @return the forwarded flag
     */
    public boolean isForwarded() {

        return m_forwarded;
    }

    /**
     * Returns true if the online help for the users current workplace language is installed.<p>
     *
     * @return true if the online help for the users current workplace language is installed
     */
    public boolean isHelpEnabled() {

        return false;
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
     * @see CmsMessages#keyDefault(String, String)
     */
    public String keyDefault(String keyName, String defaultValue) {

        return getMessages().keyDefault(keyName, defaultValue);
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
            result.append("<!DOCTYPE html>\n");
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
    public String paramsAsHidden(Collection<String> excludes) {

        StringBuffer result = new StringBuffer(512);
        Map<String, Object> params = paramValues();
        Iterator<Entry<String, Object>> i = params.entrySet().iterator();
        while (i.hasNext()) {
            Entry<String, Object> entry = i.next();
            String param = entry.getKey();
            if ((excludes == null) || (!excludes.contains(param))) {
                result.append("<input type=\"hidden\" name=\"");
                result.append(param);
                result.append("\" value=\"");
                String encoded = CmsEncoder.encode(
                    entry.getValue().toString(),
                    getCms().getRequestContext().getEncoding());
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
    public Map<String, String[]> paramsAsParameterMap() {

        return CmsRequestUtil.createParameterMap(paramValues());
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
        Map<String, Object> params = paramValues();
        Iterator<Entry<String, Object>> i = params.entrySet().iterator();
        while (i.hasNext()) {
            Entry<String, Object> entry = i.next();
            result.append(entry.getKey());
            result.append("=");
            result.append(CmsEncoder.encode(entry.getValue().toString(), getCms().getRequestContext().getEncoding()));
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

        // resolve the macros
        return getMacroResolver().resolveMacros(input);
    }

    /**
     * Sends a http redirect to the specified URI in the OpenCms VFS.<p>
     *
     * @param location the location the response is redirected to
     * @throws IOException in case redirection fails
     */
    public void sendCmsRedirect(String location) throws IOException {

        // TOOD: IBM Websphere v5 has problems here, use forward instead (which has other problems)
        getJsp().getResponse().sendRedirect(OpenCms.getSystemInfo().getOpenCmsContext() + location);
    }

    /**
     * Forwards to the specified location in the OpenCms VFS.<p>
     *
     * @param location the location the response is redirected to
     * @param params the map of parameters to use for the forwarded request
     *
     * @throws IOException in case the forward fails
     * @throws ServletException in case the forward fails
     */
    public void sendForward(String location, Map<String, String[]> params) throws IOException, ServletException {

        setForwarded(true);
        // params must be arrays of String, ensure this is the case
        Map<String, String[]> parameters = CmsRequestUtil.createParameterMap(params);
        CmsRequestUtil.forwardRequest(
            getJsp().link(location),
            parameters,
            getJsp().getRequest(),
            getJsp().getResponse());
    }

    /**
     * Sets the forwarded flag.<p>
     *
     * @param forwarded the forwarded flag to set
     */
    public void setForwarded(boolean forwarded) {

        m_forwarded = forwarded;
    }

    /**
     * Get a localized short key value for the workplace.<p>
     *
     * @param keyName name of the key
     * @return a localized short key value
     */
    public String shortKey(String keyName) {

        String value = keyDefault(keyName + CmsMessages.KEY_SHORT_SUFFIX, (String)null);
        if (value == null) {
            // short key value not found, return "long" key value
            return key(keyName);
        }
        return value;
    }

    /**
     * Auxiliary method for initialization of messages.<p>
     *
     * @param messages the {@link CmsMessages} to add
     */
    protected void addMessages(CmsMessages messages) {

        if (messages != null) {
            m_messages.addMessages(messages);
        }
    }

    /**
     * Auxiliary method for initialization of messages.<p>
     *
     * @param bundleName the resource bundle name to add
     */
    protected void addMessages(String bundleName) {

        addMessages(new CmsMessages(bundleName, getLocale()));
    }

    /**
     * Returns the values of all parameter methods of this workplace class instance.<p>
     *
     * @return the values of all parameter methods of this workplace class instance
     */
    protected Map<String, Object> allParamValues() {

        List<Method> methods = paramGetMethods();
        Map<String, Object> map = new HashMap<String, Object>(methods.size());
        Iterator<Method> i = methods.iterator();
        while (i.hasNext()) {
            Method m = i.next();
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
     * Checks that the current user is a workplace user.<p>
     *
     * @throws CmsRoleViolationException if the user does not have the required role
     */
    protected void checkRole() throws CmsRoleViolationException {

        OpenCms.getRoleManager().checkRole(m_cms, CmsRole.WORKPLACE_USER);
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
    protected Map<String, String[]> getParameterMap() {

        return m_parameterMap;
    }

    /**
     * Initializes the message object.<p>
     *
     * By default the {@link CmsWorkplaceMessages} are initialized.<p>
     *
     * You SHOULD override this method for setting the bundles you really need,
     * using the <code>{@link #addMessages(CmsMessages)}</code> or <code>{@link #addMessages(String)}</code> method.<p>
     */
    protected void initMessages() {

        // no bundles are added by default as all core bundles are added as part of the WorkplaceModuleMessages
    }

    /**
     * Sets the users time warp if configured and if the current timewarp setting is different or
     * clears the current time warp setting if the user has no configured timewarp.<p>
     *
     * Timwarping is controlled by the session attribute
     * {@link CmsContextInfo#ATTRIBUTE_REQUEST_TIME} with a value of type <code>Long</code>.<p>
     *
     * @param settings the user settings which are configured via the preferences dialog
     *
     * @param session the session of the user
     */
    protected void initTimeWarp(CmsUserSettings settings, HttpSession session) {

        long timeWarpConf = settings.getTimeWarp();
        Long timeWarpSetLong = (Long)session.getAttribute(CmsContextInfo.ATTRIBUTE_REQUEST_TIME);
        long timeWarpSet = (timeWarpSetLong != null) ? timeWarpSetLong.longValue() : CmsContextInfo.CURRENT_TIME;

        if (timeWarpConf == CmsContextInfo.CURRENT_TIME) {
            // delete:
            if (timeWarpSetLong != null) {
                // we may come from direct_edit.jsp: don't remove attribute, this is
                session.removeAttribute(CmsContextInfo.ATTRIBUTE_REQUEST_TIME);
            }
        } else {
            // this is dominant: if configured we will use it
            if (timeWarpSet != timeWarpConf) {
                session.setAttribute(CmsContextInfo.ATTRIBUTE_REQUEST_TIME, Long.valueOf(timeWarpConf));
            }
        }
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
            initWorkplaceMembers(m_jsp.getCmsObject(), m_jsp.getRequest().getSession());
        }
    }

    /**
     * Initializes this workplace class instance.<p>
     *
     * @param cms the user context
     * @param session the session
     */
    protected void initWorkplaceMembers(CmsObject cms, HttpSession session) {

        m_cms = cms;
        m_session = session;
        // check role
        try {
            checkRole();
        } catch (CmsRoleViolationException e) {
            throw new CmsIllegalStateException(e.getMessageContainer(), e);
        }

        // get / create the workplace settings
        m_settings = (CmsWorkplaceSettings)m_session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);

        if (m_settings == null) {
            // create the settings object
            m_settings = new CmsWorkplaceSettings();
            m_settings = initWorkplaceSettings(m_cms, m_settings, false);

            storeSettings(m_session, m_settings);
        }

        // initialize messages
        CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(getLocale());
        // generate a new multi messages object and add the messages from the workplace
        m_messages = new CmsMultiMessages(getLocale());
        m_messages.addMessages(messages);
        initMessages();

        if (m_jsp != null) {
            // check request for changes in the workplace settings
            initWorkplaceRequestValues(m_settings, m_jsp.getRequest());
        }
        // set cms context accordingly
        initWorkplaceCmsContext(m_settings, m_cms);

        // timewarp reset logic
        initTimeWarp(m_settings.getUserSettings(), m_session);
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
    protected Map<String, Object> paramValues() {

        List<Method> methods = paramGetMethods();
        Map<String, Object> map = new HashMap<String, Object>(methods.size());
        Iterator<Method> i = methods.iterator();
        while (i.hasNext()) {
            Method m = i.next();
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
     * Replaces the site title, if necessary.<p>
     *
     * @param title the site title
     *
     * @return the new site title
     */
    protected String substituteSiteTitle(String title) {

        return substituteSiteTitleStatic(title, getSettings().getUserSettings().getLocale());
    }

    /**
     * Helper method to change back from the temporary project to the current project.<p>
     *
     * @throws CmsException if switching back fails
     */
    protected void switchToCurrentProject() throws CmsException {

        if (m_currentProjectId != null) {
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
    protected CmsUUID switchToTempProject() throws CmsException {

        // store the current project id in member variable
        m_currentProjectId = getSettings().getProject();
        CmsUUID tempProjectId = OpenCms.getWorkplaceManager().getTempFileProjectId();
        getCms().getRequestContext().setCurrentProject(getCms().readProject(tempProjectId));
        return tempProjectId;
    }

    /**
     * Sets the cms request context and other cms related settings to the
     * values stored in the workplace settings.<p>
     *
     * @param settings the workplace settings
     * @param cms the current cms object
     */
    private void initWorkplaceCmsContext(CmsWorkplaceSettings settings, CmsObject cms) {

        CmsRequestContext reqCont = cms.getRequestContext();

        // check project setting
        if (!settings.getProject().equals(reqCont.getCurrentProject().getUuid())) {
            try {
                reqCont.setCurrentProject(cms.readProject(settings.getProject()));
            } catch (CmsDbEntryNotFoundException e) {
                try {
                    // project not found, set current project and settings to online project
                    reqCont.setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
                    settings.setProject(CmsProject.ONLINE_PROJECT_ID);
                } catch (CmsException ex) {
                    // log error
                    if (LOG.isInfoEnabled()) {
                        LOG.info(ex.getLocalizedMessage());
                    }
                }
            } catch (CmsException e1) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(e1.getLocalizedMessage());
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
    private List<Method> paramGetMethods() {

        List<Method> list = new ArrayList<Method>();
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
    private List<Method> paramSetMethods() {

        List<Method> list = new ArrayList<Method>();
        Method[] methods = getClass().getMethods();
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