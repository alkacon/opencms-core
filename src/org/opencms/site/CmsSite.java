/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/site/CmsSite.java,v $
 * Date   : $Date: 2011/03/23 14:51:56 $
 * Version: $Revision: 1.36 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.site;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Describes a configured site in OpenCms.<p>
 *
 * @author  Alexander Kandzior 
 * @author  Jan Baudisch 
 *
 * @version $Revision: 1.36 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsSite implements Cloneable, Comparable {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSite.class);

    /** The aliases for this site, a vector of CmsSiteMatcher Objects. */
    private List m_aliases;

    /** If exclusive, and set to true will generate a 404 error, if set to false will redirect to secure url. */
    private boolean m_exclusiveError;

    /** If set to true, secure resources will only be available using the configured secure url. */
    private boolean m_exclusiveUrl;

    /** This value defines a relative sorting order. */
    private float m_position;

    /** The Url of the secure server. */
    private CmsSiteMatcher m_secureServer;

    /** The site matcher that describes the site. */
    private CmsSiteMatcher m_siteMatcher;

    /** Root directory of this site in the OpenCms VFS. */
    private String m_siteRoot;

    /** UUID of this site's root directory in the OpenCms VFS. */
    private CmsUUID m_siteRootUUID;

    /** Display title of this site. */
    private String m_title;

    /**
     * Constructs a new site object without title and id information,
     * this is to be used for lookup purposes only.<p>
     * 
     * @param siteRoot root directory of this site in the OpenCms VFS
     * @param siteMatcher the site matcher for this site
     */
    public CmsSite(String siteRoot, CmsSiteMatcher siteMatcher) {

        this(siteRoot, CmsUUID.getNullUUID(), siteRoot, siteMatcher, "");
    }

    /**
     * Constructs a new site object with a default (wildcard) a site matcher,
     * this is to be used for display purposes only.<p>
     * 
     * @param siteRoot root directory of this site in the OpenCms VFS
     * @param siteRootUUID UUID of this site's root directory in the OpenCms VFS
     * @param title display name of this site
     */
    public CmsSite(String siteRoot, CmsUUID siteRootUUID, String title) {

        this(siteRoot, siteRootUUID, title, CmsSiteMatcher.DEFAULT_MATCHER, "");
    }

    /**
     * Constructs a new site object.<p>
     * 
     * @param siteRoot root directory of this site in the OpenCms VFS
     * @param siteRootUUID UUID of this site's root directory in the OpenCms VFS
     * @param title display name of this site
     * @param siteMatcher the site matcher for this site
     * @param position the sorting position
     */
    public CmsSite(String siteRoot, CmsUUID siteRootUUID, String title, CmsSiteMatcher siteMatcher, String position) {

        setSiteRoot(siteRoot);
        setSiteRootUUID(siteRootUUID);
        setTitle(title);
        setSiteMatcher(siteMatcher);
        // init the position value
        m_position = Float.MAX_VALUE;
        try {
            m_position = Float.parseFloat(position);
        } catch (Throwable e) {
            // m_position will have Float.MAX_VALUE, so this site will appear last
        }
        m_aliases = new ArrayList();
    }

    /**
     * Returns a clone of this Objects instance.<p>
     * 
     * @return a clone of this instance
     */
    @Override
    public Object clone() {

        return new CmsSite(
            getSiteRoot(),
            (CmsUUID)getSiteRootUUID().clone(),
            getTitle(),
            (CmsSiteMatcher)getSiteMatcher().clone(),
            String.valueOf(getPosition()));
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object that) {

        if (that == this) {
            return 0;
        }
        if (that instanceof CmsSite) {
            float thatPos = ((CmsSite)that).getPosition();
            // please note: can't just subtract and cast to int here because of float precision loss
            if (m_position == thatPos) {
                if (m_position == Float.MAX_VALUE) {
                    // if they both do not have any position, sort by title
                    return m_title.compareTo(((CmsSite)that).getTitle());
                }
                return 0;
            }
            return (m_position < thatPos) ? -1 : 1;
        }
        return 0;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsSite) {
            ((CmsSite)obj).m_siteMatcher.equals(m_siteMatcher);
        }
        return false;
    }

    /**
     * Returns the aliases for this site.<p>
     * 
     * @return a ArrayList with the aliases
     */
    public List getAliases() {

        return m_aliases;
    }

    /**
     * Returns the sorting position.<p>
     *
     * @return the sorting position
     */
    public float getPosition() {

        return m_position;
    }
    
    /**
     * Returns the secure server url of this site root.<p>
     * 
     * @return the secure server url
     */
    public String getSecureUrl() {

        return m_secureServer.getUrl();
    }

    /**
     * Returns the server prefix for the given resource in this site, used to distinguish between 
     * secure (https) and non-secure (http) sites.<p>
     * 
     * This is required since a resource may have an individual "secure" setting using the property
     * {@link CmsPropertyDefinition#PROPERTY_SECURE}, which means this resource
     * must be delivered only using a secure protocol.<p>
     * 
     * The result will look like <code>http://site.enterprise.com:8080/</code> or <code>https://site.enterprise.com/</code>.<p> 
     * 
     * @param cms the current users OpenCms context
     * @param resource the resource to use
     * 
     * @return the server prefix for the given resource in this site
     * 
     * @see #getSecureUrl()
     * @see #getUrl()
     */
    public String getServerPrefix(CmsObject cms, CmsResource resource) {

        if (equals(OpenCms.getSiteManager().getDefaultSite())) {
            return OpenCms.getSiteManager().getWorkplaceServer();
        }
        boolean secure = false;
        if (hasSecureServer()) {
            try {
                secure = Boolean.valueOf(
                    cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_SECURE, true).getValue()).booleanValue();
            } catch (CmsException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return (secure ? getSecureUrl() : getUrl());
    }

    /**
     * Returns the server prefix for the given resource in this site, used to distinguish between 
     * secure (https) and non-secure (http) sites.<p>
     * 
     * This is required since a resource may have an individual "secure" setting using the property
     * {@link CmsPropertyDefinition#PROPERTY_SECURE}, which means this resource
     * must be delivered only using a secure protocol.<p>
     * 
     * The result will look like <code>http://site.enterprise.com:8080/</code> or <code>https://site.enterprise.com/</code>.<p> 
     * 
     * @param cms the current users OpenCms context
     * @param resourceName the resource name
     * 
     * @return the server prefix for the given resource in this site
     * 
     * @see #getSecureUrl()
     * @see #getUrl()
     */
    public String getServerPrefix(CmsObject cms, String resourceName) {

        if (equals(OpenCms.getSiteManager().getDefaultSite())) {
            return OpenCms.getSiteManager().getWorkplaceServer();
        }
        boolean secure = false;
        if (hasSecureServer()) {
            if (resourceName.startsWith(cms.getRequestContext().getSiteRoot())) {
                // make sure this can also be used with a resource root path
                resourceName = resourceName.substring(cms.getRequestContext().getSiteRoot().length());
            }
            try {
                secure = Boolean.valueOf(
                    cms.readPropertyObject(resourceName, CmsPropertyDefinition.PROPERTY_SECURE, true).getValue()).booleanValue();
            } catch (CmsException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return (secure ? getSecureUrl() : getUrl());
    }

    /**
     * Returns the site matcher that describes the URL of this site.<p>
     * 
     * @return the site matcher that describes the URL of this site
     */
    public CmsSiteMatcher getSiteMatcher() {

        return m_siteMatcher;
    }

    /**
     * Returns the site path for the given root path in case the root path
     * actually starts with this site root, or <code>null</code> in case 
     * the root path does not.<p> 
     *
     * @param rootPath the root path to get the site path for
     *
     * @return the site path for the given root path in case the root path
     *      actually starts with this site root, or <code>null</code> in case 
     *      the root path does not
     */
    public String getSitePath(String rootPath) {
        
        String result = null;
        if (CmsStringUtil.isNotEmpty(rootPath)) {
            if (rootPath.startsWith(m_siteRoot)) {
                result = rootPath.substring(m_siteRoot.length());
            }
        }        
        return result;        
    }

    /**
     * Returns the server URL prefix to which this site is mapped.<p>
     * 
     * @return the server URL prefix to which this site is mapped
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Returns the UUID of this site's root directory in the OpenCms VFS.<p>
     * 
     * @return the UUID of this site's root directory in the OpenCms VFS
     */
    public CmsUUID getSiteRootUUID() {

        return m_siteRootUUID;
    }

    /**
     * Returns the root directory of this site in the OpenCms VFS.<p>
     * 
     * @return the root directory of this site in the OpenCms VFS
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns the server url of this site root.<p>
     * 
     * @return the server url
     */
    public String getUrl() {

        return m_siteMatcher.getUrl();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_siteRootUUID.hashCode();
    }

    /**
     * Returns true, if the site has a secure server.<p>
     * 
     * @return true, if the site has a secure server
     */
    public boolean hasSecureServer() {

        return m_secureServer != null;
    }

    /**
     * Returns the exclusive error flag.<p>
     * 
     * @return <code>true</code> will generate a 404 error, 
     *      or <code>false</code> will redirect to secure url.
     */
    public boolean isExclusiveError() {

        return m_exclusiveError;
    }

    /**
     * Returns the exclusive protocol flag.<p>
     * 
     * @return <code>true</code> secure resources will only be available using the configured secure url, 
     *      or <code>false</code> if the uri (protocol + servername) does not really matter.
     */
    public boolean isExclusiveUrl() {

        return m_exclusiveUrl;
    }

    /**
     * Sets the exclusive error flag.<p>
     * 
     * @param error the exclusive error flag
     */
    public void setExclusiveError(boolean error) {

        m_exclusiveError = error;
    }

    /**
     * Sets the exclusive protocol flag.<p>
     * 
     * @param exclusive the exclusive protocol flag
     */
    public void setExclusiveUrl(boolean exclusive) {

        m_exclusiveUrl = exclusive;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer result = new StringBuffer(128);
        result.append("server: ");
        result.append(m_siteMatcher != null ? m_siteMatcher.toString() : "null");
        result.append(" uri: ");
        result.append(m_siteRoot);
        result.append(" title: ");
        result.append(m_title);
        return result.toString();
    }

    /**
     * Adds an alias for the site.<p>
     *      
     * @param aliasServer the sitematcher for the alias
     */
    protected void addAlias(CmsSiteMatcher aliasServer) {

        m_aliases.add(aliasServer);
    }

    /**
     * Sets the aliases for the site.<p>
     *      
     * @param aliases the aliases for the site
     */
    protected void setAliases(List aliases) {

        m_aliases = aliases;
    }

    /**
     * Sets the secure server.<p>
     * 
     * @param secureServer the sitematcher of the secure server
     */
    protected void setSecureServer(CmsSiteMatcher secureServer) {

        m_secureServer = secureServer;
    }

    /**
     * Sets the site matcher that describes the URL of this site.<p>
     * 
     * @param siteMatcher the site matcher that describes the URL of this site
     */
    protected void setSiteMatcher(CmsSiteMatcher siteMatcher) {

        m_siteMatcher = siteMatcher;
    }

    /**
     * Sets the server URL prefix to which this site is mapped.<p>
     * 
     * @param siteRoot the server URL prefix to which this site is mapped
     */
    protected void setSiteRoot(String siteRoot) {

        // site roots must never end with a "/"
        if (siteRoot.endsWith("/")) {
            m_siteRoot = siteRoot.substring(0, siteRoot.length() - 1);
        } else {
            m_siteRoot = siteRoot;
        }
    }

    /**
     * Sets the UUID of this site's root directory in the OpenCms VFS.<p>
     * 
     * @param siteRootUUID the UUID of this site's root directory in the OpenCms VFS
     */
    protected void setSiteRootUUID(CmsUUID siteRootUUID) {

        m_siteRootUUID = siteRootUUID;
    }

    /**
     * Sets the display title of this site in the OpenCms VFS.<p>
     * 
     * @param name the display title of this site in the OpenCms VFS
     */
    protected void setTitle(String name) {

        m_title = name;
    }
}