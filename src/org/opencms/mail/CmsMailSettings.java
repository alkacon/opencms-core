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

import org.opencms.main.CmsLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Contains the settings for the OpenCms mail service.<p>
 *
 * @since 6.0.0
 */
public class CmsMailSettings {

    /** The default protocol for sending mail ("smtp"). */
    public static final String MAIL_DEFAULT_PROTOCOL = "smtp";

    /** The default mail from address. */
    public static final String MAIL_DEFAULT_SENDER = "opencms@unconfigured.com";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMailSettings.class);

    /** The default mail "from" sender address. */
    private String m_mailFromDefault;

    /** The list of internal mail hosts. */
    private List<CmsMailHost> m_mailHosts;

    /** The default order if no order is given for a host. */
    private int m_orderDefault;

    /**
     * Empty constructor, required for configuration.<p>
     */
    public CmsMailSettings() {

        m_mailFromDefault = MAIL_DEFAULT_SENDER;
        m_mailHosts = new ArrayList<CmsMailHost>();
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_EMPTY_CONSTRUCTOR_CALLED_1));
        }
    }

    /**
     * Adds a new mail host to the internal list of mail hosts with default port 25.<p>
     *
     * @param hostname the name of the mail host
     * @param order the order in which the host is tried
     * @param protocol the protocol to use (default "smtp")
     * @param username the user name to use for authentication
     * @param password the password to use for authentication
     */
    public void addMailHost(String hostname, String order, String protocol, String username, String password) {

        addMailHost(hostname, "25", order, protocol, null, username, password);
    }

    /**
       * Adds a new mail host to the internal list of mail hosts.<p>
       *
       * @param hostname the name of the mail host
       * @param port the port of the mail host
       * @param order the order in which the host is tried
       * @param protocol the protocol to use (default "smtp")
       * @param security the security mode
       * @param username the user name to use for authentication
       * @param password the password to use for authentication
       */
    public void addMailHost(
        String hostname,
        String port,
        String order,
        String protocol,
        String security,
        String username,
        String password) {

        addMailHost(hostname, "25", order, protocol, null, username, password, null, null);
    }

    /**
       * Adds a new mail host to the internal list of mail hosts.<p>
       *
       * @param hostname the name of the mail host
       * @param port the port of the mail host
       * @param order the order in which the host is tried
       * @param protocol the protocol to use (default "smtp")
       * @param security the security mode
       * @param username the user name to use for authentication
       * @param password the password to use for authentication
       * @param id the id of the mail host
       * @param mailfrom the mail-from address of the mail host
       */
    public void addMailHost(
        String hostname,
        String port,
        String order,
        String protocol,
        String security,
        String username,
        String password,
        String id,
        String mailfrom) {

        Integer thePort;
        try {
            thePort = Integer.valueOf(port);
        } catch (Throwable t) {
            thePort = Integer.valueOf(25);
        }
        m_orderDefault += 10;
        Integer theOrder;
        try {
            theOrder = Integer.valueOf(order);
            if (theOrder.intValue() > m_orderDefault) {
                m_orderDefault = theOrder.intValue();
            }
        } catch (Throwable t) {
            // valueOf: use jdk int cache if possible and not new operator:
            theOrder = Integer.valueOf(m_orderDefault);
        }
        CmsMailHost host = new CmsMailHost(
            hostname,
            thePort,
            theOrder,
            protocol,
            security,
            username,
            password,
            id,
            mailfrom);
        m_mailHosts.add(host);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.LOG_ADD_HOST_1, host));
        }
        Collections.sort(m_mailHosts);
    }

    /**
     * Returns the default mail host.<p>
     *
     * @return the default mail host
     */
    public CmsMailHost getDefaultMailHost() {

        return m_mailHosts.get(0);
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
     * Returns a mail host for a given id.<p>
     *
     * @param id the id
     * @return the mail host
     */
    public CmsMailHost getMailHost(String id) {

        if (id == null) {
            return null;
        }
        List<CmsMailHost> filtered = new ArrayList<CmsMailHost>();
        for (CmsMailHost mailHost : m_mailHosts) {
            if ((mailHost.getId() != null) && mailHost.getId().equals(id)) {
                filtered.add(mailHost);
            }
        }
        if (filtered.isEmpty()) {
            return null;
        } else {
            Collections.sort(filtered);
            return filtered.get(filtered.size() - 1);
        }
    }

    /**
     * Returns an unmodifiable sorted list of all configured mail hosts.<p>
     *
     * @return an unmodifiable sorted list of all configured mail hosts
     */
    public List<CmsMailHost> getMailHosts() {

        return Collections.unmodifiableList(m_mailHosts);
    }

    /**
     * Sets the mail from default sender.<p>
     *
     * @param sender the mail from default sender to set
     */
    public void setMailFromDefault(String sender) {

        m_mailFromDefault = sender;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.LOG_DEFAULT_SENDER_1, m_mailFromDefault));
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[hosts:" + m_mailHosts.toString());
        sb.append(", order:" + m_orderDefault);
        sb.append(", from:" + m_mailFromDefault);
        return sb.toString();
    }
}
