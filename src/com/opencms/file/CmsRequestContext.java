/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsRequestContext.java,v $
* Date   : $Date: 2002/02/05 09:07:02 $
* Version: $Revision: 1.47 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.file;

import java.util.*;
import javax.servlet.http.*;

import com.opencms.core.*;
import com.opencms.template.cache.*;

/**
 * This class provides access to the CmsRequestContext.
 * <br>
 * In the CmsRequestContext class are all methods bundled, which can inform about the
 * current request properties, like  the url or uri of the request.
 * <p>
 *
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Anders Fugmann
 * @author Alexander Lucas
 *
 * @version $Revision: 1.47 $ $Date: 2002/02/05 09:07:02 $
 *
 */
public class CmsRequestContext implements I_CmsConstants {

    /**
     * The rb to get access to the OpenCms.
     */
    private I_CmsResourceBroker m_rb;

    /**
     * The current CmsRequest.
     */
    private I_CmsRequest m_req;

    /**
     * The current CmsResponse.
     */
    private I_CmsResponse m_resp;

    /**
     * The current user.
     */
    private CmsUser m_user;

    /**
     * The current group of the user.
     */
    private CmsGroup m_currentGroup;

    /**
     * The current project.
     */
    private CmsProject m_currentProject;

    /**
     * Is this response streaming?
     */
    private boolean m_streaming = true;

    /**
     * In export mode the links in pages will be stored in this vector
     * for further processing.
     */
    private Vector m_links;

    /**
     * In export mode this vector is used to store all dependencies this request
     * may have. It is saved to the database and if one of the dependencies changes
     * the request will be exported again.
     */
    private Vector m_dependencies;

    /** Starting point for element cache */
    private CmsElementCache m_elementCache = null;

    /** Current languages */
    private Vector m_language = new Vector();

    /**
     * The name of the root, e.g. /site_a/vfs
     */
    private String m_siteRoot = C_DEFAULT_SITE+C_ROOTNAME_VFS;

    /**
     * The default constructor.
     *
     */
    public CmsRequestContext() {
        super();
    }

    /**
     * adds a link for the static export.
     */
    public void addLink(String link){
        m_links.add(link);
    }

    /**
     * returns all links that the templatemechanism has registered.
     */
    public Vector getLinkVector(){
        return m_links;
    }

    /**
     * adds a dependency.
     * @param dependency. The rootpath of the resource.
     */
    public void addDependency(String rootName){
        m_dependencies.add(rootName);
    }

    /**
     * returns all dependencies the templatemechanism has registered.
     */
    public Vector getDependencies(){
        return m_dependencies;
    }

