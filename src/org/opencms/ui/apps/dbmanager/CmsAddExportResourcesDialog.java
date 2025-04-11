/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.apps.dbmanager;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;

import java.util.function.Consumer;

import com.vaadin.ui.Button;
import com.vaadin.ui.TextArea;

/**
 * Dialog used to enter a list of paths to be added to the resources in the database export dialog.
 */
public class CmsAddExportResourcesDialog extends CmsBasicDialog {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The text area for entering the paths. */
    protected TextArea m_textArea;

    /** The OK button. */
    protected Button m_okButton;

    /** The cancel button. */
    protected Button m_cancelButton;

    /**
     * Creates a new instance.
     *
     * @param pathListHandler will be called with the entered text if OK is clicked
     */
    public CmsAddExportResourcesDialog(Consumer<String> pathListHandler) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_okButton.addClickListener(e -> {
            pathListHandler.accept(m_textArea.getValue());
            CmsVaadinUtils.closeWindow(CmsAddExportResourcesDialog.this);

        });
        m_cancelButton.addClickListener(e -> CmsVaadinUtils.closeWindow(CmsAddExportResourcesDialog.this));
    }

}
