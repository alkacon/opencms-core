/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/mail/CmsSimpleMail.java,v $
 * Date   : $Date: 2005/06/30 10:12:36 $
 * Version: $Revision: 1.9 $
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

import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;

import org.apache.commons.logging.Log;
import org.apache.commons.mail.SimpleEmail;

/**
 * This class is used to send simple text internet email messages without
 * attachments.<p>
 *
 * It uses the Apache Commons Email API and extends the provided classes
 * to conveniently generate emails using the OpenCms configuration.<p> 
 *
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSimpleMail extends SimpleEmail {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSimpleMail.class);

    /**
     * Default constructor of a CmsSimpleMail.<p>
     * 
     * The mail host name and the mail from address are set to the OpenCms
     * default values of the configuration.<p>
     * 
     */
    public CmsSimpleMail() {

        // call super constructor
        super();
        // set the host to the default mail host
        CmsMailHost host = OpenCms.getSystemInfo().getMailSettings().getDefaultMailHost();
        setHostName(host.getHostname());

        // check if username and password are provided
        String userName = host.getUsername();
        if (userName != null && !"".equals(userName.trim())) {
            // authentication needed, set user name and password
            setAuthentication(userName, host.getPassword());
        }
        try {
            // set default mail from address
            setFrom(OpenCms.getSystemInfo().getMailSettings().getMailFromDefault());
        } catch (MessagingException e) {
            // default email address is not valid, log error
            LOG.error(Messages.get().key(Messages.LOG_INVALID_SENDER_ADDRESS_0), e);
        }
    }

    /**
     * Overrides to add a layer for localization of exception / logging.<p>
     * 
     * Please note that in case of a <code>{@link SendFailedException}</code> 
     * the cause of this message will contain a  <code>{@link CmsRuntimeException}</code> 
     * as cause. The information of the <code>SendFaileException</code> should be used outside 
     * to remove recipients (e.g. from the beans that store them) in order to avoid duplicate 
     * sending of emails. The internal cause then just should be rethrown to allow localized 
     * output about the cause.<p>
     * 
     *  
     * @see org.apache.commons.mail.Email#send()
     * 
     * @throws MessagingException if something goes wrong
     * @throws SendFailedException if sending failed, please look at the possibility to use this type to remove 
     *         recepients from a mass mail where sending suceeded. 
     */
    public void send() throws MessagingException, SendFailedException {

        try {
            super.send();
        } catch (SendFailedException sf) {
            // The specialized exception types (authentication, wrong host) are wrapped in this type:
            MessagingException me = (MessagingException)sf.getNextException();
            CmsMailHost host = OpenCms.getSystemInfo().getMailSettings().getDefaultMailHost();
            if (me instanceof AuthenticationFailedException) {
                // wrong user credentials in opencms-system.xml
                CmsRuntimeException rte = new CmsRuntimeException(Messages.get().container(
                    Messages.ERR_SEND_EMAIL_AUTHENTICATE_2,
                    host.getUsername(),
                    host.getHostname()));
                sf.initCause(rte);
                throw sf;
            }
            // wrong hostname in opencms-system.xml
            CmsRuntimeException rte = new CmsRuntimeException(Messages.get().container(
                Messages.ERR_SEND_EMAIL_HOSTNAME_1,
                host.getHostname()), me);
            sf.initCause(rte);
            throw sf;

        }
    }
}
