/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/mail/CmsMultiPartMail.java,v $
 * Date   : $Date: 2005/02/17 12:44:35 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.apache.commons.mail.MultiPartEmail;


/**
 * This class is used to send multi-part internet email like
 * messages with attachments.<p>
 *
 * It uses the Apache Commons Email API and extends the provided classes
 * to conveniently generate emails using the OpenCms configuration.<p> 
 *
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.2 $
 * @since 5.5.0
 */
public class CmsMultiPartMail extends MultiPartEmail {
    
    /**
     * Default constructor of a CmsHtmlMail.<p>
     * 
     * The mail host name and the mail from address are set to the OpenCms
     * default values of the configuration.<p>
     * 
     */
    public CmsMultiPartMail() {
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
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error(e);
            }
        }
    }
    
}
