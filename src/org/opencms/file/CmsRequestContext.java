/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsRequestContext.java,v $
 * Date   : $Date: 2004/02/16 15:43:17 $
 * Version: $Revision: 1.4 $
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

import org.opencms.db.CmsDriverManager;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsResourceTranslator;
import org.opencms.workplace.I_CmsWpConstants;

import com.opencms.core.CmsSession;
import com.opencms.core.I_CmsRequest;
import com.opencms.core.I_CmsResponse;
import com.opencms.core.I_CmsSession;

import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Stores the information about the current request context,
 * like ths current user, the site root, the encoding and other stuff.<p>  
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 *
 * @version $Revision: 1.4 $
 */
public class CmsRequestContext {

    /** Request parameter to force locale selection */
    public static final String C_PARAMETER_LOCALE = "_locale";    
    
    /** A map for storing (optional) request context attributes */
    private HashMap m_attributeMap; 

    /** The current project */
    private CmsProject m_currentProject;
    
    /** Directroy name translator */
    private CmsResourceTranslator m_directoryTranslator;
    
    /** The rb to get access to the OpenCms */
    private CmsDriverManager m_driverManager;

    /** Current encoding */
    private String m_encoding;

    /** Flag to indicate that this request is event controlled */
    private boolean m_eventControlled;

    /** File name translator */
    private CmsResourceTranslator m_fileTranslator;
    
    /** The remote ip address */
    String m_remoteAddr;

    /** The current CmsRequest */
    private I_CmsRequest m_req;

    /** The current CmsResponse */
    private I_CmsResponse m_resp;
    
    /** Used to save / restore a site root */
    private String m_savedSiteRoot;

    /** The name of the root, e.g. /site_a/vfs */
    private String m_siteRoot;
    
    /** Flag to indicate that this context should not update the user session */
    private boolean m_updateSession;

    /** The URI for getUri() in case it is "overwritten"  */
    private String m_uri = null;

    /** The current user */
    private CmsUser m_user;

    /** The locale for this request */
    private Locale m_locale;
    
    /**
     * The default constructor.
     */
    public CmsRequestContext() {
        m_eventControlled = false;
        m_updateSession = true;
        m_savedSiteRoot = null;
        if ((OpenCms.getSiteManager() != null) && (OpenCms.getSiteManager().getDefaultSite() != null)) { 
            m_siteRoot = OpenCms.getSiteManager().getDefaultSite().getSiteRoot();
        } else {
            setSiteRoot("/");
        }
    }
    
