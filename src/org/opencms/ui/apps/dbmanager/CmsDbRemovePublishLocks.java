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

package org.opencms.ui.apps.dbmanager;

import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.linkvalidation.I_CmsUpdatableComponent;
import org.opencms.ui.report.CmsReportWidget;

import java.util.List;

import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Remove publish locks.<p>
 */
public class CmsDbRemovePublishLocks extends VerticalLayout implements I_CmsUpdatableComponent {

    /**Vaadin serial id. */
    private static final long serialVersionUID = 2234620713671506530L;

    /**Vaadin component.*/
    private FormLayout m_panel;

    /**Vaadin component.*/
    private Label m_report;

    /**
     * Constructor.<p>
     */
    public CmsDbRemovePublishLocks() {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_report.setHeight("500px");
        m_report.addStyleName("v-scrollable");
        m_report.addStyleName("o-report");
        m_report.setValue(CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_REPAIR_INTRO_0));
    }

    /**
     * @see org.opencms.ui.apps.linkvalidation.I_CmsUpdatableComponent#update(java.util.List)
     */
    public void update(List<String> resources) {

        m_panel.removeAllComponents();
        CmsRemovePubLocksThread thread = new CmsRemovePubLocksThread(A_CmsUI.getCmsObject(), resources);
        thread.start();
        CmsReportWidget widget = new CmsReportWidget(thread);
        widget.setHeight("500px");
        m_panel.addComponent(widget);
    }
}
