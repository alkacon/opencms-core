/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/Attic/CmsMailSettings.java,v $
 * Date   : $Date: 2004/03/12 16:00:48 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.configuration;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains the settings for the OpenCms mail service.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class CmsMailSettings {
    
    /**
     * Contains the configuration of an individual mail host.<p>
     */
    public class CmsMailHost implements Comparable {
        
        /** The name of the mail host */
        private String m_hostname;
        
        /** The order of this mail host */
        private Integer m_order;
        
        /** The password to use for authentication */
        private String m_password;        
        
        /** The protocol to use */
        private String m_protocol;
        
        /** The user name to use for authentication */
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
            m_protocol = (protocol!=null)?protocol:C_MAIL_DEFAULT_PROTOCOL;
            m_username = username;
            m_password = password;   
            m_order = order;
        }

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object o) {
            if (o instanceof CmsMailHost) {
                return m_order.compareTo(((CmsMailHost)o).m_order);
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
         * Returns the order of this mail host.<p>
         * 
         * @return the order of this mail host
         */
        public Integer getOrder() {
            return m_order;
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

    /** The default protocol for sending mail ("smtp") */
    public static final String C_MAIL_DEFAULT_PROTOCOL = "smtp";

    /** The default mail from sender */
    public static final String C_MAIL_DEFAULT_SENDER = "opencms@unconfigured.com";    
    
    /** The default mail "from" sender address */
    private String m_mailFromDefault;
    
    /** The list of internal mail hosts */
    private List m_mailHosts;
    
    /** The default order if no order is given for a host */
    private int m_orderDefault;
    
    /**
     * Empty constructor, required for configuration.<p> 
     */
    public CmsMailSettings() {
        m_mailFromDefault = C_MAIL_DEFAULT_SENDER;
        m_mailHosts = new ArrayList();
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Empty constructor called on " + this);
        }          
    }
    
    /**
     * Adds a new mail host to the internal list of mail hosts.<p>
     * 
     * @param hostname the name of the mail host
     * @param order the order in which the host is tried
     * @param protocol the protocol to use (default "smtp")
     * @param username the user name to use for authentication 
     * @param password the password to use for authentication
     */    
    public void addMailHost(String hostname, String order, String protocol, String username, String password) {
        m_orderDefault += 10;
        Integer theOrder;
        try {
            theOrder = Integer.valueOf(order);
            if (theOrder.intValue() > m_orderDefault) {
                m_orderDefault = theOrder.intValue();
            }
        } catch (Throwable t) {
            theOrder = new Integer(m_orderDefault);
        }
        CmsMailHost host = new CmsMailHost(hostname, theOrder, protocol, username, password);
        m_mailHosts.add(host);
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Mail configuration   : Added " + host);
        }           
        Collections.sort(m_mailHosts);
    }
    
    /**
     * Returns the mail from default sender.<p>
     * 
     * @return the mail from default sender
     */
    public String getMailFromDefault() {
        return m_mailFromDefault;
    }
    
    /**
     * Returns an unmodifiable sorted list of all configured mail hosts.<p>
     *  
     * @return an unmodifiable sorted list of all configured mail hosts
     */
    public List getMailHosts() {
        return Collections.unmodifiableList(m_mailHosts);
    }
   
    /**
     * Sets the mail from default sender.<p>
     * 
     * @param sender the mail from default sender to set
     */
    public void setMailFromDefault(String sender) {
        m_mailFromDefault = sender;
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Mail configuration   : Default mail sender is '" + m_mailFromDefault + "'");
        }                
    }
}
