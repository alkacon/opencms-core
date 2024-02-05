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

import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsStaticResourceHandler;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsStringUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;

/**
 * This class contains the data that should be cached for a specific service class.<p>
 *
 * We cache instances of this class rather than caching instances of {@link CmsGwtService} directly because
 * its superclass, {@link com.google.gwt.user.server.rpc.RemoteServiceServlet}, does some caching which we can't use because it doesn't
 * take the distinction between online and offline requests into account.
 *
 * @since 8.0.0
 *
 */
public class CmsGwtServiceContext implements I_CmsEventListener {

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGwtServiceContext.class);

    /** The name, which is used for debugging. */
    private String m_name;

    /** The serialization policy path. */
    private String m_serializationPolicyPath;

    /** The offline serialization policy. */
    private SerializationPolicy m_serPolicyOffline;

    /** The online serialization policy. */
    private SerializationPolicy m_serPolicyOnline;

    /**
     * Creates a new service context object.<p>
     *
     * @param name an identifier which is used for debugging
     */
    public CmsGwtServiceContext(String name) {

        m_name = name;

        // listen on VFS changes for serialization policies
        OpenCms.addCmsEventListener(
            this,
            new int[] {
                I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED,
                I_CmsEventListener.EVENT_RESOURCE_MODIFIED,
                I_CmsEventListener.EVENT_RESOURCES_MODIFIED,
                I_CmsEventListener.EVENT_RESOURCE_DELETED,
                I_CmsEventListener.EVENT_PUBLISH_PROJECT,
                I_CmsEventListener.EVENT_CLEAR_CACHES,
                I_CmsEventListener.EVENT_CLEAR_ONLINE_CACHES,
                I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES});

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
                Object change = event.getData().get(I_CmsEventListener.KEY_CHANGE);
                if ((change != null) && change.equals(Integer.valueOf(CmsDriverManager.NOTHING_CHANGED))) {
                    // skip lock & unlock
                    return;
                }
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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return super.toString() + "(" + m_name + ")";
    }

    /**
     * Returns the serialization policy for the service.<p>
     *
     * @param cms the current CMS context
     * @param moduleBaseURL the module's base URL
     * @param strongName the strong name of the service
     *
     * @return the serialization policy for the given service
     */
    protected SerializationPolicy getSerializationPolicy(CmsObject cms, String moduleBaseURL, String strongName) {

        if (m_serializationPolicyPath == null) {
            m_serializationPolicyPath = getSerializationPolicyPath(moduleBaseURL, strongName);
        }
        return getSerializationPolicy(cms);
    }

    /**
     * Finds the path of the serialization policy file.<p>
     *
     * @param moduleBaseURL the GWT module's base url
     * @param strongName the strong name of the service
     *
     * @return the serialization policy path
     */
    protected String getSerializationPolicyPath(String moduleBaseURL, String strongName) {

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
        return SerializationPolicyLoader.getSerializationPolicyFileName(modulePath + strongName);
    }

    /**
     * Returns the serialization policy, using lazy initialization.<p>
     *
     * @param cms the current cms context
     *
     * @return the serialization policy
     */
    private SerializationPolicy getSerializationPolicy(CmsObject cms) {

        boolean online = cms.getRequestContext().getCurrentProject().isOnlineProject();
        if (online && (m_serPolicyOnline != null)) {
            return m_serPolicyOnline;
        } else if (!online && (m_serPolicyOffline != null)) {
            return m_serPolicyOffline;
        }

        SerializationPolicy serializationPolicy = null;

        // Open the RPC resource file and read its contents
        InputStream is = null;
        try {
            // check if this is a static resource request
            if (m_serializationPolicyPath.startsWith(OpenCms.getSystemInfo().getStaticResourceContext())) {
                URL resourceURL = CmsStaticResourceHandler.getStaticResourceURL(m_serializationPolicyPath);
                URLConnection connection;
                connection = resourceURL.openConnection();
                is = connection.getInputStream();
            } else {
                // try reading from the RFS
                String rfsPath = m_serializationPolicyPath;
                if (rfsPath.startsWith(OpenCms.getSystemInfo().getContextPath())) {
                    rfsPath = rfsPath.substring(OpenCms.getSystemInfo().getContextPath().length());
                }
                rfsPath = CmsStringUtil.joinPaths(OpenCms.getSystemInfo().getWebApplicationRfsPath(), rfsPath);
                File policyFile = new File(rfsPath);
                if (policyFile.exists() && policyFile.canRead()) {
                    is = new FileInputStream(policyFile);
                } else {
                    // the file does not exist in the RFS, try the VFS
                    String policyPath = OpenCms.getLinkManager().getRootPath(cms, m_serializationPolicyPath);
                    is = new ByteArrayInputStream(cms.readFile(policyPath).getContents());
                }
            }
        } catch (Exception ex) {
            // most likely file not found
            String message = "ERROR: The serialization policy file '"
                + m_serializationPolicyPath
                + "' was not found; did you forget to include it in this deployment?";
            LOG.warn(message);
            LOG.warn(ex.getLocalizedMessage(), ex);

        }
        if (is == null) {
            return new CmsDummySerializationPolicy();
        }

        // read the policy
        try {
            serializationPolicy = SerializationPolicyLoader.loadFromStream(is, null);
        } catch (ParseException e) {
            LOG.error("ERROR: Failed to parse the policy file '" + m_serializationPolicyPath + "'", e);
        } catch (IOException e) {
            LOG.error("ERROR: Could not read the policy file '" + m_serializationPolicyPath + "'", e);
        } finally {
            try {
                is.close();
            } catch (@SuppressWarnings("unused") IOException e) {
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
