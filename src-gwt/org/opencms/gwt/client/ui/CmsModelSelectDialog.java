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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.shared.CmsModelResourceInfo;
import org.opencms.util.CmsUUID;

import java.util.List;

/**
 * Dialog to select a resource model for a new resource.<p>
 *
 * @since 8.0.3
 */
public class CmsModelSelectDialog extends A_CmsListItemSelectDialog<CmsModelResourceInfo> {

    /** The handler instance for selecting a model. */
    protected I_CmsModelSelectHandler m_selectHandler;

    /**
     * Constructor.<p>
     *
     * @param selectHandler the handler object for handling model selection
     * @param modelResources the available resource models
     * @param title the title for the model selection dialog
     * @param message the message to display in the model selection dialog
     */
    public CmsModelSelectDialog(
        I_CmsModelSelectHandler selectHandler,
        List<CmsModelResourceInfo> modelResources,
        String title,
        String message) {

        super(modelResources, title, message);
        m_selectHandler = selectHandler;
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsListItemSelectDialog#handleSelection(org.opencms.gwt.shared.CmsListInfoBean)
     */
    @Override
    protected void handleSelection(CmsModelResourceInfo info) {

        CmsUUID structureId = info.getStructureId();
        m_selectHandler.onModelSelect(structureId);
    }

}
