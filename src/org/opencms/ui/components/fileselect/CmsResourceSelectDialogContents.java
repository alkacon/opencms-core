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

package org.opencms.ui.components.fileselect;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Contents of the resource select dialog, filled using the declarative layout mechanism.<p>
 */
public class CmsResourceSelectDialogContents extends CmsBasicDialog {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Panel for additional widgets to be displayed. */
    protected FormLayout m_additionalWidgets;

    /** The whole content. */
    private VerticalLayout m_container;

    /** The site selector. */
    private ComboBox m_siteSelector;

    /** Container for the tree component. */
    private VerticalLayout m_treeContainer;

    /**
     * Creates a new widget instance.<p>
     */
    public CmsResourceSelectDialogContents() {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

    }

    /**
     * Gets the panel for additional widgets.
     *
     * @return the panel for additional widgets
     */
    public FormLayout getAdditionalWidgets() {

        return m_additionalWidgets;
    }

    /**
     * Returns the content container.<p>
     *
     * @return the content container
     */
    public VerticalLayout getContainer() {

        return m_container;
    }

    /**
     * Gets the site selector.<p>
     *
     * @return the site selector
     */
    public ComboBox getSiteSelector() {

        return m_siteSelector;
    }

    /**
     * Gets the tree data container.<p>
     *
     * @return the tree data container
     */
    public VerticalLayout getTreeContainer() {

        return m_treeContainer;
    }

}
