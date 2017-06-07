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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.workplace.commons.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * The copy to project dialog.<p>
 */
public class CmsCopyToProjectDialog extends CmsBasicDialog {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCopyToProjectDialog.class);

    /** The serial version id. */
    private static final long serialVersionUID = -3016972948701432951L;

    /** the dialog context. */
    private I_CmsDialogContext m_context;

    /**
     * Constructor.<p>
     *
     * @param context the dialog context
     */
    public CmsCopyToProjectDialog(I_CmsDialogContext context) {
        m_context = context;
        displayResourceInfo(m_context.getResources());
        String projectName = m_context.getCms().getRequestContext().getCurrentProject().getName();
        Panel resourceListPanel = null;
        try {
            List<String> projectRes = m_context.getCms().readProjectResources(
                m_context.getCms().getRequestContext().getCurrentProject());
            List<CmsResource> resources = new ArrayList<CmsResource>();
            CmsObject rootCms = OpenCms.initCmsObject(m_context.getCms());
            rootCms.getRequestContext().setSiteRoot("/");
            for (String res : projectRes) {
                try {
                    CmsResource resource = rootCms.readResource(res, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                    resources.add(resource);
                } catch (CmsException ex) {
                    LOG.warn("Error reading project resource '" + res + "'.", ex);
                }
            }
            if (!resources.isEmpty()) {
                resourceListPanel = createResourceListPanel(
                    CmsVaadinUtils.getMessageText(Messages.GUI_COPYTOPROJECT_PART_1, projectName),
                    resources);
            }
        } catch (CmsException e) {
            LOG.error("Error reading resources of the project '" + projectName + "'.", e);
        }
        VerticalLayout main = new VerticalLayout();
        if (resourceListPanel != null) {
            main.addComponent(resourceListPanel);
        }
        Label label = new Label(
            CmsVaadinUtils.getMessageText(
                Messages.GUI_COPYTOPROJECT_PROJECT_CONFIRMATION_2,
                m_context.getResources().get(0).getName(),
                projectName));
        main.addComponent(label);
        main.setSpacing(true);
        setContent(main);
        Button okButton = createButtonOK();
        okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                submit();
            }
        });
        addButton(okButton);

        Button cancelButton = createButtonCancel();
        cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                cancel();
            }
        });
        addButton(cancelButton);
        setActionHandler(new CmsOkCancelActionHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void cancel() {

                CmsCopyToProjectDialog.this.cancel();
            }

            @Override
            protected void ok() {

                submit();
            }
        });
    }

    /**
     * Cancels the dialog.<p>
     */
    void cancel() {

        m_context.finish(null);
    }

    /**
     * Submits the dialog.<p>
     */
    void submit() {

        CmsResource target = m_context.getResources().get(0);
        String resPath = m_context.getCms().getSitePath(target);
        try {
            m_context.getCms().copyResourceToProject(resPath);

            m_context.finish(Collections.singletonList(target.getStructureId()));
        } catch (CmsException e) {
            m_context.error(e);
        }
    }
}
