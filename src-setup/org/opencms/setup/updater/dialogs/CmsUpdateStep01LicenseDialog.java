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

package org.opencms.setup.updater.dialogs;

import org.opencms.setup.CmsUpdateUI;
import org.opencms.ui.CmsVaadinUtils;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.ui.CheckBox;

/**
 * License dialog.<p>
 */
public class CmsUpdateStep01LicenseDialog extends A_CmsUpdateDialog {

    /**vaadin serial id. */
    private static final long serialVersionUID = 1L;

    /**vaadin component.*/
    VerticalLayout m_licenseContainer;

    /**vaadin component.*/
    CheckBox m_accept;

    /**
     * @see org.opencms.setup.updater.dialogs.A_CmsUpdateDialog#init(org.opencms.setup.CmsUpdateUI)
     */
    @Override
    public boolean init(CmsUpdateUI ui) {

        CmsVaadinUtils.readAndLocalizeDesign(this, null, null);
        super.init(ui, false, true);
        setCaption("OpenCms Update-Wizard - License Agreement");

        Label label = htmlLabel(readSnippet("license.html"));
        label.setWidth("100%");

        m_licenseContainer.addComponent(label);

        m_accept.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                enableOK(m_accept.getValue().booleanValue());
            }
        });

        enableOK(false);
        return true;
    }

    /**
     * @see org.opencms.setup.updater.dialogs.A_CmsUpdateDialog#getNextDialog()
     */
    @Override
    A_CmsUpdateDialog getNextDialog() {

        return new CmsUpdateStep02DBDialog();
    }

    /**
     * @see org.opencms.setup.updater.dialogs.A_CmsUpdateDialog#getPreviousDialog()
     */
    @Override
    A_CmsUpdateDialog getPreviousDialog() {

        return null;
    }

}
