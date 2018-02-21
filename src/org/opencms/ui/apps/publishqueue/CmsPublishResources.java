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

package org.opencms.ui.apps.publishqueue;

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishJobBase;
import org.opencms.publish.CmsPublishJobEnqueued;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.util.CmsUUID;

import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the published resources dialog.<p>
 */
public class CmsPublishResources extends VerticalLayout {

    /** The logger for this class. */
    private static Log LOG = CmsLog.getLog(CmsPublishResources.class.getName());

    /**vaadin serial id.*/
    private static final long serialVersionUID = -3197777771233458574L;

    /**Caption of dialog.*/
    private String m_caption;

    /**vaadin component.*/
    private VerticalLayout m_panel;

    /**
     * Public constructor.<p>
     *
     * @param id job-id
     */
    public CmsPublishResources(String id) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        final CmsPublishJobBase job = OpenCms.getPublishManager().getJobByPublishHistoryId(new CmsUUID(id));

        m_caption = CmsVaadinUtils.getMessageText(
            Messages.GUI_PQUEUE_RESOURCES_2,
            job.getProjectName(),
            job.getUserName(A_CmsUI.getCmsObject()));

        String resourcesHTML = "";

        try {
            resourcesHTML = getResourcesHTML(job);
        } catch (NumberFormatException | CmsException e) {
            LOG.error("Error reading publish resources", e);
        }

        Label label = new Label();
        label.setValue(resourcesHTML);
        label.setContentMode(ContentMode.HTML);
        label.setHeight("700px");
        label.addStyleName("v-scrollable");
        label.addStyleName("o-report");
        m_panel.addComponent(label);

    }

    /**
     * @see com.vaadin.ui.AbstractComponent#getCaption()
     */
    @Override
    public String getCaption() {

        return m_caption;
    }

    /**
     * Get String with list of resources from given job formatted in HTML.<p>
     *
     * @param job CmsPublishJobBase
     * @return String
     * @throws CmsException exception
     */
    private String getResourcesHTML(CmsPublishJobBase job) throws CmsException {

        String ret = "";
        if (job instanceof CmsPublishJobEnqueued) {
            //job is enqueued ->go over publish list
            List<CmsResource> resources = ((CmsPublishJobEnqueued)job).getPublishList().getAllResources();
            for (CmsResource res : resources) {
                ret += res.getRootPath() + "<br/>";
            }
        } else {
            //job is running or finished
            List<CmsPublishedResource> resources = A_CmsUI.getCmsObject().readPublishedResources(
                job.getPublishHistoryId());
            for (CmsPublishedResource res : resources) {
                ret += res.getRootPath() + "<br/>";
            }
        }
        return ret;
    }

}
