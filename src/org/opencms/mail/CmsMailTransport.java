/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/mail/CmsMailTransport.java,v $
 * Date   : $Date: 2005/06/23 10:47:28 $
 * Version: $Revision: 1.6 $
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

package org.opencms.mail;

import org.opencms.main.CmsLog;

import javax.mail.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.mail.Email;

/**
 * Sends an email using a Thread, so that the application can 
 * continue without waiting for the mail to be send.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.6 $ 
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
    public void run() {

        try {
            m_email.send();
        } catch (MessagingException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().key(Messages.LOG_SEND_MAIL_ERR_0), e);
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
