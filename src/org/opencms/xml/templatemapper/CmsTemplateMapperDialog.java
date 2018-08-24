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

package org.opencms.xml.templatemapper;

import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.report.CmsReportWidget;

import com.vaadin.ui.Button;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Dialog for running the template mapper report thread.<p>
 */
public class CmsTemplateMapperDialog extends CmsBasicDialog {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The start button. */
    private Button m_okButton;

    /** The report thread. */
    private A_CmsReportThread m_report;

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     */
    public CmsTemplateMapperDialog(I_CmsDialogContext context) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        displayResourceInfo(context.getResources());
        m_okButton.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_TEMPLATEMAPPER_START_0));
        VerticalLayout content = new VerticalLayout();
        content.setWidth("100%");
        content.setHeight("500px");
        setContent(content);
        m_okButton.addClickListener(event -> {
            CmsReportWidget reportWidget = new CmsReportWidget(m_report);
            reportWidget.setWidth("100%");
            reportWidget.setHeight("100%");
            content.addComponent(reportWidget);
            m_okButton.setEnabled(false);
            m_report.start();

        });
    }

    /**
     * Sets the report thread.<p>
     *
     * @param report the report thread
     */
    public void setReportThread(A_CmsReportThread report) {

        m_report = report;

    }

}
