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

import org.opencms.ade.publish.CmsPublishService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.mail.CmsHtmlMail;
import org.opencms.main.OpenCms;

import java.util.List;

import org.apache.commons.mail.EmailException;

/**
 * A class used for sending mails about resources which have been released by a workflow action to the appropriate users.<p>
 */
public class CmsWorkflowNotification extends CmsHtmlMail {

    /**
     * Creates a new workflow notification instance.<p>
     * 
     * @param cms the CMS context 
     * @param recipient the user to which the mail should be sent 
     * @param user the user which triggered the workflow action 
     * @param workflowProject the project which was affected by the workflow action 
     * @param resources the list of resources affected by the workflow action 
     * 
     * @throws EmailException if sending the mail fails 
     */
    public CmsWorkflowNotification(
        CmsObject cms,
        CmsUser recipient,
        CmsUser user,
        CmsProject workflowProject,
        List<CmsResource> resources)
    throws EmailException {

        super();

        String htmlStart = "<html><head></head><body>";
        String htmlEnd = "</body></html>";
        String linkHref = OpenCms.getLinkManager().getServerLink(
            cms,
            "/system/modules/org.opencms.ade.publish/publish.jsp?"
                + CmsPublishService.PARAM_PUBLISH_PROJECT_ID
                + "="
                + workflowProject.getUuid());
        String htmlMain = "<div>" + linkHref + "</div>";
        setHtmlMsg(htmlStart + htmlMain + htmlEnd);
        addTo(recipient.getEmail());
        setSubject("Workflow notification (" + user.getName() + ")");
    }
}
