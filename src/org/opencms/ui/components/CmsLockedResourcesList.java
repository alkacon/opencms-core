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

package org.opencms.ui.components;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.ui.CmsVaadinUtils;

import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Widget used to display a list of locked resources.<p<
 */
public class CmsLockedResourcesList extends CmsBasicDialog {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The label used to display the message. */
    private Label m_messageLabel;

    /** The box containing the individual widgets for locked resources. */
    private VerticalLayout m_resourceBox;

    /** The OK button. */
    private Button m_okButton;

    /** The cancel button. */
    private Button m_cancelButton;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     * @param resources the locked resources to display
     * @param message the message to display
     * @param nextAction the action to execute after clicking the OK button
     * @param cancelAction the action to execute after clicking the Cancel button
     */
    public CmsLockedResourcesList(
        CmsObject cms,
        List<CmsResource> resources,
        String message,
        Runnable nextAction,
        Runnable cancelAction) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_messageLabel.setValue(message);
        if (nextAction != null) {
            m_okButton.addClickListener(CmsVaadinUtils.createClickListener(nextAction));
        } else {
            m_okButton.setVisible(false);
        }

        if (cancelAction != null) {
            m_cancelButton.addClickListener(CmsVaadinUtils.createClickListener(cancelAction));
        } else {
            m_cancelButton.setVisible(false);
        }
        for (CmsResource resource : resources) {
            m_resourceBox.addComponent(new CmsResourceInfo(resource));
        }
    }
}
