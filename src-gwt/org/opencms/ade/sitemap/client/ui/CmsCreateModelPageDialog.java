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

package org.opencms.ade.sitemap.client.ui;

import org.opencms.ade.sitemap.client.control.CmsSitemapController;

/**
 * Dialog for creating new model pages.<p>
 */
public class CmsCreateModelPageDialog extends A_CmsNewModelPageDialog {

    /** The controller. */
    private CmsSitemapController m_controller;

    /** 
     * Constructor.<p>
     * 
     * @param controller the controller
     */
    public CmsCreateModelPageDialog(CmsSitemapController controller) {

        super(org.opencms.ade.sitemap.client.Messages.get().key(
            org.opencms.ade.sitemap.client.Messages.GUI_CREATE_MODEL_PAGE_DIALOG_TITLE_0), null);
        m_controller = controller;
    }

    /**
     * Creates the new gallery folder.<p>
     */
    @Override
    protected void onOk() {

        m_controller.createNewModelPage(
            m_titleInput.getFormValueAsString(),
            m_descriptionInput.getFormValueAsString(),
            null);
        hide();
    }

}
