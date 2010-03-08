/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/gwt/CmsGwtService.java,v $
 * Date   : $Date: 2010/03/08 14:43:50 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.xml.sitemap.Messages;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;

/**
 * Wrapper for GWT services served through OpenCms.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public class CmsGwtService extends RemoteServiceServlet implements I_CmsEventListener {

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGwtService.class);

    /** Serialization id. */
    private static final long serialVersionUID = 8119684308154724518L;

    /** The current CMS context. */
    private ThreadLocal<CmsObject> m_perThreadCmsObject;

    /** The serialization policy path. */
    private String m_serializationPolicyPath;

    /** The offline serialization policy. */
    private SerializationPolicy m_serPolicyOffline;

    /** The online serialization policy. */
    private SerializationPolicy m_serPolicyOnline;

    /**
     * Checks the permissions of the current user to match the required security level.<p> 
     * 
     * Note that the current request and response are not available yet.<p>
     * 
     * Override if needed.<p>
     * 
     * @param cms the current cms object 
     * 
     * @throws CmsRoleViolationException if the security level can not be satisfied
     */
    public void checkPermissions(CmsObject cms) throws CmsRoleViolationException {

        OpenCms.getRoleManager().checkRole(cms, CmsRole.WORKPLACE_USER);
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        CmsResource resource = null;
        List<CmsResource> resources = null;

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED:
            case I_CmsEventListener.EVENT_RESOURCE_MODIFIED:
                // a resource has been modified in a way that it *IS NOT* necessary also to clear 
                // lists of cached sub-resources where the specified resource might be contained inside.
                resource = (CmsResource)event.getData().get(I_CmsEventListener.KEY_RESOURCE);
                uncacheResource(resource);
                break;

            case I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED:
                // a list of resources and all of their properties have been modified
                resources = CmsCollectionsGenericWrapper.list(event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                uncacheResources(resources);
                break;

            case I_CmsEventListener.EVENT_RESOURCE_MOVED:
            case I_CmsEventListener.EVENT_RESOURCE_DELETED:
            case I_CmsEventListener.EVENT_RESOURCES_MODIFIED:
                // a list of resources has been modified
                resources = CmsCollectionsGenericWrapper.list(event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                uncacheResources(resources);
                break;

            case I_CmsEventListener.EVENT_CLEAR_ONLINE_CACHES:
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                m_serPolicyOnline = null;
                break;

            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                m_serPolicyOnline = null;
                m_serPolicyOffline = null;
                break;

            case I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES:
                m_serPolicyOffline = null;
                break;

            default:
                // noop
                break;
        }
    }

    /**
     * Returns the current cms context.<p>
     *
     * @return the current cms context
     */
    public CmsObject getCmsObject() {

        return m_perThreadCmsObject.get();
    }

    /**
     * Returns the current request.<p>
     * 
     * @return the current request
     * 
     * @see #getThreadLocalRequest()
     */
    public HttpServletRequest getRequest() {

        return getThreadLocalRequest();
    }

    /**
     * Returns the current response.<p>
     * 
     * @return the current response
     * 
     * @see #getThreadLocalResponse()
     */
    public HttpServletResponse getResponse() {

        return getThreadLocalResponse();
    }

    /**
     * @see javax.servlet.GenericServlet#log(java.lang.String)
     */
    @Override
    public void log(String msg) {

        super.log(msg);
        // also log to opencms.log
        LOG.info(msg);
    }

    /**
     * @see javax.servlet.GenericServlet#log(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void log(String message, Throwable t) {

        super.log(message, t);
        // also log to opencms.log
        LOG.info(message, t);
    }

    /**
     * Sets the current cms context.<p>
     *
     * @param cms the current cms context to set
     */
    public synchronized void setCms(CmsObject cms) {

        if (m_perThreadCmsObject == null) {
            m_perThreadCmsObject = new ThreadLocal<CmsObject>();
        }
        m_perThreadCmsObject.set(cms);
    }

    /**
     * We do not want that the server goes to fetch files from the servlet context.<p>
     * 
     * @see com.google.gwt.user.server.rpc.RemoteServiceServlet#doGetSerializationPolicy(javax.servlet.http.HttpServletRequest, java.lang.String, java.lang.String)
     */
    @Override
    protected SerializationPolicy doGetSerializationPolicy(
        HttpServletRequest request,
        String moduleBaseURL,
        String strongName) {

        if (m_serializationPolicyPath == null) {
            // locate the serialization policy file in OpenCms
            String modulePath = null;
            try {
                modulePath = new URL(moduleBaseURL).getPath();
            } catch (MalformedURLException ex) {
                // moduleBaseUrl is bad
                LOG.error(ex.getLocalizedMessage(), ex);
                return null;
            } catch (NullPointerException ex) {
                // moduleBaseUrl is null
                LOG.error(ex.getLocalizedMessage(), ex);
                return null;
            }
            String serializationPolicyUrl = SerializationPolicyLoader.getSerializationPolicyFileName(modulePath
                + strongName);
            m_serializationPolicyPath = OpenCms.getLinkManager().getRootPath(getCmsObject(), serializationPolicyUrl);
        }

        return getSerializationPolicy();
    }

    /**
     * Returns the serialization policy, using lazy initialization.<p>
     * 
     * @return the serialization policy 
     */
    private SerializationPolicy getSerializationPolicy() {

        boolean online = getCmsObject().getRequestContext().currentProject().isOnlineProject();
        if (online && (m_serPolicyOnline != null)) {
            return m_serPolicyOnline;
        } else if (!online && (m_serPolicyOffline != null)) {
            return m_serPolicyOffline;
        }

        SerializationPolicy serializationPolicy = null;

        // Open the RPC resource file and read its contents
        InputStream is;
        try {
            is = new ByteArrayInputStream(getCmsObject().readFile(m_serializationPolicyPath).getContents());
        } catch (CmsException ex) {
            // most likely file not found
            String message = "ERROR: The serialization policy file '"
                + m_serializationPolicyPath
                + "' was not found; did you forget to include it in this deployment?";
            this.log(message);
            LOG.error(ex.getLocalizedMessage(), ex);
            return null;
        }

        // read the policy
        try {
            serializationPolicy = SerializationPolicyLoader.loadFromStream(is, null);
        } catch (ParseException e) {
            this.log("ERROR: Failed to parse the policy file '" + m_serializationPolicyPath + "'", e);
        } catch (IOException e) {
            this.log("ERROR: Could not read the policy file '" + m_serializationPolicyPath + "'", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Ignore this error
            }
        }

        if (online) {
            m_serPolicyOnline = serializationPolicy;
        } else {
            m_serPolicyOffline = serializationPolicy;
        }
        return serializationPolicy;
    }

    /**
     * Removes a cached resource from the cache.<p>
     * 
     * @param resource the resource
     */
    private void uncacheResource(CmsResource resource) {

        if (resource == null) {
            LOG.warn(Messages.get().container(Messages.LOG_WARN_UNCACHE_NULL_0));
            return;
        }
        if ((m_serializationPolicyPath != null) && resource.getRootPath().equals(m_serializationPolicyPath)) {
            m_serPolicyOffline = null;
        }
    }

    /**
     * Removes a bunch of cached resources from the cache.<p>
     * 
     * @param resources a list of resources
     * 
     * @see #uncacheResource(CmsResource)
     */
    private void uncacheResources(List<CmsResource> resources) {

        if (resources == null) {
            return;
        }
        for (int i = 0, n = resources.size(); i < n; i++) {
            // remove the resource
            uncacheResource(resources.get(i));
        }
    }
}