    /**
     * Returns the name of a resource with the complete site root name,
     * (e.g. /default/vfs/index.html) by adding the currently set site root prefix.<p>
     *
     * @param resourcename the resource name
     * @return the resource name including site root
     */
    public String addSiteRoot(String resourcename) {
        if (resourcename == null) {
            return null;
        }
        String siteRoot = getAdjustedSiteRoot(resourcename);        
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
     * Returns the current folder object.
     *
     * @return the current folder object.
     * @throws CmsException if operation was not successful.
     */
    public CmsFolder currentFolder() throws CmsException {
        return m_driverManager.readFolder(this, addSiteRoot(getFolderUri()));
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
     * Returns the current user object.
     *
     * @return the current user object.
     */
    public CmsUser currentUser() {
        return (m_user);
    } 

    /**
     * Returns the adjusted site root for a resoure.<p>
     * 
     * Usually, this would be the site root for the current site.
     * However, if a resource from the /system/ folder is requested,
     * this will be the empty String.<p>
     * 
     * @param resourcename the resource name to get the adjusted site root for
     * @return the adjusted site root for a resoure
     */
    public String getAdjustedSiteRoot(String resourcename) {
        if (resourcename.startsWith(I_CmsWpConstants.C_VFS_PATH_SYSTEM)) {
            return "";
        } else {
            return m_siteRoot;
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
     * @return The directory name translator this context was initialized with
     */
    public CmsResourceTranslator getDirectoryTranslator() {
        return m_directoryTranslator;
    }

    /**
     * Returns the current content encoding to be used in HTTP response
     * 
     * @return the encoding
     */
    public String getEncoding() {
        return m_encoding;
    }
        
    /**
     * @return The file name translator this context was initialized with
     */
    public CmsResourceTranslator getFileTranslator() {
        return m_fileTranslator;
    }    
    
    /**
     * Gets the name of the requested file without any path-information.
     *
     * @return the requested filename.
     */
    public String getFileUri() {
        String uri = m_req.getRequestedResource();
        uri = uri.substring(uri.lastIndexOf("/") + 1);
        return uri;
    }
    
   /**
    * Gets the name of the parent folder of the requested file
    *
    * @return the requested filename.
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
        if (m_remoteAddr == null) {
            try {
                if ((m_req != null) && (m_req.getOriginalRequestType() == I_CmsConstants.C_REQUEST_HTTP)) {
                    m_remoteAddr = ((HttpServletRequest)m_req.getOriginalRequest()).getHeader(I_CmsConstants.C_REQUEST_FORWARDED_FOR);
                    if (m_remoteAddr == null) {
                        m_remoteAddr = ((HttpServletRequest)m_req.getOriginalRequest()).getRemoteAddr();
                    }
                }
            } catch (Throwable t) {
                // This will happen only in very rare circumstances
                m_remoteAddr = I_CmsConstants.C_IP_LOCALHOST;
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn("Error reading remote ip address in request context", t);
                }
            }
        }
        return m_remoteAddr;
    }
    
    /**
     * Gets the current request, if availaible.
     *
     * @return the current request, if availaible.
     */
    public I_CmsRequest getRequest() {
        return (m_req);
    }
    
    /**
     * Gets the current response, if availaible.
     *
     * @return the current response, if availaible.
     */
    public I_CmsResponse getResponse() {
        return (m_resp);
    }
    
    /**
     * Gets the Session for this request.<p>
     *
     * This method should be used instead of the originalRequest.getSession() method.
     * 
     * @param value indicates, if a session should be created when a session for the particular client does not already exist.
     * @return the CmsSession, or <code>null</code> if no session already exists and value was set to <code>false</code>
     *
     */
    public I_CmsSession getSession(boolean value) {
        HttpSession session =
            ((HttpServletRequest) m_req.getOriginalRequest()).getSession(value);
        if (session != null) {
            return (I_CmsSession) new CmsSession(session);
        } else {
            return null;
        }
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
     * Gets the uri for the requested resource.<p>
     * 
     * For a http request, the name of the resource is extracted as follows:<br>
     * <CODE>http://{servername}/{servletpath}/{path to the cms resource}</CODE><br>
     * In the following example:<br>
     * <CODE>http://my.work.server/servlet/opencms/system/def/explorer</CODE><br>
     * the requested resource is <CODE>/system/def/explorer</CODE>.
     *
     * @return the path to the requested resource.
     */
    public String getUri() {
        if (m_uri != null) {
            return m_uri;
        }
        if (m_req != null) {
            return m_req.getRequestedResource();
        } else {
            return I_CmsConstants.C_ROOT;
        }
    }
    
    /**
     * Initializes this RequestContext.<p>
     *
     * @param driverManager the driver manager
     * @param req the CmsRequest
     * @param resp the CmsResponse
     * @param user the current user for this request
     * @param projectId the id of the current project for this request
     * @param site the current site root
     * @param directoryTranslator Translator for directories (file with full path)
     * @param fileTranslator Translator for new file names (without path)
     * @throws CmsException if operation was not successful.
     */
    public void init (
        CmsDriverManager driverManager, 
        I_CmsRequest req, 
        I_CmsResponse resp, 
        String user, 
        int projectId, 
        String site, 
        CmsResourceTranslator directoryTranslator, 
        CmsResourceTranslator fileTranslator
    ) throws CmsException {

        m_driverManager = driverManager;
        m_req = req;
        m_resp = resp;
        setSiteRoot(site);
        
        //CmsProject project = null;
        
        try {
            m_user = m_driverManager.readUser(user);
        } catch (CmsException ex) {
            // ignore
        }
        // if no user found try to read webUser
        if (m_user == null) {
            m_user = m_driverManager.readWebUser(user);
        }

        // check, if the user is disabled
        if (m_user.getDisabled()) {
            m_user = null;
        }

        // set current project, group and streaming proerties for this request
        try {
            setCurrentProject(projectId);
        } catch (CmsException exc) {
            // there was a problem to set the needed project - using the online one
            setCurrentProject(I_CmsConstants.C_PROJECT_ONLINE_ID);
        }

        m_directoryTranslator = directoryTranslator;
        m_fileTranslator = fileTranslator;

        if (req != null) {          

            // Initialize locale
            initLocale();
            
            // Initialize encoding 
            initEncoding();
        }
    }
        
    /**
     * Detects current content encoding to be used in HTTP response
     * based on requested resource or session state.
     */
    public void initEncoding() {
        try {
            m_encoding = m_driverManager.readProperty(this, addSiteRoot(m_req.getRequestedResource()), getAdjustedSiteRoot(m_req.getRequestedResource()), I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, true);
        } catch (CmsException e) {
            m_encoding = null;
        }
        if ((m_encoding != null) && ! "".equals(m_encoding)) {
            // encoding was read from resource property
            return;
        } else if (getUri().startsWith(I_CmsWpConstants.C_VFS_PATH_SYSTEM)) {
            // try to get encoding from session for special system folder only                
            if (OpenCms.getLog(this).isDebugEnabled()) {                                
                OpenCms.getLog(this).debug("Can't get encoding property for resource "
                    + m_req.getRequestedResource() + ", trying to get it from session.");
            }                    
            I_CmsSession session = getSession(false);
            if (session != null) {
                m_encoding = (String)session.getValue(I_CmsConstants.C_SESSION_CONTENT_ENCODING);
            }
        }
        if (m_encoding == null || "".equals(m_encoding)) {
            // no encoding found - use default one
            if (OpenCms.getLog(this).isDebugEnabled()) {                                
                OpenCms.getLog(this).debug("No encoding found - using default: " + OpenCms.getSystemInfo().getDefaultEncoding());
            }                  
            m_encoding = OpenCms.getSystemInfo().getDefaultEncoding();
        }
    }
    
    /**
     * Initializes the locale name for this request context.<p>
     */
    public void initLocale() {
        CmsLocaleManager localeManager = OpenCms.getLocaleManager();
        if (localeManager != null) {
            // locale manager is initialized
            String locale;
            if ((m_req != null) && ((locale = m_req.getParameter(C_PARAMETER_LOCALE)) != null)) {  
                // "_locale" parameter found in request
                m_locale = CmsLocaleManager.getLocale(locale);
            } else if (getUri().startsWith(I_CmsWpConstants.C_VFS_PATH_WORKPLACE)
                    || getUri().startsWith(I_CmsWpConstants.C_VFS_PATH_LOGIN)) {
                // the workplace/login requires a special locale handler
                m_locale = OpenCms.getWorkplaceManager().getLocale(this);
            } else {
                // request for resource outside of workplace, use default handler
                m_locale = localeManager.getLocaleHandler().getLocale(this);
            }
                        
            if (m_locale == null) {
                m_locale = OpenCms.getLocaleManager().getDefaultLocale();
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("No locale found - using default: " + m_locale);
                }
            }
        } else {
            // locale manager not initialized, this will be true _only_ during system startup
            // the value set does not matter, no locale information form VFS is used on system startup
            // this is just to protect against null pointer exceptions
            m_locale = Locale.ENGLISH;
        }
    }

    /**
     * Determines if the users is in the admin-group.
     *
     * @return <code>true</code> if the users current group is the admin-group; <code>false</code> otherwise.
     * @throws CmsException if operation was not successful.
     */
    public boolean isAdmin() throws CmsException {
        return (m_driverManager.isAdmin(this));
    }

    /**
     * Check if this request context is event controlled.<p>
     * 
     * @return true if the request context is event controlled, false otherwise
     */
    public boolean isEventControlled() {
        return m_eventControlled;
    }

    /**
     * Determines if the users current group is the projectmanager-group.<p>
     * 
     * All projectmanagers can create new projects, or close their own projects.
     *
     * @return <code>true</code> if the users current group is the projectleader-group; <code>false</code> otherwise.
     * @throws CmsException if operation was not successful.
     */
    public boolean isProjectManager() throws CmsException {
        return (m_driverManager.isProjectManager(this));
    }

    /**
     * Check if this request context will update the session.<p>
     *
     * @return true if this request context will update the session, false otherwise
     */
    public boolean isUpdateSessionEnabled() {
        return m_updateSession;
    }   
    
    /**
     * Removes the current site root prefix from the absolute path in the resource name,
     * i.e. adjusts the resource name for the current site root.<p> 
     * 
     * @param resourcename the resource name
     * @return the resource name adjusted for the current site root
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
    public synchronized void restoreSiteRoot() {
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
    public synchronized void saveSiteRoot() {
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
     * @param projectId the id of the project to be set as current project
     * @return the CmsProject instance
     * @throws CmsException if operation was not successful
     */
    public CmsProject setCurrentProject(int projectId) throws CmsException {
        CmsProject newProject = m_driverManager.readProject(projectId);
        if (newProject != null) {
            m_currentProject = newProject;
        }
        return (m_currentProject);
    }

    /**
     * Sets the current content encoding to be used in HTTP response
     * 
     * @param encoding the encoding
     */
    public void setEncoding(String encoding) {
        setEncoding(encoding, false);
    }

    /**
     * Sets the current content encoding to be used in HTTP response
     * and store it in session if it is available
     * 
     * @param encoding the encoding
     * @param storeInSession flag to store the encoding
     */
    public void setEncoding(String encoding, boolean storeInSession) {
        m_encoding = encoding;
        if (!storeInSession) {
            return;
        }
        I_CmsSession session = getSession(false);
        if (session != null) {
            session.putValue(
                I_CmsConstants.C_SESSION_CONTENT_ENCODING,
                m_encoding);
        }
    }

    /**
     * Mark this request context as event controlled.<p>
     * 
     * @param value true if the request is event controlled, false otherwise
     */
    public void setEventControlled(boolean value) {
        m_eventControlled = value;
    }

    /**
     * Sets the current root directory in the virtual file system.<p>
     * 
     * @param root the name of the new root directory
     */
    public void setSiteRoot(String root) {
        // site roots must never end with a "/"
        if (root.endsWith("/")) {
            m_siteRoot = root.substring(0, root.length()-1);
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
     * Set the value that is returned by getUri()
     * to the provided String.<p>
     * 
     * This is required in a context where
     * a cascade of included XMLTemplates are combined with JSP or other
     * Templates that use the ResourceLoader interface.
     * You need to fake the URI because the ElementCache always
     * uses cms.getRequestContext().getUri().
     *
     * @param value The value to set the Uri to, must be a complete OpenCms path name like /system/workplace/stlye.css
     * @since 5.0 beta 1
     */
    public void setUri(String value) {
        m_uri = value;
    }
}
