/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/mail/CmsMailHost.java,v $
 * Date   : $Date: 2005/06/27 23:22:25 $
 * Version: $Revision: 1.7 $
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

package org.opencms.mail;

/**
 * Contains the configuration of an individual mail host.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.0.0 
 */
public class CmsMailHost implements Comparable {

    /** The name of the mail host. */
    private String m_hostname;

    /** The order of this mail host. */
    private Integer m_order;

    /** The password to use for authentication. */
    private String m_password;

    /** The protocol to use. */
    private String m_protocol;

    /** The user name to use for authentication. */
    private String m_username;

    /**
     * Creates a new mail host.<p>
     * 
     * @param hostname the name of the mail host
     * @param order the order in which the host is tried
     * @param protocol the protocol to use (default "smtp")
     * @param username the user name to use for authentication 
     * @param password the password to use for authentication
     */
    public CmsMailHost(String hostname, Integer order, String protocol, String username, String password) {

        m_hostname = hostname;
        m_protocol = (protocol != null) ? protocol : CmsMailSettings.MAIL_DEFAULT_PROTOCOL;
        m_username = username;
        m_password = password;
        m_order = order;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {

        if (obj == this) {
            return 0;
        }
        if (obj instanceof CmsMailHost) {
            return m_order.compareTo(((CmsMailHost)obj).m_order);
        }
        return 0;
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
     * Returns the protocol used for mail sending, default is "smtp".<p>
     * 
     * @return the protocol used for mail sending
     */
    public String getProtocol() {

        return m_protocol;
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
    public String toString() {

        StringBuffer buf = new StringBuffer(64);
        buf.append(this.getClass().getName());
        buf.append(" hostname=");
        buf.append(getHostname());
        buf.append(" order=");
        buf.append(m_order);
        buf.append(" protocol=");
        buf.append(getProtocol());
        if (isAuthenticating()) {
            buf.append(" user=");
            buf.append(getUsername());
            buf.append(" password=");
            buf.append(getPassword());
        }
        return buf.toString();
    }
}