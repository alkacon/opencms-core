/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.components;

import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

public class CmsConfirmationDialog extends CmsBasicDialog {

    private Button m_okButton; /**/
    private Button m_cancelButton;
    private Label m_label;
    private Runnable m_okAction;
    private Runnable m_cancelAction;

    public CmsConfirmationDialog(String message, Runnable okAction, Runnable cancelAction) {
        m_okAction = okAction;

        m_cancelAction = cancelAction;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_label.setValue(message);
        m_okButton.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                m_okAction.run();
            }
        });
        m_cancelButton.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                m_cancelAction.run();

            }
        });
    }

    public static void show(String message, final Runnable okAction) {

        final Window window = new Window("Confirmation");
        CmsConfirmationDialog dialog = new CmsConfirmationDialog(message, new Runnable() {

            public void run() {

                window.close();
                okAction.run();
            }
        }, new Runnable() {

            public void run() {

                window.close();
            }
        });
        window.setContent(dialog);
        window.setWidth("500px");
        window.setModal(true);
        window.setResizable(false);
        A_CmsUI.get().addWindow(window);
    }

    public Label getLabel() {

        return m_label;
    }

    public void setMessage(String message) {

        m_label.setValue(message);
    }

}
