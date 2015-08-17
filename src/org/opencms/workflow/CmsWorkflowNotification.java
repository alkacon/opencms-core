/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workflow;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.notification.A_CmsNotification;
import org.opencms.util.CmsStringUtil;

import java.util.List;

import org.apache.commons.mail.EmailException;

/**
 * Notification class for the workflow 'release' action.<p>
 */
public class CmsWorkflowNotification extends A_CmsNotification {

    /** The admin CMS context. */
    private CmsObject m_adminCms;

    /** The publish link. */
    private String m_link;

    /** The path of the notification XML content. */
    private String m_notificationContent;

    /** The workflow project. */
    private CmsProject m_project;

    /** The released resources. */
    private List<CmsResource> m_resources;

    /** The user's cms context. */
    @SuppressWarnings("unused")
    private CmsObject m_userCms;

    /**
     * Creates a new workflow notification mail object.<p>
     *
     * @param adminCms the admin CMS context
     * @param userCms the user CMS context
     * @param receiver the mail recipient
     * @param notificationContent the file from which to read the notification configuration
     * @param project the workflow project
     * @param resources the workflow resources
     * @param link the link used for publishing the resources
     *
     * @throws EmailException if an email error occurs
     */
    public CmsWorkflowNotification(
        CmsObject adminCms,
        CmsObject userCms,
        CmsUser receiver,
        String notificationContent,
        CmsProject project,
        List<CmsResource> resources,
        String link)
        throws EmailException {

        super(userCms, receiver);
        m_notificationContent = notificationContent;
        m_adminCms = adminCms;
        m_userCms = userCms;
        m_project = project;
        m_resources = resources;
        m_link = link;
        String userAddress = userCms.getRequestContext().getCurrentUser().getEmail();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(userAddress)) {
            setFrom(userAddress);
        }
        addMacro("release.project", m_project.getName());
    }

    /**
     * Gets the fields which should be displayed for a single resource.<p>
     *
     * @param resource the resource for which we should fetch the fields
     *
     * @return a string array containing the information for the given resource
     */
    public String[] getResourceInfo(CmsResource resource) {

        String rootPath = resource.getRootPath();
        String title = "-";

        try {
            CmsProperty titleProp = m_adminCms.readPropertyObject(
                resource,
                CmsPropertyDefinition.PROPERTY_TITLE,
                false);
            if (!titleProp.isNullProperty()) {
                title = titleProp.getValue();
            }
        } catch (CmsException e) {
            // ignore
        }
        return new String[] {rootPath, title};
    }

    /**
     * Gets the resource info headers.<p>
     *
     * @return the resource info headers
     */
    public String[] getResourceInfoHeaders() {

        return new String[] {"Resource", "Title"};
    }

    /**
     * @see org.opencms.notification.A_CmsNotification#generateHtmlMsg()
     */
    @Override
    protected String generateHtmlMsg() {

        StringBuffer buffer = new StringBuffer();

        //---------PUBLISH LINK-----------------------------------------
        buffer.append("    <div class=\"publish_link\">");
        buffer.append(getMessage(Messages.GUI_MAIL_PUBLISH_LINK_1, m_link));
        buffer.append("</div>\r\n");

        //----------RESOURCE TABLE-------------------------------------
        buffer.append("    <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"resource_table\">\r\n");
        String[] tableHeaders = getResourceInfoHeaders();
        buffer.append("      <tr>\r\n");
        boolean first = true;
        for (String header : tableHeaders) {
            if (first) {
                buffer.append("<th align=\"left\" class=\"rescol\">");
                first = false;
            } else {
                buffer.append("<th align=\"left\" class=\"titlecol\">");
            }
            buffer.append(header);
            buffer.append("</th>");
        }
        buffer.append("</tr>\n");

        first = true;
        for (CmsResource resource : m_resources) {
            String[] resourceInfos = getResourceInfo(resource);
            buffer.append("      <tr>\r\n");
            for (String resourceInfo : resourceInfos) {
                if (first) {
                    buffer.append("<td class=\"rescol\">");
                    first = false;
                } else {
                    buffer.append("<td class=\"titlecol\">");
                    first = true;
                }
                buffer.append(resourceInfo);
                buffer.append("</td>");
            }
            buffer.append("</tr>\n");
        }
        buffer.append("</table>");

        //---------PUBLISH LINK-----------------------------------------
        buffer.append("    <div class=\"publish_link\">");
        buffer.append(getMessage(Messages.GUI_MAIL_PUBLISH_LINK_1, m_link));
        buffer.append("</div>\r\n");

        return buffer.toString();
    }

    /**
     * Gets a message from the message bundle.<p>
     *
     * @param key the message key
     * @param args the message parameters
     *
     * @return the message from the message bundle
     */
    protected String getMessage(String key, String... args) {

        return Messages.get().getBundle(getLocale()).key(key, args);
    }

    /**
     * @see org.opencms.notification.A_CmsNotification#getNotificationContent()
     */
    @Override
    protected String getNotificationContent() {

        return m_notificationContent;
    }
}
