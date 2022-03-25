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
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.report.CmsReportWidget;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the synchronization dialog.<p>
 */
public class CmsDbSynchDialog extends CmsBasicDialog {

    /**vaadin serial id.*/
    private static final long serialVersionUID = -1818182416175306822L;

    /**cancel button.*/
    Button m_cancelButton;

    /**Runnable for close action.*/
    Runnable m_closeRunnable;

    /**Vaadin component.*/
    HorizontalLayout m_confirm;

    /**icon. */
    Label m_icon;

    /**Ok button. */
    Button m_okButton;

    /**Vaadin component. */
    VerticalLayout m_report;

    /**
     * public constructor.<p>
     *
     * @param closeRunnable gets called on cancel
     */
    public CmsDbSynchDialog(Runnable closeRunnable) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_report.setVisible(false);
        m_closeRunnable = closeRunnable;

        //Setup icon
        m_icon.setContentMode(ContentMode.HTML);
        m_icon.setValue(FontOpenCms.WARNING.getHtml());

        m_okButton.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = 7329358907650680436L;

            public void buttonClick(ClickEvent event) {

                showReport();

            }
        });

        m_cancelButton.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = 4143312166220501488L;

            public void buttonClick(ClickEvent event) {

                m_closeRunnable.run();

            }
        });
    }

    /**
     * shows the synchronization report.<p>
     */
    protected void showReport() {

        m_confirm.setVisible(false);
        m_report.setVisible(true);
        CmsSynchronizeThread thread = new CmsSynchronizeThread(A_CmsUI.getCmsObject());
        thread.start();
        CmsReportWidget widget = new CmsReportWidget(thread);
        widget.setHeight("100%");
        widget.setWidth("100%");
        m_report.addComponent(widget);
        m_okButton.setVisible(false);
        m_cancelButton.setCaption(CmsVaadinUtils.getMessageText(org.opencms.ui.Messages.GUI_BUTTON_CLOSE_DIALOG_0));
    }
}
