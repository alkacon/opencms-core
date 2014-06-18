/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
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

package org.opencms.workplace.tools.sites;

import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog object for a single site.<p>
 * 
 * @since 9.0.0
 */
public class CmsSiteBean {

    /** The aliases. */
    private List<String> m_aliases = new ArrayList<String>();

    /** The URI used as error page. */
    private String m_errorPage;

    /** The exclusive flag. */
    private boolean m_exclusiveError;

    /** The exclusive URL. */
    private boolean m_exclusiveUrl;

    /** The favicon. */
    private String m_favicon;

    /** The original site. */
    private CmsSite m_originalSite;

    /** The port (e.g. 8080) which is required to access this site. */
    private int m_port;

    /** The position. */
    private float m_position;

    /** The secure server. */
    private boolean m_secureServer;

    /** The secure URL. */
    private String m_secureUrl;

    /** The servers URL. */
    private String m_server;

    /** The host name (e.g. localhost) which is required to access this site. */
    private String m_serverName;

    /** The servers protocol (e.g. "http", "https") which is required to access this site. */
    private String m_serverProtocol;

    /** The site root of this site. */
    private String m_siteRoot;

    /** The offset time in ms. */
    private long m_timeOffset;

    /** The title of this site. */
    private String m_title;

    /** Indicates whether this site should be considered when writing the web server configuration. */
    private boolean m_webserver = true;

    /**
     * Default constructor.<p>
     */
    public CmsSiteBean() {

        // noop
    }

