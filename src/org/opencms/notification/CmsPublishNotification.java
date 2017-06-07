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

package org.opencms.notification;

import java.util.Iterator;
import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessages;
import org.opencms.report.I_CmsReport;

/**
 * Class to send a notification to an OpenCms user with a summary of warnings and
 * errors occurred while publishing the project.<p>
 *
 * @since 6.5.3
 */
public class CmsPublishNotification extends A_CmsNotification {

    /** The path to the xml content with the subject, header and footer of the notification e-mail.<p> */
    public static final String NOTIFICATION_CONTENT = "/system/workplace/admin/notification/publish-notification";

    /** The report containing the errors and warnings to put into the notification. */
    private I_CmsReport m_report;

    /**
     * Creates a new CmsPublishNotification.<p>
     *
     * @param cms the cms object to use
     * @param receiver the notification receiver
     * @param report the report to write the output to
     */
    public CmsPublishNotification(CmsObject cms, CmsUser receiver, I_CmsReport report) {

        super(cms, receiver);
        m_report = report;
    }

    /**
     * @see org.opencms.notification.A_CmsNotification#generateHtmlMsg()
     */
    @Override
    protected String generateHtmlMsg() {

        StringBuffer buffer = new StringBuffer();

        CmsMessages messages = Messages.get().getBundle(getLocale());

        // add warnings to the notification
        if (m_report.hasWarning()) {
            buffer.append("<b>");
            buffer.append(messages.key(Messages.GUI_PUBLISH_WARNING_HEADER_0));
            buffer.append("</b><br/>\n");
            appendList(buffer, m_report.getWarnings());
            buffer.append("<br/>\n");
        }

        // add errors to the notification
        if (m_report.hasError()) {
            buffer.append("<b>");
            buffer.append(messages.key(Messages.GUI_PUBLISH_ERROR_HEADER_0));
            buffer.append("</b><br/>\n");
            appendList(buffer, m_report.getErrors());
            buffer.append("<br/>\n");
        }

        return buffer.toString();
    }

    /**
     * @see org.opencms.notification.A_CmsNotification#getNotificationContent()
     */
    @Override
    protected String getNotificationContent() {

        return NOTIFICATION_CONTENT;
    }

    /**
     * Appends the contents of a list to the buffer with every entry in a new line.<p>
     *
     * @param buffer The buffer were the entries of the list will be appended.
     * @param list The list with the entries to append to the buffer.
     */
    private void appendList(StringBuffer buffer, List<Object> list) {

        Iterator<Object> iter = list.iterator();
        while (iter.hasNext()) {
            Object entry = iter.next();
            buffer.append(entry).append("<br/>\n");
        }
    }

}
