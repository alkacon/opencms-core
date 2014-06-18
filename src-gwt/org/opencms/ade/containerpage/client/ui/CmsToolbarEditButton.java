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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.event.dom.client.ClickEvent;

/**
 * The edit button holding all edit related methods.<p>
 * 
 * @since 8.0.0
 */
public class CmsToolbarEditButton extends A_CmsToolbarOptionButton {

    /**
     * Constructor.<p>
     * 
     * @param handler the container-page handler
     */
    public CmsToolbarEditButton(CmsContainerpageHandler handler) {

        super(I_CmsButton.ButtonData.EDIT, handler);
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsToolbarOptionButton#createOptionForElement(org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel)
     */
    @Override
    public CmsElementOptionButton createOptionForElement(CmsContainerPageElementPanel element) {

        CmsElementOptionButton button = super.createOptionForElement(element);
        button.setImageClass(I_CmsButton.ButtonData.SELECTION.getIconClass());
        button.addStyleName(I_CmsButton.ButtonData.SELECTION.getIconClass());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(element.getNoEditReason())) {
            if (element.hasWritePermission()
                && !((element instanceof CmsGroupContainerElementPanel) && ((CmsGroupContainerElementPanel)element).isInheritContainer())) {
                // if the user has write permissions, the lock report dialog will be accessible through this button
                button.setImageClass(I_CmsButton.ButtonData.SELECTION.getIconClass()
                    + " "
                    + I_CmsLayoutBundle.INSTANCE.containerpageCss().lockedElement());
                button.setTitle(element.getNoEditReason());
            } else {
                button.disable(element.getNoEditReason());
            }
        }
        return button;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsToolbarOptionButton#onElementClick(com.google.gwt.event.dom.client.ClickEvent, org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel)
     */
    @Override
    public void onElementClick(ClickEvent event, CmsContainerPageElementPanel element) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(element.getNoEditReason())) {
            openEditor(element);
        } else {
            openLockReport(element);
        }
        event.stopPropagation();
        event.preventDefault();

    }

    /**
     * Opens the element editor.<p>
     * 
     * @param element the element
     */
    private void openEditor(CmsContainerPageElementPanel element) {

        getHandler().openEditorForElement(element, false);
    }

    /**
     * Opens the lock report for locked elements.<p>
     * 
     * @param element the element
     */
    private void openLockReport(CmsContainerPageElementPanel element) {

        getHandler().openLockReportForElement(element);
    }
}
