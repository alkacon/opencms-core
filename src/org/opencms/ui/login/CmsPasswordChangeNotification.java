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

package org.opencms.ui.login;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.notification.A_CmsNotification;

/**
 * Notification used to send password change link to user.<p>
 */
public class CmsPasswordChangeNotification extends A_CmsNotification {

    /** The link to change the password. */
    private String m_link;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     * @param receiver the receiver
     * @param link the link
     * @param expiration the formatted link expiration date
     */
    public CmsPasswordChangeNotification(CmsObject cms, CmsUser receiver, String link, String expiration) {
        super(cms, receiver);
        m_link = CmsEncoder.escapeXml(link);
        addMacro("user", receiver.getName());
        addMacro("expiration", expiration);
    }

    /**
     * @see org.opencms.notification.A_CmsNotification#generateHtmlMsg()
     */
    @Override
    protected String generateHtmlMsg() {

        return "<a href=\"" + m_link + "\">" + m_link + "</a>";

    }

    /**
     * @see org.opencms.notification.A_CmsNotification#getNotificationContent()
     */
    @Override
    protected String getNotificationContent() {

        return "/system/workplace/admin/notification/password-change-notification";
    }

}
