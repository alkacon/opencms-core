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
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog object for a single site.<p>
 */
public class CmsSiteDialogObject {

    private List<CmsSiteMatcher> m_aliases = new ArrayList<CmsSiteMatcher>();

    private boolean m_exclusiveError;

    private boolean m_exclusiveUrl;

    /** The port (e.g. 8080) which is required to access this site. */
    private int m_port;

    private boolean m_secureServer;

    private String m_secureUrl;

    /** The servers URL. */
    private String m_server;

    /** The hostname (e.g. localhost) which is required to access this site. */
    private String m_serverName;

    /** The servers protocol (e.g. "http", "https") which is required to access this site. */
    private String m_serverProtocol;

    /** The site root of this site. */
    private String m_siteRoot;

    /** The offset time in ms. */
    private long m_timeOffset;

    /** The title of this site. */
    private String m_title;

    private CmsSite m_originalSite;

    /**
     * Returns the originalSite.<p>
     *
     * @return the originalSite
     */
    public CmsSite getOriginalSite() {

        return m_originalSite;
    }

    /**
     * Default constructor.<p>
     */
    public CmsSiteDialogObject() {

        // noop
    }

    /**
     * Public constructor with a CmsSite as parameter.<p>
     * 
     * @param site the site
     */
    public CmsSiteDialogObject(CmsSite site) {

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
            }
            for (CmsSiteMatcher aMatcher : site.getAliases()) {
                m_aliases.add((CmsSiteMatcher)aMatcher.clone());
            }
        }
    }

    /**
     * Constructor using fields.<p>
     * 
     * @param aliases
     * @param exclusiveError
     * @param exclusiveUrl
     * @param port
     * @param secureServer
     * @param secureUrl
     * @param server
     * @param serverName
     * @param serverProtocol
     * @param siteRoot
     * @param timeOffset
     * @param title
     */
    public CmsSiteDialogObject(
        List<CmsSiteMatcher> aliases,
        boolean exclusiveError,
        boolean exclusiveUrl,
        int port,
        boolean secureServer,
        String secureUrl,
        String server,
        String serverName,
        String serverProtocol,
        String siteRoot,
        long timeOffset,
        String title) {

        super();
        m_aliases = aliases;
        m_exclusiveError = exclusiveError;
        m_exclusiveUrl = exclusiveUrl;
        m_port = port;
        m_secureServer = secureServer;
        m_secureUrl = secureUrl;
        m_server = server;
        m_serverName = serverName;
        m_serverProtocol = serverProtocol;
        m_siteRoot = siteRoot;
        m_timeOffset = timeOffset;
        m_title = title;
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
        CmsSiteDialogObject other = (CmsSiteDialogObject)obj;
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
    public List<CmsSiteMatcher> getAliases() {

        return m_aliases;
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
    public boolean hasSecureServer() {

        return m_secureServer;
    }

    /**
     * Sets the aliases.<p>
     *
     * @param aliases the aliases to set
     */
    public void setAliases(List<CmsSiteMatcher> aliases) {

        m_aliases = aliases;
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
     * Sets the port.<p>
     *
     * @param port the port to set
     */
    public void setPort(int port) {

        m_port = port;
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
     * Creates a new site object based on the members.<p>
     * 
     * @return a new site object based on the members
     */
    public CmsSite toCmsSite() {

        CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(m_siteRoot);
        if ((site != null) && (site.getSiteMatcher() != null)) {
            return new CmsSite(
                getSiteRoot(),
                (CmsUUID)site.getSiteRootUUID().clone(),
                getTitle(),
                new CmsSiteMatcher(m_server),
                String.valueOf(site.getPosition()),
                new CmsSiteMatcher(m_secureUrl),
                m_exclusiveUrl,
                m_exclusiveError,
                m_aliases);
        }
        return null;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "Site [m_siteRoot=" + m_siteRoot + ", m_title=" + m_title + ", m_server=" + m_server + "]";
    }
}
