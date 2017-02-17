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
import org.opencms.publish.CmsPublishJobRunning;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.util.CmsUUID;

import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Class for the published resources dialog.<p>
 */
public class CmsPublishResources extends VerticalLayout {

    /** The logger for this class. */
    private static Log LOG = CmsLog.getLog(CmsPublishResources.class.getName());

    /**vaadin serial id.*/
    private static final long serialVersionUID = -3197777771233458574L;

    /**Calling object.*/
    final CmsPublishQueue m_manager;

    /**Vaadin component.*/
    private Button m_cancel;

    /**Vaddin component.*/
    private VerticalLayout m_panel;

    /**
     * Public constructor.<p>
     *
     * @param manager calling manager object
     * @param id job-id
     */
    public CmsPublishResources(CmsPublishQueue manager, String id) {

        m_manager = manager;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        final CmsPublishJobBase job = OpenCms.getPublishManager().getJobByPublishHistoryId(new CmsUUID(id));

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
        m_panel.addComponent(label);

        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -1921697074171843576L;

            public void buttonClick(ClickEvent event) {

                if ((job instanceof CmsPublishJobRunning) | (job instanceof CmsPublishJobEnqueued)) {
                    m_manager.openSubView("", true);
                } else {
                    m_manager.openSubView(CmsPublishQueue.PATH_HISTORY, true);
                }
            }
        });
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
