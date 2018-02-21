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

package org.opencms.ui.apps.linkvalidation;

import org.opencms.main.OpenCms;
import org.opencms.relations.CmsExternalLinksValidationResult;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.report.CmsReportWidget;

import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the external link validation.<p>
 */
public class CmsLinkValidationExternal extends VerticalLayout {

    /**vaadin serial id.*/
    private static final long serialVersionUID = 4901058101922988640L;

    /**Button to start validation. */
    private Button m_exec;

    /**Label showing last report.*/
    private Label m_oldReport;

    /**Vaadin component. */
    private FormLayout m_threadReport;

    /**
     * constructor.<p>
     */
    protected CmsLinkValidationExternal() {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_oldReport.setContentMode(ContentMode.HTML);
        m_oldReport.setHeight("500px");
        m_oldReport.addStyleName("v-scrollable");
        m_oldReport.addStyleName("o-report");

        CmsExternalLinksValidationResult result = OpenCms.getLinkManager().getPointerLinkValidationResult();
        if (result == null) {
            m_oldReport.setValue(CmsVaadinUtils.getMessageText(Messages.GUI_LINKVALIDATION_NO_VALIDATION_YET_0));
        } else {
            m_oldReport.setValue(
                result.toHtml(OpenCms.getWorkplaceManager().getWorkplaceLocale(A_CmsUI.getCmsObject())));
        }

        m_exec.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -3281073871585942686L;

            public void buttonClick(ClickEvent event) {

                startValidation();
            }
        });
    }

    /**Enables the button to start the validation.<p> */
    void enableButton() {

        m_exec.setEnabled(true);
    }

    /**Starts the validation.<p> */
    void startValidation() {

        m_oldReport.setVisible(false);
        m_threadReport.removeAllComponents();
        CmsExternalLinksValidatorThread thread = new CmsExternalLinksValidatorThread(
            A_CmsUI.getCmsObject(),
            new Runnable() {

                public void run() {

                    enableButton();
                }

            });
        thread.start();
        CmsReportWidget reportWidget = new CmsReportWidget(thread);
        reportWidget.setHeight("500px");
        m_threadReport.addComponent(reportWidget);
        m_exec.setEnabled(false);
    }
}
