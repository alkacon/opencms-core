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

package org.opencms.mail;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

/**
 * Contains utility methods for dealing with emails.<p>
 */
public final class CmsMailUtil {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMailUtil.class);

    /** Value for 'security' attribute to enable SSL. */
    public static final String SECURITY_SSL = "SSL";

    /** Value for 'security' attribute to enable TLS. */
    public static final String SECURITY_STARTTLS = "STARTTLS";

    /**
     * Hidden default constructor.<p>
     */
    private CmsMailUtil() {

        // hidden default constructor
    }

    /**
     * Configures the mail from the given mail host configuration data.<p>
     *
     * @param host the mail host configuration
     * @param mail the email instance
     */
    public static void configureMail(CmsMailHost host, Email mail) {

        // set the host to the default mail host
        mail.setHostName(host.getHostname());
        mail.setSmtpPort(host.getPort());

        // check if username and password are provided
        String userName = host.getUsername();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(userName)) {
            // authentication needed, set user name and password
            mail.setAuthentication(userName, host.getPassword());
        }
        String security = host.getSecurity() != null ? host.getSecurity().trim() : null;
        if (SECURITY_SSL.equalsIgnoreCase(security)) {
            mail.setSslSmtpPort("" + host.getPort());
            mail.setSSLOnConnect(true);
        } else if (SECURITY_STARTTLS.equalsIgnoreCase(security)) {
            mail.setStartTLSEnabled(true);
        }

        try {
            // set default mail from address
            String mailFrom = host.getMailfrom() != null
            ? host.getMailfrom()
            : OpenCms.getSystemInfo().getMailSettings().getMailFromDefault();
            mail.setFrom(mailFrom);
        } catch (EmailException e) {
            // default email address is not valid, log error
            LOG.error(Messages.get().getBundle().key(Messages.LOG_INVALID_SENDER_ADDRESS_0), e);
        }
    }
}
