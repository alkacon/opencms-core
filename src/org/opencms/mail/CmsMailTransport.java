/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/mail/CmsMailTransport.java,v $
 * Date   : $Date: 2004/08/06 16:17:42 $
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
 
package org.opencms.mail;

import org.opencms.main.OpenCms;

import javax.mail.MessagingException;

import org.apache.commons.mail.Email;

/**
 * Sends an email using a Thread, so that the application can 
 * continue without waiting for the mail to be send.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsMailTransport extends Thread {

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
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error sending email", e);
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
