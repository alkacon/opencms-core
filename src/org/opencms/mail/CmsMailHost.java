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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.mail;

/**
 * Contains the configuration of an individual mail host.<p>
 *
 * @since 6.0.0
 */
public class CmsMailHost implements Comparable<CmsMailHost> {

    /** The name of the mail host. */
    private String m_hostname;

    /** The order of this mail host. */
    private Integer m_order;

    /** The password to use for authentication. */
    private String m_password;

    /** The port to use. */
    private int m_port;

    /** The protocol to use. */
    private String m_protocol;

    /** The user name to use for authentication. */
    private String m_username;

    /** The security setting. */
    private String m_security;

    /** The id of this mail host. */
    private String m_id;

    /** The mail from address of this host. */
    private String m_mailfrom;

    /**
     * Creates a new mail host.<p>
     *
     * @param hostname the name of the mail host
     * @param port the port, if < 0 then 25 is used
     * @param order the order in which the host is tried
     * @param protocol the protocol to use (default "smtp")
     * @param security the security setting
     * @param username the user name to use for authentication
     * @param password the password to use for authentication
     */
    public CmsMailHost(
        String hostname,
        Integer port,
        Integer order,
        String protocol,
        String security,
        String username,
        String password) {

        this(hostname, port, order, protocol, security, username, password, null, null);
    }

    /**
     * Creates a new mail host.<p>
     *
     * @param hostname the name of the mail host
     * @param port the port, if < 0 then 25 is used
     * @param order the order in which the host is tried
     * @param protocol the protocol to use (default "smtp")
     * @param security the security setting
     * @param username the user name to use for authentication
     * @param password the password to use for authentication
     * @param id the id of the mail host
     * @param mailfrom the mail-from address of the mail host
     */
    public CmsMailHost(
        String hostname,
        Integer port,
        Integer order,
        String protocol,
        String security,
        String username,
        String password,
        String id,
        String mailfrom) {

        m_hostname = hostname;
        int portInt = port.intValue();
        m_port = (portInt < 0) ? 25 : portInt;
        m_protocol = (protocol != null) ? protocol : CmsMailSettings.MAIL_DEFAULT_PROTOCOL;
        m_username = username;
        m_password = password;
        m_security = security;
        m_order = order;
        m_id = id;
        m_mailfrom = mailfrom;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CmsMailHost obj) {

        if (obj == this) {
            return 0;
        }
        return m_order.compareTo(obj.m_order);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsMailHost) {
            CmsMailHost other = (CmsMailHost)obj;
            return m_hostname.equals(other.m_hostname)
                && m_protocol.equals(other.m_protocol)
                && m_username.equals(other.m_username);
        }
        return false;
    }

    /**
     * Returns the host name.<p>
     *
     * @return the host name
     */
    public String getHostname() {

        return m_hostname;
    }

    /**
     * Returns the id of this mail host.<p>
     *
     * @return the id of this mail host
     */
    public String getId() {

        return m_id;
    }

    /**
     * Returns the mail from address of this host.
     * @return the mail from address of this host
     */
    public String getMailfrom() {

        return m_mailfrom;
    }

    /**
     * Returns the order of this mail host.<p>
     *
     * @return the order of this mail host
     */
    public Integer getOrder() {

        return m_order;
    }

    /**
     * Returns the password used for authentication.<p>
     *
     * @return the password used for authentication
     */
    public String getPassword() {

        return m_password;
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
     * Returns the protocol used for mail sending, default is "smtp".<p>
     *
     * @return the protocol used for mail sending
     */
    public String getProtocol() {

        return m_protocol;
    }

    /**
     * Gets the value of the security setting.<p>
     *
     * @return the value of the security setting
     */
    public String getSecurity() {

        return m_security;
    }

    /**
     * Returns the user name used for authentication.<p>
     *
     * @return the user name used for authentication
     */
    public String getUsername() {

        return m_username;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return (m_hostname.hashCode() * 1117) + (m_protocol.hashCode() * 2003) + m_username.hashCode();
    }

    /**
     * Returns <code>true</code> only if authentication is enabled,
     * the default is <code>false</code>.<p>
     *
     * Authentication is enabled only if both "username" and "password"
     * are not <code>null</code>.<p>
     *
     * @return <code>true</code> only if authentication is enabled
     */
    public boolean isAuthenticating() {

        return (m_username != null) && (m_password != null);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer buf = new StringBuffer(64);
        buf.append(this.getClass().getName());
        buf.append(" hostname=");
        buf.append(getHostname());
        buf.append(" port=");
        buf.append(getPort());
        buf.append(" order=");
        buf.append(m_order);
        buf.append(" protocol=");

        buf.append(getProtocol());
        buf.append(" security=" + getSecurity());
        if (isAuthenticating()) {
            buf.append(" user=");
            buf.append(getUsername());
            buf.append(" password=");
            buf.append(getPassword());
        }
        buf.append(" id=");
        buf.append(getId());
        buf.append(" mailfrom=");
        buf.append(getMailfrom());
        return buf.toString();
    }
}