    /**
     * Public constructor with a CmsSite as parameter.<p>
     * 
     * @param site the site
     */
    public CmsSiteBean(CmsSite site) {

        m_siteRoot = site.getSiteRoot();
        if (site.getSiteMatcher() != null) {
            m_originalSite = site;
            m_title = site.getTitle();
            m_server = site.getUrl();
            CmsSiteMatcher matcher = new CmsSiteMatcher(site.getUrl(), site.getSiteMatcher().getTimeOffset());
            m_serverProtocol = matcher.getServerProtocol();
            m_serverName = matcher.getServerName();
            m_port = matcher.getServerPort();
            m_timeOffset = matcher.getTimeOffset();
            m_secureServer = site.hasSecureServer();
            if (site.hasSecureServer()) {
                m_secureUrl = site.getSecureUrl();
                m_exclusiveUrl = site.isExclusiveUrl();
                m_exclusiveError = site.isExclusiveError();
            }
            for (CmsSiteMatcher aMatcher : site.getAliases()) {
                if ((aMatcher != null) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(aMatcher.getUrl())) {
                    m_aliases.add(aMatcher.getUrl());
                }
            }
            m_position = site.getPosition();
            m_errorPage = site.getErrorPage();
            m_webserver = site.isWebserver();
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CmsSiteBean other = (CmsSiteBean)obj;
        if (m_server == null) {
            if (other.m_server != null) {
                return false;
            }
        } else if (!m_server.equals(other.m_server)) {
            return false;
        }
        if (m_siteRoot == null) {
            if (other.m_siteRoot != null) {
                return false;
            }
        } else if (!m_siteRoot.equals(other.m_siteRoot)) {
            return false;
        }
        if (m_title == null) {
            if (other.m_title != null) {
                return false;
            }
        } else if (!m_title.equals(other.m_title)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the aliases.<p>
     *
     * @return the aliases
     */
    public List<String> getAliases() {

        return m_aliases;
    }

    /**
     * Returns the errorPage.<p>
     *
     * @return the errorPage
     */
    public String getErrorPage() {

        return m_errorPage;
    }

    /**
     * Returns the favicon.<p>
     *
     * @return the favicon
     */
    public String getFavicon() {

        return m_favicon;
    }

    /**
     * Returns the originalSite.<p>
     *
     * @return the originalSite
     */
    public CmsSite getOriginalSite() {

        return m_originalSite;
    }

    /**
     * Returns the port.<p>
     *
     * @return the port
     */
    public int getPort() {

        return m_port;
    }

    /**
     * Returns the position.<p>
     *
     * @return the position
     */
    public float getPosition() {

        return m_position;
    }

    /**
     * Returns the secureUrl.<p>
     *
     * @return the secureUrl
     */
    public String getSecureUrl() {

        return m_secureUrl;
    }

    /**
     * Returns the server.<p>
     *
     * @return the server
     */
    public String getServer() {

        return m_server;
    }

    /**
     * Returns the serverName.<p>
     *
     * @return the serverName
     */
    public String getServerName() {

        return m_serverName;
    }

    /**
     * Returns the serverProtocol.<p>
     *
     * @return the serverProtocol
     */
    public String getServerProtocol() {

        return m_serverProtocol;
    }

    /**
     * Returns the siteRoot.<p>
     *
     * @return the siteRoot
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Returns the timeOffset.<p>
     *
     * @return the timeOffset
     */
    public long getTimeOffset() {

        return m_timeOffset;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((m_server == null) ? 0 : m_server.hashCode());
        result = (prime * result) + ((m_siteRoot == null) ? 0 : m_siteRoot.hashCode());
        result = (prime * result) + ((m_title == null) ? 0 : m_title.hashCode());
        return result;
    }

    /**
     * Returns the secureServer.<p>
     *
     * @return the secureServer
     */
    public boolean hasSecureServer() {

        return m_secureServer;
    }

    /**
     * Returns the exclusiveError.<p>
     *
     * @return the exclusiveError
     */
    public boolean isExclusiveError() {

        return m_exclusiveError;
    }

    /**
     * Returns the exclusiveUrl.<p>
     *
     * @return the exclusiveUrl
     */
    public boolean isExclusiveUrl() {

        return m_exclusiveUrl;
    }

    /**
     * Returns the secureServer.<p>
     *
     * @return the secureServer
     */
    public boolean isSecureServer() {

        return m_secureServer;
    }

    /**
     * Returns the web server.<p>
     *
     * @return the web server
     */
    public boolean isWebserver() {

        return m_webserver;
    }

    /**
     * Sets the aliases.<p>
     *
     * @param aliases the aliases to set
     */
    public void setAliases(List<String> aliases) {

        m_aliases = aliases;
    }

    /**
     * Sets the errorPage.<p>
     *
     * @param errorPage the errorPage to set
     */
    public void setErrorPage(String errorPage) {

        m_errorPage = errorPage;
    }

    /**
     * Sets the exclusiveError.<p>
     *
     * @param exclusiveError the exclusiveError to set
     */
    public void setExclusiveError(boolean exclusiveError) {

        m_exclusiveError = exclusiveError;
    }

    /**
     * Sets the exclusiveUrl.<p>
     *
     * @param exclusiveUrl the exclusiveUrl to set
     */
    public void setExclusiveUrl(boolean exclusiveUrl) {

        m_exclusiveUrl = exclusiveUrl;
    }

    /**
     * Sets the favicon.<p>
     *
     * @param favicon the favicon to set
     */
    public void setFavicon(String favicon) {

        m_favicon = favicon;
    }

    /**
     * Sets the port.<p>
     *
     * @param port the port to set
     */
    public void setPort(int port) {

        m_port = port;
    }

    /**
     * Sets the position.<p>
     *
     * @param position the position to set
     */
    public void setPosition(float position) {

        m_position = position;
    }

    /**
     * Sets the secureServer.<p>
     *
     * @param secureServer the secureServer to set
     */
    public void setSecureServer(boolean secureServer) {

        m_secureServer = secureServer;
    }

    /**
     * Sets the secureUrl.<p>
     *
     * @param secureUrl the secureUrl to set
     */
    public void setSecureUrl(String secureUrl) {

        m_secureUrl = secureUrl;
    }

    /**
     * Sets the server.<p>
     *
     * @param server the server to set
     */
    public void setServer(String server) {

        m_server = server;
    }

    /**
     * Sets the serverName.<p>
     *
     * @param serverName the serverName to set
     */
    public void setServerName(String serverName) {

        m_serverName = serverName;
    }

    /**
     * Sets the serverProtocol.<p>
     *
     * @param serverProtocol the serverProtocol to set
     */
    public void setServerProtocol(String serverProtocol) {

        m_serverProtocol = serverProtocol;
    }

    /**
     * Sets the siteRoot.<p>
     *
     * @param siteRoot the siteRoot to set
     */
    public void setSiteRoot(String siteRoot) {

        m_siteRoot = siteRoot;
    }

    /**
     * Sets the timeOffset.<p>
     *
     * @param timeOffset the timeOffset to set
     */
    public void setTimeOffset(long timeOffset) {

        m_timeOffset = timeOffset;
    }

    /**
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }

    /**
     * Sets the web server.<p>
     *
     * @param webserver the web server to set
     */
    public void setWebserver(boolean webserver) {

        m_webserver = webserver;
    }

    /**
     * Creates a new site object based on the members.<p>
     * 
     * @return a new site object based on the members
     */
    public CmsSite toCmsSite() {

        m_siteRoot = m_siteRoot.endsWith("/") ? m_siteRoot.substring(0, m_siteRoot.length() - 1) : m_siteRoot;
        CmsSiteMatcher matcher = CmsStringUtil.isNotEmpty(m_secureUrl) ? new CmsSiteMatcher(m_secureUrl) : null;
        CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(m_siteRoot);
        CmsUUID uuid = new CmsUUID();
        if ((site != null) && (site.getSiteMatcher() != null)) {
            uuid = (CmsUUID)site.getSiteRootUUID().clone();
        }
        String errorPage = CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_errorPage) ? m_errorPage : null;
        List<CmsSiteMatcher> aliases = new ArrayList<CmsSiteMatcher>();
        for (String alias : m_aliases) {
            aliases.add(new CmsSiteMatcher(alias));
        }
        return new CmsSite(
            m_siteRoot,
            uuid,
            m_title,
            new CmsSiteMatcher(m_server),
            String.valueOf(m_position),
            errorPage,
            matcher,
            m_exclusiveUrl,
            m_exclusiveError,
            m_webserver,
            aliases);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "Site [m_siteRoot=" + m_siteRoot + ", m_title=" + m_title + ", m_server=" + m_server + "]";
    }
}
