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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.I_CmsButton;

import com.google.gwt.event.dom.client.ClickEvent;

/**
 * Button to open the list manager for list configuration contents.<p>
 *
 * @since 8.0.0
 */
public class CmsToolbarListManagerButton extends A_CmsToolbarOptionButton {

    /** The list configuration resource type name. */
    private static final String LIST_CONFIG_TYPE = "listconfig";

    /**
     * Constructor.<p>
     *
     * @param handler the container-page handler
     */
    public CmsToolbarListManagerButton(CmsContainerpageHandler handler) {

        super(I_CmsButton.ButtonData.LIST, handler);
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsToolbarOptionButton#isOptionAvailable(org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel)
     */
    @Override
    public boolean isOptionAvailable(CmsContainerPageElementPanel element) {

        return LIST_CONFIG_TYPE.equals(element.getResourceType())
            && element.hasWritePermission()
            && !CmsContainerpageController.get().isEditingDisabled();
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsToolbarOptionButton#onElementClick(com.google.gwt.event.dom.client.ClickEvent, org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel)
     */
    @Override
    public void onElementClick(ClickEvent event, CmsContainerPageElementPanel element) {

        String target = CmsCoreProvider.get().getDefaultWorkplaceLink();
        target += "#!list-management/!!resourceId::"
            + CmsContainerpageController.getServerId(element.getId())
            + "!!locale::"
            + CmsContainerpageController.get().getData().getLocale();
        CmsContainerpageController.get().leaveUnsaved(target);
    }
}