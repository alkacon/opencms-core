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

import org.opencms.main.OpenCms;

import org.apache.commons.mail.HtmlEmail;

/**
 * This class is used to send an HTML formatted email with optional attachments.<p>
 *
 * A text message can also be set for HTML unaware email clients,
 * such as text-based email clients.<p>
 *
 * It uses the Apache Commons Email API and extends the provided classes
 * to conveniently generate emails using the OpenCms configuration.<p>
 *
 * @since 6.0.0
 */
public class CmsHtmlMail extends HtmlEmail {

    /**
     * Default constructor of a CmsHtmlMail.<p>
     *
     * The mail host name and the mail from address are set to the OpenCms
     * default values of the configuration.<p>
     *
     */
    public CmsHtmlMail() {

        this(OpenCms.getSystemInfo().getMailSettings().getDefaultMailHost());
    }

    /**
     * Constructor of a CmsHtmlMail where the mail host is explicitly chosen.<p>
     *
     * The mail from address is set to the OpenCms
     * default values of the configuration.<p>
     *
     * @param mailHost the mail host to use (a host configured in OpenCms).
     *
     */
    public CmsHtmlMail(CmsMailHost mailHost) {

        // call super constructor
        super();
        CmsMailUtil.configureMail(mailHost, this);
    }

    /**
     * Constructor of a CmsHtmlMail where the id of the mail host is explicitly chosen.<p>
     *
     * If the mail host with the chosen id is not available, fall back to the default mail host.
     *
     * @param mailHostId the mail host id
     */
    public CmsHtmlMail(String mailHostId) {

        super();
        CmsMailSettings mailSettings = OpenCms.getSystemInfo().getMailSettings();
        CmsMailHost mailHost = mailSettings.getMailHost(mailHostId);
        CmsMailHost defaultMailHost = mailSettings.getDefaultMailHost();
        CmsMailUtil.configureMail(mailHost != null ? mailHost : defaultMailHost, this);
    }
}