    /**
     * Returns the current folder object.
     *
     * @return the current folder object.
     *
     * @exception CmsException if operation was not successful.
     */
    public CmsFolder currentFolder() throws CmsException {
        // truncate the filename from the pathinformation
        String folderName = getUri().substring(0, getUri().lastIndexOf("/") + 1);
        return (m_rb.readFolder(currentUser(), currentProject(), getSiteRoot(folderName), ""));
    }
    /**
     * Returns the current group of the current user.
     *
     * @return the current group of the current user.
     */
    public CmsGroup currentGroup() {
        return(m_currentGroup);
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
        return(m_user);
    }
   /**
    * Gets the name of the requested file without any path-information.
    *
    * @return the requested filename.
    */
    public String getFileUri() {
        String uri = m_req.getRequestedResource();
        uri=uri.substring(uri.lastIndexOf("/")+1);
        return uri;
    }
    /**
     * Gets the current request, if availaible.
     *
     * @return the current request, if availaible.
     */
    public I_CmsRequest getRequest() {
        return( m_req );
    }
    /**
     * Gets the current response, if availaible.
     *
     * @return the current response, if availaible.
     */
    public I_CmsResponse getResponse() {
        return( m_resp );
    }
    /**
     * Gets the Session for this request.
     * <br>
     * This method should be used instead of the originalRequest.getSession() method.
     * @param value indicates, if a session should be created when a session for the particular client does not already exist.
     * @return the CmsSession, or <code>null</code> if no session already exists and value was set to <code>false</code>
     *
     */
    public I_CmsSession getSession(boolean value) {
        HttpSession session = ((HttpServletRequest)m_req.getOriginalRequest()).getSession(value);
        if(session != null) {
            return (I_CmsSession) new CmsSession(session);
        } else {
            return null;
        }
    }
    /**
     * Gets the uri for the requested resource.
     * <p>
     * For a http request, the name of the resource is extracted as follows:<br>
     * <CODE>http://{servername}/{servletpath}/{path to the cms resource}</CODE><br>
     * In the following example:<br>
     * <CODE>http://my.work.server/servlet/opencms/system/def/explorer</CODE><br>
     * the requested resource is <CODE>/system/def/explorer</CODE>.
     * </P>
     *
     * @return the path to the requested resource.
     */
    public String getUri() {
        if( m_req != null ) {
            return( m_req.getRequestedResource() );
        } else {
            return( C_ROOT );
        }
    }
    /**
     * Initializes this RequestContext.
     *
     * @param req the CmsRequest.
     * @param resp the CmsResponse.
     * @param user the current user for this request.
     * @param currentGroup the current group for this request.
     * @param currentProjectId the id of the current project for this request.
     * @param streaming <code>true</code> if streaming should be enabled for this response, <code>false</code> otherwise.
     * @param elementCache Starting point for the element cache or <code>null</code> if the element cache should be disabled.
     *
     * @exception CmsException if operation was not successful.
     */
    void init(I_CmsResourceBroker rb, I_CmsRequest req, I_CmsResponse resp,
              String user, String currentGroup, int currentProjectId, boolean streaming, CmsElementCache elementCache)
        throws CmsException {
        m_rb = rb;
        m_req = req;
        m_resp = resp;
        m_links = new Vector();
        m_dependencies = new Vector();

        try {
            m_user = m_rb.readUser(null, null, user);
        } catch (CmsException ex){
        }
        // if no user found try to read webUser
        if (m_user == null) {
            m_user = m_rb.readWebUser(null, null, user);
        }

        // check, if the user is disabled
        if( m_user.getDisabled() == true ) {
            m_user = null;
        }

        // set current project, group and streaming proerties for this request
        try {
            setCurrentProject(currentProjectId);
        } catch(CmsException exc) {
            // there was a problem to set the needed project - using the online one
            setCurrentProject(I_CmsConstants.C_PROJECT_ONLINE_ID);
        }
        m_currentGroup = m_rb.readGroup(m_user, m_currentProject, currentGroup);
        m_streaming = streaming;
        m_elementCache = elementCache;

        // Analyze the user's preferred languages coming with the request
        if(req != null) {
            try{
                HttpServletRequest httpReq = (HttpServletRequest)req.getOriginalRequest();
                String accLangs = null;
                if(httpReq != null){
                    accLangs = httpReq.getHeader("Accept-Language");
                }
                if(accLangs != null) {
                    StringTokenizer toks = new StringTokenizer(accLangs, ",");
                    while(toks.hasMoreTokens()) {
                        // Loop through all languages and cut off trailing extensions
                        String current = toks.nextToken().trim();
                        if(current.indexOf("-") > -1) {
                            current = current.substring(0, current.indexOf("-"));
                        }
                        if(current.indexOf(";") > -1) {
                            current = current.substring(0, current.indexOf(";"));
                        }
                        m_language.addElement(current);

                    }
                }
            }catch(UnsupportedOperationException e){
            }
        }
    }
    /**
     * Determines if the users is in the admin-group.
     *
     * @return <code>true</code> if the users current group is the admin-group; <code>false</code> otherwise.
     *
     * @exception CmsException if operation was not successful.
     */
    public boolean isAdmin()
        throws CmsException {
        return( m_rb.isAdmin(m_user, m_currentProject) );
    }
    /**
     * Determines if the users current group is the projectmanager-group.
     * <BR>
     * All projectmanagers can create new projects, or close their own projects.
     *
     * @return <code>true</code> if the users current group is the projectleader-group; <code>false</code> otherwise.
     *
     * @exception CmsException if operation was not successful.
     */
    public  boolean isProjectManager()
        throws CmsException {
        return( m_rb.isProjectManager(m_user, m_currentProject) );
    }
    /**
     * Sets the current group of the current user.
     *
     * @param groupname the name of the group to be set as current group.
     *
     * @exception CmsException if operation was not successful.
     */
    public void setCurrentGroup(String groupname)
        throws CmsException {

        // is the user in that group?
        if(m_rb.userInGroup(m_user, m_currentProject, m_user.getName(), groupname)) {
            // Yes - set it to the current Group.
            m_currentGroup = m_rb.readGroup(m_user, m_currentProject, groupname);
        } else {
            // No - throw exception.
            throw new CmsException("[" + this.getClass().getName() + "] " + groupname,
                CmsException.C_NO_ACCESS);
        }
    }
    /**
     * Sets the current project for the user.
     *
     * @param projectId the id of the project to be set as current project.
     * @exception CmsException if operation was not successful.
     */
    public CmsProject setCurrentProject(int projectId)
        throws CmsException  {
        CmsProject newProject = m_rb.readProject(m_user,
                                                   m_currentProject,
                                                   projectId);
        if( newProject != null ) {
            m_currentProject = newProject;
        }
        return( m_currentProject );
    }

