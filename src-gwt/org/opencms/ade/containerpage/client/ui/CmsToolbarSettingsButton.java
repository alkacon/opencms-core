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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.I_CmsButton;

import com.google.gwt.event.dom.client.ClickEvent;

/**
 * The properties button holding all properties related methods.<p>
 *
 * @since 8.0.0
 */
public class CmsToolbarSettingsButton extends A_CmsToolbarOptionButton {

    /**
     * Constructor.<p>
     *
     * @param handler the container-page handler
     */
    public CmsToolbarSettingsButton(CmsContainerpageHandler handler) {

        super(I_CmsButton.ButtonData.SETTINGS_BUTTON, handler);
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsToolbarOptionButton#isOptionAvailable(org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel)
     */
    @Override
    public boolean isOptionAvailable(CmsContainerPageElementPanel element) {

        boolean hasValidId;
        try {
            element.getStructureId();
            hasValidId = true;
        } catch (Throwable t) {
            hasValidId = false;
        }
        boolean disableButtons = CmsContainerpageController.get().isEditingDisabled();
        boolean useTemplateContexts = CmsContainerpageController.get().getData().getTemplateContextInfo().shouldShowElementTemplateContextSelection();
        boolean isGroupContainer = element instanceof CmsGroupContainerElementPanel;
        return hasValidId
            && (useTemplateContexts || (element.hasSettings() || CmsCoreProvider.get().getUserInfo().isDeveloper()))
            && !element.getParentTarget().isDetailView()
            && !disableButtons
            && !isGroupContainer;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsToolbarOptionButton#onElementClick(com.google.gwt.event.dom.client.ClickEvent, org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel)
     */
    @Override
    public void onElementClick(ClickEvent event, CmsContainerPageElementPanel element) {

        getHandler().editElementSettings(element);
    }
}
