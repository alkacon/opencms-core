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

package org.opencms.file;

import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.util.CmsResourceTranslator;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

/**
 * Stores the information about the current users OpenCms context,
 * for example the requested URI, the current project, the selected site and more.<p>
 *
 * @since 6.0.0
 */
public final class CmsRequestContext {

    /** Request context attribute for the ADE context path (should be a root path). */
    public static final String ATTRIBUTE_ADE_CONTEXT_PATH = CmsRequestContext.class.getName() + ".ADE_CONTEXT_PATH";

    /** Request context attribute for indicating that an editor is currently open. */
    public static final String ATTRIBUTE_EDITOR = CmsRequestContext.class.getName() + ".ATTRIBUTE_EDITOR";

    /** Request context attribute for indicating we want full links generated for HTML fields. */
    public static final String ATTRIBUTE_FULLLINKS = CmsRequestContext.class.getName() + ".ATTRIBUTE_FULLLINKS";

    /** Request context attribute for indicating the model file for a create resource operation. */
    public static final String ATTRIBUTE_MODEL = CmsRequestContext.class.getName() + ".ATTRIBUTE_MODEL";

    /** Request context attribute for indicating content locale for a create resource operation. */
    public static final String ATTRIBUTE_NEW_RESOURCE_LOCALE = CmsRequestContext.class.getName()
        + ".NEW_RESOURCE_LOCALE";

    /** A map for storing (optional) request context attributes. */
    private Map<String, Object> m_attributeMap;

    /** The current project. */
    private CmsProject m_currentProject;

    /** The detail content resource (possibly null). */
    private CmsResource m_detailResource;

    /** Directory name translator. */
    private CmsResourceTranslator m_directoryTranslator;

    /** Current encoding. */
    private String m_encoding;

    /** File name translator. */
    private CmsResourceTranslator m_fileTranslator;

    /** Flag to control whether links should be absolute even if we're linking to the current site. */
    private boolean m_forceAbsoluteLinks;

    /** The secure request flag. */
    private boolean m_isSecureRequest;

    /** The locale used by this request context. */
    private Locale m_locale;

    /** The fully qualified name of the organizational unit for this request. */
    private String m_ouFqn;

    /** The remote ip address. */
    private String m_remoteAddr;

    /** the matcher for the current request, that is the host part of the URI from the original http request. */
    private CmsSiteMatcher m_requestMatcher;

    /** The current request time. */
    private long m_requestTime;

    /** The name of the root, e.g. /site_a/vfs. */
    private String m_siteRoot;

    /** Flag to indicate that this context should not update the user session. */
    private boolean m_updateSession;

    /** The URI for getUri() in case it is "overwritten".  */
    private String m_uri;

    /** The current user. */
    private CmsUser m_user;

    /**
     * Constructs a new request context.<p>
     *
     * @param user the current user
     * @param project the current project
     * @param requestedUri the requested OpenCms VFS URI
     * @param requestMatcher the matcher for the current request, that is the host part of the URI from the original http request
     * @param siteRoot the users current site root
     * @param isSecureRequest true if this is a secure request
     * @param locale the users current locale
     * @param encoding the encoding to use for this request
     * @param remoteAddr the remote IP address of the user
     * @param requestTime the time of the request (used for resource publication / expiration date)
     * @param directoryTranslator the directory translator
     * @param fileTranslator the file translator
     * @param ouFqn the fully qualified name of the organizational unit
     * @param forceAbsoluteLinks if true, links should be generated with a server prefix even if we're linking to the current site
     */
    public CmsRequestContext(
        CmsUser user,
        CmsProject project,
        String requestedUri,
        CmsSiteMatcher requestMatcher,
        String siteRoot,
        boolean isSecureRequest,
        Locale locale,
        String encoding,
        String remoteAddr,
        long requestTime,
        CmsResourceTranslator directoryTranslator,
        CmsResourceTranslator fileTranslator,
        String ouFqn,
        boolean forceAbsoluteLinks) {

        m_updateSession = true;
        m_user = user;
        m_currentProject = project;
        m_uri = requestedUri;
        m_requestMatcher = requestMatcher;
        m_isSecureRequest = isSecureRequest;
        setSiteRoot(siteRoot);
        m_locale = locale;
        m_encoding = encoding;
        m_remoteAddr = remoteAddr;
        m_requestTime = requestTime;
        m_directoryTranslator = directoryTranslator;
        m_fileTranslator = fileTranslator;
        setOuFqn(ouFqn);
        m_forceAbsoluteLinks = forceAbsoluteLinks;
    }

