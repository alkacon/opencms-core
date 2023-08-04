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

import org.opencms.ade.galleries.shared.CmsResourceTypeBean;
import org.opencms.file.CmsResource;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.dialogs.A_CmsSelectResourceTypeDialog;

import com.vaadin.annotations.DesignRoot;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the resource type select dialog.<p>
 */
@DesignRoot
public class CmsSelectResourceTypeDialog extends A_CmsSelectResourceTypeDialog {

    /**vaadin serial id.*/
    private static final long serialVersionUID = -6944584336945436950L;

    /** The cancel button. */
    protected Button m_cancelButton;

    /** Container for the type list. */
    protected VerticalLayout m_typeContainer;

    /** Element view selector. */
    protected ComboBox m_viewSelector;

    protected Button m_modeToggle = new Button();

    /**
     * public constructor.<p>
     *
     * @param folderResource resource
     * @param context dialog context
     */
    public CmsSelectResourceTypeDialog(CmsResource folderResource, I_CmsDialogContext context) {

        super(folderResource, context);
    }

    /**
     * @see org.opencms.ui.dialogs.A_CmsSelectResourceTypeDialog#getCancelButton()
     */
    @Override
    public Button getCancelButton() {

        return m_cancelButton;
    }

    @Override
    public Button getModeToggle() {

        return m_modeToggle;
    }

    /**
     * Returns selected resource type.<p>
     *
     * @return resource type name
     */
    public String getSelectedResource() {

        return m_selectedType == null ? "" : m_selectedType.getType();
    }

    /**
     * @see org.opencms.ui.dialogs.A_CmsSelectResourceTypeDialog#getVerticalLayout()
     */
    @Override
    public VerticalLayout getVerticalLayout() {

        return m_typeContainer;
    }

    /**
     * @see org.opencms.ui.dialogs.A_CmsSelectResourceTypeDialog#getViewSelector()
     */
    @Override
    public ComboBox getViewSelector() {

        return m_viewSelector;
    }

    /**
     * @see org.opencms.ui.dialogs.A_CmsSelectResourceTypeDialog#handleSelection(org.opencms.ade.galleries.shared.CmsResourceTypeBean)
     */
    @Override
    public void handleSelection(CmsResourceTypeBean selectedType) {

        m_selectedType = selectedType;
        finish(null);
    }

    /**
     * @see org.opencms.ui.dialogs.A_CmsSelectResourceTypeDialog#useDefault()
     */
    @Override
    public boolean useDefault() {

        return false;
    }
}