    /**
     * Get the current mode for HTTP streaming.
     *
     * @return <code>true</code> if template classes are allowed to stream the
     *    results to the response output stream theirselves, <code>false</code> otherwise.
     */
    public boolean isStreaming() {
        return m_streaming;
    }

    /**
     * Set the current mode for HTTP streaming.<p>
     * Calling this method is only allowed, if the response output stream was
     * not used before. Otherwise the streaming mode must not be changed.
     *
     * @param b <code>true</code> if template classes are allowed to stream the
     *    results to the response's output stream theirselves, <code>false</code> otherwise.
     * @exception CmsException if the output stream was already used previously.
     */
    public void setStreaming(boolean b) throws CmsException {
        if((m_streaming != b) && getResponse().isOutputWritten()) {
            throw new CmsException("[CmsRequestContext] Cannot switch streaming mode, if output stream is used previously.", CmsException.C_STREAMING_ERROR);
        }
        m_streaming = b;
    }

    /**
     * Get the current mode for element cache.
     *
     * @return <code>true</code> if element cache is active, <code>false</code> otherwise.
     */
    public boolean isElementCacheEnabled() {
        return (m_elementCache != null);
    }

    /**
     * Get the CmsElementCache object. This is the starting point for the element cache area.
     * @return CmsElementCachee
     */
    public CmsElementCache getElementCache() {
        return m_elementCache;
    }

    /**
     * Get a Vector of all accepted languages for this request.
     * Languages are coded in international shortcuts like "en" or "de".
     * If the browser has sent special versions of languages (e.g. "de-ch" for Swiss-German)
     * these extensions will be cut off.
     * @return Vector of Strings with language codes or <code>null</code> if no request object is available.
     */
    public Vector getAcceptedLanguages() {
        return m_language;
    }

    /**
     * Returns the name of the current site root, e.g. /default/vfs
     *
     * @param resourcename
     * @return String The resourcename with its site root
     */
    public String getSiteRoot(String resourcename){
        if(resourcename.startsWith("///")){
            return resourcename.substring(2);
        } else if (resourcename.startsWith("//")){
            return C_DEFAULT_SITE+resourcename.substring(1);
        } else {
            return m_siteRoot+resourcename;
        }
    }

    /**
     * Returns the name of the current site, e.g. /default
     *
     * @return String The site name
     */
    public String getSiteName(){
        return C_DEFAULT_SITE;
    }

    /**
     * Sets the name of the current site root
     * of the virtual file system
     */
    public void setContextTo(String name){
        m_siteRoot = C_DEFAULT_SITE+name;
    }
}
