/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/site/CmsSite.java,v $
 * Date   : $Date: 2005/06/23 10:47:32 $
 * Version: $Revision: 1.21 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.site;

import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.ArrayList;

/**
 * Describes a configured site in OpenCms.<p>
 *
 * @author  Alexander Kandzior 
 * @author  Jan Baudisch 
 *
 * @version $Revision: 1.21 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsSite implements Cloneable {

    /** Name of the property to use for defining directories as site roots. */
    public static final String C_PROPERTY_SITE = "siteroot";

    /** The aliases for this site, a vector of CmsSiteMatcher Objects. */
    private List m_aliases;

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

        this(siteRoot, CmsUUID.getNullUUID(), siteRoot, siteMatcher);
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

        this(siteRoot, siteRootUUID, title, CmsSiteMatcher.DEFAULT_MATCHER);
    }

    /**
     * Constructs a new site object.<p>
     * 
     * @param siteRoot root directory of this site in the OpenCms VFS
     * @param siteRootUUID UUID of this site's root directory in the OpenCms VFS
     * @param title display name of this site
     * @param siteMatcher the site matcher for this site
     */
    public CmsSite(String siteRoot, CmsUUID siteRootUUID, String title, CmsSiteMatcher siteMatcher) {

        setSiteRoot(siteRoot);
        setSiteRootUUID(siteRootUUID);
        setTitle(title);
        setSiteMatcher(siteMatcher);
        m_aliases = new ArrayList();
    }

    /**
     * Hides the public default constructor.<p>
     */
    private CmsSite() {

        // NOOP
    }

    /**
     * Returns a clone of this Objects instance.<p>
     * 
     * @return a clone of this instance
     */
    public Object clone() {

        return new CmsSite(
            getSiteRoot(),
            (CmsUUID)getSiteRootUUID().clone(),
            getTitle(),
            (CmsSiteMatcher)getSiteMatcher().clone());
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
     * Returns the secure server url of this site root.<p>
     * 
     * @return the secure server url
     */
    public String getSecureUrl() {

        return m_secureServer.getUrl();
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
     * @see java.lang.Object#toString()
     */
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