    /**
     * Returns the adjusted site root for a resource using the provided site root as a base.<p>
     *
     * Usually, this would be the site root for the current site.
     * However, if a resource from the <code>/system/</code> folder is requested,
     * this will be the empty String.<p>
     *
     * @param siteRoot the site root of the current site
     * @param resourcename the resource name to get the adjusted site root for
     *
     * @return the adjusted site root for the resource
     */
    public static String getAdjustedSiteRoot(String siteRoot, String resourcename) {

        if (resourcename.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)
            || OpenCms.getSiteManager().startsWithShared(resourcename)
            || (resourcename.startsWith(CmsWorkplace.VFS_PATH_SITES) && !resourcename.startsWith(siteRoot))) {
            return "";
        } else {
            return siteRoot;
        }
    }

    /**
     * Adds the current site root of this context to the given resource name,
     * and also translates the resource name with the configured the directory translator.<p>
     *
     * @param resourcename the resource name
     * @return the translated resource name including site root
     * @see #addSiteRoot(String, String)
     */
    public String addSiteRoot(String resourcename) {

        return addSiteRoot(m_siteRoot, resourcename);
    }

    /**
     * Adds the given site root of this context to the given resource name,
     * taking into account special folders like "/system" where no site root must be added,
     * and also translates the resource name with the configured the directory translator.<p>
     *
     * @param siteRoot the site root to add
     * @param resourcename the resource name
     * @return the translated resource name including site root
     */
    public String addSiteRoot(String siteRoot, String resourcename) {

        if ((resourcename == null) || (siteRoot == null)) {
            return null;
        }
        siteRoot = getAdjustedSiteRoot(siteRoot, resourcename);
        StringBuffer result = new StringBuffer(128);
        result.append(siteRoot);
        if (((siteRoot.length() == 0) || (siteRoot.charAt(siteRoot.length() - 1) != '/'))
            && ((resourcename.length() == 0) || (resourcename.charAt(0) != '/'))) {
            // add slash between site root and resource if required
            result.append('/');
        }
        result.append(resourcename);
        return m_directoryTranslator.translateResource(result.toString());
    }

    /**
     * Returns the current project of the current user.
     *
     * @return the current project of the current user
     *
     * @deprecated use {@link #getCurrentProject()} instead
     */
    @Deprecated
    public CmsProject currentProject() {

        return getCurrentProject();
    }

    /**
     * Returns the current user object.<p>
     *
     * @return the current user object
     *
     * @deprecated use {@link #getCurrentUser()} instead
     */
    @Deprecated
    public CmsUser currentUser() {

        return getCurrentUser();
    }

    /**
     * Returns the adjusted site root for a resource this context current site root.<p>
     *
     * @param resourcename the resource name to get the adjusted site root for
     *
     * @return the adjusted site root for the resource
     *
     * @see #getAdjustedSiteRoot(String, String)
     */
    public String getAdjustedSiteRoot(String resourcename) {

        return getAdjustedSiteRoot(m_siteRoot, resourcename);
    }

    /**
     * Gets the value of an attribute from the OpenCms request context attribute list.<p>
     *
     * @param attributeName the attribute name
     * @return Object the attribute value, or <code>null</code> if the attribute was not found
     */
    public Object getAttribute(String attributeName) {

        if (m_attributeMap == null) {
            return null;
        }
        return m_attributeMap.get(attributeName);
    }

    /**
     * Returns the current project of the current user.
     *
     * @return the current project of the current user
     */
    public CmsProject getCurrentProject() {

        return m_currentProject;
    }

    /**
     * Returns the current user object.<p>
     *
     * @return the current user object
     */
    public CmsUser getCurrentUser() {

        return m_user;
    }

    /**
     * Gets the detail content structure id (or null if no detail content has been loaded).<p>
     *
     * @return the detail content id
     */
    public CmsUUID getDetailContentId() {

        if (m_detailResource == null) {
            return null;
        }
        return m_detailResource.getStructureId();
    }

    /**
     * Gets the detail content resource (or null if no detail content has been loaded).<p>
     *
     * @return the detail content resource
     */
    public CmsResource getDetailResource() {

        return m_detailResource;
    }

    /**
     * Returns the directory name translator this context was initialized with.<p>
     *
     * The directory translator is used to translate old VFS path information
     * to a new location. Example: <code>/bodys/index.html --> /system/bodies/</code>.<p>
     *
     * @return the directory name translator this context was initialized with
     */
    public CmsResourceTranslator getDirectoryTranslator() {

        return m_directoryTranslator;
    }

    /**
     * Returns the current content encoding to be used in HTTP response.<p>
     *
     * @return the encoding
     */
    public String getEncoding() {

        return m_encoding;
    }

    /**
     * Returns the file name translator this context was initialized with.<p>
     *
     * The file name translator is used to translate filenames from uploaded files
     * to valid OpenCms filenames. Example: <code>W&uuml;ste W&ouml;rter.doc --> Wueste_Woerter.doc</code>.<p>
     *
     * @return the file name translator this context was initialized with
     */
    public CmsResourceTranslator getFileTranslator() {

        return m_fileTranslator;
    }

    /**
     * Gets the name of the parent folder of the requested file.<p>
     *
     * @return the name of the parent folder of the requested file
     */
    public String getFolderUri() {

        return CmsResource.getFolderPath(m_uri);
    }

    /**
     * Returns the locale used by this request context.<p>
     *
     * In normal operation, the request context locale is initialized using
     * {@link org.opencms.i18n.I_CmsLocaleHandler#getI18nInfo(javax.servlet.http.HttpServletRequest, CmsUser, CmsProject, String)}
     * depending on the requested resource URI.<p>
     *
     * @return the locale used by this request context
     *
     * @see org.opencms.i18n.I_CmsLocaleHandler#getI18nInfo(javax.servlet.http.HttpServletRequest, CmsUser, CmsProject, String)
     * @see org.opencms.i18n.CmsLocaleManager#getDefaultLocale(CmsObject, String)
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the fully qualified name of the organizational unit.<p>
     *
     * @return the fully qualified name of the organizational unit
     */
    public String getOuFqn() {

        return m_ouFqn;
    }

    /**
     * Returns the remote ip address.<p>
     *
     * @return the remote ip address as string
     */
    public String getRemoteAddress() {

        return m_remoteAddr;
    }

    /**
     * Returns the matcher for the current request, that is the host part of the URI from the original http request.<p>
     *
     * @return the matcher for the current request, that is the host part of the URI from the original http request
     */
    public CmsSiteMatcher getRequestMatcher() {

        return m_requestMatcher;
    }

    /**
     * Returns the current request time.<p>
     *
     * @return the current request time
     */
    public long getRequestTime() {

        return m_requestTime;
    }

    /**
     * Returns this request contexts uri extended with the current site root path.<p>
     *
     * @return this request contexts uri extended with the current site root path
     *
     * @see #getUri()
     * @see #addSiteRoot(String)
     */
    public String getRootUri() {

        return addSiteRoot(m_siteRoot, m_uri);
    }

    /**
     * Adjusts the absolute resource root path for the current site.<p>
     *
     * The full root path of a resource is always available using
     * <code>{@link CmsResource#getRootPath()}</code>. From this name this method cuts
     * of the current site root using
     * <code>{@link CmsRequestContext#removeSiteRoot(String)}</code>.<p>
     *
     * If the resource root path does not start with the current site root,
     * it is left untouched.<p>
     *
     * @param resource the resource to get the adjusted site root path for
     *
     * @return the absolute resource path adjusted for the current site
     *
     * @see #removeSiteRoot(String)
     * @see CmsResource#getRootPath()
     * @see CmsObject#getSitePath(CmsResource)
     */
    public String getSitePath(CmsResource resource) {

        return removeSiteRoot(resource.getRootPath());
    }

    /**
     * Returns the current root directory in the virtual file system.<p>
     *
     * @return the current root directory in the virtual file system
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Returns the OpenCms VFS URI of the requested resource.<p>
     *
     * @return the OpenCms VFS URI of the requested resource
     */
    public String getUri() {

        return m_uri;
    }

    /**
     * Returns true if links to the current site should be generated as absolute links, i.e. with a server prefix.
     *
     * @return true if links to the current site should be absolute
     */
    public boolean isForceAbsoluteLinks() {

        return m_forceAbsoluteLinks;
    }

    /**
     * Checks if we are currently either in the Online project or the 'direct edit disabled' mode.
     *
     * @return true if we are online or in 'direct edit enabled' mode
     */
    public boolean isOnlineOrEditDisabled() {

        if (getCurrentProject().isOnlineProject()) {
            return true;
        }
        Object directEdit = getAttribute(CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT);
        if (Boolean.TRUE.equals(directEdit)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if this is a secure request.<p>
     *
     * @return true if this is secure
     */
    public boolean isSecureRequest() {

        return m_isSecureRequest;
    }

    /**
     * Check if this request context will update the session.<p>
     *
     * This is used mainly for CmsReports that continue to use the
     * users context, even after the http request is already finished.<p>
     *
     * @return true if this request context will update the session, false otherwise
     */
    public boolean isUpdateSessionEnabled() {

        return m_updateSession;
    }

    /**
     * Removes an attribute from the request context.<p>
     *
     * @param key the name of the attribute to remove
     *
     * @return the removed attribute, or <code>null</code> if no attribute was set with this name
     */
    public Object removeAttribute(String key) {

        if (m_attributeMap != null) {
            return m_attributeMap.remove(key);
        }
        return null;
    }

    /**
     * Removes the current site root prefix from the absolute path in the resource name,
     * that is adjusts the resource name for the current site root.<p>
     *
     * If the resource name does not start with the current site root,
     * it is left untouched.<p>
     *
     * @param resourcename the resource name
     *
     * @return the resource name adjusted for the current site root
     *
     * @see #getSitePath(CmsResource)
     */
    public String removeSiteRoot(String resourcename) {

        String siteRoot = getAdjustedSiteRoot(m_siteRoot, resourcename);
        if ((siteRoot == m_siteRoot)
            && resourcename.startsWith(siteRoot)
            && ((resourcename.length() == siteRoot.length()) || (resourcename.charAt(siteRoot.length()) == '/'))) {
            resourcename = resourcename.substring(siteRoot.length());
        }
        if (resourcename.length() == 0) {
            // input was a site root folder without trailing slash
            resourcename = "/";
        }
        return resourcename;
    }

    /**
     * Sets an attribute in the request context.<p>
     *
     * @param key the attribute name
     * @param value the attribute value
     */
    public void setAttribute(String key, Object value) {

        if (m_attributeMap == null) {
            // hash table is still the most efficient form of a synchronized Map
            m_attributeMap = new Hashtable<String, Object>();
        }
        m_attributeMap.put(key, value);
    }

    /**
     * Sets the current project for the user.<p>
     *
     * @param project the project to be set as current project
     *
     * @return the CmsProject instance
     */
    public CmsProject setCurrentProject(CmsProject project) {

        if (project != null) {
            m_currentProject = project;
        }
        return m_currentProject;
    }

    /**
     * Sets the detail content resource.<p>
     *
     * @param detailResource the detail content resource
     */
    public void setDetailResource(CmsResource detailResource) {

        m_detailResource = detailResource;
    }

    /**
     * Sets the current content encoding to be used in HTTP response.<p>
     *
     * @param encoding the encoding
     */
    public void setEncoding(String encoding) {

        m_encoding = encoding;
    }

    /**
     * Enables/disables link generation with full server prefix for the current site.
     *
     * @param forceAbsoluteLinks true if links to the current site should be generated with server prefix
     */
    public void setForceAbsoluteLinks(boolean forceAbsoluteLinks) {

        m_forceAbsoluteLinks = forceAbsoluteLinks;
    }

    /**
     * Sets the locale used by this request context.<p>
     *
     * @param locale the locale to set
     *
     * @see #getLocale() for more information about how the locale is set in normal operation
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * Sets the organizational unit fully qualified name.<p>
     *
     * @param ouFqn the organizational unit fully qualified name
     */
    public void setOuFqn(String ouFqn) {

        String userOu = CmsOrganizationalUnit.getParentFqn(m_user.getName());
        if (ouFqn != null) {
            if (ouFqn.startsWith(userOu)
                || (ouFqn.startsWith(CmsOrganizationalUnit.SEPARATOR) && ouFqn.substring(1).startsWith(userOu))) {
                m_ouFqn = ouFqn;
            } else {
                throw new CmsIllegalArgumentException(
                    Messages.get().container(Messages.ERR_BAD_ORGUNIT_2, ouFqn, userOu));
            }
        } else {
            m_ouFqn = userOu;
        }
        m_ouFqn = CmsOrganizationalUnit.removeLeadingSeparator(m_ouFqn);
    }

    /**
     * Sets the current request time.<p>
     *
     * @param time the request time
     */
    public void setRequestTime(long time) {

        m_requestTime = time;
    }

    /**
     * Sets the 'secure request' status.<p>
     *
     * @param secureRequest the new value
     */
    public void setSecureRequest(boolean secureRequest) {

        m_isSecureRequest = secureRequest;
    }

    /**
     * Sets the current root directory in the virtual file system.<p>
     *
     * @param root the name of the new root directory
     */
    public void setSiteRoot(String root) {

        // site roots must never end with a "/"
        if (root.endsWith("/")) {
            m_siteRoot = root.substring(0, root.length() - 1);
        } else {
            m_siteRoot = root;
        }
    }

    /**
     * Mark this request context to update the session or not.<p>
     *
     * @param value true if this request context will update the session, false otherwise
     */
    public void setUpdateSessionEnabled(boolean value) {

        m_updateSession = value;
    }

    /**
     * Set the requested resource OpenCms VFS URI, that is the value returned by {@link #getUri()}.<p>
     *
     * Use this with caution! Many things (caches etc.) depend on this value.
     * If you change this value, better make sure that you change it only temporarily
     * and reset it in a <code>try { // do something // } finally { // reset URI // }</code> statement.<p>
     *
     * @param value the value to set the Uri to, must be a complete OpenCms path name like /system/workplace/style.css
     */
    public void setUri(String value) {

        m_uri = value;
    }

    /**
     * Switches the user in the context, required after a login.<p>
     *
     * @param user the new user to use
     * @param project the new users current project
     * @param ouFqn the organizational unit
     */
    protected void switchUser(CmsUser user, CmsProject project, String ouFqn) {

        m_user = user;
        m_currentProject = project;
        setOuFqn(ouFqn);
    }
}