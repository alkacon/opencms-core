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

package org.opencms.ui.apps.cacheadmin;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.components.CmsBasicDialog;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;

/**
 * Dialog to confirm flush without any options.<p>
 */
public class CmsConfirmSimpleFlushDialog extends CmsBasicDialog {

    /**Vaadin serial id.*/
    private static final long serialVersionUID = 6454462284178282427L;

    /**Vaadin button.*/
    private Button m_cancelButton;

    /**Vaadin label.*/
    private Label m_icon;

    /**Vaadin label.*/
    private Label m_label;

    /**Vaadin button.*/
    private Button m_okButton;

    /**
     * Public constructor.<p>
     *
     * @param message to be shown
     * @param okAction runnable for ok Button
     * @param closeAction runnable for close Button
     */
    public CmsConfirmSimpleFlushDialog(String message, final Runnable okAction, final Runnable closeAction) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_label.setValue(message);

        m_icon.setContentMode(ContentMode.HTML);
        m_icon.setValue(FontOpenCms.WARNING.getHtml());

        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1336535963768785962L;

            public void buttonClick(ClickEvent event) {

                okAction.run();
            }
        });

        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 3918008642649054392L;

            public void buttonClick(ClickEvent event) {

                closeAction.run();
            }
        });
    }
}
