/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/site/CmsSiteMatcher.java,v $
 * Date   : $Date: 2005/06/23 10:47:32 $
 * Version: $Revision: 1.17 $
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

/**
 * A matcher object to compare request data against the configured sites.<p>
 *
 * @author  Alexander Kandzior 
 *
 * @version $Revision: 1.17 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsSiteMatcher implements Cloneable {

    /** Constant for the "http" port. */
    private static final int PORT_HTTP = 80;

    /** Constant for the "https" port. */
    private static final int PORT_HTTPS = 443;

    /** Constant for the "http" scheme. */
    private static final String SCHEME_HTTP = "http";

    /** Constant for the "https" scheme. */
    private static final String SCHEME_HTTPS = "https";

    /** Wildcard for string matching. */
    private static final String WILDCARD = "*";

    /** Default matcher that always matches all other Site matchers. */
    public static final CmsSiteMatcher DEFAULT_MATCHER = new CmsSiteMatcher(WILDCARD, WILDCARD, 0);

    /** Hashcode buffer to save multiple calculations. */
    private Integer m_hashCode;

    /** The hostname (e.g. localhost) which is required to access this site. */
    private String m_serverName;

    /** The port (e.g. 80) which is required to access this site. */
    private int m_serverPort;

    /** The protocol (e.g. "http", "https") which is required to access this site. */
    private String m_serverProtocol;

    /**
     * Construct a new site matcher from a String which should be in default URL notation.<p>
     * 
     * If no port is provided, the default port 80 or 443 will be used for http or https respectively.
     * If no protocol is provided, the default protocol "http" will be used.
     * 
     * @param serverString the String, e.g. http://localhost:8080
     */
    public CmsSiteMatcher(String serverString) {

        if (serverString == null) {
            init(WILDCARD, WILDCARD, 0);
            return;
        }
        // remove whitespace
        serverString = serverString.trim();
        // cut trailing "/" 
        if (serverString.endsWith("/")) {
            serverString = serverString.substring(0, serverString.length() - 1);
        }
        int pos, serverPort;
        String serverProtocol, serverName;
        // check for protocol
        pos = serverString.indexOf("://");
        if (pos >= 0) {
            serverProtocol = serverString.substring(0, pos);
            serverString = serverString.substring(pos + 3);
        } else {
            serverProtocol = SCHEME_HTTP;
        }
        // check for server name and port
        pos = serverString.indexOf(":");
        if (pos >= 0) {
            serverName = serverString.substring(0, pos);
            try {
                String port = serverString.substring(pos + 1);
                pos = port.indexOf("/");
                if (pos >= 0) {
                    port = port.substring(0, pos);
                }
                serverPort = Integer.valueOf(port).intValue();
            } catch (NumberFormatException e) {
                serverPort = PORT_HTTP;
            }
        } else {
            serverName = serverString;
            if (SCHEME_HTTPS.equals(serverProtocol)) {
                serverPort = PORT_HTTPS;
            } else {
                serverPort = PORT_HTTP;
            }
        }

        // cut trailing path in server name
        pos = serverName.indexOf("/");
        if (pos >= 0) {
            serverName = serverName.substring(0, pos);
        }

        // initialize members
        init(serverProtocol, serverName, serverPort);
    }

    /**
     * Constructs a new site matcher object.<p>
     * 
     * @param serverProtocol to protocol required to access this site
     * @param serverName the server URL prefix to which this site is mapped
     * @param serverPort the port required to access this site
     */
    public CmsSiteMatcher(String serverProtocol, String serverName, int serverPort) {

        init(serverProtocol, serverName, serverPort);
    }

    /**
     * Returns a clone of this Objects instance.<p>
     * 
     * @return a clone of this instance
     */
    public Object clone() {

        return new CmsSiteMatcher(m_serverProtocol, m_serverName, m_serverPort);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CmsSiteMatcher)) {
            return false;
        }
        // if one of the object is the default matcher the result is always true
        if ((this == DEFAULT_MATCHER) || (obj == DEFAULT_MATCHER)) {
            return true;
        }
        CmsSiteMatcher other = (CmsSiteMatcher)obj;
        return (m_serverPort == other.m_serverPort)
            && m_serverName.equals(other.m_serverName)
            && m_serverProtocol.equals(other.m_serverProtocol);
    }

    /**
     * Returns the hostname (e.g. localhost) which is required to access this site.<p>
     * 
     * @return the hostname (e.g. localhost) which is required to access this site
     */
    public String getServerName() {

        return m_serverName;
    }

    /**
     * Returns the port (e.g. 80) which is required to access this site.<p>
     *
     * @return the port (e.g. 80) which is required to access this site
     */
    public int getServerPort() {

        return m_serverPort;
    }

    /**
     * Returns the protocol (e.g. "http", "https") which is required to access this site.<p>
     * 
     * @return the protocol (e.g. "http", "https") which is required to access this site
     */
    public String getServerProtocol() {

        return m_serverProtocol;
    }

    /**
     * Returns the url of this site matcher.<p>
     * 
     * @return the url, i.e. {protocol}://{servername}[:{port}], port appened only if != 80
     */
    public String getUrl() {

        return m_serverProtocol
            + "://"
            + m_serverName
            + (((m_serverPort != PORT_HTTP) && (m_serverPort != PORT_HTTPS)) ? ":" + m_serverPort : "");
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        if (m_hashCode == null) {
            m_hashCode = new Integer(toString().hashCode());
        }
        return m_hashCode.intValue();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer result = new StringBuffer(32);
        if ((m_serverProtocol != null) && !(WILDCARD.equals(m_serverProtocol))) {
            result.append(m_serverProtocol);
            result.append("://");
        }
        result.append(m_serverName);
        if ((m_serverPort > 0)
            && (!(SCHEME_HTTP.equals(m_serverProtocol) && (m_serverPort == PORT_HTTP)))
            && (!(SCHEME_HTTPS.equals(m_serverProtocol) && (m_serverPort == PORT_HTTPS)))) {
            result.append(":");
            result.append(m_serverPort);
        }
        return result.toString();
    }

    /**
     * Sets the hostname (e.g. localhost) which is required to access this site.<p>
     * 
     * Setting the hostname to "*" is a wildcard that matches all hostnames
     * 
     * @param serverName the hostname (e.g. localhost) which is required to access this site
     */
    protected void setServerName(String serverName) {

        if ((serverName == null) || ("".equals(serverName)) || (WILDCARD.equals(serverName))) {
            m_serverName = WILDCARD;
        } else {
            m_serverName = serverName.trim();
        }
    }

    /**
     * Sets the port (e.g. 80) which is required to access this site.<p>
     *
     * Setting the port to 0 (zero) is a wildcard that matches all ports
     *
     * @param serverPort the port (e.g. 80) which is required to access this site
     */
    protected void setServerPort(int serverPort) {

        m_serverPort = serverPort;
        if (m_serverPort < 0) {
            m_serverPort = 0;
        }
    }

    /**
     * Sets the protocol (e.g. "http", "https") which is required to access this site.<p>
     * 
     * Setting the protocol to "*" is a wildcard that matches all protocols.<p>
     *      
     * @param serverProtocol the protocol (e.g. "http", "https") which is required to access this site
     */
    protected void setServerProtocol(String serverProtocol) {

        int pos;
        if ((serverProtocol == null) || ("".equals(serverProtocol)) || (WILDCARD.equals(serverProtocol))) {
            m_serverProtocol = WILDCARD;
        } else if ((pos = serverProtocol.indexOf("/")) > 0) {
            m_serverProtocol = serverProtocol.substring(0, pos).toLowerCase();
        } else {
            m_serverProtocol = serverProtocol.toLowerCase().trim();
        }
    }

    /**
     * Inits the member variables.<p>
     * 
     * @param serverProtocol to protocol required to access this site
     * @param serverName the server URL prefix to which this site is mapped
     * @param serverPort the port required to access this site
     */
    private void init(String serverProtocol, String serverName, int serverPort) {

        setServerProtocol(serverProtocol);
        setServerName(serverName);
        setServerPort(serverPort);
    }
}