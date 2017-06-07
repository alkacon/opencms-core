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

import org.apache.commons.logging.Log;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

/**
 * Sends an email using a Thread, so that the application can
 * continue without waiting for the mail to be send.<p>
 *
 * @since 6.0.0
 */
public class CmsMailTransport extends Thread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMailTransport.class);

    /** The email to be send. */
    private Email m_email;

    /**
     * Creates a new CmsMailTransport.<p>
     *
     * @param email the email to be send with this transport
     */
    public CmsMailTransport(Email email) {

        m_email = email;
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        try {
            m_email.send();
        } catch (EmailException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_SEND_MAIL_ERR_0), e);
            }
        }
    }

    /**
     * Sends the email in this transport object,
     * same as calling <code>start()</code>.<p>
     */
    public void send() {

        start();
    }
}
