/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/ui/Attic/CmsLeavePageDialog.java,v $
 * Date   : $Date: 2010/10/13 05:56:47 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsPopupDialog;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;

/**
 * Dialog to prevent the user from leaving the page unsaved.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsLeavePageDialog extends CmsPopupDialog {

    /** The save button. */
    CmsPushButton m_saveButton;

    /** The cancel button. */
    CmsPushButton m_cancelButton;

    /**
     * Constructor.<p>
     * 
     * @param controller the sitemap controller
     * @param sitePath the target path
     */
    public CmsLeavePageDialog(final CmsSitemapController controller, final String sitePath) {

        super();
        Label content = new Label(org.opencms.ade.sitemap.client.Messages.get().key(
            org.opencms.ade.sitemap.client.Messages.GUI_CONFIRM_DIRTY_LEAVING_0));
        setContent(content);
        this.setText(org.opencms.ade.sitemap.client.Messages.get().key(
            org.opencms.ade.sitemap.client.Messages.GUI_CONFIRM_LEAVING_TITLE_0));
        this.setGlassEnabled(true);
        m_saveButton = new CmsPushButton();
        m_saveButton.setTitle(Messages.get().key(Messages.GUI_SAVE_0));
        m_saveButton.setText(Messages.get().key(Messages.GUI_SAVE_0));
        m_saveButton.setSize(I_CmsButton.Size.small);
        m_saveButton.setUseMinWidth(true);
        m_saveButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                disableButtons();
                controller.saveAndLeavePage(sitePath);
            }
        });
        m_cancelButton = new CmsPushButton();
        m_cancelButton.setTitle(Messages.get().key(Messages.GUI_CANCEL_0));
        m_cancelButton.setText(Messages.get().key(Messages.GUI_CANCEL_0));
        m_cancelButton.setSize(I_CmsButton.Size.small);
        m_cancelButton.setUseMinWidth(true);
        m_cancelButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                disableButtons();
                hide();
            }
        });

        addButton(m_cancelButton);
        addButton(m_saveButton);
    }

    /**
     * Disables all dialog buttons.<p>
     */
    protected void disableButtons() {

        m_saveButton.setEnabled(false);
        m_cancelButton.setEnabled(false);
    }
}
