/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsRequestContext.java,v $
 * Date   : $Date: 2004/08/10 15:46:18 $
 * Version: $Revision: 1.13 $
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

package org.opencms.file;

import org.opencms.util.CmsResourceTranslator;
import org.opencms.workplace.I_CmsWpConstants;

import java.util.HashMap;
import java.util.Locale;

/**
 * Stores the information about the current request context,
 * like ths current user, the site root, the encoding and other stuff.<p>  
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 *
 * @version $Revision: 1.13 $
 */
public class CmsRequestContext {

    /** Request context attribute for indicating that an editor is currently open. */
    public static final String ATTRIBUTE_EDITOR = "org.opencms.file.CmsRequestContext.ATTRIBUTE_EDITOR";

    /** A map for storing (optional) request context attributes. */
    private HashMap m_attributeMap;

    /** The current project. */
    private CmsProject m_currentProject;

    /** Directory name translator. */
    private CmsResourceTranslator m_directoryTranslator;

    /** Current encoding. */
    private String m_encoding;

    /** File name translator. */
    private CmsResourceTranslator m_fileTranslator;

    /** The locale for this request. */
    private Locale m_locale;

    /** The remote ip address. */
    private String m_remoteAddr;

    /** The current request time. */
    private long m_requestTime;

    /** Used to save / restore a site root .*/
    private String m_savedSiteRoot;

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
     * @param project the current users project
     * @param requestedUri the requested OpenCms VFS URI
     * @param siteRoot the users current site root
     * @param locale the users current locale 
     * @param encoding the encoding to use for this request
     * @param remoteAddr the remote IP address of the user
     * @param directoryTranslator the directory translator
     * @param fileTranslator the file translator
     */
    public CmsRequestContext(
        CmsUser user,
        CmsProject project,
        String requestedUri,
        String siteRoot,
        Locale locale,
        String encoding,
        String remoteAddr,
        CmsResourceTranslator directoryTranslator,
        CmsResourceTranslator fileTranslator) {

        m_updateSession = true;
        m_user = user;
        m_currentProject = project;
        m_uri = requestedUri;
        setSiteRoot(siteRoot);
        m_locale = locale;
        m_encoding = encoding;
        m_remoteAddr = remoteAddr;
        m_directoryTranslator = directoryTranslator;
        m_fileTranslator = fileTranslator;
        m_requestTime = System.currentTimeMillis();
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
        if (((siteRoot.length() == 0) || (siteRoot.charAt(siteRoot.length()-1) != '/'))
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
     * @return the current project of the current user.
     */
    public CmsProject currentProject() {

        return m_currentProject;
    }

    /**
     * Returns the current user object.<p>
     *
     * @return the current user object
     */
    public CmsUser currentUser() {

        return m_user;
    }

    /**
     * Returns the adjusted site root for a resoure this context current site root.<p>
     * 
     * @param resourcename the resource name to get the adjusted site root for
     * @return the adjusted site root for the resoure
     * @see #getAdjustedSiteRoot(String, String)
     */
    public String getAdjustedSiteRoot(String resourcename) {

        return getAdjustedSiteRoot(m_siteRoot, resourcename);
    }

    /**
     * Returns the adjusted site root for a resoure using the provided site root as a base.<p>
     * 
     * Usually, this would be the site root for the current site.
     * However, if a resource from the /system/ folder is requested,
     * this will be the empty String.<p>
     * 
     * @param siteRoot the site root to use
     * @param resourcename the resource name to get the adjusted site root for
     * @return the adjusted site root for the resoure
     */
    public String getAdjustedSiteRoot(String siteRoot, String resourcename) {

        if (resourcename.startsWith(I_CmsWpConstants.C_VFS_PATH_SYSTEM)) {
            return "";
        } else {
            return siteRoot;
        }
    }

    /**
     * Gets the value of an attribute from the OpenCms request context attribute list.<p>
     * 
     * @param attributeName the attribute name
     * @return Object the attribute value, or <code>null</code> if the attribute was not found
     */
    public synchronized Object getAttribute(String attributeName) {

        if (m_attributeMap == null) {
            return null;
        }
        return m_attributeMap.get(attributeName);
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
     * to valid OpenCms filenames. Example: <code>Wüste Wörter.doc --> Wueste_Woerter.doc</code>.<p>
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

        return getUri().substring(0, getUri().lastIndexOf("/") + 1);
    }

    /**
     * Returns the name of the requested locale within this context.<p>
     * 
     * @return the name of the locale
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the remote ip address.<p>
     * 
     * @return the renote ip addresss as string
     */
    public String getRemoteAddress() {

        return m_remoteAddr;
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
     * Adjusts the absolute resource root path for the current site.<p> 
     * 
     * The full root path of a resource is always available using
     * {@link CmsResource#getRootPath()}. From this name this method cuts 
     * of the current site root using 
     * {@link CmsRequestContext#removeSiteRoot(String)}.<p>
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
     * Removes the current site root prefix from the absolute path in the resource name,
     * that is adjusts the resource name for the current site root.<p> 
     * 
     * @param resourcename the resource name
     * 
     * @return the resource name adjusted for the current site root
     * 
     * @see #getSitePath(CmsResource)
     */
    public String removeSiteRoot(String resourcename) {

        String siteRoot = getAdjustedSiteRoot(resourcename);
        if ((siteRoot.length() > 0) && resourcename.startsWith(siteRoot)) {
            resourcename = resourcename.substring(siteRoot.length());
        }
        return resourcename;
    }

    /**
     * Restores the saved site root.<p>
     *
     * @throws RuntimeException in case there is no site root saved
     */
    public synchronized void restoreSiteRoot() throws RuntimeException {

        if (m_savedSiteRoot == null) {
            throw new RuntimeException("Saved siteroot empty!");
        }
        m_siteRoot = m_savedSiteRoot;
        m_savedSiteRoot = null;
    }

    /**
     * Saves the current site root.<p>
     *
     * @throws RuntimeException in case there is already a site root saved
     */
    public synchronized void saveSiteRoot() throws RuntimeException {

        if (m_savedSiteRoot != null) {
            throw new RuntimeException("Saved siteroot not empty: " + m_savedSiteRoot);
        }
        m_savedSiteRoot = m_siteRoot;
    }

    /**
     * Sets an attribute in the request context.<p>
     * 
     * @param key the attribute name
     * @param value the attribute value
     */
    public synchronized void setAttribute(String key, Object value) {

        if (m_attributeMap == null) {
            m_attributeMap = new HashMap();
        }
        m_attributeMap.put(key, value);
    }

    /**
     * Sets the current project for the user.<p>
     *
     * @param project the project to be set as current project
     * @return the CmsProject instance
     */
    public CmsProject setCurrentProject(CmsProject project) {

        if (project != null) {
            m_currentProject = project;
        }
        return m_currentProject;
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
     * Sets the current request time.<p>
     * 
     * @param time the request time
     */
    public void setRequestTime(long time) {

        m_requestTime = time;
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
     * and reset it in a <code>try { // do something // } finaly { // reset URI // }</code> statement.<p>
     * 
     * @param value the value to set the Uri to, must be a complete OpenCms path name like /system/workplace/stlye.css
     * @since 5.0 beta 1
     */
    public void setUri(String value) {

        m_uri = value;
    }

    /**
     * Switches the user in the context, required after a login.<p>
     * 
     * @param user the new user to use
     * @param project the new users current project
     */
    protected void switchUser(CmsUser user, CmsProject project) {

        m_user = user;
        m_currentProject = project;
    }
}